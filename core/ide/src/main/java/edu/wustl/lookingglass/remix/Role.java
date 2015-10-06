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

import java.util.Map;
import java.util.Set;

import org.lgna.project.ast.AbstractDeclaration;
import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractType;
import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.DecodeIdPolicy;
import org.lgna.project.ast.Decoder;
import org.lgna.project.ast.JavaType;
import org.lgna.project.ast.Node;
import org.lgna.project.ast.UserField;

import edu.cmu.cs.dennisc.java.util.Sets;
import edu.cmu.cs.dennisc.math.AffineMatrix4x4;
import edu.wustl.lookingglass.remix.ast.exceptions.SnippetFileException;
import edu.wustl.lookingglass.remix.roles.RoleType;

/**
 * This class describes a role element in a {@link SnippetScript}. A role must
 * be filled during the remix process. Additionally, the <code>Role</code> class
 * provides methods for encoding and decoding an xml role element.
 *
 * @author Paul Gross
 */
public final class Role {
	private UserField originField;
	private AbstractType<?, ?, ?> lowestTypeReferenced;
	private AffineMatrix4x4 initialTransformation;
	private AffineMatrix4x4 initialCameraRelativeTransformation;
	private BlockStatement initializationStatements;
	private RoleType roleType;

	private int callerCount = 0;
	private int paramCount = 0;

	private Set<AbstractMethod> jointMethods = Sets.newHashSet(); // Does not get encoded, used during remixing

	public Role( UserField originField, AbstractType<?, ?, ?> lowestTypeReferenced, RoleType roleType ) {
		this.originField = originField;

		// Fall back to field type
		if( lowestTypeReferenced == null ) {
			lowestTypeReferenced = originField.getValueType().getFirstEncounteredJavaType();
		}
		this.lowestTypeReferenced = lowestTypeReferenced;
		this.roleType = roleType;
	}

