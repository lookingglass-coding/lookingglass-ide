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
package edu.wustl.lookingglass.ide;

import org.alice.nonfree.NebulousStoryApi;
import org.lgna.croquet.views.ExternalHyperlink;

import edu.wustl.lookingglass.ide.operations.LookingGlassSiteOperation;

public class AboutPane extends javax.swing.JPanel {

	protected static final String LOOKINGGLASS_URL = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAbsoluteUrl( "/" ).toString();

	// DO NOT MODIFY THIS LIST HERE!
	// This list in maintained in the file share and then copied here. Do not modify this list here.
	// Modify the list in fs1.seas.wustl.edu/lookingglass/misc/credits.ods and then copy it here.
	protected String[] creditsList = {
			"Shaurya Ahuja", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Gazihan Alankus", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Aarthi Arunachalam", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Nicole Backart", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Evan Balzuweit", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Adam Basloe", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Gail Burks", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Genevieve Buthod", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Paul Carleton", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Jason Chen", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Mary Chou", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Alexis Chuck", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Danielle Clemons", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Dennis Cosgrove", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Yoanna Dosouto", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Melynda Eden", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Reilly Ellis", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Joe Fiala", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Meir Friedenberg", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Shannon Gray", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Paul Gross", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Kyle Harms", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Micah Herstand", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Atalie Holman", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Michelle Ichinco", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Caitlin Kelleher", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Jordana Kerr", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Kevin Kieselbach", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Daryl Koopersmith", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Terian Koscik", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Amanda Lazar", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Ari Levin", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Michael Liu", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Matt May", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Sara Melnick", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Patrick Nevels", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Julian Ozen", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Kendall Park", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Jeremy Philipp", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Michael Pogran", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Amanda Priscilla Araujo da Silva", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Noah Rowlett", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Mark Santolucito", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Simon Tam", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Emily Yang", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Jennifer Yang", // DO NOT MODIFY. SEE NOTE ABOVE!
			"Aaron Zemach" // DO NOT MODIFY. SEE NOTE ABOVE!
	};

	public AboutPane() {
		setBorder( null );
		setLayout( new net.miginfocom.swing.MigLayout( "", "0[620px,grow,center]0", "0[][][][:300px:,grow]0" ) );

		javax.swing.JLabel logo = new javax.swing.JLabel();
		logo.setAlignmentX( java.awt.Component.CENTER_ALIGNMENT );
		logo.setHorizontalAlignment( javax.swing.SwingConstants.CENTER );
		logo.setIcon( edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "about-logo", org.lgna.croquet.icon.IconSize.FIXED ) );
		add( logo, "cell 0 0" );

		ExternalHyperlink homePageLink = new LookingGlassSiteOperation().createExternalHyperlink();
		homePageLink.getAwtComponent().setToolTipText( "" );
		homePageLink.getAwtComponent().setHorizontalAlignment( javax.swing.SwingConstants.CENTER );
		homePageLink.getAwtComponent().setAlignmentX( java.awt.Component.CENTER_ALIGNMENT );
		add( homePageLink.getAwtComponent(), "cell 0 1" );

		javax.swing.JLabel versionLabel = new javax.swing.JLabel( "Version: " + LookingGlassIDE.APPLICATION_VERSION );
		versionLabel.setAlignmentX( java.awt.Component.CENTER_ALIGNMENT );
		versionLabel.setHorizontalAlignment( javax.swing.SwingConstants.CENTER );
		add( versionLabel, "cell 0 2" );

		javax.swing.JTabbedPane tabbedPane = new javax.swing.JTabbedPane( javax.swing.JTabbedPane.BOTTOM );
		add( tabbedPane, "cell 0 3" );

		javax.swing.JPanel creditsPanel = new javax.swing.JPanel();
		creditsPanel.setBorder( null );
		tabbedPane.addTab( "Credits", null, creditsPanel, null );
		creditsPanel.setLayout( new net.miginfocom.swing.MigLayout( "", "0[24px:n][pref]0", "0[][]0[]0[]0[][][][][]0" ) );

		javax.swing.JLabel supportedLabel = new javax.swing.JLabel( "Supported by:" );
		creditsPanel.add( supportedLabel, "cell 0 0 2 1,alignx left,aligny center" );

		javax.swing.JLabel barnesLabel = new javax.swing.JLabel( "Barnes Jewish Foundation" );
		creditsPanel.add( barnesLabel, "cell 1 1,alignx left,aligny center" );

		javax.swing.JLabel eaLabel = new javax.swing.JLabel( "Electronic Arts" );
		creditsPanel.add( eaLabel, "cell 1 2,alignx left,aligny center" );

		javax.swing.JLabel nsfLabel = new javax.swing.JLabel( "National Science Foundation" );
		creditsPanel.add( nsfLabel, "cell 1 3,alignx left,aligny center" );

		javax.swing.JLabel wustlLabel = new javax.swing.JLabel( "Washington University in St. Louis" );
		creditsPanel.add( wustlLabel, "cell 1 4,alignx left,aligny center" );

		javax.swing.JLabel createdLabel = new javax.swing.JLabel( "Created by:" );
		creditsPanel.add( createdLabel, "cell 0 5 2 1,alignx left,aligny center" );

		StringBuilder credits = new StringBuilder( "<html>" );
		for( String name : this.creditsList ) {
			credits.append( name );
			credits.append( ", " );
		}
		credits.deleteCharAt( credits.length() - 1 );
		credits.deleteCharAt( credits.length() - 1 );
		credits.append( "</html>" );

		javax.swing.JLabel authorsLabel = new javax.swing.JLabel( credits.toString() );
		creditsPanel.add( authorsLabel, "cell 1 6,alignx left,aligny center" );

		javax.swing.JLabel simsLabel = new javax.swing.JLabel( "<html>The Sims<sup>TM</sup> 2 Art Assets donated by Electronic Arts.</html>" );
		creditsPanel.add( simsLabel, "cell 0 7 2 1,alignx left,aligny center" );

		javax.swing.JLabel cmuLabel = new javax.swing.JLabel( "This product includes software developed by Carnegie Mellon University." );
		cmuLabel.setHorizontalAlignment( javax.swing.SwingConstants.CENTER );
		creditsPanel.add( cmuLabel, "cell 0 8 2 1,alignx left,aligny center" );

		javax.swing.JPanel licensePanel = new javax.swing.JPanel();
		tabbedPane.addTab( "Licenses", null, licensePanel, null );
		licensePanel.setLayout( new net.miginfocom.swing.MigLayout( "", "0[grow]0", "0[]0" ) );

		javax.swing.JScrollPane licenseScrollPane = new javax.swing.JScrollPane();
		licenseScrollPane.setVerticalScrollBarPolicy( javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED );
		licenseScrollPane.setViewportBorder( null );
		licenseScrollPane.setHorizontalScrollBarPolicy( javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		licensePanel.add( licenseScrollPane, "cell 0 0" );

		javax.swing.JTextPane licensePane = new javax.swing.JTextPane();
		licensePane.setBorder( null );
		licensePane.setEditable( false );
		licensePane.setText( getLicenseText() );
		licenseScrollPane.setViewportView( licensePane );
		licensePane.setCaretPosition( 0 );
	}

	protected String getLicenseText() {
		return edu.wustl.lookingglass.ide.license.LookingGlassLicense.LICENSE +
				"=======================================================\n\n" +
				NebulousStoryApi.nonfree.getLookingGlassSimsLicense() +
				"=======================================================\n\n" +
				org.lgna.project.License.TEXT;
	}
}
