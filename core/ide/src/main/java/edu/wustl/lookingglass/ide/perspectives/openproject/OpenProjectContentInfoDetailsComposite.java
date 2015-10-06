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
package edu.wustl.lookingglass.ide.perspectives.openproject;

import org.alice.ide.uricontent.BlankSlateProjectLoader;
import org.alice.ide.uricontent.FileProjectLoader;
import org.alice.ide.uricontent.UriProjectLoader;

import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.OpenProjectTab;
import edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.WelcomeComposite;
import edu.wustl.lookingglass.ide.uricontent.CommunityProjectLoader;

/**
 * @author Michael Pogran
 */
public class OpenProjectContentInfoDetailsComposite extends org.lgna.croquet.CardOwnerComposite {

	private final OpenProjectDetailWelcomeComposite welcomeDetailComposite = new OpenProjectDetailWelcomeComposite();
	private final OpenProjectDetailHelpComposite openProjectHelpDetailComposite = new OpenProjectDetailHelpComposite();
	private final OpenProjectDetailWithPreviewComposite openProjectDetailWithPreviewComposite = new OpenProjectDetailWithPreviewComposite();
	private final OpenProjectDetailWithoutPreviewComposite openProjectDetailWithoutPreviewComposite = new OpenProjectDetailWithoutPreviewComposite();

	private final OpenProjectComposite parentComposite;

	public OpenProjectContentInfoDetailsComposite( OpenProjectComposite parentComposite ) {
		super( java.util.UUID.fromString( "8b67c337-39f5-46a6-9d5c-ce739d676d6c" ) );
		this.parentComposite = parentComposite;

		this.addCard( welcomeDetailComposite );
		this.addCard( openProjectDetailWithPreviewComposite );
		this.addCard( openProjectDetailWithoutPreviewComposite );
		this.addCard( openProjectHelpDetailComposite );
	}

	public void handleMetaStateValueChanged( UriProjectLoader uriProjectLoader ) {

		if( uriProjectLoader == null ) {
			OpenProjectTab activeTab = this.parentComposite.getTabState().getValue();
			if( activeTab instanceof WelcomeComposite ) {
				this.showCard( welcomeDetailComposite );
			} else {
				this.showCard( openProjectHelpDetailComposite );
			}
		}
		else if( uriProjectLoader instanceof BlankSlateProjectLoader ) {
			// this is a blank template: show details without preview
			BlankSlateProjectLoader templateLoader = (BlankSlateProjectLoader)uriProjectLoader;
			openProjectDetailWithoutPreviewComposite.update( templateLoader );
			this.showCard( openProjectDetailWithoutPreviewComposite );
		}
		else if( uriProjectLoader instanceof CommunityProjectLoader ) {
			//this is a community template: show details without preview
			CommunityProjectLoader communityProjectLoader = (CommunityProjectLoader)uriProjectLoader;
			openProjectDetailWithoutPreviewComposite.update( communityProjectLoader );
			this.showCard( openProjectDetailWithoutPreviewComposite );
		}
		else if( uriProjectLoader instanceof FileProjectLoader ) {
			//this is a world: show detail with preview
			FileProjectLoader fileProjectLoader = (FileProjectLoader)uriProjectLoader;
			openProjectDetailWithPreviewComposite.update( fileProjectLoader );
			this.showCard( openProjectDetailWithPreviewComposite );
		}
		else {
			Logger.severe( "todo", uriProjectLoader );
		}
	}
}
