/*******************************************************************************
 * Copyright (c) 2006, 2015, Carnegie Mellon University. All rights reserved.
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
 * 3. Products derived from the software may not be called "Alice", nor may
 *    "Alice" appear in their name, without prior written permission of
 *    Carnegie Mellon University.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Carnegie Mellon University"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 * ANY AND ALL EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT
 * SHALL THE AUTHORS, COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.lgna.common;

import java.util.concurrent.Callable;

/**
 * @author Dennis Cosgrove
 */
public final class ComponentThread {

	private static final ComponentThreadPoolExecutor pool = new ComponentThreadPoolExecutor();

	private static final java.util.concurrent.Semaphore taskMapLock = new java.util.concurrent.Semaphore( 1 );
	private static java.util.WeakHashMap<java.util.concurrent.Future<?>, ComponentThread> taskMap = new java.util.WeakHashMap<>();

	private static final java.util.concurrent.Semaphore threadMapLock = new java.util.concurrent.Semaphore( 1 );
	private static java.util.WeakHashMap<Thread, ComponentThread> threadMap = new java.util.WeakHashMap<>();

	private static final java.util.concurrent.Semaphore threadIdLock = new java.util.concurrent.Semaphore( 1 );
	private static int threadIdCounter = 0;

	private static ComponentThread getComponentThread( Runnable r ) {
		final ComponentThread componentThread;
		try {
			taskMapLock.acquireUninterruptibly();
			componentThread = taskMap.get( r );
		} finally {
			taskMapLock.release();
		}
		assert componentThread != null;
		return componentThread;
	}

	protected static class ComponentThreadPoolExecutor extends java.util.concurrent.ThreadPoolExecutor {

		public ComponentThreadPoolExecutor() {
			super( 0, Integer.MAX_VALUE, 60L, java.util.concurrent.TimeUnit.SECONDS, new java.util.concurrent.SynchronousQueue<Runnable>(), new ComponentThreadFactory() );
		}

		@Override
		protected void beforeExecute( java.lang.Thread t, java.lang.Runnable r ) {
			final ComponentThread componentThread = getComponentThread( r );
			try {
				threadMapLock.acquireUninterruptibly();
				threadMap.put( t, componentThread );
			} finally {
				threadMapLock.release();
			}
		}

		@Override
		protected void afterExecute( java.lang.Runnable r, java.lang.Throwable t ) {
			// If we use the Future task, then exceptions don't get propagated...
			/*
			 * Note: When actions are enclosed in tasks (such as FutureTask) either
			 * explicitly or via methods such as submit, these task objects catch and
			 * maintain computational exceptions, and so they do not cause abrupt
			 * termination, and the internal exceptions are not passed to this method.
			 */
			if( ( t == null ) && ( r instanceof java.util.concurrent.Future<?> ) ) {
				try {
					java.util.concurrent.Future<?> future = (java.util.concurrent.Future<?>)r;
					future.get();
				} catch( java.util.concurrent.ExecutionException ee ) {
					t = ee.getCause();
				} catch( Throwable ft ) {
					t = ft;
				}
			}

			if( t != null ) {
				final ComponentThread componentThread = getComponentThread( r );
				componentThread.handleException( t );
			}
		}
	};

	protected static class ComponentThreadFactory implements java.util.concurrent.ThreadFactory {

		private static final ThreadGroup threadGroup = new ThreadGroup( ComponentThread.class.getPackage().getName() + " component thread group" );

		private static final java.util.concurrent.Semaphore threadCountLock = new java.util.concurrent.Semaphore( 1 );
		private static int threadCount = 0;

		@Override
		public Thread newThread( Runnable r ) {
			final Thread newThread;

			try {
				threadCountLock.acquireUninterruptibly();
				newThread = new Thread( threadGroup, r, "ComponentThread-" + threadCount++ );
			} finally {
				threadCountLock.release();
			}
			return newThread;
		}
	}

	private final Runnable target;
	private final String description;
	private final int id;

