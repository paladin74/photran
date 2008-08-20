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

public class ASTSubscriptTripletNode extends ASTNode
{
    ASTExprNode lb; // in ASTSubscriptTripletNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTSubscriptTripletNode
    ASTExprNode ub; // in ASTSubscriptTripletNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon2; // in ASTSubscriptTripletNode
    ASTExprNode step; // in ASTSubscriptTripletNode

    public ASTExprNode getLb()
    {
        return this.lb;
    }

    public void setLb(ASTExprNode newValue)
    {
        this.lb = newValue;
    }


    public ASTExprNode getUb()
    {
        return this.ub;
    }

    public void setUb(ASTExprNode newValue)
    {
        this.ub = newValue;
    }


    public ASTExprNode getStep()
    {
        return this.step;
    }

    public void setStep(ASTExprNode newValue)
    {
        this.step = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTSubscriptTripletNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 5;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.lb;
        case 1:  return this.hiddenTColon;
        case 2:  return this.ub;
        case 3:  return this.hiddenTColon2;
        case 4:  return this.step;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lb = (ASTExprNode)value; return;
        case 1:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.ub = (ASTExprNode)value; return;
        case 3:  this.hiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.step = (ASTExprNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

