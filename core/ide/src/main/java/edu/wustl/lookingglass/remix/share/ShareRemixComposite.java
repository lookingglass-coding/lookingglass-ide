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
package edu.wustl.lookingglass.remix.share;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.alice.ide.IDE;
import org.alice.ide.ReasonToDisableSomeAmountOfRendering;
import org.alice.ide.perspectives.ProjectPerspective;
import org.alice.stageide.sceneeditor.StorytellingSceneEditor;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.croquet.views.AbstractWindow;
import org.lgna.croquet.views.Dialog;

import edu.wustl.lookingglass.community.api.packets.SnippetPacket;
import edu.wustl.lookingglass.community.exceptions.CommunityApiException;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.croquet.models.community.ShareContentObserver;
import edu.wustl.lookingglass.ide.perspectives.dinah.AbstractDinahPerspective;
import edu.wustl.lookingglass.ide.perspectives.dinah.DinahRemixPerspective;
import edu.wustl.lookingglass.ide.program.TimeScrubProgramImp;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.ide.program.event.TimeScrubProgramListener;
import edu.wustl.lookingglass.ide.program.thread.RecordReplayTimePeriodThread;
import edu.wustl.lookingglass.ide.program.thread.RecordReplayTimePeriodThread.RecordingCompletedListener;
import edu.wustl.lookingglass.media.ImagesToWebmEncoder;
import edu.wustl.lookingglass.remix.SnippetScript;
import edu.wustl.lookingglass.remix.models.EndCaptureState;
import edu.wustl.lookingglass.remix.models.StartCaptureState;
import edu.wustl.lookingglass.virtualmachine.StateListeningVirtualMachine;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;

/**
 * @author Caitlin Kelleher
 */
public class ShareRemixComposite extends AbstractShareComposite {

	private static final int MAX_VIDEO_MIN = 10;

	private static class SingletonHolder {
		private static ShareRemixComposite instance = new ShareRemixComposite();
	}

	public static ShareRemixComposite getInstance() {
		return SingletonHolder.instance;
	}

	private RecordRemixPage recordRemixPage = new RecordRemixPage( this );
	private PreviewWorldPage annotateRemixPage = new PreviewWorldPage( this );
	private double savedProgramTime = 0.0;

	private TimeScrubProgramImp program;
	private SnippetScript snippetScript;
	private edu.wustl.lookingglass.media.ImagesToWebmEncoder encoder;

	private final TimeScrubProgramListener programListener;

	private ShareRemixComposite() {
		super( java.util.UUID.fromString( "85a06667-99e2-4c04-81eb-adf27db1aa14" ), org.alice.ide.IDE.PROJECT_GROUP );

		this.addPage( recordRemixPage );
		this.addPage( annotateRemixPage );

		this.programListener = new TimeScrubProgramListener() {
			@Override
			public void programStateChange( ProgramStateEvent programStateEvent ) {
				int numSeconds = new Double( Math.floor( programStateEvent.getTime() ) ).intValue();
				int numMinutes = numSeconds / 60;
				if( ( numMinutes > MAX_VIDEO_MIN ) && ( program.getVirtualMachine() instanceof StateListeningVirtualMachine ) ) {
					( (StateListeningVirtualMachine)program.getVirtualMachine() ).pauseVirtualMachine();
				}
				final double time = programStateEvent.getTime();
				edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnSwingThread( ( ) -> recordRemixPage.setRecordTime( time ) );
			}

			@Override
			public void startingExecution( ProgramExecutionEvent programExecutionEvent ) {
			}

			@Override
			public void endingExecution( ProgramExecutionEvent programExecutionEvent ) {
			}
		};

		StartCaptureState.getInstance().addListener( ( ValueEvent<AbstractEventNode<?>> e ) -> {
			if( ( e.getNextValue() != null ) && ( EndCaptureState.getInstance().getValue() != null ) ) {
				getLaunchOperation().setEnabled( true );
			}
		} );

		EndCaptureState.getInstance().addListener( ( ValueEvent<AbstractEventNode<?>> e ) -> {
			if( ( e.getNextValue() != null ) && ( StartCaptureState.getInstance().getValue() != null ) ) {
				getLaunchOperation().setEnabled( true );
			}
		} );

		getLaunchOperation().setEnabled( false );
	}

