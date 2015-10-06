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

import java.util.List;
import java.util.Vector;

import org.lgna.croquet.Application;
import org.lgna.croquet.StringState;
import org.lgna.croquet.views.Button;

import edu.cmu.cs.dennisc.java.awt.font.TextWeight;
import edu.cmu.cs.dennisc.java.util.Lists;
import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus;
import edu.wustl.lookingglass.community.exceptions.CommunityApiException;
import edu.wustl.lookingglass.ide.community.connection.observer.CommunityLoginObserver;
import edu.wustl.lookingglass.ide.croquet.preferences.CommunityPasswordState;
import edu.wustl.lookingglass.ide.croquet.preferences.CommunityUsernameState;

public class CommunityLoginOperation extends org.lgna.croquet.Operation implements edu.wustl.lookingglass.community.CommunityStatusObserver {

	private List<CommunityLoginObserver> communityLoginObservers = Lists.newCopyOnWriteArrayList();

	private static class SingletonHolder {
		private static CommunityLoginOperation instance = new CommunityLoginOperation();
	}

	public static CommunityLoginOperation getInstance() {
		return SingletonHolder.instance;
	}

	private CommunityLoginOperation() {
		super( Application.DOCUMENT_UI_GROUP, java.util.UUID.fromString( "6025bbda-1e6c-4fb0-936f-31a4b5ad4e2e" ) );
		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().addObserver( this );
		this.communityLoginObservers = new Vector<CommunityLoginObserver>();
	}

	// all of the observer handling.
	// todo: these should really be passed in to the operation which argues for moving this out of the singleton pattern
	// maybe make login and out actions that the community controller can give back
	public void addCommunityLoginListener( CommunityLoginObserver newListener ) {
		this.communityLoginObservers.add( newListener );
	}

	public void removeCommunityLoginListener( CommunityLoginObserver listenerToRemove ) {
		this.communityLoginObservers.remove( listenerToRemove );
	}

	public void fireLoginBegin() {
		for( CommunityLoginObserver listener : communityLoginObservers ) {
			listener.loginAttemptBeginning();
		}
	}

	public void fireLoginEnd() {
		for( CommunityLoginObserver listener : communityLoginObservers ) {
			listener.loginAttemptEnding();
		}
	}

	public void fireLoginError( String errorMessage ) {
		for( CommunityLoginObserver listener : communityLoginObservers ) {
			listener.loginErrorOccurred( errorMessage );
		}
	}

	@Override
	public Button createButton( edu.cmu.cs.dennisc.java.awt.font.TextAttribute<?>... textAttributes ) {
		Button rv = super.createButton();
		rv.changeFont( TextWeight.BOLD );
		return rv;
	}

	// getters for the child models
	public StringState getUserNameStringState() {
		return CommunityUsernameState.getInstance();
	}

	public StringState getPasswordStringState() {
		return CommunityPasswordState.getInstance();
	}

	@Override
	protected void perform( org.lgna.croquet.history.Transaction transaction, org.lgna.croquet.triggers.Trigger trigger ) {
		// notify the login panel that login is in progress
		this.fireLoginBegin();

		javax.swing.SwingWorker<Void, Void> swingWorker = new javax.swing.SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				String userName = CommunityUsernameState.getInstance().getValue();
				String password = CommunityPasswordState.getInstance().getPassword();
				edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().loginUser( userName, password );
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
					// notify the login panel that login has completed
					fireLoginEnd();

				} catch( InterruptedException e ) {
					if( communityLoginObservers.size() > 0 ) {
						fireLoginError( findLocalizedText( "unknownError" ) );
					}
				} catch( java.util.concurrent.ExecutionException e ) {
					if( e.getCause() instanceof edu.wustl.lookingglass.community.exceptions.InvalidRequestException ) {
						if( communityLoginObservers.size() > 0 ) {
							fireLoginError( findLocalizedText( "invalidUsernamePassword" ) );
						}
					} else if( e.getCause() instanceof edu.wustl.lookingglass.community.exceptions.IncompatibleApiException ) {
						if( communityLoginObservers.size() > 0 ) {
							fireLoginError( findLocalizedText( "incompatibleAPI" ) );
						}
					} else if( e.getCause() instanceof edu.wustl.lookingglass.community.exceptions.IncompatibleTypeException ) {
						if( communityLoginObservers.size() > 0 ) {
							fireLoginError( findLocalizedText( "connectionError" ) );
						}
					} else if( e.getCause() instanceof edu.wustl.lookingglass.community.exceptions.InvalidConnectionException ) {
						if( communityLoginObservers.size() > 0 ) {
							fireLoginError( findLocalizedText( "connectionError" ) );
						}
					} else if( e.getCause() instanceof edu.wustl.lookingglass.community.exceptions.CommunityIOException ) {
						if( communityLoginObservers.size() > 0 ) {
							fireLoginError( findLocalizedText( "communityIOException" ) );
						}
					} else if( e.getCause() instanceof CommunityApiException ) {
						if( communityLoginObservers.size() > 0 ) {
							fireLoginError( findLocalizedText( "unknownError" ) );
						}
						Logger.throwable( e.getCause() );
					} else {
						Logger.throwable( e.getCause() );
						e.printStackTrace();
						if( communityLoginObservers.size() > 0 ) {
							fireLoginError( findLocalizedText( "connectionError" ) );
						}
					}
				}

			}
		};
		swingWorker.execute();
	}

	@Override
	public void connectionChanged( ConnectionStatus status ) {
	}

	@Override
	public void accessChanged( AccessStatus status ) {
		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			this.setEnabled( status != edu.wustl.lookingglass.community.CommunityStatus.AccessStatus.USER_ACCESS );
		} );
	}
}
