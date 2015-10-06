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
import java.util.UUID;

import org.lgna.common.ComponentThread;
import org.lgna.common.Resource;
import org.lgna.project.ProjectMain;
import org.lgna.project.ast.AbstractField;
import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.NamedUserType;
import org.lgna.project.ast.UserField;
import org.lgna.project.ast.UserMethod;

import edu.cmu.cs.dennisc.java.util.Maps;
import edu.cmu.cs.dennisc.java.util.Sets;
import edu.cmu.cs.dennisc.math.AffineMatrix4x4;
import edu.wustl.lookingglass.remix.ast.RemixUtilities;
import edu.wustl.lookingglass.remix.roles.RoleType;

/**
 * the <code>SnippetScript</code> class represents a snippet used during the
 * remix process. The information stored can be used to recreate the snippet
 * code and provides a means of substituting new fields for snippet roles.
 *
 * @author Michael Pogran
 */
public class SnippetScript implements ProjectMain {
	private Integer communityId;
	private Integer communityWorldId;

	private String title;
	private String description;

	private double startTime;
	private double endTime;
	private ComponentThread executingThread;

	private Map<AbstractMethod, Set<UserField>> fieldsForMethods = Maps.newHashMap();
	private Map<RoleType, Set<Role>> roles = Maps.newHashMap();

	private Set<UserField> newFields = Sets.newHashSet();
	private Map<AbstractField, Role> roleForFields = Maps.newHashMap();

	private AffineMatrix4x4 initialCameraTransform = null;
	private Set<Resource> scriptResources = null;

	private UserMethod rootMethod;
	private NamedUserType scriptType;

	// These variables can be used to recreate this Snippet from its original World
	private UUID beginNodeUUID;
	private UUID endNodeUUID;
	private int beginNodeExecutionCount = 1;
	private int endNodeExecutionCount = 1;

	public SnippetScript() {
		this.roles.put( RoleType.ACTIVE, new java.util.HashSet<Role>() );
		this.roles.put( RoleType.SPECIAL, new java.util.HashSet<Role>() );
	}

	protected void setScriptType( NamedUserType type ) {
		this.scriptType = type;
		this.rootMethod = (UserMethod)type.getDeclaredMethod( RemixUtilities.SCRIPT_METHOD_NAME );
	}

	@Override
	public org.lgna.project.ast.UserMethod getMainMethod() {
		return this.rootMethod;
	}

	public Integer getCommunityId() {
		return this.communityId;
	}

	public void setCommunityId( Integer communityId ) {
		this.communityId = communityId;
	}

	public Integer getCommunityWorldId() {
		return this.communityWorldId;
	}

