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
import org.lgna.story.Move;
import org.lgna.story.MoveDirection;
import org.lgna.story.SBiped;
import org.lgna.story.StrideLength;
import org.lgna.story.implementation.BipedImp;
import org.lgna.story.implementation.JointImp;
import org.lgna.story.implementation.JointedModelImp;
import org.lgna.story.implementation.StandInImp;

import edu.cmu.cs.dennisc.math.OrthogonalMatrix3x3;
import edu.cmu.cs.dennisc.math.Point3;
import edu.cmu.cs.dennisc.math.UnitQuaternion;

/**
 * @author Caitlin Kelleher
 *
 *         TODO: tighten this all up. plenty o room for that.
 */
public abstract class AbstractWalkAnimation extends edu.cmu.cs.dennisc.animation.AbstractAnimation {
	protected final SBiped subject;
	protected final double stepSpeed;
	protected final StrideLength strideLength;
	protected final Bounce bounce;
	protected final ArmSwing armSwing;

	protected JointImp rightUpper = null;
	protected JointImp rightLower = null;
	protected JointImp rightFoot = null;

	protected JointImp leftUpper = null;
	protected JointImp leftLower = null;
	protected JointImp leftFoot = null;

	protected JointImp rightUpperArm = null;
	protected JointImp rightLowerArm = null;

	protected JointImp leftUpperArm = null;
	protected JointImp leftLowerArm = null;

	protected static final double normalContactAngle = .46;
	protected static final double normalBackRecoilAngle = 1.3;
	protected static final double normalFrontRecoilAngle = 0.5;

	protected double upperArmAngle = 0.3;
	protected double lowerArmAngle = 0.05;

	protected double portionContact = 1.0 / 3.0;
	protected double portionRecoil = 1.0 / 6.0;
	protected double portionPassing = 1.0 / 3.0;
	protected double portionHighPoint = 1.0 / 6.0;

	protected double contactAngle = 0.2450;

	protected double recoilBackLowerAngle = 0.6;
	protected double recoilFrontUpperAngle = 0.3;

	protected double passingFrontUpperAngle = 0;
	protected double passingFrontLowerAngle = 0;
	protected double passingFrontFootAngle = 0;

	protected double passingBackLowerAngle = 0.2;

	protected double highPointFrontUpperAngle = 0.2;
	protected double highPointBackUpperAngle = 0.7;
	protected double highPointBackLowerAngle = 0;

	protected double heightFromGround = 0.0;
	protected double initialBoundingBoxHeight = 0.0;

	protected boolean firstTimeContact = true;
	protected boolean firstTimeRecoil = true;
	protected boolean firstTimePassing = true;
	protected boolean firstTimeHighPoint = true;

	// leg Lengths
	protected double totalLength = 0.0;
	protected double upperLength = 0.0;
	protected double lowerLength = 0.0;
	protected double footLength = 0.0;
	protected double footHorizLength = 0.0;

	// leg initial orient

	protected OrthogonalMatrix3x3 rightUpperInitialOrient = null;
	protected OrthogonalMatrix3x3 rightLowerInitialOrient = null;
	protected OrthogonalMatrix3x3 rightFootInitialOrient = null;

	protected OrthogonalMatrix3x3 leftUpperInitialOrient = null;
	protected OrthogonalMatrix3x3 leftLowerInitialOrient = null;
	protected OrthogonalMatrix3x3 leftFootInitialOrient = null;

	// arm initial orient
	protected OrthogonalMatrix3x3 rightUpperArmInitialOrient = null;
	protected OrthogonalMatrix3x3 rightLowerArmInitialOrient = null;

	protected OrthogonalMatrix3x3 leftUpperArmInitialOrient = null;
	protected OrthogonalMatrix3x3 leftLowerArmInitialOrient = null;

	protected Point3 initialPos = null;

	//	protected OrthogonalMatrix3x3 defaultOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	// leg contact quaternions
	protected OrthogonalMatrix3x3 frontUpperContactOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 frontLowerContactOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 frontFootContactOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected OrthogonalMatrix3x3 backUpperContactOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 backLowerContactOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 backFootContactOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected Point3 contactPos = null;
	protected double distanceToMoveContact = 0.0;

	// leg recoil quaternions
	protected OrthogonalMatrix3x3 frontUpperRecoilOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 frontLowerRecoilOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 frontFootRecoilOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected OrthogonalMatrix3x3 backUpperRecoilOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 backLowerRecoilOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 backFootRecoilOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected Point3 recoilPos = null;
	protected double distanceToMoveRecoil = 0.0;

	// leg passing stuff
	protected OrthogonalMatrix3x3 frontUpperPassingOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 frontLowerPassingOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 frontFootPassingOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected OrthogonalMatrix3x3 backUpperPassingOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 backLowerPassingOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 backFootPassingOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected Point3 passingPos = null;
	protected double distanceToMovePassing = 0.0;

