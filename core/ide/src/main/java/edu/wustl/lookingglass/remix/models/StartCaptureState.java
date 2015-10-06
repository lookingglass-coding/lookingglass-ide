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

import org.lgna.croquet.views.Button;

import edu.wustl.lookingglass.ide.perspectives.dinah.finder.operations.ShowStatementLocationOperation;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractLoopEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ContainerEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EventNodeUtilities;

public class StartCaptureState extends StatementCaptureState {
	private static class SingletonHolder {
		private static StartCaptureState instance = new StartCaptureState();
	}

	public static StartCaptureState getInstance() {
		return SingletonHolder.instance;
	}

	private StartCaptureState() {
		this.previewOperation = new PreviewStartStatementOperation();
	}

	@Override
	public StatementCaptureState getDependentCaptureState() {
		return EndCaptureState.getInstance();
	}

	@Override
	public boolean isSelectionValid( AbstractEventNode<?> eventNode ) {
		AbstractEventNode<?> endStateValue = getDependentCaptureState().getValue();

		if( endStateValue == null ) {
			return true;
		}
		AbstractEventNode<?> sharedParent = EventNodeUtilities.getSharedParentNode( endStateValue, eventNode );

		boolean isParentThreaded = ( sharedParent.getParent() != null ) && EventNodeUtilities.isThreadedStatementType( sharedParent.getParent().getAstNode() ); // shared parent will be body of AbstractStatementWithBody
		boolean areTimesCorrect = ( endStateValue.getStartTime() >= eventNode.getStartTime() ) && ( endStateValue.getEndTime() >= eventNode.getEndTime() );
		boolean isValidContainer = isInValidContainerContext( eventNode, endStateValue );
		boolean isValidMethod = isInValidMethodContext( eventNode, endStateValue );

		return ( isParentThreaded || areTimesCorrect ) && isValidContainer && isValidMethod;
	}

	@Override
	public boolean isInSpecialContainerPosition( AbstractEventNode<?> eventNode ) {
		int iterNum = EventNodeUtilities.getIterationForEventNode( eventNode );
		ContainerEventNode<?> container = EventNodeUtilities.getIterationContainer( eventNode );

		if( ( container != null ) && ( container.getParent() instanceof AbstractLoopEventNode ) ) {

			// Special position for start is the first statement in the first iteration of the loop
			return ( iterNum == 0 ) && EventNodeUtilities.isFirstChild( container, eventNode );
		}
		return false;
	}

	@Override
	public boolean isInSpecialMethodPosition( AbstractEventNode<?> eventNode ) {
		ContainerEventNode<?> container = EventNodeUtilities.getMethodContainer( eventNode );
		return EventNodeUtilities.isFirstChild( container, eventNode );
	}

	@Override
	public String getInvalidReason( AbstractEventNode<?> eventNode ) {
		AbstractEventNode<?> endStateValue = getDependentCaptureState().getValue();

		if( endStateValue == null ) {
			//pass
		} else if( endStateValue.getStartTime() < eventNode.getStartTime() ) {
			return "startStartAfterEndStart";
		} else if( endStateValue.getEndTime() < eventNode.getEndTime() ) {
			return "startEndAfterEndEnd";
		} else if( !isInValidContainerContext( eventNode, endStateValue ) ) {
			return "invalidContainerContext";
		} else if( !isInValidMethodContext( eventNode, endStateValue ) ) {
			return "invalidMethodContext";
		}

		return "defaultError";
	}

	class PreviewStartStatementOperation extends ShowStatementLocationOperation {

		public PreviewStartStatementOperation() {
			super( java.util.UUID.fromString( "7a118228-0124-4f87-b8cc-f1a735787872" ) );
		}

		@Override
		public Button createButton( edu.cmu.cs.dennisc.java.awt.font.TextAttribute<?>... textAttributes ) {
			return super.createButton();
		}
	}
}
