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
package org.alice.ide.common;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.BorderFactory;

import org.lgna.croquet.icon.IconSize;
import org.lgna.croquet.views.BoxUtilities;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.LineAxisPanel;
import org.lgna.croquet.views.PageAxisPanel;
import org.lgna.project.ast.Statement;

import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.LookingGlassTheme;
import edu.wustl.lookingglass.ide.croquet.components.StatementMenuButton;
import edu.wustl.lookingglass.puzzle.CompletionPuzzle;
import edu.wustl.lookingglass.puzzle.PuzzleProjectProperties;
import edu.wustl.lookingglass.puzzle.PuzzleStatementUtility;
import edu.wustl.lookingglass.puzzle.ui.PuzzleAstI18nFactory;
import edu.wustl.lookingglass.remix.models.StatementMenuOperation;

/**
 * @author Dennis Cosgrove
 */
public abstract class AbstractStatementPane extends org.alice.ide.common.StatementLikeSubstance {
	private static final java.awt.Color PASSIVE_OUTLINE_PAINT_FOR_NON_DRAGGABLE = edu.cmu.cs.dennisc.java.awt.ColorUtilities.createGray( 160 );

	public AbstractStatementPane( org.lgna.croquet.DragModel model, org.alice.ide.x.AstI18nFactory factory, org.lgna.project.ast.Statement statement, org.lgna.project.ast.StatementListProperty owner ) {
		super( model, org.alice.ide.common.StatementLikeSubstance.getClassFor( statement ), javax.swing.BoxLayout.LINE_AXIS );
		this.factory = factory;
		this.statement = statement;
		this.owner = owner;
		if( this.factory instanceof org.alice.ide.x.MutableAstI18nFactory ) {
			this.isEnabledListener = new edu.cmu.cs.dennisc.property.event.PropertyListener() {
				@Override
				public void propertyChanging( edu.cmu.cs.dennisc.property.event.PropertyEvent e ) {
				}

				@Override
				public void propertyChanged( edu.cmu.cs.dennisc.property.event.PropertyEvent e ) {
					AbstractStatementPane.this.repaint();
				}
			};
		} else {
			this.isEnabledListener = null;
		}

		//<lg>
		this.annotationPanel = new org.lgna.croquet.views.LineAxisPanel();
		this.addComponent( annotationPanel );
		this.addPuzzleAnnotations();

		if( !( this.factory instanceof edu.wustl.lookingglass.ide.perspectives.dinah.DinahAstI18nFactory ) ) {
			edu.wustl.lookingglass.ide.perspectives.dinah.StatementMenuOperationManager.addListener( this.statement, this.dinahListener );
		}
		//</lg>
	}

	//<lg>
	protected LineAxisPanel getAnnotationPanel() {
		return this.annotationPanel;
	}

	protected void addPuzzleAnnotations() {
		if( this.factory instanceof PuzzleAstI18nFactory ) {
			PuzzleAstI18nFactory puzzleAstI18nFactory = (PuzzleAstI18nFactory)this.factory;
			CompletionPuzzle puzzle = puzzleAstI18nFactory.getPuzzle();

			boolean showLock = false;
			boolean showPointer = false;
			boolean showModify = false;
			if( puzzle.isStatementStatic( statement ) && !puzzle.isStatementNonMutable( statement ) ) {
				showLock = true;
				if( PuzzleStatementUtility.isBodyStatement( statement ) ) {
					showModify = true;
				}
			} else if( !puzzle.isStatementStatic( statement ) && puzzle.isStatementNonMutable( statement ) ) {
				showLock = true;
				showPointer = true;
			}
			if( puzzle.isStatementStatic( statement ) && puzzle.isStatementNonMutable( statement ) ) {
				showLock = true;
			}

			if( showLock || showPointer ) {
				synchronized( this.annotationPanel.getTreeLock() ) {
					PageAxisPanel attributePanel = new PageAxisPanel();
					if( showPointer ) {
						Label icon = new Label( LookingGlassTheme.getIcon( "puzzle-pointer", IconSize.EXTRA_SMALL ) );
						icon.setBorder( BorderFactory.createEmptyBorder( 2, 3, 0, 0 ) );
						attributePanel.addComponent( icon );
					}
					if( showModify ) {
						Label icon = new Label( LookingGlassTheme.getIcon( "puzzle-modify", IconSize.EXTRA_SMALL ) );
						icon.setBorder( BorderFactory.createEmptyBorder( 2, 3, 0, 0 ) );
						attributePanel.addComponent( icon );
					}
					if( showLock ) {
						if( showPointer || showModify ) {
							attributePanel.addComponent( BoxUtilities.createVerticalStrut( 2 ) );
						}
						Label icon = new Label( LookingGlassTheme.getIcon( "puzzle-lock", IconSize.EXTRA_SMALL ) );
						icon.setBorder( BorderFactory.createEmptyBorder( 2, 3, 0, 0 ) );
						attributePanel.addComponent( icon );
					}
					this.annotationPanel.addComponent( attributePanel );
				}
			}
		}
	}

