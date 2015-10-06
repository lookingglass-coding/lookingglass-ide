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
package edu.wustl.lookingglass.ide.perspectives.dinah;

import org.alice.ide.common.AbstractStatementPane;
import org.alice.ide.x.AstI18nFactory;
import org.alice.ide.x.ImmutableAstI18nFactory;
import org.alice.ide.x.components.AbstractExpressionView;
import org.lgna.croquet.DragModel;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.SwingComponentView;
import org.lgna.project.ast.AbstractType;
import org.lgna.project.ast.Expression;
import org.lgna.project.ast.LocalAccess;
import org.lgna.project.ast.MethodInvocation;
import org.lgna.project.ast.ParameterAccess;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.StatementListProperty;
import org.lgna.project.ast.ThisExpression;
import org.lgna.project.ast.UserField;

/**
 * @author Michael Pogran
 */
public class DinahAstI18nFactory extends ImmutableAstI18nFactory {

	private final edu.cmu.cs.dennisc.java.util.DStack<UserField> stack = edu.cmu.cs.dennisc.java.util.Stacks.newStack();

	private static class SingletonHolder {
		private static DinahAstI18nFactory instance = new DinahAstI18nFactory();
	}

	@Override
	protected AbstractType<?, ?, ?> getFallBackTypeForThisExpression() {
		return null;
	}

	public static DinahAstI18nFactory getInstance() {
		return SingletonHolder.instance;
	}

	private DinahAstI18nFactory() {
	}

	public AbstractStatementPane createExecutionTraceStatementPane( Statement statement, boolean selected, UserField targetField ) {
		if( targetField != null ) {
			this.stack.push( targetField );
		}
		try {
			AbstractStatementPane rv = super.createStatementPane( null, statement, null );
			rv.setActive( selected );
			return rv;
		} finally {
			this.stack.pop();
		}
	}

	@Override
	public AbstractStatementPane createStatementPane( DragModel dragModel, Statement statement, StatementListProperty statementListProperty ) {
		return super.createStatementPane( null, statement, statementListProperty );
	}

	@Override
	public SwingComponentView<?> createExpressionPane( Expression expression ) {
		if( stack.isEmpty() ) {
			//pass
		} else {
			if( ( expression instanceof ThisExpression ) && ( expression.getParent() instanceof MethodInvocation ) ) {
				return new ExpressionReplacementView( this, expression, this.stack.peek() );
			}
			else if( ( expression instanceof ParameterAccess ) && ( expression.getParent() instanceof MethodInvocation ) ) {
				return new ExpressionReplacementView( this, expression, this.stack.peek() );
			}
			else if( ( expression instanceof LocalAccess ) && ( expression.getParent() instanceof MethodInvocation ) ) {
				return new ExpressionReplacementView( this, expression, this.stack.peek() );
			}
		}
		return super.createExpressionPane( expression );
	}

	class ExpressionReplacementView extends AbstractExpressionView<Expression> {
		public ExpressionReplacementView( AstI18nFactory factory, Expression expression, UserField targetField ) {
			super( factory, expression );
			this.addComponent( new Label( targetField.getName() ) );
		}
	}
}
