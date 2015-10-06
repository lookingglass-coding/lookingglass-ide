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
package edu.wustl.lookingglass.animation;

import org.lgna.common.ComponentThread;

import edu.cmu.cs.dennisc.animation.ClockBasedAnimator;
import edu.cmu.cs.dennisc.animation.WaitingAnimation;
import edu.wustl.lookingglass.animation.event.AnimatorTimeListener;

/**
 *
 * @author grosspa
 *
 *         This animator has two major functions: -- To remember what VM thread
 *         originally added an animation, and thus caused it to happen --
 *         Broadcast casting the animator's time to objects that need to display
 *         the current time
 *
 */
public class TimeAndThreadTrackingAnimator extends ClockBasedAnimator {

	protected java.util.HashSet<AnimatorTimeListener> timeListeners = new java.util.HashSet<AnimatorTimeListener>();
	protected ComponentThread updatingThread = null;

	@Override
	protected void preAnimationUpdate( WaitingAnimation waitingAnimation ) {
		this.updatingThread = waitingAnimation.getThread();
	}

	public ComponentThread getSourceThreadForCurrentAnimation() {
		return this.updatingThread;
	}

	public void addAnimatorTimeListener( AnimatorTimeListener listener ) {
		this.timeListeners.add( listener );
	}

	public void removeAnimatorTimeListener( AnimatorTimeListener listener ) {
		this.timeListeners.remove( listener );
	}

	@Override
	protected void updateCurrentTime( boolean isPaused ) {
		super.updateCurrentTime( isPaused );

		if( isPaused ) {
			//pass
		} else {
			double time = this.getCurrentTime();
			for( AnimatorTimeListener listener : this.timeListeners ) {
				listener.notifyCurrentTime( time );
			}
		}

	}
}
