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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.alice.nonfree.NebulousIde;
import org.alice.stageide.ast.JointMethodUtilities;
import org.lgna.common.Resource;
import org.lgna.project.Project;
import org.lgna.project.ast.AbstractDeclaration;
import org.lgna.project.ast.AbstractField;
import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractType;
import org.lgna.project.ast.Expression;
import org.lgna.project.ast.ExpressionStatement;
import org.lgna.project.ast.FieldAccess;
import org.lgna.project.ast.JavaType;
import org.lgna.project.ast.LocalAccess;
import org.lgna.project.ast.MethodInvocation;
import org.lgna.project.ast.ParameterAccess;
import org.lgna.project.ast.ResourceExpression;
import org.lgna.project.ast.SimpleArgument;
import org.lgna.project.ast.UserField;
import org.lgna.project.ast.UserLocal;
import org.lgna.project.ast.UserParameter;
import org.lgna.project.virtualmachine.UserInstance;
import org.lgna.story.SGround;
import org.lgna.story.SThing;
import org.lgna.story.implementation.EntityImp;

import edu.cmu.cs.dennisc.java.util.Lists;
import edu.cmu.cs.dennisc.java.util.Maps;
import edu.cmu.cs.dennisc.java.util.Sets;
import edu.cmu.cs.dennisc.scenegraph.Composite;
import edu.cmu.cs.dennisc.scenegraph.Joint;
import edu.wustl.lookingglass.remix.SnippetScript;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ConditionalStatementEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ContainerEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.CountLoopEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EachInArrayTogetherEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ExpressionEvaluationEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ExpressionStatementEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ForEachInArrayLoopEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.LocalDeclarationStatementEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ReturnStatementEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.WhileLoopEventNode;

/**
 * The basic purpose of the <code>RemixReferencesCrawler</code> class is to
 * crawl the{@link AbstractEventNode} tree provided to find references to
 * fields. These fields becomes roles in the {@link SnippetScript} .
 * Additionally, the crawler computes the lowest <code>AbstractType</code>
 * referenced by a field, any vehicle changes, and resources required for the
 * snippet.
 *
 * @author Michael Pogran
 */
public class RemixReferencesCrawler {
	private double startTime;
	private double endTime;

	private Set<UserField> activeFields = Sets.newHashSet();
	private Set<UserField> specialFields = Sets.newHashSet();

	private Map<UserLocal, AbstractEventNode<?>> declaredLocals = Maps.newHashMap(); // The crawler also tracks referenced UserLocals
	private Map<UserLocal, List<ExpressionEvaluationEventNode>> referencedLocals = Maps.newHashMap();
	private Map<UserParameter, List<ExpressionEvaluationEventNode>> referencedParams = Maps.newHashMap();

	private Map<UserField, AbstractType<?, ?, ?>> lowestReferencedType = Maps.newHashMap();
	private Map<UserField, Composite> nonDefaultVehicles = Maps.newHashMap();
	private Set<Resource> requiredResources = Sets.newHashSet();
	private Map<AbstractMethod, Set<UserField>> fieldsForMethods = Maps.newHashMap();
	private Map<UserField, Integer> callerCount = Maps.newHashMap();
	private Map<UserField, Integer> paramCount = Maps.newHashMap();

	private UserInstance sceneInstance;
	private Project project;

	private Map<UUID, AbstractDeclaration> oldToNewDeclarations; // References crawler wants a copy of to old/new declarations mappings from copier

	public RemixReferencesCrawler( AbstractEventNode<?> startEventNode, AbstractEventNode<?> endEventNode ) {
		this.startTime = startEventNode.getStartTime();
		this.endTime = endEventNode.getEndTime();
	}

	public Map<AbstractMethod, Set<UserField>> getFieldsForMethods() {
		return this.fieldsForMethods;
	}

	public Set<UserField> getActiveFields() {
		return this.activeFields;
	}

	public Set<UserField> getSpecialFields() {
		return this.specialFields;
	}

