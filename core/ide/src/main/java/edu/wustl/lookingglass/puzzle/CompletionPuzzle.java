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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.alice.stageide.ast.BootstrapUtilties;
import org.alice.stageide.ast.StoryApiSpecificAstUtilities;
import org.lgna.project.Project;
import org.lgna.project.ast.AbstractStatementWithBody;
import org.lgna.project.ast.AccessLevel;
import org.lgna.project.ast.AstUtilities;
import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.CrawlPolicy;
import org.lgna.project.ast.DecodeIdPolicy;
import org.lgna.project.ast.DoInOrder;
import org.lgna.project.ast.ExpressionStatement;
import org.lgna.project.ast.JavaType;
import org.lgna.project.ast.MethodInvocation;
import org.lgna.project.ast.NamedUserType;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.StatementListProperty;
import org.lgna.project.ast.ThisExpression;
import org.lgna.project.ast.UserField;
import org.lgna.project.ast.UserLambda;
import org.lgna.project.ast.UserMethod;
import org.lgna.project.ast.UserParameter;

import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.cmu.cs.dennisc.pattern.IsInstanceCrawler;
import edu.cmu.cs.dennisc.property.event.AddListPropertyEvent;
import edu.cmu.cs.dennisc.property.event.ListPropertyEvent;
import edu.cmu.cs.dennisc.property.event.ListPropertyListener;
import edu.cmu.cs.dennisc.property.event.RemoveListPropertyEvent;
import edu.cmu.cs.dennisc.property.event.SimplifiedListPropertyAdapter;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.perspectives.puzzle.CompletionPuzzlePerspective;
import edu.wustl.lookingglass.puzzle.ui.croquet.CompletionPuzzleComposite;
import edu.wustl.lookingglass.study.StudyConfiguration;

/**
 * @author Kyle J. Harms
 */
public class CompletionPuzzle {

	public enum PuzzleProjectState {
		REFERENCE,
		PUZZLE
	}

	public static final boolean PUZZLE_DEBUG = Boolean.valueOf( System.getProperty( "edu.wustl.lookingglass.survey.puzzle.debug", "false" ) );

	private PuzzleProjectState puzzleState;
	private CompletionPuzzlePerspective puzzlePerspective;

	// Puzzle Project Resources
	private org.lgna.project.Project puzzleProject;
	private UserField puzzleField;
	private UserMethod puzzleMethod;
	private UserMethod referenceMethod;
	private ExpressionStatement puzzleMethodInvocation;
	private ExpressionStatement referenceMethodInvocation;

	// Reference Puzzle - puzzle ids
	private List<Statement> referenceStatements;
	private List<Statement> referenceStatementsCopy;
	private Map<UUID, Statement> referenceStatementsCopyMap;
	private List<Statement> referenceBodyStatementsCopy;
	private DoInOrder referenceDoInOrder;

	// The actual puzzle resources
	private Statement beginStatement;
	private Integer beginIndex;
	private Statement endStatement;
	private Integer endIndex;
	private List<Statement> puzzleStatements;
	private List<Statement> nonPuzzleStatements;
	private List<Statement> nonMutableStatements;
	private List<Statement> distractorStatements;
	private List<Statement> nonScrambledStatements;
	private List<Statement> staticStatements;
	private DoInOrder puzzleDoInOrder;

	// Special blocks statements to keep the puzzle working
	private BlockStatement binStatements;

