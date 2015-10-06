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
package edu.wustl.lookingglass.lgpedia;

import java.awt.Component;

import javax.swing.SwingUtilities;

import org.alice.ide.ast.ExpressionCreator.CannotCreateExpressionException;
import org.alice.ide.instancefactory.InstanceFactory;
import org.alice.nonfree.NebulousIde;
import org.lgna.croquet.SimpleOperationUnadornedDialogCoreComposite;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;
import org.lgna.project.ast.ManagementLevel;
import org.lgna.project.virtualmachine.events.ExpressionEvaluationEvent;
import org.lgna.project.virtualmachine.events.MethodInvocationEvent;
import org.lgna.project.virtualmachine.events.StatementExecutionEvent;
import org.lgna.story.Color;
import org.lgna.story.Paint;
import org.lgna.story.SScene;

import edu.cmu.cs.dennisc.math.AffineMatrix4x4;
import edu.cmu.cs.dennisc.math.Dimension3;
import edu.cmu.cs.dennisc.math.OrthogonalMatrix3x3;
import edu.cmu.cs.dennisc.math.Point3;
import edu.wustl.lookingglass.ide.program.RunProgramAsPreviewContext;
import edu.wustl.lookingglass.lgpedia.views.LgpediaView;

/**
 * @author Michael Pogran
 */
public class LgpediaComposite extends SimpleOperationUnadornedDialogCoreComposite<edu.wustl.lookingglass.lgpedia.views.LgpediaView> {

	public static enum ConstructType {
		PROCEDURE,
		FUNCTION,
		CONTROL_FLOW,
	}

	private static final LgpediaComposite singleton = new LgpediaComposite();

	public static LgpediaComposite getInstance() {
		return singleton;
	}

	private final java.util.List<org.lgna.project.ast.AbstractMethod> factoryMethods;
	private edu.cmu.cs.dennisc.java.util.DStack<org.lgna.croquet.Operation> executingOperationStack;
	private int methodIndex;
	private org.lgna.project.Project currentUserProject;

	private LgpediaExpressionCreator expressionGenerator;
	private org.lgna.project.Project previewProject;
	private org.lgna.project.ast.UserField hareField;
	private org.lgna.project.ast.UserField tigerField;
	private org.lgna.project.ast.UserField treasureChestField;

	private RunProgramAsPreviewContext programContext;
	private java.util.HashMap<String, Object> propertyValueMap;
	private java.util.HashMap<String, Object> overrideValueMap;
	private org.lgna.project.ast.UserField activeField;

	private final CancelOperation cancelOperation;
	private final ChangeMethodOperation nextOperation;
	private final ChangeMethodOperation previousOperation;

	private LgpediaComposite() {
		super( java.util.UUID.fromString( "582b852b-99de-49af-a97d-b446d1bb1cb8" ), org.lgna.croquet.Application.APPLICATION_UI_GROUP );
		this.factoryMethods = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();
		this.executingOperationStack = edu.cmu.cs.dennisc.java.util.Stacks.newStack();
		this.cancelOperation = new CancelOperation();
		this.nextOperation = new ChangeMethodOperation( findLocalizedText( "NextOperation" ) );
		this.previousOperation = new ChangeMethodOperation( findLocalizedText( "PreviousOperation" ) );
		this.methodIndex = -1;

		org.alice.ide.IDE.getActiveInstance().getDocumentFrame().getInstanceFactoryState().addAndInvokeNewSchoolValueListener( ( ValueEvent<InstanceFactory> e ) -> {
			this.factoryMethods.clear();
			org.alice.ide.member.ProcedureTabComposite composite = org.alice.ide.members.MembersComposite.getInstance().getProcedureTabComposite();
			for( org.alice.ide.member.MethodsSubComposite subComposite : composite.getSubComposites() ) {
				if( ( subComposite != null ) && subComposite.isRelevant() && subComposite.isShowingDesired() ) {
					for( org.lgna.project.ast.AbstractMethod method : subComposite.getMethods() ) {
						if( method instanceof org.lgna.project.ast.JavaMethod ) {
							this.factoryMethods.add( method );
						}
					}
				}
			}
		} );
	}

