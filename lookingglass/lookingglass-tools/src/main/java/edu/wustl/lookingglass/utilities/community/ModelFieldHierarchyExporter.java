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
package edu.wustl.lookingglass.utilities.community;

import org.alice.nonfree.NebulousIde;

import edu.wustl.lookingglass.community.models.ModelField;

public class ModelFieldHierarchyExporter {
	private static final java.util.Map<Class<? extends org.lgna.story.resources.JointedModelResource>, Class<? extends org.lgna.story.SJointedModel>> mapResourceClsToStoryApiCls;

	static {
		java.util.Map<Class<? extends org.lgna.story.resources.JointedModelResource>, Class<? extends org.lgna.story.SJointedModel>> map = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();
		map.put( org.lgna.story.resources.BipedResource.class, org.lgna.story.SBiped.class );
		map.put( org.lgna.story.resources.FlyerResource.class, org.lgna.story.SFlyer.class );
		map.put( org.lgna.story.resources.PropResource.class, org.lgna.story.SProp.class );
		map.put( org.lgna.story.resources.QuadrupedResource.class, org.lgna.story.SQuadruped.class );
		map.put( org.lgna.story.resources.SlithererResource.class, org.lgna.story.SSlitherer.class );
		map.put( org.lgna.story.resources.SwimmerResource.class, org.lgna.story.SSwimmer.class );
		map.put( org.lgna.story.resources.TransportResource.class, org.lgna.story.STransport.class );
		mapResourceClsToStoryApiCls = java.util.Collections.unmodifiableMap( map );
	}

	private static edu.cmu.cs.dennisc.tree.DefaultNode<Class> getDatabaseTreeNodeBuildingHierarchyIfNecessary( edu.cmu.cs.dennisc.tree.DefaultNode<Class> databaseTreeRoot, Class cls ) {
		edu.cmu.cs.dennisc.tree.DefaultNode<Class> rv = databaseTreeRoot.get( cls );
		if( rv != null ) {
			//pass
		} else {
			rv = edu.cmu.cs.dennisc.tree.DefaultNode.createSafeInstance( cls, Class.class );
			Class superType = cls.getSuperclass();
			if( superType != null ) {
				edu.cmu.cs.dennisc.tree.DefaultNode<Class> superNode = getDatabaseTreeNodeBuildingHierarchyIfNecessary( databaseTreeRoot, superType );
				superNode.addChild( rv );
			} else {
				databaseTreeRoot.addChild( rv );
			}
		}
		return rv;
	}

	private static edu.cmu.cs.dennisc.tree.DefaultNode<Class> getDatabaseTreeNodeBuildingHierarchyIfNecessaryWithAlreadyDeterminedSuperCls( edu.cmu.cs.dennisc.tree.DefaultNode<Class> databaseTreeRoot, Class cls, Class parentCls ) {
		edu.cmu.cs.dennisc.tree.DefaultNode<Class> rv = databaseTreeRoot.get( cls );
		if( rv != null ) {
			//pass
		} else {
			rv = edu.cmu.cs.dennisc.tree.DefaultNode.createSafeInstance( cls, Class.class );
			edu.cmu.cs.dennisc.tree.DefaultNode<Class> superNode = getDatabaseTreeNodeBuildingHierarchyIfNecessary( databaseTreeRoot, parentCls );
			superNode.addChild( rv );
		}
		return rv;
	}

	private static edu.cmu.cs.dennisc.tree.DefaultNode<Class> buildDatabaseTreeFromStoryApiClses() {
		edu.cmu.cs.dennisc.tree.DefaultNode<Class> root = edu.cmu.cs.dennisc.tree.DefaultNode.createSafeInstance( null, Class.class );

		java.util.List<ClassLoader> classLoadersList = new java.util.LinkedList<ClassLoader>();
		classLoadersList.add( org.reflections.util.ClasspathHelper.contextClassLoader() );
		classLoadersList.add( org.reflections.util.ClasspathHelper.staticClassLoader() );
		org.reflections.Reflections reflections = new org.reflections.Reflections( new org.reflections.util.ConfigurationBuilder()
				.setScanners( new org.reflections.scanners.SubTypesScanner( false /* don't exclude Object.class */ ), new org.reflections.scanners.ResourcesScanner() )
				.setUrls( org.reflections.util.ClasspathHelper.forClassLoader( classLoadersList.toArray( new ClassLoader[ 0 ] ) ) )
				.filterInputsBy( new org.reflections.util.FilterBuilder().include( org.reflections.util.FilterBuilder.prefix( org.lgna.story.SThing.class.getPackage().getName() ) ) ) );
		java.util.Set<Class<? extends org.lgna.story.SThing>> classes = reflections.getSubTypesOf( org.lgna.story.SThing.class );

		for( Class<? extends org.lgna.story.SThing> cls : classes ) {
			getDatabaseTreeNodeBuildingHierarchyIfNecessary( root, cls );
		}
		return root;
	}

	private static Class<?> getParentClsForResourceCls( Class<? extends org.lgna.story.resources.ModelResource> cls ) {
		Class<?> rv = mapResourceClsToStoryApiCls.get( cls );
		if( rv != null ) {
			//pass
		} else {
			if( cls.isEnum() ) {
				//pass
			} else {
				rv = cls.getSuperclass();
			}
			if( rv != null ) {
				//pass
			} else {
				rv = cls.getInterfaces()[ 0 ];
			}
		}
		return rv;
	}

