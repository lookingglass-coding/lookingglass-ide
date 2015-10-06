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
package org.alice.ide.croquet.models.projecturi;

import java.io.IOException;

/**
 * @author Dennis Cosgrove
 */
public abstract class AbstractSaveOperation extends UriActionOperation {
	public AbstractSaveOperation( java.util.UUID id ) {
		super( id );
	}

	protected abstract boolean isPromptNecessary( java.io.File file );

	protected abstract java.io.File getDefaultDirectory( org.alice.ide.ProjectApplication application );

	protected abstract String getExtension();

	protected abstract void save( org.alice.ide.ProjectApplication application, java.io.File file ) throws java.io.IOException;

	protected abstract String getInitialFilename();

	protected abstract void removeAfterFailure( org.alice.ide.ProjectApplication application, java.io.File file ) throws java.io.IOException;

	@Override
	protected void perform( org.lgna.croquet.history.CompletionStep<?> step ) {
		org.alice.ide.ProjectApplication application = this.getProjectApplication();
		java.net.URI uri = application.getUri();
		java.io.File filePrevious = edu.cmu.cs.dennisc.java.net.UriUtilities.getFile( uri );
		boolean isExceptionRaised = false;
		do {
			java.io.File fileNext;
			if( isExceptionRaised || this.isPromptNecessary( filePrevious ) ) {
				fileNext = application.getDocumentFrame().showSaveFileDialog( this.getDefaultDirectory( application ), this.getInitialFilename(), this.getExtension(), true );
			} else {
				fileNext = filePrevious;
			}
			isExceptionRaised = false;
			if( fileNext != null ) {
				try {
					this.save( application, fileNext );
				} catch( java.io.IOException ioe ) {
					isExceptionRaised = true;

					java.awt.Component owner;
					org.lgna.croquet.views.Frame frame = application.getDocumentFrame().getFrame();
					if( frame != null ) {
						owner = frame.getAwtComponent();
					} else {
						owner = null;
					}

					try {
						this.removeAfterFailure( application, fileNext );
					} catch( IOException e ) {
						e.printStackTrace();
					}
					//<lg>
					String RETRY_TEXT = "Choose...";
					String CANCEL_TEXT = "Cancel";
					Object[] options = { RETRY_TEXT, CANCEL_TEXT };

					StringBuilder sb = new StringBuilder();
					sb.append( "<html>" );
					sb.append( "<span style='font-weight: bold; font-size: 1.25em;'>Looking Glass could not save your project.</span><br><br>" );
					sb.append( "Oh no!" );
					sb.append( " There isn't enough space to save " );
					sb.append( "<span style='font-weight: bold; font-style:italic;'>" + fileNext.getName() + "</span>" );
					if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isWindows() ) {
						sb.append( "<br>on the " );
						sb.append( fileNext.toPath().getRoot() );
						sb.append( " Drive." );
					} else {
						sb.append( "<br>on your hardrive." );
					}
					sb.append( " Please choose somewhere else<br>to save your project." );
					sb.append( "</html>" );

					String title = "Unable To Save Project";
					javax.swing.Icon icon = edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "logo-128x128", org.lgna.croquet.icon.IconSize.FIXED );
					int selection = javax.swing.JOptionPane.showOptionDialog( owner, sb.toString(), title, javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE, icon, options, RETRY_TEXT );
					if( selection == 1 ) {
						isExceptionRaised = false;
					}
					//</lg>
				}
				if( isExceptionRaised ) {
					//pass
				} else {
					step.finish();
				}
			} else {
				step.cancel();
			}
		} while( isExceptionRaised );
	}
}
