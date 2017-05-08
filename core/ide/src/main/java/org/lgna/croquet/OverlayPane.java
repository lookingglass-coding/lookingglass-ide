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
package org.lgna.croquet;

import java.awt.GridBagConstraints;

import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;

import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;

public class OverlayPane extends org.lgna.croquet.views.LayerStencil {

	public static final Integer DEFAULT_OVERLAY_LAYER = javax.swing.JLayeredPane.MODAL_LAYER;

	public static final java.awt.Color DEFAULT_OVERLAY_COLOR = new java.awt.Color( 7, 25, 64, 160 );
	public static final java.awt.Color TRANSPARENT_COLOR = new java.awt.Color( 0, 0, 0, 0 );

	public static final OverlayPane newBlockingOverlayPane( org.lgna.croquet.views.AbstractWindow<?> window ) {
		return new Builder( window ).defaultClose( false ).overlayColor( TRANSPARENT_COLOR ).blockInput( true ).build();
	}

	public static class Builder {
		public Builder( org.lgna.croquet.views.AbstractWindow<?> window, org.lgna.croquet.views.SwingComponentView<?> component ) {
			this.window = window;
			this.component = component;
		}

		public Builder( org.lgna.croquet.views.AbstractWindow<?> window ) {
			this( window, null );
		}

		public OverlayPane build() {
			return new OverlayPane( this );
		}

		public Builder blockInput( boolean blockInput ) {
			this.blockInput = blockInput;
			return this;
		}

		public Builder onClickRunnable( Runnable onClickRunnable ) {
			this.onClickRunnable = onClickRunnable;
			return this;
		}

		public Builder closeOperation( Operation closeOperation ) {
			assert this.defaultClose == false : "defaultClose must be false to set closeOperation";
			this.closeOperation = closeOperation;
			return this;
		}

		public Builder defaultClose( boolean defaultClose ) {
			assert this.closeOperation == null : "closeOperation must be null to use default close operation";
			this.defaultClose = defaultClose;
			return this;
		}

		public Builder overlayColor( java.awt.Color overlayColor ) {
			this.overlayColor = overlayColor;
			return this;
		}

		public Builder borderMargin( int borderMargin ) {
			this.borderMargin = borderMargin;
			return this;
		}

		public Builder layerId( int layerId ) {
			this.layerId = layerId;
			return this;
		}

		public Builder drawDefaultBorder( boolean drawDefaultBorder ) {
			this.drawDefaultBorder = drawDefaultBorder;
			return this;
		}

		private final org.lgna.croquet.views.AbstractWindow<?> window;
		private final org.lgna.croquet.views.SwingComponentView<?> component;
		private boolean blockInput = false;
		private Runnable onClickRunnable = null;
		private Operation closeOperation;
		private boolean defaultClose = false;
		private boolean drawDefaultBorder = true;
		private java.awt.Color overlayColor;
		private Integer layerId = DEFAULT_OVERLAY_LAYER;
		private int borderMargin = 5;
	}

	private final org.lgna.croquet.views.AwtComponentView<?> component;

	private java.util.List<edu.wustl.lookingglass.croquetfx.FxViewAdaptor> fxViewAdaptors;
	private java.util.List<org.lgna.stencil.Hole> jfxHoles;

	private java.awt.Color overlayColor;

	private final Runnable onClickRunnable;

