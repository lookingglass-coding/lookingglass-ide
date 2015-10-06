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
package edu.wustl.lookingglass.story.recorder;

import org.lgna.common.ComponentThread;

import edu.wustl.lookingglass.scenegraph.recorder.RandomAccessRecorder;

public class PropertyValueRecorder extends RandomAccessRecorder {

	private final java.util.Map<org.lgna.story.implementation.Property, PropertyTimeLine> history = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();
	private final java.util.Map<org.lgna.story.implementation.Property, PropertyChange> propertyCache = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();

	private final org.lgna.story.implementation.Property.Listener<Object> propertyListener = new org.lgna.story.implementation.Property.Listener<Object>() {
		@Override
		public void propertyChanged( org.lgna.story.implementation.Property<Object> property, Object prevValue, Object nextValue ) {
			recordPropertyChanged( property, prevValue, nextValue );
		}
	};

	private final org.lgna.story.implementation.Property.EPIC_HACK_FOR_DINAH_PropertyCreationListener propertyCreationListener = new org.lgna.story.implementation.Property.EPIC_HACK_FOR_DINAH_PropertyCreationListener() {
		@Override
		public void propertyCreated( org.lgna.story.implementation.Property property ) {
			handlePropertyCreated( property );
		}
	};

	@Override
	public void setRecording( boolean isRecording ) {
		super.setRecording( isRecording );
		if( isRecording ) {
			org.lgna.story.implementation.Property.EPIC_HACK_FOR_DINAH_addPropertyCreationListener( propertyCreationListener );
		} else {
			org.lgna.story.implementation.Property.EPIC_HACK_FOR_DINAH_removePropertyCreationListener( propertyCreationListener );
		}
	}

	private void recordPropertyChanged( org.lgna.story.implementation.Property<?> property, Object prevValue, Object nextValue ) {
		if( this.shouldRecordChange() ) {
			synchronized( this.history ) {
				PropertyTimeLine propertyTimeLine = this.history.get( property );
				propertyTimeLine.add( prevValue, nextValue, this.getSourceThreadForCurrentAnimation(), this.getProgramTime() );
			}
		}
	}

	private void handlePropertyCreated( org.lgna.story.implementation.Property property ) {
		synchronized( this.history ) {
			PropertyTimeLine propertyTimeLine = new PropertyTimeLine( property, ComponentThread.currentThread(), this.getProgramTime() );
			this.history.put( property, propertyTimeLine );
		}
		property.addPropertyListener( propertyListener );
	}

	@Override
	protected synchronized void scrubToTime( double rollbackTime, java.util.Set<Object> objectsToFilter ) {
		for( PropertyTimeLine propertyTimeLine : this.history.values() ) {
			java.util.SortedMap<Double, PropertyChange> map = propertyTimeLine.getHeadMap( rollbackTime );
			PropertyChange propertyChange;
			if( map.isEmpty() ) {
				propertyChange = propertyTimeLine.getNaNPropertyChange();
			} else {
				Double time = map.lastKey();
				propertyChange = map.get( time );
			}

			if( ( propertyChange != null ) && !propertyChange.equals( propertyCache.get( propertyTimeLine.getProperty() ) ) ) {
				if( objectsToFilter.contains( propertyChange.getSourceThread() ) || objectsToFilter.isEmpty() ) {
					org.lgna.story.implementation.Property property = propertyTimeLine.getProperty();
					property.setValue( propertyChange.getValue() );
					propertyCache.put( property, propertyChange );
				}
			}
		}
	}

	@Override
	public void clearHistory() {
		for( org.lgna.story.implementation.Property property : this.history.keySet() ) {
			property.removePropertyListener( propertyListener );
		}
		this.history.clear();
		this.propertyCache.clear();
	}
}
