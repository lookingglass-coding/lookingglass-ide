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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import edu.wustl.lookingglass.codetest.internal.crawlers.CodeTestCrawler;
import edu.wustl.lookingglass.community.api.packets.CodeTestPacket;
import edu.wustl.lookingglass.community.api.packets.CodeTestPacket.CodeTestType;
import edu.wustl.lookingglass.community.api.packets.CodeTestPacket.PublishedStatusType;

/**
 * @author Aaron Zemach
 */
public class CodeTestYAMLUtilities {

	public static String getYamlFromPacket( CodeTestPacket packet, int testId ) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put( "id", testId );
		data.put( "user_id", 1 );
		data.put( "title", packet.getTitle() );
		data.put( "description", packet.getDescription() );
		data.put( "snippet", packet.getSnippet() );
		data.put( "type_cd", packet.getType().getRubyString() );
		data.put( "crawler_cd", packet.getCrawler().getRubyString() );
		data.put( "crawl_cd", packet.getCrawled().getRubyString() );
		data.put( "return_cd", packet.getReturned().getRubyString() );
		data.put( "crawler_param1", packet.getParam1().getId() );
		data.put( "crawler_param2", packet.getParam2().getId() );
		data.put( "crawler_param3", packet.getParam3().getId() );
		data.put( "published_cd", packet.getPublishedStatus().getRubyString() );

		Map<String, Map> wrapper = new HashMap<String, Map>();
		wrapper.put( convertToFileName( packet.getTitle(), testId ), data );

		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );
		options.setLineBreak( DumperOptions.LineBreak.UNIX );

		// SnakeYAML does really strange line break escaping that Ruby YAML does not read correctly.
		// Tell SnakeYAML to not break the lines by setting the column width to as large as we can.
		options.setWidth( Integer.MAX_VALUE );

		StringWriter code = new StringWriter();
		Yaml yaml = new Yaml( options );
		yaml.dump( wrapper, code );
		return code.toString();
	}

	public static HashMap<String, Object> getMapFromYaml( java.io.InputStream yaml ) {
		Yaml loader = new Yaml();
		HashMap<String, Map> result = (HashMap<String, Map>)loader.load( yaml );

		java.util.Set<String> keys = result.keySet();
		java.util.Iterator<String> it = keys.iterator();
		String key = it.next();

		return (HashMap<String, Object>)result.get( key );
	}

	public static CodeTestPacket getPacketFromYaml( java.io.InputStream yaml ) {
		HashMap<String, Object> result = getMapFromYaml( yaml );

		// TODO: Parse this into a separate function, comes up a TON
		Boolean[] params = new Boolean[] { null, null, null };
		params[ 0 ] = CodeTestPacket.CrawlerOptionType.getType( (Integer)result.get( "crawler_param1" ) ).getValue();
		params[ 1 ] = CodeTestPacket.CrawlerOptionType.getType( (Integer)result.get( "crawler_param2" ) ).getValue();
		params[ 2 ] = CodeTestPacket.CrawlerOptionType.getType( (Integer)result.get( "crawler_param3" ) ).getValue();

		CodeTestPacket packet = CodeTestPacket.createInstance(
				CodeTestType.SKILL,
				(String)result.get( "title" ),
				(String)result.get( "description" ),
				(String)result.get( "snippet" ),
				(Class<? extends CodeTestCrawler>)CodeTestPacket.CrawlerType.getType( stripRubyString( (String)result.get( "crawler_cd" ) ) ).getCrawlerKlass(),
				new edu.wustl.lookingglass.codetest.internal.crawlers.CrawlerParameters(
						CodeTestPacket.ASTNodeType.getType( stripRubyString( (String)result.get( "crawl_cd" ) ) ).getNodeClass(),
						CodeTestPacket.ASTNodeType.getType( stripRubyString( (String)result.get( "return_cd" ) ) ).getNodeClass(),
						params ),
				PublishedStatusType.PUBLISHED );

		packet.setId( (int)result.get( "id" ) );

		return packet;
	}

	public static String makeRubyString( String input ) {
		return "<%= CodeTest." + input + " %>";
	}

	public static String stripRubyString( String input ) {
		int start = input.indexOf( "." );
		String parse = input.substring( start + 1 );
		int end = parse.indexOf( " " );
		return parse.substring( 0, end );
	}

	public static String convertToFileName( String title, int num ) {
		String safeName = title.replaceAll( "\\W+", "" ).toLowerCase();
		return num + "_" + safeName;
	}

	// Load and Re-save all tests...
	// This is useful when we change the format of the code tests yaml.
	private static void resaveAllExistingCodeTestFixtures() throws java.io.IOException {
		java.util.ArrayList<CodeTest> codeTests = new java.util.ArrayList<CodeTest>();

		java.io.File ymlDir = new java.io.File( edu.cmu.cs.dennisc.app.ApplicationRoot.getApplicationDirectory(), "/fixtures/code_tests" );
		if( ymlDir.exists() ) {
			java.io.File[] files = ymlDir.listFiles();
			for( java.io.File file : files ) {
				if( file.getName().endsWith( ".yml" ) ) {
					try {
						CodeTest test = edu.wustl.lookingglass.codetest.CodeTestFactory.getCodeTest( file );
						codeTests.add( test );
					} catch( java.io.FileNotFoundException e ) {
						// should never happen.
						e.printStackTrace();
					}
				}
			}
		}

		for( CodeTest test : codeTests ) {
			CodeTestPacket packet = test.getPacket();
			String yml = getYamlFromPacket( packet, packet.getId() );

			java.io.File file = new java.io.File( ymlDir, CodeTestYAMLUtilities.convertToFileName( packet.getTitle(), packet.getId() ) + ".yml" );
			java.io.FileOutputStream os = new java.io.FileOutputStream( file );
			os.write( yml.getBytes() );
			os.close();
		}
	}

	public static void main( String[] args ) throws java.io.IOException {
		resaveAllExistingCodeTestFixtures();
	}
}