	private Role( org.w3c.dom.Element roleElement, Map<Integer, AbstractDeclaration> decodeMap ) {

		if( ( roleElement != null ) && ( roleElement.getTagName().equals( SnippetFileUtilities.ROLE_ELEMENT ) ) ) {

			// get role type
			String roleType = roleElement.getAttribute( SnippetFileUtilities.ROLE_TYPE_ATTRIBUTE );
			this.setRoleType( SnippetFileUtilities.parseRoleType( roleType ) );

			// get role counts
			String callerCount = roleElement.getAttribute( SnippetFileUtilities.ROLE_CALLER_COUNT_ATTRIBUTE );
			setCallerCount( callerCount.isEmpty() ? 0 : Integer.parseInt( callerCount, 16 ) );

			String parameterCount = roleElement.getAttribute( SnippetFileUtilities.ROLE_PARAMETER_COUNT_ATTRIBUTE );
			setParameterCount( parameterCount.isEmpty() ? 0 : Integer.parseInt( parameterCount, 16 ) );

			// get field for role
			org.w3c.dom.Element fieldElement = (org.w3c.dom.Element)roleElement.getElementsByTagName( SnippetFileUtilities.ROLE_FIELD_ELEMENT ).item( 0 );
			String fieldKey = fieldElement.getAttribute( SnippetFileUtilities.ROLE_FIELD_KEY_ATTRIBUTE );
			AbstractDeclaration fieldDeclaration = decodeMap.get( Integer.parseInt( fieldKey, 16 ) );

			this.setOriginField( (UserField)fieldDeclaration );

			// get lowest type referenced
			org.w3c.dom.Element lowestTypeElement = (org.w3c.dom.Element)roleElement.getElementsByTagName( SnippetFileUtilities.ROLE_LOWEST_TYPE_REFERENCED_ELEMENT ).item( 0 );
			String typeKey = lowestTypeElement.getAttribute( SnippetFileUtilities.ROLE_LOWEST_TYPE_REFERENCED_KEY_ATTRIBUTE );

			AbstractType<?, ?, ?> lowestTypeReferenced = null;
			if( typeKey.isEmpty() ) {
				String lowestType = lowestTypeElement.getTextContent();
				try {
					lowestTypeReferenced = JavaType.getInstance( Class.forName( lowestType ) );
				} catch( ClassNotFoundException e ) {
					throw new SnippetFileException( "Invalid lowestTypeReferenced element" );
				}
			} else {
				AbstractDeclaration typeDeclaration = decodeMap.get( Integer.parseInt( typeKey, 16 ) );
				lowestTypeReferenced = (AbstractType<?, ?, ?>)typeDeclaration;
			}

			if( lowestTypeReferenced != null ) {
				this.setLowestTypeReferenced( lowestTypeReferenced );
			} else {
				throw new SnippetFileException( "Invalid lowestTypeReferenced element" );
			}

			// get

			// get initial transformation
			org.w3c.dom.Element initTransformElement = (org.w3c.dom.Element)roleElement.getElementsByTagName( SnippetFileUtilities.ROLE_INITIAL_TRANSFORM_ELEMENT ).item( 0 );
			if( initTransformElement != null ) {
				String initTransform = initTransformElement.getTextContent();
				this.setInitialTransformation( AffineMatrix4x4.createFromColumnMajorArray16( SnippetFileUtilities.parseDoubleArray( initTransform ) ) );
			}

			// get initialCameraTranformation
			org.w3c.dom.Element cameraRelativeElement = (org.w3c.dom.Element)roleElement.getElementsByTagName( SnippetFileUtilities.ROLE_INITIAL_CAMERA_RELATIVE_TRANSFORM_ELEMENT ).item( 0 );
			if( cameraRelativeElement != null ) {
				String cameraRelative = cameraRelativeElement.getTextContent();
				this.setInitialCameraRelativeTransformation( AffineMatrix4x4.createFromColumnMajorArray16( SnippetFileUtilities.parseDoubleArray( cameraRelative ) ) );
			}

			// get initializationStatements
			org.w3c.dom.Element initStatementsElement = (org.w3c.dom.Element)roleElement.getElementsByTagName( SnippetFileUtilities.ROLE_INITIALIZATION_STATEMENTS_ELEMENT ).item( 0 );
			if( initStatementsElement != null ) {
				Decoder decoder = new Decoder( null, null, DecodeIdPolicy.PRESERVE_IDS );
				Node astNode = decoder.decode( (org.w3c.dom.Element)initStatementsElement.getFirstChild(), decodeMap );
				this.setInitializationStatements( (BlockStatement)astNode );
			}
		} else {
			throw new SnippetFileException( "Invalid role element" );
		}
	}

	public String getName() {
		return this.originField.getName();
	}

	public void addJointMethod( AbstractMethod method ) {
		this.jointMethods.add( method );
	}

	public java.util.Set<AbstractMethod> getJointMethods() {
		return this.jointMethods;
	}

	/* package-private */void setRoleType( RoleType roleType ) {
		this.roleType = roleType;
	}

	public RoleType getRoleType() {
		return this.roleType;
	}

	/* package-private */void setOriginField( UserField originField ) {
		this.originField = originField;
	}

	public UserField getOriginField() {
		return this.originField;
	}

	/* package-private */void setLowestTypeReferenced( AbstractType<?, ?, ?> type ) {
		this.lowestTypeReferenced = type;
	}

	public AbstractType<?, ?, ?> getLowestTypeReferenced() {
		return this.lowestTypeReferenced;
	}

	/* package-private */void setInitialTransformation( AffineMatrix4x4 transform ) {
		this.initialTransformation = transform;
	}

	public AffineMatrix4x4 getInitialTransformation() {
		return this.initialTransformation;
	}

	/* package-private */void setInitialCameraRelativeTransformation( AffineMatrix4x4 transform ) {
		this.initialCameraRelativeTransformation = transform;
	}

	public AffineMatrix4x4 getInitialCameraRelativeTransformation() {
		return this.initialCameraRelativeTransformation;
	}

	/* package-private */void setInitializationStatements( BlockStatement statement ) {
		initializationStatements = statement;
	}

	public BlockStatement getInitializationStatements() {
		return this.initializationStatements;
	}

	public void setCallerCount( int callerCount ) {
		this.callerCount = callerCount;
	}

