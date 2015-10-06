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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractNode;
import org.lgna.project.ast.AbstractStatementWithBody;
import org.lgna.project.ast.ArithmeticInfixExpression;
import org.lgna.project.ast.BooleanLiteral;
import org.lgna.project.ast.ConditionalInfixExpression;
import org.lgna.project.ast.ConditionalStatement;
import org.lgna.project.ast.ContentEqualsStrictness;
import org.lgna.project.ast.CountLoop;
import org.lgna.project.ast.DoubleLiteral;
import org.lgna.project.ast.Expression;
import org.lgna.project.ast.ExpressionStatement;
import org.lgna.project.ast.FieldAccess;
import org.lgna.project.ast.IntegerLiteral;
import org.lgna.project.ast.JavaKeyedArgument;
import org.lgna.project.ast.MethodInvocation;
import org.lgna.project.ast.RelationalInfixExpression;
import org.lgna.project.ast.SimpleArgument;
import org.lgna.project.ast.StringLiteral;
import org.lgna.project.ast.ThisExpression;
import org.lgna.project.ast.UserMethod;

/**
 * @author Michelle Ichinco
 */
public class Program {

	private static ArrayList<String> numericParameters = new ArrayList<String>( Arrays.asList( "depth", "factor", "width", "height", "opacity", "walkPace", "distanceBetween", "alongAxisOffset", "amount", "duration" ) );
	private static ArrayList<String> nonNumericParameters = new ArrayList<String>( Arrays.asList( "vehicle", "strideLength", "bounce", "armSwing", "asSeenBy", "animationStyle", "pathStyle", "policy", "text", "direction", "target", "spatialRelation", "bubbleFillColor", "fontColor", "bubbleOutlineColor", "paint", "isVolumePreserved" ) );

