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
package edu.wustl.lookingglass.ide.community.connection;

public final class CommunityIncompatibleApiCard extends org.lgna.croquet.SimpleComposite<org.lgna.croquet.views.Panel> {
	private final org.lgna.croquet.PlainStringValue message = this.createStringValue( "message" );
	private final org.lgna.croquet.PlainStringValue help = this.createStringValue( "help" );

	public CommunityIncompatibleApiCard() {
		super( java.util.UUID.fromString( "d7c2d9ab-42c3-4c34-98da-31a7d0f73b28" ) );
	}

	@Override
	protected org.lgna.croquet.views.Panel createView() {
		org.lgna.croquet.views.ImmutableTextArea messageLabel = this.message.createImmutableTextArea( 1.4f, edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );
		messageLabel.setBackgroundColor( null );
		messageLabel.setOpaque( false );

		org.lgna.croquet.views.ImmutableTextArea helpLabel = this.help.createImmutableTextArea();
		helpLabel.setBackgroundColor( null );
		helpLabel.setOpaque( false );
		org.lgna.croquet.views.ExternalHyperlink hyperlink = edu.wustl.lookingglass.ide.croquet.models.community.browseroperation.DownloadBrowserOperation.getInstance().createExternalHyperlink();

		org.lgna.croquet.views.MigPanel rv = new org.lgna.croquet.views.MigPanel( this, "fill", "", "[][][]push" );
		rv.addComponent( new org.lgna.croquet.views.Label( edu.cmu.cs.dennisc.javax.swing.IconUtilities.getErrorIcon() ), "cell 0 0" );
		rv.addComponent( messageLabel, "cell 0 0, growx" );
		rv.addComponent( helpLabel, "cell 0 1, growx, gapleft 60" );
		rv.addComponent( hyperlink, "cell 0 2, gapleft 60" );
		return rv;
	}
}
