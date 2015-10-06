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

import edu.wustl.lookingglass.codetest.exceptions.BadParametersException;
import edu.wustl.lookingglass.codetest.exceptions.IncompatibleNodeTypeException;
import edu.wustl.lookingglass.codetest.exceptions.InvalidCrawlerTypeException;
import edu.wustl.lookingglass.codetest.exceptions.InvalidNodeTypeException;
import edu.wustl.lookingglass.codetest.internal.crawlers.CodeTestCrawler;
import edu.wustl.lookingglass.community.api.packets.CodeTestPacket.ASTNodeType;
import edu.wustl.lookingglass.community.api.packets.CodeTestPacket.CrawlerOptionType;
import edu.wustl.lookingglass.community.api.packets.CodeTestPacket.CrawlerType;

/**
 * @author Aaron Zemach
 */
public class CodeTestFactory {

	@SuppressWarnings( "unchecked" )
	public static CodeTest getCodeTest( Integer crawlerType, Integer crawled, Integer returned, Integer param1, Integer param2, Integer param3, String testCode ) {
		assert testCode != null;
		edu.wustl.lookingglass.codetest.internal.crawlers.CrawlerDescriptor descriptor;

		//If invalid node types specified, blow up
		if( ASTNodeType.getType( crawled ) == null ) {
			throw new InvalidNodeTypeException( "Invalid node type to crawl." );
		}
		if( ASTNodeType.getType( returned ) == null ) {
			throw new InvalidNodeTypeException( "Invalid node type to return." );
		}

		Class<?> crawled_class = ASTNodeType.getType( crawled ).getNodeClass();
		Class<?> returned_class = ASTNodeType.getType( returned ).getNodeClass();

		CrawlerType cType = CrawlerType.getType( crawlerType );

		//If invalid crawler type specified, blow up
		if( cType == null ) {
			throw new InvalidCrawlerTypeException( "Invalid crawler type specified." );
		}

		if( ( CrawlerOptionType.getType( param1 ) == null ) || ( CrawlerOptionType.getType( param2 ) == null )
				|| ( CrawlerOptionType.getType( param3 ) == null ) ) {
			throw new BadParametersException( "A parameter value is invalid." );
		}

		if( ( CrawlerOptionType.getType( param1 ) == CrawlerOptionType.NULL ) &&
				( ( CrawlerOptionType.getType( param2 ) != CrawlerOptionType.NULL ) || ( CrawlerOptionType.getType( param3 ) != CrawlerOptionType.NULL ) ) ) {
			throw new BadParametersException( "Illogical assignment of parameters." );
		}
		if( ( CrawlerOptionType.getType( param2 ) == CrawlerOptionType.NULL ) &&
				( CrawlerOptionType.getType( param3 ) != CrawlerOptionType.NULL ) ) {
			throw new BadParametersException( "Illogical assignment of parameters." );
		}

		Boolean[] params = new Boolean[] { null, null, null };
		params[ 0 ] = CrawlerOptionType.getType( param1 ).getValue();
		params[ 1 ] = CrawlerOptionType.getType( param2 ).getValue();
		params[ 2 ] = CrawlerOptionType.getType( param3 ).getValue();

		switch( cType ) {
		case CONSTRUCT:
			if( !org.lgna.project.ast.Statement.class.isAssignableFrom( crawled_class ) ) {
				throw new IncompatibleNodeTypeException( "Type to crawl is not a statement." );
			}

			descriptor = new edu.wustl.lookingglass.codetest.internal.crawlers.ConstructCrawlerDescriptor(
					(Class<? extends org.lgna.project.ast.Statement>)crawled_class );
			break;
		case EXPRESSION:
			if( !org.lgna.project.ast.Expression.class.isAssignableFrom( crawled_class ) ) {
				throw new IncompatibleNodeTypeException( "Type to crawl is not an expression." );
			}

			descriptor = new edu.wustl.lookingglass.codetest.internal.crawlers.ExpressionCrawlerDescriptor(
					(Class<? extends org.lgna.project.ast.Expression>)crawled_class );
			break;
		case EXPRESSION_STATEMENT:
			if( ( returned_class != org.lgna.project.ast.ExpressionStatement.class )
					&& ( returned_class != org.lgna.project.ast.UserMethod.class )
					&& !org.lgna.project.ast.Statement.class.isAssignableFrom( returned_class ) ) {
				throw new IncompatibleNodeTypeException( "Invalid return type" );
			}

			if( ( CrawlerOptionType.getType( param1 ) == CrawlerOptionType.NULL )
					|| ( CrawlerOptionType.getType( param2 ) == CrawlerOptionType.NULL ) ) {
				throw new BadParametersException( "Not enough parameters specified." );
			}

			descriptor = new edu.wustl.lookingglass.codetest.internal.crawlers.ExpressionStatementCrawlerDescriptor( returned_class, params );
			break;
		case EXPRESSION_STATEMENT_CHUNK:
			if( ( CrawlerOptionType.getType( param1 ) == CrawlerOptionType.NULL )
					|| ( CrawlerOptionType.getType( param2 ) == CrawlerOptionType.NULL ) ) {
				throw new BadParametersException( "Not enough parameters specified." );
			}

			descriptor = new edu.wustl.lookingglass.codetest.internal.crawlers.ExpressionStatementChunkCrawlerDescriptor( returned_class, params );
			break;
		case CONSECUTIVE_EXPRESSION_STATEMENT:
			if( ( returned_class != edu.wustl.lookingglass.codetest.internal.NodeHolder.class )
					&& ( returned_class != org.lgna.project.ast.UserMethod.class )
					&& !org.lgna.project.ast.Statement.class.isAssignableFrom( returned_class ) ) {
				throw new IncompatibleNodeTypeException( "Invalid return type" );
			}

			if( ( CrawlerOptionType.getType( param1 ) == CrawlerOptionType.NULL )
					|| ( CrawlerOptionType.getType( param2 ) == CrawlerOptionType.NULL )
					|| ( CrawlerOptionType.getType( param3 ) == CrawlerOptionType.NULL ) ) {
				throw new BadParametersException( "Not enough parameters specified." );
			}

			descriptor = new edu.wustl.lookingglass.codetest.internal.crawlers.ConsecutiveExpressionStatementCrawlerDescriptor( returned_class, params );
			break;
		case SPREAD_EXPRESSION:
			if( !org.lgna.project.ast.Expression.class.isAssignableFrom( crawled_class ) ) {
				throw new IncompatibleNodeTypeException( "Type to crawl is not an expression." );
			}

			descriptor = new edu.wustl.lookingglass.codetest.internal.crawlers.SpreadExpressionCrawlerDescriptor(
					(Class<? extends org.lgna.project.ast.Expression>)crawled_class );
			break;
		case SPREAD_EXPRESSION_STATEMENT:
			if( ( returned_class != edu.wustl.lookingglass.codetest.internal.NodeHolder.class )
					&& ( returned_class != org.lgna.project.ast.UserMethod.class )
					&& !org.lgna.project.ast.Statement.class.isAssignableFrom( returned_class ) ) {
				throw new IncompatibleNodeTypeException( "Invalid return type" );
			}

			if( ( CrawlerOptionType.getType( param1 ) == CrawlerOptionType.NULL )
					|| ( CrawlerOptionType.getType( param2 ) == CrawlerOptionType.NULL ) ) {
				throw new BadParametersException( "Not enough parameters specified." );
			}

			descriptor = new edu.wustl.lookingglass.codetest.internal.crawlers.SpreadExpressionStatementCrawlerDescriptor( returned_class, params );
			break;
		default:
			throw new InvalidCrawlerTypeException( "Invalid crawler type specified." );
		}

		return new edu.wustl.lookingglass.codetest.CodeTest( descriptor, testCode );
	}

