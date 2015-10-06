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

import org.alice.ide.video.preview.VideoComposite;
import org.lgna.croquet.CancelException;
import org.lgna.croquet.Operation;
import org.lgna.croquet.PlainStringValue;
import org.lgna.croquet.SingleSelectListState;
import org.lgna.croquet.WizardPageComposite;
import org.lgna.croquet.codecs.DefaultItemCodec;
import org.lgna.croquet.edits.Edit;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.history.CompletionStep;

import edu.cmu.cs.dennisc.video.vlcj.VlcjVideoPlayer;
import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus;
import edu.wustl.lookingglass.community.CommunityStatusObserver;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.remix.share.views.PreviewWorldView;

/**
 * @author Caitlin Kelleher
 */
public class PreviewWorldPage extends WizardPageComposite<PreviewWorldView, AbstractShareComposite> implements CommunityStatusObserver {
	private final VideoComposite videoComposite = new VideoComposite();

	private final ErrorStatus noPosterError;
	private final ErrorStatus noTitleError;
	private final ErrorStatus noDescriptionError;

	private final PlainStringValue instructionsString = this.createStringValue( "instructions" );
	private final PlainStringValue selectPosterString = this.createStringValue( "selectPoster" );
	private final PlainStringValue createdByString = this.createStringValue( "createdBy" );
	private final SingleSelectListState<java.awt.Image, ?> snapshotListState;
	private final Operation capturePosterOperation;

	public PreviewWorldPage( AbstractShareComposite owner ) {
		super( java.util.UUID.fromString( "b399d26e-bb11-48b1-825a-a47bd09001db" ), owner );
		this.noPosterError = this.createErrorStatus( "noPosterError" );
		this.noTitleError = this.createErrorStatus( "noTitleError" );
		this.noDescriptionError = this.createErrorStatus( "noDescriptionError" );
		this.snapshotListState = this.createMutableListState( "snapshotState", java.awt.Image.class, DefaultItemCodec.createInstance( java.awt.Image.class ), -1 );

		this.registerSubComposite( this.videoComposite );

		this.capturePosterOperation = this.createActionOperation( "capturePosterOperation", new Action() {

			@Override
			public Edit perform( CompletionStep<?> step, org.lgna.croquet.AbstractComposite.InternalActionOperation source ) throws CancelException {
				java.awt.Image posterImage = getVideoComposite().getView().getVideoPlayer().getSnapshot();
				if( posterImage != null ) {
					snapshotListState.addItem( posterImage );
					snapshotListState.setSelectedIndex( snapshotListState.getItemCount() - 1 );

					getView().getSnapshotList().ensureIndexIsVisible( snapshotListState.getSelectedIndex() );
				}
				step.finish();
				return null;
			}
		} );

		this.snapshotListState.addAndInvokeNewSchoolValueListener( new org.lgna.croquet.event.ValueListener<java.awt.Image>() {

			@Override
			public void valueChanged( ValueEvent<java.awt.Image> e ) {
				if( e.getNextValue() != null ) {
					getOwner().setPosterImage( e.getNextValue() );
					PreviewWorldPage.this.refreshOwnerStatus();
				}
			}
		} );

		this.capturePosterOperation.setButtonIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "list-add", org.lgna.croquet.icon.IconSize.SMALL ) );
	}

	@Override
	public void resetData() {
	}

	@Override
	public org.lgna.croquet.AbstractSeverityStatusComposite.Status getPageStatus( CompletionStep<?> step ) {
		org.lgna.croquet.AbstractSeverityStatusComposite.Status rv = IS_GOOD_TO_GO_STATUS;

		AbstractShareComposite owner = this.getOwner();
		String titleValue = owner.getTitleState().getValue();
		String descriptionValue = owner.getDescriptionState().getValue();
		if( ( titleValue == null ) || ( titleValue.length() == 0 ) ) {
			rv = noTitleError;
		} else if( ( descriptionValue == null ) || ( descriptionValue.length() == 0 ) ) {
			rv = noDescriptionError;
		} else if( owner.getPosterImage() == null ) {
			rv = noPosterError;
		}
		return rv;
	}

	@Override
	protected PreviewWorldView createView() {
		return new PreviewWorldView( this );
	}

	public VideoComposite getVideoComposite() {
		return this.videoComposite;
	}

	public SingleSelectListState<java.awt.Image, ?> getSnapshotListState() {
		return this.snapshotListState;
	}

	public Operation getCapturePosterOperation() {
		return this.capturePosterOperation;
	}

	public PlainStringValue getInstructionsString() {
		return this.instructionsString;
	}

	public PlainStringValue getSelectPosterString() {
		return this.selectPosterString;
	}

	public PlainStringValue getCreatedByString() {
		return this.createdByString;
	}

	private void generateSnapshots() {
		this.snapshotListState.clear();
		if( this.videoComposite.getView().getVideoPlayer() instanceof VlcjVideoPlayer ) {
			final VlcjVideoPlayer videoPlayer = (VlcjVideoPlayer)this.videoComposite.getView().getVideoPlayer();

			String mediaPath = videoPlayer.getMediaPath();
			float length = videoPlayer.getLengthInSeconds();
			ThreadHelper.runInBackground( ( ) -> {
				for( float position : new float[] { 0.10f, 0.50f, 0.90f } ) {
					float seconds = length * position;
					final java.awt.Image snapshot = edu.wustl.lookingglass.media.FFmpegImageExtractor.getFrameAt( mediaPath, seconds );
					if( snapshot != null ) {
						ThreadHelper.runOnSwingThread( ( ) -> {
							snapshotListState.addItem( snapshot );
						} );
					}
				}
			} );
		}
	}

	private void updateUserString() {
		String original = findLocalizedText( "createdBy" );
		String username = LookingGlassIDE.getCommunityController().getUsername();

		if( username.isEmpty() ) {
			this.createdByString.setText( "" );
		} else {
			String value = original.replace( "</value/>", username );
			this.createdByString.setText( value );
		}
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		java.io.File file = getOwner().getRecordedVideo();
		this.videoComposite.getView().setUri( file.toURI() );

		// Start playing the video, so you can see the preview.
		VlcjVideoPlayer videoPlayer = (VlcjVideoPlayer)this.videoComposite.getView().getVideoPlayer();
		videoPlayer.setPosition( 0.0f );
		videoPlayer.playResume();

		// This must be called after play resume so the video player reports the time correctly.
		this.generateSnapshots();

		LookingGlassIDE.getCommunityController().addAndInvokeObserver( this );
	}

	@Override
	public void handlePostDeactivation() {
		LookingGlassIDE.getCommunityController().removeObserver( this );
		super.handlePostDeactivation();
	}

	@Override
	public void connectionChanged( ConnectionStatus status ) {
	}

	@Override
	public void accessChanged( AccessStatus status ) {
		ThreadHelper.runOnSwingThread( ( ) -> {
			if( status == AccessStatus.USER_ACCESS ) {
				updateUserString();
			}
		} );
	}
}