	public void setAstNode( org.lgna.project.ast.AbstractNode astNode, boolean showDetails ) {
		// Modify view
		ConstructType type;
		if( astNode instanceof org.lgna.project.ast.AbstractMethod ) {
			type = ( (org.lgna.project.ast.AbstractMethod)astNode ).isProcedure() ? ConstructType.PROCEDURE : ConstructType.FUNCTION;
		} else {
			type = ConstructType.CONTROL_FLOW;
		}
		setBackgroundColorType( type );
		getView().resetStatementsPanel();

		if( astNode instanceof org.lgna.project.ast.AbstractMethod ) {
			org.lgna.project.ast.AbstractMethod method = (org.lgna.project.ast.AbstractMethod)astNode;
			getView().setMethodName( method );
			getView().setDetailsVisible( showDetails );

			// Set next and previous buttons
			this.methodIndex = factoryMethods.indexOf( method );
			if( this.methodIndex < ( factoryMethods.size() - 1 ) ) {
				this.nextOperation.setEnabled( true );
				org.lgna.project.ast.AbstractMethod nextMethod = this.factoryMethods.get( this.methodIndex + 1 );
				this.nextOperation.setMethod( nextMethod );
			} else {
				this.nextOperation.setEnabled( false );
				//this.nextOperation.setName( "None" );
			}
			if( this.methodIndex > 0 ) {
				this.previousOperation.setEnabled( true );
				org.lgna.project.ast.AbstractMethod previousMethod = this.factoryMethods.get( this.methodIndex - 1 );
				this.previousOperation.setMethod( previousMethod );
			} else {
				this.previousOperation.setEnabled( false );
				//this.previousOperation.setName( "None" );
			}

			// Set active field for method
			if( method.getDeclaringType().isAssignableTo( org.lgna.story.STurnable.class ) ) {
				org.lgna.project.ast.AbstractType<?, ?, ?> selectedType = org.alice.ide.IDE.getActiveInstance().getDocumentFrame().getInstanceFactoryState().getValue().getValueType();
				if( selectedType.isAssignableTo( org.lgna.story.SCamera.class ) ) {
					this.activeField = org.alice.stageide.ast.StoryApiSpecificAstUtilities.getFirstFieldWithType( this.previewProject.getProgramType(), org.lgna.story.SCamera.class );
				} else {
					this.activeField = this.hareField;
				}
			} else {
				this.activeField = org.alice.stageide.ast.StoryApiSpecificAstUtilities.getFirstFieldWithType( this.previewProject.getProgramType(), method.getDeclaringType().getFirstEncounteredJavaType() );
			}

			// Add methods to view
			handleMethodChange( method );

			// Special case orient methods
			this.overrideValueMap = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();
			if( method.getName().toLowerCase().contains( "orient" ) || method.getName().contentEquals( "pointAt" ) ) {
				edu.cmu.cs.dennisc.math.AngleInDegrees pitch;
				edu.cmu.cs.dennisc.math.AngleInDegrees yaw;
				edu.cmu.cs.dennisc.math.AngleInDegrees roll;
				if( activeField.getValueType().isAssignableTo( org.lgna.story.SCamera.class ) ) {
					pitch = new edu.cmu.cs.dennisc.math.AngleInDegrees( 00.0 );
					yaw = new edu.cmu.cs.dennisc.math.AngleInDegrees( 180.0 );
					roll = new edu.cmu.cs.dennisc.math.AngleInDegrees( 45.0 );
				} else {
					pitch = new edu.cmu.cs.dennisc.math.AngleInDegrees( -60.0 );
					yaw = new edu.cmu.cs.dennisc.math.AngleInDegrees( 90.0 );
					roll = new edu.cmu.cs.dennisc.math.AngleInDegrees( 00.0 );
				}

				this.overrideValueMap.put( this.activeField.getName() + ".localOrientation", new OrthogonalMatrix3x3( new edu.cmu.cs.dennisc.math.EulerAngles( pitch, yaw, roll, edu.cmu.cs.dennisc.math.EulerAngles.Order.PITCH_YAW_ROLL ) ) );
			}

			// Special case appear method
			if( method.getName().contentEquals( "appear" ) ) {
				this.overrideValueMap.put( this.activeField.getName() + ".opacity", 0.0f );
			}

			// Special case straighten out joints method
			if( method.getName().contentEquals( "straightenOutJoints" ) ) {
				edu.cmu.cs.dennisc.math.AngleInDegrees pitch = new edu.cmu.cs.dennisc.math.AngleInDegrees( 30.0 );
				edu.cmu.cs.dennisc.math.AngleInDegrees yaw = new edu.cmu.cs.dennisc.math.AngleInDegrees( 00.0 );
				edu.cmu.cs.dennisc.math.AngleInDegrees roll = new edu.cmu.cs.dennisc.math.AngleInDegrees( 00.0 );

				// Note: translation values are defaults for their respective joints
				this.overrideValueMap.put( this.activeField.getName() + ".RIGHT_SHOULDER", new AffineMatrix4x4( new edu.cmu.cs.dennisc.math.EulerAngles( pitch, yaw, roll, edu.cmu.cs.dennisc.math.EulerAngles.Order.PITCH_YAW_ROLL ), new Point3( 5.329070399086743E-17, -2.8421708795129297E-16, -0.06958700716495514 ) ) );
				this.overrideValueMap.put( this.activeField.getName() + ".LEFT_SHOULDER", new AffineMatrix4x4( new edu.cmu.cs.dennisc.math.EulerAngles( pitch, yaw, roll, edu.cmu.cs.dennisc.math.EulerAngles.Order.PITCH_YAW_ROLL ), new Point3( -4.884981281880909E-17, -1.4210854397564648E-16, -0.06958714872598648 ) ) );
			}

			// Special case depth method
			if( method.getName().toLowerCase().contains( "depth" ) ) {
				edu.cmu.cs.dennisc.math.AngleInDegrees pitch = new edu.cmu.cs.dennisc.math.AngleInDegrees( 00.0 );
				edu.cmu.cs.dennisc.math.AngleInDegrees yaw = new edu.cmu.cs.dennisc.math.AngleInDegrees( -90.0 );
				edu.cmu.cs.dennisc.math.AngleInDegrees roll = new edu.cmu.cs.dennisc.math.AngleInDegrees( 00.0 );

				this.overrideValueMap.put( this.activeField.getName() + ".localOrientation", new OrthogonalMatrix3x3( new edu.cmu.cs.dennisc.math.EulerAngles( pitch, yaw, roll, edu.cmu.cs.dennisc.math.EulerAngles.Order.PITCH_YAW_ROLL ) ) );
			}

			if( this.programContext != null ) {
				edu.cmu.cs.dennisc.animation.Animator animator = this.programContext.getProgramImp().getAnimator();
				if( animator instanceof edu.cmu.cs.dennisc.animation.AbstractAnimator ) {
					( (edu.cmu.cs.dennisc.animation.AbstractAnimator)animator ).cancelAnimation();
				}
				setCharacterProperties( this.activeField );
			}
		}
	}

