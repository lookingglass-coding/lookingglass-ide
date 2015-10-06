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
package edu.wustl.lookingglass.ide.croquet.models.preview;

import java.util.concurrent.Semaphore;

import org.lgna.croquet.views.BorderPanel;
import org.lgna.project.Project;

import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.croquet.models.community.AbstractIsProgramExecutingState;
import edu.wustl.lookingglass.ide.program.RunProgramAsPreviewContext;
import edu.wustl.lookingglass.virtualmachine.event.VirtualMachineExecutionStateListener;

public class IsPreviewProgramExecutingState extends AbstractIsProgramExecutingState implements VirtualMachineExecutionStateListener {

	private Project currentProject;
	private final Semaphore lock = new Semaphore( 1 );
	private final PreviewProjectComposite previewProjectComposite;

	public IsPreviewProgramExecutingState( PreviewProjectComposite previewProjectComposite ) {
		super( java.util.UUID.fromString( "f6b9e210-46ce-11e1-b86c-0800200c9a66" ) );
		this.previewProjectComposite = previewProjectComposite;
		this.executingProgramContainer = new BorderPanel();
	}

	@Override
	public void programRun() {
		javax.swing.SwingUtilities.invokeLater( () -> {
			this.lock.acquireUninterruptibly();
			if( programContext != null ) {
				stopProgramRun();
			}
			startProgramRun();
			this.lock.release();
		} );
	}

	@Override
	public void programStop() {
		javax.swing.SwingUtilities.invokeLater( () -> {
			this.lock.acquireUninterruptibly();
			if( programContext != null ) {
				stopProgramRun();
			}
			this.lock.release();
		} );
	}

	private void startProgramRun() {
		assert ThreadHelper.isSwingThread();

		if( currentProject != null ) {
			if( exceptionHandler == null ) {
				exceptionHandler = new org.lgna.common.ProgramExecutionExceptionHandler() {
					@Override
					public void handleProgramExecutionExeception( Throwable t ) {
						// Eat exceptions
						edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( t );
					}
				};
			}

			try {
				this.previewProjectComposite.getPreviewWorldPanel().shouldPaintBackground( true );
				programContext = new RunProgramAsPreviewContext( currentProject );
				programContext.setProgramExceptionHandler( exceptionHandler );
				programContext.initializeInContainer( executingProgramContainer.getAwtComponent() );

				ThreadHelper.runInBackground( ( ) -> {
					programContext.setActiveSceneOnComponentThreadAndWait();
					if( programContext instanceof RunProgramAsPreviewContext ) {
						( (RunProgramAsPreviewContext)programContext ).addVirtualMachineExecutionStateListener( IsPreviewProgramExecutingState.this );
					}
					currentExecutingProgram = programContext.getProgramImp();

					this.previewProjectComposite.getPreviewWorldPanel().shouldPaintBackground( false );
					executingProgramContainer.revalidateAndRepaint();
				} );
			} catch( Throwable t ) {
				edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( t, this );
				this.previewProjectComposite.loadError();
			}
		}
	}

	private void stopProgramRun() {
		programContext.cleanUpProgram();
		programContext = null;
		currentExecutingProgram = null;
	}

	public void setSelectedProject( Project project ) {
		this.setValueTransactionlessly( false ); // stop the current execution.

		// See if we can actually play this project, this might be the open source version...
		if( project.doAllTypeClassesExist() ) {
			this.currentProject = project;
		} else {
			this.currentProject = null;
		}
	}

	public void clearSelectedProject() {
		this.setSelectedProject( null );
	}

	public Project getCurrentProject() {
		return this.currentProject;
	}

	// there are no threads currently running which probably means it's safe to stop
	// this assumption is going to have to die when events come in.
	@Override
	public void isChangedToPaused() {
		this.setValueTransactionlessly( false );
	}

	@Override
	public void isChangedToRunning() {
	}

	@Override
	public void isEndingExecution() {
	}
}
