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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alice.ide.ProjectStack;
import org.alice.ide.ast.ExpressionCreator.CannotCreateExpressionException;
import org.alice.ide.croquet.edits.ast.DeclareGalleryFieldEdit;
import org.alice.ide.name.validators.FieldNameValidator;
import org.alice.ide.name.validators.MethodNameValidator;
import org.alice.ide.typemanager.TypeManager;
import org.alice.nonfree.NebulousIde;
import org.alice.stageide.ast.JointMethodUtilities;
import org.alice.stageide.ast.StoryApiSpecificAstUtilities;
import org.alice.stageide.sceneeditor.SetUpMethodGenerator;
import org.alice.stageide.sceneeditor.StorytellingSceneEditor;
import org.lgna.croquet.edits.Edit;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.project.Project;
import org.lgna.project.ast.AbstractField;
import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractType;
import org.lgna.project.ast.ArrayInstanceCreation;
import org.lgna.project.ast.AstUtilities;
import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.ConstructorInvocationStatement;
import org.lgna.project.ast.CrawlPolicy;
import org.lgna.project.ast.Expression;
import org.lgna.project.ast.ExpressionStatement;
import org.lgna.project.ast.FieldAccess;
import org.lgna.project.ast.GlobalFirstInstanceExpression;
import org.lgna.project.ast.InstanceCreation;
import org.lgna.project.ast.JavaField;
import org.lgna.project.ast.JavaKeyedArgument;
import org.lgna.project.ast.LocalDeclarationStatement;
import org.lgna.project.ast.MethodInvocation;
import org.lgna.project.ast.NamedUserConstructor;
import org.lgna.project.ast.NamedUserType;
import org.lgna.project.ast.SimpleArgument;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.ThisExpression;
import org.lgna.project.ast.UserField;
import org.lgna.project.ast.UserMethod;
import org.lgna.project.ast.crawlers.NodePropertyValueIsInstanceCrawler;
import org.lgna.story.Position;
import org.lgna.story.SGround;
import org.lgna.story.SScene;
import org.lgna.story.ast.GlobalFirstInstanceExpressionUtitlies;

import edu.cmu.cs.dennisc.java.util.Lists;
import edu.cmu.cs.dennisc.java.util.Maps;
import edu.cmu.cs.dennisc.java.util.Sets;
import edu.cmu.cs.dennisc.java.util.Stacks;
import edu.cmu.cs.dennisc.math.AffineMatrix4x4;
import edu.cmu.cs.dennisc.pattern.IsInstanceCrawler;
import edu.cmu.cs.dennisc.property.InstanceProperty;
import edu.wustl.lookingglass.remix.Role;
import edu.wustl.lookingglass.remix.SnippetScript;
import edu.wustl.lookingglass.remix.ast.edits.DeclareFieldForPreviewEdit;
import edu.wustl.lookingglass.remix.ast.exceptions.ASTCopyException;
import edu.wustl.lookingglass.remix.ast.exceptions.NoJointReplacementException;

/**
 * A copier designed to copy the main method - and all dependent methods - of a
 * <code>SnippetScript</code> to a <code>Project</code>. Provides additional
 * functionality for resolving the declaration of user methods required by the
 * snippet and the creation of croquet edits for adding new fields to the scene.
 *
 * @author Michael Pogran
 */

public class ScriptToASTCopier extends ASTCopier {
	private SnippetScript snippetScript;

	private Map<UserMethod, Set<NamedUserType>> typesForMethod = Maps.newHashMap();

	public ScriptToASTCopier( Project project ) {
		super( project, true );
	}