	public String getShowDetailText() {
		return findLocalizedText( "showDetails" );
	}

	public String getHideDetailText() {
		return findLocalizedText( "hideDetails" );
	}

	public CancelOperation getCancelOperation() {
		return this.cancelOperation;
	}

	public ChangeMethodOperation getNextOperation() {
		return this.nextOperation;
	}

	public ChangeMethodOperation getPreviousOperation() {
		return this.previousOperation;
	}

	public void executeStatement( org.lgna.project.ast.Statement statement ) {
		if( this.programContext != null ) {
			// Cancel animation
			edu.cmu.cs.dennisc.animation.Animator animator = this.programContext.getProgramImp().getAnimator();
			if( animator instanceof edu.cmu.cs.dennisc.animation.AbstractAnimator ) {
				( (edu.cmu.cs.dennisc.animation.AbstractAnimator)animator ).cancelAnimation();
			}
			// Reset character
			setCharacterProperties( this.activeField );

			org.lgna.project.virtualmachine.UserInstance sceneInstance = this.programContext.getSceneInstance();
			org.lgna.story.SScene scene = (SScene)sceneInstance.getJavaInstance();
			org.lgna.story.implementation.SceneImp sceneImp = org.lgna.story.EmployeesOnly.getImplementation( scene );

			org.lgna.common.ComponentThread thread = new org.lgna.common.ComponentThread( () -> {
				sceneImp.delay( 0.5 );
				this.programContext.getVirtualMachine().ACCEPTABLE_HACK_FOR_SCENE_EDITOR_executeStatement( sceneInstance, statement );
				performVehicleMoveIfNecessary( statement );
			} , "previewMethod" );
			thread.start();
		}
	}

	public void pushExecutionOperation( org.lgna.croquet.Operation operation ) {
		operation.setEnabled( false );
		synchronized( this.executingOperationStack ) {
			this.executingOperationStack.push( operation );
		}
	}

	public org.lgna.croquet.Operation peekExecutionOperation() {
		synchronized( this.executingOperationStack ) {
			if( this.executingOperationStack.isEmpty() ) {
				return null;
			} else {
				return this.executingOperationStack.peek();
			}
		}
	}

	/*package-private*/void setupProjectIfNecessary() {
		if( this.currentUserProject != edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().getProject() ) {
			this.currentUserProject = edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().getProject();
			this.previewProject = setupProject();
			this.expressionGenerator = new LgpediaExpressionCreator( this.tigerField, this.treasureChestField );
		}
	}

	private void performVehicleMoveIfNecessary( org.lgna.project.ast.Statement statement ) {
		if( statement instanceof org.lgna.project.ast.ExpressionStatement ) {
			org.lgna.project.ast.ExpressionStatement expressionStatement = (org.lgna.project.ast.ExpressionStatement)statement;
			if( expressionStatement.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) {
				org.lgna.project.ast.MethodInvocation invocation = (org.lgna.project.ast.MethodInvocation)expressionStatement.expression.getValue();
				org.lgna.project.ast.AbstractMethod method = invocation.method.getValue();

				if( method.getName().contentEquals( "setVehicle" ) ) {
					org.lgna.story.SMovableTurnable object = (org.lgna.story.SMovableTurnable)this.programContext.getSceneInstance().getFieldValueInstanceInJava( this.hareField );
					object.move( org.lgna.story.MoveDirection.BACKWARD, 2.0 );
					object.move( org.lgna.story.MoveDirection.RIGHT, 2.0 );
					object.move( org.lgna.story.MoveDirection.FORWARD, 2.0 );
					object.move( org.lgna.story.MoveDirection.LEFT, 2.0 );

				}
			}
		}
	}

