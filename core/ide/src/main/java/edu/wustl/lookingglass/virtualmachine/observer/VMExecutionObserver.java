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
package edu.wustl.lookingglass.virtualmachine.observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.lgna.common.ComponentThread;
import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractStatementWithBody;
import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.DoTogether;
import org.lgna.project.ast.EachInArrayTogether;
import org.lgna.project.ast.Expression;
import org.lgna.project.ast.MethodInvocation;
import org.lgna.project.ast.Node;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.UserField;
import org.lgna.project.ast.UserLambda;
import org.lgna.project.ast.UserMethod;
import org.lgna.project.virtualmachine.UserInstance;
import org.lgna.story.SScene;

import edu.cmu.cs.dennisc.java.util.Lists;
import edu.cmu.cs.dennisc.java.util.Maps;
import edu.cmu.cs.dennisc.java.util.Sets;
import edu.wustl.lookingglass.virtualmachine.MessagingVirtualMachine;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ConditionalStatementEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ContainerEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.CountLoopEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EachInArrayTogetherEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EventNodeFactory;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ExpressionEvaluationEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ExpressionStatementEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ForEachInArrayLoopEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.LambdaEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.LocalDeclarationStatementEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ReturnStatementEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.VMObservableEvent;
import edu.wustl.lookingglass.virtualmachine.eventtracing.WhileLoopEventNode;
import edu.wustl.lookingglass.virtualmachine.observer.event.CurrentExecutionListener;

/**
 * An object that receives <code>VMObserver</code> events from the
 * <code>VMObserverManager</code>, processes these events, creates the
 * appropriate <code>EventNode</code>, and then notifies any listeners of
 * statement execution.
 *
 * @author Paul Gross
 */
public class VMExecutionObserver implements VMObserver {

	private boolean isRecording = false;
	private boolean hasInvokedEntryPoint = false;

	private final Map<Object, UserField> instanceMap = Maps.newHashMap();
	private final Set<CurrentExecutionListener> executionListeners = Sets.newHashSet();

	private final java.util.Map<Statement, java.util.ArrayList<AbstractEventNode<?>>> statementEventNodes = Maps.newHashMap();
	private final java.util.List<LambdaEventNode> lambdaEventNodes = Lists.newLinkedList();

	private final KeyedStack<ComponentThread, AbstractEventNode<?>> eventNodeStack = new KeyedStack<ComponentThread, AbstractEventNode<?>>();
	private AbstractEventNode<?> rootEventNode;
	private UserInstance sceneInstance;

	public VMExecutionObserver( MessagingVirtualMachine virtualMachine ) {
		virtualMachine.addObserver( this );
	}

	public void addCurrentExecutionListener( CurrentExecutionListener listener ) {
		synchronized( this.executionListeners ) {
			this.executionListeners.add( listener );
		}
	}

	public boolean removeCurrentExecutionListener( CurrentExecutionListener listener ) {
		synchronized( this.executionListeners ) {
			return this.executionListeners.remove( listener );
		}
	}

	public UserInstance getSceneInstance() {
		return this.sceneInstance;
	}

	public boolean eventNodesExistForStatement( Statement statement ) {
		java.util.Collection<AbstractEventNode<?>> nodes = this.statementEventNodes.get( statement );
		return ( nodes != null ) && ( nodes.size() > 0 );
	}

	public java.util.ArrayList<AbstractEventNode<?>> getEventNodesForStatement( Statement statement ) {
		return this.statementEventNodes.get( statement );
	}

	public AbstractEventNode<?> getEventNodeForStatementAtTime( Statement statement, double time ) {
		for( AbstractEventNode<?> event : getEventNodesForStatement( statement ) ) {
			if( event.isExecutingAtTime( time ) ) {
				return event;
			}
		}
		return null;
	}

	public boolean isRootEventNode( AbstractEventNode<?> eventNode ) {
		return ( eventNode == this.rootEventNode );
	}

	public AbstractEventNode<?> getRootEventNode() {
		return this.rootEventNode;
	}

	public java.util.List<LambdaEventNode> getLambdaEventNodes() {
		return this.lambdaEventNodes;
	}

	public Map<Object, UserField> getInstanceMap() {
		return this.instanceMap;
	}

	public boolean hasInvokedEntryPoint() {
		return this.hasInvokedEntryPoint;
	}

	public boolean isRecording() {
		return this.isRecording;
	}

	public void setIsRecording( boolean value ) {
		this.isRecording = value;
	}

