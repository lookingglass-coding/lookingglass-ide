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
package edu.wustl.lookingglass.remix.share;

import org.alice.media.video.WebmRecordingAdapter;
import org.alice.stageide.program.VideoEncodingProgramContext;

import edu.cmu.cs.dennisc.animation.FrameObserver;

/**
 * @author Michael Pogran
 */
public class RecordWorldObserver implements FrameObserver {
	private java.awt.image.BufferedImage image = null;
	private int imageCount = 0;

	private final VideoEncodingProgramContext programContext;
	private final WebmRecordingAdapter encoder;

	RecordWorldObserver( VideoEncodingProgramContext programContext, WebmRecordingAdapter encoder ) {
		this.programContext = programContext;
		this.encoder = encoder;
	}

	public int getImageCount() {
		return this.imageCount;
	}

	private void handleImage( java.awt.image.BufferedImage image, int imageCount ) {
		if( image != null ) {
			encoder.addBufferedImage( image );
		}
	}

	@Override
	public void update( double tCurrent ) {
		edu.cmu.cs.dennisc.render.OnscreenRenderTarget<?> renderTarget = programContext.getProgramImp().getOnscreenRenderTarget();
		if( renderTarget instanceof edu.cmu.cs.dennisc.render.gl.GlrCaptureFauxOnscreenRenderTarget ) {
			edu.cmu.cs.dennisc.render.gl.GlrCaptureFauxOnscreenRenderTarget captureLookingGlass = (edu.cmu.cs.dennisc.render.gl.GlrCaptureFauxOnscreenRenderTarget)renderTarget;
			captureLookingGlass.captureImage( new edu.cmu.cs.dennisc.render.gl.GlrCaptureFauxOnscreenRenderTarget.Observer() {
				@Override
				public void handleImage( java.awt.image.BufferedImage image, boolean isUpSideDown ) {
					if( image != null ) {
						if( isUpSideDown ) {
							RecordWorldObserver.this.handleImage( image, imageCount );
							imageCount++;
						}
					}
				}
			} );
		} else {
			if( ( renderTarget.getSurfaceWidth() > 0 ) && ( renderTarget.getSurfaceHeight() > 0 ) ) {
				if( image != null ) {
					//pass
				} else {
					image = renderTarget.getSynchronousImageCapturer().createBufferedImageForUseAsColorBuffer();
				}
				if( image != null ) {
					boolean[] atIsUpsideDown = { false };
					synchronized( image ) {
						image = renderTarget.getSynchronousImageCapturer().getColorBufferNotBotheringToFlipVertically( image, atIsUpsideDown );
						if( atIsUpsideDown[ 0 ] ) {
							handleImage( image, imageCount );
						} else {
							System.out.println( "SEVERE: IMAGE IS NOT UPSIDE DOWN" );
						}
					}
					imageCount++;
				} else {
					edu.cmu.cs.dennisc.java.util.logging.Logger.severe( "image is null" );
				}
			} else {
				edu.cmu.cs.dennisc.java.util.logging.Logger.severe( "width:", renderTarget.getSurfaceWidth(), "height:", renderTarget.getSurfaceHeight() );
			}
		}
	}

	@Override
	public void complete() {
	}

}
