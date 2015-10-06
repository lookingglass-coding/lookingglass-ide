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

import org.alice.ide.IDE;
import org.alice.ide.uricontent.UriProjectLoader;
import org.lgna.croquet.Application;
import org.lgna.croquet.CancelException;
import org.lgna.croquet.ImmutableDataTabState;
import org.lgna.croquet.Operation;
import org.lgna.croquet.edits.Edit;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.croquet.meta.MetaState;
import org.lgna.croquet.meta.TransactionHistoryTrackingMetaState;

import edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.ExistingProjectComposite;
import edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.NewProjectComposite;
import edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.OpenProjectTab;
import edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.SelectPuzzleComposite;
import edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.WelcomeComposite;
import edu.wustl.lookingglass.ide.perspectives.openproject.views.OpenProjectView;

/**
 * @author Caitlin Kelleher
 */
public class OpenProjectComposite extends org.lgna.croquet.SimpleComposite<OpenProjectView> {

	private final NewProjectComposite newTab = new NewProjectComposite();
	private final ExistingProjectComposite existingTab = new ExistingProjectComposite();
	private final WelcomeComposite welcomeTab = new WelcomeComposite();
	private final SelectPuzzleComposite puzzleTab = new SelectPuzzleComposite();

	private final Operation returnToPreviousProjectOperation = this.createActionOperation( "returnToPreviousProjectOperation", new Action() {

		@Override
		public Edit perform( CompletionStep<?> step, org.lgna.croquet.AbstractComposite.InternalActionOperation source ) throws CancelException {
			edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().setPerspective( IDE.getActiveInstance().getDocumentFrame().getCodePerspective() );
			return null;
		}
	} );

	private final ImmutableDataTabState<OpenProjectTab> tabState = this.createImmutableTabState( "tabState", 0, OpenProjectTab.class, this.welcomeTab, this.newTab, this.existingTab, this.puzzleTab );

	private final class SelectedUriProjectLoaderMetaState extends TransactionHistoryTrackingMetaState<UriProjectLoader> {

		@Override
		public UriProjectLoader getValue() {
			OpenProjectTab activeTab = tabState.getValue();
			return activeTab != null ? activeTab.getSelectedUriProjectLoader() : null;
		}

	}

	private final SelectedUriProjectLoaderMetaState metaState = new SelectedUriProjectLoaderMetaState();

	public MetaState<UriProjectLoader> getMetaState() {
		return this.metaState;
	}

	private ProjectDetailsComposite projectDetailsComposite = new ProjectDetailsComposite( this );

	public OpenProjectComposite() {
		super( java.util.UUID.fromString( "b2b0b211-c357-4ab9-9545-0d9007cf0f69" ) );
		this.registerSubComposite( projectDetailsComposite );
		for( OpenProjectTab tab : this.tabState ) {
			this.tabState.getItemSelectedState( tab ).setIconForBothTrueAndFalse( tab.getTabButtonIcon() );
		}
		this.localize();

		this.puzzleTab.setOpenProjectComposite( this );
	}

	@Override
	protected void localize() {
		super.localize();
		for( OpenProjectTab tab : this.tabState ) {
			this.tabState.getItemSelectedState( tab ).initializeIfNecessary();
		}
		this.tabState.getItemSelectedState( welcomeTab ).setTextForBothTrueAndFalse( this.findLocalizedText( "tabState.welcomeTab" ) );
		this.welcomeTab.setTitleText( this.findLocalizedText( "tabState.welcomeTab" ) );
		this.tabState.getItemSelectedState( newTab ).setTextForBothTrueAndFalse( this.findLocalizedText( "tabState.newTab" ) );
		this.newTab.setTitleText( this.findLocalizedText( "tabState.newTab" ) );
		this.tabState.getItemSelectedState( existingTab ).setTextForBothTrueAndFalse( this.findLocalizedText( "tabState.existingTab" ) );
		this.existingTab.setTitleText( this.findLocalizedText( "tabState.existingTab" ) );
		this.tabState.getItemSelectedState( puzzleTab ).setTextForBothTrueAndFalse( this.findLocalizedText( "tabState.puzzleTab" ) );
		this.puzzleTab.setTitleText( this.findLocalizedText( "tabState.puzzleTab" ) );
	}

	public ImmutableDataTabState<OpenProjectTab> getTabState() {
		return this.tabState;
	}

	public Operation getReturnToPreviousProjectOperation() {
		return this.returnToPreviousProjectOperation;
	}

	public NewProjectComposite getNewProjectComposite() {
		return this.newTab;
	}

	public ExistingProjectComposite getExistingProjectComposite() {
		return this.existingTab;
	}

	public WelcomeComposite getWelcomeComposite() {
		return this.welcomeTab;
	}

	@Override
	protected OpenProjectView createView() {
		return new OpenProjectView( this );
	}

	public ProjectDetailsComposite getProjectDetailsComposite() {
		return this.projectDetailsComposite;
	}

	public void setURIForReturn() {
		java.awt.image.BufferedImage returnThumbnail = null;
		try {
			returnThumbnail = edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().createThumbnail();
		} catch( Throwable e ) {
			e.printStackTrace();
		}
		if( returnThumbnail != null ) {
			getView().getSidePanelTabbedPane().setReturnIcon( returnThumbnail );
		}
		getView().getSidePanelTabbedPane().showReturnButton();
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		this.metaState.pushActivation( Application.getActiveInstance().getApplicationOrDocumentTransactionHistory() );
	}

	@Override
	public void handlePostDeactivation() {
		super.handlePostDeactivation();
		this.metaState.popActivation();
		for( OpenProjectTab tab : this.tabState ) {
			tab.handlePerspectiveDeactivation();
		}
		this.releaseView();
	}
}
