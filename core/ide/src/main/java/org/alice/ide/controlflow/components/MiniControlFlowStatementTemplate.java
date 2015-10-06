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

package org.alice.ide.controlflow.components;

/**
 * @author Dennis Cosgrove
 */
public class MiniControlFlowStatementTemplate extends org.alice.ide.templates.StatementTemplate {
	private final org.lgna.project.ast.Statement incompleteStatement;
	private org.lgna.croquet.views.SwingComponentView<?> incompleteStatementPane;
	private org.lgna.croquet.views.LineAxisPanel panel;

	public MiniControlFlowStatementTemplate( org.alice.ide.ast.draganddrop.statement.StatementTemplateDragModel dragModel ) {
		super( dragModel, dragModel.getStatementCls() );
		this.incompleteStatement = dragModel.getPossiblyIncompleteStatement();
	}

	private String labelText;

	private String getLabelText() {
		if( this.labelText != null ) {
			//pass
		} else {
			Class<?> cls;
			org.lgna.croquet.DragModel model = this.getModel();
			if( model instanceof org.alice.ide.ast.draganddrop.statement.ExpressionStatementTemplateDragModel ) {
				org.alice.ide.ast.draganddrop.statement.ExpressionStatementTemplateDragModel expressionStatementTemplateDragModel = (org.alice.ide.ast.draganddrop.statement.ExpressionStatementTemplateDragModel)model;
				cls = expressionStatementTemplateDragModel.getExpressionCls();
			} else {
				cls = this.getStatementCls();
			}
			this.labelText = edu.cmu.cs.dennisc.java.util.ResourceBundleUtilities.getStringFromSimpleNames( cls, "org.alice.ide.controlflow.Templates", javax.swing.JComponent.getDefaultLocale() );
		}
		return this.labelText;
	}

	private org.lgna.croquet.views.SwingComponentView<?> getIncompleteStatementPane() {
		if( this.incompleteStatementPane != null ) {
			//pass
		} else {
			this.incompleteStatementPane = org.alice.ide.x.TemplateAstI18nFactory.getInstance().createStatementPane( this.incompleteStatement );
		}
		return this.incompleteStatementPane;
	}

	@Override
	public org.lgna.croquet.views.SwingComponentView<?> getSubject() {
		return this.getIncompleteStatementPane();
	}

	@Override
	protected java.awt.Paint getBackgroundPaint( int x, int y, int width, int height ) {
		if( incompleteStatement instanceof org.lgna.project.ast.Comment ) {
			return java.awt.Color.WHITE;
		} else {
			return org.alice.ide.ThemeUtilities.getActiveTheme().getControlFlowStatementColor();
		}
	}

	//	@Override
	//	protected java.awt.Point getToolTipLocation( java.awt.Point location, java.awt.event.MouseEvent event ) {
	//		javax.swing.JToolTip toolTip = this.createToolTip( null );
	//		if( toolTip != null ) {
	//			int offset = toolTip.getPreferredSize().height;
	//			//			if( offset == 0 ) {
	//			//				edu.cmu.cs.dennisc.javax.swing.SwingUtilities.invalidateTree( toolTip );
	//			//				edu.cmu.cs.dennisc.javax.swing.SwingUtilities.doLayoutTree( toolTip );
	//			//				edu.cmu.cs.dennisc.javax.swing.SwingUtilities.setSizeToPreferredSizeTree( toolTip );
	//			//				offset = toolTip.getPreferredSize().height;
	//			//			}
	//			offset += 4;
	//			return new java.awt.Point( 0, -offset );
	//		} else {
	//			return location;
	//		}
	//	}

	//	@Override
	//	protected javax.swing.JToolTip createToolTip( javax.swing.JToolTip jToolTip ) {
	//		if( this.toolTip != null ) {
	//			//pass
	//		} else {
	//			this.toolTip = new edu.cmu.cs.dennisc.javax.swing.tooltips.JToolTip( this.getIncompleteStatementPane().getAwtComponent() );
	//		}
	//		return this.toolTip;
	//	}

	@Override
	protected int getInsetTop() {
		return 2;
	}

	@Override
	protected int getInsetBottom() {
		return 2;
	}

	@Override
	protected int getInsetRight() {
		return 5;
	}

	@Override
	protected void handleDisplayable() {
		super.handleDisplayable();
		if( this.panel != null ) {
			//pass
		} else {
			String statementText = this.getLabelText();
			if( statementText.lastIndexOf( "</" ) > 0 ) {
				org.lgna.croquet.views.Label preLabel = new org.lgna.croquet.views.Label( statementText.substring( 0, statementText.lastIndexOf( "</" ) ), edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );
				edu.wustl.lookingglass.ide.common.EmptyArgumentPane fillPane = new edu.wustl.lookingglass.ide.common.EmptyArgumentPane( statementText.substring( statementText.lastIndexOf( "</" ) + 2, statementText.lastIndexOf( "/>" ) ) );
				org.lgna.croquet.views.Label postLabel = new org.lgna.croquet.views.Label( statementText.substring( statementText.lastIndexOf( "/>" ) + 2, statementText.length() ), edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );

				preLabel.setBorder( javax.swing.BorderFactory.createEmptyBorder( 0, 4, 0, 0 ) );
				this.panel = new org.lgna.croquet.views.LineAxisPanel( preLabel, fillPane, postLabel );
			} else {
				org.lgna.croquet.views.Label label = new org.lgna.croquet.views.Label( statementText, edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );
				label.setBorder( javax.swing.BorderFactory.createEmptyBorder( 0, 4, 0, 0 ) );
				if( this.incompleteStatement instanceof org.lgna.project.ast.Comment ) {
					label.setForegroundColor( org.alice.ide.ThemeUtilities.getActiveTheme().getCommentForegroundColor() );
				}
				this.panel = new org.lgna.croquet.views.LineAxisPanel( label );

			}
			this.addComponent( this.panel );
		}
	}

	@Override
	protected void handleUndisplayable() {
		//		this.removeAllComponents();
		super.handleUndisplayable();
	}
}
