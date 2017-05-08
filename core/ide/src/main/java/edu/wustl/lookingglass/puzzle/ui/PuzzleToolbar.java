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

import java.awt.Cursor;

import org.lgna.croquet.icon.IconSize;
import org.lgna.croquet.undo.event.HistoryInsertionIndexEvent;

import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.LookingGlassTheme;
import edu.wustl.lookingglass.puzzle.CompletionPuzzle;
import edu.wustl.lookingglass.puzzle.ui.croquet.CompletionPuzzleComposite;
import javafx.fxml.FXML;

/**
 * @author Kyle J. Harms
 */
public class PuzzleToolbar extends FxComponent {

	private final CompletionPuzzle puzzle;
	private final CompletionPuzzleComposite puzzleComposite;

	@FXML private javafx.scene.control.Button undo;
	@FXML private javafx.scene.control.Button redo;
	@FXML private javafx.scene.control.Button reset;
	@FXML private javafx.scene.control.Button done;

	public PuzzleToolbar( CompletionPuzzle puzzle, CompletionPuzzleComposite puzzleComposite ) {
		super( PuzzleToolbar.class );
		this.puzzle = puzzle;
		this.puzzleComposite = puzzleComposite;

		this.undo.setGraphic( LookingGlassTheme.getFxImageView( "puzzle-undo", IconSize.SMALL ) );
		this.undo.setTooltip( new javafx.scene.control.Tooltip( this.getResources().getString( "PuzzleToolbar.undo.tooltip" ) ) );

		this.redo.setGraphic( LookingGlassTheme.getFxImageView( "puzzle-redo", IconSize.SMALL ) );
		this.redo.setTooltip( new javafx.scene.control.Tooltip( this.getResources().getString( "PuzzleToolbar.redo.tooltip" ) ) );

		this.reset.setGraphic( LookingGlassTheme.getFxImageView( "puzzle-clear", IconSize.SMALL ) );
		this.reset.setTooltip( new javafx.scene.control.Tooltip( this.getResources().getString( "PuzzleToolbar.reset.tooltip" ) ) );

		this.done.setGraphic( LookingGlassTheme.getFxImageView( "puzzle-quit", IconSize.SMALL ) );
		this.done.setTooltip( new javafx.scene.control.Tooltip( this.getResources().getString( "PuzzleToolbar.done.tooltip" ) ) );

		this.register( this.undo, this::handleUndoAction );
		this.register( this.redo, this::handleRedoAction );
		this.register( this.reset, this::handleResetAction );
		this.register( this.done, this::handleDoneAction );

		// update widgets to show correct status for undo/redo/reset
		this.puzzleComposite.setHistoryListener( new org.lgna.croquet.undo.event.HistoryListener() {
			@Override
			public void insertionIndexChanged( HistoryInsertionIndexEvent e ) {
				org.lgna.croquet.undo.UndoHistory source = e.getTypedSource();
				if( source.getGroup() == org.lgna.croquet.Application.PROJECT_GROUP ) {
					PuzzleToolbar.this.updateUndoRedoStatus();
				}
			}
		} );
		this.updateUndoRedoStatus();
	}

	private void updateUndoRedoStatus() {
		final int currentIndex = this.puzzleComposite.getHistoryManager().getInsertionIndex();
		final int stackSize = this.puzzleComposite.getHistoryManager().getStack().size();
		final int indexZero = this.puzzleComposite.getHistoryIndexZero();
		final org.lgna.croquet.edits.Edit activeEdit = this.puzzleComposite.getHistoryManager().getActiveEdit();

		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			// undo
			if( currentIndex > indexZero ) {
				this.undo.setDisable( false );
			} else {
				this.undo.setDisable( true );
			}

			// redo
			if( currentIndex < stackSize ) {
				this.redo.setDisable( false );
			} else {
				this.redo.setDisable( true );
			}

			// reset
			if( ( currentIndex > indexZero ) && !( activeEdit instanceof edu.wustl.lookingglass.puzzle.ui.croquet.edits.ResetPuzzleEdit ) ) {
				this.reset.setDisable( false );
			} else {
				this.reset.setDisable( true );
			}
		} );
	}

	private void handleUndoAction( javafx.event.ActionEvent event ) {
		ThreadHelper.runOnSwingThread( () -> {
			LookingGlassIDE.getActiveInstance().setCursor( Cursor.WAIT_CURSOR );
			this.puzzleComposite.undo();
			LookingGlassIDE.getActiveInstance().setCursor( Cursor.DEFAULT_CURSOR );
		} );
	}

	private void handleRedoAction( javafx.event.ActionEvent event ) {
		ThreadHelper.runOnSwingThread( () -> {
			LookingGlassIDE.getActiveInstance().setCursor( Cursor.WAIT_CURSOR );
			this.puzzleComposite.redo();
			LookingGlassIDE.getActiveInstance().setCursor( Cursor.DEFAULT_CURSOR );
		} );
	}

	private void handleResetAction( javafx.event.ActionEvent event ) {
		ThreadHelper.runOnSwingThread( () -> {
			LookingGlassIDE.getActiveInstance().setCursor( Cursor.WAIT_CURSOR );
			this.puzzleComposite.reset();
			LookingGlassIDE.getActiveInstance().setCursor( Cursor.DEFAULT_CURSOR );
		} );
	}

	private void handleDoneAction( javafx.event.ActionEvent event ) {
		if( this.puzzle.getPuzzleComparison().isCorrect() ) {
			this.puzzleComposite.showCorrectPane();
		} else {
			this.puzzleComposite.showIncorrectPane();
		}
	}
}
