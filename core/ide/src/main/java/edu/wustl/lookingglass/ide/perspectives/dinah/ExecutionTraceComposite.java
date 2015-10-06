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

import org.lgna.croquet.views.BorderPanel;

import edu.wustl.lookingglass.ide.perspectives.dinah.finder.rightnowtree.ExecutionTraceTreeModel;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ExpressionStatementEventNode;

public class ExecutionTraceComposite extends org.lgna.croquet.SimpleComposite<org.lgna.croquet.views.BorderPanel> implements TimeScrubUpdateable {

	public static Color IS_EXECUTING_COLOR = new java.awt.Color( 32, 175, 37 );

	private DinahProgramManager programManager;

	private ExecutingStatementsComposite executingStatementsComposite;
	private TimeScrubComposite timeScrubComposite;

	private BorderPanel programContainer;

	public ExecutionTraceComposite() {
		super( java.util.UUID.fromString( "c3436cd3-6e58-4918-bb8b-6e4e965cff8f" ) );
		this.executingStatementsComposite = new ExecutingStatementsComposite();
		this.timeScrubComposite = new TimeScrubComposite();

		this.registerSubComposite( this.executingStatementsComposite );
		this.registerSubComposite( this.timeScrubComposite );

		this.programContainer = new BorderPanel();
	}

	@Override
	public void setProgramManager( DinahProgramManager programManager ) {
		this.programManager = programManager;
		this.getExecutingStatementsComposite().setProgramManager( programManager );
		this.getTimeScrubComposite().setProgramManager( programManager );

		synchronized( getView().getTreeLock() ) {
			getView().addPageStartComponent( programContainer );
			org.lgna.croquet.views.MigPanel panel = new org.lgna.croquet.views.MigPanel( null, "fill, ins 0", "[]", "[][]" );
			panel.addComponent( getTimeScrubComposite().getView(), "cell 0 0, growx" );
			panel.addComponent( getExecutingStatementsComposite().getView(), "cell 0 1, grow, push" );
			getView().addCenterComponent( panel );
		}
		getView().revalidateAndRepaint();
	}

	@Override
	public void removeProgramManager() {
		synchronized( getView().getTreeLock() ) {
			getView().removeAllComponents();
		}
		this.getExecutingStatementsComposite().removeProgramManager();
		this.getTimeScrubComposite().removeProgramManager();

		this.programManager = null;
	}

	public DinahProgramManager getProgramManager() {
		return this.programManager;
	}

	public ExecutingStatementsComposite getExecutingStatementsComposite() {
		return this.executingStatementsComposite;
	}

	public TimeScrubComposite getTimeScrubComposite() {
		return this.timeScrubComposite;
	}

	public void resetProgramContainer() {
		synchronized( getView().getTreeLock() ) {
			if( ( getView() != null ) && ( edu.wustl.lookingglass.remix.share.ShareRemixComposite.getInstance().getProgramContainer() != null ) ) {
				getView().addPageStartComponent( edu.wustl.lookingglass.remix.share.ShareRemixComposite.getInstance().getProgramContainer() );
				this.programContainer = (BorderPanel)this.getView().getPageStartComponent();
				this.getView().revalidateAndRepaint();
			}
		}

	}

	public org.lgna.croquet.views.SwingComponentView<?> getProgramContainer() {
		return this.programContainer;
	}

	@Override
	protected BorderPanel createView() {
		return new BorderPanel();
	}

	@Override
	public void update( ProgramStateEvent programStateEvent ) {
		this.executingStatementsComposite.update( programStateEvent );
		this.timeScrubComposite.update( programStateEvent );
	}

	@Override
	public void update( ProgramExecutionEvent programExecutionEvent, boolean isStartEvent ) {
		this.executingStatementsComposite.update( programExecutionEvent, isStartEvent );
		this.timeScrubComposite.update( programExecutionEvent, isStartEvent );

		ExecutionTraceTreeModel model = getExecutingStatementsComposite().getExecutionTree().getModel();
		synchronized( model ) {
			if( programExecutionEvent.getEventNode() instanceof ExpressionStatementEventNode ) {
				if( isStartEvent ) {
					getExecutingStatementsComposite().getExecutionTree().addEventNode( (ExpressionStatementEventNode)programExecutionEvent.getEventNode() );
				} else {
					getExecutingStatementsComposite().getExecutionTree().removeEventNode( (ExpressionStatementEventNode)programExecutionEvent.getEventNode() );
				}
			}
		}
	}
}
