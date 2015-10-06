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

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.lgna.project.ast.Statement;

/**
 * @author Kyle J. Harms
 */
public class PuzzleStatus {

	public static enum State {
		UNKNOWN,
		CORRECT,
		INCORRECT
	}

	public static class StatementStatus {
		private final Statement statement;
		private final java.util.List<Statement> childStatements;
		private State state;

		private StatementStatus parentStatus = null;
		private final java.util.List<StatementStatus> childStatuses = new java.util.LinkedList<>();

		protected StatementStatus( Statement statement, State state ) {
			this.statement = statement;
			this.state = state;

			this.childStatements = PuzzleStatementUtility.getChildStatements( this.statement );
		}

		protected void mergeStatementStatus( StatementStatus other ) {
			// Update parent
			if( ( this.parentStatus == null ) && ( other.parentStatus != null ) ) {
				this.parentStatus = other.parentStatus;
			}

			// Update state
			this.state = other.state;

			// Update child statuses
			for( StatementStatus otherChildStatus : other.childStatuses ) {
				StatementStatus match = null;
				for( StatementStatus thisChildStatus : this.childStatuses ) {
					if( ( thisChildStatus.statement != null ) && ( otherChildStatus.statement != null ) && ( thisChildStatus.statement == otherChildStatus.statement ) ) {
						match = thisChildStatus;
						break;
					}
				}
				if( match == null ) {
					this.childStatuses.add( otherChildStatus );
				}
			}
		}

		public Statement getStatement() {
			return this.statement;
		}

		public java.util.List<Statement> getChildStatements() {
			return this.childStatements;
		}

		public State getState() {
			return this.state;
		}

		public void setState( State state ) {
			this.state = state;
		}

		public java.util.List<StatementStatus> getChildStatuses() {
			return this.childStatuses;
		}

		public StatementStatus getParentStatus() {
			return this.parentStatus;
		}

		public void setParentStatus( StatementStatus parentStatus ) {
			this.parentStatus = parentStatus;
		}

		public int getStatusCount() {
			int count = 1; // us
			for( StatementStatus ss : this.childStatuses ) {
				count += ss.getStatusCount();
			}
			return count;
		}

		public java.util.List<StatementStatus> getMissingStatuses() {
			return this.childStatuses.parallelStream().filter( ( ss ) -> ss.getStatement() == null ).collect( Collectors.toCollection( ArrayList::new ) );
		}

		@Override
		public java.lang.String toString() {
			StringBuilder out = new StringBuilder();

			out.append( "StatementStatus: { " ).append( statement == null ? null : statement.getReprWithId() ).append( "; children: " );
			for( Statement s : this.childStatements ) {
				out.append( s.getReprWithId() ).append( ", " );
			}
			out.append( "; parent: " );
			if( this.parentStatus != null ) {
				out.append( this.parentStatus.getStatement() == null ? null : this.parentStatus.getStatement().getReprWithId() );
			}
			out.append( "; child status: " );
			for( StatementStatus ss : this.childStatuses ) {
				out.append( ss.getStatement() == null ? "" : ss.getStatement().getReprWithId() ).append( ", " );
			}
			out.append( "}" );

			return out.toString();
		}
	}

	private final org.lgna.project.ast.AbstractStatementWithBody referenceBody;
	private final org.lgna.project.ast.AbstractStatementWithBody puzzleBody;

	private final java.util.List<StatementStatus> statuses;

	public PuzzleStatus( org.lgna.project.ast.AbstractStatementWithBody referenceBody, org.lgna.project.ast.AbstractStatementWithBody puzzleBody ) {
		this.referenceBody = referenceBody;
		this.puzzleBody = puzzleBody;
		this.statuses = new java.util.LinkedList<>();

		java.util.List<Statement> puzzleStatements = new java.util.LinkedList<org.lgna.project.ast.Statement>();
		PuzzleStatementUtility.flattenStatements( puzzleStatements, this.puzzleBody );
		for( Statement statement : puzzleStatements ) {
			this.addStatementStatus( new StatementStatus( statement, State.UNKNOWN ) );
		}
	}