	private org.lgna.project.Project setupProject() {
		// Create fields
		this.hareField = createUserField( "hare", org.lgna.story.resources.biped.HareResource.DEFAULT, org.lgna.story.SBiped.class );
		this.tigerField = createUserField( "tiger", org.lgna.story.resources.biped.StuffedTigerResource.DEFAULT, org.lgna.story.SBiped.class );
		this.treasureChestField = createUserField( "chest", org.lgna.story.resources.prop.TreasureChestResource.DEFAULT, org.lgna.story.SProp.class );

		java.util.List<org.lgna.project.ast.UserField> fields = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();
		fields.add( this.hareField );
		fields.add( this.tigerField );
		fields.add( this.treasureChestField );

		// Create programType
		org.lgna.project.ast.NamedUserType programType;
		if( org.alice.stageide.ast.StoryApiSpecificAstUtilities.programHasRoom( edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().getProgramType() ) ) {
			programType = NebulousIde.nonfree.createRoomProgramType( fields );
		} else {
			programType = org.alice.stageide.ast.BootstrapUtilties.createProgramType( fields, Color.DARK_GRAY, Color.LIGHT_GRAY, 0.2, Color.WHITE, Color.BLACK );
		}

		// Move fields
		edu.cmu.cs.dennisc.math.AngleInDegrees propPitch = new edu.cmu.cs.dennisc.math.AngleInDegrees( 20.0 );
		edu.cmu.cs.dennisc.math.AngleInDegrees propYaw = new edu.cmu.cs.dennisc.math.AngleInDegrees( 00.0 );
		edu.cmu.cs.dennisc.math.AngleInDegrees propRoll = new edu.cmu.cs.dennisc.math.AngleInDegrees( 00.0 );
		edu.cmu.cs.dennisc.math.EulerAngles propRotation = new edu.cmu.cs.dennisc.math.EulerAngles( propPitch, propYaw, propRoll, edu.cmu.cs.dennisc.math.EulerAngles.Order.PITCH_YAW_ROLL );
		edu.cmu.cs.dennisc.math.UnitQuaternion propQuat = propRotation.createUnitQuaternion();

		edu.cmu.cs.dennisc.math.AngleInDegrees harePitch = new edu.cmu.cs.dennisc.math.AngleInDegrees( 00.0 );
		edu.cmu.cs.dennisc.math.AngleInDegrees hareYaw = new edu.cmu.cs.dennisc.math.AngleInDegrees( -10.0 );
		edu.cmu.cs.dennisc.math.AngleInDegrees hareRoll = new edu.cmu.cs.dennisc.math.AngleInDegrees( 00.0 );
		edu.cmu.cs.dennisc.math.EulerAngles hareRotation = new edu.cmu.cs.dennisc.math.EulerAngles( harePitch, hareYaw, hareRoll, edu.cmu.cs.dennisc.math.EulerAngles.Order.PITCH_YAW_ROLL );
		edu.cmu.cs.dennisc.math.UnitQuaternion hareQuat = hareRotation.createUnitQuaternion();

		positionUserField( this.hareField, new org.lgna.story.Position( 0, 0, -1 ), new org.lgna.story.Orientation( hareQuat.x, hareQuat.y, hareQuat.z, hareQuat.w ), programType );
		positionUserField( this.tigerField, new org.lgna.story.Position( 2, 0, -1 ), new org.lgna.story.Orientation( 0, 0, 0, 0 ), programType );
		positionUserField( this.treasureChestField, new org.lgna.story.Position( -2, 0, -1 ), new org.lgna.story.Orientation( propQuat.x, propQuat.y, propQuat.z, propQuat.w ), programType );

		return new org.lgna.project.Project( programType );
	}

	private org.lgna.project.ast.UserField createUserField( String fieldName, Enum<? extends org.lgna.story.resources.ModelResource> resourceConstant, Class<?> superTypeClss ) {
		org.alice.stageide.modelresource.EnumConstantResourceKey key = new org.alice.stageide.modelresource.EnumConstantResourceKey( resourceConstant );
		org.lgna.project.ast.AbstractType<?, ?, ?> type = org.alice.ide.typemanager.TypeManager.getNamedUserTypeFromArgumentField( org.lgna.project.ast.JavaType.getInstance( superTypeClss ), key.getField() );

		org.lgna.project.ast.UserField field = new org.lgna.project.ast.UserField();
		field.accessLevel.setValue( org.lgna.project.ast.AccessLevel.PRIVATE );
		field.managementLevel.setValue( ManagementLevel.MANAGED );
		field.valueType.setValue( type );
		field.name.setValue( fieldName );
		field.initializer.setValue( key.createInstanceCreation() );

		return field;
	}

	private void positionUserField( org.lgna.project.ast.UserField field, org.lgna.story.Position position, org.lgna.story.Orientation orientation, org.lgna.project.ast.NamedUserType programType ) {
		org.lgna.project.ast.NamedUserType sceneType = org.alice.stageide.ast.StoryApiSpecificAstUtilities.getSceneTypeFromProgramType( programType );
		org.lgna.project.ast.UserMethod setupMethod = org.alice.stageide.ast.StoryApiSpecificAstUtilities.getPerformEditorGeneratedSetUpMethod( sceneType );

		try {
			org.lgna.project.ast.Statement positionStatement = org.alice.stageide.sceneeditor.SetUpMethodGenerator.createPositionStatement( false, field, position );
			org.lgna.project.ast.Statement orientationStatement = org.alice.stageide.sceneeditor.SetUpMethodGenerator.createOrientationStatement( false, field, orientation );

			setupMethod.body.getValue().statements.add( positionStatement, orientationStatement );
		} catch( CannotCreateExpressionException e ) {
			e.printStackTrace();
		}
	}

