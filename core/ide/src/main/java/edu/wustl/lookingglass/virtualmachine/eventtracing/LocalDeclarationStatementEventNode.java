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
package edu.wustl.lookingglass.virtualmachine.eventtracing;

import org.lgna.common.ComponentThread;
import org.lgna.project.ast.LocalDeclarationStatement;
import org.lgna.project.ast.UserLocal;

import edu.wustl.lookingglass.remix.ast.ASTCopier;

/**
 * The <code>LocalDeclarationStatementEventNode</code> subclass describes an
 * {@link AbstractEventNode} with a <code>LocalDeclarationStatement</code> ast
 * node.
 *
 * @author Michael Pogran
 */
public class LocalDeclarationStatementEventNode extends AbstractEventNode<LocalDeclarationStatement> {
	UserLocal local;
	ExpressionEvaluationEventNode initializerExpressionNode;

	protected LocalDeclarationStatementEventNode( LocalDeclarationStatement astNode, ComponentThread thread, double startTime, double endTime, AbstractEventNode<?> parent ) {
		super( astNode, thread, startTime, endTime, parent );
		this.local = astNode.local.getValue();
	}

	public void setInitializerExpression( ExpressionEvaluationEventNode initializerExpression ) {
		this.initializerExpressionNode = initializerExpression;
	}

	public UserLocal getLocal() {
		return this.local;
	}

	public ExpressionEvaluationEventNode getInitializerExpressionNode() {
		return this.initializerExpressionNode;
	}

	public Object getValue() {
		if( this.initializerExpressionNode != null ) {
			return this.initializerExpressionNode.getValue();
		}
		return null;
	}

	/* ---------------- Copy methods  -------------- */

	private void setLocal( UserLocal local ) {
		this.local = local;
	}

	static LocalDeclarationStatementEventNode createCopy( LocalDeclarationStatementEventNode eventNode, ASTCopier copier ) {
		LocalDeclarationStatementEventNode rv = new LocalDeclarationStatementEventNode( (LocalDeclarationStatement)copier.copyAbstractNode( eventNode.getAstNode() ), eventNode.getThread(), eventNode.getStartTime(), eventNode.getEndTime(), null );

		// Copier initializer expression event node
		ExpressionEvaluationEventNode copyInitNode = EventNodeFactory.copyEventNode( eventNode.getInitializerExpressionNode(), copier );
		copyInitNode.setParent( rv );
		rv.setInitializerExpression( copyInitNode );

		// Copy ast nodes
		rv.setLocal( (UserLocal)copier.copyAbstractNode( eventNode.getLocal() ) );
		return rv;
	}

	@Override
	protected void handleChildAdded( AbstractEventNode<?> eventNode ) {
		//pass
	}

}
