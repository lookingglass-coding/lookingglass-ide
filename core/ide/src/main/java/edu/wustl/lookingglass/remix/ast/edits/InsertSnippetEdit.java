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
package edu.wustl.lookingglass.remix.ast.edits;

import java.util.List;
import java.util.Map;

import org.lgna.croquet.edits.AbstractEdit;
import org.lgna.croquet.edits.Edit;
import org.lgna.croquet.history.CompletionStep;
import org.lgna.project.Project;
import org.lgna.project.ast.AbstractField;
import org.lgna.project.ast.NamedUserType;
import org.lgna.project.ast.UserMethod;
import org.lgna.story.SScene;

import edu.wustl.lookingglass.ide.croquet.edits.ast.AddMethodToTypeEdit;
import edu.wustl.lookingglass.ide.croquet.edits.ast.ScrambleMethodEdit;
import edu.wustl.lookingglass.remix.Role;
import edu.wustl.lookingglass.remix.SnippetScript;
import edu.wustl.lookingglass.remix.ast.ScriptToASTCopier;

/**
 * Croquet edit responsible for copying a <code>SnippetScript</code> into a
 * <code>Project</code>. This edit performs a number of sub-edits that perform
 * the task of adding necessary characters to the project and adding the remixed
 * method to the methodType provided.
 *
 * @author Michael Pogran
 */
public final class InsertSnippetEdit extends AbstractEdit {

	public static class Builder {
		private CompletionStep<?> completionStep;
		private SnippetScript snippetScript;
		private Map<Role, AbstractField> fieldForRoles;
		private Project project;
		private NamedUserType methodType;
		private String methodName;
		private Boolean isScramble = false;
		private Boolean isPreview = false;

		public Builder( CompletionStep<?> completionStep, SnippetScript snippetScript, Map<Role, AbstractField> fieldForRoles ) {
			this.completionStep = completionStep;
			this.snippetScript = snippetScript;
			this.fieldForRoles = fieldForRoles;
		}

		public Builder project( Project project ) {
			this.project = project;
			return this;
		}

		public Builder methodType( NamedUserType type ) {
			this.methodType = type;
			return this;
		}

		public Builder methodName( String name ) {
			this.methodName = name;
			return this;
		}

		public Builder isScramble( boolean isScramble ) {
			this.isScramble = isScramble;
			return this;
		}

		public Builder isPreview( boolean isPreview ) {
			this.isPreview = isPreview;
			return this;
		}

		public InsertSnippetEdit build() {
			return new InsertSnippetEdit( this );
		}
	}

	private final SnippetScript snippetScript;
	private final Project project;

	private final Map<Role, AbstractField> fieldForRoles;
	private final NamedUserType methodType; // type final remix method should be placed on
	private final String methodName;

	private final boolean isScramble;
	private final boolean isPreview;

	private UserMethod rootMethod;

	private AddMethodToTypeEdit addMethodEdit;
	private List<Edit> addFieldEdits;

	private InsertSnippetEdit( Builder builder ) {
		super( builder.completionStep );
		this.project = builder.project;
		this.snippetScript = builder.snippetScript;
		this.fieldForRoles = builder.fieldForRoles;
		this.methodType = builder.methodType;
		this.methodName = builder.methodName;
		this.isScramble = builder.isScramble;
		this.isPreview = builder.isPreview;
	}

	public UserMethod getRootMethod() {
		return rootMethod;
	}

	public Project getProject() {
		return this.project;
	}

	@Override
	protected void doOrRedoInternal( boolean isDo ) {
		ScriptToASTCopier copier = new ScriptToASTCopier( this.project );

		UserMethod remixMethod = copier.copyScriptToAST( this.snippetScript, this.fieldForRoles, this.methodName );

		// must fix ThisExpressions if remix is not being added to the scene
		if( this.methodType.isAssignableTo( SScene.class ) ) {
			//pass
		} else {
			copier.resolveThisExpressions( remixMethod );
		}
		this.rootMethod = remixMethod;

		// get and perform edits adding new fields to the project
		this.addFieldEdits = copier.getFieldEdits( getCompletionStep(), this.isPreview );
		for( Edit subEdit : this.addFieldEdits ) {
			subEdit.doOrRedo( isDo );
		}

		// scramble method if applicable

		if( this.isScramble ) {
			ScrambleMethodEdit scrambleEdit = new ScrambleMethodEdit( this.getCompletionStep(), this.rootMethod );
			scrambleEdit.doOrRedo( isDo );
		}

		// Add new method to scene
		this.addMethodEdit = new AddMethodToTypeEdit( this.getCompletionStep(), this.methodType, this.rootMethod );
		this.addMethodEdit.doOrRedo( isDo );
	}

	@Override
	protected void undoInternal() {
		for( Edit subEdit : this.addFieldEdits ) {
			subEdit.undo();
		}

		this.addMethodEdit.undo();
	}

	@Override
	protected void appendDescription( StringBuilder rv, DescriptionStyle descriptionStyle ) {
		rv.append( "remix snippet: " );
		rv.append( this.snippetScript.getTitle() );
		rv.append( " as: " );
		rv.append( this.methodName );
	}
}
