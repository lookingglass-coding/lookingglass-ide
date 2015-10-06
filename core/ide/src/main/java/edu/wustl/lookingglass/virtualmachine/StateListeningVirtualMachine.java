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
package edu.wustl.lookingglass.virtualmachine;

import org.lgna.project.virtualmachine.ReleaseVirtualMachine;

import edu.wustl.lookingglass.virtualmachine.event.VirtualMachineExecutionStateListener;

public class StateListeningVirtualMachine extends ReleaseVirtualMachine {

	private java.util.Set<VirtualMachineExecutionStateListener> vmStateListeners = edu.cmu.cs.dennisc.java.util.Sets.newHashSet();
	private boolean pauseOnFirstLambda = false;
	private boolean isPaused = false;

	public void addVirtualMachinePauseStateListener( VirtualMachineExecutionStateListener listener ) {
		vmStateListeners.add( listener );
	}

	public void removeVirtualMachinePauseStateListener( VirtualMachineExecutionStateListener listener ) {
		vmStateListeners.remove( listener );
	}

	protected void notifyStateListenersOfPause() {
		for( VirtualMachineExecutionStateListener listener : vmStateListeners ) {
			listener.isChangedToPaused();
		}
	}

	protected void notifyStateListenersofResume() {
		for( VirtualMachineExecutionStateListener listener : vmStateListeners ) {
			listener.isChangedToRunning();
		}
	}

	protected void notifyEndingExecution() {
		for( VirtualMachineExecutionStateListener listener : vmStateListeners ) {
			listener.isEndingExecution();
		}
	}

	@Override
	protected void execute( org.lgna.project.ast.Statement statement ) throws org.lgna.project.virtualmachine.ReturnException {
		pauseThreadIfAppropriate();
		super.execute( statement );
	}

	@Override
	protected org.lgna.project.virtualmachine.UserInstance createInstanceFromUserConstructor( org.lgna.project.ast.NamedUserConstructor constructor, java.lang.Object[] arguments ) {
		return org.lgna.project.virtualmachine.UserInstance.createInstanceWithInverseMap( this, constructor, arguments );
	}

	@Override
	protected Object invoke( org.lgna.project.ast.MethodInvocation mi, Object instance, org.lgna.project.ast.AbstractMethod method, Object... arguments ) {
		pauseThreadIfAppropriate();
		return super.invoke( mi, instance, method, arguments );
	}

	@Override
	public void ENTRY_POINT_invoke( org.lgna.project.virtualmachine.UserInstance instance, org.lgna.project.ast.AbstractMethod method, java.lang.Object... arguments ) {
		try {
			super.ENTRY_POINT_invoke( instance, method, arguments );
		} catch( StopExecutionException exception ) {
			notifyEndingExecution();
		}
	}

	@Override
	public void stopExecution() {
		super.stopExecution();
		notifyEndingExecution();
	}

	@Override
	protected void pushLambdaFrame( org.lgna.project.virtualmachine.UserInstance instance, org.lgna.project.ast.UserLambda lambda, org.lgna.project.ast.AbstractMethod singleAbstractMethod, java.util.Map<org.lgna.project.ast.AbstractParameter, java.lang.Object> map ) {

		if( getPauseOnFirstLambda() ) {
			pauseVirtualMachine();
			setPauseOnFirstLambda( false );
			pauseThreadIfAppropriate();
		}

		if( getCurrentThreadCount() == 0 ) {
			notifyStateListenersofResume();
		}
		super.pushLambdaFrame( instance, lambda, singleAbstractMethod, map );
	}

	@Override
	protected void popFrame() {
		super.popFrame();
		if( getCurrentThreadCount() == 0 ) {
			notifyStateListenersOfPause();
		}
	}

	public boolean isPaused() {
		return isPaused;
	}

	public void setPaused() {
		synchronized( this ) {
			isPaused = true;
		}
	}

	public void setUnpaused() {
		synchronized( this ) {
			isPaused = false;
			notifyAll();
		}
	}

	protected void pauseThreadIfAppropriate() {
		synchronized( this ) {
			if( isPaused ) {
				pauseThread();
			}
		}
	}

	private void pauseThread() {
		assert Thread.holdsLock( this );
		while( isPaused ) {
			try {
				this.wait();
			} catch( InterruptedException ie ) {
				//pass
			}
		}
	}

	public void pauseVirtualMachine() {
		setPaused();
		notifyStateListenersOfPause();
	}

	public void resumeVirtualMachine() {
		notifyStateListenersofResume();
		setUnpaused();
	}

	public void setPauseOnFirstLambda( boolean isStartingInReuseModeState ) {
		this.pauseOnFirstLambda = isStartingInReuseModeState;
	}

	public boolean getPauseOnFirstLambda() {
		return this.pauseOnFirstLambda;
	}
}
