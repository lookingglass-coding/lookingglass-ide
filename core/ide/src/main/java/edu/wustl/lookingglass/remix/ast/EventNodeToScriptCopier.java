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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alice.ide.ast.ExpressionCreator.CannotCreateExpressionException;
import org.alice.nonfree.NebulousIde;
import org.alice.stageide.ast.ExpressionCreator;
import org.alice.stageide.ast.StoryApiSpecificAstUtilities;
import org.lgna.project.Project;
import org.lgna.project.ast.AbstractArgument;
import org.lgna.project.ast.AbstractConstructor;
import org.lgna.project.ast.AbstractDeclaration;
import org.lgna.project.ast.AbstractField;
import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractNode;
import org.lgna.project.ast.AbstractTransient;
import org.lgna.project.ast.AbstractType;
import org.lgna.project.ast.ArrayAccess;
import org.lgna.project.ast.ArrayInstanceCreation;
import org.lgna.project.ast.AstUtilities;
import org.lgna.project.ast.BlockStatement;
import org.lgna.project.ast.CrawlPolicy;
import org.lgna.project.ast.Declaration;
import org.lgna.project.ast.EachInArrayTogether;
import org.lgna.project.ast.Expression;
import org.lgna.project.ast.ExpressionStatement;
import org.lgna.project.ast.FieldAccess;
import org.lgna.project.ast.ForEachInArrayLoop;
import org.lgna.project.ast.GlobalFirstInstanceExpression;
import org.lgna.project.ast.JavaType;
import org.lgna.project.ast.LocalAccess;
import org.lgna.project.ast.LocalDeclarationStatement;
import org.lgna.project.ast.MethodInvocation;
import org.lgna.project.ast.NamedUserConstructor;
import org.lgna.project.ast.NamedUserType;
import org.lgna.project.ast.Node;
import org.lgna.project.ast.ParameterAccess;
import org.lgna.project.ast.ReturnStatement;
import org.lgna.project.ast.SimpleArgument;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.ThisExpression;
import org.lgna.project.ast.UserField;
import org.lgna.project.ast.UserLocal;
import org.lgna.project.ast.UserMethod;
import org.lgna.project.ast.UserPackage;
import org.lgna.project.ast.UserParameter;
import org.lgna.project.ast.UserType;
import org.lgna.project.ast.crawlers.NodePropertyValueIsInstanceCrawler;
import org.lgna.project.virtualmachine.UserArrayInstance;
import org.lgna.project.virtualmachine.UserInstance;
import org.lgna.story.EmployeesOnly;
import org.lgna.story.SJoint;
import org.lgna.story.SThing;
import org.lgna.story.implementation.JointImp;
import org.lgna.story.implementation.JointedModelImp;
import org.lgna.story.implementation.alice.AliceResourceClassUtilities;
import org.lgna.story.resources.BipedResource;

import edu.cmu.cs.dennisc.java.lang.ArrayUtilities;
import edu.cmu.cs.dennisc.java.util.Lists;
import edu.cmu.cs.dennisc.java.util.Maps;
import edu.cmu.cs.dennisc.java.util.Sets;
import edu.cmu.cs.dennisc.pattern.Crawlable;
import edu.cmu.cs.dennisc.pattern.Crawler;
import edu.cmu.cs.dennisc.pattern.Criterion;
import edu.cmu.cs.dennisc.pattern.IsInstanceCrawler;
import edu.cmu.cs.dennisc.property.InstanceProperty;
import edu.cmu.cs.dennisc.scenegraph.Composite;
import edu.wustl.lookingglass.remix.SnippetScript;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ContainerEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EachInArrayTogetherEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EventNodeFactory;
import edu.wustl.lookingglass.virtualmachine.eventtracing.EventNodeUtilities;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ExpressionEvaluationEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ForEachInArrayLoopEventNode;

/**
 * An <code>ASTCopier</code> subclass that takes a selection of the
 * {@link AbstractEventNode} tree, copies the contained ast, and provides
 * additional functionality used to modify the ast while creating a
 * {@link SnippetScript}.
 *
 * @author Michael Pogran
 */
public class EventNodeToScriptCopier extends ASTCopier {

	private NamedUserType scriptType;
	private ContainerEventNode<BlockStatement> rootEventNode;
	private final Map<AbstractDeclaration, AbstractDeclaration> newToOldDeclarations = Maps.newHashMap(); // Note: not UUID because ID isn't initially set
	private final Map<java.util.UUID, AbstractNode> oldToNewNodeMap = Maps.newHashMap();
	private UserMethod mainMethod;
	private boolean selectionInUserMethod = false;

	private ExpressionCreator expressionCreator = new ExpressionCreator(); // Creates substitute expressions for Object values
	private RemixReferencesCrawler crawler;

