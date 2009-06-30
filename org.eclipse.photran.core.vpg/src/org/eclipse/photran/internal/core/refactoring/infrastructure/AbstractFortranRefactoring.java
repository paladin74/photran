/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring.infrastructure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.photran.core.FortranAST;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.util.IterableWrapper;
import org.eclipse.photran.core.vpg.util.OffsetLength;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTContainsStmtNode;
import org.eclipse.photran.internal.core.parser.ASTImplicitStmtNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTUseStmtNode;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.parser.IProgramUnit;
import org.eclipse.photran.internal.core.parser.ISpecificationPartConstruct;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.text.edits.ReplaceEdit;

import bz.over.vpg.TokenRef;
import bz.over.vpg.VPGLog;

/**
 * Superclass for all refactorings in Photran.
 * <p>
 * In addition to implementing the LTK refactoring interface, this class provides a number of methods to subclasses,
 * including methods to display error messages, find enclosing nodes in an AST, check identifier tokens, etc.
 * 
 * @author Jeff Overbey, Timofey Yuvashev
 */
public abstract class AbstractFortranRefactoring extends Refactoring
{
    // Preconditions toward bottom of file
    
    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////
    
	/** The OS-dependent end-of-line sequence (\n or \r\n) */
	protected static final String EOL = System.getProperty("line.separator");
	
    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////
    
    protected PhotranVPG vpg;
    protected CompositeChange allChanges = null;
    
    ///////////////////////////////////////////////////////////////////////////
    // LTK Refactoring Implementation
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public final RefactoringStatus checkInitialConditions(IProgressMonitor pm)
    {
        RefactoringStatus status = new RefactoringStatus();
        
        status.addWarning("C preprocessor directives are IGNORED by the refactoring engine.  Use at your own risk.");
        
        pm.beginTask("Ensuring virtual program graph is up-to-date", IProgressMonitor.UNKNOWN);
        vpg.ensureVPGIsUpToDate(pm);
       	pm.done();
        
       	status = getAbstractSyntaxTree(status);
       	
       	if(status.hasFatalError())
       	    return status;
        
        pm.beginTask("Checking initial preconditions", IProgressMonitor.UNKNOWN);
        try
        {
        	doCheckInitialConditions(status, pm);
        }
        catch (PreconditionFailure f)
        {
        	status.addFatalError(f.getMessage());
        }
        pm.done();
        
        return status;
    }

    protected abstract RefactoringStatus getAbstractSyntaxTree(RefactoringStatus status);
  
    protected abstract void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure;

    protected void logVPGErrors(RefactoringStatus status)
    {
		for (VPGLog<Token, PhotranTokenRef>.Entry entry : vpg.log.getEntries())
		{
			if (entry.isWarning())
				status.addWarning(entry.getMessage(), createContext(entry.getTokenRef()));
			else
				status.addError(entry.getMessage(), createContext(entry.getTokenRef()));
		}
	}

	@Override
    public final RefactoringStatus checkFinalConditions(IProgressMonitor pm)
    {
        allChanges = new CompositeChange(getName());
        
		RefactoringStatus status = new RefactoringStatus();
		pm.beginTask("Checking final preconditions; please wait...", IProgressMonitor.UNKNOWN);
        try
        {
        	doCheckFinalConditions(status, pm);
        }
        catch (PreconditionFailure f)
        {
        	status.addFatalError(f.getMessage());
        }
        pm.done();
        return status;
    }

	protected abstract void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure;

    @Override
    public final Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
    	assert pm != null;
    	
