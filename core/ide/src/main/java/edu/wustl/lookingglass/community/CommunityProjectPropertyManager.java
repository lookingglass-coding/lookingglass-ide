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

public class CommunityProjectPropertyManager {

	// Project Properties
	private static final org.lgna.project.properties.StringPropertyKey PROJECT_TITLE_KEY = new org.lgna.project.properties.StringPropertyKey( java.util.UUID.fromString( "a8e60afe-766e-449f-9caa-c2c911148053" ), "Project Title" );
	private static final org.lgna.project.properties.StringPropertyKey PROJECT_DESCRIPTION_KEY = new org.lgna.project.properties.StringPropertyKey( java.util.UUID.fromString( "ba461524-721d-43f9-96f5-a36122f0e715" ), "Project Description" );
	private static final org.lgna.project.properties.IntegerPropertyKey PROJECT_USER_ID_KEY = new org.lgna.project.properties.IntegerPropertyKey( java.util.UUID.fromString( "fe6fd4a7-870b-43e0-b2a2-6b3ed17870e8" ), "User ID" );

	// Community Properites
	private static final org.lgna.project.properties.StringPropertyKey COMMUNITY_HOSTNAME_KEY = new org.lgna.project.properties.StringPropertyKey( java.util.UUID.fromString( "3474cbd9-2143-4cb8-82e7-0565a5935e4c" ), "Community Hostname" );

	private static final org.lgna.project.properties.IntegerPropertyKey COMMUNITY_PROJECT_ID_KEY = new org.lgna.project.properties.IntegerPropertyKey( java.util.UUID.fromString( "2428bf79-cd35-4ba2-b02b-09e6379e138b" ), "Project ID" );
	private static final org.lgna.project.properties.IntegerPropertyKey COMMUNITY_CHALLENGE_ID_KEY = new org.lgna.project.properties.IntegerPropertyKey( java.util.UUID.fromString( "36671b40-30a3-4b1a-87e0-832d820c11c7" ), "Challenge ID" );

	// this should be a ; separated list of the remix ids for which the current project is the remixer.
	private static final org.lgna.project.properties.StringPropertyKey COMMUNITY_PROJECT_REMIXED_WORLD_IDS_KEY = new org.lgna.project.properties.StringPropertyKey( java.util.UUID.fromString( "52926f07-8520-4966-87a3-aeb08a1350d0" ), "Project Remixed World IDs" );
	private static final org.lgna.project.properties.StringPropertyKey COMMUNITY_PROJECT_REMIXED_SNIPPET_IDS_KEY = new org.lgna.project.properties.StringPropertyKey( java.util.UUID.fromString( "18f70ffa-a8f0-46d7-a3c3-ef58fc138ed2" ), "Project Remixed Snippet IDs" );

	private static CommunityController communityController;

	/* package-private */static void initialize( CommunityController controller ) {
		CommunityProjectPropertyManager.communityController = controller;
	}

	public static boolean isCommunityMetadataValid( org.lgna.project.Project project ) {
		String hostname = getCommunityHostname( project );
		if( ( hostname == null ) || CommunityProjectPropertyManager.communityController.getHost().equals( hostname ) ) {
			// the current community and this project's host are the same
			return true;
		} else {
			return false;
		}
	}

	public static void validateCommunityMetadata( org.lgna.project.Project project ) {
		if( isCommunityMetadataValid( project ) ) {
			// the current community and this project's host are the same
		} else {
			final String hostname = getCommunityHostname( project );
			edu.cmu.cs.dennisc.java.util.logging.Logger.warning( "You are currently connected to " + CommunityProjectPropertyManager.communityController.getHost() + ", but your current project was created with " + hostname + ". You cannot mix projects with different community installations." );
			scrubCommunityMetadata( project );
		}

		// set the community hostname if necessary.
		if( project.getValueFor( COMMUNITY_HOSTNAME_KEY ) == null ) {
			project.putValueFor( COMMUNITY_HOSTNAME_KEY, CommunityProjectPropertyManager.communityController.getHost() );
		}
	}

	public static void scrubCommunityMetadata( org.lgna.project.Project project ) {
		edu.cmu.cs.dennisc.java.util.logging.Logger.warning( "scrubed project of community metadata" );
		project.removeValueFor( COMMUNITY_HOSTNAME_KEY );
		project.removeValueFor( COMMUNITY_PROJECT_ID_KEY );
		project.removeValueFor( COMMUNITY_CHALLENGE_ID_KEY );
		project.removeValueFor( COMMUNITY_PROJECT_REMIXED_WORLD_IDS_KEY );
		project.removeValueFor( COMMUNITY_PROJECT_REMIXED_SNIPPET_IDS_KEY );
	}

