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
package edu.wustl.lookingglass.ide.croquet.models.preview.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;

import org.lgna.croquet.BooleanState;
import org.lgna.croquet.views.BorderPanel;

import edu.wustl.lookingglass.ide.croquet.models.preview.IsPreviewProgramExecutingState;
import edu.wustl.lookingglass.ide.croquet.models.preview.PreviewProjectComposite;
import edu.wustl.lookingglass.ide.views.OverlayPlayIcon;

/**
 * @author Caitlin Kelleher
 */
public class PreviewWorldPanel extends BorderPanel {

	private static final int ICON_SIZE = 80;
	private static final OverlayPlayIcon playIcon = new OverlayPlayIcon( new java.awt.Dimension( ICON_SIZE, ICON_SIZE ) );

	private boolean isPlayAffordanceDesired = false;
	private boolean isPlayDisabled = false;
	private final IsPreviewProgramExecutingState isPreviewProgramExecutingState;
	private final BooleanState validProjectState;

	private final JPanel sceneViewPanel;
	private Image backgroundImage;

	private int playCount = 0;
	private boolean hidePlayIconUntilFirstPlay = false;

	private AtomicBoolean paintScene = new AtomicBoolean( true );

	public PreviewWorldPanel( PreviewProjectComposite ownerComposite, IsPreviewProgramExecutingState isPreviewProgramExecutingState, BooleanState validProjectState ) {
		super( ownerComposite );
		this.isPreviewProgramExecutingState = isPreviewProgramExecutingState;
		this.validProjectState = validProjectState;

		sceneViewPanel = new JEventIgnoringPanel();
		sceneViewPanel.setBorder( javax.swing.BorderFactory.createEmptyBorder() );
		sceneViewPanel.setLayout( new java.awt.BorderLayout() );
		sceneViewPanel.setOpaque( false );
		sceneViewPanel.setBackground( Color.BLACK );
		this.getAwtComponent().add( sceneViewPanel, BorderLayout.CENTER );
	}

	public void shouldPaintBackground( boolean shouldPaint ) {
		this.paintScene.set( shouldPaint );
	}

	public void shouldHidePlayIconUntilFirstPlay( boolean shouldHide ) {
		this.hidePlayIconUntilFirstPlay = shouldHide;
	}

	public void setPlayIconShowing( boolean isPlayAffordanceDesired ) {
		this.isPlayAffordanceDesired = isPlayAffordanceDesired;
		this.repaint();
	}

	public void setIsPlayDisabled( boolean isPlayDisabled ) {
		this.isPlayDisabled = isPlayDisabled;
		this.repaint();
	}

	public void setBackgroundImage( Image backgroundImage ) {
		this.backgroundImage = backgroundImage;
		this.repaint();
	}

	@Override
	protected JPanel createJPanel() {
		return new JPreviewAnimationPanel();
	}

	public void clear() {
		sceneViewPanel.removeAll();
	}

	public void update( boolean isPlaying ) {
		if( !isPlaying && hidePlayIconUntilFirstPlay && ( this.playCount <= 0 ) ) {
		} else {
			this.setPlayIconShowing( !isPlaying );
		}

		if( isPlaying ) {
			this.playCount++;
		}
	}

	public void updateProgram() {
		if( isPreviewProgramExecutingState.getExecutingProgramContainer() != null ) {
			org.lgna.croquet.views.SwingComponentView<?> executingProgramContainer = isPreviewProgramExecutingState.getExecutingProgramContainer();
			executingProgramContainer.setBorder( javax.swing.BorderFactory.createEmptyBorder() );
			sceneViewPanel.add( executingProgramContainer.getAwtComponent(), java.awt.BorderLayout.CENTER );
		} else {
			clear();
		}

		// this forces the play button to reappear
		this.revalidateAndRepaint();
	}

	class JEventIgnoringPanel extends javax.swing.JPanel {
		public JEventIgnoringPanel() {
			this.disableEvents( java.awt.AWTEvent.MOUSE_EVENT_MASK );
		}

		@Override
		public boolean contains( int x, int y ) {
			return false;
		}

		@Override
		public void paint( java.awt.Graphics g ) {
			super.paint( g );

			if( !isPreviewProgramExecutingState.getValue() || PreviewWorldPanel.this.paintScene.get() ) {
				java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;
				if( backgroundImage != null ) {
					g2.setRenderingHint( java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC );
					g2.drawImage( backgroundImage, 0, 0, this.getWidth(), this.getHeight(), null );
				}
			} else {
			}
		}
	}

	class JPreviewAnimationPanel extends javax.swing.JPanel {
		private boolean isMouseWithin;

		public JPreviewAnimationPanel() {
			this.enableEvents( java.awt.AWTEvent.MOUSE_EVENT_MASK );
		}

		@Override
		public void paint( java.awt.Graphics g ) {
			super.paint( g );

			if( isPreviewProgramExecutingState.getValue() ) {
				// pass
			} else {
				java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;
				if( isPlayAffordanceDesired ) {
					if( this.isMouseWithin ) {
						float xCenter = this.getWidth() * 0.5f;
						float yCenter = this.getHeight() * 0.5f;
						g2.setPaint( new java.awt.Color( 255, 255, 191, 9 ) );
						for( float radius = 50.0f; radius < 80.0f; radius += 4.0f ) {
							java.awt.geom.Ellipse2D.Float shape = new java.awt.geom.Ellipse2D.Float( xCenter - radius, yCenter - radius, radius + radius, radius + radius );
							g2.fill( shape );
						}
					}
					playIcon.paintIcon( this, g, ( this.getWidth() - ICON_SIZE ) / 2, ( this.getHeight() - ICON_SIZE ) / 2 );
				}
			}
		}

		@Override
		protected void processMouseEvent( java.awt.event.MouseEvent e ) {
			int eventId = e.getID();
			switch( eventId ) {
			case java.awt.event.MouseEvent.MOUSE_PRESSED:
				if( isPlayDisabled ) {
					// pass
				} else {
					Boolean isCurrentlyExecuting = isPreviewProgramExecutingState.getValue();
					if( isCurrentlyExecuting ) {
						( (PreviewProjectComposite)getComposite() ).stopPreview();
						isPlayAffordanceDesired = true;
					} else {
						( (PreviewProjectComposite)getComposite() ).playPreview();
						isPlayAffordanceDesired = false;
					}
				}

				this.repaint();
				break;
			case java.awt.event.MouseEvent.MOUSE_ENTERED:
				this.isMouseWithin = true && validProjectState.getValue();
				this.repaint();
				break;
			case java.awt.event.MouseEvent.MOUSE_EXITED:
				this.isMouseWithin = false;
				this.repaint();
				break;
			}
			super.processMouseEvent( e );
		}
	}

}
