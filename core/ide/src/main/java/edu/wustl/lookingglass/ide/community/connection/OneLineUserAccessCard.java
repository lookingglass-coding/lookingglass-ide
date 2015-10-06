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

import org.lgna.croquet.Operation;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;

import edu.wustl.lookingglass.ide.croquet.models.community.CommunityLogoutOperation;

/**
 * @author Caitlin Kelleher
 */

//todo: are there errors we need to handle from logout, the refresh ones are handled through the refreshable access card
public class OneLineUserAccessCard extends OneLineRefreshableAccessCard {
	private Label statusMessageLabel;

	public OneLineUserAccessCard() {
		super( java.util.UUID.fromString( "4ae744c8-2f94-41d5-8bd0-599e1da35d87" ), null );
	}

	public OneLineUserAccessCard( Operation refreshOperation ) {
		super( java.util.UUID.fromString( "4ae744c8-2f94-41d5-8bd0-599e1da35d87" ), refreshOperation );
	}

	@Override
	protected MigPanel createView() {
		MigPanel view = new MigPanel( this, "", "10[align left, grow]5[align right]5[align right]5[align right]5[align right]10", "0[]0" );
		statusMessageLabel = new Label();

		view.addComponent( statusMessageLabel );
		view.addComponent( errorMessageLabel );

		if( refreshOperation != null ) {
			view.addComponent( progressDial );
			view.addComponent( refreshOperation.createHyperlink() );
		}
		view.addComponent( CommunityLogoutOperation.getInstance().createHyperlink() );

		return view;
	}

	@Override
	public void handlePreActivation() {
		String userName = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getUsername();
		statusMessageLabel.setText( "You are logged in as " + userName );
	}
}
