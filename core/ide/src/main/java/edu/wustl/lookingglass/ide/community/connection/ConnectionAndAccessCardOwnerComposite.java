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

import org.lgna.croquet.history.TransactionManager;
import org.lgna.croquet.triggers.NullTrigger;

import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus;
import edu.wustl.lookingglass.community.CommunityStatusObserver;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;

public final class ConnectionAndAccessCardOwnerComposite extends org.lgna.croquet.CardOwnerComposite {
	public static class Builder {
		private static class InternalComposite extends org.lgna.croquet.SimpleComposite<org.lgna.croquet.views.Panel> {
			private final org.lgna.croquet.views.Panel view;

			public InternalComposite( org.lgna.croquet.views.Panel view ) {
				super( java.util.UUID.fromString( "073a52ed-aa62-469b-b596-e6bed840196e" ) );
				this.view = view;
			}

			@Override
			protected org.lgna.croquet.views.Panel createView() {
				return this.view;
			}
		}

		private static org.lgna.croquet.Composite<?> createComposite( org.lgna.croquet.views.Panel view ) {
			return new InternalComposite( view );
		}

		private final java.util.UUID migrationId;
		private org.lgna.croquet.Composite<?> defaultComposite;
		private org.lgna.croquet.Composite<?> disconnectedComposite;
		private org.lgna.croquet.Composite<?> incompatibleApiComposite;
		private org.lgna.croquet.Composite<?> connectedNoAccessComposite;
		private org.lgna.croquet.Composite<?> connectedAnonymousAccessComposite;
		private org.lgna.croquet.Composite<?> connectedUserAccessComposite;

		public Builder( java.util.UUID migrationId, org.lgna.croquet.Composite<?> defaultComposite ) {
			this.migrationId = migrationId;
			this.defaultComposite = defaultComposite;
		}

		public Builder( java.util.UUID migrationId ) {
			this( migrationId, (org.lgna.croquet.Composite<?>)null );
		}

		public Builder defaultView( org.lgna.croquet.Composite<?> defaultComposite ) {
			this.defaultComposite = defaultComposite;
			return this;
		}

		public Builder disconnected( org.lgna.croquet.Composite<?> disconnectedComposite ) {
			this.disconnectedComposite = disconnectedComposite;
			return this;
		}

		public Builder incompatibleApi( org.lgna.croquet.Composite<?> incompatibleApiComposite ) {
			this.incompatibleApiComposite = incompatibleApiComposite;
			return this;
		}

		public Builder connectedNoAccess( org.lgna.croquet.Composite<?> connectedNoAccessComposite ) {
			this.connectedNoAccessComposite = connectedNoAccessComposite;
			return this;
		}

		public Builder connectedAnonymousAccess( org.lgna.croquet.Composite<?> connectedAnonymousAccessComposite ) {
			this.connectedAnonymousAccessComposite = connectedAnonymousAccessComposite;
			return this;
		}

		public Builder connectedUserAccess( org.lgna.croquet.Composite<?> connectedUserAccessComposite ) {
			this.connectedUserAccessComposite = connectedUserAccessComposite;
			return this;
		}

		public ConnectionAndAccessCardOwnerComposite build() {
			if( this.defaultComposite != null ) {
				//pass
			} else {
				assert ( this.disconnectedComposite != null ) && ( this.incompatibleApiComposite != null ) && ( this.connectedNoAccessComposite != null ) && ( this.connectedAnonymousAccessComposite != null ) && ( this.connectedUserAccessComposite != null ) : this;
			}
			return new ConnectionAndAccessCardOwnerComposite( this.migrationId, this.defaultComposite, this.disconnectedComposite, this.incompatibleApiComposite, this.connectedNoAccessComposite, this.connectedAnonymousAccessComposite, this.connectedUserAccessComposite );
		}
	}

