/*******************************************************************************
 * Copyright (c) 2006, 2015, Carnegie Mellon University. All rights reserved.
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
 * 3. Products derived from the software may not be called "Alice", nor may
 *    "Alice" appear in their name, without prior written permission of
 *    Carnegie Mellon University.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Carnegie Mellon University"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 * ANY AND ALL EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT
 * SHALL THE AUTHORS, COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package edu.cmu.cs.dennisc.scenegraph;

import edu.cmu.cs.dennisc.math.AxisAlignedBox;
import edu.cmu.cs.dennisc.math.Point3;

public class SkeletonVisual extends Visual {

	public SkeletonVisualBoundingBoxTracker getTracker() {
		return this.tracker;
	}

	public void setTracker( SkeletonVisualBoundingBoxTracker tracker ) {
		this.tracker = tracker;
	}

	@Override
	public void setParent( Composite parent ) {
		super.setParent( parent );
		if( this.skeleton.getValue() != null ) {
			this.skeleton.getValue().setParent( parent );
		}
	};

	@Override
	protected void actuallyRelease() {
		super.actuallyRelease();
		if( skeleton.getValue() != null )
		{
			skeleton.getValue().release();
		}
		for( WeightedMesh mesh : weightedMeshes.getValue() ) {
			mesh.release();
		}
		for( WeightedMesh mesh : defaultPoseWeightedMeshes.getValue() ) {
			mesh.release();
		}
		for( TexturedAppearance texture : textures.getValue() ) {
			texture.release();
		}
	}

	@Override
	public int getGeometryCount()
	{
		return super.getGeometryCount() + this.weightedMeshes.getLength();
	}

	@Override
	public Geometry getGeometryAt( int index )
	{
		if( index < super.getGeometryCount() )
		{
			return super.getGeometryAt( index );
		}
		else
		{
			return this.weightedMeshes.getValue()[ index - super.getGeometryCount() ];
		}
	};

	public edu.cmu.cs.dennisc.math.AxisAlignedBox getAxisAlignedMinimumBoundingBox( edu.cmu.cs.dennisc.math.AxisAlignedBox rv, boolean ignoreJointOrientations ) {
		if( !ignoreJointOrientations && ( this.tracker != null ) ) {
			this.tracker.getAxisAlignedMinimumBoundingBox( rv );
		}
		else {
			//There's a problem here.
			//This code is used to return a bounding box for a Skeleton Visual in 2 cases:
			//	1) When there's no bounding box tracker (the utility class which lets us access the skeleton visual adapter and get bounding box data from the actual transformed weighted mesh
			//  2) When we want a bounding box that ignores the positions of the joints
			//This all works with the assumption that the raw weighted mesh data is a valid representation of these things.
			//Unfortunately, this assumption no longer holds true. We now have models which have a bind pose (the configuration of the raw mesh) and a default pose (the pose the model wants to come into alice in)
			//The logic of this function wants to use the default pose data, but for this to work we need to process the weighted mesh with this skeleton pose.
			//This means we can't rely on the data in the raw weighted mesh to give us what we want.
			//Options:
			//  1) Preprocess the weighted mesh based on the default pose such that the data in this.weightedMeshes gives us the desired values
			//		This may have repercussions. Is this data used elsewhere? The skin weight algortithm needs this mesh data to be in the bind pose for the skinning to work.
			//  2) Eliminate this second logic path and make everything use the tracker. This would mean adding ignoreJointOrientations to the tracker class
			//		This is probably the way to go, but we'll need to look into how often we want to ignoreJointOrientations and how often we don't have a tracker
			if( this.hasDefaultPoseWeightedMeshes.getValue() ) {
				if( this.defaultPoseWeightedMeshes.getValue() != null ) {
					for( edu.cmu.cs.dennisc.scenegraph.WeightedMesh wm : this.defaultPoseWeightedMeshes.getValue() )
					{
						edu.cmu.cs.dennisc.math.AxisAlignedBox b = wm.getAxisAlignedMinimumBoundingBox();
						rv.union( b );
					}
				}
			}
			else {
				if( this.weightedMeshes.getValue() != null ) {
					for( edu.cmu.cs.dennisc.scenegraph.WeightedMesh wm : this.weightedMeshes.getValue() )
					{
						edu.cmu.cs.dennisc.math.AxisAlignedBox b = wm.getAxisAlignedMinimumBoundingBox();
						rv.union( b );
					}
				}
			}
			if( this.geometries.getValue() != null ) {
				for( edu.cmu.cs.dennisc.scenegraph.Geometry g : this.geometries.getValue() )
				{
					edu.cmu.cs.dennisc.math.AxisAlignedBox b = g.getAxisAlignedMinimumBoundingBox();
					rv.union( b );
				}
			}
		}
		if( !rv.isNaN() )
		{
			rv.scale( this.scale.getValue() );
		}
		return rv;
	}

	@Override
	public edu.cmu.cs.dennisc.math.AxisAlignedBox getAxisAlignedMinimumBoundingBox( edu.cmu.cs.dennisc.math.AxisAlignedBox rv ) {
		if( this.tracker != null ) {
			this.tracker.getAxisAlignedMinimumBoundingBox( rv );
		}
		else {
			//See comment above
			if( this.hasDefaultPoseWeightedMeshes.getValue() ) {
				if( this.defaultPoseWeightedMeshes.getValue() != null ) {
					for( edu.cmu.cs.dennisc.scenegraph.WeightedMesh wm : this.defaultPoseWeightedMeshes.getValue() )
					{
						edu.cmu.cs.dennisc.math.AxisAlignedBox b = wm.getAxisAlignedMinimumBoundingBox();
						rv.union( b );
					}
				}
			}
			else {
				if( this.weightedMeshes.getValue() != null ) {
					for( edu.cmu.cs.dennisc.scenegraph.WeightedMesh wm : this.weightedMeshes.getValue() )
					{
						edu.cmu.cs.dennisc.math.AxisAlignedBox b = wm.getAxisAlignedMinimumBoundingBox();
						rv.union( b );
					}
				}
			}
			if( this.geometries.getValue() != null ) {
				for( edu.cmu.cs.dennisc.scenegraph.Geometry g : this.geometries.getValue() )
				{
					edu.cmu.cs.dennisc.math.AxisAlignedBox b = g.getAxisAlignedMinimumBoundingBox();
					rv.union( b );
				}
			}
		}
		if( !rv.isNaN() )
		{
			rv.scale( this.scale.getValue() );
		}
		return rv;
	}

	@Override
	public edu.cmu.cs.dennisc.math.Sphere getBoundingSphere( edu.cmu.cs.dennisc.math.Sphere rv ) {
		edu.cmu.cs.dennisc.math.AxisAlignedBox box = new edu.cmu.cs.dennisc.math.AxisAlignedBox();
		getAxisAlignedMinimumBoundingBox( box );
		if( !box.isNaN() ) {
			double diameter = Point3.calculateDistanceBetween( box.getMinimum(), box.getMaximum() );
			rv.center.set( box.getCenter() );
			rv.radius = diameter / 2;
		} else {
			rv.setNaN();
		}
		return rv;
	}

	public boolean renderBackfaces() {
		if( this.weightedMeshes.getValue() != null ) {
			for( edu.cmu.cs.dennisc.scenegraph.WeightedMesh wm : this.weightedMeshes.getValue() )
			{
				if( !wm.cullBackfaces.getValue() ) {
					return true;
				}
			}
		}
		if( this.geometries.getValue() != null ) {
			for( edu.cmu.cs.dennisc.scenegraph.Geometry g : this.geometries.getValue() )
			{
				if( ( g instanceof Mesh ) && !( (Mesh)g ).cullBackfaces.getValue() ) {
					return true;
				}
			}
		}
		return false;
	}

	public final edu.cmu.cs.dennisc.property.InstanceProperty<Joint> skeleton = new edu.cmu.cs.dennisc.property.InstanceProperty<Joint>( this, null );
	public final edu.cmu.cs.dennisc.property.InstanceProperty<AxisAlignedBox> baseBoundingBox = new edu.cmu.cs.dennisc.property.InstanceProperty<AxisAlignedBox>( this, new AxisAlignedBox() );

	private SkeletonVisualBoundingBoxTracker tracker = null;

	public final edu.cmu.cs.dennisc.property.CopyableArrayProperty<WeightedMesh> weightedMeshes = new edu.cmu.cs.dennisc.property.CopyableArrayProperty<WeightedMesh>( this, new WeightedMesh[ 0 ] )
	{
		@Override
		protected WeightedMesh[] createArray( int length ) {
			return new WeightedMesh[ length ];
		}

		@Override
		protected WeightedMesh createCopy( WeightedMesh src ) {
			//todo?
			return src;
		}
	};

	public final edu.cmu.cs.dennisc.property.CopyableArrayProperty<TexturedAppearance> textures = new edu.cmu.cs.dennisc.property.CopyableArrayProperty<TexturedAppearance>( this, new TexturedAppearance[ 0 ] )
	{
		@Override
		protected TexturedAppearance[] createArray( int length ) {
			return new TexturedAppearance[ length ];
		}

		@Override
		protected TexturedAppearance createCopy( TexturedAppearance src ) {
			//todo?
			return src;
		}
	};

	//This property is used to indicate if the visual has a separate set of weighted meshes which have been transformed into the default pose.
	//This is use to handle models which have different bind poses and default poses
	//By default most models have the same bind pose and default pose so they do not have defaultPoseWeightedMeshes
	public final edu.cmu.cs.dennisc.property.BooleanProperty hasDefaultPoseWeightedMeshes = new edu.cmu.cs.dennisc.property.BooleanProperty( this, false );

	public final edu.cmu.cs.dennisc.property.CopyableArrayProperty<WeightedMesh> defaultPoseWeightedMeshes = new edu.cmu.cs.dennisc.property.CopyableArrayProperty<WeightedMesh>( this, new WeightedMesh[ 0 ] )
	{
		@Override
		protected WeightedMesh[] createArray( int length ) {
			return new WeightedMesh[ length ];
		}

		@Override
		protected WeightedMesh createCopy( WeightedMesh src ) {
			//todo?
			return src;
		}
	};
}
