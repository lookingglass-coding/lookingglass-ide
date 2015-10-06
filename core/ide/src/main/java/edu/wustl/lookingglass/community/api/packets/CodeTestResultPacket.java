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

public class CodeTestResultPacket extends edu.wustl.lookingglass.community.api.packets.JsonPacket {

	public static enum CodeTestResultStatus {
		CREATED( "Created", 0 ),
		SUBMITTED( "Submitted", 1 ),
		EXECUTING( "Executing", 2 ),
		TERMINATED( "Terminated", 3 ),
		FAILED( "Failed", 4 );

		private final String name;
		private final Integer id;

		private CodeTestResultStatus( String name, Integer id ) {
			this.name = name;
			this.id = id;
		}

		public Integer getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public static CodeTestResultStatus getType( Integer id ) {
			switch( id ) {
			case 0:
				return CodeTestResultStatus.CREATED;
			case 1:
				return CodeTestResultStatus.SUBMITTED;
			case 2:
				return CodeTestResultStatus.EXECUTING;
			case 3:
				return CodeTestResultStatus.TERMINATED;
			case 4:
				return CodeTestResultStatus.FAILED;
			default:
				return null;
			}
		}
	}

	@Expose private InnerCodeTestResult code_test_result;

	/* package-private */static class InnerCodeTestResult {
		@Expose( serialize = false ) Integer id;

		@Expose( serialize = false ) Integer code_test_id;

		@Expose( serialize = false ) Integer world_id;

		@Expose( serialize = false ) Integer status_cd;

		@Expose( serialize = false ) String exception;

		@Expose( serialize = false ) Integer fail_count;

		@Expose( serialize = false ) Integer success_count;

		@Expose( serialize = false ) org.joda.time.DateTime created_at;

		@Expose( serialize = false ) org.joda.time.DateTime updated_at;
	}

	@Override
	public boolean isValid() {
		return ( this.code_test_result != null ) && ( this.getStatus() != null );
	}

	public Integer getId() {
		return this.code_test_result.id;
	}

	public CodeTestResultStatus getStatus() {
		return CodeTestResultStatus.getType( this.code_test_result.status_cd );
	}

	public String getException() {
		return this.code_test_result.exception;
	}

	public Integer getWorldId() {
		return this.code_test_result.world_id;
	}

	public Integer getCodeTestId() {
		return this.code_test_result.code_test_id;
	}

	public Integer getFailCount() {
		return this.code_test_result.fail_count;
	}

	public Integer getSuccessCount() {
		return this.code_test_result.success_count;
	}

	public org.joda.time.DateTime getCreatedAt() {
		return this.code_test_result.created_at;
	}

	public org.joda.time.DateTime getUpdatedAt() {
		return this.code_test_result.updated_at;
	}
}
