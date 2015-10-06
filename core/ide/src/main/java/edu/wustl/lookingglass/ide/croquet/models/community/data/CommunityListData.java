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

import java.util.Collection;
import java.util.Iterator;

import org.lgna.croquet.ItemCodec;
import org.lgna.croquet.data.AbstractMutableListData;

import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus;
import edu.wustl.lookingglass.community.CommunityStatusObserver;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.community.connection.observer.ContentDownloadedObserver;

public abstract class CommunityListData<T> extends AbstractMutableListData<T> implements CommunityStatusObserver {
	private final java.util.List<ContentDownloadedObserver> worldInfosDownloadedListeners = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();
	private java.util.List<T> values;
	private String searchQuery = "";

	public CommunityListData( ItemCodec<T> itemCodec ) {
		super( itemCodec );
		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().addObserver( this );
		this.values = java.util.Collections.emptyList();
	}

	public java.util.List<T> getValues() {
		return this.values;
	}

	public void clearValues() {
		this.values.clear();
		this.fireContentsChanged();
	}

	public boolean isSearchSet() {
		if( getSearchQuery() == null ) {
			return false;
		} else {
			return !this.searchQuery.isEmpty();
		}
	}

	public void setSearchTerms( String searchTermsString ) {
		this.searchQuery = searchTermsString;
	}

	public void resetSearchTerms() {
		this.setSearchTerms( "" );
		this.downloadCommunityContent();
	}

	public String getSearchQuery() {
		if( ( this.searchQuery == null ) || this.searchQuery.isEmpty() ) {
			return null;
		} else {
			return this.searchQuery;
		}
	}

	public void addContentDownloadedListener( ContentDownloadedObserver listener ) {
		this.worldInfosDownloadedListeners.add( listener );
	}

	public void removeContentDownloadedListener( ContentDownloadedObserver listener ) {
		this.worldInfosDownloadedListeners.remove( listener );
	}

	private void notifyContentDownloadStarted() {
		for( ContentDownloadedObserver listener : worldInfosDownloadedListeners ) {
			listener.handleContentDownloadStarted();
		}
	}

	protected void notifyContentDownloadComplete() {
		for( ContentDownloadedObserver listener : worldInfosDownloadedListeners ) {
			listener.handleContentDownloadComplete();
		}
	}

	private void notifyContentDownloadError( Exception e ) {
		for( ContentDownloadedObserver listener : worldInfosDownloadedListeners ) {
			listener.handleContentDownloadError( e );
		}
	}

	protected boolean isEmptyWhenLoggedOut() {
		return false;
	}

	protected void handleDownloadedContentInfos( java.util.List<T> nextContentInfos ) {
		if( nextContentInfos != null ) {
			this.values = nextContentInfos;
		} else {
			this.values = java.util.Collections.emptyList();
		}
		this.fireContentsChanged();
	}

	@Override
	public void accessChanged( AccessStatus status ) {
		if( this.getItemCount() == 0 ) {
			this.downloadCommunityContent();
		}
	}

	@Override
	public void connectionChanged( ConnectionStatus status ) {
		if( this.getItemCount() == 0 ) {
			this.downloadCommunityContent();
		}
	}

	protected abstract java.util.List<T> createValues() throws Exception;

	protected boolean shouldDownloadContent() {
		if( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAccessStatus() == edu.wustl.lookingglass.community.CommunityStatus.AccessStatus.NONE ) {
			return false;
		} else if( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAccessStatus() != edu.wustl.lookingglass.community.CommunityStatus.AccessStatus.ANONYMOUS_ACCESS ) {
			return !isEmptyWhenLoggedOut();
		}
		return true;
	}

	private Boolean downloadStarted = false;

	public final void downloadCommunityContent() {
		ThreadHelper.runOnSwingThread( ( ) -> {
			if( this.shouldDownloadContent() && !downloadStarted ) {
				this.downloadStarted = true;
				this.notifyContentDownloadStarted();
				new javax.swing.SwingWorker<java.util.List<T>, Void>() {

					@Override
					protected java.util.List<T> doInBackground() throws Exception {
						return createValues();
					}

					@Override
					protected void done() {
						try {
							java.util.List<T> items = get();
							CommunityListData.this.handleDownloadedContentInfos( items );
							CommunityListData.this.notifyContentDownloadComplete();
						} catch( InterruptedException e ) {
							edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( e );
							notifyContentDownloadError( e );
						} catch( java.util.concurrent.ExecutionException e ) {
							Throwable t = e.getCause();
							if( t instanceof edu.wustl.lookingglass.community.exceptions.CommunityApiException ) {
								notifyContentDownloadError( (edu.wustl.lookingglass.community.exceptions.CommunityApiException)t );
							} else {
								throw new RuntimeException( t );
							}
						} finally {
							downloadStarted = false;
						}
					}
				}.execute();
			}
		} );
	}

	@Override
	protected T[] toArray( Class<T> componentType ) {
		return edu.cmu.cs.dennisc.java.lang.ArrayUtilities.createArray( this.values, componentType );
	}

	@Override
	public boolean contains( T item ) {
		return this.values.contains( item );
	}

	@Override
	public T getItemAt( int index ) {
		return this.values.get( index );
	}

	@Override
	public int getItemCount() {
		return this.values.size();
	}

	@Override
	public int indexOf( T item ) {
		return this.values.indexOf( item );
	}

	@Override
	public void internalAddItem( int index, T item ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void internalRemoveItem( T item ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void internalSetAllItems( Collection<T> items ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void internalSetItemAt( int index, T item ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<T> iterator() {
		return this.values.iterator();
	}
}
