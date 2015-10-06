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
package edu.wustl.lookingglass.remix.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.lgna.project.Project;
import org.lgna.project.ast.AbstractConstructor;
import org.lgna.project.ast.AbstractDeclaration;
import org.lgna.project.ast.AbstractField;
import org.lgna.project.ast.AbstractLiteral;
import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractNode;
import org.lgna.project.ast.AbstractPackage;
import org.lgna.project.ast.AbstractParameter;
import org.lgna.project.ast.AbstractStatementWithBody;
import org.lgna.project.ast.AbstractType;
import org.lgna.project.ast.AnonymousUserConstructor;
import org.lgna.project.ast.AnonymousUserType;
import org.lgna.project.ast.ArithmeticInfixExpression;
import org.lgna.project.ast.ArrayAccess;
import org.lgna.project.ast.ArrayInstanceCreation;
import org.lgna.project.ast.ArrayLength;
import org.lgna.project.ast.AssertStatement;
import org.lgna.project.ast.AssignmentExpression;
import org.lgna.project.ast.BitwiseInfixExpression;
import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.BooleanExpressionBodyPair;
import org.lgna.project.ast.BooleanLiteral;
import org.lgna.project.ast.Comment;
import org.lgna.project.ast.ConditionalInfixExpression;
import org.lgna.project.ast.ConditionalStatement;
import org.lgna.project.ast.ConstructorBlockStatement;
import org.lgna.project.ast.ConstructorInvocationStatement;
import org.lgna.project.ast.CountLoop;
import org.lgna.project.ast.DoInOrder;
import org.lgna.project.ast.DoTogether;
import org.lgna.project.ast.DoubleLiteral;
import org.lgna.project.ast.EachInArrayTogether;
import org.lgna.project.ast.EachInIterableTogether;
import org.lgna.project.ast.Expression;
import org.lgna.project.ast.ExpressionStatement;
import org.lgna.project.ast.FieldAccess;
import org.lgna.project.ast.FloatLiteral;
import org.lgna.project.ast.ForEachInArrayLoop;
import org.lgna.project.ast.ForEachInIterableLoop;
import org.lgna.project.ast.Getter;
import org.lgna.project.ast.GlobalFirstInstanceExpression;
import org.lgna.project.ast.InfixExpression;
import org.lgna.project.ast.InstanceCreation;
import org.lgna.project.ast.IntegerLiteral;
import org.lgna.project.ast.JavaConstructor;
import org.lgna.project.ast.JavaField;
import org.lgna.project.ast.JavaKeyedArgument;
import org.lgna.project.ast.JavaMethod;
import org.lgna.project.ast.JavaPackage;
import org.lgna.project.ast.JavaParameter;
import org.lgna.project.ast.JavaType;
import org.lgna.project.ast.Lambda;
import org.lgna.project.ast.LambdaExpression;
import org.lgna.project.ast.LocalAccess;
import org.lgna.project.ast.LocalDeclarationStatement;
import org.lgna.project.ast.LogicalComplement;
import org.lgna.project.ast.MethodInvocation;
import org.lgna.project.ast.NamedUserConstructor;
import org.lgna.project.ast.NamedUserType;
import org.lgna.project.ast.NullLiteral;
import org.lgna.project.ast.NumberLiteral;
import org.lgna.project.ast.ParameterAccess;
import org.lgna.project.ast.RelationalInfixExpression;
import org.lgna.project.ast.ResourceExpression;
import org.lgna.project.ast.ReturnStatement;
import org.lgna.project.ast.Setter;
import org.lgna.project.ast.SetterParameter;
import org.lgna.project.ast.ShiftInfixExpression;
import org.lgna.project.ast.SimpleArgument;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.StringConcatenation;
import org.lgna.project.ast.StringLiteral;
import org.lgna.project.ast.SuperConstructorInvocationStatement;
import org.lgna.project.ast.SuperExpression;
import org.lgna.project.ast.ThisConstructorInvocationStatement;
import org.lgna.project.ast.ThisExpression;
import org.lgna.project.ast.ThisInstanceExpression;
import org.lgna.project.ast.TypeExpression;
import org.lgna.project.ast.TypeLiteral;
import org.lgna.project.ast.UserArrayType;
import org.lgna.project.ast.UserField;
import org.lgna.project.ast.UserLambda;
import org.lgna.project.ast.UserLocal;
import org.lgna.project.ast.UserMethod;
import org.lgna.project.ast.UserPackage;
import org.lgna.project.ast.UserParameter;
import org.lgna.project.ast.UserType;
import org.lgna.project.ast.WhileLoop;

import edu.wustl.lookingglass.remix.ast.exceptions.ASTCopyException;

/**
 * Abstract Alice/Looking Glass AST copier. Produces a copy of an AST node with
 * the same values for all node properties. <code>ASTCopier</code> makes no
 * assumptions as to the use case of copy - additional functionality for
 * use-specific copy provided by sub-classes.
 *
 *
 * @author Michael Pogran
 */
public class ASTCopier {

	private final Project project;
	private final Map<UUID, AbstractDeclaration> oldToNewDeclarations = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();
	private boolean shouldPreserveIds;

	public ASTCopier( Project project, boolean shouldPreserveIds ) {
		this.project = project;
		this.shouldPreserveIds = shouldPreserveIds;
	}

	public Project copyProject() {
		return new Project( (NamedUserType)copyAbstractNode( this.project.getProgramType() ) );
	}

	protected Project getProject() {
		return this.project;
	}

	public AbstractDeclaration getNewDeclaration( AbstractDeclaration oldDeclaration ) {
		if( oldDeclaration != null ) {
			return this.oldToNewDeclarations.get( oldDeclaration.getId() );
		} else {
			return null;
		}
	}

