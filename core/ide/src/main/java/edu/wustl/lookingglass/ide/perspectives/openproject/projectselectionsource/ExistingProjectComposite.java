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
package edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource;

import java.net.URI;

import org.alice.ide.croquet.models.projecturi.SaveProjectOperation;
import org.alice.ide.projecturi.DirectoryUriListData;
import org.alice.ide.recentprojects.RecentProjectsListData;
import org.alice.ide.uricontent.FileProjectLoader;
import org.alice.ide.uricontent.UriProjectLoader;
import org.lgna.croquet.CancelException;
import org.lgna.croquet.ImmutableDataSingleSelectListState;
import org.lgna.croquet.SingleSelectListState;
import org.lgna.croquet.StringState;
import org.lgna.croquet.edits.Edit;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.history.CompletionStep;

import edu.cmu.cs.dennisc.java.util.Lists;
import edu.wustl.lookingglass.ide.croquet.models.data.MyProjectsData;
import edu.wustl.lookingglass.ide.perspectives.openproject.SortState;
import edu.wustl.lookingglass.ide.perspectives.openproject.views.ExistingProjectView;

/**
 * @author Caitlin Kelleher
 */
public class ExistingProjectComposite extends OpenProjectTab {
	private final SingleSelectListState<FileProjectLoader, ?> projectsState;
	private final StringState searchState;
	private final ImmutableDataSingleSelectListState<SortState> sortState;

	private final MyProjectsData projectsData;
	private org.lgna.croquet.event.ValueListener<String> searchListener;

	private final org.lgna.croquet.Operation clearSearchOperation;
	private final org.lgna.croquet.Operation sortByDateOperation;
	private final org.lgna.croquet.Operation sortByNameOperation;
	private final org.lgna.croquet.Operation browseOperation;
	private final java.util.Set<org.lgna.croquet.Operation> sortOperations;

	private java.util.Timer searchTimer = new java.util.Timer();
	private java.util.TimerTask searchTask;

