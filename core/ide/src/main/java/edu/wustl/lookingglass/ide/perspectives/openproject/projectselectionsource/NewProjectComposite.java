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
package edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource;

import org.alice.ide.uricontent.UriProjectLoader;
import org.lgna.croquet.CancelException;
import org.lgna.croquet.Operation;
import org.lgna.croquet.SingleSelectListState;
import org.lgna.croquet.StringState;
import org.lgna.croquet.data.ListData;
import org.lgna.croquet.edits.Edit;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.event.ValueListener;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.croquet.meta.LastOneInWinsMetaState;

import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus;
import edu.wustl.lookingglass.community.CommunityStatusObserver;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.community.connection.observer.CommunityLoginObserver;
import edu.wustl.lookingglass.ide.community.connection.observer.ContentDownloadedObserver;
import edu.wustl.lookingglass.ide.croquet.models.community.CommunityLoginOperation;
import edu.wustl.lookingglass.ide.croquet.models.community.data.BlankTemplateData;
import edu.wustl.lookingglass.ide.croquet.models.community.data.BookmarkedTemplateData;
import edu.wustl.lookingglass.ide.croquet.models.community.data.RecentTemplateData;
import edu.wustl.lookingglass.ide.perspectives.openproject.views.NewProjectView;
import edu.wustl.lookingglass.ide.uricontent.CommunityProjectLoader;

/**
 * @author Michael Pogran
 */
public class NewProjectComposite extends OpenProjectTab {
	private final RecentTemplateData recentTemplateData = new RecentTemplateData();

	private final SingleSelectListState<? extends UriProjectLoader, ListData<CommunityProjectLoader>> topTemplatesState = this.createGenericListState( "topTemplatesState", this.recentTemplateData, -1 );
	private final SingleSelectListState<? extends UriProjectLoader, ListData<UriProjectLoader>> blankTemplatesState = this.createGenericListState( "blankTemplatesState", BlankTemplateData.getInstance(), -1 );
	private final SingleSelectListState<? extends UriProjectLoader, ListData<CommunityProjectLoader>> bookmarkedTemplatesState = this.createGenericListState( "bookmarkedTemplatesState", BookmarkedTemplateData.getInstance(), -1 );

	private final StringState tagSearchState = this.createStringState( "tagSearchState" );
	private final StringState pageNumberState = this.recentTemplateData.getPageNumberState();

	private final Operation nextPageOperation = this.recentTemplateData.getNextPageOperation();
	private final Operation prevPageOperation = this.recentTemplateData.getPreviousPageOperation();

	private java.util.Timer searchTimer = new java.util.Timer();
	private java.util.TimerTask searchTask;

	private final LastOneInWinsMetaState<UriProjectLoader> metaState = new LastOneInWinsMetaState<UriProjectLoader>( (org.lgna.croquet.State<UriProjectLoader>)bookmarkedTemplatesState, (org.lgna.croquet.State<UriProjectLoader>)topTemplatesState, (org.lgna.croquet.State<UriProjectLoader>)blankTemplatesState );

	private final Operation clearSearchFieldOperation = this.createActionOperation( "clearOperation", new Action() {
		@Override
		public Edit perform( CompletionStep<?> step, org.lgna.croquet.AbstractComposite.InternalActionOperation source ) throws CancelException {
			tagSearchState.setValueTransactionlessly( null );
			NewProjectComposite.this.recentTemplateData.resetSearchTerms();
			return null;
		}

	} );

	private final Operation refreshCommunityOperation = this.createActionOperation( "refreshCommunityOperation", new Action() {
		@Override
		public Edit perform( CompletionStep<?> step, org.lgna.croquet.AbstractComposite.InternalActionOperation source ) throws CancelException {
			NewProjectComposite.this.recentTemplateData.downloadCommunityContent();
			BookmarkedTemplateData.getInstance().downloadCommunityContent();
			return null;
		}

	} );

	private final ValueListener<String> searchListener = new ValueListener<String>() {
		@Override
		public void valueChanged( ValueEvent<String> e ) {
			if( searchTask != null ) {
				searchTask.cancel();
			}

			if( ( e.getNextValue() != null ) && e.getNextValue().isEmpty() ) {
				if( ( NewProjectComposite.this.recentTemplateData.getSearchQuery() == null ) || ( NewProjectComposite.this.recentTemplateData.getSearchQuery().isEmpty() ) ) {
					//pass
				} else {
					NewProjectComposite.this.recentTemplateData.resetSearchTerms();
				}
			} else {
				NewProjectComposite.this.recentTemplateData.setSearchTerms( e.getNextValue() );

				if( ( e.getNextValue() != null ) && ( e.getNextValue().length() >= 3 ) ) {
					searchTask = new SearchTask();
					searchTimer.schedule( searchTask, 1000 );
				} else {
					NewProjectComposite.this.recentTemplateData.clearValues();
				}
			}
		}
	};

	private final CommunityStatusObserver communityStatusListener = new CommunityStatusObserver() {
		@Override
		public void connectionChanged( ConnectionStatus status ) {
			ThreadHelper.runOnSwingThread( () -> {
				handleAccessOrConnectionChanged();
			} );
		}

		@Override
		public void accessChanged( AccessStatus status ) {
			ThreadHelper.runOnSwingThread( () -> {
				handleAccessOrConnectionChanged();
			} );
			//BookmarkedTemplateData.getInstance().refresh(); TODO: Bring back in refresh somehow
		}
	};