	/**
	 * Creates an instance of <code>EventNodeToScriptCopier</code> by copying
	 * the selected portion of the event node tree and creating the scriptType
	 * for the <code>SnippetScript</code>.
	 *
	 * @param crawler
	 *
	 * @param startEventNode beginning boundary for snippet
	 * @param endEventNode end boundary for snippet
	 */
	public EventNodeToScriptCopier( Project project, RemixReferencesCrawler crawler, AbstractEventNode<?> startEventNode, AbstractEventNode<?> endEventNode ) {
		super( project, true );
		this.crawler = crawler;

		this.scriptType = new NamedUserType(
				RemixUtilities.SCRIPT_TYPE_NAME,
				new UserPackage( RemixUtilities.SCRIPT_PACKAGE_NAME ),
				JavaType.getInstance( Object.class ),
				new NamedUserConstructor[ 0 ],
				new UserMethod[ 0 ],
				new UserField[ 0 ] );

		this.rootEventNode = getRootEventNode( startEventNode, endEventNode );
		this.mainMethod = new UserMethod( RemixUtilities.SCRIPT_METHOD_NAME, JavaType.VOID_TYPE, new UserParameter[ 0 ], this.rootEventNode.getAstNode() );
		this.scriptType.methods.add( this.mainMethod );
	}

	public ContainerEventNode<BlockStatement> getRootEventNode() {
		return this.rootEventNode;
	}

	public NamedUserType getScriptType() {
		return this.scriptType;
	}

	public UserField getOldFieldForNew( UserField newField ) {
		UserField rv = (UserField)this.newToOldDeclarations.get( newField );
		if( rv == null ) {
			return newField;
		} else {
			return rv;
		}
	}

	public Map<AbstractDeclaration, AbstractDeclaration> getNewToOldDeclarations() {
		return this.newToOldDeclarations;
	}

	public AbstractDeclaration getOrCreateNewDeclaration( AbstractDeclaration oldDeclaration ) {
		AbstractDeclaration rv = getNewDeclaration( oldDeclaration );

		if( rv == null ) {
			rv = (AbstractDeclaration)copyAbstractNode( oldDeclaration );
		}
		return rv;
	}

	private ExpressionCreator getExpressionCreator() {
		return this.expressionCreator;
	}

	/**
	 * Builds a new container node to hold all event nodes between provided
	 * boundaries. To preserve original event node tree and corresponding ast
	 * statements, a deep-copy is performed on each node before insertion.
	 *
	 * @param startEventNode beginning boundary node
	 * @param endEventNode end boundary node
	 * @return container node with bounded event node tree selection
	 */
	private ContainerEventNode<BlockStatement> getRootEventNode( AbstractEventNode<?> startEventNode, AbstractEventNode<?> endEventNode ) {
		ContainerEventNode<BlockStatement> rootEventNode = null;
		List<AbstractEventNode<?>> nodesToAdd = Lists.newArrayList();

		if( startEventNode.equals( endEventNode ) ) {
			rootEventNode = EventNodeFactory.createEmptyContainer( startEventNode.getThread(), startEventNode.getStartTime(), startEventNode.getEndTime() );
			this.selectionInUserMethod = EventNodeUtilities.isInUserMethod( startEventNode );

			// Add nodes to add
			nodesToAdd.add( EventNodeFactory.copyEventNode( startEventNode, this ) );
		} else {
			ContainerEventNode<?> sharedParentNode = (ContainerEventNode<?>)EventNodeUtilities.getSharedParentNode( startEventNode, endEventNode );
			rootEventNode = EventNodeFactory.createEmptyContainer( sharedParentNode.getThread(), startEventNode.getStartTime(), endEventNode.getEndTime() );
			this.selectionInUserMethod = EventNodeUtilities.isInUserMethod( sharedParentNode );

			int startIndex = EventNodeUtilities.getIndexInAncestor( (ContainerEventNode<?>)sharedParentNode, startEventNode );
			int endIndex = EventNodeUtilities.getIndexInAncestor( (ContainerEventNode<?>)sharedParentNode, endEventNode );

			// Special case for selecting start node on each in array together, end node within construct
			if( sharedParentNode instanceof EachInArrayTogetherEventNode ) {
				if( startEventNode.equals( sharedParentNode ) ) {
					startIndex = endIndex;
				}
			}

			// Add nodes to add
			for( AbstractEventNode<?> childEventNode : sharedParentNode.getChildren() ) {
				int index = sharedParentNode.getChildren().indexOf( childEventNode );
				if( ( startIndex <= index ) && ( endIndex >= index ) ) {

					// Special case for when selecting statements within an iterating type, want children to be different when copied
					if( EventNodeUtilities.isEventNodeIteratingType( sharedParentNode ) ) {
						this.setShouldPreserveIds( false );

						AbstractEventNode<?> childCopy = EventNodeFactory.copyEventNode( childEventNode, this );
						if( childCopy instanceof ContainerEventNode ) {
							for( AbstractEventNode<?> containerChild : ( (ContainerEventNode<?>)childCopy ).getChildren() ) {
								nodesToAdd.add( containerChild );
							}
						} else {
							nodesToAdd.add( childCopy );
						}
						this.oldToNewNodeMap.clear(); // Clear map because we want new ast nodes (see above comment)
					} else {
						nodesToAdd.add( EventNodeFactory.copyEventNode( childEventNode, this ) );
					}
				}
			}
		}

		for( AbstractEventNode<?> eventNode : nodesToAdd ) {
			rootEventNode.addChild( eventNode );
			BlockStatement block = rootEventNode.getAstNode();

			if( eventNode instanceof ForEachInArrayLoopEventNode ) {
				copyForEachInArrayLoopEventNode( (ForEachInArrayLoopEventNode)eventNode ); // special case for loops, check for num of iterations
			}

			if( eventNode.getAstNode() instanceof Statement ) {
				block.statements.add( (Statement)eventNode.getAstNode() );
			}
		}

		if( this.selectionInUserMethod ) {
			UserField callerField = EventNodeUtilities.findUserMethodCaller( startEventNode );
			resolveThisExpressions( rootEventNode, callerField );
		}

		return rootEventNode;
	}

