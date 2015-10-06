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
package edu.wustl.lookingglass.ide.croquet.models.preview;

import edu.wustl.lookingglass.ide.LookingGlassIDE;

/**
 * @author Kyle J. Harms
 */
public class PreviewSnippetComposite extends org.lgna.croquet.SimpleOperationInputDialogCoreComposite<edu.wustl.lookingglass.ide.croquet.models.community.views.PreviewWithTitleDescriptionPanel> {

	private edu.wustl.lookingglass.ide.uricontent.CommunitySnippetLoader snippetLoader;
	private final edu.wustl.lookingglass.ide.croquet.models.community.DetailWithVideoComposite detailWithVideoComposite;

	public PreviewSnippetComposite() {
		super( java.util.UUID.fromString( "71d443e1-e835-4a69-867a-dc72c4917c68" ), org.alice.ide.ProjectApplication.PROJECT_GROUP );
		this.detailWithVideoComposite = new edu.wustl.lookingglass.ide.croquet.models.community.DetailWithVideoComposite();
		this.registerSubComposite( this.detailWithVideoComposite );
	}

	@Override
	protected edu.wustl.lookingglass.ide.croquet.models.community.views.PreviewWithTitleDescriptionPanel createView() {
		return this.detailWithVideoComposite.getView();
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();

		// This is absolutely ridiculous... If we tried to update this video when it doesn't
		// exist we'll get an exception. So for now we'll just hack this with an invoke later
		// and cross our fingers that this fragile timing doesn't break.
		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			if( this.snippetLoader != null ) {
				this.detailWithVideoComposite.update( this.snippetLoader );
				this.detailWithVideoComposite.getVideoPlayer().playResume();
			}
		} );
	}

	public void setSnippetLoader( edu.wustl.lookingglass.ide.uricontent.CommunitySnippetLoader snippetLoader ) {
		this.snippetLoader = snippetLoader;
	}

	@Override
	protected java.awt.Dimension calculateWindowSize( org.lgna.croquet.views.AbstractWindow<?> window ) {
		// TODO: This seems so wrong... but I can't figure out how to make croquet do this...
		return new java.awt.Dimension( 500, 600 );
	}

	@Override
	protected org.lgna.croquet.AbstractSeverityStatusComposite.Status getStatusPreRejectorCheck( org.lgna.croquet.history.CompletionStep<?> step ) {
		return null;
	}

	@Override
	protected org.lgna.croquet.edits.Edit createEdit( org.lgna.croquet.history.CompletionStep<?> completionStep ) {
		return null;
	}

	@Override
	public void perform( org.lgna.croquet.OwnedByCompositeOperationSubKey subKey, org.lgna.croquet.history.CompletionStep<?> completionStep ) {
		super.perform( subKey, completionStep );
		if( !completionStep.isCanceled() && ( this.snippetLoader != null ) ) {
			try {
				edu.wustl.lookingglass.remix.SnippetScript script = this.snippetLoader.getContentWaitingIfNecessary( org.alice.ide.uricontent.UriContentLoader.MutationPlan.PROMISE_NOT_TO_MUTATE );

				if( script != null ) {
					edu.wustl.lookingglass.remix.roles.CharacterSelectionOperation operation = new edu.wustl.lookingglass.remix.roles.CharacterSelectionOperation( script, LookingGlassIDE.getActiveInstance().getProject(), true );
					operation.fire();
				}
			} catch( InterruptedException | java.util.concurrent.ExecutionException e ) {
				e.printStackTrace();
			}
		}
	}
}