	/**
	 * Called when a new {@link VMObservableEvent} is passed. Executes the
	 * correct method based on the {@link VMMessage} for the event.
	 *
	 * @param event {@link VMObservableEvent} passed from
	 *            {@link VMObserverManager}
	 */
	@Override
	public synchronized void update( VMObservableEvent event ) {
		VMMessage eventMsg = event.getVMMessage();

		if( eventMsg == VMMessage.START_RECORDING ) {
			this.isRecording = true;
		}
		else if( eventMsg == VMMessage.STOP_RECORDING ) {
			this.isRecording = false;
		}
		else if( eventMsg == VMMessage.CREATE_INSTANCE ) {
			if( ( event.getProperties().length == 1 ) && ( event.getProperties()[ 0 ] instanceof UserField ) ) {
				UserField field = (UserField)event.getProperties()[ 0 ];
				Object instance = event.getObject();

				if( instance instanceof UserInstance ) {
					if( ( (UserInstance)instance ).getType().isAssignableTo( SScene.class ) ) {
						this.sceneInstance = (UserInstance)instance;
					}

					instance = ( (UserInstance)instance ).getJavaInstance();
				}

				this.instanceMap.put( instance, field );
			}
		}

		// Lambda invocations come from eventListeners and are
		// recorded outside the default record loop
		if( eventMsg == VMMessage.START_LAMBDA_INVOKE ) {
			if( event.getObject() instanceof UserLambda ) {
				startLambdaInvocation( event );
			} else {
				throw new ExecutionObserverException( event );
			}
		}
		else if( eventMsg == VMMessage.END_LAMBDA_INVOKE ) {
			if( event.getObject() instanceof UserLambda ) {
				endLambdaInvocation( event );
			} else {
				throw new ExecutionObserverException( event );
			}
		}

		// Should we record this message?
		boolean shouldRecord = isRecording();
		if( !shouldRecord ) {
			if( event.getThread() != null ) {
				shouldRecord = event.getThread().getDescription().contentEquals( "eventThread" );
			}
		}

		synchronized( this ) {
			if( shouldRecord ) {
				switch( eventMsg ) {
				case RESET:
					reset();
					break;
				case START_INVOKE_ENTRY_POINT:
					this.hasInvokedEntryPoint = true;
					break;
				case START_EXP_EVAL:
					if( this.hasInvokedEntryPoint ) {
						if( event.getObject() instanceof Expression ) {

							// Non-function method invocations are handled separately
							if( event.getObject() instanceof MethodInvocation ) {
								MethodInvocation methodInvocation = (MethodInvocation)event.getObject();
								if( methodInvocation.method.getValue().isFunction() ) {
									startExpressionEvaluation( event );
								}
							} else {
								startExpressionEvaluation( event );
							}
						} else {
							throw new ExecutionObserverException( event );
						}
					}
					break;
				case END_EXP_EVAL:
					if( this.hasInvokedEntryPoint ) {
						if( event.getObject() instanceof Expression ) {

							// Non-function method invocations are handled separately
							if( event.getObject() instanceof MethodInvocation ) {
								MethodInvocation methodInvocation = (MethodInvocation)event.getObject();
								if( methodInvocation.method.getValue().isFunction() ) {
									endExpressionEvaluation( event );
								}
							} else {
								endExpressionEvaluation( event );
							}
						} else {
							throw new ExecutionObserverException( event );
						}
					}
					break;
				case START_METHOD_INVOKE:
					if( this.hasInvokedEntryPoint ) {
						if( event.getObject() instanceof MethodInvocation ) {
							startMethodInvocation( event );
						} else {
							throw new ExecutionObserverException( event );
						}
					}
					break;
				case START_STMT_EXEC:
					if( this.hasInvokedEntryPoint ) {
						if( event.getObject() instanceof BlockStatement ) {
							//pass
							//						} else if( event.getObject() instanceof Comment ) {
							//							//pass
							//						} else {
						} else {
							startStatement( event );
						}
					}
					break;
				case END_STMT_EXEC:
					if( this.hasInvokedEntryPoint ) {
						if( event.getObject() instanceof BlockStatement ) {
							//							//pass
							//						} else if( event.getObject() instanceof Comment ) {
							//							//pass
							//						} else {
						} else {
							endStatement( event );
						}
					}
					break;
				case START_CONTAINER:
					if( this.hasInvokedEntryPoint ) {
						if( ( event.getObject() instanceof AbstractStatementWithBody ) || ( event.getObject() instanceof BlockStatement ) ) {
							startContainer( event );
						} else {
							throw new ExecutionObserverException( event );
						}
					}
					break;
				case END_CONTAINER:
					if( this.hasInvokedEntryPoint ) {
						if( ( event.getObject() instanceof AbstractStatementWithBody ) || ( event.getObject() instanceof BlockStatement ) ) {
							endContainer( event );
						} else {
							throw new ExecutionObserverException( event );
						}
					}
					break;
				}
			}
		}
	}