	// leg high point stuff
	protected OrthogonalMatrix3x3 frontUpperHighPointOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 frontLowerHighPointOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 frontFootHighPointOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected OrthogonalMatrix3x3 backUpperHighPointOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 backLowerHighPointOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 backFootHighPointOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected Point3 highPointPos = null;
	protected double distanceToMoveHighPoint = 0.0;

	// arm orients
	protected OrthogonalMatrix3x3 frontRightUpperArmOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 frontRightLowerArmOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected OrthogonalMatrix3x3 frontLeftUpperArmOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 frontLeftLowerArmOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected OrthogonalMatrix3x3 backRightUpperArmOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 backRightLowerArmOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected OrthogonalMatrix3x3 backLeftUpperArmOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 backLeftLowerArmOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	// current orientations
	protected OrthogonalMatrix3x3 rightUpperCurrentOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 rightLowerCurrentOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 rightFootCurrentOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected OrthogonalMatrix3x3 leftUpperCurrentOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 leftLowerCurrentOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 leftFootCurrentOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected OrthogonalMatrix3x3 rightUpperArmCurrentOrient = null; //OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 rightLowerArmCurrentOrient = null; //OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	protected OrthogonalMatrix3x3 leftUpperArmCurrentOrient = null; //OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
	protected OrthogonalMatrix3x3 leftLowerArmCurrentOrient = null; //OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();

	public AbstractWalkAnimation( SBiped subject, double stepSpeed, StrideLength strideLength, Bounce bounce, ArmSwing armSwing ) {
		this.subject = subject;
		this.stepSpeed = stepSpeed;
		this.strideLength = strideLength;
		this.bounce = bounce;
		this.armSwing = armSwing;
	}

	@Override
	public void prologue() {

		rightUpper = EmployeesOnly.getImplementation( subject.getRightHip() );
		rightLower = EmployeesOnly.getImplementation( subject.getRightKnee() );
		rightFoot = EmployeesOnly.getImplementation( subject.getRightAnkle() );

		leftUpper = EmployeesOnly.getImplementation( subject.getLeftHip() );
		leftLower = EmployeesOnly.getImplementation( subject.getLeftKnee() );
		leftFoot = EmployeesOnly.getImplementation( subject.getLeftAnkle() );

		rightUpperArm = EmployeesOnly.getImplementation( subject.getRightShoulder() );
		rightLowerArm = EmployeesOnly.getImplementation( subject.getRightElbow() );

		leftUpperArm = EmployeesOnly.getImplementation( subject.getLeftShoulder() );
		leftLowerArm = EmployeesOnly.getImplementation( subject.getLeftElbow() );

		resetData();

		recoilFrontUpperAngle = normalFrontRecoilAngle;

		if( armSwing == ArmSwing.HUGE ) {
			upperArmAngle = 0.8;
			lowerArmAngle = 1.2;
		} else if( armSwing == ArmSwing.BIG ) {
			upperArmAngle = 0.675;
			lowerArmAngle = 0.925;
		} else if( armSwing == ArmSwing.NORMAL ) {
			upperArmAngle = 0.55;
			lowerArmAngle = 0.65;
		} else if( armSwing == ArmSwing.LITTLE ) {
			upperArmAngle = 0.425;
			lowerArmAngle = 0.375;
		} else if( armSwing == ArmSwing.TINY ) {
			upperArmAngle = 0.3;
			lowerArmAngle = 0.1;
		} else if( armSwing == ArmSwing.NONE ) {
			upperArmAngle = Double.NaN;
			lowerArmAngle = Double.NaN;
		}

		if( bounce == Bounce.HUGE ) {
			recoilFrontUpperAngle = 0.5;
			recoilBackLowerAngle = 2.0;
		} else if( bounce == Bounce.BIG ) {
			recoilFrontUpperAngle = 0.37;
			recoilBackLowerAngle = 1.625;
		} else if( bounce == Bounce.NORMAL ) {
			recoilFrontUpperAngle = 0.125;
			recoilBackLowerAngle = 1.25;
		} else if( bounce == Bounce.LITTLE ) {
			recoilFrontUpperAngle = 0.12;
			recoilBackLowerAngle = 0.875;
		} else if( bounce == Bounce.TINY ) {
			recoilFrontUpperAngle = 0.0;
			recoilBackLowerAngle = 0.5;
		}

		if( strideLength == StrideLength.HUGE ) {
			contactAngle = normalContactAngle * 1.5;
		} else if( strideLength == StrideLength.BIG ) {
			contactAngle = normalContactAngle * 1.25;
		} else if( strideLength == StrideLength.NORMAL ) {
			contactAngle = normalContactAngle;
		} else if( strideLength == StrideLength.LITTLE ) {
			contactAngle = normalContactAngle * 0.75;
		} else if( strideLength == StrideLength.TINY ) {
			contactAngle = normalContactAngle * 0.5;
		}

		setLegLengths();
		setInitialOrientations();
		setContactData();
		setRecoilData();
		setPassingData();
		setHighPointData();
		setArmData();

		this.getCurrentOrientations();
	}

