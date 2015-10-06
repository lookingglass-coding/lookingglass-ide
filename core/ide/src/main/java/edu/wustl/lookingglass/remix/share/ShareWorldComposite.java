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
import java.util.concurrent.ExecutionException;

import org.alice.ide.IDE;
import org.alice.ide.ReasonToDisableSomeAmountOfRendering;
import org.alice.media.video.WebmRecordingAdapter;
import org.alice.stageide.ast.StoryApiSpecificAstUtilities;
import org.alice.stageide.program.VideoEncodingProgramContext;
import org.alice.stageide.sceneeditor.StorytellingSceneEditor;
import org.lgna.croquet.views.AbstractWindow;
import org.lgna.croquet.views.BorderPanel;
import org.lgna.project.Project;

import edu.cmu.cs.dennisc.animation.FrameBasedAnimator;
import edu.cmu.cs.dennisc.animation.FrameObserver;
import edu.cmu.cs.dennisc.media.animation.MediaPlayerAnimation;
import edu.wustl.lookingglass.community.CommunityProjectPropertyManager;
import edu.wustl.lookingglass.community.api.packets.WorldPacket;
import edu.wustl.lookingglass.community.exceptions.CommunityApiException;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.croquet.models.community.ShareContentObserver;
import edu.wustl.lookingglass.virtualmachine.StateListeningVirtualMachine;
import edu.wustl.lookingglass.virtualmachine.event.VirtualMachineExecutionStateListener;

/**
 * @author Caitlin Kelleher
 */
public class ShareWorldComposite extends AbstractShareComposite {
	private static class SingletonHolder {
		private static ShareWorldComposite instance = new ShareWorldComposite();
	}

	public static ShareWorldComposite getInstance() {
		return SingletonHolder.instance;
	}

	private RecordWorldPage recordWorldPage = new RecordWorldPage( this );
	private PreviewWorldPage previewWorldPage = new PreviewWorldPage( this );

	private VideoEncodingProgramContext programContext;
	private WebmRecordingAdapter encoder;
	private RecordWorldObserver observer;

	private WorldPacket worldPacket;

	private ShareWorldComposite() {
		super( java.util.UUID.fromString( "0a7afad4-f3ef-4ff5-9625-5ef237dc6103" ), org.alice.ide.IDE.PROJECT_GROUP );
		this.addPage( recordWorldPage );
		this.addPage( previewWorldPage );
	}

	public int getFramesRecorded() {
		if( this.observer != null ) {
			return this.observer.getImageCount();
		} else {
			return 0;
		}
	}

	@Override
	public File getRecordedVideo() {
		if( this.encoder != null ) {
			return this.encoder.getEncodedVideoFile();
		} else {
			return null;
		}
	}

	@Override
	protected void shareContent( ShareContentObserver observer ) {
		uploadWorld( observer, null );
	}

	@Override
	protected void updateContent( ShareContentObserver observer, Integer contentId ) {
		uploadWorld( observer, contentId );
	}

	@Override
	protected Dimension calculateWindowSize( AbstractWindow<?> window ) {
		return new Dimension( 950, 735 );
	}

	private void uploadWorld( ShareContentObserver observer, Integer worldId ) {
		if( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().isUserLoggedIn() ) {
			observer.updateMessage( "Uploading your world..." );

			try {
				Project project = IDE.getActiveInstance().getUpToDateProject();
				RenderedImage poster = (RenderedImage)getPosterImage();
				File video = getRecordedVideo();

				assert ( project != null ) && ( poster != null ) && ( video != null );

				WorldPacket world = WorldPacket.createInstance( this.getTitleState().getValue(), this.getDescriptionState().getValue(), this.getTagState().getValue() );
				world.setProject( project, IDE.getActiveInstance().getAdditionalDataSources() );
				world.setPoster( poster );
				world.setVideo( video );
				world.setUserId( LookingGlassIDE.getCommunityController().getCurrentUser().getId() );

				WorldPacket response;
				if( worldId != null ) {
					world.setId( worldId );
					response = LookingGlassIDE.getCommunityController().updateWorld( world );
				} else {
					response = LookingGlassIDE.getCommunityController().newWorld( world );

					Integer challengeId = CommunityProjectPropertyManager.getCommunityChallengeID( project );
					if( challengeId != null ) {
						try {
							LookingGlassIDE.getCommunityController().submitWorldToTemplate( challengeId, response.getId() ); // we need the actual challenge id
						} catch( CommunityApiException cae ) {
							edu.cmu.cs.dennisc.java.util.logging.Logger.warning( "Failed to submit world to template", cae );
						}
					}
				}

				observer.uploadSuccessful( LookingGlassIDE.getCommunityController().getAbsoluteUrl( response.getWorldPath() ) );
			} catch( CommunityApiException cae ) {
				observer.uploadFailed( "We're sorry, Looking Glass couldn't upload your world. Please try again." );
				edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( cae );
			}
		}
	}

