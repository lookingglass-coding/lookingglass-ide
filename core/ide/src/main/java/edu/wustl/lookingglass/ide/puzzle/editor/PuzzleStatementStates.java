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
package edu.wustl.lookingglass.ide.puzzle.editor;

import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.UserMethod;

import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.puzzle.PuzzleProjectProperties;

/**
 * @author Kyle J. Harms
 */
public class PuzzleStatementStates {

	private final Statement statement;

	private IsBeginPuzzleStatementState isBeginPuzzleStatementState;
	private IsEndPuzzleStatementState isEndPuzzleStatementState;
	private IsNonMutablePuzzleStatementState isNonMutablePuzzleStatementState;
	private IsDistractorPuzzleStatementState isDistractorPuzzleStatementState;
	private IsNonScrambledPuzzleStatementState isNonScrambledPuzzleStatementState;
	private IsStaticPuzzleStatementState isStaticPuzzleStatementState;

	public PuzzleStatementStates( Statement statement ) {
		this.statement = statement;
	}

	public void update() {
		// Go grab that global variable. I hope it's the right one... on wait... we can only have one!
		PuzzleProjectProperties properties = LookingGlassIDE.getActiveInstance().getPuzzleProjectProperties();

		boolean isBeginEnabled = true;
		boolean isEndEnabled = true;
		boolean isNonMutableEnabled = true;
		boolean isDistractorEnabled = true;
		boolean isNonScrambledEnabled = true;
		boolean isStaticEnabled = true;

		boolean isBeginStatement = properties.isBeginStatement( this.statement );
		boolean isEndStatement = properties.isEndStatement( this.statement );
		boolean isNonMutableStatement = properties.containsNonMutableStatement( this.statement );
		boolean isDistractorStatement = properties.containsDistractorStatement( this.statement );
		boolean isNonScrambledStatement = properties.containsNonScrambledStatement( this.statement );
		boolean isStaticStatement = properties.containsStaticStatement( this.statement );
		if( isBeginStatement || isEndStatement ) {
			isDistractorEnabled = false;
		}
		if( isDistractorStatement ) {
			isBeginEnabled = false;
			isEndEnabled = false;
			isStaticEnabled = false;
		}
		if( isNonScrambledStatement ) {
			isStaticEnabled = false;
		}
		if( isStaticStatement ) {
			isDistractorEnabled = false;
			isNonScrambledEnabled = false;
		}

		if( ( this.statement.getParent() != null ) && ( this.statement.getParent().getParent() instanceof UserMethod ) ) {
			UserMethod method = (UserMethod)this.statement.getParent().getParent();
			BlockStatement block = method.body.getValue();

			Integer currentIndex = block.statements.indexOf( this.statement );
			Integer beginIndex = null;
			Integer endIndex = null;
			for( int i = 0; i < block.statements.size(); i++ ) {
				if( properties.isBeginStatement( block.statements.get( i ) ) ) {
					beginIndex = i;
				}
				if( properties.isEndStatement( block.statements.get( i ) ) ) {
					endIndex = i;
				}
			}

			if( ( beginIndex != null ) && ( currentIndex < beginIndex ) ) {
				isEndEnabled = false;
			}
			if( ( endIndex != null ) && ( currentIndex > endIndex ) ) {
				isBeginEnabled = false;
			}
		}

		if( this.isBeginPuzzleStatementState != null ) {
			this.isBeginPuzzleStatementState.setEnabled( isBeginEnabled );
		}
		if( this.isEndPuzzleStatementState != null ) {
			this.isEndPuzzleStatementState.setEnabled( isEndEnabled );
		}
		if( this.isNonMutablePuzzleStatementState != null ) {
			this.isNonMutablePuzzleStatementState.setEnabled( isNonMutableEnabled );
		}
		if( this.isDistractorPuzzleStatementState != null ) {
			this.isDistractorPuzzleStatementState.setEnabled( isDistractorEnabled );
		}
		if( this.isNonScrambledPuzzleStatementState != null ) {
			this.isNonScrambledPuzzleStatementState.setEnabled( isNonScrambledEnabled );
		}
		if( this.isStaticPuzzleStatementState != null ) {
			this.isStaticPuzzleStatementState.setEnabled( isStaticEnabled );
		}
	}

	public void setIsBeginPuzzleStatementState( IsBeginPuzzleStatementState isBeginPuzzleStatementState ) {
		this.isBeginPuzzleStatementState = isBeginPuzzleStatementState;
	}

	public void setIsEndPuzzleStatementState( IsEndPuzzleStatementState isEndPuzzleStatementState ) {
		this.isEndPuzzleStatementState = isEndPuzzleStatementState;
	}

	public void setIsNonMutablePuzzleStatementState( IsNonMutablePuzzleStatementState isNonMutablePuzzleStatementState ) {
		this.isNonMutablePuzzleStatementState = isNonMutablePuzzleStatementState;
	}

	public void setIsDistractorPuzzleStatementState( IsDistractorPuzzleStatementState isDistractorPuzzleStatementState ) {
		this.isDistractorPuzzleStatementState = isDistractorPuzzleStatementState;
	}

	public void setIsNonScrambledPuzzleStatementState( IsNonScrambledPuzzleStatementState isNonScrambledPuzzleStatementState ) {
		this.isNonScrambledPuzzleStatementState = isNonScrambledPuzzleStatementState;
	}

	public void setIsStaticPuzzleStatementState( IsStaticPuzzleStatementState isStaticPuzzleStatementState ) {
		this.isStaticPuzzleStatementState = isStaticPuzzleStatementState;
	}
}