	/**
	 * Copies the scriptRun method of a <code>SnippetScript</code> to the AST of
	 * the provided <code>Project</code> given a Map of fields to fill each
	 * <code>Role</code>.
	 *
	 * @param snippetScript <code>SnippetScript</code> to copy
	 * @param fieldForRoles <code>Map</code> containing role assignments
	 */
	public UserMethod copyScriptToAST( SnippetScript snippetScript, Map<Role, AbstractField> fieldForRoles, String methodName ) {
		this.snippetScript = snippetScript;

		Map<AbstractField, AbstractField> fieldsMap = constructFieldsMap( fieldForRoles );
		Set<NamedUserType> addedTypes = Sets.newHashSet();
		boolean hasAddedScene = false;

		// Add the appropriate declaration mappings
		addNewDeclaration( this.snippetScript.getScriptType(), getProjectSceneType() );

		for( AbstractField roleField : fieldsMap.keySet() ) {
			if( roleField.getValueType().isAssignableTo( SScene.class ) ) {
				hasAddedScene = true;
			}

			AbstractField newField = fieldsMap.get( roleField );
			addNewDeclaration( roleField, newField );

			AbstractType<?, ?, ?> roleType = roleField.getValueType();
			AbstractType<?, ?, ?> newType = newField.getValueType();

			if( newField instanceof UserField ) {
				if( newType.equals( roleType ) ) {
					//pass
				} else {
					( (UserField)newField ).valueType.setValue( newType );
				}
			}

			addNewDeclaration( roleType, newType );

			while( ( roleType instanceof NamedUserType ) && ( newType instanceof NamedUserType ) ) {
				if( addedTypes.contains( newType ) ) {
					// pass
				} else {
					addNewDeclaration( roleType, newType );
					addedTypes.add( (NamedUserType)roleType );
				}

				AbstractType<?, ?, ?> roleSuperType = roleType.getSuperType();
				AbstractType<?, ?, ?> newSuperType = newType.getSuperType();

				if( ( roleSuperType instanceof NamedUserType ) && ( newSuperType instanceof NamedUserType ) ) {
					( (NamedUserType)newType ).superType.setValue( newSuperType );
				}

				roleType = roleSuperType;
				newType = newSuperType;
			}
		}

		// Ensure a mapping from the snippet scene to project scene exists
		if( !hasAddedScene ) {
			UserField roleField = getSnippetSceneField();

			if( roleField != null ) {
				UserField newField = StoryApiSpecificAstUtilities.getSceneFieldFromProgramType( getProject().getProgramType() );
				addNewDeclaration( roleField, newField );
				addNewDeclaration( roleField.getValueType(), newField.getValueType() );
			}
		}

		setTypesForMethods( fieldsMap );

		UserMethod copyRootMethod = (UserMethod)copyAbstractNode( this.snippetScript.getRootMethod() );

		// This should not be doing magic. If I ask this copier to do something it better just do it.
		// It shouldn't rename things without me knowing. Instead pass in valid options.
		MethodNameValidator validator = new MethodNameValidator( getProjectSceneType() );
		assert validator.isNameAvailable( methodName );
		copyRootMethod.setName( methodName );

		return copyRootMethod;
	}

	/**
	 * Resolves all <code>ThisExpression</code> instances found in the snippet
	 * main method to <code>FieldAccess</code> expressions. Used when the root
	 * method is being added to a type other than the scene.
	 *
	 * @return root method.
	 */
	@SuppressWarnings( "rawtypes" )
	public UserMethod resolveThisExpressions( UserMethod method ) {
		NodePropertyValueIsInstanceCrawler<ThisExpression> thisCrawler = NodePropertyValueIsInstanceCrawler.createInstance( ThisExpression.class );
		method.crawl( thisCrawler, CrawlPolicy.EXCLUDE_REFERENCES_ENTIRELY );

		for( InstanceProperty property : thisCrawler.getList() ) {
			Expression expression = GlobalFirstInstanceExpressionUtitlies.createExpression( this.getProject().getProgramType() );
			property.setValue( expression );
		}
		return method;
	}

	/**
	 * Gets all the croquet edits needed to add any new fields to fill snippet
	 * roles. If this is a remix preview (ie. fields will be added to a program
	 * context other than the scene editor) a headless edit is used.
	 *
	 * @param step <code>CompletionStep</code> used to create edits
	 * @param isPreview determines type of edits returned
	 */
	public java.util.List<Edit> getFieldEdits( CompletionStep<?> step, boolean isPreview ) {
		List<Edit> rv = Lists.newArrayList();
		Set<UserField> addedFields = Sets.newHashSet();

		for( UserField field : this.snippetScript.getNewFields() ) {
			BlockStatement initStatements = copyBlockStatement( this.snippetScript.getRoleForField( field ).getInitializationStatements() );

			java.util.Set<UserField> dependentFields = getDependentUserFields( initStatements.statements.getValue() );
			dependentFields.removeAll( addedFields );

			for( UserField dependentField : dependentFields ) {
				if( this.snippetScript.getNewFields().contains( dependentField ) ) {
					Edit edit = getAddFieldEdit( this.snippetScript.getRoleForField( dependentField ), dependentField, initStatements, isPreview );
					if( edit != null ) {
						rv.add( edit );
					}
					addedFields.add( dependentField );
				}
			}
			Edit edit = getAddFieldEdit( this.snippetScript.getRoleForField( field ), field, initStatements, isPreview );
			if( edit != null ) {
				rv.add( edit );
			}
			addedFields.add( field );
		}
		return rv;
	}

