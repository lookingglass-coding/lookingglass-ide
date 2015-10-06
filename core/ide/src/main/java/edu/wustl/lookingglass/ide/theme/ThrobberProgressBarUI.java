/*
 * @(#)AquaSpinningProgressBarUI.java
 *
 * $Date: 2011-05-02 16:01:45 -0500 (Mon, 02 May 2011) $
 *
 * Copyright (c) 2011 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Jeremy Wood. For details see accompanying license terms.
 *
 * This software is probably, but not necessarily, discussed here:
 * http://javagraphics.java.net/
 *
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package edu.wustl.lookingglass.ide.theme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.lang.ref.WeakReference;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Twelve short line segments that pulse in a clockwise direction.
 *
 * <P>
 * The line segments are painted in the component's foreground color, with
 * varying levels of opacity. (When this UI is installed the component's
 * foreground color is set to Color.black.)
 * <P>
 * Also the line segments will complete a full revolution in 500 milliseconds.
 * You can change this rate with the client property "period". This is the
 * length (in milliseconds) this UI takes to complete a full cycle.
 */
public class ThrobberProgressBarUI extends javax.swing.plaf.ProgressBarUI {

	static class RepaintListener implements ActionListener {
		WeakReference<JComponent> referenceComponent;
		WeakReference<ThrobberProgressBarUI> referenceUI;

		public RepaintListener( JComponent c, ThrobberProgressBarUI ui ) {
			referenceComponent = new WeakReference<JComponent>( c );
			referenceUI = new WeakReference<ThrobberProgressBarUI>( ui );
		}

		@Override
		public void actionPerformed( ActionEvent e ) {
			JComponent jc = referenceComponent.get();
			if( jc != null ) {
				ThrobberProgressBarUI ui = referenceUI.get();
				synchronized( ui ) {
					//note: this assumes one ui per component
					if( ui.isActuallyPainting || ui.isOneTimeRepaintRequired ) {
						ui.isOneTimeRepaintRequired = false;
						jc.repaint();
					}
				}
			}
		}
	}

	/**
	 * The default duration (in ms) it takes to complete a cycle. <BR>
	 * You can customize this by setting the client property "period" to an
	 * arbitrary positive number.
	 */
	private static final Long DEFAULT_PERIOD = new Long( 1000 );

	private static final Hashtable<Color, Color[]> foregroundTable = new Hashtable<Color, Color[]>();

	private static final Line2D line = new Line2D.Float();

	private static final String REPAINTER_KEY = "SpinningProgressBarUI.repainter";

	private static final BasicStroke stroke = new BasicStroke( 1.9f, BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_BEVEL );

	private boolean isActuallyPainting = true;

	/** @return <code>getPreferredSize(c)</code> */
	@Override
	public Dimension getMaximumSize( JComponent c ) {
		return getPreferredSize( c );
	}

	/** @return <code>getPreferredSize(c)</code> */
	@Override
	public Dimension getMinimumSize( JComponent c ) {
		return getPreferredSize( c );
	}

	@Override
	public Dimension getPreferredSize( JComponent c ) {
		return new Dimension( 20, 20 );
	}

	/**
	 * Returns the number of milliseconds between calls to repaint.
	 * <P>
	 * This should be a fixed value that does not change.
	 */
	public int getRepaintDelay() {
		return 1000 / 24;
	}

	private boolean isOneTimeRepaintRequired = false;

	public boolean isActuallyPainting() {
		return this.isActuallyPainting;
	}

	public void setActuallyPainting( boolean isActuallyPainting ) {
		if( this.isActuallyPainting != isActuallyPainting ) {
			this.isActuallyPainting = isActuallyPainting;
			if( this.isActuallyPainting ) {
				//pass
			} else {
				this.isOneTimeRepaintRequired = true;
			}
		}
	}

	@Override
	public void installUI( JComponent c ) {
		super.installUI( c );
		Timer timer = new Timer( getRepaintDelay(), new RepaintListener( c, this ) );
		c.putClientProperty( REPAINTER_KEY, timer );
		timer.start();
		c.setForeground( Color.DARK_GRAY );
	}

	@Override
	public void paint( Graphics g0, JComponent jc ) {
		Graphics2D g = (Graphics2D)g0.create();
		paintBackground( g, jc );

		if( this.isActuallyPainting ) {
			Dimension d = getPreferredSize( jc );
			double sx = ( (double)jc.getWidth() ) / ( (double)d.width );
			double sy = ( (double)jc.getHeight() ) / ( (double)d.height );
			double scale = Math.min( sx, sy );
			g.scale( scale, scale );

			paintForeground( g, jc, d );
			g.dispose();
		}
	}

	protected void paintBackground( Graphics2D g, JComponent jc ) {
		if( jc.isOpaque() ) {
			g.setColor( jc.getBackground() );
			g.fillRect( 0, 0, jc.getWidth(), jc.getHeight() );
		}
	}

	protected void paintForeground( Graphics2D g, JComponent jc, Dimension size ) {
		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON );

		g.setRenderingHint( RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_PURE );

		Number period = (Number)jc.getClientProperty( "period" );
		if( period == null ) {
			period = DEFAULT_PERIOD;
		}

		float f = ( ( (float)( System.currentTimeMillis() % period.longValue() ) ) / period.longValue() ) * 12;

		Number forcedValue = (Number)jc.getClientProperty( "forcedFraction" );
		if( forcedValue != null ) {
			f = forcedValue.floatValue();
		}

		int i = (int)f;

		Color fgnd = jc.getForeground();

		Color[] colors = (Color[])foregroundTable.get( fgnd );
		if( colors == null ) {
			int red = fgnd.getRed();
			int green = fgnd.getGreen();
			int blue = fgnd.getBlue();
			colors = new Color[] {
					new Color( red, green, blue, 255 ),
					new Color( red, green, blue, 240 ),
					new Color( red, green, blue, 225 ),
					new Color( red, green, blue, 200 ),
					new Color( red, green, blue, 160 ),
					new Color( red, green, blue, 130 ),
					new Color( red, green, blue, 115 ),
					new Color( red, green, blue, 100 ),
					new Color( red, green, blue, 90 ),
					new Color( red, green, blue, 80 ),
					new Color( red, green, blue, 70 ),
					new Color( red, green, blue, 60 )

			};
		}

		int centerX = size.width / 2;
		int centerY = size.height / 2;

		g.setStroke( stroke );
		double theta;
		for( int a = 0; a < colors.length; a++ ) {
			g.setColor( colors[ ( i + a ) % colors.length ] );
			theta = ( -( (double)a ) / ( (double)colors.length ) ) * Math.PI * 2;
			line.setLine( centerX + ( 5 * Math.cos( theta ) ),
					centerY + ( 5 * Math.sin( theta ) ),
					centerX + ( 8 * Math.cos( theta ) ),
					centerY + ( 8 * Math.sin( theta ) ) );

			g.draw( line );
		}
	}

	/** Disarms the timer and removes the <code>ChangeListener</code> */
	@Override
	public void uninstallUI( JComponent c ) {
		super.uninstallUI( c );
		Timer timer = (Timer)c.getClientProperty( REPAINTER_KEY );
		timer.stop();
		c.repaint();
	}
}
