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
package edu.wustl.lookingglass.croquetfx;

import java.awt.Dimension;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import edu.cmu.cs.dennisc.java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.embed.swing.JFXPanel;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/**
 * Legacy swing "croquet" view to add JavaFX components into wrapped swing (i.e.
 * croquet).
 *
 * @author Kyle J. Harms
 */
@Deprecated
public class FxViewAdaptor<C extends FxComponent> extends org.lgna.croquet.views.CompositeView<javafx.embed.swing.JFXPanel, FxCompositeAdaptor<C>> {

	private JFXPanel jfxPanel;

	private C component;

	private javafx.scene.Scene scene;
	private javafx.scene.layout.StackPane stack;
	private Map<Integer, Region> stencils = new HashMap<>();
	private List<Integer> layers = new LinkedList<>();

	private final java.awt.event.ComponentListener resizeListener;

	// Lame FX refresh hack. This helps fx components to show up in croquet world.
	// 100.0 milliseconds should be enough, but it's a hack... so this might need to
	// be adjusted... yes... I'm very ashamed of myself.
	private static final double HACK_FX_REFRESH_TIME = 200.0; // ms
	private boolean useFxRefreshHack = true;

	/**
	 * This is the easiest way to use JavaFx within croquet. Just create an
	 * FxComponent and call getFxViewAdaptor(). You do not need to mess with
	 * composites with this option.
	 *
	 * Note: This must be called from the JavaFX thread.
	 */
	FxViewAdaptor( C component ) {
		this( new FxCompositeAdaptor<C>() );
		this.setComponent( component );
	}

	/**
	 * This constructor is called by the composite. This constructor let's you
	 * use JavaFx in the more croquet way, where you must first have a
	 * composite, and that composite later creates the view.
	 *
	 * Note: This can be called in any thread.
	 */
	FxViewAdaptor( FxCompositeAdaptor<C> composite ) {
		super( composite );
		this.getComposite().setView( this );

		// The JFXPanel only sets it's preferred size once, right after setting the scene.
		// The causes layout problems later if the the size changes. This is a hack to recompute
		// the preferred size every time the size changes. It does however cause a bit of tearing... bummer.
		this.resizeListener = new java.awt.event.ComponentListener() {
			@Override
			public void componentResized( java.awt.event.ComponentEvent e ) {
				FxViewAdaptor.this.recomputePreferredSize();
			}

			@Override
			public void componentMoved( java.awt.event.ComponentEvent e ) {
			}

			@Override
			public void componentShown( java.awt.event.ComponentEvent e ) {
			}

			@Override
			public void componentHidden( java.awt.event.ComponentEvent e ) {
			}
		};
		this.shouldRecomputePreferredSize( true );
	}

	final void setComponent( C component ) {
		assert javafx.application.Platform.isFxApplicationThread();

		this.component = component;

		// This is a little silly... but it's necessary to play well with croquet.
		assert this.component.getRootNode().getScene() == null;

		// Make a "layered pane" for this croquet view
		this.stack = new javafx.scene.layout.StackPane();
		this.stack.getChildren().add( this.component.getRootNode() );

		// croquet-fx refresh hack
		this.component.getRootNode().setVisible( false );

		this.scene = new javafx.scene.Scene( this.stack );
		FxComponent.addDefaultStyle( this.scene );

		if( this.jfxPanel != null ) {
			this.jfxPanel.setScene( this.scene );
		}
	}

	public final javafx.scene.Scene getScene() {
		return this.scene;
	}

	public final C getFxComponent() {
		return this.component;
	}

	private Region addStencil( Integer layerId ) {
		Region stencil = this.stencils.get( layerId );
		if( stencil == null ) {
			stencil = new Region();
			this.layers.add( layerId );
			Collections.sort( this.layers );
			this.stencils.put( layerId, stencil );

			Integer index = this.layers.indexOf( layerId );
			this.stack.getChildren().add( this.stack.getChildren().size() - index, stencil );
		}
		return stencil;
	}

	private void removeStencil( Integer layerId ) {
		Region stencil = this.stencils.get( layerId );
		this.stack.getChildren().remove( stencil );
		this.layers.remove( layerId );
		this.stencils.remove( layerId );
	}

	public void showStencil( Integer layerId, java.awt.Color color ) {
		ThreadHelper.runOnFxThread( () -> {
			Region stencil = addStencil( layerId );
			javafx.scene.paint.Color c = new javafx.scene.paint.Color( color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0, color.getAlpha() / 255.0 );
			stencil.setBackground( new Background( new BackgroundFill( c, CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY ) ) );
			stencil.setVisible( true );
		} );
	}

	public void hideStencil( Integer layerId ) {
		ThreadHelper.runOnFxThread( () -> {
			this.removeStencil( layerId );
		} );
	}

	public void recomputePreferredSize() {
		ThreadHelper.runOnFxThread( () -> {
			if( ( this.component != null ) && ( this.component.getRootNode() != null ) ) {
				Dimension prefSize = new Dimension( (int)this.component.getRootNode().prefWidth( -1 ), (int)this.component.getRootNode().prefHeight( -1 ) );
				ThreadHelper.runOnSwingThread( () -> {
					javafx.embed.swing.JFXPanel jfxPanel = this.getAwtComponent();
					if( jfxPanel != null ) {
						jfxPanel.setPreferredSize( prefSize );
					}
				} );
			}
		} );
	}

