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
package edu.wustl.lookingglass.ide.perspectives.dinah;

import java.awt.Container;
import java.awt.event.ActionEvent;

import org.lgna.common.ComponentThread;
import org.lgna.croquet.views.SwingComponentView;
import org.lgna.story.implementation.ProgramImp.AwtContainerInitializer;

import edu.wustl.lookingglass.ide.program.TimeScrubProgramContext;
import edu.wustl.lookingglass.ide.program.TimeScrubProgramImp;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.ide.program.event.TimeScrubProgramListener;
import edu.wustl.lookingglass.remix.models.EndCaptureState;
import edu.wustl.lookingglass.remix.models.StartCaptureState;
import edu.wustl.lookingglass.scenegraph.recorder.RecorderManager;

/**
 * @author Michael Pogran
 */
public class DinahProgramManager {

	private TimeScrubProgramContext programContext;
	private TimeScrubProgramImp programImp;

	private TimeScrubProgramListener programListener;
	private TimeScrubUpdateable updateable;

	public DinahProgramManager( TimeScrubUpdateable updateable ) {
		this.updateable = updateable;
		this.programListener = new TimeScrubProgramListener() {

			@Override
			public void programStateChange( ProgramStateEvent programStateEvent ) {
				if( programStateEvent.getNextState() != programStateEvent.getPreviousState() ) {
					updateable.update( programStateEvent );
				}
			}

			@Override
			public void startingExecution( ProgramExecutionEvent programExecutionEvent ) {
				updateable.update( programExecutionEvent, true );
			}

			@Override
			public void endingExecution( ProgramExecutionEvent programExecutionEvent ) {
				updateable.update( programExecutionEvent, false );
			}

		};
	}

	/*package-private*/void startDinahProgram( boolean shouldPause, DinahAwtContainerInitializer initializer ) {
		this.startDinahProgram( new TimeScrubRunnable( shouldPause, initializer ) );
	}

	/*package-private*/void startDinahProgram( boolean shouldPause, SwingComponentView<?> componentView ) {
		this.startDinahProgram( new TimeScrubRunnable( shouldPause, componentView.getAwtComponent() ) );
	}

	/*package-private*/void startDinahProgram( boolean shouldPause, Container awtContainer ) {
		this.startDinahProgram( new TimeScrubRunnable( shouldPause, awtContainer ) );
	}

	private void startDinahProgram( TimeScrubRunnable runnable ) {
		new ComponentThread( runnable, "dinahProgram" ).start();
	}

	/*package-private*/void stopDinahProgram() {
		this.programImp.removeTimeScrubProgramListener( this.programListener );
		this.programContext.cleanUpProgram();

		this.programImp = null;
		this.programImp = null;

		StartCaptureState.getInstance().removeAllListeners();
		EndCaptureState.getInstance().removeAllListeners();

		StartCaptureState.getInstance().removeCurrentSelection();
		EndCaptureState.getInstance().removeCurrentSelection();

		RecorderManager.getInstance().deactivate();

		this.updateable.removeProgramManager();
	}

	private void setProgramContext( TimeScrubProgramContext programContext ) {
		this.programContext = programContext;
		this.programImp = (TimeScrubProgramImp)programContext.getProgramImp();

		this.updateable.setProgramManager( this );
		this.programImp.addTimeScrubProgramListener( this.programListener );
	}

	public TimeScrubProgramContext getProgramContext() {
		return this.programContext;
	}

	public TimeScrubProgramImp getProgramImp() {
		return this.programImp;
	}

	private class TimeScrubRunnable implements Runnable {
		private AwtContainerInitializer awtContainerInitializer;
		private boolean shouldPause;
		private javax.swing.Action restartAction;

		public TimeScrubRunnable( boolean shouldPause, DinahAwtContainerInitializer initializer ) {
			this.shouldPause = shouldPause;
			this.awtContainerInitializer = initializer;
			this.restartAction = new javax.swing.AbstractAction() {
				@Override
				public void actionPerformed( ActionEvent e ) {
					stopDinahProgram();
					startDinahProgram( false, initializer );
				}
			};
		}

		public TimeScrubRunnable( boolean shouldPause, Container awtContainer ) {
			this.shouldPause = shouldPause;
			this.awtContainerInitializer = new org.lgna.story.implementation.ProgramImp.DefaultAwtContainerInitializer( awtContainer );
			this.restartAction = new javax.swing.AbstractAction() {
				@Override
				public void actionPerformed( ActionEvent e ) {
					stopDinahProgram();
					startDinahProgram( false, awtContainer );
				}
			};
		}

		@Override
		public void run() {
			RecorderManager.getInstance().initialize();

			TimeScrubProgramContext programContext = new TimeScrubProgramContext( this.shouldPause );
			TimeScrubProgramImp programImp = (TimeScrubProgramImp)programContext.getProgramImp();

			programImp.setRestartAction( this.restartAction );
			RecorderManager.getInstance().setProgram( programImp );

			javax.swing.SwingUtilities.invokeLater( ( ) -> {
				programContext.initializeInContainer( this.awtContainerInitializer );
				setProgramContext( programContext );

				programContext.setActiveSceneOnComponentThreadAndWait();

				Container programContainer = this.awtContainerInitializer.getAwtContainer();
				programContainer.revalidate();
				programContainer.repaint();
			} );
		}
	}
}
