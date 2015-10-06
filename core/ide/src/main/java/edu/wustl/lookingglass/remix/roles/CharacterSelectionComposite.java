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
package edu.wustl.lookingglass.remix.roles;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.alice.ide.IDE;
import org.alice.ide.ProjectStack;
import org.alice.ide.croquet.codecs.NodeCodec;
import org.alice.ide.instancefactory.GlobalFirstInstanceSceneFactory;
import org.alice.ide.instancefactory.ThisInstanceFactory;
import org.alice.ide.members.MembersComposite;
import org.alice.ide.members.components.templates.TemplateFactory;
import org.alice.ide.name.validators.MethodNameValidator;
import org.alice.nonfree.NebulousIde;
import org.alice.stageide.ast.JointMethodUtilities;
import org.alice.stageide.ast.StoryApiSpecificAstUtilities;
import org.alice.stageide.sceneeditor.SetUpMethodGenerator;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.lgna.common.ProgramExecutionExceptionHandler;
import org.lgna.croquet.Application;
import org.lgna.croquet.SimpleOperationUnadornedDialogCoreComposite;
import org.lgna.croquet.SingleSelectListState;
import org.lgna.croquet.StringState;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.croquet.event.ValueListener;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.icon.IconSize;
import org.lgna.croquet.meta.LastOneInWinsMetaState;
import org.lgna.croquet.triggers.AutomaticCompletionTrigger;
import org.lgna.croquet.triggers.Trigger;
import org.lgna.croquet.views.AbstractWindow;
import org.lgna.croquet.views.ComponentManager;
import org.lgna.croquet.views.SwingComponentView;
import org.lgna.project.Project;
import org.lgna.project.ast.AbstractField;
import org.lgna.project.ast.AbstractMethod;
import org.lgna.project.ast.AbstractType;
import org.lgna.project.ast.AstUtilities;
import org.lgna.project.ast.NamedUserType;
import org.lgna.project.ast.Statement;
import org.lgna.project.ast.ThisExpression;
import org.lgna.project.ast.UserField;
import org.lgna.project.ast.UserMethod;
import org.lgna.project.virtualmachine.UserInstance;
import org.lgna.story.EmployeesOnly;
import org.lgna.story.SCameraMarker;
import org.lgna.story.SGround;
import org.lgna.story.SMarker;
import org.lgna.story.SScene;
import org.lgna.story.SThingMarker;
import org.lgna.story.implementation.ObjectMarkerImp;
import org.lgna.story.implementation.PerspectiveCameraMarkerImp;
import org.lgna.story.implementation.ProgramImp.AwtContainerInitializer;

import edu.cmu.cs.dennisc.java.lang.ArrayUtilities;
import edu.cmu.cs.dennisc.java.util.DStack;
import edu.cmu.cs.dennisc.java.util.Lists;
import edu.cmu.cs.dennisc.java.util.Maps;
import edu.cmu.cs.dennisc.java.util.Stacks;
import edu.wustl.lookingglass.community.CommunityProjectPropertyManager;
import edu.wustl.lookingglass.croquetfx.FxComponent;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;
import edu.wustl.lookingglass.croquetfx.components.TitlePanePopOver;
import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.ide.LookingGlassTheme;
import edu.wustl.lookingglass.ide.program.RemixAssignmentPreviewContext;
import edu.wustl.lookingglass.puzzle.CompletionPuzzle;
import edu.wustl.lookingglass.remix.Role;
import edu.wustl.lookingglass.remix.SnippetScript;
import edu.wustl.lookingglass.remix.ast.DefaultJointSubstitutionManager;
import edu.wustl.lookingglass.remix.ast.edits.InsertSnippetEdit;
import edu.wustl.lookingglass.remix.roles.components.RoleAssignment;
import edu.wustl.lookingglass.remix.roles.views.CharacterSelectionView;
import edu.wustl.lookingglass.utilities.DialogUtilities;
import edu.wustl.lookingglass.virtualmachine.StateListeningVirtualMachine;
import edu.wustl.lookingglass.virtualmachine.event.VirtualMachineExecutionStateListener;

/**
 * @author Michael Pogran
 */
public class CharacterSelectionComposite extends SimpleOperationUnadornedDialogCoreComposite<CharacterSelectionView> {

	private final SnippetScript snippetScript;
	private final Project project;
	private final List<RoleAssignment> roleAssignments;
	private final NamedUserType typeToAddTo;

	private final SingleSelectListState<UserField, ?> projectFieldsState;
	private final SingleSelectListState<UserField, ?> remixFieldsState;
	private final LastOneInWinsMetaState<UserField> fieldOptionsState;

	private final CompleteRemixOperation completeRemixOperation;
	private final PreviewRemixOperation previewRemixOperation;
	private final CancelOperation cancelOperation;

	private final org.lgna.croquet.BooleanState remixPuzzleState;
	private final StringState remixTitleState;