	private PuzzleComparison puzzleComparison;
	private final AtomicBoolean autoEvaluatePuzzle = new AtomicBoolean( true );;
	private final ListPropertyListener<Statement> evaluatePuzzleListener = new SimplifiedListPropertyAdapter<Statement>() {
		@Override
		protected void changing( ListPropertyEvent<Statement> e ) {
		}

		@Override
		protected void changed( ListPropertyEvent<Statement> e ) {
			if( CompletionPuzzle.this.autoEvaluatePuzzle.get() ) {
				CompletionPuzzle.this.evaluatePuzzle();
				CompletionPuzzle.this.logPuzzleStatus();
			}
		}
	};
	private final ListPropertyListener<Statement> addRemoveEvaluationListener = new SimplifiedListPropertyAdapter<Statement>() {
		@Override
		protected void changing( ListPropertyEvent<Statement> e ) {
		}

		@Override
		protected void changed( ListPropertyEvent<Statement> e ) {
			if( e instanceof AddListPropertyEvent<?> ) {
				AddListPropertyEvent<Statement> addListPropertyEvent = (AddListPropertyEvent<Statement>)e;
				for( Statement statement : addListPropertyEvent.getElements() ) {
					PuzzleStatementUtility.forEachStatement( statement, ( s ) -> {
						AbstractStatementWithBody statementWithBody = PuzzleStatementUtility.asBodyStatement( s );
						if( statementWithBody != null ) {
							statementWithBody.body.getValue().statements.addListPropertyListener( CompletionPuzzle.this.addRemoveEvaluationListener );
							statementWithBody.body.getValue().statements.addListPropertyListener( CompletionPuzzle.this.evaluatePuzzleListener );
						}
					} );
				}
			} else if( e instanceof RemoveListPropertyEvent<?> ) {
				RemoveListPropertyEvent<Statement> removeListPropertyEvent = (RemoveListPropertyEvent<Statement>)e;
				for( Statement statement : removeListPropertyEvent.getElements() ) {
					PuzzleStatementUtility.forEachStatement( statement, ( s ) -> {
						AbstractStatementWithBody statementWithBody = PuzzleStatementUtility.asBodyStatement( s );
						if( statementWithBody != null ) {
							statementWithBody.body.getValue().statements.removeListPropertyListener( CompletionPuzzle.this.addRemoveEvaluationListener );
							statementWithBody.body.getValue().statements.removeListPropertyListener( CompletionPuzzle.this.evaluatePuzzleListener );
						}
					} );
				}
			}
		}
	};

	private final long DEFAULT_PUZZLE_TIME_LIMIT_MS = 1000 * 60 * 12; // 12 minutes; -1 is no time limit;
	private final long puzzleTimeLimitMs = Long.valueOf( System.getProperty( "edu.wustl.edu.lookingglass.puzzle.timeLimit", Long.toString( DEFAULT_PUZZLE_TIME_LIMIT_MS ) ) );
	private final Timer puzzleTimer = new Timer( "Puzzle Time Limit" );

	private Runnable runWhenDone = null;

	public CompletionPuzzle( Project project ) {
		this( project, StoryApiSpecificAstUtilities.getSceneFieldFromProgramType( project.getProgramType() ), StoryApiSpecificAstUtilities.getUserMain( project.getProgramType() ) );
	}

	public CompletionPuzzle( Project project, UserField field, UserMethod method ) {
		// Initialize the puzzle project
		// We create a copy to work on while the user completes the puzzle.
		this.initializePuzzleProject( project, field, method );

		// Compute the puzzle
		this.intializePuzzle();
	}

