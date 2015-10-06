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

import org.lgna.project.Project;

import edu.wustl.lookingglass.community.CommunityController.SnippetQuery;
import edu.wustl.lookingglass.community.api.packets.SnippetPacket;
import edu.wustl.lookingglass.ide.community.codecs.RemixInfoCodec;
import edu.wustl.lookingglass.ide.uricontent.CommunitySnippetLoader;

/**
 * @author Michael Pogran
 */
public class RemixableActionsListData extends CommunityListData<CommunitySnippetLoader> {

	private static final int NUM_VISIBLE_REMIXES = 3;
	private static final int NUM_DOWNLOADED_REMIXES = 6; // TODO: once we get more remixes... have more than three... maybe 12!?

	private static class SingletonHolder {
		private static RemixableActionsListData instance = new RemixableActionsListData();
	}

	public static RemixableActionsListData getInstance() {
		return SingletonHolder.instance;
	}

	private int pageSize;
	private int pageNumber;

	private int displaySize;
	private org.lgna.project.ast.AbstractType<?, ?, ?> currentType;
	private boolean isRefresh = false;

	private java.util.Map<org.lgna.project.ast.AbstractType<?, ?, ?>, java.util.List<CommunitySnippetLoader>> loadersForFields = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();

	public RemixableActionsListData() {
		this( NUM_DOWNLOADED_REMIXES, 0, NUM_VISIBLE_REMIXES );
	}

	public RemixableActionsListData( int pageSize, int pageNumber, int displaySize ) {
		super( RemixInfoCodec.SINGLETON );
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
		this.displaySize = displaySize;
	}

	public void setDisplaySize( int displaySize ) {
		this.displaySize = displaySize;
	}

	public void setPageSize( int pageSize ) {
		this.pageSize = pageSize;
	}

	public void setPageNumber( int pageNumber ) {
		this.pageNumber = pageNumber;
	}

	@Override
	public java.lang.String getSearchQuery() {
		return null;
	}

	public int getDisplaySize() {
		return this.displaySize;
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public int getPageNumber() {
		return this.pageNumber;
	}

	public int[] getLessonIds() {
		// TODO: Adapt this based on the user's current level
		//		return new int[] { 100 };
		return null;
	}

	public void updateValues( org.lgna.project.ast.AbstractType<?, ?, ?> type ) {
		clearCachedThumbnails();
		this.currentType = type;
		if( this.loadersForFields.containsKey( this.currentType ) ) {
			this.isRefresh = true;
			handleDownloadedContentInfos( this.loadersForFields.get( this.currentType ) );
			this.isRefresh = false;
		} else {
			downloadCommunityContent();
		}
	}

	public void clearCachedThumbnails() {
		if( getValues() != null ) {
			for( CommunitySnippetLoader loader : getValues() ) {
				loader.clearCachedThumbnail();
			}
		}
	}

	@Override
	protected void handleDownloadedContentInfos( java.util.List<CommunitySnippetLoader> values ) {
		if( this.currentType != null ) {
			if( this.isRefresh ) {
				//pass
			} else {
				this.loadersForFields.put( this.currentType, values );
			}
		}
		if( !values.isEmpty() ) {
			int size = Math.min( getDisplaySize(), values.size() );
			values = edu.cmu.cs.dennisc.java.util.CollectionUtilities.randomSample( values, size );
		}
		super.handleDownloadedContentInfos( values );
	}

	@Override
	protected java.util.List<CommunitySnippetLoader> createValues() throws Exception {
		// TODO: we need to refresh these once they have passed a puzzle
		// TODO: we need to refresh these if they have changed/added/removed characters
		edu.wustl.lookingglass.community.CommunityController.SnippetQuery query = new SnippetQuery()
				.pageSize( getPageSize() )
				.page( getPageNumber() )
				.limitVersion( true )
				.lessonIds( this.getLessonIds() );

		Integer activeId = edu.wustl.lookingglass.community.models.ModelField.getModelFieldId( this.currentType );
		query.activeModelFieldId( activeId );

		// Ugh... go and grab this global variable, instead of passing it in...
		Project project = edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().getUpToDateProject();

		// Do not suggest snippets that have already been used in this world.
		Integer[] ignoredSnippetIds = edu.wustl.lookingglass.community.CommunityProjectPropertyManager.getCommunityRemixedSnippetIds( project );
		int[] intIgnoredSnippetIds = new int[ ignoredSnippetIds.length ];
		for( int i = 0; i < ignoredSnippetIds.length; i++ ) {
			intIgnoredSnippetIds[ i ] = ignoredSnippetIds[ i ];
		}
		query.ignoredIds( intIgnoredSnippetIds );

		int[] fields = edu.wustl.lookingglass.community.models.ModelField.getWorldFieldIds( project );
		query.modelFieldIds( fields );

		SnippetPacket[] snippetPackets = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getSnippets( query );

		java.util.List<CommunitySnippetLoader> values = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();
		for( SnippetPacket remixPacket : snippetPackets ) {
			values.add( new CommunitySnippetLoader( remixPacket ) );
		}
		return values;
	}
}
