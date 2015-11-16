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
package edu.wustl.lookingglass.croquetfx;

import java.awt.EventQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * @author Kyle J. Harms
 */
public class ThreadHelper {

	private static final ExecutorService workerPool = new ThreadPoolExecutor( 0, Integer.MAX_VALUE,
			60L, TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>()) {

		@Override
		protected void afterExecute( Runnable r, Throwable t ) {
			// We must make sure that we don't swallow exceptions, we must propagate them back.
			if( ( t == null ) && ( r instanceof Future<?> ) ) {
				try {
					Future<?> future = (Future<?>)r;
					assert future.isDone();
					future.get();
				} catch( CancellationException ce ) {
					t = ce;
				} catch( ExecutionException ee ) {
					t = ee.getCause();
				} catch( InterruptedException ie ) {
					Thread.currentThread().interrupt(); // ignore/reset
				}
			}
			if( t != null ) {
				Thread.getDefaultUncaughtExceptionHandler().uncaughtException( Thread.currentThread(), t );
			}
		};
	};

	/* Background Worker Thread */

	public static Future<?> runInBackground( Runnable run ) {
		return workerPool.submit( run );
	}

	/* Swing */

	public static void runOnSwingThread( Runnable run ) {
		if( javax.swing.SwingUtilities.isEventDispatchThread() ) {
			run.run();
		} else {
			runOnSwingThreadLater( run );
		}
	}

	public static void runOnSwingThreadLater( Runnable run ) {
		javax.swing.SwingUtilities.invokeLater( run );
	}

	/* Java FX */

	static public void runOnFxThread( final Runnable run ) {
		if( javafx.application.Platform.isFxApplicationThread() ) {
			run.run();
		} else {
			runOnFxThreadLater( run );
		}
	}

	static public void runOnFxThreadLater( final Runnable run ) {
		javafx.application.Platform.runLater( run );
	}

	/**
	 * Invoke on the JavaFx thread and wait for it to return. Be very careful
	 * with this because this can cause deadlocks.
	 */
	static public void invokeInFxThreadAndWait( final Runnable run ) {
		if( javafx.application.Platform.isFxApplicationThread() ) {
			run.run();
			return;
		}

		try {
			java.util.concurrent.FutureTask<Void> future = new java.util.concurrent.FutureTask<>( run, null );
			javafx.application.Platform.runLater( future );
			future.get();
		} catch( java.util.concurrent.ExecutionException e ) {
			throw new edu.wustl.lookingglass.croquetfx.exceptions.FxThreadException( e.getCause() );
		} catch( InterruptedException e ) {
			throw new edu.wustl.lookingglass.croquetfx.exceptions.FxThreadException( e );
		}
	}

	/**
	 * Invoke on the JavaFx thread and wait for it to return. Be very careful
	 * with this because this can cause deadlocks. This method will return
	 * something.
	 */
	static public <V> V invokeInFxThreadAndWait( final java.util.concurrent.Callable<V> call ) {
		if( javafx.application.Platform.isFxApplicationThread() ) {
			try {
				return call.call();
			} catch( Exception e ) {
				throw new edu.wustl.lookingglass.croquetfx.exceptions.FxThreadException( e );
			}
		}

		try {
			java.util.concurrent.FutureTask<V> future = new java.util.concurrent.FutureTask<>( call );
			javafx.application.Platform.runLater( future );
			return future.get();
		} catch( java.util.concurrent.ExecutionException e ) {
			throw new edu.wustl.lookingglass.croquetfx.exceptions.FxThreadException( e.getCause() );
		} catch( InterruptedException e ) {
			throw new edu.wustl.lookingglass.croquetfx.exceptions.FxThreadException( e );
		}
	}

	static public boolean isFxThread() {
		return Platform.isFxApplicationThread();
	}

	static public boolean isSwingThread() {
		return EventQueue.isDispatchThread();
	}

	static public boolean isUIThread() {
		return isFxThread() || isSwingThread();
	}
}
