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

package org.alice.ide.member;

/**
 * @author Dennis Cosgrove
 */
public abstract class MethodsSubComposite extends org.lgna.croquet.ToolPaletteCoreComposite<org.alice.ide.member.views.MethodsSubView<?>> {
	public MethodsSubComposite( java.util.UUID migrationId, boolean isExpandedInitialValue ) {
		super( migrationId, org.lgna.croquet.Application.DOCUMENT_UI_GROUP, MemberTabComposite.getExpandedAccountingForInert( isExpandedInitialValue ) );
	}

	public abstract java.util.List<? extends org.lgna.project.ast.AbstractMethod> getMethods();

	protected boolean isMethodCountDesired( boolean isExpanded, int methodCount ) {
		return isExpanded == false;
	}

	@Override
	protected String modifyTextIfNecessary( String text, boolean isExpanded ) {
		text = super.modifyTextIfNecessary( text, isExpanded );
		java.util.List<? extends org.lgna.project.ast.AbstractMethod> methods = this.getMethods();
		if( this.isMethodCountDesired( isExpanded, methods.size() ) ) {
			text += " (" + methods.size() + ")";
		}
		return text;
	}

	public void updateTabTitle() {
		org.lgna.croquet.BooleanState isExpandedState = this.getOuterComposite().getIsExpandedState();
		isExpandedState.updateNameAndIcon();
	}

	public boolean isShowingDesired() {
		return true;
	}

	@Override
	protected org.lgna.croquet.views.ScrollPane createScrollPaneIfDesired() {
		return null;
	}

	// <lg> Looking Glass needs this for the Remixable Actions composite
	public boolean isRelevant() {
		return true;
	}

	org.lgna.croquet.views.Label titleLabel;

	public org.lgna.croquet.views.Label getTitleLabel() {
		if( this.titleLabel == null ) {
			this.titleLabel = new org.lgna.croquet.views.Label( this.getOuterComposite().getIsExpandedState().getTrueText(), edu.cmu.cs.dennisc.java.awt.font.TextPosture.OBLIQUE );
		}
		return this.titleLabel;
	}

	// </lg>
}