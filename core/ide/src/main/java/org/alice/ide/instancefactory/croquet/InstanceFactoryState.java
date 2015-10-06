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

package org.alice.ide.instancefactory.croquet;

import org.alice.ide.instancefactory.InstanceFactory;
import org.alice.nonfree.NebulousIde;
import org.alice.stageide.ast.StoryApiSpecificAstUtilities;
import org.lgna.project.ast.AbstractType;
import org.lgna.project.ast.CrawlPolicy;
import org.lgna.project.ast.Declaration;
import org.lgna.project.ast.NamedUserType;
import org.lgna.project.ast.UserMethod;

import edu.cmu.cs.dennisc.pattern.Criterion;
import edu.cmu.cs.dennisc.pattern.IsInstanceCrawler;
import edu.wustl.lookingglass.ide.LookingGlassIDE;

/**
 * @author Dennis Cosgrove
 */
public class InstanceFactoryState extends org.lgna.croquet.CustomItemStateWithInternalBlank<InstanceFactory> {
	public InstanceFactoryState( org.alice.ide.ProjectDocumentFrame projectDocumentFrame ) {
		super( org.lgna.croquet.Application.DOCUMENT_UI_GROUP, java.util.UUID.fromString( "f4e26c9c-0c3d-4221-95b3-c25df0744a97" ), null, org.alice.ide.instancefactory.croquet.codecs.InstanceFactoryCodec.SINGLETON );
		projectDocumentFrame.getMetaDeclarationFauxState().addValueListener( declarationListener );
		org.alice.ide.project.ProjectChangeOfInterestManager.SINGLETON.addProjectChangeOfInterestListener( this.projectChangeOfInterestListener );
		this.setValueTransactionlessly( org.alice.ide.instancefactory.GlobalFirstInstanceSceneFactory.getInstance() );
	}

	// <lg>
	//	private void fallBackToDefaultFactory() {
	//		this.setValueTransactionlessly( org.alice.ide.instancefactory.ThisInstanceFactory.getInstance() );
	//	}
	// </lg>

