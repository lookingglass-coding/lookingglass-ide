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
package edu.wustl.lookingglass.puzzle.ui.croquet.views;

import org.lgna.croquet.views.BorderPanel;
import org.lgna.croquet.views.LineAxisPanel;

import edu.wustl.lookingglass.croquetfx.FxViewAdaptor;
import edu.wustl.lookingglass.puzzle.CompletionPuzzle;
import edu.wustl.lookingglass.puzzle.ui.InstructionsPane;
import edu.wustl.lookingglass.puzzle.ui.PuzzleToolbar;

public class PuzzleWorkspaceView extends org.lgna.croquet.views.MigPanel {

	private final CompletionPuzzle puzzle;

	private LineAxisPanel header;
	private PuzzleToolbar toolbar;
	private BorderPanel statementPane;
	private InstructionsPane instructionsPane;

	public PuzzleWorkspaceView( org.lgna.croquet.views.SwingComponentView<?> statementListComponent, CompletionPuzzle puzzle ) {
		super( null, "gapx 0, gapy 10, fillx, insets 10 13 11 13" );
		this.puzzle = puzzle;

		this.header = new org.lgna.croquet.views.LineAxisPanel();
		header.setBorder( javax.swing.BorderFactory.createEmptyBorder() );
		this.addComponent( header, "growx, wrap" );

		this.statementPane = new BorderPanel();
		this.statementPane.setBackgroundColor( CompletionPuzzleView.PUZZLE_BACKGROUND_COLOR );
		this.statementPane.addCenterComponent( statementListComponent );
		this.addComponent( statementPane, "growx, wrap" );

		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( ( ) -> {
			this.toolbar = new edu.wustl.lookingglass.puzzle.ui.PuzzleToolbar( this.puzzle );
			edu.wustl.lookingglass.croquetfx.FxViewAdaptor toolbarView = this.toolbar.getFxViewAdaptor();

			this.instructionsPane = new InstructionsPane( this.puzzle );
			FxViewAdaptor instructionsView = this.instructionsPane.getFxViewAdaptor();

			javax.swing.SwingUtilities.invokeLater( ( ) -> {
				synchronized( this.getTreeLock() ) {
					header.addComponent( toolbarView );
					statementPane.addPageStartComponent( instructionsView );
				}
			} );
		} );

		this.setBackgroundColor( CompletionPuzzleView.PUZZLE_EDITABLE_COLOR );
		this.setOpaque( false );
	}

	@Override
	protected javax.swing.JPanel createJPanel() {
		return new DefaultJPanel() {
			@Override
			public java.awt.Dimension getMaximumSize() {
				java.awt.Dimension preferredSize = this.getPreferredSize();
				return new java.awt.Dimension( Short.MAX_VALUE, preferredSize.height );
			}

			@Override
			public void paint( java.awt.Graphics g ) {
				final int ARC_SIZE = 6;
				final double x0 = 0;
				final double x1 = this.getWidth() - 1;
				final double y0 = 0;
				final double y1 = this.getHeight() - 1;

				java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
				path.moveTo( x0, y1 - ARC_SIZE );
				path.quadTo( x0, y1, x0 + ARC_SIZE, y1 );
				path.lineTo( x1 - ARC_SIZE, y1 );
				path.quadTo( x1, y1, x1, y1 - ARC_SIZE );
				path.lineTo( x1, ARC_SIZE );
				path.quadTo( x1, y0, x1 - ARC_SIZE, y0 );
				path.lineTo( ARC_SIZE, y0 );
				path.quadTo( x0, y0, x0, ARC_SIZE );
				path.closePath();

				java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;
				edu.cmu.cs.dennisc.java.awt.GraphicsContext gc = edu.cmu.cs.dennisc.java.awt.GraphicsContext.getInstanceAndPushGraphics( g );
				gc.pushAndSetAntialiasing( true );
				gc.pushStroke();
				gc.pushPaint();

				g2.setColor( this.getBackground() );
				g2.fill( path );
				g2.setStroke( new java.awt.BasicStroke( 1.0f ) );
				g2.setPaint( CompletionPuzzleView.PUZZLE_BORDER_COLOR );
				g2.draw( path );
				gc.popAll();
				super.paint( g );
			}
		};
	}
}
