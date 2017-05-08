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

import edu.wustl.lookingglass.ide.LookingGlassIDE;

/**
 * @author Dennis Cosgrove
 */
public abstract class IDE extends org.alice.ide.ProjectApplication {
	public static final org.lgna.croquet.Group RUN_GROUP = org.lgna.croquet.Group.getInstance( java.util.UUID.fromString( "f7a87645-567c-42c6-bf5f-ab218d93a226" ), "RUN_GROUP" );
	public static final org.lgna.croquet.Group EXPORT_GROUP = org.lgna.croquet.Group.getInstance( java.util.UUID.fromString( "624d4db6-2e1a-43c2-b1df-c0bfd6407b35" ), "EXPORT_GROUP" );

	public static IDE getActiveInstance() {
		return edu.cmu.cs.dennisc.java.lang.ClassUtilities.getInstance( org.lgna.croquet.Application.getActiveInstance(), IDE.class );
	}

	private final org.lgna.croquet.event.ValueListener<org.alice.ide.perspectives.ProjectPerspective> perspectiveListener = new org.lgna.croquet.event.ValueListener<org.alice.ide.perspectives.ProjectPerspective>() {
		@Override
		public void valueChanged( org.lgna.croquet.event.ValueEvent<org.alice.ide.perspectives.ProjectPerspective> e ) {
			IDE.this.setPerspective( e.getNextValue() );
		}
	};

	public org.alice.ide.codedrop.CodePanelWithDropReceptor getCodeEditorInFocus() {
		org.alice.ide.perspectives.ProjectPerspective perspective = this.getDocumentFrame().getPerspectiveState().getValue();
		if( perspective != null ) {
			return perspective.getCodeDropReceptorInFocus();
		} else {
			return null;
		}
	}

	private org.alice.ide.stencil.PotentialDropReceptorsFeedbackView potentialDropReceptorsStencil;

	private final IdeConfiguration ideConfiguration;

	public IDE( IdeConfiguration ideConfiguration, ApiConfigurationManager apiConfigurationManager ) {
		super( ideConfiguration, apiConfigurationManager );
		this.ideConfiguration = ideConfiguration;

		// <lg/> Let the OS set the locale. We don't need to set it...
	}

	public IdeConfiguration getIdeConfiguration() {
		return this.ideConfiguration;
	}

	public final ApiConfigurationManager getApiConfigurationManager() {
		return this.getDocumentFrame().getApiConfigurationManager();
	}

	@Override
	public void initialize( String[] args ) {
		super.initialize( args );
		ProjectDocumentFrame documentFrame = this.getDocumentFrame();
		documentFrame.getPerspectiveState().addNewSchoolValueListener( this.perspectiveListener );
		documentFrame.initialize();
	}

	public abstract org.alice.ide.sceneeditor.AbstractSceneEditor getSceneEditor();

	private Theme theme;

	protected Theme createTheme() {
		return new DefaultTheme();
	}

	public final Theme getTheme() {
		if( this.theme != null ) {
			//pass
		} else {
			this.theme = this.createTheme();
		}
		return this.theme;
	}

	@Override
	public org.lgna.croquet.Operation getPreferencesOperation() {
		return null;
	}

	public abstract org.lgna.croquet.Operation createPreviewOperation( org.alice.ide.members.components.templates.ProcedureInvocationTemplate procedureInvocationTemplate );

	public enum AccessorAndMutatorDisplayStyle {
		GETTER_AND_SETTER,
		ACCESS_AND_ASSIGNMENT
	}

	public AccessorAndMutatorDisplayStyle getAccessorAndMutatorDisplayStyle( org.lgna.project.ast.AbstractField field ) {
		if( field != null ) {
			org.lgna.project.ast.AbstractType<?, ?, ?> declaringType = field.getDeclaringType();
			if( ( declaringType != null ) && declaringType.isUserAuthored() ) {
				return AccessorAndMutatorDisplayStyle.ACCESS_AND_ASSIGNMENT;
			} else {
				return AccessorAndMutatorDisplayStyle.GETTER_AND_SETTER;
				//return AccessorAndMutatorDisplayStyle.ACCESS_AND_ASSIGNMENT;
			}
		} else {
			return AccessorAndMutatorDisplayStyle.ACCESS_AND_ASSIGNMENT;
		}
	}

