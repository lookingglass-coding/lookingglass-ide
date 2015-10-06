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
package edu.wustl.lookingglass.ide.perspectives.dinah.processbar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.GeneralPath;

import org.alice.ide.IDE;
import org.alice.ide.highlight.IdeHighlightStencil;
import org.lgna.croquet.views.AbstractButton;
import org.lgna.croquet.views.AwtComponentView;
import org.lgna.croquet.views.BoxUtilities;
import org.lgna.croquet.views.Button;
import org.lgna.croquet.views.ImmutableTextField;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.LineAxisPanel;
import org.lgna.croquet.views.MigPanel;

import edu.cmu.cs.dennisc.java.awt.font.TextWeight;
import edu.wustl.lookingglass.ide.croquet.components.StatementSelectionIcon;
import edu.wustl.lookingglass.ide.croquet.components.ToggleIcon;
import edu.wustl.lookingglass.ide.croquet.preferences.ShowAboutRemixStencilState;
import edu.wustl.lookingglass.ide.perspectives.dinah.AbstractRemixStepsComposite;
import edu.wustl.lookingglass.remix.models.EndCaptureState;
import edu.wustl.lookingglass.remix.models.StartCaptureState;

/**
 * @author ckelleher
 */
public abstract class AbstractStepOverviewPanel extends MigPanel {
	protected static final int PAD = 8;
	protected static final int ARROW_SIZE = 16;
	protected static final int SEPARATOR = 6;

	private StepAxisPanel startPanel;
	private StepAxisPanel shareOrUsePanel;
	private StepAxisPanel endPanel;
	private StepAxisPanel previewPanel;

	protected abstract StepAxisPanel createShareOrUsePanel( AbstractRemixStepsComposite composite );

