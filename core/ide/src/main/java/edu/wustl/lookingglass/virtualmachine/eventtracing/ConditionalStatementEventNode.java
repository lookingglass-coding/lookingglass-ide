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
import org.lgna.project.ast.BooleanExpressionBodyPair;
import org.lgna.project.ast.ConditionalStatement;
import org.lgna.project.ast.Expression;

import edu.cmu.cs.dennisc.java.util.Lists;
import edu.wustl.lookingglass.remix.ast.ASTCopier;

/**
 * The <code>ConditionalStatementEventNode</code> subclass describes an
 * {@link AbstractEventNode} with a <code>ConditionalStatement</code> ast node.
 *
 * @author Michael Pogran
 */
public class ConditionalStatementEventNode extends AbstractEventNode<ConditionalStatement> {
	private ContainerEventNode<BlockStatement> bodyNode;
	private Expression expression;
	private BlockStatement body;
	private boolean isElseStatement;

	private ArrayList<ExpressionEvaluationEventNode> conditionalEvals = Lists.newArrayList();

	ConditionalStatementEventNode( ConditionalStatement astNode, ComponentThread thread, double startTime, double endTime, AbstractEventNode<?> parent ) {
		super( astNode, thread, startTime, endTime, parent );
	}

	public boolean isElseStatement() {
		return isElseStatement;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	protected void handleChildAdded( AbstractEventNode<?> eventNode ) {
		// When a child is added, find the expression and body for the executed statement
		if( eventNode instanceof ContainerEventNode ) {
			if( eventNode.getAstNode() instanceof BlockStatement ) {
				for( BooleanExpressionBodyPair pair : this.astNode.booleanExpressionBodyPairs ) {
					if( pair.body.getValue() == eventNode.getAstNode() ) {
						this.isElseStatement = false;
						this.expression = pair.expression.getValue();
						this.body = pair.body.getValue();
						this.bodyNode = (ContainerEventNode<BlockStatement>)eventNode;
						return;
					}
				}
				if( this.astNode.elseBody.getValue() == eventNode.getAstNode() ) {
					this.isElseStatement = true;
					this.body = this.astNode.elseBody.getValue();
					this.bodyNode = (ContainerEventNode<BlockStatement>)eventNode;
					return;
				}
			}
		}
	}

	public void addConditionalEvaluation( ExpressionEvaluationEventNode conditionalEval ) {
		this.conditionalEvals.add( conditionalEval );
	}

	public ContainerEventNode<BlockStatement> getBodyEventNode() {
		return this.bodyNode;
	}

	public Expression getTrueExpression() {
		return this.expression;
	}

	public ExpressionEvaluationEventNode getTrueExpressionNode() {
		for( ExpressionEvaluationEventNode expressionNode : this.getConditionalEvaluations() ) {
			if( expressionNode.getAstNode().equals( getTrueExpression() ) ) {
				return expressionNode;
			}
		}
		return null;
	}

	public BlockStatement getTrueBody() {
		return this.body;
	}

	public ArrayList<ExpressionEvaluationEventNode> getConditionalEvaluations() {
		return this.conditionalEvals;
	}

	/* ---------------- Copy methods  -------------- */

	private void setBodyEventNode( ContainerEventNode<BlockStatement> childNode ) {
		this.bodyNode = childNode;
	}

	private void setBody( BlockStatement body ) {
		this.body = body;
	}

	private void setExpression( Expression expression ) {
		this.expression = expression;
	}

	private void setIsElseStatement( boolean isElseStatement ) {
		this.isElseStatement = isElseStatement;
	}

	private void setConditionalEvaluations( ArrayList<ExpressionEvaluationEventNode> conditionalEvals ) {
		this.conditionalEvals = conditionalEvals;
	}

	static ConditionalStatementEventNode createCopy( ConditionalStatementEventNode eventNode, ASTCopier copier ) {
		ConditionalStatementEventNode rv = new ConditionalStatementEventNode( (ConditionalStatement)copier.copyAbstractNode( eventNode.getAstNode() ), eventNode.getThread(), eventNode.getStartTime(), eventNode.getEndTime(), null );

		// Copy conditional children event nodes
		ArrayList<ExpressionEvaluationEventNode> conditionalEvals = Lists.newArrayList();
		for( ExpressionEvaluationEventNode evalNode : eventNode.getConditionalEvaluations() ) {
			ExpressionEvaluationEventNode copyNode = EventNodeFactory.copyEventNode( evalNode, copier );
			copyNode.setParent( rv );
			conditionalEvals.add( copyNode );
		}
		rv.setConditionalEvaluations( conditionalEvals );

		// Copy body event node
		ContainerEventNode<BlockStatement> copyBodyNode = EventNodeFactory.copyEventNode( eventNode.getBodyEventNode(), copier );
		copyBodyNode.setParent( rv );
		rv.setBodyEventNode( copyBodyNode );

		// Copy ast nodes
		if( eventNode.isElseStatement() ) {
			//pass
		} else {
			rv.setBody( (BlockStatement)copier.copyAbstractNode( eventNode.getTrueBody() ) );
			rv.setExpression( (Expression)copier.copyAbstractNode( eventNode.getTrueExpression() ) );
		}

		rv.setIsElseStatement( eventNode.isElseStatement() );
		return rv;
	}
}