        pm.beginTask("Constructing workspace transformation; please wait...", IProgressMonitor.UNKNOWN);
        // allChanges constructed above in #checkFinalConditions
        doCreateChange(pm);
        pm.done();
        return allChanges;
    }

    protected abstract void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException;

    ///////////////////////////////////////////////////////////////////////////
    // Utilities for Subclasses
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A <code>PreconditionFailure</code> is thrown (e.g., by {@link AbstractFortranRefactoring#fail(String)})
     * to indicate an error severe enough that the refactoring cannot be completed.
     */
    protected static class PreconditionFailure extends Exception
    {
		private static final long serialVersionUID = 1L;

		public PreconditionFailure(String message)
    	{
    		super(message);
    	}
    }
    
    /**
     * Throws a <code>PreconditionFailure</code>, indicating an error severe enough
     * that the refactoring cannot be completed.
     * 
     * @param message an error message to display to the user
     */
    protected void fail(String message) throws PreconditionFailure
    {
    	throw new PreconditionFailure(message);
    }
    
    // CODE EXTRACTION ////////////////////////////////////////////////////////
    
    /**
     * Parses the given Fortran statement.
     * <p>
     * Internally, <code>string</code> is embedded into the following program
     * <pre>
     * program p
     *   (string is placed here)
     * end program
     * </pre>
     * which is parsed and its body extracted and returned,
     * so <code>string</code> must "make sense" (syntactically) in this context.
     * No semantic analysis is done; it is only necessary that the
     * program be syntactically correct.
     */
    protected IBodyConstruct parseLiteralStatement(String string)
    {
        return parseLiteralStatementSequence(string).get(0);
    }
    
    /**
     * Parses the given list of Fortran statements.
     * <p>
     * @see parseLiteralStatement
     */
    protected IASTListNode<IBodyConstruct> parseLiteralStatementSequence(String string)
    {
        string = "program p\n" + string + "\nend program";
        return ((ASTMainProgramNode)parseLiteralProgramUnit(string)).getBody();
    }

    /** @return a CONTAINS statement */
    protected ASTContainsStmtNode createContainsStmt()
    {
        String string = "program p\ncontains\nsubroutine s\nend subroutine\nend program";
        return ((ASTMainProgramNode)parseLiteralProgramUnit(string)).getContainsStmt();
    }

    /**
     * Parses the given Fortran program unit.
     * <p>
     * No semantic analysis is done; it is only necessary that the
     * program unit be syntactically correct.
     */
    protected IProgramUnit parseLiteralProgramUnit(String string)
    {
        try
        {
            IAccumulatingLexer lexer = LexerFactory.createLexer(
                new ByteArrayInputStream(string.getBytes()), null, "(none)",
                SourceForm.UNPREPROCESSED_FREE_FORM, true);
            if (parser == null) parser = new Parser();
            
            FortranAST ast = new FortranAST(null, parser.parse(lexer), lexer.getTokenList());
            return ast.getRoot().getProgramUnitList().get(0);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }
    private Parser parser = null;

//    /**
//     * Parses the given Fortran expression.
//     * <p>
//     * Internally, <code>string</code> is embedded into the following program
//     * <pre>
//     * program p
//     *   x = (string is placed here)
//     * end program
//     * </pre>
//     * which is parsed and the resulting expression extracted and returned,
//     * so <code>string</code> must "make sense" (syntactically) in this context.
//     * No semantic analysis is done; it is only necessary that the
//     * program be syntactically correct.
//     */
//    protected ASTExpressionNode parseLiteralExpression(String string)
//    {
//        IBodyConstruct stmt = parseLiteralStatement("x = " + string);
//        return ((ASTAssignmentStmtNode)stmt).getExpr();
//    }

    // USER INTERACTION ///////////////////////////////////////////////////////
    
    protected String describeToken(Token token)
    {
        return "\"" + token.getText() + "\" " + this.describeTokenPos(token);
    }
    
    protected String describeTokenPos(Token token)
    {
        return "(line " + token.getLine() + ", column " + token.getCol() + ")";
    }

    // REFACTORING STATUS /////////////////////////////////////////////////////

	protected RefactoringStatusContext createContext(TokenRef<Token> tokenRef)
	{
		if (tokenRef == null) return null;
		
		IFile file = PhotranVPG.getIFileForFilename(tokenRef.getFilename());
		if (file == null) return null;
		
		return new FileStatusContext(file,
		                             new Region(tokenRef.getOffset(), tokenRef.getLength()));
	}

    // CHANGE CREATION ////////////////////////////////////////////////////////

	/**
	 * This method should be called from within the <code>doCreateChange</code>
	 * method after all of the changes to a file's AST have been made.
	 */
    protected void addChangeFromModifiedAST(IFile file, IProgressMonitor pm)
    {
        try
        {
            IFortranAST ast = vpg.acquireTransientAST(file);
            TextFileChange changeThisFile = new TextFileChange(getName() + " - " + file.getFullPath().toOSString(), file);
            changeThisFile.initializeValidationData(pm);
            changeThisFile.setEdit(new ReplaceEdit(0, getSizeOf(file), SourcePrinter.getSourceCodeFromAST(ast)));
            allChanges.add(changeThisFile);
            //FortranWorkspace.getInstance().releaseTU(file);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    private int getSizeOf(IFile file) throws CoreException, IOException
    {
        int size = 0;
        InputStream in = file.getContents();
        while (in.read() > -1)
            size++;
        return size;
    }
    
    // TEXT<->TREE MAPPING ////////////////////////////////////////////////////
    
    protected Token findEnclosingToken(IFortranAST ast, final ITextSelection selection)
    {
        Token prevToken = null;
        for (Token token : new IterableWrapper<Token>(ast))
        {
        	if (OffsetLength.contains(token.getFileOffset(), token.getLength(),
        	                          selection.getOffset(), selection.getLength()))
        	{
                String tokenText = token.getText();
                //If we get whitespace, that means we want the previous token (cursor was put AFTER
                // the identifier we want to rename
                if(tokenText.length() == 1 && Character.isWhitespace(tokenText.charAt(0)))
                {
                    return prevToken;
                }
                else
                    return token;
        	}
        	prevToken = token;
        }
        return null;
    }
    
    protected IASTNode findEnclosingNode(IFortranAST ast, ITextSelection selection)
    {
        Token firstToken = this.findFirstTokenAfter(ast, selection.getOffset());
        Token lastToken = this.findLastTokenBefore(ast, OffsetLength.getPositionPastEnd(selection.getOffset(), selection.getLength()));
        if (firstToken == null || lastToken == null) return null;

        for (IASTNode parent = lastToken.getParent(); parent != null; parent = parent.getParent())
            if (contains(parent, firstToken))
                return parent;
        
        return null;
    }
    
    protected boolean nodeExactlyEnclosesRegion(IASTNode parent, Token firstToken, Token lastToken)
    {
        return parent.findFirstToken() == firstToken && parent.findLastToken() == lastToken;
    }
    
    protected boolean nodeExactlyEnclosesRegion(IASTNode node, IFortranAST ast, ITextSelection selection)
    {
        Token firstInNode = node.findFirstToken();
        Token lastInNode = node.findLastToken();
        
        Token firstInSel = this.findFirstTokenAfter(ast, selection.getOffset());
        Token lastInSel = this.findLastTokenBefore(ast, OffsetLength.getPositionPastEnd(selection.getOffset(), selection.getLength()));
        
        return firstInNode != null
            && lastInNode != null
            && firstInSel != null
            && lastInSel != null
            && firstInNode == firstInSel
            && lastInNode == lastInSel;
    }

//    protected IASTNode findEnclosingNode(IFortranAST ast, ITextSelection selection, Nonterminal nodeType, boolean allowNesting)
//    {
//        IASTNode smallestEnclosure = findEnclosingNode(ast, selection);
//        if (smallestEnclosure == null) return null;
//        
//        for (IASTNode n = smallestEnclosure; n != null; n = n.getParent())
//        {
//            if (n.getNonterminal() == nodeType)
//            {
//                if (allowNesting)
//                    return n;
//                else if (n.getParent() == null)
//                    return null;
//                else if (n.getParent().getNonterminal() != nodeType)
//                    return n;
//                else if (n.getParent().getNonterminal() == nodeType)
//                    continue;
//            }
//        }
//        
//        return null;
//    }
    
    private boolean contains(IASTNode target, Token token)
    {
        for (IASTNode node = token.getParent(); node != null; node = node.getParent())
            if (node == target)
                return true;
        return false;
    }

    private Token findFirstTokenAfter(IFortranAST ast, final int targetFileOffset)
    {
        for (Token token : new IterableWrapper<Token>(ast))
            if (token.isOnOrAfterFileOffset(targetFileOffset))
                return token;
        return null;
    }
    
    private Token findLastTokenBefore(IFortranAST ast, final int targetFileOffset)
    {
        Token previousToken = null;
        for (Token token : new IterableWrapper<Token>(ast))
        {
            if (token.isOnOrAfterFileOffset(targetFileOffset))
                return previousToken;
            else
                previousToken = token;
        }
        return null;
    }

    protected static class StatementSequence
    {
        public final ScopingNode enclosingScope;
        public final IASTListNode<? extends IASTNode> listContainingStmts;
        public final int startIndex;
        public final int endIndex;
        public final List<IASTNode> selectedStmts;

        private StatementSequence(ScopingNode enclosingScope, IASTListNode<? extends IASTNode> body, int startIndex, int endIndex)
        {
            this.enclosingScope = enclosingScope;
            this.listContainingStmts = body;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            
//            this.precedingStmts = new ArrayList<IASTNode>();
//            for (int i = 1; i < startIndex; i++)
//                this.precedingStmts.add(body.get(i));
            
            this.selectedStmts = new ArrayList<IASTNode>();
            for (int i = startIndex; i <= endIndex; i++)
                this.selectedStmts.add(body.get(i));
            
//            this.followingStmts = new ArrayList<IASTNode>();
//            for (int i = endIndex + 1; i < body.size(); i++)
//                this.followingStmts.add(body.get(i));
        }

        public IASTNode firstStmt()
        {
            return selectedStmts.get(0);
        }

        public Token firstToken()
        {
            return firstStmt().findFirstToken();
        }

        public IASTNode lastStmt()
        {
            return selectedStmts.get(selectedStmts.size()-1);
        }

        public Token lastToken()
        {
            return lastStmt().findLastToken();
        }
    }
    
    @SuppressWarnings("unchecked")
    protected StatementSequence findEnclosingStatementSequence(IFortranAST ast, ITextSelection selection)
    {
        Token firstToken = this.findFirstTokenAfter(ast, selection.getOffset());
        Token lastToken = this.findLastTokenBefore(ast, selection.getOffset()+selection.getLength());
        if (firstToken == null || lastToken == null) return null;

        IASTListNode<? extends IASTNode> listContainingFirstToken = firstToken.findNearestAncestor(IASTListNode.class);
        IASTListNode<? extends IASTNode> listContainingLastToken = lastToken.findNearestAncestor(IASTListNode.class);
        if (listContainingFirstToken == null || listContainingLastToken == null || listContainingFirstToken != listContainingLastToken) return null;

        IASTListNode<? extends IASTNode> listContainingStmts = listContainingFirstToken;
        int startIndex = -1;
        int endIndex = -1;
        for (int i = 0; i < listContainingStmts.size(); i++)
        {
            IASTNode node = listContainingStmts.get(i);
            if (contains(node, firstToken))
                startIndex = i;
            if (contains(node, lastToken))
                endIndex = i;
        }
        if (startIndex < 0 || endIndex < 0 || endIndex < startIndex)
            throw new Error("INTERNAL ERROR: Unable to locate selected statements in IASTListNode");
        
        return new StatementSequence(
            listContainingStmts.findNearestAncestor(ScopingNode.class),
            listContainingStmts,
            startIndex,
            endIndex);
    }

//    private IASTListNode<? extends IASTNode> findEnclosingBodyNode(Token token)
//    {
//        ScopingNode scope = token.findNearestAncestor(ScopingNode.class);
//        return scope == null ? null : scope.getBody();
//    }
//
//    private boolean isBodyNode(IASTNode currentNode)
//    {
//        return currentNode instanceof ASTBodyNode;
//    }
//
//    private boolean isNestedBodyNode(IASTNode currentNode)
//    {
//        return isBodyNode(currentNode)
//               && currentNode.getParent() != null
//               && currentNode.getParent() instanceof ASTBodyNode;
//    }

    protected int findIndexToInsertTypeDeclaration(IASTListNode<? extends IASTNode> body)
    {
        IASTNode node = null;
        Iterator<? extends IASTNode> iterator = body.iterator();
        while(iterator.hasNext())
        {
            node = iterator.next();
            if (!(node instanceof ASTUseStmtNode) && !(node instanceof ASTImplicitStmtNode))
            {
                break;
            }
        }
        //If there are no other nodes besides use statements and implicit none, then increment the index
        if (node instanceof ASTUseStmtNode || node instanceof ASTImplicitStmtNode)
        {
            return body.indexOf(node) + 1; 
        }else
        {
            return body.indexOf(node);            
        }
    }

    protected int findIndexToInsertStatement(IASTListNode<? extends IASTNode> body)
    {
        IASTNode node = null;
        Iterator<? extends IASTNode> iterator = body.iterator();
        while(iterator.hasNext())
        {
            node = iterator.next();
            if (!(node instanceof ISpecificationPartConstruct))
            {
                break;
            }
        }
        //If there are no other nodes besides those that implement ISpecificationPartConstruct, 
        //then increment the index
        if (node instanceof ISpecificationPartConstruct)
        {
            return body.indexOf(node) + 1; 
        }else
        {
            return body.indexOf(node);            
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    //
    // P R E C O N D I T I O N S
    //
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    protected abstract void ensureProjectHasRefactoringEnabled(RefactoringStatus status) throws PreconditionFailure;
    
    protected boolean isIdentifier(Token token)
    {
        return token != null && token.getTerminal() == Terminal.T_IDENT;
    }

    protected boolean isPreprocessed(Token token)
    {
        return token.getPreprocessorDirective() != null;
    }

    protected boolean isValidIdentifier(String name)
    {
        return Pattern.matches("[A-Za-z][A-Za-z0-9_]*", name);
    }
    
    protected boolean isBoundIdentifier(Token t)
    {
        return isIdentifier(t) && !t.resolveBinding().isEmpty();
    }
    
    protected boolean isUniquelyDefinedIdentifer(Token t)
    {
        return isBoundIdentifier(t) && t.resolveBinding().size() == 1;
    }
}
