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
package edu.wustl.lookingglass.puzzle;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.lgna.project.ast.AbstractStatementWithBody;
import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.ContentEqualsStrictness;
import org.lgna.project.ast.DoTogether;
import org.lgna.project.ast.Expression;
import org.lgna.project.ast.FieldAccess;
import org.lgna.project.ast.Node;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.ThisExpression;
import org.lgna.project.ast.UserField;

/**
 * @author Kyle J. Harms
 */
public abstract class PuzzleStatementUtility {

	private static final boolean PUZZLE_COMPARISION_DEBUG = Boolean.valueOf( System.getProperty( "edu.wustl.lookingglass.puzzle.debug", "false" ) );

	public static boolean isBodyStatement( Statement statement ) {
		AbstractStatementWithBody construct = asBodyStatement( statement );
		return ( construct != null );
	}

	public static AbstractStatementWithBody asBodyStatement( Statement statement ) {
		if( statement instanceof AbstractStatementWithBody ) {
			return (AbstractStatementWithBody)statement;
		} else {
			return null;
		}
	}

	public static boolean areStatementsEqual( Statement referenceStatement, Statement puzzleStatement ) {
		boolean equal = false;
		if( referenceStatement.getId().equals( puzzleStatement.getId() ) ) {
			equal = true;
		} else if( referenceStatement.contentEquals( puzzleStatement, ContentEqualsStrictness.DECLARATIONS_HAVE_SAME_NAME, IGNORE_BODY_FILTER ) ) {
			equal = true;
		}

		if( PUZZLE_COMPARISION_DEBUG ) {
			System.err.println( "  areStatementsEqual: " + referenceStatement.getReprWithId() + " ~ " + puzzleStatement.getReprWithId() + " = " + equal );
		}
		return equal;
	}

	private static final edu.cmu.cs.dennisc.property.PropertyFilter IGNORE_BODY_FILTER = new edu.cmu.cs.dennisc.property.PropertyFilter() {
		@Override
		public boolean isToBeIgnored( edu.cmu.cs.dennisc.property.InstanceProperty<?> thisInstanceProperty, edu.cmu.cs.dennisc.property.InstanceProperty<?> otherInstanceProperty ) {
			edu.cmu.cs.dennisc.property.InstancePropertyOwner thisInstanceOwner = thisInstanceProperty.getOwner();

			if( thisInstanceOwner instanceof AbstractStatementWithBody ) {
				AbstractStatementWithBody statementWithBody = (AbstractStatementWithBody)thisInstanceOwner;
				return statementWithBody.body == thisInstanceProperty;
			} else {
				return false;
			}
		}
	};

	public static int getStatementCount( Statement statement ) {
		int count = 0;
		if( statement != null ) {
			count = 1;
		}

		AbstractStatementWithBody construct = asBodyStatement( statement );
		if( construct != null ) {
			java.util.List<Statement> body = construct.body.getValue().statements.getValue();
			for( Statement s : body ) {
				AbstractStatementWithBody c = asBodyStatement( s );
				if( c == null ) {
					count += 1;
				} else {
					count += getStatementCount( s );
				}
			}
		}
		return count;
	}

	public static java.util.List<Statement> getChildStatements( Statement statement ) {
		java.util.List<Statement> childStatements = new java.util.LinkedList<>();
		AbstractStatementWithBody bodyStatement = asBodyStatement( statement );
		if( bodyStatement != null ) {
			for( Statement childStatement : bodyStatement.body.getValue().statements.getValue() ) {
				childStatements.add( childStatement );
			}
		}
		return childStatements;
	}

	public static void flattenStatements( java.util.List<Statement> statements, Statement statement ) {
		statements.add( statement );
		AbstractStatementWithBody bodyStatement = asBodyStatement( statement );
		if( bodyStatement != null ) {
			extractAllStatements( statements, bodyStatement.body.getValue() );
		}
	}

	public static void extractAllStatements( java.util.List<Statement> statements, Statement statement ) {
		statements.add( statement );
		AbstractStatementWithBody bodyStatement = asBodyStatement( statement );
		if( bodyStatement != null ) {
			extractAllStatements( statements, bodyStatement.body.getValue() );
		}
	}

