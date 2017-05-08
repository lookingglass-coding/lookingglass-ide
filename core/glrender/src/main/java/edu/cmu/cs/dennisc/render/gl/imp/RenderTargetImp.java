/*******************************************************************************
 * Copyright (c) 2006, 2015, Carnegie Mellon University. All rights reserved.
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
 * 3. Products derived from the software may not be called "Alice", nor may
 *    "Alice" appear in their name, without prior written permission of
 *    Carnegie Mellon University.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Carnegie Mellon University"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 * ANY AND ALL EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT
 * SHALL THE AUTHORS, COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package edu.cmu.cs.dennisc.render.gl.imp;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;

import edu.cmu.cs.dennisc.render.gl.GlDrawableUtils;
import edu.cmu.cs.dennisc.render.gl.imp.adapters.AdapterFactory;
import edu.cmu.cs.dennisc.render.gl.imp.adapters.GlrAbstractCamera;
import edu.cmu.cs.dennisc.system.graphics.ConformanceTestResults;

/**
 * @author Dennis Cosgrove
 */
public class RenderTargetImp {
	public RenderTargetImp( edu.cmu.cs.dennisc.render.RenderTarget renderTarget ) {
		this.renderTarget = renderTarget;
		this.reusableLookingGlassRenderEvent = new ReusableLookingGlassRenderEvent( this.getRenderTarget(), new Graphics2D( this.renderContext ) );
	}

	public edu.cmu.cs.dennisc.render.RenderTarget getRenderTarget() {
		return this.renderTarget;
	}

	public edu.cmu.cs.dennisc.render.SynchronousPicker getSynchronousPicker() {
		return this.synchronousPicker;
	}

	public edu.cmu.cs.dennisc.render.SynchronousImageCapturer getSynchronousImageCapturer() {
		return this.synchronousImageCapturer;
	}

	public edu.cmu.cs.dennisc.render.AsynchronousPicker getAsynchronousPicker() {
		return this.asynchronousPicker;
	}

	public edu.cmu.cs.dennisc.render.AsynchronousImageCapturer getAsynchronousImageCapturer() {
		return this.asynchronousImageCapturer;
	}

	public void addRenderTargetListener( edu.cmu.cs.dennisc.render.event.RenderTargetListener listener ) {
		this.renderTargetListeners.add( listener );
	}

	public void removeRenderTargetListener( edu.cmu.cs.dennisc.render.event.RenderTargetListener listener ) {
		this.renderTargetListeners.remove( listener );
	}

	public java.util.List<edu.cmu.cs.dennisc.render.event.RenderTargetListener> getRenderTargetListeners() {
		return java.util.Collections.unmodifiableList( this.renderTargetListeners );
	}

	public void addSgCamera( edu.cmu.cs.dennisc.scenegraph.AbstractCamera sgCamera, com.jogamp.opengl.GLAutoDrawable glAutoDrawable ) {
		assert sgCamera != null : this;
		this.sgCameras.add( sgCamera );
		if( this.isListening() ) {
			//pass
		} else {
			this.startListening( glAutoDrawable );
		}
	}

	public void removeSgCamera( edu.cmu.cs.dennisc.scenegraph.AbstractCamera sgCamera, com.jogamp.opengl.GLAutoDrawable glAutoDrawable ) {
		assert sgCamera != null;
		this.sgCameras.remove( sgCamera );
		if( this.isListening() ) {
			if( this.sgCameras.isEmpty() ) {
				this.stopListening( glAutoDrawable );
			}
		}
	}

	public void clearSgCameras( com.jogamp.opengl.GLAutoDrawable glAutoDrawable ) {
		if( this.sgCameras.size() > 0 ) {
			this.sgCameras.clear();
		}
		if( this.isListening() ) {
			this.stopListening( glAutoDrawable );
		}
	}

	public int getSgCameraCount() {
		return this.sgCameras.size();
	}

	public edu.cmu.cs.dennisc.scenegraph.AbstractCamera getSgCameraAt( int index ) {
		return this.sgCameras.get( index );
	}

	public java.util.List<edu.cmu.cs.dennisc.scenegraph.AbstractCamera> getSgCameras() {
		return java.util.Collections.unmodifiableList( this.sgCameras );
	}

