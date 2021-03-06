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
package org.alice.ide;

/**
 * @author Dennis Cosgrove
 */
public interface Theme {
	public static final java.awt.Dimension DEFAULT_SMALLER_ICON_SIZE = org.lgna.croquet.icon.IconSize.ALICE_WIDE_SMALLER.getSize();
	public static final java.awt.Dimension DEFAULT_SMALL_ICON_SIZE = org.lgna.croquet.icon.IconSize.ALICE_WIDE_SMALL.getSize();
	public static final java.awt.Dimension DEFAULT_LARGE_ICON_SIZE = org.lgna.croquet.icon.IconSize.ALICE_WIDE_LARGE.getSize();

	//<lg>
	public java.awt.Color getProcedureStatementColor();

	public java.awt.Color getFunctionStatementColor();

	public java.awt.Color getControlFlowStatementColor();

	public java.awt.Color getDefaultNounColor();

	//</lg>

	public java.awt.Color getTypeColor();

	public java.awt.Color getMutedTypeColor();

	public java.awt.Color getProcedureColor();

	public java.awt.Color getFunctionColor();

	public java.awt.Color getConstructorColor();

	public java.awt.Color getFieldColor();

	public java.awt.Color getLocalColor();

	public java.awt.Color getParameterColor();

	public java.awt.Color getEventColor();

	public java.awt.Color getEventBodyColor();

	public java.awt.Paint getPaintFor( Class<? extends org.lgna.project.ast.Statement> cls, int x, int y, int width, int height );

	public java.awt.Color getColorFor( Class<? extends org.lgna.project.ast.Node> cls );

	public java.awt.Color getColorFor( org.lgna.project.ast.Node node );

	public java.awt.Color getCommentForegroundColor();

	public java.awt.Color getCodeColor( org.lgna.project.ast.Code code );

	public java.awt.Color getSelectedColor();

	public java.awt.Color getUnselectedColor();

	public java.awt.Color getPrimaryBackgroundColor();

	public java.awt.Color getSecondaryBackgroundColor();

	default public java.awt.Color getControlFlowColor() {
		return null;
	}
}
