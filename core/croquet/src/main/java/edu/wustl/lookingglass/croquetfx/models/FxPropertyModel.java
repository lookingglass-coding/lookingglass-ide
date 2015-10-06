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

import java.util.function.BiConsumer;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;

import org.lgna.croquet.Operation;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.history.TransactionHistory;
import org.lgna.croquet.triggers.Trigger;

import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.croquetfx.edits.FxPropertyEdit;
import edu.wustl.lookingglass.croquetfx.exceptions.FxNotApplicableException;

/**
 * @author Kyle J. Harms
 */
public class FxPropertyModel<PropertyType> extends Operation {

	protected final BiConsumer<PropertyType, PropertyType> handler;

	protected final String summary;

	private final java.util.concurrent.Semaphore eventLock = new java.util.concurrent.Semaphore( 1 );

	public FxPropertyModel( FxComponent component, Object control, ObservableValue<PropertyType> controlProperty, BiConsumer<PropertyType, PropertyType> handler ) {
		// These UUID no longer have purpose in croquet. So I just don't care if it's random...
		super( org.lgna.croquet.Application.INHERIT_GROUP, java.util.UUID.randomUUID() );

		this.handler = handler;
		this.summary = generateSummary( component, control, controlProperty );
		controlProperty.addListener( this::handlePropertyChanged );
	}

	public void initializeModel( FxComponent.Block block ) {
		assert javafx.application.Platform.isFxApplicationThread();

		this.eventLock.acquireUninterruptibly();
		block.execute();
		this.eventLock.release();
	}

	private String generateSummary( FxComponent component, Object control, ObservableValue<PropertyType> controlProperty ) {
		StringBuilder id = new StringBuilder();
		id.append( "; class=" ).append( component.getClass().getName() ).append( "(" ).append( component.hashCode() ).append( ");" );

		id.append( "component=" ).append( control.getClass().getName() );
		if( control instanceof javafx.scene.control.Control ) {
			id.append( "(" ).append( ( (Control)control ).getId() ).append( ");" );
		} else {
			id.append( ";" );
		}

		id.append( "property=" );
		if( controlProperty instanceof javafx.beans.property.Property ) {
			javafx.beans.property.Property<?> property = (javafx.beans.property.Property<?>)controlProperty;
			id.append( property.getName() ).append( ";" );
		} else if( controlProperty instanceof javafx.beans.property.ReadOnlyBooleanProperty ) {
			javafx.beans.property.ReadOnlyProperty<?> property = (javafx.beans.property.ReadOnlyProperty<?>)controlProperty;
			id.append( property.getName() ).append( ";" );
		} else {
			id.append( controlProperty ).append( ";" );
		}

		return id.toString();
	}

	protected void handlePropertyChanged( ObservableValue<? extends PropertyType> observable, PropertyType oldValue, PropertyType newValue ) {
		assert javafx.application.Platform.isFxApplicationThread();

		// Sometimes we don't want this code executed, try to get the lock, if we fail then we don't want it executed
		if( this.eventLock.tryAcquire() ) {
			this.eventLock.release();

			Trigger trigger = org.lgna.croquet.triggers.NullTrigger.createUserInstance();
			TransactionHistory history = org.lgna.croquet.Application.getActiveInstance().getApplicationOrDocumentTransactionHistory().getActiveTransactionHistory();
			Transaction transaction = history.acquireActiveTransaction();
			CompletionStep<FxPropertyModel<PropertyType>> completionStep = transaction.createAndSetCompletionStep( this, trigger );
			try {
				org.lgna.croquet.edits.Edit edit = new FxPropertyEdit<PropertyType>( completionStep, oldValue, newValue );
				if( edit != null ) {
					completionStep.commit( edit );
				} else {
					completionStep.finish();
				}
				if( this.handler != null ) {
					this.handler.accept( oldValue, newValue );
				}
			} catch( org.lgna.croquet.CancelException ce ) {
				completionStep.cancel();
			}
		}
	}

	@Override
	protected void perform( Transaction transaction, Trigger trigger ) {
		throw new FxNotApplicableException( "NA: method has no parallel in CroquetFX." );
	}

	@Override
	protected void appendRepr( java.lang.StringBuilder repr ) {
		super.appendRepr( repr );
		repr.append( this.summary );
	}
}
