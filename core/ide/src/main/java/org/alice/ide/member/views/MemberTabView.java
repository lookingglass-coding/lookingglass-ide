/*******************************************************************************
 * Copyright (c) 2006, 2015, Carnegie Mellon University. All rights reserved.
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
 * 3. Products derived from the software may not be called "Alice", nor may
 *    "Alice" appear in their name, without prior written permission of
 *    Carnegie Mellon University.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Carnegie Mellon University"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 * ANY AND ALL EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT
 * SHALL THE AUTHORS, COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.alice.ide.member.views;

import org.alice.ide.member.MemberTabComposite;
import org.lgna.croquet.event.ValueEvent;

import edu.wustl.lookingglass.croquetfx.ThreadHelper;

/**
 * @author Dennis Cosgrove
 */
public abstract class MemberTabView extends org.lgna.croquet.views.MigPanel implements edu.wustl.lookingglass.community.CommunityStatusObserver {
	private final java.util.Map<org.lgna.project.ast.Member, org.lgna.croquet.views.SwingComponentView<?>> map = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();
	private final TitlePanel titlePanel;
	org.lgna.croquet.views.MigPanel membersPanel;
	private final org.lgna.croquet.views.PopupButton addMethodButton;

	public abstract java.awt.Color getTitleColor();

	public MemberTabView( org.alice.ide.member.MemberTabComposite<?> composite ) {
		super( composite, "insets 0, fill", "[]", "[]0[]" );

		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().addObserver( this );

		if( composite.getAddMethodMenuModel() != null ) {
			this.addMethodButton = composite.getAddMethodMenuModel().getPopupPrepModel().createPopupButton();
			this.addMethodButton.setClobberIcon( org.alice.stageide.icons.PlusIconFactory.getInstance().getIcon( new java.awt.Dimension( 16, 16 ) ) );
			this.addMethodButton.setHorizontalTextPosition( org.lgna.croquet.views.HorizontalTextPosition.RIGHT );
			this.addMethodButton.setVerticalTextPosition( org.lgna.croquet.views.VerticalTextPosition.CENTER );
		} else {
			this.addMethodButton = null;
		}
		this.titlePanel = new TitlePanel( composite.getTitleStringState() );
		this.titlePanel.setBackgroundColor( getTitleColor() );

		this.membersPanel = new org.lgna.croquet.views.MigPanel( null, "fill, insets 0", "[]", "[]" );
		this.membersPanel.setBackgroundColor( getBackgroundColor() );

		this.setBorder( javax.swing.BorderFactory.createMatteBorder( 0, 0, 10, 0, getBackgroundColor() ) );

		this.addComponent( this.titlePanel, "growx, wrap" );

		org.alice.ide.member.MethodsSubComposite remixablePanel = getComposite().getRemixableActionsSubComposite();
		if( remixablePanel != null ) {
			remixablePanel.getView().setBackgroundColor( getBackgroundColor() );
			this.addComponent( remixablePanel.getView(), "gaptop 10, wrap" );
		}

		this.addComponent( this.membersPanel, "grow, push" );
		setBackgroundColor( getBackgroundColor() );
	}

	@Override
	public org.alice.ide.member.MemberTabComposite<?> getComposite() {
		return (MemberTabComposite<?>)super.getComposite();
	}

	private static org.lgna.croquet.views.SwingComponentView<?> createDragView( org.lgna.project.ast.Member member ) {
		return new org.lgna.croquet.views.Label( member.getName() );
	}

	protected org.lgna.croquet.views.SwingComponentView<?> getComponentFor( org.lgna.project.ast.Member member ) {
		synchronized( this.map ) {
			org.lgna.croquet.views.SwingComponentView<?> rv = this.map.get( member );
			if( rv != null ) {
				//pass
			} else {
				rv = createDragView( member );
				this.map.put( member, rv );
			}
			return rv;
		}
	}

	@Override
	protected void internalRefresh() {
		super.internalRefresh();
		this.membersPanel.removeAllComponents();

		//<lg/> Add everything to scroll pane. The kids want to click on this because it's so prominent.
		//		if( getComposite().getRemixableActionsSubComposite() != null ) {
		//			org.alice.ide.member.MethodsSubComposite remixableActions = getComposite().getRemixableActionsSubComposite();
		//			if( remixableActions.isShowingDesired() && remixableActions.isRelevant() ) {
		//				org.lgna.croquet.views.ToolPaletteView view = remixableActions.getOuterComposite().getView();
		//
		//				this.membersPanel.addComponent( view, "wrap" );
		//			}
		//		}

		if( ( this.addMethodButton != null ) && getComposite().getAddMethodMenuModel().isRelevant() ) {
			this.membersPanel.addComponent( this.addMethodButton, "align left, wrap" );
		}

		for( org.alice.ide.member.MethodsSubComposite subComposite : getComposite().getSubComposites() ) {
			if( subComposite != org.alice.ide.member.MemberTabComposite.SEPARATOR ) {
				// <lg/> Don't bother adding it if isn't also relevant
				if( subComposite.isShowingDesired() && subComposite.isRelevant() ) {
					org.lgna.croquet.views.ToolPaletteView view = subComposite.getOuterComposite().getView();

					view.getTitle().setInert( true );
					view.getTitle().changeFont( edu.cmu.cs.dennisc.java.awt.font.TextPosture.OBLIQUE );
					view.setBackgroundColor( this.getBackgroundColor() );

					if( subComposite instanceof org.alice.ide.member.UserMethodsSubComposite ) {
						view.getTitle().setSuppressed( ( (org.alice.ide.member.UserMethodsSubComposite)subComposite ).isRelevant() == false );
					}

					membersPanel.addComponent( subComposite.getTitleLabel(), "gapleft 4, wrap" );
					membersPanel.addComponent( subComposite.getOuterComposite().getCoreComposite().getView(), "wrap" );
				}
			} else {
				membersPanel.addComponent( org.lgna.croquet.views.Separator.createInstanceSeparatingTopFromBottom(), "wrap" );
			}
		}
	}

	// <lg/> Need to force refresh for remixable actions if the connection to the internet changes.
	@Override
	public void connectionChanged( edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus status ) {
		ThreadHelper.runOnSwingThread( ( ) -> {
			this.refreshLater();
		} );
	}

	// <lg/> Need to force refresh for remixable actions if the connection to the internet changes.
	@Override
	public void accessChanged( edu.wustl.lookingglass.community.CommunityStatus.AccessStatus status ) {
		ThreadHelper.runOnSwingThread( ( ) -> {
			this.refreshLater();
		} );
	}

	private class TitlePanel extends org.lgna.croquet.views.MigPanel {

		public TitlePanel( org.lgna.croquet.StringState titleStringState ) {
			super( null, "fill, insets 10 10 10 0" );
			org.lgna.croquet.views.Label title = new org.lgna.croquet.views.Label( titleStringState.getValue(), 1.6f, edu.cmu.cs.dennisc.java.awt.font.TextWeight.BOLD );
			title.setHorizontalTextPosition( org.lgna.croquet.views.HorizontalTextPosition.TRAILING );
			title.setForegroundColor( edu.cmu.cs.dennisc.java.awt.ColorUtilities.shiftHSB( MemberTabView.this.getBackgroundColor(), 0, 0, -0.75f ) );
			addComponent( title );

			titleStringState.addNewSchoolValueListener( ( ValueEvent<String> e ) -> {
				javax.swing.SwingUtilities.invokeLater( ( ) -> {
					title.setText( e.getNextValue() );
				} );
			} );

			setBackgroundColor( null );
		}
	}
}