	public Map<UUID, AbstractDeclaration> getOldToNewDeclarations() {
		return this.oldToNewDeclarations;
	}

	protected void addNewDeclaration( AbstractDeclaration oldDeclaration, AbstractDeclaration newDeclaration ) {
		if( oldDeclaration != null ) {
			this.oldToNewDeclarations.put( oldDeclaration.getId(), newDeclaration );
		}
	}

	protected boolean newDeclarationExists( AbstractDeclaration oldDeclaration ) {
		return this.oldToNewDeclarations.containsKey( oldDeclaration.getId() );
	}

	public void setShouldPreserveIds( boolean shouldPreserveIds ) {
		this.shouldPreserveIds = shouldPreserveIds;
	}

	protected final void setId( AbstractNode node, AbstractNode rv ) {
		if( this.shouldPreserveIds ) {
			rv.setId( node.getId() );
		}
	}

	public AbstractNode copyAbstractNode( AbstractNode node ) {

		AbstractNode rv = null;
		// AbstractArgument subclasses
		if( node instanceof JavaKeyedArgument ) {
			rv = copyJavaKeyedArugment( (JavaKeyedArgument)node );
		}
		else if( node instanceof SimpleArgument ) {
			rv = copySimpleArugment( (SimpleArgument)node );
		}
		// AbstractConstructor subclasses
		else if( node instanceof AbstractDeclaration ) {
			rv = copyAbstractDeclaration( (AbstractDeclaration)node );
		}
		// BooleanExpressionBodyPair
		else if( node instanceof BooleanExpressionBodyPair ) {
			rv = copyBooleanExpressionBodyPair( (BooleanExpressionBodyPair)node );
		}
		// Expression
		else if( node instanceof Expression ) {
			rv = copyExpression( (Expression)node );
		}
		// Statement
		else if( node instanceof Statement ) {
			rv = copyStatement( (Statement)node );
		}
		else {
			throw new ASTCopyException( "Unsupported node type: " + node.getClass().getSimpleName(), node );
		}

		if( rv != null ) {
			setId( node, rv );
		}
		return rv;
	}

	/* AbstractArgument copy methods */
	protected JavaKeyedArgument copyJavaKeyedArugment( JavaKeyedArgument argument ) {

		JavaKeyedArgument rv = new JavaKeyedArgument();
		rv.expression.setValue( (Expression)copyAbstractNode( argument.expression.getValue() ) );
		rv.parameter.setValue( (AbstractParameter)copyAbstractNode( argument.parameter.getValue() ) );

		return rv;
	}

	protected SimpleArgument copySimpleArugment( SimpleArgument argument ) {
		SimpleArgument rv = new SimpleArgument( (AbstractParameter)copyAbstractNode( argument.parameter.getValue() ), (Expression)copyAbstractNode( argument.expression.getValue() ) );

		return rv;
	}

	/* AbstractDeclaration copy methods: Note, we keep track of declaration copies */
	protected AbstractDeclaration copyAbstractDeclaration( AbstractDeclaration declaration ) {
		if( declaration instanceof AbstractConstructor ) {
			return copyAbstractConstructor( (AbstractConstructor)declaration );
		}
		else if( declaration instanceof AbstractMethod ) {
			return copyAbstractMethod( (AbstractMethod)declaration );
		}
		else if( declaration instanceof AbstractField ) {
			return copyAbstractField( (AbstractField)declaration );
		}
		else if( declaration instanceof AbstractType ) {
			return copyAbstractType( (AbstractType<?, ?, ?>)declaration );
		}
		else if( declaration instanceof AbstractPackage ) {
			return copyAbstractPackage( (AbstractPackage)declaration );
		}
		// AbstractTransient
		else if( declaration instanceof AbstractParameter ) {
			return copyAbstractParameter( (AbstractParameter)declaration );
		}
		else if( declaration instanceof UserLocal ) {
			return copyUserLocal( (UserLocal)declaration );
		}
		else {
			throw new ASTCopyException( "Unsupported declaration type: " + declaration.getClass().getSimpleName(), declaration );
		}
	}

	/* Abstract Constructor copy methods */
	protected AbstractConstructor copyAbstractConstructor( AbstractConstructor constructor ) {
		if( constructor instanceof JavaConstructor ) {
			return constructor;
		}
		else if( constructor instanceof AnonymousUserConstructor ) {
			return copyAnonymousUserConstructor( (AnonymousUserConstructor)constructor );
		}
		else if( constructor instanceof NamedUserConstructor ) {
			return copyNamedUserConstructor( (NamedUserConstructor)constructor );
		}
		else {
			throw new ASTCopyException( "Unsupported constructor type: " + constructor.getClass().getSimpleName(), constructor );
		}
	}

	protected AnonymousUserConstructor copyAnonymousUserConstructor( AnonymousUserConstructor constructor ) {
		throw new ASTCopyException( "Unsupported constructor type: " + constructor.getClass().getSimpleName(), constructor );
	}

	protected NamedUserConstructor copyNamedUserConstructor( NamedUserConstructor constructor ) {
		NamedUserConstructor rv = (NamedUserConstructor)getNewDeclaration( constructor );

		if( rv == null ) {
			rv = new NamedUserConstructor();
			addNewDeclaration( constructor, rv );

			rv.requiredParameters.setValue( copyNodeCollection( constructor.requiredParameters.getValue() ) );
			rv.body.setValue( (ConstructorBlockStatement)copyAbstractNode( constructor.body.getValue() ) );
			rv.managementLevel.setValue( constructor.managementLevel.getValue() );
			rv.accessLevel.setValue( constructor.accessLevel.getValue() );
			rv.isDeletionAllowed.setValue( constructor.isDeletionAllowed.getValue() );
			rv.isSignatureLocked.setValue( constructor.isSignatureLocked.getValue() );
		}
		return rv;
	}

