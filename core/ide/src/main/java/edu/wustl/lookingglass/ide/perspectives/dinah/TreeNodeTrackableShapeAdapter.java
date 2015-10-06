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
package edu.wustl.lookingglass.ide.perspectives.dinah;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.lgna.croquet.views.ScreenElement;
import org.lgna.croquet.views.ScrollPane;
import org.lgna.croquet.views.TrackableShape;

public class TreeNodeTrackableShapeAdapter implements TrackableShape {

	private JTree tree = null;
	//	private TreeNode treeNode = null;
	private TreePath treePath = null;

	public TreeNodeTrackableShapeAdapter( JTree tree, TreeNode treeNode ) {
		this.tree = tree;
		//		this.treeNode = treeNode;

		treePath = getTreePathFor( treeNode );
	}

	private TreePath getTreePathFor( TreeNode treeNode ) {
		List<TreeNode> list = new ArrayList<TreeNode>();
		TreeNode pathNode = treeNode;

		while( pathNode != null ) {
			list.add( pathNode );
			pathNode = pathNode.getParent();
		}
		Collections.reverse( list );

		TreePath pathToNode = new TreePath( list.toArray() );
		return pathToNode;
	}

	@Override
	public boolean isInView() {
		return tree.isVisible( treePath );
	}

	//	public java.awt.Shape getShape( org.lgna.croquet.views.ScreenElement asSeenBy, java.awt.Insets insets ) {
	//	org.lgna.croquet.views.Component<?> src = CodeEditor.this.getAsSeenBy();
	//	if( src != null ) {
	//		java.awt.Rectangle rv = src.convertRectangle( this.boundsAtIndex, asSeenBy );
	//		//note: ignore insets
	//		return rv;
	//	} else {
	//		return null;
	//	}
	//}

	@Override
	public Shape getShape( ScreenElement asSeenBy, Insets insets ) {
		Rectangle rect = tree.getPathBounds( treePath );

		if( rect != null ) {
			return javax.swing.SwingUtilities.convertRectangle( tree, rect, asSeenBy.getAwtComponent() );
		} else {
			return null;
		}
	}

	//	public java.awt.Shape getVisibleShape( org.lgna.croquet.views.ScreenElement asSeenBy, java.awt.Insets insets ) {
	//	org.lgna.croquet.views.Component<?> src = CodeEditor.this.getAsSeenBy();
	//	if( src != null ) {
	//		java.awt.Rectangle bounds = src.convertRectangle( this.boundsAtIndex, asSeenBy );
	//		//note: ignore insets
	////			java.awt.Rectangle visibleBounds = statementListPropertyPane.getVisibleRectangle( asSeenBy );
	////			return bounds.intersection( visibleBounds );
	//		return bounds;
	//	} else {
	//		return null;
	//	}
	//}

	@Override
	public Shape getVisibleShape( ScreenElement asSeenBy, Insets insets ) {
		// TODO Auto-generated method stub
		return getShape( asSeenBy, insets );
	}

	@Override
	public ScrollPane getScrollPaneAncestor() {
		//		Container parent = tree.getParent();
		//		while (parent != null) {
		//			if (parent instanceof ScrollPane) {
		//
		//			}
		//		}
		// I don't think I can get a croquet ScrollPane ancestor
		return null;
	}

	@Override
	public void addComponentListener( ComponentListener listener ) {
		tree.addComponentListener( listener );
	}

	@Override
	public void removeComponentListener( ComponentListener listener ) {
		tree.removeComponentListener( listener );
	}

	@Override
	public void addHierarchyBoundsListener( HierarchyBoundsListener listener ) {
		tree.addHierarchyBoundsListener( listener );
	}

	@Override
	public void removeHierarchyBoundsListener( HierarchyBoundsListener listener ) {
		tree.removeHierarchyBoundsListener( listener );
	}
}

//public class StatementListIndexTrackableShape implements org.lgna.croquet.views.TrackableShape {
//	private org.lgna.project.ast.StatementListProperty statementListProperty;
//	private int index;
//	private StatementListPropertyView statementListPropertyPane;
//	private java.awt.Rectangle boundsAtIndex;
//	private StatementListIndexTrackableShape( org.lgna.project.ast.StatementListProperty statementListProperty, int index, StatementListPropertyView statementListPropertyPane, java.awt.Rectangle boundsAtIndex ) {
//		this.statementListProperty = statementListProperty;
//		this.index = index;
//		this.statementListPropertyPane = statementListPropertyPane;
//		this.boundsAtIndex = boundsAtIndex;
//	}
//
//	private org.lgna.project.ast.StatementListProperty getStatementListProperty() {
//		return this.statementListProperty;
//	}
//	public org.lgna.project.ast.BlockStatement getBlockStatement() {
//		return (org.lgna.project.ast.BlockStatement)this.statementListProperty.getOwner();
//	}
//	public int getIndex() {
//		return this.index;
//	}
//
//	public java.awt.Shape getShape( org.lgna.croquet.views.ScreenElement asSeenBy, java.awt.Insets insets ) {
//		org.lgna.croquet.views.Component<?> src = CodeEditor.this.getAsSeenBy();
//		if( src != null ) {
//			java.awt.Rectangle rv = src.convertRectangle( this.boundsAtIndex, asSeenBy );
//			//note: ignore insets
//			return rv;
//		} else {
//			return null;
//		}
//	}
//	public java.awt.Shape getVisibleShape( org.lgna.croquet.views.ScreenElement asSeenBy, java.awt.Insets insets ) {
//		org.lgna.croquet.views.Component<?> src = CodeEditor.this.getAsSeenBy();
//		if( src != null ) {
//			java.awt.Rectangle bounds = src.convertRectangle( this.boundsAtIndex, asSeenBy );
//			//note: ignore insets
////				java.awt.Rectangle visibleBounds = statementListPropertyPane.getVisibleRectangle( asSeenBy );
////				return bounds.intersection( visibleBounds );
//			return bounds;
//		} else {
//			return null;
//		}
//	}
//	public boolean isInView() {
//		if( isWarningAlreadyPrinted ) {
//			//pass
//		} else {
//			edu.cmu.cs.dennisc.java.util.logging.Logger.info( "getTrackableShapeAtIndexOf" );
//			isWarningAlreadyPrinted = true;
//		}
//		return true;
//	}
//	public org.lgna.croquet.views.ScrollPane getScrollPaneAncestor() {
//		return this.statementListPropertyPane.getScrollPaneAncestor();
//	}
//	public void addComponentListener(java.awt.event.ComponentListener listener) {
//		this.statementListPropertyPane.addComponentListener(listener);
//	}
//	public void removeComponentListener(java.awt.event.ComponentListener listener) {
//		this.statementListPropertyPane.removeComponentListener(listener);
//	}
//	public void addHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener listener) {
//		this.statementListPropertyPane.addHierarchyBoundsListener(listener);
//	}
//	public void removeHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener listener) {
//		this.statementListPropertyPane.removeHierarchyBoundsListener(listener);
//	}
//}