	public NewProjectComposite() {
		super( java.util.UUID.fromString( "4d2ea98f-52ec-4491-94ca-3e8909920b39" ) );
		this.nextPageOperation.setButtonIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "navigation-next", org.lgna.croquet.icon.IconSize.SMALL ) );
		this.prevPageOperation.setButtonIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "navigation-previous", org.lgna.croquet.icon.IconSize.SMALL ) );

		this.tagSearchState.setValueTransactionlessly( null );
		this.tagSearchState.addAndInvokeNewSchoolValueListener( this.searchListener );

		if( LookingGlassIDE.getCommunityController().getAccessStatus() != AccessStatus.NONE ) {
			this.recentTemplateData.downloadCommunityContent();

			if( LookingGlassIDE.getCommunityController().getAccessStatus() == AccessStatus.USER_ACCESS ) {
				BookmarkedTemplateData.getInstance().downloadCommunityContent();
			}
		}

		this.recentTemplateData.addContentDownloadedListener( new ContentDownloadedObserver() {
			@Override
			public void handleContentDownloadStarted() {
				javax.swing.SwingUtilities.invokeLater( () -> {
					topTemplatesState.clearSelection();
					( (NewProjectView)getView() ).setSearchErrorLabelVisible( false );
				} );
			}

			@Override
			public void handleContentDownloadComplete() {
				if( topTemplatesState.getItemCount() > 0 ) {
					topTemplatesState.setSelectedIndex( 0 );
				}
			}

			@Override
			public void handleContentDownloadError( Exception e ) {
				javax.swing.SwingUtilities.invokeLater( () -> {
					( (NewProjectView)getView() ).setSearchErrorLabelVisible( true );
				} );
			}

		} );

		CommunityLoginOperation.getInstance().addCommunityLoginListener( new CommunityLoginObserver() {
			@Override
			public void loginAttemptBeginning() {
				NewProjectView view = (NewProjectView)NewProjectComposite.this.getView();
				view.updateLoginError( "" );
			}

			@Override
			public void loginAttemptEnding() {
			}

			@Override
			public void loginErrorOccurred( String userMessage ) {
				NewProjectView view = (NewProjectView)NewProjectComposite.this.getView();
				view.updateLoginError( userMessage );
			}
		} );

		this.blankTemplatesState.setSelectedIndex( 0 );
	}

	private void handleAccessOrConnectionChanged() {
		ConnectionStatus connection = LookingGlassIDE.getCommunityController().getConnectionStatus();
		AccessStatus access = LookingGlassIDE.getCommunityController().getAccessStatus();

		NewProjectView view = (NewProjectView)this.getView();
		if( ( connection == ConnectionStatus.INCOMPATIBLE_API ) || ( connection == ConnectionStatus.DISCONNECTED ) ) {
			view.setCommunityContentVisible( false );
		} else if( ( connection == ConnectionStatus.CONNECTED ) && ( access == AccessStatus.USER_ACCESS ) ) {
			view.setLoginViewVisible( false );
		} else if( ( connection == ConnectionStatus.CONNECTED ) && ( access == AccessStatus.ANONYMOUS_ACCESS ) ) {
			view.setLoginViewVisible( true );
		}
	}

	@Override
	protected org.lgna.croquet.views.ScrollPane createScrollPaneIfDesired() {
		return null;
	}

	@Override
	protected NewProjectView createView() {
		return new NewProjectView( this );
	}

	@Override
	public UriProjectLoader getSelectedUriProjectLoader() {
		return metaState.getValue();
	}

	@Override
	public javax.swing.Icon getTabButtonIcon() {
		return edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "start-new", org.lgna.croquet.icon.IconSize.FIXED );
	}

	public SingleSelectListState<? extends UriProjectLoader, ListData<CommunityProjectLoader>> getTopTemplatesState() {
		return this.topTemplatesState;
	}

	public SingleSelectListState<? extends UriProjectLoader, ListData<UriProjectLoader>> getBlankTemplatesState() {
		return this.blankTemplatesState;
	}

	public SingleSelectListState<? extends UriProjectLoader, ListData<CommunityProjectLoader>> getBookmarkedTemplatesState() {
		return this.bookmarkedTemplatesState;
	}

	public StringState getTagSearchState() {
		return this.tagSearchState;
	}

	public Operation getRefreshOperation() {
		return this.refreshCommunityOperation;
	}

	public Operation getClearOperation() {
		return this.clearSearchFieldOperation;
	}

	public Operation getNextPageOperation() {
		return this.nextPageOperation;
	}

	public Operation getPreviousPageOperation() {
		return this.prevPageOperation;
	}

	public StringState getPageNumberState() {
		return this.pageNumberState;
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		LookingGlassIDE.getCommunityController().addObserver( communityStatusListener );
		handleAccessOrConnectionChanged();
	}

	@Override
	public void handlePostDeactivation() {
		LookingGlassIDE.getCommunityController().removeObserver( communityStatusListener );
		super.handlePostDeactivation();
	}

	private class SearchTask extends java.util.TimerTask {

		public SearchTask() {
		}

		@Override
		public void run() {
			javax.swing.SwingUtilities.invokeLater( () -> {
				NewProjectComposite.this.recentTemplateData.downloadCommunityContent();
			} );
		}
	}

	@Override
	public void handlePerspectiveDeactivation() {
		NewProjectComposite.this.recentTemplateData.clearCachedThumbnails();
		BlankTemplateData.getInstance().clearCachedThumbnails();
		BookmarkedTemplateData.getInstance().clearCachedThumbnails();
		this.releaseView();
	}
}
