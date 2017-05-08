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
package edu.wustl.lookingglass.community;

import java.io.File;
import java.util.ArrayList;

import org.scribe.model.Verb;

import edu.wustl.lookingglass.community.CommunityRepositorySyncStatus.SyncStatus;
import edu.wustl.lookingglass.community.api.CommunityBaseController;
import edu.wustl.lookingglass.community.api.QueryParameter;
import edu.wustl.lookingglass.community.api.QueryParameterArray;
import edu.wustl.lookingglass.community.api.packets.ActivityPacket;
import edu.wustl.lookingglass.community.api.packets.CodeTestPacket;
import edu.wustl.lookingglass.community.api.packets.CodeTestResultPacket;
import edu.wustl.lookingglass.community.api.packets.FilePacket;
import edu.wustl.lookingglass.community.api.packets.JsonPacket;
import edu.wustl.lookingglass.community.api.packets.ModulePacket;
import edu.wustl.lookingglass.community.api.packets.ModuleResultPacket;
import edu.wustl.lookingglass.community.api.packets.SnippetPacket;
import edu.wustl.lookingglass.community.api.packets.TemplatePacket;
import edu.wustl.lookingglass.community.api.packets.UserPacket;
import edu.wustl.lookingglass.community.api.packets.WorldPacket;
import edu.wustl.lookingglass.community.exceptions.CommunityApiException;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.croquet.preferences.CommunityPasswordState;
import edu.wustl.lookingglass.ide.croquet.preferences.CommunityUsernameState;
import edu.wustl.lookingglass.ide.croquet.preferences.PersistentCommunityCredentialsState;
import edu.wustl.lookingglass.project.VersionExceedsCurrentException;
import edu.wustl.lookingglass.study.StudyConfiguration;

/**
 * @author Kyle J. Harms
 */
public final class CommunityController extends CommunityBaseController {

	private java.util.Set<CommunityControllerListener> listeners = edu.cmu.cs.dennisc.java.util.Sets.newHashSet();

	public CommunityController( File projectsRepoDir ) {
		super( projectsRepoDir );

		// this is forcing the CommunityProjectPropertyManager to load the properties so that worlds are ok.
		CommunityProjectPropertyManager.initialize( this );
	}

	public CommunityController() {
		this( null );
	}

	public void initializeConnection() throws CommunityApiException {
		try {
			if( this.isAutoConnect() && PersistentCommunityCredentialsState.getInstance().getValue() && ( CommunityUsernameState.getInstance().getValue() != null ) && ( CommunityPasswordState.getInstance().getValue() != null ) ) {
				this.loginUser( CommunityUsernameState.getInstance().getValue(), CommunityPasswordState.getInstance().getPassword().toCharArray() );
			} else if( ( StudyConfiguration.INSTANCE.getCommunityUserName() != null ) && ( StudyConfiguration.INSTANCE.getCommunityPassword() != null ) ) {
				this.loginUser( (String)StudyConfiguration.INSTANCE.getCommunityUserName(), edu.wustl.lookingglass.study.StudyConfiguration.INSTANCE.getCommunityPassword().toCharArray() );
			} else {
				this.loginAnonymous();
			}
		} catch( edu.wustl.lookingglass.community.exceptions.CommunityApiException e ) {
			this.loginAnonymous();
		}
	}

	// This should be protected; do not change to public
	protected FilePacket getFilePacket( String url ) throws CommunityApiException, java.io.IOException {
		assert url != null;
		return sendRequest( Verb.GET, getAbsoluteUrl( url ), FilePacket.class );
	}

	/*
	 * Capabilities
	 */
	public java.net.URL getHelpUrl() {
		if( this.getCapabilitiesPacket() != null ) {
			return getAbsoluteUrl( this.getCapabilitiesPacket().getHelpPath() );
		} else {
			return null;
		}
	}

	public java.net.URL getDownloadUrl() {
		if( this.getCapabilitiesPacket() != null ) {
			return getAbsoluteUrl( this.getCapabilitiesPacket().getDownloadPath() );
		} else {
			return null;
		}
	}

	public java.net.URL getOpenTemplateTutorialUrl() {
		if( this.getCapabilitiesPacket() != null ) {
			return getAbsoluteUrl( this.getCapabilitiesPacket().getOpenTemplateTutorialPath() );
		} else {
			return null;
		}
	}

	public java.net.URL getAnimateStoryTutorialUrl() {
		if( this.getCapabilitiesPacket() != null ) {
			return getAbsoluteUrl( this.getCapabilitiesPacket().getAnimateStoryTutorialPath() );
		} else {
			return null;
		}
	}