	@Override
	protected String getShareDialogTitle() {
		return "Remix";
	}

	@Override
	protected void shareContent( ShareContentObserver observer ) {
		if( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().isUserLoggedIn() ) {
			observer.updateMessage( "uploading your new remix..." );

			SnippetScript script = getSnippetScript();
			RenderedImage poster = (RenderedImage)getPosterImage();
			File video = getRecordedVideo();

			assert ( script != null ) && ( poster != null ) && ( video != null );

			try {
				String title = this.getTitleState().getValue();
				String description = this.getDescriptionState().getValue();

				script.setTitle( title );
				script.setDescription( description );

				SnippetPacket snippet = SnippetPacket.createInstance( script, title, description, this.getTagState().getValue() );
				snippet.setPoster( poster );
				snippet.setVideo( video );

				SnippetPacket response = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().newSnippet( snippet );

				observer.uploadSuccessful( LookingGlassIDE.getCommunityController().getAbsoluteUrl( response.getSnippetPath() ) );

			} catch( CommunityApiException cae ) {
				observer.uploadFailed( "We're sorry, Looking Glass couldn't upload your remix. Please try again." );
				cae.printStackTrace();
			}
		}
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnSwingThread( ( ) -> LookingGlassIDE.getActiveInstance().getDocumentFrame().setToCodePerspectiveTransactionlessly() );
	}

	@Override
	protected void updateContent( ShareContentObserver observer, Integer contentId ) {
		// pass
	}

	@Override
	protected Dimension calculateWindowSize( AbstractWindow<?> window ) {
		return new Dimension( 950, 735 );
	}

	public ImagesToWebmEncoder getEncoder() {
		return this.encoder;
	}

	public SnippetScript getSnippetScript() {
		return this.snippetScript;
	}

	public TimeScrubProgramImp getProgram() {
		return this.getProgram();
	}

	@Override
	public File getRecordedVideo() {
		if( encoder != null ) {
			return encoder.getEncodedVideoFile();
		}
		else {
			return null;
		}
	}

	public void initializeRecording() {
		cleanUpRecording();
		this.encoder = new edu.wustl.lookingglass.media.ImagesToWebmEncoder( this.getFrameRate(), this.getRecordingSize() );
		this.recordRemixPage.resetProgramContainer();
	}

	private void cleanUpRecording() {
		this.snippetScript = null;
		this.encoder = null;
	}

	public org.lgna.croquet.views.AwtComponentView<?> getProgramContainer() {
		return this.recordRemixPage.getProgramContainer();
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		initializeRecording();

		TimeScrubProgramImp program = LookingGlassIDE.getActiveInstance().getDinahProgramImp();
		assert program != null;

		program.stopReplay();
		program.pauseProgram();

		SnippetScript script = LookingGlassIDE.getActiveInstance().getSnippetScript();
		assert script != null;

		this.snippetScript = script;
		this.program = program;
		this.savedProgramTime = program.getCurrentProgramTime();

		program.addTimeScrubProgramListener( this.programListener );

		StorytellingSceneEditor.getInstance().disableRendering( ReasonToDisableSomeAmountOfRendering.MODAL_DIALOG_WITH_RENDER_WINDOW_OF_ITS_OWN );

		ProjectPerspective perspective = IDE.getActiveInstance().getDocumentFrame().getPerspectiveState().getValue();
		if( perspective instanceof DinahRemixPerspective ) {
			DinahRemixPerspective remixPerspective = (DinahRemixPerspective)perspective;
			recordRemixPage.setProgramContainer( remixPerspective.getExecutionTraceComposite().getProgramContainer() );
		}
	}

