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
package edu.wustl.lookingglass.ide.program;

import org.lgna.common.ComponentThread;
import org.lgna.project.virtualmachine.VirtualMachine;

import edu.wustl.lookingglass.animation.TimeAndThreadTrackingAnimator;
import edu.wustl.lookingglass.ide.program.event.ProgramReplayManager;
import edu.wustl.lookingglass.ide.program.thread.ReplayThread;
import edu.wustl.lookingglass.remix.share.ShareRemixComposite;
import edu.wustl.lookingglass.virtualmachine.MessagingVirtualMachine;
import edu.wustl.lookingglass.virtualmachine.event.VirtualMachineExecutionStateListener;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;

/**
 * @author Michael Pogran
 */

public abstract class ReplayableProgramImp extends org.lgna.story.implementation.ProgramImp implements VirtualMachineExecutionStateListener {
	private final edu.wustl.lookingglass.animation.TimeAndThreadTrackingAnimator animator;
	private final edu.wustl.lookingglass.virtualmachine.MessagingVirtualMachine virtualMachine;
	private edu.wustl.lookingglass.ide.program.event.ProgramReplayManager programReplayManager;
	private edu.wustl.lookingglass.virtualmachine.observer.VMExecutionObserver executionObserver;
	private edu.wustl.lookingglass.ide.program.thread.ReplayThread replayThread;

	private boolean finishedExecution = false;
	private ProgramState programState = ProgramState.PLAYING_LIVE;

	protected double currentProgramTime = 0.0;
	protected double maxProgramTime = 0.0;

	protected abstract void notifyProgramStateChange( ProgramState nextState, ProgramState previousState );

	protected abstract void notifyTimeChange( double currentTime );

	protected abstract void notifyStartingExecution( AbstractEventNode node, int index );

	protected abstract void notifyEndingExecution( AbstractEventNode node, int index );

	public ReplayableProgramImp( org.lgna.story.SProgram abstraction, VirtualMachine vm ) {
		super( abstraction, edu.cmu.cs.dennisc.render.gl.GlrRenderFactory.getInstance().createLightweightOnscreenRenderTarget( new edu.cmu.cs.dennisc.render.RenderCapabilities.Builder().build() ) );
		this.virtualMachine = (MessagingVirtualMachine)vm;
		this.animator = new edu.wustl.lookingglass.animation.TimeAndThreadTrackingAnimator();
		this.executionObserver = new edu.wustl.lookingglass.virtualmachine.observer.VMExecutionObserver( this.getVirtualMachine() );
		this.programReplayManager = new edu.wustl.lookingglass.ide.program.event.ProgramReplayManager( this );

		this.getVirtualMachine().addVirtualMachinePauseStateListener( this );
	}

	@Override
	public TimeAndThreadTrackingAnimator getAnimator() {
		return this.animator;
	}

	@Override
	public void shutDown() {
		this.executionObserver.setIsRecording( false );

		this.programReplayManager = null;
		this.executionObserver = null;
		super.shutDown();
	}

	public edu.wustl.lookingglass.virtualmachine.MessagingVirtualMachine getVirtualMachine() {
		return this.virtualMachine;
	}

	public ProgramReplayManager getProgramReplayManager() {
		return this.programReplayManager;
	}

	public edu.wustl.lookingglass.virtualmachine.observer.VMExecutionObserver getExecutionObserver() {
		return this.executionObserver;
	}

	public edu.wustl.lookingglass.ide.program.thread.ReplayThread getReplayThread() {
		return this.replayThread;
	}

	public void resumeProgram() {
		if( getCurrentProgramTime() < getMaxProgramTime() ) {
			handleProgramStateChange( ProgramState.PLAYING_REPLAY );
		} else {
			if( this.isProgramFinishedExecuting() ) {
				handleProgramStateChange( ProgramState.PAUSED_REPLAY );
			} else {
				handleProgramStateChange( ProgramState.PLAYING_LIVE );
			}
		}
	}

	public void pauseProgram() {
		if( getCurrentProgramTime() < getMaxProgramTime() ) {
			handleProgramStateChange( ProgramState.PAUSED_REPLAY );
		} else {
			handleProgramStateChange( ProgramState.PAUSED_LIVE );
		}
	}

	public void setCurrentTime( double currentTime ) {
		if( currentTime != Double.NaN ) {
			this.setProgramTime( currentTime );
			// Going to a less than max time while not replaying, we pause
			if( ( currentTime < this.maxProgramTime ) && ( this.getProgramState() != ProgramState.PLAYING_REPLAY ) ) {
				this.handleProgramStateChange( ProgramState.PAUSED_REPLAY );
			} else {
				this.notifyTimeChange( currentTime );
			}
		}
	}

	private void setProgramTime( double time ) {
		// when going to the end of an unfinished statement
		if( Double.isInfinite( time ) ) {
			time = this.maxProgramTime;
		}
		this.currentProgramTime = time;
		if( time > this.maxProgramTime ) {
			this.maxProgramTime = time;
		}
	}

	public double getCurrentProgramTime() {
		return this.currentProgramTime;
	}

	public double getMaxProgramTime() {
		return this.maxProgramTime;
	}

