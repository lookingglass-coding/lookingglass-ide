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
package edu.wustl.lookingglass.community.api.packets;

import com.google.gson.annotations.Expose;

import edu.wustl.lookingglass.common.VersionNumber;

public class CapabilitiesPacket extends edu.wustl.lookingglass.community.api.packets.JsonPacket {

	@Expose( serialize = false ) private Capabilities capabilities;

	/* package-private */static class Capabilities {
		@Expose( serialize = false ) VersionNumber community_api_version;

		@Expose( serialize = false ) VersionNumber lookingglass_version;

		@Expose( serialize = false ) String downloads_path;

		@Expose( serialize = false ) String reset_password_path;

		@Expose( serialize = false ) String sign_up_path;

		@Expose( serialize = false ) String help_path;

		@Expose( serialize = false ) String open_tempate_tutorial_path;

		@Expose( serialize = false ) String animate_story_tutorial_path;

		@Expose( serialize = false ) String remix_actions_tutorial_path;

		@Expose( serialize = false ) String share_world_tutorial_path;

		@Expose( serialize = false ) ModulePacket[] module_packets;
	}

	@Override
	public boolean isValid() {
		return this.capabilities != null;
	}

	public VersionNumber getCommunityApiVersion() {
		return this.capabilities.community_api_version;
	}

	public VersionNumber getLatestLookingGlassVersion() {
		return this.capabilities.lookingglass_version;
	}

	public String getHelpPath() {
		return this.capabilities.help_path;
	}

	public String getResetPasswordPath() {
		return this.capabilities.reset_password_path;
	}

	public String getSignUpPath() {
		return this.capabilities.sign_up_path;
	}

	public String getDownloadPath() {
		return this.capabilities.downloads_path;
	}

	public String getOpenTemplateTutorialPath() {
		return this.capabilities.open_tempate_tutorial_path;
	}

	public String getAnimateStoryTutorialPath() {
		return this.capabilities.animate_story_tutorial_path;
	}

	public String getRemixActionsTutorialPath() {
		return this.capabilities.remix_actions_tutorial_path;
	}

	public String getShareWorldTutorialPath() {
		return this.capabilities.share_world_tutorial_path;
	}

	public ModulePacket[] getModulePackets() {
		return this.capabilities.module_packets;
	}
}
