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
package edu.wustl.lookingglass.remix.share;

import org.lgna.croquet.CancelException;
import org.lgna.croquet.SimpleComposite;
import org.lgna.croquet.edits.Edit;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.croquet.views.MigPanel;

/**
 * @author Michael Pogran
 */
public class ConfirmUpdateComposite extends SimpleComposite<org.lgna.croquet.views.MigPanel> {
	private final org.lgna.croquet.Operation confirmOperation;
	private final org.lgna.croquet.Operation cancelOperation;

	public ConfirmUpdateComposite( RecordWorldPage parentComposite ) {
		super( java.util.UUID.fromString( "fffc54f6-1e95-456d-9854-b7037e42a527" ) );

		this.confirmOperation = this.createActionOperation( "confirmOperation", new Action() {

			@Override
			public Edit perform( CompletionStep<?> step, org.lgna.croquet.AbstractComposite.InternalActionOperation source ) throws CancelException {
				parentComposite.getOwner().setIsUpdate( true );
				parentComposite.closeUpdateDialog();
				return null;
			}

		} );

		this.cancelOperation = this.createActionOperation( "cancelOperation", new Action() {

			@Override
			public Edit perform( CompletionStep<?> step, org.lgna.croquet.AbstractComposite.InternalActionOperation source ) throws CancelException {
				parentComposite.closeUpdateDialog();
				return null;
			}

		} );
	}

	@Override
	protected MigPanel createView() {
		MigPanel rv = new MigPanel( this, "fill", "[][]", "[][]" );
		rv.addComponent( new org.lgna.croquet.views.PlainMultiLineLabel( findLocalizedText( "message" ) ), "cell 0 0, spanx 2, grow" );
		rv.addComponent( confirmOperation.createButton(), "cell 0 1" );
		rv.addComponent( cancelOperation.createButton(), "cell 1 1" );

		return rv;
	}
}
