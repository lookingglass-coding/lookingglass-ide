/*******************************************************************************
 * Copyright (c) 2008, 2015, Washington University in St. Louis.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Products derived from the software may not be called "Looking Glass", nor
 *    may "Looking Glass" appear in their name, without prior written permission
 *    of Washington University in St. Louis.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Washington University in St. Louis"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.  ANY AND ALL
 * EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE,
 * TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHORS,
 * COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package edu.wustl.lookingglass.virtualmachine.eventtracing;

import java.util.ArrayList;

import org.lgna.common.ComponentThread;
import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.Expression;
import org.lgna.project.ast.WhileLoop;

import edu.cmu.cs.dennisc.java.util.Lists;
import edu.wustl.lookingglass.remix.ast.ASTCopier;

/**
 * The <code>WhileLoopEventNode</code> subclass describes an
 * {@link AbstractLoopEventNode} with a <code>WhileLoop</code> ast node.
 *
 * @author Michael Pogran
 */
public class WhileLoopEventNode extends AbstractLoopEventNode<WhileLoop> {
	Expression conditionalExpression;
	private ArrayList<ExpressionEvaluationEventNode> conditionalEvalNodes = Lists.newArrayList();

	WhileLoopEventNode( WhileLoop astNode, ComponentThread thread, double startTime, double endTime, AbstractEventNode<?> parent ) {
		super( astNode, thread, startTime, endTime, parent );
		this.conditionalExpression = ( (WhileLoop)astNode ).conditional.getValue();
	}

	public Expression getConditionalExpression() {
		return this.conditionalExpression;
	}

	private void setConditionalExpression( Expression conditionalExpression ) {
		this.conditionalExpression = conditionalExpression;
	}

	public void addConditionalEvaluation( ExpressionEvaluationEventNode conditionalEval ) {
		this.conditionalEvalNodes.add( conditionalEval );
	}

	public ArrayList<ExpressionEvaluationEventNode> getConditionalEvaluations() {
		return this.conditionalEvalNodes;
	}

	@Override
	public void addChild( AbstractEventNode<?> child ) {
		if( ( (WhileLoop)getAstNode() ).body.getValue().equals( child.getAstNode() ) ) {
			super.addChild( child );
		}
	}

	/* ---------------- Copy methods  -------------- */

	private void setConditionalEvaluations( ArrayList<ExpressionEvaluationEventNode> conditionalEvals ) {
		this.conditionalEvalNodes = conditionalEvals;
	}

	static WhileLoopEventNode createCopy( WhileLoopEventNode eventNode, ASTCopier copier ) {
		WhileLoopEventNode rv = new WhileLoopEventNode( (WhileLoop)copier.copyAbstractNode( eventNode.getAstNode() ), eventNode.getThread(), eventNode.getStartTime(), eventNode.getEndTime(), null );

		// Copy conditional event nodes
		ArrayList<ExpressionEvaluationEventNode> conditionalEvals = Lists.newArrayList();
		for( ExpressionEvaluationEventNode evalNode : eventNode.getConditionalEvaluations() ) {
			ExpressionEvaluationEventNode copyNode = EventNodeFactory.copyEventNode( evalNode, copier );
			copyNode.setParent( rv );
			conditionalEvals.add( copyNode );
		}
		rv.setConditionalEvaluations( conditionalEvals );

		// Copy child event nodes
		ArrayList<AbstractEventNode<?>> childNodes = Lists.newArrayList();
		for( AbstractEventNode<?> childNode : eventNode.getChildren() ) {
			AbstractEventNode<?> copyChildNode = EventNodeFactory.copyEventNode( childNode, copier );
			copyChildNode.setParent( rv );
			childNodes.add( copyChildNode );
		}
		rv.setChildNodes( childNodes );

		// Copy ast nodes
		rv.setConditionalExpression( (Expression)copier.copyAbstractNode( eventNode.getConditionalExpression() ) );
		rv.setBody( (BlockStatement)copier.copyAbstractNode( eventNode.getBody() ) );

		return rv;
	}
}