	// in the step methods, portion is the portion for the current step, not the
	// animation
	// as a whole.

	public void stepRight( double portion, boolean lastStep ) {
		step( rightUpper, portion, lastStep );
	}

	public void stepLeft( double portion, boolean lastStep ) {
		step( leftUpper, portion, lastStep );
	}

	private boolean isArmSwingDesired() {
		return Double.isNaN( this.upperArmAngle ) == false;
	}

	protected void step( JointImp leg, double portion, boolean lastStep ) {
		// move arms...
		// System.out.println("update: " + portion);
		adjustHeight();

		if( this.isArmSwingDesired() ) {
			updateArms( leg, portion, lastStep );
		}

		if( portion < portionContact ) {
			if( firstTimeContact ) {
				firstTimeContact = false;
				firstTimeHighPoint = true;
				getCurrentOrientations();
			}
			portion = portion / portionContact;
			updateContact( leg, portion );
		} else if( portion < ( portionContact + portionRecoil ) ) {
			if( firstTimeRecoil ) {
				firstTimeRecoil = false;
				firstTimeContact = true;
				getCurrentOrientations();
			}
			portion = ( portion - portionContact ) / portionRecoil;
			updateRecoil( leg, portion );

		} else if( portion < ( portionContact + portionRecoil + portionPassing ) ) {
			if( firstTimePassing ) {
				firstTimePassing = false;
				firstTimeRecoil = true;
				getCurrentOrientations();
			}
			portion = ( portion - portionContact - portionRecoil ) / portionPassing;
			updatePassing( leg, portion );
		} else {
			if( firstTimeHighPoint ) {
				firstTimeHighPoint = false;
				firstTimePassing = true;
				getCurrentOrientations();
			}
			portion = ( portion - portionContact - portionRecoil - portionPassing ) / portionHighPoint;
			updateHighPoint( leg, portion, lastStep );
		}

		adjustHeight();

	}

	protected void adjustHeight() {
		JointedModelImp subjectImp = EmployeesOnly.getImplementation( subject );
		double distanceAboveGround = subjectImp.getAxisAlignedMinimumBoundingBox( org.lgna.story.implementation.AsSeenBy.SCENE ).getCenterOfBottomFace().y;

		// subtracting the initial heightFromGround allows characters to walk on things other than the ground.
		subject.move( MoveDirection.DOWN, distanceAboveGround - heightFromGround, Move.duration( 0 ), Move.asSeenBy( subjectImp.getScene().getAbstraction() ) );
	}

	public void getCurrentOrientations() {
		if( rightUpper != null ) {
			rightUpperCurrentOrient = rightUpper.getLocalOrientation();
		}
		//			rightUpperCurrentOrient = rightUpper.getOrientation( org.lgna.story.implementation.AsSeenBy.PARENT );
		if( rightLower != null ) {
			rightLowerCurrentOrient = rightLower.getLocalOrientation();
		}
		if( rightFoot != null ) {
			rightFootCurrentOrient = rightFoot.getLocalOrientation();
		}

		if( leftUpper != null ) {
			leftUpperCurrentOrient = leftUpper.getLocalOrientation();
		}
		if( leftLower != null ) {
			leftLowerCurrentOrient = leftLower.getLocalOrientation();
		}
		if( leftFoot != null ) {
			leftFootCurrentOrient = leftFoot.getLocalOrientation();
		}

		if( rightUpperArm != null ) {
			rightUpperArmCurrentOrient = rightUpperArm.getLocalOrientation();
		}

		if( rightLowerArm != null ) {
			rightLowerArmCurrentOrient = rightLowerArm.getLocalOrientation();
		}

		if( leftUpperArm != null ) {
			leftUpperArmCurrentOrient = leftUpperArm.getLocalOrientation();
		}
		if( leftLowerArm != null ) {
			leftLowerArmCurrentOrient = leftLowerArm.getLocalOrientation();
		}

	}

