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

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.util.Duration;

/**
 * @author Kyle J. Harms
 */
public class PuzzleStatusIndicatorSkin extends javafx.scene.control.SkinBase<PuzzleStatusIndicator> {

	private final javafx.scene.layout.HBox hbox;

	private final DoubleProperty executionPulse;
	private final Timeline timeline;

	private final java.util.concurrent.Semaphore updateLock = new java.util.concurrent.Semaphore( 1 );

	public PuzzleStatusIndicatorSkin( PuzzleStatusIndicator indicator ) {
		super( indicator );

		this.hbox = new javafx.scene.layout.HBox();
		this.hbox.getStyleClass().add( "control" );
		this.getChildren().add( this.hbox );

		this.executionPulse = new javafx.beans.property.SimpleDoubleProperty( this, "executionPulseProperty", 0.0 );

		// TODO: we should turn this off when this widget isn't visible
		this.timeline = new Timeline();
		this.timeline.setCycleCount( Timeline.INDEFINITE );
		this.timeline.setAutoReverse( true );
		KeyFrame kf0 = new KeyFrame( Duration.millis( 0 ), new KeyValue( this.executionPulse, 0.0, Interpolator.EASE_BOTH ) );
		KeyFrame kf1 = new KeyFrame( Duration.millis( 500 ), new KeyValue( this.executionPulse, 1.0, Interpolator.EASE_BOTH ) );
		this.timeline.getKeyFrames().addAll( kf0, kf1 );

		indicator.statementStatusesProperty().addListener( new ListChangeListener<StatementStatusIndicator>() {
			@Override
			public void onChanged( ListChangeListener.Change<? extends StatementStatusIndicator> c ) {
				updateLock.acquireUninterruptibly();
				{
					PuzzleStatusIndicatorSkin.this.hbox.getChildren().clear();

					java.util.List<StatementStatusIndicator> incorrectStatuses = new java.util.LinkedList<StatementStatusIndicator>();
					java.util.List<StatementStatusIndicator> unknownStatuses = new java.util.LinkedList<StatementStatusIndicator>();
					for( StatementStatusIndicator statementStatus : c.getList() ) {
						statementStatus.setSyncExecutionPulse( executionPulse );

						switch( statementStatus.getStatus() ) {
						case CORRECT:
							PuzzleStatusIndicatorSkin.this.hbox.getChildren().add( statementStatus );
							break;
						case INCORRECT:
							incorrectStatuses.add( statementStatus );
							break;
						case UNKNOWN:
							unknownStatuses.add( statementStatus );
							break;
						}
					}

					for( StatementStatusIndicator statementStatus : incorrectStatuses ) {
						PuzzleStatusIndicatorSkin.this.hbox.getChildren().add( statementStatus );
					}
					for( StatementStatusIndicator statementStatus : unknownStatuses ) {
						PuzzleStatusIndicatorSkin.this.hbox.getChildren().add( statementStatus );
					}
				}
				updateLock.release();
			}
		} );
	}

	/*package-private*/void setActive( boolean isActive ) {
		if( isActive ) {
			this.timeline.play();
		} else {
			this.timeline.stop();
		}
	}
}