	public void addSpecialField( UserField field ) {
		this.specialFields.add( field );
	}

	public AbstractType<?, ?, ?> getLowestReferencedType( UserField field ) {
		return this.lowestReferencedType.get( field );
	}

	public Set<Resource> getRequiredResources() {
		return this.requiredResources;
	}

	public Map<UserField, Composite> getNonDefaultVehicles() {
		java.util.Iterator<java.util.Map.Entry<UserField, Composite>> iterator = this.nonDefaultVehicles.entrySet().iterator();

		// Filter out vehicle changes for relevance
		while( iterator.hasNext() ) {
			java.util.Map.Entry<UserField, Composite> entry = iterator.next();
			Composite vehicle = entry.getValue();

			if( vehicle instanceof Joint ) {
				vehicle = RemixUtilities.getParentForJoint( vehicle );
			}

			if( vehicle != null ) {
				UserField vehicleField = this.sceneInstance.ACCEPTABLE_HACK_FOR_SCENE_EDITOR_getFieldForInstanceInJava( EntityImp.getAbstractionFromSgElement( vehicle ) );
				UserField newVehicleField = (UserField)this.oldToNewDeclarations.get( vehicleField.getId() );

				// If the vehicle is an active field
				if( getActiveFields().contains( newVehicleField ) ) {
					Integer count = this.paramCount.get( newVehicleField );
					this.paramCount.put( newVehicleField, count == null ? new Integer( 1 ) : new Integer( count + 1 ) );
				}
				// Otherwise, check if ancestor vehicles are active
				else {
					java.util.List<Composite> vehicleChain = vehicle.getVehicleChain();
					int splitIndex = -1;
					for( Composite composite : vehicleChain ) {
						UserField compositeField = this.sceneInstance.ACCEPTABLE_HACK_FOR_SCENE_EDITOR_getFieldForInstanceInJava( EntityImp.getAbstractionFromSgElement( composite ) );

						if( compositeField != null ) {
							UserField newCompositeField = (UserField)this.oldToNewDeclarations.get( compositeField.getId() );
							if( getActiveFields().contains( newCompositeField ) ) {
								splitIndex = vehicleChain.indexOf( composite );
								break;
							}
						}
					}
					// If we found an ancestor, add vehicles to activeFields and nonDefaultVehicles
					if( splitIndex > -1 ) {
						vehicleChain = vehicleChain.subList( 0, splitIndex );
						vehicleChain.add( 0, vehicle );

						for( Composite composite : vehicleChain ) {
							UserField compositeField = this.sceneInstance.ACCEPTABLE_HACK_FOR_SCENE_EDITOR_getFieldForInstanceInJava( EntityImp.getAbstractionFromSgElement( composite ) );

							if( compositeField != null ) {
								UserField newCompositeField = (UserField)this.oldToNewDeclarations.get( compositeField.getId() );

								if( newCompositeField != null ) {
									this.nonDefaultVehicles.put( newCompositeField, composite.getParent() );

									// add the vehicle field if it's not already referenced
									if( this.activeFields.contains( newCompositeField ) ) {
										//pass
									} else {
										this.activeFields.add( newCompositeField );
										this.lowestReferencedType.put( newCompositeField, JavaType.getInstance( SThing.class ) );
									}
									Integer count = this.paramCount.get( newCompositeField );
									this.paramCount.put( newCompositeField, count == null ? new Integer( 1 ) : new Integer( count + 1 ) );
								}
							}
						}
					}
					// If no ancestors found, remove from nonDefaultVehicles
					else {
						iterator.remove();
					}
				}
			}
		}
		return this.nonDefaultVehicles;
	}

	public Map<UserParameter, List<ExpressionEvaluationEventNode>> getReferencedParameters() {
		return this.referencedParams;
	}

	public int getCallerCount( UserField field ) {
		Integer rv = this.callerCount.get( field );
		return rv == null ? 0 : rv;
	}