	public void resetData() {
		contactAngle = 0.245;

		recoilBackLowerAngle = 0.2;
		recoilFrontUpperAngle = 0.4;

		passingFrontUpperAngle = 0;
		passingFrontLowerAngle = 0;
		passingFrontFootAngle = 0;

		passingBackLowerAngle = 0.2;

		highPointFrontUpperAngle = 0.2;
		highPointBackUpperAngle = 0.7;
		highPointBackLowerAngle = 0;

		frontUpperContactOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		frontLowerContactOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		frontFootContactOrient = rightFoot.getOriginalOrientation().createOrthogonalMatrix3x3(); //new MatrixD3x3();

		backUpperContactOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		backLowerContactOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		backFootContactOrient = rightFoot.getOriginalOrientation().createOrthogonalMatrix3x3(); //new MatrixD3x3();

		contactPos = null;
		distanceToMoveContact = 0.0;

		// leg recoil quaternions
		frontUpperRecoilOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		frontLowerRecoilOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		frontFootRecoilOrient = rightFoot.getOriginalOrientation().createOrthogonalMatrix3x3(); //new MatrixD3x3();

		backUpperRecoilOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		backLowerRecoilOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		backFootRecoilOrient = rightFoot.getOriginalOrientation().createOrthogonalMatrix3x3(); //new MatrixD3x3();

		recoilPos = null;
		distanceToMoveRecoil = 0.0;

		// leg passing stuff
		frontUpperPassingOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		frontLowerPassingOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		frontFootPassingOrient = rightFoot.getOriginalOrientation().createOrthogonalMatrix3x3(); //new MatrixD3x3();

		backUpperPassingOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		backLowerPassingOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		backFootPassingOrient = rightFoot.getOriginalOrientation().createOrthogonalMatrix3x3(); //new MatrixD3x3();

		passingPos = null;
		distanceToMovePassing = 0.0;

		// leg high point stuff
		frontUpperHighPointOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		frontLowerHighPointOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		frontFootHighPointOrient = rightFoot.getOriginalOrientation().createOrthogonalMatrix3x3(); //new MatrixD3x3();

		backUpperHighPointOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		backLowerHighPointOrient = OrthogonalMatrix3x3.createIdentity(); //new MatrixD3x3();
		backFootHighPointOrient = rightFoot.getOriginalOrientation().createOrthogonalMatrix3x3(); //new MatrixD3x3();

		highPointPos = null;
		distanceToMoveHighPoint = 0.0;
	}

	// assuming that legs are the same length
	public void setLegLengths() {
		Point3 top = new Point3();
		Point3 bottom = new Point3();

		footLength = 0.0;
		footHorizLength = 0.0;

		if( rightFoot != null ) {
			top = rightFoot.getTransformation( rightFoot ).translation;
			//			top = rightFoot.getPosition( rightFoot );
			bottom = rightFoot.getAxisAlignedMinimumBoundingBox().getCenterOfBottomFace();

			footLength = top.y - bottom.y;
			footHorizLength = bottom.z - top.z;
		}

		lowerLength = rightFoot.getTransformation( rightLower ).translation.calculateMagnitude();

		upperLength = rightLower.getTransformation( rightUpper ).translation.calculateMagnitude();

		totalLength = footLength + lowerLength + upperLength;
	}

	public double getStepLength() {
		double stepLength = totalLength * java.lang.Math.sin( contactAngle ) * 1.5;
		if( stepLength == 0.0 ) {
			stepLength = 1.0;
		}
		return stepLength;
	}

	public void setInitialOrientations() {
		if( rightUpper != null ) {
			rightUpperInitialOrient = rightUpper.getTransformation( rightUpper ).orientation;
		}
		//			rightUpperInitialOrient = rightUpper.getOrientation( rightUpper );
		if( rightLower != null ) {
			rightLowerInitialOrient = rightLower.getTransformation( rightLower ).orientation;
		}
		//			rightLower.getOrientation( rightLower );
		if( rightFoot != null )
		{
			rightFootInitialOrient = rightFoot.getTransformation( rightFoot ).orientation;
			//			rightFoot.getOrientation( rightFoot );
		}

		if( leftUpper != null ) {
			leftUpperInitialOrient = leftUpper.getTransformation( leftUpper ).orientation;
		}
		//			leftUpper.getOrientation( leftUpper );
		if( leftLower != null ) {
			leftLowerInitialOrient = leftLower.getTransformation( leftLower ).orientation;
		}
		//			leftLower.getOrientation( leftLower );
		if( leftFoot != null )
		{
			leftFootInitialOrient = leftFoot.getTransformation( leftFoot ).orientation;
			//			leftFoot.getOrientation( leftFoot );
		}

		if( rightUpperArm != null ) {
			rightUpperArmInitialOrient = rightUpperArm.getTransformation( org.lgna.story.implementation.AsSeenBy.PARENT ).orientation;
		}
		//			rightUpperArm.getOrientation( rightUpperArm );
		if( rightLowerArm != null )
		{
			rightLowerArmInitialOrient = rightLowerArm.getTransformation( org.lgna.story.implementation.AsSeenBy.PARENT ).orientation;
			//			rightLowerArm.getOrientation( rightLowerArm );
		}

		if( leftUpperArm != null ) {
			leftUpperArmInitialOrient = leftUpperArm.getTransformation( org.lgna.story.implementation.AsSeenBy.PARENT ).orientation;
		}

		//			leftUpperArm.getOrientation( leftUpperArm );
		if( leftLowerArm != null )
		{
			leftLowerArmInitialOrient = leftLowerArm.getTransformation( org.lgna.story.implementation.AsSeenBy.PARENT ).orientation;
			//			leftLowerArm.getOrientation( leftLowerArm );
		}

		if( ( rightUpper != null ) && ( leftUpper != null ) ) {

			Point3 top = rightUpper.getTransformation( rightUpper ).translation;
			//					rightUpper.getPosition( rightUpper );
			assert ( rightUpper.getAxisAlignedMinimumBoundingBox() != null );
			Point3 bottom = rightUpper.getAxisAlignedMinimumBoundingBox().getCenterOfBottomFace();
			double offset = ( top.y - bottom.y ) - totalLength;

			top = leftUpper.getTransformation( leftUpper ).translation;
			//					leftUpper.getPosition( leftUpper );
			bottom = leftUpper.getAxisAlignedMinimumBoundingBox().getCenterOfBottomFace();
			double offset2 = ( top.y - bottom.y ) - totalLength;

			if( offset2 > offset ) {
				offset = offset2;
			}

			//			initialPos = this.getPositionInScene(subject, new Point3( 0, offset, 0 ));
			//					// subject.getPositionInScene( new Point3( 0, offset, 0 ) );
			//			heightFromGround = initialPos.y;

			JointedModelImp subjectImp = EmployeesOnly.getImplementation( subject );
			heightFromGround = subjectImp.getAxisAlignedMinimumBoundingBox( org.lgna.story.implementation.AsSeenBy.SCENE ).getCenterOfBottomFace().y;

			initialBoundingBoxHeight = getCurrentLegHeight();
		}
	}