	public edu.cmu.cs.dennisc.scenegraph.AbstractCamera getCameraAtPixel( int xPixel, int yPixel ) {
		java.util.ListIterator<edu.cmu.cs.dennisc.scenegraph.AbstractCamera> iterator = this.sgCameras.listIterator( this.sgCameras.size() );
		while( iterator.hasPrevious() ) {
			edu.cmu.cs.dennisc.scenegraph.AbstractCamera sgCamera = iterator.previous();
			synchronized( s_actualViewportBufferForReuse ) {
				this.renderTarget.getActualViewportAsAwtRectangle( s_actualViewportBufferForReuse, sgCamera );
				if( s_actualViewportBufferForReuse.contains( xPixel, yPixel ) ) {
					return sgCamera;
				}
			}
		}
		return null;
	}

	public void forgetAllCachedItems() {
		if( this.renderContext != null ) {
			this.renderContext.forgetAllCachedItems();
		}
	}

	public void clearUnusedTextures() {
		if( this.renderContext != null ) {
			this.renderContext.clearUnusedTextures();
		}
	}

	/*package-private*/void addDisplayTask( DisplayTask displayTask ) {
		displayTask.setRtImp( this );
		this.drawable.invoke( false, displayTask );
	}

	private void fireInitialized( edu.cmu.cs.dennisc.render.event.RenderTargetInitializeEvent e ) {
		for( edu.cmu.cs.dennisc.render.event.RenderTargetListener rtListener : this.renderTargetListeners ) {
			rtListener.initialized( e );
		}
	}

	private void fireCleared( edu.cmu.cs.dennisc.render.event.RenderTargetRenderEvent e ) {
		for( edu.cmu.cs.dennisc.render.event.RenderTargetListener rtListener : this.renderTargetListeners ) {
			rtListener.cleared( e );
		}
	}

	private void fireRendered( edu.cmu.cs.dennisc.render.event.RenderTargetRenderEvent e ) {
		for( edu.cmu.cs.dennisc.render.event.RenderTargetListener rtListener : this.renderTargetListeners ) {
			rtListener.rendered( e );
		}
	}

	private void fireResized( edu.cmu.cs.dennisc.render.event.RenderTargetResizeEvent e ) {
		for( edu.cmu.cs.dennisc.render.event.RenderTargetListener rtListener : this.renderTargetListeners ) {
			rtListener.resized( e );
		}
	}

	//todo:
	private void fireDisplayChanged( edu.cmu.cs.dennisc.render.event.RenderTargetDisplayChangeEvent e ) {
		for( edu.cmu.cs.dennisc.render.event.RenderTargetListener rtListener : this.renderTargetListeners ) {
			rtListener.displayChanged( e );
		}
	}

	private static class ReusableLookingGlassRenderEvent extends edu.cmu.cs.dennisc.render.event.RenderTargetRenderEvent {
		public ReusableLookingGlassRenderEvent( edu.cmu.cs.dennisc.render.RenderTarget renderTarget, Graphics2D g ) {
			super( renderTarget, g );
		}

		@Override
		public boolean isReservedForReuse() {
			return true;
		}

		private void prologue() {
			( (Graphics2D)getGraphics2D() ).initialize( getTypedSource().getSurfaceSize() );
		}

		private void epilogue() {
			getGraphics2D().dispose();
		}
	}

	private boolean isListening;

	public boolean isListening() {
		return this.isListening;
	}

	public void startListening( com.jogamp.opengl.GLAutoDrawable drawable ) {
		if( this.isListening ) {
			if( drawable == this.drawable ) {
				//pass
			} else {
				edu.cmu.cs.dennisc.java.util.logging.Logger.severe( drawable, this.drawable );
			}
			edu.cmu.cs.dennisc.java.util.logging.Logger.warning( "request GLEventAdapter.startListening( drawable ) ignored; already listening." );
		} else {
			this.isListening = true;
			this.drawable = drawable;
			this.drawable.addGLEventListener( this.glEventListener );
		}
	}

	public void stopListening( com.jogamp.opengl.GLAutoDrawable drawable ) {
		if( drawable == this.drawable ) {
			//pass
		} else {
			edu.cmu.cs.dennisc.java.util.logging.Logger.severe( drawable, this.drawable );
		}
		if( this.isListening ) {
			this.isListening = false;
			drawable.removeGLEventListener( this.glEventListener );
		} else {
			edu.cmu.cs.dennisc.java.util.logging.Logger.warning( "request GLEventAdapter.stopListening( drawable ) ignored; already not listening." );
		}
		this.drawable = null;
	}

