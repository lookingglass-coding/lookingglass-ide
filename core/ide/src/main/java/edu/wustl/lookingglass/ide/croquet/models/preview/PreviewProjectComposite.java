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
package edu.wustl.lookingglass.ide.croquet.models.preview;

import java.awt.Image;

import javax.swing.SwingWorker;

import org.alice.ide.uricontent.UriContentLoader.MutationPlan;
import org.alice.ide.uricontent.UriProjectLoader;
import org.lgna.croquet.BooleanState;
import org.lgna.croquet.SimpleComposite;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.event.ValueListener;
import org.lgna.croquet.views.FixedAspectRatioPanel;
import org.lgna.croquet.views.Panel;
import org.lgna.project.Project;

import edu.cmu.cs.dennisc.javax.swing.IconUtilities;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.croquet.components.NotAvailableIcon;
import edu.wustl.lookingglass.ide.croquet.models.preview.views.PreviewWorldPanel;
import edu.wustl.lookingglass.utilities.ProjectThumbnailGenerator;

/**
 * @author Michael Pogran
 */
public class PreviewProjectComposite extends SimpleComposite<Panel> {

	private final static String NO_THUMBNAIL_TEXT = "Preview Unavailable";

	private final IsPreviewProgramExecutingState isPreviewProgramExecutingState = new IsPreviewProgramExecutingState( this );

	private final PreviewWorldPanel previewWorldPanel;
	private final ValueListener<Boolean> previewExecutingListener;

	private UriProjectLoader uriProjectLoader;
	private SwingWorker<Project, Void> projectWorker;
	private SwingWorker<Image, Void> imageWorker;
	private BooleanState validProjectState = this.createBooleanState( "validProjectState", true );

	private boolean isPreviewImageLoaded = false;
	private Image previewImage;

	private double speedFactor = 2.0;

	public PreviewProjectComposite() {
		super( java.util.UUID.fromString( "9ccceadd-cb4e-40f6-be42-da6fd798decb" ) );
		assert java.awt.EventQueue.isDispatchThread() : this.getClass().getSimpleName() + " not created in EDT";

		this.previewWorldPanel = new PreviewWorldPanel( this, this.isPreviewProgramExecutingState, this.validProjectState );

		this.previewExecutingListener = new ValueListener<Boolean>() {
			@Override
			public void valueChanged( ValueEvent<Boolean> event ) {
				previewWorldPanel.update( event.getNextValue() );
			}
		};
	}

	public void loadProject( final UriProjectLoader uriProjectLoader ) {
		if( uriProjectLoader != null ) {
			this.reset();

			this.uriProjectLoader = uriProjectLoader;

			// Don't try and load a project marked as invalid
			if( !uriProjectLoader.isValid() ) {
				loadError();
				return;
			}

			this.projectWorker = new SwingWorker<Project, Void>() {
				@Override
				protected Project doInBackground() throws Exception {
					return uriProjectLoader.getContentWaitingIfNecessary( MutationPlan.PROMISE_NOT_TO_MUTATE );
				}

				@Override
				protected void done() {
					try {
						Project project = get();
						if( project != null ) {
							loadProject( project, false, true );
						} else {
							loadError();
						}
					} catch( Exception e ) {
						//pass
					}
				}
			};

			this.imageWorker = new SwingWorker<Image, Void>() {
				@Override
				protected java.awt.Image doInBackground() throws Exception {
					return uriProjectLoader.getThumbnailWaitingIfNecessary();
				}

				@Override
				protected void done() {
					try {
						Image image = get();
						if( image != null ) {
							setPreviewImage( image, false );
						} else {
							setPreviewImage( IconUtilities.iconToImage( new NotAvailableIcon( LookingGlassIDE.DEFAULT_WORLD_DIMENSION_WIDTH, LookingGlassIDE.DEFAULT_WORLD_DIMENSION_HEIGHT, NO_THUMBNAIL_TEXT ) ), false );
						}
					} catch( Exception e ) {
						//pass
					}
				}
			};
			this.imageWorker.execute(); // only load image initially
		}
	}

	public void reset() {
		if( this.projectWorker != null ) {
			this.projectWorker.cancel( true );
		}
		if( this.imageWorker != null ) {
			this.imageWorker.cancel( true );
		}
		this.projectWorker = null;
		this.imageWorker = null;
		this.uriProjectLoader = null;

		this.isPreviewProgramExecutingState.setValueTransactionlessly( false );
		this.validProjectState.setValueTransactionlessly( true );
		this.previewWorldPanel.clear();
		this.previewWorldPanel.setIsPlayDisabled( false );

		this.resetPreviewImage();
	}

	public void loadError() {
		this.previewWorldPanel.setIsPlayDisabled( true );
		this.validProjectState.setValueTransactionlessly( false );
		this.isPreviewProgramExecutingState.clearSelectedProject();

		if( this.uriProjectLoader != null ) {
			this.uriProjectLoader.setValid( false );
		}

		this.setPreviewImage( IconUtilities.iconToImage( new NotAvailableIcon( LookingGlassIDE.DEFAULT_WORLD_DIMENSION_WIDTH, LookingGlassIDE.DEFAULT_WORLD_DIMENSION_HEIGHT, "Project Unavailable" ) ), true );
	}