	protected Point3 getPositionInScene( SBiped subject, Point3 offset ) {
		BipedImp subjectImp = EmployeesOnly.getImplementation( subject );
		StandInImp subjectStandIn = StandInImp.acquireStandIn( subjectImp );
		subjectStandIn.setLocalPosition( offset );

		return subjectStandIn.getAbsoluteTransformation().translation;
	}

	public double getCurrentLegHeight() {
		if( rightUpper != null ) {
			rightUpper.getAxisAlignedMinimumBoundingBox( org.lgna.story.implementation.AsSeenBy.SCENE/* , HowMuch.INSTANCE */);

			double boundingBoxHeight = rightUpper.getAxisAlignedMinimumBoundingBox( org.lgna.story.implementation.AsSeenBy.SCENE ).getHeight();
			double boundingBoxHeight2 = leftUpper.getAxisAlignedMinimumBoundingBox( org.lgna.story.implementation.AsSeenBy.SCENE ).getHeight();
			if( boundingBoxHeight2 > boundingBoxHeight ) {
				boundingBoxHeight = boundingBoxHeight2;
			}

			return boundingBoxHeight;
		} else {
			return 0.0;
		}
	}

	public void setContactData() {
		double rotationLower = 0.0;
		double rotationUpper = 0.0;

		if( ( leftLower == null ) || ( rightLower == null ) ) {
			rotationUpper = contactAngle;
		} else {

			double lowerLegEffectiveLength = java.lang.Math.sqrt( ( footHorizLength * footHorizLength ) + ( ( lowerLength + footLength ) * ( lowerLength + footLength ) ) );
			double kneeAngle = ( ( totalLength * totalLength ) - ( upperLength * upperLength ) - ( lowerLegEffectiveLength * lowerLegEffectiveLength ) ) / ( -2.0 * upperLength * lowerLegEffectiveLength );

			kneeAngle = java.lang.Math.acos( kneeAngle );

			rotationLower = ( java.lang.Math.PI - kneeAngle ) + java.lang.Math.atan( footHorizLength / ( footLength + lowerLength ) );
			rotationUpper = contactAngle - java.lang.Math.asin( ( lowerLegEffectiveLength * java.lang.Math.sin( kneeAngle ) ) / totalLength );

			recoilBackLowerAngle += rotationLower;
			recoilFrontUpperAngle += contactAngle;

			passingFrontUpperAngle = recoilFrontUpperAngle;
			passingFrontLowerAngle = recoilFrontUpperAngle + 0.2;
			passingFrontFootAngle = 0.2;
			passingBackLowerAngle += recoilBackLowerAngle;

			highPointBackLowerAngle = passingBackLowerAngle / 2.0;
		}

		RotationUtilities.rotateAroundX( frontUpperContactOrient, -1.0 * contactAngle );
		RotationUtilities.rotateAroundX( backUpperContactOrient, rotationUpper );
		RotationUtilities.rotateAroundX( backLowerContactOrient, rotationLower );

		distanceToMoveContact = totalLength - ( totalLength * java.lang.Math.cos( contactAngle ) );
		contactPos = this.getPositionInScene( subject, new Point3( 0, -1.0 * distanceToMoveContact, 0 ) );
		//subject.getPositionInScene( new Point3( 0, -1.0 * distanceToMoveContact, 0 ) );
	}

	public void setRecoilData() {
		RotationUtilities.rotateAroundX( frontUpperRecoilOrient, -1.0 * recoilFrontUpperAngle );
		RotationUtilities.rotateAroundX( frontLowerRecoilOrient, recoilFrontUpperAngle );
		RotationUtilities.rotateAroundX( backLowerRecoilOrient, recoilBackLowerAngle );

		double distance = ( ( upperLength - ( upperLength * java.lang.Math.cos( passingFrontUpperAngle ) ) ) + lowerLength ) - ( lowerLength * java.lang.Math.cos( passingFrontLowerAngle - passingFrontUpperAngle ) );
		recoilPos = this.getPositionInScene( subject, new Point3( 0, -1.0 * distance, 0 ) );
		//				subject.getPositionInScene( new Point3( 0, -1.0 * distance, 0 ) );
	}

