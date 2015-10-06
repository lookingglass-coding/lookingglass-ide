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
package edu.wustl.lookingglass.lgpedia;

import java.io.IOException;

import org.alice.ide.ast.ExpressionCreator.CannotCreateExpressionException;

/**
 * @author Michael Pogran
 */
public class LgpediaExpressionCreator {
	private final org.alice.stageide.ast.ExpressionCreator expressionCreator = new org.alice.stageide.ast.ExpressionCreator();
	private final org.lgna.project.ast.UserField maximumField;
	private final org.lgna.project.ast.UserField minimumField;
	private org.lgna.common.resources.AudioResource soundResource;

	public LgpediaExpressionCreator( org.lgna.project.ast.UserField maximumField, org.lgna.project.ast.UserField minimumField ) {
		this.maximumField = maximumField;
		this.minimumField = minimumField;

		java.io.File soundFile = new java.io.File( org.lgna.story.implementation.StoryApiDirectoryUtilities.getSoundGalleryDirectory(), "Musical Cues/success_keyboard.mp3" );
		try {
			this.soundResource = new org.lgna.common.resources.AudioResource( soundFile );
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}

	public java.util.List<org.lgna.project.ast.Expression> generateExpressionsForParamter( org.lgna.project.ast.AbstractParameter parameter ) {
		java.util.List<org.lgna.project.ast.Expression> rv = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();

		rv.add( generateExpressionForParameter( parameter, true ) );
		if( parameter.getValueType().isAssignableTo( String.class ) || parameter.getValueType().isAssignableTo( org.lgna.story.AudioSource.class ) ) {
			//pass
		} else {
			rv.add( generateExpressionForParameter( parameter, false ) );
		}
		try {
			// want third case for paint, reset to white
			if( ( parameter.getName() != null ) && parameter.getName().contentEquals( "paint" ) ) {
				rv.add( expressionCreator.createExpression( org.lgna.story.Color.WHITE ) );
			}
			else if( ( parameter.getName() != null ) && parameter.getName().contentEquals( "opacity" ) ) {
				rv.add( expressionCreator.createExpression( new Double( 1.0 ) ) );
			}
		} catch( CannotCreateExpressionException e ) {
			//pass
		}
		return rv;
	}

	public org.lgna.project.ast.Expression generateExpressionForParameter( org.lgna.project.ast.AbstractParameter parameter, boolean isMaximum ) {
		org.lgna.project.ast.Expression rv = null;
		try {
			// Text parameter
			if( parameter.getValueType().isAssignableTo( String.class ) ) {
				rv = expressionCreator.createExpression( "Hello World!" );
			}
			else if( parameter.getValueType().isAssignableTo( Number.class ) ) {
				// factor parameter
				if( ( parameter.getName() != null ) && parameter.getName().contentEquals( "factor" ) ) {
					if( isMaximum ) {
						rv = expressionCreator.createExpression( new Double( 5.0 ) );
					} else {
						rv = expressionCreator.createExpression( new Double( 0.5 ) );
					}
				}
				// opacity parameter
				else if( ( parameter.getName() != null ) && parameter.getName().contentEquals( "opacity" ) ) {
					if( isMaximum ) {
						rv = expressionCreator.createExpression( new Double( 0.5 ) );
					} else {
						rv = expressionCreator.createExpression( new Double( 0.0 ) );
					}
				}
				// density parameter
				else if( ( parameter.getName() != null ) && parameter.getName().contentEquals( "density" ) ) {
					if( isMaximum ) {
						rv = expressionCreator.createExpression( new Double( 1.0 ) );
					} else {
						rv = expressionCreator.createExpression( new Double( 0.5 ) );
					}
				}
				// number parameter
				else {
					if( isMaximum ) {
						rv = expressionCreator.createExpression( new Double( 5.0 ) );
					} else {
						rv = expressionCreator.createExpression( new Double( 1.0 ) );
					}
				}
			}
			else if( parameter.getValueType().isAssignableTo( org.lgna.story.MoveDirection.class ) ) {
				if( isMaximum ) {
					rv = expressionCreator.createExpression( org.lgna.story.MoveDirection.FORWARD );
				} else {
					rv = expressionCreator.createExpression( org.lgna.story.MoveDirection.BACKWARD );
				}
			}
			else if( parameter.getValueType().isAssignableTo( org.lgna.story.TurnDirection.class ) ) {
				if( isMaximum ) {
					rv = expressionCreator.createExpression( org.lgna.story.TurnDirection.LEFT );
				} else {
					rv = expressionCreator.createExpression( org.lgna.story.TurnDirection.RIGHT );
				}
			}
			else if( parameter.getValueType().isAssignableTo( org.lgna.story.RollDirection.class ) ) {
				if( isMaximum ) {
					rv = expressionCreator.createExpression( org.lgna.story.RollDirection.LEFT );
				} else {
					rv = expressionCreator.createExpression( org.lgna.story.RollDirection.RIGHT );
				}
			}
			else if( parameter.getValueType().isAssignableTo( org.lgna.story.Paint.class ) ) {
				if( isMaximum ) {
					rv = expressionCreator.createExpression( org.lgna.story.Color.BLUE );
				} else {
					rv = expressionCreator.createExpression( org.lgna.story.Color.RED );
				}
			}
			else if( parameter.getValueType().isAssignableTo( org.lgna.story.SpatialRelation.class ) ) {
				if( isMaximum ) {
					rv = expressionCreator.createExpression( org.lgna.story.SpatialRelation.IN_FRONT_OF );
				} else {
					rv = expressionCreator.createExpression( org.lgna.story.SpatialRelation.ABOVE );
				}
			}
			else if( parameter.getValueType().isAssignableTo( org.lgna.story.SThing.class ) ) {
				if( isMaximum ) {
					rv = createFieldExpression( this.maximumField );
				} else {
					rv = createFieldExpression( this.minimumField );
				}
			}
			else if( parameter.getValueType().isAssignableTo( org.lgna.story.StrideLength.class ) ) {
				if( isMaximum ) {
					rv = expressionCreator.createExpression( org.lgna.story.StrideLength.HUGE );
				} else {
					rv = expressionCreator.createExpression( org.lgna.story.StrideLength.TINY );
				}
			}
			else if( parameter.getValueType().isAssignableTo( org.lgna.story.Bounce.class ) ) {
				if( isMaximum ) {
					rv = expressionCreator.createExpression( org.lgna.story.Bounce.HUGE );
				} else {
					rv = expressionCreator.createExpression( org.lgna.story.Bounce.TINY );
				}
			}
			else if( parameter.getValueType().isAssignableTo( org.lgna.story.ArmSwing.class ) ) {
				if( isMaximum ) {
					rv = expressionCreator.createExpression( org.lgna.story.ArmSwing.HUGE );
				} else {
					rv = expressionCreator.createExpression( org.lgna.story.ArmSwing.TINY );
				}
			}
			else if( parameter.getValueType().isAssignableTo( org.lgna.story.AnimationStyle.class ) ) {
				if( isMaximum ) {
					rv = expressionCreator.createExpression( org.lgna.story.AnimationStyle.BEGIN_AND_END_ABRUPTLY );
				} else {
					rv = expressionCreator.createExpression( org.lgna.story.AnimationStyle.BEGIN_AND_END_GENTLY );
				}
			}
			else if( parameter.getValueType().isAssignableTo( org.lgna.story.PathStyle.class ) ) {
				if( isMaximum ) {
					rv = expressionCreator.createExpression( org.lgna.story.PathStyle.SMOOTH );
				} else {
					rv = expressionCreator.createExpression( org.lgna.story.PathStyle.BEE_LINE );
				}
			}
			else if( parameter.getValueType().isAssignableTo( Boolean.class ) ) {
				if( isMaximum ) {
					rv = expressionCreator.createExpression( Boolean.TRUE );
				} else {
					rv = expressionCreator.createExpression( Boolean.FALSE );
				}
			}
			else if( parameter.getValueType().isAssignableTo( org.lgna.story.SetDimensionPolicy.class ) ) {
				if( isMaximum ) {
					rv = expressionCreator.createExpression( org.lgna.story.SetDimensionPolicy.PRESERVE_ASPECT_RATIO );
				} else {
					rv = expressionCreator.createExpression( org.lgna.story.SetDimensionPolicy.PRESERVE_NOTHING );
				}
			}
			else if( parameter.getValueType().isAssignableTo( org.lgna.story.AudioSource.class ) ) {
				rv = expressionCreator.createExpression( this.soundResource );
			}

		} catch( CannotCreateExpressionException e ) {
			e.printStackTrace();
		}
		return rv;
	}

	private org.lgna.project.ast.Expression createFieldExpression( org.lgna.project.ast.UserField field ) {
		if( field.getValueType().isAssignableTo( org.lgna.story.SScene.class ) ) {
			return new org.lgna.project.ast.ThisExpression();
		} else {
			return new org.lgna.project.ast.FieldAccess( new org.lgna.project.ast.ThisExpression(), field );
		}
	}

	public org.alice.stageide.ast.ExpressionCreator getExpressionCreator() {
		return this.expressionCreator;
	}
}
