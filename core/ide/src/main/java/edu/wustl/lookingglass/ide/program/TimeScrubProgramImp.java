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

import java.awt.event.ActionEvent;

import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;
import org.lgna.project.ast.Statement;

import edu.cmu.cs.dennisc.java.util.Sets;
import edu.wustl.lookingglass.animation.event.AnimatorTimeListener;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.ide.program.event.StatementChangeEvent;
import edu.wustl.lookingglass.ide.program.event.StatementChangeListener;
import edu.wustl.lookingglass.ide.program.event.TimeScrubProgramListener;
import edu.wustl.lookingglass.ide.program.models.FastForwardOperation;
import edu.wustl.lookingglass.ide.program.models.ProgramStatementManager;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.observer.event.CurrentExecutionListener;

/**
 * @author Michael Pogran
 */

public class TimeScrubProgramImp extends ReplayableProgramImp {
	private final PlayPauseOperation playPauseOperation;
	private final RestartOperation restartOperation;
	private final FastForwardOperation fastForwardOperation;
	private ProgramStatementManager programStatementManager;
	private java.util.HashSet<TimeScrubProgramListener> programListeners = Sets.newHashSet();

	public TimeScrubProgramImp( org.lgna.story.SProgram abstraction, org.lgna.project.virtualmachine.VirtualMachine vm ) {
		super( abstraction, vm );
		this.programStatementManager = new ProgramStatementManager( this );
		this.playPauseOperation = new PlayPauseOperation();
		this.restartOperation = new RestartOperation();
		this.fastForwardOperation = new FastForwardOperation( this );

		this.getAnimator().addAnimatorTimeListener( new AnimatorTimeListener() {

			@Override
			public void notifyCurrentTime( double currentTime ) {
				TimeScrubProgramImp.this.setCurrentTime( currentTime );
			}
		} );

		this.getExecutionObserver().addCurrentExecutionListener( new CurrentExecutionListener() {

			@Override
			public void startingExecution( AbstractEventNode<?> node ) {
				TimeScrubProgramImp.this.notifyStartingExecution( node, TimeScrubProgramImp.this.programStatementManager.getMaximum() );
			}

			@Override
			public void endingExecution( AbstractEventNode<?> node ) {
				TimeScrubProgramImp.this.notifyEndingExecution( node, TimeScrubProgramImp.this.programStatementManager.getMaximum() );
			}

		} );

		this.getProgramStatementManager().addStatementChangeListener( new StatementChangeListener() {

			@Override
			public void statementChange( StatementChangeEvent statementChangeEvent ) {
				for( AbstractEventNode<?> node : statementChangeEvent.getPreviousStatements() ) {
					TimeScrubProgramImp.this.notifyEndingExecution( node, statementChangeEvent.getPreviousIndex() );
				}

				for( AbstractEventNode<?> node : statementChangeEvent.getNextStatements() ) {
					TimeScrubProgramImp.this.notifyStartingExecution( node, statementChangeEvent.getNextIndex() );
				}
			}
		} );
	}

	@Override
	public void shutDown() {
		synchronized( this.programListeners ) {
			this.programListeners.clear();
		}
		this.programStatementManager.shutDown();
		this.programStatementManager = null;
		super.shutDown();
	}

	public ProgramStatementManager getProgramStatementManager() {
		return this.programStatementManager;
	}

	public PlayPauseOperation getPlayPauseOperation() {
		return this.playPauseOperation;
	}

	public RestartOperation getRestartOperation() {
		return this.restartOperation;
	}

	public FastForwardOperation getFastForwardOperation() {
		return this.fastForwardOperation;
	}

	public void addTimeScrubProgramListener( TimeScrubProgramListener listener ) {
		synchronized( this.programListeners ) {
			this.programListeners.add( listener );
		}
	}

	public void removeTimeScrubProgramListener( TimeScrubProgramListener listener ) {
		synchronized( this.programListeners ) {
			this.programListeners.remove( listener );
		}
	}

