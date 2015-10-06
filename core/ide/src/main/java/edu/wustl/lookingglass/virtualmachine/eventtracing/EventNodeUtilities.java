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
package edu.wustl.lookingglass.virtualmachine.eventtracing;

import org.lgna.project.ast.AbstractNode;
import org.lgna.project.ast.UserField;

/**
 * @author Michael Pogran
 */
public class EventNodeUtilities {

	public static boolean isContainerStatementType( org.lgna.project.ast.Node node ) {
		return ( node instanceof org.lgna.project.ast.AbstractStatementWithBody ) || ( node instanceof org.lgna.project.ast.BlockStatement );
	}

	public static boolean isThreadedStatementType( org.lgna.project.ast.Node node ) {
		return ( node instanceof org.lgna.project.ast.DoTogether ) || ( node instanceof org.lgna.project.ast.AbstractEachInTogether );
	}

	public static AbstractEventNode<?> getRootNode( AbstractEventNode<?> eventNode ) {
		// root node doesn't have a parent
		while( eventNode.getParent() != null ) {
			eventNode = eventNode.getParent();
		}

		return eventNode;
	}

	public static AbstractEventNode<?> getSharedParentNode( AbstractEventNode<?> firstNode, AbstractEventNode<?> secondNode ) {
		if( ( firstNode != null ) && ( secondNode != null ) ) {
			java.util.ArrayList<AbstractEventNode<?>> firstPath = getPathToRoot( firstNode );
			java.util.ArrayList<AbstractEventNode<?>> secondPath = getPathToRoot( secondNode );

			AbstractEventNode<?> sharedParent = firstPath.get( firstPath.size() - 1 ); // root node

			for( int i = 0; i < firstPath.size(); i++ ) {
				for( int r = 0; r < secondPath.size(); r++ ) {
					if( firstPath.get( i ).equals( secondPath.get( r ) ) ) {
						return firstPath.get( i );
					}
				}
			}

			return sharedParent;
		} else {
			return null;
		}
	}

	public static ExpressionStatementEventNode getAncestorUserMethodEventNode( AbstractEventNode<?> eventNode ) {

		while( eventNode != null ) {
			if( eventNode instanceof ExpressionStatementEventNode ) {
				if( ( (ExpressionStatementEventNode)eventNode ).isUserMethod() ) {
					return (ExpressionStatementEventNode)eventNode;
				}
			}
			eventNode = eventNode.getParent();
		}
		return null;
	}

	public static ContainerEventNode<?> getIterationContainer( AbstractEventNode<?> eventNode ) {
		ContainerEventNode<?> rv = null;

		while( eventNode != null ) {

			if( ( eventNode.getParent() instanceof AbstractLoopEventNode ) || ( eventNode.getParent() instanceof EachInArrayTogetherEventNode ) ) {
				rv = (ContainerEventNode<?>)eventNode; // this should always be the case
				break;
			}
			eventNode = eventNode.getParent();
		}

		return rv;
	}

	public static ContainerEventNode<?> getMethodContainer( AbstractEventNode<?> eventNode ) {
		ContainerEventNode<?> rv = null;

		while( eventNode != null ) {

			if( eventNode.getParent() instanceof ExpressionStatementEventNode ) {
				rv = (ContainerEventNode<?>)eventNode; // this should always be the case
				break;
			}
			eventNode = eventNode.getParent();
		}

		return rv;
	}

	public static ExpressionStatementEventNode searchForParentExpressionStatementEventNode( AbstractEventNode<?> eventNode ) {
		ExpressionStatementEventNode rv = null;

		while( ( rv == null ) && ( eventNode != null ) ) {
			if( eventNode instanceof ExpressionStatementEventNode ) {
				rv = (ExpressionStatementEventNode)eventNode;
			} else {
				eventNode = eventNode.getParent();
			}
		}
		return rv;
	}

