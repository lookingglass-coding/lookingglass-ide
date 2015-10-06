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

import org.lgna.croquet.history.event.Event;

import edu.wustl.lookingglass.community.CommunityControllerEvent;
import edu.wustl.lookingglass.community.api.packets.ModuleResultPacket;
import edu.wustl.lookingglass.community.exceptions.CommunityApiException;

/**
 * The abstract class {@code CollectionModule} is the super class of all data
 * collection modules. Provides appropriate methods for handling thrown
 * exceptions and sending collected data to the LG Community.
 * 
 * @author Michael Pogran
 */
public abstract class CollectionModule implements AbstractCollectionModule {
	private int moduleId;

	/**
	 * The entry point for a {@code CollectionModule}. This method is called by
	 * a {@link edu.wustl.lookingglass.modules.CollectionModuleLoader
	 * CollectionModuleLoader} through reflection.
	 * 
	 * <p>
	 * Sets the moduleId for a collection module, registers the module with the
	 * {@link edu.wustl.lookingglass.modules.CollectionModuleManager
	 * CollectionModuleManager}, and calls {@link #performSetup()}.
	 * 
	 */
	@Override
	public final void initialize( int id ) {
		this.moduleId = id;
		try {
			edu.wustl.lookingglass.ide.LookingGlassIDE.getModuleManager().addModule( this );
			performSetup();
		} catch( Throwable t ) {
			handleException( t );
		}
	}

	/**
	 * Determines the id of the collection module.
	 * 
	 * @return the id of the associated IdeCollectionModule on the LG Community.
	 */
	public int getModuleId() {
		return this.moduleId;
	}

	/**
	 * Handles a thrown exception by removing the module from the
	 * {@link edu.wustl.lookingglass.modules.CollectionModuleManager
	 * CollectionModuleManager} and sending an appropriate report to the LG
	 * Community.
	 * 
	 * @param throwable the exception to be reported
	 */
	protected void handleException( Throwable throwable ) {
		edu.wustl.lookingglass.ide.LookingGlassIDE.getModuleManager().removeModule( this );
		this.sendException( throwable.toString(), edu.cmu.cs.dennisc.issue.IssueUtilities.getThrowableText( throwable ) );
	}

	/**
	 * Creates a {@code ModuleResultPacket} and sends it to the LG Community.
	 * 
	 * @param result the result to be sent to the LG Community
	 */
	protected void sendResult( final String result ) {

		new javax.swing.SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				try {
					edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().sendModuleResult( ModuleResultPacket.createInstance( new Integer( CollectionModule.this.getModuleId() ), result, null ) );
				} catch( CommunityApiException e ) {
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}

	/**
	 * Creates a {@link java.lang.Runnable Runnable} object calling the module
	 * {@link AbstractCollectionModule#transactionTask(Event event)
	 * transationTask} method.
	 * 
	 * @param event the transaction event
	 * @return Runnable object
	 */
	protected Runnable createTransactionTaskRunnable( final Event<?> event ) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					transactionTask( event );
				} catch( Throwable t ) {
					handleException( t );
				}
			}
		};
	}

	/**
	 * Creates a {@link java.lang.Runnable Runnable} object calling the module
	 * {@link AbstractCollectionModule#communityEventTask(CommunityControllerEvent event, boolean isResponse)
	 * communityEventTask} method.
	 * 
	 * @param event community controller event
	 * @param isResponse indicates whether event is a response or request
	 * @return Runnable object
	 */
	protected Runnable createCommunityEventTaskRunnable( final CommunityControllerEvent event, final boolean isResponse ) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					communityEventTask( event, isResponse );
				} catch( Throwable t ) {
					handleException( t );
				}
			}
		};
	}

	/**
	 * Creates a {@link java.lang.Runnable Runnable} object calling the module
	 * {@link AbstractCollectionModule#timedTask() timedTask} method.
	 * 
	 * @return Runnable object
	 */
	protected Runnable createTimedTaskRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				try {
					timedTask();
				} catch( Throwable t ) {
					handleException( t );
				}
			}
		};
	}

	/**
	 * Creates a {@link java.lang.Runnable Runnable} object calling the module
	 * {@link AbstractCollectionModule#programCloseTask() programCloseTask}
	 * method.
	 * 
	 * @return Runnable object
	 */
	protected Runnable createprogramCloseTaskRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				try {
					programCloseTask();
				} catch( Throwable t ) {
					handleException( t );
				}
			}
		};
	}

	/**
	 * Creates a {@code ModuleResultPacket} and sends it to the LG Community.
	 * 
	 * @param exception to be sent to the LG Community
	 */
	private void sendException( final String exception, final String message ) {

		new javax.swing.SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				try {
					edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().sendModuleResult( ModuleResultPacket.createInstance( new Integer( CollectionModule.this.getModuleId() ), message, exception ) );
				} catch( CommunityApiException e ) {
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}

}
