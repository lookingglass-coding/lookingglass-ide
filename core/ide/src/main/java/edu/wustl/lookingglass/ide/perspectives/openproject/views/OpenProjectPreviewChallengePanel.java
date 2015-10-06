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

import java.awt.Image;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;

import org.alice.ide.uricontent.UriProjectLoader;
import org.lgna.croquet.views.Button;
import org.lgna.croquet.views.FixedAspectRatioPanel;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.PlainMultiLineLabel;

import edu.cmu.cs.dennisc.java.awt.font.TextWeight;
import edu.wustl.lookingglass.community.api.packets.TemplatePacket;
import edu.wustl.lookingglass.ide.croquet.models.preview.views.PreviewImagePanel;
import edu.wustl.lookingglass.ide.perspectives.openproject.OpenProjectAbstractDetailComposite;
import edu.wustl.lookingglass.ide.uricontent.CommunityProjectLoader;

/**
 * @author Michael Pogran
 */
public class OpenProjectPreviewChallengePanel extends OpenProjectPreviewPanel {
	private final PlainMultiLineLabel titleLabel;
	private final Label createdByLabel;
	private final Label viewsLabel;
	private final PreviewImagePanel imagePreview;
	private final PlainMultiLineLabel descriptionLabel;
	private final Button openButton;

	public OpenProjectPreviewChallengePanel( OpenProjectAbstractDetailComposite composite ) {
		super( composite, "fill", "[grow]", "[grow][][grow]" );

		titleLabel = new PlainMultiLineLabel( "", 1.5f, TextWeight.BOLD );
		titleLabel.setBackgroundColor( null );
		imagePreview = new PreviewImagePanel();
		createdByLabel = new Label( "", 1.2f, TextWeight.REGULAR );

		MigPanel previewPanel = new MigPanel( null, "fill", "[]", "[grow]10[grow 0]5[grow 0]" );
		previewPanel.addComponent( new FixedAspectRatioPanel( imagePreview, 1.77 ), "cell 0 0, grow" );
		previewPanel.addComponent( titleLabel, "cell 0 1, grow x" );
		previewPanel.addComponent( createdByLabel, "cell 0 2, grow x" );
		previewPanel.setBackgroundColor( java.awt.Color.WHITE );
		previewPanel.setBorder( BorderFactory.createLineBorder( new java.awt.Color( 208, 208, 208 ), 2 ) );

		Label promptLabel = new Label( "Description:", 1.25f, TextWeight.BOLD );
		descriptionLabel = new PlainMultiLineLabel();
		descriptionLabel.setBackgroundColor( null );
		viewsLabel = new Label( "", 1.2f, TextWeight.REGULAR );

		MigPanel descriptionPanel = new MigPanel( null, "fill", "[grow]", "[grow 0]10[grow, 125::]5[grow 0]" );
		descriptionPanel.addComponent( promptLabel, "cell 0 0, growx" );
		descriptionPanel.addComponent( descriptionLabel, "cell 0 1, grow" );
		descriptionPanel.addComponent( viewsLabel, "cell 0 2" );
		descriptionPanel.setBackgroundColor( java.awt.Color.WHITE );
		descriptionPanel.setBorder( BorderFactory.createLineBorder( new java.awt.Color( 208, 208, 208 ), 2 ) );

		openButton = composite.getLoadUriAction().createButton( 1.5f );

		this.addComponent( previewPanel, "cell 0 0, grow" );
		this.addComponent( descriptionPanel, "cell 0 1, grow" );
		this.addComponent( openButton, "cell 0 2, center, pushy" );
	}

	@Override
	protected Button getDefaultButton() {
		return this.openButton;
	}

	public void update( UriProjectLoader uriProjectLoader ) {
		titleLabel.setText( uriProjectLoader.getTitle() );
		descriptionLabel.setText( uriProjectLoader.getDescription() );
		this.imagePreview.setBackgroundImage( null );

		if( uriProjectLoader instanceof CommunityProjectLoader ) {
			CommunityProjectLoader communityLoader = (CommunityProjectLoader)uriProjectLoader;
			this.createdByLabel.setText( "created by " + communityLoader.getUsername() );

			TemplatePacket packet = (TemplatePacket)communityLoader.getProjectPacket();
			this.viewsLabel.setText( "<html><strong>views:</strong> " + packet.getViews() + "</html>" );
		} else {
			this.createdByLabel.setText( "" );
			this.viewsLabel.setText( "" );
		}

		try {
			Image snapshot = uriProjectLoader.getThumbnailWaitingIfNecessary();
			if( snapshot != null ) {
				snapshot = getImageForDisplay( snapshot );
				this.imagePreview.setBackgroundImage( snapshot );
			}
		} catch( InterruptedException | ExecutionException e ) {
			e.printStackTrace();
		}

		this.revalidateAndRepaint();
	}

	public Image getImageForDisplay( Image image ) {
		double aspectRatio = (double)image.getHeight( null ) / (double)image.getWidth( null );
		Image rv = null;

		if( aspectRatio == .75 ) {
			java.awt.image.BufferedImage clipped = new java.awt.image.BufferedImage( 640, 360, java.awt.image.BufferedImage.TYPE_INT_RGB );
			java.awt.Graphics2D g2 = (java.awt.Graphics2D)clipped.getGraphics();
			g2.setRenderingHint( java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC );
			g2.drawImage( image, 80, 0, 480, 360, java.awt.Color.BLACK, null );
			rv = clipped;
		}
		else {
			rv = image.getScaledInstance( 640, 360, java.awt.Image.SCALE_DEFAULT );
		}
		return rv;
	}
}
