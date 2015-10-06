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
package org.lgna.croquet.icon;

/**
 * @author Michael Pogran
 */

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import javax.swing.Icon;

/**
 * This class has been automatically generated using svg2java
 *
 */
public class ExternalHyperlinkIcon implements Icon {

	private float origAlpha = 1.0f;

	public void paint( Graphics2D g ) {
		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		origAlpha = 1.0f;
		Composite origComposite = g.getComposite();
		if( origComposite instanceof AlphaComposite ) {
			AlphaComposite origAlphaComposite =
					(AlphaComposite)origComposite;
			if( origAlphaComposite.getRule() == AlphaComposite.SRC_OVER ) {
				origAlpha = origAlphaComposite.getAlpha();
			}
		}

		// _0
		AffineTransform trans_0 = g.getTransform();
		paintRootGraphicsNode_0( g );
		g.setTransform( trans_0 );

	}

	private void paintShapeNode_0_0_0( Graphics2D g ) {
		GeneralPath shape0 = new GeneralPath();
		shape0.moveTo( 127.609, 102.818 );
		shape0.lineTo( 24.784, 102.818 );
		shape0.lineTo( 24.784, 102.818 );
		shape0.lineTo( 24.784, -0.001 );
		shape0.lineTo( 127.60899, -0.001 );
		shape0.moveTo( 37.374, 90.227 );
		shape0.lineTo( 115.018, 90.227 );
		shape0.lineTo( 115.018, 12.589 );
		shape0.lineTo( 37.374, 12.589 );
		shape0.lineTo( 37.374, 90.227 );
		shape0.lineTo( 37.374, 90.227 );
		shape0.closePath();
		g.setPaint( fillColor );
		g.fill( shape0 );
	}

	private void paintShapeNode_0_0_1( Graphics2D g ) {
		GeneralPath shape1 = new GeneralPath();
		shape1.moveTo( 0.389, 127.999 );
		shape1.lineTo( 0.389, 25.18 );
		shape1.lineTo( 31.866, 25.182 );
		shape1.lineTo( 31.866, 37.771 );
		shape1.lineTo( 12.98, 37.771 );
		shape1.lineTo( 12.98, 115.407 );
		shape1.lineTo( 90.625, 115.407 );
		shape1.lineTo( 90.625, 96.53 );
		shape1.lineTo( 103.215, 96.53 );
		shape1.lineTo( 103.215, 127.999 );
		shape1.lineTo( 0.389, 127.999 );
		shape1.closePath();
		g.fill( shape1 );
	}

	private void paintShapeNode_0_0_2( Graphics2D g ) {
		GeneralPath shape2 = new GeneralPath();
		shape2.moveTo( 51.282, 66.89 );
		shape2.lineTo( 92.201, 25.971 );
		shape2.lineTo( 101.104, 34.873 );
		shape2.lineTo( 60.185, 75.793 );
		shape2.lineTo( 51.282, 66.89 );
		shape2.closePath();
		g.fill( shape2 );
	}

	private void paintShapeNode_0_0_3( Graphics2D g ) {
		GeneralPath shape3 = new GeneralPath();
		shape3.moveTo( 90.358, 74.38 );
		shape3.lineTo( 90.356, 36.718 );
		shape3.lineTo( 52.854, 36.718 );
		shape3.lineTo( 52.854, 24.127 );
		shape3.lineTo( 102.949, 24.127 );
		shape3.lineTo( 102.949, 74.38 );
		shape3.lineTo( 90.358, 74.38 );
		shape3.closePath();
		g.fill( shape3 );
	}

