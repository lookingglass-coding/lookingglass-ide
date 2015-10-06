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
import org.lgna.croquet.StringValue;
import org.lgna.croquet.WizardPageComposite;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.event.ValueListener;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.croquet.views.AwtComponentView;
import org.lgna.croquet.views.SwingComponentView;

import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus;
import edu.wustl.lookingglass.community.CommunityStatusObserver;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.LookingGlassTheme;
import edu.wustl.lookingglass.remix.share.views.RecordRemixView;

/**
 * @author Caitlin Kelleher
 */
public class RecordRemixPage extends WizardPageComposite<RecordRemixView, ShareRemixComposite> implements CommunityStatusObserver {
	private final ErrorStatus noVideoStatus = this.createErrorStatus( "noVideoStatus" );
	private final ErrorStatus noTitleError = this.createErrorStatus( "noTitleError" );
	private final ErrorStatus noDescriptionError = this.createErrorStatus( "noDescriptionError" );
	private final ErrorStatus communityErrorStatus = this.createErrorStatus( "communityErrorStatus" );

	private BooleanState isRecordingState = this.createBooleanState( "isRecordingState", false );
	private final PlainStringValue instructionsString = this.createStringValue( "instructions" );
	private final StringValue timeString = this.createStringValue( "time" );

	private final ValueListener<Boolean> isRecordingListener;

	public RecordRemixPage( ShareRemixComposite owner ) {
		super( java.util.UUID.fromString( "d6988d44-41b9-43f0-befe-715cd9e901b3" ), owner );

		isRecordingState.setIconForTrueAndIconForFalse( LookingGlassTheme.getIcon( "world-stop", org.lgna.croquet.icon.IconSize.SMALL ), LookingGlassTheme.getIcon( "world-record", org.lgna.croquet.icon.IconSize.SMALL ) );
		isRecordingState.setTextForTrueAndTextForFalse( "Stop Recording", "Start Recording" );

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
			rv = noTitleError;
		} else if( descriptionValue.isEmpty() ) {
			rv = noDescriptionError;
		} else if( ( getOwner().getEncoder().getEncodedVideoFile() == null ) || ( isRecordingState.getValue() ) ) {
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
	protected RecordRemixView createView() {
		return new RecordRemixView( this );
	}

	public PlainStringValue getInstructionsString() {
		return this.instructionsString;
	}

	public StringValue getTimeString() {
		return this.timeString;
	}

	public BooleanState getIsRecordingState() {
		return this.isRecordingState;
	}

	public Dimension getRecordingSize() {
		return getOwner().getRecordingSize();
	}

	public void setProgramContainer( SwingComponentView<?> programContainer ) {
		getView().setProgramContainer( programContainer );
	}

	public AwtComponentView<?> getProgramContainer() {
		return getView().getProgramContainer();
	}

	public void resetProgramContainer() {
		getView().resetProgramContainer();
	}

	@Override
	public void connectionChanged( ConnectionStatus status ) {
	}

	@Override
	public void accessChanged( AccessStatus status ) {
		ThreadHelper.runOnSwingThread( ( ) -> {
			getView().setLoginDialogShowing( status != AccessStatus.USER_ACCESS );
		} );
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
			timeText += "0" + numFractionalSeconds + " seconds";
		} else {
			timeText += numFractionalSeconds + " seconds";
		}
		timeString.setText( timeText );
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		this.isRecordingState.addNewSchoolValueListener( this.isRecordingListener );
		this.isRecordingState.setEnabled( true );
		this.isRecordingState.setValueTransactionlessly( false );
		this.isRecordingState.updateNameAndIcon();
		this.timeString.setText( "0:00.00" );
		LookingGlassIDE.getCommunityController().addAndInvokeObserver( this );
	}

	@Override
	public void handlePostDeactivation() {
		this.isRecordingState.removeNewSchoolValueListener( this.isRecordingListener );

		LookingGlassIDE.getCommunityController().removeObserver( this );

		super.handlePostDeactivation();
	}
}
