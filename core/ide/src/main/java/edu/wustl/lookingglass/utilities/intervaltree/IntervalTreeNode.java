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

public class IntervalTreeNode<E extends Interval<Double>> {
	private double centerValue;
	private IntervalTreeNode<E> left = null, right = null;
	private java.util.TreeSet<E> centerBegins = new java.util.TreeSet<E>( new MinValueComparator() );
	private java.util.TreeSet<E> centerEnds = new java.util.TreeSet<E>( new MaxValueComparator() );

	public IntervalTreeNode( double centerValue ) {
		this.centerValue = centerValue;
	}

	public IntervalTreeNode( E element, double centerValue ) {
		this.centerValue = centerValue;
		this.insert( element );
	}

	public IntervalTreeNode( java.util.Collection<E> elements, double centerValue ) {
		java.util.List<E> leftList = new java.util.LinkedList<E>();
		java.util.List<E> rightList = new java.util.LinkedList<E>();

		this.centerValue = centerValue;

		double minLeft = Double.MAX_VALUE, maxLeft = Double.MIN_VALUE;
		double minRight = Double.MAX_VALUE, maxRight = Double.MIN_VALUE;

		for( E e : elements ) {
			if( e.getMaxValue() < centerValue ) {
				leftList.add( e );

				if( e.getMinValue() < minLeft ) {
					minLeft = e.getMinValue();
				}
				if( e.getMaxValue() > maxLeft ) {
					maxLeft = e.getMaxValue();
				}

			} else if( e.getMinValue() > centerValue ) {
				rightList.add( e );

				if( e.getMinValue() < minRight ) {
					minRight = e.getMinValue();
				}
				if( e.getMaxValue() > maxRight ) {
					maxRight = e.getMaxValue();
				}

			} else {
				centerBegins.add( e );
				centerEnds.add( e );
			}
		}

		// TODO: Consider better metrics for choosing center value other than the mean of the time interval end points

		if( !leftList.isEmpty() ) {
			left = new IntervalTreeNode<E>( leftList, ( minLeft + maxLeft ) / 2 );
		}
		if( !rightList.isEmpty() ) {
			right = new IntervalTreeNode<E>( rightList, ( maxRight + minRight ) / 2 );
		}
	}

	public double getCenterValue() {
		return centerValue;
	}

	public IntervalTreeNode<E> getLeft() {
		return left;
	}

	public IntervalTreeNode<E> getRight() {
		return right;
	}

	public java.util.TreeSet<E> getCenterBegins() {
		return centerBegins;
	}

	public java.util.TreeSet<E> getCenterEnds() {
		return centerEnds;
	}

	public void insert( E element ) {
		if( element.getMaxValue() < centerValue ) {
			if( left != null ) {
				left.insert( element );
			} else {
				left = new IntervalTreeNode<E>( element, ( element.getMaxValue() + element.getMinValue() ) / 2 );
			}
		} else if( element.getMinValue() > centerValue ) {
			if( right != null ) {
				right.insert( element );
			} else {
				right = new IntervalTreeNode<E>( element, ( element.getMaxValue() + element.getMinValue() ) / 2 );
			}
		} else {
			centerBegins.add( element );
			centerEnds.add( element );
		}
	}

	public java.util.List<E> findElementsContainingIndex( double searchIndex ) {
		java.util.List<E> rangeMatches;

		if( searchIndex < centerValue ) {
			if( left != null ) {
				rangeMatches = left.findElementsContainingIndex( searchIndex );
			} else {
				rangeMatches = new java.util.LinkedList<E>();
			}

			for( E e : centerBegins ) {
				if( e.getMinValue() <= searchIndex ) {
					rangeMatches.add( e );
				} else {
					break;
				}
			}
		} else if( searchIndex > centerValue ) {
			if( right != null ) {
				rangeMatches = right.findElementsContainingIndex( searchIndex );
			} else {
				rangeMatches = new java.util.LinkedList<E>();
			}

			for( E e : centerEnds ) {
				if( e.getMaxValue() >= searchIndex ) {
					rangeMatches.add( e );
				} else {
					break;
				}
			}
		} else {
			return new java.util.LinkedList<E>( centerBegins );
		}

		return rangeMatches;
	}

	public java.util.Collection<E> findElementsIntersectingRange( E element ) {
		return findElementsIntersectingRange( element.getMinValue(), element.getMaxValue() );
	}

	private java.util.List<E> findElementsIntersectingRange( double startIndex, double endIndex ) {
		java.util.List<E> intersectingRanges = new java.util.LinkedList<E>();

		if( ( startIndex < centerValue ) && ( endIndex > centerValue ) ) {
			if( left != null ) {
				intersectingRanges.addAll( left.findElementsIntersectingRange( startIndex, endIndex ) );
			}
			if( right != null ) {
				intersectingRanges.addAll( right.findElementsIntersectingRange( startIndex, endIndex ) );
			}

			intersectingRanges.addAll( centerBegins );
		} else if( endIndex <= centerValue ) {
			if( left != null ) {
				intersectingRanges.addAll( left.findElementsIntersectingRange( startIndex, endIndex ) );
			}

			for( E element : centerBegins ) {
				if( element.getMinValue() <= endIndex ) {
					intersectingRanges.add( element );
				} else {
					break;
				}
			}
		} else if( startIndex >= centerValue ) {
			if( right != null ) {
				intersectingRanges.addAll( right.findElementsIntersectingRange( startIndex, endIndex ) );
			}

			for( E element : centerEnds ) {
				if( element.getMaxValue() >= startIndex ) {
					intersectingRanges.add( element );
				} else {
					break;
				}
			}
		}

		return intersectingRanges;
	}

	public int size() {
		int rsize = 0, lsize = 0;
		if( right != null ) {
			rsize = right.size();
		}
		if( left != null ) {
			lsize = left.size();
		}
		return centerBegins.size() + rsize + lsize;
	}

	public int getMaxDepth() {
		int rdepth = 0, ldepth = 0;
		if( right != null ) {
			rdepth = right.getMaxDepth();
		}
		if( left != null ) {
			ldepth = left.getMaxDepth();
		}
		return 1 + Math.max( rdepth, ldepth );
	}

	@Override
	public String toString() {
		return "Center: " + centerValue + ", Ranges(" + centerBegins.size() + "): " + centerBegins;
	}

	public java.util.Collection<E> findElementsContainedInRange( E element ) {
		java.util.Collection<E> elements = findElementsIntersectingRange( element );
		java.util.LinkedList<E> result = new java.util.LinkedList<E>();

		for( E maybeContained : elements ) {
			if( ( element.getMinValue() <= maybeContained.getMinValue() ) &&
					( element.getMaxValue() >= maybeContained.getMaxValue() ) ) {
				result.add( maybeContained );
			}
		}

		return result;
	}
}
