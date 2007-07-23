// Generated by Ludwig version 1.0 alpha 6

package org.eclipse.photran.internal.core.parser; import org.eclipse.photran.internal.core.lexer.*;


/**
 * <FormatIdentifier> ::= LblRef:<LblRef>  :production728
 * <FormatIdentifier> ::= CExpr:<CExpr>  :production729
 * <FormatIdentifier> ::= tasterisk:T_ASTERISK  :production730
 */
public class ASTFormatIdentifierNode extends ParseTreeNode
{
    public ASTFormatIdentifierNode(Nonterminal nonterminal, Production production)
    {
        super(nonterminal, production);
    }

    public ASTLblRefNode getASTLblRef()
    {
        return (ASTLblRefNode)this.getChild("LblRef");
    }

    public ASTCExprNode getASTCExpr()
    {
        return (ASTCExprNode)this.getChild("CExpr");
    }

    public Token getASTTasterisk()
    {
        return this.getChildToken("tasterisk");
    }

    protected void visitThisNodeUsing(ASTVisitor visitor) { visitor.visitASTFormatIdentifierNode(this); }
}