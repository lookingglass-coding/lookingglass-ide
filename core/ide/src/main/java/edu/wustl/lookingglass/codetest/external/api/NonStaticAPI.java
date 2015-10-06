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
package edu.wustl.lookingglass.codetest.external.api;

/**
 * Encapsulates the non-static API classes for usage in code tests.
 *
 * @author Aaron Zemach
 */
public class NonStaticAPI {

	/*
	 * Every new API class that is non-static should be created as an instance variable
	 * and then initialized in the constructor. This allows test-writers to access non-static
	 * functionality without the risk of creating (and possibly not destroying) the non-static
	 * objects multiple times throughout the course of the test. By wrapping all classes in this
	 * one NonStaticAPI class, it ensures that as new non-static classes are created, old tests
	 * do not deprecate, and the overall text of tests can remain the same.
	 */

	public InvokedAPI InvokedAPI;

	public NonStaticAPI( org.lgna.project.ProjectMain testable ) {
		edu.wustl.lookingglass.codetest.external.api.InvokedAPI api;
		if( testable instanceof org.lgna.project.Project ) {
			api = new edu.wustl.lookingglass.codetest.external.api.ProjectInvokedAPI( testable.getMainMethod() );
		} else if( testable instanceof edu.wustl.lookingglass.remix.SnippetScript ) {
			api = new edu.wustl.lookingglass.codetest.external.api.RemixInvokedAPI();
		} else {
			// TODO: should probably throw an error.
			api = null;
		}
		InvokedAPI = api;
	}
}
