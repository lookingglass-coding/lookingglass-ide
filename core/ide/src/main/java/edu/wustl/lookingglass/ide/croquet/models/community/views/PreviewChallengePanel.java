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
package edu.wustl.lookingglass.ide.croquet.models.community.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JTextArea;

import org.lgna.croquet.views.BorderPanel;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;

import edu.cmu.cs.dennisc.image.ImageUtilities;

public class PreviewChallengePanel extends MigPanel {
	private Label titleLabel = null;
	private Label snapshotPanel = new Label();
	private JTextArea challengeDirectionsTextArea = null;
	private BorderPanel challengeSnapshotPanel = null;

	public PreviewChallengePanel() { //ChallengesTabSelectionState.getInstance().getSelectedItem();
		super( null, "", "[grow]", "[][grow][][]" );

		this.setBackgroundColor( Color.WHITE );

		MigPanel worldTitlePanel = new MigPanel( null, "", "0[grow]0[]0[]0", "0[]0" );
		worldTitlePanel.getAwtComponent().setOpaque( false );
		this.addComponent( worldTitlePanel, "cell 0 0,grow" );

		titleLabel = new Label();
		titleLabel.setFont( titleLabel.getFont().deriveFont( titleLabel.getFont().getStyle() | Font.BOLD, 20f ) );
		worldTitlePanel.addComponent( titleLabel, "cell 0 0" );

		challengeSnapshotPanel = new BorderPanel();
		challengeSnapshotPanel.addCenterComponent( snapshotPanel );
		this.addComponent( challengeSnapshotPanel, "cell 0 1,grow" );

		Label promptLabel = new Label( "Description" );
		promptLabel.setFont( promptLabel.getFont().deriveFont( Font.BOLD ) );
		challengeDirectionsTextArea = new JTextArea();
		challengeDirectionsTextArea.setEditable( false );
		challengeDirectionsTextArea.setHighlighter( null );
		challengeDirectionsTextArea.setLineWrap( true );
		challengeDirectionsTextArea.setWrapStyleWord( true );
		challengeDirectionsTextArea.setAutoscrolls( true );
		challengeDirectionsTextArea.setBorder( null );
		this.addComponent( promptLabel, "cell 0 2, grow" );
		this.getAwtComponent().add( challengeDirectionsTextArea, "cell 0 3,grow, hmin 150" );

	}

	public void updateTitleAndDescription( String title, String description ) {
		titleLabel.setText( title );
		challengeDirectionsTextArea.setText( description );
		this.revalidateAndRepaint();
	}

	public void updateSnapshot( Image snapshot ) {
		if( !( snapshot instanceof BufferedImage ) ) {
			snapshot = ImageUtilities.createBufferedImage( snapshot, BufferedImage.TYPE_INT_RGB );

		}

		this.snapshotPanel.setIcon( new edu.cmu.cs.dennisc.javax.swing.icons.ScaledIcon( new ImageIcon( snapshot ), 435, 245 ) );
	}
}
