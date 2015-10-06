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
package edu.wustl.lookingglass.puzzle.ui.croquet;

import org.lgna.project.virtualmachine.events.StatementExecutionEvent;
import org.lgna.project.virtualmachine.events.VirtualMachineListener;

import edu.wustl.lookingglass.ide.program.RunProjectProgramContext;
import edu.wustl.lookingglass.puzzle.CompletionPuzzle;
import edu.wustl.lookingglass.puzzle.PuzzleStatementUtility;
import edu.wustl.lookingglass.puzzle.PuzzleStatus;
import edu.wustl.lookingglass.puzzle.PuzzleStatus.StatementStatus;
import edu.wustl.lookingglass.puzzle.ui.PuzzleStatusIndicator;
import edu.wustl.lookingglass.puzzle.ui.StatementStatusIndicator;
import edu.wustl.lookingglass.puzzle.ui.StatementStatusIndicator.ExecutionStatus;
import edu.wustl.lookingglass.puzzle.ui.croquet.views.PlayPuzzleProjectView;
import edu.wustl.lookingglass.virtualmachine.event.VirtualMachineExecutionStateListener;

/**
 * @author Kyle J. Harms
 */
public class PlayPuzzleProjectComposite extends org.lgna.croquet.SimpleComposite<PlayPuzzleProjectView> {

	private static final double FAST_FORWARD_SPEED = 2.0;

	private final CompletionPuzzle puzzle;

	private final PuzzleStatusIndicator indicator;

	private RunProjectProgramContext programContext;
	private VirtualMachineListener vmStateListener;
	private VirtualMachineExecutionStateListener vmExecutionListener;
	private final org.lgna.common.ProgramExecutionExceptionHandler programExceptionHandler;

	private Runnable onExecutionEnded;

	private final java.util.concurrent.Semaphore programLock = new java.util.concurrent.Semaphore( 1 );

	public PlayPuzzleProjectComposite( CompletionPuzzle puzzle, PuzzleStatusIndicator indicator ) {
		super( java.util.UUID.fromString( "2ebb3c13-af1f-45d8-be4f-b571d6ea6f53" ) );
		this.puzzle = puzzle;
		this.indicator = indicator;

		this.vmStateListener = new VirtualMachineListener() {
			@Override
			public void statementExecuting( StatementExecutionEvent statementExecutionEvent ) {
				PlayPuzzleProjectComposite.this.handleStatementExecutingEvent( statementExecutionEvent );
			}

			@Override
			public void statementExecuted( org.lgna.project.virtualmachine.events.StatementExecutionEvent statementExecutionEvent ) {
				PlayPuzzleProjectComposite.this.handleStatementExecutedEvent( statementExecutionEvent );
			}
		};

		this.vmExecutionListener = new VirtualMachineExecutionStateListener() {
			@Override
			public void isChangedToPaused() {
				if( onExecutionEnded != null ) {
					onExecutionEnded.run();
				}
			}
		};

		this.programExceptionHandler = new org.lgna.common.ProgramExecutionExceptionHandler() {
			@Override
			public void handleProgramExecutionExeception( java.lang.Throwable t ) {
				PlayPuzzleProjectComposite.this.handleProgramException( t );
			}
		};
	}

	public void play() {
		javax.swing.SwingUtilities.invokeLater( () -> {
			try {
				this.programLock.acquireUninterruptibly();
				this.cleanup();
				this.start();
			} finally {
				this.programLock.release();
			}
		} );
	}

	public void reset() {
		javax.swing.SwingUtilities.invokeLater( () -> {
			try {
				this.programLock.acquireUninterruptibly();
				this.cleanup();
			} finally {
				this.programLock.release();
			}
		} );
	}

	public void toggleFastForward( boolean shouldFastForward ) {
		try {
			this.programLock.acquireUninterruptibly();
			if( this.programContext != null ) {
				double speed = 1.0;
				if( shouldFastForward ) {
					speed = FAST_FORWARD_SPEED;
				}
				programContext.getProgramImp().setRunningProgramSpeed( speed );
			}
		} finally {
			this.programLock.release();
		}
	}

