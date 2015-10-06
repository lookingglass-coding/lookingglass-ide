/*******************************************************************************
 * Copyright (c) 2006, 2015, Carnegie Mellon University. All rights reserved.
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
 * 3. Products derived from the software may not be called "Alice", nor may
 *    "Alice" appear in their name, without prior written permission of
 *    Carnegie Mellon University.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Carnegie Mellon University"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 * ANY AND ALL EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT
 * SHALL THE AUTHORS, COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.alice.stageide.run;

import java.awt.Container;
import java.awt.event.ActionEvent;

import org.lgna.croquet.views.AwtComponentView;
import org.lgna.croquet.views.BorderPanel;
import org.lgna.croquet.views.Frame;

import edu.wustl.lookingglass.croquetfx.ThreadHelper;

/**
 * @author Dennis Cosgrove
 */
public class RunComposite extends org.lgna.croquet.SimpleModalFrameComposite<org.alice.stageide.run.views.RunView> {
	private static class SingletonHolder {
		private static RunComposite instance = new RunComposite();
	}

	public static RunComposite getInstance() {
		return SingletonHolder.instance;
	}

	private final org.lgna.croquet.PlainStringValue restartLabel = this.createStringValue( "restart" );

	private RunComposite() {
		super( java.util.UUID.fromString( "985b3795-e1c7-4114-9819-fae4dcfe5676" ), org.alice.ide.IDE.RUN_GROUP );
		//todo: move to localize
		//		this.getLaunchOperation().setButtonIcon( new org.alice.stageide.run.views.icons.RunIcon() );

		// Be able to keep the run window up and edit at the same time. This happens so much
		// in user testing it really makes sense to have this feature.
		// This kinda messes up the transaction history. But restart, play, pause, fast forward, all
		// skip the transaction history anyway... so why does it matter?
		this.setModal( false );
		this.setReuseFrame( true );
	}

	private transient org.alice.stageide.program.RunProgramContext programContext;
	public static final double WIDTH_TO_HEIGHT_RATIO = 16.0 / 9.0;
	private static final int DEFAULT_WIDTH = 640;
	private static final int DEFAULT_HEIGHT = (int)( DEFAULT_WIDTH / WIDTH_TO_HEIGHT_RATIO );
	private java.awt.Point location = null;
	private java.awt.Dimension size = null;

	@Override
	protected GoldenRatioPolicy getGoldenRatioPolicy() {
		return null;
	}

	@Override
	protected java.awt.Point getDesiredWindowLocation() {
		return this.location;
	}

	@Override
	protected java.awt.Dimension calculateWindowSize( org.lgna.croquet.views.AbstractWindow<?> window ) {
		if( this.size != null ) {
			return this.size;
		} else {
			return super.calculateWindowSize( window );
		}
	}

	private class ProgramRunnable implements Runnable {
		public ProgramRunnable( org.lgna.story.implementation.ProgramImp.AwtContainerInitializer awtContainerInitializer ) {
			RunComposite.this.programContext = new org.alice.stageide.program.RunProgramContext();
			RunComposite.this.programContext.getProgramImp().setRestartAction( RunComposite.this.restartAction );
			RunComposite.this.programContext.initializeInContainer( awtContainerInitializer );
		}

		@Override
		public void run() {
			RunComposite.this.programContext.setActiveScene();
		}
	}

	private final class RunAwtContainerInitializer implements org.lgna.story.implementation.ProgramImp.AwtContainerInitializer {
		@Override
		public void addComponents( edu.cmu.cs.dennisc.render.OnscreenRenderTarget<?> onscreenRenderTarget ) {
			AwtComponentView.checkEventDispatchThread( this );

			BorderPanel runView = RunComposite.this.getView().getRunPanel();
			runView.forgetAndRemoveAllComponents();

			org.lgna.croquet.views.AwtComponentView<?> lookingGlassContainer = new org.lgna.croquet.views.AwtAdapter( onscreenRenderTarget.getAwtComponent() );
			org.lgna.croquet.views.FixedAspectRatioPanel fixedAspectRatioPanel = new org.lgna.croquet.views.FixedAspectRatioPanel( lookingGlassContainer, WIDTH_TO_HEIGHT_RATIO );
			fixedAspectRatioPanel.setBackgroundColor( java.awt.Color.BLACK );
			runView.addCenterComponent( fixedAspectRatioPanel );
			runView.revalidateAndRepaint();
		}

		@Override
		public Container getAwtContainer() {
			return RunComposite.this.getView().getRunPanel().getAwtComponent();
		}
	}

	private final RunAwtContainerInitializer runAwtContainerInitializer = new RunAwtContainerInitializer();

