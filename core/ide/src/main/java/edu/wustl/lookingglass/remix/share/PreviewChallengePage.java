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

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.alice.stageide.ast.StoryApiSpecificAstUtilities;
import org.alice.stageide.sceneeditor.ThumbnailGenerator;
import org.lgna.croquet.PlainStringValue;
import org.lgna.croquet.WizardPageComposite;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.project.ast.NamedUserType;
import org.lgna.project.ast.UserMethod;

import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus;
import edu.wustl.lookingglass.community.CommunityStatusObserver;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.remix.share.views.PreviewChallengeView;

/**
 * @author Caitlin Kelleher
 */
public class PreviewChallengePage extends WizardPageComposite<PreviewChallengeView, AbstractShareComposite> implements CommunityStatusObserver {
	private final ErrorStatus noTitleError = this.createErrorStatus( "noTitleError" );
	private final ErrorStatus noDescriptionError = this.createErrorStatus( "noDescriptionError" );
	private final ErrorStatus communityErrorStatus = this.createErrorStatus( "communityErrorStatus" );

	private final PlainStringValue instructionsString = this.createStringValue( "instructions" );
	private final PlainStringValue codeWarningString = this.createStringValue( "codeWarning" );

	public PreviewChallengePage( AbstractShareComposite owner ) {
		super( java.util.UUID.fromString( "895d44a7-6bcf-48d8-8e93-69628428cf99" ), owner );
	}

	@Override
	public void resetData() {
	}

	@Override
	public org.lgna.croquet.AbstractSeverityStatusComposite.Status getPageStatus( CompletionStep<?> step ) {
		org.lgna.croquet.AbstractSeverityStatusComposite.Status rv = IS_GOOD_TO_GO_STATUS;

		String titleValue = getOwner().getTitleState().getValue();
		String descriptionValue = getOwner().getDescriptionState().getValue();

		if( ( titleValue == null ) || ( titleValue.length() == 0 ) ) {
			rv = noTitleError;
		} else if( ( descriptionValue == null ) || ( descriptionValue.length() == 0 ) ) {
			rv = noDescriptionError;
		} else if( invalidCommunityStatus() ) {
			rv = communityErrorStatus;
		}

		return rv;
	}

	private boolean invalidCommunityStatus() {
		return !( ( LookingGlassIDE.getCommunityController().getConnectionStatus() == ConnectionStatus.CONNECTED ) && ( LookingGlassIDE.getCommunityController().getAccessStatus() == AccessStatus.USER_ACCESS ) );
	}

	@Override
	protected PreviewChallengeView createView() {
		return new PreviewChallengeView( this );
	}

	public PlainStringValue getInstructionsString() {
		return this.instructionsString;
	}

	public PlainStringValue getCodeWarningString() {
		return this.codeWarningString;
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		ThumbnailGenerator generator = new ThumbnailGenerator( (int)getOwner().getRecordingSize().getWidth(), (int)getOwner().getRecordingSize().getHeight() );
		BufferedImage posterImage = generator.createThumbnail();

		if( posterImage != null ) {
			getOwner().setPosterImage( posterImage );
			getView().setPoster( new ImageIcon( posterImage ) );
		}

		getView().setWarningVisible( checkCodeInWorld() );

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
			getView().setLoginDialogShowing( status != AccessStatus.USER_ACCESS );
		} );
	}

	private boolean checkCodeInWorld() {
		NamedUserType sceneType = StoryApiSpecificAstUtilities.getSceneTypeFromProject( LookingGlassIDE.getActiveInstance().getProject() );
		UserMethod userMain = StoryApiSpecificAstUtilities.getUserMethodsInvokedSceneActivationListeners( sceneType ).get( 0 );

		return userMain.body.getValue().statements.size() > 0;
	}
}
