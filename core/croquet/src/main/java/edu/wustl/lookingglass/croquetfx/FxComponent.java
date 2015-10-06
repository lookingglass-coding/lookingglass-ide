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

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import org.lgna.croquet.Operation;

import edu.wustl.lookingglass.croquetfx.exceptions.FxInvalidException;
import edu.wustl.lookingglass.croquetfx.models.FxEventPropertyModel;
import edu.wustl.lookingglass.croquetfx.models.FxPropertyModel;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Croquet "hook" to use JavaFX. This is not a wrapper for JavaFX like Croquet
 * is a wrapper for Swing. This is a hook to tie into the transaction framework
 * of Croquet using JavaFX components and their events.
 *
 * This new setup is designed to be used the JavaFX Scene Builder. It supports
 * custom components. So there is very little reason to program (in Java) your
 * interface. Instead use the Scene Builder and extend this class to add the
 * application logic. This will make for faster development and easier
 * maintenance.
 *
 * @author Kyle J. Harms
 */
public class FxComponent implements javafx.fxml.Initializable {

	public static final String DEFAULT_L10N_RESOURCE_PATH = "edu.wustl.lookingglass.l10n";
	public static final ResourceBundle DEFAULT_RESOURCES = loadResources( DEFAULT_L10N_RESOURCE_PATH );

	protected final Parent root;
	protected final java.util.ResourceBundle resources;
	protected Stage stage = null;

	private final WeakHashMap<Object, Operation> componentModelMap = new WeakHashMap<>();

	public FxComponent( Parent root ) {
		assert Platform.isFxApplicationThread();

		this.root = root;
		this.resources = null;
	}

	public FxComponent( java.net.URL fxmlURL, java.util.ResourceBundle resources ) {
		assert Platform.isFxApplicationThread();

		this.resources = resources;
		this.root = (Parent)loadFxml( this, fxmlURL, this.resources );
	}

	public FxComponent( String fxmlFilename ) {
		assert Platform.isFxApplicationThread();

		this.resources = DEFAULT_RESOURCES;
		this.root = loadFxml( this, getClass(), fxmlFilename, this.resources );
	}

	/**
	 * Loads an fxml file based on the fxmlResourceClass name.
	 */
	public FxComponent( Class<?> fxmlResourceClass ) {
		assert Platform.isFxApplicationThread();

		this.resources = DEFAULT_RESOURCES;
		this.root = loadFxml( this, fxmlResourceClass, fxmlResourceClass.getSimpleName() + ".fxml", this.resources );
	}

	public static java.util.ResourceBundle loadResources( String resourcesPath ) {
		java.util.ResourceBundle resources = null;
		try {
			resources = java.util.ResourceBundle.getBundle( resourcesPath, java.util.Locale.getDefault() );
		} catch( java.util.MissingResourceException e ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.warning( e );
		}
		return resources;
	}

	public static <T> T loadFxml( javafx.fxml.Initializable controller, Class<?> resourceClass, String fxmlFilename, java.util.ResourceBundle resources ) {
		return loadFxml( controller, resourceClass.getResource( fxmlFilename ), resources );
	}

	public static <T> T loadFxml( final javafx.fxml.Initializable controller, final java.net.URL fxmlURL, final java.util.ResourceBundle resources ) {
		assert Platform.isFxApplicationThread();

		javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
		loader.setLocation( fxmlURL );
		if( resources != null ) {
			loader.setResources( resources );
		}
		if( controller != null ) {
			loader.setController( controller );
		}

		try {
			T content = loader.load();
			return content;
		} catch( java.io.IOException e ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( e, fxmlURL );
			throw new edu.wustl.lookingglass.croquetfx.exceptions.FxLoadException( e );
		}
	}

	public static final void addDefaultStyle( Scene scene ) {
		scene.getStylesheets().add( FxComponent.class.getResource( "DefaultStyle.css" ).toExternalForm() );
	}

	@Override
	public void initialize( java.net.URL location, java.util.ResourceBundle resources ) {
	}

	public Stage getStage() {
		if( this.stage == null ) {
			assert this.root.getScene() == null;
			this.stage = new Stage();
			this.stage.setScene( this.getScene() );
		}
		return this.stage;
	}