	private ArrayList<UserField> declaredFields;
	private Project previewProject;
	private Map<UserField, org.lgna.story.Color> markerFieldsToColor = Maps.newHashMap();
	private DStack<org.lgna.story.Color> markerColorsStack = Stacks.newStack();

	private final Semaphore programLock = new Semaphore( 1 );

	@SuppressWarnings( "unchecked" )
	public CharacterSelectionComposite( SnippetScript snippet, Project project, Map<Role, AbstractField> assignments, NamedUserType remixType, String remixMethodName ) {
		super( java.util.UUID.fromString( "0bf934bc-19a9-4921-8f0b-c5229adbfcd5" ), Application.PROJECT_GROUP );

		this.snippetScript = snippet;
		this.project = project;
		this.typeToAddTo = remixType;

		this.roleAssignments = Lists.newArrayList();

		setJointMethodsOnRoles(); // needs to happen before getProjectFields() is called
		this.declaredFields = Lists.newArrayList( getProjectSceneType().fields.toArray( UserField.class ) );

		// Create a RoleAssignment for each Role
		for( Role role : this.snippetScript.getActiveRoles() ) {
			RoleAssignment roleAssignment;

			if( role.getOriginField().getValueType().isAssignableTo( SMarker.class ) ) {
				roleAssignment = new RoleAssignment( role, this, getColorForMaker( role.getOriginField() ) ); // Each marker is assigned a color for its icon
			} else {
				roleAssignment = new RoleAssignment( role, this );
			}
			this.roleAssignments.add( roleAssignment );
		}

		this.completeRemixOperation = new CompleteRemixOperation( this );
		this.previewRemixOperation = new PreviewRemixOperation( this );
		this.cancelOperation = new CancelOperation( this );

		this.remixPuzzleState = this.createBooleanState( "remixPuzzleState", false );
		this.remixTitleState = this.createStringState( "remixTitleState", remixMethodName );

		this.remixTitleState.addAndInvokeNewSchoolValueListener( ( event ) -> {
			completeRemixOperation.setEnabled( isRemixReady() );
		} );

		this.projectFieldsState = this.createMutableListState( "projectFields", UserField.class, NodeCodec.getInstance( UserField.class ), -1, getProjectFields() );
		this.remixFieldsState = this.createMutableListState( "remixFields", UserField.class, NodeCodec.getInstance( UserField.class ), -1, getRemixFields() );

		this.projectFieldsState.setListSelectionModel( new DisabledItemSelectionModel( projectFieldsState, this ) );
		this.remixFieldsState.setListSelectionModel( new DisabledItemSelectionModel( remixFieldsState, this ) );

		this.fieldOptionsState = new LastOneInWinsMetaState<UserField>( this.projectFieldsState, this.remixFieldsState );

		this.fieldOptionsState.addAndInvokeValueListener( new ValueListener<UserField>() {
			@Override
			public void valueChanged( ValueEvent<UserField> e ) {
				if( e.getNextValue() != null ) {
					getView().getRoleSelector().AssignRole( e.getNextValue() );
					setupRemixPreview();
					completeRemixOperation.setEnabled( isRemixReady() );
				}
			}
		} );

		this.getView().getRoleSelector().getRangeState().addAndInvokeNewSchoolValueListener( new ValueListener<Integer>() {
			@Override
			public void valueChanged( ValueEvent<Integer> e ) {
				// Find the currently selected RoleAssignment
				for( RoleAssignment roleAssignment : roleAssignments ) {
					if( roleAssignment.getRole() == getView().getRoleSelector().getCurrentRole() ) {
						roleAssignment.scrollToVisible();
						if( roleAssignment.getAssignment() == null ) {
							projectFieldsState.clearSelection();
							remixFieldsState.clearSelection();
						} else {
							if( projectFieldsState.containsItem( roleAssignment.getAssignment() ) ) {
								projectFieldsState.setValueTransactionlessly( roleAssignment.getAssignment() );
							} else {
								remixFieldsState.setValueTransactionlessly( roleAssignment.getAssignment() );
							}
						}
						break;
					}
				}
			}
		} );

		this.addAssignments( assignments );
	}

	@Override
	public void handlePreActivation() {
		super.handlePreActivation();
		setupRemixPreview();

		// Check for default assignment
		for( RoleAssignment roleAssignment : roleAssignments ) {
			if( roleAssignment.getRole() == getView().getRoleSelector().getCurrentRole() ) {
				roleAssignment.scrollToVisible();
				if( roleAssignment.getAssignment() == null ) {
					projectFieldsState.clearSelection();
					remixFieldsState.clearSelection();
				} else {
					if( projectFieldsState.containsItem( roleAssignment.getAssignment() ) ) {
						projectFieldsState.setValueTransactionlessly( roleAssignment.getAssignment() );
					} else {
						remixFieldsState.setValueTransactionlessly( roleAssignment.getAssignment() );
					}
				}
				break;
			}
		}
	}

	@Override
	protected CharacterSelectionView createView() {
		return new CharacterSelectionView( this );
	}

