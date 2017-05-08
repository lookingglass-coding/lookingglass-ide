/*******************************************************************************
 * Copyright (c) 2008, 2016, Washington University in St. Louis.
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
package edu.wustl.lookingglass.croquetfx.scene;

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;

/**
 * @author Kyle J. Harms
 */
public interface VLCPlayerEventListener extends MediaPlayerEventListener {
	@Override
	default public void mediaChanged( MediaPlayer mediaPlayer, libvlc_media_t media, String mrl ) {
	}

	@Override
	default public void opening( MediaPlayer mediaPlayer ) {
	}

	@Override
	default public void buffering( MediaPlayer mediaPlayer, float newCache ) {
	}

	@Override
	default public void playing( MediaPlayer mediaPlayer ) {
	}

	@Override
	default public void paused( MediaPlayer mediaPlayer ) {
	}

	@Override
	default public void stopped( MediaPlayer mediaPlayer ) {
	}

	@Override
	default public void forward( MediaPlayer mediaPlayer ) {
	}

	@Override
	default public void backward( MediaPlayer mediaPlayer ) {
	}

	@Override
	default public void finished( MediaPlayer mediaPlayer ) {
	}

	@Override
	default public void timeChanged( MediaPlayer mediaPlayer, long newTime ) {
	}

	@Override
	default public void positionChanged( MediaPlayer mediaPlayer, float newPosition ) {
	}

	@Override
	default public void seekableChanged( MediaPlayer mediaPlayer, int newSeekable ) {
	}

	@Override
	default public void pausableChanged( MediaPlayer mediaPlayer, int newPausable ) {
	}

	@Override
	default public void titleChanged( MediaPlayer mediaPlayer, int newTitle ) {
	}

	@Override
	default public void snapshotTaken( MediaPlayer mediaPlayer, String filename ) {
	}

	@Override
	default public void lengthChanged( MediaPlayer mediaPlayer, long newLength ) {
	}

	@Override
	default public void videoOutput( MediaPlayer mediaPlayer, int newCount ) {
	}

	@Override
	default public void scrambledChanged( MediaPlayer mediaPlayer, int newScrambled ) {
	}

	@Override
	default public void elementaryStreamAdded( MediaPlayer mediaPlayer, int type, int id ) {
	}

	@Override
	default public void elementaryStreamDeleted( MediaPlayer mediaPlayer, int type, int id ) {
	}

	@Override
	default public void elementaryStreamSelected( MediaPlayer mediaPlayer, int type, int id ) {
	}

	@Override
	default public void corked( MediaPlayer mediaPlayer, boolean corked ) {
	}

	@Override
	default public void muted( MediaPlayer mediaPlayer, boolean muted ) {
	}

	@Override
	default public void volumeChanged( MediaPlayer mediaPlayer, float volume ) {
	}

	@Override
	default public void audioDeviceChanged( MediaPlayer mediaPlayer, String audioDevice ) {
	}

	// VLCJ 3.10.1
	//	@Override
	//	default public void chapterChanged( MediaPlayer mediaPlayer, int newChapter ) {
	//	}

	@Override
	default public void error( MediaPlayer mediaPlayer ) {
	}

	@Override
	default public void mediaMetaChanged( MediaPlayer mediaPlayer, int metaType ) {
	}

	@Override
	default public void mediaSubItemAdded( MediaPlayer mediaPlayer, libvlc_media_t subItem ) {
	}

	@Override
	default public void mediaDurationChanged( MediaPlayer mediaPlayer, long newDuration ) {
	}

	@Override
	default public void mediaParsedChanged( MediaPlayer mediaPlayer, int newStatus ) {
	}

	@Override
	default public void mediaFreed( MediaPlayer mediaPlayer ) {
	}

	@Override
	default public void mediaStateChanged( MediaPlayer mediaPlayer, int newState ) {
	}

	@Override
	default public void mediaSubItemTreeAdded( MediaPlayer mediaPlayer, libvlc_media_t item ) {
	}

	@Override
	default public void newMedia( MediaPlayer mediaPlayer ) {
	}

	@Override
	default public void subItemPlayed( MediaPlayer mediaPlayer, int subItemIndex ) {
	}

	@Override
	default public void subItemFinished( MediaPlayer mediaPlayer, int subItemIndex ) {
	}

	@Override
	default public void endOfSubItems( MediaPlayer mediaPlayer ) {
	}
}
