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

/**
 * @author Kyle J. Harms
 */
public class PuzzleComparison {

	private final org.lgna.project.ast.AbstractStatementWithBody referenceParentStatement;
	private final org.lgna.project.ast.AbstractStatementWithBody puzzleParentStatement;
	private final java.util.List<org.lgna.project.ast.Statement> referenceIncorrectStatements;
	private final java.util.List<org.lgna.project.ast.Statement> puzzleIncorrectStatements;

	private final PuzzleStatus status;

	public static PuzzleComparison puzzleMatches( org.lgna.project.ast.AbstractStatementWithBody referenceParentStatement, org.lgna.project.ast.AbstractStatementWithBody puzzleParentStatement, PuzzleStatus status ) {
		return new PuzzleComparison( referenceParentStatement, puzzleParentStatement, (org.lgna.project.ast.Statement)null, (org.lgna.project.ast.Statement)null, status );
	}

	public PuzzleComparison( org.lgna.project.ast.AbstractStatementWithBody referenceParentStatement, org.lgna.project.ast.AbstractStatementWithBody puzzleParentStatement, java.util.List<org.lgna.project.ast.Statement> referenceIncorrectStatements, java.util.List<org.lgna.project.ast.Statement> puzzleIncorrectStatements, PuzzleStatus status ) {
		assert( ( referenceParentStatement != null ) && ( puzzleParentStatement != null ) && ( status != null ) );

		this.referenceParentStatement = referenceParentStatement;
		this.puzzleParentStatement = puzzleParentStatement;
		this.referenceIncorrectStatements = referenceIncorrectStatements;
		this.puzzleIncorrectStatements = puzzleIncorrectStatements;

		this.status = status;
	}

	public PuzzleComparison( org.lgna.project.ast.AbstractStatementWithBody referenceParentStatement, org.lgna.project.ast.AbstractStatementWithBody puzzleParentStatement, org.lgna.project.ast.Statement referenceIncorrectStatement, org.lgna.project.ast.Statement puzzleIncorrectStatement, PuzzleStatus status ) {
		this( referenceParentStatement, puzzleParentStatement, new java.util.LinkedList<>(), new java.util.LinkedList<>(), status );

		if( referenceIncorrectStatement != null ) {
			this.referenceIncorrectStatements.add( referenceIncorrectStatement );
		}
		if( puzzleIncorrectStatement != null ) {
			this.puzzleIncorrectStatements.add( puzzleIncorrectStatement );
		}
	}

	public PuzzleComparison( org.lgna.project.ast.AbstractStatementWithBody referenceParentStatement, org.lgna.project.ast.AbstractStatementWithBody puzzleParentStatement, PuzzleComparison comparison, PuzzleStatus status ) {
		this( referenceParentStatement, puzzleParentStatement, comparison.getReferenceIncorrectStatements(), comparison.getPuzzleIncorrectStatements(), status );
	}

	public org.lgna.project.ast.AbstractStatementWithBody getReferenceParentStatement() {
		return this.referenceParentStatement;
	}

	public org.lgna.project.ast.AbstractStatementWithBody getPuzzleParentStatement() {
		return this.puzzleParentStatement;
	}

	public java.util.List<org.lgna.project.ast.Statement> getReferenceIncorrectStatements() {
		return this.referenceIncorrectStatements;
	}

	public java.util.List<org.lgna.project.ast.Statement> getPuzzleIncorrectStatements() {
		return this.puzzleIncorrectStatements;
	}

	public PuzzleStatus getStatus() {
		return this.status;
	}

	public double getRelativeScore() {
		return this.status.getRelativeScore();
	}

	public boolean isCorrect() {
		// TODO: Should this also check the puzzle indicator status... to see if everything is marked as correct? I'm not sure.
		return ( ( this.referenceIncorrectStatements.isEmpty() ) && ( this.puzzleIncorrectStatements.isEmpty() ) );
	}

	public boolean isIncorrect() {
		return !isCorrect();
	}

	@Override
	public java.lang.String toString() {
		StringBuilder out = new StringBuilder();
		out.append( "puzzle comparison; correct: " ).append( isCorrect() ).append( "\n" );

		out.append( "  incorrect reference statement(s): " );
		for( org.lgna.project.ast.Statement statement : this.referenceIncorrectStatements ) {
			out.append( statement.getReprWithId() ).append( ", " );
		}
		out.append( "\n" );

		out.append( "  incorrect puzzle statement(s): " );
		for( org.lgna.project.ast.Statement statement : this.puzzleIncorrectStatements ) {
			out.append( statement.getReprWithId() ).append( ", " );
		}
		out.append( "\n" );

		out.append( "  status: " ).append( this.status );

		return out.toString();
	}
}