	private java.util.concurrent.Future<?> task = null;
	private ComponentThread parentThread;
	private java.util.concurrent.CopyOnWriteArraySet<ComponentThread> childThreads = new java.util.concurrent.CopyOnWriteArraySet<>();

	private ProgramExecutionExceptionHandler exceptionHandler;

	public ComponentThread( Runnable target, String description ) {
		this.target = target;
		this.description = description;

		try {
			threadIdLock.acquireUninterruptibly();
			id = threadIdCounter++;
		} finally {
			threadIdLock.release();
		}
	}

	public String getDescription() {
		return this.description;
	}

	public int getId() {
		return this.id;
	}

	public void setExceptionHandler( ProgramExecutionExceptionHandler handler ) {
		this.exceptionHandler = handler;
	}

	public ProgramExecutionExceptionHandler getExceptionHandler() {
		return this.exceptionHandler;
	}

	protected ProgramExecutionExceptionHandler locateExceptionHandler() {
		ProgramExecutionExceptionHandler handler = this.getExceptionHandler();
		if( handler != null ) {
			return handler;
		} else {
			if( this.getParentThread() != null ) {
				return this.getParentThread().locateExceptionHandler();
			} else {
				return null;
			}
		}
	}

	public static Throwable unwrapRuntimeException( Throwable t ) {
		if( t instanceof RuntimeException ) {
			RuntimeException re = (RuntimeException)t;
			t = re.getCause();
			if( t == null ) {
				return re;
			} else {
				return unwrapRuntimeException( t );
			}
		} else {
			return t;
		}
	}

	public void handleException( Throwable throwable ) {
		if( throwable != null ) {
			throwable = unwrapRuntimeException( throwable );
			Throwable root = org.apache.commons.lang.exception.ExceptionUtils.getRootCause( throwable );

			if( root instanceof Error ) {
				// Something is wrong with the VM. You don't get to handle this.
				throw (Error)root;
			} else if( throwable instanceof Error ) {
				throw (Error)throwable;
			} else if( org.lgna.common.ProgramClosedException.isProgramClosedException( throwable ) ) {
				// Default back to Alice's default behavior.
				edu.cmu.cs.dennisc.java.util.logging.Logger.info( "ProgramClosedException caught." );
			} else if( ( throwable instanceof java.util.concurrent.CancellationException )
					|| ( throwable instanceof InterruptedException )
					|| ( throwable instanceof java.util.concurrent.BrokenBarrierException ) ) {
				// Do nothing... This thread is trying to die.
			} else {
				ProgramExecutionExceptionHandler handler = locateExceptionHandler();
				if( handler != null ) {
					handler.handleProgramExecutionExeception( throwable );
				} else {
					final RuntimeException re;
					if( throwable instanceof RuntimeException ) {
						re = (RuntimeException)throwable;
					} else {
						re = new RuntimeException( throwable );
					}
					throw re;
				}
			}
		}
	}

	public ComponentThread getParentThread() {
		return this.parentThread;
	}

	public void addChildThread( ComponentThread child ) {
		this.childThreads.add( child );
	}

	public java.util.Set<ComponentThread> getChildrenThreads() {
		return this.childThreads;
	}

	public void start() {
		assert this.task == null;

		this.parentThread = currentThread();
		if( this.parentThread != null ) {
			this.parentThread.addChildThread( this );
		}

		try {
			taskMapLock.acquireUninterruptibly();
			this.task = pool.submit( this.target );
			taskMap.put( this.task, this );
		} finally {
			taskMapLock.release();
		}
	}

	public void join() throws InterruptedException {
		this.join( 0 );
	}

	public void join( long millis ) throws InterruptedException {
		assert this.task != null;

		long base = System.currentTimeMillis();
		long now = 0;

		if( millis < 0 ) {
			throw new IllegalArgumentException( "timeout value is negative" );
		}

		if( millis == 0 ) {
			while( isAlive() ) {
				Thread.sleep( 0 );
			}
		} else {
			while( isAlive() ) {
				long delay = millis - now;
				if( delay <= 0 ) {
					break;
				}
				Thread.sleep( delay );
				now = System.currentTimeMillis() - base;
			}
		}
	}