	private void initializePuzzleProject( final Project originalProject, final UserField originalField, final UserMethod originalMethod ) {
		// This should only ever be run once.
		assert puzzleProject == null;
		assert puzzleField == null;
		assert puzzleMethod == null;

		// Copy the project
		this.puzzleProject = org.lgna.project.CopyUtilities.createCopy( originalProject, DecodeIdPolicy.PRESERVE_IDS );
		assert this.puzzleProject != null;
		PuzzleProjectProperties puzzleProperties = new PuzzleProjectProperties( puzzleProject );

		// Locate the field in the copy
		IsInstanceCrawler<UserField> fieldCrawler = IsInstanceCrawler.createInstance( UserField.class );
		this.puzzleProject.getProgramType().crawl( fieldCrawler, CrawlPolicy.COMPLETE );
		for( UserField field : fieldCrawler.getList() ) {
			if( field.getId().equals( originalField.getId() ) ) {
				this.puzzleField = field;
				break;
			}
		}
		assert this.puzzleField != null;

		// Locate the method in the copy
		IsInstanceCrawler<UserMethod> methodCrawler = IsInstanceCrawler.createInstance( UserMethod.class );
		this.puzzleProject.getProgramType().crawl( methodCrawler, CrawlPolicy.COMPLETE );
		for( UserMethod method : methodCrawler.getList() ) {
			if( method.getId().equals( originalMethod.getId() ) ) {
				this.puzzleMethod = method;
				break;
			}
		}
		assert this.puzzleMethod != null;

		// If the main method and the puzzle method are the same, we need to know about it.
		// If so we need to create a new main, since that's how these puzzles work.
		final boolean puzzleMethodIsMain = ( StoryApiSpecificAstUtilities.getUserMain( originalProject.getProgramType() ) == originalMethod );
		if( puzzleMethodIsMain ) {
			String newMainName = "puzzle+main+" + UUID.randomUUID().toString();
			UserMethod method = BootstrapUtilties.createMethod( AccessLevel.PUBLIC, Void.TYPE, newMainName );
			ExpressionStatement newMainInvocation = AstUtilities.createMethodInvocationStatement( new ThisExpression(), method );

			NamedUserType sceneType = StoryApiSpecificAstUtilities.getSceneTypeFromProgramType( this.puzzleProject.getProgramType() );
			sceneType.methods.add( method );
			MethodInvocation invocation = StoryApiSpecificAstUtilities.getMethodInvocationsInvokedSceneActivationListeners( sceneType ).get( 0 );
			UserLambda lambda = invocation.getFirstAncestorAssignableTo( UserLambda.class );

			StatementListProperty statements = lambda.body.getValue().statements;
			statements.remove( 0 );
			statements.add( 0, newMainInvocation );
		}
		UserMethod userMain = StoryApiSpecificAstUtilities.getUserMain( this.puzzleProject.getProgramType() );

		// We need to cleanup the puzzle method before we can first start using it.
		// 1. remove disabled statements
		// 2. remove distractor statements

		UUID[] distractorIds = puzzleProperties.getDistractorStatementIds();

		// Remove disabled statements
		PuzzleStatementUtility.removeStatements( this.puzzleMethod.body.getValue(), ( statement ) -> {
			if( !statement.isEnabled.getValue() ) {
				// If it's a distractor, and it's disabled, we should turn it back on!
				for( UUID id : distractorIds ) {
					if( statement.getId().equals( id ) ) {
						statement.isEnabled.setValue( true );
						return false;
					}
				}

				// Not a distractor, remove it!
				return true;
			} else {
				return false;
			}
		} );

		// Collect the non-mutable statements
		UUID[] nonMutableIds = puzzleProperties.getNonMutableStatementIds();
		this.nonMutableStatements = PuzzleStatementUtility.collectStatements( this.puzzleMethod.body.getValue(), ( statement ) -> {
			for( UUID id : nonMutableIds ) {
				if( statement.getId().equals( id ) ) {
					return true;
				}
			}
			return false;
		} );

		// Collect the static statements (statements that cannot be moved)
		UUID[] staticIds = puzzleProperties.getStaticStatementIds();
		this.staticStatements = PuzzleStatementUtility.collectStatements( this.puzzleMethod.body.getValue(), ( statement ) -> {
			for( UUID id : staticIds ) {
				if( statement.getId().equals( id ) ) {
					return true;
				}
			}
			return false;
		} );

		// Collect non-scrambled statements (statements that are not placed into the bin, but are left in place)
		UUID[] nonScrambledIds = puzzleProperties.getNonScrambledStatementIds();
		this.nonScrambledStatements = PuzzleStatementUtility.collectStatements( this.puzzleMethod.body.getValue(), ( statement ) -> {
			for( UUID id : nonScrambledIds ) {
				if( statement.getId().equals( id ) ) {
					return true;
				}
			}
			return false;
		} );

		// Remove the distractor statements. Only remove after we process the other types of statements, since distractors
		// can also be those types.
		this.distractorStatements = PuzzleStatementUtility.collectStatements( this.puzzleMethod.body.getValue(), ( statement ) -> {
			for( UUID id : distractorIds ) {
				if( statement.getId().equals( id ) ) {
					return true;
				}
			}
			return false;
		} );

		// If a statement is a distractor statement, then anything nested inside of that distractor statement
		// is implicitly a distractor also.
		List<Statement> implicitDistractorStatements = new LinkedList<>();
		for( Statement statement : this.distractorStatements ) {
			if( !this.nonMutableStatements.contains( statement ) ) {
				AbstractStatementWithBody statementWithBody = PuzzleStatementUtility.asBodyStatement( statement );
				if( statementWithBody != null ) {
					PuzzleStatementUtility.extractAllStatements( implicitDistractorStatements, statementWithBody, ( s ) -> {
						return this.staticStatements.contains( s );
					} , ( s ) -> {
						return this.nonMutableStatements.contains( s );
					} , ( s ) -> {
						return !this.nonScrambledStatements.contains( s );
					} );
				}
			}
		}
		this.distractorStatements.addAll( implicitDistractorStatements );

		// Remove the distractors from the puzzle.
		PuzzleStatementUtility.removeStatements( this.puzzleMethod.body.getValue(), ( statement ) -> {
			if( this.nonScrambledStatements.contains( statement ) ) {
				return false;
			} else {
				for( UUID id : distractorIds ) {
					if( statement.getId().equals( id ) ) {
						return true;
					}
				}
				return false;
			}
		} );

		// Set the begin and end of the puzzle
		List<Statement> bodyStatements = this.puzzleMethod.body.getValue().statements.getValue();
		assert bodyStatements.size() > 0;
		UUID beginId = puzzleProperties.getBeginStatementId();
		for( Statement statement : bodyStatements ) {
			if( statement.getId().equals( beginId ) ) {
				this.beginStatement = statement;
				break;
			}
		}
		if( this.beginStatement == null ) {
			this.beginStatement = bodyStatements.get( 0 );
		}

		UUID endId = puzzleProperties.getEndStatementId();
		for( Statement statement : bodyStatements ) {
			if( statement.getId().equals( endId ) ) {
				this.endStatement = statement;
				break;
			}
		}
		if( this.endStatement == null ) {
			this.endStatement = bodyStatements.get( bodyStatements.size() - 1 );
		}

		// Do a little sanity check to make sure someone didn't create a bad puzzle.
		beginIndex = bodyStatements.indexOf( this.beginStatement );
		endIndex = bodyStatements.indexOf( this.endStatement );
		List<Statement> invalidDistractors = new LinkedList<>();
		for( int i = 0; i < bodyStatements.size(); i++ ) {
			Statement statement = bodyStatements.get( i );
			if( ( i < beginIndex ) || ( i > endIndex ) ) {
				if( this.distractorStatements.contains( statement ) ) {
					Logger.warning( "invalid distractor found. removing " + statement.getReprWithId() );
					invalidDistractors.add( statement );
				}
			}
		}
		bodyStatements.removeAll( invalidDistractors );
		this.distractorStatements.removeAll( invalidDistractors );

		// Now actually get the beginning and end of puzzle since we have done all of the pre-processing.
		beginIndex = bodyStatements.indexOf( this.beginStatement );
		endIndex = bodyStatements.indexOf( this.endStatement );
		assert beginIndex <= endIndex;

		// copy the puzzleMethod to create the correct playback
		NamedUserType root = this.puzzleProject.getProgramType();
		BlockStatement copy = AstUtilities.createCopy( this.puzzleMethod.body.getValue(), root );
		String referenceMethodName = this.puzzleMethod.name.getValue() + "+correct+" + UUID.randomUUID().toString();
		this.referenceMethod = new UserMethod( referenceMethodName, JavaType.VOID_TYPE, new UserParameter[] {}, copy );
		assert this.referenceMethod != null;
		assert this.referenceMethod.getName().equals( this.puzzleMethod.getName() ) == false;
		this.puzzleMethod.getDeclaringType().methods.add( this.referenceMethod );

		// Create an ID map to map the correct statement with the puzzle statements
		Map<Statement, Statement> puzzleToReferenceStatementMap = new HashMap<>();
		Map<Statement, Statement> referenceToPuzzleStatementMap = new HashMap<>();
		List<Statement> puzzleMethodStatements = new LinkedList<Statement>();
		PuzzleStatementUtility.extractAllStatements( puzzleMethodStatements, this.puzzleMethod.body.getValue() );

		List<Statement> referenceMethodStatements = new LinkedList<Statement>();
		PuzzleStatementUtility.extractAllStatements( referenceMethodStatements, this.referenceMethod.body.getValue() );
		assert referenceMethodStatements.size() == puzzleMethodStatements.size();
		for( int i = 0; i < referenceMethodStatements.size(); i++ ) {
			puzzleToReferenceStatementMap.put( puzzleMethodStatements.get( i ), referenceMethodStatements.get( i ) );
			referenceToPuzzleStatementMap.put( referenceMethodStatements.get( i ), puzzleMethodStatements.get( i ) );
		}

		// Are there any non-scrambled statements that are also distractors that made it into the copy?
		// If so, we need to get rid of those, because that's the wrong puzzle for the correct reference copy.
		PuzzleStatementUtility.removeStatements( this.referenceMethod.body.getValue(), ( statement ) -> {
			Statement puzzleStatement = referenceToPuzzleStatementMap.get( statement );
			if( this.distractorStatements.contains( puzzleStatement ) ) {
				return true;
			} else {
				return false;
			}
		} );

		// Store the reference statements, b/c now they won't have any distractors.
		this.referenceStatements = new ArrayList<Statement>();
		PuzzleStatementUtility.extractAllStatements( this.referenceStatements, this.referenceMethod.body.getValue() );
		this.referenceStatements = Collections.unmodifiableList( this.referenceStatements );

		// clear the main of the copy project
		userMain.body.getValue().statements.clear();

		// Setup the puzzle method in the main
		this.puzzleMethodInvocation = AstUtilities.createMethodInvocationStatement( PuzzleStatementUtility.createCallerExpression( this.puzzleField ), this.puzzleMethod );
		userMain.body.getValue().statements.add( this.puzzleMethodInvocation );

		// Setup the correct method in the main
		this.referenceMethodInvocation = AstUtilities.createMethodInvocationStatement( PuzzleStatementUtility.createCallerExpression( this.puzzleField ), this.referenceMethod );
		userMain.body.getValue().statements.add( this.referenceMethodInvocation );

		this.setPuzzleProjectState( PuzzleProjectState.REFERENCE );
	}