	private void addStatementStatus( Integer index, StatementStatus statementStatus ) {
		if( index == null ) {
			this.statuses.add( statementStatus );
		} else {
			this.statuses.add( index, statementStatus );
		}

		this.updateStatementStatus( statementStatus );
	}

	private void updateStatementStatus( StatementStatus statementStatus ) {
		// Reconnect parent status
		StatementStatus parentStatus = statementStatus.getParentStatus();
		if( parentStatus != null ) {
			if( !this.statuses.contains( parentStatus ) ) {
				java.util.List<StatementStatus> parents = this.statuses.parallelStream().filter( ( ss ) -> ss.getStatement() == parentStatus.getStatement() ).collect( Collectors.toCollection( ArrayList::new ) );
				assert parents.size() <= 1;
				if( parents.size() > 0 ) {
					statementStatus.setParentStatus( parents.get( 0 ) );
				}
			}
		}

		// Reconnect child statuses
		java.util.List<StatementStatus> childStatuses = statementStatus.getChildStatuses();
		java.util.List<StatementStatus> newChildStatuses = new java.util.LinkedList<>();
		for( StatementStatus childStatus : childStatuses ) {
			if( ( childStatus.statement != null ) && !this.statuses.contains( childStatus ) ) {
				Statement statement = childStatus.getStatement();
				StatementStatus newStatus = this.getStatementStatus( statement );
				assert newStatus != null;
				newChildStatuses.add( newStatus );
			} else {
				newChildStatuses.add( childStatus );
			}
		}
		statementStatus.getChildStatuses().clear();
		statementStatus.getChildStatuses().addAll( newChildStatuses );

		for( Statement s : statementStatus.getChildStatements() ) {
			boolean foundMatch = false;
			for( StatementStatus childStatementStatus : statementStatus.getChildStatuses() ) {
				if( childStatementStatus.getStatement() == s ) {
					foundMatch = true;
					break;
				}
			}
			if( !foundMatch ) {
				StatementStatus childMatch = null;
				for( StatementStatus ss : this.statuses ) {
					if( ss.getStatement() == s ) {
						childMatch = ss;
						break;
					}
				}
				if( childMatch != null ) {
					statementStatus.getChildStatuses().add( childMatch );
				}
			}
		}
		for( StatementStatus ss : this.statuses ) {
			if( !ss.getChildStatuses().contains( statementStatus ) && ss.getChildStatements().contains( statementStatus.getStatement() ) ) {
				ss.getChildStatuses().add( statementStatus );
				statementStatus.setParentStatus( ss );
				break;
			}
		}
	}

	private void addStatementStatus( StatementStatus statementStatus ) {
		this.addStatementStatus( null, statementStatus );
	}

	public double getRelativeScore() {
		return ( ( (double)this.getStateCount( State.CORRECT ) ) / ( (double)this.statuses.size() ) );
	}

	private int getStateCount( State state ) {
		int total = 0;
		for( StatementStatus statementStatus : this.statuses ) {
			if( statementStatus.getState() == state ) {
				total += 1;
			}
		}
		return total;
	}

	public boolean isCorrect() {
		for( StatementStatus statementStatus : this.statuses ) {
			if( statementStatus.getState() != PuzzleStatus.State.CORRECT ) {
				return false;
			}
		}
		return true;
	}

	public double getPercentCorrect() {
		int correctCount = 0;
		for( StatementStatus statementStatus : this.statuses ) {
			if( statementStatus.getState() == PuzzleStatus.State.CORRECT ) {
				correctCount = correctCount + 1;
			}
		}

		// Remove the puzzle do in order from score
		correctCount = correctCount - 1;

		return (double)correctCount / (double)( PuzzleStatementUtility.getStatementCount( this.referenceBody ) - 1 );
	}

	public StatementStatus getStatementStatus( Statement statement ) {
		StatementStatus statementStatus = null;
		int count = 0;
		for( StatementStatus status : this.statuses ) {
			if( status.getStatement() == statement ) {
				statementStatus = status;
				count++;
			}
		}
		assert count <= 1;
		return statementStatus;
	}

