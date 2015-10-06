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
package edu.wustl.lookingglass.ide.croquet.models.community.views;

/**
 * @author Caitlin Kelleher
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.border.MatteBorder;

import org.alice.ide.uricontent.UriContentLoader;
import org.lgna.croquet.views.HorizontalAlignment;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;

import edu.wustl.lookingglass.ide.uricontent.CommunityProjectLoader;
import edu.wustl.lookingglass.ide.uricontent.CommunitySnippetLoader;

public class ContentInfoListCellRenderer extends MigPanel implements ListCellRenderer<UriContentLoader<?>> {

	private JTextArea titlePane;
	private Label labelThumbnail;
	private JTextArea descriptionPane;

	final static java.awt.Color SELECTED_BACKGROUND = new java.awt.Color( 255, 255, 200 );
	final static java.awt.Color UNSELECTED_BACKGROUND = new java.awt.Color( 240, 255, 255 );

	private final boolean DEBUG_COMMUNITY = false;

	public ContentInfoListCellRenderer() {
		super( null, "", "[][]", "[][]" );
		this.setOpaque( true );

		setBorder( new MatteBorder( 0, 0, 1, 0, (Color)Color.GRAY ) );

		titlePane = new JTextArea();
		titlePane.setWrapStyleWord( true );
		titlePane.setLineWrap( true );
		titlePane.setEditable( false );
		titlePane.setBorder( null );
		titlePane.setBackground( UNSELECTED_BACKGROUND );
		titlePane.setFont( new Font( "Arial", Font.BOLD, 14 ) );
		this.getAwtComponent().add( titlePane, "cell 1 0, grow, wmin 185" );

		descriptionPane = new JTextArea();
		descriptionPane.setWrapStyleWord( true );
		descriptionPane.setLineWrap( true );
		descriptionPane.setEditable( false );
		descriptionPane.setBorder( null );
		descriptionPane.setBackground( UNSELECTED_BACKGROUND );
		this.getAwtComponent().add( descriptionPane, "cell 1 1,aligny top, grow, wmin 185" );

		labelThumbnail = new Label( "" );
		addComponent( labelThumbnail, "cell 0 0 1 2" );
		labelThumbnail.setHorizontalAlignment( HorizontalAlignment.CENTER );
	}

	@Override
	public Component getListCellRendererComponent( JList<? extends UriContentLoader<?>> list, UriContentLoader<?> value, int index, boolean isSelected, boolean cellHasFocus ) {
		assert ( value != null );

		String title = value.getTitle();

		if( title == null ) {
			title = "untitled";
		} else if( title.length() > 40 ) {
			title = title.substring( 0, 40 ) + "…";
			int lastSpace = title.lastIndexOf( " " );
			if( lastSpace != -1 ) {
				title = title.substring( 0, lastSpace ) + "…";
			}
		}

		titlePane.setText( title );

		java.awt.Image thumbnailImage = null;
		try {
			thumbnailImage = value.getThumbnail( list );
		} catch( Exception e ) {
			e.printStackTrace();
		}

		if( thumbnailImage == null ) {
			labelThumbnail.setIcon( new AbstractNotAvailableIcon() );
		} else {
			double aspectRatio = (double)thumbnailImage.getHeight( null ) / (double)thumbnailImage.getWidth( null );
			if( aspectRatio == .75 ) {
				java.awt.image.BufferedImage clipped = new java.awt.image.BufferedImage( 215, 121, java.awt.image.BufferedImage.TYPE_INT_RGB );
				java.awt.Graphics2D g2 = (java.awt.Graphics2D)clipped.getGraphics();
				g2.setRenderingHint( java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC );
				g2.drawImage( thumbnailImage, 27, 0, 161, 121, java.awt.Color.BLACK, null );
				labelThumbnail.setIcon( new edu.cmu.cs.dennisc.javax.swing.icons.ScaledIcon( new ImageIcon( clipped ), 215, 121 ) );
			}
			else {
				labelThumbnail.setIcon( new edu.cmu.cs.dennisc.javax.swing.icons.ScaledIcon( new ImageIcon( thumbnailImage ), 215, 121 ) );
			}
		}

		String description = value.getDescription();

		if( description == null ) {
			description = "no description";
		} else if( description.length() > 100 ) {
			description = description.substring( 0, 100 ) + "…";

			int lastSpace = description.lastIndexOf( " " );
			if( lastSpace != -1 ) {
				description = description.substring( 0, lastSpace ) + "…";
			}
		}

		if( DEBUG_COMMUNITY ) {
			if( value instanceof CommunityProjectLoader ) {
				assert value != null;
				assert ( (CommunityProjectLoader)value ).getProjectPacket() != null;
				description += "          WORLD ID: " + ( (CommunityProjectLoader)value ).getProjectPacket().getId();
			} else if( value instanceof CommunitySnippetLoader ) {
				assert value != null;
				assert ( (CommunitySnippetLoader)value ).getRemixPacket() != null;
				description += "          REMIX ID: " + ( (CommunitySnippetLoader)value ).getRemixPacket().getId() + "   WORLD ID: " + ( (CommunitySnippetLoader)value ).getRemixPacket().getWorldId();
			}
		}
		descriptionPane.setText( description );

		if( isSelected ) {
			this.setBackgroundColor( SELECTED_BACKGROUND );
			this.titlePane.setBackground( SELECTED_BACKGROUND );
			this.descriptionPane.setBackground( SELECTED_BACKGROUND );
		} else {
			this.setBackgroundColor( UNSELECTED_BACKGROUND );
			this.titlePane.setBackground( UNSELECTED_BACKGROUND );
			this.descriptionPane.setBackground( UNSELECTED_BACKGROUND );
		}
		return this.getAwtComponent();
	}

	private static final class AbstractNotAvailableIcon implements javax.swing.Icon {
		@Override
		public int getIconWidth() {
			return 215;
		}

		@Override
		public int getIconHeight() {
			return 121;
		}

		protected String getText() {
			return "loading...";
		}

		@Override
		public void paintIcon( java.awt.Component c, java.awt.Graphics g, int x, int y ) {
			int width = this.getIconWidth();
			int height = this.getIconHeight();

			( (java.awt.Graphics2D)g ).setRenderingHint( java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
			( (java.awt.Graphics2D)g ).setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

			g.setColor( java.awt.Color.DARK_GRAY );
			g.fillRect( x, y, width, height );
			g.setColor( java.awt.Color.LIGHT_GRAY );
			edu.cmu.cs.dennisc.java.awt.GraphicsUtilities.drawCenteredText( g, this.getText(), x, y, width, height );
		}
	}
}
