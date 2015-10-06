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

import edu.wustl.lookingglass.codetest.external.api.NonStaticAPI;
import edu.wustl.lookingglass.codetest.internal.CodeTestResult;
import edu.wustl.lookingglass.codetest.internal.NodeHolder;
import edu.wustl.lookingglass.codetest.internal.StatementCodeTest;

/**
 * @author Aaron Zemach
 */
public class SpreadExpressionCrawlerDescriptor extends CrawlerDescriptor {

	public SpreadExpressionCrawlerDescriptor( Class<? extends org.lgna.project.ast.Expression> expressionClass ) {
		super( SpreadExpressionCrawler.class, new CrawlerParameters( expressionClass, NodeHolder.class, null ) );
	}

	@Override
	public String generateValidStatementCode() {
		return "def isValidStatement(self, expList):";
	}

	/**
	 * Takes in a list of all Expressions as input. If the test is passed,
	 * returns the relevant Expressions as output. If it's possible for multiple
	 * cases to exist for the test, it should only return one.
	 */
	@Override
	public String getMethodHeader() {
		return "def isValidStatement(expList):";
	}

	@Override
	public CodeTestResult runCodeTest( AbstractMethod root, NonStaticAPI api, StatementCodeTest test ) {
		CodeTestResult lists = new CodeTestResult();
		SpreadExpressionCrawler crawler = SpreadExpressionCrawler.createInstance( this.options.crawled );
		root.crawl( crawler, org.lgna.project.ast.CrawlPolicy.COMPLETE, null );

		java.util.LinkedList<AbstractNode> crawlList = (LinkedList<AbstractNode>)crawler.getList();
		java.util.List<AbstractNode> result = (java.util.List<AbstractNode>)test.isValidStatement( crawlList );

		while( result.size() > 0 ) {
			lists.passing_statements.add( NodeHolder.createNodeHolder( result ) );
			for( AbstractNode n : result ) {
				crawlList.remove( n );
			}
			result = (java.util.List<AbstractNode>)test.isValidStatement( crawlList );
		}

		return lists;
	}
}
