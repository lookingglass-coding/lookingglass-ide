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
package edu.wustl.lookingglass.virtualmachine;

import org.lgna.common.ComponentThread;
import org.lgna.project.ast.UserField;
import org.lgna.project.virtualmachine.UserInstance;
import org.lgna.project.virtualmachine.events.ExpressionEvaluationEvent;

import edu.wustl.lookingglass.ide.program.ReplayableProgramImp;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EventNodeUtilities;
import edu.wustl.lookingglass.virtualmachine.observer.VMMessage;
import edu.wustl.lookingglass.virtualmachine.observer.VMObserver;
import edu.wustl.lookingglass.virtualmachine.observer.VMObserverManager;

/**
 *
 * This manages adding an asynchronous observer to the vm. Assume there is some
 * bug or something else in the observer that goes sour, we never want to crash
 * the vm or the program. Anything observing the vm gets messages about what the
 * vm is doing in threads outside the vm thread that way they don't interfere
 * with the vm.
 *
 * @author Paul Gross
 */
public class MessagingVirtualMachine extends StateListeningVirtualMachine {

	private VMObserverManager observerManager;
	private org.lgna.project.ast.NamedUserType programType;
	private ReplayableProgramImp executingProgram = null;

	public MessagingVirtualMachine() {
		this.observerManager = new VMObserverManager();
		this.observerManager.startObserving();

		this.addVirtualMachinePauseStateListener( this.observerManager );

		this.addVirtualMachineListener( new org.lgna.project.virtualmachine.events.VirtualMachineListener() {

			@Override
			public void statementExecuting( org.lgna.project.virtualmachine.events.StatementExecutionEvent statementEvent ) {
				org.lgna.project.ast.Statement statement = statementEvent.getStatement();
				if( statement.isEnabled.getValue() ) {
					if( EventNodeUtilities.isContainerStatementType( statement ) ) {
						observerManager.addEvent( VMMessage.START_CONTAINER,
								getRunningProgramTime(),
								statement,
								ComponentThread.currentThread() );
					} else {
						observerManager.addEvent( VMMessage.START_STMT_EXEC,
								getRunningProgramTime(),
								statement,
								ComponentThread.currentThread() );
					}
				}

				if( statement instanceof org.lgna.project.ast.ExpressionStatement ) {
					org.lgna.project.ast.ExpressionStatement expressionStatement = (org.lgna.project.ast.ExpressionStatement)statement;
					org.lgna.project.ast.Expression expression = expressionStatement.expression.getValue();

					if( expression instanceof org.lgna.project.ast.AssignmentExpression ) {
						org.lgna.project.ast.AssignmentExpression assignmentExpression = (org.lgna.project.ast.AssignmentExpression)expression;
						org.lgna.project.ast.Expression leftHandExpression = assignmentExpression.leftHandSide.getValue();

						if( ( leftHandExpression instanceof org.lgna.project.ast.FieldAccess ) ||
								( leftHandExpression instanceof org.lgna.project.ast.LocalAccess ) ||
								( leftHandExpression instanceof org.lgna.project.ast.ArrayAccess ) ) {
							observerManager.addEvent( VMMessage.ASSIGN_VALUE,
									getRunningProgramTime(),
									assignmentExpression,
									ComponentThread.currentThread(),
									leftHandExpression );
						}
					}
				}
			}

			@Override
			public void statementExecuted( org.lgna.project.virtualmachine.events.StatementExecutionEvent statementEvent ) {
				org.lgna.project.ast.Statement statement = statementEvent.getStatement();
				if( statement.isEnabled.getValue() ) {
					if( EventNodeUtilities.isContainerStatementType( statement ) ) {
						observerManager.addEvent( VMMessage.END_CONTAINER,
								getRunningProgramTime(),
								statement,
								ComponentThread.currentThread() );
					} else {
						observerManager.addEvent( VMMessage.END_STMT_EXEC,
								getRunningProgramTime(),
								statement,
								ComponentThread.currentThread() );
					}
				}
			}

			@Override
			public void expressionEvaluating( ExpressionEvaluationEvent expressionEvaluationEvent ) {
				observerManager.addEvent( VMMessage.START_EXP_EVAL,
						getRunningProgramTime(),
						expressionEvaluationEvent.getExpression(),
						ComponentThread.currentThread(),
						expressionEvaluationEvent.getValue() );
			}

			@Override
			public void expressionEvaluated( org.lgna.project.virtualmachine.events.ExpressionEvaluationEvent expressionEvaluationEvent ) {
				observerManager.addEvent( VMMessage.END_EXP_EVAL,
						getRunningProgramTime(),
						expressionEvaluationEvent.getExpression(),
						ComponentThread.currentThread(),
						expressionEvaluationEvent.getValue() );
			}

			@Override
			public void methodInvoking( org.lgna.project.virtualmachine.events.MethodInvocationEvent methodInvocationEvent ) {
				observerManager.addEvent( VMMessage.START_METHOD_INVOKE,
						getRunningProgramTime(),
						methodInvocationEvent.getMethodInvocation(),
						ComponentThread.currentThread(),
						methodInvocationEvent.getMethod(),
						methodInvocationEvent.getInstance() );

			}

			@Override
			public void methodInvoked( org.lgna.project.virtualmachine.events.MethodInvocationEvent methodInvocationEvent ) {
				observerManager.addEvent( VMMessage.END_METHOD_INVOKE,
						getRunningProgramTime(),
						methodInvocationEvent.getMethodInvocation(),
						ComponentThread.currentThread(),
						methodInvocationEvent.getMethod(),
						methodInvocationEvent.getInstance() );
			}

			@Override
			public void lambdaExecuting( org.lgna.project.virtualmachine.events.LambdaEvent lambdaEvent ) {
				observerManager.addEvent( VMMessage.START_LAMBDA_INVOKE,
						getRunningProgramTime(),
						lambdaEvent.getLambda(),
						ComponentThread.currentThread(),
						lambdaEvent.getMethod(),
						lambdaEvent.getThisInstance() );

			}

			@Override
			public void lambdaExecuted( org.lgna.project.virtualmachine.events.LambdaEvent lambdaEvent ) {
				observerManager.addEvent( VMMessage.END_LAMBDA_INVOKE,
						getRunningProgramTime(),
						lambdaEvent.getLambda(),
						ComponentThread.currentThread(),
						lambdaEvent.getMethod(),
						lambdaEvent.getThisInstance() );
			}
		} );
	}

