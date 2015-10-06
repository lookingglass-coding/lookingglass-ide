/*******************************************************************************
 * Copyright (c) 2006, 2015, Carnegie Mellon University. All rights reserved.
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
 * 3. Products derived from the software may not be called "Alice", nor may
 *    "Alice" appear in their name, without prior written permission of
 *    Carnegie Mellon University.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Carnegie Mellon University"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 * ANY AND ALL EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT
 * SHALL THE AUTHORS, COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package edu.cmu.cs.dennisc.math;

/**
 * @author Dennis Cosgrove
 */
public abstract class AbstractMatrix4x4 implements edu.cmu.cs.dennisc.print.Printable {
	public abstract double[] getAsColumnMajorArray16( double[] rv );

	public final double[] getAsColumnMajorArray16() {
		return getAsColumnMajorArray16( new double[ 16 ] );
	}

	public abstract boolean isAffine();

	public abstract void setNaN();

	public abstract boolean isNaN();

	public abstract void setIdentity();

	public abstract boolean isIdentity();

	public abstract Vector4 setReturnValueToTransformed( Vector4 rv, Vector4 b );

	public final Vector4 createTransformed( Vector4 b ) {
		return setReturnValueToTransformed( new Vector4(), b );
	}

	public final void transform( Vector4 b ) {
		setReturnValueToTransformed( b, b );
	}

	public abstract Vector3 setReturnValueToTransformed( Vector3 rv, Vector3 b );

	public final Vector3 createTransformed( Vector3 b ) {
		return setReturnValueToTransformed( new Vector3(), b );
	}

	public final void transform( Vector3 b ) {
		setReturnValueToTransformed( b, b );
	}

	public abstract Point3 setReturnValueToTransformed( Point3 rv, Point3 b );

	public final Point3 createTransformed( Point3 b ) {
		return setReturnValueToTransformed( new Point3(), b );
	}

	public final void transform( Point3 b ) {
		setReturnValueToTransformed( b, b );
	}

	public abstract Vector4f setReturnValueToTransformed( Vector4f rv, Vector4f b );

	public final Vector4f createTransformed( Vector4f b ) {
		return setReturnValueToTransformed( new Vector4f(), b );
	}

	public final void transform( Vector4f b ) {
		setReturnValueToTransformed( b, b );
	}

	public abstract Vector3f setReturnValueToTransformed( Vector3f rv, Vector3f b );

	public final Vector3f createTransformed( Vector3f b ) {
		return setReturnValueToTransformed( new Vector3f(), b );
	}

	public final void transform( Vector3f b ) {
		setReturnValueToTransformed( b, b );
	}

	public abstract Point3f setReturnValueToTransformed( Point3f rv, Point3f b );

	public final Point3f createTransformed( Point3f b ) {
		return setReturnValueToTransformed( new Point3f(), b );
	}

	public final void transform( Point3f b ) {
		setReturnValueToTransformed( b, b );
	}

	public abstract boolean isWithinEpsilonOfIdentity( double epsilon );

	public boolean isWithinReasonableEpsilonOfIdentity() {
		return this.isWithinEpsilonOfIdentity( EpsilonUtilities.REASONABLE_EPSILON );
	}

	@Override
	public String toString() {
		double[] val = getAsColumnMajorArray16();
		return "[" + val[ 0 ] + "|" + val[ 4 ] + "|" + val[ 8 ] + "|" + val[ 12 ] + "]\n" + "[" + val[ 1 ] + "|" + val[ 5 ] + "|"
				+ val[ 9 ] + "|" + val[ 13 ] + "]\n" + "[" + val[ 2 ] + "|" + val[ 6 ] + "|" + val[ 10 ] + "|" + val[ 14 ] + "]\n" + "["
				+ val[ 3 ] + "|" + val[ 7 ] + "|" + val[ 11 ] + "|" + val[ 15 ] + "]\n";
	}
}