	public static UserField searchForContainerCaller( ContainerEventNode<?> containerNode ) {
		UserField rv = null;

		while( rv == null ) {
			java.util.ArrayList<AbstractEventNode<?>> children = containerNode.getChildren();

			for( AbstractEventNode<?> childNode : children ) {
				if( childNode instanceof ExpressionStatementEventNode ) {
					rv = ( (ExpressionStatementEventNode)childNode ).getCallerField();
					break;
				}
				else if( childNode instanceof ContainerEventNode ) {
					rv = searchForContainerCaller( (ContainerEventNode<?>)childNode );
				}
				else if( childNode instanceof ConditionalStatementEventNode ) {
					rv = searchForContainerCaller( ( (ConditionalStatementEventNode)childNode ).getBodyEventNode() );
				}
			}
		}
		return rv;
	}

	public static boolean isInIteratingType( AbstractEventNode<?> eventNode ) {
		return getIterationContainer( eventNode ) != null;
	}

	public static int getIterationForEventNode( AbstractEventNode<?> eventNode ) {

		ContainerEventNode<?> container = getIterationContainer( eventNode );
		if( container != null ) {
			if( container.getParent() instanceof AbstractLoopEventNode ) {
				return ( (AbstractLoopEventNode<?>)container.getParent() ).getIterationNumber( container );
			}
		}

		return -1;
	}

	public static boolean isEventNodeDescendant( AbstractEventNode<?> checkNode, AbstractEventNode<?> ancestorNode ) {

		while( checkNode != null ) {
			if( checkNode.equals( ancestorNode ) ) {
				return true;
			}
			checkNode = checkNode.getParent();
		}
		return false;
	}

	public static boolean isInUserMethod( AbstractEventNode<?> eventNode ) {
		eventNode = eventNode.getParent();

		while( eventNode != null ) {
			if( eventNode instanceof ExpressionStatementEventNode ) {
				if( ( (ExpressionStatementEventNode)eventNode ).isUserMethod() ) {
					return true;
				}
			}
			eventNode = eventNode.getParent();
		}
		return false;
	}

	public static UserField findUserMethodCaller( AbstractEventNode<?> eventNode ) {
		eventNode = eventNode.getParent();

		while( eventNode != null ) {
			if( eventNode instanceof ExpressionStatementEventNode ) {
				if( ( (ExpressionStatementEventNode)eventNode ).isUserMethod() ) {
					return ( (ExpressionStatementEventNode)eventNode ).getCallerField();
				}
			}
			eventNode = eventNode.getParent();
		}
		return null;
	}

	public static boolean isEventNodeIteratingType( AbstractEventNode<?> eventNode ) {
		return ( eventNode instanceof AbstractLoopEventNode ) || ( eventNode instanceof EachInArrayTogetherEventNode );
	}

	public static boolean isLastChild( ContainerEventNode<?> parent, AbstractEventNode<?> child ) {
		if( parent.hasChildren() ) {
			AbstractEventNode<?> lastChild = parent.getChildren().get( parent.getChildren().size() - 1 );

			if( lastChild.equals( child ) ) {
				return true;
			}

			if( lastChild instanceof ContainerEventNode ) {
				return isLastChild( (ContainerEventNode<?>)lastChild, child );
			}
		}
		return false;
	}

	public static boolean isFirstChild( ContainerEventNode<?> parent, AbstractEventNode<?> child ) {
		if( parent.hasChildren() ) {
			AbstractEventNode<?> firstChild = parent.getChildren().get( 0 );

			if( firstChild.equals( child ) ) {
				return true;
			}

			if( firstChild instanceof ContainerEventNode ) {
				return isFirstChild( (ContainerEventNode<?>)firstChild, child );
			}
		}
		return false;
	}

