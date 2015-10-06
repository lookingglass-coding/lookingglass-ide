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
package edu.wustl.lookingglass.remix.ast;

import java.util.HashMap;

import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractType;
import org.lgna.project.ast.MethodInvocation;
import org.lgna.story.SBiped;
import org.lgna.story.SFlyer;
import org.lgna.story.SQuadruped;
import org.lgna.story.SSwimmer;

/**
 * @author Caitlin Kelleher
 */
public class DefaultJointSubstitutionManager {

	/*
	 * This is a series of lookup tables to provide a simplistic strategy for replacing
	 * parts not in one type with the anatomically equivalent parts in the target type.
	 * Note that there aren't valid substitutions for all parts. So, in these cases it
	 * currently seems best to return null and let the user pick a substitution.
	 */

	public enum UniversalJoint {
		PELVIS,
		SPINE_BASE,
		SPINE_MIDDLE,
		SPINE_UPPER,
		NECK,

		HEAD,
		MOUTH,
		RIGHT_EYE,
		RIGHT_EYELID,
		LEFT_EYE,
		LEFT_EYELID,
		RIGHT_EAR,
		LEFT_EAR,

		LOWER_RIGHT_HIP,
		LOWER_RIGHT_KNEE,
		LOWER_RIGHT_ANKLE,
		LOWER_RIGHT_FOOT,
		LOWER_RIGHT_HOCK,
		LOWER_RIGHT_TOE,

		LOWER_LEFT_HIP,
		LOWER_LEFT_KNEE,
		LOWER_LEFT_ANKLE,
		LOWER_LEFT_FOOT,
		LOWER_LEFT_HOCK,
		LOWER_LEFT_TOE,

		UPPER_RIGHT_CLAVICLE,
		UPPER_RIGHT_SHOULDER,
		UPPER_RIGHT_ELBOW,
		UPPER_RIGHT_WRIST,
		UPPER_RIGHT_HAND,
		UPPER_RIGHT_THUMB,
		UPPER_RIGHT_THUMB_KNUCKLE,
		UPPER_RIGHT_INDEX_FINGER,
		UPPER_RIGHT_INDEX_FINGER_KNUCKLE,
		UPPER_RIGHT_MIDDLE_FINGER,
		UPPER_RIGHT_MIDDLE_FINGER_KNUCKLE,
		UPPER_RIGHT_PINKY_FINGER,
		UPPER_RIGHT_PINKY_FINGER_KNUCKLE,
		UPPER_RIGHT_HOCK,
		UPPER_RIGHT_TOE,

		UPPER_LEFT_CLAVICLE,
		UPPER_LEFT_SHOULDER,
		UPPER_LEFT_ELBOW,
		UPPER_LEFT_WRIST,
		UPPER_LEFT_HAND,
		UPPER_LEFT_THUMB,
		UPPER_LEFT_THUMB_KNUCKLE,
		UPPER_LEFT_INDEX_FINGER,
		UPPER_LEFT_INDEX_FINGER_KNUCKLE,
		UPPER_LEFT_MIDDLE_FINGER,
		UPPER_LEFT_MIDDLE_FINGER_KNUCKLE,
		UPPER_LEFT_PINKY_FINGER,
		UPPER_LEFT_PINKY_FINGER_KNUCKLE,
		UPPER_LEFT_HOCK,
		UPPER_LEFT_TOE,

		TAIL,
		TAIL2,
		TAIL3,
		TAIL4
	}

	private static HashMap<UniversalJoint, String> bipedJointMap = new HashMap<>();
	private static HashMap<UniversalJoint, String> flyerJointMap = new HashMap<>();
	private static HashMap<UniversalJoint, String> quadrupedJointMap = new HashMap<>();
	private static HashMap<UniversalJoint, String> fishJointMap = new HashMap<>();

	private static HashMap<String, UniversalJoint> bipedReverseJointMap = new HashMap<>();
	private static HashMap<String, UniversalJoint> flyerReverseJointMap = new HashMap<>();
	private static HashMap<String, UniversalJoint> quadrupedReverseJointMap = new HashMap<>();
	private static HashMap<String, UniversalJoint> fishReverseJointMap = new HashMap<>();

	private static boolean mapsInitialized = false;

