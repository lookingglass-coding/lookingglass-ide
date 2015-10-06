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

import javax.swing.BorderFactory;

import org.alice.ide.uricontent.FileProjectLoader;
import org.alice.ide.uricontent.UriProjectLoader;
import org.lgna.croquet.views.Button;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.MultiLineLabel;
import org.lgna.croquet.views.PlainMultiLineLabel;

import edu.cmu.cs.dennisc.java.awt.font.TextWeight;
import edu.wustl.lookingglass.ide.croquet.models.preview.PreviewProjectComposite;
import edu.wustl.lookingglass.ide.perspectives.openproject.OpenProjectAbstractDetailComposite;

/**
 * @author Michael Pogran
 */
public class OpenProjectPreviewWorldPanel extends OpenProjectPreviewPanel {
	private final MultiLineLabel<?> titleLabel, descriptionLabel;
	private final Label dateModifiedLabel;
	private final Button openButton;

	public OpenProjectPreviewWorldPanel( PreviewProjectComposite previewComponent, OpenProjectAbstractDetailComposite composite ) {
		super( null, "fill", "[]", "[grow][][grow]" );

		titleLabel = new PlainMultiLineLabel( "", 1.5f, TextWeight.BOLD );
		titleLabel.setBackgroundColor( null );
		dateModifiedLabel = new Label( "", 0.8f, TextWeight.MEDIUM );

		// this should be turned into a model of the world that can generate it's own
		// editable and uneditable components. seems like we want to wrap worldpacket somehow
		// into a model then we can also make worlds on disk and on the community behave
		// more or less the same in the code.

		MigPanel previewPanel = new MigPanel( null, "fill", "[]", "[grow]5[grow 0]" );
		previewPanel.addComponent( previewComponent.getView(), "cell 0 0, grow, top" );
		previewPanel.addComponent( titleLabel, "cell 0 1, grow x, top" );
		previewPanel.setBackgroundColor( java.awt.Color.WHITE );
		previewPanel.setBorder( BorderFactory.createLineBorder( new java.awt.Color( 208, 208, 208 ), 2 ) );

		Label promptLabel = new Label( "Description:", 1.25f, TextWeight.BOLD );
		descriptionLabel = new PlainMultiLineLabel();
		descriptionLabel.setBackgroundColor( null );
		descriptionLabel.setBorder( null );

		MigPanel descriptionPanel = new MigPanel( null, "fill", "[grow]", "[grow 0]10[grow, 125::]5[grow 0]" );
		descriptionPanel.addComponent( promptLabel, "cell 0 0, growx" );
		descriptionPanel.addComponent( descriptionLabel, "cell 0 1, grow" );
		descriptionPanel.addComponent( dateModifiedLabel, "cell 0 2, alignx right" );
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

	public void setSelectedUriContentLoader( UriProjectLoader uriProjectLoader ) {
		if( uriProjectLoader != null ) {
			titleLabel.setText( uriProjectLoader.getTitle() );
			descriptionLabel.setText( uriProjectLoader.getDescription() );

			if( uriProjectLoader instanceof FileProjectLoader ) {
				FileProjectLoader fpl = (FileProjectLoader)uriProjectLoader;
				this.dateModifiedLabel.setText( "last modified " + fpl.getModifiedDate() );
			}

		} else {
			titleLabel.setText( "" );
			descriptionLabel.setText( "" );
		}
	}
}