	private void startProgram() {
		// <lg> There is a race condition with this code. The setActiveScene must complete executing before anything else happens...
		// Have you ever run in LG and wondered why it's just black? It's this issue. So now we just lock and don't move on
		// until the setActiveScene has run. This really isn't the way to fix this... but... it's the path of least resistance...
		org.lgna.common.ComponentThread startThread = new org.lgna.common.ComponentThread( new ProgramRunnable( runAwtContainerInitializer ), RunComposite.this.getLaunchOperation().getImp().getName() );
		startThread.start();
		try {
			startThread.join();
		} catch( InterruptedException e ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( e, this );
		}
		// </lg>
		if( this.fastForwardToStatementOperation != null ) {
			this.fastForwardToStatementOperation.pre( this.programContext );
		}
	}

	private void stopProgram() {
		if( this.programContext != null ) {
			this.programContext.cleanUpProgram();
			this.programContext = null;
		} else {
			//			edu.cmu.cs.dennisc.java.util.logging.Logger.warning( this );
		}
	}

	private class RestartAction extends javax.swing.AbstractAction {
		@Override
		public void actionPerformed( java.awt.event.ActionEvent e ) {
			RunComposite.this.stopProgram();
			RunComposite.this.startProgram();
		}
	};

	private final RestartAction restartAction = new RestartAction();

	@Override
	protected void localize() {
		super.localize();
		this.restartAction.putValue( javax.swing.Action.NAME, this.restartLabel.getText() );
	}

	@Override
	protected void handlePreShowWindow( org.lgna.croquet.views.Frame frame ) {
		// <lg/> study logger
		edu.wustl.lookingglass.study.StudyConfiguration.INSTANCE.getStudyLogger().log( java.util.logging.Level.INFO, "started playing program" );
		super.handlePreShowWindow( frame );
		if( frame.isAlwaysOnTopSupported() ) {
			frame.setAlwaysOnTop( true );
		}

		this.startProgram();

		if( this.getCurrentFrame() == null ) {
			if( this.size != null ) {
				frame.setSize( this.size );
			} else {
				this.programContext.getOnscreenRenderTarget().getAwtComponent().setPreferredSize( new java.awt.Dimension( DEFAULT_WIDTH, DEFAULT_HEIGHT ) );
				frame.pack();
			}
			if( this.location != null ) {
				frame.setLocation( this.location );
			} else {
				org.lgna.croquet.views.Frame documentFrame = org.alice.ide.IDE.getActiveInstance().getDocumentFrame().getFrame();
				if( documentFrame != null ) {
					frame.setLocationRelativeTo( documentFrame );
				} else {
					frame.setLocationByPlatform( true );
				}
			}
		} else {
			frame.restore();
		}
	}

	@Override
	protected void handlePreHideWindow( Frame frame ) {
		this.stopProgram();

		super.handlePreHideWindow( frame );
	}

	@Override
	protected void handlePostHideWindow( org.lgna.croquet.views.Frame frame ) {
		if( ( this.programContext != null ) && ( this.programContext.getProgramImp() != null ) ) {
			java.awt.Rectangle bounds = this.programContext.getProgramImp().getNormalDialogBounds( frame.getAwtComponent() );
			this.location = bounds.getLocation();
			this.size = bounds.getSize();
		}
		super.handlePostHideWindow( frame );
		edu.wustl.lookingglass.study.StudyConfiguration.INSTANCE.getStudyLogger().log( java.util.logging.Level.INFO, "stopped playing program" );
	}

	@Override
	protected void handleFinally() {
		if( this.fastForwardToStatementOperation != null ) {
			this.fastForwardToStatementOperation.post();
			this.fastForwardToStatementOperation = null;
		}
		this.stopProgram();
		super.handleFinally();
	}

	@Override
	protected org.alice.stageide.run.views.RunView createView() {
		return new org.alice.stageide.run.views.RunView( this );
	}

	public void setFastForwardToStatementOperation( FastForwardToStatementOperation fastForwardToStatementOperation ) {
		this.fastForwardToStatementOperation = fastForwardToStatementOperation;
	}

	public void restart() {
		ThreadHelper.runOnSwingThread( () -> {
			if( this.programContext != null ) {
				this.programContext.getProgramImp().getRestartAction().actionPerformed( null );
			}
		} );
	}

	public void toggleFastForward( boolean shouldFastForward ) {
		final double speed;
		if( shouldFastForward ) {
			speed = 2.0;
		} else {
			speed = 1.0;
		}

		ThreadHelper.runOnSwingThread( () -> {
			if( this.programContext != null ) {
				this.programContext.getProgramImp().setRunningProgramSpeed( speed );
			}
		} );
	}

	// TODO: one day this should be real full screen again... instead of this fake stuff...
	public void toggleFullScreen( boolean isFullScreen ) {
		ThreadHelper.runOnSwingThread( () -> {
			ActionEvent event = new ActionEvent( this.getView().getRunPanel().getAwtComponent(), ( isFullScreen ? 1 : 0 ), "" );
			if( this.programContext != null ) {
				this.programContext.getProgramImp().getToggleFullScreenAction().actionPerformed( event );
			}
		} );
	}

	private FastForwardToStatementOperation fastForwardToStatementOperation;

}
