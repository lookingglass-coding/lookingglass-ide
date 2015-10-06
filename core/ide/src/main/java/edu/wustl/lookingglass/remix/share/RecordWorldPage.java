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

import org.lgna.croquet.BooleanState;
import org.lgna.croquet.PlainStringValue;
import org.lgna.croquet.WizardPageComposite;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.event.ValueListener;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.croquet.views.BorderPanel;

import edu.wustl.lookingglass.community.CommunityProjectPropertyManager;
import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus;
import edu.wustl.lookingglass.community.CommunityStatusObserver;
import edu.wustl.lookingglass.community.api.packets.WorldPacket;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.LookingGlassTheme;
import edu.wustl.lookingglass.remix.share.views.RecordWorldView;

/**
 * @author Caitlin Kelleher
 */
public class RecordWorldPage extends WizardPageComposite<RecordWorldView, ShareWorldComposite> implements CommunityStatusObserver {
	private final ErrorStatus noVideoStatus = this.createErrorStatus( "noVideoStatus" );
	private final ErrorStatus noTitleStatus = this.createErrorStatus( "noTitleStatus" );
	private final ErrorStatus noDescriptionStatus = this.createErrorStatus( "noDescriptionStatus" );
	private final ErrorStatus communityErrorStatus = this.createErrorStatus( "communityErrorStatus" );

	private final BooleanState isRecordingState = this.createBooleanState( "isRecordingState", false );
	private final PlainStringValue instructionsString = this.createStringValue( "instructions" );
	private final PlainStringValue timeString = this.createStringValue( "time" );

	private final ValueListener<Boolean> isRecordingListener;
	private final ValueListener<Boolean> isUpdateListener;

	private boolean isWaitingForPacket = false;

	public RecordWorldPage( ShareWorldComposite owner ) {
		super( java.util.UUID.fromString( "6907b85f-ffc2-4050-9ac2-3ece85a2018c" ), owner );
		this.isRecordingState.setIconForTrueAndIconForFalse( LookingGlassTheme.getIcon( "world-stop", org.lgna.croquet.icon.IconSize.SMALL ), LookingGlassTheme.getIcon( "world-record", org.lgna.croquet.icon.IconSize.SMALL ) );
		this.isRecordingState.setTextForTrueAndTextForFalse( "Stop Recording", "Start Recording" );

		this.isRecordingListener = new ValueListener<Boolean>() {
			@Override
			public void valueChanged( ValueEvent<Boolean> e ) {
				if( e.getNextValue() ) {
					getOwner().startRecording();
				} else {
					getOwner().pauseRecording();
				}
				refreshOwnerStatus();
			}
		};

		this.isUpdateListener = new ValueListener<Boolean>() {

			@Override
			public void valueChanged( ValueEvent<Boolean> e ) {
				if( e.getNextValue() ) {
					setValuesForUpdate();
				}
			}

		};
	}

	@Override
	public void resetData() {
	}

	@Override
	public org.lgna.croquet.AbstractSeverityStatusComposite.Status getPageStatus( CompletionStep<?> step ) {
		org.lgna.croquet.AbstractSeverityStatusComposite.Status rv = IS_GOOD_TO_GO_STATUS;

		String titleValue = getOwner().getTitleState().getValue();
		String descriptionValue = getOwner().getDescriptionState().getValue();

		if( titleValue.isEmpty() ) {
			rv = noTitleStatus;
		} else if( descriptionValue.isEmpty() ) {
			rv = noDescriptionStatus;
		} else if( ( getOwner().getFramesRecorded() < 10 ) || ( isRecordingState.getValue() ) ) {
			rv = noVideoStatus;
		} else if( invalidCommunityStatus() ) {
			rv = communityErrorStatus;
		}
		return rv;
	}

