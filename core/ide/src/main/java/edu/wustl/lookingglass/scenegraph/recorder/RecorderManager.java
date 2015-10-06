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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.wustl.lookingglass.ide.program.TimeScrubProgramImp;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.ide.program.event.TimeScrubProgramListener;
import edu.wustl.lookingglass.story.recorder.PropertyValueRecorder;

public class RecorderManager implements TimeScrubProgramListener {

	private TimeScrubProgramImp program;
	private java.util.List<RandomAccessRecorder> recorders = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();
	private PropertyValueRecorder propertyRecorder;
	private VehicleChangeRecorder vehicleRecorder;
	private TransformationChangeRecorder transformationRecorder;
	private ScaleChangeRecorder scaleRecorder;

	private static final ExecutorService pool = Executors.newCachedThreadPool( new java.util.concurrent.ThreadFactory() {
		private Integer count = 0;

		@Override
		public Thread newThread( Runnable r ) {
			synchronized( count ) {
				return new Thread( r, "Recorder-" + count++ );
			}
		}
	} );

	private static final RecorderManager singleton = new RecorderManager();

	// TODO: remove this singleton
	public static RecorderManager getInstance() {
		return singleton;
	}

	private RecorderManager() {
	}

	// TimeScrubProgramListener
	@Override
	public void programStateChange( ProgramStateEvent programStateEvent ) {
		if( programStateEvent.getNextState().isLive() ) {
			setRecordersViewingHistory( false );
		} else {
			setRecordersViewingHistory( true );
			scrubToTime( programStateEvent.getTime() );
		}
	}

	@Override
	public void startingExecution( ProgramExecutionEvent programExecutionEvent ) {
	}

	@Override
	public void endingExecution( ProgramExecutionEvent programExecutionEvent ) {
	}

	public void scrubToTime( double time ) {
		synchronized( this.recorders ) {
			java.util.concurrent.CyclicBarrier barrier = new java.util.concurrent.CyclicBarrier( 3, this.transformationRecorder.createScrubRunnable( time, RecorderRollbackFilter.getFilterCopy() ) );

			for( RandomAccessRecorder recorder : this.recorders ) {
				if( !( recorder instanceof TransformationChangeRecorder ) ) {
					Runnable task = recorder.createScrubRunnable( time, RecorderRollbackFilter.getFilterCopy(), barrier );
					RecorderManager.pool.execute( task );
				}
			}
		}
	}

	public void setToTime( double time ) {
		synchronized( this.recorders ) {
			for( RandomAccessRecorder recorder : this.recorders ) {
				Runnable task = recorder.createScrubRunnable( time, RecorderRollbackFilter.getFilterCopy() );
				task.run();
			}
		}
	}

	private void setRecordersViewingHistory( boolean isViewingHistory ) {
		synchronized( this.recorders ) {
			for( RandomAccessRecorder recorder : recorders ) {
				recorder.setViewingHistory( isViewingHistory );
			}
		}
	}

	public PropertyValueRecorder getPropertyRecorder() {
		return this.propertyRecorder;
	}

	public VehicleChangeRecorder getVehicleRecorder() {
		return this.vehicleRecorder;
	}

	public void initialize() {
		this.propertyRecorder = new PropertyValueRecorder();
		this.vehicleRecorder = new VehicleChangeRecorder();
		this.transformationRecorder = new TransformationChangeRecorder();
		this.scaleRecorder = new ScaleChangeRecorder();

		recorders.add( this.vehicleRecorder );
		recorders.add( this.propertyRecorder );
		recorders.add( this.scaleRecorder );
		recorders.add( this.transformationRecorder );

		synchronized( this.recorders ) {
			for( RandomAccessRecorder recorder : this.recorders ) {
				recorder.setRecording( true );
			}
		}
	}

	public void setProgram( TimeScrubProgramImp program ) {
		if( program != null ) {
			this.program = program;
			this.program.addTimeScrubProgramListener( this );
		}

		synchronized( this.recorders ) {
			for( RandomAccessRecorder recorder : this.recorders ) {
				recorder.setProgram( program );
			}
		}
	}

	public void deactivate() {
		synchronized( this.recorders ) {
			for( RandomAccessRecorder recorder : this.recorders ) {
				recorder.cleanUpRecorder();
			}
			this.recorders.clear();
		}

		this.propertyRecorder = null;
		this.vehicleRecorder = null;
		this.transformationRecorder = null;
		this.scaleRecorder = null;

		if( this.program != null ) {
			this.program.removeTimeScrubProgramListener( RecorderManager.this );
			this.program = null;
		}
	}
}
