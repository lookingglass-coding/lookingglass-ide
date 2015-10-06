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

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.util.Duration;
import uk.co.caprica.vlcj.player.MediaPlayer;

/**
 * Designed after this issue: https://github.com/caprica/vlcj-javafx/issues/3
 *
 * @author Kyle J. Harms
 */
public class VLCPlayer extends javafx.scene.canvas.Canvas {

	/*package-private*/class FxMediaPlayerComponent extends uk.co.caprica.vlcj.component.DirectMediaPlayerComponent {
		public FxMediaPlayerComponent( javafx.scene.canvas.Canvas canvas ) {
			super( new FxBufferFormatCallback( canvas ) );
		}
	}

	/*package-private*/class FxBufferFormatCallback implements uk.co.caprica.vlcj.player.direct.BufferFormatCallback {
		private final javafx.scene.canvas.Canvas canvas;

		public FxBufferFormatCallback( javafx.scene.canvas.Canvas canvas ) {
			this.canvas = canvas;
		}

		@Override
		public uk.co.caprica.vlcj.player.direct.BufferFormat getBufferFormat( final int sourceWidth, final int sourceHeight ) {
			edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( ( ) -> {
				this.canvas.setWidth( sourceWidth );
				this.canvas.setHeight( sourceHeight );
				this.canvas.getParent().requestLayout();
			} );
			return new uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat( sourceWidth, sourceHeight );
		}
	}

	public static String toVLCPath( String url ) {
		try {
			return toVLCPath( new java.net.URL( url ) );
		} catch( MalformedURLException e ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( e, url );
			return null;
		}
	}

	public static String toVLCPath( java.net.URL url ) {
		java.net.URI uri;
		try {
			uri = url.toURI();
		} catch( URISyntaxException e ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( e, url );
			return null;
		}

		String scheme = uri.getScheme();
		if( scheme.equalsIgnoreCase( "file" ) ) {
			java.io.File file = new java.io.File( uri );
			return file.getAbsolutePath();
		} else {
			return uri.toString();
		}
	}

	private final FxMediaPlayerComponent mediaPlayerComponent;

	private final PixelWriter pixelWriter;
	private final WritablePixelFormat<java.nio.ByteBuffer> pixelFormat;

	private Timeline renderTimeline;

	private static final double DEFAULT_FRAME_RATE = 1000.0 / 60.0; // 60 fps

	public VLCPlayer() {
		super();

		edu.cmu.cs.dennisc.video.vlcj.VlcjUtilities.initializeIfNecessary();

		this.mediaPlayerComponent = new FxMediaPlayerComponent( this );

		this.pixelWriter = this.getGraphicsContext2D().getPixelWriter();
		this.pixelFormat = PixelFormat.getByteBgraInstance();

		this.renderTimeline = new Timeline();
		this.renderTimeline.setCycleCount( Timeline.INDEFINITE );

		this.mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener( new uk.co.caprica.vlcj.player.MediaPlayerEventListener() {
			@Override
			public void mediaChanged( MediaPlayer mediaPlayer, uk.co.caprica.vlcj.binding.internal.libvlc_media_t media, String mrl ) {
			}

			@Override
			public void opening( MediaPlayer mediaPlayer ) {
			}

			@Override
			public void buffering( MediaPlayer mediaPlayer, float newCache ) {
			}

			@Override
			public void playing( MediaPlayer mediaPlayer ) {
				VLCPlayer.this.adjustFrameRate();
				VLCPlayer.this.startRendering();
			}

			@Override
			public void paused( MediaPlayer mediaPlayer ) {
				edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( ( ) -> VLCPlayer.this.renderFrame() );
				VLCPlayer.this.stopRendering();
			}

			@Override
			public void stopped( MediaPlayer mediaPlayer ) {
				edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( ( ) -> VLCPlayer.this.renderFrame() );
				VLCPlayer.this.stopRendering();
			}

			@Override
			public void forward( MediaPlayer mediaPlayer ) {
			}

			@Override
			public void backward( MediaPlayer mediaPlayer ) {
			}

			@Override
			public void finished( MediaPlayer mediaPlayer ) {
				edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( ( ) -> VLCPlayer.this.renderFrame() );
				VLCPlayer.this.stopRendering();
			}

			@Override
			public void timeChanged( MediaPlayer mediaPlayer, long newTime ) {
			}

			@Override
			public void positionChanged( MediaPlayer mediaPlayer, float newPosition ) {
			}

			@Override
			public void seekableChanged( MediaPlayer mediaPlayer, int newSeekable ) {
			}

			@Override
			public void pausableChanged( MediaPlayer mediaPlayer, int newPausable ) {
			}

			@Override
			public void titleChanged( MediaPlayer mediaPlayer, int newTitle ) {
			}

			@Override
			public void snapshotTaken( MediaPlayer mediaPlayer, String filename ) {
			}

			@Override
			public void lengthChanged( MediaPlayer mediaPlayer, long newLength ) {
			}

			@Override
			public void videoOutput( MediaPlayer mediaPlayer, int newCount ) {
			}

			@Override
			public void scrambledChanged( MediaPlayer mediaPlayer, int newScrambled ) {
			}

			@Override
			public void elementaryStreamAdded( MediaPlayer mediaPlayer, int type, int id ) {
			}

			@Override
			public void elementaryStreamDeleted( MediaPlayer mediaPlayer, int type, int id ) {
			}

			@Override
			public void elementaryStreamSelected( MediaPlayer mediaPlayer, int type, int id ) {
			}

			@Override
			public void error( MediaPlayer mediaPlayer ) {
			}

			@Override
			public void mediaMetaChanged( MediaPlayer mediaPlayer, int metaType ) {
			}

			@Override
			public void mediaSubItemAdded( MediaPlayer mediaPlayer, uk.co.caprica.vlcj.binding.internal.libvlc_media_t subItem ) {
			}

			@Override
			public void mediaDurationChanged( MediaPlayer mediaPlayer, long newDuration ) {
			}

			@Override
			public void mediaParsedChanged( MediaPlayer mediaPlayer, int newStatus ) {
			}

			@Override
			public void mediaFreed( MediaPlayer mediaPlayer ) {
			}

			@Override
			public void mediaStateChanged( MediaPlayer mediaPlayer, int newState ) {
			}

			@Override
			public void newMedia( MediaPlayer mediaPlayer ) {
			}

			@Override
			public void subItemPlayed( MediaPlayer mediaPlayer, int subItemIndex ) {
			}

			@Override
			public void subItemFinished( MediaPlayer mediaPlayer, int subItemIndex ) {
			}

			@Override
			public void endOfSubItems( MediaPlayer mediaPlayer ) {
			}
		} );
	}