	public static boolean is_same_caller( ExpressionStatement e1, ExpressionStatement e2 ) {
		//how is this different from MethodsAPI.getCaller(es) ==  MethodsAPI.getCaller(es2) ??
		if( !( e1.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ||
				!( e2.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ) {
			return false;
		} else {
			org.lgna.project.ast.MethodInvocation m1 = (org.lgna.project.ast.MethodInvocation)e1.expression.getValue();
			org.lgna.project.ast.MethodInvocation m2 = (org.lgna.project.ast.MethodInvocation)e2.expression.getValue();

			if( !( m1.expression.getValue() instanceof org.lgna.project.ast.FieldAccess ) ||
					!( m2.expression.getValue() instanceof org.lgna.project.ast.FieldAccess ) ) {
				return false;
			}

			org.lgna.project.ast.FieldAccess f1 = (org.lgna.project.ast.FieldAccess)m1.expression.getValue();
			org.lgna.project.ast.FieldAccess f2 = (org.lgna.project.ast.FieldAccess)m2.expression.getValue();
			return ( f1.field.getValue() ).equals( f2.field.getValue() );
		}
	}

	@Deprecated
	public static boolean isSameCaller( ExpressionStatement e1, ExpressionStatement e2 ) {
		return is_same_caller( e1, e2 );
	}

	public static boolean is_same_method( ExpressionStatement e1, ExpressionStatement e2 ) {
		if( !( e1.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ||
				!( e2.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ) {
			return false;
		} else {
			org.lgna.project.ast.AbstractMethod m1 = MethodsAPI.getMethod( e1 );
			org.lgna.project.ast.AbstractMethod m2 = MethodsAPI.getMethod( e2 );
			return ( m1 ).equals( m2 );
		}
	}

	@Deprecated
	public static boolean isSameMethod( ExpressionStatement e1, ExpressionStatement e2 ) {
		return is_same_method( e1, e2 );

	}

	public static org.lgna.project.ast.AbstractMethod get_method( ExpressionStatement es ) {
		if( !( es.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ) {
			return null;
		}
		return ( (MethodInvocation)es.expression.getValue() ).method.getValue();
	}

	@Deprecated
	public static org.lgna.project.ast.AbstractMethod getMethod( ExpressionStatement es ) {
		return get_method( es );
	}

	public static org.lgna.project.ast.Expression get_caller( ExpressionStatement es ) {
		if( !( es.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ) {
			return null;
		}

		org.lgna.project.ast.MethodInvocation m = ( (MethodInvocation)es.expression.getValue() );

		if( ( m.expression.getValue() instanceof org.lgna.project.ast.ThisExpression ) ) {
			return ( m.expression.getValue() ); //if caller is "scene"
		}

		if( ( m.expression.getValue() instanceof org.lgna.project.ast.LocalAccess ) ) {
			return ( (org.lgna.project.ast.LocalAccess)m.expression.getValue() ); //if caller is variable
		}

		if( ( m.expression.getValue() instanceof org.lgna.project.ast.FieldAccess ) ) {
			return ( (FieldAccess)m.expression.getValue() ); //returns regular (prop, character, etc. caller)
		}

		if( !( m.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ) {
			return null;
		}

		org.lgna.project.ast.MethodInvocation m2 = ( (MethodInvocation)m.expression.getValue() );
		if( ( m2.expression.getValue() instanceof org.lgna.project.ast.FieldAccess ) ) {
			return ( (FieldAccess)m2.expression.getValue() ); //this returns the caller if it has a get part
		}

		return null;
	}

	@Deprecated
	public static org.lgna.project.ast.Expression getCaller( ExpressionStatement es ) {
		return get_caller( es );
	}

	public static String get_caller_unique_name( org.lgna.project.ast.ExpressionStatement es ) {
		org.lgna.project.ast.Expression caller = get_caller( es );
		if( caller == null ) {
			return null;
		}
		return getCallerUniqueName( caller );
	}

	@Deprecated
	public static String getCallerUniqueName( org.lgna.project.ast.ExpressionStatement es ) {
		return get_caller_unique_name( es );
	}

	public static String get_caller_unique_name( org.lgna.project.ast.Expression e ) {
		if( e instanceof org.lgna.project.ast.FieldAccess ) {
			return ( (org.lgna.project.ast.UserField)( (org.lgna.project.ast.FieldAccess)e ).field.getValue() ).name.getValue();
		}
		else if( e instanceof org.lgna.project.ast.LocalAccess ) {
			return ( (org.lgna.project.ast.UserLocal)( (org.lgna.project.ast.LocalAccess)e ).local.getValue() ).name.getValue();
		}
		else if( e.getType() instanceof org.lgna.project.ast.NamedUserType ) {
			return ( (org.lgna.project.ast.NamedUserType)e.getType() ).getNamePropertyIfItExists().getValue();
		} //this could be neglecting Active Ene??
		else {
			return null;
		}
	}

	@Deprecated
	public static String getCallerUniqueName( org.lgna.project.ast.Expression e ) {
		return get_caller_unique_name( e );
	}

	public static String get_caller_type( org.lgna.project.ast.ExpressionStatement es ) {
		org.lgna.project.ast.Expression caller = getCaller( es );
		if( caller == null ) {
			return null;
		}
		return getCallerType( caller );
	}

	@Deprecated
	public static String getCallerType( org.lgna.project.ast.ExpressionStatement es ) {
		return get_caller_type( es );
	}

	//This method return the type of the caller. For example: ChildPerson, AdultPerson, Banana, Alien, etc.
	//getCallerName returns the name of the character, sometimes can be the same as the type if it has not been changed.
	//For example a ChildPerson that was called Emma. getCallerName will return Emma and getCallerType will return ChildPerson.
	public static String get_caller_type( org.lgna.project.ast.Expression e ) {
		if( e instanceof org.lgna.project.ast.FieldAccess ) {
			return e.getType().getName();
		}
		else if( e instanceof org.lgna.project.ast.LocalAccess ) {
			return e.getType().getName();
		}
		else if( e.getType() instanceof org.lgna.project.ast.NamedUserType ) {
			return ( (org.lgna.project.ast.NamedUserType)e.getType() ).getNamePropertyIfItExists().getValue();
		}
		else {
			return null;
		}
	}

	@Deprecated
	public static String getCallerType( org.lgna.project.ast.Expression e ) {
		return get_caller_type( e );
	}

	public static java.util.HashMap<String, org.lgna.project.ast.Expression> get_parameters_as_dict( ExpressionStatement es ) {
		if( !( es.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ) {
			return null;
		}
		MethodInvocation mi = (MethodInvocation)es.expression.getValue();
		ArrayList<org.lgna.project.ast.SimpleArgument> requiredArgs = mi.requiredArguments.getValue();
		ArrayList<org.lgna.project.ast.SimpleArgument> variableArgs = mi.variableArguments.getValue();
		ArrayList<org.lgna.project.ast.JavaKeyedArgument> keyedArgs = mi.keyedArguments.getValue();

		ArrayList<org.lgna.project.ast.AbstractArgument> allArgs = new ArrayList<org.lgna.project.ast.AbstractArgument>();
		allArgs.addAll( requiredArgs );
		allArgs.addAll( variableArgs );
		allArgs.addAll( keyedArgs );

		java.util.HashMap<String, org.lgna.project.ast.Expression> parameters = new java.util.HashMap<String, org.lgna.project.ast.Expression>();

		for( int i = 0; i < allArgs.size(); i++ ) {
			org.lgna.project.ast.AbstractArgument arg = allArgs.get( i );
			String name = arg.parameter.getValue().getName();
			org.lgna.project.ast.Expression exp = arg.expression.getValue();
			parameters.put( name, exp );
		}

		return parameters;
	}

	@Deprecated
	public static java.util.HashMap<String, org.lgna.project.ast.Expression> getParametersAsMap( ExpressionStatement es ) {
		return get_parameters_as_dict( es );
	}

	private static void fill_keyed_parameters_as_map( java.util.Map<String, org.lgna.project.ast.Expression> map, MethodInvocation mi ) {
		for( JavaKeyedArgument arg : mi.keyedArguments.getValue() ) {
			map.put( arg.getKeyMethod().getName(), org.lgna.project.ast.AstUtilities.getJavaKeyedArgumentSubArgument0Expression( arg ) );
		}
	}

	private static void fill_keyed_parameter_vals_as_map( java.util.Map<String, Object> map, MethodInvocation mi ) {
		for( JavaKeyedArgument arg : mi.keyedArguments.getValue() ) {
			if( org.lgna.project.ast.AstUtilities.getJavaKeyedArgumentSubArgument0Expression( arg ) instanceof DoubleLiteral ) {
				map.put( arg.getKeyMethod().getName(), Program.getParamTypeDouble( org.lgna.project.ast.AstUtilities.getJavaKeyedArgumentSubArgument0Expression( arg ), arg.getKeyMethod().getName() ) );
			}
		}
	}

	@Deprecated
	private static void fillKeyedParametersAsMap( java.util.Map<String, org.lgna.project.ast.Expression> map, MethodInvocation mi ) {
		fill_keyed_parameters_as_map( map, mi );
	}

	public static java.util.Map<String, Object> get_param_vals_as_dict( ExpressionStatement es ) {
		if( !( es.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ) {
			return null;
		}
		MethodInvocation mi = (MethodInvocation)es.expression.getValue();

		java.util.Map<String, Object> map = new java.util.HashMap<String, Object>();

		for( SimpleArgument arg : mi.requiredArguments.getValue() ) {
			map.put( arg.parameter.getValue().getName(), get_parameter( es, arg.parameter.getValue().getName() ) );
		}

		int i = 0;
		for( SimpleArgument arg : mi.variableArguments.getValue() ) {
			map.put( "vararg" + i, arg.expression.getValue() );
			i++;
		}

		fill_keyed_parameter_vals_as_map( map, mi );

		return map;
	}

	public static java.util.Map<String, Expression> get_all_parameters_as_dict( ExpressionStatement es ) {
		if( !( es.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ) {
			return null;
		}
		MethodInvocation mi = (MethodInvocation)es.expression.getValue();

		java.util.Map<String, Expression> map = new java.util.HashMap<String, Expression>();

		for( SimpleArgument arg : mi.requiredArguments.getValue() ) {
			map.put( arg.parameter.getValue().getName(), arg.expression.getValue() );
		}

		int i = 0;
		for( SimpleArgument arg : mi.variableArguments.getValue() ) {
			map.put( "vararg" + i, arg.expression.getValue() );
			i++;
		}

		fill_keyed_parameters_as_map( map, mi );

		return map;
	}

	@Deprecated
	public static java.util.Map<String, org.lgna.project.ast.Expression> getAllParametersAsMap( ExpressionStatement es ) {
		return get_all_parameters_as_dict( es );
	}

	public static java.util.Map<String, org.lgna.project.ast.Expression> get_keyed_parameters_as_map( ExpressionStatement es ) {
		if( !( es.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ) {
			return null;
		}
		MethodInvocation mi = (MethodInvocation)es.expression.getValue();

		java.util.Map<String, org.lgna.project.ast.Expression> map = new java.util.HashMap<String, org.lgna.project.ast.Expression>();

		fill_keyed_parameters_as_map( map, mi );

		return map;
	}

	@Deprecated
	public static java.util.Map<String, org.lgna.project.ast.Expression> getKeyedParametersAsMap( ExpressionStatement es ) {
		return get_keyed_parameters_as_map( es );
	}

	public static boolean are_identical_method_calls( ExpressionStatement e1, ExpressionStatement e2 ) {
		if( !( e1.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ||
				!( e2.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ) {
			return false;
		}

		return e1.contentEquals( e2, ContentEqualsStrictness.DECLARATIONS_EQUAL );
	}

	@Deprecated
	public static boolean areIdenticalMethodCalls( ExpressionStatement e1, ExpressionStatement e2 ) {
		return are_identical_method_calls( e1, e2 );
	}

	public static org.lgna.project.ast.AbstractMethod get_caller_get_part_method( ExpressionStatement es ) {
		if( !( es.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ) {
			return null;
		}

		org.lgna.project.ast.MethodInvocation m = ( (MethodInvocation)es.expression.getValue() );

		if( !( m.expression.getValue() instanceof org.lgna.project.ast.MethodInvocation ) ) {
			return null;
		}

		return ( (MethodInvocation)m.expression.getValue() ).method.getValue();
	}

	@Deprecated
	public static org.lgna.project.ast.AbstractMethod getCallerGetPartMethod( ExpressionStatement es ) {
		return get_caller_get_part_method( es );
	}

	public static org.lgna.project.ast.AbstractMethod get_target_get_part_method( ExpressionStatement es ) {
		if( getParametersAsMap( es ).get( "target" ) == null ) {
			return null;
		}

		if( !( getParametersAsMap( es ).get( "target" ) instanceof org.lgna.project.ast.MethodInvocation ) ) {
			return null;
		}

		return ( (MethodInvocation)getParametersAsMap( es ).get( "target" ) ).method.getValue();
	}

	@Deprecated
	public static org.lgna.project.ast.AbstractMethod getTargetGetPartMethod( ExpressionStatement es ) {
		return get_target_get_part_method( es );
	}

	public static boolean is_empty_user_method( UserMethod method ) {
		for( org.lgna.project.ast.Statement s : method.body.getValue().statements ) {
			if( !InvokedAPI.isInsideDisabledCode( s ) ) {
				if( s instanceof ExpressionStatement ) {
					if( !( MethodsAPI.getMethod( (ExpressionStatement)s ) instanceof UserMethod ) ) {
						return false;
					}
					if( !isEmptyUserMethod( (UserMethod)MethodsAPI.getMethod( (ExpressionStatement)s ) ) ) {
						return false;
					}

				}

				if( ControlConstructAPI.allContainedExpressionStatements( s, true ).size() > 0 ) {// will return an empty list is s isn't an abstractStatementWithbody or ConditionalStatement
					return false;
				}
			}
		}

		return true;
	}

	@Deprecated
	public static boolean isEmptyUserMethod( UserMethod method ) {
		return is_empty_user_method( method );
	}

	private static Double get_result_for_arithmetical_expression( ExpressionStatement expressionStatement, String parameterType ) {

		double _leftOperand, _rightOperand;
		double result = 0;
		String _operator;

		//NR: checks to see if parameter is even in the form of an Arithmetic Expression
		if( !( getAllParametersAsMap( expressionStatement ).get( parameterType ) instanceof ArithmeticInfixExpression ) ) {
			return null;
		}

		ArithmeticInfixExpression _operation = (ArithmeticInfixExpression)( getAllParametersAsMap( expressionStatement ).get( parameterType ) );

		_leftOperand = ( (DoubleLiteral)_operation.leftOperand.getValue() ).value.getValue();
		_rightOperand = ( (DoubleLiteral)_operation.rightOperand.getValue() ).value.getValue();
		_operator = _operation.operator.getValue().toString();

		//ARITHMETICAL
		if( _operator.equals( "PLUS" ) ) {
			result = _leftOperand + _rightOperand;
		}
		else if( _operator.equals( "MINUS" ) ) {
			result = _leftOperand - _rightOperand;
		}
		else if( _operator.equals( "TIMES" ) ) {
			result = _leftOperand * _rightOperand;
		}
		else if( _operator.equals( "REAL_DIVIDE" ) ) {
			result = _leftOperand / _rightOperand;
		}
		else if( _operator.equals( "INTEGER_DIVIDE" ) ) {
			result = _leftOperand / _rightOperand;
		}
		else if( _operator.equals( "REAL_REMAINDER" ) ) {
			result = _leftOperand % _rightOperand;
		}
		else if( _operator.equals( "INTEGER_REMAINDER" ) ) {
			result = _leftOperand % _rightOperand;
		}

		return result;

	}

	private static Double get_result_for_arithmetical_expression( Expression expression, String parameterType ) {

		double _leftOperand, _rightOperand;
		double result = 0;
		String _operator;

		//NR: checks to see if parameter is even in the form of an Arithmetic Expression
		if( !( expression instanceof ArithmeticInfixExpression ) ) {
			return null;
		}

		ArithmeticInfixExpression _operation = (ArithmeticInfixExpression)( expression );

		_leftOperand = ( (DoubleLiteral)_operation.leftOperand.getValue() ).value.getValue();
		_rightOperand = ( (DoubleLiteral)_operation.rightOperand.getValue() ).value.getValue();
		_operator = _operation.operator.getValue().toString();

		//ARITHMETICAL
		if( _operator.equals( "PLUS" ) ) {
			result = _leftOperand + _rightOperand;
		}
		else if( _operator.equals( "MINUS" ) ) {
			result = _leftOperand - _rightOperand;
		}
		else if( _operator.equals( "TIMES" ) ) {
			result = _leftOperand * _rightOperand;
		}
		else if( _operator.equals( "REAL_DIVIDE" ) ) {
			result = _leftOperand / _rightOperand;
		}
		else if( _operator.equals( "INTEGER_DIVIDE" ) ) {
			result = _leftOperand / _rightOperand;
		}
		else if( _operator.equals( "REAL_REMAINDER" ) ) {
			result = _leftOperand % _rightOperand;
		}
		else if( _operator.equals( "INTEGER_REMAINDER" ) ) {
			result = _leftOperand % _rightOperand;
		}

		return result;

	}

	@Deprecated
	private static Double getResultForArithmeticalExpression( ExpressionStatement expressionStatement, String parameterType ) {
		return get_result_for_arithmetical_expression( expressionStatement, parameterType );
	}

	@Deprecated
	private static Double getResultForArithmeticalExpression( Expression expression, String parameterType ) {
		return get_result_for_arithmetical_expression( expression, parameterType );
	}

	private static String formatParameter( String parameterType ) {
		String theParameter = "ERROR";
		Map<String, String> mapNumeric = new HashMap<String, String>();
		Map<String, String> mapNonNumeric = new HashMap<String, String>();
		String[] valueNonNumericArray = nonNumericParameters.toArray( new String[ 0 ] );
		String[] valueNumericArray = numericParameters.toArray( new String[ 0 ] );

		//Covert keys to be toLowerCase
		String[] numericArray = numericParameters.toArray( new String[ 0 ] );
		for( int i = 0; i < numericArray.length; ++i )
		{
			numericArray[ i ] = numericArray[ i ].toLowerCase();
		}

		String[] nonNumericArray = nonNumericParameters.toArray( new String[ 0 ] );
		for( int i = 0; i < nonNumericArray.length; ++i )
		{
			nonNumericArray[ i ] = nonNumericArray[ i ].toLowerCase();
		}

		//Assigning value and key to the Map

		for( int i = 0; i < Math.min( numericArray.length, valueNumericArray.length ); i++ ) {
			mapNumeric.put( numericArray[ i ], valueNumericArray[ i ] );
		}

		for( int i = 0; i < Math.min( nonNumericArray.length, valueNonNumericArray.length ); i++ ) {
			mapNonNumeric.put( nonNumericArray[ i ], valueNonNumericArray[ i ] );
		}

		//Finally return the formated parameter
		if( mapNumeric.containsKey( parameterType ) ) {

			theParameter = mapNumeric.get( parameterType ).toString();

		}
		if( mapNonNumeric.containsKey( parameterType ) ) {

			theParameter = mapNonNumeric.get( parameterType ).toString();

		}

		return theParameter;
	}

	public static String getParamTypeString( ExpressionStatement expressionStatement, String parameterName ) {
		//This is going to break if you use getParamTypeString directly in code test for the parameter Text
		String valueOfParameter = "empty";
		String theParameter = formatParameter( parameterName.toLowerCase() );
		if( theParameter.equalsIgnoreCase( "text" ) ) {
			valueOfParameter = ( (StringLiteral)getAllParametersAsMap( expressionStatement ).get( theParameter ) ).getValueProperty().getValue();//breaks here for text
		}
		else {
			//NR: tests edge cases, parameter shouldn't be null and shouldn't be anything other than a String
			if( !( getAllParametersAsMap( expressionStatement ).get( theParameter ) instanceof FieldAccess ) ) {
				return null;
			}
			FieldAccess paramField = ( (FieldAccess)getAllParametersAsMap( expressionStatement ).get( theParameter ) );
			valueOfParameter = paramField.field.getValue().getName();
		}

		return valueOfParameter;
	}

	public static String getParamTypeString( Expression expression, String parameterName ) {
		//This is going to break if you use getParamTypeString directly in code test for the parameter Text
		String valueOfParameter = "empty";
		String theParameter = formatParameter( parameterName.toLowerCase() );
		if( theParameter.equalsIgnoreCase( "text" ) ) {
			valueOfParameter = ( (StringLiteral)expression ).getValueProperty().getValue();//breaks here for text
		}
		else {
			//NR: tests edge cases, parameter shouldn't be null and shouldn't be anything other than a String
			if( !( expression instanceof FieldAccess ) ) {
				return null;
			}
			FieldAccess paramField = ( (FieldAccess)expression );
			valueOfParameter = paramField.field.getValue().getName();
		}

		return valueOfParameter;
	}

	//NR: primitive types cannot be null, so instead use wrapper classes. I've updated methods accordingly
	public static Double getParamTypeDouble( ExpressionStatement expressionStatement, String parameterName ) {
		double valueOfParameter = 0;
		String theParameter = formatParameter( parameterName.toLowerCase() );

		//NR: tests edge cases, parameter shouldn't be null and shouldn't be anything other than a double
		if( !( ( getAllParametersAsMap( expressionStatement ).get( theParameter ) ) instanceof DoubleLiteral ) ) {
			return null;
		}
		DoubleLiteral paramLiteral = (DoubleLiteral)( getAllParametersAsMap( expressionStatement ).get( theParameter ) );
		valueOfParameter = paramLiteral.getValueProperty().getValue();
		return valueOfParameter;
	}

	public static Double getParamTypeDouble( Expression expression, String parameterName ) {
		double valueOfParameter = 0;
		String theParameter = formatParameter( parameterName.toLowerCase() );

		//NR: tests edge cases, parameter shouldn't be null and shouldn't be anything other than a double
		if( !( expression instanceof DoubleLiteral ) ) {
			return null;
		}
		DoubleLiteral paramLiteral = (DoubleLiteral)( expression );
		valueOfParameter = paramLiteral.getValueProperty().getValue();
		return valueOfParameter;
	}

	public static Double getParamTypeArithmeticExpression( ExpressionStatement expressionStatement, String parameterName ) {

		Double valueOfParameter = 0.0;
		String theParameter = formatParameter( parameterName.toLowerCase() );
		valueOfParameter = getResultForArithmeticalExpression( expressionStatement, theParameter );

		return valueOfParameter;
	}

	public static Double getParamTypeArithmeticExpression( Expression expression, String parameterName ) {

		Double valueOfParameter = 0.0;
		String theParameter = formatParameter( parameterName.toLowerCase() );
		valueOfParameter = getResultForArithmeticalExpression( expression, theParameter );

		return valueOfParameter;
	}

	public static String get_method_name( ExpressionStatement es ) {
		String methodName = Program.get_method( es ).getName();

		return methodName;
	}

	@Deprecated
	public static String getMethodName( ExpressionStatement es ) {
		return get_method_name( es );
	}

	//Normally bad form to return type Object, but here we handle all problematic situations
	//now that this handles all parameter types, can we set specific getParams to private?
	public static Object get_parameter( ExpressionStatement es, String parameterName ) {
		org.lgna.project.ast.Expression param = MethodsAPI.getAllParametersAsMap( es ).get( formatParameter( parameterName.toLowerCase() ) );

		//parameter types
		if( param instanceof DoubleLiteral ) {
			return getParamTypeDouble( es, parameterName );

		}
		else if( param instanceof StringLiteral ) {
			return getParamTypeString( es, parameterName );
		}
		else if( param instanceof FieldAccess ) {
			return getParamTypeString( es, parameterName );

		}
		else if( param instanceof ArithmeticInfixExpression ) {
			return getParamTypeArithmeticExpression( es, parameterName );
		}
		else if( param instanceof BooleanLiteral ) {
			return getParamTypeString( es, parameterName );
		}
		else if( param instanceof ThisExpression )
		{
			return getParamTypeString( es, parameterName );
		}

		//for whatever reason it isn't a possible parameter type, cop out with null
		else {
			return null;
		}
	}

	public static Object get_parameter( Expression es, String parameterName ) {
		org.lgna.project.ast.Expression param = es;

		//parameter types
		if( param instanceof DoubleLiteral ) {
			return getParamTypeDouble( es, parameterName );

		}
		else if( param instanceof StringLiteral ) {
			return getParamTypeString( es, parameterName );
		}
		else if( param instanceof FieldAccess ) {
			return getParamTypeString( es, parameterName );

		}
		else if( param instanceof ArithmeticInfixExpression ) {
			return getParamTypeArithmeticExpression( es, parameterName );
		}
		else if( param instanceof BooleanLiteral ) {
			return getParamTypeString( es, parameterName );
		}
		else if( param instanceof ThisExpression )
		{
			return getParamTypeString( es, parameterName );
		}

		//for whatever reason it isn't a possible parameter type, cop out with null
		else {
			return null;
		}
	}

	@Deprecated
	public static Object getParameter( ExpressionStatement es, String parameterName ) {
		return get_parameter( es, parameterName );
	}

	public static org.lgna.project.ast.Statement get_encasing_statement( org.lgna.project.ast.AbstractNode start ) {
		org.lgna.project.ast.AbstractNode node = start;

		while( node.getParent() != null ) {
			if( org.lgna.project.ast.Statement.class.isAssignableFrom( node.getParent().getClass() ) ) {
				return (org.lgna.project.ast.Statement)node.getParent();
			}
			node = (AbstractNode)node.getParent();
		}

		return null;

	}

	@Deprecated
	public static org.lgna.project.ast.Statement getEncasingStatement( org.lgna.project.ast.AbstractNode start ) {
		return get_encasing_statement( start );
	}

	public static org.lgna.project.ast.UserMethod get_encasing_method( org.lgna.project.ast.AbstractNode start ) {
		org.lgna.project.ast.AbstractNode node = start;

		while( ( node.getParent() != null ) && !( node instanceof org.lgna.project.ast.UserMethod ) ) {
			node = (AbstractNode)node.getParent();
		}

		return (org.lgna.project.ast.UserMethod)node;
	}

	@Deprecated
	public static org.lgna.project.ast.UserMethod getEncasingUserMethod( org.lgna.project.ast.AbstractNode start ) {
		return get_encasing_method( start );
	}

	public static boolean is_contained_by( org.lgna.project.ast.AbstractNode start, Class<org.lgna.project.ast.AbstractNode> type ) {
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

	@Deprecated
	public static boolean isContainedBy( org.lgna.project.ast.AbstractNode start, Class<org.lgna.project.ast.AbstractNode> type ) {
		return is_contained_by( start, type );
	}

	public static org.lgna.project.ast.AbstractNode get_encasing_node_of_type( org.lgna.project.ast.AbstractNode start, Class<org.lgna.project.ast.AbstractNode> type ) {
		return getEncasingNodeOfType( start, type, false );
	}

	@Deprecated
	public static org.lgna.project.ast.AbstractNode getEncasingNodeOfType( org.lgna.project.ast.AbstractNode start, Class<org.lgna.project.ast.AbstractNode> type ) {
		return get_encasing_node_of_type( start, type );
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

	public static List<ExpressionStatement> all_contained_expression_statements( org.lgna.project.ast.Statement state, boolean includeContainedConditionals ) {
		List<ExpressionStatement> esList = new ArrayList<ExpressionStatement>();
		if( ConditionalStatement.class.isAssignableFrom( state.getClass() ) ) { //allows outermost parent to be a conditional while still excluding contained conditionals
			esList.addAll( ifExpressionStatements( (org.lgna.project.ast.ConditionalStatement)state, includeContainedConditionals ) );
			esList.addAll( elseExpressionStatements( (org.lgna.project.ast.ConditionalStatement)state, includeContainedConditionals ) );
			return esList;
		}
		else {
			return addExpressionStatementsHelper( state, includeContainedConditionals );
		}

	}

	@Deprecated
	public static List<ExpressionStatement> allContainedExpressionStatements( org.lgna.project.ast.Statement state, boolean includeContainedConditionals ) {
		return all_contained_expression_statements( state, includeContainedConditionals );
	}

	private static List<ExpressionStatement> addExpressionStatementsHelper( org.lgna.project.ast.Statement state, boolean includeContainedConditionals ) {
		List<ExpressionStatement> esList = new ArrayList<ExpressionStatement>();

		if( AbstractStatementWithBody.class.isAssignableFrom( state.getClass() ) ) {
			for( org.lgna.project.ast.Statement s : ( (AbstractStatementWithBody)state ).body.getValue().statements ) {
				if( s instanceof ExpressionStatement ) {
					esList.add( (ExpressionStatement)s );
				}
				else if( ( AbstractStatementWithBody.class.isAssignableFrom( s.getClass() ) ) || ( ConditionalStatement.class.isAssignableFrom( s.getClass() ) ) ) {
					esList.addAll( addExpressionStatementsHelper( s, includeContainedConditionals ) );
				}
			}
		}
		else if( ConditionalStatement.class.isAssignableFrom( state.getClass() ) && includeContainedConditionals ) {
			esList.addAll( if_expression_statements( (org.lgna.project.ast.ConditionalStatement)state, true ) );
			esList.addAll( else_expression_statements( (org.lgna.project.ast.ConditionalStatement)state, true ) );
		}

		return esList;
	}

	public static List<ExpressionStatement> if_expression_statements( org.lgna.project.ast.ConditionalStatement cs, boolean includeContainedConditionals ) {
		List<ExpressionStatement> esList = new ArrayList<ExpressionStatement>();
		for( org.lgna.project.ast.Statement s : cs.booleanExpressionBodyPairs.get( 0 ).body.getValue().statements ) {
			if( s instanceof org.lgna.project.ast.ExpressionStatement ) {
				esList.add( (ExpressionStatement)s );
			}
			else if( s instanceof org.lgna.project.ast.AbstractStatementWithBody ) {
				esList.addAll( addExpressionStatementsHelper( (AbstractStatementWithBody)s, includeContainedConditionals ) );
			}

			else if( ( s instanceof org.lgna.project.ast.ConditionalStatement ) && includeContainedConditionals ) {
				esList.addAll( ifExpressionStatements( (org.lgna.project.ast.ConditionalStatement)s, true ) );
				esList.addAll( elseExpressionStatements( (org.lgna.project.ast.ConditionalStatement)s, true ) );
			}
		}
		return esList;
	}

	@Deprecated
	public static List<ExpressionStatement> ifExpressionStatements( org.lgna.project.ast.ConditionalStatement cs, boolean includeContainedConditionals ) {
		return if_expression_statements( cs, includeContainedConditionals );
	}

	public static List<ExpressionStatement> else_expression_statements( org.lgna.project.ast.ConditionalStatement cs, boolean includeContainedConditionals ) {
		List<ExpressionStatement> esList = new ArrayList<ExpressionStatement>();
		for( org.lgna.project.ast.Statement s : cs.elseBody.getValue().statements ) {
			if( s instanceof org.lgna.project.ast.ExpressionStatement ) {
				esList.add( (ExpressionStatement)s );
			}
			else if( s instanceof org.lgna.project.ast.AbstractStatementWithBody ) {
				esList.addAll( addExpressionStatementsHelper( (AbstractStatementWithBody)s, includeContainedConditionals ) );
			}
			else if( ( s instanceof org.lgna.project.ast.ConditionalStatement ) && includeContainedConditionals ) {
				esList.addAll( if_expression_statements( (org.lgna.project.ast.ConditionalStatement)s, true ) );
				esList.addAll( else_expression_statements( (org.lgna.project.ast.ConditionalStatement)s, true ) );
			}
		}
		return esList;
	}

	@Deprecated
	public static List<ExpressionStatement> elseExpressionStatements( org.lgna.project.ast.ConditionalStatement cs, boolean includeContainedConditionals ) {
		return else_expression_statements( cs, includeContainedConditionals );
	}

	public static List<org.lgna.project.ast.Expression> get_array_objects( AbstractStatementWithBody abs ) {

		List<org.lgna.project.ast.Expression> eList = new ArrayList<org.lgna.project.ast.Expression>();
		if( abs instanceof org.lgna.project.ast.EachInArrayTogether ) {
			for( org.lgna.project.ast.Expression e : ( (org.lgna.project.ast.ArrayInstanceCreation)( (org.lgna.project.ast.EachInArrayTogether)abs ).getArrayProperty().getValue() ).expressions ) {
				eList.add( e );
			}
		}
		if( abs instanceof org.lgna.project.ast.ForEachInArrayLoop ) {
			for( org.lgna.project.ast.Expression e : ( (org.lgna.project.ast.ArrayInstanceCreation)( (org.lgna.project.ast.ForEachInArrayLoop)abs ).getArrayProperty().getValue() ).expressions ) {
				eList.add( e );
			}
		}
		return eList;
	}

	@Deprecated
	public static List<org.lgna.project.ast.Expression> getArrayObjects( AbstractStatementWithBody abs ) {
		return get_array_objects( abs );
	}

	public static int get_counter( CountLoop countLoop ) {

		int theCounter;
		theCounter = ( (IntegerLiteral)countLoop.count.getValue() ).getValueProperty().getValue();
		return theCounter;

	}

	@Deprecated
	public static int getCounter( CountLoop countLoop ) {
		return get_counter( countLoop );
	}

	public static boolean get_condition( org.lgna.project.ast.WhileLoop whileLoop ) {

		boolean theCondition;
		theCondition = ( (BooleanLiteral)whileLoop.conditional.getValue() ).getValueProperty().getValue();
		return theCondition;

	}

	@Deprecated
	public static boolean getCondition( org.lgna.project.ast.WhileLoop whileLoop ) {
		return get_condition( whileLoop );
	}

	public static int get_size( AbstractStatementWithBody typeOfConstruct ) {
		int size = typeOfConstruct.body.getValue().statements.size();
		return size;
	}

	@Deprecated
	public static int getSize( AbstractStatementWithBody typeOfConstruct ) {
		return get_size( typeOfConstruct );
	}

	public static Boolean getParamTypeRelationalExpression( org.lgna.project.ast.Statement statement ) {

		Boolean valueOfParameter = null;
		if( statement instanceof org.lgna.project.ast.WhileLoop ) {
			valueOfParameter = getParameterTypeRelationalExpressionWL( (org.lgna.project.ast.WhileLoop)statement );
		}
		else if( statement instanceof ConditionalStatement ) {
			valueOfParameter = getParameterTypeRelationalExpressionCS( (ConditionalStatement)statement );
		}
		else {
			return null;
		}
		return valueOfParameter;
	}

	//for Conditional Statements
	private static Boolean getParameterTypeRelationalExpressionCS( ConditionalStatement conditionalStatement ) {

		Boolean valueOfParameter = null;

		if( ( conditionalStatement.booleanExpressionBodyPairs.get( 0 ).expression.getValue() instanceof ConditionalInfixExpression ) ) {
			boolean _leftOperand;
			boolean _rightOperand;
			String _operator;

			ConditionalInfixExpression _operation = (ConditionalInfixExpression)( conditionalStatement.booleanExpressionBodyPairs.get( 0 ).expression.getValue() );

			_leftOperand = ( (BooleanLiteral)( _operation ).leftOperand.getValue() ).value.getValue();
			_rightOperand = ( (BooleanLiteral)( _operation ).rightOperand.getValue() ).value.getValue();
			_operator = ( _operation ).operator.getValue().toString();

			valueOfParameter = getResultForConditionalExpression( _leftOperand, _rightOperand, _operator );
		}
		else if( ( conditionalStatement.booleanExpressionBodyPairs.get( 0 ).expression.getValue() instanceof RelationalInfixExpression ) ) {
			Double _leftOperand;
			Double _rightOperand;
			String _operator;

			RelationalInfixExpression _operation = (RelationalInfixExpression)( conditionalStatement.booleanExpressionBodyPairs.get( 0 ).expression.getValue() );

			_leftOperand = ( (DoubleLiteral)( _operation ).leftOperand.getValue() ).value.getValue();
			_rightOperand = ( (DoubleLiteral)( _operation ).rightOperand.getValue() ).value.getValue();
			_operator = ( _operation ).operator.getValue().toString();

			valueOfParameter = getResultForRelationalExpression( _leftOperand, _rightOperand, _operator );
		}

		return valueOfParameter;
	}

	//for While Loops
	private static Boolean getParameterTypeRelationalExpressionWL( org.lgna.project.ast.WhileLoop whileLoop ) {

		Boolean valueOfParameter = null;

		if( ( whileLoop.conditional.getValue() instanceof ConditionalInfixExpression ) ) {
			Boolean _leftOperand;
			Boolean _rightOperand;
			String _operator;

			ConditionalInfixExpression _operation = (ConditionalInfixExpression)( whileLoop.conditional.getValue() );

			_leftOperand = ( (BooleanLiteral)( _operation ).leftOperand.getValue() ).value.getValue();
			_rightOperand = ( (BooleanLiteral)( _operation ).rightOperand.getValue() ).value.getValue();
			_operator = ( _operation ).operator.getValue().toString();

			valueOfParameter = getResultForConditionalExpression( _leftOperand, _rightOperand, _operator );
		}
		else if( ( whileLoop.conditional.getValue() instanceof RelationalInfixExpression ) ) {
			Double _leftOperand;
			Double _rightOperand;
			String _operator;

			RelationalInfixExpression _operation = (RelationalInfixExpression)( whileLoop.conditional.getValue() );

			_leftOperand = ( (DoubleLiteral)( _operation ).leftOperand.getValue() ).value.getValue();
			_rightOperand = ( (DoubleLiteral)( _operation ).rightOperand.getValue() ).value.getValue();
			_operator = ( _operation ).operator.getValue().toString();

			valueOfParameter = getResultForRelationalExpression( _leftOperand, _rightOperand, _operator );
		}

		return valueOfParameter;
	}

	//evaluates conditional
	private static Boolean getResultForRelationalExpression( double left, double right, String operator ) {
		boolean result = true;

		//RELATIONAL
		if( operator.equals( "LESS_EQUALS" ) ) {
			if( left <= right ) {
				result = true;
			}
		}
		else if( operator.equals( "GREATER_EQUALS" ) ) {
			if( left >= right ) {
				result = true;
			}
		}
		else if( operator.equals( "LESS" ) ) {
			if( left < right ) {
				result = true;
			}
		}
		else if( operator.equals( "EQUALS" ) ) {
			if( left == right ) {
				result = true;
			}
		}
		else if( operator.equals( "GREATER" ) ) {
			if( left > right ) {
				result = true;
			}
		}
		else if( operator.equals( "NOT_EQUALS" ) ) {
			if( left != right ) {
				result = true;
			}
		} else {
			result = false;
		}
		return result;
	}

	//evaluates conditional
	private static Boolean getResultForConditionalExpression( boolean left, boolean right, String operator ) {
		Boolean result = null;

		//RELATIONAL
		if( operator.equals( "AND" ) ) {
			if( left && right ) {
				result = true;
			}
			else {
				return false;
			}
		}
		else if( operator.equals( "OR" ) ) {
			if( left || right ) {
				result = true;
			}
			else {
				result = false;
			}
		}
		return result;
	}

}
