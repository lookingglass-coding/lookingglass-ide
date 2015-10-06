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

import java.awt.Color;

import org.lgna.croquet.SimpleComposite;
import org.lgna.croquet.StringState;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;

import edu.wustl.lookingglass.ide.community.connection.observer.CommunityLoginObserver;
import edu.wustl.lookingglass.ide.croquet.models.community.CommunityLoginOperation;

/**
 * @author Caitlin Kelleher
 */
public class OneLineLoginUserAnonymousAccessCard extends SimpleComposite<MigPanel> implements CommunityLoginObserver {
	private final Label errorLabel = new Label();

	public OneLineLoginUserAnonymousAccessCard() {
		super( java.util.UUID.fromString( "499e3421-f02b-417f-a314-5f45b8ebdc84" ) );
		errorLabel.setForegroundColor( Color.RED );
	}

	@Override
	protected MigPanel createView() {
		MigPanel view = new MigPanel( this, "fill", "0[grow]10[align right]10[align right]5[align right]10[align right]5[align right]10[align right]", "[]" );

		StringState userNameStringState = CommunityLoginOperation.getInstance().getUserNameStringState();
		StringState passwordStringState = CommunityLoginOperation.getInstance().getPasswordStringState();

		view.addComponent( errorLabel );
		view.addComponent( new Label( "Created by:" ) );

		view.addComponent( userNameStringState.getSidekickLabel().createLabel() );
		view.addComponent( userNameStringState.createTextField(), "width 100" );

		view.addComponent( passwordStringState.getSidekickLabel().createLabel() );
		view.addComponent( passwordStringState.createPasswordField(), "width 100" );

		view.addComponent( CommunityLoginOperation.getInstance().createButton() );

		return view;
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		CommunityLoginOperation.getInstance().addCommunityLoginListener( this );
	}

	@Override
	public void handlePostDeactivation() {
		super.handlePostDeactivation();
		CommunityLoginOperation.getInstance().removeCommunityLoginListener( this );
	}

	@Override
	public void loginAttemptBeginning() {
		errorLabel.setText( "" );
	}

	@Override
	public void loginAttemptEnding() {
		errorLabel.setText( "" );
	}

	@Override
	public void loginErrorOccurred( String userMessage ) {
		errorLabel.setText( "Login Error - try again?" );
	}

}