	private static void initializeBipedMap() {
		bipedJointMap.put( UniversalJoint.PELVIS, "getPelvis" );
		bipedJointMap.put( UniversalJoint.SPINE_BASE, "getSpineBase" );
		bipedJointMap.put( UniversalJoint.SPINE_MIDDLE, "getSpineMiddle" );
		bipedJointMap.put( UniversalJoint.SPINE_UPPER, "getSpineUpper" );
		bipedJointMap.put( UniversalJoint.NECK, "getNeck" );
		bipedJointMap.put( UniversalJoint.HEAD, "getHead" );
		bipedJointMap.put( UniversalJoint.MOUTH, "getMouth" );
		bipedJointMap.put( UniversalJoint.RIGHT_EYE, "getRightEye" );
		bipedJointMap.put( UniversalJoint.LEFT_EYE, "getLeftEye" );
		bipedJointMap.put( UniversalJoint.RIGHT_EYELID, "getRightEyelid" );
		bipedJointMap.put( UniversalJoint.LEFT_EYELID, "getLeftEyelid" );

		bipedJointMap.put( UniversalJoint.LOWER_RIGHT_HIP, "getRightHip" );
		bipedJointMap.put( UniversalJoint.LOWER_RIGHT_KNEE, "getRightKnee" );
		bipedJointMap.put( UniversalJoint.LOWER_RIGHT_ANKLE, "getRightAnkle" );
		bipedJointMap.put( UniversalJoint.LOWER_RIGHT_FOOT, "getRightFoot" );

		bipedJointMap.put( UniversalJoint.LOWER_LEFT_HIP, "getLeftHip" );
		bipedJointMap.put( UniversalJoint.LOWER_LEFT_KNEE, "getLeftKnee" );
		bipedJointMap.put( UniversalJoint.LOWER_LEFT_ANKLE, "getLeftAnkle" );
		bipedJointMap.put( UniversalJoint.LOWER_LEFT_FOOT, "getLeftFoot" );

		bipedJointMap.put( UniversalJoint.UPPER_RIGHT_CLAVICLE, "getRightClavicle" );
		bipedJointMap.put( UniversalJoint.UPPER_RIGHT_SHOULDER, "getRightShoulder" );
		bipedJointMap.put( UniversalJoint.UPPER_RIGHT_ELBOW, "getRightElbow" );
		bipedJointMap.put( UniversalJoint.UPPER_RIGHT_WRIST, "getRightWrist" );
		bipedJointMap.put( UniversalJoint.UPPER_RIGHT_HAND, "getRightHand" );
		bipedJointMap.put( UniversalJoint.UPPER_RIGHT_THUMB, "getRightThumb" );
		bipedJointMap.put( UniversalJoint.UPPER_RIGHT_THUMB_KNUCKLE, "getRightThumbKnuckle" );
		bipedJointMap.put( UniversalJoint.UPPER_RIGHT_INDEX_FINGER, "getRightIndexFinger" );
		bipedJointMap.put( UniversalJoint.UPPER_RIGHT_INDEX_FINGER_KNUCKLE, "getRightIndexFingerKnuckle" );
		bipedJointMap.put( UniversalJoint.UPPER_RIGHT_MIDDLE_FINGER, "getRightMiddleFinger" );
		bipedJointMap.put( UniversalJoint.UPPER_RIGHT_MIDDLE_FINGER_KNUCKLE, "getRightMiddleFingerKnuckle" );
		bipedJointMap.put( UniversalJoint.UPPER_RIGHT_PINKY_FINGER, "getRightPinkyFinger" );
		bipedJointMap.put( UniversalJoint.UPPER_RIGHT_PINKY_FINGER_KNUCKLE, "getRightPinkyFingerKnuckle" );

		bipedJointMap.put( UniversalJoint.UPPER_LEFT_CLAVICLE, "getLeftClavicle" );
		bipedJointMap.put( UniversalJoint.UPPER_LEFT_SHOULDER, "getLeftShoulder" );
		bipedJointMap.put( UniversalJoint.UPPER_LEFT_ELBOW, "getLeftElbow" );
		bipedJointMap.put( UniversalJoint.UPPER_LEFT_WRIST, "getLeftWrist" );
		bipedJointMap.put( UniversalJoint.UPPER_LEFT_HAND, "getLeftHand" );
		bipedJointMap.put( UniversalJoint.UPPER_LEFT_THUMB, "getLeftThumb" );
		bipedJointMap.put( UniversalJoint.UPPER_LEFT_THUMB_KNUCKLE, "getLeftThumbKnuckle" );
		bipedJointMap.put( UniversalJoint.UPPER_LEFT_INDEX_FINGER, "getLeftIndexFinger" );
		bipedJointMap.put( UniversalJoint.UPPER_LEFT_INDEX_FINGER_KNUCKLE, "getLeftIndexFingerKnuckle" );
		bipedJointMap.put( UniversalJoint.UPPER_LEFT_MIDDLE_FINGER, "getLeftMiddleFinger" );
		bipedJointMap.put( UniversalJoint.UPPER_LEFT_MIDDLE_FINGER_KNUCKLE, "getLeftMiddleFingerKnuckle" );
		bipedJointMap.put( UniversalJoint.UPPER_LEFT_PINKY_FINGER, "getLeftPinkyFinger" );
		bipedJointMap.put( UniversalJoint.UPPER_LEFT_PINKY_FINGER_KNUCKLE, "getLeftPinkyFingerKnuckle" );

	}