	public static void extractAllStatements( java.util.List<Statement> statements, BlockStatement block ) {
		extractAllStatements( statements, block, null, null, ( statement ) -> {
			return false;
		} );
	}

	public static void extractAllStatements( java.util.List<Statement> statements, BlockStatement block, Predicate<Statement> ignoreStatement, Predicate<Statement> ignoreSubStatements, Predicate<Statement> extractStatement ) {
		final Iterator<Statement> each = block.statements.iterator();
		while( each.hasNext() ) {
			Statement statement = each.next();
			if( ( ignoreStatement == null ) || !ignoreStatement.test( statement ) ) {
				statements.add( statement );
				if( ( extractStatement == null ) || extractStatement.test( statement ) ) {
					each.remove();
				}
			}
			if( ( ignoreSubStatements == null ) || !ignoreSubStatements.test( statement ) ) {
				AbstractStatementWithBody statementWithBody = PuzzleStatementUtility.asBodyStatement( statement );
				if( statementWithBody != null ) {
					extractAllStatements( statements, statementWithBody.body.getValue(), ignoreStatement, ignoreSubStatements, extractStatement );
				}
			}
		}
	}

	public static void extractAllStatements( java.util.List<Statement> statements, AbstractStatementWithBody statement, Predicate<Statement> ignoreStatement, Predicate<Statement> ignoreSubStatements, Predicate<Statement> extractStatementInPlace ) {
		extractAllStatements( statements, statement.body.getValue(), ignoreStatement, ignoreSubStatements, extractStatementInPlace );
	}

	public static void forEachStatement( Statement statement, Consumer<Statement> consumer ) {
		Objects.requireNonNull( consumer );
		Objects.requireNonNull( statement );

		consumer.accept( statement );
		AbstractStatementWithBody statementWithBody = PuzzleStatementUtility.asBodyStatement( statement );
		if( statementWithBody != null ) {
			for( Statement s : statementWithBody.body.getValue().statements.getValue() ) {
				forEachStatement( s, consumer );
			}
		}
	}

	public static List<Statement> collectStatements( BlockStatement block, Predicate<Statement> filter ) {
		Objects.requireNonNull( filter );

		List<Statement> statements = new LinkedList<Statement>();
		final Iterator<Statement> each = block.statements.iterator();
		while( each.hasNext() ) {
			Statement statement = each.next();
			if( filter.test( statement ) ) {
				statements.add( statement );
			}

			AbstractStatementWithBody statementWithBody = PuzzleStatementUtility.asBodyStatement( statement );
			if( statementWithBody != null ) {
				statements.addAll( collectStatements( statementWithBody.body.getValue(), filter ) );
			}
		}
		return statements;
	}

	public static List<Statement> removeStatements( BlockStatement block, Predicate<Statement> filter ) {
		Objects.requireNonNull( filter );

		List<Statement> statements = new LinkedList<Statement>();
		final Iterator<Statement> each = block.statements.iterator();
		while( each.hasNext() ) {
			Statement statement = each.next();
			if( filter.test( statement ) ) {
				statements.add( statement );
				each.remove();
			}

			AbstractStatementWithBody statementWithBody = PuzzleStatementUtility.asBodyStatement( statement );
			if( statementWithBody != null ) {
				statements.addAll( removeStatements( statementWithBody.body.getValue(), filter ) );
			}
		}
		return statements;
	}

	public static Node findParentNode( Node node, Predicate<Node> filter ) {
		Objects.requireNonNull( node );
		Objects.requireNonNull( filter );

		Node parent = node.getParent();
		if( parent != null ) {
			if( filter.test( parent ) ) {
				return parent;
			} else {
				return findParentNode( parent, filter );
			}
		} else {
			return null;
		}
	}

