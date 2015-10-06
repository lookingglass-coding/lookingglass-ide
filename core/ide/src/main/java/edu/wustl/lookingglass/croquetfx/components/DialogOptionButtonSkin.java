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
package edu.wustl.lookingglass.croquetfx.components;

import javafx.beans.value.ObservableValue;

import org.lgna.croquet.icon.IconSize;

import com.sun.javafx.scene.control.behavior.ButtonBehavior;

import edu.wustl.lookingglass.ide.LookingGlassTheme;

/**
 * @author Michael Pogran
 */
public class DialogOptionButtonSkin extends com.sun.javafx.scene.control.skin.BehaviorSkinBase<DialogOptionButton, com.sun.javafx.scene.control.behavior.ButtonBehavior<DialogOptionButton>> {

	private static final javafx.css.PseudoClass DEFAULT_PSEUDOCLASS_STATE = javafx.css.PseudoClass.getPseudoClass( "default" ); //$NON-NLS-1$

	private javafx.scene.image.ImageView graphic;
	private javafx.scene.control.Label title;
	private javafx.scene.control.Label message;

	protected DialogOptionButtonSkin( DialogOptionButton control ) {
		super( control, new ButtonBehavior<DialogOptionButton>( control ) );

		control.titleProperty().addListener( this::handleTitlePropertyChange );
		control.messageProperty().addListener( this::handleMessagePropertyChange );
		control.defaultProperty().addListener( this::handleDefaultPropertyChange );

		this.graphic = new javafx.scene.image.ImageView( LookingGlassTheme.getFxImage( "arrow-green-right", IconSize.FIXED ) );
		this.title = new javafx.scene.control.Label( control.getTitle() );
		this.message = new javafx.scene.control.Label( control.getMessage() );

		this.graphic.setFitHeight( 16 );
		this.graphic.setFitHeight( 16 );

		this.title.setPrefSize( javafx.scene.layout.Region.USE_COMPUTED_SIZE, javafx.scene.layout.Region.USE_COMPUTED_SIZE );
		this.message.setPrefSize( javafx.scene.layout.Region.USE_COMPUTED_SIZE, javafx.scene.layout.Region.USE_COMPUTED_SIZE );

		this.title.getStyleClass().add( "title" );
		this.message.getStyleClass().add( "message" );

		javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox();
		javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox();

		hbox.setSpacing( 10 );
		hbox.setAlignment( javafx.geometry.Pos.CENTER_LEFT );

		vbox.getChildren().addAll( this.title, this.message );
		hbox.getChildren().addAll( this.graphic, vbox );

		getChildren().add( hbox );

		if( control.isDefault() ) {
			setDefault( true );
		}
	}

	private void handleTitlePropertyChange( ObservableValue<? extends String> observable, String oldValue, String newValue ) {
		this.title.setText( newValue );
	}

	private void handleMessagePropertyChange( ObservableValue<? extends String> observable, String oldValue, String newValue ) {
		this.message.setText( newValue );
	}

	private void handleDefaultPropertyChange( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
		setDefault( newValue );
	}

	private void setDefault( boolean value ) {
		this.pseudoClassStateChanged( DEFAULT_PSEUDOCLASS_STATE, value );
	}

}
