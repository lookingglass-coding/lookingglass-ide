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
package edu.wustl.lookingglass.lgpedia.components;

import org.lgna.croquet.Operation;
import org.lgna.croquet.views.Button;

/**
 * @author Michael Pogran
 */
public class LgpediaButton extends Button {

	public LgpediaButton( Operation model ) {
		super( model );
	}

	@Override
	protected javax.swing.JButton createAwtComponent() {
		javax.swing.JButton rv = new javax.swing.JButton() {

			@Override
			protected void paintComponent( java.awt.Graphics g ) {
				int w = 18;
				int h = 18;
				int x = getInsets().left;
				int y = getInsets().top;

				java.awt.Color baseColor;
				if( getParent().getBackground() != null ) {
					baseColor = getParent().getBackground();
				} else {
					baseColor = new java.awt.Color( 152, 160, 217 );
				}

				java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;
				g2.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

				java.awt.Color fillOne = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( baseColor, 1.0, 1.15, 1.20 );//new java.awt.Color( 179, 188, 255 );
				java.awt.Color fillTwo = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( baseColor, 1.0, 1.15, 0.85 );//new java.awt.Color( 99, 108, 166 );

				java.awt.Paint fillPaint = new java.awt.GradientPaint( x, y, fillOne, x, h, fillTwo );

				java.awt.Color lineColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( baseColor, 1.0, 1.15, 0.65 );//new java.awt.Color( 103, 112, 170 );
				java.awt.Color textColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( baseColor, 1.0, 1.15, 0.35 );//new java.awt.Color( 69, 75, 115 );
				java.awt.Color highlightColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( baseColor, 1.0, 0.75, 1.50 );//new java.awt.Color( 179, 188, 255 );
				if( getModel().isRollover() ) {
					fillPaint = new java.awt.GradientPaint( x, y, fillOne.brighter(), x, h, fillTwo.brighter() );
				}
				if( getModel().isPressed() ) {
					fillPaint = new java.awt.GradientPaint( x, y, fillOne.darker(), x, h, fillTwo.darker() );
					lineColor = lineColor.darker();
					textColor = textColor.darker();
				}
				g2.setPaint( highlightColor );
				g2.fillOval( x + 1, y + 2, w, h );

				g2.setPaint( fillPaint );
				g2.fillOval( x, y, w, h );

				g2.setPaint( lineColor );
				g2.setStroke( new java.awt.BasicStroke( 1.5f ) );
				g2.drawOval( x, y, w, h );

				g2.setPaint( textColor );
				g2.setFont( new java.awt.Font( "SansSerif", java.awt.Font.BOLD, 14 ) );
				edu.cmu.cs.dennisc.java.awt.GraphicsUtilities.drawCenteredText( g2, "?", new java.awt.Rectangle( x, y, 20, 20 ) );
			}
		};
		rv.setCursor( new java.awt.Cursor( java.awt.Cursor.HAND_CURSOR ) );
		rv.setPreferredSize( new java.awt.Dimension( 20, 20 ) );
		return rv;
	}
}
