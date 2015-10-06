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
package edu.wustl.lookingglass.ide.croquet.models.community;

import org.lgna.croquet.BooleanState;

public abstract class AbstractIsProgramExecutingState extends BooleanState {
	protected org.lgna.story.implementation.ProgramImp currentExecutingProgram = null;
	protected org.alice.stageide.program.RunProgramContext programContext = null;
	protected org.lgna.croquet.views.SwingComponentView<?> executingProgramContainer = null;
	protected org.lgna.common.ProgramExecutionExceptionHandler exceptionHandler = null;

	protected abstract void programRun();

	protected abstract void programStop();

	protected AbstractIsProgramExecutingState( java.util.UUID uuid ) {
		super( org.alice.ide.IDE.RUN_GROUP, uuid, false );

		setIconForTrueAndIconForFalse(
				getStopProgramIcon(),
				getRunProgramIcon() );

		this.addValueListener( new org.lgna.croquet.State.ValueListener<Boolean>() {
			@Override
			public void changing( org.lgna.croquet.State<Boolean> state, Boolean prevValue, Boolean nextValue, boolean isAdjusting ) {
				if( nextValue ) {
					programRun();
				} else {
					programStop();
				}
			}

			@Override
			public void changed( org.lgna.croquet.State<Boolean> state, Boolean prevValue, Boolean nextValue, boolean isAdjusting ) {
				//				getRunOperationInstance().setEnabled(!nextValue);
				//				getStopOperationInstance().setEnabled(nextValue);
			}
		} );
	}

	public org.lgna.story.implementation.ProgramImp getExecutingProgram() {
		return currentExecutingProgram;
	}

	public org.lgna.croquet.views.SwingComponentView<?> getExecutingProgramContainer() {
		return executingProgramContainer;
	}

	@Override
	public org.lgna.croquet.views.PushButton createPushButton() {
		class ProgramRunStatePushButton extends org.lgna.croquet.views.PushButton implements org.lgna.croquet.State.ValueListener<Boolean> {
			public ProgramRunStatePushButton() {
				super( AbstractIsProgramExecutingState.this );
			}

			@Override
			protected javax.swing.JButton createAwtComponent() {
				javax.swing.JButton rv = new javax.swing.JButton();
				return rv;
			}

			@Override
			public void changing( org.lgna.croquet.State<Boolean> state, Boolean prevValue, Boolean nextValue, boolean isAdjusting ) {
			}

			@Override
			public void changed( org.lgna.croquet.State<Boolean> state, Boolean prevValue, Boolean nextValue, boolean isAdjusting ) {
				if( nextValue ) {
					paintStopProgram();
				} else {
					paintRunProgram();
				}
			}

			private void paintStopProgram() {
				getAwtComponent().setBackground( getStopBackgroundColor() );
				getAwtComponent().setToolTipText( findLocalizedText( "stopToolTip" ) );
				getAwtComponent().setIcon( getTrueIcon() );
			}

			private void paintRunProgram() {
				getAwtComponent().setBackground( getRunBackgroundColor() );
				getAwtComponent().setToolTipText( findLocalizedText( "runToolTip" ) );
				getAwtComponent().setIcon( getFalseIcon() );
			}
		}

		ProgramRunStatePushButton rv = new ProgramRunStatePushButton();
		javax.swing.JButton awtButton = rv.getAwtComponent();
		rv.paintRunProgram();

		awtButton.setMargin( new java.awt.Insets( 6, 6, 6, 6 ) );

		// Hack for Nimbus
		javax.swing.UIDefaults d = new javax.swing.UIDefaults();
		d.put( "Button.contentMargins", new java.awt.Insets( 0, 0, 0, 0 ) );
		awtButton.putClientProperty( "Nimbus.Overrides", d );

		awtButton.setText( null );

		this.addValueListener( rv );

		return rv;
	}

	public org.alice.stageide.program.RunProgramContext getProgramContext() {
		return this.programContext;
	}

	static public javax.swing.Icon getRunProgramIcon() {
		return edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "world-start", org.lgna.croquet.icon.IconSize.SMALL );
	}

	static public java.awt.Color getRunBackgroundColor() {
		return new java.awt.Color( 72, 174, 45 );
	}

	static public javax.swing.Icon getStopProgramIcon() {
		return edu.wustl.lookingglass.ide.LookingGlassTheme.getIcon( "world-stop", org.lgna.croquet.icon.IconSize.SMALL );
	}

	static public java.awt.Color getStopBackgroundColor() {
		return java.awt.Color.red.darker();
		// return null;
	}
}