	public int getParameterCount( UserField field ) {
		Integer rv = this.paramCount.get( field );
		return rv == null ? 0 : rv;
	}

	/**
	 * Finds any referenced locals that were not declared in the selected code
	 * block.
	 *
	 * @return mapping of undeclared locals to the event nodes referencing them
	 */
	public Map<UserLocal, List<ExpressionEvaluationEventNode>> getUndeclaredLocals() {
		Map<UserLocal, List<ExpressionEvaluationEventNode>> rv = Maps.newHashMap();

		for( UserLocal local : this.referencedLocals.keySet() ) {
			if( this.declaredLocals.get( local ) == null ) {
				List<ExpressionEvaluationEventNode> eventNodes = this.referencedLocals.get( local );
				rv.put( local, eventNodes );
			}
		}
		return rv;
	}

	/**
	 * Crawls the {@link AbstractEventNode} tree from the rootNode.
	 *
	 * @param rootNode root node to crawl
	 * @param sceneInstance the scene instance from execution
	 * @param oldToNewDeclarations the mapping of old to new declarations from
	 *            copy
	 */
	public void crawlForReferences( AbstractEventNode<?> rootNode, UserInstance sceneInstance, Map<UUID, AbstractDeclaration> oldToNewDeclarations, Project project ) {
		this.sceneInstance = sceneInstance;
		this.oldToNewDeclarations = oldToNewDeclarations;
		this.project = project;
		visit( rootNode );
	}

	/* ---------------- Crawl methods  -------------- */

	/**
	 * Crawls an individual node, visiting any child nodes and checking the
	 * astNode based on applicability.
	 *
	 * @param eventNode the node to crawl
	 */
	private void visit( AbstractEventNode<?> eventNode ) {

		if( eventNode instanceof ExpressionStatementEventNode ) {
			checkCaller( (ExpressionStatementEventNode)eventNode );

			for( ExpressionEvaluationEventNode expressionEval : ( (ExpressionStatementEventNode)eventNode ).getExpressionEvaluationNodes() ) {
				visit( expressionEval );
			}
			if( ( (ExpressionStatementEventNode)eventNode ).isUserMethod() ) {
				visit( ( (ExpressionStatementEventNode)eventNode ).getUserMethodEventNode() );
			}
		} else if( eventNode instanceof ReturnStatementEventNode ) {
			ExpressionEvaluationEventNode expressionNode = ( (ReturnStatementEventNode)eventNode ).getExpressionNode();

			visit( expressionNode );
		} else if( eventNode instanceof LocalDeclarationStatementEventNode ) {
			ExpressionEvaluationEventNode expressionNode = ( (LocalDeclarationStatementEventNode)eventNode ).getInitializerExpressionNode();
			UserLocal local = ( (LocalDeclarationStatementEventNode)eventNode ).getLocal();

			visit( expressionNode );
			addLocal( local, eventNode ); // check astNode
		} else if( eventNode instanceof EachInArrayTogetherEventNode ) {
			ExpressionEvaluationEventNode arrayExpressionNode = ( (EachInArrayTogetherEventNode)eventNode ).getArrayExpressionNode();
			UserLocal local = ( (EachInArrayTogetherEventNode)eventNode ).getItem();

			visit( arrayExpressionNode );
			addLocal( local, eventNode ); // check astNode
		} else if( eventNode instanceof CountLoopEventNode ) {
			ExpressionEvaluationEventNode countExpressionNode = ( (CountLoopEventNode)eventNode ).getCountExpressionNode();

			visit( countExpressionNode );
		} else if( eventNode instanceof ForEachInArrayLoopEventNode ) {
			ExpressionEvaluationEventNode arrayExpressionNode = ( (ForEachInArrayLoopEventNode)eventNode ).getArrayExpressionNode();
			UserLocal local = ( (ForEachInArrayLoopEventNode)eventNode ).getItem();

			visit( arrayExpressionNode );
			addLocal( local, eventNode ); // check astNode
		} else if( eventNode instanceof WhileLoopEventNode ) {
			for( ExpressionEvaluationEventNode expressionEval : ( (WhileLoopEventNode)eventNode ).getConditionalEvaluations() ) {
				visit( expressionEval );
			}
		} else if( eventNode instanceof ConditionalStatementEventNode ) {
			for( ExpressionEvaluationEventNode expressionEval : ( (ConditionalStatementEventNode)eventNode ).getConditionalEvaluations() ) {
				visit( expressionEval );
			}
			visit( ( (ConditionalStatementEventNode)eventNode ).getBodyEventNode() );
		} else if( eventNode instanceof ExpressionEvaluationEventNode ) {
			checkExpression( ( (ExpressionEvaluationEventNode)eventNode ).getAstNode(), (ExpressionEvaluationEventNode)eventNode ); // check astNode

			for( AbstractEventNode<?> childNode : ( (ExpressionEvaluationEventNode)eventNode ).getChildren() ) {
				visit( childNode );
			}
		}
		// Check container children
		if( eventNode instanceof ContainerEventNode ) {
			for( AbstractEventNode<?> childNode : ( (ContainerEventNode<?>)eventNode ).getChildren() ) {
				visit( childNode );
			}
		}
	}

