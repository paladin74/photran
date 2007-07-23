// Generated by Ludwig version 1.0 alpha 6

package org.eclipse.photran.internal.core.parser; import org.eclipse.photran.internal.core.lexer.*;


/**
 * <IntentParList> ::= IntentPar:<IntentPar>  :production305
 * <IntentParList> ::= @:<IntentParList> tcomma:T_COMMA IntentPar:<IntentPar>  :production306
 */
public class ASTIntentParListNode extends ParseTreeNode
{
    public ASTIntentParListNode(Nonterminal nonterminal, Production production)
    {
        super(nonterminal, production);
    }

    public int count()
    {
        ParseTreeNode node = this;
        int count = 1;
        while (node.getChild("@") != null)
        {
            count++;
            node = node.getChild("@");
        }
        return count;
    }

    public ASTIntentParNode getASTIntentPar(int index)
    {
        ASTIntentParListNode node = this;
        for (int i = 0; i < index; i++)
            node = (ASTIntentParListNode)node.getChild("@");
        return (ASTIntentParNode)node.getChild("IntentPar");
    }

    public Token getASTTcomma(int index)
    {
        ASTIntentParListNode node = this;
        for (int i = 0; i < index; i++)
            node = (ASTIntentParListNode)node.getChild("@");
        return node.getChildToken("tcomma");
    }

    protected void visitThisNodeUsing(ASTVisitor visitor) { visitor.visitASTIntentParListNode(this); }
}