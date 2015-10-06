/*******************************************************************************
 * Copyright (c) 2006, 2015, Carnegie Mellon University. All rights reserved.
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
 * 3. Products derived from the software may not be called "Alice", nor may
 *    "Alice" appear in their name, without prior written permission of
 *    Carnegie Mellon University.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Carnegie Mellon University"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 * ANY AND ALL EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT
 * SHALL THE AUTHORS, COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package org.alice.ide;

import java.io.IOException;
import java.text.MessageFormat;

import org.lgna.project.ProgramTypeUtilities;
import org.lgna.project.Version;

import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.project.TypeClassNotFoundException;
import edu.wustl.lookingglass.study.StudyConfiguration;

/**
 * @author Dennis Cosgrove
 */
public abstract class ProjectApplication extends org.lgna.croquet.PerspectiveApplication<ProjectDocumentFrame> {
	public static final org.lgna.croquet.Group HISTORY_GROUP = org.lgna.croquet.Group.getInstance( java.util.UUID.fromString( "303e94ca-64ef-4e3a-b95c-038468c68438" ), "HISTORY_GROUP" );
	public static final org.lgna.croquet.Group URI_GROUP = org.lgna.croquet.Group.getInstance( java.util.UUID.fromString( "79bf8341-61a4-4395-9469-0448e66d9ac6" ), "URI_GROUP" );

	public static ProjectApplication getActiveInstance() {
		return edu.cmu.cs.dennisc.java.lang.ClassUtilities.getInstance( org.lgna.croquet.PerspectiveApplication.getActiveInstance(), ProjectApplication.class );
	}

	private org.lgna.croquet.undo.event.HistoryListener projectHistoryListener;

	public ProjectApplication( IdeConfiguration ideConfiguration, ApiConfigurationManager apiConfigurationManager ) {
		this.projectDocumentFrame = new ProjectDocumentFrame( ideConfiguration, apiConfigurationManager );
		this.projectHistoryListener = new org.lgna.croquet.undo.event.HistoryListener() {
			@Override
			public void operationPushing( org.lgna.croquet.undo.event.HistoryPushEvent e ) {
			}

			@Override
			public void operationPushed( org.lgna.croquet.undo.event.HistoryPushEvent e ) {
			}

			@Override
			public void insertionIndexChanging( org.lgna.croquet.undo.event.HistoryInsertionIndexEvent e ) {
			}

			@Override
			public void insertionIndexChanged( org.lgna.croquet.undo.event.HistoryInsertionIndexEvent e ) {
				ProjectApplication.this.handleInsertionIndexChanged( e );
			}

			@Override
			public void clearing( org.lgna.croquet.undo.event.HistoryClearEvent e ) {
			}

			@Override
			public void cleared( org.lgna.croquet.undo.event.HistoryClearEvent e ) {
			}
		};
		this.updateTitle();
	}

	@Override
	public org.alice.ide.ProjectDocumentFrame getDocumentFrame() {
		return this.projectDocumentFrame;
	}

	/* <lg> */public/* </lg> */void updateUndoRedoEnabled() {
		org.lgna.croquet.undo.UndoHistory historyManager = this.getProjectHistory( org.lgna.croquet.Application.PROJECT_GROUP );
		boolean isUndoEnabled;
		boolean isRedoEnabled;
		if( historyManager != null ) {
			int index = historyManager.getInsertionIndex();
			int size = historyManager.getStack().size();
			isUndoEnabled = index > 0;
			isRedoEnabled = index < size;
		} else {
			isUndoEnabled = false;
			isRedoEnabled = false;
		}

		ProjectDocumentFrame documentFrame = this.getDocumentFrame();
		documentFrame.getUndoOperation().setEnabled( isUndoEnabled );
		documentFrame.getRedoOperation().setEnabled( isRedoEnabled );
	}

	protected void handleInsertionIndexChanged( org.lgna.croquet.undo.event.HistoryInsertionIndexEvent e ) {
		this.updateTitle();
		org.lgna.croquet.undo.UndoHistory source = e.getTypedSource();
		if( source.getGroup() == PROJECT_GROUP ) {
			this.updateUndoRedoEnabled();
		}
	}

