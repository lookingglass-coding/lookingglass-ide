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

import org.lgna.croquet.icon.IconSize;

import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.ide.LookingGlassTheme;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

/**
 * @author Michael Pogran
 */
public class ExceptionDialog extends FxComponent {

	private javafx.collections.ObservableList<Throwable> exceptions;
	private edu.wustl.lookingglass.issue.ExceptionPane exceptionPane;
	private javafx.stage.Stage stage;
	private ChangeListener<Boolean> stageFocusListener;

	@FXML private javafx.scene.image.ImageView logo;
	@FXML private javafx.scene.control.Label title;
	@FXML private javafx.scene.control.Label message;
	@FXML private javafx.scene.control.Label stackCount;
	@FXML private javafx.scene.layout.Pane stackBadge;
	@FXML private javafx.scene.control.Pagination stackPagination;

	@FXML private javafx.scene.layout.Pane rootContainer;
	@FXML private javafx.scene.layout.Pane stackContainer;
	@FXML private javafx.scene.layout.Pane exceptionContainer;
	@FXML private javafx.scene.layout.BorderPane overlayContainer;

	@FXML private javafx.scene.control.Separator exceptionSeparator;
	@FXML private javafx.scene.control.Hyperlink stackToggle;
	@FXML private javafx.scene.control.Button closeButton;
	@FXML private javafx.scene.control.Button submitButton;

	public ExceptionDialog() {
		super( ExceptionDialog.class );

		this.stackToggle.setGraphic( LookingGlassTheme.getFxImageView( "more-details", IconSize.FIXED ) );

		this.stackContainer.managedProperty().bind( this.stackContainer.visibleProperty() );
		this.stackContainer.setVisible( false );

		this.submitButton.managedProperty().bind( this.submitButton.visibleProperty() );
		this.exceptionContainer.managedProperty().bind( this.exceptionContainer.visibleProperty() );
		this.exceptionSeparator.managedProperty().bind( this.exceptionSeparator.visibleProperty() );
		this.exceptionSeparator.visibleProperty().bind( this.exceptionContainer.visibleProperty() );

		this.register( this.stackToggle, this::handleStackToggleAction );
		this.register( this.closeButton, this::handleCloseAction );
		this.register( this.submitButton, this::handleSubmitAction );

		this.exceptions = javafx.collections.FXCollections.observableList( edu.cmu.cs.dennisc.java.util.Lists.newLinkedList() );
		this.exceptions.addListener( this::exceptionsChanged );

		this.stage = this.getStage();
		this.stage.initStyle( javafx.stage.StageStyle.DECORATED );
		this.stage.setOnHidden( this::handleStageHidden );

		this.stageFocusListener = new ChangeListener<Boolean>() {
			@Override
			public void changed( javafx.beans.value.ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
				if( newValue == false ) {
					stage.setAlwaysOnTop( false );
					stage.toFront();
				}
				stage.setAlwaysOnTop( true );
			};
		};
	}

	public void pushExceptionPane( edu.wustl.lookingglass.issue.ExceptionPane exceptionPane, boolean forcePane ) {
		assert javafx.application.Platform.isFxApplicationThread();

		// If this is the first exception presented or OutOfMemoryException thrown, perform setup
		if( ( this.exceptionPane == null ) || forcePane ) {
			String title = exceptionPane.getErrorTitle();
			String message = exceptionPane.getErrorMessage();

			edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {

				// If exceptionPane has custom title, then set it
				if( title != null ) {
					this.title.setText( title );
				}

				// If exceptionPane has custom message, then set it
				if( message != null ) {
					this.message.setText( message );
				}
				// Set exceptionPane
				if( !( this.exceptionContainer.getChildren().isEmpty() ) ) {
					this.exceptionContainer.getChildren().clear();
				}
				this.exceptionContainer.getChildren().add( exceptionPane.getRootNode() );
				this.logo.setImage( exceptionPane.getImage() );
			} );
			this.exceptionPane = exceptionPane;
			this.exceptionPane.setDialog( this );
		}