	@Override
	protected Dimension calculateWindowSize( AbstractWindow<?> window ) {
		return new Dimension( 1000, 690 );
	}

	@Override
	protected String getDialogTitle( CompletionStep<?> step ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "Remix" );
		sb.append( " " );
		sb.append( this.snippetScript.getTitle() );
		return sb.toString();
	}

	public CompleteRemixOperation getCompleteRemixOperation() {
		return this.completeRemixOperation;
	}

	public PreviewRemixOperation getPreviewRemixOperation() {
		return this.previewRemixOperation;
	}

	public CancelOperation getCancelOperation() {
		return this.cancelOperation;
	}

	public StringState getRemixTitleState() {
		return this.remixTitleState;
	}

	public org.lgna.croquet.BooleanState getRemixPuzzleState() {
		return this.remixPuzzleState;
	}

	public LastOneInWinsMetaState<UserField> getFieldOptionsState() {
		return this.fieldOptionsState;
	}

	public SingleSelectListState<UserField, ?> getProjectFieldsState() {
		return this.projectFieldsState;
	}

	public SingleSelectListState<UserField, ?> getRemixFieldsState() {
		return this.remixFieldsState;
	}

	public java.util.List<RoleAssignment> getRoleAssignments() {
		return this.roleAssignments;
	}

	private NamedUserType getProjectSceneType() {
		return StoryApiSpecificAstUtilities.getSceneTypeFromProgramType( this.project.getProgramType() );
	}

	private UserField[] getProjectFields() {
		List<UserField> assignableFields = Lists.newArrayList();

		// Iterate over all fields on the scene
		for( UserField field : this.getProjectSceneType().fields ) {
			AbstractType<?, ?, ?> type = field.getValueType();
			if( NebulousIde.nonfree.isAssignableToSRoom( type ) || type.isAssignableTo( SScene.class ) || type.isAssignableTo( SGround.class ) ) {
				//pass
			} else {
				assignableFields.add( field );
			}
		}
		return ArrayUtilities.createArray( assignableFields, UserField.class );
	}

	private UserField[] getRemixFields() {
		List<UserField> assignableFields = Lists.newArrayList();
		for( RoleAssignment roleAssignment : this.getRoleAssignments() ) {
			assignableFields.add( roleAssignment.getRole().getOriginField() );
		}
		return ArrayUtilities.createArray( assignableFields, UserField.class );
	}

	/**
	 * Helper method that gets a new Alice <code>Color</code> for a marker.
	 * Helpful in differentiating multiple markers during character selection.
	 */
	public org.lgna.story.Color getColorForMaker( UserField markerField ) {
		org.lgna.story.Color color = this.markerFieldsToColor.get( markerField );

		if( color == null ) {
			if( this.markerColorsStack.isEmpty() ) {
				resetMarkerColorStack();
			}
			color = this.markerColorsStack.pop();
			this.markerFieldsToColor.put( markerField, color );
		}
		return color;
	}

	private void resetMarkerColorStack() {
		this.markerColorsStack.push( org.lgna.story.Color.BLUE );
		this.markerColorsStack.push( org.lgna.story.Color.CYAN );
		this.markerColorsStack.push( org.lgna.story.Color.GREEN );
		this.markerColorsStack.push( org.lgna.story.Color.MAGENTA );
		this.markerColorsStack.push( org.lgna.story.Color.ORANGE );
		this.markerColorsStack.push( org.lgna.story.Color.PINK );
		this.markerColorsStack.push( org.lgna.story.Color.PURPLE );
		this.markerColorsStack.push( org.lgna.story.Color.RED );
		this.markerColorsStack.push( org.lgna.story.Color.YELLOW );
	}

	/**
	 * Is everything that the user has specified correct and valid to complete
	 * this remix?
	 *
	 * @return returns true when the remix is valid.
	 */
	public boolean isRemixReady() {
		// TODO: we need feedback to the user to say if the method name is invalid...
		return areAllRolesAssigned() && isValidMethodName();
	}

	/**
	 * Helper method that determines state of role assignment process.
	 *
	 * @return boolean indicating whether all roles in remix have been assigned
	 */
	public boolean areAllRolesAssigned() {
		for( RoleAssignment roleAssignment : this.getRoleAssignments() ) {
			if( roleAssignment.getAssignment() == null ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * We don't need any surprises about the remix code changing the name of our
	 * method. Make sure the method name is valid before the user leaves this
	 * dialog.
	 *
	 * @return returns true is the method is a valid name.
	 */
	public boolean isValidMethodName() {
		MethodNameValidator validator = new MethodNameValidator( this.typeToAddTo );
		return !this.remixTitleState.getValue().isEmpty() && validator.isNameAvailable( this.remixTitleState.getValue() );
	}

	/**
	 * Helper method that determines whether a field can be assigned to the
	 * current role.
	 *
	 * @return boolean indicating whether field can be assigned to role
	 */
	public boolean isFieldAssignable( UserField field ) {
		return validJointSubstitutions( field, getView().getCurrentRole() ) && isTypeAssignableTo( getView().getCurrentRole().getLowestTypeReferenced(), field.getValueType() );
	}

	private boolean isTypeAssignableTo( AbstractType<?, ?, ?> roleType, AbstractType<?, ?, ?> fieldType ) {
		return fieldType.isAssignableTo( roleType );
	}

	/**
	 * Helper method that determines whether a field that is a candidate for
	 * assignment has the appropriate joint substitutions for a particular role.
	 *
	 * @param assignmentField the field that is a candidate for assignment to
	 *            role
	 * @param role the role to check for default joint substitutions
	 */
	private boolean validJointSubstitutions( UserField assignmentField, Role role ) {
		boolean isValid = true;
		if( ( assignmentField != null ) && ( role != null ) ) {
			AbstractType<?, ?, ?> roleType = role.getOriginField().getValueType();
			AbstractType<?, ?, ?> assignmentType = assignmentField.getValueType();

			if( assignmentType.getName().contentEquals( roleType.getName() ) ) {
				return true;
			}

			for( AbstractMethod method : role.getJointMethods() ) {
				boolean validSubstitutionExists = DefaultJointSubstitutionManager.jointSubstitutionExists( roleType, method, assignmentType );
				if( validSubstitutionExists == false ) {
					isValid = false;
					break;
				}
			}
		} else {
			isValid = false;
		}

		return isValid;
	}

	/**
	 * Helper method that adds associated joint methods to roles. This
	 * association is used to determine whether default joint substitutions are
	 * available for fields that are candidates for role assignment.
	 */
	private void setJointMethodsOnRoles() {

		for( Map.Entry<AbstractMethod, Set<UserField>> entry : this.snippetScript.getFieldsForMethods().entrySet() ) {
			AbstractMethod method = entry.getKey();

			if( JointMethodUtilities.isJointGetter( method ) ) {
				java.util.Set<UserField> fields = entry.getValue();

				for( UserField field : fields ) {
					for( Role role : this.snippetScript.getActiveRoles() ) {
						if( role.getOriginField().equals( field ) ) {
							role.addJointMethod( method );
						}
					}
				}
			}
		}
	}

	/**
	 * Programmatically adds default assignments.
	 *
	 * @param fieldForRoles the map containing default assignments
	 */
	private void addAssignments( Map<Role, AbstractField> fieldForRoles ) {
		for( RoleAssignment roleAssignment : this.roleAssignments ) {
			AbstractField assignedField = fieldForRoles.get( roleAssignment.getRole() );
			roleAssignment.setAssignment( (UserField)assignedField );
		}
	}

	/**
	 * Maps each role in the snippet to its assigned field.
	 *
	 * @return a map of fields to roles
	 */
	private Map<Role, AbstractField> createFieldsMap() {

		Map<Role, AbstractField> mapRoleToField = Maps.newHashMap();
		for( RoleAssignment roleAssignment : this.roleAssignments ) {
			mapRoleToField.put( roleAssignment.getRole(), roleAssignment.getAssignment() );
		}

		return mapRoleToField;
	}

	/**
	 * Performs necessary preparation for remix and executes snippet edit on
	 * current project.
	 *
	 * @param transaction the transaction for snippet edit step
	 * @param trigger the trigger for snippet edit step
	 */
	public void performRemix( CompletionStep<?> step ) {
		LookingGlassIDE.getActiveInstance().setCursor( java.awt.Cursor.WAIT_CURSOR );

		// Map roles to new fields
		Map<Role, AbstractField> mapRoleToField = createFieldsMap();

		// Append community information
		if( this.snippetScript.getCommunityId() != null ) {
			CommunityProjectPropertyManager.appendCommunityRemixedSnippetId( this.project, this.snippetScript.getCommunityId() );
		}

		if( this.snippetScript.getCommunityWorldId() != null ) {
			CommunityProjectPropertyManager.appendCommunityRemixedWorldId( this.project, this.snippetScript.getCommunityWorldId() );

			new javax.swing.SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					try {
						edu.wustl.lookingglass.ide.LookingGlassIDE.getCommunityController().incrementWorldRemixCount( snippetScript.getCommunityWorldId() );
					} catch( edu.wustl.lookingglass.community.exceptions.CommunityApiException e ) {
						edu.cmu.cs.dennisc.java.util.logging.Logger.warning( e );
					}
					return null;
				}

				@Override
				protected void done() {
					try {
						get();
					} catch( InterruptedException | java.util.concurrent.ExecutionException e ) {
						edu.cmu.cs.dennisc.java.util.logging.Logger.warning( e );
					}
				}
			}.execute();
		}

		// Execute remix edit
		InsertSnippetEdit edit = new InsertSnippetEdit.Builder( step, this.snippetScript, mapRoleToField ).project( this.project ).isScramble( false ).methodType( this.typeToAddTo ).methodName( this.remixTitleState.getValue() ).build();

		LookingGlassIDE.getActiveInstance().setCursor( java.awt.Cursor.DEFAULT_CURSOR );

		try {
			step.commitAndInvokeDo( edit );
			// NOTE: side effect required of generating scene setup method
			IDE.getActiveInstance().getUpToDateProject();
			this.cleanUpAndClose();
			this.completeRemix( edit );
		} catch( RuntimeException e ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( e, this );
			this.projectFieldsState.clearSelection();
			this.remixFieldsState.clearSelection();
			getView().getRoleSelector().RollBackAssignment();
		}
	}

	/**
	 * Shows appropriate stencil after completion of remix process. The stencil
	 * placement and setup of editor differs based on whether or not the remix
	 * was added as a code scramble.
	 *
	 * @param edit executed <code>InsertSnippetEdit</code>
	 */
	private void completeRemix( InsertSnippetEdit edit ) {
		UserMethod method = edit.getRootMethod();

		final Runnable showRemixDoneNote = () -> {
			assert typeToAddTo != null;

			if( this.typeToAddTo.isAssignableFrom( IDE.getActiveInstance().getDocumentFrame().getInstanceFactoryState().getValue().getValueType() ) ) {
				// pass
			} else {
				org.alice.ide.instancefactory.InstanceFactory instanceFactory = null;
				NamedUserType sceneType = StoryApiSpecificAstUtilities.getSceneTypeFromProject( this.project );
				if( this.typeToAddTo.isAssignableFrom( sceneType ) ) {
					if( IDE.getActiveInstance().getDocumentFrame().getMetaDeclarationFauxState().getType().isAssignableTo( SScene.class ) ) {
						instanceFactory = ThisInstanceFactory.getInstance();
					} else {
						instanceFactory = GlobalFirstInstanceSceneFactory.getInstance();
					}
				} else {
					for( UserField field : sceneType.getDeclaredFields() ) {
						if( this.typeToAddTo.isAssignableFrom( field.getValueType() ) ) {
							instanceFactory = LookingGlassIDE.getActiveInstance().getInstanceFactoryForSceneOrSceneField( field );
							break;
						}
					}
				}
				IDE.getActiveInstance().getDocumentFrame().getInstanceFactoryState().setValueTransactionlessly( instanceFactory );
			}

			MembersComposite.getInstance().getProcedureTabComposite();
			TemplateFactory.getProcedureInvocationTemplate( method ).scrollToVisible();

			// Show note to user to help them use the remix...
			ThreadHelper.runOnFxThread( () -> {
				TitlePanePopOver methodNote = new TitlePanePopOver( FxComponent.DEFAULT_RESOURCES.getString( "Remix.popOver.title" ), FxComponent.DEFAULT_RESOURCES.getString( "Remix.popOver.message" ) );
				methodNote.getPopOver().arrowLocationProperty().set( ArrowLocation.LEFT_CENTER );
				methodNote.closeOnClickProperty().set( true );
				methodNote.getTitlePane().setOkVisible( false );
				methodNote.getTitlePane().setImage( LookingGlassTheme.getFxImage( "remix", IconSize.SMALL ) );

				SwingComponentView<?> component = ComponentManager.getFirstComponent( TemplateFactory.getProcedureInvocationTemplate( method ).getModel() );
				methodNote.show( component );
			} );
		};

		// Show the puzzle interface to complete the remix?
		if( this.remixPuzzleState.getValue() ) {
			// TODO: showStencil... means bring up puzzle perspective? yuck. Fix this.
			// TODO: this should actually be placed on the field that was selected. But for now that is ALWAYS the scene.
			org.lgna.project.ast.UserField field = org.alice.stageide.ast.StoryApiSpecificAstUtilities.getSceneFieldFromProgramType( edit.getProject().getProgramType() );

			// TODO: We should actually figure out what lesson we should teach, and present that puzzle.
			CompletionPuzzle puzzle = new CompletionPuzzle( edit.getProject(), field, method );
			assert puzzle != null;

			SwingUtilities.invokeLater( () -> {
				puzzle.beginPuzzle( () -> {
					org.alice.ide.ProjectDocumentFrame documentFrame = org.alice.ide.IDE.getActiveInstance().getDocumentFrame();
					documentFrame.setToCodePerspectiveTransactionlessly();

					showRemixDoneNote.run();
				} );
			} );
		} else {
			showRemixDoneNote.run();
		}
	}

	/**
	 * Scrubs previewProject of unnecessary statements and adds statements to
	 * achieve ghosting effect for fields not used in remix.
	 *
	 * @param keepFields fields in previewProject used for remix preview
	 */
	private void scrubPreviewProject( List<UserField> keepFields ) {
		NamedUserType previewSceneType = StoryApiSpecificAstUtilities.getSceneTypeFromProgramType( this.previewProject.getProgramType() );
		UserMethod previewUserMain = StoryApiSpecificAstUtilities.getUserMethodsInvokedSceneActivationListeners( previewSceneType ).get( 0 );
		UserMethod previewGenSetup = StoryApiSpecificAstUtilities.getPerformEditorGeneratedSetUpMethod( previewSceneType );

		// Add fields with non NamedUserType. i.e., scene, camera, ground, etc.
		for( UserField field : previewSceneType.getDeclaredFields() ) {
			if( field.getValueType() instanceof NamedUserType ) {
				//pass
			} else {
				keepFields.add( field );
			}
		}

		// Clear any statements in main method
		previewUserMain.body.getValue().statements.clear();

		// Iterate over declared fields and ghost fields not used in remix
		for( UserField field : previewSceneType.fields ) {
			if( keepFields.contains( field ) ) {
				//pass
			} else {
				Statement opacityStatement = SetUpMethodGenerator.createSetOpacityStatement( field, 0.25f );
				Statement colorStatement = SetUpMethodGenerator.createSetPaintStatement( field, org.lgna.story.Color.BLACK );

				//				if( ( opacityStatement != null ) && ( colorStatement != null ) ) {
				previewGenSetup.body.getValue().statements.add( opacityStatement );
				previewGenSetup.body.getValue().statements.add( colorStatement );
				//				}
			}
		}
	}

	/**
	 * Creates a new copy of project to use for preview and performs necessary
	 * preparation for displaying remix with appropriate roles.
	 *
	 */
	public void setupRemixPreview() {
		this.previewProject = org.lgna.project.CopyUtilities.createCopy( this.project, org.lgna.project.ast.DecodeIdPolicy.NEW_IDS );
		NamedUserType sceneType = StoryApiSpecificAstUtilities.getSceneTypeFromProgramType( previewProject.getProgramType() );
		UserMethod userMain = StoryApiSpecificAstUtilities.getUserMethodsInvokedSceneActivationListeners( sceneType ).get( 0 );

		// Need dummy step and transaction for remix
		Transaction transaction = LookingGlassIDE.getActiveInstance().getTransactionHistory().acquireActiveTransaction();
		CompletionStep<?> step = CompletionStep.createAndAddToTransaction( transaction, null, AutomaticCompletionTrigger.createUserInstance(), null );

		List<UserField> keepList = Lists.newArrayList(); // fields in existing project to keep for preview
		Map<Role, AbstractField> mapRoleToField = Maps.newHashMap();

		// Loop through role assignments and find appropriate field
		for( RoleAssignment roleAssignment : this.roleAssignments ) {
			UserField assignment;
			if( roleAssignment.getAssignment() != null ) {
				assignment = roleAssignment.getAssignment();

				//replace with field in copy
				int index = this.declaredFields.indexOf( assignment );
				if( index > -1 ) {
					assignment = sceneType.getDeclaredFields().get( this.declaredFields.indexOf( assignment ) );
				}
				keepList.add( assignment );
			} else {
				assignment = roleAssignment.getRole().getOriginField();
			}

			mapRoleToField.put( roleAssignment.getRole(), assignment );
		}

		// Clean project
		scrubPreviewProject( keepList );

		// Remix and add method
		ProjectStack.pushProject( this.previewProject ); // push preview project onto stack

		try {
			Project previewProject = ProjectStack.peekProject();

			InsertSnippetEdit edit = new InsertSnippetEdit.Builder( step, this.snippetScript, mapRoleToField ).project( previewProject ).methodName( this.remixTitleState.getValue() ).methodType( StoryApiSpecificAstUtilities.getSceneTypeFromProject( previewProject ) ).isPreview( true ).build();

			step.commitAndInvokeDo( edit );
			userMain.body.getValue().statements.add( AstUtilities.createMethodInvocationStatement( new ThisExpression(), edit.getRootMethod() ) );
			showRemixPreview( true );
		} catch( RuntimeException e ) {
			e.printStackTrace();
			this.projectFieldsState.clearSelection();
			this.remixFieldsState.clearSelection();
			getView().getRoleSelector().RollBackAssignment();
			showRemixErrorDialog();
		}

		ProjectStack.popProject(); // pop preview project
	}

	private RemixAssignmentPreviewContext programContext;
	private VirtualMachineExecutionStateListener vmStateListener;
	private ProgramExecutionExceptionHandler exceptionHandler;
	private boolean isPreviewPaused;
	private boolean hasFinishedExecuting;

	/**
	 * Creates and initializes new <code>ProgramContext</code> for remix
	 * previewProject.
	 *
	 * @param shouldPause determines whether vm will initially pause
	 */
	private void showRemixPreview( final boolean shouldPause ) {

		// initialize vmStateListener if necessary
		if( this.vmStateListener == null ) {
			this.vmStateListener = new VirtualMachineExecutionStateListener() {

				@Override
				public void isChangedToPaused() {
					if( isPreviewPaused ) {
						//pass
					} else {
						hasFinishedExecuting = true;
						previewRemixOperation.setEnabled( true );
					}
				}

				@Override
				public void isChangedToRunning() {
					isPreviewPaused = false;
				}

				@Override
				public void isEndingExecution() {
				}
			};
		}

		if( this.exceptionHandler == null ) {
			this.exceptionHandler = new ProgramExecutionExceptionHandler() {
				Set<String> caughtExceptions = edu.cmu.cs.dennisc.java.util.Sets.newHashSet();

				@Override
				public void handleProgramExecutionExeception( Throwable t ) {
					if( this.caughtExceptions.contains( t.getClass().toString() ) ) {
						//pass
					} else {
						this.caughtExceptions.add( t.getClass().toString() );
						LookingGlassIDE.getActiveInstance().getExceptionHandler().uncaughtException( null, t );
					}
					edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( t );
				}
			};
		}

		javax.swing.SwingUtilities.invokeLater( () -> {
			this.programLock.acquireUninterruptibly();
			this.cleanupRemixPreview();
			this.startRemixPreview( shouldPause );
			this.programLock.release();
		} );
	}

	private void startRemixPreview( boolean shouldPause ) {
		assert javax.swing.SwingUtilities.isEventDispatchThread();

		this.programContext = new RemixAssignmentPreviewContext( this.previewProject, shouldPause );

		if( this.programContext.getVirtualMachine() instanceof StateListeningVirtualMachine ) {
			( (StateListeningVirtualMachine)this.programContext.getVirtualMachine() ).addVirtualMachinePauseStateListener( this.vmStateListener );
		}
		this.programContext.setProgramExceptionHandler( this.exceptionHandler );
		this.programContext.setActiveSceneOnComponentThreadAndWait();
		this.programContext.initializeInContainer( new PreviewAwtContainerInitializer( getView().createProgramContainer().getAwtComponent() ) );

		getView().getProgramContainer().revalidateAndRepaint();

		List<UserField> markers = Lists.newArrayList();

		for( UserField field : StoryApiSpecificAstUtilities.getSceneTypeFromProject( previewProject ).getDeclaredFields() ) {
			if( field.getValueType().isAssignableTo( SMarker.class ) ) {
				markers.add( field );
			}
		}

		// Show markers and set to correct color (matching RoleAssingment for field)
		for( UserField marker : markers ) {
			Object instance = this.programContext.getSceneInstance().getFieldValue( marker );
			org.lgna.story.Color markerColor = null;

			for( RoleAssignment assignment : this.roleAssignments ) {
				if( assignment.getRole().getName().contentEquals( marker.getName() ) ) {
					markerColor = assignment.getMarkerColor();
					break;
				}
			}

			if( instance != null ) {
				if( instance instanceof UserInstance ) {
					instance = ( (UserInstance)instance ).getJavaInstance();
				}
				if( instance instanceof SThingMarker ) {
					ObjectMarkerImp imp = EmployeesOnly.getImplementation( (SThingMarker)instance );
					if( markerColor != null ) {
						imp.paint.setValue( markerColor );
					}
					imp.setShowing( true );
				}
				if( instance instanceof SCameraMarker ) {
					PerspectiveCameraMarkerImp imp = EmployeesOnly.getImplementation( (SCameraMarker)instance );
					if( markerColor != null ) {
						imp.paint.setValue( markerColor );
					}
					imp.setShowing( true );
				}
			}
		}

		this.isPreviewPaused = true;
		this.hasFinishedExecuting = false;
	}

	private void cleanupRemixPreview() {
		assert javax.swing.SwingUtilities.isEventDispatchThread();

		if( this.programContext != null ) {
			if( programContext.getVirtualMachine() instanceof StateListeningVirtualMachine ) {
				( (StateListeningVirtualMachine)this.programContext.getVirtualMachine() ).removeVirtualMachinePauseStateListener( this.vmStateListener );
			}
			this.programContext.cleanUpProgram();
			this.programContext = null;
		}
	}

	public void showRemixErrorDialog() {
		DialogUtilities.showErrorDialog( SwingUtilities.getWindowAncestor( getView().getAwtComponent() ), "There was a problem remixing, please try again. If the problem persists, try a different remix." );
	}

	/**
	 * Attempts to perform necessary cleanup on programContext and close root
	 * dialog window.
	 */
	public void cleanUpAndClose() {
		Component root = SwingUtilities.getRoot( getView().getAwtComponent() );
		if( root != null ) {
			if( this.programContext != null ) {
				this.programContext.cleanUpProgram();
				if( this.programContext.getVirtualMachine() instanceof edu.wustl.lookingglass.virtualmachine.StateListeningVirtualMachine ) {
					( (edu.wustl.lookingglass.virtualmachine.StateListeningVirtualMachine)this.programContext.getVirtualMachine() ).removeVirtualMachinePauseStateListener( this.vmStateListener );
				}
			}

			root.setVisible( false );
		}
	}

	/**
	 * <code>ActionOperation</code> that determines the state of the preview
	 * program VM and plays preview in appropriate manner.
	 *
	 * @author Michael Pogran
	 */
	public class PreviewRemixOperation extends org.lgna.croquet.Operation {
		private final CharacterSelectionComposite composite;

		public PreviewRemixOperation( CharacterSelectionComposite composite ) {
			super( org.lgna.croquet.Application.PROJECT_GROUP, java.util.UUID.fromString( "2d434e04-c014-4250-8b13-0229a33342c7" ) );
			this.composite = composite;
		}

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			// VM has finished executing current ProgramContext
			// to preview again we must initialize a new ProgramContext and play
			if( ( this.composite.programContext != null ) && this.composite.hasFinishedExecuting ) {
				showRemixPreview( false );
			}
			// VM has not executed, unpause VM in current ProgramContext
			else {
				if( this.composite.programContext.getVirtualMachine() instanceof edu.wustl.lookingglass.virtualmachine.StateListeningVirtualMachine ) {
					( (edu.wustl.lookingglass.virtualmachine.StateListeningVirtualMachine)this.composite.programContext.getVirtualMachine() ).setUnpaused();
				}
			}
			this.setEnabled( false );
		}
	}

	/**
	 * <code>ActionOperation</code> that executes a remix on the current project
	 * and closes the dialog containing the
	 * <code>CharacterSelectionComposite</code>. Only enabled when all roles
	 * have been successfully cast.
	 *
	 * @author Michael Pogran
	 */
	public class CompleteRemixOperation extends org.lgna.croquet.Operation {
		private final CharacterSelectionComposite composite;

		public CompleteRemixOperation( CharacterSelectionComposite composite ) {
			super( org.lgna.croquet.Application.PROJECT_GROUP, java.util.UUID.fromString( "b21dfd6d-a60d-4faa-998e-e14144698b66" ) );
			this.setEnabled( false );
			this.composite = composite;
		}

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			CompletionStep<?> step = CompletionStep.createAndAddToTransaction( transaction, this, trigger, null );
			composite.performRemix( step );
		}

	}

	/**
	 * <code>ActionOperation</code> that closes the dialog containing the
	 * <code>CharacterSelectionComposite</code> and performs any necessary
	 * cleanup.
	 *
	 * @author Michael Pogran
	 */
	public class CancelOperation extends org.lgna.croquet.Operation {
		private final CharacterSelectionComposite composite;

		public CancelOperation( CharacterSelectionComposite composite ) {
			super( org.lgna.croquet.Application.PROJECT_GROUP, java.util.UUID.fromString( "f6251556-8996-4e1c-97dc-b2e1d39daca5" ) );
			this.composite = composite;
		}

		@Override
		protected void perform( Transaction transaction, Trigger trigger ) {
			this.composite.cleanUpAndClose();
		}

	}

	/**
	 * Custom <code>ListSelectionModel</code> to allow for disabled items in
	 * list. Used when a field in the scene is not applicable for the role
	 * currently being filled.
	 *
	 * @author Michael Pogran
	 */
	private class DisabledItemSelectionModel extends DefaultListSelectionModel {

		private final SingleSelectListState<UserField, ?> listState; // croquet list state associated with model
		private final CharacterSelectionComposite composite;

		public DisabledItemSelectionModel( SingleSelectListState<UserField, ?> listState, CharacterSelectionComposite composite ) {
			this.listState = listState;
			this.composite = composite;
		}

		@Override
		public void setSelectionInterval( int index0, int index1 ) {
			// only if field is assignable do we allow setting the selection
			if( this.composite.isFieldAssignable( listState.getItemAt( index1 ) ) ) {
				super.setSelectionInterval( index0, index1 );
			}
		}
	}

	/**
	 * Custom <code>AwtContainerInitializer</code> needed to place preview
	 * button above render window. Using a <code>SpringLayout</code>, the button
	 * is placed in the south-east corner of the
	 * <code>OnscreenLookingGlass</code> panel.
	 *
	 * @author Michael Pogran
	 */
	private class PreviewAwtContainerInitializer implements AwtContainerInitializer {
		private final java.awt.Container awtContainer; // container for OnScreenLookingGlass panel

		public PreviewAwtContainerInitializer( Container awtContainer ) {
			this.awtContainer = awtContainer;
		}

		@Override
		public void addComponents( edu.cmu.cs.dennisc.render.OnscreenRenderTarget<?> onscreenRenderTarget ) {
			if( onscreenRenderTarget.getAwtComponent() instanceof JPanel ) {
				JPanel panel = (JPanel)onscreenRenderTarget.getAwtComponent();
				JButton button = getView().getPreviewButton().getAwtComponent();
				SpringLayout layout = new SpringLayout();

				panel.setLayout( layout );
				panel.add( button, BorderLayout.CENTER );

				layout.putConstraint( SpringLayout.EAST, button, -5, SpringLayout.EAST, panel );
				layout.putConstraint( SpringLayout.SOUTH, button, -5, SpringLayout.SOUTH, panel );
			}
			this.awtContainer.add( onscreenRenderTarget.getAwtComponent() );
		}

		@Override
		public Container getAwtContainer() {
			return this.awtContainer;
		}
	}
}
