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
package edu.wustl.lookingglass.modules;

import java.io.IOException;

import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.wustl.lookingglass.community.api.packets.ModulePacket;

/**
 * @author Michael Pogran
 */
public class CollectionModuleLoader {

	private final String jarPath;
	private final int moduleId;
	private java.util.jar.JarFile jarFile;

	public static void loadCollectionModules( ModulePacket[] modulePackets ) {
		if( modulePackets != null ) {
			for( ModulePacket packet : modulePackets ) {
				edu.wustl.lookingglass.modules.CollectionModuleLoader loader = new edu.wustl.lookingglass.modules.CollectionModuleLoader( packet );
				loader.loadCollectionModule();
			}
		}
	}

	public CollectionModuleLoader( edu.wustl.lookingglass.community.api.packets.ModulePacket packet ) {
		this.jarPath = packet.getModulePath();
		this.moduleId = packet.getId();
	}

	public void loadCollectionModule() {
		new javax.swing.SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() {
				try {
					java.io.File file = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().downloadCollectionModule( CollectionModuleLoader.this.jarPath );
					CollectionModuleLoader.this.jarFile = new java.util.jar.JarFile( file );
					loadClasses( file );
				} catch( Exception e ) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
				} catch( Exception e ) {
					e.printStackTrace();
				}
			}
		}.execute();
	}

	private void loadClasses( java.io.File file ) {
		try {
			edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().verifySignedJarCertificate( this.jarFile );

			java.net.URL jarUrl;
			jarUrl = file.toURI().toURL();
			java.net.URLClassLoader classLoader = new java.net.URLClassLoader( new java.net.URL[] { jarUrl } );

			String mainClassName = getMainClassName();
			if( mainClassName != null ) {
				Class<?> mainClass;

				mainClass = classLoader.loadClass( mainClassName );
				java.lang.reflect.Method initializeMethod;
				initializeMethod = mainClass.getMethod( "initialize", new Class[] { int.class } );
				Object moduleInstance;
				moduleInstance = mainClass.newInstance();
				initializeMethod.invoke( moduleInstance, this.moduleId );
			} else {
				Logger.severe( "FAILED TO LOAD MODULE " + this.moduleId + ": No \"Main-Class\" entry found in manifest." );
			}
			classLoader.close();
		} catch( Throwable t ) {
			Logger.throwable( t, this );
		}
	}

	String getMainClassName() throws IOException {
		java.util.jar.Attributes attr = this.jarFile.getManifest().getMainAttributes();
		return attr != null ? attr.getValue( java.util.jar.Attributes.Name.MAIN_CLASS ) : null;
	}

}
