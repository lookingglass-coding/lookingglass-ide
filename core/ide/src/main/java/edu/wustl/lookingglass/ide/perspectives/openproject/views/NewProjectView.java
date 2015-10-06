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

import org.alice.ide.uricontent.UriProjectLoader;
import org.lgna.croquet.StringState;
import org.lgna.croquet.views.Button;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.List;
import org.lgna.croquet.views.List.LayoutOrientation;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.PageAxisPanel;
import org.lgna.croquet.views.ScrollPane;
import org.lgna.croquet.views.TextArea;
import org.lgna.croquet.views.TextField;

import edu.cmu.cs.dennisc.java.awt.font.TextPosture;
import edu.cmu.cs.dennisc.java.awt.font.TextWeight;
import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.ide.croquet.components.SearchableList;
import edu.wustl.lookingglass.ide.croquet.models.community.CommunityLoginOperation;
import edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.NewProjectComposite;

/**
 * @author Caitlin Kelleher
 */
public class NewProjectView extends PageAxisPanel {

	private MigPanel loginView;
	private List<UriProjectLoader> bookmarkedList;
	private Label errorLabel;
	private MigPanel communityContentPanel;
	private Label searchErrorLabel;

	public NewProjectView( NewProjectComposite composite ) {
		super( composite );
		this.loginView = new MigPanel();

		StringState userNameStringState = CommunityLoginOperation.getInstance().getUserNameStringState();
		StringState passwordStringState = CommunityLoginOperation.getInstance().getPasswordStringState();

		errorLabel = new Label();
		errorLabel.setForegroundColor( Color.RED );

		searchErrorLabel = new Label( "Error downloading, please retry" );
		searchErrorLabel.setForegroundColor( Color.RED );
		searchErrorLabel.setVisible( false );

		loginView.addComponent( errorLabel, "wrap, span 5" );
		loginView.addComponent( new Label( "Please login to view your bookmarks.", TextPosture.OBLIQUE ), "span 5, wrap" );

		TextField userNameField = userNameStringState.createTextField();
		org.lgna.croquet.views.PasswordField passwordField = passwordStringState.createPasswordField();

		userNameField.ignoreEnterKeyEvents();
		passwordField.ignoreEnterKeyEvents();

		loginView.addComponent( userNameStringState.getSidekickLabel().createLabel() );
		loginView.addComponent( userNameField, "width 100" );

		loginView.addComponent( passwordStringState.getSidekickLabel().createLabel() );
		loginView.addComponent( passwordField, "width 100" );

		loginView.addComponent( CommunityLoginOperation.getInstance().createButton() );

		this.setBackgroundColor( new Color( 211, 215, 240 ) );
		this.setBorder( javax.swing.BorderFactory.createLineBorder( new Color( 97, 96, 94 ) ) );

		TextField tagSearchField = composite.getTagSearchState().createTextField();
		tagSearchField.enableSelectAllWhenFocusGained();
		tagSearchField.updateTextForBlankCondition( "Search For Template" );
		tagSearchField.ignoreEnterKeyEvents();

		Button clearSearchButton = composite.getClearOperation().createButton();
		Button refreshButton = composite.getRefreshOperation().createButton();

		org.lgna.croquet.views.LineAxisPanel searchPanel = new org.lgna.croquet.views.LineAxisPanel( tagSearchField, clearSearchButton, refreshButton );

		bookmarkedList = (List<UriProjectLoader>)composite.getBookmarkedTemplatesState().createList();
		bookmarkedList.setCellRenderer( new OpenProjectContentInfoListCellRenderer() );
		bookmarkedList.setLayoutOrientation( LayoutOrientation.HORIZONTAL_WRAP );
		bookmarkedList.setVisibleRowCount( -1 );
		bookmarkedList.setBackgroundColor( null );
		bookmarkedList.enableClickingDefaultButtonOnDoubleClick();

		List<UriProjectLoader> topList = new SearchableList( composite.getTopTemplatesState() );
		topList.setCellRenderer( new OpenProjectContentInfoListCellRenderer() );
		topList.setLayoutOrientation( LayoutOrientation.HORIZONTAL_WRAP );
		topList.setVisibleRowCount( -1 );
		topList.setBackgroundColor( null );
		topList.enableClickingDefaultButtonOnDoubleClick();

		List<UriProjectLoader> blankList = (List<UriProjectLoader>)composite.getBlankTemplatesState().createList();
		blankList.setCellRenderer( new OpenProjectContentInfoListCellRenderer() );
		blankList.setLayoutOrientation( LayoutOrientation.HORIZONTAL_WRAP );
		blankList.setVisibleRowCount( -1 );
		blankList.setBackgroundColor( null );
		blankList.enableClickingDefaultButtonOnDoubleClick();

		Button nextPageButton = composite.getNextPageOperation().createButton();
		Button prevPageButton = composite.getPreviousPageOperation().createButton();
		TextArea pageNumber = composite.getPageNumberState().createTextArea();
		pageNumber.setEditable( false );
		pageNumber.getAwtComponent().setFocusable( false );
		pageNumber.setBackgroundColor( null );
		pageNumber.setBorder( javax.swing.BorderFactory.createEmptyBorder() );
		edu.cmu.cs.dennisc.java.awt.font.FontUtilities.setFontToDerivedFont( pageNumber.getAwtComponent(), TextWeight.BOLD );

		MigPanel paginationPanel = new MigPanel( null, "fill, ins 2", "[grow][][grow]", "[]" );
		paginationPanel.setBackgroundColor( new Color( 198, 202, 225 ) );
		paginationPanel.setBorder( javax.swing.BorderFactory.createLineBorder( new java.awt.Color( 180, 184, 205 ), 2 ) );

		paginationPanel.addComponent( prevPageButton, "cell 0 0, left" );
		paginationPanel.addComponent( pageNumber, "cell 1 0" );
		paginationPanel.addComponent( nextPageButton, "cell 2 0, right" );

		communityContentPanel = new MigPanel( null, "fill", "[]", "[][][][][][]" );

		communityContentPanel.addComponent( searchPanel, "cell 0 0, grow" );
		communityContentPanel.addComponent( new Label( "Community Templates", 1.5f, TextWeight.BOLD ), "cell 0 1, grow" );
		communityContentPanel.addComponent( searchErrorLabel, "cell 0 1, hidemode 2" );
		communityContentPanel.addComponent( topList, "cell 0 2, hmin 100, grow" );
		communityContentPanel.addComponent( paginationPanel, "cell 0 3, grow" );
		communityContentPanel.addComponent( new Label( "Bookmarked Templates", 1.5f, TextWeight.BOLD ), "cell 0 4, gaptop 20, grow" );
		communityContentPanel.addComponent( loginView, "cell 0 5, hidemode 3, grow" );
		communityContentPanel.addComponent( bookmarkedList, "cell 0 5, hmin 100, hidemode 3, grow" );

		MigPanel scrollablePanel = new MigPanel( null, "fill" );
		scrollablePanel.setBackgroundColor( new Color( 211, 215, 240 ) );

		scrollablePanel.addComponent( communityContentPanel, "grow, wrap, hidemode 3" );
		scrollablePanel.addComponent( new Label( "Blank Templates", 1.5f, TextWeight.BOLD ), "grow, wrap, gaptop 20" );
		scrollablePanel.addComponent( blankList, "grow, wrap, hmin 100" );

		boolean loggedIn = ( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAccessStatus() != AccessStatus.NONE ) && ( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAccessStatus() == AccessStatus.USER_ACCESS );
		loginView.setVisible( !loggedIn );
		bookmarkedList.setVisible( loggedIn );

		ScrollPane scroller = new ScrollPane( scrollablePanel );
		scroller.setBackgroundColor( null );
		this.addComponent( scroller );
	}

	@Override
	public NewProjectComposite getComposite() {
		return (NewProjectComposite)super.getComposite();
	}

	public void setLoginViewVisible( boolean visible ) {
		synchronized( this.getTreeLock() ) {
			loginView.setVisible( visible );
			bookmarkedList.setVisible( !visible );
			if( visible ) {
				errorLabel.setText( "" );
			}
		}
	}

	public void setSearchErrorLabelVisible( boolean value ) {
		synchronized( this.getTreeLock() ) {
			this.searchErrorLabel.setVisible( value );
		}
	}

	public void updateLoginError( String message ) {
		errorLabel.setText( message );
	}

	public void setCommunityContentVisible( boolean visible ) {
		synchronized( this.getTreeLock() ) {
			communityContentPanel.setVisible( visible );
		}
	}
}
