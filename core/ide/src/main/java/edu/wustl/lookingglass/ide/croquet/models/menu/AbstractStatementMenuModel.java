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
package edu.wustl.lookingglass.ide.croquet.models.menu;

import java.util.UUID;

import javax.swing.Icon;

import org.lgna.croquet.StandardMenuItemPrepModel;
import org.lgna.project.ast.Statement;

import edu.wustl.lookingglass.ide.perspectives.dinah.DinahProgramManager;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;

public abstract class AbstractStatementMenuModel extends MutableMenuModel {
	private final Statement statement;
	private final DinahProgramManager programManager;

	protected abstract boolean isEventNodeAcceptable( AbstractEventNode<?> eventNode );

	protected abstract String getStatusKey( Statement statement );

	protected abstract StandardMenuItemPrepModel getModelForEventNode( AbstractEventNode<?> eventNode );

	protected abstract Icon getMenuIcon();

	public AbstractStatementMenuModel( UUID id, Statement statement, DinahProgramManager programManager ) {
		super( id );
		this.statement = statement;
		this.programManager = programManager;
	}

	public org.lgna.project.ast.Statement getStatement() {
		return this.statement;
	}

	public DinahProgramManager getProgramManager() {
		return this.programManager;
	}

	@Override
	protected void initialize() {
		super.initialize();

		if( this.programManager.getProgramImp().getExecutionObserver().eventNodesExistForStatement( this.statement ) ) {
			// pass
		} else {
			setEnabled( false );
			setName( findLocalizedText( "notRun" ) );
		}
		setSmallIcon( getMenuIcon() );
	}

	@Override
	protected org.lgna.croquet.StandardMenuItemPrepModel[] createModels() {
		java.util.List<StandardMenuItemPrepModel> models = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();

		if( this.programManager.getProgramImp().getExecutionObserver().eventNodesExistForStatement( statement ) ) {
			java.util.ArrayList<AbstractEventNode<?>> eventNodes = this.programManager.getProgramImp().getExecutionObserver().getEventNodesForStatement( statement );

			for( AbstractEventNode<?> eventNode : eventNodes ) {
				if( isEventNodeAcceptable( eventNode ) ) {
					models.add( getModelForEventNode( eventNode ) );
				}
			}
		}

		return edu.cmu.cs.dennisc.java.lang.ArrayUtilities.createArray( models, StandardMenuItemPrepModel.class );
	}

	@Override
	public org.lgna.croquet.views.ViewController<?, ?> createMenuItemAndAddTo( org.lgna.croquet.views.MenuItemContainer menuItemContainer ) {
		org.lgna.croquet.StandardMenuItemPrepModel[] models = createModels();
		if( models.length == 1 ) {
			org.lgna.croquet.StandardMenuItemPrepModel model = models[ 0 ];

			if( model instanceof org.lgna.croquet.imp.operation.OperationMenuItemPrepModel ) {
				org.lgna.croquet.imp.operation.OperationMenuItemPrepModel menuItemPrepModel = (org.lgna.croquet.imp.operation.OperationMenuItemPrepModel)model;
				menuItemPrepModel.getOperation().setName( findDefaultLocalizedText() );
				menuItemPrepModel.getOperation().setSmallIcon( getMenuIcon() );
			}

			return model.createMenuItemAndAddTo( menuItemContainer );
		} else if( models.length == 0 ) {
			String key = getStatusKey( this.statement );
			PlaceholderOperation placeholder = new PlaceholderOperation( findLocalizedText( key ), getMenuIcon() );
			return placeholder.getMenuItemPrepModel().createMenuItemAndAddTo( menuItemContainer );
		} else {
			return super.createMenuItemAndAddTo( menuItemContainer );
		}
	}

	private class PlaceholderOperation extends org.lgna.croquet.Operation {
		public PlaceholderOperation( String name, Icon icon ) {
			super( edu.wustl.lookingglass.remix.models.ReuseGroup.REUSE_GROUP, java.util.UUID.fromString( "7e14f999-039b-49e4-8818-950a698c7f95" ) );
			this.setName( name );
			if( icon != null ) {
				this.setSmallIcon( icon );
			}
			this.setEnabled( false );
		}

		@Override
		protected void perform( org.lgna.croquet.history.Transaction transaction, org.lgna.croquet.triggers.Trigger trigger ) {
			// pass
		}
	}
}
