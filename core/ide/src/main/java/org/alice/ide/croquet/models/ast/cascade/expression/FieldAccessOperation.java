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

package org.alice.ide.croquet.models.ast.cascade.expression;

/**
 * @author Dennis Cosgrove
 */
public class FieldAccessOperation extends org.alice.ide.croquet.models.ast.cascade.ProjectExpressionPropertyOperation {
	private static edu.cmu.cs.dennisc.map.MapToMap<org.lgna.project.ast.AbstractField, org.lgna.project.ast.ExpressionProperty, FieldAccessOperation> mapToMap = edu.cmu.cs.dennisc.map.MapToMap.newInstance();

	public static FieldAccessOperation getInstance( org.lgna.project.ast.AbstractField field, org.lgna.project.ast.ExpressionProperty expressionProperty ) {
		assert field != null;
		assert expressionProperty != null;
		return mapToMap.getInitializingIfAbsent( field, expressionProperty, new edu.cmu.cs.dennisc.map.MapToMap.Initializer<org.lgna.project.ast.AbstractField, org.lgna.project.ast.ExpressionProperty, FieldAccessOperation>() {
			@Override
			public FieldAccessOperation initialize( org.lgna.project.ast.AbstractField field, org.lgna.project.ast.ExpressionProperty expressionProperty ) {
				return new FieldAccessOperation( field, expressionProperty );
			}
		} );
	}

	private final org.lgna.project.ast.AbstractField field;

	private FieldAccessOperation( org.lgna.project.ast.AbstractField field, org.lgna.project.ast.ExpressionProperty expressionProperty ) {
		super( java.util.UUID.fromString( "bf8b616d-d1ab-4798-8be2-60a107e9e762" ), expressionProperty );
		this.field = field;
	}

	@Override
	protected org.lgna.project.ast.Expression createExpression() {
		return org.lgna.project.ast.AstUtilities.createFieldAccess(
				org.alice.ide.IDE.getActiveInstance().getDocumentFrame().getInstanceFactoryState().getValue().createExpression(),
				this.field
				);
	}
}
