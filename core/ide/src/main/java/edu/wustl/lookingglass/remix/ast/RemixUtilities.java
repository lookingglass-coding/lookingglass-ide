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

import org.alice.nonfree.NebulousIde;
import org.alice.stageide.ast.JointMethodUtilities;
import org.alice.stageide.ast.StoryApiSpecificAstUtilities;
import org.alice.stageide.sceneeditor.SetUpMethodGenerator;
import org.lgna.project.Project;
import org.lgna.project.ast.AbstractField;
import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractParameter;
import org.lgna.project.ast.AbstractType;
import org.lgna.project.ast.Expression;
import org.lgna.project.ast.FieldAccess;
import org.lgna.project.ast.JavaType;
import org.lgna.project.ast.MethodInvocation;
import org.lgna.project.ast.NamedUserType;
import org.lgna.project.ast.SimpleArgument;
import org.lgna.project.ast.ThisExpression;
import org.lgna.project.ast.UserField;
import org.lgna.project.ast.UserMethod;
import org.lgna.project.virtualmachine.UserInstance;
import org.lgna.story.EmployeesOnly;
import org.lgna.story.SJoint;
import org.lgna.story.SMarker;
import org.lgna.story.SModel;
import org.lgna.story.SThing;
import org.lgna.story.implementation.EntityImp;

import edu.cmu.cs.dennisc.math.AffineMatrix4x4;
import edu.cmu.cs.dennisc.math.ForwardAndUpGuide;
import edu.cmu.cs.dennisc.math.OrthogonalMatrix3x3;
import edu.cmu.cs.dennisc.math.Point3;
import edu.cmu.cs.dennisc.math.Vector3;
import edu.cmu.cs.dennisc.scenegraph.Composite;
import edu.cmu.cs.dennisc.scenegraph.Joint;
import edu.wustl.lookingglass.remix.Role;
import edu.wustl.lookingglass.remix.SnippetScript;
import edu.wustl.lookingglass.remix.models.EndCaptureState;
import edu.wustl.lookingglass.remix.models.StartCaptureState;
import edu.wustl.lookingglass.scenegraph.recorder.RecorderManager;
import edu.wustl.lookingglass.scenegraph.recorder.VehicleChangeRecorder;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ContainerEventNode;

/**
 * @author Michael Pogran
 */
public class RemixUtilities {

	public static final String SCRIPT_METHOD_NAME = "scriptRun";
	public static final String SCRIPT_TYPE_NAME = "ScriptType";
	public static final String SCRIPT_PACKAGE_NAME = "edu.wustl.lookingglass.script";

	public static boolean isMethodUserAuthoredMethod( AbstractMethod method, NamedUserType programType ) {
		boolean isUserMethod = method instanceof UserMethod;
		boolean isPublic = method.isPublicAccess();
		boolean isNotJointGetter = !( JointMethodUtilities.isJointGetter( method ) );
		boolean isNotMain = !( isUserMain( method, programType ) );
		boolean isNotResourceSetter = !( method.getName().startsWith( "set" ) && method.getName().endsWith( "Resource" ) );

		return isUserMethod && isPublic && isNotJointGetter && isNotMain && isNotResourceSetter;
	}

	private static boolean isUserMain( AbstractMethod method, NamedUserType programType ) {
		return StoryApiSpecificAstUtilities.getUserMain( programType ).getName().contentEquals( method.getName() );
	}

	public static String getValidMemberName( org.alice.ide.name.validators.MemberNameValidator validator, String baseName ) {
		String testName = baseName;
		int suffix = 0;

		assert validator.isNameValid( testName ) : testName;

		if( validator.isNameAvailable( testName ) ) {
			return testName;
		}

		String pattern = "([\\w\\s]*)";
		java.util.regex.Pattern p = java.util.regex.Pattern.compile( pattern );
		java.util.regex.Matcher m = p.matcher( baseName );
		if( m.find() ) {
			baseName = m.group( 1 );
			baseName = baseName.trim();
		}

		while( !validator.isNameAvailable( testName ) ) {
			++suffix;
			testName = baseName + " (" + suffix + ")";
		}

		return testName;
	}

