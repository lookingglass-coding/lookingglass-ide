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

import org.alice.ide.uricontent.FileProjectLoader;
import org.lgna.croquet.views.Button;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.List;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.PageAxisPanel;
import org.lgna.croquet.views.ScrollPane;
import org.lgna.croquet.views.TextField;

import edu.cmu.cs.dennisc.java.awt.font.TextWeight;
import edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.ExistingProjectComposite;

/**
 * @author Michael Pogran
 */
public class ExistingProjectView extends PageAxisPanel {

	public ExistingProjectView( ExistingProjectComposite composite ) {
		this.setBackgroundColor( new Color( 211, 215, 240 ) );
		this.setBorder( javax.swing.BorderFactory.createLineBorder( new Color( 97, 96, 94 ) ) );

		List<FileProjectLoader> projectsList = composite.getProjectsState().createList();
		projectsList.setCellRenderer( new OpenProjectContentInfoListCellRenderer() );
		projectsList.setVisibleRowCount( -1 );
		projectsList.setLayoutOrientation( org.lgna.croquet.views.List.LayoutOrientation.HORIZONTAL_WRAP );
		projectsList.setBackgroundColor( null );
		projectsList.enableClickingDefaultButtonOnDoubleClick();

		MigPanel scrollablePanel = new MigPanel( null, "fill", "[][]", "[grow 0]20[grow 0][grow 100]" );
		scrollablePanel.setBackgroundColor( new Color( 211, 215, 240 ) );

		//searchListState
		TextField tagSearchField = composite.getSearchState().createTextField();
		tagSearchField.enableSelectAllWhenFocusGained();
		tagSearchField.updateTextForBlankCondition( "Search For World" );
		Button clearSearchButton = composite.getClearSearchOperation().createButton();
		Button browseButton = composite.getBrowseOperation().createButton();
		org.lgna.croquet.views.LineAxisPanel searchPanel = new org.lgna.croquet.views.LineAxisPanel( tagSearchField, clearSearchButton, browseButton );

		org.lgna.croquet.views.Hyperlink sortName = composite.getSortByNameOperation().createHyperlink( TextWeight.BOLD );
		( (edu.cmu.cs.dennisc.javax.swing.plaf.HyperlinkUI)sortName.getAwtComponent().getUI() ).setDisabledColor( Color.BLACK );

		org.lgna.croquet.views.Hyperlink sortDate = composite.getSortByDateOperation().createHyperlink( TextWeight.BOLD );
		( (edu.cmu.cs.dennisc.javax.swing.plaf.HyperlinkUI)sortDate.getAwtComponent().getUI() ).setDisabledColor( Color.BLACK );

		scrollablePanel.addComponent( searchPanel, "cell 0 0, spanx 2, grow" );
		scrollablePanel.addComponent( new Label( "My Worlds", 1.5f, TextWeight.BOLD ), "cell 0 1, left" );
		scrollablePanel.addComponent( new Label( "sort by:", TextWeight.BOLD ), "cell 1 1, right" );
		scrollablePanel.addComponent( sortDate, "cell 1 1, right" );
		scrollablePanel.addComponent( new Label( "|", TextWeight.BOLD ), "cell 1 1, right" );
		scrollablePanel.addComponent( sortName, "cell 1 1, right, gapright 10" );
		scrollablePanel.addComponent( projectsList, "cell 0 2, spanx 2, grow, hmin 100" );
		ScrollPane scroller = new ScrollPane( scrollablePanel );
		scroller.setBackgroundColor( null );
		this.addComponent( scroller );
	}
}
