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
package edu.wustl.lookingglass.ide;

public class LookingGlassTheme extends org.alice.ide.DefaultTheme {
	private static final java.awt.Color DEFAULT_NOUN_COLOR = new java.awt.Color( 0xFFF8C0 );
	private static final java.awt.Color DEFAULT_CONSTRUCTOR_COLOR = new java.awt.Color( 0xFFF0C8 );

	private static final java.awt.Color PROCEDURE_PANE_COLOR = new java.awt.Color( 0xA4ADE4 );
	private static final java.awt.Color FUNCTION_PANE_COLOR = new java.awt.Color( 0x94D19A );
	private static final java.awt.Color FIELD_PANE_COLOR = new java.awt.Color( 0x9ACADF );
	private static final java.awt.Color CONTROL_FLOW_PANE_COLOR = new java.awt.Color( 0xA8C9FB );

	private static final java.awt.Color FUNCTION_STATEMENT_COLOR = new java.awt.Color( 0xC4EBB7 );
	private static final java.awt.Color CONTROL_FLOW_STATEMENT_COLOR = new java.awt.Color( 0xE6E9FB );

	@Override
	public java.awt.Color getFunctionStatementColor() {
		return FUNCTION_STATEMENT_COLOR;
	}

	@Override
	public java.awt.Color getControlFlowStatementColor() {
		return CONTROL_FLOW_STATEMENT_COLOR;
	}

	@Override
	public java.awt.Color getDefaultNounColor() {
		return DEFAULT_NOUN_COLOR;
	}

	@Override
	public java.awt.Color getProcedureColor() {
		return PROCEDURE_PANE_COLOR;
	}

	@Override
	public java.awt.Color getFunctionColor() {
		return FUNCTION_PANE_COLOR;
	}

	@Override
	public java.awt.Color getFieldColor() {
		return FIELD_PANE_COLOR;
	}

	@Override
	public java.awt.Color getControlFlowColor() {
		return CONTROL_FLOW_PANE_COLOR;
	}

	@Override
	public java.awt.Color getMutedTypeColor() {
		return DEFAULT_CONSTRUCTOR_COLOR;
	}

	@Override
	public java.awt.Color getConstructorColor() {
		return DEFAULT_CONSTRUCTOR_COLOR;
	}

	static public javax.swing.Icon getIcon( String id, org.lgna.croquet.icon.IconSize size ) {
		java.net.URL url = getImageURL( id, size );
		if( url == null ) {
			return null;
		}
		return new javax.swing.ImageIcon( url );
	}

	static public java.awt.Image getImage( String id, org.lgna.croquet.icon.IconSize size ) {
		java.net.URL url = getImageURL( id, size );
		if( url == null ) {
			return null;
		}
		try {
			return edu.cmu.cs.dennisc.image.ImageUtilities.read( url );
		} catch( java.io.IOException e ) {
			throw new RuntimeException( e );
		}
	}

	static public javafx.scene.image.ImageView getFxImageView( String id, org.lgna.croquet.icon.IconSize size ) {
		javafx.scene.image.Image image = getFxImage( id, size );
		if( image == null ) {
			return null;
		}
		return new javafx.scene.image.ImageView( image );
	}

	static public javafx.scene.image.Image getFxImage( String id, org.lgna.croquet.icon.IconSize size ) {
		java.net.URL url = getImageURL( id, size );
		if( url == null ) {
			return null;
		}
		return new javafx.scene.image.Image( url.toString() );
	}

	static public java.net.URL getImageURL( String resource, org.lgna.croquet.icon.IconSize size ) {
		java.net.URL url = null;
		String ext = null;

		// Check if the file has a custom extension, if so try to load it.
		String name = new java.io.File( resource ).getName();
		int k = name.lastIndexOf( "." );
		if( k != -1 ) {
			ext = name.substring( k + 1, name.length() );
			url = getImageURL( size.toString() + java.io.File.separator + resource + "." + ext );
		}

		// Default to load png
		if( url == null ) {
			ext = "png";
			url = getImageURL( size.toString() + java.io.File.separator + resource + "." + ext );
		}
		return url;
	}

	static public java.net.URL getImageURL( String resource ) {
		try {
			java.net.URL url = new java.io.File( edu.cmu.cs.dennisc.app.ApplicationRoot.getApplicationDirectory() + "/images/" + resource ).toURL();
			if( url != null ) {
				if( !new java.io.File( url.getFile() ).exists() ) {
					url = null;
				}
			}
			return url;
		} catch( java.net.MalformedURLException e ) {
			return null;
		}
	}
}
