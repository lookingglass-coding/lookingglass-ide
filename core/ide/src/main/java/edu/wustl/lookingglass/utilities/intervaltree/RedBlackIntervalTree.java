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
package edu.wustl.lookingglass.utilities.intervaltree;

public class RedBlackIntervalTree<T extends Interval<Double>> {
	protected Node root = null;
	protected java.util.Comparator<T> comparator;
	protected java.util.HashMap<T, Node> m_intervalMap = new java.util.HashMap<T, Node>();

	public RedBlackIntervalTree() {
	}

	public RedBlackIntervalTree( java.util.Comparator<T> comparator ) {
		this.comparator = comparator;
	}

	public void clear() {
		m_intervalMap.clear();
		root = null;
	}

	protected Node createNode( T interval ) {
		Node rv = new Node( interval );
		m_intervalMap.put( interval, rv );
		return rv;
	}

	protected Node getNode( Interval<Double> interval ) {
		return m_intervalMap.get( interval );
	}

	protected void removeNode( Interval<Double> interval ) {
		m_intervalMap.remove( interval );
	}

	public void insert( T interval ) {
		Node temp = root;
		Node parent = null;
		Node child = createNode( interval );

		while( temp != null ) {
			parent = temp;

			if( child.compareTo( temp ) < 0 ) {
				temp = left( temp );
			} else {
				temp = right( temp );
			}
		}

		child.parent = parent;

		if( parent == null ) {
			root = child;
		} else {
			int compareResult;
			if( comparator != null ) {
				compareResult = comparator.compare( child.value, parent.value );
			} else {
				compareResult = child.compareTo( parent );
			}

			if( compareResult < 0 ) {
				parent.left = child;
			} else {
				parent.right = child;
			}
		}

		fixMax( child );
		fixInsert( child );
	}

	public void remove( T interval ) {
		Node z = getNode( interval );
		Node x, y;
		if( ( left( z ) == null ) || ( right( z ) == null ) ) {
			y = z;
		} else {
			y = succesor( z );
		}

		if( left( y ) != null ) {
			x = left( y );
		} else {
			x = right( y );
		}

		x.parent = p( y );

		if( p( y ) == null ) {
			root = x;
		} else {
			if( y == left( p( y ) ) ) {
				y.parent.left = x;
			} else {
				y.parent.right = x;
			}
		}

		if( y != z ) {
			z.value = y.value;
		}

		if( color( y ) == NodeColor.BLACK ) {
			removeFixup( z );
		}
	}

	protected void removeFixup( Node x ) {

	}

	protected Node succesor( Node x ) {
		if( right( x ) != null ) {
			return minimum( right( x ) );
		}

		Node y = p( x );
		while( ( y != null ) && ( x == right( y ) ) ) {
			x = y;
			y = p( y );
		}

		return y;
	}

	protected Node minimum( Node x ) {
		while( left( x ) != null ) {
			x = left( x );
		}

		return x;
	}

	protected void fixMax( Node node ) {
		while( node != null ) {
			updateMax( node );
			node = p( node );
		}
	}

	protected void updateMax( Node node ) {
		double max = Math.max( node.childMax, node.value.getMaxValue() );
		//		double max = node.value.getMaxValue();
		if( right( node ) != null ) {
			max = Math.max( max, right( node ).childMax );
		}
		if( left( node ) != null ) {
			max = Math.max( max, left( node ).childMax );
		}

		node.childMax = max;
	}

	protected void fixInsert( Node node ) {
		while( ( p( node ) != null ) && ( color( p( node ) ) == NodeColor.RED ) ) {
			if( p( node ) == left( p( p( node ) ) ) ) {
				Node y = right( p( p( node ) ) );
				if( ( y != null ) && ( y.color == NodeColor.RED ) ) {
					p( node ).color = NodeColor.BLACK;
					y.color = NodeColor.BLACK;
					p( p( node ) ).color = NodeColor.RED;
					node = p( p( node ) );
				} else {
					if( node == right( p( node ) ) ) {
						node = p( node );
						leftRotate( node );
					}

					p( node ).color = NodeColor.BLACK;
					p( p( node ) ).color = NodeColor.RED;
					rightRotate( p( p( node ) ) );
				}
			} else {
				Node y = left( p( p( node ) ) );
				if( ( y != null ) && ( y.color == NodeColor.RED ) ) {
					p( node ).color = NodeColor.BLACK;
					y.color = NodeColor.BLACK;
					p( p( node ) ).color = NodeColor.RED;
					node = p( p( node ) );
				} else {
					if( node == left( p( node ) ) ) {
						node = p( node );
						rightRotate( node );
					}

					p( node ).color = NodeColor.BLACK;
					p( p( node ) ).color = NodeColor.RED;
					leftRotate( p( p( node ) ) );
				}
			}
		}

		root.color = NodeColor.BLACK;
	}