	private static void initializeReverseBipedMap() {
		bipedReverseJointMap.put( "getPelvis", UniversalJoint.PELVIS );
		bipedReverseJointMap.put( "getSpineBase", UniversalJoint.SPINE_BASE );
		bipedReverseJointMap.put( "getSpineMiddle", UniversalJoint.SPINE_MIDDLE );
		bipedReverseJointMap.put( "getSpineUpper", UniversalJoint.SPINE_UPPER );
		bipedReverseJointMap.put( "getNeck", UniversalJoint.NECK );
		bipedReverseJointMap.put( "getHead", UniversalJoint.HEAD );
		bipedReverseJointMap.put( "getMouth", UniversalJoint.MOUTH );
		bipedReverseJointMap.put( "getRightEye", UniversalJoint.RIGHT_EYE );
		bipedReverseJointMap.put( "getLeftEye", UniversalJoint.LEFT_EYE );
		bipedReverseJointMap.put( "getRightEyelid", UniversalJoint.RIGHT_EYELID );
		bipedReverseJointMap.put( "getLeftEyelid", UniversalJoint.LEFT_EYELID );

		bipedReverseJointMap.put( "getRightHip", UniversalJoint.LOWER_RIGHT_HIP );
		bipedReverseJointMap.put( "getRightKnee", UniversalJoint.LOWER_RIGHT_KNEE );
		bipedReverseJointMap.put( "getRightAnkle", UniversalJoint.LOWER_RIGHT_ANKLE );
		bipedReverseJointMap.put( "getRightFoot", UniversalJoint.LOWER_RIGHT_FOOT );

		bipedReverseJointMap.put( "getLeftHip", UniversalJoint.LOWER_LEFT_HIP );
		bipedReverseJointMap.put( "getLeftKnee", UniversalJoint.LOWER_LEFT_KNEE );
		bipedReverseJointMap.put( "getLeftAnkle", UniversalJoint.LOWER_LEFT_ANKLE );
		bipedReverseJointMap.put( "getLeftFoot", UniversalJoint.LOWER_LEFT_FOOT );

		bipedReverseJointMap.put( "getRightClavicle", UniversalJoint.UPPER_RIGHT_CLAVICLE );
		bipedReverseJointMap.put( "getRightShoulder", UniversalJoint.UPPER_RIGHT_SHOULDER );
		bipedReverseJointMap.put( "getRightElbow", UniversalJoint.UPPER_RIGHT_ELBOW );
		bipedReverseJointMap.put( "getRightWrist", UniversalJoint.UPPER_RIGHT_WRIST );
		bipedReverseJointMap.put( "getRightHand", UniversalJoint.UPPER_RIGHT_HAND );
		bipedReverseJointMap.put( "getRightThumb", UniversalJoint.UPPER_RIGHT_THUMB );
		bipedReverseJointMap.put( "getRightThumbKnuckle", UniversalJoint.UPPER_RIGHT_THUMB_KNUCKLE );
		bipedReverseJointMap.put( "getRightIndexFinger", UniversalJoint.UPPER_RIGHT_INDEX_FINGER );
		bipedReverseJointMap.put( "getRightIndexFingerKnuckle", UniversalJoint.UPPER_RIGHT_INDEX_FINGER_KNUCKLE );
		bipedReverseJointMap.put( "getRightMiddleFinger", UniversalJoint.UPPER_RIGHT_MIDDLE_FINGER );
		bipedReverseJointMap.put( "getRightMiddleFingerKnuckle", UniversalJoint.UPPER_RIGHT_MIDDLE_FINGER_KNUCKLE );
		bipedReverseJointMap.put( "getRightPinkyFinger", UniversalJoint.UPPER_RIGHT_PINKY_FINGER );
		bipedReverseJointMap.put( "getRightPinkyFingerKnuckle", UniversalJoint.UPPER_RIGHT_PINKY_FINGER_KNUCKLE );

		bipedReverseJointMap.put( "getLeftClavicle", UniversalJoint.UPPER_LEFT_CLAVICLE );
		bipedReverseJointMap.put( "getLeftShoulder", UniversalJoint.UPPER_LEFT_SHOULDER );
		bipedReverseJointMap.put( "getLeftElbow", UniversalJoint.UPPER_LEFT_ELBOW );
		bipedReverseJointMap.put( "getLeftWrist", UniversalJoint.UPPER_LEFT_WRIST );
		bipedReverseJointMap.put( "getLeftHand", UniversalJoint.UPPER_LEFT_HAND );
		bipedReverseJointMap.put( "getLeftThumb", UniversalJoint.UPPER_LEFT_THUMB );
		bipedReverseJointMap.put( "getLeftThumbKnuckle", UniversalJoint.UPPER_LEFT_THUMB_KNUCKLE );
		bipedReverseJointMap.put( "getLeftIndexFinger", UniversalJoint.UPPER_LEFT_INDEX_FINGER );
		bipedReverseJointMap.put( "getLeftIndexFingerKnuckle", UniversalJoint.UPPER_LEFT_INDEX_FINGER_KNUCKLE );
		bipedReverseJointMap.put( "getLeftMiddleFinger", UniversalJoint.UPPER_LEFT_MIDDLE_FINGER );
		bipedReverseJointMap.put( "getLeftMiddleFingerKnuckle", UniversalJoint.UPPER_LEFT_MIDDLE_FINGER_KNUCKLE );
		bipedReverseJointMap.put( "getLeftPinkyFinger", UniversalJoint.UPPER_LEFT_PINKY_FINGER );
		bipedReverseJointMap.put( "getLeftPinkyFingerKnuckle", UniversalJoint.UPPER_LEFT_PINKY_FINGER_KNUCKLE );

	}