	private void paintCanvasGraphicsNode_0_0( Graphics2D g ) {
		// _0_0_0
		AffineTransform trans_0_0_0 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f ) );
		paintShapeNode_0_0_0( g );
		g.setTransform( trans_0_0_0 );
		// _0_0_1
		AffineTransform trans_0_0_1 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f ) );
		paintShapeNode_0_0_1( g );
		g.setTransform( trans_0_0_1 );
		// _0_0_2
		AffineTransform trans_0_0_2 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f ) );
		paintShapeNode_0_0_2( g );
		g.setTransform( trans_0_0_2 );
		// _0_0_3
		AffineTransform trans_0_0_3 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f ) );
		paintShapeNode_0_0_3( g );
		g.setTransform( trans_0_0_3 );
	}

	private void paintRootGraphicsNode_0( Graphics2D g ) {
		// _0_0
		g.setComposite( AlphaComposite.getInstance( 3, 1.0f * origAlpha ) );
		AffineTransform trans_0_0 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, -0.0f, -0.0f ) );
		paintCanvasGraphicsNode_0_0( g );
		g.setTransform( trans_0_0 );
	}

	/**
	 * Returns the X of the bounding box of the original SVG image.
	 *
	 * @return The X of the bounding box of the original SVG image.
	 */
	public int getOrigX() {
		return 1;
	}

	/**
	 * Returns the Y of the bounding box of the original SVG image.
	 *
	 * @return The Y of the bounding box of the original SVG image.
	 */
	public int getOrigY() {
		return 0;
	}

	/**
	 * Returns the width of the bounding box of the original SVG image.
	 *
	 * @return The width of the bounding box of the original SVG image.
	 */
	public int getOrigWidth() {
		return 128;
	}

	/**
	 * Returns the height of the bounding box of the original SVG image.
	 *
	 * @return The height of the bounding box of the original SVG image.
	 */
	public int getOrigHeight() {
		return 128;
	}

	/**
	 * The current width of this resizable icon.
	 */
	int width;

	/**
	 * The current height of this resizable icon.
	 */
	int height;
	private Color fillColor = new Color( 0, 0, 192, 255 );

	public ExternalHyperlinkIcon() {
		this.width = getOrigWidth();
		this.height = getOrigHeight();
	}

	public ExternalHyperlinkIcon( int size ) {
		this.width = size;
		this.height = size;
	}

	public ExternalHyperlinkIcon( int size, Color color ) {
		this.width = size;
		this.height = size;
		this.fillColor = color;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.Icon#getIconHeight()
	 */
	@Override
	public int getIconHeight() {
		return height;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.Icon#getIconWidth()
	 */
	@Override
	public int getIconWidth() {
		return width;
	}

	/*
	 * Set the dimension of the icon.
	 */

	public void setDimension( Dimension newDimension ) {
		this.width = newDimension.width;
		this.height = newDimension.height;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	@Override
	public void paintIcon( Component c, Graphics g, int x, int y ) {
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.translate( x, y );

		double coef1 = (double)this.width / (double)getOrigWidth();
		double coef2 = (double)this.height / (double)getOrigHeight();
		double coef = Math.min( coef1, coef2 );
		g2d.scale( coef, coef );
		paint( g2d );
		g2d.dispose();
	}

	//
	//
	//
	//	private float origAlpha = 1.0f;
	//
	//	/**
	//	 * Paints the transcoded SVG image on the specified graphics context. You
	//	 * can install a custom transformation on the graphics context to scale the
	//	 * image.
	//	 *
	//	 * @param g
	//	 *            Graphics context.
	//	 */
	//	public void paint( Graphics2D g ) {
	//		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
	//		origAlpha = 1.0f;
	//		Composite origComposite = g.getComposite();
	//		if( origComposite instanceof AlphaComposite ) {
	//			AlphaComposite origAlphaComposite =
	//					(AlphaComposite)origComposite;
	//			if( origAlphaComposite.getRule() == AlphaComposite.SRC_OVER ) {
	//				origAlpha = origAlphaComposite.getAlpha();
	//			}
	//		}
	//
	//		// _0
	//		AffineTransform trans_0 = g.getTransform();
	//		paintRootGraphicsNode_0( g );
	//		g.setTransform( trans_0 );
	//
	//	}
	//
	//	private void paintShapeNode_0_0_0( Graphics2D g ) {
	//		GeneralPath shape0 = new GeneralPath();
	//		shape0.moveTo( 10.845, 0.523 );
	//		shape0.curveTo( 11.007, 0.234, 10.871, 0.0, 10.538, 0.0 );
	//		shape0.lineTo( 4.228, 0.0 );
	//		shape0.curveTo( 1.891, 0.0, 0.0, 2.236, 0.0, 4.998 );
	//		shape0.lineTo( 0.0, 13.004 );
	//		shape0.curveTo( 0.0, 15.765, 1.891, 18.0, 4.229, 18.0 );
	//		shape0.lineTo( 13.36, 18.0 );
	//		shape0.curveTo( 15.698, 18.0, 17.592, 15.765, 17.592, 13.004 );
	//		shape0.curveTo( 17.592, 13.004, 17.592, 9.047999, 17.592, 8.982 );
	//		shape0.curveTo( 17.592, 8.09, 14.603999, 10.276, 14.603999, 11.567 );
	//		shape0.curveTo( 14.603999, 11.742001, 14.603999, 12.891001, 14.603999, 12.981001 );
	//		shape0.curveTo( 14.603999, 13.200001, 14.614999, 13.280001, 14.499, 13.590001 );
	//		shape0.curveTo( 14.304999, 14.106001, 13.8689995, 14.4660015, 13.36, 14.469001 );
	//		shape0.lineTo( 4.229, 14.469001 );
	//		shape0.curveTo( 3.54, 14.465001, 2.986, 13.809001, 2.986, 13.003001 );
	//		shape0.lineTo( 2.986, 4.998 );
	//		shape0.curveTo( 2.986, 4.189, 3.54, 3.5300002, 4.229, 3.5300002 );
	//		shape0.curveTo( 4.229, 3.5300002, 6.984, 3.5300002, 8.127, 3.5300002 );
	//		shape0.curveTo( 9.271999, 3.5300002, 9.884, 2.2340002, 9.884, 2.2340002 );
	//		shape0.lineTo( 10.845, 0.523 );
	//		shape0.closePath();
	//		g.setPaint( this.fillColor );
	//		g.fill( shape0 );
	//	}
	//
	//	private void paintShapeNode_0_0_1_0( Graphics2D g ) {
	//		GeneralPath shape1 = new GeneralPath();
	//		shape1.moveTo( 8.076, 13.474 );
	//		shape1.curveTo( 8.076, 13.474, 8.08, 13.474, 8.076, 13.474 );
	//		shape1.closePath();
	//		shape1.moveTo( 5.968, 12.47 );
	//		shape1.curveTo( 6.151, 11.592, 6.608, 9.856, 7.652, 8.052 );
	//		shape1.curveTo( 9.028, 5.6590004, 11.576, 3.1729999, 15.653, 2.8109999 );
	//		shape1.curveTo( 15.984, 2.7819998, 16.26, 3.0419998, 16.269, 3.373 );
	//		shape1.lineTo( 16.310999, 4.823 );
	//		shape1.curveTo( 16.321, 5.1549997, 16.057999, 5.433, 15.728999, 5.467 );
	//		shape1.curveTo( 10.689999, 5.99, 9.012999, 10.109, 8.343999, 12.301001 );
	//		shape1.curveTo( 8.278999, 12.525001, 8.228999, 12.72, 8.189999, 12.885 );
	//		shape1.curveTo( 8.113998, 13.208, 7.8109984, 13.423, 7.483999, 13.363 );
	//		shape1.lineTo( 6.452, 13.17 );
	//		shape1.curveTo( 6.125, 13.107, 5.901, 12.793, 5.968, 12.47 );
	//		shape1.closePath();
	//		g.fill( shape1 );
	//	}
	//
	//	private void paintShapeNode_0_0_1_1( Graphics2D g ) {
	//		GeneralPath shape2 = new GeneralPath();
	//		shape2.moveTo( 14.871, 7.973 );
	//		shape2.lineTo( 14.626, 7.788 );
	//		shape2.curveTo( 14.360001, 7.589, 14.273001, 7.19, 14.431001, 6.8980002 );
	//		shape2.lineTo( 15.669001, 4.5950003 );
	//		shape2.curveTo( 15.826, 4.3030005, 15.738001, 3.9060001, 15.474001, 3.7050004 );
	//		shape2.lineTo( 13.59, 2.29 );
	//		shape2.curveTo( 13.325, 2.09, 13.237, 1.692, 13.395, 1.4 );
	//		shape2.lineTo( 13.59, 1.036 );
	//		shape2.curveTo( 13.747, 0.744, 14.089, 0.668, 14.354, 0.867 );
	//		shape2.lineTo( 17.445, 3.1859999 );
	//		shape2.curveTo( 17.711, 3.3849998, 17.799, 3.784, 17.64, 4.075 );
	//		shape2.lineTo( 15.636, 7.8059998 );
	//		shape2.curveTo( 15.478, 8.098, 15.137, 8.173, 14.871, 7.973 );
	//		shape2.closePath();
	//		g.fill( shape2 );
	//	}
	//
	//	private void paintCompositeGraphicsNode_0_0_1( Graphics2D g ) {
	//		// _0_0_1_0
	//		AffineTransform trans_0_0_1_0 = g.getTransform();
	//		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f ) );
	//		paintShapeNode_0_0_1_0( g );
	//		g.setTransform( trans_0_0_1_0 );
	//		// _0_0_1_1
	//		AffineTransform trans_0_0_1_1 = g.getTransform();
	//		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f ) );
	//		paintShapeNode_0_0_1_1( g );
	//		g.setTransform( trans_0_0_1_1 );
	//	}
	//
	//	private void paintCanvasGraphicsNode_0_0( Graphics2D g ) {
	//		// _0_0_0
	//		AffineTransform trans_0_0_0 = g.getTransform();
	//		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f ) );
	//		paintShapeNode_0_0_0( g );
	//		g.setTransform( trans_0_0_0 );
	//		// _0_0_1
	//		AffineTransform trans_0_0_1 = g.getTransform();
	//		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f ) );
	//		paintCompositeGraphicsNode_0_0_1( g );
	//		g.setTransform( trans_0_0_1 );
	//	}
	//
	//	private void paintRootGraphicsNode_0( Graphics2D g ) {
	//		// _0_0
	//		g.setComposite( AlphaComposite.getInstance( 3, 1.0f * origAlpha ) );
	//		AffineTransform trans_0_0 = g.getTransform();
	//		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.14050006866455078f, -0.0f ) );
	//		paintCanvasGraphicsNode_0_0( g );
	//		g.setTransform( trans_0_0 );
	//	}
	//
	//	/**
	//	 * Returns the X of the bounding box of the original SVG image.
	//	 *
	//	 * @return The X of the bounding box of the original SVG image.
	//	 */
	//	public int getOrigX() {
	//		return 1;
	//	}
	//
	//	/**
	//	 * Returns the Y of the bounding box of the original SVG image.
	//	 *
	//	 * @return The Y of the bounding box of the original SVG image.
	//	 */
	//	public int getOrigY() {
	//		return 0;
	//	}
	//
	//	/**
	//	 * Returns the width of the bounding box of the original SVG image.
	//	 *
	//	 * @return The width of the bounding box of the original SVG image.
	//	 */
	//	public int getOrigWidth() {
	//		return 18;
	//	}
	//
	//	/**
	//	 * Returns the height of the bounding box of the original SVG image.
	//	 *
	//	 * @return The height of the bounding box of the original SVG image.
	//	 */
	//	public int getOrigHeight() {
	//		return 18;
	//	}
	//
	//	/**
	//	 * The current width of this resizable icon.
	//	 */
	//	int width;
	//
	//	/**
	//	 * The current height of this resizable icon.
	//	 */
	//	int height;
	//
	//	private Color fillColor = new Color( 0, 0, 192, 255 );
	//
	//	/**
	//	 * Creates a new transcoded SVG image.
	//	 */
	//	public ExternalHyperlinkIcon() {
	//		this.width = getOrigWidth();
	//		this.height = getOrigHeight();
	//	}
	//
	//	public ExternalHyperlinkIcon( int size ) {
	//		this.width = size;
	//		this.height = size;
	//	}
	//
	//	public ExternalHyperlinkIcon( int size, Color color ) {
	//		this.width = size;
	//		this.height = size;
	//		this.fillColor = color;
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.Icon#getIconHeight()
	//	 */
	//	@Override
	//	public int getIconHeight() {
	//		return height;
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.Icon#getIconWidth()
	//	 */
	//	@Override
	//	public int getIconWidth() {
	//		return width;
	//	}
	//
	//	/*
	//	 * Set the dimension of the icon.
	//	 */
	//
	//	public void setDimension( Dimension newDimension ) {
	//		this.width = newDimension.width;
	//		this.height = newDimension.height;
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	//	 */
	//	@Override
	//	public void paintIcon( Component c, Graphics g, int x, int y ) {
	//		Graphics2D g2d = (Graphics2D)g.create();
	//		g2d.translate( x, y );
	//
	//		double coef1 = (double)this.width / (double)getOrigWidth();
	//		double coef2 = (double)this.height / (double)getOrigHeight();
	//		double coef = Math.min( coef1, coef2 );
	//		g2d.scale( coef, coef );
	//		paint( g2d );
	//		g2d.dispose();
	//	}
}
