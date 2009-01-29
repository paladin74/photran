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

import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;

public class ASTWaitSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token keyword; // in ASTWaitSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEquals; // in ASTWaitSpecNode
    IExpr expr; // in ASTWaitSpecNode

    public org.eclipse.photran.internal.core.lexer.Token getKeyword()
    {
        return this.keyword;
    }

    public void setKeyword(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.keyword = newValue;
    }


    public IExpr getExpr()
    {
        return this.expr;
    }

    public void setExpr(IExpr newValue)
    {
        this.expr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTWaitSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 3;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.keyword;
        case 1:  return this.hiddenTEquals;
        case 2:  return this.expr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.keyword = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTEquals = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.expr = (IExpr)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