	/**
	 * Resolves this expressions for selection made within a UserMethod not on
	 * the sceneType.
	 */
	@SuppressWarnings( "unchecked" )
	public void resolveThisExpressions( AbstractEventNode<?> eventNode, UserField callerField ) {
		if( callerField.getValueType().isAssignableTo( org.lgna.story.SScene.class ) ) {
			//pass
		} else {
			UserField oldScene = StoryApiSpecificAstUtilities.getSceneFieldFromProgramType( getProject().getProgramType() );

			UserField newField = (UserField)getOrCreateNewDeclaration( callerField );
			UserField newScene = (UserField)getOrCreateNewDeclaration( oldScene );
			GlobalFirstInstanceExpression globalFirstInstanceExpression = new GlobalFirstInstanceExpression( this.scriptType );

			FieldAccess sceneAccess = AstUtilities.createFieldAccess( globalFirstInstanceExpression, newScene );
			FieldAccess fieldAccess = AstUtilities.createFieldAccess( sceneAccess, newField );

			NodePropertyValueIsInstanceCrawler<ThisExpression> thisExpressionCrawler = NodePropertyValueIsInstanceCrawler.createInstance( ThisExpression.class );

			eventNode.getAstNode().crawl( thisExpressionCrawler, CrawlPolicy.EXCLUDE_REFERENCES_ENTIRELY );

			for( InstanceProperty property : thisExpressionCrawler.getList() ) {
				ThisExpression expression = (ThisExpression)property.getValue();
				property.setValue( fieldAccess );

				AbstractEventNode<?> statementEventNode = EventNodeUtilities.findChildWithAstNode( eventNode, expression );

				if( statementEventNode != null ) {
					this.crawler.addFieldReference( newField, fieldAccess, (ExpressionEvaluationEventNode)statementEventNode ); // Add new field reference
				}
			}
		}
	}

	/**
	 * Creates and inserts a replacement expression for dangling
	 * <code>UserParameter</code> references. These dangling references occur
	 * when the snippet selection is made within a user method.
	 */
	public void resolveReferencedParameters( UserInstance sceneInstance ) {
		if( this.selectionInUserMethod ) {
			Map<UserParameter, List<ExpressionEvaluationEventNode>> referencedParams = this.crawler.getReferencedParameters();

			for( Map.Entry<UserParameter, List<ExpressionEvaluationEventNode>> entry : referencedParams.entrySet() ) {
				UserParameter param = entry.getKey();
				Expression expression = getTransientExpressionReplacement( entry.getValue().get( 0 ), sceneInstance );

				NodePropertyValueIsInstanceCrawler<ParameterAccess> propertyCrawler = NodePropertyValueIsInstanceCrawler.createInstance( ParameterAccess.class );
				this.mainMethod.crawl( propertyCrawler, CrawlPolicy.INCLUDE_REFERENCES_BUT_DO_NOT_TUNNEL );

				for( InstanceProperty property : propertyCrawler.getList() ) {
					if( property.getValue() instanceof ParameterAccess ) {
						UserParameter valueParam = (UserParameter)( (ParameterAccess)property.getValue() ).parameter.getValue();

						if( valueParam.getId().equals( param.getId() ) ) {
							property.setValue( expression );
						}
					}
				}
			}
		}
	}

