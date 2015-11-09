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
package edu.wustl.lookingglass.ide.perspectives.openproject.puzzle;

import java.io.File;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import org.lgna.croquet.OverlayPane;
import org.lgna.croquet.icon.IconSize;
import org.lgna.croquet.views.AbstractWindow;

import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.LookingGlassTheme;
import edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.SelectPuzzleComposite;
import edu.wustl.lookingglass.ide.perspectives.openproject.puzzle.PuzzleSelectorThumbnail.PuzzleType;
import edu.wustl.lookingglass.puzzle.ui.SpeechBubble;
import edu.wustl.lookingglass.puzzle.ui.croquet.CompletionPuzzleComposite;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

/**
 * @author Kyle J. Harms
 */
public class PuzzleSelectorPane extends FxComponent {

	@FXML ScrollPane scrollPane;
	@FXML VBox topBox;

	@FXML private ImageView face;
	@FXML private HBox bubbleBox;
	private SpeechBubble bubble;

	@FXML private TitledPane puzzlePane;
	@FXML private VBox puzzleBox;
	@FXML private TilePane puzzleGrid;

	@FXML private TitledPane completedPane;
	@FXML private VBox completedBox;

	private SelectPuzzleComposite composite;
	private OverlayPane playOverlay;

	public PuzzleSelectorPane( SelectPuzzleComposite composite ) {
		super( PuzzleSelectorPane.class );
		this.composite = composite;

		this.face.setImage( LookingGlassTheme.getFxImage( "face-big-eyed-100", IconSize.FIXED ) );

		this.bubble = new SpeechBubble( this.getLocalizedString( "PuzzleSelectorPane.bubble" ) );
		this.bubbleBox.getChildren().add( this.bubble.getRootNode() );

		this.puzzlePane.setGraphic( LookingGlassTheme.getFxImageView( "puzzle", IconSize.SMALL ) );
		this.completedPane.setGraphic( LookingGlassTheme.getFxImageView( "puzzle-completed", IconSize.SMALL ) );

		// Load the puzzles
		File puzzleDir = new File( edu.cmu.cs.dennisc.app.ApplicationRoot.getApplicationDirectory() + "/puzzles" );
		File[] files = puzzleDir.listFiles();
		Arrays.sort( files );
		for( File puzzleFile : files ) {
			PuzzleSelectorThumbnail thumb = new PuzzleSelectorThumbnail( PuzzleType.PUZZLE );
			this.puzzleGrid.getChildren().add( thumb.getRootNode() );

			// Load the project in a background thread since we take so
			// ridiculous long to load projects from disk. This way
			// we won't tie up the UI thread.
			ThreadHelper.runInBackground( () -> {
				PuzzleProject project = new PuzzleProject( puzzleFile );
				ThreadHelper.runOnFxThread( () -> {
					thumb.initialize( project );
					thumb.setOnClicked( () -> {
						this.playPuzzle( project );
					} );
				} );
			} );
		}

		// TODO: maybe bring this back too...
		setCompletedPuzzlesShowing( false );

		// TODO: this should not be unnecessary if we ever get rid of swing and croquet. But for some reason
		// javafx scrolls bars will not show up "as needed" when nested in swing/croquet components.
		this.scrollPane.vbarPolicyProperty().set( ScrollBarPolicy.ALWAYS );
	}

	public void setCompletedPuzzlesShowing( boolean shouldShow ) {
		this.completedPane.setVisible( shouldShow );
		this.completedPane.setManaged( shouldShow );
	}

	public void playPuzzle( PuzzleProject project ) {
		ThreadHelper.runOnSwingThread( () -> {
			LookingGlassIDE.getActiveInstance().setCursor( java.awt.Cursor.WAIT_CURSOR );
			AbstractWindow<?> window = composite.getRootComponent().getRoot();

			ThreadHelper.runOnFxThread( () -> {
				PuzzleSelectorPlayPane playPane = new PuzzleSelectorPlayPane( project, () -> LookingGlassIDE.getActiveInstance().loadPuzzleFrom( project.getFile() ), this::closePlayPane );

				OverlayPane.Builder overlayPaneBuilder = new OverlayPane.Builder( window, playPane.getFxViewAdaptor() )
						.borderMargin( 0 )
						.overlayColor( CompletionPuzzleComposite.LIGHT_OVERLAY_COLOR );

				SwingUtilities.invokeLater( () -> {
					this.playOverlay = new OverlayPane( overlayPaneBuilder );
					this.playOverlay.show();
					SwingUtilities.invokeLater( () -> {
						playPane.play();
						LookingGlassIDE.getActiveInstance().setCursor( java.awt.Cursor.DEFAULT_CURSOR );
					} );
				} );
			} );
		} );
	}

	public void closePlayPane() {
		javax.swing.SwingUtilities.invokeLater( () -> {
			if( this.playOverlay != null ) {
				try {
					this.playOverlay.hide();
				} catch( Throwable t ) {
					Logger.throwable( t, this );
				} finally {
					this.playOverlay = null;
				}
			}
		} );
	}
}