	public abstract org.lgna.project.ast.UserMethod getPerformEditorGeneratedSetUpMethod();

	protected abstract edu.cmu.cs.dennisc.pattern.Criterion<org.lgna.project.ast.Declaration> getDeclarationFilter();

	public void crawlFilteredProgramType( edu.cmu.cs.dennisc.pattern.Crawler crawler ) {
		org.lgna.project.ast.NamedUserType programType = this.getProgramType();
		if( programType != null ) {
			programType.crawl( crawler, org.lgna.project.ast.CrawlPolicy.COMPLETE, this.getDeclarationFilter() );
		}
	}

	private static class UnacceptableFieldAccessCrawler extends edu.cmu.cs.dennisc.pattern.IsInstanceCrawler<org.lgna.project.ast.FieldAccess> {
		private final java.util.Set<org.lgna.project.ast.UserField> unacceptableFields;

		public UnacceptableFieldAccessCrawler( java.util.Set<org.lgna.project.ast.UserField> unacceptableFields ) {
			super( org.lgna.project.ast.FieldAccess.class );
			this.unacceptableFields = unacceptableFields;
		}

		@Override
		protected boolean isAcceptable( org.lgna.project.ast.FieldAccess fieldAccess ) {
			return this.unacceptableFields.contains( fieldAccess.field.getValue() );
		}
	}

	private String reorganizeTypeFieldsIfNecessary( org.lgna.project.ast.NamedUserType namedUserType, int startIndex, java.util.Set<org.lgna.project.ast.UserField> alreadyMovedFields ) {
		java.util.List<org.lgna.project.ast.UserField> fields = namedUserType.fields.getValue().subList( startIndex, namedUserType.fields.size() );
		java.util.Set<org.lgna.project.ast.UserField> unacceptableFields = edu.cmu.cs.dennisc.java.util.Sets.newHashSet( fields );
		org.lgna.project.ast.UserField fieldToMoveToTheEnd = null;
		java.util.List<org.lgna.project.ast.FieldAccess> accessesForFieldToMoveToTheEnd = null;
		for( org.lgna.project.ast.UserField field : fields ) {
			org.lgna.project.ast.Expression initializer = field.initializer.getValue();
			UnacceptableFieldAccessCrawler crawler = new UnacceptableFieldAccessCrawler( unacceptableFields );
			initializer.crawl( crawler, org.lgna.project.ast.CrawlPolicy.EXCLUDE_REFERENCES_ENTIRELY );
			java.util.List<org.lgna.project.ast.FieldAccess> fieldAccesses = crawler.getList();
			if( fieldAccesses.size() > 0 ) {
				fieldToMoveToTheEnd = field;
				accessesForFieldToMoveToTheEnd = fieldAccesses;
				break;
			}
			unacceptableFields.remove( field );
		}
		if( fieldToMoveToTheEnd != null ) {
			if( alreadyMovedFields.contains( fieldToMoveToTheEnd ) ) {
				//todo: better cycle detection?
				StringBuilder sb = new StringBuilder();
				sb.append( "<html>Possible cycle detected.<br>The field <strong>\"" );
				sb.append( fieldToMoveToTheEnd.getName() );
				sb.append( "\"</strong> on type <strong>\"" );
				sb.append( fieldToMoveToTheEnd.getDeclaringType().getName() );
				sb.append( "\"</strong> is referencing: " );
				String prefix = "<strong>\"";
				for( org.lgna.project.ast.FieldAccess fieldAccess : accessesForFieldToMoveToTheEnd ) {
					org.lgna.project.ast.AbstractField accessedField = fieldAccess.field.getValue();
					sb.append( prefix );
					sb.append( accessedField.getName() );
					prefix = "\"</strong>, <strong>\"";
				}
				sb.append( "\"</strong><br>" );
				sb.append( getApplicationName() );
				sb.append( " already attempted to move it once." );
				sb.append( "<br><br><strong>Your program may fail.</strong></html>" );
				return sb.toString();
			} else {
				for( org.lgna.project.ast.FieldAccess fieldAccess : accessesForFieldToMoveToTheEnd ) {
					org.lgna.project.ast.AbstractField accessedField = fieldAccess.field.getValue();
					if( accessedField == fieldToMoveToTheEnd ) {
						StringBuilder sb = new StringBuilder();
						sb.append( "<html>The field <strong>\"" );
						sb.append( fieldToMoveToTheEnd.getName() );
						sb.append( "\"</strong> on type <strong>\"" );
						sb.append( fieldToMoveToTheEnd.getDeclaringType().getName() );
						sb.append( "\"</strong> is referencing <strong>itself</strong>." );
						sb.append( "<br><br><strong>Your program may fail.</strong></html>" );
						return sb.toString();
					}
				}
				int prevIndex = namedUserType.fields.indexOf( fieldToMoveToTheEnd );
				int nextIndex = namedUserType.fields.size() - 1;
				namedUserType.fields.slide( prevIndex, nextIndex );
				alreadyMovedFields.add( fieldToMoveToTheEnd );
				return this.reorganizeTypeFieldsIfNecessary( namedUserType, prevIndex, alreadyMovedFields );
			}
		} else {
			return null;
		}
	}

