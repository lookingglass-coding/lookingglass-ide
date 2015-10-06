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

import java.util.LinkedList;

import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractNode;
import org.lgna.project.ast.ExpressionStatement;

import edu.wustl.lookingglass.codetest.external.api.InternalAPI;
import edu.wustl.lookingglass.codetest.external.api.NonStaticAPI;
import edu.wustl.lookingglass.codetest.internal.CodeTestResult;
import edu.wustl.lookingglass.codetest.internal.NodeHolder;
import edu.wustl.lookingglass.codetest.internal.StatementCodeTest;

/**
 * @author Aaron Zemach
 */
public class ConsecutiveExpressionStatementCrawlerDescriptor extends CrawlerDescriptor {

	public ConsecutiveExpressionStatementCrawlerDescriptor( Class<?> returned, Boolean[] options ) {
		super( ConsecutiveExpressionStatementCrawler.class, new CrawlerParameters( org.lgna.project.ast.ExpressionStatement.class, returned, options ) );
	}

	@Override
	public String generateValidStatementCode() {
		StringBuilder result = new StringBuilder();

		result.append( "def isValidStatement(self, firstES, secondES):" );
		return result.toString();
	}

	@Override
	public String getMethodHeader() {
		return "def shouldReceiveSuggestion(firstES, secondES):";
	}

	@Override
	public CodeTestResult runCodeTest( AbstractMethod root, NonStaticAPI api, StatementCodeTest test ) {
		CodeTestResult lists = new CodeTestResult();
		ConsecutiveExpressionStatementCrawler crawler;

		if( options.options[ 1 ] == false ) {
			crawler = ConsecutiveExpressionStatementCrawler.createInstance( null, root );
		} else {
			crawler = ConsecutiveExpressionStatementCrawler.createInstance( api, root );
		}

		root.crawl( crawler, org.lgna.project.ast.CrawlPolicy.COMPLETE, null );
		java.util.LinkedList<AbstractNode> results = (LinkedList<AbstractNode>)crawler.getList();
		ExpressionStatement old = null, construct;
		java.util.ArrayList<AbstractNode> nodes = new java.util.ArrayList<AbstractNode>();
		int i = 0;

		while( i < results.size() ) {
			construct = (ExpressionStatement)results.get( i );

			if( ( old != null ) && ( construct != null ) ) {
				//Are method invocations

				boolean methodTest = ( options.options[ 0 ] == false ) ||
						( ( org.lgna.project.ast.MethodInvocation.class.isAssignableFrom( old.expression.getValue().getClass() ) ) &&
						( org.lgna.project.ast.MethodInvocation.class.isAssignableFrom( construct.expression.getValue().getClass() ) ) );

				//Respects statement bodies
				boolean bodiesTest = ( options.options[ 1 ] == false ) ||
						( old.getParent() == construct.getParent() );

				//boolean methodTest = true;
				//boolean bodiesTest = true;

				if( methodTest && bodiesTest ) {
					while( ( old != null ) && ( construct != null ) && test.isValidStatement( old, construct ) ) {
						nodes.add( old );
						old = construct;
						i++;
						construct = (ExpressionStatement)results.get( i );
					}

					//If we have a result...
					if( nodes.size() > 0 ) {
						nodes.add( old );

						if( options.returned == org.lgna.project.ast.UserMethod.class ) {
							lists.passing_statements.add( InternalAPI.getEncasingUserMethod( nodes.get( 0 ) ) );
						} else if( options.returned != NodeHolder.class ) { //i.e. Construct return
							if( InternalAPI.isContainedBy( nodes.get( 0 ), (Class<AbstractNode>)options.returned ) ) {
								java.util.ArrayList<AbstractNode> goodNodes = new java.util.ArrayList<AbstractNode>();
								org.lgna.project.ast.AbstractNode container = InternalAPI.getEncasingNodeOfType( nodes.get( 0 ), (Class<AbstractNode>)options.returned );

								for( org.lgna.project.ast.AbstractNode member : nodes ) {
									if( InternalAPI.isContainedBy( member, (Class<AbstractNode>)options.returned ) && ( InternalAPI.getEncasingNodeOfType( member, (Class<AbstractNode>)options.returned ) == container ) ) {
										goodNodes.add( member );
									}
								}

								if( goodNodes.size() > 0 ) {
									lists.passing_statements.add( InternalAPI.getEncasingNodeOfType( nodes.get( 0 ), (Class<AbstractNode>)options.returned ) );
								} else {
									lists.failing_statements.add( NodeHolder.createNodeHolder( nodes ) );
								}
							}
						} else {
							lists.passing_statements.add( NodeHolder.createNodeHolder( nodes ) );
						}

					}
				}

			}

			nodes.clear();
			old = construct;
			i++;

		}

		return lists;
	}
}
