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
import java.util.Set;

import org.lgna.common.ComponentThread;
import org.lgna.project.ast.Expression;
import org.lgna.project.ast.ExpressionStatement;
import org.lgna.project.ast.MethodInvocation;
import org.lgna.project.ast.UserField;
import org.lgna.project.ast.UserMethod;

import edu.cmu.cs.dennisc.java.util.Lists;
import edu.cmu.cs.dennisc.java.util.Sets;
import edu.wustl.lookingglass.remix.ast.ASTCopier;

/**
 * The <code>ExpressionStatementEventNode</code> subclass describes an
 * {@link AbstractEventNode} with a <code>ExpressionStatement</code> ast node.
 * <code>ExpressionStatementEventNode</code> provides support for
 * <code>MethodInvocation</code> statements, such as access to the calling
 * <code>UserField</code> and the <code>UserMethod</code> if applicable.
 *
 * @author Michael Pogran
 */
public class ExpressionStatementEventNode extends AbstractEventNode<ExpressionStatement> {
	private ArrayList<ExpressionEvaluationEventNode> expressionEvals = Lists.newArrayList();
	private UserField callerField; // Invoking field, if expression is a MethodInvocation
	private Object callerInstance;
	private ContainerEventNode<?> methodEventNode; // ContainerEventNode for invoked UserMethod, if applicable
	private UserMethod userMethod;

	ExpressionStatementEventNode( ExpressionStatement astNode, ComponentThread thread, double startTime, double endTime, AbstractEventNode<?> parent ) {
		super( astNode, thread, startTime, endTime, parent );
	}

	public boolean isUserMethod() {
		ExpressionStatement statement = this.getAstNode();

		if( statement.expression.getValue() instanceof MethodInvocation ) {
			MethodInvocation methodInvocation = (MethodInvocation)statement.expression.getValue();
			return methodInvocation.method.getValue() instanceof UserMethod;
		}
		return false;
	}

	public void setCallerInstance( Object callerInstance ) {
		if( this.callerInstance == null ) {
			this.callerInstance = callerInstance;
		}
	}

	public void setCallerField( UserField callerField ) {
		if( this.callerField == null ) {
			this.callerField = callerField;
		}
	}

	public void setUserMethod( UserMethod isUserMethod ) {
		this.userMethod = isUserMethod;
	}

	public UserMethod getUserMethod() {
		return this.userMethod;
	}

	public UserField getCallerField() {
		return this.callerField;
	}

	public Object getCallerInstance() {
		return this.callerInstance;
	}

	public String getCallerName() {
		if( this.callerField != null ) {
			return this.callerField.getName();
		} else {
			return "Unknown";
		}
	}

	public String getExpressionString() {
		ExpressionStatement statement = this.getAstNode();
		if( statement.expression.getValue() instanceof MethodInvocation ) {
			MethodInvocation methodInvocation = (MethodInvocation)statement.expression.getValue();
			return methodInvocation.method.getValue().getName() + " - " + getCallerName();
		} else {
			return statement.getRepr();
		}
	}

	public void addUserMethodEventNode( ContainerEventNode<?> eventNode ) {
		if( isUserMethod() ) {
			this.methodEventNode = eventNode;
		}
	}

	public ContainerEventNode<?> getUserMethodEventNode() {
		return this.methodEventNode;
	}

	public void addExpressionEvaluationNode( ExpressionEvaluationEventNode evalNode ) {
		this.expressionEvals.add( evalNode );
	}

	public Object removeExpressionEvaluationNode( Expression expression ) {
		return this.expressionEvals.remove( expression );
	}

	public ArrayList<ExpressionEvaluationEventNode> getExpressionEvaluationNodes() {
		return this.expressionEvals;
	}

	public ExpressionEvaluationEventNode getExpressionEvaluationNode( Expression expression ) {
		for( ExpressionEvaluationEventNode eventNode : this.expressionEvals ) {
			if( eventNode.getAstNode().equals( expression ) ) {
				return eventNode;
			}
		}
		return null;
	}

	public Set<Expression> getExpressions() {
		Set<Expression> expressions = Sets.newHashSet();

		for( ExpressionEvaluationEventNode eventNode : this.expressionEvals ) {
			expressions.add( (Expression)eventNode.getAstNode() );
		}
		return expressions;
	}

	/* ---------------- Copy methods  -------------- */

	private ArrayList<ExpressionEvaluationEventNode> getExpressionEvals() {
		return this.expressionEvals;
	}

	private void setExpressionEvals( ArrayList<ExpressionEvaluationEventNode> expressionEvals ) {
		this.expressionEvals = expressionEvals;
	}

	static ExpressionStatementEventNode createCopy( ExpressionStatementEventNode eventNode, ASTCopier copier ) {
		ExpressionStatementEventNode rv = new ExpressionStatementEventNode( (ExpressionStatement)copier.copyAbstractNode( eventNode.getAstNode() ), eventNode.getThread(), eventNode.getStartTime(), eventNode.getEndTime(), null );
		ArrayList<ExpressionEvaluationEventNode> expressionEvals = Lists.newArrayList();

		// Copy user method event node
		if( eventNode.getUserMethodEventNode() != null ) {
			ContainerEventNode<?> copyMethodNode = EventNodeFactory.copyEventNode( eventNode.getUserMethodEventNode(), copier );
			copyMethodNode.setParent( rv );
			rv.addUserMethodEventNode( copyMethodNode );
		}

		// Copy expression evaluation event nodes
		for( ExpressionEvaluationEventNode evalNode : eventNode.getExpressionEvals() ) {
			ExpressionEvaluationEventNode copyNode = EventNodeFactory.copyEventNode( evalNode, copier );
			copyNode.setParent( rv );
			expressionEvals.add( copyNode );
		}
		rv.setExpressionEvals( expressionEvals );

		// Copy ast nodes
		if( eventNode.getUserMethod() != null ) {
			rv.setUserMethod( (UserMethod)copier.copyAbstractNode( eventNode.getUserMethod() ) );
		}
		if( eventNode.getCallerField() != null ) {
			rv.setCallerField( (UserField)copier.copyAbstractNode( eventNode.getCallerField() ) );
		}

		rv.setCallerInstance( eventNode.getCallerInstance() );
		return rv;
	}

	@Override
	protected void handleChildAdded( AbstractEventNode<?> eventNode ) {
		// pass
	}

}
