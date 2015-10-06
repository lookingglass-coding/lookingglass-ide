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
package edu.wustl.lookingglass.ide.croquet.models.community;

import org.alice.ide.uricontent.GetContentObserver;
import org.alice.ide.uricontent.UriContentLoader;

import edu.wustl.lookingglass.ide.croquet.models.community.views.PreviewChallengePanel;

public final class DetailWithoutPreviewComposite extends org.lgna.croquet.SimpleComposite<org.lgna.croquet.views.Panel> {
	private PreviewChallengePanel previewChallengePanel = null;

	public DetailWithoutPreviewComposite() {
		super( java.util.UUID.fromString( "e0db0c80-4fc5-4856-b1fa-bb7fe7566f11" ) );
		previewChallengePanel = new PreviewChallengePanel();
	}

	public void update( UriContentLoader<?> uriContentLoader ) {
		if( previewChallengePanel != null ) {
			previewChallengePanel.updateTitleAndDescription( uriContentLoader.getTitle(), uriContentLoader.getDescription() );

			// this should return immediately because we already had to get the thumbnail to do the cell rendering
			try {
				uriContentLoader.getThumbnail( new GetContentObserver<java.awt.Image>() {

					@Override
					public void workStarted() {

					}

					@Override
					public void workEnded() {
					}

					@Override
					public void completed( java.awt.Image content ) {
						java.awt.Image snapshotImage = null;
						if( content != null ) {

							double aspectRatio = (double)content.getHeight( null ) / (double)content.getWidth( null );
							if( aspectRatio == .75 ) {
								java.awt.image.BufferedImage clipped = new java.awt.image.BufferedImage( 640, 360, java.awt.image.BufferedImage.TYPE_INT_RGB );
								java.awt.Graphics2D g2 = (java.awt.Graphics2D)clipped.getGraphics();
								g2.setRenderingHint( java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC );
								g2.drawImage( content, 80, 0, 480, 360, java.awt.Color.BLACK, null );
								snapshotImage = clipped;
							}
							else {
								snapshotImage = content.getScaledInstance( 640, 360, java.awt.Image.SCALE_DEFAULT );
							}
							previewChallengePanel.updateSnapshot( snapshotImage );
						}
					}

					@Override
					public void failed( Throwable t ) {
						if( t instanceof RuntimeException ) {
							RuntimeException re = (RuntimeException)t;
							throw re;
						} else {
							throw new RuntimeException( t );
						}
					}
				} );
			} catch( Exception e ) {
				e.printStackTrace();
			}

		}
	}

	@Override
	protected org.lgna.croquet.views.Panel createView() {
		return previewChallengePanel;
	}
}