	private void handleDeclarationChanged( org.lgna.project.ast.AbstractDeclaration prevValue, org.lgna.project.ast.AbstractDeclaration nextValue ) {
		if( this.ignoreCount == 0 ) {
			// <lg>
			org.lgna.project.ast.AbstractType<?, ?, ?> prevSelectedType = org.lgna.project.ast.AstUtilities.getDeclaringTypeIfMemberOrTypeItselfIfType( prevValue );
			org.lgna.project.ast.AbstractType<?, ?, ?> nextSelectedType = org.lgna.project.ast.AstUtilities.getDeclaringTypeIfMemberOrTypeItselfIfType( nextValue );
			org.alice.ide.instancefactory.InstanceFactory value = this.getValue();

			if( org.alice.stageide.ast.StoryApiSpecificAstUtilities.isSceneType( prevSelectedType ) ) {
				if( org.alice.stageide.ast.StoryApiSpecificAstUtilities.isSceneType( nextSelectedType ) ) {
					//pass
				} else {
					org.lgna.project.ast.UserField field = null;
					if( value instanceof org.alice.ide.instancefactory.ThisFieldAccessFactory ) {
						org.alice.ide.instancefactory.ThisFieldAccessFactory fieldAccessFactory = (org.alice.ide.instancefactory.ThisFieldAccessFactory)value;
						field = fieldAccessFactory.getField();
					}
					if( field != null ) {
						this.setValueTransactionlessly( org.alice.ide.instancefactory.GlobalFirstInstanceSceneFieldInstanceFactory.getInstance( field ) );
					} else {
						this.setValueTransactionlessly( org.alice.ide.instancefactory.GlobalFirstInstanceSceneFactory.getInstance() );
					}
				}
			} else {
				if( org.alice.stageide.ast.StoryApiSpecificAstUtilities.isSceneType( nextSelectedType ) ) {
					org.lgna.project.ast.UserField field = null;
					if( value instanceof org.alice.ide.instancefactory.GlobalFirstInstanceSceneFieldInstanceFactory ) {
						org.alice.ide.instancefactory.GlobalFirstInstanceSceneFieldInstanceFactory fieldAccessFactory = (org.alice.ide.instancefactory.GlobalFirstInstanceSceneFieldInstanceFactory)value;
						field = fieldAccessFactory.getField();
					}
					if( field != null ) {
						this.setValueTransactionlessly( org.alice.ide.instancefactory.ThisFieldAccessFactory.getInstance( field ) );
					} else {
						this.setValueTransactionlessly( org.alice.ide.instancefactory.ThisInstanceFactory.getInstance() );
					}
				} else {
					if( value instanceof org.alice.ide.instancefactory.GlobalFirstInstanceSceneFieldInstanceFactory ) {
						this.setValueTransactionlessly( value );
					} else {
						this.setValueTransactionlessly( org.alice.ide.instancefactory.GlobalFirstInstanceSceneFactory.getInstance() );
					}
				}
			}
			//			if( nextValue instanceof org.lgna.project.ast.AbstractMethod ) {
			//				org.lgna.project.ast.AbstractMethod method = (org.lgna.project.ast.AbstractMethod)nextValue;
			//				if( method.isStatic() ) {
			//					this.setValueTransactionlessly( null );
			//					return;
			//				}
			//			}
			//			InstanceFactory instanceFactory = this.getValue();
			//			if( instanceFactory != null ) {
			//				if( instanceFactory.isValid() ) {
			//					//pass
			//				} else {
			//					this.fallBackToDefaultFactory();
			//				}
			//			} else {
			//				this.fallBackToDefaultFactory();
			//			}
			//			//			org.lgna.project.ast.AbstractType< ?,?,? > prevType = getDeclaringType( prevValue );
			//			//			org.lgna.project.ast.AbstractType< ?,?,? > nextType = getDeclaringType( nextValue );
			//			//			if( prevType != nextType ) {
			//			//				InstanceFactory prevValue = this.getValue();
			//			//				if( prevType != null ) {
			//			//					if( prevValue != null ) {
			//			//						map.put( prevType, prevValue );
			//			//					} else {
			//			//						map.remove( prevType );
			//			//					}
			//			//				}
			//			//				InstanceFactory nextValue;
			//			//				if( nextType != null ) {
			//			//					nextValue = map.get( nextType );
			//			//					if( nextValue != null ) {
			//			//						//pass
			//			//					} else {
			//			//						nextValue = org.alice.ide.instancefactory.ThisInstanceFactory.getInstance();
			//			//					}
			//			//				} else {
			//			//					nextValue = null;
			//			//				}
			//			//				this.setValueTransactionlessly( nextValue );
			//			//			}
			// </lg>
		}
	}

	private void handleAstChangeThatCouldBeOfInterest() {
		InstanceFactory instanceFactory = this.getValue();
		if( instanceFactory != null ) {
			if( instanceFactory.isValid() ) {
				//pass
			} else {
				//				this.fallBackToDefaultFactory();
			}
		} else {
			//			this.fallBackToDefaultFactory();
		}
	}

	private static org.lgna.croquet.CascadeBlankChild<InstanceFactory> createFillInMenuComboIfNecessary( org.lgna.croquet.CascadeFillIn<InstanceFactory, Void> item, org.lgna.croquet.CascadeMenuModel<InstanceFactory> subMenu ) {
		if( subMenu != null ) {
			return new org.lgna.croquet.CascadeItemMenuCombo<InstanceFactory>( item, subMenu );
		} else {
			return item;
		}
	}

