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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import org.alice.ide.highlight.IdeHighlightStencil;
import org.lgna.croquet.ActionOperation;
import org.lgna.croquet.CancelException;
import org.lgna.croquet.SimpleComposite;
import org.lgna.croquet.edits.Edit;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.event.ValueListener;
import org.lgna.croquet.history.CompletionStep;

import edu.wustl.lookingglass.ide.perspectives.dinah.finder.ReplayRemixStencilComponentListener;
import edu.wustl.lookingglass.ide.perspectives.dinah.processbar.AbstractStepOverviewPanel;
import edu.wustl.lookingglass.ide.program.TimeScrubProgramImp;
import edu.wustl.lookingglass.remix.RemixSnippetFactory;
import edu.wustl.lookingglass.remix.SnippetScript;
import edu.wustl.lookingglass.remix.models.EndCaptureState;
import edu.wustl.lookingglass.remix.models.StartCaptureState;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.observer.VMExecutionObserver;

public abstract class AbstractRemixStepsComposite extends SimpleComposite<AbstractStepOverviewPanel> {

	private DinahProgramManager programManager;
	private ActionOperation helpOperation;

	private final ActionOperation previewOperation;
	private final ActionOperation cancelOperation;
	private final ComponentListener stencilListener;

	private final ValueListener<AbstractEventNode<?>> captureStateListener;

	protected abstract void performCancel();

	public AbstractRemixStepsComposite( java.util.UUID uuid ) {
		super( uuid );
		this.previewOperation = this.createActionOperation( "previewOperation", new Action() {

			@Override
			public Edit perform( CompletionStep<?> step, org.lgna.croquet.AbstractComposite.InternalActionOperation source ) throws CancelException {
				IdeHighlightStencil highlightStencil = new IdeHighlightStencil( getFrame(), javax.swing.JLayeredPane.POPUP_LAYER - 2 );

				ReplayRemixStencilComponentListener stencilShowingListener = new ReplayRemixStencilComponentListener( highlightStencil, programManager );

				highlightStencil.addComponentListener( stencilShowingListener );
				highlightStencil.showHighlightOverRenderWindow();

				step.finish();
				return null;
			}

		} );

		this.cancelOperation = this.createActionOperation( "cancelOperation", new Action() {
			@Override
			public Edit perform( CompletionStep<?> step, InternalActionOperation source ) throws CancelException {
				performCancel();
				step.finish();
				return null;
			}
		} );

		this.stencilListener = new ComponentListener() {

			@Override
			public void componentResized( ComponentEvent e ) {
				//pass
			}

			@Override
			public void componentMoved( ComponentEvent e ) {
				//pass
			}

			@Override
			public void componentShown( ComponentEvent e ) {
				//pass
			}

			@Override
			public void componentHidden( ComponentEvent e ) {
				getProgramManager().getProgramImp().resumeProgram();
			}
		};

		this.captureStateListener = new ValueListener<AbstractEventNode<?>>() {

			@Override
			public void valueChanged( ValueEvent<AbstractEventNode<?>> e ) {
				boolean startSelected = StartCaptureState.getInstance().getValue() != null;
				boolean endSelected = EndCaptureState.getInstance().getValue() != null;

				getView().updateCaptureStates( startSelected, endSelected );
				getPreviewOperation().setEnabled( startSelected && endSelected );
			}
		};
	}

	public void setProgramManager( DinahProgramManager programManager ) {
		this.programManager = programManager;
	}

	public DinahProgramManager getProgramManager() {
		return this.programManager;
	}

	public void setHelpOperation( ActionOperation helpOperation ) {
		this.helpOperation = helpOperation;
	}

	public ActionOperation getHelpOperation() {
		return this.helpOperation;
	}

	public ComponentListener getStencilListener() {
		return this.stencilListener;
	}

	public ActionOperation getPreviewOperation() {
		return this.previewOperation;
	}

	public ActionOperation getCancelOperation() {
		return this.cancelOperation;
	}

	public String getStencilMessage() {
		return "Remixing allows you to capture actions in this world and then use them in your world.\n\nClick on the help buttons for more details.";
	}

	public SnippetScript getRemixScript() {
		SnippetScript rv = null;
		if( ( StartCaptureState.getInstance().getValue() != null ) && ( EndCaptureState.getInstance().getValue() != null ) ) {
			TimeScrubProgramImp program = getProgramManager().getProgramImp();
			AbstractEventNode<?> startEventNode = StartCaptureState.getInstance().getValue();
			AbstractEventNode<?> endEventNode = EndCaptureState.getInstance().getValue();
			VMExecutionObserver executionObserver = program.getExecutionObserver();

			RemixSnippetFactory factory = RemixSnippetFactory.getRemixSnippetFactory( startEventNode, endEventNode, executionObserver );

			// Disable execution recording while building snippet script.
			executionObserver.setIsRecording( false );
			rv = factory.buildSnippetScript();
			executionObserver.setIsRecording( true );
		}
		return rv;
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		StartCaptureState.getInstance().addListener( this.captureStateListener );
		EndCaptureState.getInstance().addListener( this.captureStateListener );
	}

	@Override
	public void handlePostDeactivation() {
		StartCaptureState.getInstance().removeListener( this.captureStateListener );
		EndCaptureState.getInstance().removeListener( this.captureStateListener );
		super.handlePostDeactivation();
	}
}
