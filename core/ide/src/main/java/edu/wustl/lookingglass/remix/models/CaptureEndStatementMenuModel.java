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

import java.awt.Dimension;

import javax.swing.Icon;

import org.lgna.croquet.StandardMenuItemPrepModel;
import org.lgna.project.ast.Statement;

import edu.wustl.lookingglass.ide.croquet.components.StatementSelectionIcon;
import edu.wustl.lookingglass.ide.croquet.models.menu.AbstractStatementMenuModel;
import edu.wustl.lookingglass.ide.perspectives.dinah.DinahProgramManager;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;

public class CaptureEndStatementMenuModel extends AbstractStatementMenuModel {

	public CaptureEndStatementMenuModel( Statement statement, DinahProgramManager programManager ) {
		super( java.util.UUID.fromString( "3a8eacef-d871-4445-8ce1-60d8f5310711" ), statement, programManager );
	}

	@Override
	protected boolean isEventNodeAcceptable( AbstractEventNode executionInstance ) {
		return EndCaptureState.getInstance().isSelectionValid( executionInstance );
	}

	@Override
	protected String getStatusKey( Statement statement ) {

		if( getProgramManager().getProgramImp().getExecutionObserver().eventNodesExistForStatement( statement ) ) {
			for( AbstractEventNode eventNode : getProgramManager().getProgramImp().getExecutionObserver().getEventNodesForStatement( statement ) ) {
				return EndCaptureState.getInstance().getInvalidReason( eventNode );
			}
		}
		return "notPlayed";
	}

	@Override
	protected StandardMenuItemPrepModel getModelForEventNode( AbstractEventNode eventNode ) {
		return new CaptureEndStatementOperation( eventNode, getProgramManager() ).getMenuItemPrepModel();
	}

	@Override
	protected Icon getMenuIcon() {
		return new StatementSelectionIcon( new Dimension( 22, 22 ), false, true );
	}
}
