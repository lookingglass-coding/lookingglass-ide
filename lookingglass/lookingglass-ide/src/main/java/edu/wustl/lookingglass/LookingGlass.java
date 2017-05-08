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
package edu.wustl.lookingglass;

import java.lang.Thread.UncaughtExceptionHandler;

import org.alice.ide.issue.DefaultExceptionHandler;

import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.utilities.memory.HeapWatchDog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

/**
 * @author Kyle J. Harms
 */
public class LookingGlass {

	private static HeapWatchDog heapMonitor;
	private static JFXPanel initJavaFX;
	private static UncaughtExceptionHandler exceptionHandler;

	// Initialize all things non-swing. This is necessary to run the code tests on the community.
	public static void initialize() {
		// DO NOT PUT ANY SWING/CROQUET CODE HERE.

		// Use a security manager, necessary to run code tests.
		String securityPolicyProperty = System.getProperty( "java.security.policy" );
		if( securityPolicyProperty == null ) {
			System.setProperty( "java.security.policy", edu.cmu.cs.dennisc.app.ApplicationRoot.getApplicationDirectory() + "/application.policy" );
		}
		java.security.Policy.getPolicy().refresh();
		System.setSecurityManager( new SecurityManager() );
		assert System.getSecurityManager() != null;

		// Check to make sure we are using the right file encoding for LG.
		String fileEncoding = System.getProperty( "file.encoding" );
		if( ( fileEncoding == null ) || !fileEncoding.equals( "UTF-8" ) ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.warning( "default file encoding is not UTF-8" );
		}

		// Network settings
		System.setProperty( "http.keepAlive", "true" );
		System.setProperty( "http.agent", "LookingGlass/" + edu.wustl.lookingglass.ide.LookingGlassIDE.getVersionText() + " Community/" + LookingGlassIDE.COMMUNITY_API_VERSION );

		// initialize jython
		if( System.getProperty( "python.cachedir" ) == null ) {
			java.io.File jythonCache = new java.io.File( edu.cmu.cs.dennisc.app.ApplicationRoot.getCacheDirectory(), "jython" );
			System.setProperty( "python.cachedir", jythonCache.getAbsolutePath() );
		}
		java.io.File jythonCache = new java.io.File( System.getProperty( "python.cachedir" ) );
		jythonCache.mkdirs();

		// DO NOT PUT ANY SWING/CROQUET CODE HERE.
	}

	// Initialize all things swing
	public static void initializeInterface() {
		// Call this only after the other properties have been set just as a check.
		assert java.awt.EventQueue.isDispatchThread();

		// Set anti-aliased text, makes text easier to read
		System.setProperty( "swing.aatext", "true" );

		if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isMac() ) {
			// Application Name for Mac
			String macAppName = System.getProperty( "apple.awt.application.name" );
			if( macAppName == null ) {
				System.setProperty( "apple.awt.application.name", LookingGlassIDE.APPLICATION_NAME );
			}

			// Appliation Icon for Mac
			String macAppIcon = System.getProperty( "apple.awt.application.icon" );
			if( macAppIcon == null ) {
				System.setProperty( "apple.awt.application.icon", new java.io.File( edu.cmu.cs.dennisc.app.ApplicationRoot.getApplicationDirectory(), "lookingglass.icns" ).getAbsolutePath() );
			}
		}

