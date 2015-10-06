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

import java.util.UUID;

import org.alice.ide.IDE;
import org.alice.ide.ProjectDocumentFrame;
import org.alice.stageide.perspectives.AbstractCodePerspective;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;

import edu.wustl.lookingglass.ide.LookingGlassTheme;

/**
 * @author Caitlin Kelleher
 */
public abstract class AbstractDinahPerspective extends AbstractCodePerspective {

	protected DinahMainComposite dinahMainComposite;
	private DinahProgramManager programManager;
	private final org.lgna.croquet.Operation returnOperation = new ReturnOperation();

	protected abstract boolean shouldPause();

	protected abstract DinahAwtContainerInitializer getInitializer();

	public AbstractDinahPerspective( UUID id, ProjectDocumentFrame projectDocumentFrame, AbstractRemixStepsComposite stepsComposite ) {
		super( id, projectDocumentFrame, null );
		DinahCodeComposite dinahCodeComposite = new DinahCodeComposite( stepsComposite );
		ExecutionTraceComposite executionTraceComposite = new ExecutionTraceComposite();
		this.dinahMainComposite = new DinahMainComposite( executionTraceComposite, dinahCodeComposite );
	}

	@Override
	public void handleActivation() {
		startDinahProgram();
	}

	@Override
	public void handleDeactivation() {
		stopDinahProgram();
	}

	private void startDinahProgram() {
		this.programManager = new DinahProgramManager( getMainComposite() );

		DinahAwtContainerInitializer initializer = getInitializer();

		if( initializer != null ) {
			this.programManager.startDinahProgram( shouldPause(), initializer );
		} else {
			this.programManager.startDinahProgram( shouldPause(), getExecutionTraceComposite().getProgramContainer() );
		}
	}

	private void stopDinahProgram() {
		this.programManager.stopDinahProgram();
		this.programManager = null;
	}

	@Override
	public org.lgna.croquet.views.TrackableShape getRenderWindow() {
		return getExecutionTraceComposite().getProgramContainer();
	}

	@Override
	public DinahMainComposite getMainComposite() {
		return this.dinahMainComposite;
	}

	public DinahCodeComposite getDinahCodeComposite() {
		return this.dinahMainComposite.getCodeComposite();
	}

	public ExecutionTraceComposite getExecutionTraceComposite() {
		return this.dinahMainComposite.getExecutionTraceComposite();
	}

	public AbstractRemixStepsComposite getRemixStepsComposite() {
		return this.getDinahCodeComposite().getRemixStepsComposite();
	}

	public org.lgna.croquet.Operation getReturnOperation() {
		return this.returnOperation;
	}

	public DinahProgramManager getProgramManager() {
		return this.programManager;
	}

	private class ReturnOperation extends org.lgna.croquet.Operation {

		public ReturnOperation() {
			super( org.lgna.croquet.Application.DOCUMENT_UI_GROUP, java.util.UUID.fromString( "2ae7bd45-a733-455c-8db8-45de20b7bb71" ) );
			setButtonIcon( LookingGlassTheme.getIcon( "back-to-editor", org.lgna.croquet.icon.IconSize.SMALL ) );
		}

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			IDE.getActiveInstance().getDocumentFrame().setToCodePerspectiveTransactionlessly();
		}
	}
}
