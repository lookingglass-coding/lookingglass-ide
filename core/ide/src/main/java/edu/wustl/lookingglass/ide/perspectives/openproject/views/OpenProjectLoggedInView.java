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

import org.lgna.croquet.views.Button;
import org.lgna.croquet.views.ExternalHyperlink;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;

import edu.wustl.lookingglass.ide.croquet.models.community.CommunityLogoutOperation;
import edu.wustl.lookingglass.ide.perspectives.openproject.OpenProjectLoggedInCard;

/**
 * @author Michael Pogran
 */
public class OpenProjectLoggedInView extends MigPanel {

	private final Label messageLabel;
	private final Label avatar;

	public OpenProjectLoggedInView( OpenProjectLoggedInCard composite ) {
		super( composite, "ins 0 10 0 10, fill", "[100!][][]" );

		avatar = new Label();
		avatar.setBackgroundColor( java.awt.Color.WHITE );
		avatar.setBorder( javax.swing.BorderFactory.createLineBorder( new java.awt.Color( 208, 208, 208 ), 2 ) );
		avatar.setHorizontalAlignment( org.lgna.croquet.views.HorizontalAlignment.CENTER );
		avatar.setVerticalAlignment( org.lgna.croquet.views.VerticalAlignment.CENTER );
		this.addComponent( avatar, "cell 0 0, spany 4, aligny t, h 90!, w 90!" );

		messageLabel = new Label( "", 1.5f, edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );
		this.addComponent( messageLabel, "cell 1 0" );

		Button logoutButton = CommunityLogoutOperation.getInstance().createButton();
		this.addComponent( logoutButton, "cell 2 0, align r" );

		ExternalHyperlink accountLink = composite.getUserPageOperation().createExternalHyperlink();
		this.addComponent( accountLink );
		this.addComponent( accountLink, "cell 1 1, aligny top, pushy" );
	}

	public Label getAvatarLabel() {
		return this.avatar;
	}

	public Label getMessageLabel() {
		return this.messageLabel;
	}
}
