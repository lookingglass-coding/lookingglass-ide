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
package edu.wustl.lookingglass.media;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Michael Pogran
 */
public class ZipRepairProcess {
	public static boolean isArchitectureSpecificCommandAbsolute() {
		return edu.cmu.cs.dennisc.java.lang.SystemUtilities.isLinux() == false;
	}

	public static String getArchitectureSpecificCommand() {
		final String ZIP_COMMAND = "zip";
		if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isLinux() ) {
			return ZIP_COMMAND;
		} else {
			java.io.File archDirectory = edu.cmu.cs.dennisc.app.ApplicationRoot.getArchitectureSpecificDirectory();
			StringBuilder sb = new StringBuilder();
			sb.append( "zip/" );
			sb.append( ZIP_COMMAND );
			if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isWindows() ) {
				sb.append( ".exe" );
			}
			java.io.File commandFile = new java.io.File( archDirectory, sb.toString() );
			if( commandFile.exists() ) {
				return commandFile.getAbsolutePath();
			} else {
				//todo: find on path
				throw new RuntimeException( commandFile.getAbsolutePath() );
			}
		}
	}

	private final File file;
	private Path tempDirPath;

	public ZipRepairProcess( java.io.File file ) {
		this.file = file;
		try {
			this.tempDirPath = java.nio.file.Files.createTempDirectory( "lgrepair" );
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}

	public void cleanUp() {
		File tempDir = null;
		if( tempDirPath != null ) {
			synchronized( tempDirPath ) {
				tempDir = tempDirPath.toFile();
			}
		}

		if( tempDir != null ) {

			String files[] = tempDir.list();

			for( String temp : files ) {
				File fileDelete = new File( tempDir, temp );

				if( fileDelete.isFile() ) {
					fileDelete.delete();
				}
			}
			if( tempDir.list().length == 0 ) {
				tempDir.delete();
			}
		}
	}

	public File repair() {
		if( tempDirPath != null ) {
			Path origFilePath;
			Path tmpFilePath;
			Path repFilePath;
			synchronized( tempDirPath ) {
				try {
					origFilePath = java.nio.file.Paths.get( tempDirPath.toString(), "org-" + this.file.getName() );
					tmpFilePath = java.nio.file.Paths.get( tempDirPath.toString(), "tmp-" + this.file.getName() );
					repFilePath = java.nio.file.Paths.get( tempDirPath.toString(), "rep-" + this.file.getName() );
					java.nio.file.Files.copy( this.file.toPath(), origFilePath );

				} catch( IOException e ) {
					e.printStackTrace();
					return null;
				}
			}

			String[] firstPassArgs = new String[] { getArchitectureSpecificCommand(), "-FF", origFilePath.toString(), "--out", tmpFilePath.toString() };

			ProcessBuilder firstPassBuilder = new ProcessBuilder( firstPassArgs );

			File firstFile = this.start( firstPassBuilder, tmpFilePath.toString() );

			if( firstFile != null ) {
				String[] secondPassArgs = new String[] { getArchitectureSpecificCommand(), "-FF", tmpFilePath.toString(), "--out", repFilePath.toString() };
				ProcessBuilder secondPassBuilder = new ProcessBuilder( secondPassArgs );

				File repairFile = this.start( secondPassBuilder, repFilePath.toString() );

				if( repairFile != null ) {
					try {
						Path finalPath = java.nio.file.Files.copy( repFilePath, java.nio.file.Paths.get( this.file.getParent(), "repaired-" + this.file.getName() ) );
						cleanUp();
						return finalPath.toFile();
					} catch( Exception e ) {
						e.printStackTrace();
					}
				}

			}
		}
		return null;

	}

	private File start( ProcessBuilder processBuilder, String path ) {
		try {
			Process process = processBuilder.start();

			BufferedWriter outputStream = new java.io.BufferedWriter( new java.io.OutputStreamWriter( process.getOutputStream() ) );
			BufferedReader inputStream = new java.io.BufferedReader( new java.io.InputStreamReader( process.getInputStream() ) );

			outputStream.flush();

			final boolean IS_LOCKING_A_PROBLEM_ON_WINDOWS = true;
			if( IS_LOCKING_A_PROBLEM_ON_WINDOWS && edu.cmu.cs.dennisc.java.lang.SystemUtilities.isWindows() ) {
				process.getInputStream().close();
				process.getErrorStream().close();

				inputStream = null;
			}

			int nextChar;
			StringBuilder sb = new StringBuilder();
			while( ( nextChar = inputStream.read() ) != -1 ) {
				sb.append( (char)nextChar );
				if( ( nextChar == ':' ) && sb.toString().contains( "Is this a single-disk archive?" ) ) {
					edu.cmu.cs.dennisc.java.util.logging.Logger.outln( sb.toString() );
					outputStream.write( 'y' );
					outputStream.newLine();
					outputStream.flush();
					break;
				}
			}

			try {
				synchronized( outputStream ) {
					outputStream.close();
				}
			} catch( Exception e ) {
				//pass
			}

			int status = -1;
			try {
				status = process.waitFor();
			} catch( InterruptedException e ) {
				//pass
			}
			edu.cmu.cs.dennisc.java.util.logging.Logger.outln( "Exiting Zip Process:", status );

			return new File( path );

		} catch( IOException e ) {
			//pass
		}
		return null;
	}
}
