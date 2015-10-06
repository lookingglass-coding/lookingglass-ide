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

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import javax.swing.Icon;

/**
 * @author Michael Pogran
 */
public class CircleIcon implements Icon {

	private final Icon icon;
	private final Dimension dimension;
	private final java.awt.Color borderColor;

	private static Dimension getIconDimensions( Icon icon ) {
		int size = Math.max( icon.getIconWidth(), icon.getIconHeight() );
		return new Dimension( size, size );
	}

	public CircleIcon( Icon icon, java.awt.Color color ) {
		this( icon, getIconDimensions( icon ), color );
	}

	public CircleIcon( Icon icon, Dimension dimension, java.awt.Color color ) {
		this.icon = icon;
		this.dimension = dimension;
		this.borderColor = color;
	}

	@Override
	public int getIconHeight() {
		return dimension.height;
	}

	@Override
	public int getIconWidth() {
		return dimension.width;
	}

	@Override
	public void paintIcon( Component c, Graphics g, int x, int y ) {
		Graphics2D g2 = (Graphics2D)g.create();
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2.setPaint( java.awt.Color.WHITE );
		g2.fillOval( x - 3, y - 3, this.getIconWidth() + 6, this.getIconHeight() + 6 );

		Ellipse2D shape = new Ellipse2D.Float();
		shape.setFrame( x, y, this.getIconWidth(), this.getIconHeight() );

		Graphics2D g3 = (Graphics2D)g2.create();
		g3.clip( shape );

		if( this.icon == null ) {
			//pass
		} else {
			this.icon.paintIcon( c, g3, x, y );
		}
		g2.setStroke( new BasicStroke( 1.5f ) );
		g2.setPaint( this.borderColor );
		g2.drawOval( x - 3, y - 3, this.getIconWidth() + 6, this.getIconHeight() + 6 );
		g2.dispose();
	}
}