	public void setCommunityWorldId( Integer communityWorldId ) {
		this.communityWorldId = communityWorldId;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle( String title ) {
		this.title = title;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription( String description ) {
		this.description = description;
	}

	public double getStartTime() {
		return this.startTime;
	}

	public void setStartTime( double startTime ) {
		this.startTime = startTime;
	}

	public double getEndTime() {
		return this.endTime;
	}

	public void setEndTime( double endTime ) {
		this.endTime = endTime;
	}

	public void setBeginNodeUUID( UUID startNodeUUID ) {
		this.beginNodeUUID = startNodeUUID;
	}

	public UUID getBeginNodeUUID() {
		return this.beginNodeUUID;
	}

	public void setEndNodeUUID( UUID endNodeUUID ) {
		this.endNodeUUID = endNodeUUID;
	}

	public UUID getEndNodeUUID() {
		return this.endNodeUUID;
	}

	public void setBeginNodeExecutionCount( int startNodeExecutionCount ) {
		this.beginNodeExecutionCount = startNodeExecutionCount;
	}

	public int getBeginNodeExecutionCount() {
		return this.beginNodeExecutionCount;
	}

	public void setEndNodeExecutionCount( int endNodeExecutionCount ) {
		this.endNodeExecutionCount = endNodeExecutionCount;
	}

	public int getEndNodeExecutionCount() {
		return this.endNodeExecutionCount;
	}

	public ComponentThread getExecutingThread() {
		return this.executingThread;
	}

	public void setExecutingThread( ComponentThread executingThread ) {
		this.executingThread = executingThread;
	}

	public Map<RoleType, Set<Role>> getRoles() {
		return this.roles;
	}

	public void setRoles( Map<RoleType, Set<Role>> roles ) {
		this.roles = roles;
	}

	public AffineMatrix4x4 getInitialCameraTransform() {
		return this.initialCameraTransform;
	}

	public void setInitialCameraTransform( AffineMatrix4x4 initialCameraTransform ) {
		this.initialCameraTransform = initialCameraTransform;
	}

	public Set<Resource> getScriptResources() {
		return this.scriptResources;
	}

	public void setScriptResources( Set<Resource> scriptResources ) {
		this.scriptResources = scriptResources;
	}

	public void setFieldsForMethods( Map<AbstractMethod, Set<UserField>> fieldsForMethods ) {
		this.fieldsForMethods = fieldsForMethods;
	}

	public java.util.Map<AbstractMethod, Set<UserField>> getFieldsForMethods() {
		return this.fieldsForMethods;
	}

	public NamedUserType getScriptType() {
		return this.scriptType;
	}

	public UserMethod getRootMethod() {
		return this.rootMethod;
	}

	protected void addRole( Role role, RoleType type ) {
		if( role.getOriginField() != null ) {
			this.roles.get( type ).add( role );
		}
	}

	protected void addActiveRole( Role role ) {
		this.roles.get( RoleType.ACTIVE ).add( role );
	}

	public java.util.Collection<Role> getActiveRoles() {
		return this.roles.get( RoleType.ACTIVE );
	}

	protected void addSpecialRole( Role role ) {
		this.roles.get( RoleType.SPECIAL ).add( role );
	}

	public Set<Role> getSpecialRoles() {
		return this.roles.get( RoleType.SPECIAL );
	}

	public Set<Role> getAllRoles() {
		Set<Role> rv = Sets.newHashSet();
		rv.addAll( getActiveRoles() );
		rv.addAll( getSpecialRoles() );
		return rv;
	}

	/**
	 * List of roles that actually do something in the remix.
	 *
	 * @returns list of roles that are actually active
	 */
	public java.util.List<Role> getLiveActorRoles() {
		java.util.List<Role> roles = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList( getAllRoles() );
		roles.removeIf( ( Role role ) -> {
			return ( ( role.getCallerCount() == 0 ) && ( role.getParameterCount() == 0 ) );
		} );

		return roles;
	}

	public java.util.List<Role> getLiveCallerActorRoles() {
		java.util.List<Role> roles = edu.cmu.cs.dennisc.java.util.Lists.newArrayList( getAllRoles() );

		roles.removeIf( ( Role role ) -> {
			return role.getCallerCount() == 0;
		} );
		roles.sort( ( Role o1, Role o2 ) -> {
			return new Integer( o2.getCallerCount() ).compareTo( new Integer( o1.getCallerCount() ) );
		} );

		return roles;
	}

	public java.util.List<Role> getLiveParameterActorRoles() {
		java.util.List<Role> roles = edu.cmu.cs.dennisc.java.util.Lists.newArrayList( getAllRoles() );

		roles.removeIf( ( Role role ) -> {
			return role.getParameterCount() == 0;
		} );
		roles.sort( ( Role o1, Role o2 ) -> {
			return new Integer( o2.getParameterCount() ).compareTo( new Integer( o1.getParameterCount() ) );
		} );

		return roles;
	}

	/* ---------------- Remix methods  -------------- */

	public void clearNewFields() {
		this.newFields.clear();
	}

	public void clearRoleForFields() {
		this.roleForFields.clear();
	}

	public void addNewField( UserField field ) {
		this.newFields.add( field );
	}

	public java.util.Set<UserField> getNewFields() {
		return this.newFields;
	}

	public void setRoleForFields( Map<AbstractField, Role> roleForFields ) {
		this.roleForFields = roleForFields;
	}

	public Role getRoleForField( AbstractField field ) {
		return this.roleForFields.get( field );
	}

}
