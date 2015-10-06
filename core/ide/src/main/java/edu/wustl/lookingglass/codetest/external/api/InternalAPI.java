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
package edu.wustl.lookingglass.codetest.external.api;

import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractNode;
import org.lgna.project.ast.UserMethod;

/**
 * @author Aaron Zemach
 */
@Deprecated
public class InternalAPI {

	public static org.lgna.project.ast.Statement getEncasingStatement( org.lgna.project.ast.AbstractNode start ) {
		org.lgna.project.ast.AbstractNode node = start;

		while( node.getParent() != null ) {
			if( org.lgna.project.ast.Statement.class.isAssignableFrom( node.getParent().getClass() ) ) {
				return (org.lgna.project.ast.Statement)node.getParent();
			}
			node = (AbstractNode)node.getParent();
		}

		return null;

	}

	public static org.lgna.project.ast.UserMethod getEncasingUserMethod( org.lgna.project.ast.AbstractNode start ) {
		org.lgna.project.ast.AbstractNode node = start;

		while( ( node.getParent() != null ) && !( node instanceof org.lgna.project.ast.UserMethod ) ) {
			node = (AbstractNode)node.getParent();
		}

		return (org.lgna.project.ast.UserMethod)node;
	}

	public static boolean isContainedBy( org.lgna.project.ast.AbstractNode start, Class<org.lgna.project.ast.AbstractNode> type ) {
		boolean result = false;
		org.lgna.project.ast.AbstractNode node = start;

		while( !result && ( node.getParent() != null ) ) {
			node = (AbstractNode)node.getParent();
			if( type.isAssignableFrom( node.getClass() ) ) {
				result = true;
			}
		}

		return result;
	}

	public static org.lgna.project.ast.AbstractNode getEncasingNodeOfType( org.lgna.project.ast.AbstractNode start, Class<org.lgna.project.ast.AbstractNode> type ) {
		return getEncasingNodeOfType( start, type, false );
	}

	public static org.lgna.project.ast.AbstractNode getEncasingNodeOfType( org.lgna.project.ast.AbstractNode start, Class<org.lgna.project.ast.AbstractNode> type, boolean startWithParent ) {
		if( !InternalAPI.isContainedBy( start, type ) ) {
			return null;
		}

		org.lgna.project.ast.AbstractNode node = start;
		if( startWithParent ) {
			if( node.getParent() != null ) {
				node = (AbstractNode)node.getParent();
			}
			else {
				return null;
			}
		}

		while( ( node.getParent() != null ) && !( type.isAssignableFrom( node.getClass() ) ) ) {
			node = (AbstractNode)node.getParent();
		}

		if( ( node.getParent() == null ) && !( type.isAssignableFrom( node.getClass() ) ) ) {
			return null;
		}

		return node;
	}

	public static org.lgna.project.ast.UserMethod getMainMethod( org.lgna.project.Project project ) {
		//TODO: Replace with something safer (I believe Dennis said he'd write this...)

		AbstractMethod mainMethod = project.getProgramType().findMethod( "main", String[].class );
		assert mainMethod.isStatic() : mainMethod;
		return (UserMethod)mainMethod;
	}
}
