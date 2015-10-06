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

import org.lgna.project.ast.Statement;

import edu.wustl.lookingglass.ide.perspectives.dinah.finder.operations.ShowStatementLocationOperation;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractLoopEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EachInArrayTogetherEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EventNodeUtilities;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ExpressionStatementEventNode;

public abstract class StatementCaptureState {

	public abstract StatementCaptureState getDependentCaptureState();

	public abstract boolean isSelectionValid( AbstractEventNode<?> eventNode );

	public abstract String getInvalidReason( AbstractEventNode<?> eventNode );

	public abstract boolean isInSpecialContainerPosition( AbstractEventNode<?> eventNode );

	public abstract boolean isInSpecialMethodPosition( AbstractEventNode<?> eventNode );

	protected ShowStatementLocationOperation previewOperation;
	private final java.util.List<org.lgna.croquet.event.ValueListener<AbstractEventNode<?>>> valueListeners = edu.cmu.cs.dennisc.java.util.Lists.newCopyOnWriteArrayList();
	private AbstractEventNode<?> value;

	public ShowStatementLocationOperation getPreviewOperation() {
		return previewOperation;
	}

	public void setValue( AbstractEventNode<?> nextValue ) {
		AbstractEventNode<?> previousNode = this.value;
		this.value = nextValue;
		notifyListeners( previousNode, nextValue );

		if( previewOperation != null ) {
			previewOperation.setEventNode( nextValue );
		}
	}

	public AbstractEventNode<?> getValue() {
		return this.value;
	}

	public void removeCurrentSelection() {
		setValue( null );
	}

	public void addListener( org.lgna.croquet.event.ValueListener<AbstractEventNode<?>> listener ) {
		this.valueListeners.add( listener );
	}

	public boolean removeListener( org.lgna.croquet.event.ValueListener<AbstractEventNode<?>> listener ) {
		return this.valueListeners.remove( listener );
	}

	public void removeAllListeners() {
		this.valueListeners.clear();
	}

	private void notifyListeners( AbstractEventNode<?> prevValue, AbstractEventNode<?> nextValue ) {
		if( this.valueListeners.size() > 0 ) {
			org.lgna.croquet.event.ValueEvent<AbstractEventNode<?>> e = org.lgna.croquet.event.ValueEvent.createInstance( prevValue, nextValue, false );
			for( org.lgna.croquet.event.ValueListener<AbstractEventNode<?>> valueListener : this.valueListeners ) {
				valueListener.valueChanged( e );
			}
		}
	}

	public boolean isStatementSelected( Statement statement ) {
		if( this.getValue() != null ) {
			return this.getValue().getAstNode().equals( statement );
		} else {
			return false;
		}
	}

	/**
	 * This method determines whether two statements can be selected as the
	 * start and end of a remix based on whether their selection will break
	 * apart multiple executions of an iterating statement type (
	 * <code>AbstractLoop</code> or <code>AbstractEachInTogether</code>).
	 * Invalid selections include:
	 * <p>
	 * 1. Selecting a node that corresponds to the statement executing in a
	 * different iteration of the loop. <br>
	 * 2. Selecting a node that is outside the loop.
	 * </p>
	 * However, when a selected node in a loop is in a special container
	 * position (e.g. the first statement - or an ancestor of the first
	 * statement - in the first iteration of a loop) the restrictions listed
	 * above do not apply. Special container positions are defined independently
	 * for both the start and end capture states.
	 *
	 * @param checkEventNode The <code>AbstractEventNode</code> to check
	 * @param dependentEventNode the <code>AbstractEventNode</code> already
	 *            selected
	 *
	 * @return boolean stating whether checkEventNode is a valid selection
	 */
	public boolean isInValidContainerContext( AbstractEventNode<?> checkEventNode, AbstractEventNode<?> dependentEventNode ) {

		if( checkEventNode.equals( dependentEventNode ) ) {
			return true;
		}

		AbstractEventNode<?> sharedParent = EventNodeUtilities.getSharedParentNode( checkEventNode, dependentEventNode );

		// Check if shared parent is an iterating type or if it's contained in a loop
		if( EventNodeUtilities.isEventNodeIteratingType( sharedParent ) || EventNodeUtilities.isInIteratingType( sharedParent ) ) {

			if( !( sharedParent.equals( checkEventNode ) ) && !( sharedParent.equals( dependentEventNode ) ) ) {

				// Shared parent of this type indicates that nodes are in different iterations of an EachInTogether
				if( sharedParent instanceof EachInArrayTogetherEventNode ) {
					return false;
				}
				// Shared parent of this type indicates that nodes are in different iterations of a Loop
				else if( sharedParent instanceof AbstractLoopEventNode ) {
					return this.isInSpecialContainerPosition( checkEventNode ) && this.getDependentCaptureState().isInSpecialContainerPosition( dependentEventNode );
				}
			}
			// In the same iteration, which is valid
			return true;
		}
		boolean isDependInIter = EventNodeUtilities.isInIteratingType( dependentEventNode );
		boolean isCheckInIter = EventNodeUtilities.isInIteratingType( checkEventNode );

		// Check whether each node is either: in a loop and in a special position or not in a loop
		boolean isDependentClear = ( isDependInIter && this.getDependentCaptureState().isInSpecialContainerPosition( dependentEventNode ) ) || !isDependInIter;
		boolean isCheckClear = ( isCheckInIter && this.isInSpecialContainerPosition( checkEventNode ) ) || !isCheckInIter;

		return isDependentClear && isCheckClear;
	}

