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
package edu.wustl.lookingglass.ide.program.models;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.wustl.lookingglass.ide.program.TimeScrubProgramImp;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.ide.program.event.TimeScrubProgramListener;

public class FastForwardOperation extends org.lgna.croquet.Operation {

	private final double INITIAL_FASTFORWARD_SPEED = 2.0;
	private final double SPEED_INCREMENT = 1.0;
	private final double MAX_SPEED = 5.0;
	private final long FASTFOWARD_INCREMENT_WAIT_MS = 500;

	private final TimeScrubProgramImp program;

	private static final ExecutorService pool = Executors.newCachedThreadPool( new java.util.concurrent.ThreadFactory() {
		private Integer count = 0;

		@Override
		public Thread newThread( Runnable r ) {
			synchronized( count ) {
				return new Thread( r, "Fast Forward-" + count++ );
			}
		}
	} );

	public FastForwardOperation( TimeScrubProgramImp program ) {
		super( edu.wustl.lookingglass.remix.models.ReuseGroup.REUSE_GROUP, java.util.UUID.fromString( "8cca7af4-7148-4a51-8e63-302fe439b9a1" ) );
		this.setButtonIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "world-seek-forward", org.lgna.croquet.icon.IconSize.SMALL ) );
		this.program = program;

		this.program.addTimeScrubProgramListener( new TimeScrubProgramListener() {

			@Override
			public void programStateChange( ProgramStateEvent programStateEvent ) {
				if( programStateEvent.isFinishedExecuting() && ( programStateEvent.getTime() == FastForwardOperation.this.program.getMaxProgramTime() ) ) {
					FastForwardOperation.this.setEnabled( false );
				} else {
					FastForwardOperation.this.setEnabled( true );
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
	public org.lgna.croquet.views.Button createButton( edu.cmu.cs.dennisc.java.awt.font.TextAttribute<?>... textAttributes ) {
		org.lgna.croquet.views.Button rv = new org.lgna.croquet.views.Button( this );
		rv.getAwtComponent().addMouseListener( new FastFowardOnHoldPress( this.program ) );
		return rv;
	}

	class FastFowardOnHoldPress extends java.awt.event.MouseAdapter {
		private final TimeScrubProgramImp program;
		boolean isPressed = false;

		public FastFowardOnHoldPress( TimeScrubProgramImp program ) {
			this.program = program;
		}

		private TimeScrubProgramImp getProgram() {
			return this.program;
		}

		@Override
		public void mousePressed( java.awt.event.MouseEvent e ) {
			isPressed = true;
			if( getProgram().getProgramState().isPaused() ) {
				getProgram().resumeProgram();
			}
			FastForwardOperation.pool.execute( createFastForwardTask() );
		}

		@Override
		public void mouseReleased( java.awt.event.MouseEvent e ) {
			isPressed = false;
			getProgram().resetRunningProgramSpeed();
		}

		private Runnable createFastForwardTask() {
			return new Runnable() {
				@Override
				public void run() {
					double currentSpeed = INITIAL_FASTFORWARD_SPEED;

					while( isPressed && ( currentSpeed <= MAX_SPEED ) ) {
						getProgram().setRunningProgramSpeed( currentSpeed );

						synchronized( this ) {
							try {
								wait( FASTFOWARD_INCREMENT_WAIT_MS );
							} catch( InterruptedException ie ) {
								ie.printStackTrace();
							}
						}

						currentSpeed += SPEED_INCREMENT;
					}
				}
			};
		}
	}

	@Override
	protected void perform( org.lgna.croquet.history.Transaction transaction, org.lgna.croquet.triggers.Trigger trigger ) {
		// pass
	}
}