	public static java.util.List<AbstractEventNode<?>> getNodesBetween( AbstractEventNode<?> firstNode, AbstractEventNode<?> secondNode, boolean inclusive ) {
		java.util.List<AbstractEventNode<?>> nodes = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();

		AbstractEventNode<?> sharedParent = getSharedParentNode( firstNode, secondNode );

		if( sharedParent instanceof ContainerEventNode ) {
			int startIndex = getIndexInAncestor( (ContainerEventNode<?>)sharedParent, firstNode );
			int endIndex = getIndexInAncestor( (ContainerEventNode<?>)sharedParent, secondNode );

			java.util.List<AbstractEventNode<?>> subListNodes;

			if( ( startIndex > -1 ) && ( endIndex > -1 ) ) {

				if( startIndex < endIndex ) {
					subListNodes = ( (ContainerEventNode<?>)sharedParent ).getChildren().subList( startIndex + 1, endIndex ); // add one because subList is inclusive on fromIndex
				} else {
					subListNodes = ( (ContainerEventNode<?>)sharedParent ).getChildren().subList( endIndex + 1, startIndex );
				}

				if( inclusive ) {
					nodes.add( firstNode );
				}

				if( firstNode instanceof ContainerEventNode ) {
					appendChildren( (ContainerEventNode<?>)firstNode, nodes );
				}
				else if( firstNode instanceof ExpressionStatementEventNode ) {
					appendChildren( ( (ExpressionStatementEventNode)firstNode ).getUserMethodEventNode(), nodes );
				}
				else if( firstNode instanceof ConditionalStatementEventNode ) {
					appendChildren( ( (ConditionalStatementEventNode)firstNode ).getBodyEventNode(), nodes );
				}

				for( AbstractEventNode<?> node : subListNodes ) {
					nodes.add( node );
					if( node instanceof ContainerEventNode ) {
						appendChildren( (ContainerEventNode<?>)node, nodes );
					}
					else if( node instanceof ExpressionStatementEventNode ) {
						appendChildren( ( (ExpressionStatementEventNode)node ).getUserMethodEventNode(), nodes );
					}
					else if( node instanceof ConditionalStatementEventNode ) {
						appendChildren( ( (ConditionalStatementEventNode)node ).getBodyEventNode(), nodes );
					}
				}

				if( inclusive ) {
					nodes.add( secondNode );
				}

				if( secondNode instanceof ContainerEventNode ) {
					appendChildren( (ContainerEventNode<?>)secondNode, nodes );
				}
				else if( secondNode instanceof ExpressionStatementEventNode ) {
					appendChildren( ( (ExpressionStatementEventNode)secondNode ).getUserMethodEventNode(), nodes );
				}
				else if( secondNode instanceof ConditionalStatementEventNode ) {
					appendChildren( ( (ConditionalStatementEventNode)secondNode ).getBodyEventNode(), nodes );
				}
			}
		}

		return nodes;
	}

	public static boolean wasExecutingDuringTimePeriod( AbstractEventNode<?> eventNode, double startTime, double endTime ) {
		return ( startTime <= eventNode.getStartTime() ) && ( eventNode.getEndTime() <= endTime );
	}

	public static int getIndexInAncestor( ContainerEventNode<?> ancestor, AbstractEventNode<?> eventNode ) {

		while( eventNode != null ) {
			if( ancestor.equals( eventNode.getParent() ) ) {
				return ancestor.getChildren().indexOf( eventNode );
			}
			eventNode = eventNode.getParent();
		}

		return -1;
	}

	public static AbstractEventNode<?> findChildWithAstNode( AbstractEventNode<?> eventNode, final AbstractNode astNode ) {
		EventNodeCrawler crawler = new EventNodeCrawler() {

			@Override
			protected boolean isAcceptable( AbstractEventNode<?> eventNode ) {
				return eventNode.getAstUUID().equals( astNode.getId() );
			}
		};

		return crawler.searchChildren( eventNode );
	}

	public static AbstractEventNode<?> findChildWithAstClass( AbstractEventNode<?> eventNode, final Class<? extends org.lgna.project.ast.AbstractNode> klass ) {
		EventNodeCrawler crawler = new EventNodeCrawler() {

			@Override
			protected boolean isAcceptable( AbstractEventNode<?> eventNode ) {
				return eventNode.getAstNode().getClass().equals( klass );
			}
		};

		return crawler.searchChildren( eventNode );
	}

