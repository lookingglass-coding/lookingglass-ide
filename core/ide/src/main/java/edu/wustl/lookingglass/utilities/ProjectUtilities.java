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

import java.net.URI;
import java.net.URISyntaxException;

import edu.wustl.lookingglass.ide.uricontent.CommunityProjectLoader;

public class ProjectUtilities {

	public static final String CACHE_URI_SCHEME = "lg-cache";
	private static java.util.Map<Integer, org.lgna.project.Project> projectCache = new java.util.HashMap<Integer, org.lgna.project.Project>();

	public static java.net.URI createOnetimeURIForProject( org.lgna.project.Project project ) {
		java.net.URI rv = null;
		try {
			int projectHashCode = project.hashCode();
			projectCache.put( projectHashCode, project );
			rv = new java.net.URI( CACHE_URI_SCHEME, Integer.toString( projectHashCode ), null );
		} catch( URISyntaxException e ) {
			e.printStackTrace();
		}

		return rv;
	}

	public static boolean isProjectCacheURI( java.net.URI uri ) {
		return uri.getScheme().equalsIgnoreCase( CACHE_URI_SCHEME );
	}

	public static org.lgna.project.Project resolveOnetimeURI( java.net.URI uri ) {
		if( isProjectCacheURI( uri ) && projectCache.containsKey( Integer.parseInt( uri.getSchemeSpecificPart() ) ) ) {
			int key = Integer.parseInt( uri.getSchemeSpecificPart() );
			return projectCache.get( key );
		} else {
			return null;
		}
	}

	public static void removeOnetimeURI( URI uri ) {
		if( ( uri != null ) && isProjectCacheURI( uri ) ) {
			int key = Integer.parseInt( uri.getSchemeSpecificPart() );
			projectCache.remove( key );
		}
	}

	public static final String CHALLENGE_URI_SCHEME = "lg-challenge";

	public static java.net.URI createChallengeURIForWorldInfo( CommunityProjectLoader worldInfo ) {
		assert worldInfo != null;
		assert worldInfo.getProjectPacket() != null : worldInfo;

		java.net.URI uri = null;
		try {
			uri = new java.net.URI( CHALLENGE_URI_SCHEME, Integer.toString( worldInfo.getProjectPacket().getId() ), null );
		} catch( URISyntaxException e ) {
			e.printStackTrace();
		}
		return uri;
	}

	public static boolean isChallengeURI( java.net.URI uri ) {
		return uri.getScheme().equalsIgnoreCase( CHALLENGE_URI_SCHEME );
	}
}
