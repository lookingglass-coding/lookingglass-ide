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
package edu.wustl.lookingglass.remix;

import java.util.Set;

import org.alice.nonfree.NebulousIde;
import org.alice.stageide.ast.StoryApiSpecificAstUtilities;
import org.alice.stageide.sceneeditor.SetUpMethodGenerator;
import org.lgna.project.Project;
import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractType;
import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.NamedUserType;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.UserField;
import org.lgna.project.ast.UserMethod;
import org.lgna.project.virtualmachine.UserInstance;
import org.lgna.story.EmployeesOnly;
import org.lgna.story.SCamera;
import org.lgna.story.SGround;
import org.lgna.story.SScene;
import org.lgna.story.SThing;
import org.lgna.story.implementation.EntityImp;

import edu.cmu.cs.dennisc.java.util.Sets;
import edu.cmu.cs.dennisc.math.AffineMatrix4x4;
import edu.wustl.lookingglass.community.CommunityProjectPropertyManager;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.remix.ast.EventNodeToScriptCopier;
import edu.wustl.lookingglass.remix.ast.RemixReferencesCrawler;
import edu.wustl.lookingglass.remix.ast.RemixUtilities;
import edu.wustl.lookingglass.remix.roles.RoleType;
import edu.wustl.lookingglass.scenegraph.recorder.RecorderManager;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractLoopEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ContainerEventNode;
import edu.wustl.lookingglass.virtualmachine.observer.VMExecutionObserver;

/**
 * @author Michael Pogran
 */
public class RemixSnippetFactory {
	private final AbstractEventNode<?> startEventNode;
	private final AbstractEventNode<?> endEventNode;

	private final VMExecutionObserver executionObserver;
	private final RemixReferencesCrawler crawler;
	private final EventNodeToScriptCopier copier;
	private SnippetScript snippetScript;
	private Project project;

	public static RemixSnippetFactory getRemixSnippetFactory( AbstractEventNode<?> startEventNode, AbstractEventNode<?> endEventNode, VMExecutionObserver executionObserver ) {
		return new RemixSnippetFactory( startEventNode, endEventNode, executionObserver );
	}

	private RemixSnippetFactory( AbstractEventNode<?> startEventNode, AbstractEventNode<?> endEventNode, VMExecutionObserver executionObserver ) {
		this.project = LookingGlassIDE.getActiveInstance().getProject();

		this.startEventNode = startEventNode;
		this.endEventNode = endEventNode;
		this.executionObserver = executionObserver;

		this.crawler = new RemixReferencesCrawler( startEventNode, endEventNode );
		this.copier = new EventNodeToScriptCopier( this.project, this.crawler, startEventNode, endEventNode );
		this.snippetScript = new SnippetScript();
	}

	public SnippetScript buildSnippetScript() {

		// Set basic snippet information
		this.snippetScript.setCommunityWorldId( CommunityProjectPropertyManager.getCommunityProjectID( this.project ) );

		this.snippetScript.setStartTime( this.startEventNode.getStartTime() );
		this.snippetScript.setEndTime( this.endEventNode.getEndTime() );

		this.snippetScript.setBeginNodeUUID( this.startEventNode.getAstUUID() );
		this.snippetScript.setEndNodeUUID( this.endEventNode.getAstUUID() );

		if( this.startEventNode instanceof AbstractLoopEventNode ) {
			this.snippetScript.setBeginNodeExecutionCount( ( (AbstractLoopEventNode<?>)this.startEventNode ).getNumberOfIterations() );
		}
		if( this.endEventNode instanceof AbstractLoopEventNode ) {
			this.snippetScript.setEndNodeExecutionCount( ( (AbstractLoopEventNode<?>)this.endEventNode ).getNumberOfIterations() );
		}
		this.snippetScript.setTitle( LookingGlassIDE.getActiveInstance().getUriProjectLoader().getTitle() );

		// Get root event node from copier, node containing selected code
		ContainerEventNode<BlockStatement> rootNode = this.copier.getRootEventNode();

		// Lower types of referenced locals, parameters, and declared sims methods
		this.copier.pullDownTypes();

		// Get references in snippet code
		this.crawler.crawlForReferences( rootNode, getSceneInstance(), this.copier.getOldToNewDeclarations(), this.project );

		// Appropriately handle dangling references to locals and parameters
		this.copier.resolveUndeclaredLocals( getSceneInstance() );
		this.copier.resolveReferencedParameters( getSceneInstance() );

		NamedUserType scriptType = this.copier.getScriptType();
		this.snippetScript.setScriptType( scriptType );

		// Set additional snippet information
		storeMethods();
		storeVehicles();
		storeRoles();
		storeRoleInitialTransforms();
		storeScriptResources();

		return this.snippetScript;
	}

