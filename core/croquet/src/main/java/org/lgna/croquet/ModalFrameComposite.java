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
package org.lgna.croquet;

import java.util.LinkedList;
import java.util.List;

import org.lgna.croquet.views.Frame;

import edu.cmu.cs.dennisc.java.util.logging.Logger;

/**
 * @author Dennis Cosgrove
 */
public abstract class ModalFrameComposite<V extends org.lgna.croquet.views.CompositeView<?, ?>> extends AbstractWindowComposite<V> implements OperationOwningComposite<V> {
	public ModalFrameComposite( java.util.UUID id, Group launchOperationGroup ) {
		super( id );
		this.imp = new org.lgna.croquet.imp.dialog.LaunchOperationOwningCompositeImp<ModalFrameComposite<V>>( this, launchOperationGroup );
	}

	@Override
	protected void localize() {
		super.localize();
		this.title = this.findLocalizedText( "title" );
	}

	protected org.lgna.croquet.imp.dialog.LaunchOperationOwningCompositeImp<ModalFrameComposite<V>> getImp() {
		return this.imp;
	}

	public org.lgna.croquet.Operation getLaunchOperation( String subKeyText ) {
		return this.imp.getLaunchOperation( subKeyText );
	}

	@Override
	public String modifyNameIfNecessary( OwnedByCompositeOperationSubKey subKey, String text ) {
		return text;
	}

	protected void handlePreShowWindow( org.lgna.croquet.views.Frame frame ) {
	}

	protected void handlePreHideWindow( org.lgna.croquet.views.Frame frame ) {
	}

	protected void handlePostHideWindow( org.lgna.croquet.views.Frame frame ) {
	}

	protected void handleFinally() {
	}

	@Override
	public boolean isSubTransactionHistoryRequired() {
		return false;
	}

	private static final List<ModalFrameComposite<?>> nonModalComposites = new LinkedList<>();

	public static void hideNonModalFrames() {
		for( ModalFrameComposite<?> composite : nonModalComposites ) {
			Frame frame = composite.getCurrentFrame();
			if( frame != null ) {
				frame.close();
			}
		}
	}

	private boolean isModal = true;

	// Please note that is honestly quite a reasonable feature,
	// however, because of Croquet's design it's actually a
	// hack. If you use this, please note your transaction history
	// will get messed up. But it's already so messed up (e.g.
	// list states and the setTransactionslessly...)
	// so honestly this isn't really going to matter.
	protected void setModal( boolean isModal ) {
		this.isModal = isModal;

		if( isModal ) {
			nonModalComposites.remove( this );
		} else {
			if( !nonModalComposites.contains( this ) ) {
				nonModalComposites.add( this );
			}
		}
	}

	private org.lgna.croquet.views.Frame currentFrame = null;

	public Frame getCurrentFrame() {
		return this.currentFrame;
	}

	private boolean shouldReuseFrame = false;

	protected void setReuseFrame( boolean shouldReuseFrame ) {
		this.shouldReuseFrame = shouldReuseFrame;
	}

