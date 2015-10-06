/*******************************************************************************
 * Copyright (c) 2006, 2015, Carnegie Mellon University. All rights reserved.
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
 * 3. Products derived from the software may not be called "Alice", nor may
 *    "Alice" appear in their name, without prior written permission of
 *    Carnegie Mellon University.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Carnegie Mellon University"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 * ANY AND ALL EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT
 * SHALL THE AUTHORS, COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package org.lgna.croquet.views;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

/**
 * @author Dennis Cosgrove
 */
public class StatusLabel extends SwingComponentView<javax.swing.JLabel> {
	private static final String TEXT_TO_USE_FOR_GOOD_TO_GO_STATUS = "good to go";

	public StatusLabel() {
	}

	public void setStatus( org.lgna.croquet.AbstractSeverityStatusComposite.Status status ) {
		this.checkEventDispatchThread();
		String text;
		if( org.lgna.croquet.AbstractSeverityStatusComposite.IS_GOOD_TO_GO_STATUS == status ) {
			text = TEXT_TO_USE_FOR_GOOD_TO_GO_STATUS;
		} else {
			text = status.getText();
		}
		this.getAwtComponent().setText( text );
	}

	@Override
	protected javax.swing.JLabel createAwtComponent() {
		javax.swing.JLabel rv = new javax.swing.JLabel( TEXT_TO_USE_FOR_GOOD_TO_GO_STATUS ) {
			@Override
			protected void paintComponent( java.awt.Graphics g ) {
				if( this.getText() == TEXT_TO_USE_FOR_GOOD_TO_GO_STATUS ) {
					//pass
				} else {
					super.paintComponent( g );
				}
			}
		};
		// <lg>
		rv.setIcon( new WarningIcon( 35, 35 ) );
		rv.setHorizontalTextPosition( javax.swing.SwingConstants.LEFT );
		rv.setVerticalTextPosition( javax.swing.SwingConstants.BOTTOM );
		rv.setFont( rv.getFont().deriveFont( Font.BOLD, 12.0f ) );
		return rv;
	}

	public class WarningIcon implements javax.swing.Icon {

		/** The width of this icon. */
		private int width;

		/** The height of this icon. */
		private int height;

		/** The rendered image. */
		private BufferedImage image;

		/**
		 * Creates a new transcoded SVG image.
		 */
		public WarningIcon() {
			this( 1, 1 );
		}

		/**
		 * Creates a new transcoded SVG image.
		 */
		public WarningIcon( int width, int height ) {
			this.width = width;
			this.height = height;
		}

		@Override
		public int getIconHeight() {
			return height;
		}

		@Override
		public int getIconWidth() {
			return width;
		}

		@Override
		public void paintIcon( Component c, Graphics g, int x, int y ) {
			if( image == null ) {
				image = new BufferedImage( getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB );
				double coef = Math.min( (double)width / (double)1, (double)height / (double)1 );

				Graphics2D g2d = image.createGraphics();
				g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
				g2d.scale( coef, coef );
				paint( g2d );
				g2d.dispose();
			}

			g.drawImage( image, x, y, null );
		}

