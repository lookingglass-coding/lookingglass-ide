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

import javax.swing.event.ListDataEvent;

import org.alice.ide.uricontent.UriContentLoader;
import org.lgna.croquet.StringState;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.views.BorderPanel;
import org.lgna.croquet.views.ScrollPane;

import edu.wustl.lookingglass.ide.croquet.models.community.data.CommunityListData;

public abstract class WithListStateCommunityTab<I extends UriContentLoader<?>> extends CommunityTab {
	private final org.lgna.croquet.SingleSelectListState<I, org.lgna.croquet.data.ListData<I>> listState;
	private final StringState tagSearchState;
	private final CommunityListData data;

	private java.util.Timer searchTimer = new java.util.Timer();
	private java.util.TimerTask searchTask;

	public WithListStateCommunityTab( java.util.UUID migrationId, CommunityListData data ) {
		super( migrationId, data );
		this.data = data;
		this.listState = this.createGenericListState( "listState", data, -1 );

		this.tagSearchState = this.createStringState( "tagSearchState", "" );

		this.tagSearchState.addAndInvokeNewSchoolValueListener( new TagSearchListener() );

		this.data.addListener( new javax.swing.event.ListDataListener() {

			@Override
			public void intervalAdded( ListDataEvent e ) {
			}

			@Override
			public void intervalRemoved( ListDataEvent e ) {
			}

			@Override
			public void contentsChanged( ListDataEvent e ) {
				if( listState.getItemCount() > 0 ) {
					listState.setSelectedIndex( 0 );
				} else {
					listState.setSelectedIndex( -1 );
				}
			}

		} );

		this.data.addContentDownloadedListener( new edu.wustl.lookingglass.ide.community.connection.observer.ContentDownloadedObserver() {

			@Override
			public void handleContentDownloadStarted() {
			}

			@Override
			public void handleContentDownloadComplete() {
			}

			@Override
			public void handleContentDownloadError( Exception e ) {
				data.resetSearchTerms();
			}

		} );
	}

	@Override
	public I getContentInfo() {
		return this.listState.getValue();
	}

	public org.lgna.croquet.SingleSelectListState<I, org.lgna.croquet.data.ListData<I>> getListState() {
		return this.listState;
	}

	public StringState getTagSearchState() {
		return this.tagSearchState;
	}

	@Override
	protected BorderPanel createView() {
		return new edu.wustl.lookingglass.ide.croquet.models.community.views.WithListStateCommunityTabView( this );
	}

	@Override
	protected ScrollPane createScrollPaneIfDesired() {
		return null;
	}

	// This is listening to changes on the tagSearchState and then sending them into the data. The model I'm currently
	// going for is that a refresh will use the current tag search terms. A better structure may well be possible.
	class TagSearchListener implements org.lgna.croquet.event.ValueListener<String> {

		@Override
		public void valueChanged( ValueEvent<String> e ) {
			if( searchTask != null ) {
				searchTask.cancel();
			}

			if( e.getNextValue().isEmpty() ) {
				if( ( data.getSearchQuery() == null ) || ( data.getSearchQuery().isEmpty() ) ) {
					//pass
				} else {
					data.resetSearchTerms();
				}
			} else {
				data.setSearchTerms( e.getNextValue() );

				if( e.getNextValue().length() >= 3 ) {
					searchTask = new SearchTask( data );
					searchTimer.schedule( searchTask, 1000 );
				} else {
					data.clearValues();
				}
			}
			getView().repaint();
		}
	}

	private class SearchTask extends java.util.TimerTask {
		private final CommunityListData data;

		public SearchTask( CommunityListData data ) {
			this.data = data;
		}

		@Override
		public void run() {
			this.data.downloadCommunityContent();
		}

	}
}