	public void setOnVmExecutionEnded( Runnable runnable ) {
		this.onExecutionEnded = runnable;
	}

	private void handleProgramException( Throwable t ) {
		// So when executing a statement... an error occurred... the statement may have been marked correct...
		// regardless we need the user to know that something didn't go right, so for now we just add another
		// incorrect dot.
		edu.cmu.cs.dennisc.java.util.logging.Logger.warning( "an exception occurred when executing puzzle", t );
		javafx.application.Platform.runLater( () -> {
			try {
				this.statementsLock.acquireUninterruptibly();

				StatementStatusIndicator statementStatusIndicator = new StatementStatusIndicator( null, PuzzleStatus.State.INCORRECT );
				PlayPuzzleProjectComposite.this.indicator.addStatementStatus( statementStatusIndicator );
			} finally {
				this.statementsLock.release();
			}
		} );
	}

	private void start() {
		assert javax.swing.SwingUtilities.isEventDispatchThread();

		this.programContext = new RunProjectProgramContext( this.puzzle.getPuzzleProject() );
		this.programContext.setProgramExceptionHandler( this.programExceptionHandler );
		this.programContext.getVirtualMachine().addVirtualMachineListener( this.vmStateListener );
		this.programContext.setActiveSceneOnComponentThreadAndWait();
		this.programContext.initializeInContainer( this.getView().getAwtComponent() );
		this.programContext.getVirtualMachine().addVirtualMachinePauseStateListener( this.vmExecutionListener );
	}

	private void cleanup() {
		assert javax.swing.SwingUtilities.isEventDispatchThread();

		this.getView().setBackgroundColor( java.awt.Color.BLACK );
		if( this.programContext != null ) {
			this.programContext.getVirtualMachine().removeVirtualMachineListener( this.vmStateListener );
			this.programContext.getVirtualMachine().removeVirtualMachinePauseStateListener( this.vmExecutionListener );
			this.programContext.cleanUpProgram();
			this.programContext = null;
		}

		this.executingStatementStatusesMap.clear();
		this.executingBlockStatementsMap.clear();

		this.resetIndicatorStatus();

		this.getView().revalidateAndRepaint();
	}

