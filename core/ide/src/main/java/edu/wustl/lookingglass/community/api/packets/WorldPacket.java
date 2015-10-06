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
package edu.wustl.lookingglass.community.api.packets;

import com.google.gson.annotations.Expose;

public class WorldPacket extends ProjectPacket {

	@Expose private InnerWorld world;

	/* package-private */static final class InnerWorld {
		@Expose( serialize = false ) Integer id;

		@Expose( serialize = false ) Integer user_id;

		@Expose( serialize = false ) Integer view_count;

		@Expose String title;

		@Expose String description;

		@Expose( serialize = false ) Boolean featured;

		@Expose( serialize = false ) org.joda.time.DateTime created_at;

		@Expose( serialize = false ) org.joda.time.DateTime updated_at;

		@Expose( serialize = false ) String world_path;

		@Expose( serialize = false ) String project_path;

		@Expose( serialize = false ) String video_path;

		@Expose( serialize = false ) String poster_path;

		@Expose String tag_list;

		@Expose String alice_version;

		@Expose Integer[] remixed_world_ids;

		@Expose Integer[] remixed_snippet_ids;
	}

	// These variables should never be expose as json
	private org.lgna.project.Project project = null;

	public static WorldPacket createInstance( String title, String description, String tags ) {
		WorldPacket packet = new WorldPacket();
		packet.world = new InnerWorld();

		packet.setTitle( title );
		packet.setDescription( description );
		packet.setTags( tags );
		packet.setAliceVersion( org.lgna.project.ProjectVersion.getCurrentVersion().toString() );

		return packet;
	}

	public static WorldPacket createInstance( String title, String description ) {
		return createInstance( title, description, null );
	}

	@Override
	public boolean isValid() {
		return this.world != null;
	}

	public void setId( Integer id ) {
		this.world.id = id;
	}

	public void setUserId( Integer id ) {
		this.world.user_id = id;
	}

	public void setTitle( String title ) {
		this.world.title = title;
	}

	public void setTags( String tags ) {
		this.world.tag_list = tags;
	}

	public void setDescription( String description ) {
		this.world.description = description;
	}

	public void setAliceVersion( String version ) {
		this.world.alice_version = version;
	}

	public void setProject( java.net.URI location ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException {
		try {
			this.addAttachment( "world[project_source[attachment]]", "application/x-lookingglass-project", location, "world-project." + org.lgna.project.io.IoUtilities.PROJECT_EXTENSION );
		} catch( java.io.IOException e ) {
			throw new edu.wustl.lookingglass.community.exceptions.CommunityIOException( e );
		}
	}

	public void setProject( org.lgna.project.Project project, edu.cmu.cs.dennisc.java.util.zip.DataSource... dataSources ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException {
		try {
			// Store this off so we can correctly match the community project attributes later.
			this.project = project;

			this.setRemixedSnippetIds( edu.wustl.lookingglass.community.CommunityProjectPropertyManager.getCommunityRemixedSnippetIds( this.project ) );
			this.setRemixedWorldIds( edu.wustl.lookingglass.community.CommunityProjectPropertyManager.getCommunityRemixedWorldIds( this.project ) );

			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			org.lgna.project.io.IoUtilities.writeProject( baos, project, dataSources );
			byte[] content = baos.toByteArray();
			this.addAttachment( "world[project_source[attachment]]", "application/x-lookingglass-project", content, "world-project." + org.lgna.project.io.IoUtilities.PROJECT_EXTENSION );
		} catch( java.io.IOException e ) {
			throw new edu.wustl.lookingglass.community.exceptions.CommunityIOException( e );
		}
	}

	public void setPoster( java.net.URI location ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException {
		try {
			this.addAttachment( "world[project_poster[attachment]]", "image/png", location, "world-poster.png" );
		} catch( java.io.IOException e ) {
			throw new edu.wustl.lookingglass.community.exceptions.CommunityIOException( e );
		}
	}

	public void setPoster( java.awt.image.RenderedImage image ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException {
		try {
			java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
			javax.imageio.ImageIO.write( image, "png", bos );
			byte[] content = bos.toByteArray();
			this.addAttachment( "world[project_poster[attachment]]", "image/png", content, "world-poster.png" );
		} catch( java.io.IOException e ) {
			throw new edu.wustl.lookingglass.community.exceptions.CommunityIOException( e );
		}
	}

	public void setVideo( java.net.URI location ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException {
		try {
			this.addAttachment( "world[project_video[attachment_webm]]", "video/webm", location, "world-video.webm" );
		} catch( java.io.IOException e ) {
			throw new edu.wustl.lookingglass.community.exceptions.CommunityIOException( e );
		}
	}

	public void setVideo( java.io.File file ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException {
		try {
			this.addAttachment( "world[project_video[attachment_webm]]", "video/webm", file.toURI(), "world-video.webm" );
		} catch( java.io.IOException e ) {
			throw new edu.wustl.lookingglass.community.exceptions.CommunityIOException( e );
		}
	}

	@Override
	public Integer getId() {
		return this.world.id;
	}

	@Override
	public Integer getUserId() {
		return this.world.user_id;
	}

	@Override
	public String getTitle() {
		return this.world.title;
	}

	@Override
	public String getDescription() {
		return this.world.description;
	}

	public String getTags() {
		return this.world.tag_list;
	}

	public org.joda.time.DateTime getCreatedAt() {
		return this.world.created_at;
	}

	public org.joda.time.DateTime getUpdatedAt() {
		return this.world.updated_at;
	}

	@Override
	public String getProjectPath() {
		return this.world.project_path;
	}

	public String getVideoPath() {
		return this.world.video_path;
	}

	public String getWorldPath() {
		return this.world.world_path;
	}

	@Override
	public String getPosterPath() {
		return this.world.poster_path;
	}

	public Boolean isFeatured() {
		return this.world.featured;
	}

	public Integer getViews() {
		return this.world.view_count;
	}

	public org.lgna.project.Project getProject() {
		return this.project;
	}

	/* Remixes */

	public void setRemixedWorldIds( Integer[] ids ) {
		this.world.remixed_world_ids = ids;
	}

	public void setRemixedSnippetIds( Integer[] ids ) {
		this.world.remixed_snippet_ids = ids;
	}
}