	protected void handleProgramStateChange( ProgramState nextState ) {
		ProgramState previousState = this.programState;

		//edu.cmu.cs.dennisc.java.util.logging.Logger.outln( "From: ", previousState, "To: ", nextState, "Time: ", getCurrentProgramTime() );

		if( previousState == ProgramState.PLAYING_LIVE ) {
			if( nextState.isPaused() ) {
				this.getAnimator().setSpeedFactor( 0.0 );
			}

		} else if( previousState == ProgramState.PAUSED_LIVE ) {
			if( nextState == ProgramState.PLAYING_LIVE ) {
				this.getAnimator().setSpeedFactor( 1.0 );
				if( this.virtualMachine.isPaused() ) {
					this.virtualMachine.setUnpaused();
				}
			}

		} else if( previousState == ProgramState.PLAYING_REPLAY ) {
			if( nextState == ProgramState.PLAYING_LIVE ) {
				this.getAnimator().setSpeedFactor( 1.0 );
			} else if( nextState == ProgramState.PAUSED_REPLAY ) {
				this.stopReplay();
			}

		} else if( previousState == ProgramState.PAUSED_REPLAY ) {
			if( nextState == ProgramState.PLAYING_REPLAY ) {
				this.replayThread = this.getProgramReplayManager().createReplayAndContinueThread( this );
				this.startReplayThread();
			} else if( nextState == ProgramState.PLAYING_LIVE ) {
				this.getAnimator().setSpeedFactor( 1.0 );
			}
		}

		this.programState = nextState;
		this.notifyProgramStateChange( nextState, previousState );
	}

	public void setExecutingStatement( AbstractEventNode<?> next, AbstractEventNode<?> prev, int nextIndex, int prevIndex ) {
		if( prev != null ) {
			this.notifyEndingExecution( prev, prevIndex );
		}
		this.notifyStartingExecution( next, nextIndex );
	}

	@Override
	public void isChangedToPaused() {
		if( ( this.executionObserver != null ) && this.executionObserver.hasInvokedEntryPoint() ) {
			this.finishedExecution = true;
		}
		pauseProgram();
	}

	@Override
	public void isChangedToRunning() {
		if( this.executionObserver.hasInvokedEntryPoint() ) {
			this.finishedExecution = false;
		}
		resumeProgram();
	}

	@Override
	public void isEndingExecution() {
		this.animator.completeAll( null );
		this.animator.cancelAnimation();
		this.finishedExecution = true;
	}

	public ProgramState getProgramState() {
		return this.programState;
	}

	public boolean isProgramFinishedExecuting() {
		return this.finishedExecution;
	}

	public void resetRunningProgramSpeed() {
		this.handleSpeedChange( 1.0 );
	}

	@Override
	public void setRunningProgramSpeed( double speed ) {
		this.handleSpeedChange( speed );
	}

	@Override
	protected void handleSpeedChange( double speedFactor ) {
		if( this.getProgramState() == ProgramState.PLAYING_LIVE ) {
			super.handleSpeedChange( speedFactor );
		} else {
			if( this.getReplayThread() != null ) {
				this.getReplayThread().setReplaySpeed( speedFactor );
			}
		}
	}

	// Replay Thread Support
	public boolean isReplayThreadActive() {
		return ( this.replayThread != null ) && this.replayThread.isAlive() && this.replayThread.isReplaying();
	}

	public boolean isReplayingEventNode( AbstractEventNode node ) {
		if( isReplayThreadActive() ) {
			return this.replayThread.isReplayingNode( node );
		} else {
			return false;
		}
	}

	public void stopReplayingEventNode( AbstractEventNode node ) {
		if( ( this.replayThread != null ) && this.replayThread.isReplayingNode( node ) ) {
			stopReplayThread();
		} else {
			edu.cmu.cs.dennisc.java.util.logging.Logger.severe( "Attempted to stop a null replay thread" );
		}
	}

	public void removeReplayThread() {
		this.replayThread = null;
	}

	public void stopReplay() {
		stopReplayThread();
	}

	private void stopReplayThread() {
		if( isReplayThreadActive() ) {
			this.replayThread.stopReplaying();
			this.replayThread.join();
		}
	}

	private void startReplayThread() {
		replayThread.startReplaying();
	}

	protected void setReplayThread( ReplayThread thread ) {
		this.pauseProgram();
		this.stopReplayThread();
		this.replayThread = thread;
		this.startReplayThread();
	}

	// Replay of a single statement or block
	public ReplayThread continuouslyReplayEvent( AbstractEventNode node, final boolean replayOnlyNode ) {
		this.setReplayThread( this.getProgramReplayManager().createContinuousReplayThread( node, replayOnlyNode ) );
		return this.replayThread;
	}

	// Replay of a time period - mostly this enables previewing a captured remix
	public ReplayThread continuouslyReplayDependentThreadTimePeriod( final AbstractEventNode start, final AbstractEventNode end, final AbstractEventNode sharedParent ) {
		this.setReplayThread( this.getProgramReplayManager().createContinuousThreadDependentReplayThread( start, end, sharedParent ) );
		return this.replayThread;
	}

	// Replay of a captured remix to create a video
	public ReplayThread recordTimePeriod( double startTime, double endTime, ComponentThread executionThreadToReplay ) {
		this.setReplayThread( this.getProgramReplayManager().createRecordAndReplayThread( startTime, endTime, executionThreadToReplay, null, ShareRemixComposite.getInstance().getEncoder() ) );
		return this.replayThread;
	}

	public ReplayThread replayTimePeriod( double startTime, double endTime, ComponentThread replayThreadContext, AbstractEventNode replayNode ) {
		this.setReplayThread( this.getProgramReplayManager().createReplayTimePeriodThread( startTime, endTime, replayThreadContext, replayNode ) );
		return this.replayThread;
	}

	public ReplayThread replayStep() {
		ReplayThread thread = this.getProgramReplayManager().createStepReplayThread();
		if( thread != null ) {
			this.setReplayThread( thread );
			return this.replayThread;
		} else {
			return null;
		}
	}
}
