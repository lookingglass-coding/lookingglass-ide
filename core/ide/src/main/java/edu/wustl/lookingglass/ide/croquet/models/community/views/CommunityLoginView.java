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

import javax.swing.BorderFactory;

import org.lgna.croquet.views.BorderPanel;
import org.lgna.croquet.views.Button;
import org.lgna.croquet.views.HorizontalAlignment;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.PasswordField;
import org.lgna.croquet.views.TextField;
import org.lgna.croquet.views.ViewController;

import edu.cmu.cs.dennisc.java.awt.font.TextPosture;
import edu.wustl.lookingglass.ide.community.connection.CommunityLoginComposite;
import edu.wustl.lookingglass.ide.community.connection.observer.CommunityLoginObserver;
import edu.wustl.lookingglass.ide.croquet.models.community.CommunityLoginOperation;
import edu.wustl.lookingglass.ide.views.SpinningProgressDial;

/**
 * @author Caitlin Kelleher
 */
public class CommunityLoginView extends BorderPanel implements CommunityLoginObserver {
	private final edu.wustl.lookingglass.ide.croquet.models.community.CommunityLoginOperation operation;
	private final SpinningProgressDial progressLogin;
	private final MigPanel panelNotify;
	private final Label labelNotify;
	private final TextField usernameField;
	private final PasswordField passwordField;
	private final Button loginButton;

	//todo: move the loginobserver logic into the composite

	public CommunityLoginView( CommunityLoginComposite communityLoginComposite ) {
		super( communityLoginComposite );
		this.setBorder( BorderFactory.createEmptyBorder( 4, 4, 4, 4 ) );

		//todo: remove
		this.operation = CommunityLoginOperation.getInstance();

		this.panelNotify = new MigPanel( null, "insets 5", "0[grow]0", "0[20px:n]0" );
		this.panelNotify.setBorder( null );
		//TODO: Set background to gradient... like in mock up.
		this.panelNotify.setBackgroundColor( new java.awt.Color( 255, 159, 65 ) );
		this.addPageStartComponent( panelNotify );

		labelNotify = new Label();
		labelNotify.setHorizontalAlignment( HorizontalAlignment.CENTER );
		panelNotify.addComponent( labelNotify, "grow" );
		this.clearErrorMessages();

		this.usernameField = communityLoginComposite.getUsernameState().createTextField();
		this.passwordField = communityLoginComposite.getPasswordState().createPasswordField();
		final int COLUMN_COUNT = 16;
		this.usernameField.getAwtComponent().setColumns( COLUMN_COUNT );
		this.passwordField.getAwtComponent().setColumns( COLUMN_COUNT );
		this.usernameField.enableSelectAllWhenFocusGained();
		this.passwordField.enableSelectAllWhenFocusGained();
		this.loginButton = communityLoginComposite.getLogInOperation().createButton();

		this.progressLogin = new SpinningProgressDial();
		this.progressLogin.setActuallyPainting( false );

		MigPanel migPanel = new MigPanel( null, "fill" );

		migPanel.addComponent( communityLoginComposite.getSignUpOperation().getSidekickLabel().createLabel(), "align right" );
		migPanel.addComponent( communityLoginComposite.getSignUpOperation().createButton(), "wrap" );

		migPanel.addComponent( new Label( "--- or sign in ---", TextPosture.OBLIQUE ), "skip, wrap" );

		migPanel.addComponent( communityLoginComposite.getUsernameState().getSidekickLabel().createLabel(), "align right" );
		migPanel.addComponent( this.usernameField, "wrap" );

		migPanel.addComponent( communityLoginComposite.getPasswordState().getSidekickLabel().createLabel(), "align right" );
		migPanel.addComponent( this.passwordField, "wrap" );

		migPanel.addComponent( this.progressLogin, "align right" );
		migPanel.addComponent( this.loginButton, "wrap" );
		migPanel.addComponent( communityLoginComposite.getIsRememberedState().createCheckBox(), "skip, wrap" );

		migPanel.addComponent( communityLoginComposite.getResetPasswordOperation().getSidekickLabel().createLabel(), "align right" );
		migPanel.addComponent( communityLoginComposite.getResetPasswordOperation().createHyperlink(), "wrap" );

		this.addCenterComponent( migPanel );
		//		this.addComponent( bottomPanel );
	}

	public TextField getUsernameField() {
		return this.usernameField;
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

	@Override
	public void loginAttemptBeginning() {
		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			this.clearErrorMessages();
			this.showProgressIndicatorAndDisableControls();
		} );
	}

	@Override
	public void loginAttemptEnding() {
		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			this.hideProgressIndicatorAndEnableControls();
		} );
	}

	@Override
	public void loginErrorOccurred( String userMessage ) {
		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			if( userMessage == null ) {
				this.clearErrorMessages();
			} else {
				this.displayErrorMessage( userMessage );
				this.usernameField.selectAll();
			}
			this.hideProgressIndicatorAndEnableControls();
		} );
	}

	private void displayErrorMessage( String message ) {
		edu.cmu.cs.dennisc.java.util.logging.Logger.outln( "ERROR: ", message );
		this.panelNotify.getAwtComponent().setOpaque( true );
		this.labelNotify.setText( message );
	}

	private void clearErrorMessages() {
		this.panelNotify.getAwtComponent().setOpaque( false );
		this.labelNotify.setText( null );
	}

	@Override
	protected void handleDisplayable() {
		super.handleDisplayable();
		this.operation.addCommunityLoginListener( this );

		//		javax.swing.SwingUtilities.invokeLater( ( ) -> {
		//			this.usernameField.requestFocusLater();
		//			AbstractWindow<?> window = loginButton.getRoot();
		//			if( window != null ) {
		//				window.pushDefaultButton( loginButton );
		//			}
		//		} );
	}

	@Override
	protected void handleUndisplayable() {
		this.operation.removeCommunityLoginListener( this );
		super.handleUndisplayable();
		//		javax.swing.SwingUtilities.invokeLater( ( ) -> {
		//			this.hideProgressIndicatorAndEnableControls();
		//			AbstractWindow<?> window = loginButton.getRoot();
		//			if( window != null ) {
		//				window.popDefaultButton();
		//			}
		//		} );
	}
}
