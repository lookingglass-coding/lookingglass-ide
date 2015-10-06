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
package edu.wustl.lookingglass.community.models;

import edu.wustl.lookingglass.community.api.fixtures.ModelFieldFixture;

/**
 * @author Kyle J. Harms
 */
public class ModelField extends FixtureModel<ModelFieldFixture> {

	private static java.util.List<ModelField> modelFields;
	private static java.util.Map<Integer, ModelField> modelFieldsMap;
	private static java.util.Map<String, ModelField> typeMap = new java.util.HashMap<String, ModelField>();

	protected ModelField parent = null;

	public ModelField( ModelFieldFixture fixture ) {
		super( fixture );
	}

	public ModelField getParent() {
		return this.parent;
	}

	public static void initializeModelFields( java.util.List<ModelField> modelFields, java.util.Map<Integer, ModelField> modelFieldsMap ) {
		ModelField.modelFields = modelFields;
		ModelField.modelFieldsMap = modelFieldsMap;

		for( ModelField modelField : modelFields ) {
			// Set parent for each model
			Integer parentId = modelField.getFixture().parent_id;
			if( parentId == null ) {
				modelField.parent = null;
			} else {
				modelField.parent = modelFieldsMap.get( parentId );
			}

			// Add model to hash map
			ModelField.typeMap.put( modelField.getFixture().model_class, modelField );
		}
	}

	public static Integer getModelFieldId( String modelFieldKey ) {
		ModelField field = ModelField.typeMap.get( modelFieldKey );
		if( field == null ) {
			return null;
		} else {
			return field.getFixture().id;
		}
	}

	public static Integer getModelFieldId( org.lgna.project.ast.AbstractType<?, ?, ?> valueType ) {
		assert valueType != null;
		return getModelFieldId( getModelFieldKey( valueType ) );
	}

	public static class ModelFieldKeys {
		private final String modelFieldType;
		private final String abstractModelFieldType;

		private final Integer callerStatementCount;
		private final Integer parameterCount;

		public ModelFieldKeys( String modelFieldType, String abstractModelFieldType, Integer callerStatementCount, Integer parameterCount ) {
			this.modelFieldType = modelFieldType;
			this.abstractModelFieldType = abstractModelFieldType;
			this.callerStatementCount = callerStatementCount;
			this.parameterCount = parameterCount;
		}

		public ModelFieldKeys( String modelFieldType ) {
			this( modelFieldType, null, null, null );
		}

		public String getModelFieldType() {
			return this.modelFieldType;
		}

		public String getAbstractModelFieldType() {
			return this.abstractModelFieldType;
		}

		public Integer getCallerStatementCount() {
			return this.callerStatementCount;
		}

		public Integer getParameterCount() {
			return this.parameterCount;
		}
	}

	public static String getModelFieldKey( Class<?> cls ) {
		return cls != null ? cls.getName() : "null";
	}

	public static String getModelFieldKey( org.lgna.project.ast.JavaType javaType ) {
		return getModelFieldKey( javaType != null ? javaType.getClassReflectionProxy().getReification() : null );
	}

	public static String getModelFieldKey( org.lgna.project.ast.AbstractType<?, ?, ?> valueType ) {
		if( valueType instanceof org.lgna.project.ast.NamedUserType ) {
			org.lgna.project.ast.NamedUserType namedUserType = (org.lgna.project.ast.NamedUserType)valueType;
			if( valueType.isAssignableTo( org.lgna.story.SJointedModel.class ) ) {
				org.lgna.project.ast.JavaType resourceType = org.alice.ide.typemanager.ResourceTypeUtilities.getResourceType( namedUserType );
				assert resourceType != null : valueType;
				return ModelField.getModelFieldKey( resourceType );
			} else {
				//todo
				return ModelField.getModelFieldKey( valueType.getFirstEncounteredJavaType() );
			}
		} else if( valueType instanceof org.lgna.project.ast.JavaType ) {
			org.lgna.project.ast.JavaType javaType = (org.lgna.project.ast.JavaType)valueType;
			return ModelField.getModelFieldKey( javaType );
		} else {
			return null;
		}
	}

	public static String getModelFieldKey( org.lgna.project.ast.UserField field ) {
		return getModelFieldKey( field.getValueType() );
	}

	public static int[] getWorldFieldIds( org.lgna.project.Project project ) {
		ModelFieldKeys[] keys = getWorldFieldKeys( project );
		int[] ids = new int[ keys.length ];
		for( int i = 0; i < keys.length; i++ ) {
			String key = keys[ i ].getModelFieldType();
			Integer id = getModelFieldId( key );
			if( id != null ) {
				ids[ i ] = id;
			}
		}
		return ids;
	}

	public static ModelFieldKeys[] getWorldFieldKeys( org.lgna.project.Project project ) {
		java.util.List<ModelFieldKeys> fields = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();

		// TODO: this needs caller and parameter count added to it...
		fields.add( new ModelFieldKeys( getModelFieldKey( org.alice.stageide.ast.StoryApiSpecificAstUtilities.getSceneFieldFromProgramType( project.getProgramType() ) ) ) );
		for( org.lgna.project.ast.NamedUserType type : project.getNamedUserTypes() ) {
			if( type.isAssignableTo( org.lgna.story.SScene.class ) ) {
				for( org.lgna.project.ast.UserField field : type.fields ) {
					fields.add( new ModelFieldKeys( getModelFieldKey( field ) ) );
				}
			}
		}

		ModelFieldKeys[] fieldsArray = new ModelFieldKeys[ fields.size() ];
		return fields.toArray( fieldsArray );
	}

	public static ModelFieldKeys[] getSnippetFieldKeys( edu.wustl.lookingglass.remix.SnippetScript snippet ) {
		java.util.List<ModelFieldKeys> fields = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();

		for( edu.wustl.lookingglass.remix.Role role : snippet.getLiveActorRoles() ) {
			org.lgna.project.ast.UserField field = role.getOriginField();
			org.lgna.project.ast.AbstractType<?, ?, ?> requiredType = role.getLowestTypeReferenced();

			fields.add( new ModelFieldKeys( getModelFieldKey( field ), getModelFieldKey( requiredType ), role.getCallerCount(), role.getParameterCount() ) );
		}

		ModelFieldKeys[] fieldsArray = new ModelFieldKeys[ fields.size() ];
		return fields.toArray( fieldsArray );
	}
}
