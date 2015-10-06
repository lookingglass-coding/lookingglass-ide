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

public class CommunityLogoutOperation extends org.lgna.croquet.Operation implements edu.wustl.lookingglass.community.CommunityStatusObserver {

	private static class SingletonHolder {
		private static CommunityLogoutOperation instance = new CommunityLogoutOperation();
	}

	public static CommunityLogoutOperation getInstance() {
		return SingletonHolder.instance;
	}

	private CommunityLogoutOperation() {
		super( CommunityGroup.COMMUNITY_GROUP, java.util.UUID.fromString( "3e454a66-21eb-4a93-b8b6-bc74effd9ca4" ) );

		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().addObserver( this );
		this.setEnabled( edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().isUserLoggedIn() );
	}

	@Override
	protected final void perform( final org.lgna.croquet.history.Transaction transaction, final org.lgna.croquet.triggers.Trigger trigger ) {
		new javax.swing.SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().logout();
				edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().loginAnonymous();
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
					transaction.createAndSetCompletionStep( CommunityLogoutOperation.this, trigger ).finish();
				} catch( InterruptedException e ) {
					e.printStackTrace();
				} catch( java.util.concurrent.ExecutionException e ) {
					e.printStackTrace();
				}
			}
		}.execute();
	}

	@Override
	public void connectionChanged( edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus status ) {
		// TODO: Should we disable if we lose the connection?
	}

	@Override
	public void accessChanged( edu.wustl.lookingglass.community.CommunityStatus.AccessStatus status ) {
		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			this.setEnabled( status == edu.wustl.lookingglass.community.CommunityStatus.AccessStatus.USER_ACCESS );
		} );
	}
}