	/* package-private */static org.lgna.croquet.CascadeBlankChild<InstanceFactory> createFillInMenuComboIfNecessaryForField( org.alice.ide.ApiConfigurationManager apiConfigurationManager, org.lgna.project.ast.UserField field ) {
		org.lgna.project.ast.NamedUserType programType = org.alice.ide.ProjectStack.peekProject().getProgramType();
		org.lgna.project.ast.NamedUserType sceneType = org.alice.stageide.ast.StoryApiSpecificAstUtilities.getSceneTypeFromProgramType( programType );
		org.lgna.project.ast.AbstractType<?, ?, ?> selectedType = org.alice.ide.IDE.getActiveInstance().getDocumentFrame().getTypeMetaState().getValue();

		if( selectedType == sceneType ) {
			return createFillInMenuComboIfNecessary(
					InstanceFactoryFillIn.getInstance( org.alice.ide.instancefactory.ThisFieldAccessFactory.getInstance( field ) ),
					apiConfigurationManager.getInstanceFactorySubMenuForThisFieldAccess( field ) );
		} else {
			return createFillInMenuComboIfNecessary(
					InstanceFactoryFillIn.getInstance( org.alice.ide.instancefactory.GlobalFirstInstanceSceneFieldInstanceFactory.getInstance( field ) ),
					apiConfigurationManager.getInstanceFactorySubMenuForGlobalFirstFieldAccess( field ) );
		}
	}

	@Override
	protected void appendPrepModelsToCascadeRootPath( java.util.List<org.lgna.croquet.PrepModel> cascadeRootPath, org.lgna.croquet.edits.Edit edit ) {
		super.appendPrepModelsToCascadeRootPath( cascadeRootPath, edit );
		if( edit instanceof org.lgna.croquet.edits.StateEdit ) {
			org.lgna.croquet.edits.StateEdit<InstanceFactory> stateEdit = (org.lgna.croquet.edits.StateEdit<InstanceFactory>)edit;
			InstanceFactory nextValue = stateEdit.getNextValue();
			if( nextValue instanceof org.alice.ide.instancefactory.ThisFieldAccessMethodInvocationFactory ) {
				org.alice.ide.instancefactory.ThisFieldAccessMethodInvocationFactory thisFieldAccessMethodInvocationFactory = (org.alice.ide.instancefactory.ThisFieldAccessMethodInvocationFactory)nextValue;
				org.lgna.project.ast.UserField field = thisFieldAccessMethodInvocationFactory.getField();
				org.alice.ide.IDE ide = org.alice.ide.IDE.getActiveInstance();
				org.alice.ide.ApiConfigurationManager apiConfigurationManager = ide.getApiConfigurationManager();
				cascadeRootPath.add( apiConfigurationManager.getInstanceFactorySubMenuForThisFieldAccess( field ) );
			}
		} else {
			throw new RuntimeException( edit != null ? edit.toString() : null );
		}
	}

	//todo
	private final ParametersVariablesAndConstantsSeparator parametersVariablesConstantsSeparator = new ParametersVariablesAndConstantsSeparator();