	private void startLambdaInvocation( VMObservableEvent event ) {
		org.lgna.project.ast.UserLambda lambda = (UserLambda)event.getObject();
		ComponentThread thread = event.getThread();
		double startTime = event.getTime();
		org.lgna.project.ast.AbstractMethod method = (AbstractMethod)event.getProperties()[ 0 ];

		// We ignore scene activation
		if( !method.getName().contentEquals( "sceneActivated" ) ) {
			AbstractEventNode<?> eventNode = EventNodeFactory.createEventNode( lambda, thread, startTime, null );
			( (LambdaEventNode)eventNode ).setInvokingEventMethod( method );

			pushEventNode( eventNode, false );
		}
	}

	private void endLambdaInvocation( VMObservableEvent event ) {
		org.lgna.project.ast.UserLambda lambda = (UserLambda)event.getObject();
		org.lgna.project.ast.AbstractMethod method = (AbstractMethod)event.getProperties()[ 0 ];
		ComponentThread thread = event.getThread();
		double endTime = event.getTime();

		if( !method.getName().contentEquals( "sceneActivated" ) ) {
			AbstractEventNode<?> eventNode = popEventNode( thread, false );

			lambdaEventNodes.add( (LambdaEventNode)eventNode );

			if( ( eventNode != null ) && ( eventNode.getAstNode() == lambda ) && ( eventNode.getThread() == thread ) ) {
				eventNode.setEndTime( endTime );
			} else {
				throw new ExecutionObserverException( "EventNode invalid: " + eventNode.toString(), event );
			}
		}
	}

	/**
	 * Handles the beginning of evaluation of an {@link Expression}.
	 *
	 * @param event VMObservableEvent event for expression
	 */
	private void startExpressionEvaluation( VMObservableEvent event ) {
		Expression expression = (Expression)event.getObject();
		ComponentThread thread = event.getThread();
		double startTime = event.getTime();

		AbstractEventNode<?> parentNode = peekEventNode( thread );

		ExpressionEvaluationEventNode eventNode = (ExpressionEvaluationEventNode)EventNodeFactory.createEventNode( expression, thread, startTime, parentNode );

		// Add expression evaluation to ExpressionStatementEventNode
		if( parentNode instanceof ExpressionStatementEventNode ) {
			( (ExpressionStatementEventNode)parentNode ).addExpressionEvaluationNode( eventNode );
		}
		// Handle expression evaluation for ConditionalEventNode
		else if( parentNode instanceof ConditionalStatementEventNode ) {
			( (ConditionalStatementEventNode)parentNode ).addConditionalEvaluation( eventNode );
		}
		// Handle expression evaluation for CountLoopEventNode
		else if( parentNode instanceof CountLoopEventNode ) {
			( (CountLoopEventNode)parentNode ).setCountExpressionNode( eventNode );
		}
		// Handle expression evaluation for EachInArrayTogetherEventNode
		else if( parentNode instanceof EachInArrayTogetherEventNode ) {
			( (EachInArrayTogetherEventNode)parentNode ).setArrayExpressionNode( eventNode );
		}
		// Handle expression evaluation for ForEachInArrayLoopEventNode
		else if( parentNode instanceof ForEachInArrayLoopEventNode ) {
			( (ForEachInArrayLoopEventNode)parentNode ).setArrayExpressionNode( eventNode );
		}
		// Handle expression evaluation for WhileLoopEventNode
		else if( parentNode instanceof WhileLoopEventNode ) {
			( (WhileLoopEventNode)parentNode ).addConditionalEvaluation( eventNode );
		}
		// Handle expression evaluation for LocalDeclarationStatementEventNode
		else if( parentNode instanceof LocalDeclarationStatementEventNode ) {
			( (LocalDeclarationStatementEventNode)parentNode ).setInitializerExpression( eventNode );
		}
		// Handle expression evaluation for ReturnStatementEventNode
		else if( parentNode instanceof ReturnStatementEventNode ) {
			( (ReturnStatementEventNode)parentNode ).setExpressionNode( eventNode );
		}
		// Handle expression evaluation for ExpressionEvaluationEventNode
		else if( parentNode instanceof ExpressionEvaluationEventNode ) {
			// pass - handled on endExpressionEvaluation
		}
		else {
			throw new ExecutionObserverException( "Parent node invalid:" + parentNode.toString(), event );
		}

		pushEventNode( eventNode, false );
	}

