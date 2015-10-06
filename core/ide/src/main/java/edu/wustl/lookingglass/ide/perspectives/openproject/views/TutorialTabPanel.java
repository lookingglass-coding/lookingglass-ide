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
package edu.wustl.lookingglass.ide.perspectives.openproject.views;

import java.awt.Color;
import java.awt.Image;

import org.lgna.croquet.SimpleTabComposite;
import org.lgna.croquet.views.FixedAspectRatioPanel;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.LineAxisPanel;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.PlainMultiLineLabel;

import edu.cmu.cs.dennisc.java.awt.font.TextWeight;
import edu.wustl.lookingglass.ide.croquet.models.preview.views.PreviewImagePanel;

public class TutorialTabPanel extends MigPanel {
	private PlainMultiLineLabel description;
	private LineAxisPanel stepPanel;
	private LineAxisPanel videoPanel;
	private int stepNum = 1;

	public TutorialTabPanel( SimpleTabComposite composite ) {
		super( composite, "fill", "[]", "[grow][grow 0][grow]" );
		this.setBackgroundColor( new Color( 151, 160, 217 ) );

		this.description = new PlainMultiLineLabel( "", 1.15f, TextWeight.MEDIUM );
		this.stepPanel = new LineAxisPanel();
		this.videoPanel = new LineAxisPanel( new Label( "Related Tutorial Videos:", 1.0f, TextWeight.BOLD ) );

		org.lgna.croquet.views.ScrollPane stepScroller = new org.lgna.croquet.views.ScrollPane( this.stepPanel );
		stepScroller.setVerticalScrollbarPolicy( org.lgna.croquet.views.ScrollPane.VerticalScrollbarPolicy.NEVER );
		stepScroller.setBorder( javax.swing.BorderFactory.createLineBorder( new Color( 97, 96, 94 ) ) );

		this.addComponent( description, "cell 0 0, grow, gap 5, hmin 55" );
		this.addComponent( this.videoPanel, "cell 0 1, growx" );
		this.addComponent( stepScroller, "cell 0 2, grow" );
	}

	public LineAxisPanel getVideoPanel() {
		return this.videoPanel;
	}

	public void setDescription( String text ) {
		this.description.setText( text );
	}

	public void addTutorialStep( String text, Image image ) {
		MigPanel step = new MigPanel( null, "fill" );
		step.setBackgroundColor( Color.WHITE );
		step.setBorder( javax.swing.BorderFactory.createCompoundBorder( javax.swing.BorderFactory.createLineBorder( new Color( 211, 215, 240 ), 8 ), javax.swing.BorderFactory.createLineBorder( new java.awt.Color( 208, 208, 208 ), 2 ) ) );

		PlainMultiLineLabel textLabel = new PlainMultiLineLabel( "Step " + stepNum + ": " + text, 1.0f, edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );
		PreviewImagePanel stepImage = new PreviewImagePanel();
		textLabel.setBorder( javax.swing.BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		stepImage.setBorder( javax.swing.BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		textLabel.setBackgroundColor( Color.WHITE );
		stepImage.setBackgroundImage( image );

		double ratio = (double)image.getWidth( null ) / (double)image.getHeight( null );
		FixedAspectRatioPanel imageRatioPanel = new FixedAspectRatioPanel( stepImage, ratio );

		StringBuilder constraints = new StringBuilder();
		constraints.append( "w " );
		constraints.append( (int)( image.getWidth( null ) * .5 ) );
		constraints.append( "::" );
		constraints.append( image.getWidth( null ) );
		constraints.append( ", h " );
		constraints.append( (int)( image.getHeight( null ) * .5 ) );
		constraints.append( "::" );
		constraints.append( image.getHeight( null ) );
		step.addComponent( imageRatioPanel, "wrap, grow, shrink, " + constraints.toString() );
		step.addComponent( textLabel, "wrap, grow" );
		stepPanel.addComponent( step );
		stepNum++;
	}
}
