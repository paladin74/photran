package org.eclipse.photran.internal.core.lexer;

import org.eclipse.core.resources.IFile;

/**
 * Enumerates the terminal symbols in the grammar being parsed
 */
public interface IToken
{
    ///////////////////////////////////////////////////////////////////////////
    // Accessor/Mutator Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the Terminal that this token is an instance of
     */
    public Terminal getTerminal();

    /**
     * Sets the Terminal that this token is an instance of
     */
    public void setTerminal(Terminal value);

    /**
     * Returns the token text
     */
    public String getText();

    /**
     * Sets the token text
     */
    public void setText(String value);

    /**
     * Returns whitespace and whitetext appearing before this token that should be associated with this token
     */
    public String getWhiteBefore();

    /**
     * Sets whitespace and whitetext appearing before this token that should be associated with this token
     */
    public void setWhiteBefore(String value);

    /**
     * Returns whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    public String getWhiteAfter();

    /**
     * Sets whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    public void setWhiteAfter(String value);
//
//    public String getPreprocessorDirective()
//    {
//        return preprocessorDirective;
//    }
//
//    public void setPreprocessorDirective(String preprocessorDirective)
//    {
//        this.preprocessorDirective = preprocessorDirective;
//    }

    public int getLine();
    public void setLine(int line);
    
    public int getCol();
    public void setCol(int col);

    public IFile getFile();
    public void setFile(IFile file);
    
    public int getFileOffset();
    public void setFileOffset(int fileOffset);

    public int getStreamOffset();
    public void setStreamOffset(int streamOffset);

    public int getLength();
    public void setLength(int length);
    
//    public Object getBinding()
//    {
//        return binding;
//    }
//    
//    public void setBinding(Object binding)
//    {
//        this.binding = binding;
//    }
//    
//    public Object getScope()
//    {
//        return scope;
//    }
//    
//    public void setScope(Object scope)
//    {
//        this.scope = scope;
//    }
//
//    public boolean containsFileOffset(OffsetLength other)
//    {
//        return OffsetLength.contains(fileOffset, length, other);
//    }
//    
//    public boolean isOnOrAfterFileOffset(int targetOffset)
//    {
//        return fileOffset >= targetOffset;
//    }
//
//    public boolean containsStreamOffset(OffsetLength other)
//    {
//        return OffsetLength.contains(streamOffset, length, other);
//    }
//    
//    public boolean isOnOrAfterStreamOffset(int targetOffset)
//    {
//        return streamOffset >= targetOffset;
//    }
//
//    ///////////////////////////////////////////////////////////////////////////
//    // Visitor Support
//    ///////////////////////////////////////////////////////////////////////////
//
//    @Override protected void visitTopDownUsing(ASTVisitor visitor, boolean shouldVisitRoot)
//    {
//        if (shouldVisitRoot)
//            visitor.visitToken(this);
//    }
//
//    @Override protected void visitBottomUpUsing(ASTVisitor visitor, boolean shouldVisitRoot)
//    {
//        if (shouldVisitRoot)
//            visitor.visitToken(this);
//    }
//
//    public void visitUsing(GenericParseTreeVisitor visitor)
//    {
//        visitor.visitToken(this);
//    }
//
//    ///////////////////////////////////////////////////////////////////////////
//    // Debugging Output
//    ///////////////////////////////////////////////////////////////////////////
//    
//    public String toString(int numSpaces) { return indent(numSpaces) + getDescription() + "\n"; }
//
//    /**
//     * Returns a string describing the token
//     */
//    public String getDescription() { return terminal.toString() + ": \"" + text + "\""; }
//    
//    ///////////////////////////////////////////////////////////////////////////
//    // Source Code Reproduction
//    ///////////////////////////////////////////////////////////////////////////
//    
//    public String printOn(PrintStream out, String currentPreprocessorDirective)
//    {
//        if (this.preprocessorDirective != currentPreprocessorDirective)
//        {
//            if (this.preprocessorDirective != null)
//            {
//                out.print(whiteBefore);
//                out.print(this.preprocessorDirective);
//            }
//            currentPreprocessorDirective = this.preprocessorDirective;
//        }
//        
//        if (currentPreprocessorDirective == null && this.preprocessorDirective == null)
//        {
//            out.print(whiteBefore);
//            out.print(text);
//            out.print(whiteAfter);
//        }
//        
//        return currentPreprocessorDirective;
//    }
}
