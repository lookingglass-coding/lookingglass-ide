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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;

import org.alice.ide.uricontent.GetContentObserver;
import org.lgna.croquet.views.FixedAspectRatioPanel;
import org.lgna.croquet.views.HtmlView;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.PageAxisPanel;
import org.lgna.croquet.views.PlainMultiLineLabel;

import edu.cmu.cs.dennisc.java.awt.font.TextWeight;
import edu.wustl.lookingglass.ide.croquet.models.community.RecentActivityItem;
import edu.wustl.lookingglass.ide.croquet.models.community.data.RecentActivityData;
import edu.wustl.lookingglass.ide.croquet.models.preview.views.PreviewImagePanel;
import edu.wustl.lookingglass.ide.perspectives.openproject.OpenProjectDetailWelcomeComposite;
import edu.wustl.lookingglass.ide.uricontent.CommunityProjectLoader;
import edu.wustl.lookingglass.ide.views.SpinningProgressDial;

public class WelcomeDetailPanel extends MigPanel {
	private final PlainMultiLineLabel titleLabel;
	private final Label createdByLabel;
	private final PageAxisPanel activityPanel;
	private final MigPanel content;
	private final SpinningProgressDial spinner;

	private int previewWorldHeight;
	private MigPanel previewPanel;

	public WelcomeDetailPanel( org.lgna.croquet.views.AwtComponentView<?> previewComponent, OpenProjectDetailWelcomeComposite composite ) {
		super( composite, "fill, ins 0" );
		this.activityPanel = new PageAxisPanel();

		content = new MigPanel( composite, "fill, ins 0", "[]", "[grow]5[grow 0]5[grow 0]" );

		titleLabel = new PlainMultiLineLabel( "", 1.5f, TextWeight.BOLD );
		titleLabel.setBackgroundColor( null );
		createdByLabel = new Label( "", 1.2f, TextWeight.REGULAR );

		previewPanel = new MigPanel( null, "fill", "[]", "[grow]10[grow 0]5[grow 0]" );
		previewPanel.addComponent( previewComponent, "cell 0 0, grow" );
		previewPanel.addComponent( titleLabel, "cell 0 1, grow x" );
		previewPanel.addComponent( createdByLabel, "cell 0 2, grow x" );
		previewPanel.setBackgroundColor( java.awt.Color.WHITE );
		previewPanel.setBorder( BorderFactory.createLineBorder( new java.awt.Color( 208, 208, 208 ), 2 ) );

		content.addComponent( previewPanel, "cell 0 0, top, center, grow" );
		content.addComponent( new Label( "Recent Activity", 1.35f, TextWeight.BOLD ), "cell 0 1, growx" );

		content.addComponent( activityPanel, "cell 0 2, grow x" );
		content.setVisible( false );

		spinner = new edu.wustl.lookingglass.ide.views.SpinningProgressDial();
		spinner.setActuallyPainting( true );
		spinner.getAwtComponent().setPreferredSize( new Dimension( 100, 100 ) );

		this.addComponent( spinner, "hidemode 3, center" );
		this.addComponent( content, "hidemode 3, grow" );

		this.addComponentListener( new ComponentListener() {

			@Override
			public void componentShown( ComponentEvent e ) {
			}

			@Override
			public void componentResized( ComponentEvent e ) {
				updateFeaturedWorldSize( WelcomeDetailPanel.this.getWidth() );
				updateRecentActivity( ( (OpenProjectDetailWelcomeComposite)getComposite() ).getRecentActivityData() );
			}

			@Override
			public void componentMoved( ComponentEvent e ) {
			}

			@Override
			public void componentHidden( ComponentEvent e ) {
			}
		} );
	}

	public void updateFeaturedWorldSize( int panelWidth ) {
		previewWorldHeight = (int)( panelWidth * ( 9.0 / 16.0 ) ) + titleLabel.getHeight() + createdByLabel.getHeight() + 25; // 25 for margins
		synchronized( getTreeLock() ) {
			previewPanel.getAwtComponent().setMaximumSize( new Dimension( panelWidth, previewWorldHeight ) );
			revalidateAndRepaint();
		}
	}

