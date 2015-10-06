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
package edu.wustl.lookingglass.ide.perspectives.dinah.processbar;

import org.lgna.croquet.CancelException;
import org.lgna.croquet.CardOwnerComposite;
import org.lgna.croquet.Composite;
import org.lgna.croquet.edits.Edit;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.event.ValueListener;
import org.lgna.croquet.history.CompletionStep;

import edu.wustl.lookingglass.ide.LookingGlassTheme;
import edu.wustl.lookingglass.ide.perspectives.dinah.DinahProgramManager;
import edu.wustl.lookingglass.ide.perspectives.dinah.TimeScrubUpdateable;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.remix.models.EndCaptureState;
import edu.wustl.lookingglass.remix.models.StartCaptureState;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;

public class StepInstructionsCardComposite extends CardOwnerComposite implements TimeScrubUpdateable {

	private final MoveTimeSliderCard moveSliderCard;
	private final SelectStartCard selectStartCard;
	private final SelectEndCard selectEndCard;

	private final org.lgna.croquet.Operation showNextCardOperation;
	private final org.lgna.croquet.Operation showPreviousCardOperation;
	private final org.lgna.croquet.Operation shareOrUseOperation;

	public StepInstructionsCardComposite( org.lgna.croquet.Operation shareOrUseOperation ) {
		super( java.util.UUID.fromString( "fd4a61e4-565f-466b-88a9-3d822041e6d3" ) );

		this.moveSliderCard = new MoveTimeSliderCard( this );
		this.selectStartCard = new SelectStartCard( this );
		this.selectEndCard = new SelectEndCard( this );

		this.addCard( this.moveSliderCard );
		this.addCard( this.selectStartCard );
		this.addCard( this.selectEndCard );

		this.shareOrUseOperation = shareOrUseOperation;

		this.showNextCardOperation = this.createActionOperation( "showNextOperation", new Action() {
			@Override
			public Edit perform( CompletionStep<?> step, org.lgna.croquet.AbstractComposite.InternalActionOperation source ) throws CancelException {
				if( nextCardExists() ) {
					showNextCard();
				}
				return null;
			}
		} );

		this.showPreviousCardOperation = this.createActionOperation( "showPreviousOperation", new Action() {
			@Override
			public Edit perform( CompletionStep<?> step, org.lgna.croquet.AbstractComposite.InternalActionOperation source ) throws CancelException {
				if( previousCardExists() ) {
					showPreviousCard();
				}
				return null;
			}
		} );

		this.showNextCardOperation.setButtonIcon( LookingGlassTheme.getIcon( "go-next", org.lgna.croquet.icon.IconSize.SMALL ) );
		this.showPreviousCardOperation.setButtonIcon( LookingGlassTheme.getIcon( "go-previous", org.lgna.croquet.icon.IconSize.SMALL ) );

		StartCaptureState.getInstance().addListener( new ValueListener<AbstractEventNode<?>>() {

			@Override
			public void valueChanged( ValueEvent<AbstractEventNode<?>> event ) {
				for( Composite<?> card : getCards() ) {
					( (GuideStepComposite)card ).startCaptureStateChange( event.getNextValue() );
				}
				setShareOrUseEnabled();
			}
		} );

		EndCaptureState.getInstance().addListener( new ValueListener<AbstractEventNode<?>>() {

			@Override
			public void valueChanged( ValueEvent<AbstractEventNode<?>> event ) {
				for( Composite<?> card : getCards() ) {
					( (GuideStepComposite)card ).endCaptureStateChange( event.getNextValue() );
				}
				setShareOrUseEnabled();
			}
		} );
	}

	public MoveTimeSliderCard getMoveSliderCard() {
		return this.moveSliderCard;
	}

	public SelectStartCard getSelectStartCard() {
		return this.selectStartCard;
	}

	public SelectEndCard getSelectEndCard() {
		return this.selectEndCard;
	}

	public org.lgna.croquet.Operation getShowNextCardOperation() {
		return this.showNextCardOperation;
	}

	public org.lgna.croquet.Operation getShowPreviousCardOperation() {
		return this.showPreviousCardOperation;
	}

	public org.lgna.croquet.Operation getShareOrUseOperation() {
		return shareOrUseOperation;
	}

	private void setShareOrUseEnabled() {
		boolean startSet = StartCaptureState.getInstance().getValue() != null;
		boolean endSet = EndCaptureState.getInstance().getValue() != null;
		this.shareOrUseOperation.setEnabled( startSet && endSet );
	}

	private void showNextCard() {
		int nextIndex = this.getCards().indexOf( this.getShowingCard() ) + 1;
		this.showCard( this.getCards().get( nextIndex ) );

		this.showNextCardOperation.setEnabled( nextCardExists() );
	}

	private void showPreviousCard() {
		int previousIndex = this.getCards().indexOf( this.getShowingCard() ) - 1;
		this.showCard( this.getCards().get( previousIndex ) );

		this.showPreviousCardOperation.setEnabled( previousCardExists() );
	}

	private boolean nextCardExists() {
		int nextIndex = this.getCards().indexOf( this.getShowingCard() ) + 1;
		return nextIndex < this.getCards().size();
	}

	private boolean previousCardExists() {
		int previousIndex = this.getCards().indexOf( this.getShowingCard() ) - 1;
		return previousIndex >= 0;
	}

	@Override
	public void update( ProgramStateEvent programStateEvent ) {
		for( Composite<?> card : getCards() ) {
			( (GuideStepComposite)card ).programStateChange( programStateEvent );
		}
	}

	@Override
	public void update( ProgramExecutionEvent programExecutionEvent, boolean isStartEvent ) {
	}

	@Override
	public void setProgramManager( DinahProgramManager programManager ) {
		this.selectEndCard.setProgramManager( programManager );
	}

	@Override
	public void removeProgramManager() {
	}
}
