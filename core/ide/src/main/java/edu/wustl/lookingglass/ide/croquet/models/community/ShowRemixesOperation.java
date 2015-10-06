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
package edu.wustl.lookingglass.ide.croquet.models.community;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;

import org.alice.ide.IDE;
import org.alice.ide.ReasonToDisableSomeAmountOfRendering;
import org.alice.ide.operations.InconsequentialActionOperation;
import org.alice.ide.perspectives.ProjectPerspective;
import org.alice.ide.uricontent.UriContentLoader;
import org.alice.ide.uricontent.UriContentLoader.MutationPlan;
import org.lgna.croquet.CancelException;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.project.Project;

import edu.wustl.lookingglass.ide.SetPerspectiveOperation;
import edu.wustl.lookingglass.ide.perspectives.dinah.DinahRemixPerspective;
import edu.wustl.lookingglass.ide.perspectives.dinah.DinahUseRemixPerspective;
import edu.wustl.lookingglass.ide.uricontent.CommunityProjectLoader;
import edu.wustl.lookingglass.ide.uricontent.CommunitySnippetLoader;
import edu.wustl.lookingglass.remix.SnippetScript;
import edu.wustl.lookingglass.remix.roles.CharacterSelectionOperation;
import edu.wustl.lookingglass.utilities.DialogUtilities;

public class ShowRemixesOperation extends InconsequentialActionOperation {

	private static class SingletonHolder {
		private static ShowRemixesOperation instance = new ShowRemixesOperation();
	}

	public static ShowRemixesOperation getInstance() {
		return SingletonHolder.instance;
	}

	private ShowRemixesOperation() {
		super( java.util.UUID.fromString( "2d0e9358-974c-42bf-a13a-64f55b934b8a" ) );
	}

	@Override
	protected void performInternal( CompletionStep<?> step ) {
		IDE.getActiveInstance().getSceneEditor().disableRendering( ReasonToDisableSomeAmountOfRendering.MODAL_DIALOG_WITH_RENDER_WINDOW_OF_ITS_OWN );

		try {
			SelectProjectToCaptureRemixComposite remixDialog = new SelectProjectToCaptureRemixComposite();
			remixDialog.getValueCreator().fire( step.getTrigger() );

			UriContentLoader<?> uriContentLoader = remixDialog.createValue();

			if( uriContentLoader instanceof CommunityProjectLoader ) {
				handleProjectValue( (CommunityProjectLoader)uriContentLoader, step );
			} else if( uriContentLoader instanceof CommunitySnippetLoader ) {
				handleSnippetValue( (CommunitySnippetLoader)uriContentLoader, step );
			}

		} catch( CancelException e ) {
			step.cancel();
		} finally {
			IDE.getActiveInstance().getSceneEditor().enableRendering( ReasonToDisableSomeAmountOfRendering.MODAL_DIALOG_WITH_RENDER_WINDOW_OF_ITS_OWN );
		}
	}

	private void handleProjectValue( CommunityProjectLoader loader, CompletionStep<?> step ) {
		new ShowLoadingDialogSwingWorker<Project>() {

			@Override
			public Project getContent() throws ExecutionException, InterruptedException {
				return loader.getContentWaitingIfNecessary( MutationPlan.WILL_MUTATE );
			}

			@Override
			public void processContent( Project project ) {
				if( project != null ) {
					SetPerspectiveOperation operation = new SetPerspectiveOperation( DinahRemixPerspective.class) {

						@Override
						protected ProjectPerspective createInstance() {
							DinahUseRemixPerspective remixPerspective = new DinahUseRemixPerspective( IDE.getActiveInstance().getDocumentFrame() );
							remixPerspective.pushStashAndLoad( loader );
							return remixPerspective;
						}

					};
					operation.fire( step.getTrigger() );
				} else {
					DialogUtilities.showErrorDialog( SwingUtilities.getWindowAncestor( getFrame().getAwtComponent() ), "We had trouble loading the project to remix from. Please try again." );
				}
			}
		}.execute();

	}

	private void handleSnippetValue( CommunitySnippetLoader loader, CompletionStep<?> step ) {
		new ShowLoadingDialogSwingWorker<SnippetScript>() {
			@Override
			public SnippetScript getContent() throws ExecutionException, InterruptedException {
				return loader.getContentWaitingIfNecessary( MutationPlan.PROMISE_NOT_TO_MUTATE );
			}

			@Override
			public void processContent( SnippetScript snippetScript ) {
				if( snippetScript != null ) {
					CharacterSelectionOperation operation = new CharacterSelectionOperation( snippetScript, IDE.getActiveInstance().getProject() );
					operation.fire( step.getTrigger() );
				} else {
					DialogUtilities.showErrorDialog( SwingUtilities.getWindowAncestor( getFrame().getAwtComponent() ), "We had trouble loading this remix. Please try again." );
				}
			}
		}.execute();
	}
}