	/**
	 * Adds a declared <code>UserLocal</code> to the crawler.
	 *
	 * @param local the referenced local
	 * @param eventNode the node containing astNode where referenced
	 */
	private void addLocal( UserLocal local, AbstractEventNode<?> eventNode ) {
		this.declaredLocals.put( local, eventNode );
	}

	/**
	 * Checks expression for references to field, local, and parameter access.
	 *
	 * @param expression the expression to check
	 * @param eventNode the node containing expression astNode
	 */
	private void checkExpression( Expression expression, ExpressionEvaluationEventNode eventNode ) {

		if( expression instanceof FieldAccess ) {
			AbstractField field = ( (FieldAccess)expression ).field.getValue();

			if( field instanceof UserField ) {
				addFieldReference( (UserField)field, expression, eventNode );
			}
		} else if( expression instanceof LocalAccess ) {
			UserLocal local = ( (LocalAccess)expression ).local.getValue();
			addLocalReference( local, eventNode );
			checkLocalAccessReference( (LocalAccess)expression, eventNode );
		} else if( expression instanceof ParameterAccess ) {
			UserParameter param = ( (ParameterAccess)expression ).parameter.getValue();
			addParameterReference( param, eventNode );
			checkParameterAccessReference( (ParameterAccess)expression, eventNode );
		} else if( expression instanceof MethodInvocation ) {
			checkCaller( (MethodInvocation)expression, eventNode );
		} else if( expression instanceof ResourceExpression ) {
			addResourceReference( (ResourceExpression)expression );
		}
	}

	/**
	 * Check the <code>AbstractType</code> referenced for the runtime-evaluated
	 * <code>UserField</code> value of the parameter.
	 *
	 * @param paramAccess the expression to check for reference
	 * @param eventNode the node containing <code>ParameterAccess</code>
	 *            expression
	 */
	private void checkParameterAccessReference( ParameterAccess paramAccess, ExpressionEvaluationEventNode eventNode ) {
		UserField field = eventNode.getValueField();

		if( field != null ) {
			checkReferenceType( field, paramAccess );

			Integer count = this.paramCount.get( field );
			this.paramCount.put( field, count == null ? new Integer( 1 ) : new Integer( count + 1 ) );
		}
	}

	/**
	 * Checks the <code>AbstractType</code> referenced for the runtime-evaluated
	 * <code>UserField</code> value of the local.
	 *
	 * @param localAccess the expression to check for reference
	 * @param eventNode the node containing <code>LocalAccess</code> expression
	 */
	private void checkLocalAccessReference( LocalAccess localAccess, ExpressionEvaluationEventNode eventNode ) {
		UserField field = eventNode.getValueField();

		if( field != null ) {
			checkReferenceType( field, localAccess );
		}
	}

