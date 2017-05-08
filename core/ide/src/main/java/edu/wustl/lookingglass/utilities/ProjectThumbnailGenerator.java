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
package edu.wustl.lookingglass.utilities;

import com.jogamp.opengl.GLException;

import edu.cmu.cs.dennisc.java.util.logging.Logger;

/**
 * @author Michael Pogran
 */
public class ProjectThumbnailGenerator {

	public static java.awt.image.BufferedImage createThumbnail( int width, int height, org.lgna.project.Project project ) {
		org.alice.stageide.program.ProgramContext context = new org.alice.stageide.program.RunProgramContext( project.getProgramType() );
		context.setActiveSceneOnComponentThreadAndWait();
		java.awt.image.BufferedImage rv = createThumbnail( width, height, context );
		context.cleanUpProgram();
		return rv;
	}

	public static java.awt.image.BufferedImage createThumbnail( int width, int height, org.alice.stageide.program.ProgramContext context ) {
		edu.cmu.cs.dennisc.render.OffscreenRenderTarget offscreenRenderTarget = edu.cmu.cs.dennisc.render.gl.GlrRenderFactory.getInstance().createOffscreenRenderTarget( width, height, context.getOnscreenRenderTarget(), new edu.cmu.cs.dennisc.render.RenderCapabilities.Builder().build() );
		edu.cmu.cs.dennisc.scenegraph.AbstractCamera camera = getCamera( context.getSceneInstance() );

		boolean isClearingAndAddingRequired;
		if( offscreenRenderTarget.getSgCameraCount() == 1 ) {
			if( offscreenRenderTarget.getSgCameraAt( 0 ) == camera ) {
				isClearingAndAddingRequired = false;
			} else {
				isClearingAndAddingRequired = true;
			}
		} else {
			isClearingAndAddingRequired = true;
		}
		try {
			if( isClearingAndAddingRequired ) {
				offscreenRenderTarget.clearSgCameras();
				offscreenRenderTarget.addSgCamera( camera );
			}
		} catch( GLException e ) {
			// If this doesn't work out... that's probably ok.
			Logger.throwable( e );
		}

		try {
			java.awt.image.BufferedImage thumbImage = offscreenRenderTarget.getSynchronousImageCapturer().getColorBuffer();
			edu.cmu.cs.dennisc.render.gl.GlrRenderFactory.getInstance().removeOffScreenLookingGlass( offscreenRenderTarget );
			return thumbImage;
		} catch( GLException e ) {
			// If this doesn't work out... that's probably ok.
			Logger.throwable( e );
			return null;
		}
	}

	private static edu.cmu.cs.dennisc.scenegraph.AbstractCamera getCamera( org.lgna.project.virtualmachine.UserInstance sceneInstance ) {
		org.lgna.story.implementation.SymmetricPerspectiveCameraImp cameraImp = null;
		for( org.lgna.project.ast.UserField field : sceneInstance.getType().getDeclaredFields() ) {

			if( field.getValueType().isAssignableTo( org.lgna.story.SCamera.class ) ) {
				org.lgna.story.SThing entity = edu.cmu.cs.dennisc.java.lang.ClassUtilities.getInstance( sceneInstance.getFieldValueInstanceInJava( field ), org.lgna.story.SThing.class );
				if( entity != null ) {
					cameraImp = org.lgna.story.EmployeesOnly.getImplementation( entity );
				}
				break;
			}
		}
		if( cameraImp != null ) {
			return cameraImp.getSgCamera();
		} else {
			return null;
		}
	}
}
