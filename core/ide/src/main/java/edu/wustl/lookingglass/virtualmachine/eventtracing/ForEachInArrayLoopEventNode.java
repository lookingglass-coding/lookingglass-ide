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

import java.util.ArrayList;

import org.lgna.common.ComponentThread;
import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.ForEachInArrayLoop;
import org.lgna.project.ast.UserLocal;
import org.lgna.project.virtualmachine.UserArrayInstance;

import edu.cmu.cs.dennisc.java.util.Lists;
import edu.wustl.lookingglass.remix.ast.ASTCopier;

/**
 * The <code>ForEachInArrayLoopEventNode</code> subclass describes an
 * {@link AbstractEventNode} with a <code>ForEachInArrayLoop</code> ast node.
 * Additional functionality is supplied for <code>ForEachInArrayLoop</code>
 * specific use.
 *
 * @author Michael Pogran
 */
public class ForEachInArrayLoopEventNode extends AbstractLoopEventNode<ForEachInArrayLoop> {
	UserLocal item;
	ExpressionEvaluationEventNode arrayExpressionNode;

	ForEachInArrayLoopEventNode( ForEachInArrayLoop astNode, ComponentThread thread, double startTime, double endTime, AbstractEventNode<?> parent ) {
		super( astNode, thread, startTime, endTime, parent );
		this.item = astNode.item.getValue();
	}

	public void setArrayExpressionNode( ExpressionEvaluationEventNode expression ) {
		this.arrayExpressionNode = expression;
	}

	public ExpressionEvaluationEventNode getArrayExpressionNode() {
		return this.arrayExpressionNode;
	}

	public Object[] getArrayValue() {
		if( ( this.arrayExpressionNode != null ) && ( this.arrayExpressionNode.getValue() != null ) ) {
			if( this.arrayExpressionNode.getValue() instanceof UserArrayInstance ) {
				UserArrayInstance arrayInstance = (UserArrayInstance)this.arrayExpressionNode.getValue();
				return arrayInstance.getJavaArray();
			}
			else if( this.arrayExpressionNode.getValue().getClass().isArray() ) {
				return (Object[])this.arrayExpressionNode.getValue();
			}
		}
		return null;
	}

	public UserLocal getItem() {
		return this.item;
	}

	public int getArraySize() {
		if( this.getArrayValue() != null ) {
			return this.getArrayValue().length;
		} else {
			return 0;
		}
	}

	private void setItem( UserLocal item ) {
		this.item = item;
	}

	@Override
	public void addChild( AbstractEventNode<?> child ) {
		if( this.getChildren().size() < getArraySize() ) {
			super.addChild( child );
		} else {
			if( child instanceof ExpressionEvaluationEventNode ) {
				//pass
			} else {
				throw new RuntimeException( "Tried adding more children than size allows. Count specified: " + getArraySize() + "Current Size: " + this.getChildren().size() );
			}
		}
	}

	/* ---------------- Copy methods  -------------- */

	static ForEachInArrayLoopEventNode createCopy( ForEachInArrayLoopEventNode eventNode, ASTCopier copier ) {
		ForEachInArrayLoopEventNode rv = new ForEachInArrayLoopEventNode( (ForEachInArrayLoop)copier.copyAbstractNode( eventNode.getAstNode() ), eventNode.getThread(), eventNode.getStartTime(), eventNode.getEndTime(), null );

		// Copy array expression event node

		ExpressionEvaluationEventNode copyArrayNode = EventNodeFactory.copyEventNode( eventNode.getArrayExpressionNode(), copier );
		copyArrayNode.setParent( rv );
		rv.setArrayExpressionNode( copyArrayNode );

		// Copy child event nodes
		ArrayList<AbstractEventNode<?>> childNodes = Lists.newArrayList();
		for( AbstractEventNode<?> childNode : eventNode.getChildren() ) {
			AbstractEventNode<?> copyChildNode = EventNodeFactory.copyEventNode( childNode, copier );
			copyChildNode.setParent( rv );
			childNodes.add( copyChildNode );
		}
		rv.setChildNodes( childNodes );

		// Copy ast nodes
		rv.setItem( (UserLocal)copier.copyAbstractNode( eventNode.getItem() ) );
		rv.setBody( (BlockStatement)copier.copyAbstractNode( eventNode.getBody() ) );
		return rv;
	}

}
