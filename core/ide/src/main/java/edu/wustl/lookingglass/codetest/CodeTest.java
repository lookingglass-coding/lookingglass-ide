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

public class CodeTest {

	private edu.wustl.lookingglass.community.api.packets.CodeTestPacket packet = null;
	private String snippet;
	private edu.wustl.lookingglass.codetest.internal.crawlers.CrawlerDescriptor crawlerDescriptor;

	private String testClassName = generateCodeTestClassName();
	private String testClass = null;

	public CodeTest( edu.wustl.lookingglass.codetest.internal.crawlers.CrawlerDescriptor crawlerDescriptor, String snippet ) {
		assert crawlerDescriptor != null;
		assert snippet != null;

		this.crawlerDescriptor = crawlerDescriptor;
		this.snippet = snippet;
		this.generateTestClass();
	}

	private static String generateCodeTestClassName() {
		return "CodeTest_" + java.util.UUID.randomUUID().toString().replaceAll( "-", "_" );
	}

	/* package-private */void setPacket( edu.wustl.lookingglass.community.api.packets.CodeTestPacket packet ) {
		this.packet = packet;
	}

	public edu.wustl.lookingglass.community.api.packets.CodeTestPacket getPacket() {
		return this.packet;
	}

	public void setCrawlerDescriptor( edu.wustl.lookingglass.codetest.internal.crawlers.CrawlerDescriptor crawlerDescriptor ) {
		assert crawlerDescriptor != null;

		this.crawlerDescriptor = crawlerDescriptor;
		this.generateTestClass();
	}

	public void setSnippet( String snippet ) {
		assert snippet != null;

		this.snippet = snippet;
		this.generateTestClass();
	}

	public edu.wustl.lookingglass.codetest.internal.crawlers.CrawlerDescriptor getCrawlerDescriptor() {
		return this.crawlerDescriptor;
	}

	public String getSnippet() {
		return this.snippet;
	}

	public edu.wustl.lookingglass.codetest.internal.crawlers.CrawlerDescriptor getDescriptor() {
		return this.crawlerDescriptor;
	}

	/* package-private */synchronized String getTestClassName() {
		return this.testClassName;
	}

	/* package-private */synchronized String getTestClass() {
		return this.testClass;
	}

	private synchronized void generateTestClass() {
		StringBuilder code = new StringBuilder();

		code.append( "class " + this.testClassName + "( " + edu.wustl.lookingglass.codetest.internal.StatementCodeTest.class.getSimpleName() + " ):\n" );
		code.append( "\t" + crawlerDescriptor.generateValidStatementCode() + "\n" );
		if( snippet != null ) {
			for( String line : this.snippet.split( "\n" ) ) {
				code.append( "\t\t" + line + "\n" );
			}
		}

		this.testClass = code.toString();
	}

	/**
	 * Reuse a CodeTestRuntime to help improve performance and memory
	 * consumption. It is okay to reuse a runtime for any of the skill/badge
	 * tests written by the LG team. It is NOT okay to reuse a runtime for
	 * mentor authored tests. This could led to severe security problems or side
	 * effects. For mentored authored tests (or anyone that is not on the LG
	 * team) you should ALWAYS create a new runtime when calling this function.
	 * i.e. execute( new CodeTestRuntime(), ... );
	 */
	public edu.wustl.lookingglass.codetest.internal.CodeTestResult execute( CodeTestRuntime runtime, ProjectMain testable, java.io.StringWriter output, java.io.StringWriter error ) throws Exception {
		return runtime.executeTest( this, testable, output, error );
	}

	public edu.wustl.lookingglass.codetest.internal.CodeTestResult execute( CodeTestRuntime runtime, ProjectMain testable ) throws Exception {
		return runtime.executeTest( this, testable, null, null );
	}

	public edu.wustl.lookingglass.codetest.internal.CodeTestResult execute( CodeTestRuntime runtime, String testableFilepath ) throws Exception {
		ProjectMain testable = null;

		String testableExtension = edu.cmu.cs.dennisc.java.io.FileUtilities.getExtension( testableFilepath );
		switch( testableExtension ) {
		case org.lgna.project.io.IoUtilities.PROJECT_EXTENSION:
			testable = org.lgna.project.io.IoUtilities.readProject( testableFilepath );
			break;
		case edu.wustl.lookingglass.remix.SnippetFileUtilities.SNIPPET_EXTENSION:
			testable = edu.wustl.lookingglass.remix.SnippetFileUtilities.loadSnippet( new java.io.FileInputStream( testableFilepath ) );
			break;
		default:
			throw new Exception( "Unknown file type: " + testableExtension );
		}

		return runtime.executeTest( this, testable, null, null );
	}
}