	public void addExecutionButton( edu.wustl.lookingglass.remix.models.StatementMenuOperation operation ) {
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnSwingThread( () -> {
			synchronized( this.annotationPanel.getTreeLock() ) {
				this.dinahButton = (StatementMenuButton)operation.createButton();
				this.annotationPanel.addComponent( this.dinahButton );
			}
			this.revalidateAndRepaint();
		} );
	}

	public void removeExecutionButton() {
		edu.wustl.lookingglass.croquetfx.ThreadHelper.runOnSwingThread( () -> {
			synchronized( this.annotationPanel.getTreeLock() ) {
				this.annotationPanel.removeAllComponents();
				this.dinahButton = null;
			}
			this.revalidateAndRepaint();
		} );
	}

	//</lg>

	private static java.awt.Paint createPaint( int x, int y, int yDelta, java.awt.Color baseColor ) {
		java.awt.Color aColor = baseColor;
		if( yDelta < 0 ) {
			aColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.scaleHSB( baseColor, 1.0, 1.0, 0.9 );
		}
		java.awt.Color bColor = edu.cmu.cs.dennisc.java.awt.ColorUtilities.setAlpha( baseColor, 0 );
		return new java.awt.GradientPaint( x, y, aColor, x, y + yDelta, bColor );
	}

	/**
	 * PAG -- Support for coloring code reuse selection
	 */
	@Override
	protected void paintPrologue( java.awt.Graphics2D g2, int x, int y, int width, int height ) {
		super.paintPrologue( g2, x, y, width, height );

		org.alice.ide.IDE ide = org.alice.ide.IDE.getActiveInstance();
		if( ide != null ) {
			org.alice.ide.perspectives.ProjectPerspective perspective = ide.getDocumentFrame().getPerspectiveState().getValue();
			if( perspective instanceof edu.wustl.lookingglass.ide.perspectives.dinah.AbstractDinahPerspective ) {
				edu.wustl.lookingglass.remix.models.EndCaptureState endState = edu.wustl.lookingglass.remix.models.EndCaptureState.getInstance();
				edu.wustl.lookingglass.remix.models.StartCaptureState startState = edu.wustl.lookingglass.remix.models.StartCaptureState.getInstance();

				java.awt.Color color = new java.awt.Color( 144, 184, 239 );
				if( startState.isStatementSelected( getStatement() ) && ( endState.getValue() == null ) ) {
					g2.setPaint( createPaint( x, y, height, color ) );
					super.paintPrologue( g2, x, y, width, height );
				} else if( endState.isStatementSelected( getStatement() ) && ( startState.getValue() == null ) ) {
					g2.setPaint( createPaint( x, y + height, y, color ) );
					super.paintPrologue( g2, x, y, width, height );
				} else if( endState.isStatementSelected( getStatement() ) || startState.isStatementSelected( getStatement() ) || edu.wustl.lookingglass.remix.ast.RemixUtilities.isInRemix( getStatement() ) ) {
					g2.setPaint( color );
					super.paintPrologue( g2, x, y, width, height );
				}
			} else if( LookingGlassIDE.getActiveInstance().isPuzzleEditorEnabled() && LookingGlassIDE.getActiveInstance().isInCodePerspective() ) {
				// Make some statement color soup just to show that there are puzzle attributes on the statements.
				// I'm sure there's a better UI for this, but this is really just for Me and Jason, so
				// color soup is good enough for now.
				PuzzleProjectProperties properties = LookingGlassIDE.getActiveInstance().getPuzzleProjectProperties();
				Statement statement = this.getStatement();

				ArrayList<Color> colors = new ArrayList<>();
				if( properties.isBeginStatement( statement ) ) {
					colors.add( new Color( 123, 215, 129 ) );
				}
				if( properties.containsNonMutableStatement( statement ) ) {
					colors.add( new Color( 159, 150, 216 ) );
				}
				if( properties.containsNonScrambledStatement( statement ) ) {
					colors.add( new Color( 136, 196, 207 ) );
				}
				if( properties.containsStaticStatement( statement ) ) {
					colors.add( new Color( 141, 180, 210 ) );
				}
				if( properties.containsDistractorStatement( statement ) ) {
					colors.add( new Color( 255, 232, 146 ) );
				}
				if( properties.isEndStatement( statement ) ) {
					colors.add( new Color( 255, 149, 146 ) );
				}

				if( colors.size() > 0 ) {
					if( colors.size() == 1 ) {
						colors.add( colors.get( 0 ) );
					}

					Point2D start = new Point2D.Float( x, y );
					Point2D end = new Point2D.Float( x, y + height );
					float[] fractions = new float[ colors.size() ];
					for( int i = 0; i < colors.size(); i++ ) {
						fractions[ i ] = (float)i / (float)( colors.size() - 1 );
					}
					LinearGradientPaint paint = new LinearGradientPaint( start, end, fractions, colors.toArray( new Color[ colors.size() ] ) );
					g2.setPaint( paint );
					super.paintPrologue( g2, x, y, width, height );
				}
			}
		}
	}