	public void setPassingData() {
		RotationUtilities.rotateAroundX( frontUpperPassingOrient, -1.0 * passingFrontUpperAngle );
		RotationUtilities.rotateAroundX( frontLowerPassingOrient, passingFrontLowerAngle );
		RotationUtilities.rotateAroundX( frontFootPassingOrient, -1.0 * passingFrontUpperAngle );

		RotationUtilities.rotateAroundX( backUpperPassingOrient, -1.0 * passingFrontUpperAngle );
		RotationUtilities.rotateAroundX( backLowerPassingOrient, passingBackLowerAngle );

		double distance = upperLength - ( upperLength * java.lang.Math.cos( recoilFrontUpperAngle ) );
		passingPos = this.getPositionInScene( subject, new Point3( 0, -1.0 * distance, 0 ) );
		//				subject.getPositionInScene( new Point3( 0, -1.0 * distance, 0 ) );

	}

	public void setHighPointData() {
		RotationUtilities.rotateAroundX( frontUpperHighPointOrient, highPointFrontUpperAngle );

		RotationUtilities.rotateAroundX( backUpperHighPointOrient, -1.0 * highPointBackUpperAngle );
		RotationUtilities.rotateAroundX( backLowerHighPointOrient, highPointBackLowerAngle );

		double distance = totalLength - ( totalLength * java.lang.Math.cos( highPointFrontUpperAngle ) );
		highPointPos = this.getPositionInScene( subject, new Point3( 0, -1.0 * distance, 0 ) );
		//				subject.getPositionInScene( new Point3( 0, -1.0 * distance, 0 ) );
	}

	public void setArmData() {

		frontRightUpperArmOrient = rightUpperArm.getOriginalOrientation().createOrthogonalMatrix3x3();
		RotationUtilities.rotateAroundY( frontRightUpperArmOrient, 1.0 * upperArmAngle );

		frontRightLowerArmOrient = rightLowerArm.getOriginalOrientation().createOrthogonalMatrix3x3();

		backRightUpperArmOrient = rightUpperArm.getOriginalOrientation().createOrthogonalMatrix3x3();
		RotationUtilities.rotateAroundY( backRightUpperArmOrient, -1.0 * upperArmAngle );

		backRightLowerArmOrient = rightLowerArm.getOriginalOrientation().createOrthogonalMatrix3x3();
		RotationUtilities.rotateAroundY( backRightLowerArmOrient, -1.0 * lowerArmAngle );

		frontLeftUpperArmOrient = leftUpperArm.getOriginalOrientation().createOrthogonalMatrix3x3();
		RotationUtilities.rotateAroundY( frontLeftUpperArmOrient, -1.0 * upperArmAngle );

		frontLeftLowerArmOrient = leftLowerArm.getOriginalOrientation().createOrthogonalMatrix3x3();

		backLeftUpperArmOrient = leftUpperArm.getOriginalOrientation().createOrthogonalMatrix3x3();
		RotationUtilities.rotateAroundY( backLeftUpperArmOrient, 1.0 * upperArmAngle );

		backLeftLowerArmOrient = leftLowerArm.getOriginalOrientation().createOrthogonalMatrix3x3();
		RotationUtilities.rotateAroundY( backLeftLowerArmOrient, 1.0 * lowerArmAngle );

		//		rightUpperArmCurrentOrient = rightUpper.getOriginalOrientation().createOrthogonalMatrix3x3();
		//		rightLowerArmCurrentOrient = rightLower.getOriginalOrientation().createOrthogonalMatrix3x3();
		//		leftUpperArmCurrentOrient = leftUpper.getOriginalOrientation().createOrthogonalMatrix3x3();
		//		leftLowerArmCurrentOrient = leftLower.getOriginalOrientation().createOrthogonalMatrix3x3();
	}

	public void updateContact( JointImp leg, double portion ) {
		if( portion <= 1.0 ) {
			if( leg == null ) {
			} else if( leg.equals( rightUpper ) ) {
				setUnitQuaternionD( rightUpper, rightUpperCurrentOrient, frontUpperContactOrient, portion );
				setUnitQuaternionD( rightLower, rightLowerCurrentOrient, frontLowerContactOrient, portion );

				setUnitQuaternionD( leftUpper, leftUpperCurrentOrient, backUpperContactOrient, portion );
				setUnitQuaternionD( leftLower, leftLowerCurrentOrient, backLowerContactOrient, portion );
			} else {
				setUnitQuaternionD( leftUpper, leftUpperCurrentOrient, frontUpperContactOrient, portion );
				setUnitQuaternionD( leftLower, leftLowerCurrentOrient, frontLowerContactOrient, portion );

				setUnitQuaternionD( rightUpper, rightUpperCurrentOrient, backUpperContactOrient, portion );
				setUnitQuaternionD( rightLower, rightLowerCurrentOrient, backLowerContactOrient, portion );
			}
		}
	}

