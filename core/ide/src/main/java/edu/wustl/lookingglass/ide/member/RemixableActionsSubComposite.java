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
package edu.wustl.lookingglass.ide.member;

import java.util.List;

import javax.swing.event.ListDataEvent;

import org.lgna.project.ast.AbstractMethod;

import edu.wustl.lookingglass.ide.croquet.models.preview.PreviewSnippetComposite;

/**
 * @author Kyle J. Harms
 */
public class RemixableActionsSubComposite extends org.alice.ide.member.MethodsSubComposite {

	private final PreviewSnippetComposite[] previewComposites;
	private final javax.swing.event.ListDataListener dataListener = new javax.swing.event.ListDataListener() {

		@Override
		public void intervalAdded( ListDataEvent e ) {
		}

		@Override
		public void intervalRemoved( ListDataEvent e ) {
		}

		@Override
		public void contentsChanged( ListDataEvent e ) {
			javax.swing.SwingUtilities.invokeLater( ( ) -> {
				getView().refreshLater();
			} );
		}

	};

	public RemixableActionsSubComposite() {
		super( java.util.UUID.fromString( "3fd6a917-4c12-4ce8-a7f2-513271f50cd6" ), true );
		this.previewComposites = new PreviewSnippetComposite[ getListData().getDisplaySize() ];

		for( int i = 0; i < getListData().getDisplaySize(); i++ ) {
			this.previewComposites[ i ] = new PreviewSnippetComposite();
		}
	}

	public edu.wustl.lookingglass.ide.croquet.models.community.data.RemixableActionsListData getListData() {
		return edu.wustl.lookingglass.ide.croquet.models.community.data.RemixableActionsListData.getInstance();
	}

	public PreviewSnippetComposite[] getPreviewComposites() {
		return this.previewComposites;
	}

	public void updateType( org.lgna.project.ast.AbstractType<?, ?, ?> type ) {
		edu.wustl.lookingglass.ide.croquet.models.community.data.RemixableActionsListData.getInstance().updateValues( type );
	}

	@Override
	protected org.alice.ide.member.views.MethodsSubView<?> createView() {
		return new edu.wustl.lookingglass.ide.member.views.RemixableActionsSubView( this );
	}

	@Override
	public List<? extends AbstractMethod> getMethods() {
		return java.util.Collections.emptyList();
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		edu.wustl.lookingglass.ide.croquet.models.community.data.RemixableActionsListData.getInstance().addListener( this.dataListener );
	}

	@Override
	public void handlePostDeactivation() {
		edu.wustl.lookingglass.ide.croquet.models.community.data.RemixableActionsListData.getInstance().removeListener( this.dataListener );
		super.handlePostDeactivation();
	}
}