	public OverlayPane( Builder builder ) {
		super( builder.window, builder.layerId );

		this.component = builder.component;
		this.overlayColor = builder.overlayColor == null ? DEFAULT_OVERLAY_COLOR : builder.overlayColor;
		this.onClickRunnable = builder.onClickRunnable;

		ThreadHelper.runOnSwingThread( ( ) -> {
			if( builder.blockInput ) {
				this.getAwtComponent().addMouseListener( new java.awt.event.MouseListener() {
					private java.awt.Cursor prevCursor = null;

					@Override
					public void mouseEntered( java.awt.event.MouseEvent e ) {
					}

					@Override
					public void mouseExited( java.awt.event.MouseEvent e ) {
					}

					@Override
					public void mousePressed( java.awt.event.MouseEvent e ) {
						java.awt.Component component = e.getComponent();
						this.prevCursor = component.getCursor();
						component.setCursor( java.awt.dnd.DragSource.DefaultMoveNoDrop );
					}

					@Override
					public void mouseReleased( java.awt.event.MouseEvent e ) {
						java.awt.Component component = e.getComponent();
						component.setCursor( this.prevCursor );
						this.prevCursor = null;
					}

					@Override
					public void mouseClicked( java.awt.event.MouseEvent e ) {
					}
				} );
			}

			if( this.onClickRunnable != null ) {
				this.getAwtComponent().addMouseListener( new java.awt.event.MouseListener() {
					@Override
					public void mouseEntered( java.awt.event.MouseEvent e ) {
					}

					@Override
					public void mouseExited( java.awt.event.MouseEvent e ) {
					}

					@Override
					public void mousePressed( java.awt.event.MouseEvent e ) {
						OverlayPane.this.onClickRunnable.run();
					}

					@Override
					public void mouseReleased( java.awt.event.MouseEvent e ) {
						// Mouse release doesn't happen on drag events, which is what most users do after remixing
						// which results in freezing the interface.
						//						OverlayPane.this.onClickRunnable.run();
					}

					@Override
					public void mouseClicked( java.awt.event.MouseEvent e ) {
					}
				} );
			}

			if( this.component != null ) {
				org.lgna.croquet.views.PageAxisPanel contentPanel = new org.lgna.croquet.views.PageAxisPanel();
				if( builder.drawDefaultBorder ) {
					contentPanel.setBorder( javax.swing.BorderFactory.createCompoundBorder( javax.swing.BorderFactory.createLineBorder( java.awt.Color.BLACK ), javax.swing.BorderFactory.createEmptyBorder( builder.borderMargin, builder.borderMargin, builder.borderMargin, builder.borderMargin ) ) );
				}
				contentPanel.addComponent( component );

				Operation closeOperation = builder.closeOperation;
				if( ( closeOperation == null ) && builder.defaultClose ) {
					closeOperation = new CloseOperation( this );
				}
				if( closeOperation != null ) {
					org.lgna.croquet.views.Separator seperator = org.lgna.croquet.views.Separator.createInstanceSeparatingTopFromBottom();
					seperator.setBorder( javax.swing.BorderFactory.createEmptyBorder( builder.borderMargin, 0, builder.borderMargin, 0 ) );

					org.lgna.croquet.views.LineAxisPanel buttonPanel = new org.lgna.croquet.views.LineAxisPanel( org.lgna.croquet.views.BoxUtilities.createHorizontalGlue(), closeOperation.createButton() );

					contentPanel.addComponent( seperator );
					contentPanel.addComponent( buttonPanel );
				}
				contentPanel.setOpaque( true );

				org.lgna.croquet.views.GridBagPanel stencilPanel = new org.lgna.croquet.views.GridBagPanel();
				stencilPanel.setOpaque( false );
				stencilPanel.addComponent( contentPanel, new GridBagConstraints() );

				// Make sure all of the panel if visible
				javax.swing.JScrollPane jScrollPane = new edu.cmu.cs.dennisc.javax.swing.components.JScrollPaneCoveringLinuxPaintBug( stencilPanel.getAwtComponent() );
				jScrollPane.setOpaque( false );
				jScrollPane.setBorder( javax.swing.BorderFactory.createEmptyBorder() );
				jScrollPane.getViewport().setOpaque( false );
				jScrollPane.setViewportBorder( javax.swing.BorderFactory.createEmptyBorder() );
				jScrollPane.getVerticalScrollBar().setUnitIncrement( 16 );
				jScrollPane.getVerticalScrollBar().setBlockIncrement( 32 );
				jScrollPane.getHorizontalScrollBar().setUnitIncrement( 16 );
				jScrollPane.getHorizontalScrollBar().setBlockIncrement( 32 );
				this.getAwtComponent().add( jScrollPane );
			}
		} );
	}

	public boolean isOverlayShowing() {
		return this.isStencilShowing();
	}

	public void show() {
		ThreadHelper.runOnSwingThread( ( ) -> {
			try {
				this.setStencilShowing( true );
			} catch( NullPointerException e ) {
				Logger.throwable( e, this );
			}
		} );
	}

	public void hide() {
		ThreadHelper.runOnSwingThread( ( ) -> {
			try {
				this.setStencilShowing( false );
			} catch( NullPointerException e ) {
				Logger.throwable( e, this );
			}
		} );
	}

	public java.awt.Color getOverlayColor() {
		return this.overlayColor;
	}

	public void setOverlayColor( java.awt.Color overlayColor ) {
		this.overlayColor = overlayColor;
		ThreadHelper.runOnSwingThread( ( ) -> {
			this.refreshLater();
		} );
	}

	@Override
	protected void paintComponentPrologue( java.awt.Graphics2D g2 ) {
		java.awt.Shape shape = g2.getClip();
		java.awt.geom.Area area = new java.awt.geom.Area( shape );

		// Java Fx Swing Panels flicker. So let's find them and have them draw the stencil themselves.
		for( org.lgna.stencil.Hole hole : this.jfxHoles ) {
			java.awt.geom.Area holeArea = hole.getAreaToSubstractForPaint( this );
			if( holeArea != null ) {
				area.subtract( holeArea );
				shape = area;
			}
		}

		g2.setPaint( overlayColor );
		g2.fill( shape );
	}

	@Override
	protected void paintEpilogue( java.awt.Graphics2D g2 ) {
	}

