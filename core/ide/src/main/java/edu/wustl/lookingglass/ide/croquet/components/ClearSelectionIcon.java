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
package edu.wustl.lookingglass.ide.croquet.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * @author Michael Pogran
 */
public class ClearSelectionIcon extends javax.swing.ImageIcon {

	private final Dimension size;

	public ClearSelectionIcon( Dimension size ) {
		super();
		this.size = size;
		this.setImage( createImage() );
	}

	public java.awt.Image createImage() {
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage( this.size.width, this.size.height, java.awt.image.BufferedImage.TYPE_INT_ARGB );
		java.awt.Graphics2D g2 = (Graphics2D)img.getGraphics();

		int pad = 2;
		int size = Math.min( this.size.width, this.size.height );
		int h = size - ( pad * 2 );
		int w = size - ( pad * 2 );
		int x = pad;
		int y = pad;

		g2.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

		java.awt.Color paint = new Color( 200, 200, 200 );
		java.awt.Color lineColor = java.awt.Color.GRAY;

		g2.setPaint( paint );
		g2.fillOval( x, y, w, h );

		int a = (int)( 0.33f * size );
		float center = 0.5f * size;
		int b = size - a;
		g2.setPaint( lineColor );
		g2.setStroke( new java.awt.BasicStroke( 1.0f * ( size / 10 ), java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_ROUND ) );
		java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
		path.moveTo( a, center );
		path.lineTo( b, center );
		path.moveTo( center, a );
		path.lineTo( center, b );
		g2.draw( path );

		java.awt.Paint highlightPaint = new java.awt.LinearGradientPaint( new java.awt.Point( 0, 0 ),
				new java.awt.Point( 0, h ),
				new float[] { 0.1f, 0.25f, 0.75f, 1.0f },
				new java.awt.Color[] { new java.awt.Color( 255, 255, 255, 200 ), new java.awt.Color( 255, 255, 255, 0 ), new java.awt.Color( 255, 255, 255, 0 ), new java.awt.Color( 255, 255, 255, 100 ) } );

		g2.setPaint( highlightPaint );
		g2.fillOval( x, y, w, h );

		g2.setPaint( paint.darker() );
		g2.setStroke( new java.awt.BasicStroke( 0.5f * ( size / 10 ) ) );
		g2.drawOval( x, y, w, h );

		double slashHeight = this.size.height / 6;
		double slashWidth = ( Math.sqrt( Math.pow( this.size.height, 2 ) + Math.pow( this.size.height, 2 ) ) ) * 0.8;
		java.awt.Shape slashThrough = new java.awt.geom.Rectangle2D.Double( ( this.size.width - slashWidth ) / 2, ( this.size.height / 2 ) - ( slashHeight / 2 ), slashWidth, slashHeight );
		java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
		transform.rotate( Math.toRadians( 45.0 ), this.size.width / 2, this.size.height / 2 );

		g2.setPaint( new Color( 95, 95, 95 ) );
		g2.fill( transform.createTransformedShape( slashThrough ) );

		return img;
	}

}