	public Scene getScene() {
		if( this.root.getScene() == null ) {
			new Scene( this.root );
			addDefaultStyle( this.root.getScene() );
		}
		return this.root.getScene();
	}

	public Parent getRootNode() {
		return this.root;
	}

	public javafx.stage.Window getWindow() {
		return this.getScene().getWindow();
	}

	public java.util.ResourceBundle getResources() {
		return this.resources;
	}

	protected String getLocalizedString( String key ) {
		return this.getResources().getString( key );
	}

	protected String getLocalizedString( String key, Object[] args ) {
		MessageFormat formatter = new MessageFormat( getLocalizedString( key ) );
		return formatter.format( args );
	}

	public interface Block {
				void execute();
	}

	/**
	 * This is the function for "setValueTransactionlessly" from croquet.
	 *
	 * It works by passing a component. That component's events are then
	 * suppressed while it executes the block. This will allow you to change the
	 * component's values without recording anything in the transaction history.
	 */
	public void initializeComponent( Object component, Block block ) {
		org.lgna.croquet.Operation model = this.componentModelMap.get( component );
		if( model instanceof FxPropertyModel ) {
			FxPropertyModel<?> fxPropertyModel = (FxPropertyModel<?>)model;
			fxPropertyModel.initializeModel( block );
		} else {
			// this should never execute if this function is used properly.
			block.execute();
		}
	}

	protected final void registerModel( Object component, org.lgna.croquet.Operation model ) {
		synchronized( this.componentModelMap ) {
			if( this.componentModelMap.containsKey( component ) ) {
				throw new FxInvalidException( "component already has model registered (" + component + ")" );
			} else {
				this.componentModelMap.put( component, model );
			}
		}
	}

	protected final FxEventPropertyModel<ActionEvent> registerActionEvent( Object control, ObjectProperty<EventHandler<ActionEvent>> property, Consumer<ActionEvent> eventHandler ) {
		FxEventPropertyModel<ActionEvent> model = new FxEventPropertyModel<>( this, control, property, eventHandler );
		this.registerModel( control, model );
		return model;
	}

	protected final FxEventPropertyModel<MouseEvent> registerMouseEvent( Object control, ObjectProperty<EventHandler<? super MouseEvent>> property, Consumer<MouseEvent> eventHandler ) {
		// Note: This cast is a bit of a hack... I don't really understand this yet. But it does
		// allow you to registered onMousePressedProperty or onMouseReleasedProperty...
		FxEventPropertyModel<MouseEvent> model = new FxEventPropertyModel<>( this, control, (ObjectProperty<EventHandler<MouseEvent>>)(Object)property, eventHandler );
		this.registerModel( control, model );
		return model;
	}

	protected final FxEventPropertyModel<ActionEvent> register( Button button, Consumer<ActionEvent> eventHandler ) {
		return registerActionEvent( button, button.onActionProperty(), eventHandler );
	}

	protected final FxEventPropertyModel<ActionEvent> register( Hyperlink hyperlink, Consumer<ActionEvent> eventHandler ) {
		return registerActionEvent( hyperlink, hyperlink.onActionProperty(), eventHandler );
	}

	protected final FxEventPropertyModel<ActionEvent> register( MenuItem menuItem, Consumer<ActionEvent> eventHandler ) {
		return registerActionEvent( menuItem, menuItem.onActionProperty(), eventHandler );
	}

	protected final FxEventPropertyModel<ActionEvent> register( SplitMenuButton splitMenuButton, Consumer<ActionEvent> eventHandler ) {
		return registerActionEvent( splitMenuButton, splitMenuButton.onActionProperty(), eventHandler );
	}

	protected final FxPropertyModel<String> register( TextField textField, java.util.function.BiConsumer<String, String> handler ) {
		FxPropertyModel<String> model = new FxPropertyModel<>( this, textField, textField.textProperty(), handler );
		this.registerModel( textField, model );
		return model;
	}

	protected final FxPropertyModel<Boolean> register( ToggleButton toggleButton, java.util.function.BiConsumer<Boolean, Boolean> handler ) {
		FxPropertyModel<Boolean> model = new FxPropertyModel<>( this, toggleButton, toggleButton.selectedProperty(), handler );
		this.registerModel( toggleButton, model );
		return model;
	}

