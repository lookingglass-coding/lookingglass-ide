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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;

import org.alice.ide.browser.BrowserOperation;
import org.alice.ide.uricontent.GetContentObserver;
import org.alice.ide.uricontent.ThumbnailContentWorker;
import org.lgna.croquet.views.Label;

import edu.wustl.lookingglass.community.api.packets.UserPacket;
import edu.wustl.lookingglass.community.exceptions.CommunityApiException;
import edu.wustl.lookingglass.ide.community.connection.observer.CommunityLoginObserver;
import edu.wustl.lookingglass.ide.croquet.models.community.CommunityLoginOperation;
import edu.wustl.lookingglass.ide.perspectives.openproject.views.OpenProjectLoggedInView;

/**
 * @author Michael Pogran
 */
public class OpenProjectLoggedInCard extends org.lgna.croquet.SimpleComposite<OpenProjectLoggedInView> {
	private final BrowserOperation userPageOperation = new BrowserOperation( java.util.UUID.fromString( "914d7943-2340-4b14-bb16-3139ab84afa5" ) ) {
		@Override
		public java.net.URL getUrl() {
			UserPacket user = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getCurrentUser();
			return edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAbsoluteUrl( user.getUserPath() );
		}

		@Override
		protected java.lang.Class<? extends org.lgna.croquet.Element> getClassUsedForLocalization() {
			return OpenProjectLoggedInCard.this.getClassUsedForLocalization();
		}

		@Override
		protected String getSubKeyForLocalization() {
			return "userPageOperation";
		}
	};

	private final BrowserOperation notificationLinkOperation = new BrowserOperation( java.util.UUID.fromString( "8ec0800e-e22f-41f3-b151-f8f8fbdfd3cd" ) ) {
		@Override
		public java.net.URL getUrl() {
			UserPacket user = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getCurrentUser();
			return edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAbsoluteUrl( user.getUserNotificationsPath() );
		}

		@Override
		protected java.lang.Class<? extends org.lgna.croquet.Element> getClassUsedForLocalization() {
			return OpenProjectLoggedInCard.this.getClassUsedForLocalization();
		}

		@Override
		protected String getSubKeyForLocalization() {
			return "notificationLinkOperation";
		}
	};

	private final CommunityLoginObserver loginStatusListener = new CommunityLoginObserver() {

		@Override
		public void loginAttemptBeginning() {
		}

		@Override
		public void loginAttemptEnding() {
			updateView();
		}

		@Override
		public void loginErrorOccurred( String userMessage ) {
		}
	};

	private final org.lgna.croquet.PlainStringValue welcomeText = this.createStringValue( "welcomeText" );
	private final org.lgna.croquet.PlainStringValue notificationText = this.createStringValue( "notificationText" );
	private final NotificationsWorker notificationsWorker = new NotificationsWorker();
	private final ThumbnailContentWorker thumbnailWorker = new ThumbnailContentWorker() {

		@Override
		protected Image loadThumbnail() {
			Image thumbnail = null;
			try {
				thumbnail = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().downloadAvatar( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getCurrentUser() );
			} catch( CommunityApiException | IOException e ) {
				e.printStackTrace();
			}
			return thumbnail;
		}
	};

	public OpenProjectLoggedInCard() {
		super( java.util.UUID.fromString( "0cfe0bd4-8f9c-44de-84ac-b718a06ffee4" ) );
	}

	private void updateView() {
		// Set user avatar
		this.updateAvatar();

		// Set welcome text
		String userLogin = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getUsername();
		String userText = welcomeText.getOriginalLocalizedText();
		final String welcomeText = userText.replaceAll( "</userLogin/>", userLogin );

		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			getView().getMessageLabel().setText( welcomeText );
		} );

		this.updateNotificationsCount();
	}

	@Override
	protected OpenProjectLoggedInView createView() {
		return new OpenProjectLoggedInView( this );
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		if( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getCurrentUser() != null ) {
			updateView();
		}
		CommunityLoginOperation.getInstance().addCommunityLoginListener( loginStatusListener );
	}

	@Override
	public void handlePostDeactivation() {
		super.handlePostDeactivation();
		CommunityLoginOperation.getInstance().removeCommunityLoginListener( loginStatusListener );
	}

	public BrowserOperation getUserPageOperation() {
		return this.userPageOperation;
	}

	public BrowserOperation getNotificationLinkOperation() {
		return this.notificationLinkOperation;
	}

	public void updateNotificationsCount() {
		this.notificationsWorker.execute();
	}

	public void updateAvatar() {
		this.thumbnailWorker.execute( new ImageGetter() );
	}

	private class ImageGetter implements GetContentObserver<Image> {

		@Override
		public void workStarted() {
		}

		@Override
		public void workEnded() {
		}

		@Override
		public void completed( Image content ) {
			edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnSwingThread( ( ) -> {
				Label avatar = getView().getAvatarLabel();
				int max = content.getHeight( null ) > content.getWidth( null ) ? content.getWidth( null ) : content.getHeight( null );
				BufferedImage clippedAvatarImage = ( (BufferedImage)content ).getSubimage( 0, 0, max, max );
				avatar.setIcon( new edu.cmu.cs.dennisc.javax.swing.icons.ScaledIcon( new ImageIcon( clippedAvatarImage ), 75, 75 ) );
			} );
		}

		@Override
		public void failed( Throwable t ) {
		}
	}

	private class NotificationsWorker extends javax.swing.SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getNotifications();
			return null;
		}

		@Override
		protected void done() {
			int count = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getUserNotificationCount();
			String countString = Integer.toString( count );
			String notifications = notificationText.getOriginalLocalizedText();
			final String text = notifications.replaceAll( "</count/>", countString );
			edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnSwingThread( ( ) -> {
				getView().getNotificationsLabel().setText( text );
			} );
		}

	}
}
