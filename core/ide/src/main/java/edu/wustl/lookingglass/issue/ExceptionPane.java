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
package edu.wustl.lookingglass.issue;

import java.util.MissingResourceException;

import org.alice.ide.issue.CurrentProjectAttachment;
import org.lgna.croquet.icon.IconSize;

import edu.cmu.cs.dennisc.issue.Attachment;
import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.LookingGlassTheme;
import javafx.application.Platform;
import javafx.fxml.FXML;

/**
 * @author Michael Pogran
 */
public class ExceptionPane extends FxComponent {

	private Thread thread;
	private Throwable throwable;
	private edu.wustl.lookingglass.issue.ExceptionDialog dialog;

	@FXML private javafx.scene.control.TextArea description;
	@FXML private javafx.scene.control.TextField email;
	@FXML private javafx.scene.control.CheckBox attach;

	public ExceptionPane( Thread thread, Throwable throwable ) {
		this( ExceptionPane.class, thread, throwable );
	}

	protected ExceptionPane( Class<?> fxmlResourceClass, Thread thread, Throwable throwable ) {
		super( fxmlResourceClass );
		this.thread = thread;
		this.throwable = throwable;
	}

	public void setDialog( edu.wustl.lookingglass.issue.ExceptionDialog dialog ) {
		this.dialog = dialog;
	}

	public edu.wustl.lookingglass.issue.ExceptionDialog getDialog() {
		return this.dialog;
	}

	protected void setThread( Thread thread ) {
		this.thread = thread;
	}

	public final Thread getThread() {
		return this.thread;
	}

	protected void setThrowable( Throwable throwable ) {
		this.throwable = throwable;
	}

	public final Throwable getThrowable() {
		return this.throwable;
	}

	public final Throwable getRootThrowable() {
		Throwable rootThrowable = org.apache.commons.lang.exception.ExceptionUtils.getRootCause( this.throwable );
		if( rootThrowable == null ) {
			rootThrowable = this.throwable;
		}
		return rootThrowable;
	}

	public javafx.scene.image.Image getImage() {
		return LookingGlassTheme.getFxImage( "logo-128x128", IconSize.FIXED );
	}

	public String getErrorTitle() {
		return null;
	}

	public String getErrorMessage() {
		if( this.getRootThrowable() != null ) {
			String key = this.getRootThrowable().getClass().getSimpleName();
			StackTraceElement[] trace = this.getRootThrowable().getStackTrace();

			for( StackTraceElement element : trace ) {
				String marker = element.getClassName() + "." + element.getMethodName();

				if( this.getRootThrowable().getClass().equals( AssertionError.class ) ) {
					switch( marker ) {
					case "org.alice.stageide.StageIDE.setProject":
						key += "." + marker;
						break;
					}
				}
			}
			try {
				return getLocalizedString( key );
			} catch( MissingResourceException e ) {
				return null;
			}
		} else {
			return null;
		}
	}

	public final void submit() {
		assert javafx.application.Platform.isFxApplicationThread();

		javafx.scene.control.ProgressIndicator indicator = new javafx.scene.control.ProgressIndicator( javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS );
		indicator.setPrefSize( 100, 100 );
		indicator.setMinSize( javafx.scene.control.Control.USE_PREF_SIZE, javafx.scene.control.Control.USE_PREF_SIZE );
		indicator.setMaxSize( javafx.scene.control.Control.USE_PREF_SIZE, javafx.scene.control.Control.USE_PREF_SIZE );
		indicator.setStyle( "-fx-progress-color: black;" );

		getDialog().setAndShowOverlay( indicator );

		final BugReport report = this.generateReport();
		this.attachProject( report );

		// Do not use SwingWorker in Java FX.
		new Thread( () -> {
			try {
				uploadToRedmine( report );
			} catch( Throwable t ) {
				Logger.throwable( t, report );
			}
			Platform.runLater( () -> {
				getDialog().close();
			} );
		} ).start();
	}

	private BugReport generateReport() {
		assert Platform.isFxApplicationThread();

		BugReport report = new BugReport();
		report.subject = getThrowable().toString();
		report.environment = edu.cmu.cs.dennisc.issue.IssueUtilities.getEnvironmentShortDescription();
		report.version = LookingGlassIDE.APPLICATION_VERSION.toString();
		report.ideUUID = LookingGlassIDE.getActiveInstance().getApplicationId().toString();
		report.stacktrace = getStackTrace();
		report.description = this.description.getText();
		report.email = this.email.getText();
		return report;
	}