	/* AbstractMethod copy methods */
	protected AbstractMethod copyAbstractMethod( AbstractMethod method ) {
		if( method instanceof UserLambda ) {
			return copyUserLambda( (UserLambda)method );
		}
		else if( method instanceof UserMethod ) {
			return copyUserMethod( (UserMethod)method );
		}
		else if( method instanceof JavaMethod ) {
			return method;
		}
		else if( method instanceof Getter ) {
			return copyGetter( (Getter)method );
		}
		else if( method instanceof Setter ) {
			return copySetter( (Setter)method );
		}
		else {
			throw new ASTCopyException( "Cannot copy method: " + method.getRepr(), method );
		}
	}

	protected UserLambda copyUserLambda( UserLambda lambda ) {
		UserLambda rv = (UserLambda)getNewDeclaration( lambda );

		if( rv == null ) {
			rv = new UserLambda();
			addNewDeclaration( lambda, rv );

			rv.returnType.setValue( (AbstractType<?, ?, ?>)copyAbstractNode( lambda.returnType.getValue() ) );
			rv.requiredParameters.setValue( copyNodeCollection( lambda.requiredParameters.getValue() ) );
			rv.body.setValue( (BlockStatement)copyAbstractNode( lambda.body.getValue() ) );
			if( lambda.getName() != null ) {
				rv.setName( lambda.getName() );
			}

			rv.accessLevel.setValue( lambda.accessLevel.getValue() );
			rv.isDeletionAllowed.setValue( lambda.isDeletionAllowed.getValue() );
			rv.isSignatureLocked.setValue( lambda.isSignatureLocked.getValue() );
			rv.isStrictFloatingPoint.setValue( lambda.isStrictFloatingPoint.getValue() );
			rv.isSynchronized.setValue( lambda.isSynchronized.getValue() );
			rv.managementLevel.setValue( lambda.managementLevel.getValue() );
		}
		return rv;
	}

	protected UserMethod copyUserMethod( UserMethod method ) {
		UserMethod rv = (UserMethod)getNewDeclaration( method );

		if( rv == null ) {
			rv = new UserMethod();
			addNewDeclaration( method, rv );

			rv.returnType.setValue( (AbstractType<?, ?, ?>)copyAbstractNode( method.returnType.getValue() ) );
			rv.requiredParameters.setValue( copyNodeCollection( method.requiredParameters.getValue() ) );
			rv.body.setValue( (BlockStatement)copyAbstractNode( method.body.getValue() ) );
			rv.setName( method.getName() );

			rv.accessLevel.setValue( method.accessLevel.getValue() );
			rv.isDeletionAllowed.setValue( method.isDeletionAllowed.getValue() );
			rv.isSignatureLocked.setValue( method.isSignatureLocked.getValue() );
			rv.isStrictFloatingPoint.setValue( method.isStrictFloatingPoint.getValue() );
			rv.isSynchronized.setValue( method.isSynchronized.getValue() );
			rv.managementLevel.setValue( method.managementLevel.getValue() );
			rv.isAbstract.setValue( method.isAbstract() );
			rv.isFinal.setValue( method.isFinal.getValue() );
			rv.isStatic.setValue( method.isStatic.getValue() );
		}
		return rv;
	}

	/* AbstractMethodContainedByUserField copy methods */
	protected Getter copyGetter( Getter getter ) {
		Getter rv = (Getter)getNewDeclaration( getter );

		if( rv == null ) {
			UserField field = (UserField)copyAbstractNode( getter.getField() );
			rv = field.getGetter();
			addNewDeclaration( getter, rv );
		}
		return rv;
	}

	protected Setter copySetter( Setter setter ) {
		Setter rv = (Setter)getNewDeclaration( setter );

		if( rv == null ) {
			UserField field = (UserField)copyAbstractNode( setter.getField() );
			rv = field.getSetter();
			addNewDeclaration( setter, rv );
		}
		return rv;
	}

	protected AbstractField copyAbstractField( AbstractField field ) {
		if( field instanceof JavaField ) {
			return field;
		}
		else if( field instanceof UserField ) {
			return copyUserField( (UserField)field );
		}
		else {
			throw new ASTCopyException( "Unsupported field type: " + field.getClass().getSimpleName(), field );
		}
	}

	protected UserField copyUserField( UserField field ) {
		UserField rv = (UserField)getNewDeclaration( field );

		if( rv == null ) {
			rv = new UserField();
			addNewDeclaration( field, rv );

			rv.setName( field.getName() );
			rv.valueType.setValue( (AbstractType<?, ?, ?>)copyAbstractNode( field.valueType.getValue() ) );
			rv.initializer.setValue( (Expression)copyAbstractNode( field.initializer.getValue() ) );

			rv.accessLevel.setValue( field.accessLevel.getValue() );
			rv.finalVolatileOrNeither.setValue( field.finalVolatileOrNeither.getValue() );
			rv.isDeletionAllowed.setValue( field.isDeletionAllowed.getValue() );
			rv.isStatic.setValue( field.isStatic.getValue() );
			rv.isTransient.setValue( field.isTransient.getValue() );
			rv.managementLevel.setValue( field.managementLevel.getValue() );
		}
		return rv;
	}