	private static void initializeFlyerMap() {
		flyerJointMap.put( UniversalJoint.PELVIS, "getPelvisLowerBody" );
		flyerJointMap.put( UniversalJoint.SPINE_BASE, "getSpineBase" );
		flyerJointMap.put( UniversalJoint.SPINE_MIDDLE, "getSpineMiddle" );
		flyerJointMap.put( UniversalJoint.SPINE_UPPER, "getSpineUpper" );
		flyerJointMap.put( UniversalJoint.NECK, "getNeck" );
		flyerJointMap.put( UniversalJoint.HEAD, "getHead" );
		flyerJointMap.put( UniversalJoint.MOUTH, "getMouth" );
		flyerJointMap.put( UniversalJoint.RIGHT_EYE, "getRightEye" );
		flyerJointMap.put( UniversalJoint.LEFT_EYE, "getLeftEye" );
		flyerJointMap.put( UniversalJoint.RIGHT_EYELID, "getRightEyelid" );
		flyerJointMap.put( UniversalJoint.LEFT_EYELID, "getLeftEyelid" );

		flyerJointMap.put( UniversalJoint.LOWER_RIGHT_HIP, "getRightHip" );
		flyerJointMap.put( UniversalJoint.LOWER_RIGHT_KNEE, "getRightKnee" );
		flyerJointMap.put( UniversalJoint.LOWER_RIGHT_ANKLE, "getRightAnkle" );
		flyerJointMap.put( UniversalJoint.LOWER_RIGHT_FOOT, "getRightFoot" );

		flyerJointMap.put( UniversalJoint.LOWER_LEFT_HIP, "getLeftHip" );
		flyerJointMap.put( UniversalJoint.LOWER_LEFT_KNEE, "getLeftKnee" );
		flyerJointMap.put( UniversalJoint.LOWER_LEFT_ANKLE, "getLeftAnkle" );
		flyerJointMap.put( UniversalJoint.LOWER_LEFT_FOOT, "getLeftFoot" );

		flyerJointMap.put( UniversalJoint.UPPER_RIGHT_SHOULDER, "getRightWingShoulder" );
		flyerJointMap.put( UniversalJoint.UPPER_RIGHT_ELBOW, "getRightWingElbow" );
		flyerJointMap.put( UniversalJoint.UPPER_RIGHT_WRIST, "getRightWingWrist" );

		flyerJointMap.put( UniversalJoint.UPPER_LEFT_SHOULDER, "getLeftWingShoulder" );
		flyerJointMap.put( UniversalJoint.UPPER_LEFT_ELBOW, "getLeftWingElbow" );
		flyerJointMap.put( UniversalJoint.UPPER_LEFT_WRIST, "getLeftWingWrist" );

		flyerJointMap.put( UniversalJoint.TAIL, "getTail" );
		flyerJointMap.put( UniversalJoint.TAIL2, "getTail2" );
		flyerJointMap.put( UniversalJoint.TAIL3, "getTail3" );

	}

