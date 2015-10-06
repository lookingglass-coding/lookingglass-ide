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

import javax.swing.Icon;

import org.lgna.project.ast.UserLocal;

import edu.wustl.lookingglass.ide.perspectives.dinah.DinahProgramManager;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ContainerEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.CountLoopEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EachInArrayTogetherEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EventNodeUtilities;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ForEachInArrayLoopEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.WhileLoopEventNode;

public abstract class AbstractStatementMenuOperation extends org.lgna.croquet.Operation {
	private final AbstractEventNode<?> eventNode;
	private final DinahProgramManager programManager;

	protected abstract Icon getMenuIcon();

	public AbstractStatementMenuOperation( AbstractEventNode<?> eventNode, DinahProgramManager programManager, java.util.UUID individualId ) {
		super( edu.wustl.lookingglass.remix.models.ReuseGroup.REUSE_GROUP, individualId );
		this.eventNode = eventNode;
		this.programManager = programManager;

		this.setSmallIcon( getMenuIcon() );
		this.setName( getLocalizedName() );
	}

	public DinahProgramManager getProgramManager() {
		return this.programManager;
	}

	public AbstractEventNode<?> getEventNode() {
		return eventNode;
	}

	protected String getLocalizedName() {
		StringBuilder sb = new StringBuilder();

		sb.append( String.format( "%2.2f", this.eventNode.getStartTime() ) );
		sb.append( " - " );

		if( this.eventNode.getEndTime() == Double.POSITIVE_INFINITY ) {
			sb.append( String.format( "%2.2f", this.programManager.getProgramImp().getMaxProgramTime() ) );
		} else {
			sb.append( String.format( "%2.2f", this.eventNode.getEndTime() ) );
		}

		ContainerEventNode<?> loopContainer = EventNodeUtilities.getIterationContainer( this.eventNode );

		if( loopContainer != null ) {
			sb.append( " [" );
			if( loopContainer.getParent() instanceof CountLoopEventNode ) {
				sb.append( this.findLocalizedText( "countLoop" ) );
				sb.append( " " );
				sb.append( ( (CountLoopEventNode)loopContainer.getParent() ).getIterationNumber( loopContainer ) );
			}
			else if( loopContainer.getParent() instanceof WhileLoopEventNode ) {
				sb.append( this.findLocalizedText( "whileLoop" ) );
				sb.append( " " );
				sb.append( ( (WhileLoopEventNode)loopContainer.getParent() ).getIterationNumber( loopContainer ) );
			}
			else if( loopContainer.getParent() instanceof ForEachInArrayLoopEventNode ) {
				ForEachInArrayLoopEventNode forEachNode = (ForEachInArrayLoopEventNode)loopContainer.getParent();
				UserLocal item = forEachNode.getItem();
				Object[] values = forEachNode.getArrayValue();

				sb.append( item.getName() );
				sb.append( " = " );
				sb.append( values[ forEachNode.getIterationNumber( loopContainer ) ] );
			}
			else if( loopContainer.getParent() instanceof EachInArrayTogetherEventNode ) {
				EachInArrayTogetherEventNode togetherNode = (EachInArrayTogetherEventNode)loopContainer.getParent();
				UserLocal item = togetherNode.getItem();

				sb.append( item.getName() );
				sb.append( " = " );
				sb.append( togetherNode.getCallerFieldForChild( loopContainer ).getName() );
			}
			sb.append( "]" );

		}

		return sb.toString();
	}
}
