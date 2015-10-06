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

import java.io.IOException;
import java.io.OutputStream;

import org.lgna.project.Project;
import org.lgna.project.ast.AbstractDeclaration;
import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.DecodeIdPolicy;
import org.lgna.project.ast.Decoder;
import org.lgna.project.ast.NamedUserType;
import org.lgna.project.ast.Node;
import org.lgna.project.ast.UserField;
import org.lgna.project.io.IoUtilities;
import org.lgna.project.io.MigrationManagerDecodedVersionPair;
import org.lgna.project.migration.LgAstMigrationManager;
import org.lgna.project.migration.LgStoryApiMigrationManager;
import org.lgna.project.migration.ProjectMigrationManager;
import org.lgna.project.migration.ast.AstMigrationUtilities;
import org.w3c.dom.Element;

import edu.cmu.cs.dennisc.java.util.Maps;
import edu.cmu.cs.dennisc.xml.XMLUtilities;
import edu.wustl.lookingglass.common.VersionNumber;
import edu.wustl.lookingglass.remix.roles.RoleType;

public class SnippetFileUtilities {

	private static final String SCRIPT_TYPE_ENTRY_NAME = "script.xml";
	public static final VersionNumber SNIPPET_VERSION = new VersionNumber( "2.0.0" );

	public static final String SNIPPET_EXTENSION = "lgr";
	public static final String SNIPPET_MIMETYPE = "application/x-lookingglass-remix";

	public static final String SNIPPET_ELEMENT = "snippet";
	public static final String SNIPPET_NAME_ATTRIBUTE = "name";
	public static final String SNIPPET_DESCRIPTION_ATTRIBUTE = "description";
	public static final String SNIPPET_VERSION_ATTRIBUTE = "version";

	public static final String SNIPPET_SCRIPT_ELEMENT = "script";
	public static final String SNIPPET_ROLES_ELEMENT = "roles";
	public static final String SNIPPET_METHOD_REFERENCES_ELEMENT = "referencedMethods";
	public static final String SNIPPET_INITIAL_CAMERA_TRANSFORM_ELEMENT = "initialCameraTransform";
	public static final String SNIPPET_NODE_MARKER_ELEMENT = "nodeMarkers";

	public static final String START_NODE_ELEMENT = "startNode";
	public static final String END_NODE_ELEMENT = "endNode";
	public static final String MARKER_NODE_ID_ATTRIBUTE = "uuid";
	public static final String MARKER_NODE_EXECUTION_COUNT_ATTRIBUTE = "executionCount";

	public static final String METHOD_ELEMENT = "method";
	public static final String METHOD_KEY_ATTRIBUTE = "key";
	public static final String METHOD_FIELD_REFERENCES_ELEMENT = "referencedFields";
	public static final String METHOD_FIELD_ELEMENT = "field";
	public static final String METHOD_FIELD_KEY_ATTRIBUTE = "key";

	public static final String ROLE_ELEMENT = "role";
	public static final String ROLE_TYPE_ATTRIBUTE = "roleType";
	public static final String ROLE_CALLER_COUNT_ATTRIBUTE = "callerCount";
	public static final String ROLE_PARAMETER_COUNT_ATTRIBUTE = "parameterCount";
	public static final String ROLE_NAME_ATTRIBUTE = "name";
	public static final String ROLE_FIELD_ELEMENT = "field";
	public static final String ROLE_FIELD_KEY_ATTRIBUTE = "key";
	public static final String ROLE_LOWEST_TYPE_REFERENCED_ELEMENT = "lowestTypeReferenced";
	public static final String ROLE_LOWEST_TYPE_REFERENCED_KEY_ATTRIBUTE = "key";
	public static final String ROLE_INITIAL_TRANSFORM_ELEMENT = "initialTransform";
	public static final String ROLE_INITIAL_CAMERA_RELATIVE_TRANSFORM_ELEMENT = "initialCameraRelativeTransform";
	public static final String ROLE_INITIALIZATION_STATEMENTS_ELEMENT = "initializationStatements";