	/**
	 * Handles the ending of evaluation of an {@link Expression}.
	 *
	 * @param event VMObservableEvent event for expression
	 */
	private void endExpressionEvaluation( VMObservableEvent event ) {
		Expression expression = (Expression)event.getObject();
		ComponentThread thread = event.getThread();
		double endTime = event.getTime();
		Object value = event.getProperties()[ 0 ];

		AbstractEventNode<?> eventNode = popEventNode( thread, false );

		if( ( eventNode != null ) && ( eventNode.getAstNode() == expression ) && ( eventNode.getThread() == thread ) ) {
			eventNode.setEndTime( endTime );
			( (ExpressionEvaluationEventNode)eventNode ).setValue( value );

			UserField field = this.getUserFieldForInstance( value );
			if( field != null ) {
				( (ExpressionEvaluationEventNode)eventNode ).setValueField( field );
			}
		} else {
			throw new ExecutionObserverException( "EventNode invalid: " + eventNode.toString(), event );
		}
	}

	/**
	 * Handles beginning execution of a {@link Statement}.
	 *
	 * @param event {@link VMObservableEvent} event for statement
	 */
	private void startStatement( VMObservableEvent event ) {
		Statement statement = (Statement)event.getObject();
		ComponentThread thread = event.getThread();
		double startTime = event.getTime();

		AbstractEventNode<?> parentNode = peekEventNode( getThreadForNode( statement, thread ) );

		// A Statement should always have a parent node (which can only be a ContainerEventNode)
		if( parentNode instanceof ContainerEventNode ) {
			ContainerEventNode<?> containerNode = (ContainerEventNode<?>)parentNode;
			AbstractEventNode<?> eventNode = EventNodeFactory.createEventNode( statement, thread, startTime, containerNode );

			pushEventNode( eventNode, false );
		} else {
			throw new ExecutionObserverException( "Parent node invalid: " + parentNode.toString(), event );
		}
	}

	/**
	 * Handles ending execution of a {@link Statement}.
	 *
	 * @param event {@link VMObservableEvent} event for statement
	 */
	private void endStatement( VMObservableEvent event ) {
		Statement statement = (Statement)event.getObject();
		ComponentThread thread = event.getThread();
		double endTime = event.getTime();

		AbstractEventNode<?> eventNode = popEventNode( thread, true );

		if( ( eventNode != null ) && ( eventNode.getAstNode() == statement ) && ( eventNode.getThread() == thread ) ) {
			eventNode.setEndTime( endTime );
		} else {
			throw new ExecutionObserverException( "EventNode invalid: " + eventNode.toString(), event );
		}
	}

	/**
	 * Handles beginning execution of container statement (
	 * {@link AbstractStatementWithBody} or {@link BlockStatement}).
	 *
	 * @param event {@link VMObservableEvent} event for container type statement
	 */
	private void startContainer( VMObservableEvent event ) {
		Statement statement = (Statement)event.getObject();
		ComponentThread thread = event.getThread();
		double startTime = event.getTime();

		AbstractEventNode<?> parentNode = peekEventNode( getThreadForNode( statement, thread ) );

		// Root case
		if( parentNode == null ) {
			AbstractEventNode<?> eventNode = EventNodeFactory.createEventNode( statement, thread, startTime, null );
			this.rootEventNode = eventNode;
			pushEventNode( eventNode, true );
		}
		else if( parentNode instanceof ConditionalStatementEventNode ) {
			AbstractEventNode<?> eventNode = EventNodeFactory.createEventNode( statement, thread, startTime, parentNode );
			pushEventNode( eventNode, true );
		}
		else if( parentNode instanceof ContainerEventNode ) {
			AbstractEventNode<?> eventNode = EventNodeFactory.createEventNode( statement, thread, startTime, parentNode );
			pushEventNode( eventNode, true );
		}
		else if( parentNode instanceof ExpressionStatementEventNode ) {
			AbstractEventNode<?> eventNode = EventNodeFactory.createEventNode( statement, thread, startTime, parentNode );
			( (ExpressionStatementEventNode)parentNode ).addUserMethodEventNode( (ContainerEventNode<?>)eventNode );
			pushEventNode( eventNode, true );
		}
		else if( parentNode instanceof ExpressionEvaluationEventNode ) {
			AbstractEventNode<?> eventNode = EventNodeFactory.createEventNode( statement, thread, startTime, parentNode );
			pushEventNode( eventNode, true );
		}
		else if( parentNode instanceof LambdaEventNode ) {
			AbstractEventNode<?> eventNode = EventNodeFactory.createEventNode( statement, thread, startTime, parentNode );
			( (LambdaEventNode)parentNode ).addBodyEventNode( (ContainerEventNode<?>)eventNode );
			pushEventNode( eventNode, true );
		}
		else {
			throw new ExecutionObserverException( "Parent node invalid: " + parentNode.toString(), event );
		}
	}