	public void updateRecoil( JointImp leg, double portion ) {
		if( leg == null ) {
		} else if( portion <= 1.0 ) {
			if( leg.equals( rightUpper ) ) {
				setUnitQuaternionD( rightUpper, rightUpperCurrentOrient, frontUpperRecoilOrient, portion );
				setUnitQuaternionD( rightLower, rightLowerCurrentOrient, frontLowerRecoilOrient, portion );

				setUnitQuaternionD( leftUpper, leftUpperCurrentOrient, backUpperRecoilOrient, portion );
				setUnitQuaternionD( leftLower, leftLowerCurrentOrient, backLowerRecoilOrient, portion );
			} else {
				setUnitQuaternionD( leftUpper, leftUpperCurrentOrient, frontUpperRecoilOrient, portion );
				setUnitQuaternionD( leftLower, leftLowerCurrentOrient, frontLowerRecoilOrient, portion );

				setUnitQuaternionD( rightUpper, rightUpperCurrentOrient, backUpperRecoilOrient, portion );
				setUnitQuaternionD( rightLower, rightLowerCurrentOrient, backLowerRecoilOrient, portion );
			}

		}
	}

	public void updatePassing( JointImp leg, double portion ) {
		if( leg == null ) {
		} else if( portion <= 1.0 ) {
			if( leg.equals( rightUpper ) ) {
				setUnitQuaternionD( rightUpper, rightUpperCurrentOrient, frontUpperPassingOrient, portion );
				setUnitQuaternionD( rightLower, rightLowerCurrentOrient, frontLowerPassingOrient, portion );
				setUnitQuaternionD( rightFoot, rightFootCurrentOrient, frontFootPassingOrient, portion );

				setUnitQuaternionD( leftUpper, leftUpperCurrentOrient, backUpperPassingOrient, portion );
				setUnitQuaternionD( leftLower, leftLowerCurrentOrient, backLowerPassingOrient, portion );
				setUnitQuaternionD( leftFoot, leftFootCurrentOrient, backFootPassingOrient, portion );
			} else {
				setUnitQuaternionD( leftUpper, leftUpperCurrentOrient, frontUpperPassingOrient, portion );
				setUnitQuaternionD( leftLower, leftLowerCurrentOrient, frontLowerPassingOrient, portion );
				setUnitQuaternionD( leftFoot, leftFootCurrentOrient, frontFootPassingOrient, portion );

				setUnitQuaternionD( rightUpper, rightUpperCurrentOrient, backUpperPassingOrient, portion );
				setUnitQuaternionD( rightLower, rightLowerCurrentOrient, backLowerPassingOrient, portion );
				setUnitQuaternionD( rightFoot, rightFootCurrentOrient, backFootPassingOrient, portion );
			}
		}
	}

	public void updateHighPoint( JointImp leg, double portion, boolean lastStep ) {
		if( leg == null ) {
		} else if( portion <= 1.0 ) {
			if( lastStep ) {
				if( leg.equals( rightUpper ) ) {
					setUnitQuaternionD( rightUpper, rightUpperCurrentOrient, rightUpper.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );
					setUnitQuaternionD( rightLower, rightLowerCurrentOrient, rightLower.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );
					setUnitQuaternionD( rightFoot, rightFootCurrentOrient, rightFoot.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );

					setUnitQuaternionD( leftUpper, leftUpperCurrentOrient, leftUpper.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );
					setUnitQuaternionD( leftLower, leftLowerCurrentOrient, leftLower.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );
				} else {
					setUnitQuaternionD( leftUpper, leftUpperCurrentOrient, leftUpper.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );
					setUnitQuaternionD( leftLower, leftLowerCurrentOrient, leftLower.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );
					setUnitQuaternionD( leftFoot, leftFootCurrentOrient, leftFoot.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );

					setUnitQuaternionD( rightUpper, rightUpperCurrentOrient, rightUpper.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );
					setUnitQuaternionD( rightLower, rightLowerCurrentOrient, rightLower.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );
				}
			} else {

				if( leg.equals( rightUpper ) ) {
					setUnitQuaternionD( rightUpper, rightUpperCurrentOrient, frontUpperHighPointOrient, portion );
					setUnitQuaternionD( rightLower, rightLowerCurrentOrient, frontLowerHighPointOrient, portion );
					setUnitQuaternionD( rightFoot, rightFootCurrentOrient, frontFootHighPointOrient, portion );

					setUnitQuaternionD( leftUpper, leftUpperCurrentOrient, backUpperHighPointOrient, portion );
					setUnitQuaternionD( leftLower, leftLowerCurrentOrient, backLowerHighPointOrient, portion );
				} else {
					setUnitQuaternionD( leftUpper, leftUpperCurrentOrient, frontUpperHighPointOrient, portion );
					setUnitQuaternionD( leftLower, leftLowerCurrentOrient, frontLowerHighPointOrient, portion );
					setUnitQuaternionD( leftFoot, leftFootCurrentOrient, frontFootHighPointOrient, portion );

					setUnitQuaternionD( rightUpper, rightUpperCurrentOrient, backUpperHighPointOrient, portion );
					setUnitQuaternionD( rightLower, rightLowerCurrentOrient, backLowerHighPointOrient, portion );
				}
			}

		}
	}