	public java.net.URL getRemixActionsTutorialUrl() {
		if( this.getCapabilitiesPacket() != null ) {
			return getAbsoluteUrl( this.getCapabilitiesPacket().getRemixActionsTutorialPath() );
		} else {
			return null;
		}
	}

	public java.net.URL getShareWorldTutorialUrl() {
		if( this.getCapabilitiesPacket() != null ) {
			return getAbsoluteUrl( this.getCapabilitiesPacket().getShareWorldTutorialPath() );
		} else {
			return null;
		}
	}

	public java.net.URL getResetPasswordUrl() {
		if( this.getCapabilitiesPacket() != null ) {
			return getAbsoluteUrl( this.getCapabilitiesPacket().getResetPasswordPath() );
		} else {
			return null;
		}
	}

	public java.net.URL getSignUpUrl() {
		if( this.getCapabilitiesPacket() != null ) {
			return getAbsoluteUrl( this.getCapabilitiesPacket().getSignUpPath() );
		} else {
			return null;
		}
	}

	public edu.wustl.lookingglass.common.VersionNumber getLatestLookingGlassVersion() {
		if( this.getCapabilitiesPacket() != null ) {
			return this.getCapabilitiesPacket().getLatestLookingGlassVersion();
		} else {
			return null;
		}
	}

	public boolean isLookingGlassOutdated() {
		edu.wustl.lookingglass.common.VersionNumber latestVersion = this.getLatestLookingGlassVersion();
		if( latestVersion != null ) {
			return latestVersion.compareTo( LookingGlassIDE.APPLICATION_VERSION ) > 0;
		} else {
			return false;
		}
	}

	/*
	 * Community Access
	 */

	public UserPacket loginUser( String username, char[] password ) throws CommunityApiException {
		this.userAccess( username, password );
		return this.getUserPacket();
	}

	@Deprecated
	public UserPacket loginUser( String username, String password ) throws CommunityApiException {
		return this.loginUser( username, password.toCharArray() );
	}

	public void loginAnonymous() throws CommunityApiException {
		this.anonymousAccess();
	}

	public void logout() {
		this.closeAccess();
	}

	public UserPacket getCurrentUser() {
		return this.getUserPacket();
	}

	public void syncProjectsRepository() {
		if( !StudyConfiguration.INSTANCE.shouldSyncProjecs() ) {
			return;
		}

		CommunityRepository repo = this.getProjectsRepository();

		if( repo != null ) {
			// TODO: remove this once we create UI and properly integrate projects sync into LG.
			repo.setShouldWorkOffline( true );

			repo.sync( ( status ) -> {
				if( status.getStatus() == SyncStatus.UNKNOWN_FAILURE ) {
					// These errors shouldn't have happened... it means we have a problem
					// with our sync algorithm. We need the user to report this bug so
					// we can try to fix it.
					Throwable error = status.getError();
					throw new RuntimeException( error );
				}
			} );
		}
	}

	/*
	 * Users
	 */

	public UserPacket getUser( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();
		return sendRequest( Verb.GET, getAbsoluteApiUrl( "/users/" + id + ".json" ), UserPacket.class );
	}

	public UserPacket[] getUsers( Integer pageSize, Integer page ) throws CommunityApiException {
		anonymousOrLoginRequired();
		java.util.ArrayList<QueryParameter> params = new java.util.ArrayList<QueryParameter>();
		if( pageSize != null ) {
			params.add( new QueryParameter( "page_size", pageSize ) );
		}
		if( page != null ) {
			params.add( new QueryParameter( "page", page ) );
		}
		return sendRequest( Verb.GET, getAbsoluteApiUrl( "/users.json" ), params.toArray( new QueryParameter[ params.size() ] ), UserPacket[].class );
	}

