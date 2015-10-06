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
package edu.wustl.lookingglass.ide.croquet.components;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

import org.lgna.croquet.event.ValueEvent;
import org.lgna.project.ast.Statement;

import edu.wustl.lookingglass.ide.program.ProgramState;
import edu.wustl.lookingglass.ide.program.TimeScrubProgramImp;
import edu.wustl.lookingglass.remix.models.EndCaptureState;
import edu.wustl.lookingglass.remix.models.StartCaptureState;
import edu.wustl.lookingglass.remix.models.StatementMenuOperation;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;

enum DrawState {
	NOT_RUN( java.awt.Color.LIGHT_GRAY, java.awt.Color.GRAY ),
	CURRENT( new java.awt.Color( 32, 175, 37 ), java.awt.Color.YELLOW ),
	HAS_RUN( new java.awt.Color( 207, 230, 207 ), new java.awt.Color( 66, 77, 66 ) );

	final java.awt.Color baseColor;
	final java.awt.Color crosshairColor;

	DrawState( java.awt.Color baseColor, java.awt.Color crosshairColor ) {
		this.baseColor = baseColor;
		this.crosshairColor = crosshairColor;
	}
}

public class StatementMenuButton extends org.lgna.croquet.views.Button {
	public static java.awt.Color IS_RUNNING_COLOR = DrawState.CURRENT.baseColor;

	private final Statement statement;
	private AbstractEventNode<?> node;
	private DrawState currentDrawState = DrawState.NOT_RUN;
	private ProgramState programState;

	private org.lgna.croquet.event.ValueListener<AbstractEventNode<?>> captureStateListener;

	public StatementMenuButton( StatementMenuOperation model, Statement statement ) {
		super( model );
		this.statement = statement;
		this.programState = ProgramState.PAUSED_LIVE;
		this.captureStateListener = new org.lgna.croquet.event.ValueListener<AbstractEventNode<?>>() {

			@Override
			public void valueChanged( ValueEvent<AbstractEventNode<?>> e ) {
				( (JStatementButton)StatementMenuButton.this.getAwtComponent() ).updateStartAndOrEndStatus();
			}
		};
		updateDrawState();
	}

	public void updateProgramState( ProgramState programState ) {
		this.programState = programState;
	}

	public void updateDrawState() {
		if( this.getModel().getProgramManager() != null ) {
			TimeScrubProgramImp program = this.getModel().getProgramManager().getProgramImp();
			boolean isCurrentNode = false;
			java.util.List<AbstractEventNode<?>> nodes = program.getProgramStatementManager().getCurrentEventNodes();
			if( nodes != null ) {
				for( AbstractEventNode<?> node : nodes ) {
					if( node.getAstNode() == this.statement ) {
						isCurrentNode = true;
					}
				}
			}
			if( isCurrentNode ) {
				this.currentDrawState = DrawState.CURRENT;
			} else {
				if( program != null ) {
					boolean hasRun = program.getExecutionObserver().eventNodesExistForStatement( this.statement );
					this.currentDrawState = hasRun ? DrawState.HAS_RUN : DrawState.NOT_RUN;
				}
			}
			( (JStatementButton)this.getAwtComponent() ).updateIconBasedOnDrawState( this.currentDrawState );
			( (JStatementButton)this.getAwtComponent() ).updateStartAndOrEndStatus();
		}
	}

	public void updateDrawState( AbstractEventNode<?> node, boolean isStartEvent ) {

		boolean isCurrent = false;
		if( node.getAstNode().getId().equals( this.statement.getId() ) ) {
			this.node = node;
			isCurrent = true;
		}

		if( ( this.node != null ) && !( this.programState.isLive() ) ) {
			isCurrent = ( this.node.getStartTime() <= node.getStartTime() ) && ( this.node.getEndTime() >= node.getEndTime() );
		}

		if( isCurrent ) {
			if( isStartEvent ) {
				this.currentDrawState = DrawState.CURRENT;
			} else {
				boolean hasRun = this.getModel().getProgramManager().getProgramImp().getExecutionObserver().eventNodesExistForStatement( (Statement)node.getAstNode() );
				this.currentDrawState = hasRun ? DrawState.HAS_RUN : DrawState.NOT_RUN;
			}
			( (JStatementButton)this.getAwtComponent() ).updateIconBasedOnDrawState( this.currentDrawState );
		}
	}

	@Override
	public StatementMenuOperation getModel() {
		return (StatementMenuOperation)super.getModel();
	}

	@Override
	protected void handleDisplayable() {
		super.handleDisplayable();
		EndCaptureState.getInstance().addListener( captureStateListener );
		StartCaptureState.getInstance().addListener( captureStateListener );
	}

	@Override
	protected void handleUndisplayable() {
		this.node = null;
		super.handleUndisplayable();
	}

	public class JStatementButton extends JButton {
		private DrawState currentDrawState = DrawState.NOT_RUN;
		private boolean isTheStartSelection = false;
		private boolean isTheEndSelection = false;
		private boolean isPartOfRemix = false;

		public JStatementButton() {
			//todo: investigate why we need this?
			this.setAction( StatementMenuButton.this.getModel().getImp().getSwingModel().getAction() );

			this.setBackground( java.awt.Color.LIGHT_GRAY );
			this.setToolTipText( "Click for action information" );

			this.setIcon( new StatementExecutionStatusIcon( new java.awt.Dimension( 24, 24 ) ) );
			this.setBorder( BorderFactory.createEmptyBorder() );
			this.setOpaque( false );
			this.setRolloverEnabled( true );
			this.setBorder( BorderFactory.createEmptyBorder( 3, 3, 0, 3 ) );

			this.setCursor( new java.awt.Cursor( java.awt.Cursor.HAND_CURSOR ) );
		}

		public boolean isTheStartSelection() {
			return this.isTheStartSelection;
		}

		public boolean isTheEndSelection() {
			return this.isTheEndSelection;
		}

		public boolean isPartOfRemix() {
			return this.isPartOfRemix;
		}

		public java.awt.Color getBaseColor() {
			return currentDrawState != null ? currentDrawState.baseColor : java.awt.Color.RED;
		}

		public java.awt.Color getCrosshairColor() {
			return currentDrawState != null ? currentDrawState.crosshairColor : java.awt.Color.BLACK;
		}

		public DrawState getCurrentDrawState() {
			return this.currentDrawState;
		}

		public void updateStartAndOrEndStatus() {
			boolean nextStart = StartCaptureState.getInstance().isStatementSelected( statement );
			boolean nextEnd = EndCaptureState.getInstance().isStatementSelected( statement );
			boolean nextPartOfRemix = edu.wustl.lookingglass.remix.ast.RemixUtilities.isInRemix( statement );

			if( ( nextStart != this.isTheStartSelection ) || ( nextEnd != this.isTheEndSelection ) || ( nextPartOfRemix != this.isPartOfRemix ) ) {
				this.isTheStartSelection = nextStart;
				this.isTheEndSelection = nextEnd;
				this.isPartOfRemix = nextPartOfRemix;
				this.repaint();
			}
		}

		protected void updateIconBasedOnDrawState( DrawState state ) {
			this.currentDrawState = state;
			this.repaint();
		}

		@Override
		public void updateUI() {
			this.setUI( new BasicButtonUI() );
		}
	}

	@Override
	protected javax.swing.JButton createAwtComponent() {
		return new JStatementButton();
	}
}
