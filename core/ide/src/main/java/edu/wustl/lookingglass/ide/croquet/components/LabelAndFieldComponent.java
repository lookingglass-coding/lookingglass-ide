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
package edu.wustl.lookingglass.ide.croquet.components;

import java.awt.event.FocusEvent;

import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.PlainMultiLineLabel;

/**
 * @author Michael Pogran
 */
public class LabelAndFieldComponent extends MigPanel {
	private org.lgna.croquet.views.TextComponent<?> field;
	private PlainMultiLineLabel label;
	private EditOperation edit;

	class EditOperation extends org.lgna.croquet.Operation {

		public EditOperation() {
			super( org.lgna.croquet.Application.PROJECT_GROUP, java.util.UUID.fromString( "7d4c836e-9289-48ae-8cf0-876258fb79f5" ) );
		}

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			javax.swing.SwingUtilities.invokeLater( ( ) -> {
				if( field.isVisible() ) {
					synchronized( getTreeLock() ) {
						field.setVisible( false );
						label.setVisible( true );
					}
					setName( "Edit" );

				} else {
					synchronized( getTreeLock() ) {
						field.setVisible( true );
						label.setVisible( false );
					}
					setName( "Done" );
					field.requestFocus();
				}
			} );
		}

		@Override
		protected void localize() {
			super.localize();
			setName( "Edit" );
		}

	}

	public LabelAndFieldComponent( org.lgna.croquet.StringState state, boolean isTextArea, edu.cmu.cs.dennisc.java.awt.font.TextAttribute<?>... textAttributes ) {
		this( state, isTextArea, 1.0f, textAttributes );
	}

	public LabelAndFieldComponent( org.lgna.croquet.StringState state, boolean isTextArea, float size, edu.cmu.cs.dennisc.java.awt.font.TextAttribute<?>... textAttributes ) {
		super( null, "fill, ins 0, gap 0", "[]", "[][grow 0]" );
		if( isTextArea ) {
			org.lgna.croquet.views.TextArea area = state.createTextArea();
			area.getAwtComponent().setWrapStyleWord( true );
			area.getAwtComponent().setLineWrap( true );
			area.setAlignmentX( 0.0f );
			this.field = area;
		} else {
			this.field = state.createTextField();
		}
		this.label = new PlainMultiLineLabel( state.getValue(), size, textAttributes );
		this.label.setBorder( javax.swing.BorderFactory.createEmptyBorder( 4, 4, 4, 4 ) );
		this.edit = new EditOperation();

		this.field.getAwtComponent().addFocusListener( new java.awt.event.FocusListener() {

			@Override
			public void focusGained( FocusEvent e ) {
			}

			@Override
			public void focusLost( FocusEvent e ) {
				javax.swing.SwingUtilities.invokeLater( ( ) -> {
					synchronized( getTreeLock() ) {
						field.setVisible( false );
						label.setVisible( true );
					}
					edit.setName( "Edit" );
				} );
			}

		} );

		this.field.setVisible( false );

		addComponent( this.field, "cell 0 0, grow, hidemode 3, top" );
		addComponent( this.label, "cell 0 0, grow, hidemode 3, top" );
		addComponent( this.edit.createHyperlink( 0.9f ), "cell 0 1, right" );

		state.addAndInvokeNewSchoolValueListener( new org.lgna.croquet.event.ValueListener<String>() {

			@Override
			public void valueChanged( ValueEvent<String> e ) {
				if( e.getNextValue() != null ) {
					label.setText( e.getNextValue() );
				}
			}
		} );
	}
}
