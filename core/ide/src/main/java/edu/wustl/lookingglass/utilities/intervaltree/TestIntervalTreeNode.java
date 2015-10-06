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

public class TestIntervalTreeNode {

	/**
	 * @param args
	 */
	public static void main( String[] args ) {
		java.util.LinkedList<TestInterval> list = new java.util.LinkedList<TestInterval>();
		java.util.Random r = new java.util.Random();

		int maxValue = 200, numIntervals = 1000;
		int numTests = 2000;

		for( int i = 0; i < numIntervals; i++ ) {
			int temp = r.nextInt( maxValue );
			list.add( new TestInterval( temp, r.nextInt( ( maxValue + 1 ) - temp ) + temp, "Interval " + i ) );
		}

		IntervalTreeNode<TestInterval> root = new IntervalTreeNode<TestInterval>( list, maxValue / 2 );

		java.util.Collection<TestInterval> results;

		try {
			for( int i = -1; i < ( maxValue + 1 ); i++ ) {
				results = root.findElementsContainingIndex( i );

				for( TestInterval t : list ) {
					if( ( i >= t.getMinValue() ) && ( i <= t.getMaxValue() ) ) {
						if( !results.remove( t ) ) {
							throw new Exception( "Error finding index " + i + ", did not have interval: " + t );
						}
					}
				}

				if( results.size() > 0 ) {
					throw new Exception( "Still have intervals left for finding index " + i + ", they are: " + results );
				}
			}

			System.out.println( "SUCCESS: Find elements by index" );

		} catch( Exception e ) {
			System.out.println( e.getMessage() );
			printTree( root );
		}

		try {
			for( int i = 0; i < numTests; i++ ) {
				int start = r.nextInt( maxValue );
				int end = r.nextInt( maxValue - start ) + start;
				String originalResults = "";

				//				results = root.findElementsIntersectingRange(new TestInterval(start, end, "Test Range"));
				//				originalResults = results.toString();
				//
				//				for(TestInterval t : list) {
				//					if(start >= t.getMinValue() && start <= t.getMaxValue() ||
				//					   end >= t.getMinValue() && end <= t.getMaxValue() ||
				//					   start <= t.getMinValue() && end >= t.getMaxValue()) {
				//						if(!results.remove(t))
				//							throw new Exception("Error finding range [" + start + ", " + end + "], did not have interval: " + t + "\nOriginal Results: " + originalResults);
				//					}
				//				}
				//
				//				if(results.size() > 0) {
				//					throw new Exception("Still have intervals left for finding range [" + start + ", " + end + "], they are: " + results + "\nOriginal Results: " + originalResults);
				//				}

				results = root.findElementsContainedInRange( new TestInterval( start, end, "Test Range" ) );
				originalResults = results.toString();

				for( TestInterval t : list ) {
					if( ( start <= t.getMinValue() ) && ( end >= t.getMaxValue() ) ) {
						if( !results.remove( t ) ) {
							throw new Exception( "Error finding range [" + start + ", " + end + "], did not have interval: " + t + "\nOriginal Results: " + originalResults );

						}
					}
				}

				if( results.size() > 0 ) {
					throw new Exception( "Still have intervals left for finding range [" + start + ", " + end + "], they are: " + results + "\nOriginal Results: " + originalResults );
				}

			}

			System.out.println( "SUCCESS: Find elements by range" );
		} catch( Exception e ) {
			System.out.println( e.getMessage() );
			printTree( root );
		}
	}

	public static void printTree( IntervalTreeNode root ) {
		printTree_helper( root, "", "" );
	}

	private static void printTree_helper( IntervalTreeNode current, String tabs, String side ) {
		if( current != null ) {
			System.out.println( tabs + side + " " + current );

			tabs += "\t";
			printTree_helper( current.getLeft(), tabs, "LEFT: " );

			printTree_helper( current.getRight(), tabs, "RIGHT: " );
		}
	}

	private static class TestInterval implements Interval<Double> {

		private double min, max;
		String name;

		TestInterval( double min, double max, String name ) {
			this.min = min;
			this.max = max;
			this.name = name;
		}

		@Override
		public Double getMaxValue() {
			// TODO Auto-generated method stub
			return max;
		}

		@Override
		public Double getMinValue() {
			// TODO Auto-generated method stub
			return min;
		}

		@Override
		public String toString() {
			return name + " [" + min + ", " + max + "]";
		}
	}
}