	/**
	 * Modifies the <code>AbstractType</code> for referenced transients (locals
	 * and parameter), reducing their type as far as possible. Additionally,
	 * appropriately modifies declarations for transients and moves any methods
	 * declared on sims types (myAdultPerson, etc.) to the Biped type.
	 */
	public void pullDownTypes() {
		PullDownTypesFilter filter = new PullDownTypesFilter(); // filter allows for complete crawl that does NOT tunnel into types

		// Crawl for referenced locals in main snippet method
		TransientTypeCrawler<UserLocal> localCrawler = new TransientTypeCrawler<UserLocal>() {
			@Override
			public void visit( Crawlable crawlable ) {
				if( crawlable instanceof LocalAccess ) {
					UserLocal local = ( (LocalAccess)crawlable ).local.getValue();
					checkReferenceType( local, (LocalAccess)crawlable );
				}
			}
		};
		this.mainMethod.crawl( localCrawler, CrawlPolicy.COMPLETE, filter );

		for( UserLocal local : localCrawler.getReferencedLocals() ) {
			AbstractType<?, ?, ?> referencedType = localCrawler.getLowestReferencedType( local );
			AbstractType<?, ?, ?> declaredType = local.getValueType();

			if( declaredType.isArray() ) {
				referencedType = referencedType.getArrayType();
			}

			// Modify type of local if type can be lowered
			if( referencedType.equals( declaredType ) ) {
				// pass
			} else {
				Node parent = local.getParent();

				if( parent instanceof EachInArrayTogether ) {
					EachInArrayTogether arrayTogether = (EachInArrayTogether)parent;
					Expression arrayExpression = arrayTogether.array.getValue();
					local.valueType.setValue( referencedType );

					if( arrayExpression instanceof ArrayInstanceCreation ) {
						( (ArrayInstanceCreation)arrayExpression ).arrayType.setValue( referencedType.getArrayType() );
					} else if( arrayExpression instanceof MethodInvocation ) {
						AbstractMethod method = ( (MethodInvocation)arrayExpression ).method.getValue();

						if( RemixUtilities.isMethodUserAuthoredMethod( method, this.getProject().getProgramType() ) ) {
							( (UserMethod)method ).returnType.setValue( referencedType );
						}
					}
				} else if( parent instanceof ForEachInArrayLoop ) {
					ForEachInArrayLoop arrayLoop = (ForEachInArrayLoop)parent;
					Expression arrayExpression = arrayLoop.array.getValue();
					local.valueType.setValue( referencedType );

					if( arrayExpression instanceof ArrayInstanceCreation ) {
						( (ArrayInstanceCreation)arrayExpression ).arrayType.setValue( referencedType.getArrayType() );
					} else if( arrayExpression instanceof MethodInvocation ) {
						AbstractMethod method = ( (MethodInvocation)arrayExpression ).method.getValue();

						if( RemixUtilities.isMethodUserAuthoredMethod( method, this.getProject().getProgramType() ) ) {
							( (UserMethod)method ).returnType.setValue( referencedType );
						}
					}
				} else if( parent instanceof LocalDeclarationStatement ) {
					LocalDeclarationStatement declaration = (LocalDeclarationStatement)parent;
					local.valueType.setValue( referencedType );

					Expression expression = declaration.initializer.getValue();
					if( expression instanceof MethodInvocation ) {
						AbstractMethod method = ( (MethodInvocation)expression ).method.getValue();

						if( RemixUtilities.isMethodUserAuthoredMethod( method, this.getProject().getProgramType() ) ) {
							( (UserMethod)method ).returnType.setValue( referencedType );
						}
					} else if( expression instanceof ArrayInstanceCreation ) {
						( (ArrayInstanceCreation)expression ).arrayType.setValue( referencedType );
					}

				}
			}
		}

		// Crawl for referenced parameters in main snippet method
		TransientTypeCrawler<UserParameter> paramCrawler = new TransientTypeCrawler<UserParameter>() {
			@Override
			public void visit( Crawlable crawlable ) {
				if( crawlable instanceof ParameterAccess ) {
					UserParameter param = ( (ParameterAccess)crawlable ).parameter.getValue();
					checkReferenceType( param, (ParameterAccess)crawlable );
				}
			}
		};
		this.mainMethod.crawl( paramCrawler, CrawlPolicy.COMPLETE, filter );

		for( final UserParameter param : paramCrawler.getReferencedLocals() ) {
			// Don't muck with constructor parameters
			if( param.getParent() instanceof AbstractConstructor ) {
				continue;
			}

			AbstractType<?, ?, ?> referencedType = paramCrawler.getLowestReferencedType( param );
			AbstractType<?, ?, ?> declaredType = param.getValueType();

			if( declaredType.isArray() ) {
				referencedType = referencedType.getArrayType();
			}

			// Modify type of local if type can be lowered
			if( referencedType.equals( declaredType ) ) {
				// pass
			} else {
				param.valueType.setValue( referencedType );
			}

			// If the argument for this parameter is a method invocation (a function) lower the return type of the function
			IsInstanceCrawler<MethodInvocation> invocationCrawler = new IsInstanceCrawler<MethodInvocation>( MethodInvocation.class) {
				@Override
				protected boolean isAcceptable( MethodInvocation invocation ) {
					return invocation.method.getValue().equals( param.getParent() );
				}
			};

			this.mainMethod.crawl( invocationCrawler, CrawlPolicy.COMPLETE, filter );

			for( MethodInvocation invocation : invocationCrawler.getList() ) {

				for( SimpleArgument argument : invocation.requiredArguments ) {
					if( argument.parameter.getValue().equals( param ) ) {
						Expression expression = argument.expression.getValue();

						if( expression instanceof MethodInvocation ) {
							AbstractMethod method = ( (MethodInvocation)expression ).method.getValue();

							if( RemixUtilities.isMethodUserAuthoredMethod( method, this.getProject().getProgramType() ) ) {
								( (UserMethod)method ).returnType.setValue( referencedType );
							}
						}
					}
				}
			}
		}

		// Move methods declared on sims types to biped type
		Collection<NamedUserType> projectTypes = AstUtilities.getNamedUserTypes( this.getProject().getProgramType() );
		Set<UserMethod> methods = Sets.newHashSet();

		NamedUserType bipedType = null;
		for( NamedUserType type : projectTypes ) {
			NamedUserConstructor constructor = type.constructors.get( 0 );

			if( ( constructor != null ) && ( constructor.requiredParameters.size() == 1 ) ) {
				if( NebulousIde.nonfree.isAssignableToPersonResource( constructor.requiredParameters.get( 0 ).valueType.getValue() ) ) {
					methods.addAll( type.getDeclaredMethods() );
				}

				if( type.getSuperType().equals( JavaType.getInstance( org.lgna.story.SBiped.class ) ) ) {
					bipedType = type;
				}
			}
		}

		if( methods.isEmpty() ) {
			//pass
		} else {
			if( bipedType != null ) {
				for( UserMethod method : methods ) {
					UserType<?> type = method.getDeclaringType();
					type.methods.remove( type.methods.indexOf( method ) );

					bipedType.methods.add( method );
				}
			}
		}
	}