	private void intializePuzzle() {
		// this method should only be run once
		assert this.referenceStatementsCopy == null;
		assert this.puzzleStatements == null;

		// Before we muck with the puzzle method, create a copy of it in its pristine condition
		// We will use this to later provide the ground truth for the solution to the puzzle.
		BlockStatement referenceBodyCopy = AstUtilities.createCopy( this.puzzleMethod.body.getValue(), this.puzzleProject.getProgramType(), DecodeIdPolicy.PRESERVE_IDS );

		// Generate the actual puzzle part of the method
		this.referenceBodyStatementsCopy = Collections.unmodifiableList( referenceBodyCopy.statements.getValue().subList( this.beginIndex, this.endIndex + 1 ) );

		// Create data structures that we can use later about the correct puzzle answer
		this.referenceStatementsCopy = new ArrayList<Statement>();
		for( Statement statement : this.referenceBodyStatementsCopy ) {
			this.referenceStatementsCopy.add( statement );
			AbstractStatementWithBody statementWithBody = PuzzleStatementUtility.asBodyStatement( statement );
			if( statementWithBody != null ) {
				PuzzleStatementUtility.extractAllStatements( this.referenceStatementsCopy, statementWithBody.body.getValue() );
			}
		}
		this.referenceStatementsCopy = Collections.unmodifiableList( this.referenceStatementsCopy );

		// For easy access later, also create a hash map of the puzzle statements
		this.referenceStatementsCopyMap = new HashMap<UUID, Statement>();
		for( Statement statement : this.referenceStatementsCopy ) {
			assert( !this.referenceStatementsCopyMap.containsKey( statement.getId() ) );
			this.referenceStatementsCopyMap.put( statement.getId(), statement );
		}
		this.referenceStatementsCopyMap = Collections.unmodifiableMap( this.referenceStatementsCopyMap );

		// Now configure the puzzle method to match the computed puzzle
		this.puzzleStatements = new ArrayList<Statement>();
		List<Statement> leaveInPuzzleStatements = new LinkedList<Statement>();
		for( Statement statement : this.puzzleMethod.body.getValue().statements.getValue() ) {
			boolean isPuzzleStatement = ( this.referenceStatementsCopyMap.get( statement.getId() ) != null );
			if( isPuzzleStatement ) {
				if( this.staticStatements.contains( statement ) ) {
					leaveInPuzzleStatements.add( statement );
				} else {
					this.puzzleStatements.add( statement );
				}
				if( this.nonScrambledStatements.contains( statement ) ) {
					leaveInPuzzleStatements.add( statement );
				}
				if( !this.nonMutableStatements.contains( statement ) ) {
					AbstractStatementWithBody statementWithBody = PuzzleStatementUtility.asBodyStatement( statement );
					if( statementWithBody != null ) {
						PuzzleStatementUtility.extractAllStatements( this.puzzleStatements, statementWithBody, ( s ) -> {
							return this.staticStatements.contains( s );
						} , ( s ) -> {
							return this.nonMutableStatements.contains( s );
						} , ( s ) -> {
							return !this.nonScrambledStatements.contains( s );
						} );
					}
				}
			}
		}
		assert this.puzzleStatements.size() > 0;

		// We need to check to see if the puzzleStatements are valid
		for( Statement statement : this.puzzleStatements ) {
			assert( !this.staticStatements.contains( statement ) );
		}

		// Create a fake do in order where the puzzle will happen.
		this.puzzleDoInOrder = AstUtilities.createDoInOrder();
		this.referenceDoInOrder = (DoInOrder)AstUtilities.createCopy( this.puzzleDoInOrder, this.puzzleProject.getProgramType(), DecodeIdPolicy.PRESERVE_IDS );
		this.referenceDoInOrder.body.getValue().statements.addAll( this.referenceBodyStatementsCopy );

		// Remove any distractors from the reference do in order
		PuzzleStatementUtility.removeStatements( this.referenceDoInOrder.body.getValue(), ( statement ) -> {
			for( Statement s : this.distractorStatements ) {
				if( statement.getId().equals( s.getId() ) ) {
					return true;
				}
			}
			return false;
		} );

		// Now figure out where this fake do in order needs to go
		int puzzleStartIndex = -1;
		for( int i = 0; i < this.puzzleMethod.body.getValue().statements.getValue().size(); i++ ) {
			Statement statement = this.puzzleMethod.body.getValue().statements.getValue().get( i );
			if( this.referenceStatementsCopyMap.get( statement.getId() ) != null ) {
				puzzleStartIndex = i;
				break;
			}
		}
		assert puzzleStartIndex >= 0;
		this.puzzleMethod.body.getValue().statements.add( puzzleStartIndex, this.puzzleDoInOrder );

		// Put any statements back into the puzzle do in order
		for( Statement statement : leaveInPuzzleStatements ) {
			this.puzzleMethod.body.getValue().statements.remove( statement );
			this.puzzleDoInOrder.body.getValue().statements.add( statement );
		}

		// Remove the puzzle statements from the main
		for( Statement statement : this.puzzleStatements ) {
			if( !this.nonScrambledStatements.contains( statement ) ) {
				this.puzzleMethod.body.getValue().statements.remove( statement );
			}
		}

		// Keep track of the statements that are not part of the puzzle too
		this.nonPuzzleStatements = new ArrayList<>();
		for( Statement statement : this.puzzleMethod.body.getValue().statements.getValue() ) {
			PuzzleStatementUtility.extractAllStatements( nonPuzzleStatements, statement );
		}

		// Setup data structures for the puzzle mechanics
		PuzzleStatementUtility.forEachStatement( this.puzzleDoInOrder, ( s ) -> {
			if( PuzzleStatementUtility.isBodyStatement( s ) ) {
				AbstractStatementWithBody bodyStatement = PuzzleStatementUtility.asBodyStatement( s );
				bodyStatement.body.getValue().statements.addListPropertyListener( this.addRemoveEvaluationListener );
				bodyStatement.body.getValue().statements.addListPropertyListener( this.evaluatePuzzleListener );
			}
		} );

		this.binStatements = new BlockStatement();
		for( Statement statement : this.distractorStatements ) {
			if( !this.nonScrambledStatements.contains( statement ) ) {
				this.binStatements.statements.add( statement );
			}
		}
		for( Statement statement : this.puzzleStatements ) {
			if( !this.nonScrambledStatements.contains( statement ) ) {
				this.binStatements.statements.add( statement );
			}
		}
		Collections.shuffle( this.binStatements.statements.getValue() );
		assert this.binStatements.statements.contains( null ) == false : this.binStatements;

		// Initialize the puzzle status
		this.evaluatePuzzle();
	}

