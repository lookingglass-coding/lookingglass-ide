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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;

import javax.swing.BorderFactory;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.border.Border;

import org.alice.stageide.icons.IconFactoryManager;
import org.lgna.croquet.BoundedIntegerState;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.event.ValueListener;
import org.lgna.croquet.icon.ArrowIcon;
import org.lgna.croquet.icon.IconFactory;
import org.lgna.croquet.icon.IconSize;
import org.lgna.croquet.views.HorizontalAlignment;
import org.lgna.croquet.views.HorizontalTextPosition;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.LineAxisPanel;
import org.lgna.croquet.views.VerticalTextPosition;
import org.lgna.project.ast.UserField;
import org.lgna.story.SMarker;

import edu.cmu.cs.dennisc.javax.swing.IconUtilities;
import edu.wustl.lookingglass.ide.LookingGlassTheme;
import edu.wustl.lookingglass.remix.Role;
import edu.wustl.lookingglass.remix.roles.CharacterSelectionComposite;

/**
 * @author Michael Pogran
 */
public class RoleAssignment extends LineAxisPanel {
	private final Role role;
	private UserField assignment;
	private int index;
	private Icon roleIcon;
	private BoundedIntegerState rangeState;
	private org.lgna.story.Color markerColor;
	private CharacterSelectionComposite composite;

	private ValueListener<Integer> listener;
	private MouseListener mouseListener;

	public RoleAssignment( Role role, CharacterSelectionComposite composite, org.lgna.story.Color markerColor ) {
		this.markerColor = markerColor;
		this.role = role;
		this.composite = composite;
		this.listener = new ValueListener<Integer>() {

			@Override
			public void valueChanged( ValueEvent<Integer> e ) {
				updateView( e.getNextValue() == index );
			}
		};

		IconFactory roleFactory = null;
		UserField field = this.role.getOriginField();
		if( field.getValueType().isAssignableTo( SMarker.class ) ) {
			roleFactory = IconFactoryManager.getIconFactoryForCameraMarker( this.markerColor );
		} else {
			roleFactory = IconFactoryManager.getIconFactoryForField( field );
		}

		if( roleFactory != null ) {
			this.roleIcon = roleFactory.getIcon( IconSize.LARGE.getSize() );
		}

		this.setBackgroundColor( java.awt.Color.WHITE );

		this.mouseListener = new MouseListener() {

			@Override
			public void mouseClicked( MouseEvent e ) {
				rangeState.setValueTransactionlessly( index );
			}

			@Override
			public void mousePressed( MouseEvent e ) {
			}

			@Override
			public void mouseReleased( MouseEvent e ) {
			}

			@Override
			public void mouseEntered( MouseEvent e ) {
			}

			@Override
			public void mouseExited( MouseEvent e ) {
			}

		};
	}

	public RoleAssignment( Role role, CharacterSelectionComposite composite ) {
		this( role, composite, null );
	}

	public void setAssignment( UserField assignment ) {
		this.assignment = assignment;
		updateView( this.rangeState.getValue() == this.index );
	}

	public void setGroupRangeState( BoundedIntegerState rangeState, int index ) {
		this.rangeState = rangeState;
		this.index = index;

		this.getAwtComponent().addMouseListener( this.mouseListener );
		this.rangeState.addNewSchoolValueListener( listener );
		updateView( this.rangeState.getValue() == this.index );
	}

	public UserField getAssignment() {
		return this.assignment;
	}

	public Role getRole() {
		return this.role;
	}

	public org.lgna.story.Color getMarkerColor() {
		return this.markerColor;
	}

	private final Border noAssignBorder = BorderFactory.createCompoundBorder( BorderFactory.createLineBorder( new java.awt.Color( 204, 0, 0 ), 2 ), BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
	private final Border yesAssignBorder = BorderFactory.createCompoundBorder( BorderFactory.createLineBorder( new java.awt.Color( 0, 204, 102 ), 2 ), BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
	private final Border notSelBorder = BorderFactory.createCompoundBorder( BorderFactory.createLineBorder( new java.awt.Color( 138, 138, 154 ) ), BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );
	private final Border labelBorder = BorderFactory.createCompoundBorder( BorderFactory.createLineBorder( java.awt.Color.BLACK ), BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

	public void updateView( boolean isCurrent ) {

		synchronized( this.getTreeLock() ) {
			this.removeAllComponents();
			this.setBorder( null );

			if( isCurrent ) {

				IconFactory factory;
				if( ( this.assignment != null ) && this.assignment.getValueType().isAssignableTo( org.lgna.story.SMarker.class ) ) {
					factory = IconFactoryManager.getIconFactoryForCameraMarker( composite.getColorForMaker( this.assignment ) );
				} else {
					factory = IconFactoryManager.getIconFactoryForField( this.assignment );
				}

				Icon assignmentIcon = null;
				if( factory != null ) {
					assignmentIcon = factory.getIcon( IconSize.LARGE.getSize() );
				}

				if( this.assignment == null ) {
					assignmentIcon = LookingGlassTheme.getIcon( "question-mark", IconSize.LARGE );
					this.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder( 5, 5, 5, 5, new java.awt.Color( 176, 176, 200 ) ), noAssignBorder ) );
				} else {
					this.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder( 5, 5, 5, 5, new java.awt.Color( 176, 176, 200 ) ), yesAssignBorder ) );
				}

				if( ( this.roleIcon != null ) && ( assignmentIcon != null ) ) {
					String assignName = this.assignment == null ? "blank" : this.assignment.getName();

					Label roleLabel = new Label( UserFieldCellRenderer.getTrimmedTitle( role.getName() ), this.roleIcon );
					Label assignLabel = new Label( UserFieldCellRenderer.getTrimmedTitle( assignName ), assignmentIcon );

					roleLabel.setHorizontalAlignment( HorizontalAlignment.CENTER );
					roleLabel.setHorizontalTextPosition( HorizontalTextPosition.CENTER );
					roleLabel.setVerticalTextPosition( VerticalTextPosition.BOTTOM );
					roleLabel.setBorder( labelBorder );

					assignLabel.setHorizontalAlignment( HorizontalAlignment.CENTER );
					assignLabel.setHorizontalTextPosition( HorizontalTextPosition.CENTER );
					assignLabel.setVerticalTextPosition( VerticalTextPosition.BOTTOM );
					assignLabel.setBorder( labelBorder );

					this.addComponent( roleLabel );
					this.addComponent( new Label( new ArrowIcon( 26, 21 ) ) );
					this.addComponent( assignLabel );
				} else {
					this.addComponent( new Label( this.role.getName() ) );
				}
			} else {
				Label disabledLabel;
				if( this.roleIcon != null ) {
					GrayFilter filter = new GrayFilter( true, 50 );
					ImageProducer prod = new FilteredImageSource( IconUtilities.iconToImage( this.roleIcon ).getSource(), filter );
					Image grayImage = Toolkit.getDefaultToolkit().createImage( prod );

					disabledLabel = new Label( new ImageIcon( grayImage ) );
				} else {
					disabledLabel = new Label( this.role.getName() );
				}
				disabledLabel.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder( 5, 5, 5, 5, new java.awt.Color( 176, 176, 200 ) ), notSelBorder ) );
				this.addComponent( disabledLabel );
			}
			this.revalidateAndRepaint();
		}
	}
}
