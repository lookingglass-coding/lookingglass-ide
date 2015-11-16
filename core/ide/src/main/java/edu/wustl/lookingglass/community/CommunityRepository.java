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
package edu.wustl.lookingglass.community;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand.Stage;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.IndexDiff.StageState;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.lgna.project.io.IoUtilities;

import edu.cmu.cs.dennisc.app.ApplicationRoot;
import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.wustl.lookingglass.community.CommunityRepositorySyncStatus.SyncStatus;
import edu.wustl.lookingglass.community.exceptions.CommunityRepositoryException;
import edu.wustl.lookingglass.croquetfx.ThreadHelper;

/**
 * @author Kyle J. Harms
 */
public class CommunityRepository {

	private static final File REPO_FILE_LOCK = new File( ApplicationRoot.getDataDirectory(), "projects.lck" );

	private static final int AUTO_GC_LOOSE_OBJECTS = Integer.valueOf( System.getProperty( "edu.wustl.lookingglass.projectSync.autoGcThreshold", "6700" ) );

	private static final String DEFAULT_BRANCH = "master";

	private final File repoDir;
	private final File gitDir;
	private final String baseRemoteName;

	private String remoteName;
	private URL remoteURL;
	private String username;
	private String email;
	private transient CredentialsProvider credentials;

	private final Git git;
	private final Semaphore repoLock = new Semaphore( 1 ); // mutex

	private final String syncLockPath;
	private FileChannel syncLockChannel;

	private boolean workOffline = Boolean.valueOf( System.getProperty( "edu.wustl.lookingglass.projectSync.offline", "false" ) );

	public CommunityRepository( File repoDir, String baseRemoteName ) throws IOException, GitAPIException {
		this.repoDir = repoDir;
		this.gitDir = new File( this.repoDir, ".git" );
		this.baseRemoteName = baseRemoteName;

		this.resetAuthenication();

		// In theory, if Looking Glass/Alice was designed and engineered well... we wouldn't
		// need to have multiple instances of the jvm running to allow people to edit
		// multiple worlds at a time. This would make it really easy to not worry about
		// two separate processes corrupting the git database. We don't live in that world.
		// So we need to use a file lock so that multiple processes don't corrupt the git
		// database.
		this.syncLockPath = REPO_FILE_LOCK.getAbsolutePath();

		try {
			this.repoLock.acquireUninterruptibly();
			this.lockRepo();

			// Check to see if the current projects directory has been converted to a git repository
			if( hasGitRepository( this.repoDir ) ) {
				this.git = load();
			} else {
				// The current projects directory doesn't have a git repository.
				// We should convert it, so it contains one.
				this.git = init();
			}
		} catch( URISyntaxException e ) {
			// this should never happen.
			throw new IllegalStateException( e );
		} finally {
			this.unlockRepo();
			this.repoLock.release();
		}
	}

	public void setAuthenication( URL repoURL, String username, char[] password ) {
		this.remoteURL = repoURL;
		this.username = username;
		this.credentials = new UsernamePasswordCredentialsProvider( username, password );

		if( this.baseRemoteName == null ) {
			this.remoteName = this.username;
		} else {
			this.remoteName = this.baseRemoteName + "+" + this.username;
		}

		// Since we are dealing with kids, we should probably not actually store their email
		// So since the remoteName actually tells us a lot... let's use that instead.
		this.email = this.remoteName;
	}

	public void resetAuthenication() {
		this.remoteName = null;
		this.remoteURL = null;
		this.username = null;
		this.email = null;
		this.credentials = null;
	}

	public boolean shouldWorkOffline() {
		return this.workOffline;
	}

	public void setShouldWorkOffline( boolean isOffline ) {
		this.workOffline = isOffline;
	}

	public Future<?> sync( Consumer<CommunityRepositorySyncStatus> runWhenDone ) {
		return ThreadHelper.runInBackground( () -> {
			CommunityRepositorySyncStatus status = this.syncRepo();
			if( runWhenDone != null ) {
				runWhenDone.accept( status );
			}
		} );
	}

	public CommunityRepositorySyncStatus syncAndWait() {
		return this.syncRepo();
	}

