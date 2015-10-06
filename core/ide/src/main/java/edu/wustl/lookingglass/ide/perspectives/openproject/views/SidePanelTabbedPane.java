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
package edu.wustl.lookingglass.ide.perspectives.openproject.views;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.lgna.croquet.BooleanState;
import org.lgna.croquet.Operation;
import org.lgna.croquet.TabState;
import org.lgna.croquet.views.BooleanStateButton;
import org.lgna.croquet.views.CardBasedTabbedPane;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;

import edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.OpenProjectTab;

public class SidePanelTabbedPane extends CardBasedTabbedPane<OpenProjectTab> {
	private final MigPanel sidePanel = new CustomSidePanel( null, "fill", "[]", "[grow 0][grow 0][grow 100]" );
	private final MigPanel tabButtonsPanel = new MigPanel( null, "fillx" );
	private final CustomReturnButton returnButton;

	public SidePanelTabbedPane( TabState<OpenProjectTab, ?> model, Operation returnOperation ) {
		super( (TabState)model );
		Label lgIconLabel = new Label( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "start-logo", org.lgna.croquet.icon.IconSize.FIXED ) );
		lgIconLabel.setHorizontalAlignment( org.lgna.croquet.views.HorizontalAlignment.CENTER );
		this.returnButton = new CustomReturnButton( returnOperation );