	public void initializeRecording() {
		this.cleanUpRecording();

		// initialize program context
		this.recordWorldPage.resetProgramContainer();
		BorderPanel lookingGlassContainer = this.recordWorldPage.getProgramContainer();

		assert lookingGlassContainer != null;

		// initialize context
		this.programContext = new VideoEncodingProgramContext( IDE.getActiveInstance().getProject().getProgramType(), this.getFrameRate() );
		this.programContext.initializeInContainer( lookingGlassContainer.getAwtComponent() );

		this.programContext.getProgramImp().setAnimator( new FrameBasedAnimator( this.getFrameRate() ) );
		this.programContext.setActiveSceneOnComponentThreadAndWait();

		// initialize encoder
		this.encoder = new WebmRecordingAdapter();
		this.encoder.setFrameRate( this.getFrameRate() );
		this.encoder.setDimension( programContext.getOnscreenRenderTarget().getSurfaceSize() );
		this.encoder.initializeAudioRecording();

		// add observers
		this.observer = new RecordWorldObserver( this.programContext, this.encoder );
		this.programContext.getProgramImp().getAnimator().addFrameObserver( this.observer );

		this.programContext.getProgramImp().getAnimator().addFrameObserver( new FrameObserver() {

			@Override
			public void update( double tCurrent ) {
				int numSeconds = new Double( Math.floor( tCurrent ) ).intValue();
				int numMinutes = numSeconds / 60;
				if( ( numMinutes >= 10 ) && ( programContext.getVirtualMachine() instanceof StateListeningVirtualMachine ) ) {
					( (StateListeningVirtualMachine)programContext.getVirtualMachine() ).pauseVirtualMachine();
				}
				recordWorldPage.setRecordTime( tCurrent );
			}

			@Override
			public void complete() {
			}
		} );

		if( programContext.getVirtualMachine() instanceof StateListeningVirtualMachine ) {
			( (StateListeningVirtualMachine)programContext.getVirtualMachine() ).addVirtualMachinePauseStateListener( new VirtualMachineExecutionStateListener() {

				@Override
				public void isChangedToPaused() {
					edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnSwingThread( ( ) -> {
						recordWorldPage.getIsRecordingState().setValueTransactionlessly( false );
						recordWorldPage.getIsRecordingState().setEnabled( false );
						recordWorldPage.getIsRecordingState().updateNameAndIcon();
					} );
				}

				@Override
				public void isChangedToRunning() {
				}

				@Override
				public void isEndingExecution() {
				}

			} );
		}
	}

	private void cleanUpRecording() {
		if( this.programContext != null ) {
			if( this.programContext.getProgramImp().getAnimator().getSpeedFactor() > 0 ) {
				this.programContext.getProgramImp().stopAnimator();
			}
			this.programContext.getProgramImp().getAnimator().removeFrameObserver( this.observer );
		}
		this.programContext = null;
		this.encoder = null;
		this.observer = null;
	}