	public org.lgna.project.Project getPuzzleProject() {
		return this.puzzleProject;
	}

	public String getTitle() {
		return this.puzzleMethod.getName();
	}

	public PuzzleProjectState getPuzzleProjectState() {
		return this.puzzleState;
	}

	public void setPuzzleProjectState( PuzzleProjectState state ) {
		boolean showPuzzle = false;
		boolean showReference = false;
		switch( state ) {
		case PUZZLE:
			showPuzzle = true;
			break;
		case REFERENCE:
			showReference = true;
			break;
		}
		this.puzzleMethodInvocation.isEnabled.setValue( showPuzzle );
		this.referenceMethodInvocation.isEnabled.setValue( showReference );

		this.puzzleState = state;
	}

	public void setTerminatingNonPuzzleStatementsEnabled( boolean isEnabled ) {
		List<Statement> bodyStatements = this.puzzleMethod.body.getValue().statements.getValue();
		for( int i = bodyStatements.indexOf( this.puzzleDoInOrder ) + 1; i < bodyStatements.size(); i++ ) {
			Statement statement = bodyStatements.get( i );
			statement.isEnabled.setValue( isEnabled );
		}
	}

	public UserMethod getPuzzleMethod() {
		return this.puzzleMethod;
	}

	public List<Statement> getReferenceStatements() {
		return Collections.unmodifiableList( this.referenceStatements );
	}

