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

import java.lang.reflect.InvocationTargetException;

/**
 * @author Kyle J. Harms
 */
public class FxProjectExampleExtractor {

	private final java.io.File projectFile;

	public FxProjectExampleExtractor( java.io.File projectFile ) {
		this.projectFile = projectFile;
	}

	public javafx.scene.image.Image extractScene() {
		assert javafx.application.Platform.isFxApplicationThread();

		// Convert to thumbnail to javafx image
		java.util.concurrent.Callable<javafx.scene.image.Image> callable = new java.util.concurrent.Callable<javafx.scene.image.Image>() {
			@Override
			public javafx.scene.image.Image call() throws Exception {
				javafx.scene.image.Image scene = org.lgna.project.io.IoUtilities.readThumbnailFx( projectFile );
				assert scene != null;
				return scene;
			}
		};
		return edu.wustl.lookingglass.croquetfx.ThreadHelper.invokeInFxThreadAndWait( callable );
	}

	public javafx.embed.swing.SwingNode extractSnippet( boolean runLater ) {
		assert javafx.application.Platform.isFxApplicationThread();

		org.lgna.project.Project project;
		try {
			project = org.lgna.project.io.IoUtilities.readProject( this.projectFile );
		} catch( java.io.IOException | org.lgna.project.VersionNotSupportedException | edu.wustl.lookingglass.project.VersionExceedsCurrentException e ) {
			// These are shipped with the IDE. This should never occur.
			throw new RuntimeException( e );
		}

		javafx.embed.swing.SwingNode snippetPane = new javafx.embed.swing.SwingNode();
		Runnable snippetRunnable = ( ) -> {
			// The code snippet pane
			org.lgna.project.ast.NamedUserType sceneType = org.alice.stageide.ast.StoryApiSpecificAstUtilities.getSceneTypeFromProgramType( project.getProgramType() );
			java.util.List<org.lgna.project.ast.UserMethod> methods = org.alice.stageide.ast.StoryApiSpecificAstUtilities.getUserMethodsInvokedSceneActivationListeners( sceneType );
			org.lgna.project.ast.BlockStatement body = methods.get( 0 ).body.getValue();
			org.lgna.project.ast.Statement statement;
			if( body.statements.size() == 1 ) {
				statement = body.statements.get( 0 );
			} else {
				// note: destroying original.  should we copy first???
				org.lgna.project.ast.DoInOrder doInOrder = new org.lgna.project.ast.DoInOrder();
				doInOrder.body.setValue( body );
				statement = doInOrder;
			}
			org.alice.ide.common.AbstractStatementPane pane = org.alice.ide.x.PreviewAstI18nFactory.getInstance().createStatementPane( statement );

			// HACK: So I can't figure out how to let the transparent parts of a SwingNode be transparent in the scene graph...
			// This just let's me make them a color that I won't notice...
			pane.getAwtComponent().setBorder( javax.swing.BorderFactory.createLineBorder( new java.awt.Color( 146, 146, 146 ) ) );

			// HACK: I don't think they see the action ordering box text... give it some more space...
			// Note: I tried to this, for all components (not just the top level)... but that statement factory stuff is so abstracted I couldn't figure anything out.
			( (javax.swing.JComponent)pane.getAwtComponent().getComponent( 0 ) ).setBorder( javax.swing.BorderFactory.createEmptyBorder( 0, 6, 0, 0 ) );

			snippetPane.setContent( pane.getAwtComponent() );
		};

		if( runLater ) {
			javax.swing.SwingUtilities.invokeLater( snippetRunnable );
			return snippetPane;
		} else {
			try {
				javax.swing.SwingUtilities.invokeAndWait( snippetRunnable );
				return snippetPane;
			} catch( InvocationTargetException | InterruptedException e ) {
				return null;
			}
		}
	}
}