	public ExistingProjectComposite() {
		super( java.util.UUID.fromString( "9b4ccebf-f452-4c51-9540-34a151021bca" ) );

		java.util.List<URI> directoryData = Lists.newArrayList( new DirectoryUriListData( org.alice.ide.IDE.getActiveInstance().getMyProjectsDirectory() ).toArray() );
		java.util.List<FileProjectLoader> projects = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();

		for( URI uri : directoryData ) {
			UriProjectLoader projectLoader = UriProjectLoader.createInstance( uri );
			if( projectLoader instanceof FileProjectLoader ) {
				projects.add( (FileProjectLoader)projectLoader );
			}
		}

		for( URI uri : RecentProjectsListData.getInstance() ) {
			UriProjectLoader projectLoader = UriProjectLoader.createInstance( uri );
			if( projectLoader instanceof FileProjectLoader ) {
				if( projects.contains( projectLoader ) ) {
					//pass
				} else {
					projects.add( (FileProjectLoader)projectLoader );
				}
			}
		}

		this.projectsData = new MyProjectsData( projects );
		this.projectsState = this.createGenericListState( "projectsState", this.projectsData, -1 );
		this.searchState = this.createStringState( "searchState" );
		this.sortState = this.createImmutableListStateForEnum( "sortState", SortState.class, SortState.LAST_MODIFIED );

		if( projects.size() > 0 ) {
			this.projectsState.setSelectedIndex( 0 );
		}

		this.sortByDateOperation = this.sortState.getItemSelectionOperation( SortState.LAST_MODIFIED );

		this.sortByNameOperation = this.sortState.getItemSelectionOperation( SortState.NAME );

		this.clearSearchOperation = this.createActionOperation( "clearSearchOperation", new Action() {

			@Override
			public Edit perform( CompletionStep<?> step, org.lgna.croquet.AbstractComposite.InternalActionOperation source ) throws CancelException {
				searchState.setValueTransactionlessly( "" );
				return null;
			}
		} );

		this.browseOperation = this.createActionOperation( "browseOperation", new Action() {

			@Override
			public Edit perform( CompletionStep<?> step, org.lgna.croquet.AbstractComposite.InternalActionOperation source ) throws CancelException {
				java.io.File file = org.lgna.croquet.Application.getActiveInstance().getDocumentFrame().showOpenFileDialog( org.alice.ide.ProjectApplication.getActiveInstance().getMyProjectsDirectory(), null, org.lgna.project.io.IoUtilities.PROJECT_EXTENSION, true );
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
							return null;
						}
					}
					edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().loadProjectFrom( loader );
				}
				return null;
			}
		} );

		this.searchListener = new org.lgna.croquet.event.ValueListener<String>() {

			@Override
			public void valueChanged( ValueEvent<String> e ) {
				if( searchTask != null ) {
					searchTask.cancel();
				}

				if( e.getNextValue().isEmpty() ) {
					projectsData.resetValues();
				} else {
					if( e.getNextValue().length() >= 3 ) {
						searchTask = new SearchTask( e.getNextValue() );
						searchTimer.schedule( searchTask, 1000 );
					} else {
						if( e.getPreviousValue().length() >= 3 ) {
							projectsData.resetValues();
						}
					}
				}
			}
		};

		this.sortOperations = edu.cmu.cs.dennisc.java.util.Sets.newHashSet();
		this.sortOperations.add( this.sortByDateOperation );
		this.sortOperations.add( this.sortByNameOperation );
		this.sortState.addAndInvokeNewSchoolValueListener( new org.lgna.croquet.event.ValueListener<SortState>() {

			@Override
			public void valueChanged( ValueEvent<SortState> e ) {
				SortState nextValue = e.getNextValue();

				for( org.lgna.croquet.Operation operation : sortOperations ) {
					operation.setEnabled( true );
				}

				if( nextValue.equals( SortState.LAST_MODIFIED ) ) {
					projectsData.sortByDate();
					sortByDateOperation.setEnabled( false );

				}
				else if( nextValue.equals( SortState.NAME ) ) {
					projectsData.sortByName();
					sortByNameOperation.setEnabled( false );
				}
			}

		} );
	}

	@Override
	public UriProjectLoader getSelectedUriProjectLoader() {
		return this.projectsState.getValue();
	}

	@Override
	public javax.swing.Icon getTabButtonIcon() {
		return edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "start-open", org.lgna.croquet.icon.IconSize.FIXED );
	}

	public SingleSelectListState<FileProjectLoader, ?> getProjectsState() {
		return this.projectsState;
	}

	public StringState getSearchState() {
		return this.searchState;
	}

	public org.lgna.croquet.Operation getClearSearchOperation() {
		return this.clearSearchOperation;
	}

	public org.lgna.croquet.Operation getSortByDateOperation() {
		return this.sortByDateOperation;
	}

	public org.lgna.croquet.Operation getSortByNameOperation() {
		return this.sortByNameOperation;
	}

	public org.lgna.croquet.Operation getBrowseOperation() {
		return this.browseOperation;
	}

	@Override
	protected ExistingProjectView createView() {
		return new ExistingProjectView( this );
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		this.searchState.addAndInvokeNewSchoolValueListener( searchListener );
	}

	@Override
	public void handlePostDeactivation() {
		this.searchState.removeNewSchoolValueListener( searchListener );
		super.handlePostDeactivation();
	}

	private class SearchTask extends java.util.TimerTask {
		private final String searchTerm;

		public SearchTask( String searchTerm ) {
			this.searchTerm = searchTerm.trim();
		}

		@Override
		public void run() {
			javax.swing.SwingUtilities.invokeLater( ( ) -> {
				getProjectsState().clearSelection();
				projectsData.setSearch( this.searchTerm );

				if( getProjectsState().getItemCount() > 0 ) {
					getProjectsState().setSelectedIndex( 0 );
				}
			} );
		}
	}

	@Override
	public void handlePerspectiveDeactivation() {
		this.projectsData.clearCachedThumbnails();
		this.releaseView();
	}
}