	@Override
	protected void paintComponentEpilogue( java.awt.Graphics2D g2 ) {
	}

	@Override
	protected boolean contains( int x, int y, boolean superContains ) {
		return superContains;
	}

	@Override
	protected java.awt.LayoutManager createLayoutManager( javax.swing.JPanel jPanel ) {
		return new java.awt.GridLayout();
	}

	@Override
	protected void handleDisplayable() {
		super.handleDisplayable();
		this.handleCompositePreActivation();
	}

	@Override
	public void handleCompositePreActivation() {
		assert javax.swing.SwingUtilities.isEventDispatchThread();
		super.handleCompositePreActivation();

		if( this.component instanceof org.lgna.croquet.views.CompositeView<?, ?> ) {
			org.lgna.croquet.views.CompositeView<?, ?> compositeView = (org.lgna.croquet.views.CompositeView<?, ?>)this.component;
			compositeView.getComposite().handlePreActivation();
		}

		// Find all jfxPanels
		java.util.List<javafx.embed.swing.JFXPanel> jfxPanels = new java.util.LinkedList<>();
		searchForJFXPanel( this.getRoot().getAwtComponent(), jfxPanels );

		this.fxViewAdaptors = new java.util.LinkedList<>();
		this.jfxHoles = new java.util.LinkedList<>();
		for( javafx.embed.swing.JFXPanel jfxPanel : jfxPanels ) {
			org.lgna.croquet.views.AwtComponentView<?> view = org.lgna.croquet.views.AwtComponentView.lookup( jfxPanel );
			if( ( view != null ) && ( view instanceof edu.wustl.lookingglass.croquetfx.FxViewAdaptor ) ) {
				edu.wustl.lookingglass.croquetfx.FxViewAdaptor fxViewAdaptor = (edu.wustl.lookingglass.croquetfx.FxViewAdaptor)view;
				this.fxViewAdaptors.add( fxViewAdaptor );
				org.lgna.stencil.Hole hole = new org.lgna.stencil.Hole( fxViewAdaptor ) {
					@Override
					protected java.awt.Insets getPaintInsets() {
						return new java.awt.Insets( 0, 0, 0, 0 );
					}
				};
				this.jfxHoles.add( hole );
			}
		}

		for( edu.wustl.lookingglass.croquetfx.FxViewAdaptor fxViewAdaptor : this.fxViewAdaptors ) {
			fxViewAdaptor.showStencil( this.getLayerId(), this.overlayColor );
		}
		for( org.lgna.stencil.Hole hole : this.jfxHoles ) {
			hole.bind();
		}
	}

	@Override
	protected void handleUndisplayable() {
		assert javax.swing.SwingUtilities.isEventDispatchThread();

		this.handleCompositePostDeactivation();
		super.handleUndisplayable();
	}

	@Override
	public void handleCompositePostDeactivation() {
		assert javax.swing.SwingUtilities.isEventDispatchThread();

		for( edu.wustl.lookingglass.croquetfx.FxViewAdaptor fxViewAdaptor : this.fxViewAdaptors ) {
			fxViewAdaptor.hideStencil( this.getLayerId() );
		}
		for( org.lgna.stencil.Hole hole : this.jfxHoles ) {
			hole.unbind();
		}

		if( this.component instanceof org.lgna.croquet.views.CompositeView<?, ?> ) {
			org.lgna.croquet.views.CompositeView<?, ?> compositeView = (org.lgna.croquet.views.CompositeView<?, ?>)this.component;
			compositeView.getComposite().handlePostDeactivation();
		}
		super.handleCompositePostDeactivation();

		this.fxViewAdaptors = null;
		this.jfxHoles = null;
	}

	private void searchForJFXPanel( java.awt.Container container, java.util.List<javafx.embed.swing.JFXPanel> jfxPanels ) {
		for( java.awt.Component component : container.getComponents() ) {
			// Do not include the overlay pane in the search.
			if( ( component == this.getAwtComponent() ) || ( ( this.component != null ) && ( component == this.component.getAwtComponent() ) ) ) {
				continue;
			}

			if( component instanceof javafx.embed.swing.JFXPanel ) {
				jfxPanels.add( (javafx.embed.swing.JFXPanel)component );
			} else if( component instanceof java.awt.Container ) {
				// Note: JFXPanel is also a container
				searchForJFXPanel( (java.awt.Container)component, jfxPanels );
			}
		}
	}

	private class CloseOperation extends org.lgna.croquet.Operation {
		private final OverlayPane overlayPane;

		public CloseOperation( OverlayPane overlayPane ) {
			super( org.lgna.croquet.Application.DOCUMENT_UI_GROUP, java.util.UUID.fromString( "de6b5092-c7fd-4730-ac30-e3fd95f1e76b" ) );
			this.overlayPane = overlayPane;
			this.setName( "Close" );
		}

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			overlayPane.hide();
		}
	}
}