	public static CodeTest getCodeTest( java.io.File ymlFile ) throws java.io.FileNotFoundException {
		return getCodeTestFromYaml( new java.io.FileInputStream( ymlFile ) );
	}

	//TODO: Change this, no need for intermediate step
	public static CodeTest getCodeTestFromYaml( java.io.InputStream yaml ) {
		return CodeTestFactory.getCodeTest( CodeTestYAMLUtilities.getPacketFromYaml( yaml ) );
	}

	public static CodeTest getCodeTest( edu.wustl.lookingglass.community.api.packets.CodeTestPacket packet ) {
		CodeTest test = CodeTestFactory.getCodeTest( packet.getCrawler().getId(), packet.getCrawled().getId(), packet.getReturned().getId(), packet.getParam1().getId(), packet.getParam2().getId(), packet.getParam3().getId(), packet.getSnippet() );
		test.setPacket( packet );
		return test;
	}

	public static edu.wustl.lookingglass.community.api.packets.CodeTestPacket getPacketFromTest( CodeTest test ) {
		return edu.wustl.lookingglass.community.api.packets.CodeTestPacket.createInstance(
				edu.wustl.lookingglass.community.api.packets.CodeTestPacket.CodeTestType.SUGGESTION,
				test.getTestClassName(), "", test.getSnippet(), (Class<? extends CodeTestCrawler>)test.getDescriptor().getCrawlerClass(),
				test.getDescriptor().getParameters(), edu.wustl.lookingglass.community.api.packets.CodeTestPacket.PublishedStatusType.PUBLISHED
				);
	}
}