	private CommunityRepositorySyncStatus syncRepo() {
		assert this.git != null;
		// do not run networked operations in the UI thread.
		assert !ThreadHelper.isUIThread(); // do not remove this assert.

		CommunityRepositorySyncStatus status;
		try {
			this.repoLock.acquireUninterruptibly();
			this.lockRepo();

			this.verify();
			this.commit();

			// Only go to the server if we have enough information.
			if( ( this.remoteName != null ) && ( this.credentials != null ) && !this.shouldWorkOffline() ) {
				this.pull();
				this.push();
				status = new CommunityRepositorySyncStatus( SyncStatus.SUCCESS );
			} else {
				status = new CommunityRepositorySyncStatus( SyncStatus.SUCCESS_OFFLINE );
			}
		} catch( Throwable t ) {
			status = new CommunityRepositorySyncStatus( t );
		} finally {
			this.unlockRepo();
			this.repoLock.release();
		}

		return status;
	}

	private Git init() throws URISyntaxException, IOException, IllegalStateException, GitAPIException {
		assert !hasGitRepository( this.repoDir );

		Git git = Git.init()
				.setDirectory( this.repoDir )
				.call();

		StoredConfig config = git.getRepository().getConfig();
		config.setString( "core", null, "ignorecase", "false" ); // Be case sensitive explicitly to work on Mac
		config.setString( "core", null, "filemode", "false" ); // Ignore permission changes
		config.setString( "core", null, "precomposeunicode", "true" ); // Use the same Unicode form on all filesystems
		config.setString( "push", null, "default", "simple" );
		config.save();

		return git;
	}

	private Git load() throws IOException, URISyntaxException, IllegalStateException, GitAPIException {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.setGitDir( this.gitDir )
				.readEnvironment()
				.build();

		Git newGit;
		if( hasAtLeastOneReference( repository ) ) {
			// The repository is valid
			newGit = new Git( repository );
		} else {
			// The current git dir isn't valid... so let's make a new one.
			Logger.warning( "invalid git dir found " + this.gitDir + "; deleting." );
			FileUtils.forceDelete( this.gitDir );
			newGit = init();
		}

		// We need to make sure this repo stays in shape...
		// so let's do some maintenance every now and then...
		Properties gcStats = newGit.gc().getStatistics();
		int looseObjects = Integer.valueOf( gcStats.getProperty( "numberOfLooseObjects", "0" ) );
		if( ( AUTO_GC_LOOSE_OBJECTS != 0 ) && ( looseObjects > AUTO_GC_LOOSE_OBJECTS ) ) {
			newGit.gc().call();
		}

		return newGit;
	}

	private void verify() throws IOException, GitAPIException, URISyntaxException {

		if( ( this.remoteName != null ) && ( this.remoteURL != null ) ) {
			this.addRemote( this.remoteName, this.remoteURL );
		}

		String head = this.git.getRepository().getFullBranch();
		boolean headIsBranch = false;
		List<Ref> branches = this.git.branchList().call();
		for( Ref ref : branches ) {
			if( head.equals( ref.getName() ) ) {
				headIsBranch = true;
				break;
			}
		}

		RepositoryState state = this.git.getRepository().getRepositoryState();
		switch( state ) {
		case SAFE:
			// Everything is good!
			break;
		case MERGING_RESOLVED:
		case CHERRY_PICKING_RESOLVED:
		case REVERTING_RESOLVED:
			// Commit this work!
			Logger.warning( "commiting state: " + state + "." );
			CommitCommand commit = git.commit();
			if( ( this.username != null ) && ( this.email != null ) ) {
				commit.setAuthor( this.username, this.email );
			}
			commit.call();
			break;
		case MERGING:
		case CHERRY_PICKING:
		case REVERTING:
		case REBASING:
		case REBASING_REBASING:
		case APPLY:
		case REBASING_MERGE:
		case REBASING_INTERACTIVE:
		case BISECTING:
			// Reset, because we can't sync with unresolved conflicts
			Logger.warning( "unsafe repository state: " + state + ", reseting." );
			this.git.reset()
					.setMode( ResetType.HARD )
					.setRef( head )
					.call();
			break;
		case BARE:
			throw new IllegalStateException( "invalid repository state: " + state );
		default:
			throw new IllegalArgumentException( "unknown merge repository state: " + state );
		}

		if( !headIsBranch ) {
			if( branches.size() > 0 ) {
				try {
					this.git.branchCreate()
							.setName( DEFAULT_BRANCH )
							.call();
				} catch( RefAlreadyExistsException e ) {
					// ignore
				}
				this.git.checkout()
						.setName( DEFAULT_BRANCH )
						.call();
			}
		}
	}

