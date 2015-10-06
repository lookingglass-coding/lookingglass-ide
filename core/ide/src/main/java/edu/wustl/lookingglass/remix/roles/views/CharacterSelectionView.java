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
package edu.wustl.lookingglass.remix.roles.views;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.views.AbstractLabel;
import org.lgna.croquet.views.AbstractWindow;
import org.lgna.croquet.views.BorderPanel;
import org.lgna.croquet.views.Button;
import org.lgna.croquet.views.CheckBox;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.List;
import org.lgna.croquet.views.List.LayoutOrientation;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.ScrollPane;
import org.lgna.croquet.views.Separator;
import org.lgna.croquet.views.TextField;
import org.lgna.project.ast.UserField;

import edu.cmu.cs.dennisc.java.awt.font.TextWeight;
import edu.cmu.cs.dennisc.javax.swing.SpringUtilities;
import edu.wustl.lookingglass.remix.Role;
import edu.wustl.lookingglass.remix.roles.CharacterSelectionComposite;
import edu.wustl.lookingglass.remix.roles.components.RoleAssignmentSelector;
import edu.wustl.lookingglass.remix.roles.components.UserFieldCellRenderer;

/**
 * @author Michael Pogran
 */
public class CharacterSelectionView extends BorderPanel {

	private RoleAssignmentSelector roleSelector;
	private List<UserField> projectFields;
	private List<UserField> remixFields;
	private BorderPanel previewRemixPanel;
	private BorderPanel programContainer;
	private Button completeButton;
	private Button previewButton;

	private CharacterSelectionComposite composite;

	public CharacterSelectionView( CharacterSelectionComposite composite ) {
		this.roleSelector = new RoleAssignmentSelector( composite );
		this.previewRemixPanel = new BorderPanel();
		this.composite = composite;

		this.roleSelector.getRangeState().addNewSchoolValueListener( new org.lgna.croquet.event.ValueListener<Integer>() {

			@Override
			public void valueChanged( ValueEvent<Integer> e ) {
				projectFields.repaint();
				remixFields.repaint();
			}

		} );

		this.setBackgroundColor( new java.awt.Color( 201, 201, 218 ) );

		MigPanel mainPanel = new MigPanel( null, "fill, ins panel", "[640!, grow 0]12[]", "[]12[][100::][][]" );

		mainPanel.addComponent( generateFieldsPanel(), "cell 1 0, spany 3, grow" );
		mainPanel.addComponent( previewRemixPanel, "cell 0 0, w 640!, h 360!, right, top" );
		mainPanel.addComponent( new Label( "Pick a character from the right to fill each role:", TextWeight.BOLD ), "cell 0 1" );
		mainPanel.addComponent( this.roleSelector.getView(), "cell 0 2, grow" );

		AbstractLabel titleLabel = composite.getRemixTitleState().getSidekickLabel().createLabel( TextWeight.BOLD );
		TextField titleField = composite.getRemixTitleState().createTextField();
		titleField.setMinimumPreferredWidth( 250 );

		AbstractLabel puzzleLabel = composite.getRemixPuzzleState().getSidekickLabel().createLabel( TextWeight.BOLD );
		CheckBox puzzleBox = composite.getRemixPuzzleState().createCheckBox();

		Component[] rowTitle = SpringUtilities.createRow( titleLabel.getAwtComponent(), titleField.getAwtComponent() );
		Component[] rowPuzzle = SpringUtilities.createRow( puzzleLabel.getAwtComponent(), puzzleBox.getAwtComponent() );

		JPanel panel = new JPanel();
		panel.setBackground( null );

		ArrayList<Component[]> rows = new ArrayList<Component[]>();
		rows.add( rowTitle );
		rows.add( rowPuzzle );

		SpringUtilities.springItUpANotch( panel, rows, 8, 4 );

		//		mainPanel.addComponent( Separator.createInstanceSeparatingTopFromBottom(), "cell 0 3, spanx 2, growx" );
		mainPanel.getAwtComponent().add( panel, "cell 0 4" );

		this.completeButton = composite.getCompleteRemixOperation().createButton();
		this.previewButton = composite.getPreviewRemixOperation().createButton( TextWeight.BOLD );

		this.addCenterComponent( mainPanel );

		MigPanel endPanel = new MigPanel( null, "fill", "[]", "[][]" );

		endPanel.addComponent( Separator.createInstanceSeparatingTopFromBottom(), "cell 0 0, growx" );
		endPanel.addComponent( composite.getCancelOperation().createButton(), "cell 0 1, right" );
		endPanel.addComponent( this.completeButton, "cell 0 1, right" );

		this.addPageEndComponent( endPanel );
	}

	@Override
	public void handleCompositePreActivation() {
		super.handleCompositePreActivation();
		AbstractWindow<?> root = this.getRoot();
		if( root != null ) {
			root.pushDefaultButton( this.completeButton );
		}
	}

	@Override
	public void handleCompositePostDeactivation() {
		AbstractWindow<?> root = this.getRoot();
		if( root != null ) {
			root.popDefaultButton();
		}
		super.handleCompositePostDeactivation();
	}

	public Role getCurrentRole() {
		return this.roleSelector.getCurrentRole();
	}

	public RoleAssignmentSelector getRoleSelector() {
		return this.roleSelector;
	}

	public Button getPreviewButton() {
		return this.previewButton;
	}

	public org.lgna.croquet.views.SwingComponentView<?> createProgramContainer() {
		this.programContainer = new BorderPanel();
		synchronized( this.previewRemixPanel.getTreeLock() ) {
			this.previewRemixPanel.removeAllComponents();
			this.previewRemixPanel.addCenterComponent( this.programContainer );
		}
		return this.programContainer;
	}

	public BorderPanel getProgramContainer() {
		return this.programContainer;
	}

	private ScrollPane generateFieldsPanel() {
		MigPanel panel = new MigPanel( null, "fill", "[]", "[][][][][]" );

		this.projectFields = this.composite.getProjectFieldsState().createList();
		projectFields.setCellRenderer( new UserFieldCellRenderer( this.composite ) );
		projectFields.setLayoutOrientation( LayoutOrientation.HORIZONTAL_WRAP );
		projectFields.setVisibleRowCount( -1 );
		projectFields.setBackgroundColor( null );

		this.remixFields = this.composite.getRemixFieldsState().createList();
		remixFields.setCellRenderer( new UserFieldCellRenderer( this.composite ) );
		remixFields.setLayoutOrientation( LayoutOrientation.HORIZONTAL_WRAP );
		remixFields.setVisibleRowCount( -1 );
		remixFields.setBackgroundColor( null );

		panel.addComponent( new Label( "Characters in my scene", TextWeight.BOLD ), "cell 0 0" );
		panel.addComponent( projectFields, "cell 0 1, growx" );
		panel.addComponent( new Label( "Characters in remix", TextWeight.BOLD ), "cell 0 2, gaptop 15" );
		panel.addComponent( remixFields, "cell 0 3, growx, pushy, top" );

		panel.setBackgroundColor( new java.awt.Color( 176, 176, 200 ) );

		ScrollPane rv = new ScrollPane( panel );
		rv.setOpaque( false );
		rv.setBorder( javax.swing.BorderFactory.createCompoundBorder(
				javax.swing.BorderFactory.createLineBorder( new java.awt.Color( 210, 210, 238 ) ),
				javax.swing.BorderFactory.createLineBorder( new java.awt.Color( 87, 87, 102 ) ) ) );

		return rv;
	}
}
