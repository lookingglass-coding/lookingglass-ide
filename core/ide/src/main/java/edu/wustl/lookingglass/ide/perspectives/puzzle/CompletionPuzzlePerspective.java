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
package edu.wustl.lookingglass.ide.perspectives.puzzle;

import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.puzzle.CompletionPuzzle;
import edu.wustl.lookingglass.puzzle.ui.croquet.CompletionPuzzleComposite;

/**
 * @author Kyle J. Harms
 */
public class CompletionPuzzlePerspective extends org.alice.ide.perspectives.ProjectPerspective {

	private CompletionPuzzle puzzle;
	private CompletionPuzzleComposite puzzleComposite;

	public CompletionPuzzlePerspective( CompletionPuzzle puzzle ) {
		// Ugh... this should not be globally grabbed. But that's just the design of Alice/Looking Glass.
		// TODO: set the menu bar just for the puzzle perspective.
		super( java.util.UUID.fromString( "74259037-8e4a-457f-a88c-012c92bedf2e" ), edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().getDocumentFrame(), null );
		this.puzzle = puzzle;
		this.puzzleComposite = new CompletionPuzzleComposite( this.puzzle );
	}

	public CompletionPuzzle getPuzzle() {
		return this.puzzle;
	}

	@Override
	public void handleActivation() {
		LookingGlassIDE.getActiveInstance().showLoadingLayer( false );
	}

	@Override
	public void handleDeactivation() {
		this.puzzleComposite.restoreUndoHistory();

		// Always, always, always feed the garbage collector...
		// I don't trust croquet to actually free this perspective.
		this.puzzle = null;
		this.puzzleComposite = null;
	}

	@Override
	public org.lgna.croquet.ToolBarComposite getToolBarComposite() {
		return null;
	}

	@Override
	public CompletionPuzzleComposite getMainComposite() {
		return this.puzzleComposite;
	}

	@Override
	public org.lgna.croquet.views.TrackableShape getRenderWindow() {
		return null;
	}

	@Override
	public org.alice.ide.codedrop.CodePanelWithDropReceptor getCodeDropReceptorInFocus() {
		return this.puzzleComposite.getPuzzleEditor();
	}

	@Override
	protected void addPotentialDropReceptors( java.util.List<org.lgna.croquet.DropReceptor> out, org.alice.ide.croquet.models.IdeDragModel dragModel ) {
		out.add( this.puzzleComposite.getPuzzleEditor().getDropReceptor() );
		out.add( this.puzzleComposite.getResourcesBinComposite().getDropReceptor() );
	}
}