	private void handleMethodChange( org.lgna.project.ast.AbstractMethod method ) {

		if( method.getName().contentEquals( "setVehicle" ) ) {
			handleSetVehicle( method );
			return;
		}

		for( org.lgna.project.ast.AbstractParameter parameter : method.getRequiredParameters() ) {
			getView().addParameterAnnotation( parameter, method );
			java.util.List<org.lgna.project.ast.Expression> expressions = this.expressionGenerator.generateExpressionsForParamter( parameter );

			for( org.lgna.project.ast.Expression expression : expressions ) {
				org.lgna.project.ast.MethodInvocation invocation = new org.lgna.project.ast.MethodInvocation();

				org.lgna.project.ast.Expression invocationExpression;
				if( method.getDeclaringType().isAssignableTo( org.lgna.story.SScene.class ) ) {
					invocationExpression = org.lgna.project.ast.ThisExpression.createInstanceThatCanExistWithoutAnAncestorType( method.getDeclaringType() );
				} else {
					invocationExpression = org.lgna.project.ast.AstUtilities.createFieldAccess( new org.lgna.project.ast.ThisExpression(), this.activeField );
				}

				invocation.expression.setValue( invocationExpression );
				invocation.method.setValue( method );

				for( int i = 0; i < method.getRequiredParameters().size(); i++ ) {
					org.lgna.project.ast.AbstractParameter requiredParam = method.getRequiredParameters().get( i );

					org.lgna.project.ast.Expression argumentExpression;
					if( requiredParam.equals( parameter ) ) {
						argumentExpression = expression;
					} else {
						argumentExpression = this.expressionGenerator.generateExpressionForParameter( requiredParam, false );
					}
					org.lgna.project.ast.SimpleArgument argument = new org.lgna.project.ast.SimpleArgument( parameter, argumentExpression );
					invocation.requiredArguments.add( i, argument );
				}
				getView().addParameterStatement( new org.lgna.project.ast.ExpressionStatement( invocation ), true );
			}
		}

		org.lgna.project.ast.AbstractParameter keyedParameter = method.getKeyedParameter();
		if( keyedParameter != null ) {
			org.lgna.project.ast.AbstractType<?, ?, ?> valueType = keyedParameter.getValueType().getComponentType();
			org.lgna.project.ast.AbstractType<?, ?, ?> keywordFactoryType = valueType.getKeywordFactoryType();
			if( keywordFactoryType != null ) {
				Class<?> cls = ( (org.lgna.project.ast.JavaType)keywordFactoryType ).getClassReflectionProxy().getReification();
				for( java.lang.reflect.Method mthd : cls.getMethods() ) {
					if( isValidMethod( mthd, valueType ) ) {
						org.lgna.project.ast.JavaMethod keyMethod = org.lgna.project.ast.JavaMethod.getInstance( mthd );
						org.lgna.project.ast.AbstractParameter parameter = keyMethod.getRequiredParameters().get( 0 );

						getView().addDetailAnnotation( keyMethod, method );

						java.util.List<org.lgna.project.ast.Expression> expressions = this.expressionGenerator.generateExpressionsForParamter( parameter );
						for( org.lgna.project.ast.Expression expression : expressions ) {
							org.lgna.project.ast.JavaKeyedArgument keyedArgument = new org.lgna.project.ast.JavaKeyedArgument( keyedParameter, keyMethod, expression );
							org.lgna.project.ast.MethodInvocation invocation = createDefaultInvocation( method );

							invocation.keyedArguments.add( keyedArgument );

							getView().addDetailStatement( new org.lgna.project.ast.ExpressionStatement( invocation ), true );
						}
					}
				}
			}
		}
	}

	private void handleSetVehicle( org.lgna.project.ast.AbstractMethod method ) {
		org.lgna.project.ast.UserField sceneField = org.alice.stageide.ast.StoryApiSpecificAstUtilities.getSceneFieldFromProgramType( this.previewProject.getProgramType() );
		LgpediaExpressionCreator vehicleGenerator = new LgpediaExpressionCreator( this.hareField, sceneField );
		for( org.lgna.project.ast.AbstractParameter parameter : method.getRequiredParameters() ) {
			getView().addParameterAnnotation( parameter, method );
			java.util.List<org.lgna.project.ast.Expression> expressions = vehicleGenerator.generateExpressionsForParamter( parameter );

			for( org.lgna.project.ast.Expression expression : expressions ) {
				org.lgna.project.ast.MethodInvocation invocation = new org.lgna.project.ast.MethodInvocation();

				org.lgna.project.ast.Expression invocationExpression;
				if( method.getDeclaringType().isAssignableTo( org.lgna.story.SScene.class ) ) {
					invocationExpression = org.lgna.project.ast.ThisExpression.createInstanceThatCanExistWithoutAnAncestorType( method.getDeclaringType() );
				} else {
					invocationExpression = org.lgna.project.ast.AstUtilities.createFieldAccess( new org.lgna.project.ast.ThisExpression(), this.tigerField );
				}

				invocation.expression.setValue( invocationExpression );
				invocation.method.setValue( method );

				for( int i = 0; i < method.getRequiredParameters().size(); i++ ) {
					org.lgna.project.ast.AbstractParameter requiredParam = method.getRequiredParameters().get( i );

					org.lgna.project.ast.Expression argumentExpression;
					if( requiredParam.equals( parameter ) ) {
						argumentExpression = expression;
					} else {
						argumentExpression = this.expressionGenerator.generateExpressionForParameter( requiredParam, false );
					}
					org.lgna.project.ast.SimpleArgument argument = new org.lgna.project.ast.SimpleArgument( parameter, argumentExpression );
					invocation.requiredArguments.add( i, argument );
				}
				getView().addParameterStatement( new org.lgna.project.ast.ExpressionStatement( invocation ), true );

				// add move statements
				java.util.List<org.lgna.project.ast.Statement> statements = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();
				statements.add( createMoveStatement( org.lgna.story.MoveDirection.BACKWARD ) );
				statements.add( createMoveStatement( org.lgna.story.MoveDirection.RIGHT ) );
				statements.add( createMoveStatement( org.lgna.story.MoveDirection.FORWARD ) );
				statements.add( createMoveStatement( org.lgna.story.MoveDirection.LEFT ) );

				org.lgna.project.ast.BlockStatement body = new org.lgna.project.ast.BlockStatement();
				body.statements.addAll( statements );

				getView().addParameterStatement( new org.lgna.project.ast.DoInOrder( body ), false );
			}
		}
	}

