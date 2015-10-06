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
package edu.wustl.lookingglass.croquetfx.components;

import java.util.concurrent.locks.ReentrantLock;

import javafx.fxml.FXML;
import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;

/**
 * @author Kyle J. Harms
 */
public class PopOver extends FxComponent {

	private final org.controlsfx.control.PopOver popOver;

	private ReentrantLock overlayLock = new ReentrantLock();
	private org.lgna.croquet.OverlayPane blockingInputPane;
	private org.lgna.croquet.OverlayPane closeOnClickPane;

	@FXML private javafx.scene.layout.VBox contentBox;
	@FXML private javafx.scene.layout.HBox closeBox;
	@FXML private javafx.scene.control.Button close;

	public PopOver() {
		super( PopOver.class );

		this.popOver = new org.controlsfx.control.PopOver( this.getRootNode() );
		this.popOver.detachableProperty().set( false );
		this.popOver.hideOnEscapeProperty().set( false );

		this.popOver.cornerRadiusProperty().set( 0 );
		// https://bitbucket.org/controlsfx/controlsfx/issue/155/custom-popover
		this.popOver.skinProperty().addListener( evt -> {
			javafx.scene.layout.StackPane stackPane = (javafx.scene.layout.StackPane)this.popOver.getSkin().getNode();
			stackPane.getStylesheets().add( PopOver.class.getResource( PopOver.class.getSimpleName() + ".css" ).toExternalForm() );

			// TODO: Add an object property to allow programmers to customize this popover when they want...
		} );

		this.register( this.close, this::handleCloseAction );
	}

	public PopOver( javafx.scene.Node node ) {
		this();
		this.contentNode.set( node );
	}

	public PopOver( edu.wustl.lookingglass.croquetfx.FxComponent fxScene ) {
		this( fxScene.getRootNode() );
	}

	public org.controlsfx.control.PopOver getPopOver() {
		return this.popOver;
	}

	private final javafx.beans.property.ObjectProperty<javafx.scene.Node> contentNode = new javafx.beans.property.SimpleObjectProperty<javafx.scene.Node>( this, "contentNodeProperty" ) {
		@Override
		protected void invalidated() {
			PopOver.this.contentBox.getChildren().add( this.get() );
			this.get().maxWidth( Double.MAX_VALUE );
			javafx.scene.layout.VBox.setVgrow( this.get(), javafx.scene.layout.Priority.ALWAYS );
		};
	};

	public final javafx.scene.Node getContentNode() {
		return this.contentNode.get();
	}

	public final void setContentNode( javafx.scene.Node node ) {
		this.contentNode.set( node );
	}

	public final javafx.beans.property.ObjectProperty<javafx.scene.Node> contentNodeProperty() {
		return this.contentNode;
	}

	public void show( javafx.scene.Node owner ) {
		if( !this.popOver.isShowing() ) {
			ThreadHelper.runOnSwingThread( ( ) -> {
				this.overlayLock.lock();
				try {
					if( this.blockingInput.get() ) {
						org.lgna.croquet.views.AbstractWindow<?> window = edu.wustl.lookingglass.croquetfx.FxComponent.getCroquetWindow( owner );
						if( window != null ) {
							this.destoryBlockInputPane();
							this.blockingInputPane = org.lgna.croquet.OverlayPane.newBlockingOverlayPane( window );
							this.blockingInputPane.show();
						}
					}
					if( this.closeOnClick.get() ) {
						org.lgna.croquet.views.AbstractWindow<?> window = edu.wustl.lookingglass.croquetfx.FxComponent.getCroquetWindow( owner );
						if( window != null ) {
							this.destroyCloseOnClickPane();
							this.closeOnClickPane = new org.lgna.croquet.OverlayPane.Builder( window )
									.defaultClose( false )
									.overlayColor( org.lgna.croquet.OverlayPane.TRANSPARENT_COLOR )
									.onClickRunnable( ( ) -> this.hide() )
									.build();
							this.closeOnClickPane.show();
						}
					}
				} finally {
					this.overlayLock.unlock();
				}

				edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( ( ) -> {
					this.popOver.show( owner );
				} );
			} );
		}
	}

	private static final int LEGACY_LAYER_ID = javax.swing.JLayeredPane.MODAL_LAYER + 1;
	private javax.swing.JLayeredPane legacyLayeredPane;

