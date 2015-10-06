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
package edu.wustl.lookingglass.lgpedia.views;

import javax.swing.BorderFactory;

import org.lgna.croquet.views.AbstractWindow;
import org.lgna.croquet.views.MigPanel;

import edu.wustl.lookingglass.lgpedia.LgpediaAnnotationFormatter;
import edu.wustl.lookingglass.lgpedia.LgpediaComposite;
import edu.wustl.lookingglass.lgpedia.LgpediaComposite.ConstructType;

/**
 * @author Michael Pogran
 */
public class LgpediaView extends MigPanel {
	private org.lgna.croquet.views.BorderPanel executingProgramContainer;
	private MigPanel leftPanel;
	private MigPanel namePanel;
	private org.lgna.croquet.views.LineAxisPanel buttonPanel;
	private edu.wustl.lookingglass.lgpedia.components.StatementListPanel statementPanel;

	private org.lgna.croquet.views.Label methodNameLabel;
	private org.lgna.croquet.views.PlainMultiLineLabel methodDescriptionLabel;
	private org.lgna.croquet.views.Button cancelButton;

	public LgpediaView( edu.wustl.lookingglass.lgpedia.LgpediaComposite composite ) {
		super( composite, "fill, ins 0", "[415!]0[]", "[]0[]" );
		this.executingProgramContainer = new org.lgna.croquet.views.BorderPanel();
		this.executingProgramContainer.setBackgroundColor( java.awt.Color.BLACK );

		this.methodNameLabel = new org.lgna.croquet.views.Label( "", 1.25f, edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );
		this.methodDescriptionLabel = new org.lgna.croquet.views.PlainMultiLineLabel();

		this.leftPanel = new MigPanel( null, "fill", "[]", "[][]" );
		this.leftPanel.setBackgroundColor( new java.awt.Color( 152, 160, 217 ) );

		this.namePanel = new MigPanel( null, "fill", "[390!]", "[][][]" );
		this.namePanel.addComponent( this.executingProgramContainer, "cell 0 0, w 390, h 219, top" );
		this.namePanel.addComponent( this.methodNameLabel, "cell 0 1, growx, top" );
		this.namePanel.addComponent( this.methodDescriptionLabel, "cell 0 2, grow, push" );

		this.namePanel.setBackgroundColor( java.awt.Color.WHITE );

		this.leftPanel.addComponent( this.namePanel, "cell 0 0, h 300, aligny top" );

		addComponent( this.leftPanel, "cell 0 0, spany 2, grow" );

		this.statementPanel = new edu.wustl.lookingglass.lgpedia.components.StatementListPanel( composite );
		org.lgna.croquet.views.ScrollPane scrollPane = new org.lgna.croquet.views.ScrollPane( this.statementPanel );
		this.statementPanel.setBackgroundColor( new java.awt.Color( 74, 86, 160 ) );

		addComponent( scrollPane, "cell 1 0, grow, push" );

		this.buttonPanel = new org.lgna.croquet.views.LineAxisPanel() {
			@Override
			protected javax.swing.JPanel createJPanel() {
				return new javax.swing.JPanel() {
					@Override
					protected void paintComponent( java.awt.Graphics g ) {
						edu.wustl.lookingglass.lgpedia.ShadowGradientPainter painter = new edu.wustl.lookingglass.lgpedia.ShadowGradientPainter();
						painter.paint( (java.awt.Graphics2D)g, this, getWidth(), getHeight() );
					}
				};
			}
		};
		this.cancelButton = composite.getCancelOperation().createButton();
		org.lgna.croquet.views.Button previousButton = composite.getPreviousOperation().createButton();
		org.lgna.croquet.views.Button nextButton = composite.getNextOperation().createButton();

		this.buttonPanel.addComponent( org.lgna.croquet.views.BoxUtilities.createHorizontalGlue() );
		this.buttonPanel.addComponent( previousButton );
		this.buttonPanel.addComponent( nextButton );
		this.buttonPanel.addComponent( this.cancelButton );
		this.buttonPanel.addComponent( org.lgna.croquet.views.BoxUtilities.createHorizontalStrut( 5 ) );

		addComponent( this.buttonPanel, "cell 1 1, alignx right, growx" );
	}