	protected AbstractType<?, ?, ?> copyAbstractType( AbstractType<?, ?, ?> type ) {
		if( type instanceof JavaType ) {
			return type;
		}
		else if( type instanceof UserArrayType ) {
			return copyUserArrayType( (UserArrayType)type );
		}
		else if( type instanceof AnonymousUserType ) {
			return copyAnonymousUserType( (AnonymousUserType)type );
		}
		else if( type instanceof NamedUserType ) {
			return copyNamedUserType( (NamedUserType)type );
		}
		else {
			throw new ASTCopyException( "Unsupported type: " + type.getClass().getSimpleName(), type );
		}
	}

	protected UserArrayType copyUserArrayType( UserArrayType type ) {
		UserArrayType rv = (UserArrayType)getNewDeclaration( type );

		if( rv == null ) {
			UserType<?> userType = (UserType<?>)getNewDeclaration( type.getLeafType() );
			if( userType == null ) {
				userType = (UserType<?>)copyAbstractNode( type.getLeafType() );
			}
			rv = (UserArrayType)userType.getArrayType();
		}
		return rv;
	}

	protected AnonymousUserType copyAnonymousUserType( AnonymousUserType type ) {
		AnonymousUserType rv = (AnonymousUserType)getNewDeclaration( type );

		if( rv == null ) {
			rv = new AnonymousUserType();
			addNewDeclaration( type, rv );

			rv.setName( type.getName() );
			rv.superType.setValue( (AbstractType<?, ?, ?>)copyAbstractNode( type.superType.getValue() ) );
			rv.methods.setValue( copyNodeCollection( type.methods.getValue() ) );
			rv.fields.setValue( copyNodeCollection( type.fields.getValue() ) );
		}
		return rv;
	}

	protected NamedUserType copyNamedUserType( NamedUserType type ) {
		NamedUserType rv = (NamedUserType)getNewDeclaration( type );

		if( rv == null ) {
			rv = new NamedUserType();
			addNewDeclaration( type, rv );

			rv.superType.setValue( (AbstractType<?, ?, ?>)copyAbstractNode( type.superType.getValue() ) );
			rv.methods.setValue( copyNodeCollection( type.methods.getValue() ) );
			rv.fields.setValue( copyNodeCollection( type.fields.getValue() ) );

			rv.setName( type.getName() );
			if( type._package.getValue() != null ) {
				rv._package.setValue( (UserPackage)copyAbstractNode( type._package.getValue() ) );
			}
			rv.constructors.setValue( copyNodeCollection( type.constructors.getValue() ) );

			rv.accessLevel.setValue( type.accessLevel.getValue() );
			rv.finalAbstractOrNeither.setValue( type.finalAbstractOrNeither.getValue() );
			rv.isStrictFloatingPoint.setValue( type.isStrictFloatingPoint.getValue() );
		}
		return rv;
	}

	protected AbstractPackage copyAbstractPackage( AbstractPackage abstractPackage ) {
		if( abstractPackage instanceof JavaPackage ) {
			return abstractPackage;
		}
		else if( abstractPackage instanceof UserPackage ) {
			UserPackage rv = (UserPackage)getNewDeclaration( abstractPackage );
			if( rv == null ) {
				rv = new UserPackage( abstractPackage.getName() );
				addNewDeclaration( abstractPackage, rv );
			}
			return rv;
		}
		else {
			throw new ASTCopyException( "Unsupported package type: " + abstractPackage.getClass().getSimpleName(), abstractPackage );
		}
	}

	protected AbstractParameter copyAbstractParameter( AbstractParameter parameter ) {
		if( parameter instanceof JavaParameter ) {
			return parameter;
		}
		else if( parameter instanceof SetterParameter ) {
			return copySetterParameter( (SetterParameter)parameter );
		}
		else if( parameter instanceof UserParameter ) {
			return copyUserParameter( (UserParameter)parameter );
		}
		else {
			throw new ASTCopyException( "Unsupported parameter type: " + parameter.getClass().getSimpleName(), parameter );
		}
	}

	protected SetterParameter copySetterParameter( SetterParameter parameter ) {
		SetterParameter rv = (SetterParameter)getNewDeclaration( parameter );

		if( rv == null ) {
			Setter setter = (Setter)getNewDeclaration( parameter.getCode() );
			if( setter == null ) {
				setter = (Setter)copyAbstractNode( parameter.getCode() );
			}
			rv = (SetterParameter)setter.getRequiredParameters().get( 0 ); // this should always be the case, based on current Setter implementation
		}
		return rv;
	}

	protected UserParameter copyUserParameter( UserParameter parameter ) {
		UserParameter rv = (UserParameter)getNewDeclaration( parameter );

		if( rv == null ) {
			rv = new UserParameter();
			addNewDeclaration( parameter, rv );

			rv.setName( parameter.getName() );
			rv.valueType.setValue( (AbstractType<?, ?, ?>)copyAbstractNode( parameter.getValueType() ) );
		}
		return rv;
	}

	protected UserLocal copyUserLocal( UserLocal local ) {
		UserLocal rv = (UserLocal)getNewDeclaration( local );

		if( rv == null ) {
			rv = new UserLocal();
			addNewDeclaration( local, rv );

			rv.setName( local.getName() );
			rv.valueType.setValue( (AbstractType<?, ?, ?>)copyAbstractNode( local.getValueType() ) );
			rv.isFinal.setValue( local.isFinal.getValue() );
		}
		return rv;
	}

	/* BooleanExpressionBodyPair copy methods */
	protected BooleanExpressionBodyPair copyBooleanExpressionBodyPair( BooleanExpressionBodyPair booleanPair ) {
		return new BooleanExpressionBodyPair( (Expression)copyAbstractNode( booleanPair.expression.getValue() ), (BlockStatement)copyAbstractNode( booleanPair.body.getValue() ) );
	}

