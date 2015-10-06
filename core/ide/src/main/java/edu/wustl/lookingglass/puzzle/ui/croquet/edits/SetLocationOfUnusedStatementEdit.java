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
package edu.wustl.lookingglass.puzzle.ui.croquet.edits;

import java.awt.Point;

import org.lgna.croquet.history.CompletionStep;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.StatementListProperty;

import edu.wustl.lookingglass.puzzle.ui.croquet.views.PuzzleStatementsBinView;

public class SetLocationOfUnusedStatementEdit extends org.lgna.croquet.edits.AbstractEdit<edu.wustl.lookingglass.puzzle.ui.croquet.SetLocationOfUnusedStatementOperation> {

	private final Statement statement;
	private final Point ptPrev;
	private final Point ptNext;
	private final StatementListProperty unusedStatementsListProperty;
	private final int indexPrev;
	private final int indexNext;
	private final PuzzleStatementsBinView view;

	public SetLocationOfUnusedStatementEdit( CompletionStep completionStep,
			Statement statement,
			Point ptPrev,
			Point ptNext,
			int indexPrev,
			int indexNext,
			StatementListProperty unusedStatementsListProperty,
			PuzzleStatementsBinView view ) {

		super( completionStep );
		this.statement = statement;
		this.ptPrev = ptPrev;

		// bump fix for out-of-bounds statement placements
		if( ptNext.x < 0 ) {
			ptNext.x = 0;
		}
		if( ptNext.y < 0 ) {
			ptNext.y = 0;
		}

		this.ptNext = ptNext;
		this.unusedStatementsListProperty = unusedStatementsListProperty;
		this.indexPrev = indexPrev;
		this.indexNext = indexNext;
		this.view = view;
	}

	@Override
	protected void doOrRedoInternal( boolean isDo ) {
		this.unusedStatementsListProperty.slide( this.indexPrev, this.indexNext );
		this.view.setStatementPaneLocation( this.statement, this.ptNext );
	}

	@Override
	protected void undoInternal() {
		this.unusedStatementsListProperty.slide( this.indexNext, this.indexPrev );
		this.view.setStatementPaneLocation( this.statement, this.ptPrev );
	}

	@Override
	protected void appendDescription( StringBuilder rv, org.lgna.croquet.edits.AbstractEdit.DescriptionStyle descriptionStyle ) {
		rv.append( "set location: " );
		rv.append( this.statement.getReprWithId() ).append( "; (" );
		rv.append( this.ptPrev.x );
		rv.append( ", " );
		rv.append( this.ptPrev.y );
		rv.append( ") -> (" );
		rv.append( this.ptNext.x );
		rv.append( ", " );
		rv.append( this.ptNext.y );
		rv.append( ")" );
	}
}
