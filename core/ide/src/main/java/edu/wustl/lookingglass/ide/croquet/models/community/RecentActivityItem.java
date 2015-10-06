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

import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import org.alice.ide.uricontent.ThumbnailContentWorker;

import edu.wustl.lookingglass.community.api.packets.ActivityPacket;
import edu.wustl.lookingglass.community.exceptions.CommunityApiException;

/**
 * @author Michael Pogran
 */
public class RecentActivityItem {
	final private ActivityPacket packet;
	final private String type;
	final private String[][] content;

	final private static int LABEL = 0;
	final private static int LINK = 1;

	final private ThumbnailContentWorker thumbnailWorker;

	public RecentActivityItem( ActivityPacket packet ) {
		this.packet = packet;
		this.type = packet.getTrackableType();
		this.content = packet.getContent();
		this.thumbnailWorker = new ThumbnailContentWorker() {

			@Override
			protected Image loadThumbnail() {
				Image thumbnail = null;
				try {
					thumbnail = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().downloadActivityThumbnail( RecentActivityItem.this.packet );
				} catch( CommunityApiException | IOException e ) {
					e.printStackTrace();
				}
				return thumbnail;
			}

		};
	}

	public ActivityPacket getPacket() {
		return this.packet;
	}

	public String getType() {
		return this.type;
	}

	public String[][] getContent() {
		return this.content;
	}

	public String generateHtml() {
		StringBuilder sb = new StringBuilder();
		sb.append( createLink( content[ 0 ][ LABEL ], content[ 0 ][ LINK ] ) );

		switch( type ) {
		case "User":
		case "Photo":
			sb.append( createText( content[ 1 ][ LABEL ] ) );
			break;
		case "World":
		case "Template":
		case "Snippet":
		case "Like":
		case "Post":
		case "Comment":
		case "Bookmark":
			sb.append( createText( content[ 1 ][ LABEL ] ) );
			sb.append( createLink( content[ 2 ][ LABEL ], content[ 2 ][ LINK ] ) );
			break;
		case "Entry":
		case "Reply":
		case "Remix":
			sb.append( createText( content[ 1 ][ LABEL ] ) );
			sb.append( createLink( content[ 2 ][ LABEL ], content[ 2 ][ LINK ] ) );
			sb.append( createText( content[ 3 ][ LABEL ] ) );
			sb.append( createLink( content[ 4 ][ LABEL ], content[ 4 ][ LINK ] ) );
			break;
		}

		return sb.toString();
	}

	private String createLink( String text, String link ) {
		String absoluteLink = edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAbsoluteUrl( link ).toExternalForm();
		URL imgPath = edu.wustl.lookingglass.ide.LookingGlassTheme.getImageURL( "external-hyperlink-icon", org.lgna.croquet.icon.IconSize.FIXED );
		return "<a href=\"" + absoluteLink + "\">" + text + "</a><img src=\"" + imgPath.toExternalForm() + "\">";
	}

	private String createText( String text ) {
		return text + " ";
	}

	public void clearCachedThumbnail() {
		this.thumbnailWorker.clearCachedThumbnail();
	}

	public Image getThumbnail( org.alice.ide.uricontent.GetContentObserver<Image> observer ) {
		if( this.thumbnailWorker.isThumbnailCached() ) {
			return this.thumbnailWorker.getCachedThumbnail();
		} else {
			this.thumbnailWorker.execute( observer );
			return null;
		}
	}
}
