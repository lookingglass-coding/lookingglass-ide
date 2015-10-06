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

import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;

import javax.swing.BorderFactory;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.border.Border;

import org.lgna.croquet.icon.IconSize;
import org.lgna.project.ast.UserField;

import edu.cmu.cs.dennisc.javax.swing.IconUtilities;
import edu.wustl.lookingglass.remix.roles.CharacterSelectionComposite;

/**
 * @author Michael Pogran
 */
public class UserFieldCellRenderer implements javax.swing.ListCellRenderer<UserField> {

	private static final int NAME_LENGTH = 10;

	private CharacterSelectionComposite composite;
	private final Border selBorder = BorderFactory.createCompoundBorder( BorderFactory.createLineBorder( new java.awt.Color( 0, 204, 102 ), 2 ), BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
	private final Border notSelBorder = BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder( 1, 1, 1, 1, new java.awt.Color( 138, 138, 154 ) ), BorderFactory.createEmptyBorder( 4, 4, 4, 4 ) );

	public UserFieldCellRenderer( CharacterSelectionComposite composite ) {
		this.composite = composite;
	}

	@Override
	public Component getListCellRendererComponent( JList<? extends UserField> list, UserField value, int index, boolean isSelected, boolean cellHasFocus ) {
		javax.swing.JLabel rv = new javax.swing.JLabel();

		org.lgna.croquet.icon.IconFactory factory = null;
		if( value.getValueType().isAssignableTo( org.lgna.story.SMarker.class ) ) {
			factory = org.alice.stageide.icons.IconFactoryManager.getIconFactoryForCameraMarker( composite.getColorForMaker( value ) );
		} else {
			factory = org.alice.stageide.icons.IconFactoryManager.getIconFactoryForField( value );
		}
		if( factory != null ) {
			Icon icon = factory.getIcon( IconSize.LARGE.getSize() );

			if( composite.isFieldAssignable( value ) ) {
				rv.setIcon( icon );
			} else {
				GrayFilter filter = new GrayFilter( true, 50 );
				ImageProducer prod = new FilteredImageSource( IconUtilities.iconToImage( icon ).getSource(), filter );
				Image grayImage = Toolkit.getDefaultToolkit().createImage( prod );
				rv.setIcon( new ImageIcon( grayImage ) );
			}
		}
		rv.setText( getTrimmedTitle( value.getName() ) );

		rv.setHorizontalTextPosition( javax.swing.JLabel.CENTER );
		rv.setVerticalTextPosition( javax.swing.JLabel.BOTTOM );
		rv.setHorizontalAlignment( javax.swing.JLabel.CENTER );

		javax.swing.border.Border border;
		if( isSelected ) {
			border = selBorder;
		} else {
			border = notSelBorder;
		}
		rv.setBorder( javax.swing.BorderFactory.createCompoundBorder( javax.swing.BorderFactory.createMatteBorder( 5, 5, 5, 5, new java.awt.Color( 176, 176, 200 ) ), border ) );
		rv.setBackground( java.awt.Color.WHITE );
		rv.setOpaque( true );
		rv.setSize( 50, 50 );

		return rv;
	}

	static String getTrimmedTitle( String title ) {
		if( title.length() > NAME_LENGTH ) {
			title = title.substring( 0, NAME_LENGTH ) + "…";
		}
		return title;
	}
}
