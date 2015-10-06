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
package edu.wustl.lookingglass.croquetfx;

/**
 * @author Kyle J. Harms
 */
@Deprecated
public class FxCompositeAdaptor<C extends FxComponent> implements org.lgna.croquet.Composite<FxViewAdaptor<C>> {

	private C fxComponent;

	private FxViewAdaptor<C> fxView;

	/**
	 * You should try to follow the pattern of extending FxComponent and then
	 * calling getFxViewAdaptor().
	 *
	 * However, sometimes, croquet makes this too difficult to deal with.
	 * Croquet must always run within the swing (EDT) thread. Croquet makes it
	 * very hard to run stuff in the JavaFx thread. So if you need to play in
	 * Croquet land, because it's forcing you to use a composite, but you want
	 * to use JavaFx this will let you create a composite that will later create
	 * the FxComponent in the JavaFx thread. Please note that this method may
	 * have resizing issues because of the way JFXPanel sets its preferred size
	 * on the scene. The scene in this case may not be create once the
	 * composite's view is shown, because the scene is created in another
	 * thread.
	 *
	 * Note: This can be called from any thread, although it should probably be
	 * called from the swing (EDT) thread.
	 *
	 * @param createComponentLater a lambda that returns your JavaFx
	 *            FxComponent. For example:
	 *            <code>() -> { new MyFxPane(); }</code>
	 */
	public FxCompositeAdaptor( final java.util.concurrent.Callable<C> createComponentLater ) {
		this.fxView = new FxViewAdaptor<C>( this );
		ThreadHelper.runOnFxThread( ( ) -> {
			try {
				this.fxComponent = createComponentLater.call();
				this.fxView.setComponent( this.fxComponent );
			} catch( Exception e ) {
				throw new RuntimeException( e );
			}
		} );
	}

	/**
	 * This is called by the FxViewAdaptor to create the default composite for
	 * FxComponent. This is used when you don't need to mess with composites.
	 *
	 * This is the preferred option if possible.
	 */
	FxCompositeAdaptor() {
	}

	void setView( FxViewAdaptor<C> view ) {
		this.fxView = view;
	}

	public C getFxComponent() {
		return this.fxComponent;
	}

	@Override
	public java.util.UUID getMigrationId() {
		return null;
	}

	@Override
	public void initializeIfNecessary() {
	}

	@Override
	public void relocalize() {
	}

	@Override
	public void appendUserRepr( StringBuilder sb ) {
		sb.append( "FxCompositeAdaptor: " + this.fxView );
	}

	@Override
	public java.util.UUID getCardId() {
		return null;
	}

	@Override
	public FxViewAdaptor<C> getView() {
		return this.fxView;
	}

	@Override
	public org.lgna.croquet.views.ScrollPane getScrollPaneIfItExists() {
		return null;
	}

	@Override
	public org.lgna.croquet.views.SwingComponentView<?> getRootComponent() {
		return this.getView();
	}

	@Override
	public void releaseView() {
		this.fxComponent = null;
		this.fxView = null;
	}

	@Override
	public void handlePreActivation() {
		this.fxView.handleCompositePreActivation();
	}

	@Override
	public void handlePostDeactivation() {
		this.fxView.handleCompositePostDeactivation();
	}

	@Override
	public boolean contains( org.lgna.croquet.Model model ) {
		// TODO
		assert false : "todo";
		return false;
	}
}