	private final org.lgna.croquet.Composite<?> defaultComposite;
	private final org.lgna.croquet.Composite<?> disconnectedComposite;
	private final org.lgna.croquet.Composite<?> incompatibleApiComposite;
	private final org.lgna.croquet.Composite<?> connectedNoAccessComposite;
	private final org.lgna.croquet.Composite<?> connectedAnonymousAccessComposite;
	private final org.lgna.croquet.Composite<?> connectedUserAccessComposite;

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

	private ConnectionAndAccessCardOwnerComposite( java.util.UUID migrationId, org.lgna.croquet.Composite<?> defaultComposite, org.lgna.croquet.Composite<?> disconnectedComposite, org.lgna.croquet.Composite<?> incompatibleApiComposite, org.lgna.croquet.Composite<?> connectedNoAccessComposite, org.lgna.croquet.Composite<?> connectedAnonymousAccessComposite, org.lgna.croquet.Composite<?> connectedUserAccessComposite ) {
		super( migrationId );
		this.defaultComposite = defaultComposite;
		this.disconnectedComposite = disconnectedComposite;
		this.incompatibleApiComposite = incompatibleApiComposite;
		this.connectedNoAccessComposite = connectedNoAccessComposite;
		this.connectedAnonymousAccessComposite = connectedAnonymousAccessComposite;
		this.connectedUserAccessComposite = connectedUserAccessComposite;
		if( this.defaultComposite != null ) {
			this.addCard( this.defaultComposite );
		}
		if( this.disconnectedComposite != null ) {
			this.addCard( this.disconnectedComposite );
		}
		if( this.incompatibleApiComposite != null ) {
			this.addCard( this.incompatibleApiComposite );
		}
		if( this.connectedNoAccessComposite != null ) {
			this.addCard( this.connectedNoAccessComposite );
		}
		if( this.connectedAnonymousAccessComposite != null ) {
			this.addCard( this.connectedAnonymousAccessComposite );
		}
		if( this.connectedUserAccessComposite != null ) {
			this.addCard( this.connectedUserAccessComposite );
		}
	}

	private void handleAccessOrConnectionChanged() {
		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			this.handleStateChange( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getConnectionStatus(), edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAccessStatus() );
		} );
		// to force croquet state update
		TransactionManager.TODO_REMOVE_fireEvent( NullTrigger.createUserInstance() );

	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();

		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().addObserver( this.communityStatusListener );
		this.handleAccessOrConnectionChanged();
	}

	@Override
	public void handlePostDeactivation() {
		super.handlePostDeactivation();
		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().removeObserver( this.communityStatusListener );
	}

	private void showCardOrShowDefaultCardIfNull( org.lgna.croquet.Composite<?> composite ) {
		if( composite != null ) {
			this.showCard( composite );
		} else {
			this.showCard( this.defaultComposite );
		}
	}

	public void handleStateChange( edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus connectionStatus, edu.wustl.lookingglass.community.CommunityStatus.AccessStatus accessStatus ) {
		if( connectionStatus == edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus.DISCONNECTED ) {
			this.showCardOrShowDefaultCardIfNull( this.disconnectedComposite );
		} else if( connectionStatus == edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus.INCOMPATIBLE_API ) {
			this.showCardOrShowDefaultCardIfNull( this.incompatibleApiComposite );
		} else if( connectionStatus == edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus.CONNECTED ) {
			if( accessStatus == edu.wustl.lookingglass.community.CommunityStatus.AccessStatus.NONE ) {
				this.showCardOrShowDefaultCardIfNull( this.connectedNoAccessComposite );
			} else if( accessStatus == edu.wustl.lookingglass.community.CommunityStatus.AccessStatus.ANONYMOUS_ACCESS ) {
				this.showCardOrShowDefaultCardIfNull( this.connectedAnonymousAccessComposite );
			} else if( accessStatus == edu.wustl.lookingglass.community.CommunityStatus.AccessStatus.USER_ACCESS ) {
				this.showCardOrShowDefaultCardIfNull( this.connectedUserAccessComposite );
			} else {
				this.showCard( null );
			}
		} else {
			this.showCard( null );
		}
	}

}
