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
import org.lgna.croquet.views.BoxUtilities;

import edu.wustl.lookingglass.puzzle.ui.croquet.PuzzleResourcesBinComposite;

/**
 * @author Kyle J. Harms
 */
public class PuzzleResourcesBinView extends BorderPanel {

	/*package-private*/static final int BIN_PADDING = 8;

	private final PuzzleStatementsBinView statementsBinView;

	public PuzzleResourcesBinView( PuzzleResourcesBinComposite composite ) {
		super( composite );
		this.setBackgroundColor( CompletionPuzzleView.PUZZLE_BACKGROUND_COLOR );

		this.statementsBinView = new PuzzleStatementsBinView( composite.getPuzzle() );
		this.addCenterComponent( this.statementsBinView );

		this.addPageStartComponent( BoxUtilities.createVerticalSliver( BIN_PADDING ) );
		this.addPageEndComponent( BoxUtilities.createVerticalSliver( BIN_PADDING * 2 ) );
		this.addLineStartComponent( BoxUtilities.createHorizontalSliver( BIN_PADDING * 2 ) );
		this.addLineEndComponent( BoxUtilities.createHorizontalSliver( BIN_PADDING * 2 ) );
	}

	@Override
	public PuzzleResourcesBinComposite getComposite() {
		return (PuzzleResourcesBinComposite)super.getComposite();
	}

	@Override
	protected javax.swing.JPanel createJPanel() {
		return new javax.swing.JPanel() {
			@Override
			protected void paintComponent( java.awt.Graphics g ) {
				super.paintComponent( g );

				java.awt.Graphics2D g2d = (java.awt.Graphics2D)g.create();
				g2d.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

				final double CORNER_ARC = 12;
				java.awt.Shape rect = new java.awt.geom.RoundRectangle2D.Double( BIN_PADDING, 0.0, this.getWidth() - ( BIN_PADDING * 2.0 ), this.getHeight() - BIN_PADDING, CORNER_ARC, CORNER_ARC );
				g2d.setPaint( CompletionPuzzleView.PUZZLE_EDITABLE_COLOR );
				g2d.fill( rect );
				g2d.setStroke( new java.awt.BasicStroke( 1.0f ) );
				g2d.setPaint( CompletionPuzzleView.PUZZLE_BORDER_COLOR );
				g2d.draw( rect );

				g2d.dispose();
			}
		};
	}

	public PuzzleStatementsBinView getStatementsBinView() {
		return this.statementsBinView;
	}
}
