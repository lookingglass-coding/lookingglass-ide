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
package org.lgna.common;

/**
 * @author Dennis Cosgrove
 */
public class ProgramClosedException extends RuntimeException {
	/*package-private*/static boolean isProgramClosedException( Throwable t ) {
		assert t != null;

		if( t instanceof ProgramClosedException ) {
			return true;
		} else {
			Throwable cause = t.getCause();
			if( cause != null ) {
				return isProgramClosedException( cause );
			} else {
				return false;
			}
			//unnecessary: getTargetException() is equivalent to getCause() according to docs
			//			boolean rv;
			//			if( cause != null ) {
			//				rv = isProgramClosedException( cause );
			//			} else {
			//				rv = false;
			//			}
			//			if( t instanceof java.lang.reflect.InvocationTargetException ) {
			//				java.lang.reflect.InvocationTargetException ite = (java.lang.reflect.InvocationTargetException)t;
			//				return rv || isProgramClosedException( ite.getTargetException() );
			//			} else {
			//				return rv;
			//			}
		}
	}

	public static void invokeAndCatchProgramClosedException( Runnable runnable ) {
		try {
			runnable.run();
		} catch( RuntimeException re ) {
			if( isProgramClosedException( re ) ) {
				edu.cmu.cs.dennisc.java.util.logging.Logger.info( "ProgramClosedException caught." );
			} else {
				throw re;
			}
		}
	}

	public ProgramClosedException() {
	}

	public ProgramClosedException( String message ) {
		super( message );
	}

	public ProgramClosedException( String message, Throwable cause ) {
		super( message, cause );
	}
}