	public static boolean isInRemix( org.lgna.project.ast.AbstractNode astNode ) {
		AbstractEventNode<?> startEventNode = StartCaptureState.getInstance().getValue();
		AbstractEventNode<?> endEventNode = EndCaptureState.getInstance().getValue();

		if( ( startEventNode != null ) && ( endEventNode != null ) ) {
			java.util.List<AbstractEventNode<?>> nodes = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();
			if( startEventNode.equals( endEventNode ) ) {
				nodes.add( startEventNode );

				if( startEventNode instanceof ContainerEventNode ) {
					edu.wustl.lookingglass.virtualmachine.eventtracing.EventNodeUtilities.appendChildren( (ContainerEventNode<?>)startEventNode, nodes );
				}
			} else {
				nodes.addAll( edu.wustl.lookingglass.virtualmachine.eventtracing.EventNodeUtilities.getNodesBetween( startEventNode, endEventNode, false ) );
			}

			if( nodes != null ) {
				for( AbstractEventNode<?> eventNode : nodes ) {
					if( eventNode.getAstNode().equals( astNode ) ) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isInRemix( AbstractEventNode<?> eventNode ) {
		AbstractEventNode<?> startEventNode = StartCaptureState.getInstance().getValue();
		AbstractEventNode<?> endEventNode = EndCaptureState.getInstance().getValue();

		if( ( startEventNode != null ) && ( endEventNode != null ) ) {
			java.util.List<AbstractEventNode<?>> nodes = edu.wustl.lookingglass.virtualmachine.eventtracing.EventNodeUtilities.getNodesBetween( startEventNode, endEventNode, false );
			return nodes.contains( eventNode );
		}
		return false;
	}

	public static boolean isCamera( org.lgna.project.ast.AbstractType<?, ?, ?> type ) {
		return type.isAssignableTo( org.lgna.story.SCamera.class );
	}

	public static boolean isGround( org.lgna.project.ast.AbstractType<?, ?, ?> type ) {
		return type.isAssignableTo( org.lgna.story.SGround.class );
	}

	public static boolean isRoom( org.lgna.project.ast.AbstractType<?, ?, ?> type ) {
		return NebulousIde.nonfree.isAssignableToSRoom( type );
	}

	public static boolean isScene( org.lgna.project.ast.AbstractType<?, ?, ?> type ) {
		return type.isAssignableTo( org.lgna.story.SScene.class ) || type.isAssignableTo( edu.cmu.cs.dennisc.scenegraph.Scene.class );
	}

	public static boolean isSpecialField( AbstractField field ) {
		AbstractType<?, ?, ?> fieldType = field.getValueType();
		return isCamera( fieldType ) || isGround( fieldType ) || isRoom( fieldType ) || isScene( fieldType );
	}

	public static boolean isActiveField( AbstractField field ) {
		return field.getValueType().isAssignableTo( JavaType.getInstance( SModel.class ) ) || field.getValueType().isAssignableTo( JavaType.getInstance( SMarker.class ) );
	}

	public static boolean isStatementInvokingMethod( org.lgna.project.ast.Statement statement ) {
		if( statement instanceof org.lgna.project.ast.ExpressionStatement ) {
			return ( (org.lgna.project.ast.ExpressionStatement)statement ).expression.getValue() instanceof org.lgna.project.ast.MethodInvocation;
		} else {
			return false;
		}
	}

	public static boolean isGetJointInvocation( org.lgna.project.ast.Expression expression ) {
		if( expression instanceof org.lgna.project.ast.MethodInvocation ) {
			org.lgna.project.ast.AbstractMethod method = ( (org.lgna.project.ast.MethodInvocation)expression ).method.getValue();
			return org.alice.stageide.ast.JointMethodUtilities.isJointGetter( method );
		} else {
			return false;
		}
	}

	public static boolean checkSnippetForCompatability( SnippetScript snippetScript, Project project ) {
		boolean containsRoomReference = false;
		boolean containsGroundReference = false;
		for( AbstractMethod method : snippetScript.getFieldsForMethods().keySet() ) {
			if( method.getDeclaringType() != null ) {
				if( NebulousIde.nonfree.isAssignableToSRoom( method.getDeclaringType() ) ) {
					containsRoomReference = true;
				} else if( method.getDeclaringType().isAssignableTo( org.lgna.story.SGround.class ) ) {
					containsGroundReference = true;
				}
			}
		}

		NamedUserType sceneType = org.alice.stageide.ast.StoryApiSpecificAstUtilities.getSceneTypeFromProject( project );
		if( containsRoomReference ) {
			boolean projectHasRoom = false;
			for( UserField field : sceneType.getDeclaredFields() ) {
				if( RemixUtilities.isRoom( field.getValueType() ) ) {
					projectHasRoom = true;
					break;
				}
			}
			return projectHasRoom;
		}
		if( containsGroundReference ) {
			boolean projectHasGround = false;
			for( UserField field : sceneType.getDeclaredFields() ) {
				if( RemixUtilities.isGround( field.getValueType() ) ) {
					projectHasGround = true;
					break;
				}
			}
			return projectHasGround;
		}
		return true;
	}

	public static String getSnippetIncompatabilityReason( SnippetScript snippetScript, Project project ) {
		boolean containsRoomReference = false;
		boolean containsGroundReference = false;
		for( AbstractMethod method : snippetScript.getFieldsForMethods().keySet() ) {
			if( method.getDeclaringType() != null ) {
				if( NebulousIde.nonfree.isAssignableToSRoom( method.getDeclaringType() ) ) {
					containsRoomReference = true;
				} else if( method.getDeclaringType().isAssignableTo( org.lgna.story.SGround.class ) ) {
					containsGroundReference = true;
				}
			}
		}

		if( containsRoomReference ) {
			return "In order to use this remix, try remixing into a world that uses the room template.";
		}
		if( containsGroundReference ) {
			return "In order to use this remix, try remixing into a world that does not use the room template.";
		}
		return "";
	}

	public static Composite getEarlierVehicleChange( AbstractField field, Object fieldInstance, double time ) {
		if( fieldInstance instanceof UserInstance ) {
			fieldInstance = ( (UserInstance)fieldInstance ).getJavaInstance();
		}

		if( fieldInstance instanceof SThing ) {
			SThing entity = (SThing)fieldInstance;
			EntityImp entityImp = EmployeesOnly.getImplementation( entity );
			Composite sgComposite = entityImp.getSgComposite();

			VehicleChangeRecorder vehicleChangeRecorder = RecorderManager.getInstance().getVehicleRecorder();

			return vehicleChangeRecorder.getVehicleAtTime( sgComposite, time );
		}
		return null;
	}

	public static Composite getParentForJoint( Composite joint ) {
		Composite rv = joint;
		while( rv != null ) {
			rv = rv.getParent();

			if( rv instanceof Joint ) {
				// pass
			} else {
				break;
			}
		}
		return rv;
	}

	public static MethodInvocation createSetVehicleInvocation( AbstractField field, Composite vehicle, UserInstance sceneInstance ) {
		AbstractType<?, ?, ?> mutableRiderType = JavaType.getInstance( org.lgna.story.MutableRider.class );
		AbstractType<?, ?, ?> parameterTypes[] = { JavaType.getInstance( org.lgna.story.SThing.class ) };
		AbstractMethod setVehicleMethod = mutableRiderType.findMethod( "setVehicle", parameterTypes );
		AbstractParameter vehicleParameter = setVehicleMethod.getRequiredParameters().get( 0 );

		Expression getVehicleExpression = null;
		if( vehicle instanceof Joint ) {
			SJoint joint = (SJoint)EntityImp.getAbstractionFromSgElement( vehicle );
			getVehicleExpression = SetUpMethodGenerator.getGetterExpressionForJoint( joint, sceneInstance );
		} else {
			AbstractField vehicleField = sceneInstance.ACCEPTABLE_HACK_FOR_SCENE_EDITOR_getFieldForInstanceInJava( EntityImp.getAbstractionFromSgElement( vehicle ) );
			getVehicleExpression = new FieldAccess( new ThisExpression(), vehicleField );
		}
		return new MethodInvocation( new FieldAccess( new ThisExpression(), field ), setVehicleMethod, new SimpleArgument( vehicleParameter, getVehicleExpression ) );

	}

	public static AffineMatrix4x4 getInitialTransform( Role role, UserInstance sceneInstance ) {

		if( ( role.getInitialTransformation() != null ) && ( role.getInitialCameraRelativeTransformation() != null ) ) {

			org.lgna.story.SCamera cameraInstance = null;
			for( org.lgna.project.ast.UserField field : sceneInstance.getType().getDeclaredFields() ) {
				if( isCamera( field.getValueType() ) ) {
					cameraInstance = (org.lgna.story.SCamera)sceneInstance.getFieldValue( field );
					break;
				}
			}

			AffineMatrix4x4 cameraTransformation = org.lgna.story.EmployeesOnly.getImplementation( cameraInstance ).getAbsoluteTransformation();

			// Use back projection to project the previous camera relative point to the same position in absolute scene space
			Point3 objectTranslation = role.getInitialCameraRelativeTransformation().translation;

			// Scaling for back projection
			Vector3 scaledRight = new Vector3();
			Vector3 scaledUp = new Vector3();
			Vector3 scaledBackward = new Vector3();

			Vector3.setReturnValueToMultiplication( scaledRight, cameraTransformation.orientation.right, objectTranslation.x );
			Vector3.setReturnValueToMultiplication( scaledUp, cameraTransformation.orientation.up, objectTranslation.y );
			Vector3.setReturnValueToMultiplication( scaledBackward, cameraTransformation.orientation.backward, objectTranslation.z );

			// Sum scaled vectors
			Vector3 objectPositionVectorInAbsoluteSpace = new Vector3();
			objectPositionVectorInAbsoluteSpace.add( scaledRight );
			objectPositionVectorInAbsoluteSpace.add( scaledUp );
			objectPositionVectorInAbsoluteSpace.add( scaledBackward );

			// Shift from origin by current location of the camera
			objectPositionVectorInAbsoluteSpace.add( cameraTransformation.translation );

			// Set the y to negate any differences in camera height
			objectPositionVectorInAbsoluteSpace.y = role.getInitialTransformation().translation.y;

			/*
			 * Multiply through to get the orientation relative to the camera in the new scene.
			 *
			 * note: This is problematic though because if the camera is turned forward or rolled then your object will do the same
			 * thing to appear in the same orientation to the new camera as the old. We want our object to face the same direction
			 * from a yaw perspective, but preserve the pitch and roll so they don't bend at some weird angle above the ground if
			 * they didn't do that in the original scene.
			 */

			AffineMatrix4x4 cameraRelativeOrientation = AffineMatrix4x4.createMultiplication( cameraTransformation, role.getInitialCameraRelativeTransformation() );
			ForwardAndUpGuide cameraGuide = cameraTransformation.orientation.createForwardAndUpGuide();

			cameraRelativeOrientation = AffineMatrix4x4.setReturnValueToMultiplication( new AffineMatrix4x4(), role.getInitialCameraRelativeTransformation(), AffineMatrix4x4.setReturnValueToInverse( new AffineMatrix4x4(), role.getInitialTransformation() ) );

			Vector3 cameraUp = org.alice.interact.VectorUtilities.projectOntoVector( cameraGuide.upGuide, Vector3.accessPositiveYAxis() );
			Vector3 xzPlaneNormal = Vector3.createCrossProduct( Vector3.accessPositiveXAxis(), Vector3.accessPositiveZAxis() );
			Vector3 projectedCameraForward = (Vector3)Vector3.setReturnValueToSubtraction( Vector3.createZero(), cameraGuide.forward, org.alice.interact.VectorUtilities.projectOntoVector( cameraGuide.forward, xzPlaneNormal ) );

			OrthogonalMatrix3x3 yawOnlyCameraOrientation = new OrthogonalMatrix3x3( new ForwardAndUpGuide( projectedCameraForward, cameraUp ) );
			AffineMatrix4x4 am = new AffineMatrix4x4( yawOnlyCameraOrientation, cameraTransformation.translation );
			cameraRelativeOrientation = AffineMatrix4x4.createMultiplication( am, role.getInitialCameraRelativeTransformation() );

			return new AffineMatrix4x4( cameraRelativeOrientation.orientation, new Point3( objectPositionVectorInAbsoluteSpace ) );
		}

		return null;
	}
}