	private static Integer[] splitIdList( String idList ) {
		java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
		if( idList != null ) {
			String[] splitStrings = idList.split( ";" );
			for( String splitString : splitStrings ) {
				ids.add( new Integer( splitString ) );
			}
		}
		return ids.toArray( new Integer[ ids.size() ] );
	}

	/* package-private */static void setProjectTitle( org.lgna.project.Project project, String projectTitle ) {
		project.putValueFor( PROJECT_TITLE_KEY, projectTitle );
	}

	/* package-private */static void setProjectDescription( org.lgna.project.Project project, String projectDescription ) {
		project.putValueFor( PROJECT_DESCRIPTION_KEY, projectDescription );
	}

	/* package-private */static void setCommunityProjectID( org.lgna.project.Project project, Integer projectID ) {
		validateCommunityMetadata( project );
		project.putValueFor( COMMUNITY_PROJECT_ID_KEY, projectID );
	}

	/* package-private */static void setProjectUserID( org.lgna.project.Project project, Integer userID ) {
		validateCommunityMetadata( project );
		project.putValueFor( PROJECT_USER_ID_KEY, userID );
	}

	/* package-private */static void setCommunityTemplateID( org.lgna.project.Project project, Integer challengeID ) {
		validateCommunityMetadata( project );
		project.putValueFor( COMMUNITY_CHALLENGE_ID_KEY, challengeID );
	}

	public static void appendCommunityRemixedWorldId( org.lgna.project.Project project, int remixId ) {
		validateCommunityMetadata( project );
		String remixList = project.getValueFor( COMMUNITY_PROJECT_REMIXED_WORLD_IDS_KEY );

		if( remixList == null ) {
			remixList = Integer.toString( remixId ) + ";";
		} else {
			remixList += Integer.toString( remixId ) + ";";
		}

		project.putValueFor( COMMUNITY_PROJECT_REMIXED_WORLD_IDS_KEY, remixList );
	}

	public static void appendCommunityRemixedSnippetId( org.lgna.project.Project project, int remixId ) {
		validateCommunityMetadata( project );
		String currentRemixList = project.getValueFor( COMMUNITY_PROJECT_REMIXED_SNIPPET_IDS_KEY );

		if( currentRemixList == null ) {
			currentRemixList = Integer.toString( remixId ) + ";";
		} else {
			currentRemixList += Integer.toString( remixId ) + ";";
		}

		project.putValueFor( COMMUNITY_PROJECT_REMIXED_SNIPPET_IDS_KEY, currentRemixList );
	}

	public static String getProjectTitle( org.lgna.project.Project project ) {
		return project.getValueFor( PROJECT_TITLE_KEY );
	}

	public static String getProjectDescription( org.lgna.project.Project project ) {
		return project.getValueFor( PROJECT_DESCRIPTION_KEY );
	}

	public static String getCommunityHostname( org.lgna.project.Project project ) {
		// Don't verifyCommunityHostname, we are the hostname!
		return project.getValueFor( COMMUNITY_HOSTNAME_KEY );
	}

	public static Integer getCommunityProjectID( org.lgna.project.Project project ) {
		validateCommunityMetadata( project );
		return project.getValueFor( COMMUNITY_PROJECT_ID_KEY );
	}

	public static Integer getProjectUserID( org.lgna.project.Project project ) {
		validateCommunityMetadata( project );
		return project.getValueFor( PROJECT_USER_ID_KEY );
	}

	public static Integer getCommunityChallengeID( org.lgna.project.Project project ) {
		validateCommunityMetadata( project );
		return project.getValueFor( COMMUNITY_CHALLENGE_ID_KEY );
	}

	public static Integer[] getCommunityRemixedWorldIds( org.lgna.project.Project project ) {
		validateCommunityMetadata( project );
		return splitIdList( project.getValueFor( COMMUNITY_PROJECT_REMIXED_WORLD_IDS_KEY ) );
	}

	public static Integer[] getCommunityRemixedSnippetIds( org.lgna.project.Project project ) {
		validateCommunityMetadata( project );
		return splitIdList( project.getValueFor( COMMUNITY_PROJECT_REMIXED_SNIPPET_IDS_KEY ) );
	}
}
