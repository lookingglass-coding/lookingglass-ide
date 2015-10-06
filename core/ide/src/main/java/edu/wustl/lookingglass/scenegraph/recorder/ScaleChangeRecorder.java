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

import org.lgna.common.ComponentThread;

import edu.cmu.cs.dennisc.scenegraph.Visual;
import edu.cmu.cs.dennisc.scenegraph.Visual.EPIC_HACK_FOR_DINAH_VisualCreationListener;

public class ScaleChangeRecorder extends RandomAccessRecorder {
	@Override
	public void setRecording( boolean isRecording ) {
		super.setRecording( isRecording );
		if( isRecording ) {
			edu.cmu.cs.dennisc.scenegraph.Visual.EPIC_HACK_FOR_DINAH_addVisualCreationListener( visualCreationListener );
		} else {
			edu.cmu.cs.dennisc.scenegraph.Visual.EPIC_HACK_FOR_DINAH_removeVisualCreationListener( visualCreationListener );
		}
	}

	private void handleVisualCreated( Visual visual ) {
		synchronized( this.history ) {
			ScaleTimeLine scaleTimeLine = new ScaleTimeLine( visual, ComponentThread.currentThread(), this.getProgramTime() );
			this.history.put( visual, scaleTimeLine );
		}
		visual.scale.addPropertyListener( this.scalePropertyListener );
	}

	@Override
	protected synchronized void scrubToTime( double rollbackTime, java.util.Set<Object> objectsToFilter ) {
		synchronized( this.history ) {
			for( ScaleTimeLine scaleTimeLine : this.history.values() ) {
				java.util.SortedMap<Double, ScaleChange> map = scaleTimeLine.getHeadMap( rollbackTime );
				ScaleChange scaleChange;
				if( map.isEmpty() ) {
					scaleChange = scaleTimeLine.getNaNScaleChange();
				} else {
					Double time = map.lastKey();
					scaleChange = map.get( time );
				}

				if( ( scaleChange != null ) && !scaleChange.equals( scaleCache.get( scaleTimeLine.getVisual() ) ) ) {
					if( objectsToFilter.contains( scaleChange.getSourceThread() ) || objectsToFilter.isEmpty() ) {
						Visual visual = scaleTimeLine.getVisual();
						visual.scale.setValue( scaleChange.getValue() );
						scaleCache.put( visual, scaleChange );
					}
				}
			}
		}
	}

	@Override
	public void clearHistory() {
		for( Visual visual : this.history.keySet() ) {
			visual.scale.removePropertyListener( scalePropertyListener );
		}
		history.clear();
		scaleCache.clear();
	}

	private void recordPropertyChanged( Visual visual, edu.cmu.cs.dennisc.math.Matrix3x3 prevValue, edu.cmu.cs.dennisc.math.Matrix3x3 nextValue ) {
		if( this.shouldRecordChange() ) {
			synchronized( this.history ) {
				ScaleTimeLine scaleTimeLine = this.history.get( visual );
				scaleTimeLine.add( prevValue, nextValue, this.getSourceThreadForCurrentAnimation(), this.getProgramTime() );
			}
		}
	}

	private final edu.cmu.cs.dennisc.scenegraph.Visual.EPIC_HACK_FOR_DINAH_VisualCreationListener visualCreationListener = new EPIC_HACK_FOR_DINAH_VisualCreationListener() {
		@Override
		public void visualCreated( Visual visual ) {
			handleVisualCreated( visual );
		}
	};

	private final edu.cmu.cs.dennisc.property.event.PropertyListener scalePropertyListener = new edu.cmu.cs.dennisc.property.event.PropertyListener() {
		private edu.cmu.cs.dennisc.math.Matrix3x3 prevValue;

		@Override
		public void propertyChanging( edu.cmu.cs.dennisc.property.event.PropertyEvent e ) {
			prevValue = ( (Visual)e.getOwner() ).scale.getValue();
		}

		@Override
		public void propertyChanged( edu.cmu.cs.dennisc.property.event.PropertyEvent e ) {
			recordPropertyChanged( ( (Visual)e.getOwner() ), this.prevValue, (edu.cmu.cs.dennisc.math.Matrix3x3)e.getValue() );
		}
	};
	private final java.util.Map<edu.cmu.cs.dennisc.scenegraph.Visual, ScaleTimeLine> history = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();
	private final java.util.Map<edu.cmu.cs.dennisc.scenegraph.Visual, ScaleChange> scaleCache = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();

}
