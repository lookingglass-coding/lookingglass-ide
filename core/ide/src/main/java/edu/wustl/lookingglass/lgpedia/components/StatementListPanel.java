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
package edu.wustl.lookingglass.lgpedia.components;

import java.beans.PropertyChangeEvent;

import org.lgna.croquet.views.MigPanel;

import edu.wustl.lookingglass.lgpedia.LgpediaAnnotationFormatter;
import edu.wustl.lookingglass.lgpedia.LgpediaAstI18nFactory;

/**
 * @author Michael Pogran
 */
public class StatementListPanel extends MigPanel {
	private final StatementAxisPanel statementPanel;
	private final StatementAxisPanel detailPanel;
	private final CollapseExpandLabel collapseExpandLabel;

	private java.awt.Color executeStatementBaseColor;
	private boolean hasAddedStatement;

	public StatementListPanel( edu.wustl.lookingglass.lgpedia.LgpediaComposite composite ) {
		super( composite, "fill, ins 10 0 10 0", "[]", "[]20[]20[]" );
		this.statementPanel = new StatementAxisPanel();
		this.detailPanel = new StatementAxisPanel();
		this.collapseExpandLabel = new CollapseExpandLabel( composite.getShowDetailText(), composite.getHideDetailText(), false, this.detailPanel );
		this.collapseExpandLabel.setForegroundColor( java.awt.Color.WHITE );

		addComponent( this.statementPanel, "cell 0 0, center, grow" );
		addComponent( this.collapseExpandLabel, "cell 0 1, growx, gapleft 10" );
		addComponent( this.detailPanel, "cell 0 2, grow, hidemode 1, push" );

		this.collapseExpandLabel.setVisible( false );

		this.hasAddedStatement = false;
		this.executeStatementBaseColor = new java.awt.Color( 60, 121, 147 );
	}

	@Override
	public edu.wustl.lookingglass.lgpedia.LgpediaComposite getComposite() {
		return (edu.wustl.lookingglass.lgpedia.LgpediaComposite)super.getComposite();
	}

	public void setExecuteStatementBaseColor( java.awt.Color executeStatementBaseColor ) {
		this.executeStatementBaseColor = executeStatementBaseColor;
	}

	@Override
	protected javax.swing.JPanel createJPanel() {
		return new javax.swing.JPanel() {

			@Override
			protected void paintComponent( java.awt.Graphics g ) {
				edu.wustl.lookingglass.lgpedia.ShadowGradientPainter painter = new edu.wustl.lookingglass.lgpedia.ShadowGradientPainter();
				painter.paint( (java.awt.Graphics2D)g, this, getWidth(), getHeight() );
			}
		};
	}

	public void addParameterAnnotation( org.lgna.project.ast.AbstractDeclaration declaration, org.lgna.project.ast.AbstractMethod method, boolean isDetail ) {
		String description = LgpediaAnnotationFormatter.getInstance().getDescriptionForMethodDeclaration( declaration, method );
		org.lgna.croquet.views.Label label = new org.lgna.croquet.views.Label( description );
		label.setForegroundColor( java.awt.Color.WHITE );

		addVerticalStrut( isDetail );
		addComponentToPanel( label, isDetail );
	}

	public void addStatement( org.lgna.project.ast.Statement statement, boolean addMouseEvents, boolean isDetail ) {
		org.lgna.croquet.views.AwtComponentView<?> component = createStatementComponent( statement, addMouseEvents );
		addComponentToPanel( component, isDetail );
		this.hasAddedStatement = this.hasAddedStatement ? true : !isDetail;
	}

	public void scrollToSelection( boolean scrollToDetails ) {
		if( scrollToDetails ) {
			this.detailPanel.scrollToVisible();
		} else {
			this.statementPanel.scrollToVisible();
		}
	}

	public void resetPanel() {
		synchronized( this.statementPanel.getTreeLock() ) {
			this.statementPanel.removeAllComponents();
		}
		synchronized( this.detailPanel.getTreeLock() ) {
			this.detailPanel.removeAllComponents();
		}

		synchronized( this.collapseExpandLabel.getTreeLock() ) {
			this.collapseExpandLabel.setVisible( false );
		}
		this.collapseExpandLabel.collapse();
		this.hasAddedStatement = false;
	}