	/**
	 * Move all user-authored methods used in snippet from their original
	 * declaring type to the script type.
	 * <p>
	 * <b>note:</b> While this may invalidate the method, all methods copied to
	 * the script type will ultimately be copied to their correct types during
	 * remixing.
	 * </p>
	 */
	private void storeMethods() {
		NamedUserType sceneType = (NamedUserType)this.copier.getOrCreateNewDeclaration( StoryApiSpecificAstUtilities.getSceneTypeFromProject( this.project ) );
		NamedUserType scriptType = this.snippetScript.getScriptType();

		for( AbstractMethod method : sceneType.methods ) {
			if( RemixUtilities.isMethodUserAuthoredMethod( (UserMethod)method, this.project.getProgramType() ) ) {
				if( scriptType.methods.contains( (UserMethod)method ) ) {
					//pass
				} else {
					scriptType.methods.add( (UserMethod)method );
				}
			}
		}
		clearMethods( sceneType );

		for( UserField field : this.crawler.getActiveFields() ) {
			AbstractType<?, ?, ?> type = (NamedUserType)this.copier.getNewDeclaration( field.getValueType() );

			while( type instanceof NamedUserType ) {
				for( AbstractMethod method : ( (NamedUserType)type ).methods ) {
					if( RemixUtilities.isMethodUserAuthoredMethod( (UserMethod)method, this.project.getProgramType() ) ) {
						if( scriptType.methods.contains( (UserMethod)method ) ) {
							//pass
						} else {
							scriptType.methods.add( (UserMethod)method );
						}
					}
				}
				type = type.getSuperType();
			}
			clearMethods( type );
		}

		// Store information about which fields used which methods
		this.snippetScript.setFieldsForMethods( this.crawler.getFieldsForMethods() );
	}

	/**
	 * Clear any user-authored methods from provided <code>AbstractType</code>
	 * and its super-types.
	 *
	 * @param type type to remove methods from
	 */
	private void clearMethods( AbstractType<?, ?, ?> type ) {
		while( type instanceof NamedUserType ) {
			NamedUserType programType = this.project.getProgramType();
			java.util.Iterator<UserMethod> methodsIterator = ( (NamedUserType)type ).methods.iterator();

			while( methodsIterator.hasNext() ) {
				UserMethod method = methodsIterator.next();

				if( RemixUtilities.isMethodUserAuthoredMethod( method, programType ) || type.isAssignableTo( SScene.class ) ) {
					methodsIterator.remove();
				}
			}
			type = type.getSuperType();
		}
	}

	/**
	 * Add all roles found during crawling to the snippet. Special roles (scene,
	 * room, ground, and camera) are added regardless.
	 */
	private void storeRoles() {
		addSpecialFields(); // need to add special roles, if missing

		for( UserField field : this.crawler.getActiveFields() ) {
			Role role = new Role( field, this.crawler.getLowestReferencedType( field ), RoleType.ACTIVE );
			role.setCallerCount( this.crawler.getCallerCount( field ) );
			role.setParameterCount( this.crawler.getParameterCount( field ) );
			this.snippetScript.addActiveRole( role );
		}

		for( UserField field : this.crawler.getSpecialFields() ) {
			Role role = new Role( field, field.getValueType().getFirstEncounteredJavaType(), RoleType.SPECIAL );
			role.setCallerCount( this.crawler.getCallerCount( field ) );
			role.setParameterCount( this.crawler.getParameterCount( field ) );
			this.snippetScript.addSpecialRole( role );
		}
	}

	/**
	 * Special role fields (scene, room, ground, and camera) are always added
	 * the snippet, regardless of whether or not they are referenced in the
	 * snippet code. If the scene has not been referenced, it is added as a
	 * declared field to the script type - also adding the camera and
	 * ground/room.
	 */
	private void addSpecialFields() {
		UserField sceneField = StoryApiSpecificAstUtilities.getSceneFieldFromProgramType( this.project.getProgramType() );
		UserField newSceneField = (UserField)this.copier.getOrCreateNewDeclaration( sceneField );

		// If unreferenced, add scene to script type declared fields
		if( this.crawler.getSpecialFields().contains( newSceneField ) ) {
			//pass
		} else {
			this.crawler.addSpecialField( newSceneField );
			this.copier.getScriptType().getDeclaredFields().add( newSceneField );
		}

		NamedUserType sceneType = StoryApiSpecificAstUtilities.getSceneTypeFromProgramType( this.project.getProgramType() );

		// Add other special fields
		for( UserField field : sceneType.getDeclaredFields() ) {
			UserField newField = null;
			if( NebulousIde.nonfree.isAssignableToSRoom( field.getValueType() ) ) {
				newField = (UserField)this.copier.getOrCreateNewDeclaration( field );
			} else if( field.getValueType().isAssignableTo( SGround.class ) ) {
				newField = (UserField)this.copier.getOrCreateNewDeclaration( field );
			} else if( field.getValueType().isAssignableTo( SCamera.class ) ) {
				newField = (UserField)this.copier.getOrCreateNewDeclaration( field );
			}
			if( newField != null ) {
				this.crawler.addSpecialField( newField );
			}
		}
	}

