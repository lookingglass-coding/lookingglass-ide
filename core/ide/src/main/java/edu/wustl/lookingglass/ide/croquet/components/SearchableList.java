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
package edu.wustl.lookingglass.ide.croquet.components;

import org.lgna.croquet.SingleSelectListState;
import org.lgna.croquet.data.ListData;
import org.lgna.croquet.views.List;

import edu.wustl.lookingglass.ide.croquet.models.community.data.CommunityListData;

/**
 * @author Michael Pogran
 */
public class SearchableList<T> extends List<T> {

	public SearchableList( SingleSelectListState<T, ListData<T>> model ) {
		super( model );
		assert model.getData() instanceof CommunityListData : model.getData();
	}

	private CommunityListData getData() {
		return (CommunityListData)getModel().getData();
	}

	private String generateEmptySearchString() {
		if( getData().isSearchSet() ) {
			return "No results found for " + getData().getSearchQuery();
		} else {
			return getModel().getEmptyConditionText().getText();
		}
	}

	@Override
	protected javax.swing.JList createAwtComponent() {
		return new JDefaultSearcableList();
	}

	private class JDefaultSearcableList extends javax.swing.JList {
		@Override
		protected void paintComponent( java.awt.Graphics g ) {
			super.paintComponent( g );
			if( getModel().getSize() == 0 ) {
				EmptySearchPainter<T> painter = new EmptySearchPainter<T>();
				painter.paint( (java.awt.Graphics2D)g, SearchableList.this, this.getWidth(), this.getHeight() );
			} else {
				super.paintComponent( g );
			}
		}
	}

	private class EmptySearchPainter<T> implements edu.cmu.cs.dennisc.java.awt.Painter<List<T>> {
		private final java.util.Map<java.awt.font.TextAttribute, Object> mapDeriveFont;

		public EmptySearchPainter() {
			mapDeriveFont = edu.cmu.cs.dennisc.java.util.Maps.newHashMap();
			mapDeriveFont.put( java.awt.font.TextAttribute.POSTURE, java.awt.font.TextAttribute.POSTURE_OBLIQUE );
			mapDeriveFont.put( java.awt.font.TextAttribute.WEIGHT, java.awt.font.TextAttribute.WEIGHT_LIGHT );
		}

		@Override
		public void paint( java.awt.Graphics2D g2, List<T> listView, int width, int height ) {
			String text = generateEmptySearchString();
			if( ( text != null ) && ( text.length() > 0 ) ) {
				edu.cmu.cs.dennisc.java.awt.GraphicsUtilities.setRenderingHint( g2, java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
				g2.setPaint( java.awt.Color.DARK_GRAY );
				g2.setFont( g2.getFont().deriveFont( mapDeriveFont ) );
				final int OFFSET = 4;
				g2.drawString( text, OFFSET, OFFSET + g2.getFontMetrics().getAscent() );
			}
		}
	}
}
