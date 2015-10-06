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
package edu.wustl.lookingglass.ide.uricontent;

import java.awt.Image;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.alice.ide.uricontent.UriProjectLoader;
import org.lgna.project.Project;
import org.lgna.project.VersionNotSupportedException;

import edu.wustl.lookingglass.community.api.packets.UserPacket;
import edu.wustl.lookingglass.community.exceptions.CommunityApiException;
import edu.wustl.lookingglass.project.VersionExceedsCurrentException;

/**
 * @author Caitlin Kelleher
 */

public class CommunityProjectLoader extends UriProjectLoader {

	private final edu.wustl.lookingglass.community.api.packets.ProjectPacket projectPacket;
	private String username = null;

	public CommunityProjectLoader( edu.wustl.lookingglass.community.api.packets.ProjectPacket projectPacket ) {
		assert ( projectPacket != null );
		this.projectPacket = projectPacket;
		this.loadUsername();
	}

	@Override
	protected boolean isCacheAndCopyStyle() {
		return true;
	}

	public edu.wustl.lookingglass.community.api.packets.ProjectPacket getProjectPacket() {
		return projectPacket;
	}

	@Override
	public String getTitle() {
		return projectPacket.getTitle();
	}

	@Override
	public String getDescription() {
		return projectPacket.getDescription();
	}

	public String getUsername() {
		return this.username;
	}

	protected void loadUsername() {
		UserPacket userPack;
		try {
			userPack = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getUser( projectPacket.getUserId() );
			String name = userPack.getLogin();
			username = name;
		} catch( CommunityApiException e ) {
			e.printStackTrace();
		}
	}

	@Override
	protected Image loadThumbnail() {
		Image thumbnailImage = null;

		// ah yes, errors here too
		try {
			return edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().downloadPoster( projectPacket );
		} catch( CommunityApiException | IOException e ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( e, this );
		}
		return thumbnailImage;
	}

	@Override
	public URI getUri() {
		try {
			return edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAbsoluteUrl( this.projectPacket.getProjectPath() ).toURI();
		} catch( URISyntaxException e ) {
			return null;
		}
	}

	// TODO: we're losing error handling by doing this.
	@Override
	protected Project load() {
		Project project = null;
		try {
			project = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().downloadProject( this.projectPacket );
		} catch( CommunityApiException | IOException | VersionNotSupportedException e ) {
			e.printStackTrace();
		} catch( VersionExceedsCurrentException e ) {
			org.alice.ide.ProjectApplication.getActiveInstance().handleVersionExceedsCurrent( this.projectPacket.getTitle() );
		}
		return project;
	}
}
