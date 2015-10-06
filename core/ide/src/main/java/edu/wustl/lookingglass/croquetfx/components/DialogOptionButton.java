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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;

/**
 * @author Michael Pogran
 */
public class DialogOptionButton extends javafx.scene.control.ButtonBase {

	private static final String DEFAULT_STYLE_CLASS = "dialog-option-button"; //$NON-NLS-1$

	private BooleanProperty defaultButton;
	private StringProperty title;
	private StringProperty message;

	public DialogOptionButton() {
		this( "", "" );
	}

	public DialogOptionButton( String title, String message ) {
		super();
		this.getStyleClass().add( DEFAULT_STYLE_CLASS );
		setTitle( title );
		setMessage( message );
	}

	@Override
	public java.lang.String getUserAgentStylesheet() {
		return DialogOptionButton.class.getResource( DialogOptionButton.class.getSimpleName() + ".css" ).toExternalForm();
	}

	@Override
	protected javafx.scene.control.Skin<?> createDefaultSkin() {
		return new DialogOptionButtonSkin( this );
	}

	public final BooleanProperty defaultProperty() {
		if( this.defaultButton == null ) {
			this.defaultButton = new javafx.beans.property.SimpleBooleanProperty( this, "defaultButton", false );
		}
		return this.defaultButton;
	}

	public final void setDefault( boolean value ) {
		defaultProperty().setValue( value );
	}

	public final boolean isDefault() {
		return this.defaultButton == null ? false : this.defaultButton.getValue();
	}

	public final StringProperty titleProperty() {
		if( title == null ) {
			title = new SimpleStringProperty( this, "titleText", "" );
		}
		return title;
	}

	public final void setTitle( String value ) {
		titleProperty().setValue( value );
	}

	public final String getTitle() {
		return title == null ? "" : title.getValue();
	}

	public final StringProperty messageProperty() {
		if( message == null ) {
			message = new SimpleStringProperty( this, "messageText", "" );
		}
		return message;
	}

	public final void setMessage( String value ) {
		messageProperty().setValue( value );
	}

	public final String getMessage() {
		return message == null ? "" : message.getValue();
	}

	@Override
	public void fire() {
		fireEvent( new ActionEvent() );
	}
}
