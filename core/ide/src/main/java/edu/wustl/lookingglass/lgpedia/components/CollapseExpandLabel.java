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

import org.lgna.croquet.views.Label;

/**
 * @author Michael Pogran
 */
public class CollapseExpandLabel extends Label {
	private final String expandText;
	private final String collapseText;
	private final java.util.Set<org.lgna.croquet.views.SwingComponentView<?>> components = edu.cmu.cs.dennisc.java.util.Sets.newHashSet();
	private boolean componentsVisible;
	private java.awt.Color foregroundColor;

	public CollapseExpandLabel( String expandText, String collapseText, boolean visibleByDefault, org.lgna.croquet.views.SwingComponentView<?>... components ) {
		super( visibleByDefault ? collapseText : expandText, edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );
		this.expandText = expandText;
		this.collapseText = collapseText;
		this.components.addAll( java.util.Arrays.asList( components ) );
		this.componentsVisible = visibleByDefault;

		setForegroundColor( java.awt.Color.DARK_GRAY );
		setCursor( new java.awt.Cursor( java.awt.Cursor.HAND_CURSOR ) );
		getAwtComponent().addMouseListener( new java.awt.event.MouseAdapter() {
			@Override
			public void mouseReleased( java.awt.event.MouseEvent event ) {
				if( componentsVisible ) {
					collapse();
				} else {
					expand();
				}
			}
		} );

		for( org.lgna.croquet.views.SwingComponentView<?> component : CollapseExpandLabel.this.components ) {
			synchronized( component.getTreeLock() ) {
				component.setVisible( this.componentsVisible );
			}
		}
	}

	@Override
	protected javax.swing.JLabel createAwtComponent() {
		return new javax.swing.JLabel() {

			@Override
			protected void processMouseEvent( java.awt.event.MouseEvent e ) {
				super.processMouseEvent( e );
				if( ( e.getID() == java.awt.event.MouseEvent.MOUSE_ENTERED ) || ( e.getID() == java.awt.event.MouseEvent.MOUSE_EXITED ) ) {
					repaint();
				}
			}

			@Override
			protected void paintComponent( java.awt.Graphics g ) {
				java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;
				g2.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );
				int pad = 2;
				int size = getFont().getSize() - pad;

				java.awt.Color color = getForeground();
				if( getMousePosition() != null ) {
					color = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( color, 1.0, 1.0, .9 );
				}

				edu.cmu.cs.dennisc.java.awt.GraphicsUtilities.Heading heading = componentsVisible ? edu.cmu.cs.dennisc.java.awt.GraphicsUtilities.Heading.SOUTH : edu.cmu.cs.dennisc.java.awt.GraphicsUtilities.Heading.EAST;
				java.awt.Dimension dimensions = componentsVisible ? new java.awt.Dimension( size, size / 2 ) : new java.awt.Dimension( size / 2, size );
				java.awt.Point location = new java.awt.Point( ( ( getFont().getSize() - (int)dimensions.getWidth() ) / 2 ), pad + ( ( getFont().getSize() - (int)dimensions.getHeight() ) / 2 ) );

				edu.cmu.cs.dennisc.java.awt.GraphicsUtilities.fillTriangle( g2, heading, new java.awt.Rectangle( location, dimensions ) );

				g2.setColor( color );
				g2.drawString( getText(), size + 3, getFont().getSize() );
			}
		};
	}

	public void collapse() {
		setComponentsVisible( false );
		setText( this.expandText );

		this.componentsVisible = false;
	}

	public void expand() {
		setComponentsVisible( true );
		setText( this.collapseText );

		this.componentsVisible = true;
	}

	private void setComponentsVisible( boolean isVisible ) {
		for( org.lgna.croquet.views.SwingComponentView<?> component : CollapseExpandLabel.this.components ) {
			synchronized( component.getTreeLock() ) {
				component.setVisible( isVisible );
			}
		}
	}

}