	public void removeAllListeners() {
		synchronized( this.programListeners ) {
			this.programListeners.clear();
		}
	}

	@Override
	protected void notifyProgramStateChange( ProgramState nextState, ProgramState previousState ) {
		synchronized( this.programListeners ) {
			for( TimeScrubProgramListener listener : this.programListeners ) {
				listener.programStateChange( new ProgramStateEvent( nextState, previousState, this.isProgramFinishedExecuting(), this.getCurrentProgramTime(), this.maxProgramTime ) );
			}
		}
	}

	@Override
	protected void notifyTimeChange( double currentTime ) {
		synchronized( this.programListeners ) {
			for( TimeScrubProgramListener listener : this.programListeners ) {
				listener.programStateChange( new ProgramStateEvent( this.getProgramState(), this.getProgramState(), this.isProgramFinishedExecuting(), currentTime, this.maxProgramTime ) );
			}
		}
	}

	@Override
	protected void notifyStartingExecution( AbstractEventNode node, int index ) {
		synchronized( this.programListeners ) {
			for( TimeScrubProgramListener listener : this.programListeners ) {
				listener.startingExecution( new ProgramExecutionEvent( node, (Statement)node.getAstNode(), index ) );
			}
		}
	}

	@Override
	protected void notifyEndingExecution( AbstractEventNode node, int index ) {
		synchronized( this.programListeners ) {
			for( TimeScrubProgramListener listener : this.programListeners ) {
				listener.endingExecution( new ProgramExecutionEvent( node, (Statement)node.getAstNode(), index ) );
			}
		}
	}

	public class PlayPauseOperation extends org.lgna.croquet.Operation {

		public PlayPauseOperation() {
			super( org.lgna.croquet.Application.PROJECT_GROUP, java.util.UUID.fromString( "d726c6f8-5922-4f67-8928-691d4ed20d48" ) );

			TimeScrubProgramImp.this.addTimeScrubProgramListener( new TimeScrubProgramListener() {

				@Override
				public void programStateChange( ProgramStateEvent programStateEvent ) {
					if( programStateEvent.getNextState().isPlaying() ) {
						setButtonIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "world-pause", org.lgna.croquet.icon.IconSize.SMALL ) );
					} else {
						setButtonIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "world-start", org.lgna.croquet.icon.IconSize.SMALL ) );
					}

					if( programStateEvent.isFinishedExecuting() && ( programStateEvent.getTime() == TimeScrubProgramImp.this.getMaxProgramTime() ) ) {
						setEnabled( false );
					} else {
						setEnabled( true );
					}
				}

				@Override
				public void startingExecution( ProgramExecutionEvent programExecutionEvent ) {
				}

				@Override
				public void endingExecution( ProgramExecutionEvent programExecutionEvent ) {
				}

			} );
		}

		@Override
		protected void localize() {
			this.setButtonIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "world-pause", org.lgna.croquet.icon.IconSize.SMALL ) );
		}

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			if( TimeScrubProgramImp.this.getProgramState().isPlaying() ) {
				TimeScrubProgramImp.this.pauseProgram();
				this.setButtonIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "world-start", org.lgna.croquet.icon.IconSize.SMALL ) );
			} else {
				TimeScrubProgramImp.this.resumeProgram();
				this.setButtonIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "world-pause", org.lgna.croquet.icon.IconSize.SMALL ) );
			}
		}
	}

	public class RestartOperation extends org.lgna.croquet.Operation {

		public RestartOperation() {
			super( org.lgna.croquet.Application.PROJECT_GROUP, java.util.UUID.fromString( "a7b3878a-22d9-4cc2-ab18-881ae2994fb9" ) );
		}

		@Override
		protected void localize() {
			this.setButtonIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "world-restart", org.lgna.croquet.icon.IconSize.SMALL ) );
		}

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			TimeScrubProgramImp.this.getRestartAction().actionPerformed( new java.awt.event.ActionEvent( this, ActionEvent.ACTION_PERFORMED, null ) );
		}

	}
}
