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
package edu.wustl.lookingglass.remix.share.views;

import org.lgna.croquet.OverlayPane;

import edu.wustl.lookingglass.ide.community.connection.CommunityLoginComposite;

/**
 * @author Michael Pogran
 */
public class PreviewChallengeView extends org.lgna.croquet.views.MigPanel {
	private final javax.swing.border.Border tileBorder = javax.swing.BorderFactory.createMatteBorder( 1, 1, 2, 1, new java.awt.Color( 177, 177, 192 ) );

	private org.lgna.croquet.views.LayerStencil loginStencil;
	private org.lgna.croquet.views.Label posterLabel;
	private org.lgna.croquet.views.Label warningLabel;

	public PreviewChallengeView( edu.wustl.lookingglass.remix.share.PreviewChallengePage composite ) {
		super( composite, "fillx", "[]", "[][][][]" );
		setBackgroundColor( edu.wustl.lookingglass.remix.share.ShareTemplateComposite.COMPOSITE_COLOR );

		// add instructions
		addComponent( composite.getInstructionsString().createLabel( 1.5f, edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD ), "cell 0 0, spanx 2, gap 0 0 20 10" );

		// add poster label
		this.posterLabel = new org.lgna.croquet.views.Label();
		this.posterLabel.setBackgroundColor( java.awt.Color.WHITE );
		this.posterLabel.setBorder( javax.swing.BorderFactory.createCompoundBorder(
				javax.swing.BorderFactory.createMatteBorder( 1, 1, 2, 1, new java.awt.Color( 177, 177, 192 ) ),
				javax.swing.BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) ) );

		addComponent( this.posterLabel, "cell 0 1, center" );

		// add warning label
		this.warningLabel = new org.lgna.croquet.views.Label( "<html>" + composite.getCodeWarningString().getText() + "</html>" );
		this.warningLabel.setBackgroundColor( new java.awt.Color( 255, 204, 204 ) );
		this.warningLabel.setForegroundColor( new java.awt.Color( 51, 0, 0 ) );

		this.warningLabel.setBorder( javax.swing.BorderFactory.createCompoundBorder(
				javax.swing.BorderFactory.createLineBorder( new java.awt.Color( 102, 0, 0 ) ),
				javax.swing.BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) ) );
		this.warningLabel.setHorizontalAlignment( org.lgna.croquet.views.HorizontalAlignment.CENTER );

		addComponent( this.warningLabel, "cell 0 2, w 650, center, hidemode 3" );

		// add information panel
		org.lgna.croquet.views.MigPanel informationPanel = new org.lgna.croquet.views.MigPanel( null, "fill, gap 0", "[310]20[310]", "[]0[]5[]0[]" );
		informationPanel.setBackgroundColor( java.awt.Color.WHITE );
		informationPanel.setBorder( this.tileBorder );

		org.lgna.croquet.views.ImmutableTextField titleSidekick = composite.getOwner().getTitleState().getSidekickLabel().createImmutableTextField();
		org.lgna.croquet.views.TextField titleField = composite.getOwner().getTitleState().createTextField();

		org.lgna.croquet.views.ImmutableTextField descriptionSidekick = composite.getOwner().getDescriptionState().getSidekickLabel().createImmutableTextField();
		org.lgna.croquet.views.TextArea descriptionArea = composite.getOwner().getDescriptionState().createTextArea();
		descriptionArea.getAwtComponent().setWrapStyleWord( true );
		descriptionArea.getAwtComponent().setLineWrap( true );
		descriptionArea.setAlignmentX( 0.0f );

		org.lgna.croquet.views.ImmutableTextField tagSidekick = composite.getOwner().getTagState().getSidekickLabel().createImmutableTextField();
		org.lgna.croquet.views.TextField tagField = composite.getOwner().getTagState().createTextField();

		informationPanel.addComponent( titleSidekick, "cell 0 0" );
		informationPanel.addComponent( titleField, "cell 0 1, growx" );

		informationPanel.addComponent( tagSidekick, "cell 0 2" );
		informationPanel.addComponent( tagField, "cell 0 3, growx" );

		informationPanel.addComponent( descriptionSidekick, "cell 1 0" );
		informationPanel.addComponent( descriptionArea, "cell 1 1, grow, spany 3" );

		addComponent( informationPanel, "cell 0 3, center, h 150" );
	}

	@Override
	public edu.wustl.lookingglass.remix.share.PreviewChallengePage getComposite() {
		return (edu.wustl.lookingglass.remix.share.PreviewChallengePage)super.getComposite();
	}

	public void setLoginDialogShowing( boolean isShowing ) {
		javax.swing.SwingUtilities.invokeLater( () -> {
			if( isShowing ) {
				if( this.loginStencil == null ) {
					this.loginStencil = new OverlayPane.Builder( getRoot(), new CommunityLoginComposite().getView() ).closeOperation( getComposite().getOwner().getCancelOperation() ).build();
				}
				this.loginStencil.setStencilShowing( isShowing );
			} else {
				if( this.loginStencil == null ) {
					//pass
				} else {
					this.loginStencil.setStencilShowing( isShowing );
				}
			}
		} );
	}

	public void setPoster( javax.swing.Icon icon ) {
		this.posterLabel.setIcon( icon );
		this.posterLabel.revalidateAndRepaint();
	}

	public void setWarningVisible( boolean value ) {
		synchronized( getTreeLock() ) {
			this.warningLabel.setVisible( value );
		}
		this.revalidateAndRepaint();
	}

	@Override
	protected void handleUndisplayable() {
		this.loginStencil = null;
		super.handleUndisplayable();
	}
}
