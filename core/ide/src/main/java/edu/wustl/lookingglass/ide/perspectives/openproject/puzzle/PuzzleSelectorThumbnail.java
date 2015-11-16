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

import org.lgna.croquet.icon.IconSize;

import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.LookingGlassTheme;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * @author Kyle J. Harms
 */
public class PuzzleSelectorThumbnail extends FxComponent {

	private PuzzleProject puzzleProject;

	@FXML Pane disabledPane;
	@FXML VBox thumbnail;

	@FXML StackPane stackPane;
	@FXML ImageView scene;
	@FXML ImageView emblem;
	@FXML HBox playOverlay;
	@FXML ProgressIndicator loading;
	@FXML ImageView play;

	@FXML Label title;

	private boolean isEnabled = true;
	private Runnable onClicked;

	public enum PuzzleType {
		PUZZLE,
		LOCKED,
		COMPLETED
	}

	private PuzzleType type;

	public PuzzleSelectorThumbnail( PuzzleType type ) {
		super( PuzzleSelectorThumbnail.class );
		this.type = type;

		this.play.setImage( LookingGlassTheme.getFxImage( "media-playback-start", IconSize.MEDIUM ) );

		String emblemIcon = null;
		switch( this.type ) {
		case PUZZLE:
			emblemIcon = "emblem-puzzle";
			break;
		case LOCKED:
			emblemIcon = "emblem-puzzle-locked";
			break;
		case COMPLETED:
			emblemIcon = "emblem-puzzle-completed";
			break;
		}
		this.emblem.setImage( LookingGlassTheme.getFxImage( emblemIcon, IconSize.MEDIUM ) );
	}

	public void initialize( PuzzleProject project ) {
		ThreadHelper.runOnFxThread( () -> {
			this.puzzleProject = project;

			this.isEnabled = this.puzzleProject.getProject().doAllTypeClassesExist();
			this.disabledPane.setVisible( !this.isEnabled );

			this.scene.setImage( this.puzzleProject.getImage() );
			this.title.setText( this.puzzleProject.getTitle() );
			this.loading.setVisible( false );

			if( this.isEnabled ) {
				this.thumbnail.setOnMouseEntered( ( event ) -> {
					this.getScene().setCursor( Cursor.HAND );
					this.playOverlay.setVisible( true );
				} );
				this.thumbnail.setOnMouseExited( ( event ) -> {
					this.getScene().setCursor( Cursor.DEFAULT );
					this.playOverlay.setVisible( false );
				} );

				this.registerMouseEvent( this.thumbnail, this.thumbnail.onMouseClickedProperty(), this::handleSelectedEvent );
			}
		} );
	}

	private void handleSelectedEvent( MouseEvent event ) {
		if( this.onClicked != null ) {
			this.onClicked.run();
		}
	}

	public void setOnClicked( Runnable runnable ) {
		this.onClicked = runnable;
	}
}