	/* Expression copy methods */
	protected Expression copyExpression( Expression expression ) {

		if( expression instanceof AbstractLiteral ) {
			return copyAbstractLiteral( (AbstractLiteral)expression );
		}
		else if( expression instanceof ArrayAccess ) {
			return copyArrayAccess( (ArrayAccess)expression );
		}
		else if( expression instanceof ArrayInstanceCreation ) {
			return copyArrayInstanceCreation( (ArrayInstanceCreation)expression );
		}
		else if( expression instanceof ArrayLength ) {
			return copyArrayLength( (ArrayLength)expression );
		}
		else if( expression instanceof AssignmentExpression ) {
			return copyAssignmentExpression( (AssignmentExpression)expression );
		}
		else if( expression instanceof BitwiseInfixExpression ) {
			return copyBitwiseInfixExpression( (BitwiseInfixExpression)expression );
		}
		else if( expression instanceof FieldAccess ) {
			return copyFieldAccess( (FieldAccess)expression );
		}
		else if( expression instanceof GlobalFirstInstanceExpression ) {
			return copyGlobalFirstInstanceExpression( (GlobalFirstInstanceExpression)expression );
		}
		else if( expression instanceof InfixExpression ) {
			return copyInfixExpression( (InfixExpression<?>)expression );
		}
		else if( expression instanceof InstanceCreation ) {
			return copyInstanceCreation( (InstanceCreation)expression );
		}
		else if( expression instanceof LambdaExpression ) {
			return copyLambdaExpression( (LambdaExpression)expression );
		}
		else if( expression instanceof LocalAccess ) {
			return copyLocalAccess( (LocalAccess)expression );
		}
		else if( expression instanceof LogicalComplement ) {
			return copyLogicalComplement( (LogicalComplement)expression );
		}
		else if( expression instanceof MethodInvocation ) {
			return copyMethodInvocation( (MethodInvocation)expression );
		}
		else if( expression instanceof ParameterAccess ) {
			return copyParameterAccess( (ParameterAccess)expression );
		}
		else if( expression instanceof ResourceExpression ) {
			return copyResourceExpression( (ResourceExpression)expression );
		}
		else if( expression instanceof ShiftInfixExpression ) {
			return copyShiftInfixExpression( (ShiftInfixExpression)expression );
		}
		else if( expression instanceof StringConcatenation ) {
			return copyStringConcatenation( (StringConcatenation)expression );
		}
		else if( expression instanceof SuperExpression ) {
			return copySuperExpression( (SuperExpression)expression );
		}
		else if( expression instanceof ThisExpression ) {
			return copyThisExpression( (ThisExpression)expression );
		}
		else if( expression instanceof ThisInstanceExpression ) {
			return copyThisInstanceExpression( (ThisInstanceExpression)expression );
		}
		else if( expression instanceof TypeExpression ) {
			return copyTypeExpression( (TypeExpression)expression );
		}
		else {
			throw new ASTCopyException( "Unsupported expression type: " + expression.getClass().getSimpleName(), expression );
		}
	}

	protected AbstractLiteral copyAbstractLiteral( AbstractLiteral literal ) {
		AbstractLiteral rv = null;
		if( literal instanceof BooleanLiteral ) {
			rv = new BooleanLiteral( ( (BooleanLiteral)literal ).value.getValue() );
		}
		else if( literal instanceof DoubleLiteral ) {
			rv = new DoubleLiteral( ( (DoubleLiteral)literal ).value.getValue() );
		}
		else if( literal instanceof FloatLiteral ) {
			rv = new FloatLiteral( ( (FloatLiteral)literal ).value.getValue() );
		}
		else if( literal instanceof IntegerLiteral ) {
			rv = new IntegerLiteral( ( (IntegerLiteral)literal ).value.getValue() );
		}
		else if( literal instanceof NumberLiteral ) {
			rv = new NumberLiteral( ( (NumberLiteral)literal ).value.getValue() );
		}
		else if( literal instanceof StringLiteral ) {
			rv = new StringLiteral( ( (StringLiteral)literal ).value.getValue() );
		}
		else if( literal instanceof TypeLiteral ) {
			rv = new TypeLiteral( ( (TypeLiteral)literal ).value.getValue() );
		}
		else if( literal instanceof NullLiteral ) {
			rv = new NullLiteral();
		} else {
			throw new ASTCopyException( "Unsupported abstract literal type: " + literal.getClass().getSimpleName(), literal );
		}

		if( rv != null ) {
			setId( literal, rv );
		}

		return rv;
	}

	protected ArrayAccess copyArrayAccess( ArrayAccess arrayAccess ) {
		return new ArrayAccess( (AbstractType<?, ?, ?>)copyAbstractNode( arrayAccess.array.getExpressionType() ), (Expression)copyAbstractNode( arrayAccess.array.getValue() ), (Expression)copyAbstractNode( arrayAccess.index.getValue() ) );
	}

	protected ArrayInstanceCreation copyArrayInstanceCreation( ArrayInstanceCreation arrayInstanceCreation ) {
		Expression[] expressions = new Expression[ arrayInstanceCreation.expressions.getValue().size() ];
		AbstractType<?, ?, ?> arrayType = (AbstractType<?, ?, ?>)copyAbstractNode( arrayInstanceCreation.arrayType.getValue() );

		for( int i = 0; i < expressions.length; i++ ) {
			expressions[ i ] = (Expression)copyAbstractNode( arrayInstanceCreation.expressions.get( i ) );
		}

		return new ArrayInstanceCreation( arrayType, arrayInstanceCreation.lengths.getValue().toArray( new Integer[ arrayInstanceCreation.lengths.getValue().size() ] ), expressions );
	}

