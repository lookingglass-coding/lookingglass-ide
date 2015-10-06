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
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.LookingGlassTheme;
import edu.wustl.lookingglass.puzzle.CompletionPuzzle;
import edu.wustl.lookingglass.puzzle.ui.croquet.CompletionPuzzleComposite;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

/**
 * @author Kyle J. Harms
 */
public class PuzzleIntroPane extends FxComponent {

	@FXML private javafx.scene.image.ImageView face;
	@FXML private javafx.embed.swing.SwingNode scene;
	@FXML private javafx.scene.control.Button next;
	@FXML private HBox bubbleBox;
	private SpeechBubble bubble;

	private final CompletionPuzzle puzzle;
	private final CompletionPuzzleComposite puzzleComposite;

	private edu.wustl.lookingglass.ide.croquet.models.preview.PreviewProjectComposite previewComposite;

	public PuzzleIntroPane( CompletionPuzzle puzzle, CompletionPuzzleComposite puzzleComposite, java.awt.Image sceneImage ) {
		super( PuzzleIntroPane.class );
		this.puzzle = puzzle;
		this.puzzleComposite = puzzleComposite;

		this.puzzle.setPuzzleProjectState( CompletionPuzzle.PuzzleProjectState.REFERENCE );

		this.face.setImage( LookingGlassTheme.getFxImage( "face-confused-100", IconSize.FIXED ) );
		this.next.setGraphic( LookingGlassTheme.getFxImageView( "puzzle-next", IconSize.SMALL ) );

		this.bubble = new SpeechBubble( this.getLocalizedString( "PuzzleIntroPane.bubble" ) );
		this.bubbleBox.getChildren().add( this.bubble.getRootNode() );

		this.register( this.next, this::handleNextAction );

		// Initialize the preview component
		ThreadHelper.runOnSwingThread( () -> {
			this.previewComposite = new edu.wustl.lookingglass.ide.croquet.models.preview.PreviewProjectComposite();

			org.lgna.croquet.views.Panel previewPanel = this.previewComposite.getView();
			previewPanel.setMinimumPreferredWidth( edu.wustl.lookingglass.ide.LookingGlassIDE.DEFAULT_WORLD_DIMENSION_WIDTH );
			previewPanel.setMinimumPreferredHeight( edu.wustl.lookingglass.ide.LookingGlassIDE.DEFAULT_WORLD_DIMENSION_HEIGHT );

			this.previewComposite.setPreviewImage( sceneImage, true );
			this.previewComposite.loadProject( this.puzzle.getPuzzleProject(), false, false );

			this.previewComposite.getView().revalidateAndRepaint();
			this.scene.setContent( previewPanel.getAwtComponent() );
		} );
	}

	public void play() {
		ThreadHelper.runOnSwingThread( () -> {
			if( this.previewComposite != null ) {
				this.previewComposite.playPreview();
			}
		} );
	}

	private void handleNextAction( javafx.event.ActionEvent event ) {
		this.puzzleComposite.hideIntroPane();
	}
}
