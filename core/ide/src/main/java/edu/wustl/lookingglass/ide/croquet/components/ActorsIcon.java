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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
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
public class ActorsIcon implements Icon {

	private float origAlpha = 1.0f;

	/**
	 * Paints the transcoded SVG image on the specified graphics context. You
	 * can install a custom transformation on the graphics context to scale the
	 * image.
	 *
	 * @param g Graphics context.
	 */
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

	private void paintShapeNode_0_0_0_0_0_0( Graphics2D g ) {
		GeneralPath shape0 = new GeneralPath();
		shape0.moveTo( 267.15186, 837.5324 );
		shape0.curveTo( 267.15186, 914.75305, 185.90941, 977.3527, 154.27048, 977.3527 );
		shape0.curveTo( 122.63154, 977.3527, 41.389084, 914.75305, 41.389084, 837.5324 );
		shape0.curveTo( 41.389084, 794.02313, 35.158134, 755.1555, 60.31816, 729.51166 );
		shape0.curveTo( 79.812355, 709.6426, 127.054214, 729.29504, 154.27048, 729.29504 );
		shape0.curveTo( 180.9292, 729.29504, 225.29663, 709.1588, 244.60977, 728.30066 );
		shape0.curveTo( 270.4613, 753.9229, 267.15186, 793.3326, 267.15186, 837.5325 );
		shape0.closePath();
		g.setPaint( new Color( 0, 0, 0, 255 ) );
		g.fill( shape0 );
	}

	private void paintShapeNode_0_0_0_0_0_1( Graphics2D g ) {
		GeneralPath shape1 = new GeneralPath();
		shape1.moveTo( 231.60825, 154.24522 );
		shape1.curveTo( 231.60825, 170.05275, 222.32877, 182.86728, 210.88193, 182.86728 );
		shape1.curveTo( 199.4351, 182.86728, 190.15562, 170.05275, 190.15562, 154.24522 );
		shape1.curveTo( 190.15562, 138.43771, 199.4351, 125.623184, 210.88193, 125.623184 );
		shape1.curveTo( 222.32875, 125.623184, 231.60825, 138.43771, 231.60825, 154.24522 );
		shape1.closePath();
		g.setPaint( new Color( 255, 255, 255, 255 ) );
		g.fill( shape1 );
	}

	private void paintShapeNode_0_0_0_0_0_2( Graphics2D g ) {
		GeneralPath shape2 = new GeneralPath();
		shape2.moveTo( 231.60825, 154.24522 );
		shape2.curveTo( 231.60825, 170.05275, 222.32877, 182.86728, 210.88193, 182.86728 );
		shape2.curveTo( 199.4351, 182.86728, 190.15562, 170.05275, 190.15562, 154.24522 );
		shape2.curveTo( 190.15562, 138.43771, 199.4351, 125.623184, 210.88193, 125.623184 );
		shape2.curveTo( 222.32875, 125.623184, 231.60825, 138.43771, 231.60825, 154.24522 );
		shape2.closePath();
		g.fill( shape2 );
	}

	private void paintShapeNode_0_0_0_0_0_3( Graphics2D g ) {
		GeneralPath shape3 = new GeneralPath();
		shape3.moveTo( 152.6509, 880.36926 );
		shape3.curveTo( 185.82219, 880.36926, 212.52689, 904.91785, 212.52689, 910.56793 );
		shape3.curveTo( 212.52689, 916.218, 185.82219, 900.7666, 152.65088, 900.7666 );
		shape3.curveTo( 119.47958, 900.7666, 92.77489, 916.218, 92.77489, 910.56793 );
		shape3.curveTo( 92.77489, 904.9179, 119.47958, 880.36926, 152.65088, 880.36926 );
		shape3.closePath();
		g.fill( shape3 );
	}