	public void setDetailsVisible() {
		this.collapseExpandLabel.expand();
		synchronized( this.collapseExpandLabel.getTreeLock() ) {
			this.collapseExpandLabel.setVisible( true );
		}
	}

	private void addVerticalStrut( boolean isDetail ) {
		if( isDetail ) {
			if( this.hasAddedStatement ) {
				if( this.detailPanel.getComponentCount() > 0 ) {
					synchronized( this.detailPanel.getTreeLock() ) {
						this.detailPanel.addComponent( org.lgna.croquet.views.BoxUtilities.createVerticalSliver( 15 ) );
					}
				}
				return;
			}
		}
		if( this.statementPanel.getComponentCount() > 0 ) {
			synchronized( this.statementPanel.getTreeLock() ) {
				this.statementPanel.addComponent( org.lgna.croquet.views.BoxUtilities.createVerticalSliver( 15 ) );
			}
		}
	}

	private void addComponentToPanel( org.lgna.croquet.views.AwtComponentView<?> component, boolean isDetail ) {
		component = new org.lgna.croquet.views.LineAxisPanel( org.lgna.croquet.views.BoxUtilities.createHorizontalSliver( 10 ), component );
		if( isDetail ) {
			if( hasAddedStatement ) {
				synchronized( this.detailPanel.getTreeLock() ) {
					this.detailPanel.addComponent( component );
				}
				synchronized( this.collapseExpandLabel.getTreeLock() ) {
					this.collapseExpandLabel.setVisible( true );
				}
				return;
			}
		}
		synchronized( this.statementPanel.getTreeLock() ) {
			this.statementPanel.addComponent( component );
		}
	}

	private org.lgna.croquet.views.AwtComponentView<?> createStatementComponent( org.lgna.project.ast.Statement statement, boolean addMouseEvents ) {
		org.alice.ide.common.AbstractStatementPane statementPane = LgpediaAstI18nFactory.getInstance().createStatementPane( statement, addMouseEvents );
		edu.wustl.lookingglass.lgpedia.ExecuteStatementOperation executeOperation = new edu.wustl.lookingglass.lgpedia.ExecuteStatementOperation( statement );

		org.lgna.croquet.views.AwtComponentView<?> rv;
		if( addMouseEvents ) {
			statementPane.setCursor( new java.awt.Cursor( java.awt.Cursor.HAND_CURSOR ) );
			statementPane.getAwtComponent().addMouseListener( new java.awt.event.MouseAdapter() {
				@Override
				public void mouseReleased( java.awt.event.MouseEvent e ) {
					if( getComposite().peekExecutionOperation() == executeOperation ) {
						//pass
					} else {
						getComposite().executeStatement( statement );
						getComposite().pushExecutionOperation( executeOperation );
					}
				}
			} );
			org.lgna.croquet.views.Button executionButton = executeOperation.createButton();
			( (ExecuteStatementButton)executionButton ).setBaseColor( this.executeStatementBaseColor );
			executionButton.setCursor( new java.awt.Cursor( java.awt.Cursor.HAND_CURSOR ) );
			executionButton.tightenUpMargin( new java.awt.Insets( 2, 2, 2, 2 ) );

			executionButton.getAwtComponent().addPropertyChangeListener( new java.beans.PropertyChangeListener() {
				@Override
				public void propertyChange( PropertyChangeEvent e ) {
					if( e.getPropertyName().contentEquals( "enabled" ) ) {
						Boolean value = (Boolean)e.getNewValue();
						if( value ) {
							statementPane.setCursor( new java.awt.Cursor( java.awt.Cursor.HAND_CURSOR ) );
						} else {
							statementPane.setCursor( new java.awt.Cursor( java.awt.Cursor.DEFAULT_CURSOR ) );
						}
					}
				}

			} );

			rv = new org.lgna.croquet.views.LineAxisPanel( executionButton, org.lgna.croquet.views.BoxUtilities.createHorizontalSliver( 5 ), statementPane );
		} else {
			rv = new org.lgna.croquet.views.LineAxisPanel( org.lgna.croquet.views.BoxUtilities.createHorizontalSliver( 28 ), statementPane );
		}
		return rv;
	}

	private class StatementAxisPanel extends org.lgna.croquet.views.PageAxisPanel {
		@Override
		protected int getBoxLayoutPad() {
			return 5;
		}
	}

}
