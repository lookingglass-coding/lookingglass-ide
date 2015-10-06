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
package edu.wustl.lookingglass.scenegraph.recorder;

import java.util.ArrayList;
import java.util.NavigableMap;

import edu.wustl.lookingglass.scenegraph.RecordableAbstractTransformable;
import edu.wustl.lookingglass.utilities.pattern.ObjectToArrayListMap;

/**
 * @author Michael Pogran
 */
public class TransformableTimeLine {

	public TransformableTimeLine( RecordableAbstractTransformable transformable ) {
		this.transformable = transformable;
		this.changes = new ObjectToArrayListMap<Double, TransformationChange>();
	}

	public synchronized void add( TransformationChange change, double time ) {
		this.changes.putItem( time, change );
	}

	public synchronized java.util.SortedMap<Double, ArrayList<TransformationChange>> getHeadMap( double time ) {
		if( this.changes != null ) {
			//would it be better to add on a small epsilon?
			final boolean IS_INCLUSIVE = true;
			return this.changes.headMap( time, IS_INCLUSIVE );
		} else {
			//TODO: replace when move to JDK8
			//return java.util.Collections.emptySortedMap();
			return new java.util.TreeMap<Double, ArrayList<TransformationChange>>();
		}
	}

	public synchronized NavigableMap<Double, ArrayList<TransformationChange>> getTailMap( double time ) {
		if( this.changes != null ) {
			//would it be better to add on a small epsilon?
			final boolean IS_INCLUSIVE = false;
			return this.changes.tailMap( time, IS_INCLUSIVE );
		} else {
			//TODO: replace when move to JDK8
			//return java.util.Collections.emptySortedMap();
			return new java.util.TreeMap<Double, ArrayList<TransformationChange>>();
		}
	}

	public TransformationChange getNaNTransformationChange() {
		ArrayList<TransformationChange> NaNChanges = this.changes.get( Double.NaN );
		if( NaNChanges != null ) {
			return NaNChanges.get( NaNChanges.size() - 1 );
		}

		//value hasn't changed
		return null;
	}

	public boolean hasTransformationChanges() {
		return !( ( this.changes.size() == 1 ) && this.changes.containsKey( Double.NaN ) );
	}

	private final RecordableAbstractTransformable transformable;
	private ObjectToArrayListMap<Double, TransformationChange> changes;
}
