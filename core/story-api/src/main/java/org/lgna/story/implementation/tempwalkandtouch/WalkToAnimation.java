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
package org.lgna.story.implementation.tempwalkandtouch;

import org.lgna.story.ArmSwing;
import org.lgna.story.Bounce;
import org.lgna.story.EmployeesOnly;
import org.lgna.story.SBiped;
import org.lgna.story.SModel;
import org.lgna.story.StrideLength;
import org.lgna.story.implementation.AbstractTransformableImp;
import org.lgna.story.implementation.AsSeenBy;
import org.lgna.story.implementation.BipedImp;
import org.lgna.story.implementation.ModelImp;
import org.lgna.story.implementation.StandInImp;

import edu.cmu.cs.dennisc.animation.AnimationObserver;
import edu.cmu.cs.dennisc.math.AffineMatrix4x4;
import edu.cmu.cs.dennisc.math.OrthogonalMatrix3x3;
import edu.cmu.cs.dennisc.math.Point3;

public class WalkToAnimation extends AbstractWalkAnimation {
	edu.cmu.cs.dennisc.math.AxisAlignedBox m_subjectBoundingBox = null;
	edu.cmu.cs.dennisc.math.AxisAlignedBox m_targetBoundingBox = null;

	SpatialRelation spatialRelation = SpatialRelation.IN_FRONT_OF;
	double offset = 1.0;

	double stepLength = -1.0;
	double numberOfSteps = -1.0;
	double timePerStep = -1.0;

	Point3 lastD = new Point3();
	double durationRatio = 0;

	SModel target = null;
	edu.cmu.cs.dennisc.math.polynomial.HermiteCubic m_xHermite = null;
	edu.cmu.cs.dennisc.math.polynomial.HermiteCubic m_yHermite = null;
	edu.cmu.cs.dennisc.math.polynomial.HermiteCubic m_zHermite = null;

	private edu.cmu.cs.dennisc.math.AffineMatrix4x4 m_transformationBegin;
	private edu.cmu.cs.dennisc.math.AffineMatrix4x4 m_transformationEnd;

	public WalkToAnimation( SBiped subject, SModel target, SpatialRelation spatialRelation, double offset, double stepSpeed, StrideLength strideLength, Bounce bounce, ArmSwing armSwing ) {
		super( subject, stepSpeed, strideLength, bounce, armSwing );
		this.spatialRelation = spatialRelation;
		this.target = target;

		this.offset = offset;
	}

	private void calculateCurvesWrtTarget() {
		assert ( target != null );

		BipedImp subjectImp = EmployeesOnly.getImplementation( subject );
		ModelImp targetImp = EmployeesOnly.getImplementation( target );
		//		assert( subject.hasDescendent(target) TODO: need something that checks whether or not we are asking the person to walk to one of his/her own body parts

		// get subject's current position as seen by the target's current position
		StandInImp targetStandIn = AbstractTransformableImp.acquireStandIn( targetImp );//(targetImp.getScene());
		targetStandIn.setOrientationToUpright();
		m_transformationBegin = subjectImp.getTransformation( targetStandIn );

		// find end position (also as seen by target)
		edu.cmu.cs.dennisc.math.Point3 posEnd = getPositionEnd();

		StandInImp subjectStandIn = targetStandIn.createOffsetStandIn( posEnd.x, posEnd.y, posEnd.z );
		//		subjectStandIn.setOrientationOnlyToFace(targetImp, new Point3(0,0,0));
		subjectStandIn.setOrientationOnlyToPointAt( targetImp );
		subjectStandIn.setOrientationToUpright();

		// end Transformation wrt target
		edu.cmu.cs.dennisc.math.AffineMatrix4x4 endTransformationWrtTarget = AffineMatrix4x4.createIdentity();
		endTransformationWrtTarget.orientation.setValue( subjectStandIn.getTransformation( targetStandIn ).orientation );
		endTransformationWrtTarget.translation.set( posEnd );
		m_transformationEnd = new edu.cmu.cs.dennisc.math.AffineMatrix4x4( endTransformationWrtTarget );

		edu.cmu.cs.dennisc.math.AffineMatrix4x4 targetTransformation = targetStandIn.getTransformation( AsSeenBy.SCENE );
		edu.cmu.cs.dennisc.math.AffineMatrix4x4 transformationWrtSceneBegin = AffineMatrix4x4.createMultiplication( targetTransformation, m_transformationBegin );
		edu.cmu.cs.dennisc.math.AffineMatrix4x4 transformationWrtSceneEnd = AffineMatrix4x4.createMultiplication( targetTransformation, m_transformationEnd );

		Point3 startDerivative = new Point3();
		double dx = transformationWrtSceneBegin.translation.x - transformationWrtSceneEnd.translation.x;
		double dy = transformationWrtSceneBegin.translation.y - transformationWrtSceneEnd.translation.y;
		double dz = transformationWrtSceneBegin.translation.z - transformationWrtSceneEnd.translation.z;
		double distance = Math.sqrt( ( dx * dx ) + ( dy * dy ) + ( dz * dz ) );
		double s = distance;
		if( m_xHermite == null ) {
			startDerivative.set( transformationWrtSceneBegin.orientation.createForward().x * s, transformationWrtSceneBegin.orientation.createForward().y * s, transformationWrtSceneBegin.orientation.createForward().z * s );
		} else {
			startDerivative.set( lastD );
			startDerivative.multiply( durationRatio );
		}

		//TODO: the structure of the matrix has changed - these need to be updated
		m_xHermite = new edu.cmu.cs.dennisc.math.polynomial.HermiteCubic( transformationWrtSceneBegin.translation.x, transformationWrtSceneEnd.translation.x, startDerivative.x, transformationWrtSceneEnd.orientation.createForward().x * s );
		m_yHermite = new edu.cmu.cs.dennisc.math.polynomial.HermiteCubic( transformationWrtSceneBegin.translation.y, transformationWrtSceneEnd.translation.y, startDerivative.y, transformationWrtSceneEnd.orientation.createForward().y * s );
		m_zHermite = new edu.cmu.cs.dennisc.math.polynomial.HermiteCubic( transformationWrtSceneBegin.translation.z, transformationWrtSceneEnd.translation.z, startDerivative.z, transformationWrtSceneEnd.orientation.createForward().z * s );

		getActualStepLength();

	}

