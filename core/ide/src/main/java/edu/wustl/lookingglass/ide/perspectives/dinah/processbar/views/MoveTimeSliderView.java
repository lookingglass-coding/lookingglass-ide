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
package edu.wustl.lookingglass.ide.perspectives.dinah.processbar.views;

import java.awt.Color;

import org.lgna.croquet.views.Button;
import org.lgna.croquet.views.HorizontalTextPosition;
import org.lgna.croquet.views.HtmlMultiLineLabel;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.Separator;

import edu.wustl.lookingglass.ide.perspectives.dinah.processbar.GuideStepPanel;
import edu.wustl.lookingglass.ide.perspectives.dinah.processbar.MoveTimeSliderCard;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;

/**
 * @author Michael Pogran
 */
public class MoveTimeSliderView extends GuideStepPanel {

	private Label stateLabel;

	public MoveTimeSliderView( MoveTimeSliderCard composite ) {
		this.stateLabel = new Label();
		this.setStateLabel( "Is Paused", Color.RED );
		this.stateLabel.setBorder( javax.swing.BorderFactory.createEmptyBorder( 10, 0, 0, 0 ) );

		addComponent( this.stateLabel, "cell 0 0, center" );
		addComponent( Separator.createInstanceSeparatingTopFromBottom(), "cell 0 1, growx" );

		HtmlMultiLineLabel step0HelpLabel = new HtmlMultiLineLabel( getHelpText( 0 ) );
		step0HelpLabel.setBackgroundColor( null );

		addComponent( step0HelpLabel, "cell 0 2, grow" );
		addComponent( Separator.createInstanceSeparatingTopFromBottom(), "cell 0 3, growx" );

		Button hightlightRightNowButton = composite.getHighlightRightNowPanelOperation().createButton();
		HtmlMultiLineLabel step1HelpLabel = new HtmlMultiLineLabel( getHelpText( 1 ) );
		step1HelpLabel.setBackgroundColor( null );

		addComponent( hightlightRightNowButton, "cell 0 4" );
		addComponent( step1HelpLabel, "cell 0 4, grow" );
		addComponent( Separator.createInstanceSeparatingTopFromBottom(), "cell 0 5, growx" );

		Button highlightStepPanelButton = composite.getHighlightStepPanelOperation().createButton();
		HtmlMultiLineLabel step2HelpLabel = new HtmlMultiLineLabel( getHelpText( 2 ) );
		step2HelpLabel.setBackgroundColor( null );

		addComponent( highlightStepPanelButton, "cell 0 6" );
		addComponent( step2HelpLabel, "cell 0 6, grow" );
		addComponent( Separator.createInstanceSeparatingTopFromBottom(), "cell 0 7, growx" );

		Button nextButton = composite.getParentComposite().getShowNextCardOperation().createButton();
		Button prevButton = composite.getParentComposite().getShowPreviousCardOperation().createButton();

		nextButton.setHorizontalTextPosition( HorizontalTextPosition.LEFT );

		addComponent( prevButton, "cell 0 8, left, top" );
		addComponent( nextButton, "cell 0 8, right, top,  push" );
	}

	protected String getHelpText( int stepNumber ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "<p style='font-family: sans-serif;'>" );

		if( stepNumber == 0 ) {
			sb.append( "Use this interface to capture actions and create a " );
			sb.append( "<span style='font-weight: bold;'>" );
			sb.append( "remix" );
			sb.append( "</span>" );
			sb.append( " Remixes can be shared online or applied to characters in your current project. Follow these steps to complete the remixing process." );
		}
		else if( stepNumber == 1 ) {
			sb.append( "Find the action you want to start your remix.  Use the " );
			sb.append( "<span style='font-weight: bold;'>" );
			sb.append( "right now panel" );
			sb.append( "</span>" );
			sb.append( " to see what actions are currently playing." );
		}
		else if( stepNumber == 2 ) {
			sb.append( "Use the " );
			sb.append( "<span style='font-weight: bold;'>" );
			sb.append( "slider" );
			sb.append( "</span>" );
			sb.append( " and " );
			sb.append( "<span style='font-weight: bold;'>" );
			sb.append( "step buttons" );
			sb.append( "</span>" );
			sb.append( " to move foward and backward through your world until you find the action you are looking for." );
		}
		else {
			return "";
		}
		sb.append( "</p>" );

		return sb.toString();
	}

	@Override
	protected void setStateLabel( String text, Color color ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "<html><p style='font-weight:bold;'>" );
		sb.append( "My World:" );
		sb.append( "<span style='color:" );
		sb.append( createColorString( color ) );
		sb.append( ";'> " );
		sb.append( text );
		sb.append( "</span></html>" );
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnSwingThread( ( ) -> this.stateLabel.setText( sb.toString() ) );
	}

	@Override
	public void startCaptureStateChange( AbstractEventNode<?> eventNode ) {
	}

	@Override
	public void endCaptureStateChange( AbstractEventNode<?> eventNode ) {
	}

	@Override
	public void programStateChange( ProgramStateEvent event ) {
		if( event.getNextState().isPlaying() ) {
			this.setStateLabel( "Is Playing", Color.GREEN );
		} else {
			if( event.isFinishedExecuting() && ( event.getMaxProgramTime() == event.getTime() ) ) {
				this.setStateLabel( "Has Finished Playing", Color.GRAY );
			} else {
				this.setStateLabel( "Is Paused", Color.RED );
			}
		}
	}

}
