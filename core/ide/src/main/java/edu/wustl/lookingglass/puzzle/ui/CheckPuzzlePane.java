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
import edu.wustl.lookingglass.puzzle.CompletionPuzzle.PuzzleProjectState;
import edu.wustl.lookingglass.puzzle.ui.croquet.PlayPuzzleProjectComposite;
import javafx.animation.FadeTransition;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * @author Kyle J. Harms
 */
public class CheckPuzzlePane extends FxComponent {

	private final CompletionPuzzle puzzle;

	@FXML private Button close;
	@FXML private Label title;

	@FXML private StackPane playStack;
	@FXML private ImageView overlay;
	@FXML private SwingNode scene;
	private PlayPuzzleProjectComposite playComposite;

	@FXML private Pane indicatorPane;
	private final PuzzleStatusIndicator answerIndicatorPane;

	@FXML private Button playCorrect;
	@FXML private Button playMine;
	@FXML private ToggleButton fastForward;
	@FXML private HBox nextBox;
	@FXML private Button next;

	public CheckPuzzlePane( CompletionPuzzle puzzle ) {
		super( CheckPuzzlePane.class );
		this.puzzle = puzzle;

		this.close.setGraphic( LookingGlassTheme.getFxImageView( "puzzle-quit", IconSize.SMALL ) );
		this.playCorrect.setGraphic( LookingGlassTheme.getFxImageView( "puzzle-play", org.lgna.croquet.icon.IconSize.SMALL ) );
		this.playMine.setGraphic( LookingGlassTheme.getFxImageView( "puzzle-play", org.lgna.croquet.icon.IconSize.SMALL ) );
		this.fastForward.setGraphic( LookingGlassTheme.getFxImageView( "puzzle-fast-forward", org.lgna.croquet.icon.IconSize.SMALL ) );
		this.next.setGraphic( LookingGlassTheme.getFxImageView( "puzzle-next", org.lgna.croquet.icon.IconSize.SMALL ) );

		this.overlay.setImage( LookingGlassTheme.getFxImage( "media-seek-forward", IconSize.LARGE ) );
		this.showOverlay( false );

		this.register( this.playCorrect, this::handlePlayCorrectAction );
		this.register( this.playMine, this::handlePlayMineAction );

		this.register( this.close, this::handleCloseAction );
		this.register( this.next, this::handleNextAction );

		this.register( this.fastForward, this::handleFastForwardAction );
		this.fastForward.setOnMousePressed( this::handleFastForwardPressed );
		this.fastForward.setOnMouseReleased( this::handleFastForwardReleased );

		this.answerIndicatorPane = new PuzzleStatusIndicator();
		this.indicatorPane.getChildren().add( this.answerIndicatorPane );

		this.setNextShowing( false );

		this.setPuzzleState( this.puzzle.getPuzzleProjectState() );

		// Initialize the preview component
		javax.swing.SwingUtilities.invokeLater( () -> {
			this.playComposite = new PlayPuzzleProjectComposite( this.puzzle, this.answerIndicatorPane );
			org.lgna.croquet.views.Panel previewPanel = this.playComposite.getView();
			previewPanel.setMinimumPreferredWidth( edu.wustl.lookingglass.ide.LookingGlassIDE.DEFAULT_WORLD_DIMENSION_WIDTH );
			previewPanel.setMinimumPreferredHeight( edu.wustl.lookingglass.ide.LookingGlassIDE.DEFAULT_WORLD_DIMENSION_HEIGHT );
			previewPanel.setMaximumPreferredWidth( edu.wustl.lookingglass.ide.LookingGlassIDE.DEFAULT_WORLD_DIMENSION_WIDTH );
			previewPanel.setMaximumPreferredHeight( edu.wustl.lookingglass.ide.LookingGlassIDE.DEFAULT_WORLD_DIMENSION_HEIGHT );
			this.playComposite.getView().revalidateAndRepaint();
			this.scene.setContent( previewPanel.getAwtComponent() );

			this.playComposite.setOnVmExecutionEnded( this::handleExecutionEnded );

			this.reset();
		} );
	}

