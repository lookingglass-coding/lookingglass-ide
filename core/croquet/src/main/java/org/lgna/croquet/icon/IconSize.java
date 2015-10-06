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
package org.lgna.croquet.icon;

/**
 * @author Dennis Cosgrove
 */
public enum IconSize {

	// If custom sizes are needed place in non-standard directory and use the FIXED enumeration, which will leave the image dimension exactly as is.
	FIXED( 0, "fixed" ), // <lg/> Honestly, it's too hard to fight for this, but we need it, so we'll just maintain it.

	// <lg/> These sizes are based on the standard free desktop icon specification.
	// http://tango.freedesktop.org/Tango_Icon_Theme_Guidelines
	EXTRA_SMALL( 16, "16x16" ),
	SMALL( 22, "22x22" ),
	MEDIUM( 32, "32x32" ),
	LARGE( 48, "48x48" ),
	EXTRA_LARGE( 256, "256x256" ),

	// Wide variants. Note: this is Dennis' sizing.
	ALICE_WIDE_TINY( 24, 18, "24x18" ),
	ALICE_WIDE_SMALLER( 32, 24, "32x24" ),
	ALICE_WIDE_SMALL( 40, 30, "40x30" ),
	ALICE_WIDE_LARGE( 120, 90, "120x90" );

	private final java.awt.Dimension size;
	private final String name;

	private IconSize( java.awt.Dimension size, String name ) {
		this.size = size;
		this.name = name;
	}

	private IconSize( int size, String name ) {
		this( new java.awt.Dimension( size, size ), name );
	}

	private IconSize( int width, int height, String name ) {
		this( new java.awt.Dimension( width, height ), name );
	}

	public java.awt.Dimension getSize() {
		return this.size;
	}

	@Override
	public String toString() {
		return name;
	}
}
