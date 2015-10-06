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
package edu.wustl.lookingglass.ide.perspectives.dinah.finder.rightnowtree;

import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.lgna.croquet.icon.IconFactory;
import org.lgna.croquet.icon.IconSize;
import org.lgna.croquet.views.BoxUtilities;
import org.lgna.croquet.views.LineAxisPanel;

import edu.wustl.lookingglass.ide.croquet.components.CircleIcon;
import edu.wustl.lookingglass.ide.perspectives.dinah.DinahAstI18nFactory;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ExpressionStatementEventNode;

public class ExecutionTraceTreeRenderer extends DefaultTreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus ) {

		if( value instanceof StatementTreeNode ) {
			StatementTreeNode treeNode = (StatementTreeNode)value;
			ExpressionStatementEventNode eventNode = treeNode.getEventNode();

			org.lgna.croquet.views.Label label = new org.lgna.croquet.views.Label();
			label.setText( eventNode.getAstNode().getRepr() );

			JPanel panel = new LineAxisPanel(
					DinahAstI18nFactory.getInstance().createExecutionTraceStatementPane( (org.lgna.project.ast.Statement)eventNode.getAstNode(), sel, eventNode.getCallerField() ),
					//label,
					BoxUtilities.createHorizontalGlue()
					).getAwtComponent();
			panel.setBorder( javax.swing.BorderFactory.createEmptyBorder( 2, 0, 0, 0 ) );
			return panel;

		} else if( value instanceof FieldTreeNode ) {
			FieldTreeNode treeNode = (FieldTreeNode)value;

			IconFactory factory = org.alice.stageide.icons.IconFactoryManager.getIconFactoryForField( treeNode.getField() );
			if( factory == null ) {
				factory = org.lgna.croquet.icon.EmptyIconFactory.getInstance();
			}

			JLabel label = new javax.swing.JLabel();
			Icon icon = new CircleIcon( factory.getIcon( IconSize.SMALL.getSize() ), new java.awt.Color( 32, 175, 37 ) );
			label.setIcon( icon );

			label.setText( value.toString() );
			label.setBackground( null );
			label.setForeground( new java.awt.Color( 173, 167, 208 ).darker().darker().darker().darker() );
			label.setIconTextGap( 10 );
			label.setFont( new Font( null, Font.BOLD, 16 ) );
			label.setBorder( javax.swing.BorderFactory.createEmptyBorder( 8, 4, 8, 4 ) );
			return label;
		} else {
			return super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );
		}
	}
}
