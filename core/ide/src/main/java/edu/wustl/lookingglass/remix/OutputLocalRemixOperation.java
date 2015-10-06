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
package edu.wustl.lookingglass.remix;

import java.io.IOException;

import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;

import edu.wustl.lookingglass.remix.models.EndCaptureState;
import edu.wustl.lookingglass.remix.models.StartCaptureState;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.observer.VMExecutionObserver;

/**
 * @author Michael Pogran
 */
public class OutputLocalRemixOperation extends org.lgna.croquet.Operation {

	public OutputLocalRemixOperation() {
		super( edu.wustl.lookingglass.remix.models.ReuseGroup.REUSE_GROUP, java.util.UUID.fromString( "807bc9cf-ac4c-45a2-a36f-4330cc41661c" ) );

		setEnabled( false );

		StartCaptureState.getInstance().addListener( new org.lgna.croquet.event.ValueListener<AbstractEventNode<?>>() {

			@Override
			public void valueChanged( ValueEvent<AbstractEventNode<?>> e ) {
				if( ( e.getNextValue() != null ) && ( EndCaptureState.getInstance().getValue() != null ) ) {
					setEnabled( true );
				}
			}

		} );

		EndCaptureState.getInstance().addListener( new org.lgna.croquet.event.ValueListener<AbstractEventNode<?>>() {

			@Override
			public void valueChanged( ValueEvent<AbstractEventNode<?>> e ) {
				if( ( e.getNextValue() != null ) && ( StartCaptureState.getInstance().getValue() != null ) ) {
					setEnabled( true );
				}
			}

		} );
	}

	@Override
	protected void perform( Transaction transaction, Trigger trigger ) {
		AbstractEventNode<?> startEventNode = StartCaptureState.getInstance().getValue();
		AbstractEventNode<?> endEventNode = EndCaptureState.getInstance().getValue();
		VMExecutionObserver executionObserver = edu.wustl.lookingglass.ide.LookingGlassIDE.getActiveInstance().getDinahProgramImp().getExecutionObserver();

		if( ( startEventNode != null ) && ( endEventNode != null ) && ( executionObserver != null ) ) {
			RemixSnippetFactory factory = RemixSnippetFactory.getRemixSnippetFactory( startEventNode, endEventNode, executionObserver );
			SnippetScript script = factory.buildSnippetScript();

			String homeDir = System.getProperty( "user.home" );
			java.io.File directory = new java.io.File( homeDir + "/Documents/Looking Glass/Snippets" );

			if( directory.exists() && directory.isDirectory() ) {
				// pass
			} else {
				directory.mkdirs();
			}

			java.io.File snippetFile = org.alice.ide.ProjectApplication.getActiveInstance().getDocumentFrame().showSaveFileDialog( directory, null, "lgr", true );

			if( snippetFile != null ) {
				try {
					String title = snippetFile.getName();
					title = title.substring( 0, title.length() - 4 );
					script.setTitle( title );

					java.io.FileOutputStream outputStream = new java.io.FileOutputStream( snippetFile );
					edu.wustl.lookingglass.remix.SnippetFileUtilities.writeSnippet( outputStream, script );
				} catch( IOException e ) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void localize() {
		super.localize();
		this.setName( "Save Locally" );
	}

}
