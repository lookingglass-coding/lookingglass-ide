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

import java.io.IOException;

public abstract class JsonMultipartPacket extends JsonPacket {

	/* package-private */static final String MULTIPART_BOUNDARY = java.util.UUID.randomUUID().toString();
	/* package-private */static final String CRLF = "\r\n";
	private final String PACKET_NAME = this.getClass().getSimpleName();

	private final java.util.ArrayList<HttpMultipartPart> attachments = new java.util.ArrayList<HttpMultipartPart>();

	@Override
	public String getContentType() {
		if( this.attachments.isEmpty() ) {
			return super.getContentType();
		} else {
			return "multipart/related; boundary=" + MULTIPART_BOUNDARY + "; type=application/json; start=" + PACKET_NAME + ".json";
		}
	}

	@Override
	public byte[] getPayload( com.google.gson.Gson jsonSerializer ) throws java.io.IOException {
		if( this.attachments.isEmpty() ) {
			return super.getPayload( jsonSerializer );
		} else {
			return assembleMultipartBody( new HttpMultipartPart( PACKET_NAME + ".json", "application/json; charset=UTF-8", null, super.getPayload( jsonSerializer ) ) );
		}
	}

	protected byte[] addAttachment( String name, String mimeType, byte[] content, String filename ) throws java.io.IOException {
		// Check to see if the attachment still exists, and this is a replacement
		for( HttpMultipartPart part : this.attachments ) {
			if( part.name.equals( name ) ) {
				this.attachments.remove( part );
			}
		}
		this.attachments.add( new HttpMultipartPart( name, mimeType, filename, content ) );

		return content;
	}

	protected byte[] addAttachment( String name, String mimeType, java.net.URI uri, String filename ) throws java.io.IOException {
		java.io.File file = new java.io.File( uri );
		java.io.FileInputStream fin = new java.io.FileInputStream( file );
		byte content[] = new byte[ (int)file.length() ];
		fin.read( content );
		fin.close();
		return addAttachment( name, mimeType, content, filename );
	}

	protected void clearAttachments() {
		this.attachments.clear();
	}

	/**
	 * Assembles a HTTP Multipart Request.
	 * http://www.w3.org/Protocols/rfc1341/7_2_Multipart.html
	 *
	 * @param json
	 * @return Multipart request
	 * @throws IOException
	 */
	private byte[] assembleMultipartBody( HttpMultipartPart json ) throws java.io.IOException {
		java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		try {
			// Write the JSON request first
			json.addPart( bos );

			// Add all attachments to the multipart request
			for( HttpMultipartPart part : this.attachments ) {
				part.addPart( bos );
			}

			bos.write( ( "--" + MULTIPART_BOUNDARY + "--" ).getBytes() );
		} finally {
			bos.close();
		}
		return bos.toByteArray();
	}
}
