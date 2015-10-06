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

import org.lgna.croquet.SimpleComposite;

import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus;
import edu.wustl.lookingglass.community.CommunityStatusObserver;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.community.connection.observer.ContentDownloadedObserver;
import edu.wustl.lookingglass.ide.croquet.models.community.data.FeaturedWorldData;
import edu.wustl.lookingglass.ide.croquet.models.community.data.RecentActivityData;
import edu.wustl.lookingglass.ide.croquet.models.preview.PreviewProjectComposite;
import edu.wustl.lookingglass.ide.perspectives.openproject.views.WelcomeDetailPanel;
import edu.wustl.lookingglass.ide.uricontent.CommunityProjectLoader;

/**
 * @author Michael Pogran
 */

public class OpenProjectDetailWelcomeComposite extends SimpleComposite<WelcomeDetailPanel> {
	private final PreviewProjectComposite previewProjectComposite = new PreviewProjectComposite();
	private final RecentActivityData recentActivityData = new RecentActivityData();
	private final FeaturedWorldData featuredWorldData = new FeaturedWorldData();

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

	public OpenProjectDetailWelcomeComposite() {
		super( java.util.UUID.fromString( "c672fc82-9667-40b8-a832-2f6f7ab1ec3d" ) );
		this.registerSubComposite( this.previewProjectComposite );

		this.recentActivityData.addContentDownloadedListener( new ContentDownloadedObserver() {

			@Override
			public void handleContentDownloadStarted() {
			}

			@Override
			public void handleContentDownloadComplete() {
				getView().updateRecentActivity( recentActivityData );
			}

			@Override
			public void handleContentDownloadError( Exception e ) {
			}

		} );

		this.featuredWorldData.addContentDownloadedListener( new ContentDownloadedObserver() {
			@Override
			public void handleContentDownloadStarted() {
			}

			@Override
			public void handleContentDownloadComplete() {
				previewProjectComposite.loadProject( getFeaturedWorld() );
				getView().updateFeaturedWorld( featuredWorldData.getFeaturedWorld() );
			}

			@Override
			public void handleContentDownloadError( Exception e ) {
			}

		} );

		this.featuredWorldData.downloadCommunityContent();
		this.recentActivityData.downloadCommunityContent();
	}

	public RecentActivityData getRecentActivityData() {
		return this.recentActivityData;
	}

	public CommunityProjectLoader getFeaturedWorld() {
		return this.featuredWorldData.getFeaturedWorld();
	}

	private boolean isRecentActivityInitialized = false;
	private boolean isFeaturedWorldInitialized = false;

	public synchronized boolean isRecentActivityInitialized() {
		return this.isRecentActivityInitialized;
	}

	public synchronized void setRecentActivityInitialized( boolean isRecentActivityInitialized ) {
		this.isRecentActivityInitialized = isRecentActivityInitialized;
	}

	public synchronized boolean isFeaturedWorldInitialized() {
		return this.isFeaturedWorldInitialized;
	}

	public synchronized void setFeaturedWorldInitialized( boolean isFeaturedWorldInitialized ) {
		this.isFeaturedWorldInitialized = isFeaturedWorldInitialized;
	}

	@Override
	protected WelcomeDetailPanel createView() {
		return new WelcomeDetailPanel( previewProjectComposite.getView(), this );
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().addObserver( communityStatusListener );
		handleConnectionChanged( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getConnectionStatus() );

		previewProjectComposite.loadProject( getFeaturedWorld() );
	}

	@Override
	public void handlePostDeactivation() {
		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().addObserver( communityStatusListener );

		this.recentActivityData.clearCachedThumbnails();
		this.featuredWorldData.clearCachedThumbnails();
		super.handlePostDeactivation();
	}

	public void handleConnectionChanged( ConnectionStatus connection ) {
		ThreadHelper.runOnSwingThread( ( ) -> {
			this.getView().setSpinnerVisible( !( ( connection == ConnectionStatus.INCOMPATIBLE_API ) || ( connection == ConnectionStatus.DISCONNECTED ) ) );
		} );
	}

}