	public java.awt.Image downloadAvatar( edu.wustl.lookingglass.community.api.packets.UserPacket userPacket ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException, java.io.IOException {
		anonymousOrLoginRequired();

		byte[] payload = this.getFilePacket( userPacket.getUserAvatarPath() ).getPayload();
		return javax.imageio.ImageIO.read( new java.io.ByteArrayInputStream( payload ) );
	}

	/*
	 * Project Packets
	 */

	public org.lgna.project.Project downloadProject( edu.wustl.lookingglass.community.api.packets.ProjectPacket projectPacket ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException, java.io.IOException, org.lgna.project.VersionNotSupportedException, VersionExceedsCurrentException {
		if( projectPacket instanceof WorldPacket ) {
			return downloadWorldProject( (WorldPacket)projectPacket );
		} else if( projectPacket instanceof TemplatePacket ) {
			return downloadTemplateProject( (TemplatePacket)projectPacket );
		} else {
			throw new RuntimeException( "unknown project packet type" );
		}
	}

	public java.awt.Image downloadPoster( edu.wustl.lookingglass.community.api.packets.ProjectPacket projectPacket ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException, java.io.IOException {
		anonymousOrLoginRequired();

		byte[] payload = this.getFilePacket( projectPacket.getPosterPath() ).getPayload();
		return javax.imageio.ImageIO.read( new java.io.ByteArrayInputStream( payload ) );
	}

	/*
	 * Worlds
	 */

	public static enum WorldFilter implements CommunityFilter {
		RECENT,
		POPULAR,
		FEATURED,
		BOOKMARKED
	}

	public WorldPacket getWorld( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();

		notifyRequestSent( CommunityControllerEventType.GET_WORLD, null, null );
		WorldPacket response = sendRequest( Verb.GET, getAbsoluteApiUrl( "/worlds/" + id + ".json" ), WorldPacket.class );
		notifyResponseRecieved( CommunityControllerEventType.GET_WORLD, new WorldPacket[] { response }, null );
		return response;
	}

	public static class WorldQuery {
		private WorldFilter filter = null;
		private String query = null;
		private Integer pageSize = null;
		private Integer page = null;
		private boolean limitVersion = false;

		public WorldQuery() {
		}

		public WorldQuery filter( WorldFilter filter ) {
			this.filter = filter;
			return this;
		}

		public WorldQuery query( String query ) {
			this.query = query;
			return this;
		}

		public WorldQuery pageSize( Integer pageSize ) {
			this.pageSize = pageSize;
			return this;
		}

		public WorldQuery page( Integer page ) {
			this.page = page;
			return this;
		}

		public WorldQuery limitVersion( boolean limitVersion ) {
			this.limitVersion = limitVersion;
			return this;
		}
	}

	public WorldPacket[] getWorlds( WorldQuery query ) throws CommunityApiException {
		if( ( query.filter != null ) && ( query.filter == WorldFilter.BOOKMARKED ) ) {
			loginRequired();
		} else {
			anonymousOrLoginRequired();
		}

		java.util.ArrayList<QueryParameter> params = new java.util.ArrayList<QueryParameter>();
		if( query.pageSize != null ) {
			params.add( new QueryParameter( "page_size", query.pageSize ) );
		}
		if( query.page != null ) {
			params.add( new QueryParameter( "page", query.page ) );
		}
		if( query.query != null ) {
			params.add( new QueryParameter( "query", query.query ) );
		}
		if( query.limitVersion ) {
			params.add( new QueryParameter( "alice_version", org.lgna.project.ProjectVersion.getCurrentVersion().toString() ) );
		}

		StringBuilder url = new StringBuilder();
		url.append( "/worlds" );
		if( query.filter != null ) {
			url.append( "/" );
			url.append( query.filter.getParameter() );
		}
		url.append( ".json" );

		return getWorlds( getAbsoluteApiUrl( url.toString() ), params );
	}

	private WorldPacket[] getWorlds( String url, java.util.ArrayList<QueryParameter> params ) throws CommunityApiException {
		notifyRequestSent( CommunityControllerEventType.GET_WORLD, null, params );
		WorldPacket[] response = sendRequest( Verb.GET, url, params.toArray( new QueryParameter[ params.size() ] ), WorldPacket[].class );
		notifyResponseRecieved( CommunityControllerEventType.GET_WORLD, response, params );
		return response;
	}

	public WorldPacket updateWorld( WorldPacket packet ) throws CommunityApiException {
		loginRequired();

		org.lgna.project.Project project = packet.getProject();
		if( project != null ) {
			CommunityProjectPropertyManager.setProjectTitle( project, packet.getTitle() );
			CommunityProjectPropertyManager.setProjectDescription( project, packet.getDescription() );
		}

		return sendRequest( Verb.PUT, getAbsoluteApiUrl( "/worlds/" + packet.getId() + ".json" ), packet, WorldPacket.class );
	}

	public void incrementWorldRemixCount( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();
		this.updateWorldCounter( id, "remix_count" );
	}

	public void incrementWorldViewCount( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();
		this.updateWorldCounter( id, "view_count" );
	}

	private void updateWorldCounter( Integer id, String counter ) throws CommunityApiException {
		assert id != null;
		assert counter != null;
		sendRequest( Verb.PUT, getAbsoluteApiUrl( "/worlds/" + id + "/increment_counter/" + counter ) );
	}

	public WorldPacket newWorld( WorldPacket packet ) throws CommunityApiException {
		loginRequired();

		org.lgna.project.Project project = packet.getProject();
		if( project != null ) {
			CommunityProjectPropertyManager.setProjectTitle( project, packet.getTitle() );
			CommunityProjectPropertyManager.setProjectDescription( project, packet.getDescription() );
			CommunityProjectPropertyManager.setProjectUserID( project, packet.getUserId() );
		}

		notifyRequestSent( CommunityControllerEventType.POST_WORLD, new WorldPacket[] { packet }, null );
		WorldPacket responsePacket = sendRequest( Verb.POST, getAbsoluteApiUrl( "/worlds.json" ), packet, WorldPacket.class );
		notifyResponseRecieved( CommunityControllerEventType.POST_WORLD, new WorldPacket[] { responsePacket }, null );

		// we only have the ID for the project after upload has happened. save this info in the project properties too.
		// TODO: since the world id is saved to the packet here, we should force a save of the world.
		if( project != null ) {
			CommunityProjectPropertyManager.setCommunityProjectID( project, responsePacket.getId() );
		}

		return responsePacket;
	}

	public org.lgna.project.Project downloadWorldProject( WorldPacket worldPacket ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException, java.io.IOException, org.lgna.project.VersionNotSupportedException, VersionExceedsCurrentException {
		anonymousOrLoginRequired();

		byte[] payload = this.getFilePacket( worldPacket.getProjectPath() ).getPayload();
		org.lgna.project.Project project = org.lgna.project.io.IoUtilities.readProject( new java.io.ByteArrayInputStream( payload ) );

		// Set the project attributes.
		// This set the ID, since we always have a chicken/egg situation with the ID.
		// Make sure we update the title and description, if they have changed on the site.
		CommunityProjectPropertyManager.setCommunityProjectID( project, worldPacket.getId() );
		CommunityProjectPropertyManager.setProjectTitle( project, worldPacket.getTitle() );
		CommunityProjectPropertyManager.setProjectDescription( project, worldPacket.getDescription() );
		CommunityProjectPropertyManager.setCommunityTemplateID( project, null );

		return project;
	}

	public java.awt.Image downloadWorldPoster( WorldPacket worldPacket ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException, java.io.IOException {
		anonymousOrLoginRequired();

		byte[] payload = this.getFilePacket( worldPacket.getPosterPath() ).getPayload();
		return javax.imageio.ImageIO.read( new java.io.ByteArrayInputStream( payload ) );
	}

	/*
	 * Templates
	 */

	public static enum TemplateFilter implements CommunityFilter {
		RECENT,
		POPULAR,
		FEATURED,
		BOOKMARKED
	}

	public TemplatePacket getTemplate( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();

		notifyRequestSent( CommunityControllerEventType.GET_TEMPLATE, null, null );
		TemplatePacket response = sendRequest( Verb.GET, getAbsoluteApiUrl( "/templates/" + id + ".json" ), TemplatePacket.class );
		notifyResponseRecieved( CommunityControllerEventType.GET_TEMPLATE, new TemplatePacket[] { response }, null );
		return response;
	}

	public static class TemplateQuery {
		private TemplateFilter filter = null;
		private String query = null;
		private Integer pageSize = null;
		private Integer page = null;
		private boolean limitVersion = false;

		public TemplateQuery() {
		}

		public TemplateQuery filter( TemplateFilter filter ) {
			this.filter = filter;
			return this;
		}

		public TemplateQuery query( String query ) {
			this.query = query;
			return this;
		}

		public TemplateQuery pageSize( Integer pageSize ) {
			this.pageSize = pageSize;
			return this;
		}

		public TemplateQuery page( Integer page ) {
			this.page = page;
			return this;
		}

		public TemplateQuery limitVersion( boolean limitVersion ) {
			this.limitVersion = limitVersion;
			return this;
		}
	}

	public TemplatePacket[] getTemplates( TemplateQuery query ) throws CommunityApiException {
		if( ( query.filter != null ) && ( query.filter == TemplateFilter.BOOKMARKED ) ) {
			loginRequired();
		} else {
			anonymousOrLoginRequired();
		}

		java.util.ArrayList<QueryParameter> params = new java.util.ArrayList<QueryParameter>();
		if( query.pageSize != null ) {
			params.add( new QueryParameter( "page_size", query.pageSize ) );
		}
		if( query.page != null ) {
			params.add( new QueryParameter( "page", query.page ) );
		}
		if( query.query != null ) {
			params.add( new QueryParameter( "query", query.query ) );
		}
		if( query.limitVersion ) {
			params.add( new QueryParameter( "alice_version", org.lgna.project.ProjectVersion.getCurrentVersion().toString() ) );
		}

		StringBuilder url = new StringBuilder();
		url.append( "/templates" );
		if( query.filter != null ) {
			url.append( "/" );
			url.append( query.filter.getParameter() );
		}
		url.append( ".json" );

		return getTemplates( getAbsoluteApiUrl( url.toString() ), params );
	}

	private TemplatePacket[] getTemplates( String url, java.util.ArrayList<QueryParameter> params ) throws CommunityApiException {
		notifyRequestSent( CommunityControllerEventType.GET_TEMPLATE, null, params );
		TemplatePacket[] response = sendRequest( Verb.GET, url, params.toArray( new QueryParameter[ params.size() ] ), TemplatePacket[].class );
		notifyResponseRecieved( CommunityControllerEventType.GET_TEMPLATE, response, params );
		return response;

	}

	public TemplatePacket updateTemplate( TemplatePacket packet ) throws CommunityApiException {
		loginRequired();
		return sendRequest( Verb.PUT, getAbsoluteApiUrl( "/templates/" + packet.getId() + ".json" ), packet, TemplatePacket.class );
	}

	public void submitWorldToTemplate( Integer templateId, Integer worldId ) throws CommunityApiException {
		assert templateId != null;
		assert worldId != null;
		loginRequired();
		sendRequest( Verb.POST, getAbsoluteApiUrl( "/templates/" + templateId + "/worlds/" + worldId + ".json" ) );
	}

	public WorldPacket[] getTemplateWorlds( Integer templateId ) throws CommunityApiException {
		anonymousOrLoginRequired();
		return sendRequest( Verb.GET, getAbsoluteApiUrl( "/templates/" + templateId + "/worlds.json" ), WorldPacket[].class );
	}

	public void incrementTemplateViewCount( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();
		this.updateTemplateCounter( id, "view_count" );
	}

	private void updateTemplateCounter( Integer id, String counter ) throws CommunityApiException {
		assert id != null;
		assert counter != null;
		sendRequest( Verb.PUT, getAbsoluteApiUrl( "/templates/" + id + "/increment_counter/" + counter ) );
	}

	public TemplatePacket newTemplate( TemplatePacket packet ) throws CommunityApiException {
		loginRequired();

		org.lgna.project.Project project = packet.getProject();
		if( project != null ) {
			CommunityProjectPropertyManager.setProjectTitle( project, packet.getTitle() );
			CommunityProjectPropertyManager.setProjectDescription( project, packet.getDescription() );
		}

		notifyRequestSent( CommunityControllerEventType.POST_TEMPLATE, new TemplatePacket[] { packet }, null );
		TemplatePacket responsePacket = sendRequest( Verb.POST, getAbsoluteApiUrl( "/templates.json" ), packet, TemplatePacket.class );
		notifyResponseRecieved( CommunityControllerEventType.POST_TEMPLATE, new TemplatePacket[] { responsePacket }, null );

		// we only have the ID for the project after upload has happened. save this info in the project properties too.
		// TODO: since the template id is saved to the packet here, we should force a save of the template.
		if( project != null ) {
			CommunityProjectPropertyManager.setCommunityTemplateID( project, responsePacket.getId() );
		}

		return responsePacket;
	}

	public org.lgna.project.Project downloadTemplateProject( TemplatePacket templatePacket ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException, java.io.IOException, org.lgna.project.VersionNotSupportedException, VersionExceedsCurrentException {
		anonymousOrLoginRequired();

		byte[] payload = this.getFilePacket( templatePacket.getProjectPath() ).getPayload();
		org.lgna.project.Project project = org.lgna.project.io.IoUtilities.readProject( new java.io.ByteArrayInputStream( payload ) );

		// Set the project attributes.
		// This set the ID, since we always have a chicken/egg situation with the ID.
		// Make sure we update the title and description, if they have changed on the site.
		CommunityProjectPropertyManager.setCommunityProjectID( project, null );
		CommunityProjectPropertyManager.setProjectTitle( project, templatePacket.getTitle() );
		CommunityProjectPropertyManager.setProjectDescription( project, templatePacket.getDescription() );
		CommunityProjectPropertyManager.setCommunityTemplateID( project, templatePacket.getId() );

		return project;
	}

	public java.awt.Image downloadTemplatePoster( TemplatePacket templatePacket ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException, java.io.IOException {
		anonymousOrLoginRequired();

		byte[] payload = this.getFilePacket( templatePacket.getPosterPath() ).getPayload();
		return javax.imageio.ImageIO.read( new java.io.ByteArrayInputStream( payload ) );
	}

	/*
	 * Snippets
	 */

	public static enum SnippetFilter implements CommunityFilter {
		RECENT,
		POPULAR,
		FEATURED,
		BOOKMARKED;
	}

	public SnippetPacket newSnippet( SnippetPacket packet ) throws CommunityApiException {
		loginRequired();

		notifyRequestSent( CommunityControllerEventType.POST_SNIPPET, new SnippetPacket[] { packet }, null );
		SnippetPacket response = sendRequest( Verb.POST, getAbsoluteApiUrl( "/snippets.json" ), packet, SnippetPacket.class );
		notifyResponseRecieved( CommunityControllerEventType.POST_SNIPPET, new SnippetPacket[] { response }, null );
		return response;
	}

	public SnippetPacket updateSnippet( SnippetPacket packet ) throws CommunityApiException {
		loginRequired();
		return sendRequest( Verb.PUT, getAbsoluteApiUrl( "/snippets/" + packet.getId() + ".json" ), packet, SnippetPacket.class );
	}

	public SnippetPacket getSnippet( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();

		notifyRequestSent( CommunityControllerEventType.GET_SNIPPET, null, null );
		SnippetPacket response = sendRequest( Verb.GET, getAbsoluteApiUrl( "/snippets/" + id + ".json" ), SnippetPacket.class );
		notifyResponseRecieved( CommunityControllerEventType.GET_SNIPPET, new SnippetPacket[] { response }, null );
		return response;
	}

	public static class SnippetQuery {

		private SnippetFilter filter = null;
		private String query = null;
		private Integer pageSize = null;
		private Integer page = null;
		private boolean limitVersion = false;
		private int[] ignoredIds = null;
		private int[] lessonIds = null;
		private int[] modelFieldIds = null;
		private Integer activeModelFieldId = null;
		private boolean fuzzyResults = false;

		public SnippetQuery() {
		}

		public SnippetQuery filter( SnippetFilter filter ) {
			this.filter = filter;
			return this;
		}

		public SnippetQuery query( String query ) {
			this.query = query;
			return this;
		}

		public SnippetQuery pageSize( Integer pageSize ) {
			this.pageSize = pageSize;
			return this;
		}

		public SnippetQuery page( Integer page ) {
			this.page = page;
			return this;
		}

		public SnippetQuery limitVersion( boolean limitVersion ) {
			this.limitVersion = limitVersion;
			return this;
		}

		public SnippetQuery ignoredIds( int[] ignoredIds ) {
			this.ignoredIds = ignoredIds;
			return this;
		}

		public SnippetQuery lessonIds( int[] lessonIds ) {
			this.lessonIds = lessonIds;
			return this;
		}

		public SnippetQuery modelFieldIds( int[] modelFieldIds ) {
			this.modelFieldIds = modelFieldIds;
			return this;
		}

		public SnippetQuery activeModelFieldId( Integer activeModelFieldId ) {
			this.activeModelFieldId = activeModelFieldId;
			return this;
		}

		public SnippetQuery fuzzyResults( boolean fuzzyResults ) {
			this.fuzzyResults = fuzzyResults;
			return this;
		}
	}

	public SnippetPacket[] getSnippets( SnippetQuery query ) throws CommunityApiException {
		if( ( query.filter != null ) && ( query.filter == SnippetFilter.BOOKMARKED ) ) {
			loginRequired();
		} else {
			anonymousOrLoginRequired();
		}

		java.util.ArrayList<QueryParameter> params = new java.util.ArrayList<QueryParameter>();
		if( query.pageSize != null ) {
			params.add( new QueryParameter( "page_size", query.pageSize ) );
		}
		if( query.page != null ) {
			params.add( new QueryParameter( "page", query.page ) );
		}
		if( query.query != null ) {
			params.add( new QueryParameter( "query", query.query ) );
		}
		if( query.limitVersion ) {
			params.add( new QueryParameter( "alice_version", org.lgna.project.ProjectVersion.getCurrentVersion().toString() ) );
		}
		if( query.ignoredIds != null ) {
			QueryParameterArray qarray = new QueryParameterArray( "ignored_ids", query.ignoredIds );
			params.addAll( java.util.Arrays.asList( qarray.getQueryParameters() ) );
		}
		if( query.lessonIds != null ) {
			QueryParameterArray qarray = new QueryParameterArray( "lesson_ids", query.lessonIds );
			params.addAll( java.util.Arrays.asList( qarray.getQueryParameters() ) );
		}
		if( query.modelFieldIds != null ) {
			QueryParameterArray qarray = new QueryParameterArray( "model_field_ids", query.modelFieldIds );
			params.addAll( java.util.Arrays.asList( qarray.getQueryParameters() ) );
		}
		if( query.activeModelFieldId != null ) {
			params.add( new QueryParameter( "active_model_field_id", query.activeModelFieldId ) );
		}
		if( query.fuzzyResults ) {
			params.add( new QueryParameter( "fuzzy_results", "true" ) );
		}

		StringBuilder url = new StringBuilder();
		url.append( "/snippets" );
		if( query.filter != null ) {
			url.append( "/" );
			url.append( query.filter.getParameter() );
		}
		url.append( ".json" );

		return getSnippets( getAbsoluteApiUrl( url.toString() ), params );
	}

	private SnippetPacket[] getSnippets( String url, java.util.ArrayList<QueryParameter> params ) throws CommunityApiException {
		notifyRequestSent( CommunityControllerEventType.GET_SNIPPET, null, params );
		SnippetPacket[] response = sendRequest( Verb.GET, url, params.toArray( new QueryParameter[ params.size() ] ), SnippetPacket[].class );
		notifyResponseRecieved( CommunityControllerEventType.GET_SNIPPET, response, params );
		return response;
	}

	public edu.wustl.lookingglass.remix.SnippetScript downloadSnippetScript( SnippetPacket snippetPacket ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException, java.io.IOException, org.lgna.project.VersionNotSupportedException {
		anonymousOrLoginRequired();

		byte[] payload = this.getFilePacket( snippetPacket.getProjectPath() ).getPayload();
		return snippetPacket.setSnippetScript( payload );
	}

	public java.awt.Image downloadSnippetPoster( SnippetPacket snippetPacket ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException, java.io.IOException {
		anonymousOrLoginRequired();

		byte[] payload = this.getFilePacket( snippetPacket.getPosterPath() ).getPayload();
		return javax.imageio.ImageIO.read( new java.io.ByteArrayInputStream( payload ) );
	}

	/*
	 * Code Tests
	 */

	public CodeTestPacket getCodeTest( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();
		return sendRequest( Verb.GET, getAbsoluteApiUrl( "/code_tests/" + id + ".json" ), CodeTestPacket.class );
	}

	public CodeTestPacket[] getCodeTests( Integer pageSize, Integer page, CodeTestPacket.CodeTestType type ) throws CommunityApiException {
		anonymousOrLoginRequired();

		java.util.ArrayList<QueryParameter> params = new java.util.ArrayList<QueryParameter>();
		if( pageSize != null ) {
			params.add( new QueryParameter( "page_size", pageSize ) );
		}
		if( page != null ) {
			params.add( new QueryParameter( "page", page ) );
		}
		if( type != null ) {
			params.add( new QueryParameter( "type", type.getId() ) );
		}
		return sendRequest( Verb.GET, getAbsoluteApiUrl( "/code_tests.json" ), params.toArray( new QueryParameter[ params.size() ] ), CodeTestPacket[].class );
	}

	public CodeTestPacket updateCodeTest( CodeTestPacket packet ) throws CommunityApiException {
		loginRequired();
		return sendRequest( Verb.PUT, getAbsoluteApiUrl( "/code_tests/" + packet.getId() + ".json" ), packet, CodeTestPacket.class );
	}

	public CodeTestPacket newCodeTest( CodeTestPacket packet ) throws CommunityApiException {
		loginRequired();
		return sendRequest( Verb.POST, getAbsoluteApiUrl( "/code_tests.json" ), packet, CodeTestPacket.class );
	}

	public CodeTestResultPacket executeCodeTest( Integer codeTestId, Integer worldId ) throws CommunityApiException {
		anonymousOrLoginRequired();
		return sendRequest( Verb.POST, getAbsoluteApiUrl( "/code_tests/" + codeTestId + "/execute/worlds/" + worldId + ".json" ), CodeTestResultPacket.class );
	}

	/*
	 * Code Test Results
	 */

	public CodeTestResultPacket getCodeTestResult( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();

		return sendRequest( Verb.GET, getAbsoluteApiUrl( "/code_test_results/" + id + ".json" ), CodeTestResultPacket.class );
	}

	/*
	 * Activities
	 */

	public ActivityPacket[] getActivities( Integer pageSize, Integer page ) throws CommunityApiException {
		anonymousOrLoginRequired();

		java.util.ArrayList<QueryParameter> params = new java.util.ArrayList<QueryParameter>();
		if( pageSize != null ) {
			params.add( new QueryParameter( "page_size", pageSize ) );
		}
		if( page != null ) {
			params.add( new QueryParameter( "page", page ) );
		}

		StringBuilder url = new StringBuilder();
		url.append( "/activities" );
		url.append( ".json" );
		return sendRequest( Verb.GET, getAbsoluteApiUrl( url.toString() ), params.toArray( new QueryParameter[ params.size() ] ), ActivityPacket[].class );
	}

	public ActivityPacket getActivity( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();
		return sendRequest( Verb.GET, getAbsoluteApiUrl( "/activities/" + id + ".json" ), ActivityPacket.class );
	}

	public ActivityPacket[] getWorldActivities( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();
		return sendRequest( Verb.GET, getAbsoluteApiUrl( "/worlds/" + id + "/activities.json" ), ActivityPacket[].class );
	}

	public ActivityPacket[] getTemplateActivities( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();
		return sendRequest( Verb.GET, getAbsoluteApiUrl( "/templates/" + id + "/activities.json" ), ActivityPacket[].class );
	}

	public ActivityPacket[] getUserActivities( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();
		return sendRequest( Verb.GET, getAbsoluteApiUrl( "/users/" + id + "/activities.json" ), ActivityPacket[].class );
	}

	public ActivityPacket[] getSnippetActivities( Integer id ) throws CommunityApiException {
		anonymousOrLoginRequired();
		return sendRequest( Verb.GET, getAbsoluteApiUrl( "/snippets/" + id + "/activities.json" ), ActivityPacket[].class );
	}

	public java.awt.Image downloadActivityThumbnail( edu.wustl.lookingglass.community.api.packets.ActivityPacket activityPacket ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException, java.io.IOException {
		anonymousOrLoginRequired();

		byte[] payload = this.getFilePacket( activityPacket.getThumbnailPath() ).getPayload();
		return javax.imageio.ImageIO.read( new java.io.ByteArrayInputStream( payload ) );
	}

	/*
	 * Collection Modules
	 */

	public ModulePacket[] getModulePackets() {
		if( this.getCapabilitiesPacket() != null ) {
			return this.getCapabilitiesPacket().getModulePackets();
		} else {
			return null;
		}
	}

	public java.io.File downloadCollectionModule( String modulePath ) throws edu.wustl.lookingglass.community.exceptions.CommunityApiException, java.io.IOException {
		anonymousOrLoginRequired();

		byte[] payload = this.getFilePacket( modulePath ).getPayload();
		java.io.File file = java.io.File.createTempFile( "module", ".jar" );
		file.deleteOnExit();

		java.nio.file.Files.write( file.toPath(), payload );

		return file;
	}

	public void sendModuleResult( ModuleResultPacket packet ) throws CommunityApiException {
		anonymousOrLoginRequired();

		sendRequest( Verb.POST, getAbsoluteApiUrl( "/module_results.json" ), packet, ModuleResultPacket.class );
	}

	/*
	 * Listening
	 */

	public void addCommunityControllerListener( CommunityControllerListener listener ) {
		this.listeners.add( listener );
	}

	public void removeCommunityControllerListener( CommunityControllerListener listener ) {
		this.listeners.remove( listener );
	}

	public void clearControllerListeners() {
		this.listeners.clear();
	}

	private void notifyRequestSent( CommunityControllerEventType eventType, JsonPacket[] packets, ArrayList<QueryParameter> params ) {
		for( CommunityControllerListener listener : this.listeners ) {
			listener.requestSent( new CommunityControllerEvent( eventType, packets, params ) );
		}
	}

	private void notifyResponseRecieved( CommunityControllerEventType eventType, JsonPacket[] packets, ArrayList<QueryParameter> params ) {
		for( CommunityControllerListener listener : this.listeners ) {
			listener.responseRecieved( new CommunityControllerEvent( eventType, packets, params ) );
		}
	}
}