	/**
	 * Creates a replacement expression for any locals referenced in the snippet
	 * but declared outside the selected code. The replacement expression is
	 * created based on the evaluated value of the local during runtime.
	 */
	public void resolveUndeclaredLocals( UserInstance sceneInstance ) {
		Map<UserLocal, List<ExpressionEvaluationEventNode>> undeclaredLocals = this.crawler.getUndeclaredLocals();

		for( Map.Entry<UserLocal, List<ExpressionEvaluationEventNode>> entry : undeclaredLocals.entrySet() ) {
			List<ExpressionEvaluationEventNode> eventNodes = entry.getValue();

			UserLocal oldLocal = (UserLocal)this.newToOldDeclarations.get( entry.getKey() ); // Get old local (parent of copied ast node could be outside of snippet and therefore not copied)

			if( oldLocal.getParent() instanceof LocalDeclarationStatement ) {
				modifyExpressionValuesForLocal( eventNodes, sceneInstance );
			} else if( ( oldLocal.getParent() instanceof EachInArrayTogether ) || ( oldLocal.getParent() instanceof ForEachInArrayLoop ) ) {
				modifyExpressionValuesForArray( eventNodes, sceneInstance );
			}
		}
	}

	/**
	 * Modifies the expression of each local reference based on the appropriate
	 * runtime value.
	 */
	private void modifyExpressionValuesForLocal( java.util.List<ExpressionEvaluationEventNode> eventNodes, UserInstance sceneInstance ) {

		NodePropertyValueIsInstanceCrawler<LocalAccess> propertyCrawler = NodePropertyValueIsInstanceCrawler.createInstance( LocalAccess.class );
		this.mainMethod.crawl( propertyCrawler, CrawlPolicy.INCLUDE_REFERENCES_BUT_DO_NOT_TUNNEL );

		java.util.List<Expression> expressions = Lists.newArrayList();
		for( ExpressionEvaluationEventNode eventNode : eventNodes ) {

			// If the reference is a local access, substitute the new expression
			if( eventNode.getAstNode() instanceof LocalAccess ) {
				Expression expression = getTransientExpressionReplacement( eventNode, sceneInstance );
				if( expression != null ) {
					expressions.add( expression );
				}

				for( InstanceProperty property : propertyCrawler.getList() ) {
					if( property.getValue() instanceof LocalAccess ) {
						LocalAccess localAccess = (LocalAccess)property.getValue();

						if( localAccess.getId().equals( eventNode.getAstNode().getId() ) ) {
							property.setValue( expression );
						}
					}
				}
			}
			// This is an AssignmentExpression, which we remove
			else {
				AbstractNode node = eventNode.getAstNode();
				Statement statement = node.getFirstAncestorAssignableTo( Statement.class );
				if( statement != null ) {
					BlockStatement block = statement.getFirstAncestorAssignableTo( BlockStatement.class );

					if( block != null ) {
						int index = block.statements.indexOf( statement );
						if( index > -1 ) {
							block.statements.remove( index );
						}
					}
				}
			}
		}
	}

	/**
	 * Modifies the expression of each local reference based on the appropriate
	 * runtime value.
	 */
	private void modifyExpressionValuesForArray( List<ExpressionEvaluationEventNode> eventNodes, UserInstance sceneInstance ) {

		List<Expression> expressions = Lists.newArrayList();
		for( ExpressionEvaluationEventNode eventNode : eventNodes ) {

			Expression expression = getTransientExpressionReplacement( eventNode, sceneInstance );
			expressions.add( expression );
		}
		NodePropertyValueIsInstanceCrawler<LocalAccess> propertyCrawler = NodePropertyValueIsInstanceCrawler.createInstance( LocalAccess.class );
		this.mainMethod.crawl( propertyCrawler, CrawlPolicy.INCLUDE_REFERENCES_BUT_DO_NOT_TUNNEL );

		// set values
		if( ( propertyCrawler.getList().size() == expressions.size() ) && ( propertyCrawler.getList().size() == eventNodes.size() ) ) {
			for( int i = 0; i < propertyCrawler.getList().size(); i++ ) {
				InstanceProperty property = propertyCrawler.getList().get( i );
				ExpressionEvaluationEventNode eventNode = eventNodes.get( i );

				if( property.getValue() instanceof LocalAccess ) {
					LocalAccess localAccess = (LocalAccess)property.getValue();

					if( localAccess.isEquivalentTo( eventNode.getAstNode() ) ) {
						property.setValue( expressions.get( i ) );
					}
				}
			}
		}
	}