	public static PuzzleComparison comparePuzzleBody( AbstractStatementWithBody referenceBody, AbstractStatementWithBody puzzleBody ) {
		PuzzleStatus status = new PuzzleStatus( referenceBody, puzzleBody );
		if( PUZZLE_COMPARISION_DEBUG ) {
			System.err.println( "  body: " + referenceBody.getReprWithId() + " ~ " + puzzleBody.getReprWithId() );
		}

		// Do these at least match? If they do just skip it.
		if( PuzzleStatementUtility.areStatementsEqual( referenceBody, puzzleBody ) ) {
			status.updateStatementStatus( puzzleBody, PuzzleStatus.State.CORRECT );

			java.util.List<Statement> referenceStatements = referenceBody.body.getValue().statements.getValue();

			// Constructs should not have empty bodies. We should never have given a puzzle that was so poorly formed.
			assert referenceStatements.size() > 0;

			// Do together needs special treatment for this to work.
			if( referenceBody instanceof DoTogether ) {
				assert puzzleBody instanceof DoTogether;

				PuzzleComparison comparison = comparePuzzleDoTogether( (DoTogether)referenceBody, (DoTogether)puzzleBody );
				status.updateStatementStatus( puzzleBody, comparison.getStatus() );
				if( !comparison.isCorrect() ) {
					return new PuzzleComparison( referenceBody, puzzleBody, comparison, status );
				}
			} else {
				java.util.List<Statement> puzzleStatements = puzzleBody.body.getValue().statements.getValue();

				// The puzzle construct is empty.
				if( puzzleStatements.size() == 0 ) {
					status.appendMissingStatementStatus( puzzleBody );
					return new PuzzleComparison( referenceBody, puzzleBody, referenceStatements.get( 0 ), puzzleBody, status );
				}

				for( int i = 0; i < referenceStatements.size(); i++ ) {
					Statement referenceStatement = referenceStatements.get( i );

					Statement puzzleStatement = null;
					if( i < puzzleStatements.size() ) {
						puzzleStatement = puzzleStatements.get( i );
					}

					if( PUZZLE_COMPARISION_DEBUG ) {
						String s = "null";
						if( puzzleStatement != null ) {
							s = puzzleStatement.getReprWithId();
						}
						System.err.println( "  statement: " + referenceStatement.getReprWithId() + " ~ " + s );
					}

					// The puzzle isn't complete. So mark this statement as wrong.
					if( puzzleStatement == null ) {
						status.appendMissingStatementStatus( puzzleBody );
						return new PuzzleComparison( referenceBody, puzzleBody, referenceStatement, puzzleBody, status );
					}

					// Is is a construct? Then we must check the contents
					if( PuzzleStatementUtility.isBodyStatement( referenceStatement ) && PuzzleStatementUtility.isBodyStatement( puzzleStatement ) ) {

						// Check the contents of the construct for correctness.
						PuzzleComparison comparison = comparePuzzleBody( (AbstractStatementWithBody)referenceStatement, (AbstractStatementWithBody)puzzleStatement );
						status.updateStatementStatus( (AbstractStatementWithBody)puzzleStatement, comparison.getStatus() );
						if( !comparison.isCorrect() ) {
							return new PuzzleComparison( referenceBody, puzzleBody, comparison, status );
						}
					} else {
						// Do these at least match? If they do just skip it.
						if( PuzzleStatementUtility.areStatementsEqual( referenceStatement, puzzleStatement ) ) {
							status.updateStatementStatus( puzzleStatement, PuzzleStatus.State.CORRECT );

							// This is the last statement in the correct answer... but the puzzle has different number of statements to go...
							if( i == ( referenceStatements.size() - 1 ) ) {
								assert referenceStatements.size() <= puzzleStatements.size();

								// There are more statements in the puzzle
								if( referenceStatements.size() < puzzleStatements.size() ) {
									Statement nextStatement = puzzleStatements.get( referenceStatements.size() );
									status.updateStatementStatus( nextStatement, PuzzleStatus.State.INCORRECT );
									return new PuzzleComparison( referenceBody, puzzleBody, referenceBody, nextStatement, status );
								}
							}
						} else {
							// It's wrong. They don't match.
							status.updateStatementStatus( puzzleStatement, PuzzleStatus.State.INCORRECT );
							return new PuzzleComparison( referenceBody, puzzleBody, referenceStatement, puzzleStatement, status );
						}
					}
				}
			}
		} else {
			// It's wrong. They don't match.
			status.updateStatementStatus( puzzleBody, PuzzleStatus.State.INCORRECT );
			return new PuzzleComparison( referenceBody, puzzleBody, referenceBody, puzzleBody, status );
		}

		return PuzzleComparison.puzzleMatches( referenceBody, puzzleBody, status );
	}

