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
import org.lgna.project.ast.AbstractNode;
import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.Comment;
import org.lgna.project.ast.ConditionalStatement;
import org.lgna.project.ast.CountLoop;
import org.lgna.project.ast.DoInOrder;
import org.lgna.project.ast.DoTogether;
import org.lgna.project.ast.EachInArrayTogether;
import org.lgna.project.ast.Expression;
import org.lgna.project.ast.ExpressionStatement;
import org.lgna.project.ast.ForEachInArrayLoop;
import org.lgna.project.ast.LocalDeclarationStatement;
import org.lgna.project.ast.ReturnStatement;
import org.lgna.project.ast.UserLambda;
import org.lgna.project.ast.WhileLoop;

import edu.wustl.lookingglass.remix.ast.ASTCopier;

/**
 * @author Michael Pogran
 */
public class EventNodeFactory {

	public static ContainerEventNode<BlockStatement> createEmptyContainer( ComponentThread thread, double startTime, double endTime ) {
		return new ContainerEventNode<BlockStatement>( new BlockStatement(), thread, startTime, endTime, null );
	}

	public static AbstractEventNode<?> createEventNode( AbstractNode astNode, ComponentThread thread, double startTime, AbstractEventNode parentEventNode ) {
		return createEventNode( astNode, thread, startTime, Double.POSITIVE_INFINITY, parentEventNode );
	}

	public static AbstractEventNode<?> createEventNode( AbstractNode astNode, ComponentThread thread, double startTime, double endTime, AbstractEventNode parentEventNode ) {
		if( astNode instanceof ExpressionStatement ) {
			return new ExpressionStatementEventNode( (ExpressionStatement)astNode, thread, startTime, endTime, parentEventNode );
		}
		else if( astNode instanceof ConditionalStatement ) {
			return new ConditionalStatementEventNode( (ConditionalStatement)astNode, thread, startTime, endTime, parentEventNode );
		}
		else if( astNode instanceof CountLoop ) {
			return new CountLoopEventNode( (CountLoop)astNode, thread, startTime, endTime, parentEventNode );
		}
		else if( astNode instanceof WhileLoop ) {
			return new WhileLoopEventNode( (WhileLoop)astNode, thread, startTime, endTime, parentEventNode );
		}
		else if( astNode instanceof ForEachInArrayLoop ) {
			return new ForEachInArrayLoopEventNode( (ForEachInArrayLoop)astNode, thread, startTime, endTime, parentEventNode );
		}
		else if( astNode instanceof EachInArrayTogether ) {
			return new EachInArrayTogetherEventNode( (EachInArrayTogether)astNode, thread, startTime, endTime, parentEventNode );
		}
		else if( astNode instanceof LocalDeclarationStatement ) {
			return new LocalDeclarationStatementEventNode( (LocalDeclarationStatement)astNode, thread, startTime, endTime, parentEventNode );
		}
		else if( astNode instanceof ReturnStatement ) {
			return new ReturnStatementEventNode( (ReturnStatement)astNode, thread, startTime, endTime, parentEventNode );
		}
		else if( astNode instanceof Expression ) {
			return new ExpressionEvaluationEventNode( (Expression)astNode, thread, startTime, endTime, parentEventNode );
		}
		else if( astNode instanceof BlockStatement ) {
			return new ContainerEventNode<BlockStatement>( (BlockStatement)astNode, thread, startTime, endTime, parentEventNode );
		}
		else if( astNode instanceof DoInOrder ) {
			return new ContainerEventNode<DoInOrder>( (DoInOrder)astNode, thread, startTime, endTime, parentEventNode );
		}
		else if( astNode instanceof DoTogether ) {
			return new ContainerEventNode<DoTogether>( (DoTogether)astNode, thread, startTime, endTime, parentEventNode );
		}
		else if( astNode instanceof Comment ) {
			return new CommentEventNode( (Comment)astNode, thread, startTime, endTime, parentEventNode );
		}
		else if( astNode instanceof UserLambda ) {
			return new LambdaEventNode( (UserLambda)astNode, thread, startTime, endTime, parentEventNode );
		}
		else {
			return null;
		}
	}

	@SuppressWarnings( "unchecked" )
	public static <T extends AbstractEventNode<?>> T copyEventNode( T eventNode, ASTCopier copier ) {
		AbstractEventNode<?> rv = null;
		if( eventNode instanceof ConditionalStatementEventNode ) {
			rv = (T)ConditionalStatementEventNode.createCopy( (ConditionalStatementEventNode)eventNode, copier );
		}
		else if( eventNode instanceof ExpressionStatementEventNode ) {
			rv = (T)ExpressionStatementEventNode.createCopy( (ExpressionStatementEventNode)eventNode, copier );
		}
		else if( eventNode instanceof WhileLoopEventNode ) {
			rv = (T)WhileLoopEventNode.createCopy( (WhileLoopEventNode)eventNode, copier );
		}
		else if( eventNode instanceof CountLoopEventNode ) {
			rv = (T)CountLoopEventNode.createCopy( (CountLoopEventNode)eventNode, copier );
		}
		else if( eventNode instanceof ForEachInArrayLoopEventNode ) {
			rv = (T)ForEachInArrayLoopEventNode.createCopy( (ForEachInArrayLoopEventNode)eventNode, copier );
		}
		else if( eventNode instanceof EachInArrayTogetherEventNode ) {
			rv = (T)EachInArrayTogetherEventNode.createCopy( (EachInArrayTogetherEventNode)eventNode, copier );
		}
		else if( eventNode instanceof ContainerEventNode ) {
			rv = (T)ContainerEventNode.createCopy( (ContainerEventNode<?>)eventNode, copier );
		}
		else if( eventNode instanceof LocalDeclarationStatementEventNode ) {
			rv = (T)LocalDeclarationStatementEventNode.createCopy( (LocalDeclarationStatementEventNode)eventNode, copier );
		}
		else if( eventNode instanceof ReturnStatementEventNode ) {
			rv = (T)ReturnStatementEventNode.createCopy( (ReturnStatementEventNode)eventNode, copier );
		}
		else if( eventNode instanceof ExpressionEvaluationEventNode ) {
			rv = (T)ExpressionEvaluationEventNode.createCopy( (ExpressionEvaluationEventNode)eventNode, copier );
		}
		else if( eventNode instanceof CommentEventNode ) {
			rv = (T)CommentEventNode.createCopy( (CommentEventNode)eventNode, copier );
		}

		rv.setEventNodeId( eventNode.getEventNodeId() );

		return (T)rv;
	}

}