	public void loadProject( Project project, boolean generateThumbnail, boolean shouldPlay ) {
		this.validProjectState.setValueTransactionlessly( true );
		this.isPreviewProgramExecutingState.setSelectedProject( project );
		this.setPreviewImage( getPreviewImage( project, generateThumbnail ), false );

		this.previewWorldPanel.updateProgram();
		if( shouldPlay ) {
			this.isPreviewProgramExecutingState.setValueTransactionlessly( true );
		}
	}

	public synchronized Image getPreviewImage( Project project, boolean generateThumbnail ) {
		Image image = IconUtilities.iconToImage( new NotAvailableIcon( LookingGlassIDE.DEFAULT_WORLD_DIMENSION_WIDTH, LookingGlassIDE.DEFAULT_WORLD_DIMENSION_HEIGHT, NO_THUMBNAIL_TEXT ) );

		if( ( this.imageWorker == null ) || ( this.imageWorker.isDone() && ( this.previewImage == null ) ) ) {
			if( generateThumbnail ) {
				image = this.generateThumbnail( project );
			}
		}
		return image;
	}

	public synchronized void setPreviewImage( Image image, boolean override ) {
		if( this.isPreviewImageLoaded ) {
			if( override ) {
				this.previewImage = image;
				this.previewWorldPanel.setBackgroundImage( image );
			}
		} else {
			this.previewImage = image;
			this.previewWorldPanel.setBackgroundImage( image );
			this.isPreviewImageLoaded = true;
		}
	}

	private synchronized void resetPreviewImage() {
		this.previewWorldPanel.setBackgroundImage( IconUtilities.iconToImage( new NotAvailableIcon( LookingGlassIDE.DEFAULT_WORLD_DIMENSION_WIDTH, LookingGlassIDE.DEFAULT_WORLD_DIMENSION_HEIGHT, NO_THUMBNAIL_TEXT ) ) );
		this.previewImage = null;
		this.isPreviewImageLoaded = false;
	}

	public void playPreview() {
		if( this.projectWorker != null ) {
			if( this.projectWorker.isDone() ) {
				this.isPreviewProgramExecutingState.setValueTransactionlessly( true );
			} else {
				this.projectWorker.execute();
			}
		} else if( this.isPreviewProgramExecutingState.getCurrentProject() != null ) {
			this.isPreviewProgramExecutingState.setValueTransactionlessly( true );
		}
	}

	public void stopPreview() {
		this.isPreviewProgramExecutingState.setValueTransactionlessly( false );
	}

	public boolean isPreviewPaused() {
		if( this.isPreviewProgramExecutingState.getExecutingProgram() != null ) {
			return this.isPreviewProgramExecutingState.getExecutingProgram().getAnimator().getSpeedFactor() == 0.0;
		} else {
			return true;
		}
	}

	public void pausePreview() {
		if( this.isPreviewProgramExecutingState.getExecutingProgram() != null ) {
			this.isPreviewProgramExecutingState.getExecutingProgram().getAnimator().setSpeedFactor( 0.0 );
		}
	}

	public void resumePreview() {
		if( this.isPreviewProgramExecutingState.getExecutingProgram() != null ) {
			this.isPreviewProgramExecutingState.getExecutingProgram().getAnimator().setSpeedFactor( 1.0 );
		}
	}

	public void setFastForward( boolean value ) {
		if( this.isPreviewProgramExecutingState.getExecutingProgram() != null ) {
			if( value ) {
				this.isPreviewProgramExecutingState.getExecutingProgram().getAnimator().setSpeedFactor( speedFactor );
			} else {
				this.isPreviewProgramExecutingState.getExecutingProgram().getAnimator().setSpeedFactor( 1.0 );
			}
		}
	}

	public void setFastForwardSpeedFactor( double speed ) {
		this.speedFactor = speed;
	}

	public void restartPreview() {
		this.isPreviewProgramExecutingState.setValueTransactionlessly( false );
		this.isPreviewProgramExecutingState.setValueTransactionlessly( true );
	}

	public BooleanState getValidProjectState() {
		return this.validProjectState;
	}

	public org.alice.stageide.program.RunProgramContext getProgramContext() {
		return this.isPreviewProgramExecutingState.getProgramContext();
	}

	private Image generateThumbnail( Project project ) {
		Image thumbnail;
		try {
			thumbnail = ProjectThumbnailGenerator.createThumbnail( LookingGlassIDE.DEFAULT_WORLD_DIMENSION_WIDTH, LookingGlassIDE.DEFAULT_WORLD_DIMENSION_HEIGHT, project );
		} catch( Exception e ) {
			thumbnail = IconUtilities.iconToImage( new NotAvailableIcon( LookingGlassIDE.DEFAULT_WORLD_DIMENSION_WIDTH, LookingGlassIDE.DEFAULT_WORLD_DIMENSION_HEIGHT, NO_THUMBNAIL_TEXT ) );
		}
		return thumbnail;
	}

	@Override
	protected Panel createView() {
		return new FixedAspectRatioPanel( this, this.previewWorldPanel, 16.0 / 9.0 );
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		this.isPreviewProgramExecutingState.addAndInvokeNewSchoolValueListener( previewExecutingListener );
	}

	@Override
	public void handlePostDeactivation() {
		this.reset();
		this.isPreviewProgramExecutingState.removeNewSchoolValueListener( previewExecutingListener );
		super.handlePostDeactivation();
	}

	public PreviewWorldPanel getPreviewWorldPanel() {
		return this.previewWorldPanel;
	}
}
