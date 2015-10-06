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
package org.alice.ide.uricontent;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;

/**
 * @author Dennis Cosgrove
 */
public abstract class AbstractFileProjectLoader extends UriProjectLoader {

	private final static String NO_THUMBNAIL_TEXT = "Preview Unavailable";

	public AbstractFileProjectLoader( java.io.File file, boolean openedByUser ) {
		super( openedByUser );
		this.file = file;
		this.lastModified = this.file.lastModified();
	}

	public AbstractFileProjectLoader( java.io.File file ) {
		this( file, true );
	}

	protected java.io.File getFile() {
		return this.file;
	}

	@Override
	public boolean equals( Object other ) {
		if( this == other ) {
			return true;
		} else {
			if( other instanceof AbstractFileProjectLoader ) {
				AbstractFileProjectLoader otherFileProjectLoader = (AbstractFileProjectLoader)other;
				return this.getClass().equals( other.getClass() ) && this.file.equals( otherFileProjectLoader.file ) && ( this.lastModified == otherFileProjectLoader.lastModified );
			} else {
				return false;
			}
		}
	}

	@Override
	public int hashCode() {
		int rv = 17;
		if( this.file != null ) {
			rv = ( 37 * rv ) + this.file.hashCode();
		}
		rv = ( 37 * rv ) + (int)this.lastModified;
		return rv;
	}

	@Override
	protected boolean isCacheAndCopyStyle() {
		return false;
	}

	@Override
	protected org.lgna.project.Project load() {
		if( file.exists() ) {
			final java.util.Locale locale = java.util.Locale.ENGLISH;
			String lcFilename = file.getName().toLowerCase( locale );
			if( lcFilename.endsWith( ".a2w" ) ) {
				new edu.cmu.cs.dennisc.javax.swing.option.OkDialog.Builder( "Alice3 does not load Alice2 worlds" )
						.title( "Cannot read file" )
						.messageType( edu.cmu.cs.dennisc.javax.swing.option.MessageType.ERROR )
						.buildAndShow();
			} else if( lcFilename.endsWith( org.lgna.project.io.IoUtilities.TYPE_EXTENSION.toLowerCase( locale ) ) ) {
				new edu.cmu.cs.dennisc.javax.swing.option.OkDialog.Builder( file.getAbsolutePath() + " appears to be a class file and not a project file.\n\nLook for files with an " + org.lgna.project.io.IoUtilities.PROJECT_EXTENSION + " extension." )
						.title( "Incorrect File Type" )
						.messageType( edu.cmu.cs.dennisc.javax.swing.option.MessageType.ERROR )
						.buildAndShow();
			} else {
				boolean isWorthyOfException = lcFilename.endsWith( org.lgna.project.io.IoUtilities.PROJECT_EXTENSION.toLowerCase( locale ) );
				java.util.zip.ZipFile zipFile;
				try {
					zipFile = new java.util.zip.ZipFile( file );
				} catch( java.io.IOException ioe ) {
					if( isWorthyOfException ) {
						throw new edu.wustl.lookingglass.ide.uricontent.exceptions.FailedToLoadFileException( file );
					} else {
						org.alice.ide.ProjectApplication.getActiveInstance().showUnableToOpenProjectMessageDialog( file, false );
						zipFile = null;
					}
				}
				if( zipFile != null ) {
					try {
						// <lg/> validate community meta data.
						org.lgna.project.Project project = org.lgna.project.io.IoUtilities.readProject( zipFile );
						edu.wustl.lookingglass.community.CommunityProjectPropertyManager.validateCommunityMetadata( project );
						return project;
					} catch( edu.wustl.lookingglass.project.VersionExceedsCurrentException vece ) {
						org.alice.ide.ProjectApplication.getActiveInstance().handleVersionExceedsCurrent( file );
					} catch( org.lgna.project.VersionNotSupportedException vnse ) {
						org.alice.ide.ProjectApplication.getActiveInstance().handleVersionNotSupported( file, vnse );
					} catch( java.io.IOException ioe ) {
						//<lg>
						if( isWorthyOfException ) {
							throw new edu.wustl.lookingglass.ide.uricontent.exceptions.FailedToLoadFileException( file );
							//</lg>
						} else {
							org.alice.ide.ProjectApplication.getActiveInstance().showUnableToOpenProjectMessageDialog( file, true );
						}
					}
				} else {
					//actionContext.cancel();
				}
			}
		} else {
			org.alice.ide.ProjectApplication.getActiveInstance().showUnableToOpenFileDialog( file, "It does not exist." );
		}
		return null;
	}

	// content info methods
	@Override
	public String getTitle() {
		String fileName = this.getUri().getPath();
		fileName = fileName.substring( fileName.lastIndexOf( "/" ) + 1 );
		String[] rv = fileName.split( ".lgp" );
		return rv[ 0 ];
	}

	@Override
	public String getDescription() {
		try {
			java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile( new File( this.getUri() ) );
			java.util.zip.ZipEntry zipEntry = zipFile.getEntry( "properties.bin" );

			if( zipEntry != null ) {
				java.io.InputStream is = zipFile.getInputStream( zipEntry );
				if( is != null ) {
					java.io.BufferedInputStream bis = new java.io.BufferedInputStream( is );
					edu.cmu.cs.dennisc.codec.InputStreamBinaryDecoder binaryDecoder = new edu.cmu.cs.dennisc.codec.InputStreamBinaryDecoder( bis );

					String version = binaryDecoder.decodeString();
					int N = binaryDecoder.decodeInt();

					for( int i = 0; i < N; i++ ) {
						java.util.UUID id = binaryDecoder.decodeId();
						byte[] buffer = binaryDecoder.decodeByteArray();
						org.lgna.project.properties.PropertyKey<Object> propertyKey = org.lgna.project.properties.PropertyKey.lookupInstance( id );

						if( ( propertyKey != null ) && propertyKey.getRepr().contentEquals( "Project Description" ) ) {
							java.io.ByteArrayInputStream bisProperty = new java.io.ByteArrayInputStream( buffer );
							edu.cmu.cs.dennisc.codec.BinaryDecoder bdProperty = new edu.cmu.cs.dennisc.codec.InputStreamBinaryDecoder( bisProperty );
							return (String)propertyKey.decodeValue( bdProperty );
						}
					}
				}
			}
			zipFile.close();
		} catch( Throwable t ) {
			//pass
		}
		return "This is a local project";
	}

	public String getModifiedDate() {
		java.util.Date modDate = new java.util.Date( this.file.lastModified() );
		return DateFormat.getDateInstance().format( modDate );
	}

	//<lg> get date
	public java.util.Date getLastModified() {
		return new java.util.Date( this.file.lastModified() );
	}

	@Override
	public Image loadThumbnail() {
		BufferedImage thumbnail = null;
		try {
			java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile( new File( this.getUri() ) );
			java.util.zip.ZipEntry zipEntry = zipFile.getEntry( "thumbnail.png" );
			if( zipEntry != null ) {
				java.io.InputStream is = zipFile.getInputStream( zipEntry );
				thumbnail = edu.cmu.cs.dennisc.image.ImageUtilities.read( edu.cmu.cs.dennisc.image.ImageUtilities.PNG_CODEC_NAME, is );
				zipFile.close();
			} else {
				zipFile.close();
				throw new RuntimeException( NO_THUMBNAIL_TEXT );
			}
		} catch( Throwable t ) {
			throw new RuntimeException( NO_THUMBNAIL_TEXT );
		}

		return thumbnail;
	}

	private final java.io.File file;
	private final long lastModified;
}
