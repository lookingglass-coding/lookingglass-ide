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
package edu.wustl.lookingglass.modules;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.lgna.croquet.history.event.Event;
import org.python.google.common.util.concurrent.ThreadFactoryBuilder;

import edu.wustl.lookingglass.community.CommunityControllerEvent;

/**
 * Manages collection modules. The ModuleManager Listens to specific IDE events
 * and calls the corresponding {@code CollectionModule} task. Tasks are executed
 * asynchronously and in a manner that ensures exceptions are not propagated to
 * the end user.
 *
 * @author Michael Pogran
 */
public class CollectionModuleManager {
	private final java.util.Set<CollectionModule> collectionModules = edu.cmu.cs.dennisc.java.util.Sets.newHashSet();
	private final java.util.Timer taskTimer = new java.util.Timer( true );
	private final java.util.concurrent.ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat( "collection-module-worker-%d" ).build();
	private ExecutorService executor = Executors.newFixedThreadPool( 4, namedThreadFactory );
	private boolean isAlive = true;

	org.lgna.croquet.history.event.Listener transationListener = new org.lgna.croquet.history.event.Listener() {

		@Override
		public void changing( Event<?> event ) {
		}

		@Override
		public void changed( Event<?> event ) {
			handleTrasactionChange( event );
		}

	};

	edu.wustl.lookingglass.community.CommunityControllerListener communityListener = new edu.wustl.lookingglass.community.CommunityControllerListener() {

		@Override
		public void requestSent( CommunityControllerEvent event ) {
			handleCommunityEvent( event, false );
		}

		@Override
		public void responseRecieved( CommunityControllerEvent event ) {
			handleCommunityEvent( event, true );
		}

	};

	public void initialize() {
		edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().getTransactionHistory().addListener( transationListener );
		edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().addCommunityControllerListener( communityListener );
	}

	public void addModule( CollectionModule module ) {
		this.collectionModules.add( module );

		// If timeDelay is specified, add it to our taskTimer
		if( module.getTimeDelay() > 0 ) {
			this.taskTimer.scheduleAtFixedRate( new ModuleTimerTask( module ), 0, module.getTimeDelay() );
		}
	}

	public void removeModule( CollectionModule module ) {
		this.collectionModules.remove( module );
	}

	public void clearModules() {
		this.collectionModules.clear();
	}

	private void handleCommunityEvent( CommunityControllerEvent event, boolean isResponse ) {
		if( this.isAlive ) {
			for( CollectionModule module : collectionModules ) {
				this.executor.execute( module.createCommunityEventTaskRunnable( event, isResponse ) );
			}
		}
	}

	private void handleTrasactionChange( Event<?> event ) {
		if( this.isAlive ) {
			for( CollectionModule module : collectionModules ) {
				this.executor.execute( module.createTransactionTaskRunnable( event ) );
			}
		}
	}

	public void handleProgramClose() {
		for( CollectionModule module : collectionModules ) {
			this.executor.execute( module.createprogramCloseTaskRunnable() );
		}
		try {
			this.executor.shutdown();
			this.executor.awaitTermination( 4, java.util.concurrent.TimeUnit.SECONDS );
			this.isAlive = false;
		} catch( InterruptedException e ) {
			//TODO: serialize packets and send next time LG opens
		}
	}

	private class ModuleTimerTask extends java.util.TimerTask {
		private final CollectionModule module;

		public ModuleTimerTask( CollectionModule module ) {
			this.module = module;
		}

		@Override
		public void run() {
			if( isAlive ) {
				executor.execute( this.module.createTimedTaskRunnable() );
			}
		}

	}
}