	@Override
	public void prologue() {
		super.prologue();

		m_transformationBegin = null;

		calculateCurvesWrtTarget();
	}

	@Override
	protected double update( double deltaSincePrologue, double deltaSinceLastUpdate, AnimationObserver observer ) {
		BipedImp subjectImp = EmployeesOnly.getImplementation( subject );

		//		PrintUtilities.println(subject.getName(), ": ", deltaSincePrologue, " ", deltaSinceLastUpdate);
		double timeRemaining = this.getTimeRemaining( deltaSincePrologue );

		double correctDeltaSinceLastUpdate = deltaSinceLastUpdate;
		if( timeRemaining < 0 ) {
			correctDeltaSinceLastUpdate = deltaSinceLastUpdate + timeRemaining;
			timeRemaining = 0.0;
		}

		if( deltaSinceLastUpdate == 0.0 ) {
			return timeRemaining;
		}

		if( timeRemaining >= 0 ) {

			double portion = correctDeltaSinceLastUpdate / ( correctDeltaSinceLastUpdate + timeRemaining );

			if( portion <= 1.0 ) {
				double x;
				double y;
				double z;
				double dx;
				double dy;
				double dz;

				// get the appropriate position
				x = m_xHermite.evaluate( portion );
				y = m_yHermite.evaluate( portion );
				z = m_zHermite.evaluate( portion );

				//				PrintUtilities.println("\t\tX: ", m_xHermite.m_g.x, x);

				subjectImp.setPositionOnly( subjectImp.getScene(), new Point3( x, y, z ) );
				//				subject.setPositionRightNow( x, y, z, AsSeenBy.SCENE );

				// face the direction you are moving
				dx = m_xHermite.evaluateDerivative( portion );
				//				dy = m_yHermite.evaluateDerivative(portion);
				dy = 0.0;
				dz = m_zHermite.evaluateDerivative( portion );

				lastD.set( dx, dy, dz );
				durationRatio = timeRemaining / ( timeRemaining + correctDeltaSinceLastUpdate );

				if( !( ( dx == 0 ) && ( dz == 0 ) ) ) {
					edu.cmu.cs.dennisc.math.OrthogonalMatrix3x3 orient = new edu.cmu.cs.dennisc.math.OrthogonalMatrix3x3();
					orient = OrthogonalMatrix3x3.createFromForwardAndUpGuide( new edu.cmu.cs.dennisc.math.Vector3( dx, dy, dz ), new edu.cmu.cs.dennisc.math.Vector3( 0, 1, 0 ) );
					orient.normalizeColumns();
					//System.out.println(target);
					//					subject.setOrientationRightNow(orient, AsSeenBy.SCENE );

					subjectImp.setOrientationOnly( subjectImp.getScene(), orient );

				}

				timePerStep = 1.0 / stepSpeed;

				int stepNumber = (int)java.lang.Math.ceil( deltaSincePrologue * ( 1.0 / timePerStep ) ) - 1;
				if( stepNumber == -1 ) {
					stepNumber = 0;
				}
				if( stepNumber == numberOfSteps ) {
					stepNumber -= 1;
				}
				//				double portionOfStep = (deltaSinceLastUpdate - (stepNumber * timePerStep)) / timePerStep;
				double portionOfStep = ( deltaSincePrologue - ( stepNumber * timePerStep ) ) / timePerStep;

				if( portionOfStep > 1.0 ) {
					portionOfStep = 1.0;
				}

				boolean lastStep = false;
				if( stepNumber == ( numberOfSteps - 1 ) ) {
					lastStep = true;
				}

				if( ( stepNumber % 2 ) == 0 ) {
					this.stepRight( portionOfStep, lastStep );
				} else {
					this.stepLeft( portionOfStep, lastStep );
				}
			}
			//			((Model)target).setTransformationRightNow(asSeenByTrans, subject.getWorld());
			//			target.setTransformationRightNow( asSeenByTrans, org.alice.apis.moveandturn.AsSeenBy.SCENE );
			calculateCurvesWrtTarget();
		}
		return timeRemaining;
	}

