/*******************************************************************************
 * Copyright (c) 2006, 2015, Carnegie Mellon University. All rights reserved.
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
 * 3. Products derived from the software may not be called "Alice", nor may
 *    "Alice" appear in their name, without prior written permission of
 *    Carnegie Mellon University.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Carnegie Mellon University"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 * ANY AND ALL EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT
 * SHALL THE AUTHORS, COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package org.lgna.project.virtualmachine;

import org.lgna.common.ComponentThread;

/**
 * @author Dennis Cosgrove
 */
public abstract class LgnaVmException extends org.lgna.common.LgnaRuntimeException {
	private final ComponentThread thread;
	private final VirtualMachine vm;
	private final LgnaStackTraceElement[] stackTrace;

	public LgnaVmException( String message, VirtualMachine vm ) {
		super( message );
		this.vm = vm;
		this.thread = ComponentThread.currentThread();
		this.stackTrace = this.vm.getStackTrace( this.thread );
	}

	public LgnaVmException( VirtualMachine vm ) {
		this( null, vm );
	}

	public VirtualMachine getVirtualMachine() {
		return this.vm;
	}

	public LgnaStackTraceElement[] getLgnaStackTrace() {
		return this.stackTrace;
	}

	protected abstract void appendDescription( StringBuilder sb );

	@Override
	protected final void appendFormattedString( StringBuilder sb ) {
		//<lg>
		sb.append( "<html>" );
		sb.append( "<body style='font-family:sans-serif; font-size: 1em; background: ghostwhite;'>" );
		sb.append( "<strong>" );
		this.appendDescription( sb );
		sb.append( "</strong>" );
		LgnaStackTraceElement[] lgnaStackTrace = this.getLgnaStackTrace();
		if( lgnaStackTrace != null ) {
			sb.append( "<ul style='margin: 0; list-style: none; padding-left: 1em;'>" );
			for( LgnaStackTraceElement stackTraceElement : lgnaStackTrace ) {
				sb.append( "<li>" );
				if( stackTraceElement != null ) {
					stackTraceElement.appendFormatted( sb );
				} else {
					edu.cmu.cs.dennisc.java.util.logging.Logger.severe();
				}
			}
			sb.append( "</ul>" );
		}
		sb.append( "</body>" );
		sb.append( "</html>" );
		//</lg>
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( super.toString() );
		LgnaStackTraceElement[] lgnaStackTrace = this.getLgnaStackTrace();
		if( lgnaStackTrace != null ) {
			for( LgnaStackTraceElement stackTraceElement : lgnaStackTrace ) {
				if( stackTraceElement != null ) {
					sb.append( "\n\t" + stackTraceElement.toString() );
				}
			}
		}
		return sb.toString();
	}
}
