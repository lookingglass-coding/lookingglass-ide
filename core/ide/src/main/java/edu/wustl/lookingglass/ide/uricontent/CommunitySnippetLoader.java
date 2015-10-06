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

import org.alice.ide.uricontent.UriContentLoader;
import org.lgna.project.VersionNotSupportedException;

import edu.wustl.lookingglass.community.api.packets.SnippetPacket;
import edu.wustl.lookingglass.community.exceptions.CommunityApiException;
import edu.wustl.lookingglass.remix.SnippetScript;

/**
 * @author Caitlin Kelleher
 */
public class CommunitySnippetLoader extends UriContentLoader<SnippetScript> {

	private final SnippetPacket snippetPacket;

	public CommunitySnippetLoader( SnippetPacket remixPacket ) {
		this.snippetPacket = remixPacket;
	}

	public SnippetPacket getRemixPacket() {
		return this.snippetPacket;
	}

	@Override
	public String getTitle() {
		return this.snippetPacket.getTitle();
	}

	@Override
	public String getDescription() {
		return this.snippetPacket.getDescription();
	}

	public java.net.URL getVideoURL() {
		return edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAbsoluteUrl( this.snippetPacket.getVideoPath() );
	}

	@Override
	protected Image loadThumbnail() {
		Image thumbnail = null;
		try {
			thumbnail = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().downloadSnippetPoster( snippetPacket );
		} catch( CommunityApiException | IOException e ) {
			e.printStackTrace();
		}
		return thumbnail;
	}

	@Override
	public URI getUri() {
		return null;
	}

	@Override
	protected SnippetScript load() {

		// todo: figure out how to handle errors well
		try {
			return edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().downloadSnippetScript( snippetPacket );
		} catch( CommunityApiException | IOException | VersionNotSupportedException e ) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected boolean isWorkerCachingAppropriate( MutationPlan intention ) {
		return true;
	}

	@Override
	protected SnippetScript createCopyIfNecessary( SnippetScript value ) {
		return value;
	}
}