	public List<Statement> getPuzzleStatements() {
		return Collections.unmodifiableList( this.puzzleStatements );
	}

	public List<Statement> getDistractorStatements() {
		return Collections.unmodifiableList( this.distractorStatements );
	}

	public List<Statement> getNonPuzzleStatements() {
		return Collections.unmodifiableList( this.nonPuzzleStatements );
	}

	public DoInOrder getReferenceDoInOrder() {
		return this.referenceDoInOrder;
	}

	public DoInOrder getPuzzleDoInOrder() {
		return this.puzzleDoInOrder;
	}

	public BlockStatement getBinBlockStatement() {
		return this.binStatements;
	}

	public StatementListProperty getBinStatementsListProperty() {
		return this.binStatements.statements;
	}

	public boolean isStatementNonMutable( Statement statement ) {
		return this.nonMutableStatements.contains( statement );
	}

	public boolean isStatementMutable( Statement statement ) {
		return !this.isStatementNonMutable( statement );
	}

	public boolean isStatementStatic( Statement statement ) {
		return this.staticStatements.contains( statement );
	}

	public boolean isStatementDraggable( Statement statement ) {
		return ( this.puzzleStatements.contains( statement ) || this.distractorStatements.contains( statement ) );
	}

	public PuzzleComparison getPuzzleComparison() {
		return this.puzzleComparison;
	}

