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

import org.lgna.croquet.Application;
import org.lgna.croquet.views.AwtComponentView;
import org.lgna.croquet.views.BorderPanel;
import org.lgna.croquet.views.BorderPanel.Constraint;
import org.lgna.croquet.views.BoxUtilities;
import org.lgna.croquet.views.Button;
import org.lgna.croquet.views.Dialog;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.LineAxisPanel;

import edu.cmu.cs.dennisc.javax.swing.border.EmptyBorder;

public abstract class AbstractProgressDialog extends Dialog {

	//	protected Label messageLabel = null;
	protected BorderPanel messageOkPanel = null;

	private OKOperation okOperation = new OKOperation();

	public AbstractProgressDialog( boolean includeOkButton ) {

		this.setDefaultCloseOperation( org.lgna.croquet.views.Dialog.DefaultCloseOperation.DO_NOTHING );

		okOperation.setEnabled( false );

		// create the logo
		Label logoLabel = new Label();
		logoLabel.setAlignmentX( 0.5f );
		logoLabel.setBorder( new EmptyBorder( 10, 10, 0, 10 ) );
		logoLabel.setIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "community-login-logo", org.lgna.croquet.icon.IconSize.FIXED ) );
		this.getContentPane().addCenterComponent( logoLabel );

		// create the rest of the panel we need to change
		messageOkPanel = new BorderPanel();
		AwtComponentView messagePanel = createMessageComponent();
		messageOkPanel.addComponent( messagePanel, Constraint.CENTER );

		if( includeOkButton ) {

			LineAxisPanel lineAxisPanel = new LineAxisPanel();
			lineAxisPanel.addComponent( BoxUtilities.createHorizontalGlue() );
			Button okButton = okOperation.createButton();
			lineAxisPanel.addComponent( okButton );
			lineAxisPanel.setBorder( new EmptyBorder( 0, 0, 10, 10 ) );
			messageOkPanel.addComponent( lineAxisPanel, Constraint.PAGE_END );

			this.getContentPane().addPageEndComponent( messageOkPanel );
		}

	}

	public void setEnabled( boolean enabled ) {
		if( okOperation != null ) {
			okOperation.setEnabled( enabled );
		}
	}

	public abstract AwtComponentView<?> createMessageComponent();

	private class OKOperation extends org.lgna.croquet.Operation {

		public OKOperation() {
			super( Application.INFORMATION_GROUP, java.util.UUID.fromString( "f8ccfb4f-4aad-44e5-89d7-0791368b5926" ) );
		}

		@Override
		protected void perform( org.lgna.croquet.history.Transaction transaction, org.lgna.croquet.triggers.Trigger trigger ) {
			AbstractProgressDialog.this.setVisible( false );

		}

		@Override
		protected void localize() {
			this.setName( "OK" );
		}

	}
}