	/**
	 * Based on runtime-evaluated value of transient, determines what the
	 * appropriate replacement expression should be.
	 *
	 * @param eventNode the node containing the local reference
	 * @param sceneInstance the program scene UserInstance
	 * @return expression for substitution
	 */
	private Expression getTransientExpressionReplacement( ExpressionEvaluationEventNode eventNode, UserInstance sceneInstance ) {
		Object value = eventNode.getValue();

		// Array of UserInstances (need to create new FieldAccess expressions)
		if( value instanceof UserArrayInstance ) {
			UserArrayInstance arrayInstance = (UserArrayInstance)value;
			List<Expression> expressions = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();

			for( Object object : arrayInstance.getJavaArray() ) {
				if( object instanceof UserInstance ) {
					object = ( (UserInstance)object ).getJavaInstance();
				}
				Expression expression = createFieldAccessExpression( object, eventNode, sceneInstance );
				expressions.add( expression );
			}
			return AstUtilities.createArrayInstanceCreation( arrayInstance.getType(), expressions );
		}
		// Array of joints (need to create new joint Getter expressions)
		else if( ( value.getClass().getComponentType() != null ) && SThing.class.isAssignableFrom( value.getClass().getComponentType() ) ) {
			Object[] values = (Object[])value;
			List<Expression> expressions = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();

			for( Object object : values ) {
				Expression expression = createFieldAccessExpression( object, eventNode, sceneInstance );
				expressions.add( expression );
			}

			return AstUtilities.createArrayInstanceCreation( value.getClass(), expressions );
		}
		// Single UserInstance (need to create new Field Access)
		else if( value instanceof UserInstance ) {
			value = ( (UserInstance)value ).getJavaInstance();
			return createFieldAccessExpression( value, eventNode, sceneInstance );
		}
		// Single joint (need to create new joint Getter)
		else if( value instanceof SThing ) {
			return createFieldAccessExpression( value, eventNode, sceneInstance );
		}
		// Otherwise, the expression creator can handle it
		else {
			try {
				Expression expression = getExpressionCreator().createExpression( value );
				return expression;
			} catch( CannotCreateExpressionException e ) {
				//pass
			}
		}
		return null;
	}

	//<lg>
	private static UserMethod getPartAccessorMethod( Field partField ) {
		String methodName = "get" + AliceResourceClassUtilities.getAliceMethodNameForEnum( partField.getName() );
		Class<?> returnClass = org.lgna.story.SJoint.class;
		UserParameter[] parameters = {};
		org.lgna.project.ast.JavaType jointIdType = org.lgna.project.ast.JavaType.getInstance( org.lgna.story.resources.JointId.class );
		org.lgna.project.ast.TypeExpression typeExpression = new org.lgna.project.ast.TypeExpression( org.lgna.story.SJoint.class );
		Class<?>[] methodParameterClasses = { org.lgna.story.SJointedModel.class, org.lgna.story.resources.JointId.class };
		org.lgna.project.ast.JavaMethod methodExpression = org.lgna.project.ast.JavaMethod.getInstance( org.lgna.story.SJoint.class, "getJoint", methodParameterClasses );

		org.lgna.project.ast.SimpleArgument thisArgument = new org.lgna.project.ast.SimpleArgument( methodExpression.getRequiredParameters().get( 0 ), new org.lgna.project.ast.ThisExpression() );

		org.lgna.project.ast.FieldAccess jointField = new org.lgna.project.ast.FieldAccess(
				new org.lgna.project.ast.TypeExpression( jointIdType ),
				org.lgna.project.ast.JavaField.getInstance( partField.getDeclaringClass(), partField.getName() ) );

		org.lgna.project.ast.SimpleArgument jointArgument = new org.lgna.project.ast.SimpleArgument( methodExpression.getRequiredParameters().get( 1 ), jointField );

		org.lgna.project.ast.SimpleArgument[] methodArguments = { thisArgument, jointArgument };
		org.lgna.project.ast.MethodInvocation getJointMethodInvocation = new org.lgna.project.ast.MethodInvocation( typeExpression, methodExpression, methodArguments );
		ReturnStatement returnStatement = new ReturnStatement( jointIdType, getJointMethodInvocation );
		UserMethod newMethod = new UserMethod( methodName, returnClass, parameters, new BlockStatement( returnStatement ) );
		return newMethod;
	}

	//</lg>

