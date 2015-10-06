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
package edu.wustl.lookingglass.ide.perspectives.openproject;

import org.alice.ide.uricontent.UriProjectLoader;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.event.ValueListener;

import edu.wustl.lookingglass.ide.croquet.models.preview.PreviewProjectComposite;
import edu.wustl.lookingglass.ide.perspectives.openproject.views.OpenProjectPreviewWorldPanel;

/**
 * @author user
 */
public class OpenProjectDetailWithPreviewComposite extends OpenProjectAbstractDetailComposite {
	private final PreviewProjectComposite previewProjectComposite;
	private final OpenProjectPreviewWorldPanel previewWorldPanel;

	private final ValueListener<Boolean> validProjectListener = new ValueListener<Boolean>() {

		@Override
		public void valueChanged( ValueEvent<Boolean> e ) {
			javax.swing.SwingUtilities.invokeLater( ( ) -> {
				loadUriOperation.setEnabled( e.getNextValue() );
			} );
		}
	};

	public OpenProjectDetailWithPreviewComposite() {
		super( java.util.UUID.fromString( "a67bb68a-9518-4919-ad15-50be8fb34498" ) );
		this.previewProjectComposite = new PreviewProjectComposite();
		this.previewWorldPanel = new OpenProjectPreviewWorldPanel( this.previewProjectComposite, this );
		this.registerSubComposite( this.previewProjectComposite );
	}

	@Override
	public void update( UriProjectLoader projectLoader ) {
		super.update( projectLoader );
		this.previewWorldPanel.setSelectedUriContentLoader( projectLoader );
		try {
			this.previewProjectComposite.loadProject( projectLoader );
			this.loadUriOperation.setEnabled( true );
		} catch( Exception e ) {
			this.loadUriOperation.setEnabled( false );
		}
	}

	@Override
	protected OpenProjectPreviewWorldPanel createView() {
		return previewWorldPanel;
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		this.previewProjectComposite.getValidProjectState().addAndInvokeNewSchoolValueListener( this.validProjectListener );
	}

	@Override
	public void handlePostDeactivation() {
		this.previewProjectComposite.getValidProjectState().removeNewSchoolValueListener( this.validProjectListener );
		super.handlePostDeactivation();
	}
}