	private org.lgna.project.ast.Statement createMoveStatement( org.lgna.story.MoveDirection direction ) {
		org.lgna.project.ast.JavaMethod moveMethod = org.lgna.project.ast.JavaMethod.getInstance( org.lgna.story.SMovableTurnable.class, "move", org.lgna.story.MoveDirection.class, Number.class, org.lgna.story.Move.Detail[].class );
		try {
			org.lgna.project.ast.SimpleArgument directionArgument = new org.lgna.project.ast.SimpleArgument( moveMethod.getRequiredParameters().get( 0 ), this.expressionGenerator.getExpressionCreator().createExpression( direction ) );
			org.lgna.project.ast.SimpleArgument amountArgument = new org.lgna.project.ast.SimpleArgument( moveMethod.getRequiredParameters().get( 0 ), this.expressionGenerator.getExpressionCreator().createExpression( 2.0 ) );
			org.lgna.project.ast.MethodInvocation mi = new org.lgna.project.ast.MethodInvocation( new org.lgna.project.ast.FieldAccess( new org.lgna.project.ast.ThisExpression(), this.hareField ), moveMethod, directionArgument, amountArgument );

			return new org.lgna.project.ast.ExpressionStatement( mi );
		} catch( CannotCreateExpressionException e ) {
			//pass
		}
		return null;
	}

	private boolean isValidMethod( java.lang.reflect.Method mthd, org.lgna.project.ast.AbstractType<?, ?, ?> valueType ) {
		int modifiers = mthd.getModifiers();
		if( java.lang.reflect.Modifier.isPublic( modifiers ) && java.lang.reflect.Modifier.isStatic( modifiers ) ) {
			return valueType.isAssignableFrom( mthd.getReturnType() );
		} else {
			return false;
		}
	}

	private org.lgna.project.ast.MethodInvocation createDefaultInvocation( org.lgna.project.ast.AbstractMethod method ) {
		org.lgna.project.ast.MethodInvocation invocation = new org.lgna.project.ast.MethodInvocation();

		org.lgna.project.ast.Expression invocationExpression;
		if( method.getDeclaringType().isAssignableTo( org.lgna.story.SScene.class ) ) {
			invocationExpression = org.lgna.project.ast.ThisExpression.createInstanceThatCanExistWithoutAnAncestorType( method.getDeclaringType() );
		} else {
			invocationExpression = org.lgna.project.ast.AstUtilities.createFieldAccess( new org.lgna.project.ast.ThisExpression(), this.activeField );
		}

		invocation.expression.setValue( invocationExpression );
		invocation.method.setValue( method );

		for( int i = 0; i < method.getRequiredParameters().size(); i++ ) {
			org.lgna.project.ast.AbstractParameter parameter = method.getRequiredParameters().get( i );
			org.lgna.project.ast.Expression argumentExpression = this.expressionGenerator.generateExpressionForParameter( parameter, false );
			org.lgna.project.ast.SimpleArgument argument = new org.lgna.project.ast.SimpleArgument( parameter, argumentExpression );

			invocation.requiredArguments.add( i, argument );
		}
		return invocation;
	}

	private void createPreviewContext() {
		assert javax.swing.SwingUtilities.isEventDispatchThread();

		try {
			this.programContext = new RunProgramAsPreviewContext( this.previewProject );
			this.programContext.initializeInContainer( getView().getExecutingProgramContainer().getAwtComponent() );
			this.programContext.setActiveSceneOnComponentThreadAndWait();

			org.lgna.project.virtualmachine.VirtualMachine vm = this.programContext.getVirtualMachine();

			if( vm instanceof edu.wustl.lookingglass.virtualmachine.StateListeningVirtualMachine ) {
				vm.addVirtualMachineListener( new org.lgna.project.virtualmachine.events.VirtualMachineListener() {

					@Override
					public void statementExecuting( StatementExecutionEvent statementExecutionEvent ) {
					}

					@Override
					public void statementExecuted( StatementExecutionEvent statementExecutionEvent ) {
						synchronized( executingOperationStack ) {
							if( executingOperationStack.isEmpty() ) {
								//pass
							} else {
								org.lgna.croquet.Operation operation = executingOperationStack.pop();
								operation.setEnabled( true );
							}
						}
					}

					@Override
					public void expressionEvaluating( ExpressionEvaluationEvent expressionEvaluationEvent ) {
					}

					@Override
					public void expressionEvaluated( ExpressionEvaluationEvent expressionEvaluationEvent ) {
					}

					@Override
					public void methodInvoking( MethodInvocationEvent methodInvocationEvent ) {
					}

					@Override
					public void methodInvoked( MethodInvocationEvent methodInvocationEvent ) {
					}

				} );
			}

			// capture field properties
			this.propertyValueMap = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();
			captureCharacterProperties( this.activeField );

			// set field properties
			setCharacterProperties( this.activeField );

			getView().getExecutingProgramContainer().revalidateAndRepaint();
		} catch( Throwable t ) {
			//pass
			edu.cmu.cs.dennisc.java.util.logging.Logger.outln( t );
		}
	}