	/**
	 * Performs two resource-related operations.<br>
	 * <ul>
	 * <li>Generates and sets setup statements for all active roles.</li>
	 * <li>crawls for statements referencing resources (audio clips, images)
	 * with the help of the <code>RemixReferencesCrawler</code>.</li>
	 * </ul>
	 */
	private void storeScriptResources() {
		UserInstance sceneInstance = getSceneInstance();
		Set<UserField> fieldsInScript = Sets.newHashSet();

		// Fields needed when copying setup statements
		for( Role role : this.snippetScript.getActiveRoles() ) {
			fieldsInScript.add( role.getOriginField() );
		}

		// Copy and store setup statements
		for( Role role : this.snippetScript.getActiveRoles() ) {
			UserField executionField = this.copier.getOldFieldForNew( role.getOriginField() );
			SThing javaObject = getFieldObject( executionField );

			Statement[] initStatements = SetUpMethodGenerator.getSetupStatementsForInstance( false, javaObject, sceneInstance, false );
			Statement[] copiedInitStatements = this.copier.copyInitializationStatements( initStatements, fieldsInScript );

			role.setInitializationStatements( new BlockStatement( copiedInitStatements ) );
		}

		// Set referenced resources
		this.snippetScript.setScriptResources( crawler.getRequiredResources() );
	}

	/**
	 * Stores initial transformations for each role, captured at the snippet
	 * start time.
	 * <p>
	 * <b>note:</b> The initial transformation is used in conjunction with the
	 * camera relative transformation to compute the appropriate position of a
	 * character when remixing.
	 * </p>
	 */
	private void storeRoleInitialTransforms() {
		// Move program context to start time
		RecorderManager.getInstance().setToTime( this.snippetScript.getStartTime() );

		SCamera cameraObject = getCameraObject();
		this.snippetScript.setInitialCameraTransform( EmployeesOnly.getImplementation( cameraObject ).getAbsoluteTransformation() );

		for( Role role : this.snippetScript.getActiveRoles() ) {
			UserField executionField = this.copier.getOldFieldForNew( role.getOriginField() );
			SThing javaObject = getFieldObject( executionField );

			if( javaObject != null ) {

				// Set initial transformation
				EntityImp imp = EmployeesOnly.getImplementation( javaObject );
				role.setInitialTransformation( imp.getAbsoluteTransformation() );

				// Set transformation relative to camera
				AffineMatrix4x4 transform = imp.getTransformation( EmployeesOnly.getImplementation( cameraObject ) );
				role.setInitialCameraRelativeTransformation( transform );
			} else {
				throw new RuntimeException( "Null object for field " + role.getName() );
			}
		}
	}

	/**
	 * Store all vehicle changes found during crawling with the
	 * <code>RemixReferencesCrawler</code>.
	 */
	private void storeVehicles() {
		this.copier.addSetVehicleStatements( this.crawler.getNonDefaultVehicles(), getSceneInstance() );
	}

	/* ---------------- Helper methods  -------------- */

	private SThing getFieldObject( UserField field ) {
		UserInstance sceneInstance = getSceneInstance();

		Object javaObject = sceneInstance.getFieldValue( field );
		if( javaObject instanceof UserInstance ) {
			javaObject = ( (UserInstance)javaObject ).getJavaInstance();
		}

		if( javaObject instanceof SThing ) {
			return (SThing)javaObject;
		} else {
			return null;
		}
	}

	private SCamera getCameraObject() {
		UserInstance sceneInstance = getSceneInstance();

		for( UserField sceneField : sceneInstance.getType().getDeclaredFields() ) {
			if( sceneField.getValueType().isAssignableTo( SCamera.class ) ) {
				return (SCamera)sceneInstance.getFieldValue( sceneField );
			}
		}
		return null;
	}

	private UserInstance getSceneInstance() {
		return this.executionObserver.getSceneInstance();
	}
}
