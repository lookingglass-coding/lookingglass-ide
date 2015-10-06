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

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import edu.wustl.lookingglass.ide.community.LoadingDialog;

/**
 * @author Caitlin Kelleher
 */
public abstract class ShowLoadingDialogSwingWorker<T> extends SwingWorker<T, Void> {
	LoadingDialog d;

	public ShowLoadingDialogSwingWorker() {
		d = new LoadingDialog();
	}

	public abstract T getContent() throws ExecutionException, InterruptedException;

	public abstract void processContent( T content );

	@Override
	protected T doInBackground() throws Exception {
		// get the dialog ready and show it
		d.pack();
		edu.cmu.cs.dennisc.java.awt.WindowUtilities.setLocationOnScreenToCenteredWithin( d.getAwtComponent(), d.getAwtComponent() );

		javax.swing.SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				d.setVisible( true );
			}
		} );
		return this.getContent();
	}

	@Override
	protected void done() {
		try {
			T content = this.get();
			d.downloadComplete( "Done!" );
			this.processContent( content );
		} catch( InterruptedException e ) {
			d.downloadFailed( "Oops! We're sorry but Looking Glass couldn't load this project." );
			e.printStackTrace();
		} catch( ExecutionException e ) {

			// todo: this should obviously be more detailed.
			d.downloadFailed( "Oops! We're sorry but Looking Glass couldn't load this project." );
			e.getCause().printStackTrace();
		}
	}
}