	private boolean invalidCommunityStatus() {
		return !( ( LookingGlassIDE.getCommunityController().getConnectionStatus() == ConnectionStatus.CONNECTED ) && ( LookingGlassIDE.getCommunityController().getAccessStatus() == AccessStatus.USER_ACCESS ) );
	}

	@Override
	protected RecordWorldView createView() {
		return new RecordWorldView( this );
	}

	public PlainStringValue getInstructionsString() {
		return this.instructionsString;
	}

	public PlainStringValue getTimeString() {
		return this.timeString;
	}

	public Dimension getRecordingSize() {
		return getOwner().getRecordingSize();
	}

	public BooleanState getIsRecordingState() {
		return this.isRecordingState;
	}

	public BorderPanel getProgramContainer() {
		return getView().getProgramContainer();
	}

	public void resetProgramContainer() {
		getView().resetProgramContainer();
	}

	public void setRecordTime( double seconds ) {
		int numSeconds = new Double( Math.floor( seconds ) ).intValue();
		int numFractionalSeconds = new Double( Math.floor( ( seconds - numSeconds ) * 100 ) ).intValue();
		int numMinutes = numSeconds / 60;
		int numRemainingSeconds = numSeconds % 60;

		String timeText = numMinutes + ":";
		if( numRemainingSeconds < 10 ) {
			timeText += "0" + numRemainingSeconds + ".";
		} else {
			timeText += numRemainingSeconds + ".";
		}

		if( numFractionalSeconds < 10 ) {
			timeText += "0" + numFractionalSeconds;
		} else {
			timeText += numFractionalSeconds;
		}

		final String time = timeText;
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnSwingThread( ( ) -> timeString.setText( time ) );
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		this.isRecordingState.addNewSchoolValueListener( this.isRecordingListener );
		this.isRecordingState.setEnabled( true );
		this.isRecordingState.setValueTransactionlessly( false );
		this.isRecordingState.updateNameAndIcon();
		this.timeString.setText( "0:00.00" );
		this.getOwner().getIsUpdateState().addNewSchoolValueListener( this.isUpdateListener );

		LookingGlassIDE.getCommunityController().addAndInvokeObserver( this );
	}

	@Override
	public void handlePostDeactivation() {
		this.isRecordingState.removeNewSchoolValueListener( this.isRecordingListener );
		this.getOwner().getIsUpdateState().removeNewSchoolValueListener( this.isUpdateListener );
		this.getOwner().stopRecording();

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
				getView().setLoginDialogShowing( false );
				checkForExistingWorld();
			} else {
				getView().setLoginDialogShowing( true );
			}
		} );
	}

	public void closeUpdateDialog() {
		getView().setUpdateDialogShowing( false );
	}

	public void checkForExistingWorld() {
		Integer contentId = CommunityProjectPropertyManager.getCommunityProjectID( LookingGlassIDE.getActiveInstance().getProject() );
		Integer userId = CommunityProjectPropertyManager.getProjectUserID( LookingGlassIDE.getActiveInstance().getProject() );

		if( contentId != null ) {
			if( ( userId == null ) && ( getOwner().getWorldPacket() != null ) ) {
				userId = getOwner().getWorldPacket().getUserId();
			}

			if( userId == null ) {
				this.isWaitingForPacket = true;
				return;
			}

			if( userId == LookingGlassIDE.getCommunityController().getCurrentUser().getId() ) {
				getOwner().setContentId( contentId );
				getView().setUpdateDialogShowing( true );
			}
		}
	}

	private void setValuesForUpdate() {
		if( getOwner().getWorldPacket() != null ) {
			WorldPacket world = getOwner().getWorldPacket();

			getOwner().getTitleState().setValueTransactionlessly( world.getTitle() );
			getOwner().getDescriptionState().setValueTransactionlessly( world.getDescription() );
			getOwner().getTagState().setValueTransactionlessly( world.getTags() );
		}
	}

	public boolean isWaitingForPacket() {
		return this.isWaitingForPacket;
	}
}
