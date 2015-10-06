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
package edu.wustl.lookingglass.puzzle.ui.croquet.views;

import java.awt.Point;
import java.util.logging.Level;

import org.alice.ide.ast.draganddrop.statement.StatementDragModel;
import org.alice.ide.common.AbstractStatementPane;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.StatementListProperty;

import edu.cmu.cs.dennisc.property.event.AddListPropertyEvent;
import edu.cmu.cs.dennisc.property.event.RemoveListPropertyEvent;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.puzzle.CompletionPuzzle;
import edu.wustl.lookingglass.puzzle.ui.PuzzleAstI18nFactory;
import edu.wustl.lookingglass.study.StudyConfiguration;

/**
 * @author Dennis Cosgrove
 */
public class PuzzleStatementsBinView extends org.lgna.croquet.views.Panel {

	private final CompletionPuzzle puzzle;

	private final PuzzleAstI18nFactory statementFactory;
	private final StatementListProperty binStatements;
	private final java.util.Map<org.lgna.project.ast.Statement, org.alice.ide.common.AbstractStatementPane> statementToPaneMap = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();

	private org.lgna.project.ast.Statement dragStatement;

	private final edu.cmu.cs.dennisc.property.event.ListPropertyListener<org.lgna.project.ast.Statement> statementListPropertyListener = new edu.cmu.cs.dennisc.property.event.SimplifiedListPropertyAdapter<org.lgna.project.ast.Statement>() {
		@Override
		protected void changing( edu.cmu.cs.dennisc.property.event.ListPropertyEvent<org.lgna.project.ast.Statement> e ) {
		}

		@Override
		protected void changed( edu.cmu.cs.dennisc.property.event.ListPropertyEvent<org.lgna.project.ast.Statement> e ) {
			if( e instanceof AddListPropertyEvent<?> ) {
				AddListPropertyEvent<Statement> addListPropertyEvent = (AddListPropertyEvent<Statement>)e;
				for( Statement statement : addListPropertyEvent.getElements() ) {
					PuzzleStatementsBinView.this.updateStatementAdded( statement );
				}
			} else if( e instanceof RemoveListPropertyEvent<?> ) {
				RemoveListPropertyEvent<Statement> removeListPropertyEvent = (RemoveListPropertyEvent<Statement>)e;
				for( Statement statement : removeListPropertyEvent.getElements() ) {
					PuzzleStatementsBinView.this.updateStatementRemoved( statement );
				}
			}
			PuzzleStatementsBinView.this.revalidateAndRepaint();
		}
	};

	private class PuzzleResourcesBinLayoutManager implements java.awt.LayoutManager {
		@Override
		public void addLayoutComponent( java.lang.String name, java.awt.Component comp ) {
		}

		@Override
		public void removeLayoutComponent( java.awt.Component comp ) {
		}

		@Override
		public java.awt.Dimension minimumLayoutSize( java.awt.Container parent ) {
			return new java.awt.Dimension( 0, 0 );
		}

		@Override
		public java.awt.Dimension preferredLayoutSize( java.awt.Container parent ) {
			int width = 0;
			int height = 0;
			for( java.awt.Component child : parent.getComponents() ) {
				java.awt.Dimension preferredSize = child.getPreferredSize();
				java.awt.Point location = child.getLocation();

				width = Math.max( width, location.x + preferredSize.width );
				height = Math.max( height, location.y + preferredSize.height );
			}
			return new java.awt.Dimension( width, height );
		}

		@Override
		public void layoutContainer( java.awt.Container parent ) {
			for( java.awt.Component child : parent.getComponents() ) {
				org.alice.ide.common.AbstractStatementPane statementPane = (org.alice.ide.common.AbstractStatementPane)org.lgna.croquet.views.AwtComponentView.lookup( child );
				org.lgna.project.ast.Statement statement = statementPane.getStatement();

				if( ( statement != dragStatement ) && binStatements.contains( statement ) ) {
					java.awt.Dimension preferredSize = child.getPreferredSize();
					child.setSize( preferredSize );
				} else {
					child.setSize( new java.awt.Dimension( 0, 0 ) );
				}
			}
		}
	}

	public PuzzleStatementsBinView( CompletionPuzzle puzzle ) {
		super();
		this.puzzle = puzzle;
		this.binStatements = this.puzzle.getBinStatementsListProperty();
		this.statementFactory = new PuzzleAstI18nFactory( puzzle, false );

		this.setBackgroundColor( CompletionPuzzleView.PUZZLE_EDITABLE_COLOR );
		for( Statement statement : this.binStatements ) {
			this.createStatementPane( statement );
		}
	}

	public AbstractStatementPane getStatementPane( Statement statement ) {
		AbstractStatementPane pane = this.statementToPaneMap.get( statement );
		if( pane == null ) {
			pane = this.createStatementPane( statement );
		}
		return pane;
	}

	private AbstractStatementPane createStatementPane( Statement statement ) {
		AbstractStatementPane pane = this.statementFactory.createStatementPane( StatementDragModel.getInstance( statement ), statement, this.binStatements );
		pane.setVisible( false );
		this.statementToPaneMap.put( statement, pane );
		synchronized( this.getTreeLock() ) {
			this.internalAddComponent( pane );
		}
		return pane;
	}