	private Integer getStatementStatusIndex( StatementStatus statementStatus ) {
		int index = this.statuses.indexOf( statementStatus );
		if( index < 0 ) {
			return null;
		} else {
			return index;
		}
	}

	private Integer getStatementStatusIndex( Statement statement ) {
		StatementStatus statementStatus = getStatementStatus( statement );
		return getStatementStatusIndex( statementStatus );
	}

	public void updateStatementStatus( org.lgna.project.ast.Statement statement, State state ) {
		StatementStatus statementStatus = getStatementStatus( statement );
		statementStatus.setState( state );
	}

	public void updateStatementStatus( org.lgna.project.ast.AbstractStatementWithBody statement, PuzzleStatus puzzleStatus ) {
		Integer statementIndex = getStatementStatusIndex( statement );
		assert statementIndex != null;

		for( int i = 0; i < puzzleStatus.statuses.size(); i++ ) {
			int si = i + statementIndex;

			StatementStatus thisStatementStatus;
			StatementStatus otherStatementStatus = puzzleStatus.statuses.get( i );

			// Check if we need to insert some new stuff
			if( si >= this.statuses.size() ) {
				thisStatementStatus = otherStatementStatus;
				this.addStatementStatus( si, thisStatementStatus );
			} else {
				thisStatementStatus = this.statuses.get( si );
			}

			// sanity check
			if( i == 0 ) {
				assert thisStatementStatus.getStatement() == otherStatementStatus.getStatement();
			}

			if( thisStatementStatus.getStatement() == otherStatementStatus.getStatement() ) {
				thisStatementStatus.mergeStatementStatus( otherStatementStatus );
				this.updateStatementStatus( thisStatementStatus );
			} else {
				assert otherStatementStatus.getStatement() == null;
				this.addStatementStatus( si, otherStatementStatus );
			}
		}

		// sanity check... check for duplicates
		for( StatementStatus i : this.statuses ) {
			if( i.getStatement() != null ) {
				int count = 0;
				for( StatementStatus j : this.statuses ) {
					if( i.getStatement() == j.getStatement() ) {
						count++;
					}
				}
				assert count == 1;
			}
		}
	}

	public void appendMissingStatementStatus( org.lgna.project.ast.AbstractStatementWithBody parentStatement ) {
		StatementStatus parentStatementStatus = this.getStatementStatus( parentStatement );
		assert parentStatementStatus != null;

		StatementStatus missingStatementStatus = new StatementStatus( null, State.INCORRECT );
		missingStatementStatus.setParentStatus( parentStatementStatus );

		int missingIndex = this.getStatementStatusIndex( parentStatementStatus );
		for( StatementStatus ss : parentStatementStatus.getChildStatuses() ) {
			int ssIndex = this.getStatementStatusIndex( ss );
			if( ssIndex > missingIndex ) {
				missingIndex = ssIndex;
			}
		}

		StatementStatus lastStatementStatus = this.statuses.get( missingIndex );
		missingIndex = missingIndex + lastStatementStatus.getStatusCount();
		parentStatementStatus.getChildStatuses().add( missingStatementStatus );
		this.addStatementStatus( missingIndex, missingStatementStatus );
	}

	@Override
	public java.lang.String toString() {
		StringBuilder out = new StringBuilder();

		out.append( this.referenceBody ).append( "; correct: " ).append( this.isCorrect() ).append( ", " ).append( this.getPercentCorrect() ).append( "\n" );
		for( int i = 0; i < this.statuses.size(); i++ ) {
			out.append( "  [" ).append( i ).append( "] " ).append( this.statuses.get( i ).getState() );
			out.append( " : " );
			if( this.statuses.get( i ).getStatement() != null ) {
				out.append( this.statuses.get( i ).getStatement().getReprWithId() );
			} else {
				out.append( "null;" );
			}
			out.append( "\n" );
		}

		return out.toString();
	}
}
