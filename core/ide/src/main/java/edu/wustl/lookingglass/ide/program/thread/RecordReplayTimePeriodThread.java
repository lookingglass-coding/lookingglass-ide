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
package edu.wustl.lookingglass.ide.program.thread;

import org.lgna.common.ComponentThread;

import edu.wustl.lookingglass.ide.program.ReplayableProgramImp;
import edu.wustl.lookingglass.media.ImagesToWebmEncoder;
import edu.wustl.lookingglass.remix.share.VideoEncodeHelper;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;

/**
 * This replays what ever state transformations occurred in a specific thread
 * context over a specified time period. Literally thread independent replay.
 *
 * @author Paul Gross
 */

public class RecordReplayTimePeriodThread extends ReplayTimePeriodThread {
	private final edu.wustl.lookingglass.media.ImagesToWebmEncoder encoder;
	private double frameRate = 24.0;
	private final java.util.Set<RecordingCompletedListener> listeners = edu.cmu.cs.dennisc.java.util.Sets.newHashSet();

	public void addListener( RecordingCompletedListener listener ) {
		this.listeners.add( listener );
	}

	public void removeListener( RecordingCompletedListener listener ) {
		this.listeners.remove( listener );
	}

	public RecordReplayTimePeriodThread( ReplayableProgramImp program, double startTime, double endTime, ComponentThread replayThreadContext, AbstractEventNode replayNode, ImagesToWebmEncoder encoder ) {
		super( program, startTime, endTime, replayThreadContext, replayNode );
		this.encoder = encoder;
	}

	public double getFrameRate() {
		return this.frameRate;
	}

	public void setFrameRate( double frameRate ) {
		this.frameRate = frameRate;
	}

	public java.awt.Dimension getRecordingSize() {
		return new java.awt.Dimension( 640, 480 );
	}

	@Override
	public void preRun() {
		super.preRun();
		encoder.start();
	}

	@Override
	public void postRun() {
		// The videos turn off so fast you don't get to see the end of the animation. Add a little buffer...
		VideoEncodeHelper.appendEndFrameToVideo( 0.5, encoder );

		encoder.stop();
		for( RecordingCompletedListener listener : this.listeners ) {
			listener.notifyCompletion();
		}
		super.postRun();
	}

	@Override
	protected void executeReplayLoop() {
		double currentTime = startTime;
		int frameCount = 0;
		java.awt.image.BufferedImage image = null;

		// While we haven't gotten really close to the end time, and no one has told us to stop replaying
		while( !edu.cmu.cs.dennisc.math.EpsilonUtilities.isWithinReasonableEpsilonOf0InSquaredSpace( Math.abs( endTime - currentTime ) ) && isReplaying() ) {

			// current time is the starting time + whatever time has passed in our frame
			currentTime = startTime + ( ( (double)frameCount ) / getFrameRate() );

			// If we surpass the endtime, reset to the end time. We'll leave the loop after this iteration.
			if( currentTime > endTime ) {
				currentTime = endTime;
			}

			// If no one told us to stop replaying, then update the listeners
			if( isReplaying() ) {
				this.program.setCurrentTime( currentTime );

				if( this.encoder.isRunning() ) {
					edu.cmu.cs.dennisc.render.OnscreenRenderTarget<?> renderTarget = this.program.getOnscreenRenderTarget();
					java.awt.Dimension surfaceSize = renderTarget.getSurfaceSize();
					if( ( surfaceSize.width > 0 ) && ( surfaceSize.height > 0 ) ) {
						if( image != null ) {
							//pass
						} else {
							image = renderTarget.getSynchronousImageCapturer().createBufferedImageForUseAsColorBuffer();
						}

						if( image != null ) {
							boolean[] atIsUpsideDown = { false };
							synchronized( image ) {
								image = renderTarget.getSynchronousImageCapturer().getColorBufferNotBotheringToFlipVertically( image, atIsUpsideDown );
								this.encoder.addBufferedImage( image );
							}
							frameCount++;
						} else {
							edu.cmu.cs.dennisc.java.util.logging.Logger.severe( "image is null" );
						}
					} else {
						edu.cmu.cs.dennisc.java.util.logging.Logger.severe( "width:", surfaceSize.width, "height:", surfaceSize.height );
					}
				}
			}
		}
	}

	public interface RecordingCompletedListener {
		public abstract void notifyCompletion();
	}
}
