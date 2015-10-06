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

import java.util.UUID;

import org.alice.ide.uricontent.UriContentLoader;
import org.lgna.croquet.CancelException;
import org.lgna.croquet.Operation;
import org.lgna.croquet.edits.Edit;
import org.lgna.croquet.history.CompletionStep;

import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.ide.community.connection.ConnectionAndAccessCardOwnerComposite;
import edu.wustl.lookingglass.ide.community.connection.OneLineAnonymousAccessCard;
import edu.wustl.lookingglass.ide.community.connection.OneLineConnectedNoAccessCard;
import edu.wustl.lookingglass.ide.croquet.models.community.data.CommunityListData;

/**
 * @author Caitlin Kelleher
 */
public abstract class CommunityTab extends org.lgna.croquet.SimpleTabComposite<org.lgna.croquet.views.Panel> {
	private final Operation refreshOperation = this.createActionOperation( "refreshOperation", new Action() {
		@Override
		public Edit perform( CompletionStep<?> step, InternalActionOperation source ) throws CancelException {
			data.downloadCommunityContent();
			return null;
		}
	} );

	protected final CommunityListData<?> data;
	private final ConnectionAndAccessCardOwnerComposite communityStatusCardComposite;

	public CommunityTab( UUID migrationId, CommunityListData<?> data ) {
		super( migrationId, IsCloseable.FALSE );
		this.data = data;
		this.communityStatusCardComposite = this.createCommunityStatusCardCompositeBuilder().build();
		this.registerSubComposite( this.communityStatusCardComposite );
	}

	@Override
	public void handlePostDeactivation() {
		super.handlePostDeactivation();
		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().removeObserver( data );
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();

		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().addObserver( data );
		// we may not have requested the data yet, so try downloading
		if( ( data != null ) && ( data.getItemCount() == 0 ) ) {
			if( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().getAccessStatus() != AccessStatus.NONE ) {
				data.downloadCommunityContent();
			}
		}
	}

	protected ConnectionAndAccessCardOwnerComposite.Builder createCommunityStatusCardCompositeBuilder() {
		ConnectionAndAccessCardOwnerComposite.Builder statusCompositeBuilder = new ConnectionAndAccessCardOwnerComposite.Builder( java.util.UUID.fromString( "be788382-1df8-11e2-9548-f23c91aec05e" ) );

		// note: disconnected and incompatibleApi handled in ConnectionCardOwnerComposite

		// connected and awaiting access
		statusCompositeBuilder.connectedNoAccess( new OneLineConnectedNoAccessCard() );

		OneLineAnonymousAccessCard defaultComposite = new OneLineAnonymousAccessCard( getRefreshOperation() );
		data.addContentDownloadedListener( defaultComposite );
		statusCompositeBuilder.defaultView( defaultComposite );

		return statusCompositeBuilder;
	}

	public Operation getRefreshOperation() {
		return this.refreshOperation;
	}

	public ConnectionAndAccessCardOwnerComposite getCommunityStatusCardComposite() {
		return this.communityStatusCardComposite;
	}

	public abstract UriContentLoader<?> getContentInfo();
}
