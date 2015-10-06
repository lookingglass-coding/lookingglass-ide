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

import java.util.UUID;

import org.lgna.project.Project;
import org.lgna.project.ast.Statement;
import org.lgna.project.properties.IdArrayPropertyKey;
import org.lgna.project.properties.IdPropertyKey;

/**
 * @author Kyle J. Harms
 */
public class PuzzleProjectProperties {

	private static final IdPropertyKey BEGIN_STATEMENT_ID_KEY = new IdPropertyKey( UUID.fromString( "2d66fab0-ce96-48bf-98e7-5f3f08230e02" ), "Puzzle Begin" );
	private static final IdPropertyKey END_STATEMENT_ID_KEY = new IdPropertyKey( UUID.fromString( "4cb4421b-2529-40bb-897d-6089117f1b85" ), "Puzzle End" );
	private static final IdArrayPropertyKey NONMUTABLE_STATEMENT_IDS_KEY = new IdArrayPropertyKey( UUID.fromString( "8a37ac6b-bbc8-41de-9014-fb44999bac11" ), "Non-Mutable Statements" );
	private static final IdArrayPropertyKey DISTRACTOR_STATEMENT_IDS_KEY = new IdArrayPropertyKey( UUID.fromString( "50d83a55-5f25-482f-abcf-470c1faddadc" ), "Distractor Statements" );
	private static final IdArrayPropertyKey NON_SCRAMBLED_STATEMENT_IDS_KEY = new IdArrayPropertyKey( UUID.fromString( "e5912ab6-7c71-4094-9e26-947579994ac5" ), "Non-Scrambled Statements" );
	private static final IdArrayPropertyKey STATIC_STATEMENT_IDS_KEY = new IdArrayPropertyKey( UUID.fromString( "0c21dcea-8160-46e1-a704-281489f953a1" ), "Static Statements" );

	public static void intialize() {
		// This is so stupid. Because of global singletons, the keys above must be created.
		// So this method exists to make sure they get created.
	}

	private final Project project;
	private UUID beginStatementId;
	private UUID endStatementId;
	private UUID[] nonMutableStatementIds;
	private UUID[] distractorStatementIds;
	private UUID[] nonScrambledStatementIds;
	private UUID[] staticStatementIds;

	public PuzzleProjectProperties( Project project ) {
		this.project = project;

		this.beginStatementId = this.project.getValueFor( BEGIN_STATEMENT_ID_KEY );
		this.endStatementId = this.project.getValueFor( END_STATEMENT_ID_KEY );
		this.nonMutableStatementIds = this.project.getValueFor( NONMUTABLE_STATEMENT_IDS_KEY );
		if( this.nonMutableStatementIds == null ) {
			this.nonMutableStatementIds = new UUID[ 0 ];
		}
		this.distractorStatementIds = project.getValueFor( DISTRACTOR_STATEMENT_IDS_KEY );
		if( this.distractorStatementIds == null ) {
			this.distractorStatementIds = new UUID[ 0 ];
		}
		this.nonScrambledStatementIds = project.getValueFor( NON_SCRAMBLED_STATEMENT_IDS_KEY );
		if( this.nonScrambledStatementIds == null ) {
			this.nonScrambledStatementIds = new UUID[ 0 ];
		}
		this.staticStatementIds = project.getValueFor( STATIC_STATEMENT_IDS_KEY );
		if( this.staticStatementIds == null ) {
			this.staticStatementIds = new UUID[ 0 ];
		}
	}

	public void reset() {
		this.setBeginStatementId( null );
		this.setEndStatementId( null );
		this.setNonMutableStatementIds( new UUID[ 0 ] );
		this.setDistractorStatementIds( new UUID[ 0 ] );
		this.setNonScrambledStatementIds( new UUID[ 0 ] );
		this.setStaticStatementIds( new UUID[ 0 ] );
	}

