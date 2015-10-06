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
import java.awt.Component;
import java.awt.Graphics2D;

import javax.swing.ButtonModel;

public class StatementExecutionStatusIcon extends org.lgna.croquet.icon.AbstractIcon {

	public StatementExecutionStatusIcon( java.awt.Dimension size ) {
		super( size );
	}

	public Color brighten( Color color, double factor ) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int alpha = color.getAlpha();

		int i = (int)( 1.0 / ( 1.0 - factor ) );
		if( ( r == 0 ) && ( g == 0 ) && ( b == 0 ) ) {
			return new Color( i, i, i, alpha );
		}
		if( ( r > 0 ) && ( r < i ) ) {
			r = i;
		}
		if( ( g > 0 ) && ( g < i ) ) {
			g = i;
		}
		if( ( b > 0 ) && ( b < i ) ) {
			b = i;
		}

		return new Color( Math.min( (int)( r / factor ), 255 ),
				Math.min( (int)( g / factor ), 255 ),
				Math.min( (int)( b / factor ), 255 ),
				alpha );
	}

	@Override
	protected void paintIcon( Component c, Graphics2D g2 ) {
		int size = Math.min( this.getIconWidth(), this.getIconHeight() ) - 3;
		int w = size;
		int h = size;

		g2.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

		Color baseColor;
		Color lineColor;
		Color splitColor = null;
		boolean isCurrent;
		boolean isTheStartSelection;
		boolean isTheEndSelection;
		if( c instanceof StatementMenuButton.JStatementButton ) {
			StatementMenuButton.JStatementButton statementButton = (StatementMenuButton.JStatementButton)c;
			isTheStartSelection = statementButton.isTheStartSelection();
			isTheEndSelection = statementButton.isTheEndSelection();
			isCurrent = statementButton.getCurrentDrawState() == DrawState.CURRENT;
			baseColor = statementButton.getBaseColor();
			lineColor = statementButton.getCrosshairColor();
		} else {
			isTheStartSelection = false;
			isTheEndSelection = false;
			baseColor = DrawState.HAS_RUN.baseColor;
			lineColor = DrawState.HAS_RUN.crosshairColor;
			isCurrent = false;
		}

		if( !isCurrent ) {
			if( isTheStartSelection && isTheEndSelection ) {
				baseColor = new java.awt.Color( 95, 255, 72 );
				splitColor = new java.awt.Color( 230, 30, 23 );
			} else if( isTheStartSelection ) {
				baseColor = new java.awt.Color( 95, 255, 72 );
			} else if( isTheEndSelection ) {
				baseColor = new java.awt.Color( 230, 30, 23 );
			}
		}

		boolean isRollover;
		if( c instanceof javax.swing.AbstractButton ) {
			javax.swing.AbstractButton button = (javax.swing.AbstractButton)c;
			ButtonModel model = button.getModel();
			isRollover = model.isRollover() || model.isArmed();
		} else if( c instanceof java.awt.Label ) {
			java.awt.Label label = (java.awt.Label)c;
			isRollover = label.getMousePosition() != null;
		} else {
			isRollover = false;
		}

		Color outlineColor = baseColor.darker();

		if( isRollover ) {
			baseColor = this.brighten( baseColor, 0.85 );
			splitColor = splitColor == null ? null : this.brighten( splitColor, 0.85 );
		}

		if( splitColor == null ) {
			g2.setPaint( baseColor );
			g2.fillOval( 0, 0, w, h );
		} else {
			g2.setPaint( splitColor );
			g2.fillOval( 0, 0, w, h );
			g2.setPaint( baseColor );
			g2.fillArc( 0, 0, w, h, 0, 180 );
		}

		// Draw crosshair
		float a = 0.33f * size;
		float center = 0.5f * size;
		float b = size - a;

		g2.setPaint( lineColor );
		g2.setStroke( new java.awt.BasicStroke( 2.5f, java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_ROUND ) );
		java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
		path.moveTo( a, center );
		path.lineTo( b, center );
		path.moveTo( center, a );
		path.lineTo( center, b );
		g2.draw( path );

		java.awt.Paint paint = new java.awt.LinearGradientPaint( new java.awt.Point( 0, 0 ),
				new java.awt.Point( 0, h ),
				new float[] { 0.1f, 0.25f, 0.75f, 1.0f },
				new Color[] { new Color( 255, 255, 255, 200 ), new Color( 255, 255, 255, 0 ), new Color( 255, 255, 255, 0 ), new Color( 255, 255, 255, 100 ) } );

		g2.setPaint( paint );
		g2.fillOval( 0, 0, w, h );

		if( splitColor == null ) {
			g2.setPaint( outlineColor );
			g2.setStroke( new java.awt.BasicStroke( 1.5f ) );
			g2.drawOval( 0, 0, w, h );
		} else {
			g2.setStroke( new java.awt.BasicStroke( 1.5f ) );
			g2.setPaint( splitColor.darker() );
			g2.drawArc( 0, 0, w, h, 0, -180 );
			g2.setPaint( outlineColor );
			g2.drawArc( 0, 0, w, h, 0, 180 );
		}

		g2.dispose();
	}
}
