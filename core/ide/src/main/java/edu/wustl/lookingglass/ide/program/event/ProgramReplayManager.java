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
package edu.wustl.lookingglass.ide.program.event;

import org.lgna.common.ComponentThread;

import edu.wustl.lookingglass.ide.program.ReplayableProgramImp;
import edu.wustl.lookingglass.ide.program.TimeScrubProgramImp;
import edu.wustl.lookingglass.ide.program.thread.ContinuousReplayTimePeriodThread;
import edu.wustl.lookingglass.ide.program.thread.RecordReplayTimePeriodThread;
import edu.wustl.lookingglass.ide.program.thread.ReplayThread;
import edu.wustl.lookingglass.ide.program.thread.ReplayTimePeriodThread;
import edu.wustl.lookingglass.ide.program.thread.ReplayToLiveThread;
import edu.wustl.lookingglass.media.ImagesToWebmEncoder;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;

public class ProgramReplayManager {

	private final ReplayableProgramImp program;

	public ProgramReplayManager( ReplayableProgramImp program ) {
		this.program = program;

	}

	public ReplayThread createContinuousThreadDependentReplayThread( AbstractEventNode start, AbstractEventNode end, AbstractEventNode sharedParent ) {
		return createContinuousReplayThread( start.getStartTime(), end.getEndTime(), sharedParent.getThread(), sharedParent );
	}

	public ReplayThread createRecordAndReplayThread( final double startReplayTime, final double endReplayTime, ComponentThread replayThreadContext, AbstractEventNode replayNode, ImagesToWebmEncoder encoder ) {
		return new RecordReplayTimePeriodThread( this.program, startReplayTime, endReplayTime, replayThreadContext, replayNode, encoder );
	}

	public ReplayThread createContinuousReplayThread( AbstractEventNode replayNode, boolean replayOnlyNodeThreadContext ) {
		ComponentThread nodeThreadContext = replayOnlyNodeThreadContext ? replayNode.getThread() : null;
		return createContinuousReplayThread( replayNode.getStartTime(), replayNode.getEndTime(), nodeThreadContext, replayNode );
	}

	public ReplayThread createContinuousReplayThread( final double startReplayTime, final double endReplayTime, ComponentThread replayThreadContext, AbstractEventNode replayNode ) {
		return new ContinuousReplayTimePeriodThread( this.program, startReplayTime, endReplayTime, replayThreadContext, replayNode );
	}

	public ReplayThread createReplayAndContinueThread( edu.wustl.lookingglass.ide.program.ReplayableProgramImp programToContinue ) {
		return new ReplayToLiveThread( programToContinue );
	}

	public ReplayThread createReplayTimePeriodThread( double startTime, double endTime, ComponentThread replayThreadContext, AbstractEventNode replayNode ) {
		return new ReplayTimePeriodThread( this.program, startTime, endTime, replayThreadContext, replayNode );
	}

	public ReplayThread createStepReplayThread() {
		if( this.program instanceof TimeScrubProgramImp ) {
			return new edu.wustl.lookingglass.ide.program.thread.StepReplayThread( (TimeScrubProgramImp)this.program );
		} else {
			return null;
		}
	}

}
