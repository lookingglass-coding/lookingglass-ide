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
package org.alice.ide.croquet.models.cascade;

import org.alice.ide.instancefactory.GlobalFirstInstanceSceneFactory;
import org.lgna.croquet.history.TransactionHistory;
import org.lgna.croquet.imp.cascade.ItemNode;
import org.lgna.project.ast.AbstractField;
import org.lgna.project.ast.FieldAccess;

/**
 * @author Michael Pogran
 */
public class GlobalFirstInstanceFiledAccessFillIn extends ExpressionFillInWithoutBlanks<org.lgna.project.ast.FieldAccess> {
	private static java.util.Map<org.lgna.project.ast.AbstractField, GlobalFirstInstanceFiledAccessFillIn> map = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();

	public static GlobalFirstInstanceFiledAccessFillIn getInstance( org.lgna.project.ast.AbstractField value ) {
		synchronized( map ) {
			GlobalFirstInstanceFiledAccessFillIn rv = map.get( value );
			if( rv != null ) {
				//pass
			} else {
				rv = new GlobalFirstInstanceFiledAccessFillIn( value );
				map.put( value, rv );
			}
			return rv;
		}
	}

	private final org.lgna.project.ast.FieldAccess transientValue;

	public GlobalFirstInstanceFiledAccessFillIn( org.lgna.project.ast.AbstractField field ) {
		super( java.util.UUID.fromString( "ebce4b53-b46a-41ae-8e76-caaf8d2dabd1" ) );
		this.transientValue = this.createValue( field );
	}

	@Override
	public FieldAccess getTransientValue( ItemNode<? super FieldAccess, Void> node ) {
		return this.transientValue;
	}

	@Override
	public FieldAccess createValue( ItemNode<? super FieldAccess, Void> node, TransactionHistory transactionHistory ) {
		return this.createValue( this.transientValue.field.getValue() );
	}

	private FieldAccess createValue( AbstractField value ) {
		org.lgna.project.ast.Expression sceneExpression = GlobalFirstInstanceSceneFactory.getInstance().createExpression();
		return new org.lgna.project.ast.FieldAccess( sceneExpression, value );
	}

	@Override
	protected javax.swing.Icon getLeadingIcon( org.lgna.croquet.imp.cascade.ItemNode<? super org.lgna.project.ast.FieldAccess, java.lang.Void> step ) {
		org.lgna.project.ast.AbstractField field = this.transientValue.field.getValue();
		java.awt.Dimension size = new java.awt.Dimension( 18, 18 );

		if( field instanceof org.lgna.project.ast.UserField ) {
			org.lgna.project.ast.UserField userField = (org.lgna.project.ast.UserField)field;
			org.lgna.project.ast.AbstractType<?, ?, ?> type = userField.getValueType();

			if( type != null ) {
				if( type.isAssignableTo( org.lgna.story.SThing.class ) ) {
					org.lgna.croquet.icon.IconFactory iconFactory = org.alice.stageide.icons.IconFactoryManager.getIconFactoryForField( userField );

					if( iconFactory != null ) {
						return iconFactory.getIcon( size );
					}
				}
			}
		}
		return org.lgna.croquet.icon.EmptyIconFactory.getInstance().getIcon( size );
	}
}
