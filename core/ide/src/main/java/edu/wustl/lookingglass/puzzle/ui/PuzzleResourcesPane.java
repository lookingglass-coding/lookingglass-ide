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
package edu.wustl.lookingglass.puzzle.ui;

import org.lgna.croquet.icon.IconSize;

import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.ide.LookingGlassTheme;
import edu.wustl.lookingglass.puzzle.CompletionPuzzle;
import edu.wustl.lookingglass.puzzle.ui.croquet.CompletionPuzzleComposite;
import javafx.fxml.FXML;

/**
 * @author Kyle J. Harms
 */
public class PuzzleResourcesPane extends FxComponent {

	private final CompletionPuzzle puzzle;
	private final CompletionPuzzleComposite puzzleComposite;

	@FXML private javafx.scene.layout.VBox resourcesPane;
	@FXML private javafx.scene.image.ImageView scene;
	@FXML private javafx.scene.control.Button playCorrect;
	@FXML private javafx.scene.control.Button playMine;

	public PuzzleResourcesPane( CompletionPuzzle puzzle, CompletionPuzzleComposite puzzleComposite, javafx.scene.image.Image scenePreview ) {
		super( PuzzleResourcesPane.class );
		this.puzzle = puzzle;
		this.puzzleComposite = puzzleComposite;

		this.scene.fitWidthProperty().bind( resourcesPane.widthProperty() );
		this.scene.setImage( scenePreview );

		this.playCorrect.setGraphic( LookingGlassTheme.getFxImageView( "puzzle-play", IconSize.SMALL ) );
		this.playCorrect.setTooltip( new javafx.scene.control.Tooltip( this.getResources().getString( "PuzzleResourcesPane.playCorrect.tooltip" ) ) );

		this.playMine.setGraphic( LookingGlassTheme.getFxImageView( "puzzle-play", IconSize.SMALL ) );
		this.playMine.setTooltip( new javafx.scene.control.Tooltip( this.getResources().getString( "PuzzleResourcesPane.playMine.tooltip" ) ) );

		this.register( this.playCorrect, this::handlePlayOriginalAction );
		this.register( this.playMine, this::handlePlayPuzzleAction );
	}

	private void handlePlayOriginalAction( javafx.event.ActionEvent event ) {
		this.puzzleComposite.showCheckPane( CompletionPuzzle.PuzzleProjectState.REFERENCE );
	}

	private void handlePlayPuzzleAction( javafx.event.ActionEvent event ) {
		this.puzzleComposite.showCheckPane( CompletionPuzzle.PuzzleProjectState.PUZZLE );
	}
}
