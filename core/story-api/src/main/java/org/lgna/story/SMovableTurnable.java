/*******************************************************************************
 * Copyright (c) 2006, 2015, Carnegie Mellon University. All rights reserved.
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
 * 3. Products derived from the software may not be called "Alice", nor may
 *    "Alice" appear in their name, without prior written permission of
 *    Carnegie Mellon University.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Carnegie Mellon University"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 * ANY AND ALL EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT
 * SHALL THE AUTHORS, COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package org.lgna.story;

import org.lgna.project.annotations.MethodTemplate;
import org.lgna.project.annotations.ValueTemplate;
import org.lgna.project.annotations.Visibility;

/**
 * @author Dennis Cosgrove
 */
public abstract class SMovableTurnable extends STurnable {
	@MethodTemplate( visibility = Visibility.TUCKED_AWAY )
	public Position getPositionRelativeToVehicle() {
		return Position.createInstance( this.getImplementation().getLocalPosition() );
	}

	@MethodTemplate( )
	public void move( MoveDirection direction,
			@ValueTemplate( detailsEnumCls = org.lgna.story.annotation.SpatialUnitDetails.class )
			Number amount,
			Move.Detail... details ) {
		org.lgna.common.LgnaIllegalArgumentException.checkArgumentIsNotNull( direction, 0 );
		org.lgna.common.LgnaIllegalArgumentException.checkArgumentIsNumber( amount, 1 );
		this.getImplementation().animateApplyTranslation(
				direction.createTranslation( amount.doubleValue() ),
				AsSeenBy.getValue( details, this ).getImplementation(),
				Duration.getValue( details ),
				AnimationStyle.getValue( details ).getInternal()
				);
	}

	private void internalMoveToward( SThing target, double amount, double duration, edu.cmu.cs.dennisc.animation.Style animationStyle ) {
		edu.cmu.cs.dennisc.math.Point3 tThis = this.getImplementation().getAbsoluteTransformation().translation;
		edu.cmu.cs.dennisc.math.Point3 tTarget = target.getImplementation().getAbsoluteTransformation().translation;
		edu.cmu.cs.dennisc.math.Vector3 v = edu.cmu.cs.dennisc.math.Vector3.createSubtraction( tTarget, tThis );
		double length = v.calculateMagnitude();
		if( length > 0 ) {
			v.multiply( amount / length );
		} else {
			v.set( 0, 0, amount );
		}
		this.getImplementation().animateApplyTranslation(
				v.x, v.y, v.z,
				org.lgna.story.implementation.AsSeenBy.SCENE,
				duration,
				animationStyle
				);
	}

	@MethodTemplate( )
	public void moveToward( SThing target,
			@ValueTemplate( detailsEnumCls = org.lgna.story.annotation.SpatialUnitDetails.class )
			Number amount,
			MoveToward.Detail... details ) {
		org.lgna.common.LgnaIllegalArgumentException.checkArgumentIsNotNull( target, 0 );
		org.lgna.common.LgnaIllegalArgumentException.checkArgumentIsNumber( amount, 1 );
		this.internalMoveToward(
				target,
				amount.doubleValue(),
				Duration.getValue( details ),
				AnimationStyle.getValue( details ).getInternal()
				);
	}

	@MethodTemplate( )
	public void moveAwayFrom( SThing target,
			@ValueTemplate( detailsEnumCls = org.lgna.story.annotation.SpatialUnitDetails.class )
			Number amount,
			MoveAwayFrom.Detail... details ) {
		org.lgna.common.LgnaIllegalArgumentException.checkArgumentIsNotNull( target, 0 );
		org.lgna.common.LgnaIllegalArgumentException.checkArgumentIsNumber( amount, 1 );
		this.internalMoveToward(
				target,
				-amount.doubleValue(),
				Duration.getValue( details ),
				AnimationStyle.getValue( details ).getInternal()
				);
	}

	@MethodTemplate( )
	public void moveTo( SThing target, MoveTo.Detail... details ) {
		org.lgna.common.LgnaIllegalArgumentException.checkArgumentIsNotNull( target, 0 );
		this.getImplementation().animatePositionOnly( target.getImplementation(), null, PathStyle.getValue( details ).isSmooth(), Duration.getValue( details ), AnimationStyle.getValue( details ).getInternal() );
	}

	@MethodTemplate( )
	public void moveAndOrientTo( SThing target, MoveAndOrientTo.Detail... details ) {
		org.lgna.common.LgnaIllegalArgumentException.checkArgumentIsNotNull( target, 0 );
		this.getImplementation().animateTransformation( target.getImplementation(), null, PathStyle.getValue( details ).isSmooth(), Duration.getValue( details ), AnimationStyle.getValue( details ).getInternal() );
	}

	@MethodTemplate( )
	public void place( SpatialRelation spatialRelation, SThing target, Place.Detail... details ) {
		org.lgna.common.LgnaIllegalArgumentException.checkArgumentIsNotNull( spatialRelation, 0 );
		org.lgna.common.LgnaIllegalArgumentException.checkArgumentIsNotNull( target, 1 );
		org.lgna.story.implementation.EntityImp targetImp = target != null ? target.getImplementation() : null;
		org.lgna.story.implementation.ReferenceFrame defaultAsSeenByImp = targetImp != null ? targetImp : org.lgna.story.implementation.AsSeenBy.SCENE;

		this.getImplementation().animatePlace(
				spatialRelation.getImp(),
				targetImp,
				AlongAxisOffset.getValue( details ),
				AsSeenBy.getImplementation( details, defaultAsSeenByImp ),
				PathStyle.getValue( details ).isSmooth(),
				Duration.getValue( details ),
				AnimationStyle.getValue( details ).getInternal()
				);
	}

	@MethodTemplate( visibility = Visibility.TUCKED_AWAY )
	public void setPositionRelativeToVehicle( Position position, SetPositionRelativeToVehicle.Detail... details ) {
		org.lgna.common.LgnaIllegalArgumentException.checkArgumentIsNotNull( position, 0 );
		org.lgna.story.implementation.EntityImp vehicle = this.getImplementation().getVehicle();
		if( vehicle != null ) {
			this.getImplementation().animatePositionOnly( vehicle, position.getInternal(), PathStyle.getValue( details ).isSmooth(), Duration.getValue( details ), AnimationStyle.getValue( details ).getInternal() );
		} else {
			edu.cmu.cs.dennisc.scenegraph.AbstractTransformable sgTransformable = this.getImplementation().getSgComposite();
			edu.cmu.cs.dennisc.math.AffineMatrix4x4 m = sgTransformable.getLocalTransformation();
			m.translation.set( position.getInternal() );
			sgTransformable.setLocalTransformation( m );
			edu.cmu.cs.dennisc.java.util.logging.Logger.severe( this );
		}
	}
}
