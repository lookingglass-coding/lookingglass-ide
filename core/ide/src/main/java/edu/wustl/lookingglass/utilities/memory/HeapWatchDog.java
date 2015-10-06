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
package edu.wustl.lookingglass.utilities.memory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;

/**
 * This shouldn't be necessary. But Looking Glass is not a well behaved Java
 * application. Looking Glass holds onto memory forever, never releasing it to
 * the garbage collector. This heap monitor will warn users when they are about
 * to loose their work.
 *
 * @author Kyle J. Harms
 */
public class HeapWatchDog {

	private static final double MEMORY_FULL_WARNING_THRESHOLD = Double.valueOf( System.getProperty( "edu.wustl.lookingglass.memoryWarningThreshold", "0.80" ) );
	private static final long MEMORY_CHECK_INTERVAL_SECONDS = Long.valueOf( System.getProperty( "edu.wustl.lookingglass.memoryCheckInterval", "10" ) );

	private ScheduledExecutorService service;
	private ScheduledFuture<?> task;

	public HeapWatchDog() {
		this.service = Executors.newSingleThreadScheduledExecutor();

		this.task = this.service.scheduleAtFixedRate( this::checkHeapHealth, MEMORY_CHECK_INTERVAL_SECONDS, MEMORY_CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS );
	}

	private double computeHeapUsage() {
		Runtime runtime = Runtime.getRuntime();
		final long mb = 1024 * 1024;
		int max = (int)( runtime.maxMemory() / mb );
		int total = (int)( runtime.totalMemory() / mb );
		int free = (int)( runtime.freeMemory() / mb );
		int used = total - free;

		return (double)used / (double)max;
	}

	private void checkHeapHealth() {
		double usage = computeHeapUsage();

		// Should we garbage collect?
		if( usage >= MEMORY_FULL_WARNING_THRESHOLD ) {
			System.gc();
			usage = computeHeapUsage();
		}

		if( usage >= MEMORY_FULL_WARNING_THRESHOLD ) {
			showMemoryWarning();
			this.task.cancel( true );
		}
	}

	private void showMemoryWarning() {
		Logger.warning( "Memory " + ( MEMORY_FULL_WARNING_THRESHOLD * 100.0 ) + "% full." );

		ThreadHelper.runOnFxThread( ( ) -> {
			Alert alert = new Alert( AlertType.WARNING );
			alert.setTitle( FxComponent.DEFAULT_RESOURCES.getString( "MemoryWarningPane.title" ) );
			alert.setHeaderText( FxComponent.DEFAULT_RESOURCES.getString( "MemoryWarningPane.header" ) );
			alert.setContentText( FxComponent.DEFAULT_RESOURCES.getString( "MemoryWarningPane.message" ) );
			alert.show();
			Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
			stage.toFront();
			stage.setAlwaysOnTop( true );
		} );
	}
}
