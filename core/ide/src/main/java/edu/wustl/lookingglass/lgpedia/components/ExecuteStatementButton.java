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

import java.awt.Component;
import java.awt.Graphics;

import org.lgna.croquet.Operation;
import org.lgna.croquet.views.Button;

/**
 * @author Michael Pogran
 */
public class ExecuteStatementButton extends Button {
	private java.awt.Color baseColor;

	public ExecuteStatementButton( Operation model ) {
		super( model );
		this.baseColor = new java.awt.Color( 60, 121, 147 );
	}

	public void setBaseColor( java.awt.Color baseColor ) {
		this.baseColor = baseColor;
	}

	@Override
	protected javax.swing.JButton createAwtComponent() {
		javax.swing.JButton rv = new javax.swing.JButton() {

			@Override
			public javax.swing.Icon getIcon() {
				return new TriangleIcon( baseColor );
			}

			@Override
			protected void paintComponent( java.awt.Graphics g ) {
				int w = 23;
				int h = 23;

				java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;
				g2.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

				java.awt.Color startColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( baseColor, 1.0, 0.90, 1.25 );
				java.awt.Color endColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( baseColor, 1.0, 1.15, 0.95 );
				java.awt.Color strokeColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( baseColor, 1.0, 1.15, 0.65 );
				if( isEnabled() ) {
					if( getModel().isRollover() ) {
						if( getModel().isPressed() ) {
							startColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( startColor, 1.0, 1.15, 0.80 );
							endColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( endColor, 1.0, 1.15, 0.80 );
						} else {
							startColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( startColor, 1.0, 0.90, 1.25 );
							endColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( endColor, 1.0, 0.90, 1.25 );
						}
					}
				} else {
					startColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( startColor, 1.0, 0.0, 1.0 );
					endColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( endColor, 1.0, 0.0, 1.0 );
					strokeColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( strokeColor, 1.0, 0.0, 1.0 );
				}

				java.awt.Paint fillGradient = new java.awt.GradientPaint( 0, 0, startColor, 0, h, endColor );

				g2.setPaint( fillGradient );
				g2.fillRoundRect( 0, 0, w, h, 8, 8 );

				g2.setPaint( strokeColor );
				g2.setStroke( new java.awt.BasicStroke( 1.15f ) );
				g2.drawRoundRect( 0, 0, w, h, 8, 8 );

				getIcon().paintIcon( this, g2, 8, 7 );
			}
		};
		return rv;
	}

	private class TriangleIcon implements javax.swing.Icon {
		private java.awt.Color buttonColor;

		public TriangleIcon( java.awt.Color buttonColor ) {
			this.buttonColor = buttonColor;
		}

		@Override
		public void paintIcon( Component c, Graphics g, int x, int y ) {
			int w = getIconWidth();
			int h = getIconHeight();

			java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;
			g2.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

			java.awt.Color triangleColor = java.awt.Color.WHITE;
			java.awt.Color shadowColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( this.buttonColor, 1.0, 1.15, 0.85 );
			if( c.isEnabled() ) {
				//pass
			} else {
				triangleColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( triangleColor, 1.0, 1.0, 0.85 );
				shadowColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( triangleColor, 1.0, 0.0, 0.85 );
			}

			g2.setPaint( shadowColor );
			edu.cmu.cs.dennisc.java.awt.GraphicsUtilities.fillTriangle( g2, edu.cmu.cs.dennisc.java.awt.GraphicsUtilities.Heading.EAST, x, y + 1, w - 2, h );

			g2.setPaint( triangleColor );
			edu.cmu.cs.dennisc.java.awt.GraphicsUtilities.fillTriangle( g2, edu.cmu.cs.dennisc.java.awt.GraphicsUtilities.Heading.EAST, x, y, w - 2, h );
		}

		@Override
		public int getIconWidth() {
			return 12;
		}

		@Override
		public int getIconHeight() {
			return 12;
		}

	}

}
