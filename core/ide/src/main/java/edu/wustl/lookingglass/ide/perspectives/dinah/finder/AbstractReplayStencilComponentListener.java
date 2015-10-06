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
package edu.wustl.lookingglass.ide.perspectives.dinah.finder;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import org.alice.ide.IDE;
import org.alice.ide.highlight.IdeHighlightStencil;
import org.alice.ide.perspectives.ProjectPerspective;
import org.lgna.croquet.views.ProgressBar;

import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.wustl.lookingglass.ide.croquet.components.DinahProgressBar;
import edu.wustl.lookingglass.ide.perspectives.dinah.AbstractDinahPerspective;
import edu.wustl.lookingglass.ide.perspectives.dinah.DinahProgramManager;

/**
 * @author Caitlin Kelleher
 */
public abstract class AbstractReplayStencilComponentListener implements ComponentListener {
	private IdeHighlightStencil highlightStencil;
	private ProgressBar progressBar;
	protected DinahProgramManager programManager;

	public AbstractReplayStencilComponentListener( IdeHighlightStencil stencil, DinahProgramManager programManager ) {
		this.highlightStencil = stencil;
		this.programManager = programManager;
	}

	protected abstract void startReplayThread();

	protected abstract void stopReplayThread();

	protected Rectangle getRenderWindowPosition() {
		ProjectPerspective currentPerspective = IDE.getActiveInstance().getDocumentFrame().getPerspectiveState().getValue();
		if( currentPerspective instanceof AbstractDinahPerspective ) {
			AbstractDinahPerspective dinahPerspective = (AbstractDinahPerspective)currentPerspective;

			Shape renderWindowShape = dinahPerspective.getRenderWindow().getShape( this.highlightStencil, null );
			if( renderWindowShape != null ) {
				return renderWindowShape.getBounds();
			}
		} else {
			Logger.severe( "this state shouldn't happen" );
		}

		return null;
	}

	@Override
	public void componentShown( ComponentEvent arg0 ) {
		if( this.programManager.getProgramImp() != null ) {

			this.startReplayThread();
			this.getRenderWindowPosition();

			// add in the animation replay progress bar
			int PAD = 5;
			Rectangle renderWindowPosition = this.getRenderWindowPosition();

			if( renderWindowPosition != null ) {

				if( this.progressBar == null ) {
					this.progressBar = new DinahProgressBar( this.programManager.getProgramImp().getReplayThread().getReplayTimeState().getSwingModel().getBoundedRangeModel() );
				}

				int xLoc = renderWindowPosition.x;
				int yLoc = renderWindowPosition.y + renderWindowPosition.height + PAD;

				synchronized( this.highlightStencil.getTreeLock() ) {
					this.highlightStencil.getAwtComponent().setLayout( null );

					// add in the progress bar
					this.highlightStencil.getAwtComponent().add( this.progressBar.getAwtComponent() );
					this.highlightStencil.revalidateAndRepaint();

					// set it to right location and size
					this.progressBar.setLocation( xLoc, yLoc );
					this.progressBar.getAwtComponent().setSize( renderWindowPosition.width, this.progressBar.getAwtComponent().getPreferredSize().height );

					// repaint to ensure all is well
					this.highlightStencil.repaint();

				}
			}

		}
	}

	@Override
	public void componentResized( ComponentEvent arg0 ) {
	}

	@Override
	public void componentMoved( ComponentEvent arg0 ) {
	}

	@Override
	public void componentHidden( ComponentEvent arg0 ) {
		this.highlightStencil.removeComponentListener( this );

		if( this.programManager.getProgramImp() != null ) {
			this.stopReplayThread();
		}
	}

}