	@Override
	protected void finalize() throws Throwable {
		this.release();
	};

	public void release() {
		this.stopRendering();
		this.mediaPlayerComponent.release();
	}

	/*
	 * Note: Based on the way I coded this... you should not
	 * call play or playMedia, only start or startMedia.
	 */
	public uk.co.caprica.vlcj.player.MediaPlayer getMediaPlayer() {
		return this.mediaPlayerComponent.getMediaPlayer();
	}

	private void renderFrame() {
		assert javafx.application.Platform.isFxApplicationThread();

		if( ( this.getWidth() > 0 ) && ( this.getHeight() > 0 ) ) {
			com.sun.jna.Memory[] nativeBuffers = this.mediaPlayerComponent.getMediaPlayer().lock();
			try {
				if( nativeBuffers != null ) {
					com.sun.jna.Memory nativeBuffer = nativeBuffers[ 0 ];
					if( nativeBuffer != null ) {
						java.nio.ByteBuffer byteBuffer = nativeBuffer.getByteBuffer( 0, nativeBuffer.size() );
						uk.co.caprica.vlcj.player.direct.BufferFormat bufferFormat = ( (uk.co.caprica.vlcj.player.direct.DefaultDirectMediaPlayer)this.mediaPlayerComponent.getMediaPlayer() ).getBufferFormat();
						if( ( bufferFormat.getWidth() > 0 ) && ( bufferFormat.getHeight() > 0 ) ) {
							this.pixelWriter.setPixels( 0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), this.pixelFormat, byteBuffer, bufferFormat.getPitches()[ 0 ] );
						}
					}
				}
			} finally {
				mediaPlayerComponent.getMediaPlayer().unlock();
			}
		}
	}

	private void renderFrameEvent( ActionEvent event ) {
		this.renderFrame();
	}

	private void startRendering() {
		this.renderTimeline.playFromStart();
	}

	private void stopRendering() {
		this.renderTimeline.stop();
	}

	private void adjustFrameRate() {
		try {
			java.util.List<uk.co.caprica.vlcj.player.TrackInfo> trackInfos = this.mediaPlayerComponent.getMediaPlayer().getTrackInfo();

			double frameRate = DEFAULT_FRAME_RATE;
			if( ( trackInfos.size() > 0 ) && ( trackInfos.get( 0 ) instanceof uk.co.caprica.vlcj.player.VideoTrackInfo ) ) {
				frameRate = ( (uk.co.caprica.vlcj.player.VideoTrackInfo)trackInfos.get( 0 ) ).frameRate();
			}
			frameRate = Math.min( frameRate, DEFAULT_FRAME_RATE );
			changeRenderFrameRate( frameRate );
		} catch( Throwable t ) {
			new RuntimeException( t );
		}
	}

	private void changeRenderFrameRate( double frameRate ) {
		this.stopRendering();
		this.renderTimeline.getKeyFrames().clear();
		this.renderTimeline.getKeyFrames().add( new KeyFrame( Duration.millis( frameRate ), VLCPlayer.this::renderFrameEvent ) );
	}
}
