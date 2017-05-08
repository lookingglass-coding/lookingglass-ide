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

package org.lgna.story.implementation;

import org.lgna.ik.core.IKCore;
import org.lgna.ik.core.IKCore.Limb;
import org.lgna.story.ArmSwing;
import org.lgna.story.Bounce;
import org.lgna.story.EmployeesOnly;
import org.lgna.story.SModel;
import org.lgna.story.SThing;
import org.lgna.story.StrideLength;
import org.lgna.story.implementation.tempwalkandtouch.SpatialRelation;
import org.lgna.story.implementation.tempwalkandtouch.WalkAnimation;
import org.lgna.story.implementation.tempwalkandtouch.WalkToAnimation;
import org.lgna.story.resources.JointId;

/**
 * @author Dennis Cosgrove
 */
public final class BipedImp extends JointedModelImp<org.lgna.story.SBiped, org.lgna.story.resources.BipedResource> {

	public BipedImp( org.lgna.story.SBiped abstraction, JointImplementationAndVisualDataFactory<org.lgna.story.resources.BipedResource> factory ) {
		super( abstraction, factory );
	}

	@Override
	public JointId[] getRootJointIds() {
		return org.lgna.story.resources.BipedResource.JOINT_ID_ROOTS;
	}

	@Override
	protected edu.cmu.cs.dennisc.math.Vector4 getThoughtBubbleOffset() {
		return this.getTopOffsetForJoint( this.getJointImplementation( org.lgna.story.resources.BipedResource.HEAD ) );
	}

	@Override
	protected edu.cmu.cs.dennisc.math.Vector4 getSpeechBubbleOffset() {
		return this.getFrontOffsetForJoint( this.getJointImplementation( org.lgna.story.resources.BipedResource.MOUTH ) );
	}

	public void reachFor( SThing entity, Limb reachingLimb ) {
		JointImp anchor;
		JointImp end;
		switch( reachingLimb ) {
		case RIGHT_ARM:
			anchor = EmployeesOnly.getImplementation( this.getAbstraction().getRightClavicle() );
			end = EmployeesOnly.getImplementation( this.getAbstraction().getRightWrist() );
			break;
		case LEFT_ARM:
			anchor = EmployeesOnly.getImplementation( this.getAbstraction().getLeftClavicle() );
			end = EmployeesOnly.getImplementation( this.getAbstraction().getLeftWrist() );
			break;
		case RIGHT_LEG:
			anchor = EmployeesOnly.getImplementation( this.getAbstraction().getRightHip() );
			end = EmployeesOnly.getImplementation( this.getAbstraction().getRightFoot() );
			break;
		case LEFT_LEG:
			anchor = EmployeesOnly.getImplementation( this.getAbstraction().getLeftHip() );
			end = EmployeesOnly.getImplementation( this.getAbstraction().getLeftFoot() );
			break;
		default:
			System.out.println( "Unhandled LIMB: " + reachingLimb );
			return;
		}
		IKCore.moveChainToPointInSceneSpace( anchor, end, EmployeesOnly.getImplementation( entity ).getTransformation( AsSeenBy.SCENE ).translation );
	}

	public void animateWalk( double amount, double walkPace, StrideLength strideLength, Bounce bounce, ArmSwing armSwing ) {
		this.perform( new WalkAnimation( this.getAbstraction(), amount, walkPace, strideLength, bounce, armSwing ) );
	}

	public void animateWalkTo( SModel model, double walkPace, StrideLength strideLength, Bounce bounce, ArmSwing armSwing, double distanceBetween ) {
		this.perform( new WalkToAnimation( this.getAbstraction(), model, SpatialRelation.IN_FRONT_OF, distanceBetween, walkPace, strideLength, bounce, armSwing ) );
	}
}