	public UUID getBeginStatementId() {
		return this.beginStatementId;
	}

	public void setBeginStatementId( UUID id ) {
		this.beginStatementId = id;
		this.project.putValueFor( BEGIN_STATEMENT_ID_KEY, this.beginStatementId );
	}

	public boolean isBeginStatement( Statement statement ) {
		return statement.getId().equals( this.beginStatementId );
	}

	public UUID getEndStatementId() {
		return this.endStatementId;
	}

	public void setEndStatementId( UUID id ) {
		this.endStatementId = id;
		this.project.putValueFor( END_STATEMENT_ID_KEY, endStatementId );
	}

	public boolean isEndStatement( Statement statement ) {
		return statement.getId().equals( this.endStatementId );
	}

	public UUID[] getNonMutableStatementIds() {
		return this.nonMutableStatementIds;
	}

	public boolean containsNonMutableStatement( Statement statement ) {
		return containsNonMutableStatementId( statement.getId() );
	}

	public boolean containsNonMutableStatementId( UUID id ) {
		for( UUID statementId : this.nonMutableStatementIds ) {
			if( statementId.equals( id ) ) {
				return true;
			}
		}
		return false;
	}

	public void addNonMutableStatementId( UUID id ) {
		if( ( id != null ) && !this.containsNonMutableStatementId( id ) ) {
			UUID[] ids = new UUID[ this.nonMutableStatementIds.length + 1 ];
			for( int i = 0; i < this.nonMutableStatementIds.length; i++ ) {
				ids[ i ] = this.nonMutableStatementIds[ i ];
			}
			ids[ ids.length - 1 ] = id;
			this.setNonMutableStatementIds( ids );
		}
	}

	public void removeNonMutableStatementId( UUID id ) {
		if( ( id != null ) && this.containsNonMutableStatementId( id ) ) {
			UUID[] ids = new UUID[ this.nonMutableStatementIds.length - 1 ];
			int adjust = 0;
			for( int i = 0; i < this.nonMutableStatementIds.length; i++ ) {
				if( this.nonMutableStatementIds[ i ].equals( id ) ) {
					adjust = -1;
					continue;
				} else {
					ids[ i + adjust ] = this.nonMutableStatementIds[ i ];
				}
			}
			this.setNonMutableStatementIds( ids );
		}
	}

	public void setNonMutableStatementIds( UUID[] ids ) {
		this.nonMutableStatementIds = ids;
		this.project.putValueFor( NONMUTABLE_STATEMENT_IDS_KEY, this.nonMutableStatementIds );
	}

	public UUID[] getDistractorStatementIds() {
		return this.distractorStatementIds;
	}

	public boolean containsDistractorStatement( Statement statement ) {
		return containsDistractorStatementId( statement.getId() );
	}

	public boolean containsDistractorStatementId( UUID id ) {
		for( UUID statementId : this.distractorStatementIds ) {
			if( statementId.equals( id ) ) {
				return true;
			}
		}
		return false;
	}

	public void addDistractorStatementId( UUID id ) {
		if( ( id != null ) && !this.containsDistractorStatementId( id ) ) {
			UUID[] ids = new UUID[ this.distractorStatementIds.length + 1 ];
			for( int i = 0; i < this.distractorStatementIds.length; i++ ) {
				ids[ i ] = this.distractorStatementIds[ i ];
			}
			ids[ ids.length - 1 ] = id;
			this.setDistractorStatementIds( ids );
		}
	}

	public void removeDistractorStatementId( UUID id ) {
		if( ( id != null ) && this.containsDistractorStatementId( id ) ) {
			UUID[] ids = new UUID[ this.distractorStatementIds.length - 1 ];
			int adjust = 0;
			for( int i = 0; i < this.distractorStatementIds.length; i++ ) {
				if( this.distractorStatementIds[ i ].equals( id ) ) {
					adjust = -1;
					continue;
				} else {
					ids[ i + adjust ] = this.distractorStatementIds[ i ];
				}
			}
			this.setDistractorStatementIds( ids );
		}
	}