		// Add exception to stacktrace
		this.exceptions.add( exceptionPane.getThrowable() );
	}

	public java.util.List<Throwable> getExceptions() {
		return this.exceptions;
	}

	public boolean isShowing() {
		return ( this.stage != null ) && ( this.stage.isShowing() );
	}

	public void show() {
		assert this.stage == this.getStage();

		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			this.stackPagination.setPageFactory( this::createStackTracePage );
			this.showStack( false );

			this.stage.focusedProperty().addListener( this.stageFocusListener );

			this.stage.sizeToScene();
			this.stage.show();
			this.stage.toFront();
			this.stage.setAlwaysOnTop( true );

			this.submitButton.requestFocus();

			FxComponent.centerStageOnCroquetWindow( getOwner(), getStage() );
		} );
	}

	public void close() {
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			this.stage.focusedProperty().removeListener( this.stageFocusListener );
			this.stage.setAlwaysOnTop( false );
			this.stage.close();
		} );
	}

	public void setAndShowOverlay( javafx.scene.Node node ) {
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			this.overlayContainer.setCenter( node );
			this.overlayContainer.setVisible( true );
		} );
	}

	public void hideOverlay() {
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			this.overlayContainer.setVisible( false );
		} );
	}

	public void hideSubmitButton() {
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			this.submitButton.setVisible( false );
		} );
	}

	public void hideExceptionContainer() {
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			this.exceptionContainer.setVisible( false );
		} );
	}

	private void exceptionsChanged( javafx.collections.ListChangeListener.Change<? extends Throwable> listener ) {
		Integer size = listener.getList().size();

		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( () -> {
			this.stackPagination.setPageCount( size );
			this.stackCount.setText( size.toString() );

			if( size > 1 ) {
				this.stackBadge.setVisible( true );
			}
		} );
	}

	private javafx.scene.Node createStackTracePage( int index ) {
		assert javafx.application.Platform.isFxApplicationThread();

		if( index >= this.exceptions.size() ) {
			return null;
		} else {
			String trace = edu.cmu.cs.dennisc.java.lang.ThrowableUtilities.getStackTraceAsString( this.exceptions.get( index ) );

			javafx.scene.control.TextArea rv = new javafx.scene.control.TextArea( trace );
			rv.setEditable( false );
			rv.setStyle( "-fx-font-family: monospace; -fx-font-size: 9pt;" );

			rv.setMaxWidth( Double.MAX_VALUE );
			rv.setPrefHeight( 180 );
			return rv;
		}
	}

	private java.awt.Component getOwner() {
		assert javafx.application.Platform.isFxApplicationThread();

		org.lgna.croquet.Application<?> application = org.lgna.croquet.Application.getActiveInstance();
		java.awt.Component rv = null;
		if( application != null ) {
			org.lgna.croquet.views.Frame frame = application.getDocumentFrame().getFrame();
			if( frame != null ) {
				rv = frame.getAwtComponent();
			}
		}
		return rv;
	}

	private void handleStageHidden( WindowEvent event ) {
		this.submitButton.setVisible( true );
		this.exceptionContainer.setVisible( true );

		// Clear exception panel
		this.exceptionContainer.getChildren().clear();
		this.exceptionPane = null;

		// Reset stacktrace
		this.exceptions.clear();
		this.showStack( false );
		this.stackPagination.setPageCount( 0 );
		this.stackPagination.setCurrentPageIndex( 0 );
		this.stackPagination.setPageFactory( null );
		this.stackBadge.setVisible( false );
		this.stackContainer.setVisible( false );

		// Remove overlay
		this.overlayContainer.getChildren().clear();
		this.overlayContainer.setVisible( false );

		this.title.setText( getLocalizedString( "ExceptionDialog.heading" ) );
		this.message.setText( getLocalizedString( "ExceptionDialog.warning" ) );
	}

	private void handleSubmitAction( javafx.event.ActionEvent event ) {
		if( this.exceptionPane != null ) {
			this.exceptionPane.submit();
		}
	}

	private void handleCloseAction( javafx.event.ActionEvent event ) {
		this.stage.close();
	}

	private void showStack( boolean isShowing ) {
		this.stackContainer.setVisible( isShowing );
		this.stackToggle.setGraphic( LookingGlassTheme.getFxImageView( ( isShowing ? "fewer-details" : "more-details" ), IconSize.FIXED ) );
		this.stage.sizeToScene();
	}

	private void handleStackToggleAction( javafx.event.ActionEvent event ) {
		boolean setVisible = !( this.stackContainer.isVisible() );
		this.showStack( setVisible );
	}
}
