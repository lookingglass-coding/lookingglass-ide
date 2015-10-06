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
package edu.wustl.lookingglass.virtualmachine.observer;

import org.lgna.common.ComponentThread;

import edu.wustl.lookingglass.virtualmachine.event.VirtualMachineExecutionStateListener;
import edu.wustl.lookingglass.virtualmachine.eventtracing.VMObservableEvent;

/**
 * An object that dispatches Virtual Machine events asynchronously to any
 * observers. This asynchronous behavior allows the Virtual Machine to run
 * independent of all observers and prevents any interference with execution.
 * 
 * @author Paul Gross
 */
public class VMObserverManager implements VirtualMachineExecutionStateListener {

	private java.util.concurrent.ConcurrentLinkedQueue<VMObserver> observers = new java.util.concurrent.ConcurrentLinkedQueue<VMObserver>();
	private java.util.AbstractQueue<VMObservableEvent> eventsQueue = new java.util.concurrent.LinkedBlockingQueue<VMObservableEvent>();
	private Thread eventDispatcherThread;
	private boolean vmIsRunning;

	public VMObserverManager() {
		this.vmIsRunning = true;
		this.eventDispatcherThread = new Thread( "VMObserverManager" ) {
			@Override
			public void run() {
				while( vmIsRunning ) {
					VMObservableEvent event = eventsQueue.poll();
					if( event == null ) {
						try {
							synchronized( this ) {
								wait();
							}
						} catch( InterruptedException e ) {
							e.printStackTrace();
						}
					} else {
						synchronized( observers ) {
							for( VMObserver observer : observers ) {
								observer.update( event );
							}
						}
					}
				}

				if( !vmIsRunning ) {
					while( !eventsQueue.isEmpty() ) {
						VMObservableEvent event = eventsQueue.poll();
						synchronized( observers ) {
							for( VMObserver observer : observers ) {
								observer.update( event );
							}
						}
					}
				}
			}
		};
	}

	public void startObserving() {
		this.vmIsRunning = true;
		synchronized( this.eventDispatcherThread ) {
			this.eventDispatcherThread.start();
		}
	}

	public void stopObserving() {
		this.vmIsRunning = false;
		synchronized( this.eventDispatcherThread ) {
			this.eventDispatcherThread.notify();
		}
		synchronized( this.observers ) {
			this.observers.clear();
		}
	}

	public void addObserver( VMObserver observer ) {
		this.observers.add( observer );
	}

	public VMObserver removeObserver( VMObserver observer ) {
		return this.observers.remove();
	}

	public void addEvent( VMMessage msg, double time ) {
		this.addEvent( msg, time, null );
	}

	public void addEvent( VMMessage msg, double time, Object obj ) {
		this.addEvent( msg, time, obj, null );
	}

	public void addEvent( VMMessage msg, double time, Object obj, ComponentThread thread ) {
		this.addEvent( msg, time, obj, thread, (Object[])null );
	}

	public void addEvent( VMMessage msg, double time, Object obj, ComponentThread thread, Object... properties ) {
		this.addEvent( new VMObservableEvent( msg, time, obj, thread, properties ) );
	}

	private void addEvent( VMObservableEvent event ) {
		if( this.vmIsRunning && !javax.swing.SwingUtilities.isEventDispatchThread() ) {
			this.eventsQueue.add( event );
		}

		synchronized( this.eventDispatcherThread ) {
			this.eventDispatcherThread.notify();
		}
	}

	@Override
	public void isChangedToPaused() {
	}

	@Override
	public void isChangedToRunning() {
	}

	@Override
	public void isEndingExecution() {
		stopObserving();
	}
}