	private static void initializeReverseFlyerMap() {
		flyerReverseJointMap.put( "getPelvisLowerBody", UniversalJoint.PELVIS );
		flyerReverseJointMap.put( "getSpineBase", UniversalJoint.SPINE_BASE );
		flyerReverseJointMap.put( "getSpineMiddle", UniversalJoint.SPINE_MIDDLE );
		flyerReverseJointMap.put( "getSpineUpper", UniversalJoint.SPINE_UPPER );
		flyerReverseJointMap.put( "getNeck", UniversalJoint.NECK );
		flyerReverseJointMap.put( "getHead", UniversalJoint.HEAD );
		flyerReverseJointMap.put( "getMouth", UniversalJoint.MOUTH );
		flyerReverseJointMap.put( "getRightEye", UniversalJoint.RIGHT_EYE );
		flyerReverseJointMap.put( "getLeftEye", UniversalJoint.LEFT_EYE );
		flyerReverseJointMap.put( "getRightEyelid", UniversalJoint.RIGHT_EYELID );
		flyerReverseJointMap.put( "getLeftEyelid", UniversalJoint.LEFT_EYELID );

		flyerReverseJointMap.put( "getRightHip", UniversalJoint.LOWER_RIGHT_HIP );
		flyerReverseJointMap.put( "getRightKnee", UniversalJoint.LOWER_RIGHT_KNEE );
		flyerReverseJointMap.put( "getRightAnkle", UniversalJoint.LOWER_RIGHT_ANKLE );
		flyerReverseJointMap.put( "getRightFoot", UniversalJoint.LOWER_RIGHT_FOOT );

		flyerReverseJointMap.put( "getLeftHip", UniversalJoint.LOWER_LEFT_HIP );
		flyerReverseJointMap.put( "getLeftKnee", UniversalJoint.LOWER_LEFT_KNEE );
		flyerReverseJointMap.put( "getLeftAnkle", UniversalJoint.LOWER_LEFT_ANKLE );
		flyerReverseJointMap.put( "getLeftFoot", UniversalJoint.LOWER_LEFT_FOOT );

		flyerReverseJointMap.put( "getRightWingShoulder", UniversalJoint.UPPER_RIGHT_SHOULDER );
		flyerReverseJointMap.put( "getRightWingElbow", UniversalJoint.UPPER_RIGHT_ELBOW );
		flyerReverseJointMap.put( "getRightWingWrist", UniversalJoint.UPPER_RIGHT_WRIST );

		flyerReverseJointMap.put( "getLeftWingShoulder", UniversalJoint.UPPER_LEFT_SHOULDER );
		flyerReverseJointMap.put( "getLeftWingElbow", UniversalJoint.UPPER_LEFT_ELBOW );
		flyerReverseJointMap.put( "getLeftWingWrist", UniversalJoint.UPPER_LEFT_WRIST );

		flyerReverseJointMap.put( "getTail", UniversalJoint.TAIL );
		flyerReverseJointMap.put( "getTail2", UniversalJoint.TAIL2 );
		flyerReverseJointMap.put( "getTail3", UniversalJoint.TAIL3 );

	}

	private static void initializeQuadrupedMap() {
		quadrupedJointMap.put( UniversalJoint.PELVIS, "getPelvisLowerBody" );
		quadrupedJointMap.put( UniversalJoint.SPINE_BASE, "getSpineBase" );
		quadrupedJointMap.put( UniversalJoint.SPINE_MIDDLE, "getSpineMiddle" );
		quadrupedJointMap.put( UniversalJoint.SPINE_UPPER, "getSpineUpper" );
		quadrupedJointMap.put( UniversalJoint.NECK, "getNeck" );
		quadrupedJointMap.put( UniversalJoint.HEAD, "getHead" );
		quadrupedJointMap.put( UniversalJoint.MOUTH, "getMouth" );
		quadrupedJointMap.put( UniversalJoint.RIGHT_EYE, "getRightEye" );
		quadrupedJointMap.put( UniversalJoint.LEFT_EYE, "getLeftEye" );
		quadrupedJointMap.put( UniversalJoint.RIGHT_EAR, "getRightEar" );
		quadrupedJointMap.put( UniversalJoint.LEFT_EAR, "getLeftEar" );
		quadrupedJointMap.put( UniversalJoint.RIGHT_EYELID, "getRightEyelid" );
		quadrupedJointMap.put( UniversalJoint.LEFT_EYELID, "getLeftEyelid" );

		quadrupedJointMap.put( UniversalJoint.LOWER_RIGHT_HIP, "getBackRightHip" );
		quadrupedJointMap.put( UniversalJoint.LOWER_RIGHT_KNEE, "getBackRightKnee" );
		quadrupedJointMap.put( UniversalJoint.LOWER_RIGHT_ANKLE, "getBackRightAnkle" );
		quadrupedJointMap.put( UniversalJoint.LOWER_RIGHT_FOOT, "getBackRightFoot" );
		quadrupedJointMap.put( UniversalJoint.LOWER_RIGHT_HOCK, "getBackRightHock" );
		quadrupedJointMap.put( UniversalJoint.LOWER_RIGHT_TOE, "getBackRightToe" );

		quadrupedJointMap.put( UniversalJoint.LOWER_LEFT_HIP, "getBackLeftHip" );
		quadrupedJointMap.put( UniversalJoint.LOWER_LEFT_KNEE, "getBackLeftKnee" );
		quadrupedJointMap.put( UniversalJoint.LOWER_LEFT_ANKLE, "getBackLeftAnkle" );
		quadrupedJointMap.put( UniversalJoint.LOWER_LEFT_FOOT, "getBackLeftFoot" );
		quadrupedJointMap.put( UniversalJoint.LOWER_LEFT_HOCK, "getBackLeftHock" );
		quadrupedJointMap.put( UniversalJoint.LOWER_LEFT_TOE, "getBackLeftToe" );

		quadrupedJointMap.put( UniversalJoint.UPPER_RIGHT_CLAVICLE, "getFrontRightClavicle" );
		quadrupedJointMap.put( UniversalJoint.UPPER_RIGHT_SHOULDER, "getFrontRightShoulder" );
		quadrupedJointMap.put( UniversalJoint.UPPER_RIGHT_ELBOW, "getFrontRightKnee" );
		quadrupedJointMap.put( UniversalJoint.UPPER_RIGHT_WRIST, "getFrontRightAnkle" );
		quadrupedJointMap.put( UniversalJoint.UPPER_RIGHT_HAND, "getFrontRightFoot" );
		quadrupedJointMap.put( UniversalJoint.UPPER_RIGHT_HOCK, "getFrontRightHock" );
		quadrupedJointMap.put( UniversalJoint.UPPER_RIGHT_TOE, "getFrontRightToe" );

		quadrupedJointMap.put( UniversalJoint.UPPER_LEFT_CLAVICLE, "getFrontLeftClavicle" );
		quadrupedJointMap.put( UniversalJoint.UPPER_LEFT_SHOULDER, "getFrontLeftShoulder" );
		quadrupedJointMap.put( UniversalJoint.UPPER_LEFT_ELBOW, "getFrontLeftKnee" );
		quadrupedJointMap.put( UniversalJoint.UPPER_LEFT_WRIST, "getFrontLeftAnkle" );
		quadrupedJointMap.put( UniversalJoint.UPPER_LEFT_HAND, "getFrontLeftFoot" );
		quadrupedJointMap.put( UniversalJoint.UPPER_LEFT_HOCK, "getFrontLeftHock" );
		quadrupedJointMap.put( UniversalJoint.UPPER_LEFT_TOE, "getFrontLeftToe" );

		quadrupedJointMap.put( UniversalJoint.TAIL, "getTail" );
		quadrupedJointMap.put( UniversalJoint.TAIL2, "getTail2" );
		quadrupedJointMap.put( UniversalJoint.TAIL3, "getTail3" );
		quadrupedJointMap.put( UniversalJoint.TAIL4, "getTail4" );
	}

