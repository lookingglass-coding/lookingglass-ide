/*******************************************************************************
 * Copyright (c) 2006, 2015, Carnegie Mellon University. All rights reserved.
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
 * 3. Products derived from the software may not be called "Alice", nor may
 *    "Alice" appear in their name, without prior written permission of
 *    Carnegie Mellon University.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Carnegie Mellon University"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 * ANY AND ALL EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT
 * SHALL THE AUTHORS, COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.alice.ide.declarationseditor.events.components;

import org.lgna.project.ast.Expression;
import org.lgna.project.ast.ExpressionStatement;
import org.lgna.project.ast.MethodInvocation;

/**
 * @author Matt May
 */
public class EventsContentPanel extends org.alice.ide.codedrop.CodePanelWithDropReceptor {
	private static class RootStatementListPropertyPane extends org.alice.ide.x.components.StatementListPropertyView {
		public RootStatementListPropertyPane( org.lgna.project.ast.UserCode userCode ) {
			super( org.alice.ide.x.ProjectEditorAstI18nFactory.getInstance(), userCode.getBodyProperty().getValue().statements );
		}

		@Override
		public boolean isAcceptingOfAddEventListenerMethodInvocationStatements() {
			return true;
		}

		@Override
		protected int getBoxLayoutPad() {
			return 0;
		}

		@Override
		protected int getLeftInset() {
			return 0;
		}

		@Override
		protected int getRightInset() {
			return 0;
		}

		@Override
		protected org.lgna.croquet.views.AwtComponentView<?> createComponent( org.lgna.project.ast.Statement statement ) {
			if( statement instanceof ExpressionStatement ) {
				ExpressionStatement expressionStatement = (ExpressionStatement)statement;
				Expression expression = expressionStatement.expression.getValue();
				if( expression instanceof MethodInvocation ) {
					MethodInvocation methodInvocation = (MethodInvocation)expression;
					org.alice.ide.common.AddEventListenerStatementPanel statementPanel = new org.alice.ide.common.AddEventListenerStatementPanel( expressionStatement );
					statementPanel.addComponent( new org.alice.ide.declarationseditor.events.components.EventListenerComponent( methodInvocation ) );
					return statementPanel;
				}
			}
			return null;
		}
	}

	private final org.lgna.project.ast.AbstractCode code;
	private final RootStatementListPropertyPane rootPane;

	public EventsContentPanel( org.lgna.project.ast.UserMethod code ) {
		this.code = code;
		this.rootPane = new RootStatementListPropertyPane( code );
		this.addCenterComponent( this.rootPane );
		java.awt.Color color = org.alice.ide.ThemeUtilities.getActiveTheme().getProcedureColor();
		this.rootPane.setBackgroundColor( color );
		this.setBackgroundColor( color );
	}

	@Override
	public org.lgna.project.ast.AbstractCode getCode() {
		return this.code;
	}

	@Override
	protected org.lgna.croquet.views.AwtComponentView<?> getAsSeenBy() {
		return this;
	}

	@Override
	public void setJavaCodeOnTheSide( boolean value, boolean isFirstTime ) {
	}

	@Override
	public org.lgna.croquet.views.TrackableShape getTrackableShape( org.lgna.croquet.DropSite potentialDropSite ) {
		edu.cmu.cs.dennisc.java.util.logging.Logger.todo( potentialDropSite );
		return null;
	}

	@Override
	public java.awt.print.Printable getPrintable() {
		return new edu.cmu.cs.dennisc.java.awt.PrintHelper.Builder( this.getInsets(), this.getBackgroundColor() )
				.center( this.rootPane.getAwtComponent() )
				.build();
	}
}