	public PuzzleStatus getPuzzleStatus() {
		return this.puzzleComparison.getStatus();
	}

	public boolean isCorrect() {
		return this.getPuzzleComparison().isCorrect();
	}

	private PuzzleComparison evaluatePuzzle() {
		try {
			this.puzzleComparison = PuzzleStatementUtility.comparePuzzleBody( this.referenceDoInOrder, this.puzzleDoInOrder );
		} catch( Throwable t ) {
			// Right now, this code is buggy. If we don't catch these exceptions then
			// you can't edit your puzzle. So let's just catch them and log them.
			Logger.throwable( t, this );
			this.puzzleComparison = new PuzzleComparison( this.referenceDoInOrder, this.puzzleDoInOrder, this.referenceDoInOrder, this.puzzleDoInOrder, new PuzzleStatus( this.referenceDoInOrder, this.puzzleDoInOrder ) );

			// We still want to report these exceptions, so invoke later will still allow us to gather the exception
			// and not break puzzle editing.
			SwingUtilities.invokeLater( () -> {
				throw new RuntimeException( t );
			} );
		}

		return this.puzzleComparison;
	}

	public void beginPuzzle( Runnable runWhenDone ) {
		assert runWhenDone != null; // make sure you switch perspectives or whatever...
		this.runWhenDone = runWhenDone;

		StudyConfiguration.INSTANCE.getStudyLogger().log( Level.INFO, this.toString() );
		if( PUZZLE_DEBUG ) {
			System.out.println( this );
		}
		this.logPuzzleStatus();

		// Well... I don't think this should really be in this thread... but croquet...
		SwingUtilities.invokeLater( () -> {
			LookingGlassIDE.getActiveInstance().setCursor( java.awt.Cursor.WAIT_CURSOR );

			// Ugh... this should not be globally grabbed. But that's just the design of Alice/Looking Glass.
			this.puzzlePerspective = new CompletionPuzzlePerspective( this );
			org.alice.ide.IDE.getActiveInstance().getDocumentFrame().getPerspectiveState().setValueTransactionlessly( puzzlePerspective );
			if( this.puzzleTimeLimitMs >= 0 ) {
				this.puzzleTimer.schedule( new TimerTask() {
					@Override
					public void run() {
						if( !CompletionPuzzle.this.isCorrect() ) {
							CompletionPuzzle.this.getPuzzleComposite().showTimeOutPaneWhenAppropiate();
						}
					}
				}, this.puzzleTimeLimitMs );
			}
		} );
	}