	public void setBackgroundColorType( ConstructType type ) {
		java.awt.Color baseColor;
		java.awt.Color darkerColor;
		java.awt.Color buttonColor;
		if( type == ConstructType.PROCEDURE ) {
			baseColor = new java.awt.Color( 152, 160, 217 );
			darkerColor = new java.awt.Color( 110, 119, 189 );
			buttonColor = new java.awt.Color( 96, 151, 177 );
		}
		else if( type == ConstructType.FUNCTION ) {
			baseColor = new java.awt.Color( 172, 217, 152 );
			darkerColor = new java.awt.Color( 126, 184, 101 );
			buttonColor = new java.awt.Color( 181, 204, 112 );
		}
		else {
			baseColor = new java.awt.Color( 210, 150, 105 );
			darkerColor = new java.awt.Color( 169, 105, 57 );
			buttonColor = new java.awt.Color( 77, 99, 140 );
		}
		this.statementPanel.setExecuteStatementBaseColor( buttonColor );
		this.leftPanel.setBackgroundColor( baseColor );
		this.namePanel.setBorder( javax.swing.BorderFactory.createMatteBorder( 0, 0, 2, 0, edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( baseColor, 1.0, 1.15, 0.90 ) ) );

		this.buttonPanel.setBackgroundColor( darkerColor );
		this.statementPanel.setBackgroundColor( darkerColor );

		java.awt.Color borderColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( darkerColor, 1.0, 0.90, 0.75 );
		javax.swing.border.Border buttonPanelBorder = BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder( 1, 0, 0, 0, borderColor ), BorderFactory.createEmptyBorder( 5, 0, 5, 0 ) );
		this.buttonPanel.setBorder( buttonPanelBorder );
	}

	public void setMethodName( org.lgna.project.ast.AbstractMethod method ) {
		String name = org.alice.ide.formatter.AliceFormatter.getInstance().getNameForDeclaration( method );
		String description = LgpediaAnnotationFormatter.getInstance().getDescriptionForMethod( method );
		this.methodNameLabel.setText( name + " action" );
		this.methodDescriptionLabel.setText( description );
	}

	public void addParameterAnnotation( org.lgna.project.ast.AbstractDeclaration declaration, org.lgna.project.ast.AbstractMethod method ) {
		this.statementPanel.addParameterAnnotation( declaration, method, false );
	}

	public void addParameterStatement( org.lgna.project.ast.Statement statement, boolean addMouseEvents ) {
		this.statementPanel.addStatement( statement, addMouseEvents, false );
	}

	public void addDetailAnnotation( org.lgna.project.ast.AbstractDeclaration declaration, org.lgna.project.ast.AbstractMethod method ) {
		this.statementPanel.addParameterAnnotation( declaration, method, true );
	}

	public void addDetailStatement( org.lgna.project.ast.Statement statement, boolean addMouseEvents ) {
		this.statementPanel.addStatement( statement, addMouseEvents, true );
	}

	public void resetStatementsPanel() {
		synchronized( this.statementPanel.getTreeLock() ) {
			this.statementPanel.resetPanel();
		}
	}

	public void setDetailsVisible( boolean showDetails ) {
		if( showDetails ) {
			this.statementPanel.setDetailsVisible();
		}
	}

	public void scrollToSelection( boolean scrollToDetails ) {
		this.statementPanel.scrollToSelection( scrollToDetails );
	}

	public org.lgna.croquet.views.BorderPanel getExecutingProgramContainer() {
		return this.executingProgramContainer;
	}

	@Override
	public LgpediaComposite getComposite() {
		return (LgpediaComposite)super.getComposite();
	}

	@Override
	protected void handleDisplayable() {
		super.handleDisplayable();
		AbstractWindow<?> root = getRoot();
		if( root != null ) {
			root.pushDefaultButton( this.cancelButton );
		}
	}

	@Override
	protected void handleUndisplayable() {
		AbstractWindow<?> root = getRoot();
		if( root != null ) {
			root.popDefaultButton();
		}
		super.handleUndisplayable();
	}
}