	/**
	 * Checks the caller <code>UserField</code> for a method invocation found
	 * while crawling. If applicable, method and referenced field is added to
	 * <i>fieldsForMethods</i>.
	 * <p>
	 * <b>note:</b> This covers cases beyond basic method invocations (function
	 * calls in expressions).
	 * </p>
	 *
	 * @param invocation the method invocation to check
	 * @param eventNode the node containing <code>MethodInvocation</code>
	 *            expression
	 */
	private void checkCaller( MethodInvocation invocation, ExpressionEvaluationEventNode eventNode ) {
		UserField callerField = eventNode.getValueField();
		AbstractMethod method = invocation.method.getValue();

		if( shouldRecordMethod( method ) ) {
			if( callerField != null ) {
				java.util.Set<UserField> callerFields = this.fieldsForMethods.get( method );

				if( callerFields == null ) {
					callerFields = Sets.newHashSet();
					this.fieldsForMethods.put( method, callerFields );
				}
				callerFields.add( callerField );
			}
		}
	}

	/**
	 * Checks the caller <code>UserField</code> for a method invocation
	 * <code>ExpressionStatementEventNode</code>.
	 *
	 * @param eventNode the node to check caller field for
	 */
	private void checkCaller( ExpressionStatementEventNode eventNode ) {
		ExpressionStatement statement = eventNode.getAstNode();
		UserField callerField = eventNode.getCallerField();

		if( statement.expression.getValue() instanceof MethodInvocation ) {
			MethodInvocation invocation = (MethodInvocation)statement.expression.getValue();
			AbstractMethod method = invocation.method.getValue();

			checkArguments( eventNode );

			if( shouldRecordMethod( method ) ) {

				java.util.Set<UserField> callerFields = this.fieldsForMethods.get( method );

				if( callerFields == null ) {
					callerFields = Sets.newHashSet();
					this.fieldsForMethods.put( method, callerFields );
				}
				callerFields.add( callerField );
			}
			Integer count = this.callerCount.get( callerField );
			this.callerCount.put( callerField, count == null ? new Integer( 1 ) : new Integer( count + 1 ) );
		}
	}

	/**
	 * Checks the children of an <code>ExpressionStatementEventNode</code> for
	 * <code>SimpleArgument</code> astNodes.
	 *
	 * @param eventNode the node to check for arguments
	 */
	private void checkArguments( ExpressionStatementEventNode eventNode ) {

		for( ExpressionEvaluationEventNode childNode : eventNode.getExpressionEvaluationNodes() ) {
			if( childNode.getAstNode().getParent() instanceof SimpleArgument ) {
				if( childNode.getValueField() != null ) {
					UserField argField = childNode.getValueField();
					Integer count = this.paramCount.get( argField );
					this.paramCount.put( argField, count == null ? new Integer( 1 ) : new Integer( count + 1 ) );
				}
			}
		}
	}

	/**
	 * Adds a referenced <code>UserParameter</code> to the crawler.
	 *
	 * @param param the parameter referenced
	 * @param eventNode the node where parameter is referenced
	 */
	private void addParameterReference( UserParameter param, AbstractEventNode<?> eventNode ) {
		List<ExpressionEvaluationEventNode> eventNodes = this.referencedParams.get( param );

		if( eventNodes == null ) {
			eventNodes = Lists.newArrayList();
			this.referencedParams.put( param, eventNodes );
		}
		if( eventNodes.contains( eventNode ) ) {
			//pass
		} else {
			eventNodes.add( (ExpressionEvaluationEventNode)eventNode );
		}
	}