	private String getStackTrace() {
		StringBuilder sb = new StringBuilder();
		for( Throwable throwable : this.dialog.getExceptions() ) {
			sb.append( edu.cmu.cs.dennisc.java.lang.ThrowableUtilities.getStackTraceAsString( throwable ) );
			sb.append( '\n' );
		}
		return sb.toString();
	}

	private void attachProject( BugReport report ) {
		assert Platform.isFxApplicationThread();

		if( attach.isSelected() ) {
			edu.cmu.cs.dennisc.issue.Attachment attachment = null;
			if( org.alice.ide.IDE.getActiveInstance().getProject() != null ) {
				attachment = new CurrentProjectAttachment();
			}
			if( attachment != null ) {
				report.addAttachment( attachment );
			}
		}
	}

	protected static void uploadToRedmine( BugReport report ) throws Exception {

		final String REDMINE_HOST = "https://dev.lookingglass.wustl.edu";
		final String PROJECT_KEY = "lookingglass";
		final String ACCESS_KEY = "ae86227e7fd037c3b83640c21370216c6892673d";
		final int TRACKER_ID = 5;
		final int EXCEPTION_FIELD_ID = 5;
		final int PLATFORM_FIELD_ID = 6;
		final int REPORTER_FIELD_ID = 7;
		final int AFFECTED_VERSION_FIELD_ID = 8;
		final int IDE_UUID_FIELD_ID = 9;

		// Find the correct tracker
		com.taskadapter.redmineapi.RedmineManager mgr = new com.taskadapter.redmineapi.RedmineManager( REDMINE_HOST, ACCESS_KEY );
		com.taskadapter.redmineapi.bean.Tracker tracker = null;
		for( com.taskadapter.redmineapi.bean.Tracker t : mgr.getTrackers() ) {
			if( t.getId() == TRACKER_ID ) {
				tracker = t;
				break;
			}
		}
		if( tracker == null ) {
			throw new Exception( "Unable to locate issue tracker" );
		}

		com.taskadapter.redmineapi.bean.Issue issue = new com.taskadapter.redmineapi.bean.Issue();
		issue.setTracker( tracker );

		// Redmine subjects cannot be longer than 255 characters.
		String subject = report.subject;
		if( subject.length() > 255 ) {
			subject = subject.substring( 0, 255 );
		}
		issue.setSubject( subject );

		java.util.ArrayList<com.taskadapter.redmineapi.bean.CustomField> customFields = new java.util.ArrayList<>();

		// Affected Version Field ID
		com.taskadapter.redmineapi.bean.CustomField affectedVersionField = new com.taskadapter.redmineapi.bean.CustomField( AFFECTED_VERSION_FIELD_ID, null, report.version );
		customFields.add( affectedVersionField );

		// IDE UUID FIELD ID
		com.taskadapter.redmineapi.bean.CustomField ideUUIDField = new com.taskadapter.redmineapi.bean.CustomField( IDE_UUID_FIELD_ID, null, report.ideUUID );
		customFields.add( ideUUIDField );

		// Exception/Backtrace
		String exception = report.stacktrace;
		// Our bug tracker likes CRLF best. So normalize to CRLF.
		com.taskadapter.redmineapi.bean.CustomField exceptionField = new com.taskadapter.redmineapi.bean.CustomField( EXCEPTION_FIELD_ID, null, exception.replaceAll( "\\r\\n", "\n" ).replaceAll( "\\n", "\r\n" ) );
		customFields.add( exceptionField );

		// Platform
		com.taskadapter.redmineapi.bean.CustomField platformField = new com.taskadapter.redmineapi.bean.CustomField( PLATFORM_FIELD_ID, null, report.environment );
		customFields.add( platformField );

		// Reporter
		com.taskadapter.redmineapi.bean.CustomField reporterField = new com.taskadapter.redmineapi.bean.CustomField( REPORTER_FIELD_ID, null, report.email );
		customFields.add( reporterField );

		issue.setCustomFields( customFields );

		// Description
		issue.setDescription( report.description );

		for( Attachment a : report.getAttachments() ) {
			try {
				com.taskadapter.redmineapi.bean.Attachment attachment = mgr.uploadAttachment( a.getFileName(), a.getMIMEType(), a.getBytes() );
				issue.getAttachments().add( attachment );
			} catch( Throwable t ) {
				edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( t, report );
			}
		}

		mgr.createIssue( PROJECT_KEY, issue );
	}
}
