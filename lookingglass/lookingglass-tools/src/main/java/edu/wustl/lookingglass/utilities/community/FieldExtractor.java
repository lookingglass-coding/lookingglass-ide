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
package edu.wustl.lookingglass.utilities.community;

import org.lgna.project.io.IoUtilities;

import edu.wustl.lookingglass.community.models.ModelField;
import edu.wustl.lookingglass.community.models.ModelField.ModelFieldKeys;
import edu.wustl.lookingglass.remix.SnippetFileUtilities;

public class FieldExtractor {

	public static ModelFieldKeys[] getWorldFields( org.lgna.project.Project project ) {
		return ModelField.getWorldFieldKeys( project );
	}

	public static ModelFieldKeys[] getSnippetFields( edu.wustl.lookingglass.remix.SnippetScript snippet ) {
		return ModelField.getSnippetFieldKeys( snippet );
	}

	public static void main( String[] args ) throws Exception {
		String path = args[ 0 ];
		ModelFieldKeys[] fields = null;
		if( path.endsWith( "." + IoUtilities.PROJECT_EXTENSION ) ) {
			fields = getWorldFields( org.lgna.project.io.IoUtilities.readProject( path ) );
		} else if( path.endsWith( "." + SnippetFileUtilities.SNIPPET_EXTENSION ) ) {
			fields = getSnippetFields( edu.wustl.lookingglass.remix.SnippetFileUtilities.loadSnippet( path ) );
		}

		StringBuilder out = new StringBuilder();
		for( ModelFieldKeys field : fields ) {
			out.append( field.getModelFieldType() ).append( " : " );
			if( field.getAbstractModelFieldType() == null ) {
				out.append( "null" );
			} else {
				out.append( field.getAbstractModelFieldType() );
			}

			out.append( " (" );
			if( field.getCallerStatementCount() == null ) {
				out.append( "null" );
			} else {
				out.append( field.getCallerStatementCount() );
			}
			out.append( ", " );

			if( field.getParameterCount() == null ) {
				out.append( "null" );
			} else {
				out.append( field.getParameterCount() );
			}
			out.append( ")\n" );
		}
		System.out.print( out.toString() );
	}
}
