/*******************************************************************************
 * Copyright (c) 2008, 2017, Washington University in St. Louis.
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
package edu.wustl.lookingglass.ide.operations;

import org.alice.ide.ProjectApplication;
import org.alice.ide.croquet.models.projecturi.SaveProjectOperation;
import org.alice.ide.operations.InconsequentialActionOperation;
import org.alice.ide.uricontent.FileProjectLoader;
import org.lgna.croquet.history.CompletionStep;

/**
 * @author Kyle J. Harms
 */
public class OpenProjectBrowseOperation extends InconsequentialActionOperation {

	public OpenProjectBrowseOperation() {
		super( java.util.UUID.fromString( "47d8fc09-89b2-4709-8ac7-7d15836bc50c" ) );
	}

	@Override
	protected void performInternal( CompletionStep<?> step ) {
		java.io.File file = org.lgna.croquet.Application.getActiveInstance().getDocumentFrame().showOpenFileDialog( ProjectApplication.getMyProjectsDirectory(), null, org.lgna.project.io.IoUtilities.PROJECT_EXTENSION, true );
		if( file != null ) {
			FileProjectLoader loader = new FileProjectLoader( file );

			org.alice.ide.ProjectApplication application = org.alice.ide.ProjectApplication.getActiveInstance();

			if( application.isProjectUpToDateWithFile() ) {
				//pass
			} else {
				edu.cmu.cs.dennisc.javax.swing.option.YesNoCancelResult result = new edu.cmu.cs.dennisc.javax.swing.option.YesNoCancelDialog.Builder( "Opening a new world will close the world you were working on.  Would you like to save it?" )
						.title( "Save changed world?" )
						.buildAndShow();
				if( result == edu.cmu.cs.dennisc.javax.swing.option.YesNoCancelResult.YES ) {
					SaveProjectOperation.getInstance().fire();
				} else if( result == edu.cmu.cs.dennisc.javax.swing.option.YesNoCancelResult.CANCEL ) {
					return;
				}
			}
			edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().loadProjectFrom( loader );
		}
	}
}
