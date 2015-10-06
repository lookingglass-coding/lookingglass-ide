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
package edu.wustl.lookingglass.study.survey;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

/**
 * @author Kyle J. Harms
 */
public class SevenItemLikertPane extends edu.wustl.lookingglass.croquetfx.FxComponent {

	public enum SevenItemLikertScale {
		ONE( 1 ),
		TWO( 2 ),
		THREE( 3 ),
		FOUR( 4 ),
		FIVE( 5 ),
		SIX( 6 ),
		SEVEN( 7 );

		public final Integer level;

		private SevenItemLikertScale( Integer level ) {
			this.level = level;
		}
	}

	private final ObjectProperty<SevenItemLikertScale> rating = new SimpleObjectProperty<SevenItemLikertScale>( this, "ratingProperty", null );

	private ToggleGroup likertGroup;
	@FXML private Toggle level1;
	@FXML private Toggle level2;
	@FXML private Toggle level3;
	@FXML private Toggle level4;
	@FXML private Toggle level5;
	@FXML private Toggle level6;
	@FXML private Toggle level7;

	public SevenItemLikertPane() {
		super( SevenItemLikertPane.class );

		this.likertGroup = new javafx.scene.control.ToggleGroup();
		level1.setUserData( SevenItemLikertScale.ONE );
		level2.setUserData( SevenItemLikertScale.TWO );
		level3.setUserData( SevenItemLikertScale.THREE );
		level4.setUserData( SevenItemLikertScale.FOUR );
		level5.setUserData( SevenItemLikertScale.FIVE );
		level6.setUserData( SevenItemLikertScale.SIX );
		level7.setUserData( SevenItemLikertScale.SEVEN );

		this.register( this.likertGroup, this::handleScaleSelected, level1, level2, level3, level4, level5, level6, level7 );
	}

	public void setLevelText( SevenItemLikertScale level, String text ) {
		Toggle toggle = null;
		switch( level ) {
		case ONE:
			toggle = level1;
			break;
		case TWO:
			toggle = level2;
			break;
		case THREE:
			toggle = level3;
			break;
		case FOUR:
			toggle = level4;
			break;
		case FIVE:
			toggle = level5;
			break;
		case SIX:
			toggle = level6;
			break;
		case SEVEN:
			toggle = level7;
			break;
		}

		ToggleButton button = (ToggleButton)toggle;
		button.setText( text );
	}

	public void setLevelsTexts( String one, String two, String three, String four, String five, String six, String seven ) {
		setLevelText( SevenItemLikertScale.ONE, one );
		setLevelText( SevenItemLikertScale.TWO, two );
		setLevelText( SevenItemLikertScale.THREE, three );
		setLevelText( SevenItemLikertScale.FOUR, four );
		setLevelText( SevenItemLikertScale.FIVE, five );
		setLevelText( SevenItemLikertScale.SIX, six );
		setLevelText( SevenItemLikertScale.SEVEN, seven );
	}

	public final SevenItemLikertScale getRating() {
		return this.rating.get();
	}

	public final ObjectProperty<SevenItemLikertScale> ratingProperty() {
		return this.rating;
	}

	private void handleScaleSelected( Toggle oldValue, Toggle newValue ) {
		if( newValue != null ) {
			this.rating.set( (SevenItemLikertScale)newValue.getUserData() );
		} else {
			this.rating.set( null );
		}
	}
}
