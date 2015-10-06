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
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import edu.wustl.lookingglass.croquetfx.FxComponent;

/**
 * @author Kyle J. Harms
 */
public class MentalEffortSurveyPane extends FxComponent {

	private final ObjectProperty<MentalEffortScale> mentalEffort = new SimpleObjectProperty<MentalEffortScale>( this, "mentalEffortProperty", null );

	@FXML private Label directions;

	private ToggleGroup mentalEffortGroup;
	@FXML private Toggle level1;
	@FXML private Toggle level2;
	@FXML private Toggle level3;
	@FXML private Toggle level4;
	@FXML private Toggle level5;
	@FXML private Toggle level6;
	@FXML private Toggle level7;
	@FXML private Toggle level8;
	@FXML private Toggle level9;

	public MentalEffortSurveyPane() {
		super( MentalEffortSurveyPane.class );

		this.mentalEffortGroup = new javafx.scene.control.ToggleGroup();
		level1.setUserData( MentalEffortScale.EXTREMELY_EASY );
		level2.setUserData( MentalEffortScale.VERY_EASY );
		level3.setUserData( MentalEffortScale.MODERATELY_EASY );
		level4.setUserData( MentalEffortScale.SLIGHTLY_EASY );
		level5.setUserData( MentalEffortScale.NEITHER_EASY_NOR_DIFFICULT );
		level6.setUserData( MentalEffortScale.SLIGHTLY_DIFFICULT );
		level7.setUserData( MentalEffortScale.MODERATELY_DIFFICULT );
		level8.setUserData( MentalEffortScale.VERY_DIFFICULT );
		level9.setUserData( MentalEffortScale.EXTREMELY_DIFFICULT );

		this.register( this.mentalEffortGroup, this::handleScaleSelected, level1, level2, level3, level4, level5, level6, level7, level8, level9 );
	}

	public final MentalEffortScale getMentalEffort() {
		return this.mentalEffort.get();
	}

	public final ObjectProperty<MentalEffortScale> mentalEffortProperty() {
		return this.mentalEffort;
	}

	public void setDirectionsText( String key ) {
		if( key == null ) {
			this.directions.setVisible( false );
			this.directions.setManaged( false );
		} else {
			this.directions.setVisible( true );
			this.directions.setManaged( true );
			this.directions.setText( this.getLocalizedString( key ) );
		}
	}

	private void handleScaleSelected( Toggle oldValue, Toggle newValue ) {
		if( newValue != null ) {
			this.mentalEffort.set( (MentalEffortScale)newValue.getUserData() );
		} else {
			this.mentalEffort.set( null );
		}
	}
}