	protected ArrayLength copyArrayLength( ArrayLength arrayLength ) {
		return new ArrayLength( (Expression)copyAbstractNode( arrayLength.array.getValue() ) );
	}

	protected AssignmentExpression copyAssignmentExpression( AssignmentExpression expression ) {
		Expression leftHandExpression = (Expression)copyAbstractNode( expression.leftHandSide.getValue() );
		Expression rightHandExpression = (Expression)copyAbstractNode( expression.rightHandSide.getValue() );

		return new AssignmentExpression( (AbstractType<?, ?, ?>)copyAbstractNode( expression.getType() ), leftHandExpression, expression.operator.getValue(), rightHandExpression );
	}

	protected BitwiseInfixExpression copyBitwiseInfixExpression( BitwiseInfixExpression expression ) {
		Expression leftOperand = (Expression)copyAbstractNode( expression.leftOperand.getValue() );
		Expression rightOperand = (Expression)copyAbstractNode( expression.rightOperand.getValue() );

		return new BitwiseInfixExpression( (AbstractType<?, ?, ?>)copyAbstractNode( expression.getType() ), leftOperand, expression.operator.getValue(), rightOperand );
	}

	protected FieldAccess copyFieldAccess( FieldAccess fieldAccess ) {
		return new FieldAccess( (Expression)copyAbstractNode( fieldAccess.expression.getValue() ), (AbstractField)copyAbstractNode( fieldAccess.field.getValue() ) );
	}

	protected GlobalFirstInstanceExpression copyGlobalFirstInstanceExpression( GlobalFirstInstanceExpression expression ) {
		return new GlobalFirstInstanceExpression( (AbstractType<?, ?, ?>)copyAbstractNode( ( (GlobalFirstInstanceExpression)expression ).getType() ) );
	}

	protected InfixExpression<?> copyInfixExpression( InfixExpression<?> expression ) {
		if( expression instanceof ArithmeticInfixExpression ) {
			ArithmeticInfixExpression arithmetic = (ArithmeticInfixExpression)expression;
			return new ArithmeticInfixExpression( (Expression)copyAbstractNode( arithmetic.leftOperand.getValue() ), arithmetic.operator.getValue(), (Expression)copyAbstractNode( arithmetic.rightOperand.getValue() ), (AbstractType<?, ?, ?>)copyAbstractNode( arithmetic.getType() ) );
		}
		else if( expression instanceof ConditionalInfixExpression ) {
			ConditionalInfixExpression conditional = (ConditionalInfixExpression)expression;
			return new ConditionalInfixExpression( (Expression)copyAbstractNode( conditional.leftOperand.getValue() ), conditional.operator.getValue(), (Expression)copyAbstractNode( conditional.rightOperand.getValue() ) );
		}
		else if( expression instanceof RelationalInfixExpression ) {
			RelationalInfixExpression relational = (RelationalInfixExpression)expression;
			return new RelationalInfixExpression( (Expression)copyAbstractNode( relational.leftOperand.getValue() ), relational.operator.getValue(), (Expression)copyAbstractNode( relational.rightOperand.getValue() ), (AbstractType<?, ?, ?>)copyAbstractNode( relational.leftOperand.getValue().getType() ), (AbstractType<?, ?, ?>)copyAbstractNode( relational.rightOperand.getValue().getType() ) );
		}
		else {
			throw new ASTCopyException( "Unsupported infix expression type: " + expression.getClass().getSimpleName(), expression );
		}
	}

	protected InstanceCreation copyInstanceCreation( InstanceCreation instanceCreation ) {
		SimpleArgument[] requiredArguments = copyNodeCollection( instanceCreation.requiredArguments.getValue(), SimpleArgument.class );
		SimpleArgument[] variableArguments = copyNodeCollection( instanceCreation.variableArguments.getValue(), SimpleArgument.class );
		JavaKeyedArgument[] keyedArguments = copyNodeCollection( instanceCreation.keyedArguments.getValue(), JavaKeyedArgument.class );

		return new InstanceCreation( (AbstractConstructor)copyAbstractNode( instanceCreation.constructor.getValue() ), requiredArguments, variableArguments, keyedArguments );
	}

	protected LambdaExpression copyLambdaExpression( LambdaExpression expression ) {
		Lambda lambda = expression.value.getValue();

		if( lambda instanceof UserLambda ) {
			return new LambdaExpression( (UserLambda)copyAbstractNode( (UserLambda)lambda ) );
		} else {
			throw new ASTCopyException( "Cannot copy lambda expression: " + expression.getRepr(), null );
		}
	}

	protected LocalAccess copyLocalAccess( LocalAccess localAccess ) {
		LocalAccess rv = new LocalAccess();
		rv.local.setValue( (UserLocal)copyAbstractNode( localAccess.local.getValue() ) );

		return rv;
	}

	protected LogicalComplement copyLogicalComplement( LogicalComplement complement ) {
		return new LogicalComplement( (Expression)copyAbstractNode( complement.operand.getValue() ) );
	}

	protected MethodInvocation copyMethodInvocation( MethodInvocation methodInvocation ) {
		SimpleArgument[] requiredArguments = copyNodeCollection( methodInvocation.requiredArguments.getValue(), SimpleArgument.class );
		SimpleArgument[] variableArguments = copyNodeCollection( methodInvocation.variableArguments.getValue(), SimpleArgument.class );
		JavaKeyedArgument[] keyedArguments = copyNodeCollection( methodInvocation.keyedArguments.getValue(), JavaKeyedArgument.class );

		return new MethodInvocation( (Expression)copyAbstractNode( methodInvocation.expression.getValue() ), (AbstractMethod)copyAbstractNode( methodInvocation.method.getValue() ), requiredArguments, variableArguments, keyedArguments );
	}

