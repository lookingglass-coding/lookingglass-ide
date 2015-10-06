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
package edu.wustl.lookingglass.utilities.ui;

import javax.swing.tree.MutableTreeNode;

public abstract class KeyIndexTreeModel<N extends IndexedChildObjectTreeNode, K> extends SortedTreeModel {

	protected java.util.HashMap<Object, N> rootNodes = new java.util.HashMap<Object, N>();

	public KeyIndexTreeModel( MutableTreeNode root ) {
		super( root );
	}

	public KeyIndexTreeModel( MutableTreeNode root, boolean allowsChildren ) {
		super( root, allowsChildren );
	}

	public void addObject( K object, javax.swing.JTree tree ) {
		Object key = getRootNodeIndexKey( object );

		N objectNode = rootNodes.get( key );

		if( objectNode == null ) {
			objectNode = createNewRootNode( object );
			synchronized( rootNodes ) {
				rootNodes.put( key, objectNode );
			}
		}

		invokeInsertNodeLater( tree, objectNode, createNewChildObjectNode( object ), (MutableTreeNode)getRoot() );
	}

	protected void invokeInsertNodeLater( final javax.swing.JTree tree, final N parent, final MutableTreeNode child, final MutableTreeNode root ) {
		javax.swing.SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				if( parent.getParent() == null ) {
					insertNodeInto( parent, root, 0 );
				}

				insertNodeInto( child, parent, 0 );

				if( parent.getParent() != null ) {
					tree.expandPath( new javax.swing.tree.TreePath( getRoot() ) );
					expandAllTreeRows( tree );
				}
			}
		} );
	}

	public void removeObject( final K object ) {
		Object key = getRootNodeIndexKey( object );
		N parent = rootNodes.get( key );

		if( parent != null ) {
			invokeRemoveNodeLater( object, parent );
		}
	}

	protected void invokeRemoveNodeLater( final K object, final N parent ) {
		javax.swing.SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				try {
					removeNodeFromParent( parent.removeChildObjectNode( object ) );
					if( parent.getParent() != null ) {
						if( parent.getChildCount() == 0 ) {
							removeNodeFromParent( parent );
						} else {
							reload( parent );
						}
					}
				} catch( ArrayIndexOutOfBoundsException e ) {
					edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( e, this );
				} catch( NullPointerException e ) {
					edu.cmu.cs.dennisc.java.util.logging.Logger.outln( "ERROR: ", object, parent );
				}
			}
		} );
	}

	// If you're filtering the root's children and you want to filter something out that exists
	protected void invokeRemoveExistingNodeFromRootLater( final N node ) {
		javax.swing.SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				if( ( node != null ) && ( node.getParent() != null ) ) {
					removeNodeFromParent( node );
				}
			}
		} );
	}

	static public void expandAllTreeRows( javax.swing.JTree tree ) {
		for( int row = 0; row < tree.getRowCount(); row++ ) {
			tree.expandRow( row );
		}
	}

	protected abstract N createNewRootNode( K object );

	protected abstract MutableTreeNode createNewChildObjectNode( K object );

	protected abstract Object getRootNodeIndexKey( K object );
}