	private void reorganizeFieldsIfNecessary() {
		org.lgna.project.Project project = this.getProject();
		if( project != null ) {
			for( org.lgna.project.ast.NamedUserType namedUserType : project.getNamedUserTypes() ) {
				java.util.Set<org.lgna.project.ast.UserField> alreadyMovedFields = edu.cmu.cs.dennisc.java.util.Sets.newHashSet();
				String message = this.reorganizeTypeFieldsIfNecessary( namedUserType, 0, alreadyMovedFields );
				if( message != null ) {
					new edu.cmu.cs.dennisc.javax.swing.option.OkDialog.Builder( message )
							.title( "Unable to Recover" )
							.messageType( edu.cmu.cs.dennisc.javax.swing.option.MessageType.ERROR )
							.buildAndShow();
				}
			}
		}
	}

	@Override
	public void ensureProjectCodeUpToDate() {
		org.lgna.project.Project project = this.getProject();
		if( project != null ) {
			if( this.isProjectUpToDateWithSceneSetUp() == false ) {
				synchronized( project.getLock() ) {
					this.generateCodeForSceneSetUp();
					this.reorganizeFieldsIfNecessary();
					this.updateHistoryIndexSceneSetUpSync();
				}
			}
		}
	}

	public org.lgna.project.ast.NamedUserType getUpToDateProgramType() {
		org.lgna.project.Project project = this.getUpToDateProject();
		if( project != null ) {
			return project.getProgramType();
		} else {
			return null;
		}
	}

	public java.util.List<org.lgna.project.ast.FieldAccess> getFieldAccesses( final org.lgna.project.ast.AbstractField field ) {
		return org.lgna.project.ProgramTypeUtilities.getFieldAccesses( this.getProgramType(), field, this.getDeclarationFilter() );
	}

	public java.util.List<org.lgna.project.ast.MethodInvocation> getMethodInvocations( final org.lgna.project.ast.AbstractMethod method ) {
		return org.lgna.project.ProgramTypeUtilities.getMethodInvocations( this.getProgramType(), method, this.getDeclarationFilter() );
	}

	public java.util.List<org.lgna.project.ast.SimpleArgumentListProperty> getArgumentLists( final org.lgna.project.ast.UserCode code ) {
		return org.lgna.project.ProgramTypeUtilities.getArgumentLists( this.getProgramType(), code, this.getDeclarationFilter() );
	}