	/**
	 * Creates a new <code>FieldAccess</code> expression for a given run-time
	 * evaluated value.
	 *
	 * @param value the object expression evaluated to
	 * @param eventNode the eventNode containing the expression evaluation
	 * @param sceneInstance the program scene UserInstance
	 * @param crawler the crawler to add new field reference to
	 * @param expression for substitution
	 */
	private Expression createFieldAccessExpression( Object value, ExpressionEvaluationEventNode eventNode, UserInstance sceneInstance ) {

		// If this is a joint, we need to get the Getter for the joint and the caller field
		if( value instanceof SJoint ) {
			SJoint joint = (SJoint)value;

			JointImp jointImp = EmployeesOnly.getImplementation( joint );
			JointedModelImp<?, ?> jointedModelImp = jointImp.getJointedModelParent();
			Class<?> resourceClass = jointedModelImp.getResource().getClass();
			Class<?>[] interfaces = resourceClass.getInterfaces();
			List<java.lang.reflect.Field> allJoints = Lists.newArrayList();

			// Through reflection, we get the all the joint fields for the resource
			if( NebulousIde.nonfree.isPersonResourceAssignableFrom( resourceClass ) ) {
				java.lang.reflect.Field[] jointFields = AliceResourceClassUtilities.getFieldsOfType( BipedResource.class, org.lgna.story.resources.JointId.class );
				for( java.lang.reflect.Field field : jointFields ) {
					allJoints.add( field );
				}
			}

			java.lang.reflect.Field[] jointFields = AliceResourceClassUtilities.getFieldsOfType( resourceClass, org.lgna.story.resources.JointId.class );
			for( java.lang.reflect.Field field : jointFields ) {
				allJoints.add( field );
			}

			for( Class<?> superClass : interfaces ) {
				java.lang.reflect.Field[] superFields = AliceResourceClassUtilities.getFieldsOfType( superClass, org.lgna.story.resources.JointId.class );
				for( java.lang.reflect.Field field : superFields ) {
					allJoints.add( field );
				}
			}

			// Find the appropriate getter for our joint
			UserMethod jointGetter = null;
			for( java.lang.reflect.Field jointField : allJoints ) {
				if( jointField.getName().equals( joint.getName() ) ) {
					jointGetter = getPartAccessorMethod( jointField );
				}
			}

			// Create correct getter invocation
			if( jointGetter != null ) {
				UserField oldField = sceneInstance.ACCEPTABLE_HACK_FOR_SCENE_EDITOR_getFieldForInstanceInJava( jointedModelImp.getAbstraction() );
				UserField oldScene = StoryApiSpecificAstUtilities.getSceneFieldFromProgramType( getProject().getProgramType() );

				UserField newField = (UserField)getOrCreateNewDeclaration( oldField );
				UserField newScene = (UserField)getOrCreateNewDeclaration( oldScene );
				GlobalFirstInstanceExpression globalFirstInstanceExpression = new GlobalFirstInstanceExpression( this.scriptType );

				FieldAccess sceneAccess = AstUtilities.createFieldAccess( globalFirstInstanceExpression, newScene );
				FieldAccess fieldAccess = AstUtilities.createFieldAccess( sceneAccess, newField );

				this.crawler.addFieldReference( newField, fieldAccess, eventNode ); // Add new field reference

				AbstractMethod newGetter = newField.getValueType().findMethod( jointGetter.getName() );

				return AstUtilities.createMethodInvocation( fieldAccess, newGetter );
			}

			return null;
		}
		// If it's not a joint, it must be a UserInstance
		else {
			UserField oldField = sceneInstance.ACCEPTABLE_HACK_FOR_SCENE_EDITOR_getFieldForInstanceInJava( value );
			UserField oldScene = StoryApiSpecificAstUtilities.getSceneFieldFromProgramType( getProject().getProgramType() );

			UserField newField = (UserField)getOrCreateNewDeclaration( oldField );
			UserField newScene = (UserField)getOrCreateNewDeclaration( oldScene );
			GlobalFirstInstanceExpression globalFirstInstanceExpression = new GlobalFirstInstanceExpression( this.scriptType );

			FieldAccess sceneAccess = AstUtilities.createFieldAccess( globalFirstInstanceExpression, newScene );
			FieldAccess fieldAccess = AstUtilities.createFieldAccess( sceneAccess, newField );

			crawler.addFieldReference( newField, fieldAccess, eventNode ); // Add new field reference
			return fieldAccess;
		}
	}

	/**
	 * Modifies the array creation of a <code>ForEachInArrayLoop</code> to match
	 * with the number of times it was executed (ie. remove un-executed array
	 * values).
	 *
	 * @param eventNode the node containing the loop to modify
	 */
	private void copyForEachInArrayLoopEventNode( ForEachInArrayLoopEventNode eventNode ) {
		ForEachInArrayLoop forEachLoop = eventNode.getAstNode();
		if( eventNode.getArraySize() > eventNode.getNumberOfIterations() ) {
			AbstractEventNode<?> arrayCreationNode = EventNodeUtilities.findChildWithAstClass( eventNode, ArrayInstanceCreation.class );
			ArrayInstanceCreation oldArrayExpression = (ArrayInstanceCreation)arrayCreationNode.getAstNode();

			if( arrayCreationNode != null ) {
				List<Expression> expressions = oldArrayExpression.expressions.subList( 0, eventNode.getNumberOfIterations() );
				ArrayInstanceCreation newArrayExpression = org.lgna.project.ast.AstUtilities.createArrayInstanceCreation( oldArrayExpression.arrayType.getValue(), expressions );
				forEachLoop.array.setValue( newArrayExpression );
			}
		}
	}

	/**
	 * Builds and adds a set vehicle expression to the beginning of the script
	 * main method.
	 */
	public void addSetVehicleStatements( Map<UserField, Composite> nonDefaultVehicles, UserInstance sceneInstance ) {
		BlockStatement body = this.mainMethod.body.getValue();

		if( nonDefaultVehicles.isEmpty() ) {
			//pass
		} else {
			for( UserField field : nonDefaultVehicles.keySet() ) {

				// build setVehicle expression for original fields, then copy
				UserField executionField = getOldFieldForNew( field );
				Composite vehicle = nonDefaultVehicles.get( field );

				MethodInvocation setVehicleInvocation = RemixUtilities.createSetVehicleInvocation( executionField, vehicle, sceneInstance );
				ExpressionStatement statement = copyExpressionStatement( new ExpressionStatement( setVehicleInvocation ) );
				body.statements.add( 0, statement );
			}
		}
	}

	/* ---------------- AST Copy Methods -------------- */

	@Override
	protected void addNewDeclaration( AbstractDeclaration oldDeclaration, AbstractDeclaration newDeclaration ) {
		super.addNewDeclaration( oldDeclaration, newDeclaration );
		this.newToOldDeclarations.put( newDeclaration, oldDeclaration ); // Adds reverse mapping of what ASTCopier provides
	}

