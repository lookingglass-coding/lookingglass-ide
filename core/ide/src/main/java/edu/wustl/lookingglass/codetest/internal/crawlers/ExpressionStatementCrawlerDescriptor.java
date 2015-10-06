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
package edu.wustl.lookingglass.codetest.internal.crawlers;

import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractNode;

import edu.wustl.lookingglass.codetest.external.api.InternalAPI;
import edu.wustl.lookingglass.codetest.external.api.NonStaticAPI;
import edu.wustl.lookingglass.codetest.internal.CodeTestResult;
import edu.wustl.lookingglass.codetest.internal.StatementCodeTest;

/**
 * @author Aaron Zemach
 */
public class ExpressionStatementCrawlerDescriptor extends CrawlerDescriptor {

	public ExpressionStatementCrawlerDescriptor( Class<?> returned, Boolean[] options ) {
		super( ExpressionStatementCrawler.class, new CrawlerParameters( org.lgna.project.ast.ExpressionStatement.class, returned, options ) );
	}

	@Override
	public CodeTestResult runCodeTest( AbstractMethod root, NonStaticAPI api, StatementCodeTest test ) {
		CodeTestResult lists = new CodeTestResult();
		ExpressionStatementCrawler<?> crawler;

		if( options.options[ 1 ] == false ) {
			crawler = ExpressionStatementCrawler.createInstance( null, root );
		} else {
			crawler = ExpressionStatementCrawler.createInstance( api, root );
		}

		root.crawl( crawler, org.lgna.project.ast.CrawlPolicy.COMPLETE, null );
		for( Object obj : crawler.getList() ) {
			org.lgna.project.ast.ExpressionStatement construct = (org.lgna.project.ast.ExpressionStatement)obj;

			boolean reject = false;
			if( options.options[ 0 ] == true ) {
				org.lgna.project.ast.Expression exp = construct.expression.getValue();
				if( !org.lgna.project.ast.MethodInvocation.class.isAssignableFrom( exp.getClass() ) ) {
					reject = true;
				}
			}

			if( !reject ) {
				if( test.isValidStatement( construct ) ) {
					if( options.returned == org.lgna.project.ast.UserMethod.class ) {
						lists.passing_statements.add( InternalAPI.getEncasingUserMethod( construct ) );
					} else if( options.returned != org.lgna.project.ast.ExpressionStatement.class ) { //i.e. Construct return
						if( InternalAPI.isContainedBy( construct, (Class<AbstractNode>)options.returned ) ) {
							lists.passing_statements.add( InternalAPI.getEncasingNodeOfType( construct, (Class<AbstractNode>)options.returned ) );
						} else {
							lists.failing_statements.add( construct );
						}
					} else {
						lists.passing_statements.add( construct );
					}
				} else {
					lists.failing_statements.add( construct );
				}

			}
		}
		return lists;
	}

}