	protected final FxPropertyModel<Number> register( Slider slider, java.util.function.BiConsumer<Number, Number> handler ) {
		FxPropertyModel<Number> model = new FxPropertyModel<>( this, slider, slider.valueProperty(), handler );
		this.registerModel( slider, model );
		return model;
	}

	protected final FxPropertyModel<Toggle> register( ToggleGroup toggleGroup, java.util.function.BiConsumer<Toggle, Toggle> handler, Toggle... toggles ) {
		for( Toggle toggle : toggles ) {
			toggle.setToggleGroup( toggleGroup );
		}
		return register( toggleGroup, handler );
	}

	protected final FxPropertyModel<Toggle> register( ToggleGroup toggleGroup, java.util.function.BiConsumer<Toggle, Toggle> handler ) {
		FxPropertyModel<Toggle> model = new FxPropertyModel<>( this, toggleGroup, toggleGroup.selectedToggleProperty(), handler );
		this.registerModel( toggleGroup, model );
		return model;
	}

	// TODO: Add other components as needed.

	/* Everything below this point is to work with Croquet.
	 * If we ever get rid of it, then gut everything below.
	 */

	private FxViewAdaptor<FxComponent> fxViewAdaptor = null;

	/**
	 * Return the _legacy_ croquet view for the JavaFX scene to integrate within
	 * the old style wrapped swing widgets. There should only ever be one
	 * fxAdaptor. If you need more, then you should create another instance of
	 * your FxScene. Enough with not leveraging the java runtime (i.e. garbage
	 * collector). Let's make programming easy for ourselves again.
	 */
	@Deprecated
	public FxViewAdaptor<FxComponent> getFxViewAdaptor() {
		assert javafx.application.Platform.isFxApplicationThread();
		if( this.fxViewAdaptor == null ) {
			this.fxViewAdaptor = new FxViewAdaptor<FxComponent>( this );
		}
		return this.fxViewAdaptor;
	}

	@Deprecated
	public final void resizeCroquetView() {
		if( this.fxViewAdaptor != null ) {
			this.fxViewAdaptor.recomputePreferredSize();
		}
	}

	/**
	 * Sigh... we aren't a Java FX application. This helper method let's you
	 * center a Stage on our Croquet window.
	 *
	 * Note: Stage must be shown, before you can use it to calculate properties.
	 */
	@Deprecated
	public static void centerStageOnCroquetWindow( java.awt.Component owner, Stage stage ) {
		if( owner != null ) {
			// Note: This is technically not thread safe. This is executing in the fx thread.
			// The owner methods are on an AWT component, which should be executed in the EDT.
			// However, this doesn't seem to be causing a problem yet...
			stage.setX( ( owner.getX() + ( owner.getWidth() / 2 ) ) - ( stage.getWidth() / 2 ) );
			stage.setY( ( owner.getY() + ( owner.getHeight() / 2 ) ) - ( stage.getHeight() / 2 ) );
		} else {
			stage.centerOnScreen();
		}
	}

	@Deprecated
	public static org.lgna.croquet.views.AbstractWindow<?> getCroquetWindow( Node node ) {
		// Note: This is really stupid... this would be easy if this were a java fx application...
		// scene.getWindow() would be all that we need...

		// Note: This method currently does not work if the node is nested too many times in several JFXPanels...
		try {
			javafx.stage.Window embeddedWindow = node.getScene().getWindow();
			java.lang.reflect.Field hostField = com.sun.javafx.stage.EmbeddedWindow.class.getDeclaredField( "host" );
			hostField.setAccessible( true );
			Object value = hostField.get( embeddedWindow );
			Class<?> hostContainer = Class.forName( "javafx.embed.swing.JFXPanel$HostContainer" );
			java.lang.reflect.Field thisField = hostContainer.getDeclaredField( "this$0" );
			thisField.setAccessible( true );
			javafx.embed.swing.JFXPanel jfxPanel = (javafx.embed.swing.JFXPanel)thisField.get( value );

			org.lgna.croquet.views.AwtComponentView<?> croquetView = org.lgna.croquet.views.AwtComponentView.lookup( jfxPanel );
			return croquetView.getRoot();
		} catch( Throwable t ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( t );
			return null;
		}
	}
}
