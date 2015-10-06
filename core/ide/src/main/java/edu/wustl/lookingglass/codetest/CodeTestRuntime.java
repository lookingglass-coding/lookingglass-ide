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
package edu.wustl.lookingglass.codetest;

import org.lgna.project.ProjectMain;

public class CodeTestRuntime {

	// Use extreme caution before adding anything else to this list.
	// This can open our web server up to security breaches.
	// If you need access to something that is not in this list, that probably means so do
	// other users. In this case you should add a feature to the codetest.api. When adding
	// new features to this API be extremely security conscious.
	private static final String[] SECURE_PACKAGES = {
			"org.lgna.project.ast",
			"edu.wustl.lookingglass.codetest.external.api",
			"edu.wustl.lookingglass.codetest.internal",
			"edu.wustl.lookingglass.codetest.internal.crawlers"
	};
	private static final CodeTestClassLoader classLoader = new CodeTestClassLoader( SECURE_PACKAGES );

	private final org.python.util.PythonInterpreter interpreter;
	private final java.security.AccessControlContext securityContext;
	private final int executionTimeoutSeconds;

	private static final java.io.OutputStream NULL_OUTPUT_STREAM = new java.io.OutputStream() {
		@Override
		public void write( byte[] b, int off, int len ) {
		}

		@Override
		public void write( int b ) {
		}

		@Override
		public void write( byte[] b ) throws java.io.IOException {
		}
	};

	public CodeTestRuntime() {
		// New default of 30 seconds. This helps better manage lower priority processes that run code tests.
		this( 30 );
	}

	public CodeTestRuntime( int exectionTimeoutSeconds ) {
		// do not remove these asserts. they are important to keep the security as tight as possible on the server
		assert System.getSecurityManager() != null : "you must have a security manager installed";

		this.executionTimeoutSeconds = exectionTimeoutSeconds;

		// initialize the interpreter
		org.python.core.PyDictionary table = new org.python.core.PyDictionary();
		org.python.core.PySystemState state = new org.python.core.PySystemState();
		state.setClassLoader( classLoader );
		this.interpreter = new org.python.util.PythonInterpreter( table, state );

		// Set allowed packages in interpreter
		for( String secure_package : SECURE_PACKAGES ) {
			interpreter.exec( "from " + secure_package + " import *" );
		}

		// Setup the sandbox
		java.security.Permissions perms = new java.security.Permissions();
		perms.add( new RuntimePermission( "createClassLoader" ) );
		perms.add( new RuntimePermission( "getProtectionDomain" ) );
		perms.add( new RuntimePermission( "accessDeclaredMembers" ) );

		java.security.ProtectionDomain domain = new java.security.ProtectionDomain( new java.security.CodeSource( null, (java.security.cert.Certificate[])null ), perms );
		this.securityContext = new java.security.AccessControlContext( new java.security.ProtectionDomain[] { domain } );
	}

	/* package-private */synchronized edu.wustl.lookingglass.codetest.internal.CodeTestResult executeTest( final CodeTest codeTest, final ProjectMain testable, final java.io.StringWriter output, final java.io.StringWriter error ) throws Exception {
		java.util.concurrent.Callable<edu.wustl.lookingglass.codetest.internal.CodeTestResult> callable = new java.util.concurrent.Callable<edu.wustl.lookingglass.codetest.internal.CodeTestResult>() {
			@Override
			public edu.wustl.lookingglass.codetest.internal.CodeTestResult call() throws Exception {
				return CodeTestRuntime.this.execute( codeTest, testable );
			}
		};

		java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool( 1, new java.util.concurrent.ThreadFactory() {
			@Override
			public Thread newThread( Runnable r ) {
				Thread t = new Thread( r );
				t.setDaemon( true );
				return t;
			}
		} );
		java.util.concurrent.FutureTask<edu.wustl.lookingglass.codetest.internal.CodeTestResult> task = new java.util.concurrent.FutureTask<edu.wustl.lookingglass.codetest.internal.CodeTestResult>( callable );
		java.util.concurrent.Future<?> future = null;

		edu.wustl.lookingglass.codetest.internal.CodeTestResult resultLists = null;

		org.python.core.PyObject stdout = this.interpreter.getSystemState().stdout;
		org.python.core.PyObject stderr = this.interpreter.getSystemState().stderr;
		try {
			// Set the output for this code test
			if( output != null ) {
				CodeTestRuntime.this.interpreter.setOut( output );
			} else {
				CodeTestRuntime.this.interpreter.setOut( CodeTestRuntime.NULL_OUTPUT_STREAM );
			}
			if( error != null ) {
				CodeTestRuntime.this.interpreter.setErr( error );
			} else {
				CodeTestRuntime.this.interpreter.setErr( CodeTestRuntime.NULL_OUTPUT_STREAM );
			}

			// execute the test!
			future = executor.submit( task );
			resultLists = task.get( this.executionTimeoutSeconds, java.util.concurrent.TimeUnit.SECONDS );
		} catch( Exception e ) {
			future.cancel( true );

			if( e instanceof java.util.concurrent.ExecutionException ) {
				if( e.getCause() instanceof Exception ) {
					throw (Exception)e.getCause();
				} else {
					// Something really went wrong. We should log this too.
					edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( e.getCause() );
					throw new Exception( e.getCause() );
				}
			} else {
				throw e;
			}
		} finally {
			executor.shutdownNow();

			// Reset the interpreter output
			CodeTestRuntime.this.interpreter.setOut( stdout );
			CodeTestRuntime.this.interpreter.setErr( stderr );
		}
		return resultLists;
	}

	private edu.wustl.lookingglass.codetest.internal.CodeTestResult execute( final CodeTest codeTest, final ProjectMain testable ) throws java.security.PrivilegedActionException {
		assert codeTest != null : this;
		assert testable != null : this;
		return java.security.AccessController.doPrivileged( executeInSandbox( codeTest, testable ), this.securityContext );
	}

	private java.security.PrivilegedExceptionAction<edu.wustl.lookingglass.codetest.internal.CodeTestResult> executeInSandbox( final CodeTest codeTest, final ProjectMain testable )
	{
		return new java.security.PrivilegedExceptionAction<edu.wustl.lookingglass.codetest.internal.CodeTestResult>() {
			@Override
			public edu.wustl.lookingglass.codetest.internal.CodeTestResult run() throws Exception {
				synchronized( CodeTestRuntime.this.interpreter ) {
					// always reload the class before each test in case the author set class variables
					CodeTestRuntime.this.interpreter.exec( codeTest.getTestClass() );
					org.python.core.PyObject pyCodeTest = CodeTestRuntime.this.interpreter.get( codeTest.getTestClassName() ).__call__();

					edu.wustl.lookingglass.codetest.internal.StatementCodeTest test = (edu.wustl.lookingglass.codetest.internal.StatementCodeTest)pyCodeTest.__tojava__( edu.wustl.lookingglass.codetest.internal.StatementCodeTest.class );
					return codeTest.getCrawlerDescriptor().runCodeTest( testable.getMainMethod(), new edu.wustl.lookingglass.codetest.external.api.NonStaticAPI( testable ), test );
				}
			}
		};
	}
}
