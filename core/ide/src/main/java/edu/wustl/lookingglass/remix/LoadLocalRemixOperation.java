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
package edu.wustl.lookingglass.remix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;

import org.alice.ide.perspectives.ProjectPerspective;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;

/**
 * @author Michael Pogran
 */
public class LoadLocalRemixOperation extends org.lgna.croquet.Operation {

	public LoadLocalRemixOperation() {
		super( edu.wustl.lookingglass.remix.models.ReuseGroup.REUSE_GROUP, java.util.UUID.fromString( "72b1fc85-2865-4c26-8250-d0bf6bbdf247" ) );
	}

	@Override
	protected void perform( Transaction transaction, Trigger trigger ) {
		String homeDir = System.getProperty( "user.home" );

		java.io.FilenameFilter filter = new java.io.FilenameFilter() {

			@Override
			public boolean accept( File dir, String name ) {
				int index = name.lastIndexOf( "." );
				String ext = name.substring( index + 1, name.length() );
				return ext.contentEquals( "lgr" ) || ext.contentEquals( "lgp" );
			}
		};
		java.io.File initDir = new java.io.File( homeDir + "/Documents/Looking Glass/Snippets" );

		java.io.File file = org.lgna.croquet.Application.getActiveInstance().getDocumentFrame().showOpenFileDialog( java.util.UUID.randomUUID(), "Open File To Remix", initDir, null, filter );
		if( file != null ) {
			String name = file.getName();
			int index = name.lastIndexOf( "." );
			String ext = name.substring( index + 1, name.length() );
			if( ext.contentEquals( "lgr" ) ) {

				SnippetScript script = null;
				String path = edu.cmu.cs.dennisc.java.io.FileUtilities.getCanonicalPathIfPossible( file );
				FileInputStream fis = null;
				try {
					fis = new FileInputStream( path );
				} catch( FileNotFoundException e ) {
					e.printStackTrace();
				}
				if( fis != null ) {
					try {
						script = edu.wustl.lookingglass.remix.SnippetFileUtilities.loadSnippet( fis );
					} catch( IOException e ) {
						e.printStackTrace();
					}
				}

				final SnippetScript remixScript = script;

				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						if( remixScript != null ) {
							edu.wustl.lookingglass.remix.roles.CharacterSelectionOperation operation = new edu.wustl.lookingglass.remix.roles.CharacterSelectionOperation( remixScript, edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().getProject() );
							operation.fire();
						}
					}
				} );
			} else {
				org.alice.ide.uricontent.FileProjectLoader projectLoader = new org.alice.ide.uricontent.FileProjectLoader( file );
				try {
					org.lgna.project.Project remixProject = projectLoader.getContentWaitingIfNecessary( org.alice.ide.uricontent.UriContentLoader.MutationPlan.PROMISE_NOT_TO_MUTATE );

					if( remixProject != null ) {
						org.alice.ide.IDE ide = org.alice.ide.IDE.getActiveInstance();
						edu.wustl.lookingglass.ide.perspectives.dinah.DinahUseRemixPerspective remixPerspective = new edu.wustl.lookingglass.ide.perspectives.dinah.DinahUseRemixPerspective( ide.getDocumentFrame() );

						org.lgna.croquet.Operation setRemixPerspective = new edu.wustl.lookingglass.ide.SetPerspectiveOperation( edu.wustl.lookingglass.ide.perspectives.dinah.DinahUseRemixPerspective.class) {

							@Override
							protected ProjectPerspective createInstance( ) {
								return remixPerspective;
							}
						};
						remixPerspective.pushStashAndLoad( projectLoader );
						setRemixPerspective.fire();
					}

				} catch( InterruptedException | ExecutionException e ) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void localize() {
		this.setName( "Remix From Disk..." );
	}
}
