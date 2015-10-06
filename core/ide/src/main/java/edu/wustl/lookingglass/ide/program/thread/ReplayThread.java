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
package edu.wustl.lookingglass.ide.program.thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.wustl.lookingglass.ide.program.ReplayableProgramImp;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;

/**
 *
 * This is an abstraction of a thread for replaying so we can treat replaying
 * statements in independent threads the same as replaying the whole program.
 * The subclasses address specific use cases.
 *
 * @author Paul Gross
 */
public abstract class ReplayThread implements Runnable {

	private boolean isReplaying = false;
	public AbstractEventNode<?> replayNode = null;
	protected org.lgna.common.ComponentThread thread = null;
	protected double startTime = Double.NaN;
	protected double endTime = Double.NaN;
	protected double replaySpeed = 1.0;

	protected final int REPLAY_STEPS_PER_SECOND = 200;
	protected final double REPLAY_TIME_STEP_INCREMENT = 1 / (double)REPLAY_STEPS_PER_SECOND;
	protected final int REPLAY_MS_WAIT_BETWEEN_STEPS = 1000 / REPLAY_STEPS_PER_SECOND;

	protected final ReplayableProgramImp program;

	public abstract void preRun();

	public abstract void postRun();

	private org.lgna.croquet.BoundedIntegerState replayTimeState;

	private java.util.concurrent.Future<?> task = null;

	private static final ExecutorService pool = Executors.newCachedThreadPool( new java.util.concurrent.ThreadFactory() {
		private Integer count = 0;

		@Override
		public Thread newThread( Runnable r ) {
			synchronized( count ) {
				return new Thread( r, "Replay-" + count++ );
			}
		}
	} );

	public ReplayThread( ReplayableProgramImp program ) {
		this.replayTimeState = new org.lgna.croquet.BoundedIntegerState(
				new org.lgna.croquet.BoundedIntegerState.Details(
						edu.wustl.lookingglass.remix.models.ReuseGroup.REUSE_GROUP,
						java.util.UUID.fromString( "b99d245c-cdaf-43fe-a029-d81818a7184c" )
				).maximum( 0 ).initialValue( 0 ).maximum( 100 ) ) {
		};
		this.program = program;
	}

	@Override
	public void run() {
		this.preRun();
		this.executeReplayLoop();
		this.stopReplaying();
		this.postRun();
	}

	protected void executeReplayLoop() {
		long startUpdateTime = 0L, additionalTimeToWait = 0L;
		double currentTime = startTime;

		double timeDifference = endTime - startTime;
		replayTimeState.setValueTransactionlessly( 0 );

		// While we haven't gotten really close to the end time, and no one has told us to stop replaying
		while( !edu.cmu.cs.dennisc.math.EpsilonUtilities.isWithinReasonableEpsilonOf0InSquaredSpace( Math.abs( endTime - currentTime ) ) && ( currentTime < endTime ) && this.isReplaying() ) {

			// Increment the time we're going to show next and multiply it by some speed
			currentTime += REPLAY_TIME_STEP_INCREMENT * replaySpeed;

			// If we surpass the endtime, reset to the end time. We'll leave the loop after this iteration.
			if( currentTime > endTime ) {
				currentTime = endTime;
			}

			if( this instanceof ReplayToLiveThread ) {
				//pass
			} else {
				replayTimeState.setValueTransactionlessly( (int)( ( 100 * ( currentTime - startTime ) ) / timeDifference ) );
			}

			// Get the current time so we can account for how long it may take us to update all the listeners
			//  i.e., the recorders which set the program state
			startUpdateTime = System.currentTimeMillis();

			if( isReplaying() ) {
				this.program.setCurrentTime( currentTime );
			}

			// If we took a little while to update listeners, compute how much time we actually need to wait
			//  so that we wait for a total of REPLAY_MS_WAIT_BETWEEN_STEPS. This ideally keeps our replay smooth
			//  and consistently replicates the natural refresh rate.
			additionalTimeToWait = REPLAY_MS_WAIT_BETWEEN_STEPS - ( System.currentTimeMillis() - startUpdateTime );

			// If we have to wait some and we're still going
			if( ( additionalTimeToWait > 0L ) && isReplaying() ) {
				synchronized( this ) {
					try {
						this.wait( additionalTimeToWait );
					} catch( InterruptedException e ) {
						e.printStackTrace();
					}
				}
				// If we don't have to wait, in fact, we had to wait longer than we wanted, skip ahead to the time we
				//  we should be at
			} else {
				currentTime += ( (double)( System.currentTimeMillis() - startUpdateTime ) ) / 1000;
			}
		}
	}

	public boolean isAlive() {
		return !this.task.isDone();
	}

	public void join() {
		try {
			this.task.get();
		} catch( InterruptedException | ExecutionException e ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( e, this );
		}
	}

	public synchronized void startReplaying() {
		this.isReplaying = true;
		this.task = ReplayThread.pool.submit( this );
	}

	public synchronized void stopReplaying() {
		this.isReplaying = false;
	}

	public synchronized boolean isReplaying() {
		return this.isReplaying;
	}

	public synchronized boolean isReplayingNode( AbstractEventNode<?> node ) {
		return this.isReplaying && ( this.replayNode == node );

	}

	public synchronized boolean isReplayingThread( org.lgna.common.ComponentThread thread ) {
		return this.isReplaying && ( this.thread == thread );
	}

	public void setReplaySpeed( double speed ) {
		if( speed > 0.0 ) {
			this.replaySpeed = speed;
		}
	}

	public org.lgna.croquet.BoundedIntegerState getReplayTimeState() {
		return this.replayTimeState;
	}
}