	public static void writeSnippet( java.io.OutputStream outputStream, SnippetScript snippetScript ) throws IOException {

		java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream( outputStream );

		IoUtilities.writeVersions( zos );

		org.w3c.dom.Document scriptDoc = encodeSnippet( snippetScript );
		SnippetFileUtilities.writeXML( zos, scriptDoc, SCRIPT_TYPE_ENTRY_NAME );

		if( !snippetScript.getScriptResources().isEmpty() ) {
			IoUtilities.writeResources( zos, snippetScript.getScriptResources() );
		}

		// Add the project to the zip as well
		org.alice.ide.IDE ide = org.alice.ide.IDE.getActiveInstance();
		if( ( ide != null ) && ( ide.getProject() != null ) ) {
			edu.cmu.cs.dennisc.java.util.zip.ZipUtilities.write( zos, new edu.cmu.cs.dennisc.java.util.zip.DataSource() {
				@Override
				public String getName() {
					return "project.lgp";
				}

				@Override
				public void write( OutputStream os ) throws IOException {
					java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
					org.lgna.project.io.IoUtilities.writeProject( baos, edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().getProject(), edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().getAdditionalDataSources() );
					baos.flush();
					byte[] bytes = baos.toByteArray();
					os.write( bytes );
				}
			} );
		}

		zos.flush();
		zos.close();
	}

	private static org.w3c.dom.Document encodeSnippet( SnippetScript snippetScript ) {
		org.w3c.dom.Document doc = XMLUtilities.createDocument();
		org.w3c.dom.Element snippetElement = doc.createElement( SNIPPET_ELEMENT );

		snippetElement.setAttribute( SNIPPET_NAME_ATTRIBUTE, snippetScript.getTitle() );
		snippetElement.setAttribute( SNIPPET_DESCRIPTION_ATTRIBUTE, snippetScript.getDescription() );
		snippetElement.setAttribute( SNIPPET_VERSION_ATTRIBUTE, SNIPPET_VERSION.toString() );

		java.util.Map<AbstractDeclaration, Integer> encodeMap = Maps.newHashMap();

		// encoding the roles and referenced methods depends on the declarations generated while encoding the script
		snippetElement.appendChild( encodeCode( doc, snippetScript, encodeMap ) );
		snippetElement.appendChild( encodeRoles( doc, snippetScript, encodeMap ) );
		snippetElement.appendChild( encodeMethods( doc, snippetScript, encodeMap ) );
		snippetElement.appendChild( encodeInitialCameraTransform( doc, snippetScript ) );
		snippetElement.appendChild( encodeMarkerNodes( doc, snippetScript ) );

		doc.appendChild( snippetElement );
		return doc;
	}

	private static org.w3c.dom.Element encodeCode( org.w3c.dom.Document doc, SnippetScript snippetScript, java.util.Map<AbstractDeclaration, Integer> encodeMap ) {
		org.w3c.dom.Element scriptElement = doc.createElement( SNIPPET_SCRIPT_ELEMENT );

		// encode snippet scriptType
		org.w3c.dom.Element scriptTypeElement = snippetScript.getScriptType().encode( doc, encodeMap );
		scriptElement.appendChild( scriptTypeElement );

		return scriptElement;
	}

