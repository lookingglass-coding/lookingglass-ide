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

package edu.cmu.cs.dennisc.javax.swing.components;

/**
 * @author Dennis Cosgrove
 */

public abstract class JExpandPane extends javax.swing.AbstractButton {
	// <lg> edited to make more extensible
	class ToggleButton extends javax.swing.JToggleButton {
		@Override
		public java.awt.Dimension getPreferredSize() {
			java.awt.Dimension rv = super.getPreferredSize();
			java.awt.Font font = this.getFont();
			if( font != null ) {
				java.awt.Graphics g = edu.cmu.cs.dennisc.java.awt.GraphicsUtilities.getGraphics();
				// <lg/> Turn on anti-aliasing
				( (java.awt.Graphics2D)g ).setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );
				java.awt.FontMetrics fm = g.getFontMetrics( font );
				for( String s : new String[] { JExpandPane.this.getExpandedButtonText(), JExpandPane.this.getCollapsedButtonText() } ) {
					java.awt.geom.Rectangle2D bounds = fm.getStringBounds( s, g );
					rv.width = Math.max( rv.width, (int)bounds.getWidth() + 16 );
					rv.height = Math.max( rv.height, (int)bounds.getHeight() + 4 );
				}
			}
			return rv;
		}

		@Override
		protected void paintComponent( java.awt.Graphics g ) {
			super.paintComponent( g );
			// <lg/> Turn on anti-aliasing
			( (java.awt.Graphics2D)g ).setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

			String text;
			if( this.isSelected() ) {
				text = JExpandPane.this.getExpandedButtonText();
			} else {
				text = JExpandPane.this.getCollapsedButtonText();
			}
			edu.cmu.cs.dennisc.java.awt.GraphicsUtilities.drawCenteredText( g, text, this.getSize() );
		}
	}

	private javax.swing.JLabel label = createLabel();
	private javax.swing.JToggleButton toggle = createToggleButton();
	private javax.swing.JComponent center = this.createCenterPane();

	protected abstract String getExpandedLabelText();

	protected abstract String getCollapsedLabelText();

	protected abstract javax.swing.JComponent createCenterPane();

	public JExpandPane() {
		this.setModel( new javax.swing.DefaultButtonModel() );
		this.addItemListener( new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged( java.awt.event.ItemEvent e ) {
				JExpandPane.this.handleToggled( e );
			}
		} );
		this.label.setText( this.getCollapsedLabelText() );
		this.setLayout( new java.awt.BorderLayout() );
		this.add( this.createTopPane(), java.awt.BorderLayout.NORTH );
	}

	protected javax.swing.JLabel createLabel() {
		return new javax.swing.JLabel();
	}

	protected javax.swing.JToggleButton createToggleButton() {
		final ToggleButton rv = new ToggleButton();
		rv.addActionListener( new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed( java.awt.event.ActionEvent e ) {
				JExpandPane.this.setSelected( rv.isSelected() );
			}
		} );
		rv.setIcon( getCollapsedButtonIcon() );
		return rv;

	}

	//todo: rename
	public javax.swing.JComponent getCenterComponent() {
		return this.center;
	}

	protected String getExpandedButtonText() {
		return "v";
	}

	protected String getCollapsedButtonText() {
		return ">>>";
	}

	protected javax.swing.Icon getCollapsedButtonIcon() {
		return null;
	}

	protected javax.swing.Icon getExpandedButtonIcon() {
		return null;
	}

	private void handleToggled( java.awt.event.ItemEvent e ) {
		if( e.getStateChange() == java.awt.event.ItemEvent.SELECTED ) {
			this.add( this.getCenterComponent(), java.awt.BorderLayout.CENTER );
			this.label.setText( this.getExpandedLabelText() );
			this.toggle.setIcon( this.getExpandedButtonIcon() );
		} else {
			this.remove( this.getCenterComponent() );
			this.label.setText( this.getCollapsedLabelText() );
			this.toggle.setIcon( this.getCollapsedButtonIcon() );
		}
		this.revalidate();
		this.repaint();
		java.awt.Component root = javax.swing.SwingUtilities.getRoot( this );
		if( root instanceof java.awt.Window ) {
			java.awt.Window window = (java.awt.Window)root;
			window.pack();
		}
	}

	protected javax.swing.JComponent createTopPane() {
		JLineAxisPane rv = new JLineAxisPane();
		rv.add( this.label );
		rv.add( this.toggle );
		rv.add( javax.swing.Box.createHorizontalGlue() );
		return rv;
	}
	// </lg>
}