	private void addRemote( String newName, URL newURL ) throws IOException, URISyntaxException {
		assert this.remoteName != null;
		assert this.remoteURL != null;

		boolean remoteExists = false;
		StoredConfig config = this.git.getRepository().getConfig();
		Set<String> remotes = config.getSubsections( "remote" );
		for( String oldName : remotes ) {
			String oldURL = config.getString( "remote", oldName, "url" );
			if( newName.equals( oldName ) ) {
				remoteExists = true;
				if( newURL.toExternalForm().equals( oldURL ) ) {
					break;
				} else {
					Logger.warning( "inconsistent remote url " + oldName + " : " + oldURL );
					config.setString( "remote", oldName, "url", newURL.toExternalForm() );
					config.save();
					break;
				}
			}
		}

		if( !remoteExists ) {
			RemoteConfig remoteConfig = new RemoteConfig( config, this.remoteName );
			remoteConfig.addURI( new URIish( this.remoteURL ) );
			remoteConfig.addFetchRefSpec( new RefSpec( "+refs/heads/*:refs/remotes/" + this.remoteName + "/*" ) );
			remoteConfig.update( config );
			config.save();
		}
	}

	private void commit() throws GitAPIException, IOException {
		// Check for changed files
		this.git.add()
				.setUpdate( true )
				.addFilepattern( "." )
				.call();

		// Check for new files
		File gitignore = new File( this.repoDir, ".gitignore" );
		if( gitignore.exists() ) {
			this.git.add()
					.addFilepattern( "." )
					.call();
		} else {
			// If there is no git ignore, then let's default to only adding lgp files
			Status status = this.git.status().call();
			AddCommand add = this.git.add();

			boolean newFiles = false;
			for( String untracked : status.getUntracked() ) {
				if( untracked.toLowerCase().endsWith( "." + IoUtilities.PROJECT_EXTENSION ) ) {
					add.addFilepattern( untracked );
					newFiles = true;
				}
			}

			if( newFiles ) {
				add.call();
			}
		}

		Status status = this.git.status().call();
		if( !status.getChanged().isEmpty() || !status.getAdded().isEmpty() || !status.getRemoved().isEmpty() ) {
			StringBuilder message = new StringBuilder();
			for( String changed : status.getChanged() ) {
				message.append( "/ " ).append( changed ).append( ";\n" );
			}
			for( String added : status.getAdded() ) {
				message.append( "+ " ).append( added ).append( ";\n" );
			}
			for( String removed : status.getRemoved() ) {
				message.append( "- " ).append( removed ).append( ";\n" );
			}

			CommitCommand commit = git.commit()
					.setMessage( message.toString() );
			if( ( this.username != null ) && ( this.email != null ) ) {
				commit.setAuthor( this.username, this.email );
			}
			commit.call();
		}
	}

	private void pull() throws GitAPIException, IOException {
		assert this.credentials != null;
		assert this.remoteName != null;
		assert this.username != null;
		assert this.email != null;

		String head = this.git.getRepository().getBranch();
		PullCommand pull = git.pull()
				.setCredentialsProvider( this.credentials )
				.setRemote( this.remoteName )
				.setRemoteBranchName( head );

		PullResult result = pull.call();
		MergeResult mergeResult = result.getMergeResult();
		MergeStatus mergeStatus = mergeResult.getMergeStatus();

		// How did the merge go?
		switch( mergeStatus ) {
		case ALREADY_UP_TO_DATE:
		case FAST_FORWARD:
		case FAST_FORWARD_SQUASHED:
		case MERGED:
		case MERGED_SQUASHED:
			// Yeah! Everything is good!
			break;
		case MERGED_NOT_COMMITTED:
		case MERGED_SQUASHED_NOT_COMMITTED:
			// Merged, but we need to commit
			this.git.commit()
					.setAuthor( this.username, this.email )
					.call();
			break;
		case CONFLICTING:
			// We got conflicts!
			this.resolveMerge();
			break;
		case CHECKOUT_CONFLICT:
		case ABORTED:
		case FAILED:
		case NOT_SUPPORTED:
			// something went wrong
			throw new IllegalStateException( "invalid merge state: " + mergeStatus );
		default:
			throw new IllegalArgumentException( "unknown merge status state: " + mergeStatus );
		}
	}