	@Override
	public void handlePostDeactivation() {
		if( this.program != null ) {
			this.program.removeTimeScrubProgramListener( this.programListener );
		}
		super.handlePostDeactivation();
	}

	@Override
	protected void handleFinally( CompletionStep<?> step, Dialog dialog ) {
		// in both commit and cancel cases, we need to put the executing program container back where we found it
		ProjectPerspective perspective = LookingGlassIDE.getActiveInstance().getDocumentFrame().getPerspectiveState().getValue();
		if( perspective instanceof AbstractDinahPerspective ) {
			AbstractDinahPerspective dinahPerspective = (AbstractDinahPerspective)perspective;
			dinahPerspective.getProgramManager().getProgramImp().setCurrentTime( this.savedProgramTime );
		}

		StorytellingSceneEditor.getInstance().enableRendering( ReasonToDisableSomeAmountOfRendering.MODAL_DIALOG_WITH_RENDER_WINDOW_OF_ITS_OWN );
		super.handleFinally( step, dialog );
	}

	/*package-private*/void startRecording() {
		RecordReplayTimePeriodThread thread = (RecordReplayTimePeriodThread)this.program.recordTimePeriod( this.snippetScript.getStartTime(), this.snippetScript.getEndTime(), this.snippetScript.getExecutingThread() );

		thread.addListener( new RecordingCompletedListener() {
			@Override
			public void notifyCompletion() {
				recordRemixPage.getIsRecordingState().setValueTransactionlessly( false );
				recordRemixPage.getIsRecordingState().setEnabled( false );
				recordRemixPage.getIsRecordingState().updateNameAndIcon();
			}

		} );
	}

	/*package-private*/public void pauseRecording() {
		this.program.stopReplay();
	}

	@Override
	protected void saveContentLocally() {
		SnippetScript script = getSnippetScript();
		RenderedImage poster = (RenderedImage)getPosterImage();
		File video = getRecordedVideo();

		String title = getTitleState().getValue();
		String description = getDescriptionState().getValue();

		script.setTitle( title );
		script.setDescription( description );

		java.io.File file = org.alice.ide.ProjectApplication.getActiveInstance().getDocumentFrame().showSaveFileDialog( null, title, "zip", true );

		if( file != null ) {
			try {
				java.io.OutputStream os = new java.io.FileOutputStream( file );
				java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream( os );

				edu.cmu.cs.dennisc.java.util.zip.ZipUtilities.write( zip, new edu.cmu.cs.dennisc.java.util.zip.DataSource() {

					@Override
					public String getName() {
						return "snippet.lgr";
					}

					@Override
					public void write( OutputStream os ) throws IOException {
						java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
						edu.wustl.lookingglass.remix.SnippetFileUtilities.writeSnippet( baos, script );
						byte[] content = baos.toByteArray();
						os.write( content );
					}
				} );

				edu.cmu.cs.dennisc.java.util.zip.ZipUtilities.write( zip, new edu.cmu.cs.dennisc.java.util.zip.DataSource() {

					@Override
					public String getName() {
						return "poster.png";
					}

					@Override
					public void write( OutputStream os ) throws IOException {
						javax.imageio.ImageIO.write( poster, "png", os );
					}
				} );

				edu.cmu.cs.dennisc.java.util.zip.ZipUtilities.write( zip, new edu.cmu.cs.dennisc.java.util.zip.DataSource() {

					@Override
					public String getName() {
						return "video.webm";
					}

					@Override
					public void write( OutputStream os ) throws IOException {
						byte[] content = java.nio.file.Files.readAllBytes( video.toPath() );
						os.write( content );
					}
				} );

				zip.flush();
				zip.close();

			} catch( IOException e ) {
				e.printStackTrace();
			}
		}
	}
}