	protected ParameterAccess copyParameterAccess( ParameterAccess parameterAccess ) {
		return new ParameterAccess( (UserParameter)copyAbstractNode( parameterAccess.parameter.getValue() ) );
	}

	protected ResourceExpression copyResourceExpression( ResourceExpression expression ) {
		return new ResourceExpression( (AbstractType<?, ?, ?>)copyAbstractNode( expression.getType() ), expression.resource.getValue() );
	}

	protected ShiftInfixExpression copyShiftInfixExpression( ShiftInfixExpression expression ) {
		return new ShiftInfixExpression( (AbstractType<?, ?, ?>)copyAbstractNode( expression.getType() ), (Expression)copyAbstractNode( expression.leftOperand.getValue() ), expression.operator.getValue(), (Expression)copyAbstractNode( expression.rightOperand.getValue() ) );
	}

	protected SuperExpression copySuperExpression( SuperExpression expression ) {
		return new SuperExpression();
	}

	protected StringConcatenation copyStringConcatenation( StringConcatenation expression ) {
		return new StringConcatenation( (Expression)copyAbstractNode( expression.leftOperand.getValue() ), (Expression)copyAbstractNode( expression.rightOperand.getValue() ) );
	}

	protected ThisExpression copyThisExpression( ThisExpression expression ) {
		return new ThisExpression();
	}

	protected ThisExpression copyThisInstanceExpression( ThisInstanceExpression expression ) {
		return new ThisExpression();
	}

	protected TypeExpression copyTypeExpression( TypeExpression expression ) {
		return new TypeExpression( (AbstractType<?, ?, ?>)copyAbstractNode( ( (TypeExpression)expression ).value.getValue() ) );
	}

	/* Statement copy methods */

	protected Statement copyStatement( Statement statement ) {
		Statement rv;
		if( statement instanceof AbstractStatementWithBody ) {
			rv = copyAbstractStatementWithBody( (AbstractStatementWithBody)statement );
		}
		else if( statement instanceof AssertStatement ) {
			rv = copyAssertStatement( (AssertStatement)statement );
		}
		else if( statement instanceof BlockStatement ) {
			rv = copyBlockStatement( (BlockStatement)statement );
		}
		else if( statement instanceof Comment ) {
			rv = copyComment( (Comment)statement );
		}
		else if( statement instanceof ConditionalStatement ) {
			rv = copyConditionalStatement( (ConditionalStatement)statement );
		}
		else if( statement instanceof ConstructorInvocationStatement ) {
			rv = copyConstructorInvocationStatement( (ConstructorInvocationStatement)statement );
		}
		else if( statement instanceof ExpressionStatement ) {
			rv = copyExpressionStatement( (ExpressionStatement)statement );
		}
		else if( statement instanceof LocalDeclarationStatement ) {
			rv = copyLocalDeclarationStatement( (LocalDeclarationStatement)statement );
		}
		else if( statement instanceof ReturnStatement ) {
			rv = copyReturnStatement( (ReturnStatement)statement );
		}
		else {
			throw new ASTCopyException( "Unsupported statement type: " + statement.getClass().getSimpleName(), statement );
		}

		rv.isEnabled.setValue( statement.isEnabled.getValue() );
		return rv;
	}

	protected AbstractStatementWithBody copyAbstractStatementWithBody( AbstractStatementWithBody statement ) {
		if( statement instanceof CountLoop ) {
			CountLoop countLoop = (CountLoop)statement;
			return new CountLoop( (UserLocal)copyAbstractNode( countLoop.variable.getValue() ), (UserLocal)copyAbstractNode( countLoop.constant.getValue() ), (Expression)copyAbstractNode( countLoop.count.getValue() ), (BlockStatement)copyAbstractNode( countLoop.body.getValue() ) );
		}
		else if( statement instanceof DoInOrder ) {
			return new DoInOrder( (BlockStatement)copyAbstractNode( statement.body.getValue() ) );
		}
		else if( statement instanceof DoTogether ) {
			return new DoTogether( (BlockStatement)copyAbstractNode( statement.body.getValue() ) );
		}
		else if( statement instanceof EachInArrayTogether ) {
			EachInArrayTogether eachInTogether = (EachInArrayTogether)statement;
			return new EachInArrayTogether( (UserLocal)copyAbstractNode( eachInTogether.item.getValue() ), (Expression)copyAbstractNode( eachInTogether.array.getValue() ), (BlockStatement)copyAbstractNode( eachInTogether.body.getValue() ) );
		}
		else if( statement instanceof EachInIterableTogether ) {
			EachInIterableTogether eachInTogether = (EachInIterableTogether)statement;
			return new EachInIterableTogether( (UserLocal)copyAbstractNode( eachInTogether.item.getValue() ), (Expression)copyAbstractNode( eachInTogether.iterable.getValue() ), (BlockStatement)copyAbstractNode( eachInTogether.body.getValue() ) );
		}
		else if( statement instanceof ForEachInArrayLoop ) {
			ForEachInArrayLoop forEachLoop = (ForEachInArrayLoop)statement;
			return new ForEachInArrayLoop( (UserLocal)copyAbstractNode( forEachLoop.item.getValue() ), (Expression)copyAbstractNode( forEachLoop.array.getValue() ), (BlockStatement)copyAbstractNode( forEachLoop.body.getValue() ) );
		}
		else if( statement instanceof ForEachInIterableLoop ) {
			ForEachInIterableLoop forEachLoop = (ForEachInIterableLoop)statement;
			return new ForEachInIterableLoop( (UserLocal)copyAbstractNode( forEachLoop.item.getValue() ), (Expression)copyAbstractNode( forEachLoop.iterable.getValue() ), (BlockStatement)copyAbstractNode( forEachLoop.body.getValue() ) );
		}
		else if( statement instanceof WhileLoop ) {
			WhileLoop whileLoop = (WhileLoop)statement;
			return new WhileLoop( copyExpression( whileLoop.conditional.getValue() ), (BlockStatement)copyAbstractNode( whileLoop.body.getValue() ) );
		}
		else {
			throw new ASTCopyException( "Unsupported abstract statement with body type: " + statement.getClass().getSimpleName(), statement );
		}
	}