	private static void initializeReverseQuadrupedMap() {
		quadrupedReverseJointMap.put( "getPelvisLowerBody", UniversalJoint.PELVIS );
		quadrupedReverseJointMap.put( "getSpineBase", UniversalJoint.SPINE_BASE );
		quadrupedReverseJointMap.put( "getSpineMiddle", UniversalJoint.SPINE_MIDDLE );
		quadrupedReverseJointMap.put( "getSpineUpper", UniversalJoint.SPINE_UPPER );
		quadrupedReverseJointMap.put( "getNeck", UniversalJoint.NECK );
		quadrupedReverseJointMap.put( "getHead", UniversalJoint.HEAD );
		quadrupedReverseJointMap.put( "getMouth", UniversalJoint.MOUTH );
		quadrupedReverseJointMap.put( "getRightEye", UniversalJoint.RIGHT_EYE );
		quadrupedReverseJointMap.put( "getLeftEye", UniversalJoint.LEFT_EYE );
		quadrupedReverseJointMap.put( "getRightEar", UniversalJoint.RIGHT_EAR );
		quadrupedReverseJointMap.put( "getLeftEar", UniversalJoint.LEFT_EAR );
		quadrupedReverseJointMap.put( "getRightEyelid", UniversalJoint.RIGHT_EYELID );
		quadrupedReverseJointMap.put( "getLeftEyelid", UniversalJoint.LEFT_EYELID );

		quadrupedReverseJointMap.put( "getBackRightHip", UniversalJoint.LOWER_RIGHT_HIP );
		quadrupedReverseJointMap.put( "getBackRightKnee", UniversalJoint.LOWER_RIGHT_KNEE );
		quadrupedReverseJointMap.put( "getBackRightAnkle", UniversalJoint.LOWER_RIGHT_ANKLE );
		quadrupedReverseJointMap.put( "getBackRightFoot", UniversalJoint.LOWER_RIGHT_FOOT );
		quadrupedReverseJointMap.put( "getBackRightHock", UniversalJoint.LOWER_RIGHT_HOCK );
		quadrupedReverseJointMap.put( "getBackRightToe", UniversalJoint.LOWER_RIGHT_TOE );

		quadrupedReverseJointMap.put( "getBackLeftHip", UniversalJoint.LOWER_LEFT_HIP );
		quadrupedReverseJointMap.put( "getBackLeftKnee", UniversalJoint.LOWER_LEFT_KNEE );
		quadrupedReverseJointMap.put( "getBackLeftAnkle", UniversalJoint.LOWER_LEFT_ANKLE );
		quadrupedReverseJointMap.put( "getBackLeftFoot", UniversalJoint.LOWER_LEFT_FOOT );
		quadrupedReverseJointMap.put( "getBackLeftHock", UniversalJoint.LOWER_LEFT_HOCK );
		quadrupedReverseJointMap.put( "getBackLeftToe", UniversalJoint.LOWER_LEFT_TOE );

		quadrupedReverseJointMap.put( "getFrontRightClavicle", UniversalJoint.UPPER_RIGHT_CLAVICLE );
		quadrupedReverseJointMap.put( "getFrontRightShoulder", UniversalJoint.UPPER_RIGHT_SHOULDER );
		quadrupedReverseJointMap.put( "getFrontRightKnee", UniversalJoint.UPPER_RIGHT_ELBOW );
		quadrupedReverseJointMap.put( "getFrontRightAnkle", UniversalJoint.UPPER_RIGHT_WRIST );
		quadrupedReverseJointMap.put( "getFrontRightFoot", UniversalJoint.UPPER_RIGHT_HAND );
		quadrupedReverseJointMap.put( "getFrontRightHock", UniversalJoint.UPPER_RIGHT_HOCK );
		quadrupedReverseJointMap.put( "getFrontRightToe", UniversalJoint.UPPER_RIGHT_TOE );

		quadrupedReverseJointMap.put( "getFrontLeftClavicle", UniversalJoint.UPPER_LEFT_CLAVICLE );
		quadrupedReverseJointMap.put( "getFrontLeftShoulder", UniversalJoint.UPPER_LEFT_SHOULDER );
		quadrupedReverseJointMap.put( "getFrontLeftKnee", UniversalJoint.UPPER_LEFT_ELBOW );
		quadrupedReverseJointMap.put( "getFrontLeftAnkle", UniversalJoint.UPPER_LEFT_WRIST );
		quadrupedReverseJointMap.put( "getFrontLeftFoot", UniversalJoint.UPPER_LEFT_HAND );
		quadrupedReverseJointMap.put( "getFrontLeftHock", UniversalJoint.UPPER_LEFT_HOCK );
		quadrupedReverseJointMap.put( "getFrontLeftToe", UniversalJoint.UPPER_LEFT_TOE );

		quadrupedReverseJointMap.put( "getTail", UniversalJoint.TAIL );
		quadrupedReverseJointMap.put( "getTail2", UniversalJoint.TAIL2 );
		quadrupedReverseJointMap.put( "getTail3", UniversalJoint.TAIL3 );
		quadrupedReverseJointMap.put( "getTail4", UniversalJoint.TAIL4 );
	}