	public static final String getApplicationName() {
		/* <lg/> We are Looking Glass */
		return "Looking Glass";
	}

	public static final String getVersionText() {
		/* <lg/> We have our own version number too! */
		//return org.lgna.project.Version.getCurrentVersionText();
		return LookingGlassIDE.APPLICATION_VERSION.toString();
	}

	public static final String getVersionAdornment() {
		/* <lg/> Looking Glass doesn't use version adornment */
		return null;
	}

	public static final String getApplicationSubPath() {
		// <lg/> Looking Glass only uses its real name
		return getApplicationName();
	}

	// <lg>
	private String getUnableToOpenString( String key, Object[] args ) {
		MessageFormat formatter = new MessageFormat( edu.wustl.lookingglass.croquetfx.FxComponent.DEFAULT_RESOURCES.getString( key ) );
		return formatter.format( args );
	}

	public void showUnableToOpenFileDialog( java.io.File file, String message ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "Unable to open file" );
		if( file != null ) {
			sb.append( " " + edu.cmu.cs.dennisc.java.io.FileUtilities.getCanonicalPathIfPossible( file ) );
		}
		sb.append( ".\n\n" );
		sb.append( message );

		javax.swing.SwingUtilities.invokeLater( () -> {
			new edu.cmu.cs.dennisc.javax.swing.option.OkDialog.Builder( sb.toString() ).title( "Cannot read file" ).messageType( edu.cmu.cs.dennisc.javax.swing.option.MessageType.ERROR ).buildAndShow();
		} );
	}

	public void handleVersionNotSupported( java.io.File file, org.lgna.project.VersionNotSupportedException vnse ) {
		String message = getUnableToOpenString( "VersionNotSupported.message", new String[] { getApplicationName(), Double.toString( vnse.getVersion() ), Double.toString( vnse.getMinimumSupportedVersion() ) } );
		this.showUnableToOpenFileDialog( file, message );
	}

	public void handleVersionExceedsCurrent( java.io.File file ) {
		String message = getUnableToOpenString( "VersionExceedsCurrent.message", new String[] { file.getName(), getApplicationName() } );
		this.showUnableToOpenFileDialog( file, message );
	}

	public void handleVersionExceedsCurrent( String name ) {
		String message = getUnableToOpenString( "VersionExceedsCurrent.message", new String[] { name, getApplicationName() } );
		this.showUnableToOpenFileDialog( null, message );
	}

	public void showUnableToOpenProjectMessageDialog( java.io.File file, boolean isValidZip ) {
		String message = getUnableToOpenString( "UnableToOpenProjectMessage.message", new String[] { org.lgna.project.io.IoUtilities.PROJECT_EXTENSION } );
		this.showUnableToOpenFileDialog( file, message );
	}

	//</lg>

	private org.alice.ide.uricontent.UriProjectLoader uriProjectLoader;

	public org.alice.ide.uricontent.UriProjectLoader getUriProjectLoader() {
		return this.uriProjectLoader;
	}

	public final java.net.URI getUri() {
		return this.uriProjectLoader != null ? this.uriProjectLoader.getUri() : null;
	}

	private void setUriProjectPair( final org.alice.ide.uricontent.UriProjectLoader uriProjectLoader ) {
		this.uriProjectLoader = null;
		org.lgna.project.Project project;
		if( uriProjectLoader != null ) {
			try {
				project = uriProjectLoader.getContentWaitingIfNecessary( org.alice.ide.uricontent.UriContentLoader.MutationPlan.WILL_MUTATE );
			} catch( InterruptedException ie ) {
				throw new RuntimeException( ie );
			} catch( java.util.concurrent.ExecutionException ee ) {
				throw new RuntimeException( ee );
			}
		} else {
			project = null;
		}
		if( project != null ) {
			// Remove the old project history listener, so the old project can be cleaned up
			if( ( this.getProject() != null ) && ( this.getProjectHistory() != null ) ) {
				this.getProjectHistory().removeHistoryListener( this.projectHistoryListener );
			}
			this.setProject( project );
			this.uriProjectLoader = uriProjectLoader;
			this.getProjectHistory().addHistoryListener( this.projectHistoryListener );
			java.net.URI uri = this.uriProjectLoader.getUri();
			java.io.File file = edu.cmu.cs.dennisc.java.net.UriUtilities.getFile( uri );
			StudyConfiguration.INSTANCE.getStudyLogger().log( java.util.logging.Level.INFO, "opened project: " + uriProjectLoader.getUri() ); // <lg/>
			StudyConfiguration.INSTANCE.logActiveCode(); // <lg/>
			try {
				if( uriProjectLoader.isOpenedByUser() && ( file != null ) && file.canWrite() ) {
					//org.alice.ide.croquet.models.openproject.RecentProjectsUriSelectionState.getInstance().handleOpen( file );
					org.alice.ide.recentprojects.RecentProjectsListData.getInstance().handleOpen( file );
				}
			} catch( Throwable throwable ) {
				throwable.printStackTrace();
			}
			this.updateTitle();
		} else {
			//actionContext.cancel();
		}
	}

	@Deprecated
	public final org.lgna.croquet.undo.UndoHistory getProjectHistory() {
		return this.getProjectHistory( PROJECT_GROUP );
	}

	@Deprecated
	private final org.lgna.croquet.undo.UndoHistory getProjectHistory( org.lgna.croquet.Group group ) {
		if( this.getDocument() == null ) {
			return null;
		} else {
			return this.getDocument().getUndoHistory( group );
		}
	}

	//todo: investigate
	private static final int PROJECT_HISTORY_INDEX_IF_PROJECT_HISTORY_IS_NULL = 0;

	private int projectHistoryIndexFile = 0;
	private int projectHistoryIndexSceneSetUp = 0;

	private Boolean frozenProjectHistoryIndexFile = null;

	@Deprecated
	public void freezeProjectUpToDateWithFile() {
		this.frozenProjectHistoryIndexFile = this.isProjectUpToDateWithFile();
	}

	@Deprecated
	public void thawProjectUpToDateWithFile() {
		this.frozenProjectHistoryIndexFile = null;
	}

	public boolean isProjectUpToDateWithFile() {
		org.lgna.croquet.undo.UndoHistory history = this.getProjectHistory();
		if( history == null ) {
			return true;
		} else {
			if( this.frozenProjectHistoryIndexFile == null ) {
				return this.projectHistoryIndexFile == history.getInsertionIndex();
			} else {
				return this.frozenProjectHistoryIndexFile;
			}
		}
	}

	protected boolean isProjectUpToDateWithSceneSetUp() {
		org.lgna.croquet.undo.UndoHistory history = this.getProjectHistory();
		if( history == null ) {
			return true;
		} else {
			return this.projectHistoryIndexSceneSetUp == history.getInsertionIndex();
		}
	}

	private void updateHistoryIndexFileSync() {
		org.lgna.croquet.undo.UndoHistory history = this.getProjectHistory();
		if( history != null ) {
			this.projectHistoryIndexFile = history.getInsertionIndex();
		} else {
			this.projectHistoryIndexFile = PROJECT_HISTORY_INDEX_IF_PROJECT_HISTORY_IS_NULL;
		}
		this.updateHistoryIndexSceneSetUpSync();
		this.updateTitle();
	}

	protected void updateHistoryIndexSceneSetUpSync() {
		org.lgna.croquet.undo.UndoHistory history = this.getProjectHistory();
		if( history != null ) {
			this.projectHistoryIndexSceneSetUp = history.getInsertionIndex();
		} else {
			this.projectHistoryIndexSceneSetUp = PROJECT_HISTORY_INDEX_IF_PROJECT_HISTORY_IS_NULL;
		}
	}

	private org.alice.ide.frametitle.IdeFrameTitleGenerator frameTitleGenerator;

	protected abstract org.alice.ide.frametitle.IdeFrameTitleGenerator createFrameTitleGenerator();

	public final void updateTitle() {
		if( frameTitleGenerator != null ) {
			//pass
		} else {
			this.frameTitleGenerator = this.createFrameTitleGenerator();
		}
		this.getDocumentFrame().getFrame().setTitle( this.frameTitleGenerator.generateTitle( this.getUri(), this.isProjectUpToDateWithFile() ) );
	}

	private ProjectDocument getDocument() {
		return org.alice.ide.project.ProjectDocumentState.getInstance().getValue();
	}

	private void setDocument( ProjectDocument document ) {
		org.alice.ide.project.ProjectDocumentState.getInstance().setValueTransactionlessly( document );
	}

	public org.lgna.project.Project getProject() {
		ProjectDocument document = this.getDocument();
		return document != null ? document.getProject() : null;
	}

	public void setProject( org.lgna.project.Project project ) {
		StringBuilder sb = new StringBuilder();
		java.util.Set<org.lgna.project.ast.NamedUserType> types = project.getNamedUserTypes();
		for( org.lgna.project.ast.NamedUserType type : types ) {
			boolean wasNullMethodRemoved = false;
			java.util.ListIterator<org.lgna.project.ast.UserMethod> methodIterator = type.getDeclaredMethods().listIterator();
			while( methodIterator.hasNext() ) {
				org.lgna.project.ast.UserMethod method = methodIterator.next();
				if( method != null ) {
					//pass
				} else {
					methodIterator.remove();
					wasNullMethodRemoved = true;
				}
			}
			boolean wasNullFieldRemoved = false;
			java.util.ListIterator<org.lgna.project.ast.UserField> fieldIterator = type.getDeclaredFields().listIterator();
			while( fieldIterator.hasNext() ) {
				org.lgna.project.ast.UserField field = fieldIterator.next();
				if( field != null ) {
					//pass
				} else {
					fieldIterator.remove();
					wasNullFieldRemoved = true;
				}
			}
			if( wasNullMethodRemoved ) {
				if( sb.length() > 0 ) {
					sb.append( "\n" );
				}
				sb.append( "null method was removed from " );
				sb.append( type.getName() );
				sb.append( "." );
			}
			if( wasNullFieldRemoved ) {
				if( sb.length() > 0 ) {
					sb.append( "\n" );
				}
				sb.append( "null field was removed from " );
				sb.append( type.getName() );
				sb.append( "." );
			}
		}
		if( sb.length() > 0 ) {
			javax.swing.SwingUtilities.invokeLater( () -> {
				new edu.cmu.cs.dennisc.javax.swing.option.OkDialog.Builder( sb.toString() ).title( "A Problem With Your Project Has Been Fixed" ).messageType( edu.cmu.cs.dennisc.javax.swing.option.MessageType.WARNING ).buildAndShow();
			} );
		}
		org.lgna.project.ProgramTypeUtilities.sanityCheckAllTypes( project );

		// If this is the free version of Looking Glass (i.e. no sims) then some projects
		// cannot be opened because they use non-free models or resources. We can check this
		// by verifying whether all of the types in the projects have classes that we can
		// locate. If we can't locate them, then we can't open the project.
		if( !project.doAllTypeClassesExist() ) {
			throw new TypeClassNotFoundException( ProgramTypeUtilities.getProjectTypeClasses( project ) );
		}

		this.setDocument( new ProjectDocument( project ) );
	}

	public org.lgna.croquet.history.TransactionHistory getProjectTransactionHistory() {
		return this.getDocument().getRootTransactionHistory();
	}

	public void EPIC_HACK_loadProjectFrom( org.alice.ide.uricontent.UriProjectLoader uriProjectLoader ) {
		this.setUriProjectPair( uriProjectLoader );
		this.updateHistoryIndexFileSync();
		this.updateUndoRedoEnabled();
	}

	public void loadProjectFrom( org.alice.ide.uricontent.UriProjectLoader uriProjectLoader ) {
		this.EPIC_HACK_loadProjectFrom( uriProjectLoader );
	}

	public final void loadProjectFrom( java.io.File file ) {
		this.loadProjectFrom( new org.alice.ide.uricontent.FileProjectLoader( file ) );
	}

	public final void loadProjectFrom( String path ) {
		loadProjectFrom( new java.io.File( path ) );
	}

	protected abstract java.awt.image.BufferedImage createThumbnail() throws Throwable;

	public final void saveCopyOfProjectTo( java.io.File file ) throws java.io.IOException {
		org.lgna.project.Project project = this.getUpToDateProject();
		if( project != null ) {
			edu.cmu.cs.dennisc.java.util.zip.DataSource[] dataSources = getDataSources();
			org.lgna.project.io.IoUtilities.writeProject( file, project, dataSources );
			edu.wustl.lookingglass.study.StudyConfiguration.INSTANCE.getStudyLogger().log( java.util.logging.Level.INFO, "saved copy of user project: " + file.getAbsolutePath() );
		}
	}

	public final void saveProjectTo( java.io.File file ) throws java.io.IOException {
		if( StudyConfiguration.INSTANCE.shouldPromptToSaveProjects() ) {
			this.saveCopyOfProjectTo( file );
		} else {
			this.saveCopyOfProjectTo( StudyConfiguration.INSTANCE.getFileToSaveUsersProject() );
		}
		org.alice.ide.recentprojects.RecentProjectsListData.getInstance().handleSave( file );

		//		edu.cmu.cs.dennisc.java.util.logging.Logger.errln( "todo: better handling of file project loader", file );

		this.uriProjectLoader = new org.alice.ide.uricontent.FileProjectLoader( file );
		this.updateHistoryIndexFileSync();
	}

	//<lg>
	public void removeCorruptProject( java.io.File file ) throws java.io.IOException {
		java.nio.file.Files.delete( file.toPath() );
	}

	public edu.cmu.cs.dennisc.java.util.zip.DataSource[] getAdditionalDataSources() {
		java.util.List<edu.cmu.cs.dennisc.java.util.zip.DataSource> dataSourcesList = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();

		edu.cmu.cs.dennisc.java.util.zip.DataSource originalTypeData = getOriginalProgramTypeDataSource();
		if( originalTypeData != null ) {
			dataSourcesList.add( originalTypeData );
		}
		edu.cmu.cs.dennisc.java.util.zip.DataSource originalVersionData = getOriginalVersionDataSource();
		if( originalVersionData != null ) {
			dataSourcesList.add( originalVersionData );
		}
		return edu.cmu.cs.dennisc.java.lang.ArrayUtilities.createArray( dataSourcesList, edu.cmu.cs.dennisc.java.util.zip.DataSource.class );
	}

	public edu.cmu.cs.dennisc.java.util.zip.DataSource[] getDataSources() {
		edu.cmu.cs.dennisc.java.util.zip.DataSource[] dataSources;
		try {
			final java.awt.image.BufferedImage thumbnailImage = createThumbnail();
			if( thumbnailImage != null ) {
				if( ( thumbnailImage.getWidth() > 0 ) && ( thumbnailImage.getHeight() > 0 ) ) {
					//pass
				} else {
					throw new RuntimeException();
				}
			} else {
				throw new NullPointerException();
			}
			final byte[] data = edu.cmu.cs.dennisc.image.ImageUtilities.writeToByteArray( edu.cmu.cs.dennisc.image.ImageUtilities.PNG_CODEC_NAME, thumbnailImage );
			//<lg>
			java.util.List<edu.cmu.cs.dennisc.java.util.zip.DataSource> dataSourcesList = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();
			edu.cmu.cs.dennisc.java.util.zip.DataSource thumbnailData = new edu.cmu.cs.dennisc.java.util.zip.DataSource() {
				@Override
				public String getName() {
					return "thumbnail.png";
				}

				@Override
				public void write( java.io.OutputStream os ) throws java.io.IOException {
					os.write( data );
				}
			};
			dataSourcesList.add( thumbnailData );

			edu.cmu.cs.dennisc.java.util.zip.DataSource originalTypeData = getOriginalProgramTypeDataSource();
			if( originalTypeData != null ) {
				dataSourcesList.add( originalTypeData );
			}
			edu.cmu.cs.dennisc.java.util.zip.DataSource originalVersionData = getOriginalVersionDataSource();
			if( originalVersionData != null ) {
				dataSourcesList.add( originalVersionData );
			}
			dataSources = edu.cmu.cs.dennisc.java.lang.ArrayUtilities.createArray( dataSourcesList, edu.cmu.cs.dennisc.java.util.zip.DataSource.class );
			//</lg>
		} catch( Throwable t ) {
			dataSources = new edu.cmu.cs.dennisc.java.util.zip.DataSource[] {};
		}
		return dataSources;
	}

	public edu.cmu.cs.dennisc.java.util.zip.DataSource getOriginalProgramTypeDataSource() {
		java.io.File file = edu.cmu.cs.dennisc.java.net.UriUtilities.getFile( getUri() );
		org.lgna.project.Project project = getProject();
		if( file != null ) {
			java.util.zip.ZipFile zipFile;
			byte[] buffer = null;
			try {
				zipFile = new java.util.zip.ZipFile( file );
				java.io.InputStream typeStream = zipFile.getInputStream( new java.util.zip.ZipEntry( org.lgna.project.io.IoUtilities.ORIGINAL_PROGRAM_TYPE_ENTRY_NAME ) );

				if( typeStream != null ) {
					buffer = new byte[ typeStream.available() ];
					org.apache.axis.utils.IOUtils.readFully( typeStream, buffer );
				} else {
					java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
					edu.cmu.cs.dennisc.xml.XMLUtilities.write( project.getProgramType().encode(), os );
					buffer = os.toByteArray();
				}
				zipFile.close();
			} catch( IOException e ) {
				e.printStackTrace();
			}

			if( buffer != null ) {
				final byte[] originalType = buffer;
				return new edu.cmu.cs.dennisc.java.util.zip.DataSource() {
					@Override
					public String getName() {
						return org.lgna.project.io.IoUtilities.ORIGINAL_PROGRAM_TYPE_ENTRY_NAME;
					}

					@Override
					public void write( java.io.OutputStream os ) throws IOException {
						os.write( originalType );
					}
				};
			}
		}
		return null;
	}

	public edu.cmu.cs.dennisc.java.util.zip.DataSource getOriginalVersionDataSource() {
		java.io.File file = edu.cmu.cs.dennisc.java.net.UriUtilities.getFile( getUri() );
		org.lgna.project.Project project = getProject();
		if( file != null ) {
			java.util.zip.ZipFile zipFile;
			byte[] buffer = null;
			try {
				zipFile = new java.util.zip.ZipFile( file );
				java.io.InputStream typeStream = zipFile.getInputStream( new java.util.zip.ZipEntry( org.lgna.project.io.IoUtilities.ORIGINAL_VERSION_ENTRY_NAME ) );

				if( typeStream != null ) {
					buffer = new byte[ typeStream.available() ];
					org.apache.axis.utils.IOUtils.readFully( typeStream, buffer );
				} else {
					Version version = org.lgna.project.ProjectVersion.getCurrentVersion();
					buffer = version.toString().getBytes();
				}
				zipFile.close();
			} catch( IOException e ) {
				e.printStackTrace();
			}

			if( buffer != null ) {
				final byte[] originalVersion = buffer;
				return new edu.cmu.cs.dennisc.java.util.zip.DataSource() {
					@Override
					public String getName() {
						return org.lgna.project.io.IoUtilities.ORIGINAL_VERSION_ENTRY_NAME;
					}

					@Override
					public void write( java.io.OutputStream os ) throws IOException {
						os.write( originalVersion );
					}
				};
			}
		}
		return null;
	}

	//</lg>

	public java.io.File getMyProjectsDirectory() {
		// <lg/> Use a different path for user studies
		if( edu.wustl.lookingglass.study.StudyConfiguration.INSTANCE.getUserProjectsDirectory() == null ) {
			return org.alice.ide.croquet.models.ui.preferences.UserProjectsDirectoryState.getInstance().getDirectoryEnsuringExistance();
		} else {
			return edu.wustl.lookingglass.study.StudyConfiguration.INSTANCE.getUserProjectsDirectory();
		}
	}

	public final org.lgna.project.Project getUpToDateProject() {
		this.ensureProjectCodeUpToDate();
		return this.getProject();
	}

	public abstract void ensureProjectCodeUpToDate();

	private final ProjectDocumentFrame projectDocumentFrame;
}
