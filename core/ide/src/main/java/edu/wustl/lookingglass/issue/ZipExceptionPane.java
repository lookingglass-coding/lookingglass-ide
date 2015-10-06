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
package edu.wustl.lookingglass.issue;

import javafx.event.EventHandler;
import javafx.fxml.FXML;

import org.alice.ide.perspectives.noproject.NoProjectPerspective;

import edu.wustl.lookingglass.croquetfx.components.DialogOptionButton;
import edu.wustl.lookingglass.croquetfx.components.DialogOptionButtonGroup;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.perspectives.openproject.OpenProjectComposite;

/**
 * @author Michael Pogran
 */
public class ZipExceptionPane extends ExceptionPane {

	@FXML private javafx.scene.layout.BorderPane optionButtonsContainer;

	public ZipExceptionPane( Thread thread, Throwable throwable ) {
		super( ZipExceptionPane.class, thread, throwable );

		DialogOptionButton selectNewButton = new DialogOptionButton( getLocalizedString( "ZipExceptionPane.selectNewAction" ), getLocalizedString( "ZipExceptionPane.selectNewActionSubtitle" ) );
		DialogOptionButton repairButton = new DialogOptionButton( getLocalizedString( "ZipExceptionPane.repairAction" ), getLocalizedString( "ZipExceptionPane.repairActionSubtitle" ) );

		DialogOptionButtonGroup buttonGroup = new DialogOptionButtonGroup( edu.cmu.cs.dennisc.java.util.Lists.newArrayList( repairButton, selectNewButton ), repairButton );

		this.optionButtonsContainer.setCenter( buttonGroup );

		this.registerActionEvent( selectNewButton, selectNewButton.onActionProperty(), this::handleSelectNewAction );
		this.registerActionEvent( repairButton, repairButton.onActionProperty(), this::handleSelectRepairAction );
	}

	@Override
	public java.lang.String getErrorTitle() {
		return getLocalizedString( "ZipExceptionPane.title" );
	}

	@Override
	public java.lang.String getErrorMessage() {
		String fileName = getLocalizedString( "ZipExceptionPane.defaultFileName" );
		if( getRootThrowable() instanceof edu.wustl.lookingglass.ide.uricontent.exceptions.FailedToLoadFileException ) {
			java.io.File file = ( (edu.wustl.lookingglass.ide.uricontent.exceptions.FailedToLoadFileException)getRootThrowable() ).getFile();
			fileName = file.getName();
		}

		Object[] args = { fileName };
		return getLocalizedString( "ZipExceptionPane.message", args );
	}

	private void handleSelectNewAction( javafx.event.ActionEvent event ) {
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( ( ) -> {
			getDialog().close();
		} );

		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnSwingThread( ( ) -> {
			NoProjectPerspective noProjectPerspective = LookingGlassIDE.getActiveInstance().getDocumentFrame().getNoProjectPerspective();
			OpenProjectComposite openProjectComposite = noProjectPerspective.getMainComposite();
			LookingGlassIDE.getActiveInstance().setPerspective( noProjectPerspective );
			openProjectComposite.getTabState().setValueTransactionlessly( openProjectComposite.getExistingProjectComposite() );
		} );
	}

	private void handleSelectRepairAction( javafx.event.ActionEvent event ) {
		if( getRootThrowable() instanceof edu.wustl.lookingglass.ide.uricontent.exceptions.FailedToLoadFileException ) {
			java.io.File file = ( (edu.wustl.lookingglass.ide.uricontent.exceptions.FailedToLoadFileException)getRootThrowable() ).getFile();

			Thread repairThread = new Thread( new Runnable() {

				@Override
				public void run() {
					final edu.wustl.lookingglass.media.ZipRepairProcess process = new edu.wustl.lookingglass.media.ZipRepairProcess( file );
					java.io.File repairFile = null;

					java.util.concurrent.Callable<java.io.File> callable = new java.util.concurrent.Callable<java.io.File>() {

						@Override
						public java.io.File call() throws Exception {
							return process.repair();
						}
					};

					java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool( 1 );
					java.util.concurrent.Future<java.io.File> future = null;

					future = executor.submit( callable );

					try {
						// Returned file is null if operation failed
						repairFile = future.get( 1, java.util.concurrent.TimeUnit.SECONDS );
					} catch( InterruptedException | java.util.concurrent.ExecutionException e ) {
						e.printStackTrace();
					} catch( java.util.concurrent.TimeoutException e ) {
						future.cancel( true );
						process.cleanUp();
					} finally {
						openRepairedFile( repairFile );
					}
				}
			} );
			repairThread.start();
		}
	}