	public void layoutStatements() {
		ThreadHelper.runOnSwingThread( () -> {
			java.util.Random random = new java.util.Random();
			final int PAD = 6;
			final int X_OFFSET = 36;
			final int MIN_BETWEEN_STATEMENTS = 12;
			final int WIGGLE_BETWEEN_STATEMENTS = 8;

			// compute the total height of all of the statements
			int totalPaneHeights = 0;
			for( Statement statement : this.binStatements ) {
				AbstractStatementPane pane = this.getStatementPane( statement );
				totalPaneHeights += pane.getHeight();
			}

			// compute minimum spacing between
			int totalBetweenStatements = this.binStatements.size() * ( MIN_BETWEEN_STATEMENTS + WIGGLE_BETWEEN_STATEMENTS );

			// Now try to see if it fits...
			int spaceInContainer = this.getHeight() - ( PAD * 2 );
			int minSpaceForPanes = totalPaneHeights + totalBetweenStatements;

			final int betweenStatementSpace;
			final int beginOffset;
			if( minSpaceForPanes <= spaceInContainer ) {
				int size = this.binStatements.size();
				int extraSpace = ( spaceInContainer - minSpaceForPanes ) / size;
				betweenStatementSpace = MIN_BETWEEN_STATEMENTS + extraSpace;
				beginOffset = betweenStatementSpace / 2;
			} else {
				// We are just going to have to grow the container, so we don't have overlapping statements
				betweenStatementSpace = MIN_BETWEEN_STATEMENTS;
				beginOffset = 0;
			}

			StringBuilder logMsg = new StringBuilder();
			logMsg.append( "layout puzzle bin: " );
			logMsg.append( this.puzzle.getBinBlockStatement().getReprWithId() ).append( ";\n" );

			int nextY = PAD + random.nextInt( WIGGLE_BETWEEN_STATEMENTS + 1 ) + beginOffset;

			// Place in reverse so the z order makes sense.
			for( int i = ( this.binStatements.size() - 1 ); i >= 0; i-- ) {
				Statement statement = this.binStatements.get( i );
				AbstractStatementPane pane = this.getStatementPane( statement );

				int x = Math.max( random.nextInt( X_OFFSET + 1 ), PAD );
				int y = nextY;
				nextY = y + pane.getHeight() + betweenStatementSpace + random.nextInt( WIGGLE_BETWEEN_STATEMENTS + 1 );

				pane.setLocation( x, y );
				synchronized( this.getTreeLock() ) {
					pane.setVisible( true );
				}

				logMsg.append( statement.getReprWithId() ).append( " -> (" ).append( x ).append( ", " ).append( y ).append( "); " );
			}

			this.updateZOrder();
			this.revalidateAndRepaint();

			StudyConfiguration.INSTANCE.getStudyLogger().log( Level.INFO, logMsg.toString() );
		} );
	}

	@Override
	protected void handleAddedTo( org.lgna.croquet.views.AwtComponentView<?> parent ) {
		super.handleAddedTo( parent );
		this.binStatements.addListPropertyListener( this.statementListPropertyListener );
	}

	@Override
	protected void handleRemovedFrom( org.lgna.croquet.views.AwtComponentView<?> parent ) {
		this.binStatements.removeListPropertyListener( this.statementListPropertyListener );
		super.handleRemovedFrom( parent );
	}

	public void setStatementPaneLocation( Statement statement, int x, int y ) {
		// bump fix for out-of-bounds statement placements from puzzle drop area
		if( x < 0 ) {
			x = 0;
		}
		if( y < 0 ) {
			y = 0;
		}
		AbstractStatementPane pane = this.getStatementPane( statement );
		pane.setLocation( x, y );

		this.updateZOrder();
		this.revalidateAndRepaint();
	}

	public void setStatementPaneLocation( Statement statement, Point location ) {
		this.setStatementPaneLocation( statement, location.x, location.y );
	}

	@Override
	protected java.awt.LayoutManager createLayoutManager( javax.swing.JPanel jPanel ) {
		return new PuzzleResourcesBinLayoutManager();
	}

	public void setDragStatement( org.lgna.project.ast.Statement dragStatement ) {
		this.dragStatement = dragStatement;
		this.revalidateAndRepaint();
	}

	private void updateStatementAdded( Statement statement ) {
		synchronized( this.getTreeLock() ) {
			AbstractStatementPane pane = this.getStatementPane( statement );
			pane.setVisible( true );
		}
		this.updateZOrder();
	}

	private void updateStatementRemoved( Statement statement ) {
		// note: should we do an internalRemoveComponent?
		this.updateZOrder();
	}

	private void updateZOrder() {
		int index = this.binStatements.size() - 1;
		for( org.lgna.project.ast.Statement statement : this.binStatements ) {
			this.setComponentZOrder( this.getStatementPane( statement ), index );
			index--;
		}
	}
}