	private static org.w3c.dom.Element encodeMethods( org.w3c.dom.Document doc, SnippetScript snippetScript, java.util.Map<AbstractDeclaration, Integer> encodeMap ) {
		org.w3c.dom.Element refMethodsElement = doc.createElement( SNIPPET_METHOD_REFERENCES_ELEMENT );

		for( AbstractMethod method : snippetScript.getFieldsForMethods().keySet() ) {
			org.w3c.dom.Element methodElement = doc.createElement( METHOD_ELEMENT );

			String methodKey = Integer.toHexString( encodeMap.get( method ) );
			methodElement.setAttribute( METHOD_KEY_ATTRIBUTE, methodKey );

			org.w3c.dom.Element refFieldsElement = doc.createElement( METHOD_FIELD_REFERENCES_ELEMENT );

			for( UserField field : snippetScript.getFieldsForMethods().get( method ) ) {
				org.w3c.dom.Element fieldElement = doc.createElement( METHOD_FIELD_ELEMENT );

				String fieldKey = Integer.toHexString( encodeMap.get( field ) );
				fieldElement.setAttribute( METHOD_FIELD_KEY_ATTRIBUTE, fieldKey );
				refFieldsElement.appendChild( fieldElement );
			}
			methodElement.appendChild( refFieldsElement );
			refMethodsElement.appendChild( methodElement );
		}

		return refMethodsElement;
	}

	private static org.w3c.dom.Node encodeRoles( org.w3c.dom.Document doc, SnippetScript snippetScript, java.util.Map<AbstractDeclaration, Integer> encodeMap ) {
		org.w3c.dom.Element rolesElement = doc.createElement( SNIPPET_ROLES_ELEMENT );

		for( Role role : snippetScript.getAllRoles() ) {
			rolesElement.appendChild( role.encode( doc, encodeMap ) );
		}

		return rolesElement;
	}

	private static org.w3c.dom.Node encodeInitialCameraTransform( org.w3c.dom.Document doc, SnippetScript snippetScript ) {
		org.w3c.dom.Element initTransformElement = doc.createElement( SNIPPET_INITIAL_CAMERA_TRANSFORM_ELEMENT );

		String initialCameraTransform = java.util.Arrays.toString( snippetScript.getInitialCameraTransform().getAsColumnMajorArray16() );
		initTransformElement.appendChild( doc.createTextNode( initialCameraTransform ) );

		return initTransformElement;
	}

	private static org.w3c.dom.Node encodeMarkerNodes( org.w3c.dom.Document doc, SnippetScript snippetScript ) {
		org.w3c.dom.Element markerNodesElement = doc.createElement( SNIPPET_NODE_MARKER_ELEMENT );

		org.w3c.dom.Element startNode = doc.createElement( START_NODE_ELEMENT );
		org.w3c.dom.Element endNode = doc.createElement( END_NODE_ELEMENT );

		startNode.setAttribute( MARKER_NODE_ID_ATTRIBUTE, snippetScript.getBeginNodeUUID().toString() );
		endNode.setAttribute( MARKER_NODE_ID_ATTRIBUTE, snippetScript.getEndNodeUUID().toString() );

		startNode.setAttribute( MARKER_NODE_EXECUTION_COUNT_ATTRIBUTE, Integer.toHexString( snippetScript.getBeginNodeExecutionCount() ) );
		endNode.setAttribute( MARKER_NODE_EXECUTION_COUNT_ATTRIBUTE, Integer.toHexString( snippetScript.getEndNodeExecutionCount() ) );

		markerNodesElement.appendChild( startNode );
		markerNodesElement.appendChild( endNode );

		return markerNodesElement;
	}

	/* package-private */static void createAndAppendChild( org.w3c.dom.Document doc, org.w3c.dom.Element parent, String elementName, String elementValue ) {
		org.w3c.dom.Element temp = doc.createElement( elementName );
		temp.appendChild( doc.createTextNode( elementValue ) );
		parent.appendChild( temp );
	}

	private static void writeXML( java.util.zip.ZipOutputStream zos, org.w3c.dom.Document xmlDocument, String entryName ) throws java.io.IOException {
		java.util.zip.ZipEntry programTypeEntry = new java.util.zip.ZipEntry( entryName );
		zos.putNextEntry( programTypeEntry );
		edu.cmu.cs.dennisc.xml.XMLUtilities.write( xmlDocument, zos );

		zos.closeEntry();
		zos.flush();
	}

