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
package edu.wustl.lookingglass.ide.perspectives.dinah.finder.operations;

import org.lgna.project.ast.ExpressionStatement;
import org.lgna.project.ast.MethodInvocation;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.UserMethod;

public class BreakdownStatementOperation extends org.lgna.croquet.ActionOperation {
	UserMethod method;

	public BreakdownStatementOperation( Statement statement ) {
		super( org.lgna.croquet.Application.DOCUMENT_UI_GROUP, java.util.UUID.fromString( "467b7277-6ac6-45f3-b5a3-9243e6d41c74" ) );

		this.method = getUserMethod( statement );

		StringBuilder sb = new StringBuilder();

		if( this.method != null ) {
			sb.append( findLocalizedText( "prefix" ) );
			sb.append( " " );
			sb.append( this.method.getName() );
			sb.append( " " );
			sb.append( findLocalizedText( "suffix" ) );
		} else {
			sb.append( findLocalizedText( "noInstance" ) );
			this.setEnabled( false );
		}

		this.setSmallIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "breakdown", org.lgna.croquet.icon.IconSize.EXTRA_SMALL ) );
		this.setName( sb.toString() );
	}

	@Override
	protected void perform( org.lgna.croquet.history.CompletionStep<?> step ) {
		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			org.alice.ide.IDE.getActiveInstance().getDocumentFrame().setFocusedCode( this.method );
		} );
		step.finish();
	}

	private UserMethod getUserMethod( Statement statement ) {

		UserMethod userMethod = null;
		if( ( statement instanceof ExpressionStatement ) && ( ( (ExpressionStatement)statement ).expression.getValue() instanceof MethodInvocation ) ) {
			MethodInvocation invocation = (MethodInvocation)( (ExpressionStatement)statement ).expression.getValue();

			if( invocation.method.getValue() instanceof UserMethod ) {
				userMethod = (UserMethod)invocation.method.getValue();
			}
		}

		return userMethod;
	}
}
