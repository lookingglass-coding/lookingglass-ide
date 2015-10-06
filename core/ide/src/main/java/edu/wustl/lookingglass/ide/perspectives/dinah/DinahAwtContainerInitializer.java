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

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.lgna.croquet.views.SwingComponentView;
import org.lgna.story.implementation.ProgramImp.AwtContainerInitializer;

import edu.cmu.cs.dennisc.render.OnscreenRenderTarget;

/**
 * @author Michael Pogran
 */
public class DinahAwtContainerInitializer implements AwtContainerInitializer {
	private final org.lgna.croquet.Operation operation; // overlayed action button
	private final java.awt.Container awtContainer; // container for OnScreenLookingGlass panel

	public DinahAwtContainerInitializer( SwingComponentView<?> componentView, org.lgna.croquet.Operation operation ) {
		this.operation = operation;
		this.awtContainer = componentView.getAwtComponent();
	}

	@Override
	public java.awt.Container getAwtContainer() {
		return this.awtContainer;
	}

	@Override
	public void addComponents( OnscreenRenderTarget<?> onscreenRenderTarget ) {
		if( onscreenRenderTarget.getAwtComponent() instanceof JPanel ) {

			JPanel panel = (JPanel)onscreenRenderTarget.getAwtComponent();
			JButton button = operation.createButton().getAwtComponent();
			SpringLayout layout = new SpringLayout();

			panel.setLayout( layout );
			panel.add( button, BorderLayout.CENTER );

			layout.putConstraint( SpringLayout.EAST, button, -5, SpringLayout.EAST, panel );
			layout.putConstraint( SpringLayout.SOUTH, button, -5, SpringLayout.SOUTH, panel );
		}
		this.awtContainer.add( onscreenRenderTarget.getAwtComponent() );
	}

}
