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
package edu.wustl.lookingglass.ide.perspectives.openproject;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.alice.ide.uricontent.UriProjectLoader;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.event.ValueListener;
import org.lgna.croquet.views.MigPanel;
import org.lgna.croquet.views.Panel;

import edu.wustl.lookingglass.ide.perspectives.openproject.projectselectionsource.OpenProjectTab;

/**
 * @author Caitlin Kelleher
 */
public class ProjectDetailsComposite extends org.lgna.croquet.SimpleComposite<Panel> {
	private final SelectedProjectListener projectListener;
	private final SelectedTabListener tabListener;

	private final OpenProjectContentInfoDetailsComposite programDetailsCardComposite;
	private final OpenProjectComposite parentComposite;

	public ProjectDetailsComposite( OpenProjectComposite parentComposite ) {
		super( java.util.UUID.fromString( "ab2b5ecc-e763-4d85-b29e-b6bffd72a131" ) );
		this.parentComposite = parentComposite;

		projectListener = new SelectedProjectListener();
		tabListener = new SelectedTabListener();

		programDetailsCardComposite = new OpenProjectContentInfoDetailsComposite( parentComposite );
		this.registerSubComposite( programDetailsCardComposite );
	}

	@Override
	protected Panel createView() {
		InnerPanel rv = new InnerPanel();
		rv.addComponent( programDetailsCardComposite.getView(), "grow" );
		return rv;
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		// start listening to metastate changes
		this.parentComposite.getMetaState().addAndInvokeValueListener( projectListener );
		//this.parentComposite.getTabState().addAndInvokeNewSchoolValueListener( tabListener );
	}

	@Override
	public void handlePostDeactivation() {
		// stop listening to metastate changes
		this.parentComposite.getMetaState().removeValueListener( projectListener );
		//this.parentComposite.getTabState().removeNewSchoolValueListener( tabListener );
		super.handlePostDeactivation();
	}

	private class SelectedProjectListener implements ValueListener<UriProjectLoader> {

		@Override
		public void valueChanged( ValueEvent<UriProjectLoader> e ) {
			UriProjectLoader nextValue = e.getNextValue();
			programDetailsCardComposite.handleMetaStateValueChanged( nextValue );
		}
	}

	private class SelectedTabListener implements ValueListener<OpenProjectTab> {

		@Override
		public void valueChanged( ValueEvent<OpenProjectTab> e ) {
			programDetailsCardComposite.handleMetaStateValueChanged( parentComposite.getMetaState().getValue() );
		}
	}

	private class InnerPanel extends MigPanel {

		public InnerPanel() {
			super( null, "fill", "[]", "[]" );
		}

		@Override
		protected javax.swing.JPanel createJPanel() {
			javax.swing.JPanel rv = new DefaultJPanel() {

				@Override
				public void paint( java.awt.Graphics g ) {
					int w = this.getWidth();
					int h = this.getHeight();
					Graphics2D g2 = (Graphics2D)g.create();
					g2.setClip( -1, -1, w + 2, h + 2 );
					g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

					java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
					path.moveTo( 0, 0 );
					path.lineTo( 0, h );
					path.lineTo( w - 5, h );
					path.curveTo( w - 5, h, w, h, w, h - 5 );
					path.lineTo( w, 5 );
					path.curveTo( w, 5, w, 0, w - 5, 0 );
					path.closePath();

					g2.setColor( new Color( 211, 215, 240 ) );
					g2.fill( path );

					g2.setColor( new Color( 97, 96, 94 ) );
					g2.setStroke( new BasicStroke( 1 ) );
					g2.draw( path );

					super.paint( g );
				}
			};
			return rv;
		}
	}
}
