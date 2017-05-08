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

import org.alice.ide.IDE;
import org.alice.stageide.perspectives.CodePerspective;
import org.alice.stageide.perspectives.code.CodePerspectiveComposite;
import org.lgna.croquet.SimpleComposite;
import org.lgna.croquet.views.BorderPanel;
import org.lgna.croquet.views.SplitPane;

import edu.wustl.lookingglass.ide.perspectives.dinah.processbar.DeclarationsEditorWithInstructionsComposite;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.remix.share.ShareRemixComposite;

/**
 * @author Michael Pogran
 */
public class DinahCodeComposite extends SimpleComposite<BorderPanel> implements TimeScrubUpdateable {

	private DeclarationsEditorWithInstructionsComposite editorWithInstructions;
	private AbstractRemixStepsComposite stepsComposite;

	public DinahCodeComposite( AbstractRemixStepsComposite stepsComposite ) {
		super( java.util.UUID.fromString( "b1e1ba70-57d8-4d67-b35e-c90aab78a11e" ) );

		if( stepsComposite instanceof UseRemixStepsComposite ) {
			this.editorWithInstructions = new DeclarationsEditorWithInstructionsComposite( ( (UseRemixStepsComposite)stepsComposite ).getUseRemixOperation() );
		} else {
			this.editorWithInstructions = new DeclarationsEditorWithInstructionsComposite( ShareRemixComposite.getInstance().getLaunchOperation() );
		}

		this.registerSubComposite( this.editorWithInstructions );

		this.stepsComposite = stepsComposite;
		if( this.stepsComposite != null ) {
			this.registerSubComposite( stepsComposite );
			this.stepsComposite.setHelpOperation( this.editorWithInstructions.getHelpOperation() );
		}
	}

	@Override
	public void setProgramManager( DinahProgramManager programManager ) {
		if( getRemixStepsComposite() != null ) {
			getRemixStepsComposite().setProgramManager( programManager );
		}
	}

	@Override
	public void removeProgramManager() {
	}

	public AbstractRemixStepsComposite getRemixStepsComposite() {
		return this.stepsComposite;
	}

	public DeclarationsEditorWithInstructionsComposite getEditorWithInstructions() {
		return this.editorWithInstructions;
	}

	@Override
	protected BorderPanel createView() {
		return new BorderPanel();
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		this.getView().addCenterComponent( getEditorWithInstructions().getView() );
		if( this.stepsComposite != null ) {
			synchronized( this.getView().getTreeLock() ) {
				this.getView().addPageStartComponent( stepsComposite.getView() );
			}
		}
	}

	@Override
	public void handlePostDeactivation() {
		CodePerspective codePerspective = IDE.getActiveInstance().getDocumentFrame().getCodePerspective();
		SplitPane codeSplitPane = ( (CodePerspectiveComposite)codePerspective.getMainComposite() ).getView();

		codeSplitPane.getAwtComponent().setRightComponent( org.alice.ide.IDE.getActiveInstance().getDocumentFrame().getDeclarationsEditorComposite().getView().getAwtComponent() );
		if( this.stepsComposite != null ) {
			synchronized( this.getView().getTreeLock() ) {
				this.getView().removeComponent( stepsComposite.getView() );
			}
		}
		super.handlePostDeactivation();
	}

	@Override
	public void update( ProgramStateEvent programStateEvent ) {
		this.getEditorWithInstructions().update( programStateEvent );
	}

	@Override
	public void update( ProgramExecutionEvent programExecutionEvent, boolean isStartEvent ) {
		//pass
	}
}
