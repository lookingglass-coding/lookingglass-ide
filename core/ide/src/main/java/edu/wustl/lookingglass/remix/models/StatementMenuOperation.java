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
package edu.wustl.lookingglass.remix.models;

import org.lgna.croquet.ActionOperation;
import org.lgna.croquet.views.Button;
import org.lgna.project.ast.Statement;

import edu.wustl.lookingglass.ide.croquet.components.StatementMenuButton;
import edu.wustl.lookingglass.ide.perspectives.dinah.DinahProgramManager;
import edu.wustl.lookingglass.ide.perspectives.dinah.TimeScrubUpdateable;
import edu.wustl.lookingglass.ide.perspectives.dinah.finder.operations.ExploreStatementMenuModel;
import edu.wustl.lookingglass.ide.perspectives.dinah.finder.operations.RemixStatementMenuModel;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;

public class StatementMenuOperation extends ActionOperation implements TimeScrubUpdateable {
	public final Statement statement;
	private StatementMenuButton button;

	private DinahProgramManager programManager;

	public StatementMenuOperation( org.lgna.project.ast.Statement statement ) {
		this( statement, null );
	}

	public StatementMenuOperation( org.lgna.project.ast.Statement statement, DinahProgramManager programManager ) {
		super( ReuseGroup.REUSE_GROUP, java.util.UUID.fromString( "e5376a3e-753e-4a9c-aeb0-150f1a72a158" ) );
		this.statement = statement;
		this.programManager = programManager;
	}

	private void showStatementMenu( org.lgna.croquet.history.Step<?> step ) {
		if( edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().isInPlayAndExplorePerspective() ) {
			ExploreStatementMenuModel menuModel = new ExploreStatementMenuModel( this.statement, this.programManager );
			menuModel.getPopupPrepModel().fire( step.getTrigger() );
		} else {
			RemixStatementMenuModel menuModel = new RemixStatementMenuModel( this.statement, this.programManager );
			menuModel.getPopupPrepModel().fire( step.getTrigger() );
		}
	}

	public DinahProgramManager getProgramManager() {
		return this.programManager;
	}

	@Override
	protected void perform( org.lgna.croquet.history.CompletionStep<?> step ) {
		showStatementMenu( step );
	}

	@Override
	public Button createButton( edu.cmu.cs.dennisc.java.awt.font.TextAttribute<?>... textAttributes ) {
		if( this.button == null ) {
			this.button = new StatementMenuButton( this, statement );
		}
		return this.button;
	}

	@Override
	public void setProgramManager( DinahProgramManager programManager ) {
		this.programManager = programManager;
	}

	@Override
	public void removeProgramManager() {
		this.programManager = null;
		if( this.button != null ) {
			this.button = new StatementMenuButton( this, statement );
		}
	}

	@Override
	public void update( ProgramExecutionEvent programExecutionEvent, boolean isStartEvent ) {
		if( this.button != null ) {
			this.button.updateDrawState( programExecutionEvent.getEventNode(), isStartEvent );
		}
	}

	@Override
	public void update( ProgramStateEvent programStateEvent ) {
		if( this.button != null ) {
			this.button.updateProgramState( programStateEvent.getNextState() );
		}
	}
}