	@Override
	protected GlobalFirstInstanceExpression copyGlobalFirstInstanceExpression( GlobalFirstInstanceExpression expression ) {
		return new GlobalFirstInstanceExpression( this.scriptType ); // Global first always goes to scriptType (so we don't copy the whole program)
	}

	@Override
	public AbstractNode copyAbstractNode( AbstractNode node ) {
		AbstractNode rv = this.oldToNewNodeMap.get( node.getId() ); // Necessary for random-access copying employed when copying event nodes

		if( rv == null ) {
			rv = super.copyAbstractNode( node );
			this.oldToNewNodeMap.put( node.getId(), rv );
		}
		return rv;
	}

	public Statement[] copyInitializationStatements( Statement[] statements, java.util.Set<UserField> fields ) {
		java.util.ArrayList<Statement> rv = Lists.newArrayList();
		for( Statement statement : statements ) {
			if( RemixUtilities.isStatementInvokingMethod( statement ) ) {
				MethodInvocation methodInvocation = (MethodInvocation)( (ExpressionStatement)statement ).expression.getValue();
				java.util.ArrayList<AbstractArgument> arguments = Lists.newArrayList();

				arguments.addAll( methodInvocation.requiredArguments.getValue() );
				arguments.addAll( methodInvocation.variableArguments.getValue() );
				arguments.addAll( methodInvocation.keyedArguments.getValue() );

				for( AbstractArgument argument : arguments ) {
					replaceArgumentFieldAccessesExpressionsAsNecessary( argument, fields );
				}
			}
			rv.add( copyStatement( statement ) );
		}
		return ArrayUtilities.createArray( rv, Statement.class );
	}

	private void replaceArgumentFieldAccessesExpressionsAsNecessary( AbstractArgument argument, Set<UserField> fields ) {

		// Remove dangling references to non-remix fields in initialization statements
		if( argument.expression.getValue() instanceof FieldAccess ) {
			AbstractField field = ( (FieldAccess)argument.expression.getValue() ).field.getValue();

			if( !( fields.contains( field ) ) && ( field.getDeclaringType().isAssignableTo( org.lgna.story.SScene.class ) ) ) {
				argument.expression.setValue( new ThisExpression() );
			}
		} else if( RemixUtilities.isGetJointInvocation( argument.expression.getValue() ) ) {
			MethodInvocation jointGetter = (MethodInvocation)argument.expression.getValue();

			if( jointGetter.expression.getValue() instanceof FieldAccess ) {
				AbstractField field = ( (FieldAccess)jointGetter.expression.getValue() ).field.getValue();

				if( !( fields.contains( field ) ) && ( field.getDeclaringType().isAssignableTo( org.lgna.story.SScene.class ) ) ) {
					argument.expression.setValue( new ThisExpression() );
				}
			}
		}
	}

	/**
	 * Crawl filter that allows for ast reference checking on anything other
	 * than an <code>AbstractType</code>.
	 *
	 * @author Michael Pogran
	 */
	private class PullDownTypesFilter implements Criterion<Declaration> {

		@Override
		public boolean accept( Declaration declaration ) {
			return !( declaration instanceof AbstractType );
		}
	}

	/**
	 * Crawler class that checks for references to a provided
	 * {@link AbstractTransient} and records the <code>AbstractType</code> for
	 * the referencing expression. In this manner, the crawler is able to
	 * determine the lowest referenced type for the transient.
	 *
	 * @author Michael Pogran
	 */
	private abstract class TransientTypeCrawler<T extends AbstractTransient> implements Crawler {
		Map<T, AbstractType<?, ?, ?>> lowestReferencedType = Maps.newHashMap();

		public java.util.Set<T> getReferencedLocals() {
			return this.lowestReferencedType.keySet();
		}

		public AbstractType<?, ?, ?> getLowestReferencedType( T trans ) {
			return this.lowestReferencedType.get( trans );
		}

		protected void checkReferenceType( T trans, Expression expression ) {

			AbstractType<?, ?, ?> type = null;
			if( expression.getParent() instanceof MethodInvocation ) {
				MethodInvocation mi = (MethodInvocation)expression.getParent();
				type = mi.method.getValue().getDeclaringType();
			} else if( expression.getParent() instanceof SimpleArgument ) {
				SimpleArgument arg = (SimpleArgument)expression.getParent();
				type = arg.parameter.getValue().getValueType();
			} else if( trans.getValueType().isArray() && ( expression.getParent() instanceof ArrayAccess ) ) {
				ArrayAccess arrayAccess = (ArrayAccess)expression.getParent();

				if( arrayAccess.getParent() instanceof MethodInvocation ) {
					MethodInvocation mi = (MethodInvocation)arrayAccess.getParent();
					type = mi.method.getValue().getDeclaringType();
				} else if( arrayAccess.getParent() instanceof SimpleArgument ) {
					SimpleArgument arg = (SimpleArgument)arrayAccess.getParent();
					type = arg.parameter.getValue().getValueType();
				}
			}

			if( type != null ) {
				if( this.lowestReferencedType.containsKey( trans ) ) {
					AbstractType<?, ?, ?> oldType = this.lowestReferencedType.get( trans );

					if( oldType.isAssignableFrom( type ) ) {
						this.lowestReferencedType.put( trans, type );
					}
				} else {
					this.lowestReferencedType.put( trans, type );
				}
			}
		}
	}
}