	private void openRepairedFile( java.io.File repairFile ) {
		if( repairFile != null ) {
			// Try and read project
			try {
				org.lgna.project.io.IoUtilities.readProject( repairFile );
			} catch( Exception readFailedException ) {
				removeRepairedFile( repairFile );
				showFailureDialog();
			}
			boolean shouldSave = !( org.alice.ide.ProjectApplication.getActiveInstance().isProjectUpToDateWithFile() );
			String messageText = shouldSave ? getLocalizedString( "ZipExceptionPane.openAndSave" ) : getLocalizedString( "ZipExceptionPane.open" );

			javafx.scene.Node node = createMessageDialog( getLocalizedString( "ZipExceptionPane.openTitle" ), messageText, getLocalizedString( "ZipExceptionPane.openButton" ), ( javafx.event.ActionEvent event ) -> {
				loadRepairedFile( shouldSave, repairFile );
			} );

			getDialog().setAndShowOverlay( node );

		} else {
			showFailureDialog();
		}
	}

	private void showFailureDialog() {
		javafx.scene.Node node = createMessageDialog( getLocalizedString( "ZipExceptionPane.failureTitle" ), getLocalizedString( "ZipExceptionPane.failure" ), getLocalizedString( "ZipExceptionPane.failureButton" ), this::handleSelectNewAction );
		getDialog().setAndShowOverlay( node );
	}

	private javafx.scene.Node createMessageDialog( String titleText, String messageText, String buttonText, EventHandler<javafx.event.ActionEvent> value ) {
		javafx.scene.layout.VBox container = new javafx.scene.layout.VBox( 10 );
		javafx.scene.control.Label title = new javafx.scene.control.Label( titleText );
		javafx.scene.control.Label message = new javafx.scene.control.Label( messageText );
		javafx.scene.control.Button button = new javafx.scene.control.Button( buttonText );

		title.getStyleClass().add( "title" );
		button.setOnAction( value );

		container.getStyleClass().add( "overlay-container" );
		container.setPrefSize( 400, javafx.scene.control.Control.USE_COMPUTED_SIZE );
		container.setMaxSize( javafx.scene.control.Control.USE_PREF_SIZE, javafx.scene.control.Control.USE_PREF_SIZE );
		container.setMinSize( javafx.scene.control.Control.USE_COMPUTED_SIZE, javafx.scene.control.Control.USE_COMPUTED_SIZE );

		javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow( 5.0, javafx.scene.paint.Color.BLACK );
		container.setEffect( shadow );

		container.getChildren().addAll( title, message, new javafx.scene.control.Separator( javafx.geometry.Orientation.HORIZONTAL ), button );

		return container;
	}

	private void removeRepairedFile( java.io.File repairFile ) {
		try {
			java.nio.file.Files.delete( repairFile.toPath() );
		} catch( java.io.IOException e2 ) {
			e2.printStackTrace();
		}
	}

	private void loadRepairedFile( boolean shouldSave, java.io.File repairFile ) {
		org.alice.ide.uricontent.FileProjectLoader loader = new org.alice.ide.uricontent.FileProjectLoader( repairFile );

		if( shouldSave && !( org.alice.ide.ProjectApplication.getActiveInstance().isProjectUpToDateWithFile() ) ) {
			edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnSwingThread( ( ) -> {
				org.alice.ide.croquet.models.projecturi.SaveProjectOperation.getInstance().fire();
			} );
		}
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnSwingThread( ( ) -> {
			edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().loadProjectFrom( loader );
		} );
		getDialog().close();
	}
}