	private void captureCharacterProperties( org.lgna.project.ast.UserField field ) {
		org.lgna.story.SThing object;
		if( field.getValueType().isAssignableTo( org.lgna.story.SScene.class ) ) {
			object = (org.lgna.story.SThing)this.programContext.getProgramInstance().getFieldValueInstanceInJava( field );
		} else {
			object = (org.lgna.story.SThing)this.programContext.getSceneInstance().getFieldValueInstanceInJava( field );
		}
		org.lgna.story.implementation.EntityImp imp = org.lgna.story.EmployeesOnly.getImplementation( object );

		if( imp instanceof org.lgna.story.implementation.SceneImp ) {
			org.lgna.story.implementation.SceneImp sceneImp = (org.lgna.story.implementation.SceneImp)imp;

			this.propertyValueMap.put( field.getName() + ".atmosphereColor", sceneImp.atmosphereColor.getValue() );
			this.propertyValueMap.put( field.getName() + ".fromAboveLightColor", sceneImp.fromAboveLightColor.getValue() );
			this.propertyValueMap.put( field.getName() + ".fromBelowLightColor", sceneImp.fromBelowLightColor.getValue() );
			this.propertyValueMap.put( field.getName() + ".fogDensity", sceneImp.fogDensity.getValue() );
		} else if( NebulousIde.nonfree.isInstanceOfRoomImp( imp ) ) {
			NebulousIde.nonfree.captureRoomProperties( field, imp, propertyValueMap );
		} else if( imp instanceof org.lgna.story.implementation.ModelImp ) {
			org.lgna.story.implementation.ModelImp modelImp = (org.lgna.story.implementation.ModelImp)imp;

			this.propertyValueMap.put( field.getName() + ".opacity", modelImp.opacity.getValue() );
			this.propertyValueMap.put( field.getName() + ".paint", modelImp.paint.getValue() );
			this.propertyValueMap.put( field.getName() + ".vehicle", modelImp.getVehicle() );
		}

		if( imp instanceof org.lgna.story.implementation.TransformableImp ) {
			org.lgna.story.implementation.TransformableImp transformableImp = (org.lgna.story.implementation.TransformableImp)imp;

			this.propertyValueMap.put( field.getName() + ".localPosition", transformableImp.getLocalPosition() );
			this.propertyValueMap.put( field.getName() + ".localOrientation", transformableImp.getLocalOrientation() );
		}

		if( imp instanceof org.lgna.story.implementation.JointedModelImp ) {
			org.lgna.story.implementation.BipedImp bipedImp = (org.lgna.story.implementation.BipedImp)imp;

			this.propertyValueMap.put( field.getName() + ".scale", bipedImp.getScale() );

			for( org.lgna.story.implementation.JointImp jointImp : bipedImp.getJoints() ) {
				String key = jointImp.getJointId().toString();
				this.propertyValueMap.put( field.getName() + "." + key, jointImp.getLocalTransformation() );
			}
		}
	}

