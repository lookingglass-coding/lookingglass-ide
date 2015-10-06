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
package edu.wustl.lookingglass.ide.perspectives.dinah.finder.operations.zoomout;

import org.lgna.croquet.LabelMenuSeparatorModel;
import org.lgna.croquet.StandardMenuItemPrepModel;
import org.lgna.project.ast.Statement;

import edu.wustl.lookingglass.ide.croquet.models.menu.MutableMenuModel;
import edu.wustl.lookingglass.ide.perspectives.dinah.DinahProgramManager;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EventNodeUtilities;

public class ZoomOutMenuModel extends MutableMenuModel {
	private final DinahProgramManager programManager;
	private final Statement statement;

	private final static ActionBoxLabelMenuSeparator ACTION_SEPARATOR = new ActionBoxLabelMenuSeparator();
	private final static SuperActionLabelMenuSeparator SUPER_SEPARATOR = new SuperActionLabelMenuSeparator();

	public ZoomOutMenuModel( Statement statement, DinahProgramManager programManager ) {
		super( java.util.UUID.fromString( "4c39c51c-daa6-4f3c-9bf9-9644bbb62bc9" ) );
		this.statement = statement;
		this.programManager = programManager;
	}

	@Override
	protected void initialize() {
		super.initialize();

		if( this.programManager.getProgramImp().getExecutionObserver().eventNodesExistForStatement( this.statement ) ) {
			// pass
		} else {
			setEnabled( false );
			setName( findLocalizedText( "noRun" ) );
		}
		this.setSmallIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "zoom-out", org.lgna.croquet.icon.IconSize.EXTRA_SMALL ) );
	}

	@Override
	protected StandardMenuItemPrepModel[] createModels() {
		java.util.List<StandardMenuItemPrepModel> models = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();
		java.util.ArrayList<AbstractEventNode<?>> eventNodes = this.programManager.getProgramImp().getExecutionObserver().getEventNodesForStatement( this.statement );

		if( ( eventNodes != null ) && ( eventNodes.size() > 0 ) ) {
			AbstractEventNode<?> firstEventNode = eventNodes.iterator().next();

			models.add( ACTION_SEPARATOR );
			models.add( new ZoomOutToParentConstructOperation( firstEventNode, programManager ).getMenuItemPrepModel() );
			models.add( SEPARATOR );
			models.add( SUPER_SEPARATOR );

			java.util.ArrayList<AbstractEventNode<?>> parentUserMethodNodes = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();

			for( AbstractEventNode<?> node : eventNodes ) {
				AbstractEventNode<?> methodNode = EventNodeUtilities.getAncestorUserMethodEventNode( node );
				if( methodNode != node ) {
					parentUserMethodNodes.add( methodNode );
				}
			}

			if( parentUserMethodNodes.size() > 1 ) {
				ZoomOutToInvokingMethodMenuModel model = new ZoomOutToInvokingMethodMenuModel( parentUserMethodNodes, this.programManager );
				models.add( model );
			} else {
				AbstractEventNode<?> methodNode = parentUserMethodNodes.size() > 0 ? parentUserMethodNodes.get( 0 ) : null;
				models.add( new ZoomOutToInvokingMethodOperation( methodNode, this.programManager ).getMenuItemPrepModel() );
			}

		}

		return edu.cmu.cs.dennisc.java.lang.ArrayUtilities.createArray( models, StandardMenuItemPrepModel.class );
	}

	private static class ActionBoxLabelMenuSeparator extends LabelMenuSeparatorModel {
		public ActionBoxLabelMenuSeparator() {
			super( java.util.UUID.fromString( "ad4e020c-2015-44cc-9ab1-dc791ec59652" ) );
		}
	}

	private static class SuperActionLabelMenuSeparator extends org.lgna.croquet.LabelMenuSeparatorModel {
		public SuperActionLabelMenuSeparator() {
			super( java.util.UUID.fromString( "c48b7718-85c1-4cfa-9433-a5de8ba095e2" ) );
		}
	}
}
