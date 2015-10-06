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

import org.lgna.croquet.views.Button;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;

import edu.wustl.lookingglass.ide.perspectives.dinah.TimeScrubComposite;

/**
 * @author Michael Pogran
 */
public class TimeScrubView extends MigPanel {

	private Label playLabel;

	public void setPlayButtonText( String text ) {
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnSwingThread( ( ) -> this.playLabel.setText( text ) );
	}

	public TimeScrubView( TimeScrubComposite composite ) {
		super( composite, "fill, insets 0", "", "5[]5[]" );

		Button playButton = composite.getPlayOperation().createButton();
		Button fastForwardButton = composite.getFastForwardOperation().createButton();
		Button restartButton = composite.getRestartOperation().createButton();

		this.playLabel = new org.lgna.croquet.views.Label( "Pause" );

		MigPanel playButtonGroup = new MigPanel( null, "insets 0", "[]8[]8[]", "[]2[]" );
		playButtonGroup.addComponent( playButton, "cell 0 0, center" );
		playButtonGroup.addComponent( fastForwardButton, "cell 1 0, center" );
		playButtonGroup.addComponent( restartButton, "cell 2 0, center" );
		playButtonGroup.addComponent( this.playLabel, "cell 0 1, center" );
		playButtonGroup.addComponent( composite.getFastForwardLabel(), "cell 1 1, center" );
		playButtonGroup.addComponent( composite.getRestartLabel(), "cell 2 1, center" );

		this.setBorder( javax.swing.BorderFactory.createMatteBorder( 1, 0, 0, 0, java.awt.Color.GRAY ) );

		this.addComponent( playButtonGroup, "cell 0 0, center" );
	}
}