	/*package-private*/void startRecording() {
		if( this.encoder.isVideoEncoding() ) {
			// pass
		} else {
			MediaPlayerAnimation.EPIC_HACK_setAnimationObserver( this.encoder );
			this.encoder.startVideoEncoding();
			this.programContext.getProgramImp().startAnimator();
		}

		this.programContext.getProgramImp().getAnimator().setSpeedFactor( 1 );
	}

	/*package-private*/void pauseRecording() {
		if( this.programContext != null ) {
			this.programContext.getProgramImp().getAnimator().setSpeedFactor( 0 );

		}
	}

	/*package-private*/void stopRecording() {
		this.encoder.stopVideoEncoding();
		edu.cmu.cs.dennisc.media.animation.MediaPlayerAnimation.EPIC_HACK_setAnimationObserver( null );
	}

	@Override
	protected String getShareDialogTitle() {
		return "World";
	}

	public WorldPacket getWorldPacket() {
		return this.worldPacket;
	}

	public void fetchWorldPacket() {
		Integer contentId = CommunityProjectPropertyManager.getCommunityProjectID( LookingGlassIDE.getActiveInstance().getProject() );

		if( contentId != null ) {
			javax.swing.SwingWorker<WorldPacket, Void> worker = new javax.swing.SwingWorker<WorldPacket, Void>() {

				@Override
				protected WorldPacket doInBackground() throws Exception {
					WorldPacket rv = null;
					try {
						rv = LookingGlassIDE.getCommunityController().getWorld( contentId );
					} catch( CommunityApiException e ) {
						e.printStackTrace();
					}
					return rv;
				}

				@Override
				protected void done() {
					try {
						worldPacket = get();
						if( recordWorldPage.isWaitingForPacket() ) {
							recordWorldPage.checkForExistingWorld();
						}

					} catch( InterruptedException | ExecutionException e ) {
						e.printStackTrace();
					}
				}
			};
			worker.execute();
		}
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		fetchWorldPacket();
		StorytellingSceneEditor.getInstance().disableRendering( ReasonToDisableSomeAmountOfRendering.MODAL_DIALOG_WITH_RENDER_WINDOW_OF_ITS_OWN );
		initializeRecording();

		String title = CommunityProjectPropertyManager.getProjectTitle( IDE.getActiveInstance().getProject() );
		if( ( title == null ) || title.isEmpty() ) {
			String main = StoryApiSpecificAstUtilities.getUserMain( IDE.getActiveInstance().getUpToDateProject().getProgramType() ).getName();
			if( !main.equals( org.alice.stageide.ast.BootstrapUtilties.MAIN_PROCEDURE_NAME ) ) {
				this.getTitleState().setValueTransactionlessly( main );
			}
		} else {
			this.getTitleState().setValueTransactionlessly( title );
		}
	}

	@Override
	public void handlePostDeactivation() {
		super.handlePostDeactivation();
		stopRecording();
		StorytellingSceneEditor.getInstance().enableRendering( ReasonToDisableSomeAmountOfRendering.MODAL_DIALOG_WITH_RENDER_WINDOW_OF_ITS_OWN );
		this.worldPacket = null;
	}

	@Override
	protected void saveContentLocally() {
		Project project = IDE.getActiveInstance().getUpToDateProject();
		edu.cmu.cs.dennisc.java.util.zip.DataSource[] dataSources = IDE.getActiveInstance().getDataSources();
		RenderedImage poster = (RenderedImage)getPosterImage();
		File video = getRecordedVideo();

		String title = getTitleState().getValue();

		java.io.File file = org.alice.ide.ProjectApplication.getActiveInstance().getDocumentFrame().showSaveFileDialog( null, title, "zip", true );

		if( file != null ) {
			try {
				java.io.OutputStream os = new java.io.FileOutputStream( file );
				java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream( os );

				edu.cmu.cs.dennisc.java.util.zip.ZipUtilities.write( zip, new edu.cmu.cs.dennisc.java.util.zip.DataSource() {

					@Override
					public String getName() {
						return "project.lgp";
					}

					@Override
					public void write( OutputStream os ) throws IOException {
						java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
						org.lgna.project.io.IoUtilities.writeProject( baos, project, dataSources );
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
