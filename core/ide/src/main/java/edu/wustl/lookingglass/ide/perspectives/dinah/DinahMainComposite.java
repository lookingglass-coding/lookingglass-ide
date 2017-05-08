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
package edu.wustl.lookingglass.ide.perspectives.dinah;

import org.alice.ide.IDE;
import org.alice.stageide.perspectives.CodePerspective;
import org.alice.stageide.perspectives.code.CodePerspectiveComposite;
import org.lgna.croquet.SplitComposite;
import org.lgna.croquet.views.SplitPane;
import org.lgna.croquet.views.SwingComponentView;

import edu.cmu.cs.dennisc.java.util.Sets;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.remix.models.StatementMenuOperation;

/**
 * @author Caitlin Kelleher
 */
public class DinahMainComposite extends SplitComposite implements TimeScrubUpdateable {

	private final java.beans.PropertyChangeListener dividerLocationListener;
	private final java.util.Set<StatementMenuOperation> menuOperations = Sets.newHashSet();

	public DinahMainComposite( ExecutionTraceComposite executionTraceComposite, DinahCodeComposite dinahCodeComposite ) {
		super( java.util.UUID.fromString( "3c033f5c-a2ae-4e27-9b84-7f5ace4179e3" ), executionTraceComposite, dinahCodeComposite );

		this.dividerLocationListener = new java.beans.PropertyChangeListener() {
			@Override
			public void propertyChange( java.beans.PropertyChangeEvent e ) {
				updateSplitView( (Integer)e.getNewValue() );
			}
		};
	}

	@Override
	public void setProgramManager( DinahProgramManager programManager ) {
		this.getExecutionTraceComposite().setProgramManager( programManager );
		this.getCodeComposite().setProgramManager( programManager );

		StatementMenuOperationManager.setProgramManager( programManager );
	}

	@Override
	public void removeProgramManager() {
		this.getExecutionTraceComposite().removeProgramManager();
		this.getCodeComposite().removeProgramManager();

		StatementMenuOperationManager.removeProgramManager();
	}

	public DinahCodeComposite getCodeComposite() {
		return (DinahCodeComposite)this.getTrailingComposite();
	}

	public ExecutionTraceComposite getExecutionTraceComposite() {
		return (ExecutionTraceComposite)this.getLeadingComposite();
	}

	private void updateSplitView( int nextWidth ) {
		SwingComponentView<?> view = this.getExecutionTraceComposite().getProgramContainer();
		int prevHeight = view.getHeight();
		int nextHeight = (int)( nextWidth / org.alice.stageide.run.RunComposite.WIDTH_TO_HEIGHT_RATIO );

		if( prevHeight != nextHeight ) {
			view.getAwtComponent().setPreferredSize( new java.awt.Dimension( view.getWidth(), nextHeight ) );
			this.getExecutionTraceComposite().getView().revalidateAndRepaint();
		}
	}

	@Override
	public void update( ProgramStateEvent programStateEvent ) {
		this.getExecutionTraceComposite().update( programStateEvent );
		this.getCodeComposite().update( programStateEvent );

		StatementMenuOperationManager.update( programStateEvent );
	}

	@Override
	public void update( ProgramExecutionEvent programExecutionEvent, boolean isStartEvent ) {
		this.getExecutionTraceComposite().update( programExecutionEvent, isStartEvent );
		this.getCodeComposite().update( programExecutionEvent, isStartEvent );

		for( StatementMenuOperation menuOperation : this.menuOperations ) {
			menuOperation.update( programExecutionEvent, isStartEvent );
		}

		StatementMenuOperationManager.update( programExecutionEvent, isStartEvent );
	}

	@Override
	protected SplitPane createView() {
		SplitPane rv = this.createHorizontalSplitPane();
		rv.setResizeWeight( 0.0 );
		return rv;
	}

	@Override
	public void handlePreActivation() {
		CodePerspective codePerspective = IDE.getActiveInstance().getDocumentFrame().getCodePerspective();
		SplitPane codePerspectivePane = ( (CodePerspectiveComposite)codePerspective.getMainComposite() ).getView();

		this.getView().addDividerLocationChangeListener( this.dividerLocationListener );
		this.getView().setDividerLocation( codePerspectivePane.getDividerLocation() );

		getCodeComposite().handlePreActivation();
	}

	@Override
	public void handlePostDeactivation() {
		CodePerspective codePerspective = IDE.getActiveInstance().getDocumentFrame().getCodePerspective();
		SplitPane codePerspectivePane = ( (CodePerspectiveComposite)codePerspective.getMainComposite() ).getView();

		this.getView().removeDividerLocationChangeListener( this.dividerLocationListener );
		codePerspectivePane.setDividerLocation( this.getView().getDividerLocation() );

		getCodeComposite().handlePostDeactivation();
	}
}
