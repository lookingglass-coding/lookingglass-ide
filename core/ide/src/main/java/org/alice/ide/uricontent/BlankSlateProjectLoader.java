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
package org.alice.ide.uricontent;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.alice.nonfree.NebulousIde;
import org.alice.stageide.openprojectpane.components.TemplatesTabContentPane;

/**
 * @author Dennis Cosgrove
 */
public class BlankSlateProjectLoader extends UriProjectLoader {
	private final org.alice.stageide.openprojectpane.models.TemplateUriState.Template template;

	public BlankSlateProjectLoader( org.alice.stageide.openprojectpane.models.TemplateUriState.Template template ) {
		this.template = template;
	}

	@Override
	protected boolean isCacheAndCopyStyle() {
		return false;
	}

	@Override
	public java.net.URI getUri() {
		return this.template.getUri();
	}

	@Override
	protected org.lgna.project.Project load() {
		org.lgna.project.ast.NamedUserType programType;
		if( template.isRoom() ) {
			programType = NebulousIde.nonfree.createProgramType( this.template );
		} else {
			programType = org.alice.stageide.ast.BootstrapUtilties.createProgramType( template.getSurfaceAppearance(), template.getAtmospherColor(), template.getFogDensity(), template.getAboveLightColor(), template.getBelowLightColor(), template.getGroundOpacity() );
		}
		// <lg/> check to make sure the community meta data is correct.
		org.lgna.project.Project project = new org.lgna.project.Project( programType );
		edu.wustl.lookingglass.community.CommunityProjectPropertyManager.validateCommunityMetadata( project );
		return project;
	}

	// content info methods
	@Override
	public String getTitle() {
		return "New Scene: " + getUri().getFragment();
	}

	@Override
	public String getDescription() {
		return "Create a new project by adding your own characters and props to this scene.";
	}

	@Override
	protected Image loadThumbnail() {
		BufferedImage thumbnail = null;
		try {
			thumbnail = ImageIO.read( TemplatesTabContentPane.class.getResource( "images/" + this.getUri().getFragment() + ".png" ) );
		} catch( IOException e ) {
			e.printStackTrace();
		}

		return thumbnail;
	}

}
