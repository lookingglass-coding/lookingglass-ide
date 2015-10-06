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
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

/**
 * @author Kyle J. Harms
 */
public class IncorrectPuzzlePane extends FxComponent {

	private final CompletionPuzzle puzzle;

	@FXML private javafx.scene.image.ImageView face;
	@FXML private javafx.scene.control.Button cancel;
	@FXML private javafx.scene.control.Button done;
	@FXML private VBox bubbleBox;
	private SpeechBubble bubble;

	public IncorrectPuzzlePane( CompletionPuzzle puzzle ) {
		super( IncorrectPuzzlePane.class );
		this.puzzle = puzzle;

		this.face.setImage( LookingGlassTheme.getFxImage( "face-frown-100", IconSize.FIXED ) );
		this.bubble = new SpeechBubble( this.getLocalizedString( "IncorrectPuzzlePane.bubble" ) );
		this.bubbleBox.getChildren().add( this.bubble.getRootNode() );

		this.register( this.done, this::handleDoneAction );
		this.register( this.cancel, this::handleCancelAction );
	}

	private void handleCancelAction( javafx.event.ActionEvent event ) {
		this.puzzle.getPuzzleComposite().hideIncorrectPane();
	}

	private void handleDoneAction( javafx.event.ActionEvent event ) {
		this.puzzle.getPuzzleComposite().getPuzzle().endPuzzle();
	}
}
