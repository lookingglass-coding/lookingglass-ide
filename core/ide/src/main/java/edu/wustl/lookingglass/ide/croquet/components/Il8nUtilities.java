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
package edu.wustl.lookingglass.ide.croquet.components;

import edu.cmu.cs.dennisc.java.util.logging.Logger;

/**
 *
 * @author grosspa
 *
 *         This is a port of the localization functionality that exists in
 *         org.lgna.croquet.Model I have put it in this utilities class rather
 *         than in the component hierarchy because I do not want to complicate
 *         code merges with Dennis if possible, and he should in the future
 *         offer another way to do localization for components that is more
 *         direct.
 *
 *         Two important difference: (1) Other keys in the properties file can
 *         be referenced by using underscores as a prefix and suffix. For
 *         instance, you want to get the text in the key blockTextPlural for use
 *         in another key, then you reference this as __blockTextPlural__ in the
 *         properties file, and this will look up and substitute the text for
 *         you. This is similar to what Dennis does for formatting of templates
 *         text. (2) You can reach across classes for localization, but the key
 *         reference must be qualified within the properties file.
 */

@Deprecated
public class Il8nUtilities {
	private static final String KEY_DELIMITER = "__";
	private static final java.util.regex.Pattern KEY_PATTERN = java.util.regex.Pattern.compile( KEY_DELIMITER + "[a-zA-Z0-9\\.]+?" + KEY_DELIMITER );

	private static String findLocalizedText( Class cls, Class clsRoot, String subKey ) {
		String bundleName = cls.getPackage().getName() + ".croquet";
		try {
			java.util.ResourceBundle resourceBundle = java.util.ResourceBundle.getBundle( bundleName, javax.swing.JComponent.getDefaultLocale() );
			String key = cls.getSimpleName();

			if( subKey != null ) {
				if( subKey.contains( "." ) ) {
					key = subKey;
				} else {
					StringBuilder sb = new StringBuilder();
					sb.append( key );
					sb.append( "." );
					sb.append( subKey );
					key = sb.toString();
				}
			}
			String rv = resourceBundle.getString( key );
			return evaluateResourceString( cls, clsRoot, rv );
		} catch( java.util.MissingResourceException mre ) {
			if( cls == clsRoot ) {
				return null;
			} else {
				return findLocalizedText( cls.getSuperclass(), clsRoot, subKey );
			}
		}
	}

	public static String getLocalizedText( Class cls, String subKey ) {
		return findLocalizedText( cls, cls, subKey );
	}

	public static String getLocalizedText( Object o, String subKey ) {
		return getLocalizedText( o.getClass(), subKey );
	}

	public static String getDefaultLocalizedText( Class cls ) {
		return findLocalizedText( cls, cls, null );
	}

	public static String getDefaultLocalizedText( Object o ) {
		return getDefaultLocalizedText( o.getClass() );
	}

	private static String evaluateResourceString( Class cls, Class rootCls, String s ) {
		java.util.regex.Matcher matcher = KEY_PATTERN.matcher( s );

		java.util.Map<String, String> keyMap = new java.util.HashMap<String, String>();
		while( matcher.find() ) {
			String key = matcher.group().substring( KEY_DELIMITER.length(), matcher.group().length() - KEY_DELIMITER.length() );

			if( !keyMap.containsKey( matcher.group() ) ) {

				keyMap.put( matcher.group(), findLocalizedText( cls, rootCls, key ) );
			}
		}

		for( String k : keyMap.keySet() ) {
			String v = keyMap.get( k );
			if( v != null ) {
				s = s.replaceAll( k, v );
			} else {
				Logger.severe( k, v );
			}
		}

		return s;
	}
}