	/**
	 * This method determines whether two statements can be selected as the
	 * start and end of a remix based on whether their selection will break
	 * apart method invocations. Invalid selections include:
	 * <p>
	 * 1. Selecting statements from different invocations of the same method.<br>
	 * 2. Selecting a statement from outside the method invocation that would
	 * include the same method invocation in the remix.
	 * </p>
	 * However, when a selected node is in a special method position (e.g. the
	 * first statement - or an ancestor of the first statement - in the method)
	 * another invocation of that method can be used in the remix. This rule
	 * avoids the possibility of a selection splitting a method while also
	 * including the full method in the remix path.
	 *
	 * @param checkEventNode The <code>AbstractEventNode</code> to check
	 * @param dependentEventNode the <code>AbstractEventNode</code> already
	 *            selected
	 *
	 * @return boolean stating whether checkEventNode is a valid selection
	 */
	public boolean isInValidMethodContext( AbstractEventNode<?> checkEventNode, AbstractEventNode<?> dependentEventNode ) {
		if( checkEventNode.equals( dependentEventNode ) ) {
			return true;
		}

		ExpressionStatementEventNode checkMethodNode = EventNodeUtilities.getAncestorUserMethodEventNode( checkEventNode );
		ExpressionStatementEventNode dependentMethodNode = EventNodeUtilities.getAncestorUserMethodEventNode( dependentEventNode );

		// valid if inside the same method invocation
		if( ( !checkEventNode.equals( checkMethodNode ) ) && ( !dependentEventNode.equals( dependentMethodNode ) ) ) {
			if( ( dependentMethodNode != null ) && ( checkMethodNode != null ) ) {
				return checkMethodNode.equals( dependentMethodNode );
			}
		}

		java.util.List<AbstractEventNode<?>> nodes = EventNodeUtilities.getNodesBetween( checkEventNode, dependentEventNode, false );

		// This loop check to see if the same method is invoked between our two nodes
		for( AbstractEventNode<?> node : nodes ) {

			if( ( node instanceof ExpressionStatementEventNode ) && ( (ExpressionStatementEventNode)node ).isUserMethod() ) {

				// if the same method is invoked, we check whether or not the start or end state is splitting the method in some manner.
				if( ( checkMethodNode != null ) && ( checkMethodNode.getUserMethod().equals( ( (ExpressionStatementEventNode)node ).getUserMethod() ) ) ) {

					// checkEventNode is a statement inside the method, check if in okay position
					if( !checkMethodNode.equals( checkEventNode ) ) {
						return this.isInSpecialMethodPosition( checkEventNode );
					}
				}
				else if( ( dependentMethodNode != null ) && ( dependentMethodNode.getUserMethod().equals( ( (ExpressionStatementEventNode)node ).getUserMethod() ) ) ) {

					// dependentEventNode is a statement inside the method, check if in okay position
					if( !dependentMethodNode.equals( dependentEventNode ) ) {
						return this.getDependentCaptureState().isInSpecialMethodPosition( dependentEventNode );
					}
				}
			}
		}

		// checkEventNode is selecting the method itself, dependentEventNode is inside
		if( checkEventNode.equals( checkMethodNode ) && !dependentEventNode.equals( dependentMethodNode ) ) {
			if( dependentMethodNode == null ) {
				// pass
			} else {
				return this.getDependentCaptureState().isInSpecialMethodPosition( dependentEventNode );
			}
		}

		// opposite logic of previous conditional
		if( dependentEventNode.equals( dependentMethodNode ) && !checkEventNode.equals( checkMethodNode ) ) {
			if( checkMethodNode == null ) {
				// pass
			} else {
				return this.isInSpecialMethodPosition( checkEventNode );
			}
		}

		return true;
	}
}