	public final void kill() {
		if( isAlive() ) {
			this.task.cancel( true );
		}
	}

	public final void killAll() {
		for( ComponentThread child : this.childThreads ) {
			child.killAll();
		}
		this.kill();
	}

	public final boolean isAlive() {
		if( this.task == null ) {
			return false;
		} else {
			return !this.task.isDone();
		}
	}

	static public boolean isComponentThread() {
		return ( ComponentThread.currentThread() != null );
	}

	public static ComponentThread currentThread() {
		return getComponentThread( Thread.currentThread() );
	}

	public static ComponentThread getComponentThread( Thread thread ) {
		final ComponentThread componentThread;
		try {
			threadMapLock.acquireUninterruptibly();
			componentThread = threadMap.get( thread );
		} finally {
			threadMapLock.release();
		}
		return componentThread;
	}

	static public boolean isChildThread( ComponentThread child, ComponentThread parent ) {
		if( parent != null ) {
			return parent.getChildrenThreads().contains( child );
		} else {
			return false;
		}
	}

	static public ComponentThread getParentThread( ComponentThread child ) {
		if( child != null ) {
			return child.getParentThread();
		} else {
			return null;
		}
	}

	static public java.util.Collection<ComponentThread> getAllChildThreads( ComponentThread parent, boolean inclusive ) {
		if( parent != null ) {
			java.util.HashSet<ComponentThread> rv = new java.util.HashSet<ComponentThread>();
			java.util.Collection<ComponentThread> children = parent.getChildrenThreads();

			if( ( children != null ) && !children.isEmpty() ) {
				for( ComponentThread t : children ) {
					rv.addAll( getAllChildThreads( t, true ) );
				}
			}
			if( inclusive ) {
				rv.add( parent );
			}
			return rv;
		} else {
			return null;
		}
	}

	public static java.util.Collection<ComponentThread> getDirectChildThreads( ComponentThread parent ) {
		if( parent != null ) {
			return parent.getChildrenThreads();
		} else {
			return null;
		}
	}

	@Override
	public java.lang.String toString() {
		StringBuilder out = new StringBuilder();
		out.append( this.getClass().getSimpleName() ).append( ": " );
		out.append( this.getId() ).append( " - " );
		out.append( this.getDescription() ).append( "; " );
		out.append( "parent: " ).append( this.getParentThread() != null ? this.getParentThread().getId() : "null" ).append( "; " );
		out.append( "children: " );
		for( ComponentThread child : this.childThreads ) {
			out.append( child.getId() ).append( ", " );
		}
		out.append( "; alive?: " ).append( isAlive() );
		return out.toString();
	}

	static public void runOnComponentThread( final Runnable run ) {
		if( ComponentThread.isComponentThread() ) {
			run.run();
		} else {
			ComponentThread thread = new ComponentThread( run, "invoke-util" );
			thread.start();
		}
	}

	static public void invokeOnComponentThreadAndWait( final Runnable run ) {
		if( ComponentThread.isComponentThread() ) {
			run.run();
		} else {
			ComponentThread thread = new ComponentThread( run, "invoke-util" );
			try {
				thread.start();
				thread.join();
			} catch( InterruptedException e ) {
				throw new RuntimeException( e );
			}
		}
	}

	private static class ComponentThreadReturn<V> {
		public V returnValue = null;
	};

	static public <V> V invokeOnComponentThreadAndWait( final Callable<V> call ) {
		if( ComponentThread.isComponentThread() ) {
			try {
				return call.call();
			} catch( Exception e ) {
				throw new RuntimeException( e );
			}
		} else {
			final ComponentThreadReturn<V> returnValue = new ComponentThreadReturn<>();
			ComponentThread thread = new ComponentThread( ( ) -> {
				try {
					returnValue.returnValue = call.call();
				} catch( Exception e ) {
					throw new RuntimeException( e );
				}
			}, "invoke-util" );
			try {
				thread.start();
				thread.join();
			} catch( InterruptedException e ) {
				throw new RuntimeException( e );
			}
			return returnValue.returnValue;
		}
	}
}