	public void shouldRecomputePreferredSize( boolean shouldRecompute ) {
		if( shouldRecompute ) {
			ThreadHelper.runOnSwingThread( () -> {
				if( !java.util.Arrays.asList( this.getAwtComponent().getComponentListeners() ).contains( this.resizeListener ) ) {
					this.addComponentListener( this.resizeListener );
				}
			} );
		} else {
			this.removeComponentListener( this.resizeListener );
		}
	}

	@Override
	public void handleCompositePreActivation() {
		super.handleCompositePreActivation();
		java.util.List<org.lgna.croquet.views.CompositeView<?, ?>> compositeViews = getCompositeViews();
		for( org.lgna.croquet.views.CompositeView<?, ?> view : compositeViews ) {
			org.lgna.croquet.Composite<?> composite = view.getComposite();
			if( composite != null ) {
				composite.handlePreActivation();
			} else {
				view.handleCompositePreActivation();
			}
		}
	}

	@Override
	public void handleCompositePostDeactivation() {
		java.util.List<org.lgna.croquet.views.CompositeView<?, ?>> compositeViews = getCompositeViews();
		for( org.lgna.croquet.views.CompositeView<?, ?> view : compositeViews ) {
			org.lgna.croquet.Composite<?> composite = view.getComposite();
			if( composite != null ) {
				composite.handlePostDeactivation();
			} else {
				view.handleCompositePostDeactivation();
			}
		}
		super.handleCompositePostDeactivation();

	}

	@Override
	protected void handleDisplayable() {
		super.handleDisplayable();

		if( this.useFxRefreshHack ) {
			ThreadHelper.runOnFxThread( () -> {
				this.component.getRootNode().setVisible( true );
				FadeTransition fade = new FadeTransition( Duration.millis( HACK_FX_REFRESH_TIME ), this.component.getRootNode() );
				fade.setFromValue( 0.0 );
				fade.setToValue( 1.0 );
				fade.play();
			} );
		}
	}

	@Override
	protected void handleUndisplayable() {
		super.handleUndisplayable();

		if( this.useFxRefreshHack ) {
			ThreadHelper.runOnFxThread( () -> {
				this.component.getRootNode().setVisible( false );
			} );
		}
	}

	@Override
	protected javafx.embed.swing.JFXPanel createAwtComponent() {
		this.jfxPanel = new javafx.embed.swing.JFXPanel();
		if( this.scene != null ) {
			this.jfxPanel.setScene( this.scene );
		}
		return jfxPanel;
	}

	/*
	 * This really should be only called from the java fx thread. But it'll probably
	 * work most of the time.
	 */
	private java.util.List<org.lgna.croquet.views.CompositeView<?, ?>> getCompositeViews() {
		java.util.List<org.lgna.croquet.views.CompositeView<?, ?>> compositeViews = new java.util.ArrayList<org.lgna.croquet.views.CompositeView<?, ?>>();
		FxViewAdaptor.searchForCompositeViews( this.scene.getRoot(), compositeViews );
		return compositeViews;
	}

	/*
	 * This really should be only called from the java fx thread. But it'll probably
	 * work most of the time.
	 */
	private static void searchForCompositeViews( javafx.scene.Node node, java.util.List<org.lgna.croquet.views.CompositeView<?, ?>> compositeViews ) {
		if( node instanceof javafx.embed.swing.SwingNode ) {
			javax.swing.JComponent jComponent = ( (javafx.embed.swing.SwingNode)node ).getContent();
			org.lgna.croquet.views.AwtComponentView<?> view = org.lgna.croquet.views.AwtComponentView.lookup( jComponent );
			if( view instanceof org.lgna.croquet.views.CompositeView<?, ?> ) {
				compositeViews.add( (org.lgna.croquet.views.CompositeView<?, ?>)view );
			}
		}

		if( node instanceof javafx.scene.Parent ) {
			try {
				for( javafx.scene.Node c : ( (javafx.scene.Parent)node ).getChildrenUnmodifiable() ) {
					searchForCompositeViews( c, compositeViews );
				}
			} catch( NoSuchElementException e ) {
				// Whoops. We weren't in the javafx thread and the scenegraph changed on us!
				Logger.throwable( e, node );
			}
		}
	}

	/**
	 * We cannot control the refresh because of the heavy handed way croquet
	 * composites/views revalidateAndRepaint. So this hack forces a refresh on
	 * the JavaFx scene for several milliseconds right after it's shown. lame...
	 *
	 * Sometimes this hack can cause awful flickering. In those cases the scene
	 * is already being forced to refresh, so you don't need the hack. This will
	 * let you turn it off in those circumstances.
	 */
	@Deprecated
	public void useFxRefreshHack( boolean shouldUseHack ) {
		if( !shouldUseHack ) {
			ThreadHelper.runOnFxThread( () -> {
				this.component.getRootNode().setVisible( true );
			} );
		}
		this.useFxRefreshHack = shouldUseHack;
	}
}
