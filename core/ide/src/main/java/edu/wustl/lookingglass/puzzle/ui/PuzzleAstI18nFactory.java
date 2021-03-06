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
package edu.wustl.lookingglass.puzzle.ui;

import org.lgna.croquet.views.SwingComponentView;
import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.UserCode;

import edu.wustl.lookingglass.puzzle.CompletionPuzzle;

/**
 * @author Kyle J. Harms
 */
public class PuzzleAstI18nFactory extends org.alice.ide.x.AbstractProjectEditorAstI18nFactory {

	private final CompletionPuzzle puzzle;
	private final boolean isMutable;

	public PuzzleAstI18nFactory( CompletionPuzzle puzzle, boolean isMutable ) {
		this.puzzle = puzzle;
		this.isMutable = isMutable;
	}

	public CompletionPuzzle getPuzzle() {
		return this.puzzle;
	}

	public boolean isClickAndClackAppropriate() {
		return this.isMutable == false;
	}

	@Override
	public boolean isStatementListPropertyMutable( org.lgna.project.ast.StatementListProperty statementListProperty ) {
		if( this.isMutable ) {
			Statement statement = null;
			if( statementListProperty.getOwner() instanceof Statement ) {
				statement = (Statement)statementListProperty.getOwner();
				if( statement instanceof BlockStatement ) {
					BlockStatement blockStatement = (BlockStatement)statement;
					statement = (Statement)blockStatement.getParent();
				}
			}
			return this.puzzle.isStatementMutable( statement );
		}
		return this.isMutable;
	}

	@Override
	public java.awt.Paint getInvalidExpressionPaint( java.awt.Paint paint, int x, int y, int width, int height ) {
		return paint;
	}

	@Override
	public boolean isKeyedArgumentListMutable( org.lgna.project.ast.ArgumentListProperty<org.lgna.project.ast.JavaKeyedArgument> argumentListProperty ) {
		return false;
	}

	@Override
	public boolean isSignatureLocked( org.lgna.project.ast.Code code ) {
		return true;
	}

	@Override
	public boolean isDraggable( org.lgna.project.ast.Statement statement ) {
		return this.puzzle.isStatementDraggable( statement );
	}

	@Override
	protected boolean isDropDownDesiredFor( org.lgna.project.ast.ExpressionProperty expressionProperty ) {
		return false;
	}

	@Override
	protected boolean isStatementContextMenuDesiredFor( org.lgna.project.ast.Statement statement ) {
		return false;
	}

	@Override
	public boolean isLocalDraggableAndMutable( org.lgna.project.ast.UserLocal local ) {
		return false;
	}

	@Override
	public SwingComponentView<?> createCodeHeader( UserCode code ) {
		return null;
	}
}
