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
package edu.wustl.lookingglass.lgpedia;

/**
 * @author Michael Pogran
 */
public class LgpediaAstI18nFactory extends org.alice.ide.x.PreviewAstI18nFactory {

	private static class SingletonHolder {
		private static LgpediaAstI18nFactory instance = new LgpediaAstI18nFactory();
	}

	public static LgpediaAstI18nFactory getInstance() {
		return SingletonHolder.instance;
	}

	private LgpediaAstI18nFactory() {
	}

	private final static java.util.Stack<Boolean> isEnabledStack = new java.util.Stack<Boolean>();

	@Override
	protected org.lgna.croquet.views.SwingComponentView<?> createKeyedArgumentListPropertyPane( org.lgna.project.ast.KeyedArgumentListProperty argumentListProperty ) {
		org.lgna.project.ast.ArgumentOwner owner = argumentListProperty.getOwner();
		org.lgna.project.ast.DeclarationProperty<? extends org.lgna.project.ast.AbstractCode> codeProperty = owner.getParameterOwnerProperty();
		org.lgna.project.ast.AbstractCode code = codeProperty.getValue();
		if( code.getKeyedParameter() != null ) {
			return new org.alice.ide.x.components.KeyedArgumentListPropertyView( this, argumentListProperty );
		} else {
			return new org.lgna.croquet.views.Label();
		}
	}

	public org.alice.ide.common.AbstractStatementPane createStatementPane( org.lgna.project.ast.Statement statement, boolean isEnabled ) {
		isEnabledStack.push( isEnabled );
		org.alice.ide.common.AbstractStatementPane rv = this.createStatementPane( org.alice.ide.ast.draganddrop.statement.StatementDragModel.getInstance( statement ), statement, null );
		isEnabledStack.pop();
		return rv;
	}

	@Override
	public org.alice.ide.common.AbstractStatementPane createStatementPane( org.lgna.croquet.DragModel dragModel, org.lgna.project.ast.Statement statement, org.lgna.project.ast.StatementListProperty statementListProperty ) {
		dragModel = null;
		boolean isEnabled = isEnabledStack.peek();
		if( isEnabled ) {
			return new StatementPane( dragModel, this, statement, statementListProperty );
		} else {
			return new DisabledStatementPane( dragModel, this, statement, statementListProperty );
		}
	}

	private class DisabledStatementPane extends org.alice.ide.common.DefaultStatementPane {
		public DisabledStatementPane( org.lgna.croquet.DragModel model, org.alice.ide.x.AstI18nFactory factory, org.lgna.project.ast.Statement statement, org.lgna.project.ast.StatementListProperty owner ) {
			super( model, factory, statement, owner );
		}

		@Override
		protected java.awt.Paint getBackgroundPaint( int x, int y, int width, int height ) {
			java.awt.Color rv = getParent().getBackgroundColor();

			if( rv == null ) {
				return java.awt.Color.LIGHT_GRAY;
			} else {
				return edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( rv, 1.0, 0.85, 1.15 );
			}
		}

		@Override
		protected java.awt.Paint getPassiveOutlinePaint() {
			java.awt.Color rv = getParent().getBackgroundColor();

			if( rv == null ) {
				return super.getPassiveOutlinePaint();
			} else {
				return edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( rv, 1.0, 0.85, 0.70 );
			}
		}
	}

	private class StatementPane extends org.alice.ide.common.DefaultStatementPane {
		public StatementPane( org.lgna.croquet.DragModel model, org.alice.ide.x.AstI18nFactory factory, org.lgna.project.ast.Statement statement, org.lgna.project.ast.StatementListProperty owner ) {
			super( model, factory, statement, owner );
		}

		@Override
		protected java.awt.Paint getPassiveOutlinePaint() {
			java.awt.Color rv = getParent().getBackgroundColor();

			if( rv == null ) {
				return super.getPassiveOutlinePaint();
			} else {
				return edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( rv, 1.0, 1.0, 0.50 );
			}
		}
	}

}