	/**
	 * Handles ending execution of Container statement (
	 * {@link AbstractStatementWithBody} or {@link BlockStatement}).
	 *
	 * @param event {@link VMObservableEvent} event for container type statement
	 */
	private void endContainer( VMObservableEvent event ) {
		Statement statement = (Statement)event.getObject();
		ComponentThread thread = event.getThread();
		double endTime = event.getTime();

		AbstractEventNode<?> eventNode = popEventNode( thread, true );

		if( ( eventNode != null ) && ( eventNode.getAstNode() == statement ) && ( eventNode.getThread() == thread ) ) {
			eventNode.setEndTime( endTime );
		} else {
			throw new ExecutionObserverException( "EventNode invalid: " + eventNode.toString(), event );
		}
	}

	/**
	 * Handles beginning execution of {@link MethodInvocation}.
	 *
	 * @param event {@link VMObservableEvent} event for invocation
	 */
	private void startMethodInvocation( VMObservableEvent event ) {
		MethodInvocation invocation = (MethodInvocation)event.getObject();
		ComponentThread thread = event.getThread();
		Object instance = event.getProperties()[ 1 ];

		UserField field = getUserFieldForInstance( instance );

		AbstractEventNode<?> parentNode = peekEventNode( getThreadForNode( invocation, thread ) );

		// Add instance and referencing field to parent node
		if( parentNode instanceof ExpressionStatementEventNode ) {
			ExpressionStatementEventNode statementNode = (ExpressionStatementEventNode)parentNode;
			if( ( instance != null ) & ( field != null ) ) {
				statementNode.setCallerInstance( instance );
				statementNode.setCallerField( field );
			}

			if( ( instance != null ) & ( field != null ) ) {
				notifyListenersOfStart( statementNode ); // Now that caller has been set, notify listeners
			}

			if( invocation.method.getValue() instanceof UserMethod ) {
				UserMethod userMethod = (UserMethod)invocation.method.getValue();
				if( !userMethod.isFunction() ) {
					statementNode.setUserMethod( userMethod );
				}
			}
		}
		else if( parentNode instanceof ExpressionEvaluationEventNode ) {
			if( field != null ) {
				( (ExpressionEvaluationEventNode)parentNode ).setValueField( field );
			}
		}
	}

	/**
	 * Clears all maps involved in tracking. Note: this method does <i>not</i>
	 * remove <code>CurrentExecutionListeners</code>.
	 */
	private void reset() {
		statementEventNodes.clear();
		eventNodeStack.clear();
	}

	/**
	 * Pushes an {@link AbstractEventNode<?>} onto the stack and executes
	 * associated functions.
	 *
	 * @param eventNode <code>AbstractEventNode<?></code> to push onto the stack
	 * @param shouldNotify boolean dictating whether listeners should be
	 *            notified of push
	 */
	private void pushEventNode( AbstractEventNode<?> eventNode, boolean shouldNotify ) {
		ArrayList<AbstractEventNode<?>> eventNodes = this.statementEventNodes.get( eventNode.getAstNode() );
		if( eventNodes == null ) {
			eventNodes = new ArrayList<AbstractEventNode<?>>();
			if( eventNode.getAstNode() instanceof Statement ) {
				this.statementEventNodes.put( (Statement)eventNode.getAstNode(), eventNodes );
			}
		}
		eventNodes.add( eventNode );
		this.eventNodeStack.push( eventNode, eventNode.getThread() );

		if( shouldNotify ) {
			notifyListenersOfStart( eventNode );
		}
	}

	/**
	 * Peeks {@link AbstractEventNode<?>} from top of stack for given thread.
	 *
	 * @param thread {@link ComponentThread} to check stack for
	 * @return AbstractEventNode<?> on top of stack
	 */
	private AbstractEventNode<?> peekEventNode( ComponentThread thread ) {
		return this.eventNodeStack.peek( thread );
	}