	private void paintCompositeGraphicsNode_0_0_0_0_0( Graphics2D g ) {
		// _0_0_0_0_0_0
		AffineTransform trans_0_0_0_0_0_0 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f ) );
		paintShapeNode_0_0_0_0_0_0( g );
		g.setTransform( trans_0_0_0_0_0_0 );
		// _0_0_0_0_0_1
		AffineTransform trans_0_0_0_0_0_1 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 652.3621826171875f ) );
		paintShapeNode_0_0_0_0_0_1( g );
		g.setTransform( trans_0_0_0_0_0_1 );
		// _0_0_0_0_0_2
		AffineTransform trans_0_0_0_0_0_2 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, -102.97354888916016f, 651.375244140625f ) );
		paintShapeNode_0_0_0_0_0_2( g );
		g.setTransform( trans_0_0_0_0_0_2 );
		// _0_0_0_0_0_3
		AffineTransform trans_0_0_0_0_0_3 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f ) );
		paintShapeNode_0_0_0_0_0_3( g );
		g.setTransform( trans_0_0_0_0_0_3 );
	}

	private void paintShapeNode_0_0_0_0_1_0_0( Graphics2D g ) {
		GeneralPath shape4 = new GeneralPath();
		shape4.moveTo( 267.15186, 837.5324 );
		shape4.curveTo( 267.15186, 914.75305, 185.90941, 977.3527, 154.27048, 977.3527 );
		shape4.curveTo( 122.63154, 977.3527, 41.389084, 914.75305, 41.389084, 837.5324 );
		shape4.curveTo( 41.389084, 794.02313, 35.158134, 755.1555, 60.31816, 729.51166 );
		shape4.curveTo( 79.812355, 709.6426, 127.054214, 729.29504, 154.27048, 729.29504 );
		shape4.curveTo( 180.9292, 729.29504, 225.29663, 709.1588, 244.60977, 728.30066 );
		shape4.curveTo( 270.4613, 753.9229, 267.15186, 793.3326, 267.15186, 837.5325 );
		shape4.closePath();
		g.fill( shape4 );
		g.setPaint( new Color( 0, 0, 0, 255 ) );
		g.setStroke( new BasicStroke( 9.065414f, 0, 0, 4.0f, null, 0.0f ) );
		g.draw( shape4 );
	}

	private void paintShapeNode_0_0_0_0_1_0_1( Graphics2D g ) {
		GeneralPath shape5 = new GeneralPath();
		shape5.moveTo( 231.60825, 154.24522 );
		shape5.curveTo( 231.60825, 170.05275, 222.32877, 182.86728, 210.88193, 182.86728 );
		shape5.curveTo( 199.4351, 182.86728, 190.15562, 170.05275, 190.15562, 154.24522 );
		shape5.curveTo( 190.15562, 138.43771, 199.4351, 125.623184, 210.88193, 125.623184 );
		shape5.curveTo( 222.32875, 125.623184, 231.60825, 138.43771, 231.60825, 154.24522 );
		shape5.closePath();
		g.fill( shape5 );
	}

	private void paintShapeNode_0_0_0_0_1_0_2( Graphics2D g ) {
		GeneralPath shape6 = new GeneralPath();
		shape6.moveTo( 231.60825, 154.24522 );
		shape6.curveTo( 231.60825, 170.05275, 222.32877, 182.86728, 210.88193, 182.86728 );
		shape6.curveTo( 199.4351, 182.86728, 190.15562, 170.05275, 190.15562, 154.24522 );
		shape6.curveTo( 190.15562, 138.43771, 199.4351, 125.623184, 210.88193, 125.623184 );
		shape6.curveTo( 222.32875, 125.623184, 231.60825, 138.43771, 231.60825, 154.24522 );
		shape6.closePath();
		g.fill( shape6 );
	}

	private void paintShapeNode_0_0_0_0_1_0_3( Graphics2D g ) {
		GeneralPath shape7 = new GeneralPath();
		shape7.moveTo( 152.6509, 911.8104 );
		shape7.curveTo( 119.4796, 911.8104, 92.77489, 887.26184, 92.77489, 881.61176 );
		shape7.curveTo( 92.77489, 875.9617, 119.47959, 891.4131, 152.6509, 891.4131 );
		shape7.curveTo( 185.82219, 891.4131, 212.52689, 875.9617, 212.52689, 881.61176 );
		shape7.curveTo( 212.52689, 887.2618, 185.82219, 911.8104, 152.6509, 911.8104 );
		shape7.closePath();
		g.fill( shape7 );
	}

	private void paintCompositeGraphicsNode_0_0_0_0_1_0( Graphics2D g ) {
		// _0_0_0_0_1_0_0
		AffineTransform trans_0_0_0_0_1_0_0 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f ) );
		paintShapeNode_0_0_0_0_1_0_0( g );
		g.setTransform( trans_0_0_0_0_1_0_0 );
		// _0_0_0_0_1_0_1
		AffineTransform trans_0_0_0_0_1_0_1 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 652.3621826171875f ) );
		paintShapeNode_0_0_0_0_1_0_1( g );
		g.setTransform( trans_0_0_0_0_1_0_1 );
		// _0_0_0_0_1_0_2
		AffineTransform trans_0_0_0_0_1_0_2 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, -102.97354888916016f, 651.375244140625f ) );
		paintShapeNode_0_0_0_0_1_0_2( g );
		g.setTransform( trans_0_0_0_0_1_0_2 );
		// _0_0_0_0_1_0_3
		AffineTransform trans_0_0_0_0_1_0_3 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f ) );
		paintShapeNode_0_0_0_0_1_0_3( g );
		g.setTransform( trans_0_0_0_0_1_0_3 );
	}

	private void paintCompositeGraphicsNode_0_0_0_0_1( Graphics2D g ) {
		// _0_0_0_0_1_0
		AffineTransform trans_0_0_0_0_1_0 = g.getTransform();
		g.transform( new AffineTransform( 0.9235534071922302f, 0.0f, 0.0f, 0.9235534071922302f, 20.771326065063477f, 75.05326080322266f ) );
		paintCompositeGraphicsNode_0_0_0_0_1_0( g );
		g.setTransform( trans_0_0_0_0_1_0 );
	}

	private void paintCompositeGraphicsNode_0_0_0_0( Graphics2D g ) {
		// _0_0_0_0_0
		AffineTransform trans_0_0_0_0_0 = g.getTransform();
		g.transform( new AffineTransform( 0.8109281063079834f, -0.2951536774635315f, 0.2951536774635315f, 0.8109281063079834f, -255.35548400878906f, 269.57122802734375f ) );
		paintCompositeGraphicsNode_0_0_0_0_0( g );
		g.setTransform( trans_0_0_0_0_0 );
		// _0_0_0_0_1
		AffineTransform trans_0_0_0_0_1 = g.getTransform();
		g.transform( new AffineTransform( 0.8739707469940186f, 0.31809934973716736f, -0.31809934973716736f, 0.8739707469940186f, 406.47216796875f, 52.85407257080078f ) );
		paintCompositeGraphicsNode_0_0_0_0_1( g );
		g.setTransform( trans_0_0_0_0_1 );
	}

	private void paintCompositeGraphicsNode_0_0_0( Graphics2D g ) {
		// _0_0_0_0
		AffineTransform trans_0_0_0_0 = g.getTransform();
		g.transform( new AffineTransform( 1.0273754596710205f, 0.0f, 0.0f, 1.0273754596710205f, -4.353461265563965f, -43.49837875366211f ) );
		paintCompositeGraphicsNode_0_0_0_0( g );
		g.setTransform( trans_0_0_0_0 );
	}

	private void paintCanvasGraphicsNode_0_0( Graphics2D g ) {
		// _0_0_0
		AffineTransform trans_0_0_0 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, -652.3621826171875f ) );
		paintCompositeGraphicsNode_0_0_0( g );
		g.setTransform( trans_0_0_0 );
	}

	private void paintRootGraphicsNode_0( Graphics2D g ) {
		// _0_0
		g.setComposite( AlphaComposite.getInstance( 3, 1.0f * origAlpha ) );
		AffineTransform trans_0_0 = g.getTransform();
		g.transform( new AffineTransform( 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f ) );
		paintCanvasGraphicsNode_0_0( g );
		g.setTransform( trans_0_0 );
	}

	/**
	 * Returns the X of the bounding box of the original SVG image.
	 *
	 * @return The X of the bounding box of the original SVG image.
	 */
	public int getOrigX() {
		return 0;
	}

	/**
	 * Returns the Y of the bounding box of the original SVG image.
	 *
	 * @return The Y of the bounding box of the original SVG image.
	 */
	public int getOrigY() {
		return 32;
	}

	/**
	 * Returns the width of the bounding box of the original SVG image.
	 *
	 * @return The width of the bounding box of the original SVG image.
	 */
	public int getOrigWidth() {
		return 400;
	}

	/**
	 * Returns the height of the bounding box of the original SVG image.
	 *
	 * @return The height of the bounding box of the original SVG image.
	 */
	public int getOrigHeight() {
		return 400;
	}

	/**
	 * The current width of this resizable icon.
	 */
	int width;

	/**
	 * The current height of this resizable icon.
	 */
	int height;

	/**
	 * Creates a new transcoded SVG image.
	 */
	public ActorsIcon() {
		this.width = getOrigWidth();
		this.height = getOrigHeight();
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
}
