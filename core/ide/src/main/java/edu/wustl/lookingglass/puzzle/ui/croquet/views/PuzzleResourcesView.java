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
package edu.wustl.lookingglass.puzzle.ui.croquet.views;

import edu.wustl.lookingglass.croquetfx.FxViewAdaptor;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.puzzle.CompletionPuzzle;
import edu.wustl.lookingglass.puzzle.ui.PuzzleResourcesPane;
import edu.wustl.lookingglass.puzzle.ui.croquet.PuzzleResourcesComposite;

/**
 * @author Kyle J. Harms
 */
public class PuzzleResourcesView extends org.lgna.croquet.views.MigPanel {

	private final PuzzleResourcesComposite composite;
	private final CompletionPuzzle puzzle;

	private PuzzleResourcesPane resourcesPane;

	public PuzzleResourcesView( PuzzleResourcesComposite composite, CompletionPuzzle puzzle ) {
		super( composite, "insets 0, gap 0 0, nocache", "[fill, grow, 200:400:]", "[pref:pref:pref][fill, grow]" );
		this.composite = composite;
		this.puzzle = puzzle;

		this.addComponent( this.composite.getBinComposite().getRootComponent(), "cell 0 1" );

		// Improve the appearance of resize redrawing
		this.setBackgroundColor( this.composite.getBinComposite().getRootComponent().getBackgroundColor() );

		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			this.resourcesPane = new PuzzleResourcesPane( this.composite.getPuzzle(), this.composite.getScenePreview() );
			edu.wustl.lookingglass.croquetfx.FxViewAdaptor fxViewAdaptor = this.resourcesPane.getFxViewAdaptor();

			javax.swing.SwingUtilities.invokeLater( () -> {
				synchronized( PuzzleResourcesView.this.getTreeLock() ) {
					this.addComponent( fxViewAdaptor, "cell 0 0" );
				}
				ThreadHelper.runOnFxThread( () -> {
					FxViewAdaptor viewAdaptor = this.resourcesPane.getFxViewAdaptor();
					viewAdaptor.recomputePreferredSize();
					ThreadHelper.runOnSwingThread( () -> {
						this.revalidateAndRepaint();

						// Note: This is really bad... at this current moment, based
						// on the way croquet initializes components... this is the very last
						// interface component to get initialized when loading the puzzles interface.
						// Note: This may change in the future if things get moved around.
						this.puzzle.getPuzzleComposite().initializeInterface();
					} );
				} );
			} );
		} );
	}
}