	private static void initializeSwimmerMap() {
		fishJointMap.put( UniversalJoint.SPINE_BASE, "getSpineBase" );
		fishJointMap.put( UniversalJoint.SPINE_MIDDLE, "getSpineMiddle" );
		fishJointMap.put( UniversalJoint.NECK, "getNeck" );
		fishJointMap.put( UniversalJoint.HEAD, "getHead" );
		fishJointMap.put( UniversalJoint.MOUTH, "getMouth" );
		fishJointMap.put( UniversalJoint.RIGHT_EYE, "getRightEye" );
		fishJointMap.put( UniversalJoint.LEFT_EYE, "getLeftEye" );
		fishJointMap.put( UniversalJoint.RIGHT_EYELID, "getRightEyelid" );
		fishJointMap.put( UniversalJoint.LEFT_EYELID, "getLeftEyelid" );

		fishJointMap.put( UniversalJoint.UPPER_RIGHT_SHOULDER, "getFrontRightFin" );
		fishJointMap.put( UniversalJoint.UPPER_LEFT_SHOULDER, "getFrontLeftFin" );
		fishJointMap.put( UniversalJoint.TAIL, "getTail" );

	}

	private static void initializeReverseSwimmerMap() {
		fishReverseJointMap.put( "getSpineBase", UniversalJoint.SPINE_BASE );
		fishReverseJointMap.put( "getSpineMiddle", UniversalJoint.SPINE_MIDDLE );
		fishReverseJointMap.put( "getNeck", UniversalJoint.NECK );
		fishReverseJointMap.put( "getHead", UniversalJoint.HEAD );
		fishReverseJointMap.put( "getMouth", UniversalJoint.MOUTH );
		fishReverseJointMap.put( "getRightEye", UniversalJoint.RIGHT_EYE );
		fishReverseJointMap.put( "getLeftEye", UniversalJoint.LEFT_EYE );
		fishReverseJointMap.put( "getRightEyelid", UniversalJoint.RIGHT_EYELID );
		fishReverseJointMap.put( "getLeftEyelid", UniversalJoint.LEFT_EYELID );

		fishReverseJointMap.put( "getFrontRightFin", UniversalJoint.UPPER_RIGHT_SHOULDER );
		fishReverseJointMap.put( "getFrontLeftFin", UniversalJoint.UPPER_LEFT_SHOULDER );
		fishReverseJointMap.put( "getTail", UniversalJoint.TAIL );

	}