		// Set the WM_CLASS correctly on Linux
		if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isLinux() ) {
			try {
				java.awt.Toolkit xToolkit = java.awt.Toolkit.getDefaultToolkit();
				java.lang.reflect.Field awtAppClassNameField = xToolkit.getClass().getDeclaredField( "awtAppClassName" );
				awtAppClassNameField.setAccessible( true );
				awtAppClassNameField.set( xToolkit, LookingGlassIDE.APPLICATION_NAME );
			} catch( Exception e ) {
				// It's nice, but we don't need it. Besides, who uses GNU/Linux anyway!?
			}
		}

		// Setup the Apple menu bar
		final String MENU_BAR_UI_NAME = "MenuBarUI";
		final Object macMenuBarUI;
		if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isMac() ) {
			System.setProperty( "apple.laf.useScreenMenuBar", "true" );
			if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isPropertyTrue( "apple.laf.useScreenMenuBar" ) ) {
				macMenuBarUI = javax.swing.UIManager.get( MENU_BAR_UI_NAME );
			} else {
				macMenuBarUI = null;
			}
		} else {
			macMenuBarUI = null;
		}
		edu.cmu.cs.dennisc.javax.swing.UIManagerUtilities.setLookAndFeel( "Nimbus" );
		if( macMenuBarUI != null ) {
			javax.swing.UIManager.put( MENU_BAR_UI_NAME, macMenuBarUI );
		}

		// Set the default spacing for Mig Layout... GNOME is the most conservative.
		net.miginfocom.layout.PlatformDefaults.setPlatform( net.miginfocom.layout.PlatformDefaults.GNOME );

		// todo: this should really be part of our custom scrollbar...
		javax.swing.UIManager.put( "ScrollBar.width", 15 );
		javax.swing.UIManager.put( "ScrollBar.incrementButtonGap", 0 );
		javax.swing.UIManager.put( "ScrollBar.decrementButtonGap", 0 );
		javax.swing.UIManager.put( "ScrollBar.thumb", edu.cmu.cs.dennisc.java.awt.ColorUtilities.createGray( 140 ) );

		// Check if this platform can run Looking Glass. Some versions of the mac can't!
		checkAndWarnMacCompatibility();

		// initialize JavaFX
		Platform.setImplicitExit( false );
		LookingGlass.initJavaFX = new JFXPanel();
		Application.setUserAgentStylesheet( Application.STYLESHEET_MODENA );

		// handle uncaught exceptions
		LookingGlass.exceptionHandler = new DefaultExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler( exceptionHandler );

		org.lgna.project.ast.AbstractNode.setAstLocalizerFactory( new org.lgna.project.ast.localizer.DefaultAstLocalizerFactory() {
			@Override
			public org.lgna.project.ast.localizer.AstLocalizer createInstance( final java.lang.StringBuilder sb ) {
				return new org.lgna.project.ast.localizer.DefaultAstLocalizer( sb ) {
					@Override
					public void appendThis() {
						sb.append( "this" );
					}
				};
			}
		} );

		edu.cmu.cs.dennisc.java.awt.ConsistentMouseDragEventQueue.pushIfAppropriate();

		javax.swing.SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				// Initialize JOGL
				edu.cmu.cs.dennisc.render.gl.RendererNativeLibraryLoader.initializeIfNecessary();

				// Initialize VLCJ
				edu.cmu.cs.dennisc.video.vlcj.VlcjUtilities.initializeIfNecessary();
			}
		} );

		LookingGlass.heapMonitor = new HeapWatchDog();
	}

	private static void checkAndWarnMacCompatibility() {
		assert java.awt.EventQueue.isDispatchThread();

		try {
			if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isMac() ) {
				String version = System.getProperty( "os.version" );
				String[] versions = version.split( "\\." );
				int major = Integer.valueOf( versions[ 0 ] );
				int minor = Integer.valueOf( versions[ 1 ] );
				int patch = 0;
				if( versions.length >= 3 ) {
					patch = Integer.valueOf( versions[ 2 ] );
				}
				if( ( major < 10 ) ||
						( ( major == 10 ) && ( minor < 7 ) ) ||
						( ( major == 10 ) && ( minor == 7 ) && ( patch < 3 ) ) ) {
					String[] options = { "Try it anyway", "Quit" };
					int result = javax.swing.JOptionPane.showOptionDialog( null,
							"Warning! " + LookingGlassIDE.APPLICATION_NAME + " requires Mac OS version 10.7.3 and above.\n\n" +
									"You are currently running Mac OS version " + version + ".\n\n" +
									LookingGlassIDE.APPLICATION_NAME + " will likely crash and behave in unexpected ways.\n\n" +
									"Use " + LookingGlassIDE.APPLICATION_NAME + " at your own risk!",
							LookingGlassIDE.APPLICATION_NAME + " Incompability",
							javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE, null, options, options[ 1 ] );
					if( ( result == 1 ) || ( result == -1 ) ) {
						System.exit( 1 );
					}
				}
			}
		} catch( Throwable t ) {
			// This isn't necessary. But let's just make sure it doesn't cause a silent crash.
			edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( t );
		}
	}

	/*
	 * This class is based on Alice's EntryPoint. It is modified so we can run the code tests on the community.
	 * It is called LookingGlass so on Linux (especially for community server) the java process shows up as LookingGlass,
	 * not "EntryPoint".
	 */
	public static void main( final String[] args ) {
		LookingGlass.initialize();

		javax.swing.SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				LookingGlass.initializeInterface();
				LookingGlassIDE ide = new LookingGlassIDE( args );
			}
		} );
	}
}
