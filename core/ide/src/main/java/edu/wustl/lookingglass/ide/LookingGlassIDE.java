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
package edu.wustl.lookingglass.ide;

import java.awt.Image;
import java.io.File;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.List;

import org.alice.ide.perspectives.ProjectPerspective;
import org.alice.ide.uricontent.FileProjectLoader;
import org.alice.nonfree.NebulousIde;
import org.alice.nonfree.NebulousStoryApi;
import org.lgna.croquet.preferences.PreferenceManager;

import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.wustl.lookingglass.common.VersionNumber;
import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.perspectives.puzzle.CompletionPuzzlePerspective;
import edu.wustl.lookingglass.ide.program.TimeScrubProgramImp;
import edu.wustl.lookingglass.ide.views.OpaqueLayer;
import edu.wustl.lookingglass.project.TypeClassNotFoundException;
import edu.wustl.lookingglass.puzzle.CompletionPuzzle;
import edu.wustl.lookingglass.puzzle.PuzzleProjectProperties;
import edu.wustl.lookingglass.study.StudyConfiguration;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class LookingGlassIDE extends org.alice.stageide.StageIDE {

	public static final String APPLICATION_NAME = "Looking Glass";

	public static final VersionNumber APPLICATION_VERSION;

	static {
		String version = null;
		try {
			java.util.Properties properties = new java.util.Properties();
			properties.load( ClassLoader.getSystemResourceAsStream( "application.properties" ) );
			version = properties.getProperty( "application.version" );
		} catch( java.io.IOException e ) {
			Logger.throwable( e );
		}
		if( version != null ) {
			boolean isFree = !NebulousIde.nonfree.isNonFreeEnabled() || !NebulousStoryApi.nonfree.isNonFreeEnabled();
			String freeQualifier = "";
			if( isFree ) {
				freeQualifier = "-FREE";
			}
			APPLICATION_VERSION = new VersionNumber( version + freeQualifier );
		} else {
			APPLICATION_VERSION = null;
		}
	}

	// IMPORTANT! Update this constant when the API version supported in Looking Glass changes.
	// This version number should be in the standard MAJOR.MINOR.PATCH form. (http://semver.org/)
	// * Change the MAJOR version number when the API removes or changes fundamentally
	//   how a portion of the previous API worked.
	// * Change the MINOR version number when you add new functionality that doesn't break the old.
	// * Use the optional PATCH number if you don't add/delete/modify any feature you're just fixing a bug
	//   in the implementation.
	public static final VersionNumber COMMUNITY_API_VERSION = new VersionNumber( "8.0.0" );

	public static final int DEFAULT_WORLD_DIMENSION_WIDTH = 640;
	public static final int DEFAULT_WORLD_DIMENSION_HEIGHT = 360;
	private static final String APPLICATION_ID_KEY = "ApplicationUUID";

	public static LookingGlassIDE getActiveInstance() {
		return edu.cmu.cs.dennisc.java.lang.ClassUtilities.getInstance( org.lgna.croquet.Application.getActiveInstance(), LookingGlassIDE.class );
	}

	// There is only one community controller
	public static final edu.wustl.lookingglass.community.CommunityController COMMUNITY_CONTROLLER = new edu.wustl.lookingglass.community.CommunityController();
	public static final edu.wustl.lookingglass.modules.CollectionModuleManager MODULE_MANAGER = new edu.wustl.lookingglass.modules.CollectionModuleManager();

	private static final List<Image> APPLICATION_ICONS = new java.util.ArrayList<java.awt.Image>();

	static {
		APPLICATION_ICONS.add( LookingGlassTheme.getImage( "logo", org.lgna.croquet.icon.IconSize.EXTRA_SMALL ) );
		APPLICATION_ICONS.add( LookingGlassTheme.getImage( "logo", org.lgna.croquet.icon.IconSize.SMALL ) );
		APPLICATION_ICONS.add( LookingGlassTheme.getImage( "logo", org.lgna.croquet.icon.IconSize.MEDIUM ) );
		APPLICATION_ICONS.add( LookingGlassTheme.getImage( "logo", org.lgna.croquet.icon.IconSize.LARGE ) );
		APPLICATION_ICONS.add( LookingGlassTheme.getImage( "logo", org.lgna.croquet.icon.IconSize.EXTRA_LARGE ) );
		APPLICATION_ICONS.add( LookingGlassTheme.getImage( "logo-512x512", org.lgna.croquet.icon.IconSize.FIXED ) );
	}

	private final java.util.UUID applicationId;

	private final OpaqueLayer loadingLayer;

	private boolean isPuzzleEditorEnabled = false;
	private PuzzleProjectProperties puzzleProjectProperties = null;

	public LookingGlassIDE() {
		this( new String[ 0 ] );
	}

	public LookingGlassIDE( final String[] args ) {
		super( new LookingGlassIdeConfiguration() );

		edu.wustl.lookingglass.study.StudyConfiguration.INSTANCE.getStudyLogger().log( java.util.logging.Level.INFO, "started ide" );

		// Give feedback that we are trying to bring up the frame.
		this.setCursor( java.awt.Cursor.WAIT_CURSOR );

		this.initialize( args );
		this.initializeCommunityConnection();
		MODULE_MANAGER.initialize();
		PuzzleProjectProperties.intialize();

		// Restore the window state
		edu.wustl.lookingglass.ide.croquet.preferences.WindowAttributesState.getInstance().loadWindowAttributes( this.getDocumentFrame().getFrame() );

		// In user study mode, we sometimes run fullscreen to keep the participants on task.
		if( edu.wustl.lookingglass.study.StudyConfiguration.INSTANCE.isFullScreenApplicationEnabled() ) {
			final javax.swing.JFrame mainWindow = this.getDocumentFrame().getFrame().getAwtComponent();
			mainWindow.setUndecorated( true );

			// Set Full Screen
			final java.awt.GraphicsDevice graphicsDevice = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			try {
				if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isWindows() || !graphicsDevice.isFullScreenSupported() ) {
					// Fake full screen on windows
					if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isLinux() ) {
						// Linux doesn't do fake full screen with modal dialogs.
						this.getDocumentFrame().getFrame().maximize();
					} else {
						java.awt.Rectangle bounds = graphicsDevice.getDefaultConfiguration().getBounds();
						mainWindow.setLocation( bounds.getLocation() );
						mainWindow.setSize( bounds.getSize() );
					}
				} else {
					graphicsDevice.setFullScreenWindow( mainWindow );
				}
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}

		// Get or set IDE UUID
		java.util.prefs.Preferences userPreferences = PreferenceManager.getUserPreferences();
		if( userPreferences != null ) {
			String id = userPreferences.get( APPLICATION_ID_KEY, "" );
			if( id.isEmpty() ) {
				id = java.util.UUID.randomUUID().toString();
				userPreferences.put( APPLICATION_ID_KEY, id );
			}
			this.applicationId = java.util.UUID.fromString( id );
		} else {
			this.applicationId = null;
		}

		// Less flickering, show things in the right state, instead of the class tab first...
		this.loadingLayer = new OpaqueLayer( this.getDocumentFrame().getFrame() );

		// Show the IDE
		this.getDocumentFrame().getFrame().setVisible( true );

		// This feels like this should be one of the first things process. But because of Alice's poor
		// initialization sequence (i.e. These things should be passed to the constructors) we have
		// to process this information after the IDE Frame is up.
		this.handleCommandLineArguments( args );
	}

	public void initializeCommunityConnection() {
		// Initialize connection with community
		ThreadHelper.runInBackground( () -> {
			// These should not be done in the AWT thread.
			try {
				LookingGlassIDE.getCommunityController().initializeConnection();
			} catch( Exception e ) {
				Logger.throwable( e, this );
			}

			// Load the collection modules
			LookingGlassIDE.getCommunityController().getModulePackets();
		} );
	}

	private void handleCommandLineArguments( String[] args ) {
		final String REMIX_ARG = "remix";
		final String REMIX_SUBSTITIONS_ARG = "remix-substitions";
		final String REMIX_METHOD_ARG = "remix-method";
		final String REMIX_PUZZLE_ARG = "remix-puzzle";
		final String PUZZLE_ARG = "puzzle";
		final String PUZZLE_EDITOR_ARG = "puzzle-editor";

		joptsimple.OptionParser parser = new joptsimple.OptionParser();
		parser.accepts( REMIX_SUBSTITIONS_ARG ).withRequiredArg();
		parser.accepts( REMIX_METHOD_ARG ).withRequiredArg();
		parser.accepts( REMIX_ARG ).requiredIf( REMIX_SUBSTITIONS_ARG ).requiredIf( REMIX_METHOD_ARG ).withRequiredArg();
		parser.accepts( REMIX_PUZZLE_ARG );
		parser.accepts( PUZZLE_ARG );
		parser.accepts( PUZZLE_EDITOR_ARG );
		joptsimple.OptionSet options = parser.parse( args );

		// Because of singletons we can only ever open one file at a time. So for now we just take the last one.
		String projectFilename = null;
		for( Object arg : options.nonOptionArguments() ) {
			// TODO: open all projects in new IDEs.
			projectFilename = (String)arg;
		}

		// Load the remixes
		java.util.List<?> remixFilenames = options.valuesOf( REMIX_ARG );
		java.util.List<?> remixSubstitions = options.valuesOf( REMIX_SUBSTITIONS_ARG );
		java.util.List<?> remixMethods = options.valuesOf( REMIX_METHOD_ARG );

		// TODO: support multiple projects once the singletons are gone...
		final String remixFilename;
		if( remixFilenames.size() > 0 ) {
			remixFilename = (String)remixFilenames.get( remixFilenames.size() - 1 );
		} else {
			remixFilename = null;
		}

		final String remixSubstition;
		if( remixSubstitions.size() > 0 ) {
			remixSubstition = (String)remixSubstitions.get( remixSubstitions.size() - 1 );
		} else {
			remixSubstition = null;
		}

		final String remixMethod;
		if( remixMethods.size() > 0 ) {
			remixMethod = (String)remixMethods.get( remixMethods.size() - 1 );
		} else {
			remixMethod = null;
		}

		final boolean remixPuzzle = options.has( REMIX_PUZZLE_ARG );

		this.isPuzzleEditorEnabled = ( options.has( PUZZLE_EDITOR_ARG ) || ( Boolean.valueOf( System.getProperty( "edu.wustl.lookingglass.puzzle.editor", "false" ) ) ) );

		if( projectFilename != null ) {
			java.io.File projectFile = new java.io.File( projectFilename );
			if( projectFile.exists() ) {
				if( options.has( PUZZLE_ARG ) ) {
					this.showLoadingLayer( true );
					this.loadPuzzleFrom( projectFile );
				} else {
					this.loadProjectFrom( projectFile );
				}
			} else {
				edu.cmu.cs.dennisc.java.util.logging.Logger.warning( "file does not exist:", projectFile );
				this.setPerspective( this.getDocumentFrame().getNoProjectPerspective() );
			}
		} else {
			this.setPerspective( this.getDocumentFrame().getNoProjectPerspective() );
		}

		try {
			if( remixFilename != null ) {
				final edu.wustl.lookingglass.remix.SnippetScript snippet = edu.wustl.lookingglass.remix.SnippetFileUtilities.loadSnippet( new java.io.FileInputStream( remixFilename ) );
				javax.swing.SwingUtilities.invokeLater( () -> {
					if( snippet != null ) {
						edu.wustl.lookingglass.remix.roles.CharacterSelectionOperation operation = new edu.wustl.lookingglass.remix.roles.CharacterSelectionOperation( snippet, this.getProject(), remixSubstition, remixMethod, remixPuzzle );
						// TODO: ???
						operation.fire();
					}
				} );
			}
		} catch( java.io.IOException e ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( e, this );
		}
	}

	@Override
	protected void handleWindowOpened( java.awt.event.WindowEvent e ) {
		super.handleWindowOpened( e );

		// Give feedback while it's loading up.
		javax.swing.SwingUtilities.invokeLater( () -> {
			this.setCursor( java.awt.Cursor.DEFAULT_CURSOR );
		} );
	}

	@Override
	public void loadProjectFrom( org.alice.ide.uricontent.UriProjectLoader uriProjectLoader ) {
		// <lg/> - loading a new project should happen in code perspective, not one of the dinah perspectives.
		// note: one might make a similar argument for the scene editor, but not sure. so leaving as is for now.
		org.alice.ide.perspectives.ProjectPerspective projectPerspective = this.getDocumentFrame().getPerspectiveState().getValue();
		if( projectPerspective instanceof edu.wustl.lookingglass.ide.perspectives.dinah.AbstractDinahPerspective ) {
			this.getDocumentFrame().setToCodePerspectiveTransactionlessly();
		}
		try {
			this.setCursor( java.awt.Cursor.WAIT_CURSOR );
			super.loadProjectFrom( uriProjectLoader );

			if( this.isPuzzleEditorEnabled() ) {
				this.puzzleProjectProperties = new PuzzleProjectProperties( this.getProject() );
			}
		} catch( TypeClassNotFoundException e ) {
			this.showProjectTypeError( e );
		} catch( org.lgna.project.virtualmachine.LgnaVmException e ) {
			uriProjectLoader.setValid( false );
			e.printStackTrace();
			throw new LookingGlassInitializationException( "Error initializing" );
		} finally {
			javax.swing.SwingUtilities.invokeLater( () -> {
				this.setCursor( java.awt.Cursor.DEFAULT_CURSOR );
			} );
		}
	}

	public void loadPuzzleFrom( File file ) {
		StudyConfiguration.INSTANCE.getStudyLogger().log( java.util.logging.Level.INFO, "load puzzle:\n" + file.getAbsolutePath() );
		ThreadHelper.runOnSwingThread( () -> {
			this.setCursor( java.awt.Cursor.WAIT_CURSOR );

			// This is so sad. You can't just load a project without first opening
			// the code perspective. That's ridiculous. The problem is that if you
			// are trying to make a puzzle out of a world. You can get a flash of
			// the answer from the code perspective loading first.
			// So... this is a stupid hack to work around that problem.
			this.showLoadingLayer( true );

			// This is really lame. Nothing gets initialized for editing with
			// the blocks unless you load a project first. So load the project,
			// waste tons of cpu cycles, and then load the puzzles. Note:
			// this should be totally unnecessary... but we can't just load
			// the puzzle because things like the undo queue won't get initialized.
			this.loadProjectFrom( new FileProjectLoader( file, false ) );

			CompletionPuzzle puzzle = new CompletionPuzzle( this.getProject() );
			javax.swing.SwingUtilities.invokeLater( () -> {
				puzzle.beginPuzzle( () -> {
					this.setPerspective( getDocumentFrame().getNoProjectPerspective() );
				} );
			} );
		} );
	}

	@Override
	public void showUnableToOpenFileDialog( java.io.File file, java.lang.String message ) {
		getExceptionHandler().uncaughtException( Thread.currentThread(), new UnableToOpenFileException( edu.cmu.cs.dennisc.java.io.FileUtilities.getCanonicalPathIfPossible( file ), message ) );
	}

	@Override
	public List<Image> getApplicationIcons() {
		return APPLICATION_ICONS;
	}

	static public edu.wustl.lookingglass.community.CommunityController getCommunityController() {
		return COMMUNITY_CONTROLLER;
	}

	static public edu.wustl.lookingglass.modules.CollectionModuleManager getModuleManager() {
		return MODULE_MANAGER;
	}

	public edu.wustl.lookingglass.modules.CollectionModuleManager getCollectionModuleManager() {
		return MODULE_MANAGER;
	}

	public PuzzleProjectProperties getPuzzleProjectProperties() {
		return this.puzzleProjectProperties;
	}

	public void showLoadingLayer( boolean shouldShow ) {
		this.loadingLayer.setStencilShowing( shouldShow );
	}

	@Override
	public java.awt.image.BufferedImage createThumbnail() throws Throwable {
		// Let the garbage collector do it's job. We need to stop hogging memory.
		org.alice.stageide.sceneeditor.ThumbnailGenerator thumbnailGenerator = new org.alice.stageide.sceneeditor.ThumbnailGenerator( DEFAULT_WORLD_DIMENSION_WIDTH, DEFAULT_WORLD_DIMENSION_HEIGHT );
		return thumbnailGenerator.createThumbnail();
	}

	@Override
	protected org.alice.ide.frametitle.IdeFrameTitleGenerator createFrameTitleGenerator() {
		return new LookingGlassIdeFrameTitleGenerator( this );
	}

	public void setCursor( int cursor ) {
		assert java.awt.EventQueue.isDispatchThread();
		this.getDocumentFrame().getFrame().getAwtComponent().setCursor( new java.awt.Cursor( cursor ) );
	}

	public org.lgna.croquet.Operation getSetToPlayAndExplorePerspectiveOperation() {
		return new SetPerspectiveOperation( edu.wustl.lookingglass.ide.perspectives.dinah.DinahPerspective.class) {
			@Override
			protected ProjectPerspective createInstance() {
				return new edu.wustl.lookingglass.ide.perspectives.dinah.DinahPerspective( getDocumentFrame() );
			}
		};
	}

	public org.lgna.croquet.Operation getSetToDinahRemixPerspectiveOperation() {
		return new SetPerspectiveOperation( edu.wustl.lookingglass.ide.perspectives.dinah.DinahUseRemixPerspective.class) {

			@Override
			protected ProjectPerspective createInstance() {
				return new edu.wustl.lookingglass.ide.perspectives.dinah.DinahUseRemixPerspective( getDocumentFrame() );
			}

		};
	}

	public org.lgna.croquet.Operation getSetToLocalRemixPerspectiveOperation() {
		return new SetPerspectiveOperation( edu.wustl.lookingglass.ide.perspectives.dinah.DinahRemixPerspective.class) {

			@Override
			protected ProjectPerspective createInstance() {
				return new edu.wustl.lookingglass.ide.perspectives.dinah.DinahRemixPerspective( getDocumentFrame() );
			}

		};
	}

	public boolean isInCodePerspective() {
		return this.getDocumentFrame().getPerspectiveState().getValue() == this.getDocumentFrame().getCodePerspective();
	}

	public boolean isInPlayAndExplorePerspective() {
		return this.getDocumentFrame().getPerspectiveState().getValue().getClass().equals( edu.wustl.lookingglass.ide.perspectives.dinah.DinahPerspective.class );
	}

	public boolean isInDinahRemixPerspective() {
		return this.getDocumentFrame().getPerspectiveState().getValue().getClass().equals( edu.wustl.lookingglass.ide.perspectives.dinah.DinahUseRemixPerspective.class );
	}

	public boolean isInLocalRemixPerspective() {
		return this.getDocumentFrame().getPerspectiveState().getValue().getClass().equals( edu.wustl.lookingglass.ide.perspectives.dinah.DinahRemixPerspective.class );
	}

	public boolean isInRemixPerspective() {
		return this.getDocumentFrame().getPerspectiveState().getValue() instanceof edu.wustl.lookingglass.ide.perspectives.dinah.DinahRemixPerspective;
	}

	public boolean isInOneOfThreeDinahPerspectives() {
		return this.getDocumentFrame().getPerspectiveState().getValue() instanceof edu.wustl.lookingglass.ide.perspectives.dinah.AbstractDinahPerspective;
	}

	public boolean isNodeVisible( org.lgna.project.ast.Node node ) {
		org.lgna.project.ast.AbstractCode nextFocusedCode = getAncestor( node, org.lgna.project.ast.AbstractCode.class );
		return this.getDocumentFrame().getFocusedCode().equals( nextFocusedCode );
	}

	public boolean isInCompletionPuzzlePerspective() {
		return this.getDocumentFrame().getPerspectiveState().getValue() instanceof edu.wustl.lookingglass.ide.perspectives.puzzle.CompletionPuzzlePerspective;
	}

	@Deprecated
	public CompletionPuzzle getCompletionPuzzle() {
		if( this.isInCompletionPuzzlePerspective() ) {
			return ( (CompletionPuzzlePerspective)this.getDocumentFrame().getPerspectiveState().getValue() ).getPuzzle();
		} else {
			return null;
		}
	}

	public boolean isPuzzleEditorEnabled() {
		return this.isPuzzleEditorEnabled;
	}

	@Override
	protected org.alice.ide.Theme createTheme() {
		return new LookingGlassTheme();
	}

	@Override
	public org.lgna.croquet.Operation getAboutOperation() {
		return new edu.wustl.lookingglass.ide.croquet.models.help.AboutOperation();
	}

	@Override
	public String getInstanceTextForAccessible( org.lgna.project.ast.Accessible accessible ) {
		if( accessible != null ) {
			if( accessible instanceof org.lgna.project.ast.AbstractField ) {
				org.lgna.project.ast.AbstractField field = (org.lgna.project.ast.AbstractField)accessible;

				if( field.getValueType() == this.getSceneType() ) {
					return org.alice.ide.croquet.models.ui.formatter.FormatterState.getInstance().getValue().getTextForThis();
				} else {
					return field.getName();
				}
			} else {
				return accessible.getValidName();
			}
		} else {
			return null;
		}
	}

	public void makeStatementVisible( org.lgna.project.ast.Statement statement ) {
		// open the correct tab
		org.lgna.project.ast.UserMethod userMethodOwner = statement.getFirstAncestorAssignableTo( org.lgna.project.ast.UserMethod.class );
		if( userMethodOwner != null ) {
			org.alice.ide.IDE.getActiveInstance().getDocumentFrame().selectDeclarationComposite( org.alice.ide.declarationseditor.DeclarationComposite.getInstance( userMethodOwner ) );
		} else {
			edu.cmu.cs.dennisc.java.util.logging.Logger.warning( "This statement has no user method owner" );
		}

		// need to also scroll so the statement we're interested in is actually visible.
		org.alice.ide.declarationseditor.DeclarationComposite<?, ?> composite = this.getDocumentFrame().getDeclarationsEditorComposite().getTabState().getValue();
		if( composite != null ) {
			org.alice.ide.declarationseditor.components.DeclarationView view = composite.getView();
			java.util.List<javax.swing.JComponent> jButtons = edu.cmu.cs.dennisc.java.awt.ComponentUtilities.findAllMatches( view.getAwtComponent(), javax.swing.JComponent.class );
			for( javax.swing.JComponent jButton : jButtons ) {
				org.lgna.project.ast.Statement candidate = null;
				org.lgna.croquet.views.AwtComponentView<?> component = org.lgna.croquet.views.AwtComponentView.lookup( jButton );
				if( component instanceof org.alice.ide.common.AbstractStatementPane ) {
					org.alice.ide.common.AbstractStatementPane statementPane = (org.alice.ide.common.AbstractStatementPane)component;
					candidate = statementPane.getStatement();
				}
				if( candidate == statement ) {
					if( component.getAwtComponent() instanceof javax.swing.JComponent ) {
						javax.swing.JComponent jComponent = (javax.swing.JComponent)component.getAwtComponent();
						jComponent.scrollRectToVisible( new java.awt.Rectangle( 0, 0, jComponent.getWidth(), jComponent.getHeight() ) );
						break;
					}
				}
			}
		}

	}

	public org.lgna.croquet.views.SwingComponentView<?> getComponentForNode( org.lgna.project.ast.Node node, boolean scrollToVisible ) {
		if( node instanceof org.lgna.project.ast.Statement ) {
			final org.lgna.project.ast.Statement statement = (org.lgna.project.ast.Statement)node;

			org.alice.ide.declarationseditor.DeclarationComposite<?, ?> composite = this.getDocumentFrame().getDeclarationsEditorComposite().getTabState().getValue();
			if( composite != null ) {
				org.alice.ide.declarationseditor.components.DeclarationView view = composite.getView();
				java.util.List<javax.swing.AbstractButton> jButtons = edu.cmu.cs.dennisc.java.awt.ComponentUtilities.findAllMatches( view.getAwtComponent(), javax.swing.AbstractButton.class );
				for( javax.swing.AbstractButton jButton : jButtons ) {
					org.lgna.project.ast.Statement candidate = null;
					org.lgna.croquet.views.AwtComponentView<?> component = org.lgna.croquet.views.AwtComponentView.lookup( jButton );
					if( component instanceof org.alice.ide.common.AbstractStatementPane ) {
						org.alice.ide.common.AbstractStatementPane statementPane = (org.alice.ide.common.AbstractStatementPane)component;
						candidate = statementPane.getStatement();
					}
					if( ( candidate == statement ) && ( scrollToVisible ) ) {
						if( component instanceof org.lgna.croquet.views.SwingComponentView<?> ) {
							org.lgna.croquet.views.SwingComponentView<?> jComponent = (org.lgna.croquet.views.SwingComponentView<?>)component;
							jComponent.getAwtComponent().scrollRectToVisible( new java.awt.Rectangle( 0, 0, jComponent.getWidth(), jComponent.getHeight() ) );

							return jComponent;
						}
					}
				}
				return null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public org.lgna.croquet.views.AwtComponentView<?> getComponentForNode( org.lgna.project.ast.Statement statement ) {
		return getComponentForNode( statement, false );
	}

	public edu.wustl.lookingglass.ide.program.TimeScrubProgramImp getDinahProgramImp() {
		if( this.isInOneOfThreeDinahPerspectives() ) {
			return (TimeScrubProgramImp)( (edu.wustl.lookingglass.ide.perspectives.dinah.AbstractDinahPerspective)this.getPerspective() ).getProgramManager().getProgramImp();
		} else {
			return null;
		}
	}

	public org.alice.stageide.program.ProgramContext getCurrentProgramContext() {
		if( this.isInOneOfThreeDinahPerspectives() ) {
			return ( (edu.wustl.lookingglass.ide.perspectives.dinah.AbstractDinahPerspective)this.getPerspective() ).getProgramManager().getProgramContext();
		} else {
			return null;
		}
	}

	public edu.wustl.lookingglass.remix.SnippetScript getSnippetScript() {
		if( getPerspective() instanceof edu.wustl.lookingglass.ide.perspectives.dinah.DinahRemixPerspective ) {
			return ( (edu.wustl.lookingglass.ide.perspectives.dinah.DinahRemixPerspective)this.getPerspective() ).getRemixScript();
		} else {
			return null;
		}
	}

	public java.util.Date getVersionDate() {
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat( "yyyy.MM.dd" );
		String versionString = org.alice.ide.IDE.getVersionText();
		int splitIndex = versionString.indexOf( "-" );
		if( splitIndex != -1 ) {
			versionString = versionString.substring( 0, splitIndex );
		}
		try {
			return dateFormat.parse( versionString );
		} catch( ParseException e ) {
			return null;
		}
	}

	public java.util.UUID getApplicationId() {
		return this.applicationId;
	}

	@Override
	public void updateUndoRedoEnabled() {
		super.updateUndoRedoEnabled();

		String toolTipText = "Please add actions to your world.";
		boolean isEnabled = false;
		for( org.lgna.project.ast.UserMethod method : this.getUserMethodsInvokedFromSceneActivationListeners() ) {
			if( method.body.getValue().statements.size() > 0 ) {
				isEnabled = true;
				toolTipText = null;
				break;
			}
		}

		org.lgna.croquet.Operation[] operations = new org.lgna.croquet.Operation[] { getDocumentFrame().getCodePerspective().getToolBarComposite().getPlayAndExploreOperation(), getDocumentFrame().getCodePerspective().getToolBarComposite().getShareRemixOperation(), edu.wustl.lookingglass.remix.share.ShareWorldComposite.getInstance().getLaunchOperation() };

		for( org.lgna.croquet.Operation operation : operations ) {
			operation.setEnabled( isEnabled );
			operation.setToolTipText( toolTipText );
		}
	}

	private void showProjectTypeError( TypeClassNotFoundException exception ) {
		Logger.throwable( exception );

		StringBuilder missingList = new StringBuilder();
		for( String name : exception.getNotFound() ) {
			missingList.append( "\n" ).append( name );
		}
		missingList.append( "\n\n" );
		MessageFormat formatter = new MessageFormat( FxComponent.DEFAULT_RESOURCES.getString( "ProjectTypeErrorPane.message" ) );
		final String message = formatter.format( new String[] { missingList.toString() } );

		ThreadHelper.runOnFxThread( () -> {
			Alert alert = new Alert( AlertType.ERROR );
			alert.setTitle( FxComponent.DEFAULT_RESOURCES.getString( "ProjectTypeErrorPane.title" ) );
			alert.setHeaderText( FxComponent.DEFAULT_RESOURCES.getString( "ProjectTypeErrorPane.header" ) );
			alert.setContentText( message );
			alert.show();
			Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
			stage.toFront();
			stage.setAlwaysOnTop( true );
		} );
	}
}
