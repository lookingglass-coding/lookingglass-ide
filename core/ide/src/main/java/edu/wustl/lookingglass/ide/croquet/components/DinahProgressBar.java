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

import org.lgna.croquet.views.ProgressBar;

/**
 * @author Michael Pogran
 */
public class DinahProgressBar extends ProgressBar {

	public DinahProgressBar( javax.swing.BoundedRangeModel boundedRangeModel ) {
		super( boundedRangeModel );
	}

	@Override
	protected javax.swing.JProgressBar createAwtComponent() {
		return new javax.swing.JProgressBar( this.getBoundedRangeModel() ) {

			@Override
			public void paint( java.awt.Graphics g ) {
				int x = 6;
				int y = 0;
				int w = this.getWidth() - 12;
				int h = 14;
				java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;
				g2.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

				java.awt.Shape s = new java.awt.Rectangle( x, y, w, h );
				java.awt.Color baseColor = new java.awt.Color( 161, 161, 179 );
				java.awt.Paint paint = new java.awt.GradientPaint( 0, y, baseColor.darker(), 0, y + ( h / 2 ), baseColor );
				g2.setPaint( paint );
				g2.fill( s );

				int pX = (int)( this.getPercentComplete() * w );

				g2.setPaint( new java.awt.GradientPaint( 0, y, new Color( 255, 255, 255, 212 ), 0, y + ( h / 2 ), new java.awt.Color( 32, 175, 37, 128 ) ) );
				g2.fillRect( x, y, pX, h );

				g2.setPaint( baseColor.darker().darker() );
				g2.setStroke( new java.awt.BasicStroke( 1.0f ) );
				g2.draw( s );

				java.awt.Color sliderColor = new java.awt.Color( 32, 175, 37 );

				g2.setPaint( new java.awt.GradientPaint( 0, y, sliderColor.brighter(), 0, y + ( h / 2 ), sliderColor ) );
				g2.fillOval( pX, y - 2, h + 4, h + 4 );
				g2.setPaint( sliderColor.darker() );
				g2.drawOval( pX, y - 2, h + 4, h + 4 );
			}
		};
	}

}