	public void updateArms( JointImp leg, double portion, boolean lastStep ) {
		if( lastStep && ( leg != null ) ) {
			setUnitQuaternionD( leftUpperArm, leftUpperArmCurrentOrient, leftUpperArm.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );
			setUnitQuaternionD( leftLowerArm, leftLowerArmCurrentOrient, leftLowerArm.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );

			setUnitQuaternionD( rightUpperArm, rightUpperArmCurrentOrient, rightUpperArm.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );
			setUnitQuaternionD( rightLowerArm, rightLowerArmCurrentOrient, rightLowerArm.getOriginalOrientation().createOrthogonalMatrix3x3(), portion );
		} else {
			if( leg == null ) {
			} else if( leg.equals( leftUpper ) ) {
				setUnitQuaternionD( rightUpperArm, rightUpperArmCurrentOrient, frontRightUpperArmOrient, portion );
				setUnitQuaternionD( rightLowerArm, rightLowerArmCurrentOrient, frontRightLowerArmOrient, portion );

				setUnitQuaternionD( leftUpperArm, leftUpperArmCurrentOrient, backLeftUpperArmOrient, portion );
				setUnitQuaternionD( leftLowerArm, leftLowerArmCurrentOrient, backLeftLowerArmOrient, portion );
			} else {
				setUnitQuaternionD( leftUpperArm, leftUpperArmCurrentOrient, frontLeftUpperArmOrient, portion );
				setUnitQuaternionD( leftLowerArm, leftLowerArmCurrentOrient, frontLeftLowerArmOrient, portion );

				setUnitQuaternionD( rightUpperArm, rightUpperArmCurrentOrient, backRightUpperArmOrient, portion );
				setUnitQuaternionD( rightLowerArm, rightLowerArmCurrentOrient, backRightLowerArmOrient, portion );
			}
		}
	}

	@Override
	protected void preEpilogue() {
	}

	@Override
	protected void epilogue() {
		if( leftUpper != null ) {
			if( this.isArmSwingDesired() ) {
				setUnitQuaternionD( leftUpperArm, leftUpperArmCurrentOrient, leftUpperArm.getOriginalOrientation().createOrthogonalMatrix3x3(), 1.0 );
				setUnitQuaternionD( leftLowerArm, leftLowerArmCurrentOrient, leftLowerArm.getOriginalOrientation().createOrthogonalMatrix3x3(), 1.0 );

				setUnitQuaternionD( rightUpperArm, rightUpperArmCurrentOrient, rightUpperArm.getOriginalOrientation().createOrthogonalMatrix3x3(), 1.0 );
				setUnitQuaternionD( rightLowerArm, rightLowerArmCurrentOrient, rightLowerArm.getOriginalOrientation().createOrthogonalMatrix3x3(), 1.0 );
			}

			setUnitQuaternionD( rightUpper, rightUpperCurrentOrient, rightUpper.getOriginalOrientation().createOrthogonalMatrix3x3(), 1.0 );
			setUnitQuaternionD( rightLower, rightLowerCurrentOrient, rightLower.getOriginalOrientation().createOrthogonalMatrix3x3(), 1.0 );
			setUnitQuaternionD( rightFoot, rightFootCurrentOrient, rightFoot.getOriginalOrientation().createOrthogonalMatrix3x3(), 1.0 );

			setUnitQuaternionD( leftUpper, leftUpperCurrentOrient, leftUpper.getOriginalOrientation().createOrthogonalMatrix3x3(), 1.0 );
			setUnitQuaternionD( leftLower, leftLowerCurrentOrient, leftLower.getOriginalOrientation().createOrthogonalMatrix3x3(), 1.0 );
			setUnitQuaternionD( leftFoot, leftFootCurrentOrient, leftFoot.getOriginalOrientation().createOrthogonalMatrix3x3(), 1.0 );
		}

		adjustHeight();

	}

	private void setUnitQuaternionD( JointImp part, OrthogonalMatrix3x3 initialOrient, OrthogonalMatrix3x3 finalOrient, double portion ) {
		//todo: add style
		//double positionPortion = m_style.getPortion( portion, 1 );
		double positionPortion = portion;

		// attempt to get rid of all the epsilon warnings
		initialOrient.normalizeColumns();
		finalOrient.normalizeColumns();

		UnitQuaternion currentOrient = UnitQuaternion.createInterpolation( new UnitQuaternion( initialOrient ), new UnitQuaternion( finalOrient ), positionPortion );

		if( part != null ) {

			// this will get fixed in the "rightnow" pass
			part.setLocalOrientation( currentOrient.createOrthogonalMatrix3x3() );
			//			part.setOrientationRelativeToVehicle(currentOrient);

		}
	}
}
