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

import java.awt.Dimension;

import org.alice.ide.uricontent.UriContentLoader;
import org.lgna.croquet.SingleValueCreatorInputDialogCoreComposite;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.event.ValueListener;
import org.lgna.croquet.views.AbstractWindow;
import org.lgna.croquet.views.BorderPanel;
import org.lgna.croquet.views.Panel;

import edu.wustl.lookingglass.ide.community.connection.ConnectionCardOwnerComposite;
import edu.wustl.lookingglass.ide.croquet.models.preview.PreviewProjectComposite;

/**
 * @author Caitlin Kelleher
 */
public class SelectProjectToCaptureRemixComposite extends SingleValueCreatorInputDialogCoreComposite<Panel, UriContentLoader<?>> {
	private final ErrorStatus noSelectionError;
	private final ValueListener<UriContentLoader<?>> remixSelectionListener;
	private final ValueListener<Boolean> validProjectListener;

	private final PreviewProjectComposite previewWorldComposite;
	private final RemixSelectionComposite remixesCard;

	private final RemixContentInfoDetailsComposite remixContentInfoDetailsComposite;
	private final ConnectionCardOwnerComposite connectionCardOwnerComposite;

	public SelectProjectToCaptureRemixComposite() {
		super( java.util.UUID.fromString( "ec3fa276-6a5a-4f40-a0f9-f8ea447ea957" ) );

		this.noSelectionError = this.createErrorStatus( "noSelectionError" );

		this.remixSelectionListener = new ValueListener<UriContentLoader<?>>() {
			@Override
			public void valueChanged( ValueEvent<UriContentLoader<?>> e ) {
				remixContentInfoDetailsComposite.handleMetaStateValueChanged( e.getNextValue() );
			}

		};

		this.validProjectListener = new ValueListener<Boolean>() {
			@Override
			public void valueChanged( ValueEvent<Boolean> e ) {
				updateIsGoodToGo( e.getNextValue() );
			}
		};

		this.previewWorldComposite = new PreviewProjectComposite();
		this.remixesCard = new RemixSelectionComposite();

		this.remixContentInfoDetailsComposite = new RemixContentInfoDetailsComposite( this.previewWorldComposite );
		this.connectionCardOwnerComposite = new ConnectionCardOwnerComposite( this.remixesCard );

		this.registerSubComposite( this.connectionCardOwnerComposite );
		this.registerSubComposite( remixContentInfoDetailsComposite );
	}

	@Override
	protected Dimension calculateWindowSize( AbstractWindow<?> window ) {
		return new Dimension( 900, 600 );
	}

	public ConnectionCardOwnerComposite getConnectionCardOwnerComposite() {
		return this.connectionCardOwnerComposite;
	}

	public AbstractContentInfoDetailsComposite getProgramDetailsViewComposite() {
		return this.remixContentInfoDetailsComposite;
	}

	@Override
	protected Panel createView() {
		Panel rv = new BorderPanel.Builder().center( this.connectionCardOwnerComposite.getView() ).lineEnd( this.remixContentInfoDetailsComposite.getView() ).build();
		rv.setBackgroundColor( org.alice.ide.projecturi.views.TabContentPanel.DEFAULT_BACKGROUND_COLOR );
		return rv;
	}

	@Override
	protected UriContentLoader<?> createValue() {
		return this.remixesCard.getMetaState().getValue();
	}

	@Override
	protected Status getStatusPreRejectorCheck( org.lgna.croquet.history.CompletionStep<?> step ) {
		UriContentLoader<?> uriContentLoader = this.remixesCard.getMetaState().getValue();
		if( uriContentLoader != null ) {
			return IS_GOOD_TO_GO_STATUS;
		} else {
			return this.noSelectionError;
		}
	}

	@Override
	protected void handlePreShowDialog( org.lgna.croquet.history.CompletionStep<?> completionStep ) {
		super.handlePreShowDialog( completionStep );

		this.remixesCard.getMetaState().pushActivation( completionStep );
		this.remixesCard.getMetaState().addValueListener( this.remixSelectionListener );
		this.previewWorldComposite.getValidProjectState().addAndInvokeNewSchoolValueListener( this.validProjectListener );
	}

	@Override
	protected void handlePostHideDialog( org.lgna.croquet.history.CompletionStep<?> completionStep ) {
		this.remixesCard.getMetaState().popActivation();
		this.remixesCard.getMetaState().removeValueListener( remixSelectionListener );
		this.previewWorldComposite.getValidProjectState().removeNewSchoolValueListener( this.validProjectListener );

		super.handlePostHideDialog( completionStep );
	}
}
