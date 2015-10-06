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

package edu.wustl.lookingglass.croquetfx.scene;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Example showing how to render video to a JavaFX Canvas component.
 * <p>
 * The target is to render full HD video (1920x1080) at a reasonable frame rate
 * (>25fps).
 * <p>
 * This test can render the video at a fixed size, or it can take the size from
 * the video itself.
 * <p>
 * You may need to set -Djna.library.path=[path-to-libvlc] on the command-line.
 * <p>
 * Originally based on an example contributed by John Hendrikx.
 * <p>
 * -Dprism.verbose=true -Xmx512m -verbose:gc
 * <p>
 * This version works with JavaFX on JDK 1.8, without "wrong thread" errors.
 */
public class FxVLCPlayerTest extends Application {

	private static final String VIDEO_FILE = "/home/harmsk/Videos/formative.mp4";

	private final VLCPlayer vlcPlayer;
	private final BorderPane borderPane;
	private Stage stage;
	private Scene scene;

	public FxVLCPlayerTest() {
		vlcPlayer = new VLCPlayer();

		borderPane = new BorderPane();
		borderPane.setCenter( vlcPlayer );
	}

	@Override
	public final void start( Stage primaryStage ) throws Exception {
		this.stage = primaryStage;
		stage.setTitle( "vlcj JavaFX Direct Rendering Test" );
		scene = new Scene( borderPane );
		primaryStage.setScene( scene );
		primaryStage.show();

		this.vlcPlayer.getMediaPlayer().startMedia( VIDEO_FILE );
	}

	@Override
	public final void stop() throws Exception {
	}

	public static void main( final String[] args ) {
		Application.launch( args );
	}
}