	/**
	 * Removes and returns {@link AbstractEventNode<?>} from top of stack for a
	 * given thread.
	 *
	 * @param thread {@link ComponentThread} to remove stack for
	 * @return <code>AbstractEventNode<?></code> removed from stack
	 */
	private AbstractEventNode<?> popEventNode( ComponentThread thread, boolean shouldNotify ) {
		AbstractEventNode<?> rv = this.eventNodeStack.pop( thread );

		if( shouldNotify ) {
			notifyListenersOfEnd( rv );
		}
		return rv;
	}

	/**
	 * Gets the correct thread for lookup in {@link KeyedStack}. In the instance
	 * of parallel execution (<code>DoTogether</code> and
	 * <code>EachInArrayTogether</code> statements) we want to get the parent
	 * thread.
	 *
	 * @param astNode {@link Node} in AST to check
	 * @param thread {@link ComponentThread} to reference
	 * @return the correct <code>ComponentThread</code> for lookup
	 *
	 */
	private ComponentThread getThreadForNode( Node astNode, ComponentThread thread ) {
		if( astNode.getParent() instanceof BlockStatement ) {
			BlockStatement blockStatement = (BlockStatement)astNode.getParent();
			if( blockStatement.getParent() instanceof DoTogether ) {
				DoTogether doTogether = (DoTogether)blockStatement.getParent();

				// special case for DoTogether with only one statement
				if( doTogether.body.getValue().statements.size() > 1 ) {
					return thread.getParentThread();
				} else {
					return thread;
				}
			} else {
				return thread;
			}
		} else if( astNode.getParent() instanceof EachInArrayTogether ) {
			return thread.getParentThread();
		} else {
			return thread;
		}
	}

	private void notifyListenersOfStart( AbstractEventNode<?> eventNode ) {
		synchronized( this.executionListeners ) {
			for( CurrentExecutionListener listener : this.executionListeners ) {
				listener.startingExecution( eventNode );
			}
		}
	}

	private void notifyListenersOfEnd( AbstractEventNode<?> eventNode ) {
		synchronized( this.executionListeners ) {
			for( CurrentExecutionListener listener : this.executionListeners ) {
				listener.endingExecution( eventNode );
			}
		}
	}

	/**
	 * Based on provided <code>Object</code>, finds the applicable field set
	 * during instance creation.
	 *
	 * @param caller object evaluated by vm
	 * @return mapped <code>UserField</code> for value
	 */
	private UserField getUserFieldForInstance( Object instance ) {
		if( instance instanceof UserInstance ) {
			instance = ( (UserInstance)instance ).getJavaInstance();
		}
		// Get instance from invocation - special case for joints
		else if( instance instanceof org.lgna.story.SJoint ) {
			org.lgna.story.SJoint joint = (org.lgna.story.SJoint)instance;

			org.lgna.story.implementation.JointImp jointImp = org.lgna.story.EmployeesOnly.getImplementation( joint );
			org.lgna.story.implementation.JointedModelImp<?, ?> jointedModelImp = jointImp.getJointedModelParent();
			org.lgna.story.SJointedModel jointedModel = jointedModelImp.getAbstraction();
			instance = jointedModel;
		}

		return this.instanceMap.get( instance );
	}

	/**
	 * Specialized stack-like structure that allows for multiple stacks based on
	 * a key. Each key passed into the <code>KeyedStack</code> is given a unique
	 * <code>Stack</code> and all interactions with the <code>KeyedStack</code>
	 * require said key.
	 *
	 * @author Michael Pogran
	 */
	private class KeyedStack<K, V> {
		private HashMap<K, java.util.Stack<V>> stackMap = Maps.newHashMap();

		public V push( V value, K key ) {
			java.util.Stack<V> stack = this.stackMap.get( key );
			if( stack == null ) {
				stack = new java.util.Stack<V>();
				this.stackMap.put( key, stack );
			}
			return stack.push( value );
		}

		public V peek( K key ) {
			java.util.Stack<V> stack = this.stackMap.get( key );
			if( stack != null ) {
				return stack.peek();
			} else {
				return null;
			}
		}

		public V pop( K key ) {
			java.util.Stack<V> stack = this.stackMap.get( key );
			if( stack != null ) {
				return stack.pop();
			} else {
				return null;
			}
		}

		public void clear() {
			stackMap = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();
		}
	}

}