	private Double getRunningProgramTime() {
		if( this.executingProgram != null ) {
			return this.executingProgram.getAnimator().getCurrentTime();
		} else {
			return Double.NaN;
		}
	}

	public void setProgramType( org.lgna.project.ast.NamedUserType programType ) {
		this.programType = programType;
	}

	public void addObserver( VMObserver observer ) {
		this.observerManager.addObserver( observer );
	}

	public void removeObserver( VMObserver observer ) {
		this.observerManager.removeObserver( observer );
	}

	public void setExecutingProgram( ReplayableProgramImp executingProgram ) {
		this.executingProgram = executingProgram;
	}

	public void startRecording() {
		this.observerManager.addEvent( VMMessage.START_RECORDING, getRunningProgramTime() );
	}

	public void stopRecording() {
		this.observerManager.addEvent( VMMessage.STOP_RECORDING, getRunningProgramTime() );
	}

	@Override
	public Object createAndSetFieldInstance( UserInstance userInstance, UserField field ) {
		Object rv = super.createAndSetFieldInstance( userInstance, field );

		observerManager.addEvent( VMMessage.CREATE_INSTANCE,
				getRunningProgramTime(),
				rv,
				ComponentThread.currentThread(),
				field );

		return rv;
	}

	@Override
	public void stopExecution() {
		stopRecording();
		this.observerManager.stopObserving();
		this.executingProgram = null;
		super.stopExecution();
	}

	@Override
	protected java.lang.Object invokeUserMethod( java.lang.Object instance, org.lgna.project.ast.UserMethod method, java.lang.Object... arguments ) {

		if( ( programType != null ) && method.getName().equals( org.alice.stageide.ast.StoryApiSpecificAstUtilities.getUserMain( this.programType ).getName() ) ) {
			startRecording();
			observerManager.addEvent( VMMessage.START_INVOKE_ENTRY_POINT, getRunningProgramTime(), method, ComponentThread.currentThread() );
		}
		Object rv = super.invokeUserMethod( instance, method, arguments );

		if( ( programType != null ) && method.getName().equals( org.alice.stageide.ast.StoryApiSpecificAstUtilities.getUserMain( this.programType ).getName() ) ) {
			observerManager.addEvent( VMMessage.END_INVOKE_ENTRY_POINT, getRunningProgramTime(), method, ComponentThread.currentThread() );
			stopRecording();
		}

		return rv;
	}
}
