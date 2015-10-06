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
package edu.wustl.lookingglass.remix.share.views;

import edu.cmu.cs.dennisc.java.awt.font.TextWeight;
import edu.wustl.lookingglass.remix.share.PreviewWorldPage;

/**
 * @author Michael Pogran
 */
public class PreviewWorldView extends org.lgna.croquet.views.MigPanel {
	private final org.lgna.croquet.views.List<java.awt.Image> snapshotList;

	public PreviewWorldView( PreviewWorldPage composite ) {
		super( composite, "fillx", "[]", "[]40[]10[]" );

		// add preview panel
		org.lgna.croquet.views.MigPanel previewPanel = new org.lgna.croquet.views.MigPanel( null, "fill", "[][]", "[][][][]" );
		previewPanel.setBackgroundColor( java.awt.Color.WHITE );
		previewPanel.setBorder( javax.swing.BorderFactory.createMatteBorder( 1, 1, 2, 1, new java.awt.Color( 177, 177, 192 ) ) );

		previewPanel.addComponent( composite.getVideoComposite().getView(), "cell 0 0, spany 4, h " + 322 + "!, w " + 515 + "!" );
		previewPanel.addComponent( new edu.wustl.lookingglass.ide.croquet.components.LabelAndFieldComponent( composite.getOwner().getTitleState(), false, 1.35f, edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD ), "cell 1 0, grow" );
		previewPanel.addComponent( new edu.wustl.lookingglass.ide.croquet.components.LabelAndFieldComponent( composite.getOwner().getDescriptionState(), true ), "cell 1 1, grow, pushy" );
		previewPanel.addComponent( new edu.wustl.lookingglass.ide.croquet.components.LabelAndFieldComponent( composite.getOwner().getTagState(), false ), "cell 1 2, grow" );
		previewPanel.addComponent( composite.getCreatedByString().createLabel( 0.9f, edu.cmu.cs.dennisc.java.awt.font.TextPosture.OBLIQUE ), "cell 1 3, right" );

		// add snapshot panel
		org.lgna.croquet.views.MigPanel snapshotPanel = new org.lgna.croquet.views.MigPanel( null, "fill", "[]", "[][]" );
		snapshotPanel.setBackgroundColor( java.awt.Color.WHITE );

		this.snapshotList = composite.getSnapshotListState().createList();
		this.snapshotList.setCellRenderer( new SnapshotCellRenderer() );
		this.snapshotList.setVisibleRowCount( 1 );
		this.snapshotList.setLayoutOrientation( org.lgna.croquet.views.List.LayoutOrientation.HORIZONTAL_WRAP );
		this.snapshotList.setBackgroundColor( java.awt.Color.WHITE );
		this.snapshotList.setOpaque( true );

		org.lgna.croquet.views.Button captureButton = composite.getCapturePosterOperation().createButton();
		captureButton.setHorizontalTextPosition( org.lgna.croquet.views.HorizontalTextPosition.CENTER );
		captureButton.setVerticalTextPosition( org.lgna.croquet.views.VerticalTextPosition.BOTTOM );

		snapshotPanel.addComponent( snapshotList, "cell 0 1, h 150" );
		snapshotPanel.addComponent( captureButton, "cell 1 1, align 50% 50%" );

		// set snapshot panel in scroller
		org.lgna.croquet.views.ScrollPane scrollList = new org.lgna.croquet.views.ScrollPane( snapshotPanel, org.lgna.croquet.views.ScrollPane.VerticalScrollbarPolicy.NEVER, org.lgna.croquet.views.ScrollPane.HorizontalScrollbarPolicy.AS_NEEDED );
		scrollList.setBorder( javax.swing.BorderFactory.createMatteBorder( 1, 1, 2, 1, new java.awt.Color( 177, 177, 192 ) ) );

		addComponent( previewPanel, "cell 0 0, growx" );
		addComponent( composite.getSelectPosterString().createLabel( 1.5f, TextWeight.BOLD ), "cell 0 1, w 715, center" );
		addComponent( scrollList, "cell 0 2, w 715, center" );
	}

	private class SnapshotCellRenderer implements javax.swing.ListCellRenderer<java.awt.Image> {

		@Override
		public java.awt.Component getListCellRendererComponent( javax.swing.JList<? extends java.awt.Image> list, java.awt.Image value, int index, boolean isSelected, boolean cellHasFocus ) {
			java.awt.Image posterPreview = value.getScaledInstance( 224, 126, java.awt.Image.SCALE_DEFAULT );
			javax.swing.JLabel label = new javax.swing.JLabel( new javax.swing.ImageIcon( posterPreview ) );
			if( isSelected ) {
				label.setBorder( javax.swing.BorderFactory.createCompoundBorder( javax.swing.BorderFactory.createEmptyBorder( 5, 5, 5, 5 ), javax.swing.BorderFactory.createMatteBorder( 2, 2, 2, 2, java.awt.Color.RED ) ) );
			} else {
				label.setBorder( javax.swing.BorderFactory.createEmptyBorder( 7, 7, 7, 7 ) );
			}
			return label;
		}
	}

	public org.lgna.croquet.views.List<java.awt.Image> getSnapshotList() {
		return this.snapshotList;
	}
}
