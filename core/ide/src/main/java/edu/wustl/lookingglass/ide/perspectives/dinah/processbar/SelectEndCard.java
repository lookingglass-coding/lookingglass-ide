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
package edu.wustl.lookingglass.ide.perspectives.dinah.processbar;

import org.alice.ide.highlight.IdeHighlightStencil;
import org.lgna.croquet.CancelException;
import org.lgna.croquet.Operation;
import org.lgna.croquet.edits.Edit;
import org.lgna.croquet.history.CompletionStep;

import edu.wustl.lookingglass.ide.perspectives.dinah.DinahProgramManager;
import edu.wustl.lookingglass.ide.perspectives.dinah.finder.ReplayRemixStencilComponentListener;
import edu.wustl.lookingglass.ide.perspectives.dinah.processbar.views.SelectEndView;
import edu.wustl.lookingglass.remix.models.EndCaptureState;
import edu.wustl.lookingglass.remix.models.StartCaptureState;

/**
 *
 * @author Michael Pogran
 */
public class SelectEndCard extends GuideStepComposite {
	private final Operation previewOperation;
	private DinahProgramManager programManager;

	public SelectEndCard( StepInstructionsCardComposite parentComposite ) {
		super( java.util.UUID.fromString( "fc194d2c-8af3-4af3-9a42-9305248bb68b" ), parentComposite );

		this.previewOperation = this.createActionOperation( "previewOperation", new Action() {

			@Override
			public Edit perform( CompletionStep<?> step, org.lgna.croquet.AbstractComposite.InternalActionOperation source ) throws CancelException {
				DinahProgramManager manager = getProgramManager();

				if( manager != null ) {
					IdeHighlightStencil highlightStencil = new IdeHighlightStencil( getFrame(), javax.swing.JLayeredPane.POPUP_LAYER - 2 );
					highlightStencil.addComponentListener( new ReplayRemixStencilComponentListener( highlightStencil, manager ) );
					highlightStencil.showHighlightOverRenderWindow();
				}
				step.finish();
				return null;
			}

		} );
	}

	public void setProgramManager( DinahProgramManager programManager ) {
		this.programManager = programManager;
	}

	public DinahProgramManager getProgramManager() {
		return this.programManager;
	}

	public Operation getPreviewOperation() {
		return this.previewOperation;
	}

	@Override
	protected GuideStepPanel createView() {
		return new SelectEndView( this );
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();

		this.previewOperation.setEnabled( EndCaptureState.getInstance().getValue() != null );
		this.startCaptureStateChange( StartCaptureState.getInstance().getValue() );
		this.endCaptureStateChange( EndCaptureState.getInstance().getValue() );
	}
}
