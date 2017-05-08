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
package edu.wustl.lookingglass.remix.share;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.alice.ide.IDE;
import org.lgna.croquet.views.AbstractWindow;
import org.lgna.project.Project;

import edu.wustl.lookingglass.community.api.packets.TemplatePacket;
import edu.wustl.lookingglass.community.exceptions.CommunityApiException;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.croquet.models.community.ShareContentObserver;

/**
 * @author Caitlin Kelleher
 */
public class ShareTemplateComposite extends AbstractShareComposite {

	private static class SingletonHolder {
		private static ShareTemplateComposite instance = new ShareTemplateComposite();
	}

	public static ShareTemplateComposite getInstance() {
		return SingletonHolder.instance;
	}

	private PreviewChallengePage previewChallengePage = new PreviewChallengePage( this );

	private ShareTemplateComposite() {
		super( java.util.UUID.fromString( "e2f22056-ce99-46da-b438-26961f2164ae" ), org.alice.ide.IDE.PROJECT_GROUP );
		this.addPage( this.previewChallengePage );
	}

	@Override
	protected String getShareDialogTitle() {
		return "Share as Template";
	}

	@Override
	protected void shareContent( ShareContentObserver observer ) {
		if( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().isUserLoggedIn() ) {
			observer.updateMessage( "uploading your new template..." );

			try {
				Project project = IDE.getActiveInstance().getUpToDateProject();
				RenderedImage poster = (RenderedImage)this.getPosterImage();

				assert ( project != null ) && ( poster != null );

				TemplatePacket challenge = TemplatePacket.createInstance( this.getTitleState().getValue(), this.getDescriptionState().getValue(), this.getTagState().getValue() );
				challenge.setPoster( poster );
				challenge.setProject( project, IDE.getActiveInstance().getAdditionalDataSources() );

				TemplatePacket response = LookingGlassIDE.getCommunityController().newTemplate( challenge );

				observer.uploadSuccessful( LookingGlassIDE.getCommunityController().getAbsoluteUrl( response.getTemplatePath() ) );

			} catch( CommunityApiException e ) {
				observer.uploadFailed( "We're sorry, Looking Glass couldn't upload your template. Please try again." );
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void updateContent( ShareContentObserver observer, Integer contentId ) {
	}

	@Override
	protected Dimension calculateWindowSize( AbstractWindow<?> window ) {
		return new Dimension( 950, 735 );
	}

	@Override
	public File getRecordedVideo() {
		return null;
	}

	@Override
	protected void saveContentLocally() {
		Project project = IDE.getActiveInstance().getUpToDateProject();
		edu.cmu.cs.dennisc.java.util.zip.DataSource[] dataSources = IDE.getActiveInstance().getDataSources();
		RenderedImage poster = (RenderedImage)getPosterImage();

		String title = getTitleState().getValue();

		java.io.File file = org.alice.ide.ProjectApplication.getActiveInstance().getDocumentFrame().showSaveFileDialog( null, title, "zip", true );

		if( file != null ) {
			try {
				java.io.OutputStream os = new java.io.FileOutputStream( file );
				java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream( os );

				edu.cmu.cs.dennisc.java.util.zip.ZipUtilities.write( zip, new edu.cmu.cs.dennisc.java.util.zip.DataSource() {

					@Override
					public String getName() {
						return "project.lgp";
					}

					@Override
					public void write( OutputStream os ) throws IOException {
						java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
						org.lgna.project.io.IoUtilities.writeProject( baos, project, dataSources );
						byte[] content = baos.toByteArray();
						os.write( content );
					}
				} );

				edu.cmu.cs.dennisc.java.util.zip.ZipUtilities.write( zip, new edu.cmu.cs.dennisc.java.util.zip.DataSource() {

					@Override
					public String getName() {
						return "poster.png";
					}

					@Override
					public void write( OutputStream os ) throws IOException {
						javax.imageio.ImageIO.write( poster, "png", os );
					}
				} );

				zip.flush();
				zip.close();

			} catch( IOException e ) {
				e.printStackTrace();
			}
		}
	}
}