	public static SnippetScript loadSnippet( String scriptPath ) throws IOException {
		java.io.FileInputStream fis = new java.io.FileInputStream( scriptPath );
		return loadSnippet( fis );
	}

	public static SnippetScript loadSnippet( java.io.InputStream inputStream ) throws IOException {
		java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream( inputStream );
		IoUtilities.ZipInputStreamEntryContainer zipEntryContainer = new IoUtilities.ZipInputStreamEntryContainer( zis );

		MigrationManagerDecodedVersionPair[] migrationManagerDecodedVersionPairs = IoUtilities.getMigrationManagerDecodedVersionPairs( zipEntryContainer,
				ProjectMigrationManager.LG_ABSENT_VERSION_FOR_REMIXES,
				LgAstMigrationManager.LG_ABSENT_VERSION,
				LgStoryApiMigrationManager.LG_ABSENT_VERSION );

		org.w3c.dom.Document xmlDocument = IoUtilities.readXML( zipEntryContainer, SCRIPT_TYPE_ENTRY_NAME, migrationManagerDecodedVersionPairs );
		SnippetScript snippetScript = SnippetFileUtilities.decodeScript( xmlDocument, migrationManagerDecodedVersionPairs );

		snippetScript.setScriptResources( IoUtilities.readResources( zipEntryContainer ) );
		zis.close();
		return snippetScript;
	}

