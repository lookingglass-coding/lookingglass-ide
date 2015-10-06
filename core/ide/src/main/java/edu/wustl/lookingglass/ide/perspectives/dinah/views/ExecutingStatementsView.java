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
package edu.wustl.lookingglass.ide.perspectives.dinah.views;

import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.ScrollPane.HorizontalScrollbarPolicy;
import org.lgna.croquet.views.ScrollPane.VerticalScrollbarPolicy;

import edu.wustl.lookingglass.ide.perspectives.dinah.ExecutingStatementsComposite;
import edu.wustl.lookingglass.ide.perspectives.dinah.finder.rightnowtree.ExecutionTraceTree;
import edu.wustl.lookingglass.ide.program.components.ProgramTimeSlider;

public class ExecutingStatementsView extends MigPanel {

	private ExecutionTraceTreeView executionTreeView;
	private ProgramTimeSlider slider;
	private MigPanel stepButtonGroup;

	public ExecutingStatementsView( ExecutingStatementsComposite composite ) {
		super( null, "fill, ins 0", "[fill]", "[]5[fill]" );

		this.executionTreeView = new ExecutionTraceTreeView( composite.getExecutionTree() );
		this.executionTreeView.setBackgroundColor( null );
		this.executionTreeView.setOpaque( false );

		org.lgna.croquet.views.ScrollPane scrollPane = new org.lgna.croquet.views.ScrollPane( this.executionTreeView, VerticalScrollbarPolicy.AS_NEEDED, HorizontalScrollbarPolicy.AS_NEEDED );
		scrollPane.setAlignmentX( javax.swing.JComponent.LEFT_ALIGNMENT );
		scrollPane.setOpaque( false );
		scrollPane.setBackgroundColor( null );
		scrollPane.getAwtComponent().getViewport().setOpaque( false );
		scrollPane.getAwtComponent().setViewportBorder( javax.swing.BorderFactory.createEmptyBorder() );

		this.slider = composite.getProgramTimeSlider();

		this.stepButtonGroup = new org.lgna.croquet.views.MigPanel( null, "fill", "[grow 0][fill][grow 0]", "[grow]" );
		org.lgna.croquet.views.Button stepBack = composite.getStepBackwardOperation().createButton();
		org.lgna.croquet.views.Button stepForward = composite.getStepForwardOperation().createButton();

		this.stepButtonGroup.addComponent( stepBack, "cell 0 0, grow 0, h 50!, top" );
		this.stepButtonGroup.addComponent( this.slider, "cell 1 0, grow" );
		this.stepButtonGroup.addComponent( stepForward, "cell 2 0, grow 0, h 50!, top" );

		this.addComponent( this.stepButtonGroup, "cell 0 0" );
		this.addComponent( scrollPane, "cell 0 1, push, top, gap 5 5 15 0" );
		this.setBackgroundColor( new java.awt.Color( 201, 201, 218 ) );
		this.setBorder( javax.swing.BorderFactory.createMatteBorder( 1, 0, 0, 0, java.awt.Color.GRAY ) );
	}

	public MigPanel getStepButtonGroup() {
		return this.stepButtonGroup;
	}

	private java.awt.Rectangle getSliderThumbBounds() {
		java.awt.Rectangle bounds = this.slider.getThumbBounds();
		return javax.swing.SwingUtilities.convertRectangle( this.slider.getAwtComponent(), bounds, this.getAwtComponent() );
	}

	private java.awt.Rectangle getTreeBounds() {
		return javax.swing.SwingUtilities.convertRectangle( this.executionTreeView.getAwtComponent().getParent(), this.executionTreeView.getAwtComponent().getBounds(), this.getAwtComponent() );
	}

	@Override
	protected javax.swing.JPanel createJPanel() {
		return new javax.swing.JPanel() {
			@Override
			public void paintComponent( java.awt.Graphics g ) {
				super.paintComponent( g );

				java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;
				g2.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );
				g2.setStroke( new java.awt.BasicStroke( 1.5f ) );
				g2.setPaint( new java.awt.Color( 32, 175, 37 ) );

				java.awt.Rectangle treeBounds = getTreeBounds();

				int treeX = treeBounds.x + 30;
				int treeY = treeBounds.y;

				java.awt.Rectangle thumbBounds = getSliderThumbBounds();

				int thumbX = thumbBounds.x + ( thumbBounds.width / 2 );
				int thumbY = thumbBounds.y + thumbBounds.height;

				java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
				path.moveTo( treeX, treeY + treeBounds.height );
				path.lineTo( treeX, treeY );
				path.lineTo( thumbX, thumbY + 5 );
				path.lineTo( thumbX, thumbY );

				g2.draw( path );
			}

		};
	}

	public class ExecutionTraceTreeView extends org.lgna.croquet.views.SwingComponentView<ExecutionTraceTree> {
		private final ExecutionTraceTree executionTree;

		public ExecutionTraceTreeView( ExecutionTraceTree executionTree ) {
			this.executionTree = executionTree;
		}

		@Override
		protected ExecutionTraceTree createAwtComponent() {
			return this.executionTree;
		}

	}
}