	public static PuzzleComparison comparePuzzleDoTogether( DoTogether referenceDoTogether, DoTogether puzzleDoTogether ) {
		PuzzleStatus status = new PuzzleStatus( referenceDoTogether, puzzleDoTogether );

		assert PuzzleStatementUtility.areStatementsEqual( referenceDoTogether, puzzleDoTogether );
		status.updateStatementStatus( puzzleDoTogether, PuzzleStatus.State.CORRECT );

		java.util.List<Statement> referenceStatements = referenceDoTogether.body.getValue().statements.getValue();
		java.util.List<Statement> puzzleStatements = puzzleDoTogether.body.getValue().statements.getValue();

		// Create a list of all of the statements that should exists in the do together.
		java.util.List<Statement> unAccountedForReferenceStatements = new java.util.LinkedList<>();
		for( Statement doTogetherStatement : referenceStatements ) {
			unAccountedForReferenceStatements.add( doTogetherStatement );
		}

		// Create a list of statements that do exist in the do together.
		java.util.List<Statement> unAccountedForPuzzleStatements = new java.util.LinkedList<>();
		for( Statement doTogetherStatement : puzzleStatements ) {
			unAccountedForPuzzleStatements.add( doTogetherStatement );
		}

		// Account for all of the correct statements in the Do Together.
		java.util.List<Statement> accountedForCorrectPuzzleStatements = new java.util.LinkedList<>();
		java.util.List<PuzzleComparison> accountedForCorrectPuzzleBodyScores = new java.util.LinkedList<>();
		java.util.List<PuzzleComparison> accountedForIncorrectPuzzleBodyScores = new java.util.LinkedList<>();

		// Possible puzzle body statements that might have matches... but we don't know yet.
		java.util.Map<Statement, java.util.List<PuzzleComparison>> possibleAccountedForPuzzleBodyScores = new java.util.HashMap<>();

		for( Statement puzzleStatement : unAccountedForPuzzleStatements ) {
			java.util.List<Statement> possibleCorrectBlockMatches = new java.util.LinkedList<>();

			for( final java.util.Iterator<Statement> iterator = unAccountedForReferenceStatements.iterator(); iterator.hasNext(); ) {

				Statement referenceStatement = iterator.next();
				if( PuzzleStatementUtility.areStatementsEqual( puzzleStatement, referenceStatement ) ) {

					if( PuzzleStatementUtility.isBodyStatement( puzzleStatement ) && PuzzleStatementUtility.isBodyStatement( referenceStatement ) ) {
						// We need to check the contents of the body
						possibleCorrectBlockMatches.add( referenceStatement );
					} else {
						// This is just a single statement. So since it matches remove it.
						accountedForCorrectPuzzleStatements.add( puzzleStatement );
						iterator.remove();
						break;
					}
				}
			}

			// We have more processing to do on the bodies
			// We have check all possible duplicates because we don't know which body may match the correct statements
			// because the statements can appear in any order in a Do Together.
			if( possibleCorrectBlockMatches.size() == 1 ) {
				Statement referenceStatement = possibleCorrectBlockMatches.get( 0 );

				// There can be no other match. We have found a match for this correct statement.
				unAccountedForReferenceStatements.remove( referenceStatement );

				// compute the score for this body
				PuzzleComparison score = comparePuzzleBody( (AbstractStatementWithBody)referenceStatement, (AbstractStatementWithBody)puzzleStatement );

				if( score.isCorrect() ) {
					// Found a correct body for the puzzle statement. done.
					accountedForCorrectPuzzleBodyScores.add( score );
				} else {
					// Incorrect body...
					accountedForIncorrectPuzzleBodyScores.add( score );
				}
			} else if( possibleCorrectBlockMatches.size() > 1 ) {
				// We didn't get just one possible match for this body statement. So we'll need to
				// try and compute which one is really the match...

				java.util.List<PuzzleComparison> possibleIncorrectScores = new java.util.LinkedList<>();
				for( Statement referenceStatement : possibleCorrectBlockMatches ) {
					PuzzleComparison score = comparePuzzleBody( (AbstractStatementWithBody)referenceStatement, (AbstractStatementWithBody)puzzleStatement );

					if( score.isCorrect() ) {
						// this is absolutely a match, b/c it's correct.
						unAccountedForReferenceStatements.remove( referenceStatement );

						accountedForCorrectPuzzleBodyScores.add( score );
						possibleIncorrectScores.clear();
						break;
					} else {
						// We'll need to do some more computing to see if this are matches...
						possibleIncorrectScores.add( score );
					}
				}

				if( !possibleIncorrectScores.isEmpty() ) {
					// if we have don't have a match yet...
					possibleAccountedForPuzzleBodyScores.put( puzzleStatement, possibleIncorrectScores );
				}
			}
		}

		// Tidy up our data structures... this one isn't up to date.
		// Remove the statements that we are sure we have.
		for( Statement statement : accountedForCorrectPuzzleStatements ) {
			unAccountedForPuzzleStatements.remove( statement );

			// Record this statement as correct
			status.updateStatementStatus( statement, PuzzleStatus.State.CORRECT );
		}
		for( PuzzleComparison score : accountedForCorrectPuzzleBodyScores ) {
			unAccountedForPuzzleStatements.remove( score.getPuzzleParentStatement() );

			// Record this statement as correct
			status.updateStatementStatus( score.getPuzzleParentStatement(), score.getStatus() );
		}
		for( PuzzleComparison score : accountedForIncorrectPuzzleBodyScores ) {
			unAccountedForPuzzleStatements.remove( score.getPuzzleParentStatement() );
		}

		// Remove the possible matches that did find matches.
		do {
			// Find the the highest of all the puzzleBodyStatements. We will then process that one first.
			// This will ensure we always match these incorrect bodies to best possible correct body.
			PuzzleComparison highestScore = null;

			// We need to go through the possible body matches and pick matches...
			for( final java.util.Iterator<java.util.Map.Entry<Statement, java.util.List<PuzzleComparison>>> statementIterator = possibleAccountedForPuzzleBodyScores.entrySet().iterator(); statementIterator.hasNext(); ) {
				java.util.Map.Entry<Statement, java.util.List<PuzzleComparison>> statementEntry = statementIterator.next();
				java.util.List<PuzzleComparison> scores = statementEntry.getValue();

				// If some body has already claimed a match, then we should remove it is a possible candidate.
				for( final java.util.Iterator<PuzzleComparison> scoresIterator = scores.iterator(); scoresIterator.hasNext(); ) {
					PuzzleComparison comparison = scoresIterator.next();
					Statement referenceStatement = comparison.getReferenceParentStatement();
					//					assert ( referenceStatement == puzzleBodyStatement );

					for( PuzzleComparison c : accountedForCorrectPuzzleBodyScores ) {
						if( referenceStatement == c.getReferenceParentStatement() ) {
							scoresIterator.remove();
						}
					}
					for( PuzzleComparison c : accountedForIncorrectPuzzleBodyScores ) {
						if( referenceStatement == c.getReferenceParentStatement() ) {
							scoresIterator.remove();
						}
					}
				}

				// Do we even have possible matches left now?
				if( scores.size() > 0 ) {
					// We do. Okay, now we need to compute the high scores for this.
					PuzzleComparison topScore = scores.get( 0 );
					for( PuzzleComparison score : scores ) {
						if( score.getRelativeScore() > topScore.getRelativeScore() ) {
							topScore = score;
						}
					}

					// This score is higher... let's process it first.
					if( ( highestScore == null ) || ( topScore.getRelativeScore() > highestScore.getRelativeScore() ) ) {
						highestScore = topScore;
					}
				} else {
					// There are no more matches for this one.
					statementIterator.remove();
				}
			}

			// We have found the largest score here. So let's account for it...
			if( highestScore != null ) {
				Statement puzzleStatement = highestScore.getPuzzleParentStatement();
				Statement referenceStatement = highestScore.getReferenceParentStatement();

				possibleAccountedForPuzzleBodyScores.remove( puzzleStatement );

				accountedForIncorrectPuzzleBodyScores.add( highestScore );

				unAccountedForReferenceStatements.remove( referenceStatement );
				unAccountedForPuzzleStatements.remove( puzzleStatement );
			}
		} while( !possibleAccountedForPuzzleBodyScores.isEmpty() );
		possibleAccountedForPuzzleBodyScores = null;

		// We need to report back which statements are actually wrong.
		java.util.List<Statement> incorrectReferenceStatements = new java.util.LinkedList<>();
		java.util.List<Statement> incorrectPuzzleStatements = new java.util.LinkedList<>();

		// Update the incorrect body statuses
		for( PuzzleComparison comparison : accountedForIncorrectPuzzleBodyScores ) {
			status.updateStatementStatus( (AbstractStatementWithBody)comparison.getPuzzleParentStatement(), comparison.getStatus() );

			incorrectReferenceStatements.addAll( comparison.getReferenceIncorrectStatements() );
			incorrectPuzzleStatements.addAll( comparison.getPuzzleIncorrectStatements() );
		}

		// Check to see if all of the statements in the do together belong.
		for( Statement statement : unAccountedForPuzzleStatements ) {
			status.updateStatementStatus( statement, PuzzleStatus.State.INCORRECT );

			if( !incorrectReferenceStatements.contains( referenceDoTogether ) ) {
				incorrectReferenceStatements.add( referenceDoTogether );
			}
			incorrectPuzzleStatements.add( statement );
		}

		// Check if some statements should be present, but aren't.
		for( Statement statement : unAccountedForReferenceStatements ) {
			status.appendMissingStatementStatus( puzzleDoTogether );

			incorrectReferenceStatements.add( statement );
			if( !incorrectPuzzleStatements.contains( puzzleDoTogether ) ) {
				incorrectPuzzleStatements.add( puzzleDoTogether );
			}
		}

		if( status.isCorrect() ) {
			return PuzzleComparison.puzzleMatches( referenceDoTogether, puzzleDoTogether, status );
		} else {
			return new PuzzleComparison( referenceDoTogether, puzzleDoTogether, incorrectReferenceStatements, incorrectPuzzleStatements, status );
		}
	}