	private void resolveMerge() throws NoWorkTreeException, GitAPIException, IOException {
		assert this.username != null;
		assert this.email != null;

		Status status = this.git.status().call();
		Map<String, StageState> conflicting = status.getConflictingStageState();

		for( String path : conflicting.keySet() ) {
			StageState stageState = conflicting.get( path );
			switch( stageState ) {
			case BOTH_MODIFIED: // UU
			case BOTH_ADDED: // AA
			case ADDED_BY_US: // AU
			case ADDED_BY_THEM: // UA
				// Both the local and server version have been modified
				File conflictingFile = new File( this.repoDir, path );
				String fullPath = conflictingFile.getAbsolutePath();

				// Since the local copy was modified it probably makes sense to leave it
				// since that's the copy the user has been working on. Here's my assumption...
				// a sync didn't happen, so the user opens their project and sees it's not their
				// latest changes, they accept the failure and start to fix it... finally a sync
				// happens... at this point they are probably editing this world, so when they save
				// they wouldn't even load the new file, so we should just keep the old file.

				// TODO: we should really prompt the user to resolve this conflict.
				// but that's kinda hard with the singletons... because you probably just want
				// to open both files in two different windows (editors) but we can't do that. :(

				// Recover server version
				this.git.checkout()
						.setStage( Stage.THEIRS )
						.addPath( path )
						.call();

				// Append a timestamp
				LocalDateTime date = LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "YYYY-mm-dd+HH'h'MM'm'" );
				String timestamp = date.format( formatter );
				File theirFile = new File( FilenameUtils.getFullPath( fullPath ), FilenameUtils.getBaseName( path ) + " (" + timestamp + ")." + FilenameUtils.getExtension( path ) );

				if( conflictingFile.exists() && !theirFile.exists() ) {
					Files.move( conflictingFile.toPath(), theirFile.toPath() );

					String relativePath = this.repoDir.toURI().relativize( theirFile.toURI() ).getPath();
					this.git.add()
							.addFilepattern( relativePath )
							.call();
				}

				// Recover local version
				this.git.checkout()
						.setStage( Stage.OURS )
						.addPath( path )
						.call();
				this.git.add()
						.addFilepattern( path )
						.call();
				break;

			case DELETED_BY_US: // DU
				// The modified local version is already in the checkout, so it just needs to be added.
				// We need to specifically mention the file, so we can't reuse the Add () method
				this.git.add()
						.addFilepattern( path )
						.call();
				break;
			case DELETED_BY_THEM: // UD
				// Recover server version
				this.git.checkout()
						.setStage( Stage.THEIRS )
						.addPath( path )
						.call();
				this.git.add()
						.addFilepattern( path )
						.call();
				break;
			case BOTH_DELETED: // DD
				break;
			default:
				throw new IllegalArgumentException( "Unknown StageState: " + stageState );
			}
		}

		RepositoryState resolvedState = this.git.getRepository().getRepositoryState();
		assert resolvedState == RepositoryState.MERGING_RESOLVED;

		// we are done resolving the merge!
		this.git.commit()
				.setAuthor( this.username, this.email )
				.call();

		RepositoryState safeState = this.git.getRepository().getRepositoryState();
		assert safeState == RepositoryState.SAFE;
	}

	private void push() throws GitAPIException, IOException, CommunityRepositoryException {
		assert this.credentials != null;
		assert this.remoteName != null;

		Iterable<PushResult> results = this.git.push()
				.setCredentialsProvider( this.credentials )
				.setRemote( this.remoteName )
				.call();

		for( PushResult result : results ) {
			for( final RemoteRefUpdate rru : result.getRemoteUpdates() ) {
				// Find the push that matches our current branch to make sure it made it to the server.
				if( this.git.getRepository().getFullBranch().equals( rru.getRemoteName() ) ) {
					if( ( rru.getStatus() == RemoteRefUpdate.Status.OK ) ||
							( rru.getStatus() == RemoteRefUpdate.Status.UP_TO_DATE ) ) {
						// everything went well...
					} else {
						throw new CommunityRepositoryException( "push failed: " + rru.getStatus() );
					}
				}
			}
		}
	}

	private void lockRepo() throws IOException {
		this.syncLockChannel = FileChannel.open( Paths.get( syncLockPath ), StandardOpenOption.WRITE, StandardOpenOption.CREATE );
		FileLock lock = this.syncLockChannel.lock(); // gets an exclusive lock
		assert lock.isValid();
		this.syncLockChannel.write( ByteBuffer.wrap( ManagementFactory.getRuntimeMXBean().getName().getBytes() ) );
	}

	private void unlockRepo() {
		try {
			this.syncLockChannel.close();
		} catch( IOException e ) {
			// This shouldn't ever happen
			Logger.throwable( e, this );
		}
	}

	private static boolean hasGitRepository( File dir ) {
		return RepositoryCache.FileKey.isGitRepository( new File( dir, ".git" ), FS.DETECTED );
	}

	private static boolean hasAtLeastOneReference( Repository repo ) {
		for( Ref ref : repo.getAllRefs().values() ) {
			if( ref.getObjectId() == null ) {
				continue;
			}
			return true;
		}
		return false;
	}
}
