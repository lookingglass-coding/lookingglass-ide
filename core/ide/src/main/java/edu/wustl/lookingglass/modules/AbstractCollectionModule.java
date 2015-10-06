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

/**
 * An interface for collecting data from the Looking Glass IDE. This interface
 * establishes a contract between a module and the
 * {@link edu.wustl.lookingglass.modules.CollectionModuleManager
 * CollectionModuleManager} that is not to be violated. When implemented as
 * intended, the {@code CollectionModuleManager} ensures that module tasks are
 * executed in a contained environment - meaning an exception caused by a module
 * will not propagate to the user.
 * 
 * <p>
 * If additional functionality is required, appropriate changes must be made to
 * both this interface and the {@code CollectionModuleManager}.
 * 
 * @author Michael Pogran
 */

public interface AbstractCollectionModule {

	/**
	 * Initializes an IdeCollectionModule received from the LG Community.
	 * Implemented by the abstract CollectionModule class. All module specific
	 * setup should be done in {@link #performSetup() performSetup} method.
	 * 
	 * @param id the community id for CollectionModule
	 */
	public void initialize( int id );

	/**
	 * Performs module specific setup. Used to initialize utility classes and
	 * variables necessary for module tasks.
	 * 
	 * <p>
	 * The CollectionModule interface provides hooks for listening to specific
	 * elements of Looking Glass. This hooks ensure that modules are executed in
	 * a safe environment that does not effect the user.
	 * 
	 * <p>
	 * <b>Do not use this method to add additional listeners.</b> If additional
	 * hooks are needed, make the appropriate changes to the
	 * {@link edu.wustl.lookingglass.modules.CollectionModuleManager
	 * CollectionModuleManager} and {@link AbstractCollectionModule} interface.
	 */
	void performSetup();

	/**
	 * A task to be executed when a transaction change occurs. This method is
	 * called by the
	 * {@link edu.wustl.lookingglass.modules.CollectionModuleManager
	 * CollectionModuleManager} when a change in the {@code TransactionHistory}
	 * is triggered.
	 * 
	 * @param event the transaction event
	 */
	void transactionTask( Event event );

	/**
	 * A task to be executed when a <i>GET</i> or <i>POST</i> request is made in
	 * the {@code CommunityController} for Worlds, Templates, and Snippets. This
	 * method is called by the
	 * {@link edu.wustl.lookingglass.modules.CollectionModuleManager
	 * CollectionModuleManager} an event occurs.
	 * 
	 * @param event the community controller event
	 * @param isResponse indicates whether event is a response or request
	 */
	void communityEventTask( CommunityControllerEvent event, boolean isResponse );

	/**
	 * A task to be executed at regular intervals. This method is continuously
	 * called by the
	 * {@link edu.wustl.lookingglass.modules.CollectionModuleManager
	 * CollectionModuleManager} after an elapsed period of time.
	 * <p>
	 * The duration between successive task executions is specified by the
	 * return value of the {@link #getTimeDelay() getTimeDelay} method.
	 * 
	 */
	void timedTask();

	/**
	 * A task to be executed during <i>normal</i> shutdown of Looking Glass.
	 * This method is called by the
	 * {@link edu.wustl.lookingglass.modules.CollectionModuleManager
	 * CollectionModuleManager} on quit.
	 */
	void programCloseTask();

	/**
	 * Specifies the period between successive calls to the {@link #timedTask}
	 * method.
	 * 
	 * <p>
	 * A return value less then or equal to 0 indicates that the
	 * {@code timedTask} method should never be called.
	 * 
	 * <p>
	 * To easily convert time durations between units, use
	 * {@link java.util.concurrent.TimeUnit}.
	 * 
	 * @return a delay in milliseconds used to schedule a timed task
	 */
	long getTimeDelay();

}
