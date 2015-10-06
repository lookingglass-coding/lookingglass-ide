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
package edu.wustl.lookingglass.ide.croquet.joint.all;

import org.alice.stageide.instancefactory.croquet.joint.all.JointedTypeMenuModel;

/**
 * @author Michael Pogran
 */
public class GlobalFirstFieldAccessJointedTypeMenuModel extends JointedTypeMenuModel {
	private static edu.cmu.cs.dennisc.map.MapToMap<org.lgna.project.ast.UserField, Integer, GlobalFirstFieldAccessJointedTypeMenuModel> mapToMap = edu.cmu.cs.dennisc.map.MapToMap.newInstance();

	public static GlobalFirstFieldAccessJointedTypeMenuModel getInstance( org.lgna.project.ast.UserField value ) {
		java.util.List<org.alice.stageide.ast.JointedTypeInfo> jointedTypeInfos = org.alice.stageide.ast.JointedTypeInfo.getInstances( value.getValueType() );
		return getInstance( value, jointedTypeInfos, 0 );
	}

	private static GlobalFirstFieldAccessJointedTypeMenuModel getInstance( org.lgna.project.ast.UserField value, java.util.List<org.alice.stageide.ast.JointedTypeInfo> jointedTypeInfos, int index ) {
		//todo
		synchronized( mapToMap ) {
			GlobalFirstFieldAccessJointedTypeMenuModel rv = mapToMap.get( value, index );
			if( rv != null ) {
				//pass
			} else {
				rv = new GlobalFirstFieldAccessJointedTypeMenuModel( value, jointedTypeInfos, index );
				mapToMap.put( value, index, rv );
			}
			return rv;
		}
	}

	private final org.lgna.project.ast.UserField field;

	private GlobalFirstFieldAccessJointedTypeMenuModel( org.lgna.project.ast.UserField field, java.util.List<org.alice.stageide.ast.JointedTypeInfo> jointedTypeInfos, int index ) {
		super( java.util.UUID.fromString( "aba6136e-0ef5-4aec-bc31-51603c146afa" ), jointedTypeInfos, index );
		this.field = field;
	}

	@Override
	protected org.alice.stageide.instancefactory.croquet.joint.all.JointedTypeMenuModel getInstance( java.util.List<org.alice.stageide.ast.JointedTypeInfo> jointedTypeInfos, int index ) {
		return getInstance( this.field, jointedTypeInfos, index );
	}

	@Override
	protected org.lgna.croquet.CascadeFillIn<org.alice.ide.instancefactory.InstanceFactory, ?> getFillIn( org.lgna.project.ast.AbstractMethod method ) {
		return org.alice.ide.instancefactory.croquet.InstanceFactoryFillIn.getInstance( org.alice.ide.instancefactory.GlobalFirstInstanceSceneFieldMethodInvocationFactory.getInstance( this.field, method ) );
	}

}
