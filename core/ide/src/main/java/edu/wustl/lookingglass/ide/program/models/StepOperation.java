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
package edu.wustl.lookingglass.ide.program.models;

import edu.wustl.lookingglass.ide.program.TimeScrubProgramImp;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.ide.program.event.TimeScrubProgramListener;

public abstract class StepOperation extends org.lgna.croquet.Operation {

	protected TimeScrubProgramImp program;
	private TimeScrubProgramListener programListener;

	protected abstract void step();

	protected abstract boolean isStepValid( int index );

	public StepOperation( java.util.UUID id, TimeScrubProgramImp program ) {
		super( edu.wustl.lookingglass.remix.models.ReuseGroup.REUSE_GROUP, id );

		this.program = program;
		this.setEnabled( false );

		this.programListener = new TimeScrubProgramListener() {

			@Override
			public void programStateChange( ProgramStateEvent programStateEvent ) {
				if( !program.getProgramStatementManager().isUpdating() ) {
					setEnabled( isStepValid( program.getProgramStatementManager().getCurrentIndex() ) );
				}
			}

			@Override
			public void startingExecution( ProgramExecutionEvent programExecutionEvent ) {
				setEnabled( isStepValid( program.getProgramStatementManager().getCurrentIndex() ) );
			}

			@Override
			public void endingExecution( ProgramExecutionEvent programExecutionEvent ) {
				setEnabled( isStepValid( program.getProgramStatementManager().getCurrentIndex() ) );
			}

		};
		this.program.addTimeScrubProgramListener( this.programListener );
	}

	public void destroy() {
		this.program.removeTimeScrubProgramListener( this.programListener );
		this.program = null;
		this.programListener = null;
	}

	@Override
	protected void perform( org.lgna.croquet.history.Transaction transaction, org.lgna.croquet.triggers.Trigger trigger ) {
		this.program.pauseProgram();
		step();
	}

	@Override
	public org.lgna.croquet.views.Button createButton( edu.cmu.cs.dennisc.java.awt.font.TextAttribute<?>... textAttributes ) {
		org.lgna.croquet.views.Button rv = super.createButton( textAttributes );
		rv.setVerticalTextPosition( org.lgna.croquet.views.VerticalTextPosition.BOTTOM );
		rv.setHorizontalTextPosition( org.lgna.croquet.views.HorizontalTextPosition.CENTER );
		rv.setIconTextGap( 0 );
		return rv;
	}
}
