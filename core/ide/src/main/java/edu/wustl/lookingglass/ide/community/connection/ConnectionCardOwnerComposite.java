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

import org.lgna.croquet.CardOwnerComposite;
import org.lgna.croquet.Composite;

import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus;
import edu.wustl.lookingglass.community.CommunityStatusObserver;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;

public final class ConnectionCardOwnerComposite extends CardOwnerComposite {
	private final CommunityIncompatibleApiCard incompatibleApiCard = new CommunityIncompatibleApiCard();
	private final DisconnectedCard disconnectedCard = new DisconnectedCard();
	private final Composite<?> defaultCard;

	private final CommunityStatusObserver communityStatusListener = new CommunityStatusObserver() {
		@Override
		public void connectionChanged( ConnectionStatus status ) {
			ThreadHelper.runOnSwingThread( ( ) -> {
				handleConnectionChanged( status );
			} );
		}

		@Override
		public void accessChanged( AccessStatus status ) {
		}
	};

	public ConnectionCardOwnerComposite( Composite<?> defaultCard ) {
		super( java.util.UUID.fromString( "bb967090-def6-407d-b7dd-02081edfb70e" ) );
		this.defaultCard = defaultCard;
		this.addCard( this.incompatibleApiCard );
		this.addCard( this.disconnectedCard );
		this.addCard( this.defaultCard );
	}

	private void handleConnectionChanged( ConnectionStatus status ) {
		if( status == ConnectionStatus.INCOMPATIBLE_API ) {
			this.showCard( this.incompatibleApiCard );
		} else if( status == ConnectionStatus.DISCONNECTED ) {
			this.showCard( this.disconnectedCard );
		} else {
			this.showCard( this.defaultCard );
		}
	}

	@Override
	public void handlePreActivation() {
		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().addAndInvokeObserver( this.communityStatusListener );
		super.handlePreActivation();
	}

	@Override
	public void handlePostDeactivation() {
		super.handlePostDeactivation();
		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().removeObserver( this.communityStatusListener );
	}
}
