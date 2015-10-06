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

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;

import org.lgna.croquet.views.AbstractLabel;
import org.lgna.croquet.views.Button;
import org.lgna.croquet.views.CheckBox;
import org.lgna.croquet.views.ExternalHyperlink;
import org.lgna.croquet.views.HorizontalAlignment;
import org.lgna.croquet.views.HorizontalTextPosition;
import org.lgna.croquet.views.HtmlMultiLineLabel;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.PasswordField;
import org.lgna.croquet.views.PlainMultiLineLabel;
import org.lgna.croquet.views.TextField;
import org.lgna.croquet.views.ViewController;

import edu.cmu.cs.dennisc.java.awt.font.TextWeight;
import edu.wustl.lookingglass.ide.croquet.models.community.CommunityLoginOperation;
import edu.wustl.lookingglass.ide.croquet.models.community.browseroperation.CreateAccountBrowseOperation;
import edu.wustl.lookingglass.ide.croquet.models.community.browseroperation.ResetPasswordBrowseOperation;
import edu.wustl.lookingglass.ide.croquet.preferences.CommunityPasswordState;
import edu.wustl.lookingglass.ide.croquet.preferences.CommunityUsernameState;
import edu.wustl.lookingglass.ide.croquet.preferences.PersistentCommunityCredentialsState;
import edu.wustl.lookingglass.ide.perspectives.openproject.OpenProjectLoginCard;
import edu.wustl.lookingglass.ide.views.SpinningProgressDial;

/**
 * @author Michael Pogran
 */
public class OpenProjectLoginView extends MigPanel {
	private final Label warningLabel;
	private final TextField usernameField;
	private final PasswordField passwordField;
	private final Button loginButton;
	private final SpinningProgressDial progressLogin;

	public OpenProjectLoginView( OpenProjectLoginCard composite ) {
		super( composite, "ins 0 10 0 10, fill", "[50%]20[][]", "[][][][][][][][grow]" );

		PlainMultiLineLabel welcomeLabel = new PlainMultiLineLabel( "Looking Glass is more fun when you're part of the community.  Login or Sign Up to join in the fun!", 1.15f, TextWeight.BOLD );
		welcomeLabel.setBackgroundColor( null );

		this.addComponent( welcomeLabel, "cell 0 0, spanx 3, grow, hmin 50" );

		HtmlMultiLineLabel signUpLabel = new HtmlMultiLineLabel( "<html><body style='text-align: center; font-family:sans-serif;'>Don't have an account? Click \"Sign up\" to get started.</body></html>", 1.0f, TextWeight.BOLD );
		signUpLabel.setBackgroundColor( null );

		this.addComponent( signUpLabel, "cell 0 1, top, spany 2, growx" );

		this.usernameField = CommunityUsernameState.getInstance().createTextField();
		this.usernameField.getAwtComponent().setColumns( 16 );
		this.usernameField.enableSelectAllWhenFocusGained();
		AbstractLabel usernameLabel = CommunityUsernameState.getInstance().getSidekickLabel().createLabel();

		this.addComponent( usernameLabel, "cell 1 1" );
		this.addComponent( usernameField, "cell 2 1, growx" );

		this.passwordField = CommunityPasswordState.getInstance().createPasswordField();
		this.passwordField.getAwtComponent().setColumns( 16 );
		this.passwordField.enableSelectAllWhenFocusGained();
		AbstractLabel passwordLabel = CommunityPasswordState.getInstance().getSidekickLabel().createLabel();

		this.addComponent( passwordLabel, "cell 1 2" );
		this.addComponent( passwordField, "cell 2 2, growx" );

		Button signUpButton = CreateAccountBrowseOperation.getInstance().createButton();
		signUpButton.setFont( signUpButton.getFont().deriveFont( Font.BOLD, 14 ) );
		signUpButton.getAwtComponent().setIcon( new org.lgna.croquet.icon.ExternalHyperlinkIcon( 18, Color.BLACK ) );
		signUpButton.getAwtComponent().setHorizontalTextPosition( javax.swing.SwingConstants.LEFT );
		signUpButton.getAwtComponent().setVerticalTextPosition( javax.swing.SwingConstants.CENTER );

		this.addComponent( signUpButton, "cell 0 3, center" );

		this.loginButton = CommunityLoginOperation.getInstance().createButton();
		this.loginButton.setFont( loginButton.getFont().deriveFont( Font.BOLD, 14 ) );
		composite.getFrame().setDefaultButton( loginButton );

		this.progressLogin = new SpinningProgressDial();
		this.progressLogin.setActuallyPainting( false );

		this.addComponent( progressLogin, "cell 1 3, span 2, center" );
		this.addComponent( loginButton, "cell 1 3, center" );

		CheckBox isRemembered = PersistentCommunityCredentialsState.getInstance().createCheckBox();

		this.addComponent( isRemembered, "cell 1 4, span 2" );

		ExternalHyperlink forgotLabel = ResetPasswordBrowseOperation.getInstance().createExternalHyperlink();

		this.addComponent( forgotLabel, "cell 1 5, span 2" );

		warningLabel = new Label( "warning", 1.0f, TextWeight.BOLD );
		warningLabel.setHorizontalAlignment( HorizontalAlignment.CENTER );
		warningLabel.setHorizontalTextPosition( HorizontalTextPosition.CENTER );
		warningLabel.setBackgroundColor( new Color( 232, 107, 120 ) );
		this.warningLabel.setBorder( BorderFactory.createLineBorder( Color.RED ) );
		warningLabel.setVisible( false );

		this.addComponent( warningLabel, "cell 0 6, spanx 3, grow" );
	}

	public void updateLoginAttemptBeginning() {
		this.clearErrorMessages();
		this.showProgressIndicatorAndDisableControls();
	}

	public void updateLoginAttemptEnding() {
		this.hideProgressIndicatorAndEnableControls();
	}

	public void updateErrorOccured( String userMessage ) {

		if( userMessage == null ) {
			this.clearErrorMessages();
		} else {
			this.displayErrorMessage( userMessage );
			this.usernameField.selectAll();
		}
		this.hideProgressIndicatorAndEnableControls();
	}

	private void setControlsEnabled( boolean isEnabled ) {
		for( ViewController<?, ?> viewController : new ViewController<?, ?>[] { this.usernameField, this.passwordField, this.loginButton } ) {
			viewController.getAwtComponent().setEnabled( isEnabled );
		}
	}

	private void showProgressIndicatorAndDisableControls() {
		this.progressLogin.setActuallyPainting( true );
		this.setControlsEnabled( false );
	}

	private void hideProgressIndicatorAndEnableControls() {
		this.progressLogin.setActuallyPainting( false );
		this.setControlsEnabled( true );
	}

	private void displayErrorMessage( String message ) {
		this.warningLabel.setText( message );
		synchronized( this.getTreeLock() ) {
			this.warningLabel.setVisible( true );
		}
	}

	private void clearErrorMessages() {
		this.warningLabel.setText( "warning" );
		synchronized( this.getTreeLock() ) {
			this.warningLabel.setVisible( false );
		}
	}
}