	private static void printGalleryResourceTree( edu.cmu.cs.dennisc.tree.DefaultNode<Class> databaseTreeRoot, org.alice.stageide.modelresource.ResourceNode node ) {
		org.alice.stageide.modelresource.ResourceKey resourceKey = node.getResourceKey();
		Class<? extends org.lgna.story.resources.ModelResource> cls;
		if( resourceKey instanceof org.alice.stageide.modelresource.ClassResourceKey ) {
			org.alice.stageide.modelresource.ClassResourceKey classResourceKey = (org.alice.stageide.modelresource.ClassResourceKey)resourceKey;
			cls = classResourceKey.getModelResourceCls();
		} else if( NebulousIde.nonfree.isInstanceOfPersonResourceKey( resourceKey ) ) {
			cls = NebulousIde.nonfree.getPersonResourceKeyModelResourceClass( resourceKey );
		} else if( resourceKey instanceof org.alice.stageide.modelresource.RootResourceKey ) {
			// pass
			cls = null;
		} else if( resourceKey instanceof org.alice.stageide.modelresource.EnumConstantResourceKey ) {
			// pass, we don't need to know about the tutu
			cls = null;
		} else {
			throw new RuntimeException( "unknown ResourceKey: " + resourceKey );
		}
		if( cls != null ) {
			getDatabaseTreeNodeBuildingHierarchyIfNecessaryWithAlreadyDeterminedSuperCls( databaseTreeRoot, cls, getParentClsForResourceCls( cls ) );
		}

		for( org.alice.stageide.modelresource.ResourceNode child : node.getNodeChildren() ) {
			printGalleryResourceTree( databaseTreeRoot, child );
		}
	}

	private static void addGalleryResourcesToTree( edu.cmu.cs.dennisc.tree.DefaultNode<Class> databaseTreeRoot ) {
		org.alice.stageide.modelresource.ClassHierarchyBasedResourceNode galleryResourceRoot = org.alice.stageide.modelresource.TreeUtilities.getTreeBasedOnClassHierarchy();
		printGalleryResourceTree( databaseTreeRoot, galleryResourceRoot );
	}

	private static String getThumbnailForCls( Class<?> cls ) {
		java.net.URL thumbnailUrl = org.lgna.story.implementation.alice.AliceResourceUtilties.getThumbnailURL( cls, null );
		if( thumbnailUrl != null ) {
			return getRelativeGalleryPath( thumbnailUrl );
		} else {
			return "";
		}
	}

	private static String getRelativeGalleryPath( java.net.URL galleryThumbnail ) {
		try {
			java.net.URI galleryPath = galleryThumbnail.toURI();
			java.net.URI basePath = edu.cmu.cs.dennisc.app.ApplicationRoot.getRootDirectory().toURI();
			java.net.URI relativePath = basePath.relativize( galleryPath );

			String path = relativePath.toString();
			assert path.length() <= 255;

			return path;
		} catch( Exception e ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( e );
			return "";
		}
	}

	private static void printTree( java.io.PrintWriter pw, edu.cmu.cs.dennisc.tree.DefaultNode<Class> node, Class<?> parentCls, int indent, boolean isIndentDesired ) {
		Class<?> cls = node.getValue();
		if( isIndentDesired ) {
			for( int i = 0; i < indent; i++ ) {
				pw.append( "\t" );
			}
		}

		pw.append( ModelField.getModelFieldKey( cls ) ).append( "," );
		pw.append( ModelField.getModelFieldKey( parentCls ) ).append( "," );
		pw.append( getThumbnailForCls( cls ) ).append( "\n" );

		for( edu.cmu.cs.dennisc.tree.DefaultNode<Class> child : node.getChildren() ) {
			printTree( pw, child, cls, indent + 1, isIndentDesired );
		}
	}

	public static void main( String[] args ) throws java.io.FileNotFoundException {
		edu.wustl.lookingglass.LookingGlass.initialize();

		java.io.PrintWriter pw;
		final boolean IS_INDENT_DESIRED;
		if( args.length > 0 ) {
			String csvPath = args[ 0 ];
			pw = new java.io.PrintWriter( csvPath );
			IS_INDENT_DESIRED = false;
		} else {
			pw = new java.io.PrintWriter( System.out );
			IS_INDENT_DESIRED = true;
		}

		edu.cmu.cs.dennisc.tree.DefaultNode<Class> root = buildDatabaseTreeFromStoryApiClses();

		addGalleryResourcesToTree( root );

		edu.cmu.cs.dennisc.tree.DefaultNode<Class> personResourceClsNode = NebulousIde.nonfree.getPersonResourceClassNode( root );
		edu.cmu.cs.dennisc.tree.DefaultNode<Class> sBipedResourceClsNode = root.get( org.lgna.story.resources.BipedResource.class );
		assert sBipedResourceClsNode != null;
		if( personResourceClsNode != null ) {
			sBipedResourceClsNode.addChild( personResourceClsNode );
		}

		// Print tree from SThing
		printTree( pw, root.get( org.lgna.story.SThing.class ), null, 0, IS_INDENT_DESIRED );

		pw.flush();
		if( args.length > 0 ) {
			pw.close();
		}
	}
}