	public void setPuzzleState( CompletionPuzzle.PuzzleProjectState state ) {
		this.puzzle.setPuzzleProjectState( state );
		if( state == PuzzleProjectState.PUZZLE ) {
			this.puzzle.setTerminatingNonPuzzleStatementsEnabled( this.puzzle.isCorrect() );
		}

		ThreadHelper.runOnFxThread( () -> {
			switch( state ) {
			case REFERENCE:
				this.title.setText( this.getLocalizedString( "CheckPuzzlePane.title.correct" ) );
				this.playStack.getStyleClass().remove( "play-border-mine" );
				this.playStack.getStyleClass().remove( "play-border-correct" );

				this.playStack.getStyleClass().add( "play-border-correct" );
				break;
			case PUZZLE:
				this.title.setText( this.getLocalizedString( "CheckPuzzlePane.title.mine" ) );
				this.playStack.getStyleClass().remove( "play-border-correct" );
				this.playStack.getStyleClass().remove( "play-border-mine" );

				this.playStack.getStyleClass().add( "play-border-mine" );
				break;
			}
		} );
	}

	public void play() {
		ThreadHelper.runOnFxThread( () -> {
			this.answerIndicatorPane.reset();

			ThreadHelper.runOnSwingThread( () -> {
				if( this.playComposite != null ) {
					this.playComposite.play();
				}
			} );
		} );
	}

	public void reset() {
		ThreadHelper.runOnFxThread( () -> {
			this.answerIndicatorPane.reset();

			ThreadHelper.runOnSwingThread( () -> {
				if( this.playComposite != null ) {
					this.playComposite.reset();
				}
			} );
		} );
	}

	private void showOverlay( boolean shouldShow ) {
		this.overlay.setVisible( shouldShow );
		if( shouldShow ) {
			FadeTransition fade = new FadeTransition( Duration.millis( 200 ), this.overlay );
			fade.setFromValue( 0.0 );
			fade.setToValue( 1.0 );
			fade.play();
		}
	}

	private void setNextShowing( boolean shouldShow ) {
		this.nextBox.setVisible( shouldShow );
		this.nextBox.setManaged( shouldShow );
	}

	private void closePane() {
		this.setNextShowing( false );
		this.puzzle.setTerminatingNonPuzzleStatementsEnabled( true );

		this.puzzle.getPuzzleComposite().hideCheckPane();
		if( this.puzzle.getPuzzleComparison().isCorrect() ) {
			this.puzzle.getPuzzleComposite().showCorrectPane();
		}
	}

	private void handleExecutionEnded() {
		switch( this.puzzle.getPuzzleProjectState() ) {
		case REFERENCE:
			break;
		case PUZZLE:
			if( this.puzzle.getPuzzleComparison().isCorrect() ) {
				ThreadHelper.runOnFxThread( () -> {
					this.setNextShowing( true );
				} );
			}
			break;
		}
	}

	private void handlePlayCorrectAction( javafx.event.ActionEvent event ) {
		this.setPuzzleState( PuzzleProjectState.REFERENCE );
		this.play();
	}

	private void handlePlayMineAction( javafx.event.ActionEvent event ) {
		this.setPuzzleState( PuzzleProjectState.PUZZLE );
		this.play();
	}

	private void handleCloseAction( javafx.event.ActionEvent event ) {
		this.closePane();
	}

	private void handleNextAction( javafx.event.ActionEvent event ) {
		this.closePane();
	}

	private void handleFastForwardAction( Boolean oldValue, Boolean newValue ) {
		this.playComposite.toggleFastForward( newValue );
	}

	private void handleFastForwardPressed( MouseEvent event ) {
		this.showOverlay( true );
		this.fastForward.setSelected( true );
	}

	private void handleFastForwardReleased( MouseEvent event ) {
		this.fastForward.setSelected( false );
		this.showOverlay( false );
	}
}