	/**
	 * Adds a referenced <code>UserLocal</code> to the crawler.
	 *
	 * @param local the local referenced
	 * @param eventNode the node where local is referenced
	 */
	private void addLocalReference( UserLocal local, AbstractEventNode<?> eventNode ) {
		List<ExpressionEvaluationEventNode> eventNodes = this.referencedLocals.get( local );

		if( eventNodes == null ) {
			eventNodes = Lists.newArrayList();
			this.referencedLocals.put( local, eventNodes );
		}
		if( eventNodes.contains( eventNode ) ) {
			//pass
		} else {
			eventNodes.add( (ExpressionEvaluationEventNode)eventNode );
		}
	}

	/**
	 * Adds a referenced <code>UserField</code> to the crawler. Additionally,
	 * checks the referenced <code>AbstractType</code> of the field in its
	 * parent and also checks for previous vehicle changes.
	 */
	public void addFieldReference( UserField field, Expression expression, ExpressionEvaluationEventNode eventNode ) {

		// Special fields are added at a later time
		if( RemixUtilities.isActiveField( field ) && !( RemixUtilities.isSpecialField( field ) ) ) {
			this.activeFields.add( field );
		}

		checkReferenceType( field, expression );
		checkForVehicleChanges( field, eventNode );
	}

	public void addResourceReference( ResourceExpression expression ) {
		requiredResources.add( expression.resource.getValue() );
	}

	/**
	 * Get the vehicle for the field prior to the eventNode time. If the vehicle
	 * is something other than the scene, we add the appropriate references.
	 *
	 * @param field the field to check for vehicle changes
	 * @param eventNode eventNode referencing field
	 */
	private void checkForVehicleChanges( UserField field, ExpressionEvaluationEventNode eventNode ) {
		Object fieldInstance = eventNode.getValue();
		Composite vehicle = RemixUtilities.getEarlierVehicleChange( field, fieldInstance, this.startTime );

		if( ( vehicle == null ) || RemixUtilities.isScene( JavaType.getInstance( vehicle.getClass() ) ) ) {
			// pass
		} else {
			this.nonDefaultVehicles.put( field, vehicle );
		}
	}

	/**
	 * Gets the <code>AbstractType</code> required by the field in its parent
	 * context.
	 *
	 * @param field the field to check for referenced type
	 * @param expression expression containing field reference
	 */
	private void checkReferenceType( UserField field, Expression expression ) {
		AbstractType<?, ?, ?> type = null;
		if( expression.getParent() instanceof MethodInvocation ) {
			MethodInvocation mi = (MethodInvocation)expression.getParent();

			if( JointMethodUtilities.isJointGetter( mi.method.getValue() ) ) {
				type = JavaType.getInstance( SThing.class );
			} else {
				type = mi.method.getValue().getDeclaringType().getFirstEncounteredJavaType(); // always get javaType
			}
		} else if( expression.getParent() instanceof SimpleArgument ) {
			SimpleArgument arg = (SimpleArgument)expression.getParent();
			type = arg.parameter.getValue().getValueType().getFirstEncounteredJavaType();
		}

		if( type != null ) {
			if( this.lowestReferencedType.containsKey( field ) ) {
				AbstractType<?, ?, ?> oldType = this.lowestReferencedType.get( field );
				if( oldType.isAssignableFrom( type ) ) {
					this.lowestReferencedType.put( field, type );
				}
			} else {
				this.lowestReferencedType.put( field, type );
			}
		}
	}

	/* ---------------- Helper methods  -------------- */

	private boolean shouldRecordMethod( AbstractMethod method ) {
		boolean isUserAuthored = RemixUtilities.isMethodUserAuthoredMethod( method, this.project.getProgramType() );
		boolean isJointGetter = JointMethodUtilities.isJointGetter( method );

		// We record methods that are user authored, joint getters, or declared by the ground or room (needed during remixing)
		if( method.getDeclaringType() != null ) {
			return isUserAuthored || isJointGetter || NebulousIde.nonfree.isAssignableToSRoom( method.getDeclaringType() ) || method.getDeclaringType().isAssignableTo( SGround.class );
		} else {
			return isUserAuthored || isJointGetter;
		}
	}
}
