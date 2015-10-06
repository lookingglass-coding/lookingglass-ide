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
package edu.wustl.lookingglass.remix.models;

import edu.wustl.lookingglass.ide.perspectives.dinah.finder.operations.ShowStatementLocationOperation;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractLoopEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ContainerEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EventNodeUtilities;

public class EndCaptureState extends StatementCaptureState {
	private static class SingletonHolder {
		private static EndCaptureState instance = new EndCaptureState();
	}

	public static EndCaptureState getInstance() {
		return SingletonHolder.instance;
	}

	private EndCaptureState() {
		this.previewOperation = new PreviewEndStatementOperation();
	}

	@Override
	public StatementCaptureState getDependentCaptureState() {
		return StartCaptureState.getInstance();
	}

	@Override
	public boolean isSelectionValid( AbstractEventNode<?> eventNode ) {
		AbstractEventNode<?> startCaptureState = getDependentCaptureState().getValue();

		if( startCaptureState == null ) {
			return true;
		}
		AbstractEventNode<?> sharedParent = EventNodeUtilities.getSharedParentNode( startCaptureState, eventNode );

		boolean isParentThreaded = EventNodeUtilities.isThreadedStatementType( sharedParent.getAstNode() );
		boolean areTimesCorrect = ( startCaptureState.getStartTime() <= eventNode.getStartTime() ) && ( startCaptureState.getEndTime() <= eventNode.getEndTime() );
		boolean isValidContainer = isInValidContainerContext( eventNode, startCaptureState );
		boolean isValidMethod = isInValidMethodContext( eventNode, startCaptureState );

		return ( isParentThreaded || areTimesCorrect ) && isValidContainer && isValidMethod;
	}

	@Override
	public boolean isInSpecialContainerPosition( AbstractEventNode<?> eventNode ) {
		int iterNum = EventNodeUtilities.getIterationForEventNode( eventNode );
		ContainerEventNode<?> container = EventNodeUtilities.getIterationContainer( eventNode );

		if( ( container != null ) && ( container.getParent() instanceof AbstractLoopEventNode ) ) {
			AbstractLoopEventNode<?> loopNode = (AbstractLoopEventNode<?>)container.getParent();

			// Special position for end is the last statement in the last iteration of the loop
			return ( iterNum == loopNode.getChildren().size()  - 1) && EventNodeUtilities.isLastChild( container, eventNode );
		}
		return false;
	}

	@Override
	public boolean isInSpecialMethodPosition( AbstractEventNode<?> eventNode ) {
		ContainerEventNode<?> container = EventNodeUtilities.getMethodContainer( eventNode );
		return EventNodeUtilities.isLastChild( container, eventNode );
	}

	@Override
	public String getInvalidReason( AbstractEventNode<?> eventNode ) {
		AbstractEventNode<?> startStateValue = getDependentCaptureState().getValue();

		if( startStateValue == null ) {
			// pass
		} else if( startStateValue.getStartTime() > eventNode.getStartTime() ) {
			return "endStartBeforeStartStart";
		} else if( startStateValue.getEndTime() > eventNode.getEndTime() ) {
			return "endEndBeforeStartEnd";
		} else if( !isInValidContainerContext( eventNode, startStateValue ) ) {
			return "invalidContainerContext";
		} else if( !isInValidMethodContext( eventNode, startStateValue ) ) {
			return "invalidMethodContext";
		}

		return "defaultError";
	}

	class PreviewEndStatementOperation extends ShowStatementLocationOperation {

		public PreviewEndStatementOperation() {
			super( java.util.UUID.fromString( "da75ea49-296e-42ed-a7d8-16bfca9b8cda" ) );
		}
	}
}
