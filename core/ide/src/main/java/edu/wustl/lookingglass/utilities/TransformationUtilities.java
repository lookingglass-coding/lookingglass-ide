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
package edu.wustl.lookingglass.utilities;

import edu.cmu.cs.dennisc.math.AffineMatrix4x4;
import edu.cmu.cs.dennisc.math.EpsilonUtilities;
import edu.cmu.cs.dennisc.math.UnitQuaternion;

/**
 * @author Michael Pogran
 */
public class TransformationUtilities {

	private static UnitQuaternion transformBuffer = UnitQuaternion.createNaN();

	public static float[] encode( AffineMatrix4x4 transformMatrix ) {
		if( transformMatrix != null ) {
			float bScale = 1.0f, rScale = 1.0f, uScale = 1.0f;

			boolean isNormal = transformMatrix.orientation.isWithinReasonableEpsilonOfUnitLengthSquared();
			if( !isNormal ) {
				bScale = (float)transformMatrix.orientation.backward.calculateMagnitude();
				rScale = (float)transformMatrix.orientation.right.calculateMagnitude();
				uScale = (float)transformMatrix.orientation.up.calculateMagnitude();

				AffineMatrix4x4 temp = new AffineMatrix4x4( transformMatrix );
				temp.orientation.normalizeColumns();
				transformMatrix = temp;
			}

			transformBuffer.setValue( transformMatrix.orientation );
			assert EpsilonUtilities.isWithinReasonableEpsilon( transformBuffer.calculateMagnitude(), 1.0 );

			float[] a = new float[ ( isNormal ) ? 7 : 10 ];
			a[ 0 ] = (float)transformBuffer.x;
			a[ 1 ] = (float)transformBuffer.y;
			a[ 2 ] = (float)transformBuffer.z;
			a[ 3 ] = (float)transformBuffer.w;
			a[ 4 ] = (float)transformMatrix.translation.x;
			a[ 5 ] = (float)transformMatrix.translation.y;
			a[ 6 ] = (float)transformMatrix.translation.z;

			if( !isNormal ) {
				a[ 7 ] = bScale;
				a[ 8 ] = rScale;
				a[ 9 ] = uScale;
			}

			return a;
		}
		return null;
	}

	public static AffineMatrix4x4 decode( AffineMatrix4x4 rv, float[] a ) {
		double qx = a[ 0 ];
		double qy = a[ 1 ];
		double qz = a[ 2 ];
		double qw = a[ 3 ];
		transformBuffer.set( qx, qy, qz, qw );
		assert EpsilonUtilities.isWithinReasonableEpsilon( transformBuffer.calculateMagnitude(), 1.0 );
		rv.orientation.setValue( transformBuffer );
		rv.translation.x = a[ 4 ];
		rv.translation.y = a[ 5 ];
		rv.translation.z = a[ 6 ];

		if( a.length == 10 ) {
			rv.orientation.backward.multiply( a[ 7 ] );
			rv.orientation.right.multiply( a[ 8 ] );
			rv.orientation.up.multiply( a[ 9 ] );
		}

		return rv;
	}

	public static AffineMatrix4x4 decode( float[] a ) {
		return decode( new AffineMatrix4x4(), a );
	}
}
