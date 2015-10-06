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
package edu.wustl.lookingglass.remix;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;

import edu.wustl.lookingglass.remix.models.EndCaptureState;
import edu.wustl.lookingglass.remix.models.StartCaptureState;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ConditionalStatementEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ContainerEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.CountLoopEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EachInArrayTogetherEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ExpressionEvaluationEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ExpressionStatementEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ForEachInArrayLoopEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.LambdaEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.LocalDeclarationStatementEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ReturnStatementEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.WhileLoopEventNode;

/**
 * @author Michael Pogran
 */
public class EventNodeTreeDialog extends org.lgna.croquet.views.Dialog {

	private static final org.lgna.croquet.Group DEBUG_REMIX_GROUP = org.lgna.croquet.Group.getInstance( java.util.UUID.fromString( "c13b9dd3-9917-4791-980e-586f427f4fe2" ), "DEBUG_REMIX_GROUP" );

	private void initializeDialog() {
		edu.wustl.lookingglass.ide.program.TimeScrubProgramImp program = edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().getDinahProgramImp();
		org.lgna.croquet.views.MigPanel panel = new org.lgna.croquet.views.MigPanel( null, "fill, ins 15", "[]", "[][]" );
		org.lgna.croquet.views.MigPanel content = new org.lgna.croquet.views.MigPanel( null, "fill" );

		content.setBorder( javax.swing.BorderFactory.createLineBorder( java.awt.Color.GRAY ) );
		content.setBackgroundColor( java.awt.Color.WHITE );

		if( program != null ) {
			AbstractEventNode<?> rootEventNode = program.getExecutionObserver().getRootEventNode();
			DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode( null );

			EventNodeTreeView tree = new EventNodeTreeView( rootTreeNode );
			EventNodeTreeRenderer renderer = new EventNodeTreeRenderer();
			renderer.setLeafIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "event-node-leaf", org.lgna.croquet.icon.IconSize.EXTRA_SMALL ) );
			renderer.setClosedIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "event-node", org.lgna.croquet.icon.IconSize.EXTRA_SMALL ) );
			renderer.setOpenIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "event-node", org.lgna.croquet.icon.IconSize.EXTRA_SMALL ) );
			tree.getAwtComponent().setCellRenderer( renderer );
			constructTree( rootEventNode, rootTreeNode );
			addLambdaTreeNodes( rootTreeNode, program.getExecutionObserver().getLambdaEventNodes() );

			content.addComponent( tree, "grow" );
		} else {
			content.addComponent( new org.lgna.croquet.views.Label( "No program found.", 1.15f ), "center" );
		}

		panel.addComponent( content, "cell 0 0, pushy, grow" );
		panel.addComponent( this.reloadTreeOperation.createButton(), "cell 0 1, center" );

		synchronized( this.getContentPane().getTreeLock() ) {
			this.getContentPane().getAwtComponent().removeAll();
			this.getContentPane().addCenterComponent( panel );
		}

	}

	private void addLambdaTreeNodes( DefaultMutableTreeNode treeNode, java.util.List<LambdaEventNode> lambdaNodes ) {
		for( LambdaEventNode node : lambdaNodes ) {
			constructTree( node, treeNode );
		}
	}

	private void constructTree( AbstractEventNode<?> eventNode, DefaultMutableTreeNode treeNode ) {

		if( eventNode instanceof ContainerEventNode ) {
			for( AbstractEventNode<?> childEventNode : ( (ContainerEventNode<?>)eventNode ).getChildren() ) {
				DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode( childEventNode );
				treeNode.add( childTreeNode );

				constructTree( childEventNode, childTreeNode );
			}
		}

		if( eventNode instanceof ConditionalStatementEventNode ) {
			ContainerEventNode<?> bodyEventNode = ( (ConditionalStatementEventNode)eventNode ).getBodyEventNode();
			DefaultMutableTreeNode bodyTreeNode = new DefaultMutableTreeNode( bodyEventNode );
			treeNode.add( bodyTreeNode );

			constructTree( bodyEventNode, bodyTreeNode );

			for( ExpressionEvaluationEventNode expressionEventNode : ( (ConditionalStatementEventNode)eventNode ).getConditionalEvaluations() ) {
				DefaultMutableTreeNode expressionTreeNode = new DefaultMutableTreeNode( expressionEventNode );
				treeNode.add( expressionTreeNode );

				constructTree( expressionEventNode, expressionTreeNode );
			}
		}
		else if( eventNode instanceof ExpressionStatementEventNode ) {
			if( ( (ExpressionStatementEventNode)eventNode ).isUserMethod() ) {
				ContainerEventNode<?> bodyEventNode = ( (ExpressionStatementEventNode)eventNode ).getUserMethodEventNode();
				DefaultMutableTreeNode bodyTreeNode = new DefaultMutableTreeNode( bodyEventNode );
				treeNode.add( bodyTreeNode );

				constructTree( bodyEventNode, bodyTreeNode );
			}

			for( ExpressionEvaluationEventNode expressionEventNode : ( (ExpressionStatementEventNode)eventNode ).getExpressionEvaluationNodes() ) {
				DefaultMutableTreeNode expressionTreeNode = new DefaultMutableTreeNode( expressionEventNode );
				treeNode.add( expressionTreeNode );

				constructTree( expressionEventNode, expressionTreeNode );
			}

		}
		else if( eventNode instanceof WhileLoopEventNode ) {
			for( ExpressionEvaluationEventNode expressionEventNode : ( (WhileLoopEventNode)eventNode ).getConditionalEvaluations() ) {
				DefaultMutableTreeNode expressionTreeNode = new DefaultMutableTreeNode( expressionEventNode );
				treeNode.add( expressionTreeNode );

				constructTree( expressionEventNode, expressionTreeNode );
			}
		}
		else if( eventNode instanceof CountLoopEventNode ) {
			ExpressionEvaluationEventNode expressionEventNode = ( (CountLoopEventNode)eventNode ).getCountExpressionNode();
			DefaultMutableTreeNode expressionTreeNode = new DefaultMutableTreeNode( expressionEventNode );
			treeNode.add( expressionTreeNode );

			constructTree( expressionEventNode, expressionTreeNode );
		}
		else if( eventNode instanceof ForEachInArrayLoopEventNode ) {
			ExpressionEvaluationEventNode expressionEventNode = ( (ForEachInArrayLoopEventNode)eventNode ).getArrayExpressionNode();
			DefaultMutableTreeNode expressionTreeNode = new DefaultMutableTreeNode( expressionEventNode );
			treeNode.add( expressionTreeNode );

			constructTree( expressionEventNode, expressionTreeNode );
		}
		else if( eventNode instanceof EachInArrayTogetherEventNode ) {
			ExpressionEvaluationEventNode expressionEventNode = ( (EachInArrayTogetherEventNode)eventNode ).getArrayExpressionNode();
			DefaultMutableTreeNode expressionTreeNode = new DefaultMutableTreeNode( expressionEventNode );
			treeNode.add( expressionTreeNode );

			constructTree( expressionEventNode, expressionTreeNode );
		}
		else if( eventNode instanceof LocalDeclarationStatementEventNode ) {
			ExpressionEvaluationEventNode expressionEventNode = ( (LocalDeclarationStatementEventNode)eventNode ).getInitializerExpressionNode();
			DefaultMutableTreeNode expressionTreeNode = new DefaultMutableTreeNode( expressionEventNode );
			treeNode.add( expressionTreeNode );

			constructTree( expressionEventNode, expressionTreeNode );
		}
		else if( eventNode instanceof ReturnStatementEventNode ) {
			ExpressionEvaluationEventNode expressionEventNode = ( (ReturnStatementEventNode)eventNode ).getExpressionNode();
			DefaultMutableTreeNode expressionTreeNode = new DefaultMutableTreeNode( expressionEventNode );
			treeNode.add( expressionTreeNode );

			constructTree( expressionEventNode, expressionTreeNode );
		}
		else if( eventNode instanceof ExpressionEvaluationEventNode ) {

			for( AbstractEventNode<?> childEventNode : ( (ExpressionEvaluationEventNode)eventNode ).getChildren() ) {
				DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode( childEventNode );
				treeNode.add( childTreeNode );

				constructTree( childEventNode, childTreeNode );
			}
		}
		else if( eventNode instanceof LambdaEventNode ) {
			DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode( eventNode );
			treeNode.add( childTreeNode );
			constructTree( ( (LambdaEventNode)eventNode ).getBodyEventNode(), childTreeNode );
		}
	}

	public org.lgna.croquet.Operation getLaunchDialogOperation() {
		return this.launchDialogOperation;
	}

	org.lgna.croquet.Operation reloadTreeOperation = new org.lgna.croquet.Operation( DEBUG_REMIX_GROUP, java.util.UUID.fromString( "10775214-bb2b-449e-a851-f88b0bdaea17" ) ) {
		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			initializeDialog();
			getContentPane().revalidateAndRepaint();
		}

		@Override
		protected void localize() {
			this.setName( "Reload Tree" );
		}
	};

	org.lgna.croquet.Operation launchDialogOperation = new org.lgna.croquet.Operation( DEBUG_REMIX_GROUP, java.util.UUID.fromString( "14b68df7-e283-41a2-800c-c283df478c0d" ) ) {
		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			getAwtComponent().setModal( false );
			initializeDialog();
			pack();
			setSize( 400, 400 );
			setVisible( true );
		}

		@Override
		protected void localize() {
			this.setName( "View EventNode Tree" );
		}
	};

	public class EventNodeTreeView extends org.lgna.croquet.views.SwingComponentView<javax.swing.JTree> {
		private final MutableTreeNode root;

		EventNodeTreeView( MutableTreeNode root ) {
			this.root = root;
		}

		@Override
		protected javax.swing.JTree createAwtComponent() {
			return new javax.swing.JTree( this.root );
		}

	}

	public class EventNodeTreeRenderer extends javax.swing.tree.DefaultTreeCellRenderer {

		@Override
		public java.awt.Component getTreeCellRendererComponent( javax.swing.JTree tree, java.lang.Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus ) {
			java.awt.Component rv = super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );

			if( value instanceof DefaultMutableTreeNode ) {
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;

				if( treeNode.getUserObject() instanceof AbstractEventNode<?> ) {
					AbstractEventNode<?> eventNode = (AbstractEventNode<?>)treeNode.getUserObject();
					AbstractEventNode<?> startEventNode = StartCaptureState.getInstance().getValue();
					AbstractEventNode<?> endEventNode = EndCaptureState.getInstance().getValue();

					if( eventNode.equals( startEventNode ) ) {
						rv.setForeground( java.awt.Color.GREEN );
					}
					else if( eventNode.equals( endEventNode ) ) {
						rv.setForeground( java.awt.Color.RED );
					}

					if( ( startEventNode != null ) && ( endEventNode != null ) ) {

						boolean inRemix = edu.wustl.lookingglass.remix.ast.RemixUtilities.isInRemix( eventNode ) || eventNode.equals( startEventNode ) || eventNode.equals( endEventNode );
						boolean isSharedParent = eventNode.equals( edu.wustl.lookingglass.virtualmachine.eventtracing.EventNodeUtilities.getSharedParentNode( startEventNode, endEventNode ) );

						if( inRemix ) {
							rv.setForeground( java.awt.Color.BLUE );
						}
						else if( isSharedParent ) {
							rv.setForeground( java.awt.Color.PINK );
						}
					}
				}
			}

			return rv;
		}
	}
}