	private void setCharacterProperties( org.lgna.project.ast.UserField field ) {
		org.lgna.story.SThing object;
		if( field.getValueType().isAssignableTo( org.lgna.story.SScene.class ) ) {
			object = (org.lgna.story.SThing)this.programContext.getProgramInstance().getFieldValueInstanceInJava( field );
		} else {
			object = (org.lgna.story.SThing)this.programContext.getSceneInstance().getFieldValueInstanceInJava( field );
		}
		org.lgna.story.implementation.EntityImp imp = org.lgna.story.EmployeesOnly.getImplementation( object );

		if( imp instanceof org.lgna.story.implementation.SceneImp ) {
			org.lgna.story.implementation.SceneImp sceneImp = (org.lgna.story.implementation.SceneImp)imp;

			Color atmosphereColor = (Color)getPropertyValue( field.getName() + ".atmosphereColor" );
			Color fromAboveLightColor = (Color)getPropertyValue( field.getName() + ".fromAboveLightColor" );
			Color fromBelowLightColor = (Color)getPropertyValue( field.getName() + ".fromBelowLightColor" );
			Float fogDensity = (Float)getPropertyValue( field.getName() + ".fogDensity" );

			if( atmosphereColor != null ) {
				sceneImp.atmosphereColor.setValue( atmosphereColor );
			}
			if( fromAboveLightColor != null ) {
				sceneImp.fromAboveLightColor.setValue( fromAboveLightColor );
			}
			if( fromBelowLightColor != null ) {
				sceneImp.fromBelowLightColor.setValue( fromBelowLightColor );
			}
			if( fogDensity != null ) {
				sceneImp.fogDensity.setValue( fogDensity );
			}
		} else if( NebulousIde.nonfree.isInstanceOfRoomImp( imp ) ) {
			Paint floorPaint = (Paint)getPropertyValue( field.getName() + ".floorPaint" );
			Paint wallPaint = (Paint)getPropertyValue( field.getName() + ".wallPaint" );
			Paint ceilingPaint = (Paint)getPropertyValue( field.getName() + ".ceilingPaint" );
			Float opacity = (Float)getPropertyValue( field.getName() + ".opacity" );

			NebulousIde.nonfree.setRoomProperties( imp, floorPaint, wallPaint, ceilingPaint, opacity );
		} else if( imp instanceof org.lgna.story.implementation.ModelImp ) {
			org.lgna.story.implementation.ModelImp modelImp = (org.lgna.story.implementation.ModelImp)imp;
			Float opacity = (Float)getPropertyValue( field.getName() + ".opacity" );
			Paint paint = (Paint)getPropertyValue( field.getName() + ".paint" );
			org.lgna.story.implementation.EntityImp vehicle = (org.lgna.story.implementation.EntityImp)getPropertyValue( field.getName() + ".vehicle" );

			if( opacity != null ) {
				modelImp.opacity.setValue( opacity );
			}
			if( paint != null ) {
				modelImp.paint.setValue( paint );
			}
			if( true ) {
				modelImp.setVehicle( vehicle );
			}
		}

		if( imp instanceof org.lgna.story.implementation.TransformableImp ) {
			org.lgna.story.implementation.TransformableImp transformableImp = (org.lgna.story.implementation.TransformableImp)imp;

			Point3 localPosition = (Point3)getPropertyValue( field.getName() + ".localPosition" );
			OrthogonalMatrix3x3 localOrientation = (OrthogonalMatrix3x3)getPropertyValue( field.getName() + ".localOrientation" );

			if( localPosition != null ) {
				transformableImp.setLocalPosition( localPosition );
			}
			if( localOrientation != null ) {
				transformableImp.setLocalOrientation( localOrientation );
			}
		}

		if( imp instanceof org.lgna.story.implementation.BipedImp ) {
			org.lgna.story.implementation.BipedImp bipedImp = (org.lgna.story.implementation.BipedImp)imp;

			Dimension3 scale = (Dimension3)getPropertyValue( field.getName() + ".scale" );

			if( scale != null ) {
				bipedImp.setScale( scale );
			}

			for( org.lgna.story.implementation.JointImp jointImp : bipedImp.getJoints() ) {
				String key = jointImp.getJointId().toString();
				AffineMatrix4x4 jointTransform = (AffineMatrix4x4)getPropertyValue( field.getName() + "." + key );
				if( jointTransform != null ) {
					jointImp.setLocalTransformation( jointTransform );
				}
			}
		}
	}

	private Object getPropertyValue( String key ) {
		Object rv = this.overrideValueMap.get( key );
		if( rv == null ) {
			return this.propertyValueMap.get( key );
		} else {
			return rv;
		}
	}

	@Override
	protected java.awt.Dimension calculateWindowSize( org.lgna.croquet.views.AbstractWindow<?> window ) {
		return new java.awt.Dimension( 950, 500 );
	}

	@Override
	protected LgpediaView createView() {
		return new LgpediaView( this );
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		createPreviewContext();
	}

	public void scrollToSelection( boolean scrollToDetails ) {
		this.getView().scrollToSelection( scrollToDetails );
	}

	public void setBackgroundColorType( ConstructType type ) {
		this.getView().setBackgroundColorType( type );
	}

	@Override
	public void handlePostDeactivation() {
		if( this.programContext != null ) {
			this.programContext.cleanUpProgram();
			this.programContext = null;
		}
		this.methodIndex = -1;
		super.handlePostDeactivation();
	}

	/**
	 * <code>ActionOperation</code> that closes the dialog containing the
	 * <code>CharacterSelectionComposite</code> and performs any necessary
	 * cleanup.
	 *
	 * @author Michael Pogran
	 */
	public class CancelOperation extends org.lgna.croquet.Operation {

		public CancelOperation() {
			super( org.lgna.croquet.Application.PROJECT_GROUP, java.util.UUID.fromString( "f6251556-8996-4e1c-97dc-b2e1d39daca5" ) );
		}

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			Component root = SwingUtilities.getRoot( getView().getAwtComponent() );
			if( root != null ) {
				root.setVisible( false );
			}
		}

	}

	public class ChangeMethodOperation extends org.lgna.croquet.Operation {
		private org.lgna.project.ast.AbstractMethod method;

		public ChangeMethodOperation( String name ) {
			super( org.lgna.croquet.Application.PROJECT_GROUP, java.util.UUID.fromString( "8f4a9cab-1452-4f46-b1e8-42ff3317c0e9" ) );
			setName( name );
		}

		public void setMethod( org.lgna.project.ast.AbstractMethod method ) {
			this.method = method;
		}

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			LgpediaComposite.this.setAstNode( this.method, false );
			LgpediaComposite.this.scrollToSelection( false );
		}

	}
}
