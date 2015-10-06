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
package edu.wustl.lookingglass.croquetfx.models;

import java.util.function.Consumer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.croquetfx.exceptions.FxInvalidException;

/**
 * @author Kyle J. Harms
 */
public class FxEventPropertyModel<FxEvent extends javafx.event.Event> extends FxPropertyModel<EventHandler<FxEvent>> {

	// This is not how this should be coded. This should not be a weak reference, but croquet
	// decides that it should never let anything garbage collect and so this will be stored in the
	// transaction history forever.
	protected final ObjectProperty<javafx.event.EventHandler<FxEvent>> controlProperty;
	protected final Consumer<FxEvent> eventHandler;

	private final Object control;

	private final java.util.concurrent.Semaphore eventLock = new java.util.concurrent.Semaphore( 1 );

	public FxEventPropertyModel( FxComponent component, Object control, ObjectProperty<EventHandler<FxEvent>> controlProperty, Consumer<FxEvent> eventHandler ) {
		super( component, control, (Property<EventHandler<FxEvent>>)controlProperty, null );

		this.controlProperty = controlProperty;
		this.eventHandler = eventHandler;

		synchronized( controlProperty ) {
			assert this.controlProperty.get() == null : "event handler overwritten: " + control;
			this.controlProperty.set( this::handlePropertyEvent );
			this.control = this.controlProperty.get();
		}
	}

	@Override
	public void initializeModel( FxComponent.Block block ) {
		assert javafx.application.Platform.isFxApplicationThread();

		this.eventLock.acquireUninterruptibly();
		block.execute();
		this.eventLock.release();
	}

	@Override
	protected void handlePropertyChanged( ObservableValue<? extends EventHandler<FxEvent>> observable, EventHandler<FxEvent> oldValue, EventHandler<FxEvent> newValue ) {
		// In this situation. The property is not useful.
		// However, do check to see if the user decided to override the CroquetFX eventHandler.
		synchronized( controlProperty ) {
			if( ( oldValue != null ) && ( newValue != this.control ) ) {
				throw new FxInvalidException( "event handler overwritten; pass event handler instead." );
			}
		}
	}

	protected void handlePropertyEvent( FxEvent event ) {
		assert javafx.application.Platform.isFxApplicationThread();

		// Sometimes we don't want this code executed, try to get the lock, if we fail then we don't want it executed
		if( this.eventLock.tryAcquire() ) {
			this.eventLock.release();

			org.lgna.croquet.triggers.Trigger trigger = org.lgna.croquet.triggers.NullTrigger.createUserInstance();
			org.lgna.croquet.history.TransactionHistory history = org.lgna.croquet.Application.getActiveInstance().getApplicationOrDocumentTransactionHistory().getActiveTransactionHistory();
			org.lgna.croquet.history.Transaction transaction = history.acquireActiveTransaction();
			org.lgna.croquet.history.CompletionStep<FxEventPropertyModel<FxEvent>> completionStep = transaction.createAndSetCompletionStep( this, trigger );
			try {
				// TODO: Probably need to provide an option for these types of Models to create their own edits.
				// TODO: Add this later when you need it yourself.
				org.lgna.croquet.edits.Edit edit = new edu.wustl.lookingglass.croquetfx.edits.FxEventEdit<FxEvent>( completionStep, event );

				if( edit != null ) {
					completionStep.commit( edit );
				} else {
					completionStep.finish();
				}

				// TODO: I'm not sure this is the right spot for this. This now works very differently than before...
				if( this.eventHandler != null ) {
					this.eventHandler.accept( event );
				}
			} catch( org.lgna.croquet.CancelException ce ) {
				completionStep.cancel();
			}
		}
	}
}