	public AbstractStepOverviewPanel( AbstractRemixStepsComposite composite ) {
		super( composite, "fill, insets 0", "[shrink]0[shrink]0[shrink]0[shrink]0[grow, fill]", "[]" );

		Dimension iconSize = new Dimension( 32, 32 );

		// Create help button and panel
		AbstractButton<?, ?> helpButton = composite.getHelpOperation().createButton( edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );
		helpButton.setClobberIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "help-about", org.lgna.croquet.icon.IconSize.SMALL ) );
		helpButton.setHorizontalTextPosition( org.lgna.croquet.views.HorizontalTextPosition.LEFT );

		org.lgna.croquet.views.LineAxisPanel helpPanel = new org.lgna.croquet.views.LineAxisPanel( helpButton );
		helpPanel.setBorder( new ArrowBorder( Color.decode( "#C3D8F3" ) ) ); // this makes the part that's not actively painted by the arrowborder the color for name and export
		helpPanel.setBackgroundColor( Color.decode( "#C3D8F3" ) );

		// Create the select start panel
		Label startSidekickLabel = new Label( "1. Pick a start", edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );
		startSidekickLabel.getAwtComponent().setOpaque( false );

		this.startPanel = new StepAxisPanel(
				new Label( new ToggleIcon( StartCaptureState.getInstance(), new StatementSelectionIcon( iconSize, true, true ), new StatementSelectionIcon( iconSize, true, false ) ) ),
				BoxUtilities.createHorizontalSliver( SEPARATOR ),
				startSidekickLabel,
				BoxUtilities.createHorizontalSliver( SEPARATOR )
				);
		this.startPanel.setCompanionPanel( helpPanel );
		this.startPanel.setBorder( new ArrowBorder( Color.decode( "#C3D8F3" ) ) ); // this makes the part that's not actively painted by the arrowborder the color for name and export
		this.startPanel.setBackgroundColor( Color.decode( "#C3D8F3" ) );

		// Create the select end panel
		Label endSidekickLabel = new Label( "2. Pick an end", edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );
		endSidekickLabel.getAwtComponent().setOpaque( false );

		this.endPanel = new StepAxisPanel(
				new Label( new ToggleIcon( EndCaptureState.getInstance(), new StatementSelectionIcon( iconSize, false, true ), new StatementSelectionIcon( iconSize, false, false ) ) ),
				BoxUtilities.createHorizontalSliver( SEPARATOR ),
				endSidekickLabel,
				BoxUtilities.createHorizontalSliver( SEPARATOR )
				);
		this.endPanel.setCompanionPanel( startPanel );
		this.endPanel.setBorder( new ArrowBorder( Color.decode( "#C3D8F3" ) ) );
		this.endPanel.setBackgroundColor( Color.decode( "#C3D8F3" ) ); // this makes the part that's not actively painted by the arrowborder the color for name and export

		// Create the preview panel
		Button previewButton = composite.getPreviewOperation().createButton( edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );
		previewButton.setHorizontalTextPosition( org.lgna.croquet.views.HorizontalTextPosition.LEFT );
		previewButton.getAwtComponent().setEnabled( false );

		ImmutableTextField previewSidekickLabel = composite.getPreviewOperation().getSidekickLabel().createImmutableTextField( TextWeight.BOLD );
		previewSidekickLabel.setOpaque( false );

		this.previewPanel = new StepAxisPanel(
				previewSidekickLabel,
				BoxUtilities.createHorizontalSliver( SEPARATOR ),
				previewButton
				);
		this.previewPanel.setCompanionPanel( endPanel );
		this.previewPanel.setBorder( new ArrowBorder( Color.decode( "#C3D8F3" ) ) );
		this.previewPanel.setBackgroundColor( Color.decode( "#C3D8F3" ) ); // this makes the part that's not actively painted by the arrowborder the color for name and export

		this.shareOrUsePanel = createShareOrUsePanel( composite );
		this.shareOrUsePanel.setCompanionPanel( previewPanel );
		this.shareOrUsePanel.setBackgroundColor( Color.decode( "#C3D8F3" ) );

		this.addComponent( helpPanel, "cell 0 0, shrinkx, growy" );
		this.addComponent( this.startPanel, "cell 1 0, shrinkx, growy" );
		this.addComponent( this.endPanel, "cell 2 0, shrinkx, growy" );
		this.addComponent( this.previewPanel, "cell 3 0, shrinkx, growy" );
		this.addComponent( this.shareOrUsePanel, "cell 4 0, shrinkx, growy" );
		this.setBorder( javax.swing.BorderFactory.createMatteBorder( 0, 0, 1, 0, Color.decode( "#C3D8F3" ) ) );

	}

	public void updateCaptureStates( boolean startSelected, boolean endSelected ) {
		if( startSelected && endSelected ) {
			this.shareOrUsePanel.setActiveStep();
			this.previewPanel.setActiveStep();

			this.endPanel.setInactiveStep();
			this.startPanel.setInactiveStep();
		}
		else if( startSelected ) {
			this.endPanel.setActiveStep();

			this.shareOrUsePanel.setInactiveStep();
			this.previewPanel.setInactiveStep();
			this.startPanel.setInactiveStep();
		}
		else {
			this.startPanel.setActiveStep();

			this.shareOrUsePanel.setInactiveStep();
			this.previewPanel.setInactiveStep();
			this.endPanel.setInactiveStep();
		}
	}

	@Override
	protected void handleDisplayable() {
		super.handleDisplayable();
		AbstractRemixStepsComposite composite = (AbstractRemixStepsComposite)getComposite();

		if( ShowAboutRemixStencilState.getInstance().getValue() ) {
			IdeHighlightStencil stencil = new IdeHighlightStencil( IDE.getActiveInstance().getDocumentFrame().getFrame(), javax.swing.JLayeredPane.POPUP_LAYER - 2 );
			stencil.addComponentListener( composite.getStencilListener() );
			stencil.showHighlightOverTrackableShape( this, composite.getStencilMessage() );

			// Ok. You got the message. Now don't show it again.
			ShowAboutRemixStencilState.getInstance().setValueTransactionlessly( false );
		}
	}

	class StepAxisPanel extends LineAxisPanel {
		private final Color originalColor = Color.decode( "#C3D8F3" );
		private LineAxisPanel companionPanel;

		public void setActiveStep() {
			this.getAwtComponent().setBackground( Color.decode( "#FFF3C8" ) );
			if( this.companionPanel != null ) {
				this.companionPanel.setBorder( new ArrowBorder( Color.decode( "#FFF3C8" ) ) );
			}
		}

		public void setInactiveStep() {
			this.getAwtComponent().setBackground( originalColor );
			if( this.companionPanel != null ) {
				this.companionPanel.setBorder( new ArrowBorder( originalColor ) );
			}
		}

		public StepAxisPanel( AwtComponentView<?>... components ) {
			super( null, components );
		}

		public void setCompanionPanel( LineAxisPanel panel ) {
			this.companionPanel = panel;
		}
	}

	class ArrowBorder extends javax.swing.border.EmptyBorder {
		Color arrowColor = null;

		public ArrowBorder( Color arrowColor ) {
			super( PAD, PAD, PAD, PAD + ARROW_SIZE );
			this.arrowColor = arrowColor;
		}

		@Override
		public void paintBorder( java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height ) {
			java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;
			g2.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

			g2.setPaint( this.arrowColor );
			g2.fillRect( ( x + width ) - ARROW_SIZE, y, width, height );

			java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
			path.moveTo( ( x + width ) - ARROW_SIZE, y );
			path.lineTo( x + ( width - 1 ), y + ( height / 2 ) );
			path.lineTo( ( x + width ) - ARROW_SIZE, y + height );

			java.awt.geom.GeneralPath closedPath = (GeneralPath)path.clone();
			closedPath.closePath();

			g2.setPaint( c.getBackground() );
			g2.fill( closedPath );
			g2.setPaint( Color.decode( "#26364D" ) );
			g2.setStroke( new java.awt.BasicStroke( 1.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND ) );
			g2.draw( path );
		}
	}
}
