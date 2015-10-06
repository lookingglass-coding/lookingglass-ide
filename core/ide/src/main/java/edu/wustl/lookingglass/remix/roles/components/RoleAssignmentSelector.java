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
package edu.wustl.lookingglass.remix.roles.components;

import java.util.List;

import org.lgna.croquet.Application;
import org.lgna.croquet.BoundedIntegerState;
import org.lgna.croquet.BoundedIntegerState.Details;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.event.ValueListener;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;
import org.lgna.croquet.views.LineAxisPanel;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.ScrollPane;
import org.lgna.croquet.views.ScrollPane.HorizontalScrollbarPolicy;
import org.lgna.croquet.views.ScrollPane.VerticalScrollbarPolicy;
import org.lgna.croquet.views.SwingComponentView;
import org.lgna.project.ast.UserField;

import edu.wustl.lookingglass.remix.Role;
import edu.wustl.lookingglass.remix.models.ReuseGroup;
import edu.wustl.lookingglass.remix.roles.CharacterSelectionComposite;

/**
 * @author Michael Pogran
 */
public class RoleAssignmentSelector {

	private final List<RoleAssignment> roleAssignments;
	private BoundedIntegerState rangeState;
	private CharacterSelectionComposite composite;

	private RoleAssignment lastAssignment;

	public RoleAssignmentSelector( CharacterSelectionComposite composite ) {
		this.composite = composite;
		this.roleAssignments = composite.getRoleAssignments();

		this.rangeState = new BoundedIntegerState(
				new Details( ReuseGroup.REUSE_GROUP, java.util.UUID.fromString( "e97b9f74-7e9d-4a76-8cff-b9813373da4a" ) )
						.maximum( this.roleAssignments.size() > 0 ? this.roleAssignments.size() - 1 : 0 )
						.initialValue( 0 ) ) {
		};

		for( RoleAssignment roleAssignment : this.roleAssignments ) {
			roleAssignment.setGroupRangeState( rangeState, this.roleAssignments.indexOf( roleAssignment ) );
		}
	}

	public SwingComponentView<?> getView() {
		MigPanel rv = new MigPanel( null, "fill", "[][grow][]", "[]" );

		LineAxisPanel rolesPanel = new LineAxisPanel();
		for( RoleAssignment roleAssignment : this.roleAssignments ) {
			rolesPanel.addComponent( roleAssignment );
		}

		ScrollPane scroller = new ScrollPane( rolesPanel, VerticalScrollbarPolicy.NEVER, HorizontalScrollbarPolicy.AS_NEEDED );
		rolesPanel.setBackgroundColor( new java.awt.Color( 176, 176, 200 ) );
		rolesPanel.setOpaque( true );

		rv.setBorder( javax.swing.BorderFactory.createLineBorder( new java.awt.Color( 87, 87, 102 ) ) );
		rv.setBackgroundColor( new java.awt.Color( 176, 176, 200 ) );
		rv.addComponent( new PreviousRoleAssignmentOperation().createButton(), "cell 0 0, left, growy" );
		rv.addComponent( scroller, "cell 1 0, grow" );
		rv.addComponent( new NextRoleAssignmentOperation().createButton(), "cell 2 0, right, growy" );

		return rv;
	}

	public BoundedIntegerState getRangeState() {
		return this.rangeState;
	}

	public Role getCurrentRole() {
		if( this.roleAssignments.isEmpty() ) {
			return null;
		} else {
			return this.roleAssignments.get( this.rangeState.getValue() ).getRole();
		}
	}

	public void AssignRole( UserField assignment ) {
		RoleAssignment roleAssignment = this.roleAssignments.get( this.rangeState.getValue() );
		roleAssignment.setAssignment( assignment );

		if( ( this.rangeState.getValue() == this.rangeState.getMaximum() ) || this.composite.areAllRolesAssigned() ) {
			//pass
		} else {
			this.rangeState.setValueTransactionlessly( this.rangeState.getValue() + 1 );
		}
		this.lastAssignment = roleAssignment;
	}

	public void RollBackAssignment() {
		if( this.lastAssignment != null ) {
			this.lastAssignment.setAssignment( null );
			this.rangeState.setValueTransactionlessly( this.roleAssignments.indexOf( this.lastAssignment ) );
			this.lastAssignment = null;
		}
	}

	private class NextRoleAssignmentOperation extends org.lgna.croquet.Operation {

		public NextRoleAssignmentOperation() {
			super( Application.PROJECT_GROUP, java.util.UUID.fromString( "af38e84d-6d57-4516-9f5f-d8455431d741" ) );
			rangeState.addNewSchoolValueListener( new ValueListener<Integer>() {

				@Override
				public void valueChanged( ValueEvent<Integer> e ) {
					setEnabled( e.getNextValue() < rangeState.getMaximum() );
				}
			} );

			setEnabled( rangeState.getValue() < rangeState.getMaximum() );
			setButtonIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "go-next", org.lgna.croquet.icon.IconSize.SMALL ) );
		}

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			rangeState.setValueTransactionlessly( rangeState.getValue() + 1 );
		}
	}

	private class PreviousRoleAssignmentOperation extends org.lgna.croquet.Operation {

		public PreviousRoleAssignmentOperation() {
			super( Application.PROJECT_GROUP, java.util.UUID.fromString( "111f6a6b-af6e-40d7-9b4a-8ca66d1577f3" ) );
			rangeState.addNewSchoolValueListener( new ValueListener<Integer>() {

				@Override
				public void valueChanged( ValueEvent<Integer> e ) {
					setEnabled( e.getNextValue() > 0 );
				}
			} );
			setEnabled( rangeState.getValue() > 0 );
			setButtonIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "go-previous", org.lgna.croquet.icon.IconSize.SMALL ) );
		}

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			rangeState.setValueTransactionlessly( rangeState.getValue() - 1 );
		}
	}

}
