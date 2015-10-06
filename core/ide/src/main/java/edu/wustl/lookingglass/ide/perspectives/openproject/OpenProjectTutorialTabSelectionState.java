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

import java.net.URL;

import org.alice.ide.browser.BrowserOperation;
import org.lgna.croquet.Application;
import org.lgna.croquet.MutableDataTabState;
import org.lgna.croquet.views.ExternalHyperlink;

/**
 * @author Michael Pogran
 */
public class OpenProjectTutorialTabSelectionState extends MutableDataTabState<TutorialTabComposite> {
	private final CreateTabComposite myCreateTabComposite = new CreateTabComposite();
	private final RemixTabComposite myRemixTabComposite = new RemixTabComposite();
	private final ShareTabComposite myShareTabComposite = new ShareTabComposite();

	public OpenProjectTutorialTabSelectionState() {
		super( Application.DOCUMENT_UI_GROUP, java.util.UUID.fromString( "38988485-7ae8-4ee6-b636-9849edf519f0" ), org.alice.ide.croquet.codecs.SingletonCodec.getInstance( TutorialTabComposite.class ) );
		this.addItem( null );
		this.addItem( myCreateTabComposite );
		this.addItem( myRemixTabComposite );
		this.addItem( myShareTabComposite );

		this.setValueTransactionlessly( myCreateTabComposite );
	}

	private static class CreateTabComposite extends TutorialTabComposite {

		public CreateTabComposite() {
			super( java.util.UUID.fromString( "1fa15a1b-2a7d-4398-af52-4bffb1a67c40" ) );
			this.setDescription( "Looking Glass allows users to build animated stories. Starting with a community template can help users get started quickly, because they provide an engaging context in which to build a story." );
			this.addTutorialStep( "Pick a community template and open it.", edu.wustl.lookingglass.ide.LookingGlassTheme.getImage( "tutorial-create-1", org.lgna.croquet.icon.IconSize.FIXED ) );
			this.addTutorialStep( "Drag and drop actions into your story.", edu.wustl.lookingglass.ide.LookingGlassTheme.getImage( "tutorial-create-2", org.lgna.croquet.icon.IconSize.FIXED ) );
			this.addTutorialStep( "Click \"Play\" to watch your story!", edu.wustl.lookingglass.ide.LookingGlassTheme.getImage( "tutorial-create-3", org.lgna.croquet.icon.IconSize.FIXED ) );

			BrowserOperation openTemplate = new BrowserOperation( java.util.UUID.fromString( "a7d9cd3c-f80c-49e0-9a88-58055524091a" ) ) {
				@Override
				protected URL getUrl() {
					URL url = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getOpenTemplateTutorialUrl();
					if( url != null ) {
						return url;
					} else {
						return edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAbsoluteUrl( "/" );
					}
				}

				@Override
				protected void localize() {
					super.localize();
					this.setName( "Opening A Community Template" );
				}
			};

			BrowserOperation animateStory = new BrowserOperation( java.util.UUID.fromString( "035a8c3a-e864-48cd-a073-d6a841813708" ) ) {
				@Override
				protected URL getUrl() {
					URL url = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAnimateStoryTutorialUrl();
					if( url != null ) {
						return url;
					} else {
						return edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAbsoluteUrl( "/" );
					}
				}

				@Override
				protected void localize() {
					super.localize();
					this.setName( "Animating Stories" );
				}
			};

			ExternalHyperlink openLink = openTemplate.createExternalHyperlink();
			ExternalHyperlink animateLink = animateStory.createExternalHyperlink();
			openLink.setBorder( javax.swing.BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
			animateLink.setBorder( javax.swing.BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
			this.getView().getVideoPanel().addComponent( openLink );
			this.getView().getVideoPanel().addComponent( animateLink );
		}
	}

	private static class RemixTabComposite extends TutorialTabComposite {

		public RemixTabComposite() {
			super( java.util.UUID.fromString( "7ee39e05-e307-4226-84d4-83398da0f04a" ) );
			this.setDescription( "Through remixing, you can take your favorite animations from other world projects and add them to the characters and props in your scene!" );
			this.addTutorialStep( "Click \"Remix\".", edu.wustl.lookingglass.ide.LookingGlassTheme.getImage( "tutorial-remix-1", org.lgna.croquet.icon.IconSize.FIXED ) );
			this.addTutorialStep( "Pick a world to remix.", edu.wustl.lookingglass.ide.LookingGlassTheme.getImage( "tutorial-remix-2", org.lgna.croquet.icon.IconSize.FIXED ) );
			this.addTutorialStep( "Pick a \"Start\" and an \"End\" for your remix.", edu.wustl.lookingglass.ide.LookingGlassTheme.getImage( "tutorial-remix-3", org.lgna.croquet.icon.IconSize.FIXED ) );

			BrowserOperation remixActions = new BrowserOperation( java.util.UUID.fromString( "ff1e81bd-2884-479d-bb12-24b549a402b6" ) ) {
				@Override
				protected URL getUrl() {
					URL url = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getRemixActionsTutorialUrl();
					if( url != null ) {
						return url;
					} else {
						return edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAbsoluteUrl( "/" );
					}
				}

				@Override
				protected void localize() {
					super.localize();
					this.setName( "Remixing Actions" );
				}
			};

			ExternalHyperlink remixLink = remixActions.createExternalHyperlink();
			remixLink.setBorder( javax.swing.BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
			this.getView().getVideoPanel().addComponent( remixLink );
		}

	}

	private static class ShareTabComposite extends TutorialTabComposite {

		public ShareTabComposite() {
			super( java.util.UUID.fromString( "c81146a5-d56f-4f26-a0d2-780c1bbba3e0" ) );
			this.setDescription( "Users can share the worlds they create in Looking Glass online through the Looking Glass Community." );
			this.addTutorialStep( "Click \"Share\".", edu.wustl.lookingglass.ide.LookingGlassTheme.getImage( "tutorial-share-1", org.lgna.croquet.icon.IconSize.FIXED ) );
			this.addTutorialStep( "Record your story.", edu.wustl.lookingglass.ide.LookingGlassTheme.getImage( "tutorial-share-2", org.lgna.croquet.icon.IconSize.FIXED ) );
			this.addTutorialStep( "Give it a title, description, and share your story on the web!", edu.wustl.lookingglass.ide.LookingGlassTheme.getImage( "tutorial-share-3", org.lgna.croquet.icon.IconSize.FIXED ) );

			BrowserOperation shareOnline = new BrowserOperation( java.util.UUID.fromString( "54a2e3e1-d921-4f4b-81d7-8f50eaf65d41" ) ) {
				@Override
				protected URL getUrl() {
					URL url = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getShareWorldTutorialUrl();
					if( url != null ) {
						return url;
					} else {
						return edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAbsoluteUrl( "/" );
					}
				}

				@Override
				protected void localize() {
					super.localize();
					this.setName( "Share Your Story Online" );
				}
			};

			ExternalHyperlink shareLink = shareOnline.createExternalHyperlink();
			shareLink.setBorder( javax.swing.BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
			this.getView().getVideoPanel().addComponent( shareLink );
		}

	}

}
