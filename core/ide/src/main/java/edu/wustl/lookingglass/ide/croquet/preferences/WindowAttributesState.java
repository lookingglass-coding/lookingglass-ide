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
package edu.wustl.lookingglass.ide.croquet.preferences;

public class WindowAttributesState extends org.lgna.croquet.preferences.PreferenceStringState {
	private static class SingletonHolder {
		private static WindowAttributesState instance = new WindowAttributesState();
	}

	public static WindowAttributesState getInstance() {
		return SingletonHolder.instance;
	}

	private org.lgna.croquet.StringState.ValueListener<String> observer = new org.lgna.croquet.StringState.ValueListener<String>() {

		@Override
		public void changing( org.lgna.croquet.State<String> state, String prevValue,
				String nextValue, boolean isAdjusting ) {
		}

		@Override
		public void changed( org.lgna.croquet.State<String> state, String prevValue,
				String nextValue, boolean isAdjusting ) {
			updateValues( nextValue );
		}
	};

	// These come from LaunchUtilities.java
	private static final Integer DEFAULT_X = 0;
	private static final Integer DEFAULT_Y = 0;
	private static final Integer DEFAULT_WIDTH = 1024;
	private static final Integer DEFAULT_HEIGHT = 768;
	private static final Boolean DEFAULT_MAXIMIZED = true;
	private static final String DEFAULT_PREFERENCES = "x:" + DEFAULT_X + " y:" + DEFAULT_Y + " width:" + DEFAULT_WIDTH + " height:" + DEFAULT_HEIGHT + ( DEFAULT_MAXIMIZED ? " state:maximized" : "" );

	private Integer xPreference = DEFAULT_X;
	private Integer yPreference = DEFAULT_Y;
	private Integer widthPreference = DEFAULT_WIDTH;
	private Integer heightPreference = DEFAULT_HEIGHT;
	private Boolean maximizedPreference = DEFAULT_MAXIMIZED;

	protected Boolean ignorePreference = false;

	private WindowAttributesState() {
		super( org.lgna.croquet.Application.APPLICATION_UI_GROUP, java.util.UUID.fromString( "13ab3e4f-3559-4c8a-b2af-d62ea88da27a" ), DEFAULT_PREFERENCES );
		updateValues( this.getValue() );
		addValueListener( this.observer );

		// In user study mode, we sometimes run fullscreen to keep the participants on task. So we don't want to use this preference
		if( edu.wustl.lookingglass.study.StudyConfiguration.INSTANCE.isFullScreenApplicationEnabled() ) {
			this.setIgnorePreference( true );
		}
	}

	public void setIgnorePreference( Boolean override ) {
		this.ignorePreference = override;
	}

	protected void updateValues( String state ) {
		java.util.regex.Pattern p;
		java.util.regex.Matcher m;

		// x
		p = java.util.regex.Pattern.compile( ".*x:\\s*(\\d+).*" );
		m = p.matcher( state );
		if( m.matches() ) {
			this.xPreference = Integer.valueOf( m.group( 1 ) );
		}

		// y
		p = java.util.regex.Pattern.compile( ".*y:\\s*(\\d+).*" );
		m = p.matcher( state );
		if( m.matches() ) {
			this.yPreference = Integer.valueOf( m.group( 1 ) );
		}

		// width
		p = java.util.regex.Pattern.compile( ".*width:\\s*(\\d+).*" );
		m = p.matcher( state );
		if( m.matches() ) {
			this.widthPreference = Integer.valueOf( m.group( 1 ) );
		}

		// height
		p = java.util.regex.Pattern.compile( ".*height:\\s*(\\d+).*" );
		m = p.matcher( state );
		if( m.matches() ) {
			this.heightPreference = Integer.valueOf( m.group( 1 ) );
		}

		// maximized
		p = java.util.regex.Pattern.compile( ".*state:\\s*(maximized).*" );
		m = p.matcher( state );
		this.maximizedPreference = m.matches();
	}

	public void saveWindowAttributes( org.lgna.croquet.views.Frame frame ) {
		if( !this.ignorePreference ) {
			Boolean isMaximized = ( frame.getAwtComponent().getExtendedState() & java.awt.Frame.MAXIMIZED_BOTH ) == java.awt.Frame.MAXIMIZED_BOTH;
			java.awt.Point location = frame.getLocation();
			java.awt.Dimension size = frame.getSize();

			String stateString = "";
			if( isMaximized ) {
				stateString += " state:maximized";
			}

			this.setValueTransactionlessly( "x:" + location.x + " y:" + location.y + " width:" + size.width + " height:" + size.height + stateString );
		}
	}

	public void loadWindowAttributes( org.lgna.croquet.views.Frame frame ) {
		final java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int x;
		int y;
		int width;
		int height;
		boolean maximized;

		if( this.ignorePreference ) {
			x = DEFAULT_X;
			y = DEFAULT_Y;
			width = DEFAULT_WIDTH;
			height = DEFAULT_HEIGHT;
			maximized = DEFAULT_MAXIMIZED;
		} else {
			x = this.xPreference;
			y = this.yPreference;
			width = this.widthPreference;
			height = this.heightPreference;
			maximized = this.maximizedPreference;
		}

		// Make sure these are good values to use.
		if( ( x < 0 ) || ( x > screen.width ) ) {
			x = DEFAULT_X;
		}
		if( ( y < 0 ) || ( y > screen.height ) ) {
			y = DEFAULT_Y;
		}
		if( ( width < 120 ) || ( width > screen.width ) ) {
			width = DEFAULT_WIDTH;
		}
		if( ( height < 100 ) || ( height > screen.height ) ) {
			height = DEFAULT_HEIGHT;
		}

		// You must set this size before you maximize or else the frame size gets set to zero.
		frame.setLocation( x, y );
		frame.setSize( width, height );

		if( maximized ) {
			frame.maximize();
		} else {
			frame.getAwtComponent().setExtendedState( javax.swing.JFrame.NORMAL );
		}
	}
}
