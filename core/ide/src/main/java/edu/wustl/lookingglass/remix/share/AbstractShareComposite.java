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
package edu.wustl.lookingglass.remix.share;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.lgna.croquet.Group;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;

import edu.wustl.lookingglass.ide.croquet.models.community.RecordingCreator;

/**
 * @author Caitlin Kelleher
 */
public abstract class AbstractShareComposite extends org.lgna.croquet.SimpleOperationWizardDialogCoreComposite implements RecordingCreator {
	public static java.awt.Color COMPOSITE_COLOR = new java.awt.Color( 201, 201, 218 );

	private final org.lgna.croquet.StringState titleState = this.createStringState( "titleState" );
	private final org.lgna.croquet.StringState descriptionState = this.createStringState( "descriptionState" );
	private final org.lgna.croquet.StringState tagState = this.createStringState( "tagState" );

	private final org.lgna.croquet.BooleanState isUpdateState = this.createBooleanState( "isUpdateState", false );
	private Integer contentId = null;

	private org.lgna.croquet.Operation shareLocalOperation;

	private double frameRate = 24.0;

	protected abstract String getShareDialogTitle();

	protected abstract void shareContent( edu.wustl.lookingglass.ide.croquet.models.community.ShareContentObserver observer );

	protected abstract void updateContent( edu.wustl.lookingglass.ide.croquet.models.community.ShareContentObserver observer, Integer contentId );

	protected abstract void saveContentLocally();

	public abstract File getRecordedVideo();

	private static final float FONT_SCALE = 1.2f;
	private java.awt.Image posterImage = null;

	static org.lgna.croquet.views.ImmutableTextField createSidekickField( org.lgna.croquet.AbstractCompletionModel model ) {
		org.lgna.croquet.views.ImmutableTextField rv = model.getSidekickLabel().createImmutableTextField();
		rv.scaleFont( FONT_SCALE );
		return rv;
	}

	public AbstractShareComposite( UUID migrationId, Group operationGroup ) {
		super( migrationId, operationGroup );
	}

	@Override
	protected boolean isAdornmentDesired() {
		return false;
	}

	@Override
	protected GoldenRatioPolicy getGoldenRatioPolicy() {
		return null;
	}

	@Override
	public void updateIsGoodToGo( boolean isGoodToGo ) {
		super.updateIsGoodToGo( isGoodToGo );
		if( this.shareLocalOperation != null ) {
			this.shareLocalOperation.setEnabled( getCommitOperation().isEnabled() );
		}
	}

	public org.lgna.croquet.StringState getTitleState() {
		return this.titleState;
	}

	public org.lgna.croquet.StringState getDescriptionState() {
		return this.descriptionState;
	}

	public org.lgna.croquet.StringState getTagState() {
		return this.tagState;
	}

	public void setPosterImage( java.awt.Image posterImage ) {
		this.posterImage = posterImage;
	}

	public java.awt.Image getPosterImage() {
		return posterImage;
	}

	public void setContentId( Integer contentId ) {
		this.contentId = contentId;
	}

	public Integer getContentId() {
		return this.contentId;
	}

	public void setIsUpdate( boolean value ) {
		this.isUpdateState.setValueTransactionlessly( value );
	}

	public org.lgna.croquet.BooleanState getIsUpdateState() {
		return this.isUpdateState;
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();

		this.titleState.setValueTransactionlessly( "" );
		this.descriptionState.setValueTransactionlessly( "" );
		this.tagState.setValueTransactionlessly( "" );
		this.isUpdateState.setValueTransactionlessly( false );
		this.contentId = null;

		if( edu.cmu.cs.dennisc.java.lang.SystemUtilities.isPropertyTrue( "org.alice.ide.internalTesting" ) ) {
			if( this.shareLocalOperation == null ) {
				this.shareLocalOperation = new ShareLocalOperation();
				getDialogContentComposite().getView().getControlLine().getAwtComponent().add( this.shareLocalOperation.createButton().getAwtComponent(), 5 );
			}
		}
	}

	@Override
	public double getFrameRate() {
		return this.frameRate;
	}

	@Override
	public void setFrameRate( double frameRate ) {
		this.frameRate = frameRate;
	}

	@Override
	public java.awt.Dimension getRecordingSize() {
		return new java.awt.Dimension( 640, 360 );
	}

	@Override
	protected org.lgna.croquet.edits.Edit createEdit( org.lgna.croquet.history.CompletionStep<?> completionStep ) {
		edu.wustl.lookingglass.ide.community.ShareProgressDialog shareDialog = new edu.wustl.lookingglass.ide.community.ShareProgressDialog( getShareDialogTitle() );

		shareDialog.pack();
		edu.cmu.cs.dennisc.java.awt.WindowUtilities.setLocationOnScreenToCenteredWithin( shareDialog.getAwtComponent(), this.getView().getAwtComponent() );

		// launch the share thread.
		new javax.swing.SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				if( isUpdateState.getValue() ) {
					updateContent( shareDialog, contentId );
				} else {
					shareContent( shareDialog );
				}
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
				} catch( InterruptedException e ) {
					throw new RuntimeException( e );
				} catch( ExecutionException e ) {
					throw new RuntimeException( e );
				}
			};
		}.execute();

		shareDialog.setVisible( true );
		completionStep.finish();

		return null;
	}

	private class ShareLocalOperation extends org.lgna.croquet.Operation {
		public ShareLocalOperation() {
			super( org.alice.ide.IDE.PROJECT_GROUP, java.util.UUID.fromString( "51bfaec3-d0fb-4e7e-b334-fe79208c629d" ) );
			setName( "Save To Disk..." );
			setEnabled( false );
		}

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			saveContentLocally();
		}
	}
}
