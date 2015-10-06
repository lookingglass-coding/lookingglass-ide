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
package edu.wustl.lookingglass.ide.community;

import java.net.URL;

import org.lgna.croquet.views.AwtComponentView;
import org.lgna.croquet.views.BorderPanel.Constraint;
import org.lgna.croquet.views.Hyperlink;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.LineAxisPanel;

import edu.cmu.cs.dennisc.javax.swing.border.EmptyBorder;
import edu.wustl.lookingglass.ide.croquet.models.community.ShareContentObserver;

public class ShareProgressDialog extends AbstractProgressDialog implements ShareContentObserver {

	Label messageLabel;
	String uploadDescriptor;

	public ShareProgressDialog( String uploadDescriptor ) {
		super( true );

		this.uploadDescriptor = uploadDescriptor;
	}

	// this is the main content panel - create gets called by the constructor.
	@Override
	public AwtComponentView<?> createMessageComponent() {
		if( messageLabel == null ) {

			messageLabel = new Label( "uploading your " + uploadDescriptor + "…" );
			messageLabel.setAlignmentX( 0.5f );
			messageLabel.setBorder( new EmptyBorder( 20, 20, 20, 20 ) );

		}
		return messageLabel;
	}

	@Override
	public void updateMessage( String message ) {
		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			if( messageLabel != null ) {
				messageLabel.setText( message );
			}
		} );
	}

	@Override
	public void uploadSuccessful( final URL worldURL ) {

		org.alice.ide.browser.BrowserOperation browserOperation = new org.alice.ide.browser.BrowserOperation( java.util.UUID.fromString( "76621215-72f4-4fc3-a576-a9ce0f63c661" ) ) {
			@Override
			protected void localize() {
				this.setName( "See it online?" );
			};

			@Override
			public java.net.URL getUrl() {
				return worldURL;
			}
		};

		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			synchronized( messageOkPanel.getAwtComponent().getTreeLock() ) {
				messageOkPanel.removeComponent( messageLabel );

				Hyperlink hyperlink = browserOperation.createHyperlink();
				hyperlink.setFont( hyperlink.getFont().deriveFont( 12 ) );

				Label label = new Label( "We're done uploading your " + uploadDescriptor + "!" );
				label.setBorder( new EmptyBorder( 0, 0, 0, 10 ) );

				LineAxisPanel lineAxisPanel = new LineAxisPanel( label, hyperlink );
				lineAxisPanel.setAlignmentX( 0.5f );
				lineAxisPanel.setBorder( new EmptyBorder( 20, 20, 20, 20 ) );
				messageOkPanel.addComponent( lineAxisPanel, Constraint.PAGE_START );

				this.getContentPane().revalidateAndRepaint();
			}
		} );

		this.setEnabled( true );
	}

	@Override
	public void uploadFailed( String message ) {
		messageLabel.setText( message );
		this.setEnabled( true );
	}
}