	public void updateFeaturedWorld( CommunityProjectLoader loader ) {
		if( loader != null ) {
			synchronized( this.getTreeLock() ) {
				titleLabel.setText( loader.getTitle() );
				this.createdByLabel.setText( "created by " + loader.getUsername() );
				OpenProjectDetailWelcomeComposite composite = (OpenProjectDetailWelcomeComposite)this.getComposite();
				composite.setFeaturedWorldInitialized( true );
			}
		}
		updateFeaturedWorldSize( getWidth() );
		this.updateContent();
	}

	public void setSpinnerVisible( boolean visible ) {
		synchronized( this.getTreeLock() ) {
			this.spinner.setVisible( visible );
		}
	}

	public void updateRecentActivity( RecentActivityData data ) {
		if( previewWorldHeight == 0 ) {
			updateFeaturedWorldSize( getWidth() );
		}
		int estimatedHeight = 0;
		synchronized( this.getTreeLock() ) {
			this.activityPanel.forgetAndRemoveAllComponents();
			for( RecentActivityItem item : data ) {
				MigPanel panel = createActivityItemPanel( item );
				estimatedHeight += 65;
				if( estimatedHeight < ( getHeight() - previewWorldHeight ) ) {
					this.activityPanel.addComponent( panel );
				} else {
					break;
				}
			}
			OpenProjectDetailWelcomeComposite composite = (OpenProjectDetailWelcomeComposite)this.getComposite();
			composite.setRecentActivityInitialized( true );
		}
		this.updateContent();
	}

	private synchronized void updateContent() {
		OpenProjectDetailWelcomeComposite composite = (OpenProjectDetailWelcomeComposite)this.getComposite();
		synchronized( this.getTreeLock() ) {
			this.revalidateAndRepaint();
			if( composite.isFeaturedWorldInitialized() && composite.isRecentActivityInitialized() ) {
				this.spinner.setActuallyPainting( false );
				this.spinner.setVisible( false );
				this.content.setVisible( true );
			}
		}
	}

	private MigPanel createActivityItemPanel( RecentActivityItem item ) {
		MigPanel panel = new MigPanel( null, "insets 5", "[50!]5[]", "[25:50:50]" );
		HtmlView activityView = new HtmlView();

		javax.swing.text.html.StyleSheet styles = activityView.getHtmlDocument().getStyleSheet();
		styles.addRule( "body {background-color: rgb(211, 215, 240);}" );
		styles.addRule( "a {color: rgb(0, 31, 181); text-decoration: none;}" );

		activityView.setText( item.generateHtml() );
		activityView.setBorder( javax.swing.BorderFactory.createEmptyBorder() );

		PreviewImagePanel thumbnailPanel = new PreviewImagePanel();

		java.awt.Image thumbnail = item.getThumbnail( new ActivityRefreshObserver( thumbnailPanel ) );
		if( thumbnail == null ) {
			thumbnail = new BufferedImage( 100, 100, BufferedImage.TYPE_INT_RGB );
			java.awt.Graphics g = thumbnail.getGraphics();
			drawNotAvailableImage( g, thumbnail.getWidth( null ), thumbnail.getHeight( null ) );
		}

		thumbnailPanel.setBackgroundImage( thumbnail );

		panel.addComponent( new FixedAspectRatioPanel( thumbnailPanel, 1.0 ), "cell 0 0, grow" );
		panel.addComponent( activityView, "cell 1 0, grow, shrink" );

		panel.getAwtComponent().setMaximumSize( new Dimension( getWidth(), 60 ) );

		return panel;
	}

	private void drawNotAvailableImage( java.awt.Graphics g, int width, int height ) {
		( (java.awt.Graphics2D)g ).setRenderingHint( java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		( (java.awt.Graphics2D)g ).setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

		g.setColor( java.awt.Color.DARK_GRAY );
		g.fillRect( 0, 0, width, height );
		g.setColor( java.awt.Color.LIGHT_GRAY );
		g.dispose();
	}

	private class ActivityRefreshObserver implements GetContentObserver<Image> {
		private final PreviewImagePanel imagePanel;

		public ActivityRefreshObserver( PreviewImagePanel imagePanel ) {
			this.imagePanel = imagePanel;
		}

		@Override
		public void workStarted() {
		}

		@Override
		public void workEnded() {
		}

		@Override
		public void completed( Image content ) {
			javax.swing.SwingUtilities.invokeLater( ( ) -> {
				this.imagePanel.setBackgroundImage( content );
			} );
		}

		@Override
		public void failed( Throwable t ) {
		}

	}

}
