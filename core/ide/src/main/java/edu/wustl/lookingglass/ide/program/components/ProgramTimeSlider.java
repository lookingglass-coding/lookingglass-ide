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
package edu.wustl.lookingglass.ide.program.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;

import javax.swing.JSlider;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthSliderUI;

import edu.cmu.cs.dennisc.java.awt.ColorUtilities;
import edu.wustl.lookingglass.ide.program.models.ProgramStatementManager;

public class ProgramTimeSlider extends org.lgna.croquet.views.Slider {
	ProgramTimeSliderUI sliderUI;

	public ProgramTimeSlider( ProgramStatementManager state ) {
		super( state );

		setPaintLabels( false );
		setSnapToTicks( true );
		setPaintTicks( true );
		setMajorTickSpacing( 4 );
	}

	public void setStatusColor( Color statusColor ) {
		this.sliderUI.setStatusColor( statusColor );
	}

	public void setStartStatePosition( int startStatePosition ) {
		this.sliderUI.setStartStatePosition( startStatePosition );
	}

	public void setEndStatePosition( int endStatePosition ) {
		this.sliderUI.setEndStatePosition( endStatePosition );
	}

	public Rectangle getThumbBounds() {
		return ( (ProgramTimeSliderUI)this.getAwtComponent().getUI() ).getThumbBounds();
	}

	@Override
	protected javax.swing.JSlider createAwtComponent() {
		javax.swing.JSlider rv = new javax.swing.JSlider( this.getModel().getSwingModel().getBoundedRangeModel() );

		this.sliderUI = new ProgramTimeSliderUI( rv );
		rv.setUI( this.sliderUI );

		return rv;
	}

	private class ProgramTimeSliderUI extends SynthSliderUI {

		private Color statusColor;
		private int startStatePosition;
		private int endStatePosition;

		public ProgramTimeSliderUI( JSlider slider ) {
			super( slider );
			this.statusColor = ColorUtilities.createGray( 220 );
		}

		public void setStatusColor( Color statusColor ) {
			this.statusColor = statusColor;
		}

		public void setStartStatePosition( int startStatePosition ) {
			this.startStatePosition = startStatePosition;
		}

		public void setEndStatePosition( int endStatePosition ) {
			this.endStatePosition = endStatePosition;
		}

		public Rectangle getThumbBounds() {
			return this.thumbRect;
		}

		@Override
		protected void paintTrack( SynthContext context, Graphics g, Rectangle trackBounds ) {
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

			int INSET = 4;
			int x = trackBounds.x + ( getThumbBounds().width / 2 );
			int y = trackBounds.y + ( INSET / 2 );
			int w = trackBounds.width - getThumbBounds().width;
			int h = this.getThumbSize().height - INSET;

			java.awt.Shape s = new Rectangle( x, y, w, h );
			Color baseColor = new java.awt.Color( 161, 161, 179 );
			Paint paint = new java.awt.GradientPaint( 0, y, baseColor.darker(), 0, y + ( h / 2 ), baseColor );
			g2.setPaint( paint );
			g2.fill( s );

			java.awt.Shape s2 = new Rectangle( x, y, ( ( this.getThumbBounds().x - x ) + 6 ), h );
			Paint paint2 = new java.awt.GradientPaint( 0, y, new Color( 255, 255, 255, 212 ), 0, y + ( h / 2 ), this.statusColor );
			g2.setPaint( paint2 );
			g2.fill( s2 );

			int startPosition = xPositionForValue( this.startStatePosition );
			int endPosition = xPositionForValue( this.endStatePosition );
			if( ( this.startStatePosition > 0 ) && ( this.endStatePosition > 0 ) ) {
				java.awt.Shape remixBar = new Rectangle( startPosition, y, endPosition - startPosition, h );
				Paint remixPaint = new java.awt.GradientPaint( 0, y, new Color( 255, 255, 255, 255 ), 0, y + ( h / 2 ), new java.awt.Color( 144, 184, 239, 255 ) );
				g2.setPaint( remixPaint );
				g2.fill( remixBar );
			}
			if( this.startStatePosition > 0 ) {
				g2.setPaint( java.awt.Color.BLACK );
				g2.setStroke( new BasicStroke( 2.0f ) );
				g2.drawLine( startPosition, y, startPosition, y + h );
			}
			if( this.endStatePosition > 0 ) {
				g2.setPaint( java.awt.Color.BLACK );
				g2.setStroke( new BasicStroke( 2.0f ) );
				g2.drawLine( endPosition, y, endPosition, y + h );
			}

			g2.setPaint( baseColor.darker().darker() );
			g2.setStroke( new java.awt.BasicStroke( 1.0f ) );
			g2.draw( s );
		}

		@Override
		protected java.awt.Dimension getThumbSize() {
			return new java.awt.Dimension( 18, 18 );
		}

		@Override
		protected void paintThumb( SynthContext context, Graphics g, Rectangle thumbBounds ) {
			int state = context.getComponentState();
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

			int PAD = 1;
			int x = thumbBounds.x + PAD;
			int y = thumbBounds.y + PAD;
			int w = thumbBounds.width - ( PAD * 2 );
			int h = thumbBounds.height - ( PAD * 2 );
			Color baseColor = new java.awt.Color( 32, 175, 37 );
			Paint paint;

			boolean isPressed = ( state & SynthConstants.PRESSED ) != 0;
			boolean isOver = ( state & SynthConstants.MOUSE_OVER ) != 0;

			if( isPressed ) {
				paint = baseColor.darker();
			} else if( isOver ) {
				baseColor = baseColor.brighter();
				paint = new java.awt.GradientPaint( 0, y, baseColor.brighter(), 0, y + ( h / 2 ), baseColor );
			} else {
				paint = new java.awt.GradientPaint( 0, y, baseColor.brighter(), 0, y + ( h / 2 ), baseColor );
			}

			g2.setPaint( paint );
			g2.fillOval( x, y, w, h );

			g2.setPaint( Color.YELLOW );
			g2.setStroke( new java.awt.BasicStroke( 2.25f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND ) );

			int a = (int)( 0.33f * w );
			float center = 0.5f * w;
			int b = w - a;
			java.awt.geom.GeneralPath cross = new java.awt.geom.GeneralPath();
			cross.moveTo( x + a, y + center );
			cross.lineTo( x + b, y + center );
			cross.moveTo( x + center, y + a );
			cross.lineTo( x + center, y + b );
			g2.draw( cross );

			if( isPressed ) {
				paint = baseColor.darker().darker().darker();
			} else {
				paint = baseColor.darker().darker();

			}

			g2.setPaint( paint );
			g2.setStroke( new java.awt.BasicStroke( 1.0f ) );
			g2.drawOval( x, y, w, h );
		}
	}
}
