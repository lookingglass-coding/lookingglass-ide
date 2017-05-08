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
package edu.wustl.lookingglass.utilities;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.alice.ide.ProjectApplication;
import org.alice.ide.projecturi.DirectoryUriListData;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;

/**
 * @author Michael Pogran
 */
public class LockDecodeOperation extends org.lgna.croquet.Operation {

	java.util.concurrent.ExecutorService executor = Executors.newFixedThreadPool( 8, new java.util.concurrent.ThreadFactory() {
		private Integer count = 0;

		@Override
		public Thread newThread( Runnable r ) {
			synchronized( count ) {
				return new Thread( r, "test-lock" + count++ );
			}
		}
	} );

	public LockDecodeOperation() {
		super( org.lgna.croquet.Application.PROJECT_GROUP, java.util.UUID.fromString( "6f2c68ca-5814-4cf4-9a4f-275b07581c38" ) );
	}

	@Override
	protected void localize() {
		setName( "Lock me" );
	}

	@Override
	protected void perform( Transaction transaction, Trigger trigger ) {
		DirectoryUriListData data = new DirectoryUriListData( ProjectApplication.getMyProjectsDirectory() );
		for( java.net.URI uri : data ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.outln( "START: ", uri.toString() );
			org.alice.ide.uricontent.UriProjectLoader loader = org.alice.ide.uricontent.UriProjectLoader.createInstance( uri );
			java.util.concurrent.FutureTask<Void> task = new java.util.concurrent.FutureTask<Void>( new Runnable() {

				@Override
				public void run() {
					try {
						loader.getContentWaitingIfNecessary( org.alice.ide.uricontent.UriContentLoader.MutationPlan.WILL_MUTATE );
						edu.cmu.cs.dennisc.java.util.logging.Logger.outln( "DONE: ", uri.toString() );
					} catch( InterruptedException | ExecutionException e ) {
						e.printStackTrace();
					}
				}
			}, null );
			executor.execute( task );
		}
	}

}
