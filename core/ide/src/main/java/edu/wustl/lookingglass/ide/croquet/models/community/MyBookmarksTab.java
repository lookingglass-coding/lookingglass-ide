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
package edu.wustl.lookingglass.ide.croquet.models.community;

import java.util.concurrent.ExecutionException;

import org.lgna.croquet.Composite;
import org.lgna.croquet.views.BorderPanel;
import org.lgna.croquet.views.HtmlMultiLineLabel;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.Panel;

import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus;
import edu.wustl.lookingglass.community.CommunityStatusObserver;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.community.connection.CommunityIncompatibleApiCard;
import edu.wustl.lookingglass.ide.community.connection.CommunityLoginComposite;
import edu.wustl.lookingglass.ide.community.connection.ConnectionAndAccessCardOwnerComposite.Builder;
import edu.wustl.lookingglass.ide.community.connection.OneLineUserAccessCard;

public abstract class MyBookmarksTab<LoaderType extends org.alice.ide.uricontent.UriContentLoader<?>> extends CommunityTab implements CommunityStatusObserver {
	private static final int COMMUNITY_REFRESH_INTERVAL_MILLISECONDS = 5000;

	private final CommunityLoginComposite loginComposite = new CommunityLoginComposite();
	private final CommunityIncompatibleApiCard oldApiComposite = new CommunityIncompatibleApiCard();
	private final DisconnectedComposite disconnectedComposite = new DisconnectedComposite();
	private final MyCommunityBookmarksComposite<LoaderType> loggedInComposite;

	private final org.lgna.croquet.CardOwnerComposite loginOrViewCardComposite;

	public MyBookmarksTab( java.util.UUID migrationId, MyCommunityBookmarksComposite<LoaderType> myCommunityComposite ) {
		super( migrationId, myCommunityComposite.getData() );
		assert data != null : this;
		this.loggedInComposite = myCommunityComposite;

		this.loginOrViewCardComposite = this.createAndRegisterCardOwnerComposite( this.getLoginComposite(), this.getLoggedInComposite(), this.getOldApiComposite(), this.getDisconnectedComposite() );
		this.showAppropriateCard();
	}

	@Override
	protected Builder createCommunityStatusCardCompositeBuilder() {
		Builder builder = super.createCommunityStatusCardCompositeBuilder();

		OneLineUserAccessCard oneLineUserAccessCard = new OneLineUserAccessCard( getRefreshOperation() );
		data.addContentDownloadedListener( oneLineUserAccessCard );

		builder.connectedUserAccess( oneLineUserAccessCard );
		return builder;
	}

	@Override
	public LoaderType getContentInfo() {
		Composite<?> showingCard = this.loginOrViewCardComposite.getShowingCard();
		if( showingCard == this.loggedInComposite ) {
			return this.loggedInComposite.getContentInfo();
		} else {
			return null;
		}
	}

	// Community Status Observer Methods
	@Override
	public void accessChanged( AccessStatus status ) {
		ThreadHelper.runOnSwingThread( ( ) -> {
			this.showAppropriateCard();
		} );
	}

	@Override
	public void connectionChanged( ConnectionStatus status ) {
		ThreadHelper.runOnSwingThread( ( ) -> {
			this.showAppropriateCard();
		} );
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().addObserver( this );
		this.showAppropriateCard();
	}

	@Override
	public void handlePostDeactivation() {
		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().removeObserver( this );
		super.handlePostDeactivation();
	}

	@Override
	protected org.lgna.croquet.views.ScrollPane createScrollPaneIfDesired() {
		return null;
	}

	@Override
	protected BorderPanel createView() {
		BorderPanel rv = new BorderPanel.Builder().center( loginOrViewCardComposite.getView() ).build();
		rv.setBackgroundColor( org.alice.ide.projecturi.views.TabContentPanel.DEFAULT_BACKGROUND_COLOR );

		this.loggedInComposite.getView().addPageStartComponent( this.getCommunityStatusCardComposite().getView() );

		return rv;
	}

	private CommunityLoginComposite getLoginComposite() {
		return this.loginComposite;
	}

	private Composite<Panel> getOldApiComposite() {
		return this.oldApiComposite;
	}

	private Composite<Panel> getDisconnectedComposite() {
		return this.disconnectedComposite;
	}

	private Composite<?> getLoggedInComposite() {
		return this.loggedInComposite;
	}

	private void showAppropriateCard() {
		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			if( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getConnectionStatus() == ConnectionStatus.DISCONNECTED ) {
				loginOrViewCardComposite.showCard( getDisconnectedComposite() );
			} else if( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getConnectionStatus() == ConnectionStatus.INCOMPATIBLE_API ) {
				loginOrViewCardComposite.showCard( getOldApiComposite() );
			} else if( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().isUserLoggedIn() ) {
				loginOrViewCardComposite.showCard( getLoggedInComposite() );
			} else {
				loginOrViewCardComposite.showCard( getLoginComposite() );
			}
		} );
	}

	private static final class DisconnectedComposite extends org.lgna.croquet.SimpleComposite<Panel> {
		private final HtmlMultiLineLabel messageLabel = new HtmlMultiLineLabel( "Connecting to the Looking Glass Community... " );

		public DisconnectedComposite() {
			super( java.util.UUID.fromString( "6c2e15e0-7abc-4359-a465-5d61038bc1b5" ) );
		}

		@Override
		protected Panel createView() {
			MigPanel migPanel = new MigPanel( null, "", "10[]10", "" );
			migPanel.addComponent( messageLabel, "cell 0 0" );
			return migPanel;
		}

		@Override
		public void handlePreActivation() {
			super.handlePreActivation();

			messageLabel.setText( "Connecting to the Looking Glass Community... " );
			javax.swing.SwingWorker<Void, Void> resetMessageWorker = new javax.swing.SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					Thread.sleep( COMMUNITY_REFRESH_INTERVAL_MILLISECONDS );
					return null;
				}

				@Override
				protected void done() {
					try {
						get();

						messageLabel.setText( "<html> <strong>Oh No! Looking Glass cannot access the community.</strong> <br/><br/>This is mostly likely either because: <br>1) We are updating the community server. <br/> -or- <br/> " +
								"2) Your network connection is currently down. <br/><br/> If you need access to the community, please check your connection.</html>" );
					} catch( InterruptedException | ExecutionException e ) {
						e.printStackTrace();
					}
				}
			};

			resetMessageWorker.execute();
		}
	}
}