	@Override
	public void perform( org.lgna.croquet.OwnedByCompositeOperationSubKey subKey, org.lgna.croquet.history.CompletionStep<?> step ) {
		final java.util.List<org.lgna.croquet.views.Frame> framesToDiable = edu.cmu.cs.dennisc.java.util.Lists.newLinkedList();

		org.lgna.croquet.Application<?> application = org.lgna.croquet.Application.getActiveInstance();
		DocumentFrame documentFrame = application.getDocumentFrame();

		if( this.shouldReuseFrame && ( this.currentFrame != null ) ) {
			this.handlePreShowWindow( this.currentFrame );
			this.currentFrame.setVisible( true );
			this.currentFrame.getAwtComponent().toFront();
		} else {
			final org.lgna.croquet.views.Frame frame = new org.lgna.croquet.views.Frame();
			class ModalFrameWindowListener implements java.awt.event.WindowListener {
				@Override
				public void windowOpened( java.awt.event.WindowEvent e ) {
					for( org.lgna.croquet.views.Frame frame : framesToDiable ) {
						frame.getAwtComponent().setEnabled( false );
					}
				}

				@Override
				public void windowClosing( java.awt.event.WindowEvent e ) {
					if( isWindowClosingEnabled( org.lgna.croquet.triggers.WindowEventTrigger.createUserInstance( e ) ) ) {
						for( org.lgna.croquet.views.Frame frame : framesToDiable ) {
							frame.getAwtComponent().setEnabled( true );
						}
						currentFrame = null;
						//e.getComponent().setVisible( false );

						// Work-around for race condition with JFXPanel
						// https://javafx-jira.kenai.com/browse/RT-39881
						try {
							handlePreHideWindow( frame );
							frame.getAwtComponent().dispose();
						} catch( NullPointerException npe ) {
							Logger.throwable( npe, this );
						}
					}
				}

				@Override
				public void windowClosed( java.awt.event.WindowEvent e ) {
					frame.removeWindowListener( this );
					try {
						handlePostHideWindow( frame );
					} finally {
						handleFinally();
					}
				}

				@Override
				public void windowActivated( java.awt.event.WindowEvent e ) {
				}

				@Override
				public void windowDeactivated( java.awt.event.WindowEvent e ) {
				}

				@Override
				public void windowDeiconified( java.awt.event.WindowEvent e ) {
				}

				@Override
				public void windowIconified( java.awt.event.WindowEvent e ) {
				}
			}

			ModalFrameWindowListener windowListener = new ModalFrameWindowListener();
			frame.setDefaultCloseOperation( org.lgna.croquet.views.Frame.DefaultCloseOperation.DO_NOTHING );
			frame.addWindowListener( windowListener );
			V view = this.getView();
			frame.getAwtComponent().setContentPane( view.getAwtComponent() );

			this.updateWindowSize( frame );
			final int OFFSET = 32;
			java.awt.Point p = documentFrame.getFrame().getLocation();
			frame.setLocation( p.x + OFFSET, p.y + OFFSET );
			frame.setTitle( this.getModalFrameTitle() );
			this.handlePreShowWindow( frame );
			frame.setVisible( true );

			this.currentFrame = frame;
		}

		//			dialogOwner.handlePreShowDialog( step );
		//			//application.pushWindow( dialog );
		//			dialog.setVisible( true );
		//
		//			if( isModal ) {
		//				dialogOwner.handlePostHideDialog( step );
		//				dialog.removeWindowListener( dialogWindowListener );
		//				dialogOwner.releaseView( step, view );
		//				dialog.getAwtComponent().dispose();
		//			} else {
		//				edu.cmu.cs.dennisc.java.util.logging.Logger.outln( "todo: handle non-modal dialogs" );
		//			}
		//		} finally {
		//			if( isModal ) {
		//				//application.popWindow();
		//				dialogOwner.handleFinally( step, dialog );
		//			} else {
		//				edu.cmu.cs.dennisc.java.util.logging.Logger.outln( "todo: handle non-modal dialogs" );
		//			}
		//		}
	}

	protected abstract String getName();

	protected String getModalFrameTitle() {
		this.initializeIfNecessary();
		String rv = this.title;
		if( rv != null ) {
			//pass
		} else {
			rv = this.getName();
			if( rv != null ) {
				rv = rv.replaceAll( "<[a-z]*>", "" );
				rv = rv.replaceAll( "</[a-z]*>", "" );
				if( rv.endsWith( "..." ) ) {
					rv = rv.substring( 0, rv.length() - 3 );
				}
			}
		}
		return rv;
	}

	protected boolean isWindowClosingEnabled( org.lgna.croquet.triggers.WindowEventTrigger trigger ) {
		return true;
	}

	private final org.lgna.croquet.imp.dialog.LaunchOperationOwningCompositeImp<ModalFrameComposite<V>> imp;
	private String title;
}
