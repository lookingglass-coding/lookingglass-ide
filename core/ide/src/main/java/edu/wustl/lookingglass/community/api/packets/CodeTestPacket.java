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
package edu.wustl.lookingglass.community.api.packets;

import com.google.gson.annotations.Expose;

public class CodeTestPacket extends edu.wustl.lookingglass.community.api.packets.JsonPacket {

	public static enum CodeTestType {
		SKILL( "Skill", 0 ),
		SUGGESTION( "Suggestion", 1 );

		private final String name;
		private final Integer id;

		private CodeTestType( String name, Integer id ) {
			this.name = name;
			this.id = id;
		}

		public Integer getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public String getRubyString() {
			switch( this.id ) {
			case 0:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "skill" );
			case 1:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "suggestion" );
			default:
				return null;
			}
		}

		public static CodeTestType getType( Integer id ) {
			CodeTestType theType;
			switch( id ) {
			case 0:
				theType = CodeTestType.SKILL;
				break;
			case 1:
				theType = CodeTestType.SUGGESTION;
				break;
			default:
				theType = null;
			}
			assert theType.id.intValue() == id.intValue();
			return theType;
		}
	}

	public static enum PublishedStatusType {
		PRIVATE( "Private", 0 ),
		PUBLISHED( "Published", 1 ),
		DEPRECATED( "Deprecated", 2 );

		private final String status;
		private final Integer id;

		private PublishedStatusType( String status, Integer id ) {
			this.status = status;
			this.id = id;
		}

		public Integer getId() {
			return this.id;
		}

		public String getStatus() {
			return this.status;
		}

		public String getRubyString() {
			switch( this.id ) {
			case 0:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "isprivate" );
			case 1:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "ispublished" );
			case 2:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "isdeprecated" );
			default:
				return null;
			}
		}

		public static PublishedStatusType getType( Integer id ) {
			switch( id ) {
			case 0:
				return PublishedStatusType.PRIVATE;
			case 1:
				return PublishedStatusType.PUBLISHED;
			case 2:
				return PublishedStatusType.DEPRECATED;
			default:
				return null;
			}
		}
	}

	public static enum CrawlerType {
		CONSTRUCT( edu.wustl.lookingglass.codetest.internal.crawlers.ConstructCrawler.class, 0 ),
		EXPRESSION( edu.wustl.lookingglass.codetest.internal.crawlers.ExpressionCrawler.class, 1 ),
		EXPRESSION_STATEMENT( edu.wustl.lookingglass.codetest.internal.crawlers.ExpressionStatementCrawler.class, 2 ),
		EXPRESSION_STATEMENT_CHUNK( edu.wustl.lookingglass.codetest.internal.crawlers.ExpressionStatementChunkCrawler.class, 3 ),
		CONSECUTIVE_EXPRESSION_STATEMENT( edu.wustl.lookingglass.codetest.internal.crawlers.ConsecutiveExpressionStatementCrawler.class, 4 ),
		SPREAD_EXPRESSION( edu.wustl.lookingglass.codetest.internal.crawlers.SpreadExpressionCrawler.class, 5 ),
		SPREAD_EXPRESSION_STATEMENT( edu.wustl.lookingglass.codetest.internal.crawlers.SpreadExpressionStatementCrawler.class, 6 );

		private final Class<?> klass;
		private final Integer id;

		private CrawlerType( Class<?> klass, Integer id ) {
			this.klass = klass;
			this.id = id;
		}

		public Integer getId() {
			return this.id;
		}

		public Class<?> getCrawlerKlass() {
			return this.klass;
		}

		public String getRubyString() {
			switch( this.id ) {
			case 0:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "construct" );
			case 1:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "expression" );
			case 2:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "expression_statement" );
			case 3:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "expression_statement_chunk" );
			case 4:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "consecutive_expression_statement" );
			case 5:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "spread_expression" );
			case 6:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "spread_expression_statement" );
			default:
				return null;
			}
		}

		public static CrawlerType getType( Integer id ) {
			switch( id ) {
			case 0:
				return CrawlerType.CONSTRUCT;
			case 1:
				return CrawlerType.EXPRESSION;
			case 2:
				return CrawlerType.EXPRESSION_STATEMENT;
			case 3:
				return CrawlerType.EXPRESSION_STATEMENT_CHUNK;
			case 4:
				return CrawlerType.CONSECUTIVE_EXPRESSION_STATEMENT;
			case 5:
				return CrawlerType.SPREAD_EXPRESSION;
			case 6:
				return CrawlerType.SPREAD_EXPRESSION_STATEMENT;
			default:
				return null;
			}
		}

		public static CrawlerType getType( Class<?> klass ) {
			if( klass == edu.wustl.lookingglass.codetest.internal.crawlers.ConstructCrawler.class ) {
				return CrawlerType.CONSTRUCT;
			} else if( klass == edu.wustl.lookingglass.codetest.internal.crawlers.ExpressionCrawler.class ) {
				return CrawlerType.EXPRESSION;
			} else if( klass == edu.wustl.lookingglass.codetest.internal.crawlers.ExpressionStatementCrawler.class ) {
				return CrawlerType.EXPRESSION_STATEMENT;
			} else if( klass == edu.wustl.lookingglass.codetest.internal.crawlers.ExpressionStatementChunkCrawler.class ) {
				return CrawlerType.EXPRESSION_STATEMENT_CHUNK;
			} else if( klass == edu.wustl.lookingglass.codetest.internal.crawlers.ConsecutiveExpressionStatementCrawler.class ) {
				return CrawlerType.CONSECUTIVE_EXPRESSION_STATEMENT;
			} else if( klass == edu.wustl.lookingglass.codetest.internal.crawlers.SpreadExpressionCrawler.class ) {
				return CrawlerType.SPREAD_EXPRESSION;
			} else if( klass == edu.wustl.lookingglass.codetest.internal.crawlers.SpreadExpressionStatementCrawler.class ) {
				return CrawlerType.SPREAD_EXPRESSION_STATEMENT;
			} else {
				return null;
			}
		}

		public static CrawlerType getType( String ruby ) {
			if( ruby.equals( "construct" ) ) {
				return CrawlerType.CONSTRUCT;
			} else if( ruby.equals( "expression" ) ) {
				return CrawlerType.EXPRESSION;
			} else if( ruby.equals( "expression_statement" ) ) {
				return CrawlerType.EXPRESSION_STATEMENT;
			} else if( ruby.equals( "expression_statement_chunk" ) ) {
				return CrawlerType.EXPRESSION_STATEMENT_CHUNK;
			} else if( ruby.equals( "consecutive_expression_statement" ) ) {
				return CrawlerType.CONSECUTIVE_EXPRESSION_STATEMENT;
			} else if( ruby.equals( "spread_expression" ) ) {
				return CrawlerType.SPREAD_EXPRESSION;
			} else if( ruby.equals( "spread_expression_statement" ) ) {
				return CrawlerType.SPREAD_EXPRESSION_STATEMENT;
			} else {
				return null;
			}
		}
	}

	public static enum ASTNodeType {
		NodeHolder( edu.wustl.lookingglass.codetest.internal.NodeHolder.class, 0 ),
		NodeHolderList( edu.wustl.lookingglass.codetest.internal.NodeHolderList.class, 1 ),
		AbstractValueLiteral( org.lgna.project.ast.AbstractValueLiteral.class, 2 ),
		CountLoop( org.lgna.project.ast.CountLoop.class, 3 ),
		DoInOrder( org.lgna.project.ast.DoInOrder.class, 4 ),
		DoTogether( org.lgna.project.ast.DoTogether.class, 5 ),
		EachInArrayTogether( org.lgna.project.ast.EachInArrayTogether.class, 6 ),
		ExpressionStatement( org.lgna.project.ast.ExpressionStatement.class, 7 ),
		FieldAccess( org.lgna.project.ast.FieldAccess.class, 8 ),
		ForEachInArrayLoop( org.lgna.project.ast.ForEachInArrayLoop.class, 9 ),
		InfixExpression( org.lgna.project.ast.InfixExpression.class, 10 ),
		MethodCall( org.lgna.project.ast.MethodInvocation.class, 11 ),
		ParameterAccess( org.lgna.project.ast.ParameterAccess.class, 12 ),
		WhileLoop( org.lgna.project.ast.WhileLoop.class, 13 ),
		ConditionalStatement( org.lgna.project.ast.ConditionalStatement.class, 14 ),
		UserMethod( org.lgna.project.ast.UserMethod.class, 15 );

		private final Class<?> klass;
		private final Integer id;

		private ASTNodeType( Class<?> klass, Integer id ) {
			this.klass = klass;
			this.id = id;
		}

		public Integer getId() {
			return this.id;
		}

		public Class<?> getNodeClass() {
			return this.klass;
		}

		public String getRubyString() {
			switch( this.id ) {
			case 0:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "nodeholder" );
			case 1:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "nodeholderlist" );
			case 2:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "abstractvalueliteral" );
			case 3:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "countloop" );
			case 4:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "doinorder" );
			case 5:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "dotogether" );
			case 6:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "eachinarraytogether" );
			case 7:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "expressionstatement" );
			case 8:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "fieldaccess" );
			case 9:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "foreachinarrayloop" );
			case 10:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "infixexpression" );
			case 11:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "methodcall" );
			case 12:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "parameteraccess" );
			case 13:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "whileloop" );
			case 14:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "conditionalstatement" );
			case 15:
				return edu.wustl.lookingglass.codetest.CodeTestYAMLUtilities.makeRubyString( "usermethod" );
			default:
				return null;
			}
		}

		public static ASTNodeType getType( Integer id ) {
			switch( id ) {
			case 0:
				return ASTNodeType.NodeHolder;
			case 1:
				return ASTNodeType.NodeHolderList;
			case 2:
				return ASTNodeType.AbstractValueLiteral;
			case 3:
				return ASTNodeType.CountLoop;
			case 4:
				return ASTNodeType.DoInOrder;
			case 5:
				return ASTNodeType.DoTogether;
			case 6:
				return ASTNodeType.EachInArrayTogether;
			case 7:
				return ASTNodeType.ExpressionStatement;
			case 8:
				return ASTNodeType.FieldAccess;
			case 9:
				return ASTNodeType.ForEachInArrayLoop;
			case 10:
				return ASTNodeType.InfixExpression;
			case 11:
				return ASTNodeType.MethodCall;
			case 12:
				return ASTNodeType.ParameterAccess;
			case 13:
				return ASTNodeType.WhileLoop;
			case 14:
				return ASTNodeType.ConditionalStatement;
			case 15:
				return ASTNodeType.UserMethod;
			default:
				return null;
			}
		}

		public static ASTNodeType getType( Class<?> klass ) {
			if( klass == edu.wustl.lookingglass.codetest.internal.NodeHolder.class ) {
				return ASTNodeType.NodeHolder;
			} else if( klass == edu.wustl.lookingglass.codetest.internal.NodeHolderList.class ) {
				return ASTNodeType.NodeHolderList;
			} else if( klass == org.lgna.project.ast.AbstractValueLiteral.class ) {
				return ASTNodeType.AbstractValueLiteral;
			} else if( klass == org.lgna.project.ast.CountLoop.class ) {
				return ASTNodeType.CountLoop;
			} else if( klass == org.lgna.project.ast.DoInOrder.class ) {
				return ASTNodeType.DoInOrder;
			} else if( klass == org.lgna.project.ast.DoTogether.class ) {
				return ASTNodeType.DoTogether;
			} else if( klass == org.lgna.project.ast.EachInArrayTogether.class ) {
				return ASTNodeType.EachInArrayTogether;
			} else if( klass == org.lgna.project.ast.ExpressionStatement.class ) {
				return ASTNodeType.ExpressionStatement;
			} else if( klass == org.lgna.project.ast.FieldAccess.class ) {
				return ASTNodeType.FieldAccess;
			} else if( klass == org.lgna.project.ast.ForEachInArrayLoop.class ) {
				return ASTNodeType.ForEachInArrayLoop;
			} else if( klass == org.lgna.project.ast.InfixExpression.class ) {
				return ASTNodeType.InfixExpression;
			} else if( klass == org.lgna.project.ast.MethodInvocation.class ) {
				return ASTNodeType.MethodCall;
			} else if( klass == org.lgna.project.ast.ParameterAccess.class ) {
				return ASTNodeType.ParameterAccess;
			} else if( klass == org.lgna.project.ast.WhileLoop.class ) {
				return ASTNodeType.WhileLoop;
			} else if( klass == org.lgna.project.ast.ConditionalStatement.class ) {
				return ASTNodeType.ConditionalStatement;
			} else if( klass == org.lgna.project.ast.UserMethod.class ) {
				return ASTNodeType.UserMethod;
			} else {
				return null;
			}
		}

		public static ASTNodeType getType( String ruby ) {
			if( ruby.equals( "nodeholder" ) ) {
				return ASTNodeType.NodeHolder;
			} else if( ruby.equals( "nodeholderlist" ) ) {
				return ASTNodeType.NodeHolderList;
			} else if( ruby.equals( "abstractvalueliteral" ) ) {
				return ASTNodeType.AbstractValueLiteral;
			} else if( ruby.equals( "countloop" ) ) {
				return ASTNodeType.CountLoop;
			} else if( ruby.equals( "doinorder" ) ) {
				return ASTNodeType.DoInOrder;
			} else if( ruby.equals( "dotogether" ) ) {
				return ASTNodeType.DoTogether;
			} else if( ruby.equals( "eachinarraytogether" ) ) {
				return ASTNodeType.EachInArrayTogether;
			} else if( ruby.equals( "expressionstatement" ) ) {
				return ASTNodeType.ExpressionStatement;
			} else if( ruby.equals( "fieldaccess" ) ) {
				return ASTNodeType.FieldAccess;
			} else if( ruby.equals( "foreachinarrayloop" ) ) {
				return ASTNodeType.ForEachInArrayLoop;
			} else if( ruby.equals( "infixexpression" ) ) {
				return ASTNodeType.InfixExpression;
			} else if( ruby.equals( "methodinvocation" ) ) {
				return ASTNodeType.MethodCall;
			} else if( ruby.equals( "parameteraccess" ) ) {
				return ASTNodeType.ParameterAccess;
			} else if( ruby.equals( "whileloop" ) ) {
				return ASTNodeType.WhileLoop;
			} else if( ruby.equals( "conditionalstatement" ) ) {
				return ASTNodeType.ConditionalStatement;
			} else if( ruby.equals( "usermethod" ) ) {
				return ASTNodeType.UserMethod;
			} else {
				return null;
			}
		}
	}

	public static enum CrawlerOptionType {
		NULL( null, -1 ),
		FALSE( false, 0 ),
		TRUE( true, 1 );

		private final Integer id;
		private final Boolean value;

		private CrawlerOptionType( Boolean value, Integer id ) {
			this.value = value;
			this.id = id;
		}

		public Integer getId() {
			return this.id;
		}

		public Boolean getValue() {
			return this.value;
		}

		public static CrawlerOptionType getType( Integer value ) {
			switch( value ) {
			case -1:
				return CrawlerOptionType.NULL;
			case 0:
				return CrawlerOptionType.FALSE;
			case 1:
				return CrawlerOptionType.TRUE;
			default:
				return null;
			}
		}

		public static CrawlerOptionType getType( Boolean value ) {
			if( value != null ) {
				if( value == true ) {
					return CrawlerOptionType.TRUE;
				} else {
					return CrawlerOptionType.FALSE;
				}
			} else {
				return CrawlerOptionType.NULL;
			}
		}
	}

	@Expose private InnerCodeTest code_test;

	/* package-private */static class InnerCodeTest {
		@Expose( serialize = false ) Integer id;

		@Expose( serialize = false ) Integer user_id;

		@Expose Integer type_cd;

		@Expose String title;

		@Expose String description;

		@Expose String snippet;

		@Expose Integer published_cd;

		@Expose Integer crawler_cd;

		@Expose Integer crawl_cd;

		@Expose Integer return_cd;

		@Expose Integer crawler_param1;

		@Expose Integer crawler_param2;

		@Expose Integer crawler_param3;

		@Expose( serialize = false ) org.joda.time.DateTime created_at;

		@Expose( serialize = false ) org.joda.time.DateTime updated_at;
	}

	@SuppressWarnings( "rawtypes" )
	public static CodeTestPacket createInstance( CodeTestType type, String title, String description, String snippet, Class<? extends edu.wustl.lookingglass.codetest.internal.crawlers.CodeTestCrawler> klass, edu.wustl.lookingglass.codetest.internal.crawlers.CrawlerParameters options, PublishedStatusType published ) {
		CodeTestPacket rv = new CodeTestPacket();
		rv.code_test = new InnerCodeTest();

		rv.setType( type );
		rv.setTitle( title );
		rv.setDescription( description );
		rv.setSnippet( snippet );

		rv.setCrawler( klass );
		rv.setCrawlerOptions( options );
		rv.setPublished( published );

		return rv;
	}

	public CodeTestPacket() {
		this.code_test = new InnerCodeTest();
	}

	@Override
	public boolean isValid() {
		return ( this.code_test != null ) &&
				( this.getType() != null ) &&
				( this.getCrawler() != null );
	}

	public Integer getId() {
		return this.code_test.id;
	}

	public Integer getUserId() {
		return this.code_test.user_id;
	}

	public CodeTestType getType() {
		return CodeTestType.getType( this.code_test.type_cd );
	}

	public String getTitle() {
		return this.code_test.title;
	}

	public String getDescription() {
		return this.code_test.description;
	}

	public String getSnippet() {
		return this.code_test.snippet;
	}

	public org.joda.time.DateTime getCreatedAt() {
		return this.code_test.created_at;
	}

	public org.joda.time.DateTime getUpdatedAt() {
		return this.code_test.updated_at;
	}

	public CrawlerType getCrawler() {
		return CrawlerType.getType( this.code_test.crawler_cd );
	}

	public ASTNodeType getCrawled() {
		return ASTNodeType.getType( this.code_test.crawl_cd );
	}

	public ASTNodeType getReturned() {
		return ASTNodeType.getType( this.code_test.return_cd );
	}

	public CrawlerOptionType getParam1() {
		return CrawlerOptionType.getType( this.code_test.crawler_param1 );
	}

	public CrawlerOptionType getParam2() {
		return CrawlerOptionType.getType( this.code_test.crawler_param2 );
	}

	public CrawlerOptionType getParam3() {
		return CrawlerOptionType.getType( this.code_test.crawler_param3 );
	}

	public PublishedStatusType getPublishedStatus() {
		return PublishedStatusType.getType( this.code_test.published_cd );
	}

	// Only use this during conversion from format that already contains ID into a CodeTestPacket
	public void setId( int id ) {
		this.code_test.id = id;
	}

	public void setTitle( String title ) {
		this.code_test.title = title;
	}

	public void setDescription( String description ) {
		this.code_test.description = description;
	}

	public void setSnippet( String snippet ) {
		this.code_test.snippet = snippet;
	}

	public void setType( CodeTestType type ) {
		this.code_test.type_cd = type.getId();
	}

	private void setPublished( PublishedStatusType published ) {
		this.code_test.published_cd = published.getId();
	}

	private void setCrawlerOptions( edu.wustl.lookingglass.codetest.internal.crawlers.CrawlerParameters options ) {
		this.code_test.crawl_cd = ASTNodeType.getType( options.crawled ).getId();
		this.code_test.return_cd = ASTNodeType.getType( options.returned ).getId();

		this.code_test.crawler_param1 = CrawlerOptionType.getType( options.options[ 0 ] ).getId();
		this.code_test.crawler_param2 = CrawlerOptionType.getType( options.options[ 1 ] ).getId();
		this.code_test.crawler_param3 = CrawlerOptionType.getType( options.options[ 2 ] ).getId();
	}

	@SuppressWarnings( "rawtypes" )
	private void setCrawler( Class<? extends edu.wustl.lookingglass.codetest.internal.crawlers.CodeTestCrawler> klass ) {
		this.code_test.crawler_cd = CrawlerType.getType( klass ).getId();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( this.code_test.title );

		if( this.code_test.published_cd == CodeTestPacket.PublishedStatusType.PUBLISHED.id ) {
			sb.append( " | PUBLISHED" );
		}
		else if( this.code_test.updated_at != null ) {
			sb.append( " | Last edited " + this.code_test.updated_at.toString() );
		}

		return sb.toString();
	}

}
