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
package edu.wustl.lookingglass.ide.perspectives.dinah;

import java.awt.Color;

import org.lgna.croquet.SimpleComposite;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.event.ValueListener;

import edu.cmu.cs.dennisc.java.awt.ColorUtilities;
import edu.wustl.lookingglass.ide.perspectives.dinah.finder.rightnowtree.ExecutionTraceTree;
import edu.wustl.lookingglass.ide.perspectives.dinah.finder.rightnowtree.ExecutionTraceTreeModel;
import edu.wustl.lookingglass.ide.perspectives.dinah.views.ExecutingStatementsView;
import edu.wustl.lookingglass.ide.program.components.ProgramTimeSlider;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.ide.program.models.StepBackwardOperation;
import edu.wustl.lookingglass.ide.program.models.StepForwardOperation;
import edu.wustl.lookingglass.remix.models.EndCaptureState;
import edu.wustl.lookingglass.remix.models.StartCaptureState;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;

/**
 * @author Michael Pogran
 */
public class ExecutingStatementsComposite extends SimpleComposite<ExecutingStatementsView> implements TimeScrubUpdateable {

	private DinahProgramManager programManager;

	private StepBackwardOperation stepBackwardOperation;
	private StepForwardOperation stepForwardOperation;
	private ProgramTimeSlider slider;
	private ExecutionTraceTree executionTree;

	private ValueListener<AbstractEventNode<?>> startStateListener;
	private ValueListener<AbstractEventNode<?>> endStateListener;

	public ExecutingStatementsComposite() {
		super( java.util.UUID.fromString( "fe945321-3e21-459f-bcdd-b144ca82dd26" ) );

		this.startStateListener = new ValueListener<AbstractEventNode<?>>() {

			@Override
			public void valueChanged( ValueEvent<AbstractEventNode<?>> event ) {
				ProgramTimeSlider slider = getProgramTimeSlider();
				DinahProgramManager manager = getProgramManager();
				if( ( slider != null ) && ( manager != null ) ) {
					int startPosition;
					if( event.getNextValue() != null ) {
						startPosition = manager.getProgramImp().getProgramStatementManager().getValueForTime( event.getNextValue().getStartTime() );
					} else {
						startPosition = -1;
					}
					slider.setStartStatePosition( startPosition );
					javax.swing.SwingUtilities.invokeLater( ( ) -> {
						slider.repaint();
					} );
				}
			}
		};

		this.endStateListener = new ValueListener<AbstractEventNode<?>>() {

			@Override
			public void valueChanged( ValueEvent<AbstractEventNode<?>> event ) {
				ProgramTimeSlider slider = getProgramTimeSlider();
				DinahProgramManager manager = getProgramManager();
				if( ( slider != null ) && ( manager != null ) ) {
					int endPosition;
					if( event.getNextValue() != null ) {
						endPosition = manager.getProgramImp().getProgramStatementManager().getValueForTime( event.getNextValue().getEndTime() );
					} else {
						endPosition = -1;
					}
					slider.setEndStatePosition( endPosition );
					javax.swing.SwingUtilities.invokeLater( ( ) -> {
						slider.repaint();
					} );
				}
			}
		};
	}

	@Override
	public void setProgramManager( DinahProgramManager programManager ) {
		this.programManager = programManager;

		this.stepBackwardOperation = new StepBackwardOperation( programManager.getProgramImp() );
		this.stepForwardOperation = new StepForwardOperation( programManager.getProgramImp() );
		this.slider = programManager.getProgramImp().getProgramStatementManager().createSlider();
		this.executionTree = new ExecutionTraceTree( new ExecutionTraceTreeModel(), programManager );

		StartCaptureState.getInstance().addListener( this.startStateListener );
		EndCaptureState.getInstance().addListener( this.endStateListener );
	}

	@Override
	public void removeProgramManager() {
		this.stepBackwardOperation.destroy();
		this.stepForwardOperation.destroy();

		StartCaptureState.getInstance().removeListener( this.startStateListener );
		EndCaptureState.getInstance().removeListener( this.endStateListener );

		this.programManager = null;
		this.stepBackwardOperation = null;
		this.stepForwardOperation = null;
		this.slider = null;
		this.executionTree = null;

		this.releaseView();
	}

	public DinahProgramManager getProgramManager() {
		return this.programManager;
	}

	public StepBackwardOperation getStepBackwardOperation() {
		return this.stepBackwardOperation;
	}

	public StepForwardOperation getStepForwardOperation() {
		return this.stepForwardOperation;
	}

	public ProgramTimeSlider getProgramTimeSlider() {
		return this.slider;
	}

	public ExecutionTraceTree getExecutionTree() {
		return this.executionTree;
	}

	@Override
	public void handlePostDeactivation() {
		this.stepBackwardOperation.destroy();
		this.stepForwardOperation.destroy();

		StartCaptureState.getInstance().removeListener( this.startStateListener );
		EndCaptureState.getInstance().removeListener( this.endStateListener );

		this.programManager = null;
		this.stepBackwardOperation = null;
		this.stepForwardOperation = null;
		this.slider = null;
		this.executionTree = null;

		super.handlePostDeactivation();
	}

	@Override
	protected ExecutingStatementsView createView() {
		return new ExecutingStatementsView( this );
	}

	@Override
	public void update( ProgramStateEvent programStateEvent ) {
		Color statusColor = programStateEvent.getNextState().isPlaying() ? new Color( 32, 175, 37 ) : ColorUtilities.createGray( 220 );
		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			getProgramTimeSlider().setStatusColor( statusColor );
			getProgramTimeSlider().repaint();
		} );
	}

	@Override
	public void update( ProgramExecutionEvent programExecutionEvent, boolean isStartEvent ) {
		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			if( getProgramManager().getProgramImp().getProgramState().isLive() ) {
				if( ( programExecutionEvent.getIndex() == 0 ) && isStartEvent ) {
					getView().repaint();
				}
			} else {
				getView().repaint();
			}
		} );
	}
}