	public int getCallerCount() {
		return this.callerCount;
	}

	public void setParameterCount( int paramCount ) {
		this.paramCount = paramCount;
	}

	public int getParameterCount() {
		return this.paramCount;
	}

	public static Role decode( org.w3c.dom.Element roleElement, Map<Integer, AbstractDeclaration> decodeMap ) {
		return new Role( roleElement, decodeMap );
	}

	public org.w3c.dom.Element encode( org.w3c.dom.Document doc, Map<AbstractDeclaration, Integer> encodeMap ) {
		org.w3c.dom.Element roleElement = doc.createElement( SnippetFileUtilities.ROLE_ELEMENT );
		roleElement.setAttribute( SnippetFileUtilities.ROLE_NAME_ATTRIBUTE, getName() );

		// set role type
		roleElement.setAttribute( SnippetFileUtilities.ROLE_TYPE_ATTRIBUTE, getRoleType().toString() );

		// set role counts
		roleElement.setAttribute( SnippetFileUtilities.ROLE_CALLER_COUNT_ATTRIBUTE, Integer.toHexString( getCallerCount() ) );
		roleElement.setAttribute( SnippetFileUtilities.ROLE_PARAMETER_COUNT_ATTRIBUTE, Integer.toHexString( getParameterCount() ) );

		// set field for role
		org.w3c.dom.Element fieldElement = doc.createElement( SnippetFileUtilities.ROLE_FIELD_ELEMENT );
		Integer fieldMapKey = encodeMap.get( this.getOriginField() );
		String fieldKey = Integer.toHexString( fieldMapKey );

		fieldElement.setAttribute( SnippetFileUtilities.ROLE_FIELD_KEY_ATTRIBUTE, fieldKey );
		roleElement.appendChild( fieldElement );

		// set lowest type referenced
		org.w3c.dom.Element lowestTypeElement = doc.createElement( SnippetFileUtilities.ROLE_LOWEST_TYPE_REFERENCED_ELEMENT );
		Integer typeMapKey = encodeMap.get( this.getLowestTypeReferenced() );

		if( typeMapKey != null ) {
			String typeKey = Integer.toHexString( typeMapKey );
			lowestTypeElement.setAttribute( SnippetFileUtilities.ROLE_LOWEST_TYPE_REFERENCED_KEY_ATTRIBUTE, typeKey );
		} else {
			if( this.getLowestTypeReferenced() instanceof JavaType ) {
				JavaType javaType = (JavaType)this.getLowestTypeReferenced();
				String lowestType = javaType.getClassReflectionProxy().getReification().getName();
				lowestTypeElement.setTextContent( lowestType );
			} else {
				throw new SnippetFileException( "Invalid lowestTypeReferenced for role " + getName() + ": " + getLowestTypeReferenced() );
			}
		}
		roleElement.appendChild( lowestTypeElement );

		// set initial transformation
		if( this.getInitialTransformation() != null ) {
			String transform = java.util.Arrays.toString( this.getInitialTransformation().getAsColumnMajorArray16() );
			SnippetFileUtilities.createAndAppendChild( doc, roleElement, SnippetFileUtilities.ROLE_INITIAL_TRANSFORM_ELEMENT, transform );
		}

		// set initialCameraTranformation
		if( this.getInitialCameraRelativeTransformation() != null ) {
			String cameraTransform = java.util.Arrays.toString( this.getInitialCameraRelativeTransformation().getAsColumnMajorArray16() );
			SnippetFileUtilities.createAndAppendChild( doc, roleElement, SnippetFileUtilities.ROLE_INITIAL_CAMERA_RELATIVE_TRANSFORM_ELEMENT, cameraTransform );
		}

		// set initializationStatements
		if( this.getInitializationStatements() != null ) {
			org.w3c.dom.Element setupNode = doc.createElement( SnippetFileUtilities.ROLE_INITIALIZATION_STATEMENTS_ELEMENT );
			setupNode.appendChild( getInitializationStatements().encode( doc, encodeMap ) );
			roleElement.appendChild( setupNode );
		}
		return roleElement;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( "Role: " );
		sb.append( this.getName() );
		return sb.toString();
	}
}