	//	private void paintOverlay() {
	//		edu.cmu.cs.dennisc.lookingglass.Overlay overlay = this.lookingGlass.getOverlay();
	//		if( overlay != null ) {
	//
	//			this.renderContext.gl.glMatrixMode( GL_PROJECTION );
	//			this.renderContext.gl.glPushMatrix();
	//			this.renderContext.gl.glLoadIdentity();
	//			this.renderContext.gl.glOrtho( 0, this.lookingGlass.getWidth() - 1, this.lookingGlass.getHeight() - 1, 0, -1, 1 );
	//			this.renderContext.gl.glMatrixMode( GL_MODELVIEW );
	//			this.renderContext.gl.glPushMatrix();
	//			this.renderContext.gl.glLoadIdentity();
	//
	//			this.renderContext.gl.glDisable( GL_DEPTH_TEST );
	//			this.renderContext.gl.glDisable( GL_LIGHTING );
	//			this.renderContext.gl.glDisable( GL_CULL_FACE );
	//			this.renderContext.setDiffuseColorTextureAdapter( null );
	//			this.renderContext.setBumpTextureAdapter( null );
	//
	//
	//			try {
	//				overlay.paint( this.lookingGlass );
	//				this.renderContext.gl.glFlush();
	//			} finally {
	//				this.renderContext.gl.glMatrixMode( GL_PROJECTION );
	//				this.renderContext.gl.glPopMatrix();
	//				this.renderContext.gl.glMatrixMode( GL_MODELVIEW );
	//				this.renderContext.gl.glPopMatrix();
	//			}
	//		}
	//	}

	private void performRender() {
		edu.cmu.cs.dennisc.render.RenderTarget rt = this.getRenderTarget();
		if( rt.isRenderingEnabled() ) {
			this.renderContext.actuallyForgetTexturesIfNecessary();
			this.renderContext.actuallyForgetDisplayListsIfNecessary();
			if( this.isDisplayIgnoredDueToPreviousException ) {
				//pass
			} else if( ( this.drawableWidth == 0 ) || ( this.drawableHeight == 0 ) ) {
				edu.cmu.cs.dennisc.java.util.logging.Logger.severe( this.drawableWidth, this.drawableHeight, rt.getSurfaceSize() );
			} else {
				try {
					//todo: separate clearing and rendering
					this.reusableLookingGlassRenderEvent.prologue();
					try {
						this.fireCleared( this.reusableLookingGlassRenderEvent );
					} finally {
						this.reusableLookingGlassRenderEvent.epilogue();
					}
					if( rt.getSgCameraCount() > 0 ) {
						this.renderContext.initialize();
						for( edu.cmu.cs.dennisc.scenegraph.AbstractCamera sgCamera : this.sgCameras ) {
							GlrAbstractCamera<? extends edu.cmu.cs.dennisc.scenegraph.AbstractCamera> cameraAdapterI = AdapterFactory.getAdapterFor( sgCamera );
							cameraAdapterI.performClearAndRenderOffscreen( this.renderContext, this.drawableWidth, this.drawableHeight );
							this.reusableLookingGlassRenderEvent.prologue();
							try {
								//Pass the screen size to post render because operations like speech bubbles use the screen size as a reference rather than the drawable size
								cameraAdapterI.postRender( this.renderContext, this.screenWidth, this.screenHeight, rt, this.reusableLookingGlassRenderEvent.getGraphics2D() );
							} finally {
								this.reusableLookingGlassRenderEvent.epilogue();
							}
						}
						this.renderContext.renderLetterboxingIfNecessary( this.drawableWidth, this.drawableHeight );
					} else {
						this.renderContext.gl.glClearColor( 0, 0, 0, 1 );
						this.renderContext.gl.glClear( GL_COLOR_BUFFER_BIT );
					}
					this.reusableLookingGlassRenderEvent.prologue();
					try {
						this.fireRendered( this.reusableLookingGlassRenderEvent );
					} finally {
						this.reusableLookingGlassRenderEvent.epilogue();
					}
					this.renderContext.gl.glFlush();
					if( ( this.rvColorBuffer != null ) || ( this.rvDepthBuffer != null ) ) {
						this.renderContext.captureBuffers( this.rvColorBuffer, this.rvDepthBuffer, this.atIsUpsideDown );
					}

				} catch( RuntimeException re ) {
					edu.cmu.cs.dennisc.java.util.logging.Logger.severe( "rendering will be disabled due to exception" );
					this.isDisplayIgnoredDueToPreviousException = true;
					re.printStackTrace();
					throw re;
				} catch( Error er ) {
					edu.cmu.cs.dennisc.java.util.logging.Logger.severe( "rendering will be disabled due to exception" );
					this.isDisplayIgnoredDueToPreviousException = true;
					er.printStackTrace();
					throw er;
				}
			}
		}
	}

