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
package edu.wustl.lookingglass.codetest.external.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.lgna.project.ast.ExpressionStatement;

/**
 * @author Aaron Zemach
 */
public class SequenceAPI {

	/*
	 * Based on algorithm found at http://stackoverflow.com/questions/9614326/longest-repeated-substring-better-complexity
	 */

	public static ArrayList<ArrayList<ExpressionStatement>> longestRepeatedSubstring( List<ExpressionStatement> esArrayList ) {
		ExpressionStatement[] esList = new ExpressionStatement[ esArrayList.size() ];
		esArrayList.toArray( esList );
		int n = esList.length;
		ArrayList<ExpressionStatement[]> input = new ArrayList<ExpressionStatement[]>();

		//Precompute suffixes
		for( int i = 0; i < n; i++ ) {
			input.add( i, Arrays.copyOfRange( esList, i, n ) );
		}

		//Here in the original, input should be sorted...not sure the best way to do this...
		java.util.Collections.sort( input, new Comparator<ExpressionStatement[]>() {

			@Override
			public int compare( ExpressionStatement[] arg0, ExpressionStatement[] arg1 ) {
				int m;
				if( arg0.length > arg1.length ) {
					m = arg1.length;
				} else {
					m = arg0.length;
				}

				int i = 0;
				int diff = -1;
				while( ( i < m ) && ( diff == -1 ) ) {
					//if( !MethodsAPI.areIdenticalMethodCalls( arg0[ i ], arg1[ i ] ) ) {
					//TODO: This is a bit hacky, but for now it should work
					if( !( arg0[ i ].getRepr().equals( arg1[ i ].getRepr() ) ) ) {
						diff = i;
					}
					i++;
				}

				if( i == m ) {
					return 0;
				}

				String a = arg0[ diff ].getRepr();
				String b = arg1[ diff ].getRepr();
				return a.compareTo( b );
			}

		} );

		//Find longest repeated substring
		ExpressionStatement[] lrs = new ExpressionStatement[ 0 ];

		for( int j = 0; j < ( n - 1 ); j++ ) {
			ExpressionStatement[] temp = SequenceAPI.longestCommonSubstring( input.get( j ), input.get( j + 1 ) );
			if( temp.length > lrs.length ) {
				lrs = temp;
			}
		}

		//Construct an array of all instances of this LRS
		ArrayList<ArrayList<ExpressionStatement>> output = new ArrayList<ArrayList<ExpressionStatement>>();

		int c = 0;
		int lrsL = lrs.length;

		if( lrsL > 1 ) {
			while( c < n ) {

				//if( MethodsAPI.areIdenticalMethodCalls( lrs[ 0 ], esList[ c ] ) ) {
				//TODO: This is a bit hacky, but for now it should work
				if( ( lrs[ 0 ].getRepr().equals( esList[ c ].getRepr() ) ) ) {
					boolean passes = true;
					int start = c;
					int d = 0;
					while( ( c < n ) && ( d < lrsL ) && passes ) {
						//if( !MethodsAPI.areIdenticalMethodCalls( lrs[ d ], esList[ c ] ) ) {
						//TODO: This is a bit hacky, but for now it should work
						if( !( lrs[ d ].getRepr().equals( esList[ c ].getRepr() ) ) ) {
							passes = false;
						}
						c++;
						d++;
					}
					if( ( d == lrsL ) && passes ) {
						ArrayList<ExpressionStatement> temp = new ArrayList<ExpressionStatement>();
						temp.addAll( Arrays.asList( Arrays.copyOfRange( esList, start, start + lrsL ) ) );
						output.add( temp );
					}
				}

				c++;
			}
		}
		//Finally, return list of repeated chunks
		return output;
	}

	private static ExpressionStatement[] longestCommonSubstring( ExpressionStatement[] first, ExpressionStatement[] second ) {
		int n;
		if( first.length < second.length ) {
			n = first.length;
		} else {
			n = second.length;
		}

		for( int i = 0; i < n; i++ ) {
			//if( !MethodsAPI.areIdenticalMethodCalls( first[ i ], second[ i ] ) ) {
			//TODO: This is a bit hacky, but for now it should work
			if( !( first[ i ].getRepr().equals( second[ i ].getRepr() ) ) ) {
				return Arrays.copyOfRange( first, 0, i );
			}
		}

		return Arrays.copyOfRange( first, 0, n );
	}
}