	private static SnippetScript decodeScript( org.w3c.dom.Document scriptDoc, MigrationManagerDecodedVersionPair[] migrationManagerDecodedVersionPairs ) {

		SnippetScript snippetScript = new SnippetScript();
		org.w3c.dom.Element rootElement = scriptDoc.getDocumentElement();

		// check snippet version
		String version = rootElement.getAttribute( SNIPPET_VERSION_ATTRIBUTE );
		if( version.isEmpty() ) {
			version = "1.0.0";
		}
		VersionNumber decodedVersion = new VersionNumber( version );

		if( SNIPPET_VERSION.getMajor() == decodedVersion.getMajor() ) {
			//pass
		} else {
			throw new edu.wustl.lookingglass.remix.ast.exceptions.IncompatibleSnippetVersionException( decodedVersion );
		}

		snippetScript.setTitle( rootElement.getAttribute( SNIPPET_NAME_ATTRIBUTE ) );
		snippetScript.setDescription( rootElement.getAttribute( SNIPPET_DESCRIPTION_ATTRIBUTE ) );

		org.w3c.dom.Element startNodeElement = (Element)rootElement.getElementsByTagName( START_NODE_ELEMENT ).item( 0 );
		org.w3c.dom.Element endNodeElement = (Element)rootElement.getElementsByTagName( START_NODE_ELEMENT ).item( 0 );

		snippetScript.setBeginNodeUUID( java.util.UUID.fromString( startNodeElement.getAttribute( MARKER_NODE_ID_ATTRIBUTE ) ) );
		snippetScript.setEndNodeUUID( java.util.UUID.fromString( endNodeElement.getAttribute( MARKER_NODE_ID_ATTRIBUTE ) ) );

		snippetScript.setBeginNodeExecutionCount( Integer.parseInt( startNodeElement.getAttribute( MARKER_NODE_EXECUTION_COUNT_ATTRIBUTE ), 16 ) );
		snippetScript.setEndNodeExecutionCount( Integer.parseInt( endNodeElement.getAttribute( MARKER_NODE_EXECUTION_COUNT_ATTRIBUTE ), 16 ) );

		java.util.Map<Integer, AbstractDeclaration> decodeMap = new java.util.HashMap<Integer, AbstractDeclaration>();
		Decoder decoder = new org.lgna.project.ast.Decoder( null, null, DecodeIdPolicy.PRESERVE_IDS );

		// decode the scriptType
		org.w3c.dom.Element scriptElement = (org.w3c.dom.Element)rootElement.getElementsByTagName( SNIPPET_SCRIPT_ELEMENT ).item( 0 );
		Node astNode = decoder.decode( (org.w3c.dom.Element)scriptElement.getFirstChild(), decodeMap );

		Project project = org.alice.ide.ProjectStack.peekProject();
		AstMigrationUtilities.migrateNode( astNode, project, migrationManagerDecodedVersionPairs );
		snippetScript.setScriptType( (NamedUserType)astNode );

		org.w3c.dom.NodeList roles = rootElement.getElementsByTagName( ROLE_ELEMENT );
		for( int i = 0; i < roles.getLength(); i++ ) {
			org.w3c.dom.Element roleElement = (org.w3c.dom.Element)roles.item( i );
			Role role = Role.decode( roleElement, decodeMap );

			if( role.getInitializationStatements() != null ) {
				org.lgna.project.migration.ast.AstMigrationUtilities.migrateNode( role.getInitializationStatements(), project, migrationManagerDecodedVersionPairs );
			}

			snippetScript.addRole( role, role.getRoleType() );
		}

		org.w3c.dom.Element methodRootElement = (org.w3c.dom.Element)rootElement.getElementsByTagName( SNIPPET_METHOD_REFERENCES_ELEMENT ).item( 0 );
		org.w3c.dom.NodeList methods = methodRootElement.getElementsByTagName( METHOD_ELEMENT );
		java.util.Map<AbstractMethod, java.util.Set<UserField>> fieldsForMethods = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();

		for( int i = 0; i < methods.getLength(); i++ ) {
			org.w3c.dom.Element methodElement = (org.w3c.dom.Element)methods.item( i );

			int methodKey = Integer.parseInt( methodElement.getAttribute( METHOD_KEY_ATTRIBUTE ), 16 );
			org.lgna.project.ast.AbstractMethod method = (AbstractMethod)decodeMap.get( methodKey );
			java.util.Set<UserField> fieldsSet = edu.cmu.cs.dennisc.java.util.Sets.newHashSet();

			org.w3c.dom.NodeList fields = methodElement.getElementsByTagName( METHOD_FIELD_ELEMENT );
			for( int j = 0; j < fields.getLength(); j++ ) {
				org.w3c.dom.Element fieldElement = (org.w3c.dom.Element)fields.item( j );

				int fieldKey = Integer.parseInt( fieldElement.getAttribute( METHOD_FIELD_KEY_ATTRIBUTE ), 16 );
				UserField field = (UserField)decodeMap.get( fieldKey );
				fieldsSet.add( field );
			}
			fieldsForMethods.put( method, fieldsSet );
		}

		snippetScript.setFieldsForMethods( fieldsForMethods );

		org.w3c.dom.Element initTransformElement = (org.w3c.dom.Element)rootElement.getElementsByTagName( SnippetFileUtilities.SNIPPET_INITIAL_CAMERA_TRANSFORM_ELEMENT ).item( 0 );
		String transformString = initTransformElement.getTextContent();

		if( transformString.isEmpty() ) {
			//pass
		} else {
			snippetScript.setInitialCameraTransform( edu.cmu.cs.dennisc.math.AffineMatrix4x4.createFromColumnMajorArray16( SnippetFileUtilities.parseDoubleArray( transformString ) ) );
		}

		return snippetScript;
	}

	/* package-private */static double[] parseDoubleArray( String arrayString ) {

		String[] values = arrayString.substring( 1, arrayString.length() - 1 ).split( "," );
		double[] doubleArray = new double[ values.length ];

		for( int i = 0; i < doubleArray.length; i++ ) {
			doubleArray[ i ] = Double.parseDouble( values[ i ] );
		}

		return doubleArray;
	}

	/* package-private */static RoleType parseRoleType( String roleType ) {
		if( roleType.equals( RoleType.ACTIVE.toString() ) ) {
			return RoleType.ACTIVE;
		} else {
			return edu.wustl.lookingglass.remix.roles.RoleType.SPECIAL;
		}
	}
}
