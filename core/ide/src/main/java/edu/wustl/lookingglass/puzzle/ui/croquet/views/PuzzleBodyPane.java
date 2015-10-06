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
package edu.wustl.lookingglass.puzzle.ui.croquet.views;

import edu.wustl.lookingglass.puzzle.CompletionPuzzle;

public class PuzzleBodyPane extends org.alice.ide.common.BodyPane {

	private final org.lgna.croquet.views.SwingComponentView<?> statementListComponent;

	private CompletionPuzzle puzzle;

	public PuzzleBodyPane( org.lgna.croquet.views.SwingComponentView<?> statementListComponent ) {
		super( statementListComponent );
		this.statementListComponent = statementListComponent;

		// The super constructor already added this. So I'll remove it to move it where I want it.
		this.getAwtComponent().remove( this.statementListComponent.getAwtComponent() );

		// <lg/> add some extra space to the top element
		this.addComponent( org.lgna.croquet.views.BoxUtilities.createVerticalSliver( 3 ), java.awt.BorderLayout.NORTH );
		this.addComponent( org.lgna.croquet.views.BoxUtilities.createHorizontalSliver( 4 ), java.awt.BorderLayout.WEST );
		this.addComponent( org.lgna.croquet.views.BoxUtilities.createHorizontalSliver( 2 ), java.awt.BorderLayout.EAST );
		//</lg>
	}

	/*package-private*/void initialize( org.alice.ide.x.AstI18nFactory factory, CompletionPuzzle puzzle ) {
		this.puzzle = puzzle;
		org.lgna.project.ast.BlockStatement body = this.puzzle.getPuzzleMethod().body.getValue();

		// Special pane for editing the puzzle.
		org.lgna.croquet.views.PageAxisPanel centerPanel = new org.lgna.croquet.views.PageAxisPanel();
		centerPanel.addComponent( org.lgna.croquet.views.BoxUtilities.createVerticalSliver( 4 ) );
		for( org.lgna.project.ast.Statement statement : body.statements ) {
			org.lgna.croquet.views.SwingComponentView<?> view;
			if( statement == this.puzzle.getPuzzleDoInOrder() ) {
				this.statementListComponent.setBorder( new PuzzleDoInOrderStatementListBorder() );
				view = new PuzzleWorkspaceView( this.statementListComponent, this.puzzle );
				view.setBorder( javax.swing.BorderFactory.createEmptyBorder( 0, 8, 0, 8 ) );
			} else {
				view = factory.createStatementPane( statement );
			}
			centerPanel.addComponent( view );
			centerPanel.addComponent( org.lgna.croquet.views.BoxUtilities.createVerticalSliver( 4 ) );
		}
		this.addComponent( centerPanel, java.awt.BorderLayout.CENTER );
	}
}