	public void resetIndicatorStatus() {
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			this.indicator.reset();
		} );
	}

	private final java.util.concurrent.Semaphore statementsLock = new java.util.concurrent.Semaphore( 1 );
	private final java.util.Map<StatementExecutionEvent, StatementStatusIndicator> executingStatementStatusesMap = new java.util.concurrent.ConcurrentHashMap<>();
	private final java.util.Map<org.lgna.project.ast.BlockStatement, org.lgna.project.ast.AbstractStatementWithBody> executingBlockStatementsMap = new java.util.concurrent.ConcurrentHashMap<>();

	private void handleStatementExecutingEvent( StatementExecutionEvent statementExecutionEvent ) {
		final org.lgna.project.ast.Statement statement = statementExecutionEvent.getStatement();

		if( this.puzzle.getPuzzleDoInOrder() == statement ) {
			StatementStatus statementStatus = this.puzzle.getPuzzleComparison().getStatus().getStatementStatus( statement );
			this.executingStatementStatusesMap.put( statementExecutionEvent, new StatementStatusIndicator( statementStatus.getStatement(), statementStatus.getState() ) );

			// Make sure we can report missing statements inside of block statements
			org.lgna.project.ast.AbstractStatementWithBody bodyStatement = PuzzleStatementUtility.asBodyStatement( statement );
			if( bodyStatement != null ) {
				this.executingBlockStatementsMap.put( bodyStatement.body.getValue(), bodyStatement );
			}
		} else {
			final PuzzleStatus.StatementStatus status = this.puzzle.getPuzzleComparison().getStatus().getStatementStatus( statement );
			final PuzzleStatus.State state;
			String styleClass = null;
			if( status != null ) {
				state = status.getState();
			} else if( this.puzzle.getNonPuzzleStatements().contains( statement ) ) {
				// Statements outside of the puzzle block are obviously correct.
				// Please note that we disable the end non puzzle statements until the puzzle is
				// correct. If we stop doing this, then this status for the statements after
				// the puzzle block would have to be updated for an incorrect puzzle to UNKNOWN.
				state = PuzzleStatus.State.CORRECT;
			} else if( this.puzzle.getReferenceStatements().contains( statement ) ) {
				state = PuzzleStatus.State.CORRECT;
				styleClass = "indicator-correct-puzzle";
			} else {
				state = null;
			}

			if( state != null ) {
				StatementStatusIndicator statementStatusIndicator = new StatementStatusIndicator( statement, state );
				if( styleClass != null ) {
					statementStatusIndicator.getStyleClass().add( styleClass );
				}
				this.executingStatementStatusesMap.put( statementExecutionEvent, statementStatusIndicator );

				// Make sure we can report missing statements inside of block statements
				org.lgna.project.ast.AbstractStatementWithBody bodyStatement = PuzzleStatementUtility.asBodyStatement( statement );
				if( bodyStatement != null ) {
					this.executingBlockStatementsMap.put( bodyStatement.body.getValue(), bodyStatement );
				}

				// Commit the update
				final StatementStatusIndicator ssi = statementStatusIndicator;
				javafx.application.Platform.runLater( () -> {
					try {
						this.statementsLock.acquireUninterruptibly();

						PlayPuzzleProjectComposite.this.indicator.addStatementStatus( ssi );
						ssi.setExecutionStatus( ExecutionStatus.EXECUTING );

						// Missing statements in a Do Together show up right away.
						if( ( status != null ) && ( statement instanceof org.lgna.project.ast.DoTogether ) ) {
							for( PuzzleStatus.StatementStatus missing : status.getMissingStatuses() ) {
								StatementStatusIndicator indicator = new StatementStatusIndicator( missing.getStatement(), missing.getState() );
								PlayPuzzleProjectComposite.this.indicator.addStatementStatus( indicator );
								indicator.setExecutionStatus( ExecutionStatus.NOT_EXECUTING );
							}
						}
					} finally {
						this.statementsLock.release();
					}
				} );
			}
		}
	}

	private void handleStatementExecutedEvent( StatementExecutionEvent statementExecutionEvent ) {
		final org.lgna.project.ast.Statement statement = statementExecutionEvent.getStatement();

		final StatementStatusIndicator statementStatusIndicator = this.executingStatementStatusesMap.get( statementExecutionEvent );
		this.executingStatementStatusesMap.remove( statementExecutionEvent );

		final org.lgna.project.ast.AbstractStatementWithBody bodyStatement = this.executingBlockStatementsMap.get( statement );

		if( statementStatusIndicator != null ) {
			javafx.application.Platform.runLater( () -> {
				statementStatusIndicator.setExecutionStatus( ExecutionStatus.NOT_EXECUTING );
			} );
		}

		if( bodyStatement != null ) {
			final PuzzleStatus.StatementStatus statementStatus = this.puzzle.getPuzzleComparison().getStatus().getStatementStatus( bodyStatement );

			javafx.application.Platform.runLater( () -> {
				try {
					this.statementsLock.acquireUninterruptibly();

					// Missing statements.
					if( ( statementStatus != null ) && !( statementStatus.getStatement() instanceof org.lgna.project.ast.DoTogether ) ) {
						for( PuzzleStatus.StatementStatus missing : statementStatus.getMissingStatuses() ) {
							StatementStatusIndicator indicator = new StatementStatusIndicator( missing.getStatement(), missing.getState() );
							PlayPuzzleProjectComposite.this.indicator.addStatementStatus( indicator );
							indicator.setExecutionStatus( ExecutionStatus.NOT_EXECUTING );
						}
					}
				} finally {
					this.statementsLock.release();
				}
			} );
		}
	}

	@Override
	protected PlayPuzzleProjectView createView() {
		return new PlayPuzzleProjectView( this );
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();

		this.indicator.setActive( true );
		this.reset();
	}

	@Override
	public void handlePostDeactivation() {
		super.handlePostDeactivation();

		this.indicator.setActive( false );
		this.reset();
	}
}