	public org.alice.ide.x.AstI18nFactory getFactory() {
		return this.factory;
	}

	// <lg>
	@Override
	protected boolean isClickAndClackAppropriate() {
		if( this.factory instanceof PuzzleAstI18nFactory ) {
			PuzzleAstI18nFactory puzzleAstI18nFactory = (PuzzleAstI18nFactory)this.factory;
			return puzzleAstI18nFactory.isClickAndClackAppropriate();
		} else {
			return super.isClickAndClackAppropriate();
		}
	}

	@Override
	protected java.awt.Paint getBackgroundPaint( int x, int y, int width, int height ) {
		if( this.statement instanceof org.lgna.project.ast.ExpressionStatement ) {
			org.lgna.project.ast.ExpressionStatement expressionStatement = (org.lgna.project.ast.ExpressionStatement)this.statement;

			if( expressionStatement.expression.getValue() instanceof org.lgna.project.ast.AssignmentExpression ) {
				return org.alice.ide.ThemeUtilities.getActiveTheme().getControlFlowStatementColor();
			}
		}
		return super.getBackgroundPaint( x, y, width, height );
	}

	// </lg>

	@Override
	protected java.awt.Paint getPassiveOutlinePaint() {
		if( this.getModel() != null ) {
			return super.getPassiveOutlinePaint();
		} else {
			return PASSIVE_OUTLINE_PAINT_FOR_NON_DRAGGABLE;
		}
	}

	@Override
	protected void handleDisplayable() {
		super.handleDisplayable();
		if( this.isEnabledListener != null ) {
			this.statement.isEnabled.addPropertyListener( this.isEnabledListener );
		}
	}

	@Override
	protected void handleUndisplayable() {
		if( this.isEnabledListener != null ) {
			this.statement.isEnabled.removePropertyListener( this.isEnabledListener );
		}
		super.handleUndisplayable();
	}

	public org.lgna.project.ast.Statement getStatement() {
		return this.statement;
	}

	public org.lgna.project.ast.StatementListProperty getOwner() {
		return this.owner;
	}

	@Override
	protected void paintEpilogue( java.awt.Graphics2D g2, int x, int y, int width, int height ) {
		super.paintEpilogue( g2, x, y, width, height );
		if( this.statement.isEnabled.getValue() ) {
			//pass
		} else {
			g2.setPaint( org.lgna.croquet.views.PaintUtilities.getDisabledTexturePaint() );
			this.fillBounds( g2 );
		}
	}

	private final edu.wustl.lookingglass.ide.perspectives.dinah.StatementMenuOperationListener dinahListener = new edu.wustl.lookingglass.ide.perspectives.dinah.StatementMenuOperationListener() {
		@Override
		public void addStatementMenuOperation( StatementMenuOperation operation ) {
			addExecutionButton( operation );
		}

		@Override
		public void removeStatementMenuOperation() {
			removeExecutionButton();
		}
	};

	protected edu.wustl.lookingglass.ide.croquet.components.StatementMenuButton dinahButton; // <lg/>
	private final org.lgna.croquet.views.LineAxisPanel annotationPanel; // <lg/>
	private final org.alice.ide.x.AstI18nFactory factory;
	private final org.lgna.project.ast.Statement statement;
	private final org.lgna.project.ast.StatementListProperty owner;
	private final edu.cmu.cs.dennisc.property.event.PropertyListener isEnabledListener;
}
