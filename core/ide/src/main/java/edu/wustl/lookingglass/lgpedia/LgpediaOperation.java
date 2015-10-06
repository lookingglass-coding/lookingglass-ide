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
package edu.wustl.lookingglass.lgpedia;

import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;

/**
 * @author Michael Pogran
 */

public class LgpediaOperation extends org.lgna.croquet.Operation {
	private final org.lgna.project.ast.AbstractNode method;
	private final boolean showDetails;

	public LgpediaOperation( org.lgna.project.ast.AbstractNode method, boolean showDetails ) {
		super( org.lgna.croquet.Application.DOCUMENT_UI_GROUP, java.util.UUID.fromString( "da1f923d-c8b0-42b9-b7f1-a4f48c18b663" ) );
		this.method = method;
		this.showDetails = showDetails;
		setSmallIcon( new edu.wustl.lookingglass.lgpedia.components.LgpediaIcon( 22, findDefaultLocalizedText() ) );
	}

	@Override
	protected void localize() {
		// Do nothing
	}

	@Override
	protected void perform( Transaction transaction, Trigger trigger ) {
		LgpediaComposite.getInstance().setupProjectIfNecessary();
		LgpediaComposite.getInstance().setAstNode( this.method, this.showDetails );

		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			LgpediaComposite.getInstance().getLaunchOperation().fire( trigger );
		} );

		javax.swing.SwingUtilities.invokeLater( ( ) -> {
			LgpediaComposite.getInstance().scrollToSelection( this.showDetails );
		} );
	}

	@Override
	public org.lgna.croquet.views.Button createButton( float fontScalar, edu.cmu.cs.dennisc.java.awt.font.TextAttribute<?>... textAttributes ) {
		return new edu.wustl.lookingglass.lgpedia.components.LgpediaButton( this );
	}
}