	public void endPuzzle() {
		this.puzzleTimer.cancel();
		this.puzzlePerspective = null;

		SwingUtilities.invokeLater( () -> {
			LookingGlassIDE.getActiveInstance().setCursor( java.awt.Cursor.WAIT_CURSOR );

			if( this.runWhenDone != null ) {
				this.runWhenDone.run();
			}

			SwingUtilities.invokeLater( () -> {
				LookingGlassIDE.getActiveInstance().setCursor( java.awt.Cursor.DEFAULT_CURSOR );
			} );
		} );
	}

	public CompletionPuzzleComposite getPuzzleComposite() {
		return this.puzzlePerspective.getMainComposite();
	}

	public void haltPuzzleEvaluationAndWork( Runnable worker ) {
		this.autoEvaluatePuzzle.set( false );
		worker.run();
		this.autoEvaluatePuzzle.set( true );
		this.evaluatePuzzle();
		this.logPuzzleStatus();
	}

	private void logPuzzleStatus() {
		StudyConfiguration.INSTANCE.getStudyLogger().log( Level.INFO, this.puzzleComparison.toString() );
		StringBuilder builder = new StringBuilder();
		PuzzleStatementUtility.writeStatementToText( builder, this.puzzleDoInOrder, 1 );
		StudyConfiguration.INSTANCE.getStudyLogger().log( Level.INFO, "puzzle code:\n" + builder );
		if( PUZZLE_DEBUG ) {
			System.out.println( this.puzzleComparison );
		}
	}

	@Override
	public java.lang.String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "completion puzzle, method: " ).append( this.puzzleMethod.getName() );

		List<Statement> statements = this.puzzleMethod.body.getValue().statements.getValue();
		builder.append( "\n" );
		if( this.beginIndex > 0 ) {
			PuzzleStatementUtility.writeStatementsToText( builder, statements.subList( 0, this.beginIndex ), 1 );
		}

		builder.append( "\n  puzzle: (" ).append( this.referenceDoInOrder.body.getValue().statements.size() ).append( ")\n" );
		PuzzleStatementUtility.writeStatementsToText( builder, this.referenceDoInOrder.body.getValue().statements.getValue(), 2 );

		builder.append( "\n" );
		if( this.endIndex < ( statements.size() - 1 ) ) {
			PuzzleStatementUtility.writeStatementsToText( builder, statements.subList( this.endIndex + 1, statements.size() ), 1 );
		}

		builder.append( "\n  non-mutable:\n" );
		PuzzleStatementUtility.writeStatementsToText( builder, this.nonMutableStatements, 2 );

		builder.append( "\n  static:\n" );
		PuzzleStatementUtility.writeStatementsToText( builder, this.staticStatements, 2 );

		builder.append( "\n  distractors:\n" );
		PuzzleStatementUtility.writeStatementsToText( builder, this.distractorStatements, 2 );

		return builder.toString();
	}
}
