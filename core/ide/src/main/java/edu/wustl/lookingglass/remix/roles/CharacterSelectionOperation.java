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

import java.util.Iterator;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.alice.ide.name.validators.MethodNameValidator;
import org.alice.nonfree.NebulousIde;
import org.alice.stageide.ast.StoryApiSpecificAstUtilities;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.croquet.history.Transaction;
import org.lgna.croquet.triggers.Trigger;
import org.lgna.project.Project;
import org.lgna.project.ast.AbstractField;
import org.lgna.project.ast.AbstractType;
import org.lgna.project.ast.Expression;
import org.lgna.project.ast.FieldAccess;
import org.lgna.project.ast.NamedUserType;
import org.lgna.project.ast.UserField;
import org.lgna.story.SCamera;
import org.lgna.story.SGround;
import org.lgna.story.SScene;

import edu.wustl.lookingglass.ide.LookingGlassIDE;
import edu.wustl.lookingglass.remix.Role;
import edu.wustl.lookingglass.remix.SnippetScript;
import edu.wustl.lookingglass.remix.ast.RemixUtilities;
import edu.wustl.lookingglass.remix.models.ReuseGroup;
import edu.wustl.lookingglass.utilities.DialogUtilities;

/**
 * An <code>ActionOperation</code> subclass that initiates the remix process.
 *
 * @author Michael Pogran
 */
public class CharacterSelectionOperation extends org.lgna.croquet.Operation {

	private final SnippetScript snippetScript;
	private final Project project;

	private final boolean isQuickAssign;
	private boolean autoRemix = false;
	private boolean isPuzzle = false;

	private Map<Role, AbstractField> assignments = new java.util.HashMap<Role, AbstractField>();
	private java.util.List<Role> unassignedRoles = new java.util.LinkedList<Role>();
	private NamedUserType typeToAddTo;
	private String methodName;

	public CharacterSelectionOperation( SnippetScript snippetScript, Project project ) {
		this( snippetScript, project, false );
	}

	/**
	 * Constructs a <code>SelectCharactersOperation</code> object.
	 *
	 * @param snippetScript the {@link SnippetScript} to use for remix
	 * @param project the project to remix snippet into
	 * @param shouldQuickAssign determines whether operation should look for
	 *            assignments
	 */
	public CharacterSelectionOperation( SnippetScript snippetScript, Project project, boolean shouldQuickAssign ) {
		super( ReuseGroup.REUSE_GROUP, java.util.UUID.fromString( "6c604d25-3bdc-40c4-af2d-6b18bd53ea29" ) );
		this.snippetScript = snippetScript;
		this.project = project;
		// TODO: refs #2254 Put this back in...
		// this.isQuickAssign = shouldQuickAssign;
		this.isQuickAssign = false;

		// Script is valid if room/ground is not referenced in snippet or,
		// if room/ground is referenced, project must contain same field.
		if( RemixUtilities.checkSnippetForCompatability( this.snippetScript, this.project ) ) {
			//pass
		} else {
			String message = RemixUtilities.getSnippetIncompatabilityReason( this.snippetScript, this.project );
			DialogUtilities.showErrorDialog( SwingUtilities.getWindowAncestor( getFrame().getAwtComponent() ), "Remix is incompatible. " + message );
			edu.cmu.cs.dennisc.java.util.logging.Logger.errln( "Remix incompatible: room/ground type" );
			return;
		}

		// Search for easy replacements for special roles.  Some snippets may not contain any active roles (ex. camera pan),
		// so, we check for these replacements prior to showing the character selection dialog
		for( Role role : this.snippetScript.getAllRoles() ) {
			UserField field = role.getOriginField();
			AbstractType<?, ?, ?> type = field.getValueType();
			UserField assignedField = null;

			if( NebulousIde.nonfree.isAssignableToSRoom( type ) ) {
				assignedField = getAssignedField( NebulousIde.nonfree.getSRoomClass() );
				if( assignedField == null ) {
					assignedField = getAssignedField( SGround.class );
				}
			} else if( type.isAssignableTo( SScene.class ) ) {
				assignedField = getAssignedField( SScene.class );
			} else if( type.isAssignableTo( SGround.class ) ) {
				assignedField = getAssignedField( SGround.class );
				if( assignedField == null ) {
					assignedField = getAssignedField( NebulousIde.nonfree.getSRoomClass() );
				}
			} else if( type.isAssignableTo( SCamera.class ) ) {
				assignedField = getAssignedField( SCamera.class );
			} else {
				unassignedRoles.add( role );
				continue;
			}

			// If the field was of a special type and we found a field in the project to assign it to
			if( assignedField != null ) {
				assignments.put( role, assignedField );
			} else {
				throw new RuntimeException( "Remix incompatible with assigned project." );
			}
		}

		// If only one field left to assign, see if its applicable for the currently selected field
		if( ( unassignedRoles.size() == 1 ) && this.isQuickAssign ) {
			Expression expression = org.alice.ide.IDE.getActiveInstance().getDocumentFrame().getInstanceFactoryState().getValue().createTransientExpression();
			Role role = unassignedRoles.get( 0 );

			if( expression instanceof FieldAccess ) {
				AbstractField field = ( (FieldAccess)expression ).field.getValue();

				if( field instanceof UserField ) {
					if( field.getValueType().isAssignableTo( role.getLowestTypeReferenced() ) ) {
						assignments.put( role, field );
						typeToAddTo = (NamedUserType)field.getValueType();
					}
				}
			}
		}

		if( typeToAddTo == null ) {
			this.typeToAddTo = StoryApiSpecificAstUtilities.getSceneTypeFromProject( this.project );
		}

		// method names make more sense when they are lower case based on our current conventions.
		this.methodName = RemixUtilities.getValidMemberName( new MethodNameValidator( this.typeToAddTo ), this.snippetScript.getTitle().toLowerCase() );
	}