	public double getTimeRemaining( double deltaSincePrologue ) {
		//		double walkTime = duration;
		//		if (Double.isNaN(walkTime)) {
		double walkTime = numberOfSteps / stepSpeed;
		//		}
		return walkTime - deltaSincePrologue;
	}

	protected edu.cmu.cs.dennisc.math.Point3 getPositionEnd() {
		BipedImp subjectImp = EmployeesOnly.getImplementation( subject );
		ModelImp targetImp = EmployeesOnly.getImplementation( target );

		if( m_subjectBoundingBox == null ) {
			m_subjectBoundingBox = subjectImp.getAxisAlignedMinimumBoundingBox( targetImp );

			if( m_subjectBoundingBox.getMaximum() == null ) {
				//org.alice.apis.moveandturn.Position pos = subject.getPosition( subject );
				//edu.cmu.cs.dennisc.linearalgebra.PointD3 p = new edu.cmu.cs.dennisc.linearalgebra.PointD3(pos.getDistanceRight(), pos.getDistanceUp(), -1.0 * pos.getDistanceForward());

				edu.cmu.cs.dennisc.math.Point3 p = subjectImp.getTransformation( subjectImp ).translation; //subjectImp.getPosition( subject );
				m_subjectBoundingBox = new edu.cmu.cs.dennisc.math.AxisAlignedBox( p, p );
			}
		}
		if( m_targetBoundingBox == null ) {
			m_targetBoundingBox = targetImp.getAxisAlignedMinimumBoundingBox();

			if( m_targetBoundingBox.getMaximum() == null ) {
				//org.alice.apis.moveandturn.Position pos = subject.getPosition( subject );
				//edu.cmu.cs.dennisc.linearalgebra.PointD3 p = new edu.cmu.cs.dennisc.linearalgebra.PointD3(pos.getDistanceRight(), pos.getDistanceUp(), -1.0 * pos.getDistanceForward());
				edu.cmu.cs.dennisc.math.Point3 p = subjectImp.getTransformation( subjectImp ).translation; //subjectImp.getPosition( subject );
				m_targetBoundingBox = new edu.cmu.cs.dennisc.math.AxisAlignedBox( p, p );
			}
		}
		edu.cmu.cs.dennisc.math.Point3 v = spatialRelation.getPlaceVector( offset, subjectImp.getAxisAlignedMinimumBoundingBox(), m_targetBoundingBox );
		return v;
	}

	protected double getValueAtTime( double t ) {
		double ft = m_xHermite.evaluateDerivative( t );
		double ht = m_zHermite.evaluateDerivative( t );

		return java.lang.Math.sqrt( ( ft * ft ) + ( ht * ht ) );
	}

	protected double getPathLength() {
		double x1s = getValueAtTime( 0.0 ) + getValueAtTime( 1.0 );

		double h = 0.1;

		double startT = h;
		double x4s = 0.0;

		while( startT < 1.0 ) {
			x4s += getValueAtTime( startT );
			startT += 2 * h;
		}

		startT = 2 * h;
		double x2s = 0.0;

		while( startT < 1.0 ) {
			x2s += getValueAtTime( startT );
			startT += 2 * h;
		}

		//System.out.println("distance between points: " + java.lang.Math.sqrt((m_transformationEnd.right.w - m_transformationBegin.right.w) * (m_transformationEnd.right.w - m_transformationBegin.right.w) + (m_transformationEnd.backward.w - m_transformationBegin.backward.w) * (m_transformationEnd.backward.w - m_transformationBegin.backward.w)) );
		return ( x1s + ( 4 * x4s ) + ( 2 * x2s ) ) * ( h / 3.0 );
	}

	protected double getActualStepLength() {

		double distanceToMove = getPathLength();

		if( stepLength == -1 ) {
			stepLength = this.getStepLength();
			if( stepLength == 0.0 ) {
				stepLength = 1.0;
			}
		}

		if( numberOfSteps == -1 ) {
			numberOfSteps = java.lang.Math.round( distanceToMove / stepLength );
		}

		return distanceToMove / numberOfSteps;

	}
}
