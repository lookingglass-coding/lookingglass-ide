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

import org.alice.ide.croquet.models.projecturi.SaveProjectOperation;
import org.alice.ide.perspectives.noproject.NoProjectPerspective;
import org.lgna.croquet.Application;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;

import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.LookingGlassTheme;

/**
 * @author Michael Pogran
 */
public class ReturnToOpenProjectPerspective extends org.lgna.croquet.Operation {

	public ReturnToOpenProjectPerspective() {
		super( Application.INHERIT_GROUP, java.util.UUID.fromString( "dba22566-3bf1-43b9-9a30-92f0456969bc" ) );
	}

	protected ReturnToOpenProjectPerspective( java.util.UUID uuid ) {
		super( Application.INHERIT_GROUP, uuid );
	}

	private static class SingletonHolder {
		private static ReturnToOpenProjectPerspective instance = new ReturnToOpenProjectPerspective();
	}

	public static ReturnToOpenProjectPerspective getInstance() {
		return SingletonHolder.instance;
	}

	@Override
	protected void localize() {
		super.localize();
		this.setButtonIcon( LookingGlassTheme.getIcon( "return", org.lgna.croquet.icon.IconSize.SMALL ) );
	}

	@Override
	public boolean isToolBarTextClobbered() {
		return false;
	}

	protected edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.OpenProjectTab getTabForReturn( OpenProjectComposite composite ) {
		return composite.getWelcomeComposite();
	}

	@Override
	protected void perform( Transaction transaction, Trigger trigger ) {
		org.alice.ide.ProjectApplication application = org.alice.ide.ProjectApplication.getActiveInstance();

		boolean isCanceled = false;
		if( application.isProjectUpToDateWithFile() ) {
			//pass
		} else {
			edu.cmu.cs.dennisc.javax.swing.option.YesNoCancelResult result = new edu.cmu.cs.dennisc.javax.swing.option.YesNoCancelDialog.Builder( "Your world has changed.  Would you like to save it?" )
					.title( "Save changed world?" )
					.buildAndShow();
			if( result == edu.cmu.cs.dennisc.javax.swing.option.YesNoCancelResult.YES ) {
				SaveProjectOperation.getInstance().fire();
			} else if( result == edu.cmu.cs.dennisc.javax.swing.option.YesNoCancelResult.NO ) {
				//pass
			} else {
				isCanceled = true;
			}
		}

		if( isCanceled ) {
			//pass
		} else {
			NoProjectPerspective noProjectPerspective = LookingGlassIDE.getActiveInstance().getDocumentFrame().getNoProjectPerspective();
			OpenProjectComposite openProjectComposite = noProjectPerspective.getMainComposite();
			openProjectComposite.setURIForReturn();

			LookingGlassIDE.getActiveInstance().setPerspective( noProjectPerspective );
			openProjectComposite.getTabState().setValueTransactionlessly( getTabForReturn( openProjectComposite ) );
		}
	}

}
