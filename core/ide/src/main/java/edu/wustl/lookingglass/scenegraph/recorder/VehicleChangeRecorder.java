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

import edu.cmu.cs.dennisc.scenegraph.Composite;
import edu.cmu.cs.dennisc.scenegraph.Composite.EPIC_HACK_FOR_DINAH_CompositeCreationListener;
import edu.cmu.cs.dennisc.scenegraph.event.ComponentAddedEvent;
import edu.cmu.cs.dennisc.scenegraph.event.ComponentRemovedEvent;
import edu.cmu.cs.dennisc.scenegraph.event.ComponentsListener;

/**
 * @author Michael Pogran
 */
public class VehicleChangeRecorder extends RandomAccessRecorder {

	private final java.util.Map<edu.cmu.cs.dennisc.scenegraph.Composite, VehicleTimeLine> history = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();
	private final java.util.Map<edu.cmu.cs.dennisc.scenegraph.Composite, VehicleChange> vehicleCache = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();

	@Override
	public void setRecording( boolean isRecording ) {
		super.setRecording( isRecording );
		if( isRecording ) {
			edu.cmu.cs.dennisc.scenegraph.Composite.EPIC_HACK_FOR_DINAH_addCompositeCreationListener( compositeCreationListener );
		} else {
			edu.cmu.cs.dennisc.scenegraph.Composite.EPIC_HACK_FOR_DINAH_removeCompositeCreationListener( compositeCreationListener );
		}
	}

	public void componentAdded( edu.cmu.cs.dennisc.scenegraph.event.ComponentAddedEvent event ) {

	}

	ComponentsListener componentListener = new ComponentsListener() {

		@Override
		public void componentAdded( ComponentAddedEvent e ) {
			if( ( shouldRecordChange() ) && ( e.getChild() instanceof edu.cmu.cs.dennisc.scenegraph.Composite ) ) {
				synchronized( history ) {
					VehicleTimeLine vehicleTimeLine = history.get( e.getChild() );
					vehicleTimeLine.add( e.getTypedSource(), getSourceThreadForCurrentAnimation(), getProgramTime() );
				}
			}

		}

		@Override
		public void componentRemoved( ComponentRemovedEvent e ) {
			//pass
		}

	};

	private void handleCompositeCreated( Composite composite ) {
		synchronized( this.history ) {
			VehicleTimeLine vehicleTimeLine = new VehicleTimeLine( composite );
			this.history.put( composite, vehicleTimeLine );
		}
		composite.addChildrenListener( componentListener );
	}

	edu.cmu.cs.dennisc.scenegraph.Composite.EPIC_HACK_FOR_DINAH_CompositeCreationListener compositeCreationListener = new EPIC_HACK_FOR_DINAH_CompositeCreationListener() {

		@Override
		public void compositeCreated( Composite composite ) {
			handleCompositeCreated( composite );
		}
	};

	@Override
	protected synchronized void scrubToTime( double rollbackTime, java.util.Set<Object> objectsToFilter ) {
		synchronized( this.history ) {
			for( VehicleTimeLine vehicleTimeLine : this.history.values() ) {
				java.util.SortedMap<Double, VehicleChange> map = vehicleTimeLine.getHeadMap( rollbackTime );
				VehicleChange vehicleChange;
				if( map.isEmpty() ) {
					vehicleChange = vehicleTimeLine.getNaNVehicleChange();
				} else {
					Double time = map.lastKey();
					vehicleChange = map.get( time );
				}

				if( ( vehicleChange != null ) && !vehicleChange.equals( vehicleCache.get( vehicleTimeLine.getComposite() ) ) ) {
					if( objectsToFilter.contains( vehicleChange.getSourceThread() ) || objectsToFilter.isEmpty() ) {
						Composite composite = vehicleTimeLine.getComposite();
						composite.setParent( vehicleChange.getVehicle() );
						vehicleCache.put( composite, vehicleChange );
					}
				}
			}
		}
	}

	@Override
	public void clearHistory() {
		for( Composite composite : this.history.keySet() ) {
			composite.removeChildrenListener( componentListener );
		}
		history.clear();
		vehicleCache.clear();
	}

	public edu.cmu.cs.dennisc.scenegraph.Composite getVehicleAtTime( edu.cmu.cs.dennisc.scenegraph.Composite composite, double time ) {
		synchronized( this.history ) {
			if( this.history.containsKey( composite ) ) {
				VehicleTimeLine vehicleTimeLine = this.history.get( composite );
				java.util.SortedMap<Double, VehicleChange> map = vehicleTimeLine.getHeadMap( time );

				if( map.isEmpty() ) {
					VehicleChange change = vehicleTimeLine.getNaNVehicleChange();
					if( change != null ) {
						return change.getVehicle();
					} else {
						return null;
					}
				} else {
					return map.get( map.lastKey() ).getVehicle();
				}
			}
		}
		return null;
	}

	public boolean hasVehicleChanges( edu.cmu.cs.dennisc.scenegraph.Composite composite ) {
		synchronized( this.history ) {
			return ( this.history.containsKey( composite ) ) && ( this.history.get( composite ).hasVehicleChanges() );
		}
	}
}