	protected void leftRotate( Node x ) {
		if( right( x ) != null ) {
			Node y = right( x );
			x.right = left( y );
			if( left( y ) != null ) {
				left( y ).parent = x;
			}
			y.parent = p( x );
			if( p( x ) == null ) {
				root = y;
			} else {
				if( x == left( p( x ) ) ) {
					p( x ).left = y;
				} else {
					p( x ).right = y;
				}
			}

			y.left = x;
			x.parent = y;

			//			y.childMax = x.childMax;
			updateMax( x );
			updateMax( y );
		}
	}

	protected void rightRotate( Node y ) {
		if( left( y ) != null ) {
			Node x = left( y );
			y.left = right( x );
			if( right( x ) != null ) {
				right( x ).parent = y;
			}
			x.parent = p( y );

			if( p( y ) == null ) {
				root = x;
			} else {
				if( y == left( p( y ) ) ) {
					p( y ).left = x;
				} else {
					p( y ).right = x;
				}
			}

			x.right = y;
			y.parent = x;

			//			x.childMax = y.childMax;

			updateMax( y );
			updateMax( x );
		}
	}

	public java.util.Collection<T> getNodesForTime( double time ) {
		java.util.HashSet<T> rv = new java.util.HashSet<T>();
		search( root, time, rv );

		return rv;
	}

	protected void search( Node n, double time, java.util.Collection<T> result ) {
		if( n == null ) {
			return;
		}

		// If p is to the right of the rightmost point of any interval
		// in this node and all children, there won't be any matches.
		if( time > n.childMax ) {
			return;
		}

		// Search left children
		if( left( n ) != null ) {
			search( left( n ), time, result );
		}

		// Check this node
		if( n.contains( time ) ) {
			result.add( n.value );
		}

		// If p is to the left of the start of this interval,
		// then it can't be in any child to the right.
		if( time < n.value.getMinValue() ) {
			return;
		}

		// Otherwise, search right children
		if( right( n ) != null ) {
			search( right( n ), time, result );
		}
	}

	public java.util.Collection<T> getNodesForInterval( Interval<Double> interval ) {
		java.util.HashSet<T> rv = new java.util.HashSet<T>();
		intervalSearch( root, interval, rv );

		return rv;
	}

	protected void intervalSearch( Node n, Interval<Double> i, java.util.Collection<T> result ) {
		if( n == null ) {
			return;
		}

		// If p is to the right of the rightmost point of any interval
		// in this node and all children, there won't be any matches.
		if( i.getMinValue() > n.childMax ) {
			return;
		}

		// Search left children
		if( left( n ) != null ) {
			intervalSearch( left( n ), i, result );
		}

		// Check this node
		if( n.overlapsWith( i ) ) {
			result.add( n.value );
		}

		// If p is to the left of the start of this interval,
		// then it can't be in any child to the right.
		if( i.getMaxValue() < n.value.getMinValue() ) {
			return;
		}

		// Otherwise, search right children
		if( right( n ) != null ) {
			intervalSearch( right( n ), i, result );
		}
	}

	protected void rotateLeft() {
	}

	protected Node p( Node n ) {
		return n.parent;
	}

	protected NodeColor color( Node n ) {
		return n.color;
	}

	protected Node left( Node n ) {
		return n.left;
	}

	protected Node right( Node n ) {
		return n.right;
	}

	protected enum NodeColor {
		RED,
		BLACK;
	}

	protected class Node implements Comparable<Node> {
		public T value;
		public double childMax;
		public Node left = null, right = null, parent = null;
		public NodeColor color = NodeColor.RED;

		Node( T object ) {
			value = object;
			childMax = object.getMaxValue();
		}

		public boolean contains( double x ) {
			return ( value.getMinValue() <= x ) && ( x <= value.getMaxValue() );
		}

		public boolean overlapsWith( Interval<Double> interval ) {
			return ( value.getMinValue() <= interval.getMaxValue() ) && ( value.getMaxValue() >= interval.getMinValue() );
		}

		@Override
		public int compareTo( Node o ) {
			double diff = this.value.getMinValue() - o.value.getMinValue();

			if( diff < 0 ) {
				return -1;
			} else if( diff > 0 ) {
				return 1;
			} else {
				return 0;
			}
		}

		@Override
		public boolean equals( Object o ) {
			if( o instanceof RedBlackIntervalTree.Node ) {
				Node n = (Node)o;
				return this.value == n.value;
			}

			return false;
		}

		@Override
		public String toString() {
			return value.toString();
		}

	}
}
