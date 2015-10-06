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
package edu.wustl.lookingglass.ide.perspectives.dinah.finder.operations;

import org.alice.ide.highlight.IdeHighlightStencil;
import org.lgna.croquet.ActionOperation;
import org.lgna.project.ast.Statement;

import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.perspectives.dinah.DinahProgramManager;
import edu.wustl.lookingglass.ide.perspectives.dinah.finder.ReplayStatementStencilComponentListener;
import edu.wustl.lookingglass.ide.program.TimeScrubProgramImp;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ExpressionStatementEventNode;

public class ShowStatementLocationOperation extends ActionOperation {
	private Statement statement;
	private AbstractEventNode<?> eventNode;
	private DinahProgramManager programManager;

	public ShowStatementLocationOperation( ExpressionStatementEventNode eventNode, DinahProgramManager programManager ) {
		super( edu.wustl.lookingglass.remix.models.ReuseGroup.REUSE_GROUP, java.util.UUID.fromString( "4920a605-2e11-4811-a3d7-a00a603030c2" ) );

		this.eventNode = eventNode;
		this.statement = eventNode.getAstNode();
		this.programManager = programManager;

		this.setEnabled( true );
	}

	protected ShowStatementLocationOperation( java.util.UUID uuid ) {
		super( edu.wustl.lookingglass.remix.models.ReuseGroup.REUSE_GROUP, uuid );

		this.setEnabled( false );
	}

	public void setEventNode( AbstractEventNode<?> eventNode ) {
		this.eventNode = eventNode;

		if( this.eventNode instanceof ExpressionStatementEventNode ) {
			this.statement = (Statement)this.eventNode.getAstNode();
		}

		boolean shouldBeEnabled = ( this.statement != null ) || ( this.eventNode != null );
		this.setEnabled( shouldBeEnabled );
	}

	@Override
	protected void perform( org.lgna.croquet.history.CompletionStep<?> step ) {

		TimeScrubProgramImp program = LookingGlassIDE.getActiveInstance().getDinahProgramImp();
		if( program != null ) {

			IdeHighlightStencil highlightStencil = new IdeHighlightStencil( getFrame(), javax.swing.JLayeredPane.POPUP_LAYER - 2 );
			ReplayStatementStencilComponentListener stencilShowingListener = new ReplayStatementStencilComponentListener( this.eventNode, highlightStencil, this.programManager );

			highlightStencil.addComponentListener( stencilShowingListener );
			program.pauseProgram();

			LookingGlassIDE.getActiveInstance().makeStatementVisible( this.statement );
			highlightStencil.showHighlightOverStatementAndRenderWindow( this.statement );
		}
		step.finish();
	}
}
