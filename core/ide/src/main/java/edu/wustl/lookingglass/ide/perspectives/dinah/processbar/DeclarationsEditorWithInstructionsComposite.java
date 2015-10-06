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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.lgna.croquet.CancelException;
import org.lgna.croquet.SplitComposite;
import org.lgna.croquet.edits.Edit;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.croquet.views.SplitPane;

import edu.wustl.lookingglass.ide.perspectives.dinah.DinahProgramManager;
import edu.wustl.lookingglass.ide.perspectives.dinah.TimeScrubUpdateable;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.remix.models.StartCaptureState;

public class DeclarationsEditorWithInstructionsComposite extends SplitComposite implements TimeScrubUpdateable {
	private final org.lgna.croquet.ActionOperation helpOperation;
	PropertyChangeListener dividerListener;
	ComponentListener componentListener;
	private int stepsOverviewPanelWidth = 300;
	private boolean dividerLocationInitialized = false;

	public DeclarationsEditorWithInstructionsComposite( org.lgna.croquet.Operation shareOrUseActionOperation ) {
		super( java.util.UUID.fromString( "2d149094-70e6-494d-99ca-f703ed32209c" ), org.alice.ide.IDE.getActiveInstance().getDocumentFrame().getDeclarationsEditorComposite(), new StepInstructionsCardComposite( shareOrUseActionOperation ) );

		this.helpOperation = this.createActionOperation( "helpOperation", new org.lgna.croquet.AbstractComposite.Action() {

			@Override
			public Edit perform( CompletionStep<?> step, org.lgna.croquet.AbstractComposite.InternalActionOperation source ) throws CancelException {
				if( StartCaptureState.getInstance().getValue() != null ) {
					getStepInstructionsComposite().showCard( getStepInstructionsComposite().getSelectEndCard() );
				} else {
					getStepInstructionsComposite().showCard( getStepInstructionsComposite().getMoveSliderCard() );

				}

				setHelpPanelVisible( !isStepsOverviewPanelVisible() );
				return null;
			}
		} );

		this.dividerListener = new PropertyChangeListener() {
			@Override
			public void propertyChange( PropertyChangeEvent evt ) {
				SplitPane sp = getView();
				stepsOverviewPanelWidth = sp.getWidth() - (Integer)evt.getNewValue();
			}
		};

		this.componentListener = new ComponentListener() {
			@Override
			public void componentResized( ComponentEvent e ) {
				SplitPane sp = getView();

				if( !dividerLocationInitialized ) {
					sp.setDividerLocation( sp.getWidth() - 300 );
					dividerLocationInitialized = true;
				} else if( stepsOverviewPanelWidth < 300 ) {
					// this is here in part because maximize/unmaximize results in
					// a target width change.
					stepsOverviewPanelWidth = 300;
					sp.setDividerLocation( sp.getWidth() - stepsOverviewPanelWidth );
				} else {
					sp.setDividerLocation( sp.getWidth() - stepsOverviewPanelWidth );
				}

				// sometimes setting the divider results in strange paint states.
				getTrailingComposite().getView().revalidateAndRepaint();

			}

			@Override
			public void componentShown( ComponentEvent e ) {
			}

			@Override
			public void componentMoved( ComponentEvent e ) {
			}

			@Override
			public void componentHidden( ComponentEvent e ) {
			}
		};
	}

	public StepInstructionsCardComposite getStepInstructionsComposite() {
		return (StepInstructionsCardComposite)getTrailingComposite();
	}

	public org.lgna.croquet.ActionOperation getHelpOperation() {
		return this.helpOperation;
	}

	public boolean isStepsOverviewPanelVisible() {
		return this.getView().getAwtComponent().getRightComponent() != null;
	}

	public void setHelpPanelVisible( boolean visible ) {
		if( visible ) {
			SplitPane splitPane = this.getView();
			splitPane.getAwtComponent().setRightComponent( this.getTrailingComposite().getView().getAwtComponent() );

			splitPane.setDividerLocation( this.getLeadingComposite().getView().getWidth() - 300 );
			splitPane.setDividerSize( 5 );
		} else {
			SplitPane splitPane = this.getView();
			splitPane.getAwtComponent().setRightComponent( null );
			splitPane.setDividerSize( 0 );
		}
	}

	@Override
	protected SplitPane createView() {
		SplitPane rv = this.createHorizontalSplitPane();
		rv.setResizeWeight( 0 );
		rv.getAwtComponent().setEnabled( true );
		rv.addComponentListener( componentListener );
		rv.addDividerLocationChangeListener( dividerListener );
		return rv;
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		SplitPane splitPane = this.getView();
		splitPane.getAwtComponent().setLeftComponent( this.getLeadingComposite().getView().getAwtComponent() );
		this.setHelpPanelVisible( false );
	}

	@Override
	public void update( ProgramStateEvent programStateEvent ) {
		getStepInstructionsComposite().update( programStateEvent );
	}

	@Override
	public void update( ProgramExecutionEvent programExecutionEvent, boolean isStartEvent ) {
	}

	@Override
	public void setProgramManager( DinahProgramManager programManager ) {
		getStepInstructionsComposite().setProgramManager( programManager );
	}

	@Override
	public void removeProgramManager() {
	}

}
