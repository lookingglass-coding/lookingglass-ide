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
package edu.wustl.lookingglass.ide.views;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.lgna.croquet.views.AbstractWindow;
import org.lgna.croquet.views.LayerStencil;

/**
 * @author Kyle J. Harms
 */
public class OpaqueLayer extends LayerStencil {

	private static final int DEFAULT_LAYER_ID = javax.swing.JLayeredPane.DRAG_LAYER - 1;
	private static final Color DEFAULT_COLOR = UIManager.getColor( "control" );

	private final Color color;

	public OpaqueLayer( AbstractWindow<?> window ) {
		this( DEFAULT_COLOR, window );
	}

	public OpaqueLayer( Color color, AbstractWindow<?> window ) {
		this( color, window, DEFAULT_LAYER_ID );
	}

	public OpaqueLayer( Color color, AbstractWindow<?> window, int layerId ) {
		super( window, layerId );
		this.color = color;
	}

	@Override
	protected void paintComponentPrologue( Graphics2D g2 ) {
		Graphics2D graphics = (Graphics2D)g2.create();
		graphics.setColor( this.color );
		graphics.fill( graphics.getClip() );
	}

	@Override
	protected void paintComponentEpilogue( Graphics2D g2 ) {
	}

	@Override
	protected void paintEpilogue( Graphics2D g2 ) {
	}

	@Override
	protected boolean contains( int x, int y, boolean superContains ) {
		return false;
	}

	@Override
	protected LayoutManager createLayoutManager( JPanel jPanel ) {
		return null;
	}
}
