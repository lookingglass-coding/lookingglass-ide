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
package edu.wustl.lookingglass.community.api;

/**
 * @author Kyle J. Harms
 */
public final class CommunityIDs {

	public static enum BadgeTrackID {
		TODO( 1 );

		public final int id;

		private BadgeTrackID( int id ) {
			this.id = id;
		}
	}

	public static enum BadgeID {
		TODO( 1 );

		public final int id;

		private BadgeID( int id ) {
			this.id = id;
		}
	}

	public static enum BadgeRequirementID {
		TODO( 1 );

		public final int id;

		private BadgeRequirementID( int id ) {
			this.id = id;
		}
	}

	public static enum BadgePrerequisteID {
		TODO( 1 );

		public final int id;

		private BadgePrerequisteID( int id ) {
			this.id = id;
		}
	}

	public static enum SkillID {
		TODO( 1 );

		public final int id;

		private SkillID( int id ) {
			this.id = id;
		}
	}

	public static enum SkillGroupID {
		TODO( 1 );

		public final int id;

		private SkillGroupID( int id ) {
			this.id = id;
		}
	}

	public static enum SkillGroupSkillID {
		TODO( 1 );

		public final int id;

		private SkillGroupSkillID( int id ) {
			this.id = id;
		}
	}

	public static enum CodeTestID {
		SAY( 1 ),
		THINK( 25 ),
		DELAY( 50 ),
		TURN_TO_FACE( 75 ),
		WALK_TO( 100 ),
		STRAIGHTEN_OUT_JOINTS( 150 ),
		TURN_WITH_PART( 2000 ),
		ROLL_WITH_PART( 2025 ),
		DO_TOGETHER( 3000 ),
		DO_TOGHETER_WITH_PART( 4000 ),
		GET_PART( 7000 ),
		SET_DURATION( 7025 ),
		ANIMATION_STYLE( 7050 );

		public final int id;

		private CodeTestID( int id ) {
			this.id = id;
		}
	}
}
