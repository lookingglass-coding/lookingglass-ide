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
package edu.wustl.lookingglass.ide.perspectives.dinah.processbar;

import java.awt.Color;
import java.awt.Graphics2D;

import org.lgna.croquet.views.BorderPanel;

import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;

/**
 * @author Michael Pogran
 */
public abstract class GuideStepPanel extends BorderPanel {
	private org.lgna.croquet.views.Label titleLabel;
	private org.lgna.croquet.views.MigPanel mainView;

	public abstract void startCaptureStateChange( AbstractEventNode<?> eventNode );

	public abstract void endCaptureStateChange( AbstractEventNode<?> eventNode );

	public abstract void programStateChange( ProgramStateEvent event );

	protected abstract void setStateLabel( String text, Color color );

	public GuideStepPanel() {
		this.titleLabel = new org.lgna.croquet.views.Label();
		this.mainView = new org.lgna.croquet.views.MigPanel( null, "fill, gap 15!, insets 5 10 10 10" );
		this.titleLabel.setFont( titleLabel.getFont().deriveFont( java.awt.Font.BOLD, 14f ) );
		this.mainView.setBorder( new RoundedCompoundBorder( 5, 10, 10, 10, Color.WHITE ) );
		this.titleLabel.setBorder( javax.swing.BorderFactory.createEmptyBorder( 10, 5, 0, 0 ) );

		this.addPageStartComponent( titleLabel );
		this.addCenterComponent( mainView );
		this.setBackgroundColor( new Color( 195, 216, 243 ) );
	}

	public String createColorString( Color color ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "rgb(" );
		sb.append( color.getRed() );
		sb.append( ", " );
		sb.append( color.getGreen() );
		sb.append( ", " );
		sb.append( color.getBlue() );
		sb.append( ")" );

		return sb.toString();
	}

	public void setTitle( String title ) {
		this.titleLabel.setText( title );
	}

	public void addComponent( org.lgna.croquet.views.AwtComponentView<?> component, String constraint ) {
		this.mainView.addComponent( component, constraint );
	}

	public void addComponent( org.lgna.croquet.views.AwtComponentView<?> component ) {
		this.mainView.addComponent( component );
	}

	private class RoundedCompoundBorder extends javax.swing.border.EmptyBorder {
		private Color color;

		public RoundedCompoundBorder( int top, int left, int bottom, int right, Color color ) {
			super( top, left, bottom, right );
			this.color = color;
		}

		@Override
		public void paintBorder( java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height ) {
			java.awt.Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );
			g2.setPaint( this.color );
			g2.fillRoundRect( x + left, y + top, width - ( left + right ), height - ( top + bottom ), 10, 10 );
			g2.setPaint( this.color.darker() );
			g2.drawRoundRect( x + left, y + top, width - ( left + right ), height - ( top + bottom ), 10, 10 );
		}
	}

}
