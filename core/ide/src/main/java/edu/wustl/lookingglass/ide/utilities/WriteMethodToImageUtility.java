/**
 * Copyright (c) 2008-2014, Washington University in St. Louis. All rights reserved.
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
 */
package edu.wustl.lookingglass.ide.utilities;

import java.io.IOException;

import org.lgna.project.io.IoUtilities;

/**
 * @author Michael Pogran
 */
public class WriteMethodToImageUtility {

	public static java.io.File[] getFiles( java.io.File dirFile ) {
		java.io.FileFilter worldFilter = new java.io.FileFilter() {

			@Override
			public boolean accept( java.io.File f ) {
				String name = f.getName();
				String extension = name.substring( name.lastIndexOf( "." ) + 1, name.length() );
				return extension.equals( IoUtilities.PROJECT_EXTENSION );
			}
		};
		return dirFile.listFiles( worldFilter );
	}

	public static void captureMain( java.io.File worldFile, java.io.File imageFile ) {
		captureMethod( null, worldFile, imageFile );
	}

	public static void captureMethod( String methodName, java.io.File worldFile, java.io.File imageFile ) {
		org.alice.ide.IDE.getActiveInstance().loadProjectFrom( worldFile );

		if( methodName != null ) {
			org.lgna.project.ast.NamedUserType programType = org.alice.ide.IDE.getActiveInstance().getProgramType();
			org.lgna.project.ast.NamedUserType sceneType = org.alice.stageide.ast.StoryApiSpecificAstUtilities.getSceneTypeFromProgramType( programType );

			org.lgna.project.ast.UserMethod method = sceneType.getDeclaredMethod( methodName );
			org.alice.ide.declarationseditor.DeclarationTabState declarationTabState = org.alice.ide.IDE.getActiveInstance().getDocumentFrame().getDeclarationsEditorComposite().getTabState();
			declarationTabState.setValueTransactionlessly( org.alice.ide.declarationseditor.CodeComposite.getInstance( method ) );
		}

		edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().getCodeEditorInFocus().getAwtComponent().validate();

		javax.swing.JScrollPane scrollPane = (javax.swing.JScrollPane)edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().getCodeEditorInFocus().getCenterComponent().getAwtComponent();
		javax.swing.JPanel codePanel = (javax.swing.JPanel)scrollPane.getViewport().getView();

		java.awt.image.BufferedImage image = new java.awt.image.BufferedImage( codePanel.getWidth(), codePanel.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB );

		javax.swing.CellRendererPane renderPane = new javax.swing.CellRendererPane();
		renderPane.add( codePanel );

		renderPane.paintComponent( image.createGraphics(), codePanel, renderPane, codePanel.getBounds() );
		try {
			javax.imageio.ImageIO.write( image, "PNG", imageFile );
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}
}