	public static boolean jointSubstitutionExists( AbstractType<?, ?, ?> typeForOldJoint, AbstractMethod method, AbstractType<?, ?, ?> typeForNewJoint ) {
		// initialize the maps, if necessary
		if( bipedJointMap.entrySet().size() == 0 ) {
			initializeBipedMap();
			initializeReverseBipedMap();

			initializeFlyerMap();
			initializeReverseFlyerMap();

			initializeQuadrupedMap();
			initializeReverseQuadrupedMap();

			initializeSwimmerMap();
			initializeReverseSwimmerMap();
		}

		String getJointName = method.getName();

		UniversalJoint universalJoint = null;
		if( typeForOldJoint.isAssignableTo( SBiped.class ) ) {
			universalJoint = bipedReverseJointMap.get( getJointName );
		} else if( typeForOldJoint.isAssignableTo( SFlyer.class ) ) {
			universalJoint = flyerReverseJointMap.get( getJointName );
		} else if( typeForOldJoint.isAssignableTo( SQuadruped.class ) ) {
			universalJoint = quadrupedReverseJointMap.get( getJointName );
		} else if( typeForOldJoint.isAssignableTo( SSwimmer.class ) ) {
			universalJoint = fishReverseJointMap.get( getJointName );
		}

		if( universalJoint == null ) {
			//pass
		} else {
			String newMethodName = null;
			if( typeForNewJoint.isAssignableTo( SBiped.class ) ) {
				newMethodName = bipedJointMap.get( universalJoint );
			} else if( typeForNewJoint.isAssignableTo( SFlyer.class ) ) {
				newMethodName = flyerJointMap.get( universalJoint );
			} else if( typeForNewJoint.isAssignableTo( SQuadruped.class ) ) {
				newMethodName = quadrupedJointMap.get( universalJoint );
			} else if( typeForNewJoint.isAssignableTo( SSwimmer.class ) ) {
				newMethodName = fishJointMap.get( universalJoint );
			}

			if( newMethodName != null ) {
				return true;
			}
		}

		return false;
	}

	public static AbstractMethod findJointSubstitutionFor( AbstractType<?, ?, ?> typeForOldJoint, MethodInvocation oldGetJointMethodInvocation, AbstractType<?, ?, ?> typeForNewJoint ) {
		// initialize the maps, if necessary
		if( bipedJointMap.entrySet().size() == 0 ) {
			initializeBipedMap();
			initializeReverseBipedMap();

			initializeFlyerMap();
			initializeReverseFlyerMap();

			initializeQuadrupedMap();
			initializeReverseQuadrupedMap();

			initializeSwimmerMap();
			initializeReverseSwimmerMap();
		}

		String getJointName = oldGetJointMethodInvocation.method.getValue().getName();

		UniversalJoint universalJoint = null;
		if( typeForOldJoint.isAssignableTo( SBiped.class ) ) {
			universalJoint = bipedReverseJointMap.get( getJointName );
		} else if( typeForOldJoint.isAssignableTo( SFlyer.class ) ) {
			universalJoint = flyerReverseJointMap.get( getJointName );
		} else if( typeForOldJoint.isAssignableTo( SQuadruped.class ) ) {
			universalJoint = quadrupedReverseJointMap.get( getJointName );
		} else if( typeForOldJoint.isAssignableTo( SSwimmer.class ) ) {
			universalJoint = fishReverseJointMap.get( getJointName );
		}

		if( universalJoint == null ) {
			return null;
		} else {
			String newMethodName = null;
			if( typeForNewJoint.isAssignableTo( SBiped.class ) ) {
				newMethodName = bipedJointMap.get( universalJoint );
			} else if( typeForNewJoint.isAssignableTo( SFlyer.class ) ) {
				newMethodName = flyerJointMap.get( universalJoint );
			} else if( typeForNewJoint.isAssignableTo( SQuadruped.class ) ) {
				newMethodName = quadrupedJointMap.get( universalJoint );
			} else if( typeForNewJoint.isAssignableTo( SSwimmer.class ) ) {
				newMethodName = fishJointMap.get( universalJoint );
			}

			if( newMethodName != null ) {
				AbstractMethod newMethod = typeForNewJoint.getDeclaredMethod( newMethodName );

				if( newMethod == null ) {
					newMethod = typeForNewJoint.getFirstEncounteredJavaType().getDeclaredMethod( newMethodName );
				}
				return newMethod;
			}
			return null;
		}
	}

}
