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

import org.lgna.croquet.views.BorderPanel;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.MultiLineLabel;
import org.lgna.croquet.views.PlainMultiLineLabel;

import edu.cmu.cs.dennisc.java.awt.font.TextWeight;
import edu.cmu.cs.dennisc.javax.swing.border.EmptyBorder;
import edu.wustl.lookingglass.ide.views.OverlayPlayIcon;

public class PreviewWithTitleDescriptionPanel extends BorderPanel {

	private final MultiLineLabel<?> titleLabel;
	private final MultiLineLabel<?> descriptionLabel;

	public PreviewWithTitleDescriptionPanel( org.lgna.croquet.views.AwtComponentView<?> previewComponent ) {
		super( 0, 4 );
		this.setBorder( new EmptyBorder( 4, 4, 4, 4 ) );
		this.setBackgroundColor( javax.swing.UIManager.getColor( "Panel.background" ) );

		// set up the title line
		titleLabel = new PlainMultiLineLabel( "", 2.0f, TextWeight.BOLD );
		MigPanel titlePanel = new MigPanel( null, "fillx, inset 0", "", "" );
		titlePanel.addComponent( titleLabel, "growx" );
		this.addPageStartComponent( titlePanel );

		this.addCenterComponent( previewComponent );

		// this should be turned into a model of the world that can generate it's own
		// editable and uneditable components. seems like we want to wrap worldpacket somehow
		// into a model then we can also make worlds on disk and on the community behave
		// more or less the same in the code.
		descriptionLabel = new PlainMultiLineLabel();
		descriptionLabel.setMinimumPreferredHeight( 100 );
		this.addPageEndComponent( descriptionLabel );
	}

	public void setSelectedUriContentLoader( org.alice.ide.uricontent.UriContentLoader<?> uriContentLoader ) {
		if( uriContentLoader != null ) {
			this.setTitle( uriContentLoader.getTitle() );
			this.setDescription( uriContentLoader.getDescription() );
		} else {
			this.setTitle( null );
			this.setDescription( null );
		}
	}

	private void setTitle( String title ) {
		titleLabel.setText( title );
	}

	private void setDescription( String description ) {
		descriptionLabel.setText( description );
	}

	final int ICON_SIZE = 80;
	final OverlayPlayIcon playIcon = new OverlayPlayIcon( new java.awt.Dimension( ICON_SIZE, ICON_SIZE ) );
}
