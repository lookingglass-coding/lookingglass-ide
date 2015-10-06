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

import java.io.IOException;

import com.google.gson.annotations.Expose;

public class SnippetPacket extends edu.wustl.lookingglass.community.api.packets.JsonMultipartPacket {

	@Expose private InnerSnippet snippet;

	/* package-private */static class InnerSnippet {
		@Expose( serialize = false ) Integer id;

		@Expose( serialize = false ) Integer user_id;

		@Expose Integer world_id;

		@Expose String title;

		@Expose String description;

		@Expose java.util.UUID begin_node;

		@Expose java.util.UUID end_node;

		@Expose Integer begin_execution_count;

		@Expose Integer end_execution_count;

		@Expose( serialize = false ) String snippet_path;

		@Expose( serialize = false ) String project_path;

		@Expose( serialize = false ) String video_path;

		@Expose( serialize = false ) String poster_path;

		@Expose( serialize = false ) org.joda.time.DateTime created_at;

		@Expose( serialize = false ) org.joda.time.DateTime updated_at;

		@Expose String tag_list;

		@Expose String alice_version;
	}

	// These variables should be for this class only, they should not be serialized via json.
	private edu.wustl.lookingglass.remix.SnippetScript script;

	public static SnippetPacket createInstance( edu.wustl.lookingglass.remix.SnippetScript script, String title, String description, String tags ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException {
		SnippetPacket packet = new SnippetPacket();
		packet.snippet = new InnerSnippet();

		packet.setSnippetScript( script );
		packet.setTitle( title );
		packet.setDescription( description );
		packet.setTags( tags );
		packet.setAliceVersion( org.lgna.project.ProjectVersion.getCurrentVersion().toString() );

		return packet;
	}

	@Override
	public boolean isValid() {
		return ( this.snippet != null );
	}

	public edu.wustl.lookingglass.remix.SnippetScript setSnippetScript( byte[] payload ) {
		try {
			this.script = edu.wustl.lookingglass.remix.SnippetFileUtilities.loadSnippet( new java.io.ByteArrayInputStream( payload ) );
		} catch( IOException e ) {
			throw new RuntimeException( "Unable to create snippet" );
		}
		this.script.setCommunityId( this.getId() );
		this.script.setCommunityWorldId( this.getWorldId() );
		this.script.setBeginNodeUUID( this.getBeginNodeUUID() );
		this.script.setEndNodeUUID( this.getEndNodeUUID() );
		this.script.setBeginNodeExecutionCount( this.getBeginNodeExecutionCount() );
		this.script.setEndNodeExecutionCount( this.getEndNodeExecutionCount() );
		return this.script;
	}

	private void setSnippetScript( edu.wustl.lookingglass.remix.SnippetScript script ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException {
		this.script = script;
		this.setWorldId( script.getCommunityWorldId() );
		this.setBeginNodeUUID( script.getBeginNodeUUID() );
		this.setEndNodeUUID( script.getEndNodeUUID() );
		this.setBeginNodeExecutionCount( script.getBeginNodeExecutionCount() );
		this.setEndNodeExecutionCount( script.getEndNodeExecutionCount() );

		try {
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			edu.wustl.lookingglass.remix.SnippetFileUtilities.writeSnippet( baos, script );
			byte[] content = baos.toByteArray();
			this.addAttachment( "snippet[project_source[attachment]]", "application/x-lookingglass-remix", content, "snippet-project." + edu.wustl.lookingglass.remix.SnippetFileUtilities.SNIPPET_EXTENSION );
		} catch( java.io.IOException e ) {
			throw new edu.wustl.lookingglass.community.exceptions.CommunityIOException( e );
		}
	}

	public void setPoster( java.net.URI location ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException {
		try {
			this.addAttachment( "snippet[project_poster[attachment]]", "image/png", location, "snippet-poster.png" );
		} catch( java.io.IOException e ) {
			throw new edu.wustl.lookingglass.community.exceptions.CommunityIOException( e );
		}
	}

	public void setPoster( java.awt.image.RenderedImage image ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException {
		try {
			java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
			javax.imageio.ImageIO.write( image, "png", bos );
			byte[] content = bos.toByteArray();
			this.addAttachment( "snippet[project_poster[attachment]]", "image/png", content, "snippet-poster.png" );
		} catch( java.io.IOException e ) {
			throw new edu.wustl.lookingglass.community.exceptions.CommunityIOException( e );
		}
	}

	public void setVideo( java.net.URI location ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException {
		try {
			this.addAttachment( "snippet[project_video[attachment_webm]]", "video/webm", location, "snippet-video.webm" );
		} catch( java.io.IOException e ) {
			throw new edu.wustl.lookingglass.community.exceptions.CommunityIOException( e );
		}
	}

	public void setVideo( java.io.File file ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException {
		try {
			this.addAttachment( "snippet[project_video[attachment_webm]]", "video/webm", file.toURI(), "snippet-video.webm" );
		} catch( java.io.IOException e ) {
			throw new edu.wustl.lookingglass.community.exceptions.CommunityIOException( e );
		}
	}

	public Integer getId() {
		return this.snippet.id;
	}

	public Integer getUserId() {
		return this.snippet.user_id;
	}

	public String getTitle() {
		return this.snippet.title;
	}

	public String getDescription() {
		return this.snippet.description;
	}

	public String getTags() {
		return this.snippet.tag_list;
	}

	public org.joda.time.DateTime getCreatedAt() {
		return this.snippet.created_at;
	}

	public org.joda.time.DateTime getUpdatedAt() {
		return this.snippet.updated_at;
	}

	public String getProjectPath() {
		return this.snippet.project_path;
	}

	public String getVideoPath() {
		return this.snippet.video_path;
	}

	public String getSnippetPath() {
		return this.snippet.snippet_path;
	}

	public String getPosterPath() {
		return this.snippet.poster_path;
	}

	public void setWorldId( Integer id ) {
		this.snippet.world_id = id;
	}

	public Integer getWorldId() {
		return this.snippet.world_id;
	}

	public java.util.UUID getBeginNodeUUID() {
		return this.snippet.begin_node;
	}

	public java.util.UUID getEndNodeUUID() {
		return this.snippet.end_node;
	}

	public Integer getBeginNodeExecutionCount() {
		return this.snippet.begin_execution_count;
	}

	public Integer getEndNodeExecutionCount() {
		return this.snippet.end_execution_count;
	}

	public edu.wustl.lookingglass.remix.SnippetScript getScript() {
		return this.script;
	}

	public void setId( Integer id ) {
		this.snippet.id = id;
	}

	public void setTitle( String title ) {
		this.snippet.title = title;
	}

	public void setDescription( String description ) {
		this.snippet.description = description;
	}

	public void setTags( String tags ) {
		this.snippet.tag_list = tags;
	}

	public void setAliceVersion( String version ) {
		this.snippet.alice_version = version;
	}

	public void setBeginNodeUUID( java.util.UUID uuid ) {
		this.snippet.begin_node = uuid;
	}

	public void setEndNodeUUID( java.util.UUID uuid ) {
		this.snippet.end_node = uuid;
	}

	public void setBeginNodeExecutionCount( Integer count ) {
		this.snippet.begin_execution_count = count;
	}

	public void setEndNodeExecutionCount( Integer count ) {
		this.snippet.end_execution_count = count;
	}

}