	public void setDistractorStatementIds( UUID[] ids ) {
		this.distractorStatementIds = ids;
		this.project.putValueFor( DISTRACTOR_STATEMENT_IDS_KEY, this.distractorStatementIds );
	}

	public UUID[] getNonScrambledStatementIds() {
		return this.nonScrambledStatementIds;
	}

	public boolean containsNonScrambledStatement( Statement statement ) {
		return containsNonScrambledStatementId( statement.getId() );
	}

	public boolean containsNonScrambledStatementId( UUID id ) {
		for( UUID statementId : this.nonScrambledStatementIds ) {
			if( statementId.equals( id ) ) {
				return true;
			}
		}
		return false;
	}

	public void addNonScrambledStatementId( UUID id ) {
		if( ( id != null ) && !this.containsNonScrambledStatementId( id ) ) {
			UUID[] ids = new UUID[ this.nonScrambledStatementIds.length + 1 ];
			for( int i = 0; i < this.nonScrambledStatementIds.length; i++ ) {
				ids[ i ] = this.nonScrambledStatementIds[ i ];
			}
			ids[ ids.length - 1 ] = id;
			this.setNonScrambledStatementIds( ids );
		}
	}

	public void removeNonScrambledStatementId( UUID id ) {
		if( ( id != null ) && this.containsNonScrambledStatementId( id ) ) {
			UUID[] ids = new UUID[ this.nonScrambledStatementIds.length - 1 ];
			int adjust = 0;
			for( int i = 0; i < this.nonScrambledStatementIds.length; i++ ) {
				if( this.nonScrambledStatementIds[ i ].equals( id ) ) {
					adjust = -1;
					continue;
				} else {
					ids[ i + adjust ] = this.nonScrambledStatementIds[ i ];
				}
			}
			this.setNonScrambledStatementIds( ids );
		}
	}

	public void setNonScrambledStatementIds( UUID[] ids ) {
		this.nonScrambledStatementIds = ids;
		this.project.putValueFor( NON_SCRAMBLED_STATEMENT_IDS_KEY, this.nonScrambledStatementIds );
	}

	public UUID[] getStaticStatementIds() {
		return this.staticStatementIds;
	}

	public boolean containsStaticStatement( Statement statement ) {
		return containsStaticStatementId( statement.getId() );
	}

	public boolean containsStaticStatementId( UUID id ) {
		for( UUID statementId : this.staticStatementIds ) {
			if( statementId.equals( id ) ) {
				return true;
			}
		}
		return false;
	}

	public void addStaticStatementId( UUID id ) {
		if( ( id != null ) && !this.containsStaticStatementId( id ) ) {
			UUID[] ids = new UUID[ this.staticStatementIds.length + 1 ];
			for( int i = 0; i < this.staticStatementIds.length; i++ ) {
				ids[ i ] = this.staticStatementIds[ i ];
			}
			ids[ ids.length - 1 ] = id;
			this.setStaticStatementIds( ids );
		}
	}

	public void removeStaticStatementId( UUID id ) {
		if( ( id != null ) && this.containsStaticStatementId( id ) ) {
			UUID[] ids = new UUID[ this.staticStatementIds.length - 1 ];
			int adjust = 0;
			for( int i = 0; i < this.staticStatementIds.length; i++ ) {
				if( this.staticStatementIds[ i ].equals( id ) ) {
					adjust = -1;
					continue;
				} else {
					ids[ i + adjust ] = this.staticStatementIds[ i ];
				}
			}
			this.setStaticStatementIds( ids );
		}
	}

	public void setStaticStatementIds( UUID[] ids ) {
		this.staticStatementIds = ids;
		this.project.putValueFor( STATIC_STATEMENT_IDS_KEY, this.staticStatementIds );
	}
}
