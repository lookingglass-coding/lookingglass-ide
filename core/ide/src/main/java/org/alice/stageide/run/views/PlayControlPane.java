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
package org.alice.stageide.run.views;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import org.alice.stageide.run.RunComposite;
import org.lgna.croquet.icon.IconSize;

import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.ide.LookingGlassTheme;

/**
 * @author Kyle J. Harms
 */
public class PlayControlPane extends FxComponent {

	@FXML private Button play;
	@FXML private ToggleButton fastForward;
	@FXML private Button fullScreen;
	@FXML private ImageView feedback;

	private boolean isFullScreen = false;
	private final RunComposite runComposite;

	public PlayControlPane( RunComposite runComposite ) {
		super( PlayControlPane.class );
		this.runComposite = runComposite;

		this.play.setGraphic( LookingGlassTheme.getFxImageView( "media-playback-start", IconSize.SMALL ) );
		this.fastForward.setGraphic( LookingGlassTheme.getFxImageView( "media-seek-forward", IconSize.SMALL ) );
		this.feedback.setImage( LookingGlassTheme.getFxImage( "media-seek-forward", IconSize.MEDIUM ) );

		this.setFullScreen( false );

		this.register( this.play, this::handlePlayAction );
		this.register( this.fullScreen, this::handleFullScreenAction );

		this.register( this.fastForward, this::handleFastForwardAction );
		this.fastForward.setOnMousePressed( this::handleFastForwardPressed );
		this.fastForward.setOnMouseReleased( this::handleFastForwardReleased );
	}

	private void setFullScreen( boolean shouldFullScreen ) {
		this.isFullScreen = shouldFullScreen;
		if( this.isFullScreen ) {
			this.fullScreen.setText( this.getLocalizedString( "exitFullScreen" ) );
			this.fullScreen.setGraphic( LookingGlassTheme.getFxImageView( "view-restore", IconSize.SMALL ) );
		} else {
			this.fullScreen.setText( this.getLocalizedString( "fullScreen" ) );
			this.fullScreen.setGraphic( LookingGlassTheme.getFxImageView( "view-fullscreen", IconSize.SMALL ) );
		}
	}

	public void handlePlayAction( ActionEvent event ) {
		this.runComposite.restart();
	}

	public void handleFullScreenAction( ActionEvent event ) {
		this.setFullScreen( !this.isFullScreen );
		this.runComposite.toggleFullScreen( isFullScreen );
	}

	private void handleFastForwardAction( Boolean oldValue, Boolean newValue ) {
		this.runComposite.toggleFastForward( newValue );
	}

	private void handleFastForwardPressed( MouseEvent event ) {
		this.feedback.setVisible( true );
		FadeTransition fade = new FadeTransition( Duration.millis( 200 ), this.feedback );
		fade.setFromValue( 0.0 );
		fade.setToValue( 1.0 );
		fade.play();

		this.fastForward.setSelected( true );
	}

	private void handleFastForwardReleased( MouseEvent event ) {
		this.feedback.setVisible( false );
		this.fastForward.setSelected( false );
	}

}