		this.sidePanel.addComponent( lgIconLabel, "cell 0 0, gaptop 10, center" );
		this.sidePanel.addComponent( tabButtonsPanel, "cell 0 1, center" );
		this.sidePanel.addComponent( returnButton, "cell 0 2, aligny bottom, h 100!, w 120!" );
	}

	public MigPanel getSidePanel() {
		return this.sidePanel;
	}

	@Override
	protected javax.swing.JPanel createAwtComponent() {
		javax.swing.JPanel rv = super.createAwtComponent();
		rv.add( this.sidePanel.getAwtComponent(), java.awt.BorderLayout.LINE_START );
		rv.add( this.getCardOwner().getView().getAwtComponent(), java.awt.BorderLayout.CENTER );
		rv.setBorder( javax.swing.BorderFactory.createEmptyBorder() );
		return rv;
	}

	@Override
	protected LayoutManager createLayoutManager( JPanel jPanel ) {
		return new BorderLayout( 6, 0 );
	}

	@Override
	protected BooleanStateButton<? extends AbstractButton> createTitleButton( OpenProjectTab item, BooleanState itemSelectedState ) {
		return new CustomToggleButton( itemSelectedState );
	}

	@Override
	protected void removeAllDetails() {
		super.removeAllDetails();
		this.tabButtonsPanel.removeAllComponents();
	}

	@Override
	protected void addItem( OpenProjectTab item, BooleanStateButton<?> button ) {
		super.addItem( item, button );
		button.setHorizontalTextPosition( org.lgna.croquet.views.HorizontalTextPosition.CENTER );
		button.setVerticalTextPosition( org.lgna.croquet.views.VerticalTextPosition.BOTTOM );
		button.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
		this.tabButtonsPanel.addComponent( button, "grow, gaptop 10, wrap" );
	}

	public CustomReturnButton getReturnButton() {
		return this.returnButton;
	}

	public void showReturnButton() {
		getReturnButton().setVisible( true );
	}

	public void setReturnIcon( BufferedImage image ) {
		getReturnButton().setReturnImage( image );
		returnButton.revalidateAndRepaint();
	}

	@Override
	protected void handleUndisplayable() {
		this.returnButton.setReturnImage( null );
		super.handleUndisplayable();
	}

	private class CustomReturnButton extends org.lgna.croquet.views.Button {
		Image returnImage = null;

		public CustomReturnButton( Operation model ) {
			super( model );
			this.setVisible( false );
			this.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
		}

		public void setReturnImage( Image returnImage ) {
			this.returnImage = returnImage;
		}

		@Override
		protected JButton createAwtComponent() {
			return new JButton() {
				@Override
				protected void paintComponent( Graphics g ) {
					int w = getWidth();
					int h = getHeight();

					Graphics2D g2 = (Graphics2D)g;
					g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
					g2.setClip( -2, -2, w + 15, h + 4 );

					Shape origClip = g2.getClip();

					GeneralPath path = new GeneralPath();
					path.moveTo( 5, 0 );
					path.quadTo( 0, 0, 0, 5 );
					path.lineTo( 0, h - 5 );
					path.quadTo( 0, h, 5, h );
					path.lineTo( w - 5, h );
					path.quadTo( w, h, w, h - 5 );
					if( this.isSelected() ) {
						path.lineTo( w, ( h / 2 ) + 8 );
						path.lineTo( w + 8, ( h / 2 ) );
						path.lineTo( w, ( h / 2 ) - 8 );
					}
					path.lineTo( w, 5 );
					path.quadTo( w, 0, w - 5, 0 );
					path.closePath();

					GradientPaint paint;
					if( getAwtComponent().getModel().isPressed() ) {
						paint = new GradientPaint( 0, 0, new Color( 152, 161, 218 ), 0, getHeight() / 2, new Color( 134, 142, 192 ) );
					}
					else if( getAwtComponent().getModel().isRollover() ) {
						paint = new GradientPaint( 0, 0, new Color( 191, 199, 255 ), 0, getHeight() / 2, new Color( 170, 180, 243 ) );
					} else {
						paint = new GradientPaint( 0, 0, new Color( 170, 180, 243 ), 0, getHeight() / 2, new Color( 151, 160, 217 ) );
					}
					g2.setPaint( paint );
					g2.fill( path );

					g2.setPaint( new Color( 99, 106, 141 ) );
					g2.setStroke( new BasicStroke( 1.25f ) );
					g2.draw( path );

					g2.setPaint( Color.BLACK );
					g2.setFont( new Font( " SansSerif", Font.BOLD, 11 ) );
					String text = getText();
					int stringWidth = (int)g2.getFontMetrics().getStringBounds( text, g2 ).getWidth();
					g2.drawString( text, ( w - stringWidth ) / 2, h - 5 );

					java.awt.geom.RoundRectangle2D rect = new java.awt.geom.RoundRectangle2D.Double( 10, 10, 100, 56, 5, 5 );
					g2.setClip( rect );
					g2.drawImage( returnImage, 10, 10, 100, 56, null );
					g2.setClip( origClip );
					g2.setStroke( new BasicStroke( 1.35f ) );
					g2.draw( rect );
				}
			};
		}
	}

	private class CustomToggleButton extends org.lgna.croquet.views.ToggleButton {

		public CustomToggleButton( BooleanState model ) {
			super( model );
		}

		@Override
		protected JToggleButton createAwtComponent() {
			return new JToggleButton() {

				@Override
				protected void paintComponent( Graphics g ) {
					int w = getWidth();
					int h = getHeight();

					Graphics2D g2 = (Graphics2D)g;
					g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
					g2.setClip( -2, -2, w + 15, h + 4 );

					GeneralPath path = new GeneralPath();
					path.moveTo( 5, 0 );
					path.quadTo( 0, 0, 0, 5 );
					path.lineTo( 0, h - 5 );
					path.quadTo( 0, h, 5, h );
					path.lineTo( w - 5, h );
					path.quadTo( w, h, w, h - 5 );
					if( this.isSelected() ) {
						path.lineTo( w, ( h / 2 ) + 8 );
						path.lineTo( w + 8, ( h / 2 ) );
						path.lineTo( w, ( h / 2 ) - 8 );
					}
					path.lineTo( w, 5 );
					path.quadTo( w, 0, w - 5, 0 );
					path.closePath();

					GradientPaint paint;
					if( getAwtComponent().isSelected() ) {
						paint = new GradientPaint( 0, 0, new Color( 170, 180, 243 ), 0, getHeight() / 2, new Color( 151, 160, 217 ) );
					}
					else if( getAwtComponent().getModel().isPressed() ) {
						paint = new GradientPaint( 0, 0, new Color( 152, 161, 218 ), 0, getHeight() / 2, new Color( 134, 142, 192 ) );
					}
					else if( getAwtComponent().getModel().isRollover() ) {
						paint = new GradientPaint( 0, 0, new Color( 191, 199, 255 ), 0, getHeight() / 2, new Color( 170, 180, 243 ) );
					}
					else {
						paint = new GradientPaint( 0, 0, new Color( 170, 180, 243 ), 0, getHeight() / 2, new Color( 151, 160, 217 ) );
					}
					g2.setPaint( paint );
					g2.fill( path );

					if( this.isSelected() ) {
						g2.setPaint( Color.WHITE );
						g2.setStroke( new BasicStroke( 2.5f ) );
					} else {
						g2.setPaint( new Color( 99, 106, 141 ) );
						g2.setStroke( new BasicStroke( 1.25f ) );
					}
					g2.draw( path );

					javax.swing.Icon icon = this.getIcon();
					if( icon != null ) {
						int iconHeight = icon.getIconHeight();
						int iconWidth = icon.getIconWidth();
						icon.paintIcon( this, g2, ( w - iconWidth ) / 2, ( ( h - iconHeight ) / 2 ) - 5 );
					}

					if( this.isSelected() ) {
						g2.setPaint( Color.WHITE );
					} else {
						g2.setPaint( Color.BLACK );
					}
					g2.setFont( new Font( " SansSerif", Font.BOLD, 12 ) );
					String text = getText();
					int stringWidth = (int)g2.getFontMetrics().getStringBounds( text, g2 ).getWidth();
					g2.drawString( text, ( w - stringWidth ) / 2, h - 5 );
				}
			};
		}
	}

	private class CustomSidePanel extends MigPanel {

		public CustomSidePanel( org.lgna.croquet.Composite<?> composite, String layoutConstraints, String columnConstraints, String rowConstraints ) {
			super( composite, layoutConstraints, columnConstraints, rowConstraints );
		}

		@Override
		protected javax.swing.JPanel createJPanel() {
			javax.swing.JPanel rv = new DefaultJPanel() {
				@Override
				public void paint( java.awt.Graphics g ) {
					int w = this.getWidth();
					int h = this.getHeight();
					Graphics2D g2 = (Graphics2D)g.create();
					g2.setClip( -1, -1, w + 2, h + 2 );
					g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

					java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
					path.moveTo( 0, 5 );
					path.lineTo( 0, h - 5 );
					path.curveTo( 0, h - 5, 0, h, 5, h );
					path.lineTo( w, h );
					path.lineTo( w, 0 );
					path.lineTo( 5, 0 );
					path.curveTo( 5, 0, 0, 0, 0, 5 );
					path.closePath();

					g2.setColor( new Color( 211, 215, 240 ) );
					g2.fill( path );

					g2.setColor( new Color( 97, 96, 94 ) );
					g2.setStroke( new BasicStroke( 1 ) );
					g2.draw( path );

					super.paint( g );
				}
			};
			return rv;
		}
	}
}
