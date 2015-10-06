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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import org.lgna.croquet.views.ExternalHyperlink;
import org.lgna.croquet.views.FolderTabbedPane;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;

import edu.wustl.lookingglass.ide.perspectives.openproject.OpenProjectTutorialTabSelectionState;
import edu.wustl.lookingglass.ide.perspectives.openproject.TutorialTabComposite;
import edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.WelcomeComposite;

/**
 * @author Michael Pogran
 */
public class WelcomeView extends MigPanel {

	private final MigPanel newVersionPanel;

	public WelcomeView( WelcomeComposite composite ) {
		super( composite, "fill, ins 0", "[grow]", "" );
		this.setBorder( javax.swing.BorderFactory.createLineBorder( new Color( 97, 96, 94 ) ) );

		Label labelLogo = new Label( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "start-text", org.lgna.croquet.icon.IconSize.FIXED ) );
		OpenProjectTutorialTabSelectionState tutorialSelectionState = new OpenProjectTutorialTabSelectionState();
		FolderTabbedPane<TutorialTabComposite> tutorials = tutorialSelectionState.createFolderTabbedPane();
		tutorials.setBackgroundColor( new Color( 211, 215, 240 ) );

		Label newVersionLabel = new Label( "There is a new version of Looking Glass available.", edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );
		newVersionLabel.setBorder( javax.swing.BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

		ExternalHyperlink hyperlink = edu.wustl.lookingglass.ide.croquet.models.community.browseroperation.DownloadBrowserOperation.getInstance().createExternalHyperlink();
		hyperlink.changeFont( edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );

		this.newVersionPanel = new MigPanel( null, "", "[]push[center]push[]", "" );
		newVersionPanel.addComponent( newVersionLabel, "cell 1 0" );
		newVersionPanel.addComponent( hyperlink, "cell 1 0" );
		newVersionPanel.setBackgroundColor( new Color( 181, 187, 228 ) );
		newVersionPanel.setBorder( javax.swing.BorderFactory.createLineBorder( new Color( 151, 160, 217 ) ) );

		this.addComponent( newVersionPanel, "wrap, growx, center, gapbottom 15px, hidemode 3" );
		this.addComponent( labelLogo, "wrap, center" );
		this.addComponent( composite.getCommunityComposite().getView(), "wrap, growx, center" );
		this.addComponent( tutorials, "grow" );

		newVersionPanel.setVisible( false );
	}

	public void setNewVersionAvailableVisible( boolean visible ) {
		synchronized( this.getTreeLock() ) {
			newVersionPanel.setVisible( visible );
		}
	}

	@Override
	protected JPanel createJPanel() {
		return new JPanel() {

			@Override
			protected void paintComponent( Graphics g ) {
				Graphics2D g2 = (Graphics2D)g;
				GradientPaint paint = new GradientPaint( 0, 50, new Color( 241, 245, 255 ), 0, 125, new Color( 211, 215, 240 ) );
				g2.setPaint( paint );
				g2.fillRect( 0, 0, getWidth(), getHeight() );
			}
		};
	}

}