	private java.awt.image.BufferedImage createBufferedImageForUseAsColorBuffer( int type ) {
		if( this.drawable != null ) {
			if( ( this.drawableWidth != GlDrawableUtils.getGlDrawableWidth( this.drawable ) ) || ( this.drawableHeight != GlDrawableUtils.getGlDrawableHeight( this.drawable ) ) ) {
				this.drawableWidth = GlDrawableUtils.getGlDrawableWidth( this.drawable );
				this.drawableHeight = GlDrawableUtils.getGlDrawableHeight( this.drawable );
				this.screenWidth = GlDrawableUtils.getGLJPanelWidth( drawable );
				this.screenHeight = GlDrawableUtils.getGLJPanelHeight( drawable );
			}
		} else {
			edu.cmu.cs.dennisc.print.PrintUtilities.println( "warning: drawable null" );
		}

		if( ( this.drawableWidth > 0 ) && ( this.drawableHeight > 0 ) ) {
			return new java.awt.image.BufferedImage( this.drawableWidth, this.drawableHeight, type );
		} else {
			return null;
		}
	}

	public java.awt.image.BufferedImage createBufferedImageForUseAsColorBuffer() {
		//		boolean isClearedToCreateImage;
		//		if( this.this.renderContext.gl != null ) {
		//			String extensions = this.this.renderContext.gl.glGetString( GL_EXTENSIONS );
		//			if( extensions != null ) {
		//				boolean isABGRExtensionSupported = extensions.contains( "GL_EXT_abgr" );
		//				if( isABGRExtensionSupported ) {
		//					//pass
		//				} else {
		//					edu.cmu.cs.dennisc.print.PrintUtilities.println( "createBufferedImageForUseAsColorBuffer: capturing images from gl is expected to fail since since GL_EXT_abgr not found in: " );
		//					edu.cmu.cs.dennisc.print.PrintUtilities.println( "\t" + extensions );
		//				}
		//				isClearedToCreateImage = isABGRExtensionSupported;
		//			} else {
		//				edu.cmu.cs.dennisc.print.PrintUtilities.println( "createBufferedImageForUseAsColorBuffer: capturing images from gl is expected to fail since since gl.glGetString( GL_EXTENSIONS ) returns null." );
		//				isClearedToCreateImage = false;
		//			}
		//		} else {
		//			edu.cmu.cs.dennisc.print.PrintUtilities.println( "createBufferedImageForUseAsColorBuffer: opengl is not initialized yet, so we will assume the GL_EXT_abgr extension is present." );
		//			isClearedToCreateImage = true;
		//		}
		//
		//
		//		//todo: investigate
		//		if( isClearedToCreateImage ) {
		//			//pass
		//		} else {
		//			isClearedToCreateImage = true;
		//		}
		//
		//		if( isClearedToCreateImage ) {
		//			//todo:
		//			//int type = java.awt.image.BufferedImage.TYPE_3BYTE_ABGR;
		//			int type = java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
		//			//int type = java.awt.image.BufferedImage.TYPE_INT_ARGB;
		//			return createBufferedImageForUseAsColorBuffer( type );
		//		} else {
		//			return null;
		//		}
		int type = java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
		return createBufferedImageForUseAsColorBuffer( type );
	}

	public java.awt.image.BufferedImage getColorBuffer( java.awt.image.BufferedImage rv, boolean[] atIsUpsideDown ) {
		return this.getColorBufferWithTransparencyBasedOnDepthBuffer( rv, null, atIsUpsideDown );
	}

	public java.awt.image.BufferedImage createBufferedImageForUseAsColorBufferWithTransparencyBasedOnDepthBuffer() {
		return createBufferedImageForUseAsColorBuffer( java.awt.image.BufferedImage.TYPE_4BYTE_ABGR );
	}

	public java.nio.FloatBuffer createFloatBufferForUseAsDepthBuffer() {
		return java.nio.FloatBuffer.allocate( this.drawableWidth * this.drawableHeight );
	}

	public java.nio.FloatBuffer getDepthBuffer( java.nio.FloatBuffer rv ) {
		this.rvDepthBuffer = rv;
		this.drawable.setAutoSwapBufferMode( false );
		try {
			this.drawable.display();
		} finally {
			this.rvDepthBuffer = null;
			this.drawable.setAutoSwapBufferMode( true );
		}
		return rv;
	}

