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

import org.lgna.croquet.SimpleTabComposite;
import org.lgna.croquet.data.ListData;
import org.lgna.croquet.views.BorderPanel;
import org.lgna.croquet.views.ScrollPane;

import edu.wustl.lookingglass.ide.croquet.models.community.data.CommunityListData;
import edu.wustl.lookingglass.ide.croquet.models.community.views.ContentInfoListCellRenderer;

/**
 * @author Caitlin Kelleher
 */
public abstract class MyCommunityBookmarksComposite<LoaderType extends org.alice.ide.uricontent.UriContentLoader<?>> extends SimpleTabComposite<BorderPanel> {

	private final org.lgna.croquet.SingleSelectListState<LoaderType, ListData<LoaderType>> listState;

	public MyCommunityBookmarksComposite( java.util.UUID migrationID, CommunityListData listData ) {
		super( migrationID, IsCloseable.FALSE );
		this.listState = this.createGenericListState( "listState", listData, -1 );

		( (CommunityListData<?>)this.listState.getData() ).addContentDownloadedListener( new edu.wustl.lookingglass.ide.community.connection.observer.ContentDownloadedObserver() {

			@Override
			public void handleContentDownloadStarted() {
			}

			@Override
			public void handleContentDownloadComplete() {
				if( MyCommunityBookmarksComposite.this.getData().getItemCount() != 0 ) {
					MyCommunityBookmarksComposite.this.listState.setSelectedIndex( 0 );
				}
			}

			@Override
			public void handleContentDownloadError( Exception e ) {
			}

		} );
	}

	@Override
	protected ScrollPane createScrollPaneIfDesired() {
		return null;
	}

	public CommunityListData<?> getData() {
		return (CommunityListData<?>)this.listState.getData();
	}

	@Override
	protected BorderPanel createView() {

		org.lgna.croquet.views.List<LoaderType> list = listState.createList();
		list.setCellRenderer( new ContentInfoListCellRenderer() );
		list.enableClickingDefaultButtonOnDoubleClick();

		ScrollPane scrollPane = new ScrollPane( list );
		BorderPanel view = new BorderPanel.Builder()
				.center( scrollPane )
				.build();

		view.setBackgroundColor( org.alice.ide.projecturi.views.TabContentPanel.DEFAULT_BACKGROUND_COLOR );
		return view;
	}

	public LoaderType getContentInfo() {
		return this.listState.getValue();
	}
}