	public CharacterSelectionOperation( SnippetScript snippetScript, Project project, String rolesArg, String methodArg, boolean isPuzzle ) {
		this( snippetScript, project );
		this.setAutoRemix( true );

		this.isPuzzle = isPuzzle;

		if( methodArg != null ) {
			this.methodName = methodArg;
		}

		// This really shouldn't use asserts, it should throw exceptions, but this is a development feature, so I'm assuming the developer
		// doesn't need really amazing feedback to use this correctly.
		if( rolesArg != null ) {
			String[] subs = rolesArg.split( "(?<!\\\\)," );
			for( String sub : subs ) {
				assert sub != null;

				String[] splits = sub.split( "(?<!\\\\):" );
				assert splits.length == 2;

				String roleName = splits[ 0 ];
				String objectName = splits[ 1 ];

				// Find the page that has this...
				Iterator<Role> i = this.unassignedRoles.iterator();
				while( i.hasNext() ) {
					Role role = i.next();
					if( role.getName().equals( roleName ) ) {
						org.lgna.project.ast.NamedUserType sceneType = StoryApiSpecificAstUtilities.getSceneTypeFromProject( this.project );
						org.lgna.project.ast.UserField replacementField = null;

						// TODO: looks like scene is left out of this... but this is good enough for now
						for( org.lgna.project.ast.UserField field : sceneType.fields ) {
							if( field.getName().equals( objectName ) ) {
								replacementField = field;
								break;
							}
						}

						if( replacementField != null ) {
							this.assignments.put( role, replacementField );
							i.remove();
						}
					}
				}
			}
		}
	}

	/**
	 * When set, do the remix automatically without showing the recast dialog if
	 * possible
	 */
	public void setAutoRemix( boolean autoRemix ) {
		this.autoRemix = autoRemix;
	}

	@Override
	protected void perform( Transaction transaction, Trigger trigger ) {
		LookingGlassIDE.getActiveInstance().setCursor( java.awt.Cursor.WAIT_CURSOR );

		CharacterSelectionComposite composite = new CharacterSelectionComposite( this.snippetScript, this.project, this.assignments, this.typeToAddTo, this.methodName );

		composite.getRemixPuzzleState().setValueTransactionlessly( this.isPuzzle );

		if( this.autoRemix && composite.isRemixReady() ) {
			CompletionStep<?> step = CompletionStep.createAndAddToTransaction( transaction, this, trigger, null );
			composite.performRemix( step );
		} else {
			composite.getLaunchOperation().fire( trigger );
		}

		LookingGlassIDE.getActiveInstance().setCursor( java.awt.Cursor.DEFAULT_CURSOR );
	}

	/**
	 * Helper method that finds the corresponding field for a provided
	 * <code>JavaType</code> class.
	 */
	private UserField getAssignedField( Class<?> assignedClass ) {
		NamedUserType sceneType = StoryApiSpecificAstUtilities.getSceneTypeFromProject( this.project );

		if( sceneType.isAssignableTo( assignedClass ) ) {
			return StoryApiSpecificAstUtilities.getSceneFieldFromProgramType( this.project.getProgramType() );
		} else {
			for( UserField field : sceneType.getDeclaredFields() ) {
				AbstractType<?, ?, ?> fieldType = field.getValueType();
				if( fieldType.isAssignableTo( assignedClass ) ) {
					return field;
				}
			}
		}
		return null;
	}
}