	public java.awt.image.BufferedImage getColorBufferWithTransparencyBasedOnDepthBuffer( java.awt.image.BufferedImage rv, java.nio.FloatBuffer depthBuffer, boolean[] atIsUpsideDown ) {
		com.jogamp.opengl.GLContext glCurrentContext = com.jogamp.opengl.GLContext.getCurrent();
		if( ( glCurrentContext != null ) && ( glCurrentContext == this.drawable.getContext() ) ) {
			this.renderContext.captureBuffers( rv, depthBuffer, atIsUpsideDown );
		} else {
			if( this.rvColorBuffer != null ) {
				edu.cmu.cs.dennisc.java.util.logging.Logger.severe( this.rvColorBuffer );
			}
			this.rvColorBuffer = rv;
			this.rvDepthBuffer = depthBuffer;
			this.atIsUpsideDown = atIsUpsideDown;
			this.drawable.setAutoSwapBufferMode( false );
			try {
				this.drawable.display();
			} finally {
				this.rvColorBuffer = null;
				this.rvDepthBuffer = null;
				this.atIsUpsideDown = null;
				this.drawable.setAutoSwapBufferMode( true );
			}
		}
		return rv;
	}

	public int getDrawableWidth() {
		return this.drawableWidth;
	}

	public int getDrawableHeight() {
		return this.drawableHeight;
	}

	public int getScreenHeight() {
		return this.screenHeight;
	}

	public int getScreenWidth() {
		return this.screenWidth;
	}

	private void initialize( com.jogamp.opengl.GLAutoDrawable drawable ) {
		//edu.cmu.cs.dennisc.print.PrintUtilities.println( "initialize", drawable );
		assert drawable == this.drawable;
		com.jogamp.opengl.GL2 gl = drawable.getGL().getGL2();
		ConformanceTestResults.SINGLETON.updateRenderInformationIfNecessary( gl );

		//edu.cmu.cs.dennisc.print.PrintUtilities.println( drawable.getChosenGLCapabilities() );

		final boolean USE_DEBUG_GL = false;
		if( USE_DEBUG_GL ) {
			if( gl instanceof com.jogamp.opengl.DebugGL2 ) {
				// pass
			} else {
				gl = new com.jogamp.opengl.DebugGL2( gl );
				edu.cmu.cs.dennisc.java.util.logging.Logger.info( "using debug gl: ", gl );
				drawable.setGL( gl );
			}
		}

		this.drawableWidth = GlDrawableUtils.getGlDrawableWidth( drawable );
		this.drawableHeight = GlDrawableUtils.getGlDrawableHeight( drawable );
		this.screenWidth = GlDrawableUtils.getGLJPanelWidth( drawable );
		this.screenHeight = GlDrawableUtils.getGLJPanelHeight( drawable );

		this.renderContext.setGL( gl );
		this.fireInitialized( new edu.cmu.cs.dennisc.render.event.RenderTargetInitializeEvent( this.getRenderTarget(), GlDrawableUtils.getGlDrawableWidth( this.drawable ), GlDrawableUtils.getGlDrawableHeight( this.drawable ) ) );
	}

	//todo: investigate not being invoked
	private void handleInit( com.jogamp.opengl.GLAutoDrawable drawable ) {
		//edu.cmu.cs.dennisc.print.PrintUtilities.println( "init", drawable );
		initialize( drawable );
	}

	private void handleDisplay( com.jogamp.opengl.GLAutoDrawable drawable ) {
		//edu.cmu.cs.dennisc.print.PrintUtilities.println( "display:", drawable );
		assert drawable == this.drawable;
		//this.lookingGlass.commitAnyPendingChanges();
		//todo?
		com.jogamp.opengl.GL2 gl = drawable.getGL().getGL2();
		if( this.renderContext.gl != null ) {
			//pass
		} else {
			initialize( drawable );
		}
		if( ( this.drawableWidth > 0 ) && ( this.drawableHeight > 0 ) ) {
			//pass
		} else {
			int nextWidth = GlDrawableUtils.getGlDrawableWidth( drawable );
			int nextHeight = GlDrawableUtils.getGlDrawableHeight( drawable );
			int nextScreenWidth = GlDrawableUtils.getGLJPanelWidth( drawable );
			int nextScreenHeight = GlDrawableUtils.getGLJPanelHeight( drawable );
			if( ( this.drawableWidth != nextWidth ) || ( this.drawableHeight != nextHeight ) ) {
				edu.cmu.cs.dennisc.java.util.logging.Logger.severe( this.drawableWidth, this.drawableHeight, nextWidth, nextHeight );
				this.drawableWidth = nextWidth;
				this.drawableHeight = nextHeight;
				this.screenHeight = nextScreenHeight;
				this.screenWidth = nextScreenWidth;
			}
		}
		this.renderContext.setGL( gl );

		performRender();
	}