	public boolean isDropDownDesiredFor( org.lgna.project.ast.Expression expression ) {
		if( org.lgna.project.ast.AstUtilities.isKeywordExpression( expression ) || LookingGlassIDE.getActiveInstance().isInDinahRemixPerspective() ) { // </lg>
			return false;
		}
		return ( ( expression instanceof org.lgna.project.ast.TypeExpression ) || ( expression instanceof org.lgna.project.ast.ResourceExpression ) ) == false;
	}

	public abstract org.alice.ide.cascade.ExpressionCascadeManager getExpressionCascadeManager();

	public org.alice.ide.stencil.PotentialDropReceptorsFeedbackView getPotentialDropReceptorsFeedbackView() {
		if( this.potentialDropReceptorsStencil == null ) {
			this.potentialDropReceptorsStencil = new org.alice.ide.stencil.PotentialDropReceptorsFeedbackView( this.getDocumentFrame().getFrame() );
		}
		return this.potentialDropReceptorsStencil;
	}

	public void showDropReceptorsStencilOver( org.lgna.croquet.views.DragComponent potentialDragSource, final org.lgna.project.ast.AbstractType<?, ?, ?> type ) {
		this.getPotentialDropReceptorsFeedbackView().showStencilOver( potentialDragSource, type );
	}

	public void hideDropReceptorsStencil() {
		this.getPotentialDropReceptorsFeedbackView().hideStencil();
	}

	@Deprecated
	@Override
	public void setDragInProgress( boolean isDragInProgress ) {
		super.setDragInProgress( isDragInProgress );
		this.getPotentialDropReceptorsFeedbackView().setDragInProgress( isDragInProgress );
	}

	protected boolean isAccessibleDesired( org.lgna.project.ast.Accessible accessible ) {
		return accessible.getValueType().isArray() == false;
	}

	@Override
	public void setProject( org.lgna.project.Project project ) {
		boolean isScenePerspectiveDesiredByDefault = edu.cmu.cs.dennisc.java.lang.SystemUtilities.getBooleanProperty( "org.alice.ide.IDE.isScenePerspectiveDesiredByDefault", false );
		ProjectDocumentFrame documentFrame = this.getDocumentFrame();
		org.alice.ide.perspectives.ProjectPerspective defaultPerspective = isScenePerspectiveDesiredByDefault ? documentFrame.getSetupScenePerspective() : documentFrame.getCodePerspective();
		documentFrame.getPerspectiveState().setValueTransactionlessly( defaultPerspective );
		super.setProject( project );
		org.lgna.croquet.Perspective perspective = this.getPerspective();
		if( ( perspective == null ) || ( perspective == documentFrame.getNoProjectPerspective() ) ) {
			this.setPerspective( documentFrame.getPerspectiveState().getValue() );
		}
	}

	public <N extends org.lgna.project.ast.Node> N createCopy( N original ) {
		org.lgna.project.ast.NamedUserType root = this.getProgramType();
		return org.lgna.project.ast.AstUtilities.createCopy( original, root );
	}

	private org.lgna.project.ast.Comment commentThatWantsFocus = null;

	public org.lgna.project.ast.Comment getCommentThatWantsFocus() {
		return this.commentThatWantsFocus;
	}

	public void setCommentThatWantsFocus( org.lgna.project.ast.Comment commentThatWantsFocus ) {
		this.commentThatWantsFocus = commentThatWantsFocus;
	}

	protected abstract void promptForLicenseAgreements();

	@Override
	protected void handleWindowOpened( java.awt.event.WindowEvent e ) {
		this.promptForLicenseAgreements();
	}

	@Override
	protected void handleOpenFiles( java.util.List<java.io.File> files ) {
	}

	public void preservePreferences() {
		try {
			org.lgna.croquet.preferences.PreferenceManager.preservePreferences();
		} catch( java.util.prefs.BackingStoreException bse ) {
			bse.printStackTrace();
		}
	}

