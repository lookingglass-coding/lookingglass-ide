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
package edu.wustl.lookingglass.ide.perspectives.dinah;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.lgna.project.ast.Statement;

import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.remix.models.StatementMenuOperation;

/**
 * @author Michael Pogran
 */
public class StatementMenuOperationManager {
	private static DinahProgramManager programManager;
	private static java.util.Map<StatementMenuOperationListener, Statement> listenersMap = new ConcurrentHashMap<>();
	private static java.util.Set<StatementMenuOperation> operations = Collections.newSetFromMap( new ConcurrentHashMap<StatementMenuOperation, Boolean>() );

	public static void addListener( Statement statement, StatementMenuOperationListener listener ) {
		listenersMap.put( listener, statement );
		if( programManager != null ) {
			setOperationForListener( statement, listener );
		}
	}

	public static void setProgramManager( DinahProgramManager programManager ) {
		StatementMenuOperationManager.programManager = programManager;

		for( java.util.Map.Entry<StatementMenuOperationListener, Statement> entry : listenersMap.entrySet() ) {
			StatementMenuOperationListener listener = entry.getKey();
			Statement statement = entry.getValue();

			setOperationForListener( statement, listener );
		}
	}

	public static void update( ProgramStateEvent programStateEvent ) {
		for( StatementMenuOperation operation : operations ) {
			operation.update( programStateEvent );
		}
	}

	public static void update( ProgramExecutionEvent programExecutionEvent, boolean isStartEvent ) {
		for( StatementMenuOperation operation : operations ) {
			operation.update( programExecutionEvent, isStartEvent );
		}
	}

	public static void removeProgramManager() {
		for( StatementMenuOperationListener listener : listenersMap.keySet() ) {
			listener.removeStatementMenuOperation();
		}
		operations.clear();
		StatementMenuOperationManager.programManager = null;
	}

	private static void setOperationForListener( Statement statement, StatementMenuOperationListener listener ) {
		StatementMenuOperation operation = new StatementMenuOperation( statement, programManager );
		operations.add( operation );
		listener.addStatementMenuOperation( operation );
	}

}
