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
package edu.wustl.lookingglass.puzzle.ui.croquet;

import java.awt.Dimension;

import javax.swing.SwingUtilities;

import org.alice.ide.ProjectApplication;
import org.lgna.croquet.OverlayPane;
import org.lgna.croquet.views.AbstractWindow;

import edu.cmu.cs.dennisc.java.lang.SystemUtilities;
import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.wustl.lookingglass.croquetfx.FxViewAdaptor;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.puzzle.CompletionPuzzle;
import edu.wustl.lookingglass.puzzle.ui.CheckPuzzlePane;
import edu.wustl.lookingglass.puzzle.ui.CorrectPuzzlePane;
import edu.wustl.lookingglass.puzzle.ui.IncorrectPuzzlePane;
import edu.wustl.lookingglass.puzzle.ui.PuzzleIntroPane;
import edu.wustl.lookingglass.puzzle.ui.TimeOutPuzzlePane;
import edu.wustl.lookingglass.puzzle.ui.croquet.views.CompletionPuzzleView;

/**
 * @author Kyle J. Harms
 */
public class CompletionPuzzleComposite extends org.lgna.croquet.SimpleComposite<CompletionPuzzleView> {

	private final CompletionPuzzle puzzle;

	private final java.awt.image.BufferedImage scenePreview;
	private final javafx.scene.image.Image fxScenePreview;

	private final PuzzleResourcesComposite resources;
	private final PuzzleEditorComposite editor;

	private OverlayPane introOverlay;
	private CheckPuzzlePane checkPane;
	private OverlayPane checkOverlay;
	private OverlayPane correctOverlay;
	private OverlayPane incorrectOverlay;
	private OverlayPane timeOutOverlay;
	private boolean shouldShowTimeOutOverlay = false;

	private final org.lgna.croquet.undo.UndoHistory historyManager;
	private final int historyIndexZero;
	private final ResetPuzzleOperation resetOperation;
	private org.lgna.croquet.undo.event.HistoryListener historyListener = null;

	private AbstractWindow<?> window = null;

	public static final java.awt.Color LIGHT_OVERLAY_COLOR;

