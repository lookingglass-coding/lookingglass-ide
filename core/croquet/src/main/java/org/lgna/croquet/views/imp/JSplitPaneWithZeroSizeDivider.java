/*
 * Copyright (c) 2011 Karl Tauber <karl at jformdesigner dot com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.lgna.croquet.views.imp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.ConstructorProperties;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * A JSplitPane that uses a 1 pixel thin visible divider, but a 9 pixel wide
 * transparent drag area.
 */
public class JSplitPaneWithZeroSizeDivider
		extends JSplitPane
{

	public static final Color DIVIDER_COLOR = new Color( 146, 151, 161 ); // Danielle's color

	/**
	 * The size of the transparent drag area.
	 */
	private int dividerDragSize = 9;

	/**
	 * The offset of the transparent drag area relative to the visible divider
	 * line. Positive offset moves the drag area left/top to the divider line.
	 * If zero then the drag area is right/bottom of the divider line. Useful
	 * values are in the range 0 to {@link #dividerDragSize}. Default is
	 * centered.
	 */
	private int dividerDragOffset = 4;

	/**
	 * Creates a new <code>JSplitPaneWithZeroSizeDivider</code> configured to
	 * arrange the child components side-by-side horizontally, using two buttons
	 * for the components.
	 */
	public JSplitPaneWithZeroSizeDivider() {
		super();
		this.initialize();
	}

	/**
	 * Creates a new <code>JSplitPaneWithZeroSizeDivider</code> configured with
	 * the specified orientation.
	 *
	 * @param newOrientation <code>JSplitPane.HORIZONTAL_SPLIT</code> or
	 *            <code>JSplitPane.VERTICAL_SPLIT</code>
	 * @exception IllegalArgumentException if <code>orientation</code> is not
	 *                one of HORIZONTAL_SPLIT or VERTICAL_SPLIT.
	 */
	@ConstructorProperties( { "orientation" } )
	public JSplitPaneWithZeroSizeDivider( int newOrientation ) {
		super( newOrientation );
		this.initialize();
	}

	/**
	 * Creates a new <code>JSplitPane</code> with the specified orientation and
	 * redrawing style.
	 *
	 * @param newOrientation <code>JSplitPane.HORIZONTAL_SPLIT</code> or
	 *            <code>JSplitPane.VERTICAL_SPLIT</code>
	 * @param newContinuousLayout a boolean, true for the components to redraw
	 *            continuously as the divider changes position, false to wait
	 *            until the divider position stops changing to redraw
	 * @exception IllegalArgumentException if <code>orientation</code> is not
	 *                one of HORIZONTAL_SPLIT or VERTICAL_SPLIT
	 */
	public JSplitPaneWithZeroSizeDivider( int newOrientation,
			boolean newContinuousLayout ) {
		super( newOrientation, newContinuousLayout );
		this.initialize();
	}

	/**
	 * Creates a new <code>JSplitPaneWithZeroSizeDivider</code> with the
	 * specified orientation and the specified components.
	 *
	 * @param newOrientation <code>JSplitPane.HORIZONTAL_SPLIT</code> or
	 *            <code>JSplitPane.VERTICAL_SPLIT</code>
	 * @param newLeftComponent the <code>Component</code> that will appear on
	 *            the left of a horizontally-split pane, or at the top of a
	 *            vertically-split pane
	 * @param newRightComponent the <code>Component</code> that will appear on
	 *            the right of a horizontally-split pane, or at the bottom of a
	 *            vertically-split pane
	 * @exception IllegalArgumentException if <code>orientation</code> is not
	 *                one of: HORIZONTAL_SPLIT or VERTICAL_SPLIT
	 */
	public JSplitPaneWithZeroSizeDivider( int newOrientation,
			Component newLeftComponent,
			Component newRightComponent ) {
		super( newOrientation, newLeftComponent, newRightComponent );
		this.initialize();
	}

	/**
	 * Creates a new <code>JSplitPaneWithZeroSizeDivider</code> with the
	 * specified orientation and redrawing style, and with the specified
	 * components.
	 *
	 * @param newOrientation <code>JSplitPane.HORIZONTAL_SPLIT</code> or
	 *            <code>JSplitPane.VERTICAL_SPLIT</code>
	 * @param newContinuousLayout a boolean, true for the components to redraw
	 *            continuously as the divider changes position, false to wait
	 *            until the divider position stops changing to redraw
	 * @param newLeftComponent the <code>Component</code> that will appear on
	 *            the left of a horizontally-split pane, or at the top of a
	 *            vertically-split pane
	 * @param newRightComponent the <code>Component</code> that will appear on
	 *            the right of a horizontally-split pane, or at the bottom of a
	 *            vertically-split pane
	 * @exception IllegalArgumentException if <code>orientation</code> is not
	 *                one of HORIZONTAL_SPLIT or VERTICAL_SPLIT
	 */
	public JSplitPaneWithZeroSizeDivider( int newOrientation,
			boolean newContinuousLayout,
			Component newLeftComponent,
			Component newRightComponent ) {
		super( newOrientation, newContinuousLayout, newLeftComponent, newRightComponent );
		this.initialize();
	}

	private void initialize() {
		setContinuousLayout( true );
		setDividerSize( 1 );
	}

	public int getDividerDragSize() {
		return dividerDragSize;
	}

	public void setDividerDragSize( int dividerDragSize ) {
		this.dividerDragSize = dividerDragSize;
		revalidate();
	}

	public int getDividerDragOffset() {
		return dividerDragOffset;
	}

	public void setDividerDragOffset( int dividerDragOffset ) {
		this.dividerDragOffset = dividerDragOffset;
		revalidate();
	}

	@Override
	@SuppressWarnings( "deprecation" )
	public void layout() {
		super.layout();

		// increase divider width or height
		BasicSplitPaneDivider divider = ( (BasicSplitPaneUI)getUI() ).getDivider();
		Rectangle bounds = divider.getBounds();
		if( orientation == HORIZONTAL_SPLIT ) {
			bounds.x -= dividerDragOffset;
			bounds.width = dividerDragSize;
		} else {
			bounds.y -= dividerDragOffset;
			bounds.height = dividerDragSize;
		}
		divider.setBounds( bounds );
	}

	@Override
	public void updateUI() {
		setUI( new SplitPaneWithZeroSizeDividerUI() );
		revalidate();
	}

	//---- class SplitPaneWithZeroSizeDividerUI -------------------------------

	private class SplitPaneWithZeroSizeDividerUI
			extends BasicSplitPaneUI
	{
		@Override
		public BasicSplitPaneDivider createDefaultDivider() {
			return new ZeroSizeDivider( this );
		}
	}

	//---- class ZeroSizeDivider ----------------------------------------------

	private class ZeroSizeDivider
			extends BasicSplitPaneDivider
	{
		public ZeroSizeDivider( BasicSplitPaneUI ui ) {
			super( ui );
			super.setBorder( null );
			setBackground( DIVIDER_COLOR );
		}

		@Override
		public void setBorder( Border border ) {
			// ignore
		}

		@Override
		public void paint( Graphics g ) {
			g.setColor( getBackground() );
			if( orientation == HORIZONTAL_SPLIT ) {
				g.drawLine( dividerDragOffset, 0, dividerDragOffset, getHeight() - 1 );
			} else {
				g.drawLine( 0, dividerDragOffset, getWidth() - 1, dividerDragOffset );
			}
		}

		@Override
		protected void dragDividerTo( int location ) {
			super.dragDividerTo( location + dividerDragOffset );
		}

		@Override
		protected void finishDraggingTo( int location ) {
			super.finishDraggingTo( location + dividerDragOffset );
		}
	}

	public static void main( String[] args ) {
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch( Exception ex ) {
			ex.printStackTrace();
		}

		// create left and right components
		JComponent left = new JScrollPane( new JTree() );
		JComponent right = new JScrollPane( new JList(
				new String[] { "white", "black", "gray", "red", "green", "blue" } ) );

		// remove borders from scroll panes
		left.setBorder( null );
		right.setBorder( null );

		// create split pane
		JSplitPaneWithZeroSizeDivider splitPane = new JSplitPaneWithZeroSizeDivider();
		splitPane.setBorder( null );
		splitPane.setDividerLocation( 200 );
		splitPane.setLeftComponent( left );
		splitPane.setRightComponent( right );

		// create frame
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.add( splitPane );
		frame.setSize( 400, 300 );
		frame.setVisible( true );
	}
}