	public static void appendChildren( ContainerEventNode<?> containerNode, java.util.List<AbstractEventNode<?>> nodes ) {
		if( containerNode != null ) {
			for( AbstractEventNode<?> node : containerNode.getChildren() ) {
				nodes.add( node );

				if( node instanceof ContainerEventNode ) {
					appendChildren( (ContainerEventNode<?>)node, nodes );
				}
				else if( node instanceof ExpressionStatementEventNode ) {
					appendChildren( ( (ExpressionStatementEventNode)node ).getUserMethodEventNode(), nodes );
				}
				else if( node instanceof ConditionalStatementEventNode ) {
					appendChildren( ( (ConditionalStatementEventNode)node ).getBodyEventNode(), nodes );
				}
			}
		}
	}

	private static java.util.ArrayList<AbstractEventNode<?>> getPathToRoot( AbstractEventNode<?> eventNode ) {
		java.util.ArrayList<AbstractEventNode<?>> path = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();

		while( eventNode != null ) {
			path.add( eventNode );
			eventNode = eventNode.getParent();
		}

		return path;
	}

	private static abstract class EventNodeCrawler {

		protected abstract boolean isAcceptable( AbstractEventNode<?> eventNode );

		public AbstractEventNode<?> searchChildren( AbstractEventNode<?> eventNode ) {
			AbstractEventNode<?> rv = null;
			if( isAcceptable( eventNode ) ) {
				rv = eventNode;
			}

			if( eventNode instanceof ExpressionStatementEventNode ) {
				for( ExpressionEvaluationEventNode expressionEval : ( (ExpressionStatementEventNode)eventNode ).getExpressionEvaluationNodes() ) {
					if( rv == null ) {
						rv = searchChildren( expressionEval );
					}
				}

				if( ( (ExpressionStatementEventNode)eventNode ).isUserMethod() ) {
					if( rv == null ) {
						rv = searchChildren( ( (ExpressionStatementEventNode)eventNode ).getUserMethodEventNode() );
					}
				}
			}
			else if( eventNode instanceof ReturnStatementEventNode ) {
				if( rv == null ) {
					rv = searchChildren( ( (ReturnStatementEventNode)eventNode ).getExpressionNode() );
				}
			}
			else if( eventNode instanceof LocalDeclarationStatementEventNode ) {
				if( rv == null ) {
					rv = searchChildren( ( (LocalDeclarationStatementEventNode)eventNode ).getInitializerExpressionNode() );
				}
			}
			else if( eventNode instanceof EachInArrayTogetherEventNode ) {
				if( rv == null ) {
					rv = searchChildren( ( (EachInArrayTogetherEventNode)eventNode ).getArrayExpressionNode() );
				}
			}
			else if( eventNode instanceof CountLoopEventNode ) {
				if( rv == null ) {
					rv = searchChildren( ( (CountLoopEventNode)eventNode ).getCountExpressionNode() );
				}
			}
			else if( eventNode instanceof ForEachInArrayLoopEventNode ) {
				if( rv == null ) {
					rv = searchChildren( ( (ForEachInArrayLoopEventNode)eventNode ).getArrayExpressionNode() );
				}
			}
			else if( eventNode instanceof WhileLoopEventNode ) {
				for( ExpressionEvaluationEventNode expressionEval : ( (WhileLoopEventNode)eventNode ).getConditionalEvaluations() ) {
					if( rv == null ) {
						rv = searchChildren( expressionEval );
					}
				}
			}
			else if( eventNode instanceof ConditionalStatementEventNode ) {
				for( ExpressionEvaluationEventNode expressionEval : ( (ConditionalStatementEventNode)eventNode ).getConditionalEvaluations() ) {
					if( rv == null ) {
						rv = searchChildren( expressionEval );
					}
				}
			}
			else if( eventNode instanceof ExpressionEvaluationEventNode ) {
				for( AbstractEventNode<?> childNode : ( (ExpressionEvaluationEventNode)eventNode ).getChildren() ) {
					if( rv == null ) {
						rv = searchChildren( childNode );
					}
				}
			}

			// Check container children
			if( eventNode instanceof ContainerEventNode ) {
				for( AbstractEventNode<?> childNode : ( (ContainerEventNode<?>)eventNode ).getChildren() ) {
					if( rv == null ) {
						rv = searchChildren( childNode );
					}
				}
			}

			return rv;
		}

	}
}
