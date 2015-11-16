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

import java.util.ArrayList;
import java.util.Collections;

import org.lgna.croquet.icon.IconSize;

import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.ide.LookingGlassTheme;
import edu.wustl.lookingglass.puzzle.CompletionPuzzle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * @author Kyle J. Harms
 */
public class CorrectPuzzlePane extends FxComponent {

	private final CompletionPuzzle puzzle;

	@FXML private javafx.scene.image.ImageView face;
	@FXML private javafx.scene.control.Button cancel;
	@FXML private javafx.scene.control.Button done;
	@FXML private VBox bubbleBox;
	private SpeechBubble bubble;

	private final String[] SIMILEYS = { "face-big-smile-100", "face-smile-100", "face-giant-smile-100", "face-cool-100", "face-smirk-100", "face-tongue-100", "face-sweet-100" };
	private final String[] BUBBLE_TEXT = {
			this.getLocalizedString( "CorrectPuzzlePane.bubble.big-smile" ),
			this.getLocalizedString( "CorrectPuzzlePane.bubble.smile" ),
			this.getLocalizedString( "CorrectPuzzlePane.bubble.giant-smile" ),
			this.getLocalizedString( "CorrectPuzzlePane.bubble.cool" ),
			this.getLocalizedString( "CorrectPuzzlePane.bubble.smirk" ),
			this.getLocalizedString( "CorrectPuzzlePane.bubble.tongue" ),
			this.getLocalizedString( "CorrectPuzzlePane.bubble.sweet" ),
	};

	private int currentIndex = -1;
	private final int[] faceIndex = new int[ 3 ];

	public CorrectPuzzlePane( CompletionPuzzle puzzle ) {
		super( CorrectPuzzlePane.class );
		this.puzzle = puzzle;

		// Pick the faces...
		ArrayList<Integer> index = new ArrayList<Integer>();
		for( int i = 0; i < SIMILEYS.length; i++ ) {
			index.add( i );
		}
		Collections.shuffle( index );
		for( int i = 0; i < faceIndex.length; i++ ) {
			faceIndex[ i ] = index.get( i );
		}

		this.bubble = new SpeechBubble();
		( (Region)this.bubble.getRootNode() ).setPrefWidth( 128 );
		this.bubbleBox.getChildren().add( this.bubble.getRootNode() );
		this.setRandomFace();

		Timeline timeline = new Timeline();
		timeline.setCycleCount( Timeline.INDEFINITE );
		timeline.getKeyFrames().add( new KeyFrame( Duration.seconds( 2.0 ), actionEvent -> {
			setRandomFace();
		} ) );
		timeline.play();

		this.register( this.done, this::handleDoneAction );
		this.register( this.cancel, this::handleCancelAction );
	}

	private void setRandomFace() {
		currentIndex++;
		if( ( currentIndex < 0 ) || ( currentIndex >= faceIndex.length ) ) {
			currentIndex = 0;
		}

		this.face.setImage( LookingGlassTheme.getFxImage( SIMILEYS[ this.faceIndex[ currentIndex ] ], IconSize.FIXED ) );
		this.bubble.setText( BUBBLE_TEXT[ this.faceIndex[ currentIndex ] ] );
	}

	private void handleCancelAction( javafx.event.ActionEvent event ) {
		this.puzzle.getPuzzleComposite().hideCorrectPane();
	}

	private void handleDoneAction( javafx.event.ActionEvent event ) {
		this.puzzle.endPuzzle();
	}
}