	static {
		java.awt.Color oldColor = OverlayPane.DEFAULT_OVERLAY_COLOR;
		LIGHT_OVERLAY_COLOR = new java.awt.Color( oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), (int)( oldColor.getAlpha() / 1.6 ) );
	}

	public CompletionPuzzleComposite( CompletionPuzzle puzzle ) {
		super( java.util.UUID.fromString( "a6115ce0-f70f-4848-854c-0630fc25726c" ) );
		assert java.awt.EventQueue.isDispatchThread(); // sigh...

		this.puzzle = puzzle;

		ProjectApplication.getActiveInstance().freezeProjectUpToDateWithFile();
		this.historyManager = ProjectApplication.getActiveInstance().getDocumentFrame().getDocument().getUndoHistory( org.lgna.croquet.Application.PROJECT_GROUP );
		this.historyIndexZero = this.historyManager.getInsertionIndex();
		this.resetOperation = new ResetPuzzleOperation( this.historyManager, this.historyIndexZero );

		// Compute all of the preview images up front to improve loading...
		this.scenePreview = edu.wustl.lookingglass.utilities.ProjectThumbnailGenerator.createThumbnail( edu.wustl.lookingglass.ide.LookingGlassIDE.DEFAULT_WORLD_DIMENSION_WIDTH, edu.wustl.lookingglass.ide.LookingGlassIDE.DEFAULT_WORLD_DIMENSION_HEIGHT, this.puzzle.getPuzzleProject() );
		this.fxScenePreview = edu.wustl.lookingglass.croquetfx.ThreadHelper.invokeInFxThreadAndWait( () -> {
			javafx.scene.image.Image fxImage = javafx.embed.swing.SwingFXUtils.toFXImage( this.scenePreview, null );
			return fxImage;
		} );

		this.resources = new PuzzleResourcesComposite( this.puzzle, this, this.fxScenePreview );
		this.registerSubComposite( this.resources );

		this.editor = new PuzzleEditorComposite( this.puzzle, this );
		this.registerSubComposite( this.editor );
	}

	public CompletionPuzzle getPuzzle() {
		return this.puzzle;
	}

	public PuzzleResourcesComposite getResourcesComposite() {
		return this.resources;
	}

	public PuzzleResourcesBinComposite getResourcesBinComposite() {
		return this.resources.getBinComposite();
	}

	public PuzzleEditorComposite getEditorComposite() {
		return this.editor;
	}

	public edu.wustl.lookingglass.puzzle.ui.croquet.views.PuzzleCodeEditor getPuzzleEditor() {
		return this.editor.getEditor();
	}

	// This whole method is a hack. I'm sorry. I wrote this better... but I'm forced do some of this
	// this way because of the way croquet initializes composites and perspectives.
	public void initializeInterface() {
		javax.swing.SwingUtilities.invokeLater( () -> {
			this.window = this.getRootComponent().getRoot();
			assert this.window != null;

			// Ugh. This is really awful... I'm trying to delay based on the delays
			// that happens to create the directions pane
			edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
				this.checkPane = new CheckPuzzlePane( this.puzzle, this );

				FxViewAdaptor fxViewAdaptor = this.checkPane.getFxViewAdaptor();
				fxViewAdaptor.useFxRefreshHack( false );
				OverlayPane.Builder checkOverlayPaneBuilder = new OverlayPane.Builder( this.window, fxViewAdaptor )
						.borderMargin( 0 )
						.overlayColor( LIGHT_OVERLAY_COLOR );

				javax.swing.SwingUtilities.invokeLater( () -> {
					this.checkOverlay = new OverlayPane( checkOverlayPaneBuilder );
					edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().setCursor( java.awt.Cursor.DEFAULT_CURSOR );
				} );
			} );

			this.showIntroPane();

			// We have no/little control over when composites will realize. So since
			// we can't control this in croquet we'll have to wait until we know things are
			// ready to layout items.
			ThreadHelper.runOnFxThread( () -> {
				SwingUtilities.invokeLater( () -> {
					SwingUtilities.invokeLater( () -> {
						this.resources.getBinComposite().getView().revalidateAndRepaint();
						this.resources.getBinComposite().getView().getStatementsBinView().layoutStatements();

						this.macPuzzleToolbarNotVisibleHack();
					} );
				} );
			} );
		} );
	}

	@Deprecated
	private void macPuzzleToolbarNotVisibleHack() {
		boolean doHack = Boolean.valueOf( System.getProperty( "edu.wustl.lookingglass.puzzle.resizeHack", Boolean.toString( SystemUtilities.isMac() ) ) );
		if( doHack ) {
			final Dimension windowSize = this.window.getSize();
			this.window.setSize( windowSize.width - 1, windowSize.height - 1 );
			ThreadHelper.runInBackground( () -> {
				try {
					Thread.sleep( 2 * 1000 );
				} catch( Exception e ) {
				}
				SwingUtilities.invokeLater( () -> {
					this.window.setSize( windowSize );
				} );
			} );
		}
	}

	public void showIntroPane() {
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			PuzzleIntroPane introPane = new PuzzleIntroPane( this.puzzle, this, this.scenePreview );
			OverlayPane.Builder overlayPaneBuilder = new OverlayPane.Builder( this.window, introPane.getFxViewAdaptor() )
					.borderMargin( 0 )
					.overlayColor( LIGHT_OVERLAY_COLOR );

			ThreadHelper.runOnSwingThread( () -> {
				this.introOverlay = new OverlayPane( overlayPaneBuilder );
				this.introOverlay.show();
				introPane.play();
			} );
		} );
	}

	public void hideIntroPane() {
		ThreadHelper.runOnSwingThread( () -> {
			if( this.introOverlay != null ) {
				this.introOverlay.hide();
				this.introOverlay = null;
			}
		} );
	}

	public void showCheckPane( CompletionPuzzle.PuzzleProjectState state ) {
		this.checkPane.setPuzzleState( state );

		if( this.checkOverlay != null ) {
			ThreadHelper.runOnSwingThread( () -> {
				this.checkOverlay.show();
				this.checkPane.play();
			} );
		}
	}

	public void hideCheckPane() {
		ThreadHelper.runOnSwingThread( () -> {
			if( checkPane != null ) {
				this.checkPane.reset();
			}
			if( checkOverlay != null ) {
				this.checkOverlay.hide();
			}
			this.checkAndShowTimeOutPane();
		} );
	}

	public void showCorrectPane() {
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			final CorrectPuzzlePane correctPane = new CorrectPuzzlePane( this.puzzle, this );
			final OverlayPane.Builder correctPaneBuilder = new OverlayPane.Builder( this.window, correctPane.getFxViewAdaptor() )
					.borderMargin( 0 )
					.overlayColor( LIGHT_OVERLAY_COLOR );

			ThreadHelper.runOnSwingThread( () -> {
				this.correctOverlay = new OverlayPane( correctPaneBuilder );
				this.correctOverlay.show();
			} );
		} );
	}

	public void hideCorrectPane() {
		ThreadHelper.runOnSwingThread( () -> {
			if( this.correctOverlay != null ) {
				this.correctOverlay.hide();
				this.correctOverlay = null;
			}
		} );
	}

	public void showIncorrectPane() {
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			final IncorrectPuzzlePane donePane = new IncorrectPuzzlePane( this.puzzle, this );
			final OverlayPane.Builder donePaneBuilder = new OverlayPane.Builder( this.window, donePane.getFxViewAdaptor() )
					.borderMargin( 0 )
					.overlayColor( LIGHT_OVERLAY_COLOR );

			ThreadHelper.runOnSwingThread( () -> {
				this.incorrectOverlay = new OverlayPane( donePaneBuilder );
				this.incorrectOverlay.show();
			} );
		} );
	}

	public void hideIncorrectPane() {
		ThreadHelper.runOnSwingThread( () -> {
			if( this.incorrectOverlay != null ) {
				this.incorrectOverlay.hide();
				this.incorrectOverlay = null;
			}
			this.checkAndShowTimeOutPane();
		} );
	}

	protected void checkAndShowTimeOutPane() {
		if( ( this.timeOutOverlay != null ) && this.timeOutOverlay.isOverlayShowing() ) {
			return;
		} else if( this.shouldShowTimeOutOverlay ) {
			this.showTimeOutPane();
		}
	}

	public void showTimeOutPaneWhenAppropiate() {
		if( ( ( this.checkOverlay != null ) && this.checkOverlay.isOverlayShowing() ) ||
				( ( this.incorrectOverlay != null ) && this.incorrectOverlay.isOverlayShowing() ) ) {
			this.shouldShowTimeOutOverlay = true;
		} else {
			this.showTimeOutPane();
		}
	}

	protected void showTimeOutPane() {
		this.shouldShowTimeOutOverlay = false;
		assert !this.puzzle.isCorrect() : "puzzle is correct. time out invalid.";

		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			final TimeOutPuzzlePane timeOutPane = new TimeOutPuzzlePane( this.puzzle, this );
			final OverlayPane.Builder timeOutPaneBuilder = new OverlayPane.Builder( this.window, timeOutPane.getFxViewAdaptor() )
					.borderMargin( 0 )
					.overlayColor( LIGHT_OVERLAY_COLOR )
					.layerId( OverlayPane.DEFAULT_OVERLAY_LAYER + 1 );

			ThreadHelper.runOnSwingThread( () -> {
				this.timeOutOverlay = new OverlayPane( timeOutPaneBuilder );
				this.timeOutOverlay.show();
			} );
		} );
	}

	public void hideTimeOutPane() {
		this.shouldShowTimeOutOverlay = false;

		ThreadHelper.runOnSwingThread( () -> {
			if( this.timeOutOverlay != null ) {
				this.timeOutOverlay.hide();
				this.timeOutOverlay = null;
			}
		} );
	}

	@Override
	protected CompletionPuzzleView createView() {
		return new CompletionPuzzleView( this );
	}

	public org.lgna.croquet.undo.UndoHistory getHistoryManager() {
		return this.historyManager;
	}

	public int getHistoryIndexZero() {
		return this.historyIndexZero;
	}

	public void setHistoryListener( org.lgna.croquet.undo.event.HistoryListener historyListener ) {
		this.historyListener = historyListener;
		this.historyManager.addHistoryListener( this.historyListener );
	}

	public void undo() {
		ThreadHelper.runOnSwingThread( () -> {
			this.puzzle.haltPuzzleEvaluationAndWork( () -> this.getHistoryManager().performUndo() );
		} );
	}

	public void redo() {
		ThreadHelper.runOnSwingThread( () -> {
			this.puzzle.haltPuzzleEvaluationAndWork( () -> this.getHistoryManager().performRedo() );
		} );
	}

	public void reset() {
		ThreadHelper.runOnSwingThread( () -> {
			this.puzzle.haltPuzzleEvaluationAndWork( () -> this.resetOperation.fire() );
		} );
	}

	// sigh... this is a hack... to make undo work for the puzzles and still not break undo for code editing
	public void restoreUndoHistory() {
		if( this.historyListener != null ) {
			this.historyManager.removeHistoryListener( this.historyListener );
		}

		ProjectApplication.getActiveInstance().thawProjectUpToDateWithFile();
		this.historyManager.setInsertionIndex( this.historyIndexZero, false );
		while( this.historyManager.getStack().size() > this.historyManager.getInsertionIndex() ) {
			this.historyManager.getStack().pop();
		}
		org.alice.ide.ProjectApplication.getActiveInstance().updateUndoRedoEnabled();
	}

	@Override
	public void handlePostDeactivation() {
		try {
			this.hideTimeOutPane();

			this.hideIntroPane();
			this.hideCheckPane();
			this.hideCorrectPane();
			this.hideIncorrectPane();
		} catch( Throwable t ) {
			// We should clean up just fine, but in case we don't it's really ok.
			Logger.throwable( t, this );
		}

		super.handlePostDeactivation();
	}
}