	@Override
	protected void updateBlankChildren( java.util.List<org.lgna.croquet.CascadeBlankChild> blankChildren, org.lgna.croquet.imp.cascade.BlankNode<InstanceFactory> blankNode ) {
		org.alice.ide.IDE ide = org.alice.ide.IDE.getActiveInstance();
		org.alice.ide.ApiConfigurationManager apiConfigurationManager = ide.getApiConfigurationManager();
		org.lgna.project.ast.AbstractDeclaration declaration = org.alice.ide.meta.DeclarationMeta.getDeclaration();
		boolean isStaticMethod;
		if( declaration instanceof org.lgna.project.ast.AbstractMethod ) {
			org.lgna.project.ast.AbstractMethod method = (org.lgna.project.ast.AbstractMethod)declaration;
			isStaticMethod = method.isStatic();
		} else {
			isStaticMethod = false;
		}

		if( isStaticMethod ) {
			//pass
		} else {
			// <lg>
			org.lgna.project.ast.NamedUserType programType = org.alice.ide.ProjectStack.peekProject().getProgramType();
			org.lgna.project.ast.NamedUserType sceneType = org.alice.stageide.ast.StoryApiSpecificAstUtilities.getSceneTypeFromProgramType( programType );
			org.lgna.project.ast.AbstractType<?, ?, ?> selectedType = org.alice.ide.IDE.getActiveInstance().getDocumentFrame().getTypeMetaState().getValue();

			// Always add "this"
			blankChildren.add(
					createFillInMenuComboIfNecessary(
							InstanceFactoryFillIn.getInstance( org.alice.ide.instancefactory.ThisInstanceFactory.getInstance() ),
							apiConfigurationManager.getInstanceFactorySubMenuForThis( selectedType ) ) );

			if( selectedType != sceneType ) {
				blankChildren.add(
						createFillInMenuComboIfNecessary(
								InstanceFactoryFillIn.getInstance( org.alice.ide.instancefactory.GlobalFirstInstanceSceneFactory.getInstance() ),
								apiConfigurationManager.getInstanceFactorySubMenuForThis( sceneType ) ) );
			}
			// </lg>
		}
		if( org.alice.ide.meta.DeclarationMeta.getType() instanceof org.lgna.project.ast.NamedUserType ) {
			org.lgna.project.ast.NamedUserType namedUserType = (org.lgna.project.ast.NamedUserType)org.alice.ide.meta.DeclarationMeta.getType();
			if( isStaticMethod ) {
				//pass
			} else {
				// <lg>
				NamedUserType sceneType = StoryApiSpecificAstUtilities.getSceneTypeFromProject( LookingGlassIDE.getActiveInstance().getProject() );
				java.util.List<UserMethod> methods = StoryApiSpecificAstUtilities.getUserMethodsInvokedSceneActivationListeners( sceneType );
				IsInstanceCrawler<org.lgna.project.ast.UserField> crawler = IsInstanceCrawler.createInstance( org.lgna.project.ast.UserField.class );

				if( methods.size() > 0 ) {
					UserMethod userMain = methods.get( 0 );

					userMain.crawl( crawler, CrawlPolicy.COMPLETE, new Criterion<Declaration>() {

						@Override
						public boolean accept( Declaration e ) {
							return !( e instanceof AbstractType );
						}
					} );
				}

				java.util.List<org.lgna.project.ast.UserField> fields = namedUserType.getDeclaredFields();
				java.util.List<org.lgna.project.ast.UserField> filteredFields = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();
				for( org.lgna.project.ast.UserField field : fields ) {
					if( apiConfigurationManager.isInstanceFactoryDesiredForType( field.getValueType() ) ) {
						filteredFields.add( field );
					}
				}
				java.util.List<org.lgna.project.ast.UserField> activeFields = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();
				java.util.List<org.lgna.project.ast.UserField> propFields = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();

				for( org.lgna.project.ast.UserField field : filteredFields ) {
					if( field.getValueType().isAssignableTo( org.lgna.story.SProp.class ) ) {
						if( crawler.getList().contains( field ) ) {
							activeFields.add( field );
						} else {
							propFields.add( field );
						}
					} else if( NebulousIde.nonfree.isAssignableToSRoom( field.getValueType() ) ) {
						propFields.add( field );
					} else if( field.getValueType().isAssignableTo( org.lgna.story.SGround.class ) ) {
						propFields.add( field );
					} else {
						activeFields.add( field );
					}
				}
				java.util.List<org.lgna.croquet.CascadeBlankChild> activeBlanks = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();
				java.util.List<org.lgna.croquet.CascadeBlankChild> propBlanks = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();

				for( org.lgna.project.ast.UserField field : activeFields ) {
					activeBlanks.add( createFillInMenuComboIfNecessaryForField( apiConfigurationManager, field ) );
				}
				for( org.lgna.project.ast.UserField field : propFields ) {
					propBlanks.add( createFillInMenuComboIfNecessaryForField( apiConfigurationManager, field ) );
				}

				blankChildren.add( new edu.wustl.lookingglass.ide.croquet.components.LabelSeparator( "Active Characters:" ) );
				blankChildren.addAll( activeBlanks );
				blankChildren.add( new edu.wustl.lookingglass.ide.croquet.components.LabelSeparator( "Prop Characters:" ) );
				blankChildren.addAll( propBlanks );

				//				blankChildren.add( org.lgna.croquet.CascadeLineSeparator.getInstance() );
				//				blankChildren.add( new ActorsMenu( activeBlanks ) );
				//				blankChildren.add( org.lgna.croquet.CascadeLineSeparator.getInstance() );
				//				blankChildren.add( new PropsMenu( propBlanks ) );
				// </lg>
			}

			org.lgna.project.ast.AbstractCode code = ide.getDocumentFrame().getFocusedCode();
			if( code instanceof org.lgna.project.ast.UserCode ) {

				java.util.List<org.lgna.croquet.CascadeBlankChild> parameters = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();
				java.util.List<org.lgna.croquet.CascadeBlankChild> locals = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();
				boolean containsVariable = false;
				boolean containsConstant = false;
				org.lgna.project.ast.UserCode userCode = (org.lgna.project.ast.UserCode)code;
				for( org.lgna.project.ast.UserParameter parameter : userCode.getRequiredParamtersProperty() ) {
					if( apiConfigurationManager.isInstanceFactoryDesiredForType( parameter.getValueType() ) ) {
						parameters.add(
								createFillInMenuComboIfNecessary(
										InstanceFactoryFillIn.getInstance( org.alice.ide.instancefactory.ParameterAccessFactory.getInstance( parameter ) ),
										apiConfigurationManager.getInstanceFactorySubMenuForParameterAccess( parameter ) ) );
					}
				}

				for( org.lgna.project.ast.UserLocal local : org.lgna.project.ProgramTypeUtilities.getLocals( userCode ) ) {
					if( apiConfigurationManager.isInstanceFactoryDesiredForType( local.getValueType() ) ) {
						if( local.isFinal.getValue() ) {
							containsConstant = true;
						} else {
							containsVariable = true;
						}
						locals.add(
								createFillInMenuComboIfNecessary(
										InstanceFactoryFillIn.getInstance( org.alice.ide.instancefactory.LocalAccessFactory.getInstance( local ) ),
										apiConfigurationManager.getInstanceFactorySubMenuForLocalAccess( local ) ) );
					}
				}
				if( ( parameters.size() > 0 ) || ( locals.size() > 0 ) ) {
					blankChildren.add( org.lgna.croquet.CascadeLineSeparator.getInstance() );
					blankChildren.add( this.parametersVariablesConstantsSeparator );
					StringBuilder sb = new StringBuilder();
					org.lgna.project.ast.NodeUtilities.safeAppendRepr( sb, code );
					sb.append( " " );
					String prefix = "";
					if( parameters.size() > 0 ) {
						sb.append( "parameters" );
						blankChildren.addAll( parameters );
						prefix = ", ";
					}
					if( locals.size() > 0 ) {
						if( containsVariable ) {
							sb.append( prefix );
							sb.append( "variables" );
							prefix = ", ";
						}
						if( containsConstant ) {
							sb.append( prefix );
							sb.append( "constants" );
							prefix = ", ";
						}
						blankChildren.addAll( locals );
					}
					this.parametersVariablesConstantsSeparator.setMenuItemText( sb.toString() );
				}

				if( userCode instanceof org.lgna.project.ast.UserMethod ) {
					org.lgna.project.ast.UserMethod userMethod = (org.lgna.project.ast.UserMethod)userCode;
					if( org.alice.stageide.StageIDE.INITIALIZE_EVENT_LISTENERS_METHOD_NAME.equals( userMethod.getName() ) ) {
						for( org.lgna.project.ast.Statement statement : userMethod.body.getValue().statements ) {
							if( statement instanceof org.lgna.project.ast.ExpressionStatement ) {
								org.lgna.project.ast.ExpressionStatement expressionStatement = (org.lgna.project.ast.ExpressionStatement)statement;
								org.lgna.project.ast.Expression expression = expressionStatement.expression.getValue();
								if( expression instanceof org.lgna.project.ast.MethodInvocation ) {
									org.lgna.project.ast.MethodInvocation methodInvocation = (org.lgna.project.ast.MethodInvocation)expression;
									java.util.List<org.lgna.croquet.CascadeBlankChild> methodInvocationBlankChildren = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();

									for( org.lgna.project.ast.SimpleArgument argument : methodInvocation.requiredArguments ) {
										org.lgna.project.ast.Expression argumentExpression = argument.expression.getValue();
										if( argumentExpression instanceof org.lgna.project.ast.LambdaExpression ) {
											org.lgna.project.ast.LambdaExpression lambdaExpression = (org.lgna.project.ast.LambdaExpression)argumentExpression;
											org.lgna.project.ast.Lambda lambda = lambdaExpression.value.getValue();
											if( lambda instanceof org.lgna.project.ast.UserLambda ) {
												org.lgna.project.ast.UserLambda userLambda = (org.lgna.project.ast.UserLambda)lambda;
												for( org.lgna.project.ast.UserParameter parameter : userLambda.getRequiredParameters() ) {
													org.lgna.project.ast.AbstractType<?, ?, ?> parameterType = parameter.getValueType();
													for( org.lgna.project.ast.AbstractMethod parameterMethod : org.lgna.project.ast.AstUtilities.getAllMethods( parameterType ) ) {
														org.lgna.project.ast.AbstractType<?, ?, ?> parameterMethodReturnType = parameterMethod.getReturnType();
														if( parameterMethodReturnType.isAssignableTo( org.lgna.story.SThing.class ) ) {
															methodInvocationBlankChildren.add(
																	createFillInMenuComboIfNecessary(
																			InstanceFactoryFillIn.getInstance( org.alice.ide.instancefactory.ParameterAccessMethodInvocationFactory.getInstance( parameter, parameterMethod ) ),
																			apiConfigurationManager.getInstanceFactorySubMenuForParameterAccessMethodInvocation( parameter, parameterMethod ) ) );
														}
													}
												}
											}
										}
									}

									if( methodInvocationBlankChildren.size() > 0 ) {
										org.lgna.project.ast.AbstractMethod method = methodInvocation.method.getValue();
										blankChildren.add( org.alice.ide.croquet.models.cascade.MethodNameSeparator.getInstance( method ) );
										blankChildren.addAll( methodInvocationBlankChildren );
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected org.alice.ide.instancefactory.InstanceFactory getSwingValue() {
		return this.value;
	}

	@Override
	protected void setSwingValue( org.alice.ide.instancefactory.InstanceFactory value ) {
		this.value = value;
	}

	private int ignoreCount = 0;

	public void pushIgnoreAstChanges() {
		ignoreCount++;
	}

	public void popIgnoreAstChanges() {
		ignoreCount--;
		if( ignoreCount == 0 ) {
			this.handleAstChangeThatCouldBeOfInterest();
		}
	}

	private final org.alice.ide.MetaDeclarationFauxState.ValueListener declarationListener = new org.alice.ide.MetaDeclarationFauxState.ValueListener() {
		@Override
		public void changed( org.lgna.project.ast.AbstractDeclaration prevValue, org.lgna.project.ast.AbstractDeclaration nextValue ) {
			InstanceFactoryState.this.handleDeclarationChanged( prevValue, nextValue );
		}
	};
	//todo: map AbstractCode to Stack< InstanceFactory >
	//private java.util.Map< org.lgna.project.ast.AbstractDeclaration, InstanceFactory > map = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();
	private InstanceFactory value;

	private final org.alice.ide.project.events.ProjectChangeOfInterestListener projectChangeOfInterestListener = new org.alice.ide.project.events.ProjectChangeOfInterestListener() {
		@Override
		public void projectChanged() {
			handleAstChangeThatCouldBeOfInterest();
		}
	};
}
