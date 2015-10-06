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
import java.util.List;

import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractNode;
import org.lgna.project.ast.UserMethod;

import edu.wustl.lookingglass.codetest.external.api.InternalAPI;
import edu.wustl.lookingglass.codetest.external.api.NonStaticAPI;
import edu.wustl.lookingglass.codetest.internal.CodeTestResult;
import edu.wustl.lookingglass.codetest.internal.NodeHolder;
import edu.wustl.lookingglass.codetest.internal.StatementCodeTest;

/**
 * @author Aaron Zemach
 */
public class SpreadExpressionStatementCrawlerDescriptor extends CrawlerDescriptor {

	public SpreadExpressionStatementCrawlerDescriptor( Class<?> returned, Boolean[] options ) {
		super( SpreadExpressionStatementCrawler.class, new CrawlerParameters( org.lgna.project.ast.ExpressionStatement.class, returned, options ) );
	}

	@Override
	public String generateValidStatementCode() {
		return "def isValidStatement(self, esList):";
	}

	/**
	 * Takes in a list of all ExpressionStatements as input. If the test is
	 * passed, returns the relevant ExpressionStatements as output. If it's
	 * possible for multiple cases to exist for the test, it should only return
	 * one.
	 */
	@Override
	public String getMethodHeader() {
		return "def shouldReceiveSuggestion(esList):";
	}

	@Override
	public CodeTestResult runCodeTest( AbstractMethod root, NonStaticAPI api, StatementCodeTest test ) {
		CodeTestResult lists = new CodeTestResult();
		SpreadExpressionStatementCrawler crawler;

		if( options.options[ 1 ] == false ) {
			crawler = SpreadExpressionStatementCrawler.createInstance( options.options[ 0 ], null, root );
		} else {
			crawler = SpreadExpressionStatementCrawler.createInstance( options.options[ 0 ], api, root );
		}

		root.crawl( crawler, org.lgna.project.ast.CrawlPolicy.COMPLETE, null );

		java.util.LinkedList<AbstractNode> crawlList = (LinkedList<AbstractNode>)crawler.getList();
		java.util.List<AbstractNode> result = (List<AbstractNode>)test.isValidStatement( crawlList );

		while( result.size() > 0 ) {
			if( options.returned == org.lgna.project.ast.UserMethod.class ) {
				UserMethod um = InternalAPI.getEncasingUserMethod( result.get( 0 ) );
				boolean contained = true;
				for( AbstractNode n : result ) {
					if( !( InternalAPI.getEncasingUserMethod( n ) == um ) ) {
						contained = false;
					}
				}
				if( contained ) {
					lists.passing_statements.add( um );
				} else {
					lists.failing_statements.add( um );
				}

			} else if( options.returned != NodeHolder.class ) { //i.e. Construct return

				java.util.LinkedList<AbstractNode> resultHolder = new java.util.LinkedList<AbstractNode>();
				for( AbstractNode n : result ) {
					if( InternalAPI.isContainedBy( n, (Class<AbstractNode>)options.returned ) ) {
						resultHolder.add( n );
					}
				}

				if( resultHolder.size() > 0 ) {

					AbstractNode oldContainer = null;
					java.util.LinkedList<AbstractNode> temp = new java.util.LinkedList<AbstractNode>();

					for( AbstractNode node : resultHolder ) {
						if( InternalAPI.isContainedBy( node, (Class<AbstractNode>)options.returned ) ) {
							if( oldContainer == null ) {
								oldContainer = InternalAPI.getEncasingNodeOfType( node, (Class<AbstractNode>)options.returned );
							}
							if( InternalAPI.getEncasingNodeOfType( node, (Class<AbstractNode>)options.returned ) == oldContainer ) {
								temp.add( node );
							}
						}
					}

					if( temp.size() > 0 ) {
						if( test.isValidStatement( temp ).size() > 0 ) {
							lists.passing_statements.add( oldContainer );
						}
					}
				}

			} else {
				lists.passing_statements.add( NodeHolder.createNodeHolder( result ) );
			}

			for( AbstractNode n : result ) {
				crawlList.remove( n );
			}
			result = (List<AbstractNode>)test.isValidStatement( crawlList );
		}

		return lists;
	}
}
