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
package edu.wustl.lookingglass.ide.croquet.models.data;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;

import org.alice.ide.uricontent.FileProjectLoader;
import org.alice.ide.uricontent.UriProjectLoader;
import org.lgna.croquet.data.AbstractMutableListData;

import edu.wustl.lookingglass.ide.community.codecs.ProjectCodec;

/**
 * @author Michael Pogran
 */
public class MyProjectsData extends AbstractMutableListData<FileProjectLoader> {
	private final java.util.List<FileProjectLoader> projects;
	private java.util.List<FileProjectLoader> values;

	Comparator<FileProjectLoader> sortTime = new Comparator<FileProjectLoader>() {

		@Override
		public int compare( FileProjectLoader o1, FileProjectLoader o2 ) {
			return o2.getLastModified().compareTo( o1.getLastModified() );
		}
	};

	Comparator<FileProjectLoader> sortName = new Comparator<FileProjectLoader>() {

		@Override
		public int compare( FileProjectLoader o1, FileProjectLoader o2 ) {
			return o1.getTitle().toLowerCase().compareTo( o2.getTitle().toLowerCase() );
		}
	};

	public MyProjectsData( java.util.List<FileProjectLoader> projects ) {
		super( ProjectCodec.SINGLETON );
		this.projects = projects;
		this.values = projects;
		this.values.sort( this.sortTime );
	}

	public void clearCachedThumbnails() {
		for( UriProjectLoader loader : this.values ) {
			loader.clearCachedThumbnail();
		}
	}

	public void setSearch( String term ) {
		java.util.List<FileProjectLoader> searchValues = edu.cmu.cs.dennisc.java.util.Lists.newArrayList( this.projects );

		Predicate<FileProjectLoader> filter = new Predicate<FileProjectLoader>() {

			@Override
			public boolean test( FileProjectLoader t ) {
				return !( t.getTitle().contains( term ) );
			}
		};

		searchValues.removeIf( filter );
		this.values = searchValues;
		fireContentsChanged();
	}

	public void resetValues() {
		this.values = this.projects;
		fireContentsChanged();
	}

	public void sortByDate() {
		sortAndRefreshData( this.sortTime );
	}

	public void sortByName() {
		sortAndRefreshData( this.sortName );
	}

	private void sortAndRefreshData( java.util.Comparator<FileProjectLoader> compare ) {
		this.values.sort( compare );
		fireContentsChanged();
	}

	@Override
	public boolean contains( FileProjectLoader item ) {
		return this.values.contains( item );
	}

	@Override
	public FileProjectLoader getItemAt( int index ) {
		return this.values.get( index );
	}

	@Override
	public int getItemCount() {
		return this.values.size();
	}

	@Override
	public int indexOf( FileProjectLoader item ) {
		return this.values.indexOf( item );
	}

	@Override
	public void internalAddItem( int index, FileProjectLoader item ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void internalRemoveItem( FileProjectLoader item ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void internalSetAllItems( Collection<FileProjectLoader> items ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void internalSetItemAt( int index, FileProjectLoader item ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<FileProjectLoader> iterator() {
		return this.values.iterator();
	}

	@Override
	protected FileProjectLoader[] toArray( Class<FileProjectLoader> componentType ) {
		return edu.cmu.cs.dennisc.java.lang.ArrayUtilities.createArray( this.values, componentType );
	}
}
