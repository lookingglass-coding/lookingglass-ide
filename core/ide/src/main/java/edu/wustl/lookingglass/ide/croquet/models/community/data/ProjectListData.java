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
package edu.wustl.lookingglass.ide.croquet.models.community.data;

import java.util.List;

import org.lgna.croquet.StringState;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;

import edu.wustl.lookingglass.community.CommunityController.TemplateFilter;
import edu.wustl.lookingglass.community.CommunityController.TemplateQuery;
import edu.wustl.lookingglass.community.CommunityController.WorldFilter;
import edu.wustl.lookingglass.community.CommunityController.WorldQuery;
import edu.wustl.lookingglass.community.api.packets.ProjectPacket;
import edu.wustl.lookingglass.ide.community.codecs.WorldInfoCodec;
import edu.wustl.lookingglass.ide.croquet.models.community.CommunityGroup;
import edu.wustl.lookingglass.ide.uricontent.CommunityProjectLoader;

/**
 * @author Caitlin Kelleher
 */
public abstract class ProjectListData extends CommunityListData<CommunityProjectLoader> {

	/* package-private */static enum ProjectType {
		WORLD,
		TEMPLATE;
	}

	/* package-private */
	public static enum FilterType {
		RECENT,
		POPULAR,
		FEATURED,
		BOOKMARKED;

		public static WorldFilter getWorldFilter( FilterType filter ) {
			switch( filter ) {
			case RECENT:
				return WorldFilter.RECENT;
			case POPULAR:
				return WorldFilter.POPULAR;
			case FEATURED:
				return WorldFilter.FEATURED;
			case BOOKMARKED:
				return WorldFilter.BOOKMARKED;
			default:
				return null;
			}
		}

		public static TemplateFilter getTemplateFilter( FilterType filter ) {
			switch( filter ) {
			case RECENT:
				return TemplateFilter.RECENT;
			case POPULAR:
				return TemplateFilter.POPULAR;
			case FEATURED:
				return TemplateFilter.FEATURED;
			case BOOKMARKED:
				return TemplateFilter.BOOKMARKED;
			default:
				return null;
			}
		}
	}

	private PageNumberState pageNumber;
	private int pageSize = 9;
	private int pageNum = 1;

	private org.lgna.croquet.Operation nextPageOperation = new org.lgna.croquet.Operation( CommunityGroup.COMMUNITY_GROUP, java.util.UUID.fromString( "a3317e7e-84a6-4314-8903-3655f7bb190e" ) ) {

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			nextPage();
			clearValues();
			downloadCommunityContent();
		}
	};

	private org.lgna.croquet.Operation prevPageOperation = new org.lgna.croquet.Operation( CommunityGroup.COMMUNITY_GROUP, java.util.UUID.fromString( "5c94aa9e-015a-4488-b69d-8c47ed0c9570" ) ) {

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			previousPage();
			clearValues();
			downloadCommunityContent();
		}
	};

	public ProjectListData() {
		super( WorldInfoCodec.SINGLETON );
		this.pageNumber = new PageNumberState( getPageNumber() );
	}

	@Override
	public void setSearchTerms( java.lang.String searchTermsString ) {
		setPageNumber( 1 );
		super.setSearchTerms( searchTermsString );
	}

	@Override
	public void resetSearchTerms() {
		setPageNumber( 0 );
		super.resetSearchTerms();
	}

	private int getPageSize() {
		return this.pageSize;
	}

	private int getPageNumber() {
		return this.pageNum;
	}

	public StringState getPageNumberState() {
		return this.pageNumber;
	}

	public org.lgna.croquet.Operation getNextPageOperation() {
		return this.nextPageOperation;
	}

	public org.lgna.croquet.Operation getPreviousPageOperation() {
		return this.prevPageOperation;
	}

	public void setPageSize( int pageSize ) {
		this.pageSize = pageSize;
	}

	public void setPageNumber( int pageNum ) {
		this.pageNum = pageNum;
		this.pageNumber.setPage( pageNum );
	}

	public void nextPage() {
		setPageNumber( this.pageNum + 1 );
	}

	public void previousPage() {
		setPageNumber( this.pageNum - 1 );
	}

	public boolean prevOkay() {
		return this.pageNum > 1;
	}

	public boolean nextOkay() {
		return this.getItemCount() == this.pageSize;
	}

	public void clearCachedThumbnails() {
		for( CommunityProjectLoader loader : getValues() ) {
			loader.clearCachedThumbnail();
		}
	}

	protected abstract ProjectType getProjectType();

	protected abstract FilterType getFilterType();

	@Override
	protected void notifyContentDownloadComplete() {
		super.notifyContentDownloadComplete();
		this.nextPageOperation.setEnabled( nextOkay() );
		this.prevPageOperation.setEnabled( prevOkay() );
	}

	@Override
	protected List<CommunityProjectLoader> createValues() throws Exception {
		List<CommunityProjectLoader> values = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();

		edu.wustl.lookingglass.community.api.packets.ProjectPacket[] projectPackets = null;
		switch( getProjectType() ) {
		case WORLD:
			edu.wustl.lookingglass.community.CommunityController.WorldQuery worldQuery = new WorldQuery()
					.filter( FilterType.getWorldFilter( getFilterType() ) )
					.query( getSearchQuery() )
					.pageSize( getPageSize() )
					.page( getPageNumber() )
					.limitVersion( true );
			projectPackets = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getWorlds( worldQuery );
			break;
		case TEMPLATE:
			edu.wustl.lookingglass.community.CommunityController.TemplateQuery templateQuery = new TemplateQuery();

			if( ( getSearchQuery() != null ) && !getSearchQuery().isEmpty() ) {
				// No filter if searching
			} else {
				templateQuery = templateQuery.filter( FilterType.getTemplateFilter( getFilterType() ) );
			}
			templateQuery = templateQuery.query( getSearchQuery() )
					.pageSize( getPageSize() )
					.page( getPageNumber() )
					.limitVersion( true );
			projectPackets = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getTemplates( templateQuery );
			break;
		}

		for( ProjectPacket projectPacket : projectPackets ) {
			values.add( new CommunityProjectLoader( projectPacket ) );
		}

		return values;
	}

	private class PageNumberState extends StringState {

		public PageNumberState( int page ) {
			super( CommunityGroup.COMMUNITY_GROUP, java.util.UUID.fromString( "69763679-619c-40b1-8ff6-a3f9e5586d9f" ), "" );
			setPage( page );
		}

		public void setPage( int page ) {
			this.setValueTransactionlessly( Integer.toString( page ) );
		}

	}
}
