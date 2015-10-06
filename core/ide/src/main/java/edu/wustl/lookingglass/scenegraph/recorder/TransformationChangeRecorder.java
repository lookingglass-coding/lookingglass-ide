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
package edu.wustl.lookingglass.scenegraph.recorder;

import org.lgna.common.ComponentThread;

import edu.cmu.cs.dennisc.scenegraph.Composite;
import edu.wustl.lookingglass.scenegraph.RecordableAbstractTransformable;
import edu.wustl.lookingglass.scenegraph.RecordableAbstractTransformable.EPIC_HACK_FOR_DINAH_TransformationChangeListener;
import edu.wustl.lookingglass.scenegraph.RecordableAbstractTransformable.TransformationChangeEvent;
import edu.wustl.lookingglass.utilities.TransformationUtilities;

public class TransformationChangeRecorder extends RandomAccessRecorder {

	protected java.util.Map<edu.cmu.cs.dennisc.scenegraph.Composite, TransformableTimeLine> history = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();
	protected java.util.HashMap<edu.cmu.cs.dennisc.scenegraph.Composite, TransformationChange> transformCache = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();
	private double lastTime = Double.MIN_VALUE, currentTime = Double.MIN_VALUE;

	@Override
	public void setRecording( boolean isRecording ) {
		super.setRecording( isRecording );
		if( isRecording ) {
			RecordableAbstractTransformable.EPIC_HACK_FOR_DINAH_addTransformationChangeListener( transformationListener );
		} else {
			RecordableAbstractTransformable.EPIC_HACK_FOR_DINAH_removeTransformationChangeListener( transformationListener );
		}
	}

	private EPIC_HACK_FOR_DINAH_TransformationChangeListener transformationListener = new EPIC_HACK_FOR_DINAH_TransformationChangeListener() {

		@Override
		public void transformationChange( TransformationChangeEvent event ) {
			if( shouldRecordChange() ) {
				TransformableTimeLine transformableTimeLine;
				synchronized( history ) {
					transformableTimeLine = history.get( event.getTransformable() );

					if( transformableTimeLine == null ) {
						transformableTimeLine = new TransformableTimeLine( event.getTransformable() );
						history.put( event.getTransformable(), transformableTimeLine );
					}
				}

				TransformationChange transformationChange;
				if( event.isRelativeTransformationDifferent() ) {
					transformationChange = new MatrixTransformationChange( TransformationUtilities.encode( event.getLocalTransform() ), event.getRelativeTransform(), getSourceThreadForCurrentAnimation() );
				} else {
					transformationChange = new QuaternionTransformationChange( TransformationUtilities.encode( event.getLocalTransform() ), TransformationUtilities.encode( event.getRelativeTransform() ), event.applyLeftSide(), event.getAsSeenBy(), getSourceThreadForCurrentAnimation() );
				}
				transformableTimeLine.add( transformationChange, getProgramTime() );
			}
		}

	};

	@Override
	public void clearHistory() {
		this.history.clear();
		this.transformCache.clear();
	}

	@Override
	protected synchronized void scrubToTime( double rollbackTime, java.util.Set<Object> objectsToFilter ) {
		this.lastTime = currentTime;
		this.currentTime = rollbackTime;

		if( this.lastTime > this.currentTime ) {
			this.lastTime = this.currentTime;
		}
		this.rollbackChildren( getSceneSGComposite(), rollbackTime, this.lastTime, objectsToFilter );
	}

	private void rollbackChildren( edu.cmu.cs.dennisc.scenegraph.Composite composite, double rollbackTime, double lastTime, java.util.Set<Object> objectsToFilter ) {

		if( composite != null ) {
			for( edu.cmu.cs.dennisc.scenegraph.Component component : composite.getComponents() ) {
				if( component instanceof Composite ) {
					Composite transformable = (Composite)component;

					if( history.containsKey( transformable ) ) {
						TransformableTimeLine transformableTimeLine = history.get( transformable );

						//check if transformable has recorded changes
						if( transformableTimeLine.hasTransformationChanges() ) {

							//the transform to apply
							TransformationChange transform = null;

							java.util.SortedMap<Double, java.util.ArrayList<TransformationChange>> headMap = transformableTimeLine.getHeadMap( rollbackTime );

							//check for initial setup transforms if headMap is empty
							if( headMap.isEmpty() ) {

								//special case joints
								if( org.lgna.story.implementation.EntityImp.getInstance( transformable ) instanceof org.lgna.story.implementation.JointImp ) {
									org.lgna.story.implementation.JointImp jointImp = (org.lgna.story.implementation.JointImp)org.lgna.story.implementation.EntityImp.getInstance( transformable );
									transform = new MatrixTransformationChange( TransformationUtilities.encode( jointImp.getOriginalTransformation() ), null, ComponentThread.currentThread() );
								} else {
									transform = transformableTimeLine.getNaNTransformationChange();
								}
							} else {
								java.util.ArrayList<TransformationChange> transforms = headMap.get( headMap.lastKey() );

								//if we're not filtering, get transform closest to rollbackTime
								if( objectsToFilter.isEmpty() ) {
									if( transforms.size() >= 1 ) {
										transform = transforms.get( transforms.size() - 1 );
									}
								} else if( transformable instanceof RecordableAbstractTransformable ) {
									edu.cmu.cs.dennisc.math.AffineMatrix4x4 localTransformation = ( (RecordableAbstractTransformable)transformable ).getLocalTransformation();

									//get all previous transformations and reapply them
									java.util.SortedMap<Double, java.util.ArrayList<TransformationChange>> intermediateTransforms = headMap.tailMap( lastTime );

									for( java.util.ArrayList<TransformationChange> changes : intermediateTransforms.values() ) {
										for( int i = 0; i < changes.size(); i++ ) {
											TransformationChange change = changes.get( i );

											//only applying transformation if thread is not filtered
											if( objectsToFilter.contains( change.getSourceThread() ) ) {
												change.reapplyTransform( transformable, localTransformation );
											}
										}
										( (RecordableAbstractTransformable)transformable ).setLocalTransformation( localTransformation );
										this.transformCache.put( transformable, null );
									}
								}
							}
							if( ( transform != null ) && ( !transform.equals( this.transformCache.get( transformable ) ) ) && ( transformable instanceof RecordableAbstractTransformable ) ) {
								if( objectsToFilter.contains( transform.getSourceThread() ) || objectsToFilter.isEmpty() ) {
									( (RecordableAbstractTransformable)transformable ).setLocalTransformation( TransformationUtilities.decode( transform.getLocalTransform() ) );
									this.transformCache.put( transformable, transform );
								}
							}
						}
					}
					rollbackChildren( transformable, rollbackTime, lastTime, objectsToFilter );
				}
			}
		}
	}
}
