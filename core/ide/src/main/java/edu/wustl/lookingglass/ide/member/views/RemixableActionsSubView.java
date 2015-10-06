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
package edu.wustl.lookingglass.ide.member.views;

import java.awt.RenderingHints;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;

import org.alice.ide.member.views.MethodsSubView;
import org.lgna.croquet.icon.IconSize;

/**
 * @author Kyle J. Harms
 */
public class RemixableActionsSubView extends MethodsSubView<edu.wustl.lookingglass.ide.member.RemixableActionsSubComposite> {

	private static final int MAXIMUM_TITLE_LENGTH = 20;

	private static final java.awt.Image playIcon = edu.wustl.lookingglass.ide.LookingGlassTheme.getImage( "media-playback-start", IconSize.LARGE );

	public RemixableActionsSubView( edu.wustl.lookingglass.ide.member.RemixableActionsSubComposite composite ) {
		super( composite );
	}

	@Override
	protected void internalRefresh() {
		super.internalRefresh();

		// Don't show this panel, if we weren't able to suggest anything
		if( getComposite().getListData().getItemCount() > 0 ) {
			org.lgna.croquet.views.MigPanel snippetsPanel = new org.lgna.croquet.views.MigPanel( null, "fill, ins 0", "[120!][120!][120!]", "[][]2[]" );

			// TODO: localize this label
			org.lgna.croquet.views.Label label = new org.lgna.croquet.views.Label( "Remixable Actions", edu.cmu.cs.dennisc.java.awt.font.TextPosture.OBLIQUE );
			snippetsPanel.addComponent( label, "span 3 1, cell 0 0" );

			for( int i = 0; i < getComposite().getListData().getItemCount(); i++ ) {
				edu.wustl.lookingglass.ide.uricontent.CommunitySnippetLoader loader = getComposite().getListData().getItemAt( i );

				edu.wustl.lookingglass.ide.croquet.models.preview.PreviewSnippetComposite previewSnippetComposite = getComposite().getPreviewComposites()[ i ];
				previewSnippetComposite.setSnippetLoader( loader );

				javax.swing.JButton button = createButton( loader, previewSnippetComposite.getLaunchOperation() );
				org.lgna.croquet.views.Hyperlink title = previewSnippetComposite.getLaunchOperation().createHyperlink();
				title.setClobberText( getTitle( loader.getTitle() ) );

				if( button != null ) {
					snippetsPanel.getAwtComponent().add( button, "cell " + i + " 1, w 79!, h 79!, center" );
					snippetsPanel.addComponent( title, "cell " + i + " 2, center" );
				}
			}

			this.addComponent( snippetsPanel );
			this.addComponent( org.lgna.croquet.views.BoxUtilities.createVerticalStrut( 4 ) );
		}
	}

	private String getTitle( String title ) {
		if( title.length() > MAXIMUM_TITLE_LENGTH ) {
			title = title.substring( 0, MAXIMUM_TITLE_LENGTH ) + "…";
			int lastSpace = title.lastIndexOf( " " );
			if( lastSpace != -1 ) {
				title = title.substring( 0, lastSpace ) + "…";
			}
		}
		return title;
	}

	private JButton createButton( edu.wustl.lookingglass.ide.uricontent.CommunitySnippetLoader loader, org.lgna.croquet.Operation operation ) {
		java.awt.Image thumbnail;
		try {
			thumbnail = loader.getThumbnailWaitingIfNecessary();
		} catch( InterruptedException | ExecutionException e ) {
			thumbnail = null;
		}

		if( thumbnail != null ) {
			// More efficient to scale with a BufferedImage
			// TODO: this should use getSize() and compute everything based on that...
			final java.awt.image.BufferedImage buttonImage = new java.awt.image.BufferedImage( 133, 75, java.awt.image.BufferedImage.TYPE_INT_ARGB );
			java.awt.Graphics2D g = buttonImage.createGraphics();

			g.drawImage( thumbnail, 0, 0, 133, 75, null );
			javax.swing.JButton button = new javax.swing.JButton( operation.getImp().getSwingModel().getAction() ) {
				@Override
				protected void paintComponent( java.awt.Graphics g ) {
					int PAD = 2;
					int SIZE = 75;
					java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;
					g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

					java.awt.geom.RoundRectangle2D rect = new java.awt.geom.RoundRectangle2D.Double( PAD, PAD, SIZE, SIZE, 8, 8 );

					g2.setPaint( new java.awt.Color( 0, 0, 0, 80 ) );
					g2.fillRoundRect( PAD, PAD + 1, SIZE, SIZE, 8, 8 );

					g2.clip( rect );
					g2.drawImage( buttonImage, -30, PAD, 133, 75, null );

					if( getModel().isPressed() ) {
						g2.setPaint( new java.awt.Color( 0, 0, 0, 100 ) );
						g2.fill( rect );
					}
					else if( getModel().isRollover() ) {
						g2.setPaint( new java.awt.Color( 255, 255, 255, 50 ) );
						g2.fill( rect );
					}

					g2.setPaint( new java.awt.Color( 0, 0, 0, 200 ) );
					g2.setStroke( new java.awt.BasicStroke( 1.25f ) );
					g2.drawRoundRect( PAD, PAD, SIZE - 1, SIZE - 1, 8, 8 );

					java.awt.geom.RoundRectangle2D insetRect = new java.awt.geom.RoundRectangle2D.Double( PAD + 1, PAD + 1, SIZE - 2, SIZE - 2, 6, 6 );
					java.awt.Color colorOne = new java.awt.Color( 255, 255, 255, 175 );
					java.awt.Color colorTwo = new java.awt.Color( 255, 255, 255, 140 );
					java.awt.Color colorThree = new java.awt.Color( 255, 255, 255, 0 );

					g2.setPaint( new java.awt.GradientPaint( 0, 0, colorOne, 0, 20, colorThree ) );
					g2.fill( insetRect );
					g2.setPaint( new java.awt.GradientPaint( 0, 73, colorTwo, 0, 40, colorThree ) );
					g2.fill( insetRect );

					// Play button
					if( getModel().isRollover() ) {
						final int offset = ( this.getWidth() / 2 ) - ( playIcon.getWidth( null ) / 2 );
						g.drawImage( playIcon, offset, offset, null );
					}

					g2.dispose();
				}
			};
			button.setCursor( new java.awt.Cursor( java.awt.Cursor.HAND_CURSOR ) );
			return button;
		} else {
			return null;
		}
	}
}