	/* ---------------- Helper methods  -------------- */

	/**
	 * Returns the scene <code>UserField</code> if present in the
	 * <code>SnippetScript</code>.
	 *
	 * @return the snippet scene field
	 */
	private UserField getSnippetSceneField() {
		IsInstanceCrawler<UserField> crawler = IsInstanceCrawler.createInstance( UserField.class );

		this.snippetScript.getScriptType().crawl( crawler, CrawlPolicy.COMPLETE );

		for( UserField field : crawler.getList() ) {
			if( field.getValueType().isAssignableTo( SScene.class ) ) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Returns the scene type from the <code>Project</code>.
	 *
	 * @return the scene type
	 */
	private NamedUserType getProjectSceneType() {
		return StoryApiSpecificAstUtilities.getSceneTypeFromProject( getProject() );
	}

	/**
	 * Returns the type determined to be the common ancestor of a collection of
	 * types.
	 *
	 * @param types <code>Collection</code> of types
	 * @return common ancestor of types
	 */
	private AbstractType<?, ?, ?> computeSharedType( java.util.Collection<AbstractType<?, ?, ?>> types ) {
		AbstractType<?, ?, ?> sharedType = null;
		for( AbstractType<?, ?, ?> type : types ) {
			if( sharedType == null ) {
				sharedType = type;
			} else {
				while( !sharedType.isAssignableFrom( type ) ) {
					sharedType = sharedType.getSuperType();
				}
			}
		}
		return sharedType;
	}

	/**
	 * Constructs a mapping of role fields to new fields. If the new field for a
	 * role is not already in the project, we create a new
	 * <code>UserField</code> correctly typed for the provided
	 * <code>Project</code>.
	 *
	 * @param rolesMap mapping of fields to fill each role
	 * @return mapping of fields to replace each role field
	 */
	private Map<AbstractField, AbstractField> constructFieldsMap( Map<Role, AbstractField> rolesMap ) {
		this.snippetScript.clearNewFields();
		this.snippetScript.clearRoleForFields();

		Map<AbstractField, AbstractField> rv = Maps.newHashMap();
		Map<AbstractField, Role> roleForFields = Maps.newHashMap();

		// Iterate over active roles for snippet
		for( Role role : rolesMap.keySet() ) {
			AbstractField roleField = rolesMap.get( role );
			if( getProjectSceneType().equals( roleField.getDeclaringType() ) ) {
				// pass - Field already on scene
			} else if( getProject().getProgramType().equals( roleField.getDeclaringType() ) ) {
				// pass - Field is scene
			} else {
				roleField = createNewField( role.getOriginField() );
				this.snippetScript.addNewField( (UserField)roleField );
			}
			rv.put( role.getOriginField(), roleField );
			roleForFields.put( roleField, role );
		}

		// Ensure all special roles are correctly filled
		for( Role role : this.snippetScript.getSpecialRoles() ) {
			AbstractField specialField = role.getOriginField();
			AbstractField replacementField = null;

			if( RemixUtilities.isScene( specialField.getValueType() ) ) {
				replacementField = StoryApiSpecificAstUtilities.getSceneFieldFromProgramType( getProject().getProgramType() );
			} else {
				for( AbstractField field : getProjectSceneType().getDeclaredFields() ) {
					if( field.getValueType().isAssignableTo( specialField.getValueType() ) ) {
						replacementField = field;
						break;
					}
				}
			}

			// Should be okay to replace with ground/room (or vice versa), checked previously in CharacterSelectionOperation
			if( replacementField == null ) {
				if( RemixUtilities.isRoom( specialField.getValueType() ) ) {
					for( AbstractField field : getProjectSceneType().getDeclaredFields() ) {
						if( field.getValueType().isAssignableTo( SGround.class ) ) {
							replacementField = field;
							break;
						}
					}
				} else if( RemixUtilities.isGround( specialField.getValueType() ) ) {
					for( AbstractField field : getProjectSceneType().getDeclaredFields() ) {
						if( NebulousIde.nonfree.isAssignableToSRoom( field.getValueType() ) ) {
							replacementField = field;
							break;
						}
					}
				}
			}

			rv.put( specialField, replacementField );
		}

		this.snippetScript.setRoleForFields( roleForFields );

		return rv;
	}

	/**
	 * Helper method that creates a new <code>AbstractField</code> based on the
	 * <code>AbstractType</code> derived from the roleField's initializer and
	 * the project <code>TypeManager</code>.
	 *
	 * @param roleField the original field for the role
	 * @return a new field built for the current project
	 */
	private AbstractField createNewField( AbstractField roleField ) {
		if( roleField instanceof JavaField ) {
			return roleField;
		} else {
			UserField field = (UserField)roleField;
			Expression initializer = field.initializer.getValue();

			NamedUserType newValueType = null;
			Expression newInitializer = null;
			InstanceCreation simsInstanceCreation = null;

			// Get JavaField for resource
			if( initializer instanceof InstanceCreation ) {
				InstanceCreation instanceCreation = (InstanceCreation)initializer;

				JavaField resourceField = null;
				for( SimpleArgument arg : instanceCreation.requiredArguments ) {

					if( NebulousIde.nonfree.isAssignableToPersonResource( arg.parameter.getValue().getValueType() ) ) {
						Expression expression = arg.expression.getValue();

						if( expression instanceof InstanceCreation ) {
							simsInstanceCreation = (InstanceCreation)expression;
						}
					} else if( arg.parameter.getValue().getValueType().isAssignableTo( org.lgna.story.resources.ModelResource.class ) ) {
						Expression expression = arg.expression.getValue();

						if( expression instanceof FieldAccess ) {
							resourceField = (JavaField)( (FieldAccess)expression ).field.getValue();
						}
					}
				}
				// If no resourceField was found, check the super constructor
				if( resourceField == null ) {
					if( instanceCreation.constructor.getValue() instanceof NamedUserConstructor ) {
						NamedUserConstructor constructor = (NamedUserConstructor)instanceCreation.constructor.getValue();

						ConstructorInvocationStatement invocStatement = constructor.body.getValue().constructorInvocationStatement.getValue();
						for( SimpleArgument arg : invocStatement.requiredArguments ) {

							if( arg.parameter.getValue().getValueType().isAssignableTo( org.lgna.story.resources.ModelResource.class ) ) {
								Expression expression = arg.expression.getValue();

								if( expression instanceof FieldAccess ) {
									resourceField = (JavaField)( (FieldAccess)expression ).field.getValue();
								}
							}
						}
					}
				}

				// Attempt to set newValueType and newInitializer
				if( simsInstanceCreation != null ) {
					newValueType = TypeManager.getNamedUserTypeFromPersonResourceInstanceCreation( simsInstanceCreation );
					newInitializer = AstUtilities.createInstanceCreation( newValueType.getDeclaredConstructors().get( 0 ), simsInstanceCreation );
				} else {
					if( resourceField != null ) {
						newValueType = TypeManager.getNamedUserTypeFromArgumentField( field.getValueType().getFirstEncounteredJavaType(), resourceField );
					} else {
						newValueType = TypeManager.getNamedUserTypeFromSuperType( field.getValueType().getFirstEncounteredJavaType() );
					}

					if( newValueType != null ) {
						NamedUserConstructor constructor = ( (NamedUserType)newValueType ).getDeclaredConstructors().get( 0 );
						Expression[] argumentExpressions;
						if( ( constructor.getRequiredParameters().size() == 1 ) && ( resourceField != null ) ) {
							argumentExpressions = new org.lgna.project.ast.Expression[] { AstUtilities.createStaticFieldAccess( resourceField ) };
						} else {
							argumentExpressions = new Expression[] {};
						}
						newInitializer = AstUtilities.createInstanceCreation( constructor, argumentExpressions );
					}
				}
			}

			// Unable to find valid replacements
			if( ( newValueType == null ) || ( newInitializer == null ) ) {
				throw new RuntimeException( "Unable to create new field for " + field );
			}

			// Add new type and super-types to project for future calls to createNewField
			if( newValueType != null ) {
				Project project = ProjectStack.peekProject();
				AbstractType<?, ?, ?> typeToAdd = newValueType;

				while( ( typeToAdd != null ) && ( typeToAdd instanceof NamedUserType ) ) {
					if( project.getNamedUserTypes().contains( typeToAdd ) ) {
						// pass
					} else {
						project.addNamedUserType( (NamedUserType)typeToAdd );
					}
					typeToAdd = typeToAdd.getSuperType();
				}
			}

			// Create new field
			UserField rv = new UserField();

			rv.valueType.setValue( newValueType );
			rv.initializer.setValue( newInitializer );

			String newFieldName = RemixUtilities.getValidMemberName( new FieldNameValidator( getProjectSceneType() ), roleField.getName() );
			rv.setName( newFieldName );

			rv.accessLevel.setValue( field.accessLevel.getValue() );
			rv.finalVolatileOrNeither.setValue( field.finalVolatileOrNeither.getValue() );
			rv.isDeletionAllowed.setValue( field.isDeletionAllowed.getValue() );
			rv.isStatic.setValue( field.isStatic.getValue() );
			rv.isTransient.setValue( field.isTransient.getValue() );
			rv.managementLevel.setValue( field.managementLevel.getValue() );

			rv.setId( field.getId() );

			return rv;
		}
	}

	/**
	 * Helper method that determines the correct types to add all user methods
	 * used in the snippet to. This determination is based off the calling
	 * field(s) of the method in the snippet and the new field(s) for given
	 * roles.
	 *
	 * @param fieldsMap mapping of new fields to role fields
	 */
	private void setTypesForMethods( Map<AbstractField, AbstractField> fieldsMap ) {
		for( UserMethod method : this.snippetScript.getScriptType().methods ) {
			if( method.getName().equals( RemixUtilities.SCRIPT_METHOD_NAME ) ) {
				// pass
			} else {
				Set<UserField> fields = this.snippetScript.getFieldsForMethods().get( method );

				if( ( fields != null ) && ( fields.size() == 1 ) ) {
					UserField field = fields.iterator().next();

					if( field.getValueType() instanceof NamedUserType ) {
						Set<NamedUserType> typesToAdd = Sets.newHashSet();
						AbstractField newField = fieldsMap.get( field );
						typesToAdd.add( (NamedUserType)newField.getValueType() );
						this.typesForMethod.put( method, typesToAdd );
					}
				} else if( ( fields != null ) && ( fields.size() > 1 ) ) {
					List<AbstractType<?, ?, ?>> types = Lists.newArrayList();

					for( UserField field : fields ) {
						AbstractField newField = fieldsMap.get( field );
						types.add( newField.getValueType() );
					}

					AbstractType<?, ?, ?> sharedType = computeSharedType( types );

					if( sharedType instanceof NamedUserType ) {
						Set<NamedUserType> typesToAdd = Sets.newHashSet();
						if( getNewDeclaration( sharedType ) != null ) {
							sharedType = (NamedUserType)getNewDeclaration( sharedType );
						}

						typesToAdd.add( (NamedUserType)sharedType );
						this.typesForMethod.put( method, typesToAdd );
					} else {
						Set<NamedUserType> typesToAdd = Sets.newHashSet();
						for( UserField field : fields ) {
							AbstractField newField = fieldsMap.get( field );
							AbstractType<?, ?, ?> fieldType = newField.getValueType();
							if( fieldType instanceof NamedUserType ) {
								typesToAdd.add( (NamedUserType)fieldType );
							}
							this.typesForMethod.put( method, typesToAdd );
						}
					}
				} else {
					Set<NamedUserType> typesToAdd = Sets.newHashSet();
					typesToAdd.add( getProjectSceneType() );
					this.typesForMethod.put( method, typesToAdd );
				}
			}
		}
	}

	/**
	 * Helper method that gets the appropriate Edit for adding a new
	 * <code>UserField</code> to the scene.
	 *
	 * @param role role for field
	 * @param field field to add
	 * @param initStatements statements used for field initialization
	 * @param isPreview determines type of edits returned
	 * @return croquet edit used to add a new field to the scene.
	 */
	private Edit getAddFieldEdit( Role role, UserField field, BlockStatement initStatements, Boolean isPreview ) {
		if( role != null ) {
			NamedUserType sceneType = StoryApiSpecificAstUtilities.getSceneTypeFromProject( getProject() );
			Statement[] doStatements = initStatements.statements.toArray( Statement.class );

			Statement positionStatement = null;
			try {
				AffineMatrix4x4 initialTransform = RemixUtilities.getInitialTransform( role, StorytellingSceneEditor.getInstance().getActiveSceneInstance() );
				Position position = org.lgna.story.EmployeesOnly.createPosition( initialTransform.translation );
				positionStatement = SetUpMethodGenerator.createPositionStatement( false, field, position, 0 );
			} catch( CannotCreateExpressionException e ) {
				//pass
			}
			if( positionStatement != null ) {
				for( int i = 0; i < doStatements.length; i++ ) {
					Statement statement = doStatements[ i ];
					if( ( statement instanceof ExpressionStatement ) && ( ( (ExpressionStatement)statement ).expression.getValue() instanceof MethodInvocation ) ) {
						MethodInvocation mi = (MethodInvocation)( (ExpressionStatement)statement ).expression.getValue();
						AbstractMethod method = mi.method.getValue();

						if( method.getName().equals( "setPositionRelativeToVehicle" ) ) {
							doStatements[ i ] = positionStatement;
						}
					}
				}
			}

			if( isPreview ) {
				return new DeclareFieldForPreviewEdit( null, sceneType, field, doStatements );
			} else {
				return new DeclareGalleryFieldEdit( null, StorytellingSceneEditor.getInstance(), sceneType, field, doStatements, StorytellingSceneEditor.getInstance().getUndoStatementsForAddField( field ) );
			}
		} else {
			return null;
		}
	}

	/**
	 * Helper method used to determine fields required by another field during
	 * initialization. This method assumes any dependent user fields will
	 * strictly be in the arguments to a method call. Any other type of
	 * initialization statement which makes a reference to the field are not
	 * checked.
	 *
	 * @param initializationStatements statements to check
	 * @return fields required for initialization statements
	 */
	private Set<UserField> getDependentUserFields( List<Statement> initializationStatements ) {
		Set<UserField> dependentUserFields = Sets.newHashSet();

		for( Statement statement : initializationStatements ) {
			if( RemixUtilities.isStatementInvokingMethod( statement ) ) {
				MethodInvocation methodInvocation = (MethodInvocation)( (ExpressionStatement)statement ).expression.getValue();

				for( SimpleArgument argument : methodInvocation.requiredArguments ) {
					UserField dependentField = getDependentUserField( argument.expression.getValue() );
					if( dependentField != null ) {
						dependentUserFields.add( dependentField );
					}
				}
				for( SimpleArgument argument : methodInvocation.variableArguments ) {
					UserField dependentField = getDependentUserField( argument.expression.getValue() );
					if( dependentField != null ) {
						dependentUserFields.add( dependentField );
					}
				}
				for( JavaKeyedArgument argument : methodInvocation.keyedArguments ) {
					UserField dependentField = getDependentUserField( argument.expression.getValue() );
					if( dependentField != null ) {
						dependentUserFields.add( dependentField );
					}
				}
			}
		}
		return dependentUserFields;
	}

	/**
	 * Helper method to get the field from a given expression.
	 *
	 * @param expression argument expression to check for field
	 * @return field access in expression, if applicable
	 */
	private UserField getDependentUserField( Expression expression ) {

		if( expression instanceof FieldAccess ) {
			AbstractField field = ( (FieldAccess)expression ).field.getValue();

			if( field instanceof UserField ) {
				return (UserField)field;
			}
		} else if( RemixUtilities.isGetJointInvocation( expression ) ) {
			MethodInvocation jointGetter = (MethodInvocation)expression;

			if( jointGetter.expression.getValue() instanceof FieldAccess ) {
				AbstractField field = ( (FieldAccess)jointGetter.expression.getValue() ).field.getValue();

				if( field instanceof UserField ) {
					return (UserField)field;
				}
			}
		}
		return null;
	}

	/* ---------------- AST Copy Methods -------------- */

	private edu.cmu.cs.dennisc.java.util.DStack<NamedUserType> typeStack = Stacks.newStack(); // used to resolve ThisExpression when copying method to new type

	@Override
	protected GlobalFirstInstanceExpression copyGlobalFirstInstanceExpression( GlobalFirstInstanceExpression expression ) {
		GlobalFirstInstanceExpression rv = new GlobalFirstInstanceExpression( getProject().getProgramType() ); // Global first takes the remix project type

		if( rv != null ) {
			setId( expression, rv );
		}

		return rv;
	}

	@Override
	protected ThisExpression copyThisExpression( ThisExpression expression ) {
		if( this.typeStack.isEmpty() ) {
			return new ThisExpression();
		} else {
			return ThisExpression.createInstanceThatCanExistWithoutAnAncestorType( this.typeStack.peek() ); // When possible, set value type of this expression
		}
	}

	@Override
	protected LocalDeclarationStatement copyLocalDeclarationStatement( LocalDeclarationStatement declaration ) {
		LocalDeclarationStatement rv = super.copyLocalDeclarationStatement( declaration );
		if( rv.initializer.getValue().getType().equals( rv.local.getValue().getValueType() ) ) {
			//pass
		} else {
			rv.local.getValue().valueType.setValue( rv.initializer.getValue().getType() ); // Resolve type discrepancies between local and initialization expression
		}
		return rv;
	}

	@Override
	protected ArrayInstanceCreation copyArrayInstanceCreation( ArrayInstanceCreation arrayInstanceCreation ) {
		ArrayInstanceCreation rv = super.copyArrayInstanceCreation( arrayInstanceCreation );

		java.util.List<AbstractType<?, ?, ?>> expressionTypes = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();
		for( Expression expression : rv.expressions ) {
			expressionTypes.add( expression.getType() );
		}
		AbstractType<?, ?, ?> arrayType = computeSharedType( expressionTypes );
		rv.arrayType.setValue( arrayType.getArrayType() ); // Resolve type discrepancies

		return rv;
	}

	@Override
	protected MethodInvocation copyMethodInvocation( MethodInvocation methodInvocation ) {
		if( JointMethodUtilities.isJointGetter( methodInvocation.method.getValue() ) ) {
			return copyJointGetterInvocation( methodInvocation );
		} else {
			// need to account for user methods on scriptType
			AbstractMethod method = methodInvocation.method.getValue();
			if( method instanceof UserMethod ) {
				if( this.snippetScript.getScriptType().equals( method.getDeclaringType() ) ) {
					return copyScriptTypeUserMethod( methodInvocation );
				}
			}
			return super.copyMethodInvocation( methodInvocation );
		}
	}

	/**
	 * Copies the provided method off the scriptType and move it to types
	 * previously determined by {@link #setTypesForMethods(Map)}.
	 */
	private MethodInvocation copyScriptTypeUserMethod( MethodInvocation methodInvocation ) {
		UserMethod origMethod = (UserMethod)methodInvocation.method.getValue();
		Expression origExpression = methodInvocation.expression.getValue();
		UserMethod newMethod = (UserMethod)getNewDeclaration( origMethod );
		Expression newExpression = copyExpression( methodInvocation.expression.getValue() );

		Set<NamedUserType> types = typesForMethod.get( origMethod );

		if( origExpression.getType() instanceof NamedUserType ) {
			for( NamedUserType type : types ) {
				if( ( newMethod != null ) && type.methods.contains( newMethod ) ) {
					// pass
				} else {
					typeStack.push( type ); // We push the type to the stack to ensure that ThisExpressions are correctly resolved when copying the body and return type

					newMethod = new UserMethod();

					String methodName = RemixUtilities.getValidMemberName( new MethodNameValidator( type ), origMethod.getName() );
					newMethod.setName( methodName );

					newMethod.returnType.setValue( copyAbstractType( origMethod.returnType.getValue() ) );
					newMethod.requiredParameters.setValue( copyNodeCollection( origMethod.requiredParameters.getValue() ) );
					newMethod.body.setValue( copyBlockStatement( origMethod.body.getValue() ) );

					newMethod.accessLevel.setValue( origMethod.accessLevel.getValue() );
					newMethod.isDeletionAllowed.setValue( origMethod.isDeletionAllowed.getValue() );
					newMethod.isSignatureLocked.setValue( origMethod.isSignatureLocked.getValue() );
					newMethod.isStrictFloatingPoint.setValue( origMethod.isStrictFloatingPoint.getValue() );
					newMethod.isSynchronized.setValue( origMethod.isSynchronized.getValue() );
					newMethod.managementLevel.setValue( origMethod.managementLevel.getValue() );
					newMethod.isAbstract.setValue( origMethod.isAbstract() );
					newMethod.isFinal.setValue( origMethod.isFinal.getValue() );
					newMethod.isStatic.setValue( origMethod.isStatic.getValue() );
					newMethod.setId( origMethod.getId() );

					addNewDeclaration( origMethod, newMethod );
					type.methods.add( newMethod );

					typeStack.pop();
				}
			}
			// Create and return new MethodInvocation with our copied method
			SimpleArgument[] requiredArguments = copyNodeCollection( methodInvocation.requiredArguments.getValue(), SimpleArgument.class );
			SimpleArgument[] variableArguments = copyNodeCollection( methodInvocation.variableArguments.getValue(), SimpleArgument.class );
			JavaKeyedArgument[] keyedArguments = copyNodeCollection( methodInvocation.keyedArguments.getValue(), JavaKeyedArgument.class );

			MethodInvocation rv = new MethodInvocation( newExpression, newMethod, requiredArguments, variableArguments, keyedArguments );

			if( rv != null ) {
				setId( methodInvocation, rv );
			}

			return rv;
		} else {
			throw new ASTCopyException( "Unable to copy scriptType method", origMethod );
		}

	}

	/**
	 * Copies the provided joint method invocation. If a direct substitution is
	 * possible (new invoking expression is of the same type as the old
	 * expression) then simply find the appropriate declared method, otherwise
	 * the <code>DefaultJointSubstitutionManager</code> searches for a possible
	 * joint replacement.
	 */
	private MethodInvocation copyJointGetterInvocation( MethodInvocation methodInvocation ) {
		AbstractMethod origMethod = methodInvocation.method.getValue();
		AbstractMethod newMethod = (AbstractMethod)getNewDeclaration( methodInvocation.method.getValue() );
		Expression newExpression = copyExpression( methodInvocation.expression.getValue() );

		AbstractType<?, ?, ?> typeForJointReplacement = newExpression.getType();
		AbstractMethod methodForJointReplacement = null;

		// Same type, find joint method
		if( origMethod.getDeclaringType().getName().contentEquals( typeForJointReplacement.getName() ) ) {
			methodForJointReplacement = JointMethodUtilities.getJointGetter( origMethod.getName(), typeForJointReplacement );
		}
		// Different type, ask the DefaultJointSubstitutionManager for a replacement
		else if( typeForJointReplacement != null ) {
			methodForJointReplacement = DefaultJointSubstitutionManager.findJointSubstitutionFor( origMethod.getDeclaringType(), methodInvocation, typeForJointReplacement );
		}

		if( methodForJointReplacement != null ) {
			// check method with previously stored declaration (if not null)
			if( newMethod != null ) {
				if( newMethod.getName().contentEquals( methodForJointReplacement.getName() ) ) {
					//pass
				} else {
					newMethod = methodForJointReplacement;
					addNewDeclaration( origMethod, newMethod );
				}
			} else {
				newMethod = methodForJointReplacement;
				addNewDeclaration( origMethod, newMethod );
			}
		} else {
			// Theoretically, this should never happen, as we know what joint methods are used by each
			// role and only present applicable fields to fill those roles during character selection
			throw new NoJointReplacementException( "No joint replacement found for " + origMethod.getDeclaringType().getName() + " to " + typeForJointReplacement.getName() + " for " + origMethod.getName(), methodInvocation, typeForJointReplacement );
		}

		// Create and return new MethodInvocation with our new joint method
		SimpleArgument[] requiredArguments = copyNodeCollection( methodInvocation.requiredArguments.getValue(), SimpleArgument.class );
		SimpleArgument[] variableArguments = copyNodeCollection( methodInvocation.variableArguments.getValue(), SimpleArgument.class );
		JavaKeyedArgument[] keyedArguments = copyNodeCollection( methodInvocation.keyedArguments.getValue(), JavaKeyedArgument.class );

		MethodInvocation rv = new MethodInvocation( newExpression, newMethod, requiredArguments, variableArguments, keyedArguments );

		if( rv != null ) {
			setId( methodInvocation, rv );
		}

		return rv;
	}
}
