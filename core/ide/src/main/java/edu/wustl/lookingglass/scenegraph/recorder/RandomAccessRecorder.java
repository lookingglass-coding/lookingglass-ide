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

import java.util.concurrent.BrokenBarrierException;

import org.lgna.common.ComponentThread;

import edu.wustl.lookingglass.animation.TimeAndThreadTrackingAnimator;
import edu.wustl.lookingglass.ide.program.ReplayableProgramImp;

public abstract class RandomAccessRecorder {
	private ReplayableProgramImp program;
	private boolean isViewingHistory = false;
	private boolean isRecording = false;

	public void setProgram( ReplayableProgramImp program ) {
		this.program = program;
	}

	public void cleanUpRecorder() {
		setRecording( false );
		clearHistory();
		this.program = null;
	}

	public void setRecording( boolean isRecording ) {
		synchronized( this ) {
			this.isRecording = isRecording;
		}
	}

	protected boolean isRecording() {
		return this.isRecording;
	}

	public void setViewingHistory( boolean isViewingHistory ) {
		synchronized( this ) {
			this.isViewingHistory = isViewingHistory;
		}
	}

	protected boolean isViewingHistory() {
		synchronized( this ) {
			return isViewingHistory && !Double.isNaN( getProgramTime() );
		}
	}

	protected boolean shouldRecordChange() {
		return ( this.isRecording ) && ( !this.isViewingHistory );
	}

	protected edu.cmu.cs.dennisc.animation.Animator getProgramAnimator() {
		if( this.program != null ) {
			return this.program.getAnimator();
		} else {
			return null;
		}
	}

	protected double getProgramTime() {
		if( this.program != null ) {
			return this.program.getAnimator().getCurrentTime();
		} else {
			return Double.NaN;
		}
	}

	protected edu.cmu.cs.dennisc.scenegraph.Scene getSceneSGComposite() {
		if( this.program != null ) {
			return this.program.getAbstraction().getActiveScene().getImplementation().getSgComposite();
		} else {
			return null;
		}
	}

	protected ComponentThread getSourceThreadForCurrentAnimation() {
		edu.cmu.cs.dennisc.animation.Animator animator = getProgramAnimator();
		if( ( animator != null ) && ( animator instanceof TimeAndThreadTrackingAnimator ) ) {
			return ( (TimeAndThreadTrackingAnimator)animator ).getSourceThreadForCurrentAnimation();
		} else {
			return ComponentThread.currentThread();
		}
	}

	protected Runnable createScrubRunnable( final double time, final java.util.Set<Object> objectsToFilter ) {
		return new Runnable() {
			@Override
			public void run() {
				scrubToTime( time, objectsToFilter );
			}
		};
	}

	protected Runnable createScrubRunnable( final double time, final java.util.Set<Object> objectsToFilter, java.util.concurrent.CyclicBarrier barrier ) {
		return new Runnable() {
			@Override
			public void run() {
				scrubToTime( time, objectsToFilter );
				try {
					barrier.await();
				} catch( InterruptedException e ) {
					e.printStackTrace();
				} catch( BrokenBarrierException e ) {
					e.printStackTrace();
				}
			}
		};
	}

	protected abstract void scrubToTime( double rollbackTime, java.util.Set<Object> objectsToFilter );

	public abstract void clearHistory();
}