	protected AssertStatement copyAssertStatement( AssertStatement statement ) {
		return new AssertStatement( (Expression)copyAbstractNode( statement.expression.getValue() ), (Expression)copyAbstractNode( statement.message.getValue() ) );
	}

	protected BlockStatement copyBlockStatement( BlockStatement statement ) {

		if( statement instanceof ConstructorBlockStatement ) {
			return copyConstructorBlockStatement( (ConstructorBlockStatement)statement );
		} else {
			BlockStatement rv = new BlockStatement();

			copyBodyStatements( rv, statement );

			return rv;
		}
	}

	protected Comment copyComment( Comment comment ) {
		return new Comment( comment.text.getValue() );
	}

	protected ConstructorBlockStatement copyConstructorBlockStatement( ConstructorBlockStatement statement ) {
		ConstructorBlockStatement rv = new ConstructorBlockStatement();

		rv.constructorInvocationStatement.setValue( (ConstructorInvocationStatement)copyAbstractNode( statement.constructorInvocationStatement.getValue() ) );
		copyBodyStatements( rv, statement );

		return rv;
	}

	protected ConditionalStatement copyConditionalStatement( ConditionalStatement conditional ) {
		ConditionalStatement rv = new ConditionalStatement();

		for( BooleanExpressionBodyPair pair : conditional.booleanExpressionBodyPairs.getValue() ) {
			rv.booleanExpressionBodyPairs.add( (BooleanExpressionBodyPair)copyAbstractNode( pair ) );
		}

		rv.elseBody.setValue( (BlockStatement)copyAbstractNode( conditional.elseBody.getValue() ) );

		return rv;
	}

	protected ConstructorInvocationStatement copyConstructorInvocationStatement( ConstructorInvocationStatement statement ) {
		SimpleArgument[] requiredArguments = copyNodeCollection( statement.requiredArguments.getValue(), SimpleArgument.class );
		SimpleArgument[] variableArguments = copyNodeCollection( statement.variableArguments.getValue(), SimpleArgument.class );
		JavaKeyedArgument[] keyedArguments = copyNodeCollection( statement.keyedArguments.getValue(), JavaKeyedArgument.class );

		if( statement instanceof SuperConstructorInvocationStatement ) {
			return new SuperConstructorInvocationStatement( (AbstractConstructor)copyAbstractNode( statement.constructor.getValue() ), requiredArguments, variableArguments, keyedArguments );
		}
		else if( statement instanceof ThisConstructorInvocationStatement ) {
			return new ThisConstructorInvocationStatement( (NamedUserConstructor)copyAbstractNode( (NamedUserConstructor)statement.constructor.getValue() ), requiredArguments, variableArguments, keyedArguments );
		}
		else {
			throw new ASTCopyException( "Cannot Copy ConstructorInvocationStatement: " + statement.getRepr(), statement );
		}
	}

	protected ExpressionStatement copyExpressionStatement( ExpressionStatement expressionStatement ) {
		return new ExpressionStatement( (Expression)copyAbstractNode( expressionStatement.expression.getValue() ) );
	}

	protected LocalDeclarationStatement copyLocalDeclarationStatement( LocalDeclarationStatement declaration ) {
		return new LocalDeclarationStatement( (UserLocal)copyAbstractNode( declaration.local.getValue() ), (Expression)copyAbstractNode( declaration.initializer.getValue() ) );
	}

	protected ReturnStatement copyReturnStatement( ReturnStatement statement ) {
		return new ReturnStatement( (AbstractType<?, ?, ?>)copyAbstractNode( statement.expressionType.getValue() ), (Expression)copyAbstractNode( statement.expression.getValue() ) );
	}

	/* Helper copy methods */
	protected final BlockStatement copyBodyStatements( BlockStatement rv, BlockStatement original ) {

		for( Statement statement : original.statements.getValue() ) {
			rv.statements.add( (Statement)copyAbstractNode( statement ) );
		}

		return rv;
	}

	protected final <T extends AbstractNode> ArrayList<T> copyNodeCollection( Collection<T> nodes ) {
		ArrayList<T> rv = edu.cmu.cs.dennisc.java.util.Lists.newArrayListWithInitialCapacity( nodes.size() );

		for( T node : nodes ) {
			rv.add( (T)copyAbstractNode( node ) );
		}

		return rv;
	}

	protected final <T extends AbstractNode> T[] copyNodeCollection( Collection<T> nodes, Class<T> klass ) {
		ArrayList<T> rv = edu.cmu.cs.dennisc.java.util.Lists.newArrayListWithInitialCapacity( nodes.size() );

		for( T node : nodes ) {
			rv.add( (T)copyAbstractNode( node ) );
		}

		return edu.cmu.cs.dennisc.java.lang.ArrayUtilities.createArray( rv, klass );
	}
}
