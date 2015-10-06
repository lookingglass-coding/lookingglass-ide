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
package edu.wustl.lookingglass.scenegraph;

import edu.cmu.cs.dennisc.math.AffineMatrix4x4;
import edu.cmu.cs.dennisc.scenegraph.ReferenceFrame;
import edu.cmu.cs.dennisc.scenegraph.TransformationAffect;

public abstract class RecordableAbstractTransformable extends edu.cmu.cs.dennisc.scenegraph.AbstractTransformable {

	public static interface EPIC_HACK_FOR_DINAH_TransformationChangeListener {
		public void transformationChange( TransformationChangeEvent event );
	}

	public class TransformationChangeEvent {
		private RecordableAbstractTransformable transformable;
		private AffineMatrix4x4 localTransform;
		private boolean relTransIsDiff;
		private AffineMatrix4x4 relativeTransform;
		private ReferenceFrame asSeenBy;
		private boolean applyLeftSide;

		public TransformationChangeEvent( RecordableAbstractTransformable transformable, AffineMatrix4x4 localTransform, boolean relTransIsDiff, AffineMatrix4x4 relativeTransform, ReferenceFrame asSeenBy, boolean applyLeftSide ) {
			this.transformable = transformable;
			this.localTransform = localTransform;
			this.relTransIsDiff = relTransIsDiff;
			this.relativeTransform = relativeTransform;
			this.asSeenBy = asSeenBy;
			this.applyLeftSide = applyLeftSide;
		}

		public RecordableAbstractTransformable getTransformable() {
			return this.transformable;
		}

		public AffineMatrix4x4 getLocalTransform() {
			return this.localTransform;
		}

		public AffineMatrix4x4 getRelativeTransform() {
			return this.relativeTransform;
		}

		public ReferenceFrame getAsSeenBy() {
			return this.asSeenBy;
		}

		public boolean applyLeftSide() {
			return this.applyLeftSide;
		}

		public boolean isRelativeTransformationDifferent() {
			return this.relTransIsDiff;
		}
	}

	private static final java.util.List<EPIC_HACK_FOR_DINAH_TransformationChangeListener> changeListeners = edu.cmu.cs.dennisc.java.util.Lists.newCopyOnWriteArrayList();

	public static void EPIC_HACK_FOR_DINAH_addTransformationChangeListener( EPIC_HACK_FOR_DINAH_TransformationChangeListener listener ) {
		changeListeners.add( listener );
	}

	public static void EPIC_HACK_FOR_DINAH_removeTransformationChangeListener( EPIC_HACK_FOR_DINAH_TransformationChangeListener listener ) {
		changeListeners.remove( listener );
	}

	@Override
	protected void setLeftAppliedLocalTransformation( AffineMatrix4x4 transformation, TransformationAffect affect, AffineMatrix4x4 relativeTransformation ) {
		setLocalTransformationFromEitherSide( transformation, affect, null, relativeTransformation, true );
	}

	@Override
	protected void setLocalTransformation( AffineMatrix4x4 transformation, TransformationAffect affect, AffineMatrix4x4 relativeTransformation ) {
		setLocalTransformationFromEitherSide( transformation, affect, null, relativeTransformation, false );
	}

	@Override
	protected void setLocalTransformation( AffineMatrix4x4 transformation, TransformationAffect affect, ReferenceFrame asSeenBy, AffineMatrix4x4 relativeTransformation ) {
		setLocalTransformationFromEitherSide( transformation, affect, asSeenBy, relativeTransformation, true );
	}

	@Override
	protected void setLocalTransformation( AffineMatrix4x4 transformation, TransformationAffect affect ) {
		setLocalTransformation( transformation, affect, null );
	}

	private void setLocalTransformationFromEitherSide( AffineMatrix4x4 transformation, TransformationAffect affect, ReferenceFrame asSeenBy, AffineMatrix4x4 relativeTransformation, boolean applyLeftHandSide ) {
		if( ( transformation == null ) || transformation.isNaN() ) {
			throw new RuntimeException( "invalid transformation" );
		}

		AffineMatrix4x4 m = this.accessLocalTransformation();
		boolean relativeTransformIsDiff = false;

		if( relativeTransformation == null ) {
			relativeTransformation = new AffineMatrix4x4( m );
			relativeTransformIsDiff = true;
		}

		affect.set( m, transformation );
		touchLocalTransformation( m );
		fireAbsoluteTransformationChange();

		if( relativeTransformIsDiff ) {
			relativeTransformation.invert();
			relativeTransformation.multiply( accessLocalTransformation() );
		}

		for( EPIC_HACK_FOR_DINAH_TransformationChangeListener listener : changeListeners ) {
			TransformationChangeEvent event = new TransformationChangeEvent( this, accessLocalTransformation(), relativeTransformIsDiff, relativeTransformation, asSeenBy, applyLeftHandSide );
			listener.transformationChange( event );
		}
	}

	public AffineMatrix4x4 getVehicleInverseAbsoluteTransformation() {
		if( this.getVehicle() != null ) {
			return new AffineMatrix4x4( this.getVehicle().getInverseAbsoluteTransformation() );
		}
		return null;
	}
}
