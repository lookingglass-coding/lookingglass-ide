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
package edu.wustl.lookingglass.study;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.lgna.project.ast.Declaration;
import org.lgna.project.ast.UserMethod;

import edu.cmu.cs.dennisc.app.ApplicationRoot;
import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.puzzle.PuzzleStatementUtility;

/**
 * @author Kyle J. Harms
 *
 *         Use this settings class to configure Looking Glass for your user
 *         study. Do NOT ever check in any changes to these settings into any
 *         branch that is not for a user study.
 */
public enum StudyConfiguration {

	INSTANCE;

	public static enum StudyCondition {
		UNKNOWN,
		CONTROL,
		EXPERIMENTAL
	}

	public static enum StudyPhase {
		UNKNOWN,
		TRAINING,
		TRANSFER,
		OPEN_PROGRAMMING
	}

	private final Properties studyProperties;
	// Auto-login as a user during a study.
	private final String communityUserName = null;
	private final String communityPassword = null;

	// Run LG in fullscreen mode to keep the kids on track during the study.
	private final boolean fullScreenApplicationEnabled = false;

	// User's projects/world directory
	private final java.io.File userProjectsDirectory;

	// Loggers for the study
	private final java.util.logging.Logger studyLogger;

	private final org.lgna.croquet.history.event.Listener transactionHistoryListener;
	private final java.util.logging.Logger transactionHistoryLogger;
	private int projectHistoryIndex = 0;

	// Unique ID or code word input from user. Use this to help ID your user with their data.
	private final Integer participantId;

	// Allow scene editing?
	private final boolean sceneEditingEnabled = true;

	// Save user's current project to disk
	private final boolean saveUsersProjectOnExit = false;
	private final boolean promptToSaveProjects = true;

	private final StudyCondition studyCondition;
	private StudyPhase studyPhase;

	private StudyConfiguration() {

		final boolean LOAD_STUDY_PROPERTIES = false;
		if( LOAD_STUDY_PROPERTIES ) {
			this.studyProperties = loadStudyProperties();
		} else {
			this.studyProperties = null;
		}

		final boolean LOAD_PARTICIPANT_ID_PROPERTIES = false;
		if( ( this.studyProperties != null ) && LOAD_PARTICIPANT_ID_PROPERTIES ) {
			int id = Integer.valueOf( this.studyProperties.getProperty( "id", "-1" ) );
			this.participantId = id;
		} else {
			this.participantId = null;
		}

		// Create a special worlds directory just for the study.
		final boolean INITIALIZE_USER_PROJECTS_DIR = false;
		{
			if( INITIALIZE_USER_PROJECTS_DIR ) {
				this.userProjectsDirectory = new java.io.File( getStudyDirectory(), "worlds" );
				this.userProjectsDirectory.mkdirs();
			} else {
				this.userProjectsDirectory = null;
			}
		}

		// Initialize the loggers for running a study.
		final boolean INITIALIZE_LOGGERS = false;
		{
			java.util.logging.Handler handler = null;
			if( INITIALIZE_LOGGERS ) {
				try {
					java.io.File logFile = new java.io.File( getStudyDirectory(), "study.log" );
					handler = new java.util.logging.FileHandler( logFile.getAbsolutePath(), true );
					handler.setFormatter( new java.util.logging.XMLFormatter() );
				} catch( java.io.IOException e ) {
					// Failed to create file handler. Just print this. This is just for user studies.
					e.printStackTrace();
				}
			}
			this.studyLogger = createLogger( "study", handler );
			this.transactionHistoryLogger = createLogger( "study.transactionHistory", handler );
			if( INITIALIZE_LOGGERS ) {
				// Print to the console for the study logger. This helps when develop the log messages.
				this.studyLogger.addHandler( new java.util.logging.ConsoleHandler() );

				// Setup the dennisc logger (should be IDE logger) to also output severe events to the log also.
				// You may need to know if a crash occurred during your study.
				java.util.logging.Logger ideLogger = edu.cmu.cs.dennisc.java.util.logging.Logger.getInstance();
				ideLogger.addHandler( handler );

				// Log transaction history events
				this.transactionHistoryListener = new org.lgna.croquet.history.event.Listener() {
					@Override
					public void changing( org.lgna.croquet.history.event.Event<?> e ) {
					}

					@Override
					public void changed( org.lgna.croquet.history.event.Event<?> e ) {
						if( e instanceof org.lgna.croquet.history.event.EditCommittedEvent ) {
							StudyConfiguration.this.transactionHistoryLogger.log( java.util.logging.Level.INFO, "commit: " + e.getNode().toString() );
						} else if( e instanceof org.lgna.croquet.history.event.FinishedEvent ) {
							StudyConfiguration.this.transactionHistoryLogger.log( java.util.logging.Level.INFO, "finish: " + e.getNode().toString() );
						} else if( e instanceof org.lgna.croquet.history.event.CancelEvent ) {
							StudyConfiguration.this.transactionHistoryLogger.log( java.util.logging.Level.INFO, "cancel: " + e.getNode().toString() );
						} else if( e instanceof org.lgna.croquet.history.event.AddStepEvent ) {
							StudyConfiguration.this.transactionHistoryLogger.log( java.util.logging.Level.INFO, "add step: " + e.getNode().toString() );
						}

						// If the code has changed, log it, so we can see what's different.
						if( LookingGlassIDE.getActiveInstance().getDocumentFrame().isInCodePerspective() ) {
							int historyIndex = 0;
							if( LookingGlassIDE.getActiveInstance().getProjectHistory() != null ) {
								historyIndex = LookingGlassIDE.getActiveInstance().getProjectHistory().getInsertionIndex();
							}
							if( historyIndex != StudyConfiguration.this.projectHistoryIndex ) {
								logActiveCode();
							}
							StudyConfiguration.this.projectHistoryIndex = historyIndex;
						}
					}
				};
				org.lgna.croquet.Application.getActiveInstance().getApplicationOrDocumentTransactionHistory().addListener( this.transactionHistoryListener );

				org.lgna.croquet.event.ValueListener<org.alice.ide.ProjectDocument> projectListener = new org.lgna.croquet.event.ValueListener<org.alice.ide.ProjectDocument>() {
					@Override
					public void valueChanged( org.lgna.croquet.event.ValueEvent<org.alice.ide.ProjectDocument> e ) {
						org.alice.ide.ProjectDocument nextValue = e.getNextValue();
						if( nextValue != null ) {
							nextValue.getRootTransactionHistory().addListener( StudyConfiguration.this.transactionHistoryListener );
						}
					}
				};
				org.alice.ide.project.ProjectDocumentState.getInstance().addNewSchoolValueListener( projectListener );
			} else {
				this.studyLogger.setLevel( java.util.logging.Level.OFF );

				// No logging of transaction history
				this.transactionHistoryLogger.setLevel( java.util.logging.Level.OFF );
				this.transactionHistoryListener = null;
			}
		}

		// Set the condition for this IDE (control/experimental).
		this.studyCondition = StudyCondition.UNKNOWN;
		this.setStudyPhase( StudyPhase.UNKNOWN );

		this.studyLogger.log( java.util.logging.Level.INFO, "study condition: " + this.studyCondition );
		this.studyLogger.log( java.util.logging.Level.INFO, "study participant id: " + this.participantId );
	}

