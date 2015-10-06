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
package edu.wustl.lookingglass.ide.perspectives.dinah.processbar.views;

import java.awt.Color;
import java.awt.Dimension;

import org.alice.ide.x.PreviewAstI18nFactory;
import org.lgna.croquet.views.BoxUtilities;
import org.lgna.croquet.views.Button;
import org.lgna.croquet.views.HtmlMultiLineLabel;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.LineAxisPanel;
import org.lgna.croquet.views.Separator;
import org.lgna.project.ast.Statement;

import edu.cmu.cs.dennisc.java.awt.font.TextPosture;
import edu.cmu.cs.dennisc.java.awt.font.TextWeight;
import edu.wustl.lookingglass.ide.croquet.components.StatementSelectionIcon;
import edu.wustl.lookingglass.ide.croquet.components.ToggleIcon;
import edu.wustl.lookingglass.ide.perspectives.dinah.processbar.GuideStepPanel;
import edu.wustl.lookingglass.ide.perspectives.dinah.processbar.SelectEndCard;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.remix.models.EndCaptureState;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;

/**
 * @author Michael Pogran
 */
public class SelectEndView extends GuideStepPanel {

	private LineAxisPanel startStatementPanel;
	private LineAxisPanel endStatementPanel;
	private Label stateLabel;

	public SelectEndView( SelectEndCard composite ) {
		this.stateLabel = new Label();
		this.setStateLabel( "Incomplete", Color.RED );

		addComponent( this.stateLabel, "cell 0 0, center" );
		addComponent( Separator.createInstanceSeparatingTopFromBottom(), "cell 0 1, growx" );

		Dimension iconSize = new Dimension( 32, 32 );
		Label buttonLabel = new Label( new ToggleIcon( EndCaptureState.getInstance(), new StatementSelectionIcon( iconSize, false, true ), new StatementSelectionIcon( iconSize, false, false ) ) );

		HtmlMultiLineLabel stepHelpLabel = new HtmlMultiLineLabel( getHelpText(), 1.15f );
		stepHelpLabel.setBackgroundColor( null );

		addComponent( buttonLabel, "cell 0 2" );
		addComponent( stepHelpLabel, "cell 0 2, grow" );
		addComponent( Separator.createInstanceSeparatingTopFromBottom(), "cell 0 3, growx" );

		this.startStatementPanel = new LineAxisPanel( new Label( "No start selected", TextPosture.OBLIQUE, TextWeight.BOLD ) );
		this.endStatementPanel = new LineAxisPanel( new Label( "No end selected", TextPosture.OBLIQUE, TextWeight.BOLD ) );

		addComponent( new Label( "Selected Actions:", 1.25f, TextWeight.BOLD ), "cell 0 4" );
		addComponent( new Label( "Remix Start:" ), "cell 0 5" );
		addComponent( this.startStatementPanel, "cell 0 6" );

		addComponent( new Label( "Remix End:" ), "cell 0 7" );
		addComponent( this.endStatementPanel, "cell 0 8" );
		addComponent( Separator.createInstanceSeparatingTopFromBottom(), "cell 0 9, growx" );

		Button previewButton = composite.getPreviewOperation().createButton();
		Button prevButton = composite.getParentComposite().getShowPreviousCardOperation().createButton();
		Button shareOrUseButton = composite.getParentComposite().getShareOrUseOperation().createButton();

		addComponent( prevButton, "cell 0 10, left" );
		addComponent( previewButton, "cell 0 10, left" );
		addComponent( shareOrUseButton, "cell 0 11, left, top, push" );
	}

	protected String getHelpText() {
		StringBuilder sb = new StringBuilder();
		sb.append( "<p style='font-family: sans-serif;'>" );
		sb.append( "Again, find the " );
		sb.append( "<span style='font-weight:bold;'>" );
		sb.append( "action" );
		sb.append( "</span>" );
		sb.append( " you want to use as the end of your remix, click the " );
		sb.append( "<span style='font-weight:bold;'>" );
		sb.append( "+" );
		sb.append( "</span>" );
		sb.append( " button, and select " );
		sb.append( "<span style='font-weight:bold;'>" );
		sb.append( "Mark action box as remix end" );
		sb.append( "</span>" );
		sb.append( " from the menu." );
		sb.append( "</p>" );

		return sb.toString();
	}

	@Override
	protected void setStateLabel( String text, Color color ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "<html><p style='font-weight:bold;'>" );
		sb.append( "Step 1. Pick An End:" );
		sb.append( "<span style='color:" );
		sb.append( createColorString( color ) );
		sb.append( ";'> " );
		sb.append( text );
		sb.append( "</span></html>" );
		this.stateLabel.setText( sb.toString() );
	}

	@Override
	public void startCaptureStateChange( AbstractEventNode<?> eventNode ) {
		synchronized( this.startStatementPanel.getTreeLock() ) {
			this.startStatementPanel.removeAllComponents();
			if( eventNode != null ) {
				this.startStatementPanel.addComponent( PreviewAstI18nFactory.getInstance().createStatementPane( (Statement)eventNode.getAstNode() ) );
				this.startStatementPanel.addComponent( BoxUtilities.createHorizontalGlue() );
			} else {
				this.startStatementPanel.addComponent( new Label( "No start selected", TextPosture.OBLIQUE, TextWeight.BOLD ) );
			}
			this.startStatementPanel.revalidateAndRepaint();
		}
	}

	@Override
	public void endCaptureStateChange( AbstractEventNode<?> eventNode ) {
		synchronized( this.endStatementPanel.getTreeLock() ) {
			this.endStatementPanel.removeAllComponents();
			if( eventNode != null ) {
				this.endStatementPanel.addComponent( PreviewAstI18nFactory.getInstance().createStatementPane( (Statement)eventNode.getAstNode() ) );
				this.endStatementPanel.addComponent( BoxUtilities.createHorizontalGlue() );
				this.setStateLabel( "Complete", Color.GREEN );
			} else {
				endStatementPanel.addComponent( new Label( "No end selected", TextPosture.OBLIQUE, TextWeight.BOLD ) );
				this.setStateLabel( "Incomplete", Color.RED );
			}
			this.endStatementPanel.revalidateAndRepaint();
		}
	}

	@Override
	public void programStateChange( ProgramStateEvent event ) {
	}

}