		/**
		 * Paints the transcoded SVG image on the specified graphics context.
		 *
		 * @param g Graphics context.
		 */
		private void paint( Graphics2D g ) {
			Shape shape = null;

			float origAlpha = 1.0f;

			java.util.LinkedList<AffineTransform> transformations = new java.util.LinkedList<AffineTransform>();

			transformations.offer( g.getTransform() );
			g.transform( new AffineTransform( 0.01f, 0, 0, 0.01f, 0, 0 ) );

			shape = new GeneralPath();
			( (GeneralPath)shape ).moveTo( 50.0, 14.5 );
			( (GeneralPath)shape ).curveTo( 50.1, 14.6, 50.3, 14.9, 50.5, 15.3 );
			( (GeneralPath)shape ).lineTo( 68.6, 47.399998 );
			( (GeneralPath)shape ).lineTo( 72.9, 54.899998 );
			( (GeneralPath)shape ).lineTo( 77.200005, 62.399998 );
			( (GeneralPath)shape ).lineTo( 95.3, 94.5 );
			( (GeneralPath)shape ).curveTo( 95.5, 94.9, 95.600006, 95.2, 95.700005, 95.4 );
			( (GeneralPath)shape ).curveTo( 95.50001, 95.5, 95.200005, 95.5, 94.700005, 95.5 );
			( (GeneralPath)shape ).lineTo( 58.5, 95.5 );
			( (GeneralPath)shape ).lineTo( 41.5, 95.5 );
			( (GeneralPath)shape ).lineTo( 5.3, 95.5 );
			( (GeneralPath)shape ).curveTo( 4.9, 95.5, 4.6000004, 95.5, 4.3, 95.4 );
			( (GeneralPath)shape ).curveTo( 4.4, 95.200005, 4.5, 94.9, 4.7000003, 94.5 );
			( (GeneralPath)shape ).lineTo( 22.800001, 62.4 );
			( (GeneralPath)shape ).lineTo( 27.100002, 54.9 );
			( (GeneralPath)shape ).lineTo( 31.400002, 47.4 );
			( (GeneralPath)shape ).lineTo( 49.5, 15.300003 );
			( (GeneralPath)shape ).curveTo( 49.7, 14.9, 49.9, 14.6, 50.0, 14.5 );
			( (GeneralPath)shape ).moveTo( 50.0, 10.0 );
			( (GeneralPath)shape ).curveTo( 48.4, 10.0, 46.9, 11.1, 45.7, 13.2 );
			( (GeneralPath)shape ).lineTo( 27.6, 45.2 );
			( (GeneralPath)shape ).curveTo( 25.300001, 49.3, 21.400002, 56.1, 19.1, 60.4 );
			( (GeneralPath)shape ).lineTo( 1.0, 92.5 );
			( (GeneralPath)shape ).curveTo( -1.3, 96.6, 0.6, 100.0, 5.3, 100.0 );
			( (GeneralPath)shape ).lineTo( 41.5, 100.0 );
			( (GeneralPath)shape ).curveTo( 46.2, 100.0, 53.9, 100.0, 58.5, 100.0 );
			( (GeneralPath)shape ).lineTo( 94.7, 100.0 );
			( (GeneralPath)shape ).curveTo( 99.399994, 100.0, 101.299995, 96.6, 99.0, 92.5 );
			( (GeneralPath)shape ).lineTo( 80.9, 60.3 );
			( (GeneralPath)shape ).curveTo( 78.6, 56.2, 74.700005, 49.4, 72.4, 45.1 );
			( (GeneralPath)shape ).lineTo( 54.3, 13.1 );
			( (GeneralPath)shape ).curveTo( 53.1, 11.0, 51.6, 10.0, 50.0, 10.0 );
			( (GeneralPath)shape ).lineTo( 50.0, 10.0 );
			( (GeneralPath)shape ).closePath();

			g.setPaint( Color.RED.darker().darker() );
			g.fill( shape );

			shape = new GeneralPath();
			( (GeneralPath)shape ).moveTo( 45.8, 64.4 );
			( (GeneralPath)shape ).lineTo( 44.6, 46.100002 );
			( (GeneralPath)shape ).curveTo( 44.399998, 42.500004, 44.3, 40.000004, 44.3, 38.4 );
			( (GeneralPath)shape ).curveTo( 44.3, 36.300003, 44.8, 34.600002, 45.899998, 33.4 );
			( (GeneralPath)shape ).curveTo( 46.999996, 32.2, 48.499996, 31.7, 50.199997, 31.7 );
			( (GeneralPath)shape ).curveTo( 52.299995, 31.7, 53.799995, 32.5, 54.499996, 34.0 );
			( (GeneralPath)shape ).curveTo( 55.199997, 35.5, 55.599995, 37.7, 55.599995, 40.5 );
			( (GeneralPath)shape ).curveTo( 55.599995, 42.1, 55.499996, 43.9, 55.399994, 45.6 );
			( (GeneralPath)shape ).lineTo( 53.799995, 64.5 );
			( (GeneralPath)shape ).curveTo( 53.599995, 66.8, 53.299995, 68.4, 52.599995, 69.6 );
			( (GeneralPath)shape ).curveTo( 51.899994, 70.799995, 51.099995, 71.5, 49.699993, 71.5 );
			( (GeneralPath)shape ).curveTo( 48.29999, 71.5, 47.499992, 71.0, 46.899994, 69.8 );
			( (GeneralPath)shape ).curveTo( 46.4, 68.5, 46.1, 66.8, 45.8, 64.4 );
			( (GeneralPath)shape ).closePath();
			( (GeneralPath)shape ).moveTo( 50.0, 89.7 );
			( (GeneralPath)shape ).curveTo( 48.5, 89.7, 47.1, 89.2, 46.1, 88.2 );
			( (GeneralPath)shape ).curveTo( 45.0, 87.2, 44.399998, 85.799995, 44.399998, 83.899994 );
			( (GeneralPath)shape ).curveTo( 44.399998, 82.299995, 44.899998, 80.99999, 45.999996, 79.899994 );
			( (GeneralPath)shape ).curveTo( 47.099995, 78.799995, 48.399998, 78.299995, 49.899998, 78.299995 );
			( (GeneralPath)shape ).curveTo( 51.399998, 78.299995, 52.8, 78.799995, 53.899998, 79.899994 );
			( (GeneralPath)shape ).curveTo( 55.1, 80.99999, 55.6, 82.399994, 55.6, 83.899994 );
			( (GeneralPath)shape ).curveTo( 55.6, 85.59999, 55.1, 87.09999, 53.899998, 88.2 );
			( (GeneralPath)shape ).curveTo( 52.8, 89.3, 51.5, 89.7, 50.0, 89.7 );
			( (GeneralPath)shape ).closePath();

			g.fill( shape );

			g.setTransform( transformations.poll() );

		}
	}
	//</lg>
}
