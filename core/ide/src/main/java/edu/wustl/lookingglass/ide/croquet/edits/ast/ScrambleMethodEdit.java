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
package edu.wustl.lookingglass.ide.croquet.edits.ast;

import java.util.ArrayList;
import java.util.Stack;

import org.alice.ide.ast.draganddrop.BlockStatementIndexPair;
import org.alice.ide.croquet.edits.ast.InsertStatementEdit;
import org.lgna.croquet.edits.AbstractEdit;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.project.ast.AbstractStatementWithBody;
import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.Comment;
import org.lgna.project.ast.Node;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.StatementListProperty;
import org.lgna.project.ast.UserMethod;

import edu.cmu.cs.dennisc.java.util.logging.Logger;

/**
 * @author Caitlin Kelleher
 */
public class ScrambleMethodEdit extends AbstractEdit<org.lgna.croquet.CompletionModel> {

	private UserMethod methodToScramble;
	private Stack<StatementPositionInfo> undoStatementMovesStack = null;

	public ScrambleMethodEdit( CompletionStep completionStep, UserMethod methodToScramble ) {
		super( completionStep );
		this.methodToScramble = methodToScramble;
	}

	@Override
	protected void doOrRedoInternal( boolean isDo ) {
		scrambleMethod();
	}

	@Override
	protected void undoInternal() {
		Logger.errln( "HA!" );
	}

	@Override
	protected void appendDescription( StringBuilder rv, DescriptionStyle descriptionStyle ) {
		rv.append( "scramble: " );
		rv.append( this.methodToScramble.getName() );
	}

	private void scrambleMethod() {
		ArrayList<Statement> actionStatements = new ArrayList<>();
		ArrayList<Statement> constructs = new ArrayList<>();

		// populate list of actions and list of constructs
		this.sortStatements( methodToScramble.body.getValue(), actionStatements, constructs );

		undoStatementMovesStack = new Stack<>();

		// get the methodToScramble's statement list
		StatementListProperty methodToScrambleStatementList = null;
		for( edu.cmu.cs.dennisc.property.InstanceProperty<?> p : methodToScramble.getBodyProperty().getValue().getProperties() ) {
			if( p instanceof StatementListProperty ) {
				methodToScrambleStatementList = (StatementListProperty)p;
			}
		}

		// loop through all the statements and move them
		for( Statement statement : actionStatements ) {
			for( edu.cmu.cs.dennisc.property.InstanceProperty<?> p : statement.getParent().getProperties() ) {
				if( p instanceof StatementListProperty ) {
					// remove from parent
					StatementListProperty statements = (StatementListProperty)p;
					int index = statements.indexOf( statement );
					if( index != -1 ) {
						undoStatementMovesStack.add( new StatementPositionInfo( statement, statement.getParent(), index ) );
						Logger.errln( "statement", statement, "removed from", statement.getParent(), "index", index );
						statements.remove( index );
					}

					// add to user method
					if( methodToScrambleStatementList != null ) {
						int insertIndex = (int)Math.floor( Math.random() * methodToScrambleStatementList.getValue().size() );
						methodToScrambleStatementList.add( insertIndex, statement );
					}
				}
			}
		}

		for( Statement statement : constructs ) {
			Logger.errln( statement );
		}
		String note = "Your code has been scrambled! Unscramble the code to make your animation work correctly.";
		Comment comment = new Comment( note );
		InsertStatementEdit edit = new InsertStatementEdit( this.getCompletionStep(), new BlockStatementIndexPair( methodToScramble.getBodyProperty().getValue(), 0 ), comment );
		this.getCompletionStep().commitAndInvokeDo( edit );
	}

	private void unscrambleMethod() {
		// get the methodToScramble's statement list
		StatementListProperty methodToScrambleStatementList = null;
		for( edu.cmu.cs.dennisc.property.InstanceProperty<?> p : methodToScramble.getBodyProperty().getValue().getProperties() ) {
			if( p instanceof StatementListProperty ) {
				methodToScrambleStatementList = (StatementListProperty)p;
			}
		}

		while( !undoStatementMovesStack.isEmpty() ) {
			StatementPositionInfo statementPositionInfo = undoStatementMovesStack.firstElement();

			assert statementPositionInfo != null;

			// remove from methodToScramble
			assert statementPositionInfo.getStatement().getParent() == methodToScramble;
			int index = methodToScrambleStatementList.indexOf( statementPositionInfo.getStatement() );
			if( index != -1 ) {
				methodToScrambleStatementList.remove( index );
			}

			// add back in original location
			for( edu.cmu.cs.dennisc.property.InstanceProperty<?> p : statementPositionInfo.getParent().getProperties() ) {
				if( p instanceof StatementListProperty ) {
					// remove from parent
					StatementListProperty statements = (StatementListProperty)p;
					statements.add( statementPositionInfo.getParentIndex(), statementPositionInfo.getStatement() );
				}
			}
		}

	}

	private void sortStatements( BlockStatement blockStatement, ArrayList<Statement> statementsWithoutBody, ArrayList<Statement> statementsWithBody ) {
		//note: if statements are not currently handled
		for( Statement childStatement : blockStatement.statements.getValue() ) {
			if( childStatement instanceof AbstractStatementWithBody ) {
				AbstractStatementWithBody bodyStatement = (AbstractStatementWithBody)childStatement;
				statementsWithBody.add( childStatement );

				sortStatements( bodyStatement.body.getValue(), statementsWithoutBody, statementsWithBody );
			} else {
				statementsWithoutBody.add( childStatement );
			}
		}

	}

	class StatementPositionInfo {
		private Statement statement;
		private Node parent;
		private int parentIndex;

		public StatementPositionInfo( Statement statement, Node parent, int parentIndex ) {
			this.statement = statement;
			this.parent = parent;
			this.parentIndex = parentIndex;
		}

		public Statement getStatement() {
			return this.statement;
		}

		public Node getParent() {
			return this.parent;
		}

		public int getParentIndex() {
			return this.parentIndex;
		}
	}

}
