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

/**
 * @author Michael Pogran
 */
import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.alice.ide.uricontent.UriContentLoader;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;

import edu.wustl.lookingglass.ide.croquet.components.NotAvailableIcon;

public class OpenProjectContentInfoListCellRenderer extends MigPanel implements ListCellRenderer<UriContentLoader<?>> {
	final static java.awt.Color SELECTED_BACKGROUND = new java.awt.Color( 63, 63, 127 );
	final static java.awt.Color UNSELECTED_BACKGROUND = java.awt.Color.WHITE;

	@Override
	public Component getListCellRendererComponent( JList<? extends UriContentLoader<?>> list, UriContentLoader<?> value, int index, boolean isSelected, boolean cellHasFocus ) {
		MigPanel panel = new MigPanel( null, "", "[]", "[][]" );
		panel.setOpaque( false );

		Label titlePane = new Label( "", 1.0f, edu.cmu.cs.dennisc.java.awt.font.TextWeight.HEAVY );
		titlePane.setHorizontalTextPosition( org.lgna.croquet.views.HorizontalTextPosition.CENTER );
		titlePane.setHorizontalAlignment( org.lgna.croquet.views.HorizontalAlignment.CENTER );
		titlePane.setOpaque( true );

		titlePane.setText( getTitleForCell( value ) );

		Label labelThumbnail = new Label();
		labelThumbnail.setHorizontalAlignment( org.lgna.croquet.views.HorizontalAlignment.CENTER );

		labelThumbnail.setIcon( getIconForCell( value, list ) );

		panel.addComponent( labelThumbnail, "wrap" );
		panel.addComponent( titlePane, "growx" );

		panel.setBorder( javax.swing.BorderFactory.createLineBorder( new java.awt.Color( 208, 208, 208 ), 2 ) );

		this.addComponent( panel, "grow" );
		this.setBorder( javax.swing.BorderFactory.createEmptyBorder() );

		if( isSelected ) {
			panel.setBackgroundColor( SELECTED_BACKGROUND );
			panel.setBorder( getBorder( javax.swing.BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) ) );
			titlePane.setBackgroundColor( SELECTED_BACKGROUND );
			titlePane.setForegroundColor( java.awt.Color.YELLOW );
		} else {
			panel.setBackgroundColor( UNSELECTED_BACKGROUND );
			panel.setBorder( getBorder( javax.swing.BorderFactory.createLineBorder( new java.awt.Color( 208, 208, 208 ), 2 ) ) );
			titlePane.setBackgroundColor( UNSELECTED_BACKGROUND );
			titlePane.setForegroundColor( java.awt.Color.BLACK );
		}
		return panel.getAwtComponent();
	}

	private javax.swing.border.Border getBorder( javax.swing.border.Border border ) {
		return javax.swing.BorderFactory.createCompoundBorder( javax.swing.BorderFactory.createMatteBorder( 5, 5, 5, 5, new Color( 211, 215, 240 ) ), border );
	}

	private javax.swing.Icon getIconForCell( UriContentLoader<?> loader, Component component ) {
		java.awt.Image thumbnailImage = loader.getThumbnail( component );
		java.awt.Dimension size = new java.awt.Dimension( 200, 113 );

		javax.swing.Icon rv;
		if( thumbnailImage == null ) {
			if( loader.exceptionThrown() ) {
				rv = new NotAvailableIcon( size, loader.getExceptionMessage() );
			} else {
				rv = new NotAvailableIcon( size, "loading..." );
			}
		} else {
			double aspectRatio = (double)thumbnailImage.getHeight( null ) / (double)thumbnailImage.getWidth( null );
			if( aspectRatio == .75 ) {
				java.awt.image.BufferedImage clipped = new java.awt.image.BufferedImage( 215, 121, java.awt.image.BufferedImage.TYPE_INT_RGB );
				java.awt.Graphics2D g2 = (java.awt.Graphics2D)clipped.getGraphics();
				g2.setRenderingHint( java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC );
				g2.drawImage( thumbnailImage, 27, 0, 161, 121, java.awt.Color.BLACK, null );

				rv = new edu.cmu.cs.dennisc.javax.swing.icons.ScaledIcon( new ImageIcon( clipped ), size );
			} else {
				rv = new edu.cmu.cs.dennisc.javax.swing.icons.ScaledIcon( new ImageIcon( thumbnailImage ), size );
			}
		}
		return rv;
	}

	private String getTitleForCell( UriContentLoader<?> loader ) {
		String title = loader.getTitle();

		if( title == null ) {
			title = "untitled";
		} else if( title.length() > 25 ) {
			title = title.substring( 0, 25 ) + "…";
			int lastSpace = title.lastIndexOf( " " );
			if( lastSpace != -1 ) {
				title = title.substring( 0, lastSpace ) + "…";
			}
		}
		return title;
	}
}