	static private Properties loadStudyProperties() {
		Properties properties = new Properties();
		try {
			InputStream input = new FileInputStream( new File( ApplicationRoot.getRootDirectory(), "study.properties" ) );
			properties.load( input );
		} catch( Throwable t ) {
			Logger.throwable( t );
		}
		return properties;
	}

	static private java.util.logging.Logger createLogger( String loggerName, java.util.logging.Handler handler ) {
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger( loggerName );
		logger.setUseParentHandlers( false );
		logger.setLevel( java.util.logging.Level.ALL );
		if( handler != null ) {
			logger.addHandler( handler );
		}
		return logger;
	}

	public java.io.File getStudyDirectory() {
		// Should the i be appended to the study directory? (Helps segregate data for different participants.)
		final boolean INCLUDE_PARTICIPANT_ID_IN_DIR = true;

		java.io.File studyDirectory = new java.io.File( edu.cmu.cs.dennisc.app.ApplicationRoot.getRootDirectory(), "study-data" );
		studyDirectory.mkdirs();
		if( INCLUDE_PARTICIPANT_ID_IN_DIR ) {
			assert this.participantId != null;
			studyDirectory = new java.io.File( studyDirectory, this.participantId.toString() );
			studyDirectory.mkdirs();
		}
		return studyDirectory;
	}

	public String getCommunityUserName() {
		return this.communityUserName;
	}

	public String getCommunityPassword() {
		return this.communityPassword;
	}

	public boolean isFullScreenApplicationEnabled() {
		return this.fullScreenApplicationEnabled;
	}

	public boolean isSceneEditingEnabled() {
		return this.sceneEditingEnabled;
	}

	public boolean shouldSaveUsersProjectOnExit() {
		return this.saveUsersProjectOnExit;
	}

	public boolean shouldPromptToSaveProjects() {
		return this.promptToSaveProjects;
	}

	public java.io.File getFileToSaveUsersProject() {
		return new java.io.File( this.getStudyDirectory(), java.util.UUID.randomUUID().toString() + "." + org.lgna.project.io.IoUtilities.PROJECT_EXTENSION );
	}

	public Integer getParticipantId() {
		return this.participantId;
	}

	public StudyCondition getStudyCondition() {
		return this.studyCondition;
	}

	public void setStudyPhase( StudyPhase phase ) {
		this.studyPhase = phase;
		this.studyLogger.log( java.util.logging.Level.INFO, "study phase: " + this.studyPhase );
	}

	public StudyPhase getStudyPhase() {
		return this.studyPhase;
	}

	public java.io.File getUserProjectsDirectory() {
		return this.userProjectsDirectory;
	}

	public java.util.logging.Logger getStudyLogger() {
		return this.studyLogger;
	}

	public void logActiveCode() {
		org.alice.ide.declarationseditor.DeclarationTabState declarationTabState = org.alice.ide.IDE.getActiveInstance().getDocumentFrame().getDeclarationsEditorComposite().getTabState();
		org.alice.ide.declarationseditor.DeclarationComposite<?, ?> declarationComposite = declarationTabState.getValue();
		if( declarationComposite != null ) {
			Declaration declaration = declarationComposite.getDeclaration();
			if( declaration instanceof UserMethod ) {
				UserMethod userMethod = (UserMethod)declaration;

				StringBuilder builder = new StringBuilder();
				builder.append( "active user method: " ).append( userMethod.getParent().getRepr() ).append( "::" ).append( userMethod.getReprWithId() ).append( "\n" );
				PuzzleStatementUtility.writeStatementsToText( builder, userMethod.body.getValue().statements.getValue(), 1 );
				StudyConfiguration.INSTANCE.getStudyLogger().log( java.util.logging.Level.INFO, builder.toString() );
			}
		}
	}
}
