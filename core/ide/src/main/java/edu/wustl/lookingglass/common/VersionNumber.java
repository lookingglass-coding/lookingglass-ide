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
package edu.wustl.lookingglass.common;

/**
 * Version class that support Semantic Versioning (http://semver.org/)
 *
 * @author Kyle J. Harms
 */
public class VersionNumber implements Comparable<VersionNumber> {

	protected final Integer major;
	protected final Integer minor;
	protected final Integer patch;
	protected final String qualifer;

	protected final String version;

	public class MalformedVersionException extends IllegalArgumentException {
		private static final long serialVersionUID = 4319692934087027786L;

		protected String version;

		public MalformedVersionException( String version ) {
			super();
			this.version = version;
		}

		@Override
		public java.lang.String getMessage() {
			return "malformed version number: " + this.version;
		}
	}

	public VersionNumber( String version ) throws MalformedVersionException {
		this.version = version;

		java.util.regex.Pattern p = java.util.regex.Pattern.compile( "^(\\d+)\\.(\\d+)(\\.(\\d+))?(-(.+))?$" );
		java.util.regex.Matcher m = p.matcher( version );
		if( m.find() ) {
			this.major = Integer.valueOf( m.group( 1 ) );
			this.minor = Integer.valueOf( m.group( 2 ) );
			if( m.group( 4 ) != null ) {
				this.patch = Integer.valueOf( m.group( 4 ) );
			} else {
				this.patch = null;
			}
			if( m.group( 6 ) != null ) {
				this.qualifer = m.group( 6 );
			} else {
				this.qualifer = null;
			}
		} else {
			throw new MalformedVersionException( version );
		}
	}

	public Integer getMajor() {
		return this.major;
	}

	public Integer getMinor() {
		return this.minor;
	}

	public Integer getPatch() {
		return this.patch;
	}

	public String getQualifer() {
		return this.qualifer;
	}

	@Override
	public String toString() {
		return this.version;
	}

	@Override
	public int compareTo( VersionNumber o ) {
		if( this.major > o.major ) {
			return 1;
		} else if( this.major < o.major ) {
			return -1;
		} else {
			if( this.minor > o.minor ) {
				return 1;
			} else if( this.minor < o.minor ) {
				return -1;
			} else {
				if( ( this.patch != null ) && ( o.patch != null ) ) {
					if( this.patch > o.patch ) {
						return 1;
					} else if( this.patch < o.patch ) {
						return -1;
					} else {
						return 0;
					}
				} else if( ( this.patch == null ) && ( o.patch != null ) ) {
					return -1;
				} else if( ( this.patch != null ) & ( o.patch == null ) ) {
					return 1;
				} else {
					return 0;
				}
			}
		}
	}
}