	@Deprecated
	public void show( org.lgna.croquet.views.SwingComponentView<?> component ) {
		ThreadHelper.runOnSwingThread( ( ) -> {
			org.lgna.croquet.views.AbstractWindow<?> window = component.getRoot();

			// Make a fake javafx node to put ontop of the actual swing component so we can
			// give a popover a node to draw.
			java.awt.Rectangle componentBounds = component.getVisibleRectangle( window.getRootPane() );
			javafx.embed.swing.JFXPanel componentPanel = new javafx.embed.swing.JFXPanel() {
				@Override
				public boolean contains( int x, int y ) {
					return false;
				}
			};
			componentPanel.setLocation( componentBounds.getLocation() );
			componentPanel.setSize( componentBounds.getSize() );

			this.legacyLayeredPane = window.getRootPane().getLayeredPane().getAwtComponent();
			for( java.awt.Component c : this.legacyLayeredPane.getComponentsInLayer( LEGACY_LAYER_ID ) ) {
				this.legacyLayeredPane.remove( c );
			}
			this.legacyLayeredPane.add( componentPanel, LEGACY_LAYER_ID );

			edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( ( ) -> {
				javafx.scene.layout.Region fxComponentNode = new javafx.scene.layout.Region();
				fxComponentNode.setMinSize( javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE );
				fxComponentNode.setMaxSize( javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE );
				fxComponentNode.setPrefSize( componentBounds.getWidth(), componentBounds.getHeight() );
				fxComponentNode.setOpacity( 0.0 );
				javafx.scene.Scene scene = new javafx.scene.Scene( fxComponentNode, javafx.scene.paint.Color.TRANSPARENT );
				componentPanel.setScene( scene );

				this.show( fxComponentNode );
			} );
		} );
	}

	public void hide() {
		this.overlayLock.lock();
		try {
			// If legacy shown, cleanup after ourselves...
			ThreadHelper.runOnSwingThread( ( ) -> {
				if( this.legacyLayeredPane != null ) {
					for( java.awt.Component c : legacyLayeredPane.getComponentsInLayer( LEGACY_LAYER_ID ) ) {
						legacyLayeredPane.remove( c );
					}
					this.legacyLayeredPane = null;
				}
			} );

			edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnFxThread( ( ) -> {
				this.popOver.hide();
				popOver.detach();
			} );

			this.destoryBlockInputPane();

			this.destroyCloseOnClickPane();
		} finally {
			this.overlayLock.unlock();
		}
	}

	private void destoryBlockInputPane() {
		this.overlayLock.lock();
		try {
			if( this.blockingInputPane != null ) {
				this.blockingInputPane.hide();
				this.blockingInputPane = null;
			}
		} finally {
			this.overlayLock.unlock();
		}
	}

	private void destroyCloseOnClickPane() {
		this.overlayLock.lock();
		try {
			if( closeOnClickPane != null ) {
				this.closeOnClickPane.hide();
				this.closeOnClickPane = null;
			}
		} finally {
			this.overlayLock.unlock();
		}
	}

	private final javafx.beans.property.BooleanProperty closeOnClick = new javafx.beans.property.SimpleBooleanProperty( true, "closeOnClick" ) {
		@Override
		protected void invalidated() {
			if( blockingInput.get() ) {
				set( false );
				throw new IllegalStateException( "blockingInput already set" );
			}
		};
	};

	public final boolean isCloseOnClick() {
		return this.closeOnClick.get();
	}

	public final void setCloseOnClick( boolean blockInput ) {
		this.closeOnClick.set( blockInput );
	}

	public final javafx.beans.property.BooleanProperty closeOnClickProperty() {
		return this.closeOnClick;
	}

	private final javafx.beans.property.BooleanProperty blockingInput = new javafx.beans.property.SimpleBooleanProperty( true, "blockingInput" ) {
		@Override
		protected void invalidated() {
			if( closeOnClick.get() ) {
				set( false );
				throw new IllegalStateException( "closeOnClick already set" );
			}
		};
	};

	public final boolean isBlockingInput() {
		return this.blockingInput.get();
	}

	public final void setBlockingInput( boolean blockInput ) {
		this.blockingInput.set( blockInput );
	}

	public final javafx.beans.property.BooleanProperty blockingInputProperty() {
		return this.blockingInput;
	}

	private final javafx.beans.property.BooleanProperty closeVisible = new javafx.beans.property.SimpleBooleanProperty( true, "closeVisible" ) {
		@Override
		protected void invalidated() {
			PopOver.this.closeBox.setVisible( get() );
		};
	};

	public final boolean isCloseVisible() {
		return this.closeVisible.get();
	}

	public final void setCloseVisible( boolean isVisible ) {
		this.closeVisible.set( isVisible );
	}

	public final javafx.beans.property.BooleanProperty closeVisibleProperty() {
		return this.closeVisible;
	}

	private void handleCloseAction( javafx.event.ActionEvent event ) {
		this.hide();
	}
}
