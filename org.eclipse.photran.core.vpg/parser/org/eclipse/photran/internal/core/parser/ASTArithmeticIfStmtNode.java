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
package org.eclipse.photran.internal.core.parser;

import java.io.PrintStream;
import java.util.Iterator;

import java.util.List;

import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings("all")
public class ASTArithmeticIfStmtNode extends ASTNode implements IObsoleteActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTArithmeticIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIf; // in ASTArithmeticIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTArithmeticIfStmtNode
    IExpr expr; // in ASTArithmeticIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTArithmeticIfStmtNode
    ASTLblRefNode first; // in ASTArithmeticIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTArithmeticIfStmtNode
    ASTLblRefNode second; // in ASTArithmeticIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma2; // in ASTArithmeticIfStmtNode
    ASTLblRefNode third; // in ASTArithmeticIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTArithmeticIfStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getExpr()
    {
        return this.expr;
    }

    public void setExpr(IExpr newValue)
    {
        this.expr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTLblRefNode getFirst()
    {
        return this.first;
    }

    public void setFirst(ASTLblRefNode newValue)
    {
        this.first = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTLblRefNode getSecond()
    {
        return this.second;
    }

    public void setSecond(ASTLblRefNode newValue)
    {
        this.second = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTLblRefNode getThird()
    {
        return this.third;
    }

    public void setThird(ASTLblRefNode newValue)
    {
        this.third = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTArithmeticIfStmtNode(this);
        visitor.visitIObsoleteActionStmt(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 11;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.hiddenTIf;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.expr;
        case 4:  return this.hiddenTRparen;
        case 5:  return this.first;
        case 6:  return this.hiddenTComma;
        case 7:  return this.second;
        case 8:  return this.hiddenTComma2;
        case 9:  return this.third;
        case 10: return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTIf = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.expr = (IExpr)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.first = (ASTLblRefNode)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.second = (ASTLblRefNode)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenTComma2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.third = (ASTLblRefNode)value; if (value != null) value.setParent(this); return;
        case 10: this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

