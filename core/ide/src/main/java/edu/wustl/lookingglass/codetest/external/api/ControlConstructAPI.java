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
import java.util.List;

import org.lgna.project.ast.AbstractStatementWithBody;
import org.lgna.project.ast.BooleanLiteral;
import org.lgna.project.ast.ConditionalInfixExpression;
import org.lgna.project.ast.ConditionalStatement;
import org.lgna.project.ast.CountLoop;
import org.lgna.project.ast.DoubleLiteral;
import org.lgna.project.ast.ExpressionStatement;
import org.lgna.project.ast.IntegerLiteral;
import org.lgna.project.ast.RelationalInfixExpression;

/**
 * @author Reilly Ellis, Yoanna Dosouto, Noah Rowlett
 */
@Deprecated
public class ControlConstructAPI {

	public static List<ExpressionStatement> allContainedExpressionStatements( org.lgna.project.ast.Statement state, boolean includeContainedConditionals ) {
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
			esList.addAll( ifExpressionStatements( (org.lgna.project.ast.ConditionalStatement)state, true ) );
			esList.addAll( elseExpressionStatements( (org.lgna.project.ast.ConditionalStatement)state, true ) );
		}

		return esList;
	}

	public static List<ExpressionStatement> ifExpressionStatements( org.lgna.project.ast.ConditionalStatement cs, boolean includeContainedConditionals ) {
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

	public static List<ExpressionStatement> elseExpressionStatements( org.lgna.project.ast.ConditionalStatement cs, boolean includeContainedConditionals ) {
		List<ExpressionStatement> esList = new ArrayList<ExpressionStatement>();
		for( org.lgna.project.ast.Statement s : cs.elseBody.getValue().statements ) {
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

	public static List<org.lgna.project.ast.Expression> getArrayObjects( AbstractStatementWithBody abs ) {

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

	public static int getCounter( CountLoop countLoop ) {

		int theCounter;
		theCounter = ( (IntegerLiteral)countLoop.count.getValue() ).getValueProperty().getValue();
		return theCounter;

	}

	public static boolean getCondition( org.lgna.project.ast.WhileLoop whileLoop ) {

		boolean theCondition;
		theCondition = ( (BooleanLiteral)whileLoop.conditional.getValue() ).getValueProperty().getValue();
		return theCondition;

	}

	public static int getSize( AbstractStatementWithBody typeOfConstruct ) {
		int size = typeOfConstruct.body.getValue().statements.size();
		return size;
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
