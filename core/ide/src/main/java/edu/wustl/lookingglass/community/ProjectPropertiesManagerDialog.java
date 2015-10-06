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
package edu.wustl.lookingglass.community;

import java.io.IOException;

import org.lgna.croquet.SimpleComposite;
import org.lgna.croquet.StringState;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;
import org.lgna.croquet.views.Dialog;
import org.lgna.croquet.views.Label;
import org.lgna.croquet.views.MigPanel;
import org.lgna.project.Project;

import edu.cmu.cs.dennisc.java.net.UriUtilities;
import edu.wustl.lookingglass.ide.LookingGlassIDE;

/**
 * @author Michael Pogran
 */
public class ProjectPropertiesManagerDialog extends Dialog {

	public ProjectPropertiesManagerDialog() {
		updateDialog();
	}

	private void updateDialog() {
		if( LookingGlassIDE.getActiveInstance().getProject() != null ) {
			synchronized( getContentPane().getTreeLock() ) {
				getContentPane().getAwtComponent().removeAll();
				getContentPane().addCenterComponent( new PropertiesManagerComposite().getView() );
			}
		}
	}

	public org.lgna.croquet.Operation getLaunchDialogOperation() {
		return this.launchDialogOperation;
	}

	@Override
	public void setVisible( boolean isVisible ) {
		if( isVisible ) {
			updateDialog();
		}
		super.setVisible( isVisible );
	}

	public class PropertiesManagerComposite extends SimpleComposite<MigPanel> {
		StringState idState = this.createStringState( "id" );
		StringState titleState = this.createStringState( "title" );
		StringState descriptionState = this.createStringState( "description" );
		StringState userIdState = this.createStringState( "userId" );

		public PropertiesManagerComposite() {
			super( java.util.UUID.fromString( "c3365bc6-dcde-4acd-81de-54a7982b3f2f" ) );
			Project project = LookingGlassIDE.getActiveInstance().getProject();
			if( project != null ) {
				String id = CommunityProjectPropertyManager.getCommunityProjectID( project ) == null ? "" : CommunityProjectPropertyManager.getCommunityProjectID( project ).toString();
				String userId = CommunityProjectPropertyManager.getProjectUserID( project ) == null ? "" : CommunityProjectPropertyManager.getProjectUserID( project ).toString();

				setIdState( id );
				setUserIdState( userId );
				setTitleState( CommunityProjectPropertyManager.getProjectTitle( project ) );
				setDescriptionState( CommunityProjectPropertyManager.getProjectDescription( project ) );
			}
		}

		@Override
		protected MigPanel createView() {
			MigPanel panel = new MigPanel( null, "fill", "[][50]", "[][][100][][]" );

			panel.addComponent( new Label( "Community ID:" ), "cell 0 0" );
			panel.addComponent( this.idState.createTextField(), "cell 1 0, grow" );
			panel.addComponent( new Label( "Title:" ), "cell 0 1" );
			panel.addComponent( this.titleState.createTextField(), "cell 1 1, grow" );
			panel.addComponent( new Label( "Description:" ), "cell 0 2" );
			panel.addComponent( this.descriptionState.createTextArea(), "cell 1 2, grow" );
			panel.addComponent( new Label( "User ID:" ), "cell 0 3" );
			panel.addComponent( this.userIdState.createTextField(), "cell 1 3, grow" );

			panel.addComponent( this.savePropertiesOperation.createButton(), "cell 0 4, spanx 2, pushy, alignx center, align y top" );

			return panel;
		}

		public void setIdState( String value ) {
			this.idState.setValueTransactionlessly( value );
		}

		public void setTitleState( String value ) {
			this.titleState.setValueTransactionlessly( value );
		}

		public void setDescriptionState( String value ) {
			this.descriptionState.setValueTransactionlessly( value );
		}

		public void setUserIdState( String value ) {
			this.userIdState.setValueTransactionlessly( value );
		}

		org.lgna.croquet.Operation savePropertiesOperation = new org.lgna.croquet.Operation( org.lgna.croquet.Application.PROJECT_GROUP, java.util.UUID.fromString( "14b68df7-e283-41a2-800c-c283df478c0d" ) ) {
			@Override
			protected void perform( Transaction transaction, Trigger trigger ) {
				Project project = LookingGlassIDE.getActiveInstance().getProject();
				if( project != null ) {
					if( idState.getValue().isEmpty() ) {
						CommunityProjectPropertyManager.setCommunityProjectID( project, null );
					} else {
						CommunityProjectPropertyManager.setCommunityProjectID( project, Integer.parseInt( idState.getValue() ) );
					}

					if( userIdState.getValue().isEmpty() ) {
						CommunityProjectPropertyManager.setProjectUserID( project, null );
					} else {
						CommunityProjectPropertyManager.setProjectUserID( project, Integer.parseInt( userIdState.getValue() ) );
					}

					if( titleState.getValue().isEmpty() ) {
						CommunityProjectPropertyManager.setProjectTitle( project, null );
					} else {
						CommunityProjectPropertyManager.setProjectTitle( project, titleState.getValue() );
					}

					if( descriptionState.getValue().isEmpty() ) {
						CommunityProjectPropertyManager.setProjectDescription( project, null );
					} else {
						CommunityProjectPropertyManager.setProjectDescription( project, descriptionState.getValue() );
					}
				}
				try {
					LookingGlassIDE.getActiveInstance().saveProjectTo( UriUtilities.getFile( LookingGlassIDE.getActiveInstance().getUri() ) );
				} catch( IOException e ) {
					e.printStackTrace();
				}
			}

			@Override
			protected void localize() {
				this.setName( "Save Properties" );
			}
		};
	}

	org.lgna.croquet.Operation launchDialogOperation = new org.lgna.croquet.Operation( org.lgna.croquet.Application.PROJECT_GROUP, java.util.UUID.fromString( "14b68df7-e283-41a2-800c-c283df478c0d" ) ) {
		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			getAwtComponent().setModal( false );
			pack();
			setSize( 400, 300 );
			setVisible( true );
		}

		@Override
		protected void localize() {
			this.setName( "View Community Properties" );
		}
	};
}
