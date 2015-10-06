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
package edu.cmu.cs.dennisc.app;

/**
 * @author Dennis Cosgrove
 */
public class ApplicationRoot {
	private static final String DEFAULT_APPLICATION_ROOT_SYSTEM_PROPERTY = "org.alice.ide.rootDirectory";
	// TODO: We need to determine a way to standardize this... this scattered all over the code.
	// Can Maven filtered resources can help us solve this problem?
	private static final String DEFAULT_APPLICATION_NAME = "Looking Glass";

	private static java.io.File rootDirectory;

	public static void initializeIfNecessary() {
		if( rootDirectory != null ) {
			//pass
		} else {
			String rootDirectoryPath = System.getProperty( DEFAULT_APPLICATION_ROOT_SYSTEM_PROPERTY );
			//todo: fallback to System.getProperty( "user.dir" ) ???
			if( rootDirectoryPath != null ) {
				rootDirectory = new java.io.File( rootDirectoryPath );
				if( rootDirectory.exists() ) {
					//pass
				} else {
					StringBuilder sb = new StringBuilder();
					sb.append( "system property: " );
					sb.append( DEFAULT_APPLICATION_ROOT_SYSTEM_PROPERTY );
					sb.append( " is incorrectly set.\n" );
					sb.append( rootDirectory );
					sb.append( " does not exist.\n" );
					sb.append( DEFAULT_APPLICATION_NAME );
					sb.append( " will not work until this is addressed." );
					javax.swing.JOptionPane.showMessageDialog( null, sb.toString(), "Application Root Error", javax.swing.JOptionPane.ERROR_MESSAGE );
					System.exit( -1 );
				}
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append( "system property: " );
				sb.append( DEFAULT_APPLICATION_ROOT_SYSTEM_PROPERTY );
				sb.append( " is not set.\n" );
				sb.append( DEFAULT_APPLICATION_NAME );
				sb.append( " will not work until this is addressed." );
				javax.swing.JOptionPane.showMessageDialog( null, sb.toString(), "Application Root Error", javax.swing.JOptionPane.ERROR_MESSAGE );
				rootDirectory = null;
				System.exit( -1 );
			}
		}
	}

	private ApplicationRoot() {
		throw new AssertionError();
	}

	public static java.io.File getRootDirectory() {
		initializeIfNecessary();
		return rootDirectory;
	}

	public static java.io.File getPlatformDirectory() {
		return new java.io.File( getRootDirectory(), "platform" );
	}

	public static java.io.File getArchitectureSpecificDirectory() {
		StringBuilder sb = new StringBuilder();
		if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isMac() ) {
			sb.append( "macosx" );
		} else {
			Integer bitCount = edu.cmu.cs.dennisc.java.lang.SystemUtilities.getBitCount();
			if( bitCount != null ) {
				if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isWindows() ) {
					sb.append( "win" );
					sb.append( bitCount );
				} else if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isLinux() ) {
					sb.append( "linux-" );
					switch( bitCount ) {
					case 32:
						sb.append( "i586/" );
						break;
					case 64:
						sb.append( "amd64/" );
						break;
					default:
						throw new RuntimeException( System.getProperty( "sun.arch.data.model" ) );
					}
				} else {
					throw new RuntimeException( System.getProperty( "os.name" ) );
				}
			} else {
				throw new RuntimeException( System.getProperty( "sun.arch.data.model" ) );
			}
		}
		return new java.io.File( getPlatformDirectory(), sb.toString() );
	}

	//	public static java.io.File getCommand( String subPath ) {
	//		StringBuilder sb = new StringBuilder();
	//		sb.append( subPath );
	//		if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isWindows() ) {
	//			sb.append( ".exe" );
	//		}
	//		return new java.io.File( this.getArchitectureSpecificDirectory(), sb.toString() );
	//	}

	// <lg> Looking Glass directories are different than Alice

	static public java.io.File getApplicationDirectory() {
		return new java.io.File( ApplicationRoot.getRootDirectory(), "application" );
	}

	public static final String getApplicationNameSafePath() {
		return ApplicationRoot.DEFAULT_APPLICATION_NAME.toLowerCase().replace( " ", "" );
	}

	static public java.io.File getCacheDirectory() {
		java.io.File cacheDir = null;

		// Needed to run code tests on the community server
		String cachePath = System.getProperty( "edu.wustl.lookingglass.cache.dir" );
		if( cachePath != null ) {
			cacheDir = new java.io.File( cachePath );
		}

		if( ( cacheDir != null ) && cacheDir.exists() && cacheDir.isDirectory() ) {
			// pass
		} else {
			cacheDir = new java.io.File( getXDGCacheHome(), ApplicationRoot.getApplicationNameSafePath() );
			cacheDir.mkdirs();
		}

		if( ( cacheDir != null ) && cacheDir.exists() && cacheDir.isDirectory() ) {
			return cacheDir;
		} else {
			return getDefaultDirectory();
		}
	}

	static public java.io.File getDefaultDirectory() {
		return edu.cmu.cs.dennisc.java.io.FileUtilities.getDefaultDirectory();
	}

	static public java.io.File getDocumentsDirectory() {
		return edu.cmu.cs.dennisc.java.io.FileUtilities.getDocumentsDirectory();
	}

	static protected java.io.File getXDGCacheHome() {
		// Follows the XDG Base Directory Specification: http://standards.freedesktop.org/basedir-spec/basedir-spec-latest.html
		java.io.File xdgCacheHome = null;
		String xdgCacheHomePath = System.getenv( "XDG_CACHE_HOME" );
		if( xdgCacheHomePath != null ) {
			xdgCacheHome = new java.io.File( xdgCacheHomePath );
			if( !edu.cmu.cs.dennisc.java.io.FileUtilities.exists( xdgCacheHomePath ) ) {
				xdgCacheHome.mkdirs();
			}
		}

		if( ( xdgCacheHome != null ) && xdgCacheHome.exists() && xdgCacheHome.isDirectory() ) {
			// pass
		} else {
			xdgCacheHome = new java.io.File( getDefaultDirectory(), ".cache" );
			xdgCacheHome.mkdirs();

			// set the file to hidden on windows
			if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isWindows() ) {
				try {
					java.nio.file.Files.setAttribute( java.nio.file.Paths.get( xdgCacheHome.getAbsolutePath() ), "dos:hidden", true );
				} catch( java.io.IOException e ) {
				}
			}
		}

		// Ok. Now we have just given up. We tried, we really really tried.
		// This is not standard to XDG.
		if( ( xdgCacheHome != null ) && xdgCacheHome.exists() && xdgCacheHome.isDirectory() ) {
			// pass
		} else {
			xdgCacheHome = new java.io.File( System.getProperty( "java.io.tmpdir" ) );
		}

		if( ( xdgCacheHome != null ) && xdgCacheHome.exists() && xdgCacheHome.isDirectory() ) {
			return xdgCacheHome;
		} else {
			return getDefaultDirectory();
		}
	}

	// </lg>
}
