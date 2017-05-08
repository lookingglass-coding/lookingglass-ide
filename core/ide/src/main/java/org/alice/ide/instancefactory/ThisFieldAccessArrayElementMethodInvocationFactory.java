/**
 * Copyright (c) 2006-2012, Carnegie Mellon University. All rights reserved.
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
 */
package org.alice.ide.instancefactory;

/**
 * @author dculyba
 */
public class ThisFieldAccessArrayElementMethodInvocationFactory extends AbstractInstanceFactory {

	private static edu.cmu.cs.dennisc.map.MapToMap<org.lgna.project.ast.UserField, org.lgna.project.ast.AbstractMethod, ThisFieldAccessArrayElementMethodInvocationFactory> mapToMap = edu.cmu.cs.dennisc.map.MapToMap.newInstance();

	public static synchronized ThisFieldAccessArrayElementMethodInvocationFactory getInstance( org.lgna.project.ast.UserField field, org.lgna.project.ast.AbstractMethod method ) {
		assert field != null;
		return mapToMap.getInitializingIfAbsent( field, method, new edu.cmu.cs.dennisc.map.MapToMap.Initializer<org.lgna.project.ast.UserField, org.lgna.project.ast.AbstractMethod, ThisFieldAccessArrayElementMethodInvocationFactory>() {
			@Override
			public ThisFieldAccessArrayElementMethodInvocationFactory initialize( org.lgna.project.ast.UserField field, org.lgna.project.ast.AbstractMethod method ) {
				return new ThisFieldAccessArrayElementMethodInvocationFactory( field, method );
			}
		} );
	}

	private final org.lgna.project.ast.AbstractMethod method;
	private final org.lgna.project.ast.UserField field;
	private final Integer arrayIndex;

	public ThisFieldAccessArrayElementMethodInvocationFactory( org.lgna.project.ast.UserField field, org.lgna.project.ast.AbstractMethod method ) {
		super( field.name );
		this.method = method;
		this.field = field;
		this.arrayIndex = 0;
	}

	protected org.lgna.project.ast.AbstractType<?, ?, ?> getValidInstanceType( org.lgna.project.ast.AbstractType<?, ?, ?> type, org.lgna.project.ast.AbstractCode code ) {
		org.lgna.project.ast.AbstractType<?, ?, ?> fieldDeclarationType = this.field.getDeclaringType();
		if( ( fieldDeclarationType != null ) && fieldDeclarationType.isAssignableFrom( type ) ) {
			return this.field.getValueType();
		} else {
			return null;
		}
	}

	@Override
	protected final boolean isValid( org.lgna.project.ast.AbstractType<?, ?, ?> type, org.lgna.project.ast.AbstractCode code ) {
		org.lgna.project.ast.AbstractType<?, ?, ?> methodDeclarationType = this.method.getDeclaringType();
		return ( methodDeclarationType != null ) && methodDeclarationType.isAssignableFrom( this.getValidInstanceType( type, code ) );
	}

	public org.lgna.project.ast.AbstractMethod getMethod() {
		return this.method;
	}

	public org.lgna.project.ast.UserField getField() {
		return this.field;
	}

	private org.lgna.project.ast.FieldAccess createFieldAccess( org.lgna.project.ast.Expression expression ) {
		return new org.lgna.project.ast.FieldAccess( expression, this.field );
	}

	protected org.lgna.project.ast.Expression createTransientExpressionForMethodInvocation() {
		return this.createFieldAccess( createTransientThisExpression() );
	}

	protected org.lgna.project.ast.Expression createExpressionForMethodInvocation() {
		return this.createFieldAccess( createThisExpression() );
	}

	private org.lgna.project.ast.MethodInvocation createMethodInvocation( org.lgna.project.ast.Expression access ) {
		return new org.lgna.project.ast.MethodInvocation( access, this.method );
	}

	private org.lgna.project.ast.ArrayAccess createArrayAccess( org.lgna.project.ast.Expression access ) {
		return new org.lgna.project.ast.ArrayAccess( this.method.getReturnType(), this.createMethodInvocation( access ), new org.lgna.project.ast.IntegerLiteral( this.arrayIndex ) );
	}

	@Override
	public final org.lgna.project.ast.Expression createTransientExpression() {
		return this.createArrayAccess( this.createTransientExpressionForMethodInvocation() );
	}

	@Override
	public final org.lgna.project.ast.Expression createExpression() {
		return this.createArrayAccess( this.createExpressionForMethodInvocation() );
	}

	@Override
	public final org.lgna.project.ast.AbstractType<?, ?, ?> getValueType() {
		return this.method.getReturnType().getComponentType();
	}

	protected StringBuilder addAccessRepr( StringBuilder rv ) {
		rv.append( "this." );
		rv.append( this.field.getName() );
		return rv;
	}

	@Override
	public final String getRepr() {
		StringBuilder sb = new StringBuilder();
		this.addAccessRepr( sb );
		sb.append( "'s " );
		sb.append( this.method.getName().substring( 3 ) );
		sb.append( "[ " );
		sb.append( this.arrayIndex );
		sb.append( " ]" );
		return sb.toString();
	}
}