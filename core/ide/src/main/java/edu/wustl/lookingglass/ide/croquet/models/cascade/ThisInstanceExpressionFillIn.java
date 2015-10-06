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
package edu.wustl.lookingglass.ide.croquet.models.cascade;

public class ThisInstanceExpressionFillIn extends org.alice.ide.croquet.models.cascade.ExpressionFillInWithoutBlanks<org.lgna.project.ast.ThisInstanceExpression> {
	private static final java.util.Map<org.lgna.project.ast.UserType, ThisInstanceExpressionFillIn> map = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();

	public static synchronized ThisInstanceExpressionFillIn getInstance( org.lgna.project.ast.UserType type ) {
		ThisInstanceExpressionFillIn rv = map.get( type );
		if( rv != null ) {
			//pass
		} else {
			rv = new ThisInstanceExpressionFillIn( type );
			map.put( type, rv );
		}
		return rv;
	}

	private final org.lgna.project.ast.UserType type;
	private final org.lgna.project.ast.ThisInstanceExpression transientValue;

	private ThisInstanceExpressionFillIn( org.lgna.project.ast.UserType type ) {
		super( java.util.UUID.fromString( "4a1f26d8-634b-42e8-839f-13ddb1542fe3" ) );
		this.type = type;
		this.transientValue = new org.lgna.project.ast.ThisInstanceExpression( this.type );
	}

	@Override
	public org.lgna.project.ast.ThisInstanceExpression createValue( org.lgna.croquet.imp.cascade.ItemNode<? super org.lgna.project.ast.ThisInstanceExpression, Void> step, org.lgna.croquet.history.TransactionHistory transactionHistory ) {
		return new org.lgna.project.ast.ThisInstanceExpression( this.type );
	}

	@Override
	public org.lgna.project.ast.ThisInstanceExpression getTransientValue( org.lgna.croquet.imp.cascade.ItemNode<? super org.lgna.project.ast.ThisInstanceExpression, Void> step ) {
		return this.transientValue;
	}
}