	public static Expression createCallerExpression( UserField field ) {
		if( field.getValueType().isAssignableTo( org.lgna.story.SScene.class ) ) {
			return new ThisExpression();
		} else {
			return new FieldAccess( new ThisExpression(), field );
		}
	}

	public static void writeStatementToText( StringBuilder builder, Statement statement, int indent ) {
		StringBuilder indentBuilder = new StringBuilder();
		for( int i = 0; i < indent; i++ ) {
			indentBuilder.append( "  " );
		}
		builder.append( indentBuilder ).append( statement.getReprWithId() );

		AbstractStatementWithBody construct = asBodyStatement( statement );
		if( construct != null ) {
			builder.append( " {\n" );
			java.util.List<Statement> body = construct.body.getValue().statements.getValue();
			for( Statement s : body ) {
				writeStatementToText( builder, s, indent + 1 );
			}
			builder.append( indentBuilder ).append( "}\n" );
		} else {
			builder.append( "\n" );
		}
	}

	public static void writeStatementsToText( StringBuilder builder, java.util.List<Statement> statements, int indent ) {
		for( Statement statement : statements ) {
			writeStatementToText( builder, statement, indent );
		}
	}

	public static String writeStatementsToText( java.util.List<Statement> statements ) {
		StringBuilder string = new StringBuilder();
		writeStatementsToText( string, statements, 0 );
		return string.toString();
	}
}