	private final org.alice.ide.croquet.models.projecturi.ClearanceCheckingExitOperation clearanceCheckingExitOperation = new org.alice.ide.croquet.models.projecturi.ClearanceCheckingExitOperation( this.getDocumentFrame() );

	@Override
	public final void handleQuit( org.lgna.croquet.triggers.Trigger trigger ) {
		// <lg/> Quit stuff moved to org.alice.ide.croquet.models.projecturi.SystemExitOperation
		this.clearanceCheckingExitOperation.fire( trigger );
	}

	protected org.lgna.project.virtualmachine.VirtualMachine createVirtualMachineForSceneEditor() {
		return new org.lgna.project.virtualmachine.ReleaseVirtualMachine();
	}

	protected abstract void registerAdaptersForSceneEditorVm( org.lgna.project.virtualmachine.VirtualMachine vm );

	public final org.lgna.project.virtualmachine.VirtualMachine createRegisteredVirtualMachineForSceneEditor() {
		org.lgna.project.virtualmachine.VirtualMachine vm = this.createVirtualMachineForSceneEditor();
		this.registerAdaptersForSceneEditorVm( vm );
		return vm;
	}

	protected abstract String getInnerCommentForMethodName( String methodName );

	private void generateCodeForSceneSetUp() {
		org.lgna.project.ast.UserMethod userMethod = this.getPerformEditorGeneratedSetUpMethod();
		org.lgna.project.ast.StatementListProperty bodyStatementsProperty = userMethod.body.getValue().statements;
		bodyStatementsProperty.clear();
		String innerComment = getInnerCommentForMethodName( userMethod.getName() );
		if( innerComment != null ) {
			bodyStatementsProperty.add( new org.lgna.project.ast.Comment( innerComment ) );
		}
		this.getSceneEditor().generateCodeForSetUp( bodyStatementsProperty );
	}

	public org.lgna.project.ast.NamedUserType getProgramType() {
		org.lgna.project.Project project = this.getProject();
		if( project != null ) {
			return project.getProgramType();
		} else {
			return null;
		}
	}

	public String getInstanceTextForAccessible( org.lgna.project.ast.Accessible accessible ) {
		String text;
		if( accessible != null ) {
			if( accessible instanceof org.lgna.project.ast.AbstractField ) {
				org.lgna.project.ast.AbstractField field = (org.lgna.project.ast.AbstractField)accessible;
				text = field.getName();
				org.lgna.project.ast.AbstractCode focusedCode = this.getDocumentFrame().getFocusedCode();
				if( focusedCode != null ) {
					org.lgna.project.ast.AbstractType<?, ?, ?> scopeType = focusedCode.getDeclaringType();
					if( field.getValueType() == scopeType ) {
						text = "this";
					} else if( field.getDeclaringType() == scopeType ) {
						if( org.alice.ide.croquet.models.ui.preferences.IsIncludingThisForFieldAccessesState.getInstance().getValue() ) {
							text = "this." + text;
						}
					}
				}
			} else {
				text = accessible.getValidName();
			}
		} else {
			text = null;
		}
		return text;
	}

	protected static <E extends org.lgna.project.ast.Node> E getAncestor( org.lgna.project.ast.Node node, Class<E> cls ) {
		org.lgna.project.ast.Node ancestor = node.getParent();
		while( ancestor != null ) {
			if( cls.isAssignableFrom( ancestor.getClass() ) ) {
				break;
			} else {
				ancestor = ancestor.getParent();
			}
		}
		return (E)ancestor;
	}

	public org.lgna.croquet.views.AwtComponentView<?> getPrefixPaneForFieldAccessIfAppropriate( org.lgna.project.ast.FieldAccess fieldAccess ) {
		return null;
	}

	public org.lgna.croquet.views.AwtComponentView<?> getPrefixPaneForInstanceCreationIfAppropriate( org.lgna.project.ast.InstanceCreation instanceCreation ) {
		return null;
	}

	public abstract boolean isInstanceCreationAllowableFor( org.lgna.project.ast.NamedUserType userType );
}
