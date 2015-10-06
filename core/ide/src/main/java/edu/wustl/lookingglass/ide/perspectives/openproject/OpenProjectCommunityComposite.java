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
package edu.wustl.lookingglass.ide.perspectives.openproject;

import org.lgna.croquet.CardOwnerComposite;

import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus;
import edu.wustl.lookingglass.community.CommunityStatusObserver;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.community.connection.CommunityIncompatibleApiCard;
import edu.wustl.lookingglass.ide.community.connection.DisconnectedCard;

/**
 * @author Michael Pogran
 */
public class OpenProjectCommunityComposite extends CardOwnerComposite {
	private final OpenProjectLoginCard loginCard = new OpenProjectLoginCard();
	private final CommunityIncompatibleApiCard incompatibleApiCard = new CommunityIncompatibleApiCard();
	private final DisconnectedCard disconnectedCard = new DisconnectedCard();
	private final OpenProjectLoggedInCard loggedInCard = new OpenProjectLoggedInCard();

	private final CommunityStatusObserver communityStatusListener = new CommunityStatusObserver() {
		@Override
		public void connectionChanged( ConnectionStatus status ) {
			ThreadHelper.runOnSwingThread( ( ) -> {
				handleAccessOrConnectionChanged();
			} );
		}

		@Override
		public void accessChanged( AccessStatus status ) {
			ThreadHelper.runOnSwingThread( ( ) -> {
				handleAccessOrConnectionChanged();
			} );
		}
	};

	public OpenProjectCommunityComposite() {
		super( java.util.UUID.fromString( "d1cc77fb-5b3d-4bff-b7f6-fe5175af069d" ) );
		this.addCard( loginCard );
		this.addCard( incompatibleApiCard );
		this.addCard( disconnectedCard );
		this.addCard( loggedInCard );
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().addObserver( communityStatusListener );
		handleAccessOrConnectionChanged();
	}

	@Override
	public void handlePostDeactivation() {
		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().removeObserver( communityStatusListener );
		this.releaseView();
		super.handlePostDeactivation();
	}

	private void handleAccessOrConnectionChanged() {
		ConnectionStatus connection = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getConnectionStatus();
		AccessStatus access = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAccessStatus();
		handleStateChange( connection, access );
	}

	private void handleStateChange( ConnectionStatus connection, AccessStatus access ) {
		if( connection == ConnectionStatus.INCOMPATIBLE_API ) {
			this.showCard( incompatibleApiCard );
		} else if( connection == ConnectionStatus.DISCONNECTED ) {
			this.showCard( disconnectedCard );
		} else if( ( connection == ConnectionStatus.CONNECTED ) && ( access == AccessStatus.USER_ACCESS ) ) {
			this.showCard( loggedInCard );
		} else if( ( connection == ConnectionStatus.CONNECTED ) && ( access == AccessStatus.ANONYMOUS_ACCESS ) ) {
			this.showCard( loginCard );
		}
	}
}
