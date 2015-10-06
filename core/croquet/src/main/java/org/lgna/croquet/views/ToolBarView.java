/*******************************************************************************
 * Copyright (c) 2006, 2015, Carnegie Mellon University. All rights reserved.
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
 * 3. Products derived from the software may not be called "Alice", nor may
 *    "Alice" appear in their name, without prior written permission of
 *    Carnegie Mellon University.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Carnegie Mellon University"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 * ANY AND ALL EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT
 * SHALL THE AUTHORS, COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.lgna.croquet.views;

import java.awt.Insets;

/**
 * @author Dennis Cosgrove
 */
// <lg/> Use JToolbar since it's feedback is better. Especially since the custom croquet Toolbar/MigPanel
// buttons are so noisy and draw too much attention during user studies.
public abstract class ToolBarView extends CompositeView<javax.swing.JToolBar, org.lgna.croquet.Composite<?>> {

	public ToolBarView( org.lgna.croquet.ToolBarComposite composite ) {
		super( composite );
		for( org.lgna.croquet.Element element : composite.getSubElements() ) {
			this.addViewForElement( element );
		}
	}

	@Override
	protected final javax.swing.JToolBar createAwtComponent() {
		javax.swing.JToolBar toolbar = new javax.swing.JToolBar();
		toolbar.setFloatable( false );
		toolbar.setRollover( true );
		toolbar.setMargin( new Insets( 2, 2, 2, 2 ) );
		return toolbar;
	}

	public void addSeparator() {
		this.getAwtComponent().addSeparator();
	}

	public void addGlue() {
		this.getAwtComponent().add( javax.swing.Box.createHorizontalGlue() );
	}

	public void addComponent( AwtComponentView<?> component ) {
		this.internalAddComponent( component );
	}

	protected void addViewForElement( org.lgna.croquet.Element element ) {
		if( element == org.lgna.croquet.GapToolBarSeparator.getInstance() ) {
			this.addSeparator();
		} else if( element == org.lgna.croquet.PushToolBarSeparator.getInstance() ) {
			this.addGlue();
		} else {
			SwingComponentView<?> component;
			if( element instanceof org.lgna.croquet.Operation ) {
				org.lgna.croquet.Operation operation = (org.lgna.croquet.Operation)element;
				Button button = operation.createButton();
				button.setMargin( new Insets( 4, 4, 4, 4 ) );
				if( operation.isToolBarTextClobbered() ) {
					button.setToolTipText( operation.getImp().getName() );
					button.setClobberText( "" );
				}
				component = button;
			} else if( element instanceof org.lgna.croquet.SingleSelectListState<?, ?> ) {
				org.lgna.croquet.SingleSelectListState<?, ?> listSelectionState = (org.lgna.croquet.SingleSelectListState<?, ?>)element;
				ComboBox<?> comboBox = listSelectionState.getPrepModel().createComboBoxWithItemCodecListCellRenderer();
				component = comboBox;
			} else if( element instanceof org.lgna.croquet.Composite<?> ) {
				org.lgna.croquet.Composite<?> subComposite = (org.lgna.croquet.Composite<?>)element;
				component = subComposite.getView();
			} else if( element instanceof org.lgna.croquet.PlainStringValue ) {
				org.lgna.croquet.PlainStringValue stringValue = (org.lgna.croquet.PlainStringValue)element;
				component = stringValue.createLabel();
			} else {
				edu.cmu.cs.dennisc.java.util.logging.Logger.severe( element );
				component = null;
			}
			if( component != null ) {
				this.internalAddComponent( component );
			}
		}
	}
}
