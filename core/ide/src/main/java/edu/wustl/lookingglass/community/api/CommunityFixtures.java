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
package edu.wustl.lookingglass.community.api;

import edu.wustl.lookingglass.codetest.CodeTest;
import edu.wustl.lookingglass.community.api.fixtures.AbstractFixture;
import edu.wustl.lookingglass.community.api.fixtures.ModelFieldFixture;
import edu.wustl.lookingglass.community.models.FixtureModel;
import edu.wustl.lookingglass.community.models.ModelField;

/**
 * @author Kyle J. Harms
 */
public class CommunityFixtures {

	public final edu.wustl.lookingglass.community.api.CommunityFixtures fixtures = new CommunityFixtures();

	final private java.util.List<ModelField> modelFields;
	final private java.util.Map<Integer, ModelField> modelFieldsMap;

	private CommunityFixtures() {
		this.modelFields = initializeFixtureList( loadFixtures( ModelFieldFixture.class ) );
		this.modelFieldsMap = initializeFixtureMap( this.modelFields );
		ModelField.initializeModelFields( java.util.Collections.unmodifiableList( this.modelFields ), java.util.Collections.unmodifiableMap( this.modelFieldsMap ) );
	}

	private static <Fixture extends AbstractFixture, Model extends FixtureModel<Fixture>> FixtureModel<?> fixtureModelFactory( Fixture fixture ) {
		if( fixture instanceof ModelFieldFixture ) {
			return new ModelField( (ModelFieldFixture)fixture );
		} else {
			return new FixtureModel<Fixture>( fixture );
		}
	}

	private static <Fixture extends AbstractFixture> java.util.List<Fixture> loadFixtures( Class<Fixture> fixtureClass ) {
		java.util.ArrayList<Fixture> fixtures = new java.util.ArrayList<Fixture>();

		// Convert the classname to the fixture name
		StringBuilder name = new StringBuilder( fixtureClass.getSimpleName().replaceFirst( "Fixture$", "" ) );
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile( "([a-z])([A-Z])" );
		java.util.regex.Matcher matcher = pattern.matcher( name );
		while( matcher.find() ) {
			String l = name.substring( matcher.start( 2 ), matcher.end( 2 ) ).toLowerCase();
			name.replace( matcher.start( 2 ), matcher.end( 2 ), "_" + l );
		}
		String fixture_name = name.toString().toLowerCase() + "s";

		// Check for a yml file
		java.io.File ymlFile = new java.io.File( edu.cmu.cs.dennisc.app.ApplicationRoot.getApplicationDirectory(), "/fixtures/" + fixture_name + ".yml" );
		if( ymlFile.exists() ) {
			try {
				deserializeFixture( fixtureClass, fixtures, new java.io.FileInputStream( ymlFile ) );
			} catch( java.io.FileNotFoundException e ) {
				// should never happen.
				e.printStackTrace();
			}
		}

		return fixtures;
	}

	private static <Fixture extends AbstractFixture> void deserializeFixture( Class<Fixture> fixtureClass, java.util.ArrayList<Fixture> fixtures, java.io.InputStream is ) {
		org.yaml.snakeyaml.Yaml loader = new org.yaml.snakeyaml.Yaml();
		@SuppressWarnings( "unchecked" ) java.util.Map<String, Object> elements = (java.util.Map<String, Object>)loader.load( is );

		for( Object element : elements.values() ) {
			org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml( new org.yaml.snakeyaml.constructor.Constructor( fixtureClass ) );
			@SuppressWarnings( "unchecked" ) Fixture fixture = (Fixture)yaml.load( yaml.dump( element ) );
			fixtures.add( fixture );
		}
	}

	private static <Fixture extends AbstractFixture, Model extends FixtureModel<Fixture>> java.util.List<Model> initializeFixtureList( java.util.List<Fixture> fixtures ) {
		java.util.List<Model> list = new java.util.ArrayList<Model>();
		for( Fixture fixture : fixtures ) {
			@SuppressWarnings( "unchecked" ) Model model = (Model)fixtureModelFactory( fixture );
			assert model != null : fixture;
			list.add( model );
		}
		return list;
	}

	private static <Model extends FixtureModel<?>> java.util.Map<Integer, Model> initializeFixtureMap( java.util.List<Model> models ) {
		java.util.Map<Integer, Model> map = new java.util.concurrent.ConcurrentHashMap<>();
		for( Model model : models ) {
			map.put( model.getFixture().id, model );
		}
		return map;
	}

	private static java.util.List<CodeTest> loadCodeTestFixtures() {
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
		return codeTests;
	}

	private static java.util.Map<Integer, CodeTest> initializeCodeTestMap( java.util.List<CodeTest> list ) {
		java.util.Map<Integer, CodeTest> map = new java.util.concurrent.ConcurrentHashMap<>();
		for( CodeTest test : list ) {
			map.put( test.getPacket().getId(), test );
		}
		return map;
	}
}