	private void handleReshape( com.jogamp.opengl.GLAutoDrawable drawable, int x, int y, int width, int height ) {
		//edu.cmu.cs.dennisc.print.PrintUtilities.println( "reshape", drawable, x, y, width, height );

		assert drawable == this.drawable;
		this.drawableWidth = width;
		this.drawableHeight = height;
		this.screenWidth = GlDrawableUtils.getGLJPanelWidth( drawable );
		this.screenHeight = GlDrawableUtils.getGLJPanelHeight( drawable );
		this.fireResized( new edu.cmu.cs.dennisc.render.event.RenderTargetResizeEvent( this.getRenderTarget(), width, height ) );
	}

	//	public void displayChanged( com.jogamp.opengl.GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged ) {
	//		//edu.cmu.cs.dennisc.print.PrintUtilities.println( "displayChanged", drawable, modeChanged, deviceChanged );
	//		assert drawable == this.drawable;
	//		this.rtImp.fireDisplayChanged( new edu.cmu.cs.dennisc.renderer.event.RenderTargetDisplayChangeEvent( this.rtImp.getRenderTarget(), modeChanged, deviceChanged ) );
	//	}

	private void handleDispose( com.jogamp.opengl.GLAutoDrawable drawable ) {
		edu.cmu.cs.dennisc.java.util.logging.Logger.todo( drawable );
	}

	private final RenderContext renderContext = new RenderContext();

	private com.jogamp.opengl.GLAutoDrawable drawable;

	//The drawable size and the screen size are not necessarily the same
	//This is known to be the case on retina displays where the drawable size is 2x the screen size
	//See https://jogamp.org/bugzilla/show_bug.cgi?id=741 for details
	private int drawableWidth;
	private int drawableHeight;
	private int screenWidth;
	private int screenHeight;

	private java.awt.image.BufferedImage rvColorBuffer = null;
	private java.nio.FloatBuffer rvDepthBuffer = null;
	private boolean[] atIsUpsideDown = null;

	private boolean isDisplayIgnoredDueToPreviousException = false;
	private final ReusableLookingGlassRenderEvent reusableLookingGlassRenderEvent;

	private final edu.cmu.cs.dennisc.render.RenderTarget renderTarget;

	private final SynchronousPicker synchronousPicker = new SynchronousPicker( this );
	private final SynchronousImageCapturer synchronousImageCapturer = new SynchronousImageCapturer( this );

	private final GlrAsynchronousPicker asynchronousPicker = new GlrAsynchronousPicker( this );
	private final GlrAsynchronousImageCapturer asynchronousImageCapturer = new GlrAsynchronousImageCapturer( this );

	private final java.util.List<edu.cmu.cs.dennisc.render.event.RenderTargetListener> renderTargetListeners = edu.cmu.cs.dennisc.java.util.Lists.newCopyOnWriteArrayList();

	private final java.util.List<edu.cmu.cs.dennisc.scenegraph.AbstractCamera> sgCameras = edu.cmu.cs.dennisc.java.util.Lists.newCopyOnWriteArrayList();

	//
	private static java.awt.Rectangle s_actualViewportBufferForReuse = new java.awt.Rectangle();
	private final com.jogamp.opengl.GLEventListener glEventListener = new com.jogamp.opengl.GLEventListener() {
		@Override
		public void init( com.jogamp.opengl.GLAutoDrawable drawable ) {
			handleInit( drawable );
		}

		@Override
		public void display( com.jogamp.opengl.GLAutoDrawable drawable ) {
			handleDisplay( drawable );
		}

		@Override
		public void reshape( com.jogamp.opengl.GLAutoDrawable drawable, int x, int y, int width, int height ) {
			handleReshape( drawable, x, y, width, height );
		}

		@Override
		public void dispose( com.jogamp.opengl.GLAutoDrawable drawable ) {
			handleDispose( drawable );
		}
	};
}
