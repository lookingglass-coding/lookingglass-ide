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

package edu.cmu.cs.dennisc.render.gl.imp.adapters;

import edu.cmu.cs.dennisc.render.gl.imp.RenderContext;
import edu.cmu.cs.dennisc.texture.Texture;

/**
 * @author Dennis Cosgrove
 */
public class GlrTexturedAppearance extends GlrSimpleAppearance<edu.cmu.cs.dennisc.scenegraph.TexturedAppearance> {
	@Override
	public boolean isAlphaBlended() {
		return super.isAlphaBlended() || this.isDiffuseColorTextureAlphaBlended;
	}

	@Override
	public void setPipelineState( RenderContext rc, int face ) {
		super.setPipelineState( rc, face );
		setTexturePipelineState( rc );
	}

	public void setTexturePipelineState( RenderContext rc ) {
		rc.setDiffuseColorTextureAdapter( this.diffuseColorTextureAdapter, this.isDiffuseColorTextureClamped );
		rc.setBumpTextureAdapter( this.bumpTextureAdapter );
	}

	@Override
	protected void handleReleased() {
		super.handleReleased();
		if( ( this.diffuseColorTextureAdapter != null ) && ( this.diffuseColorTextureAdapter.owner != null ) ) {
			this.diffuseColorTextureAdapter.removeReference();
			if( !this.diffuseColorTextureAdapter.isReferenced() ) {
				this.diffuseColorTextureAdapter.handleReleased();
			}
		}
		if( ( this.bumpTextureAdapter != null ) && ( this.bumpTextureAdapter.owner != null ) ) {
			this.bumpTextureAdapter.removeReference();
			this.bumpTextureAdapter.handleReleased();
			if( !this.bumpTextureAdapter.isReferenced() ) {
				this.bumpTextureAdapter.handleReleased();
			}
		}
	}

	@Override
	protected void propertyChanged( edu.cmu.cs.dennisc.property.InstanceProperty<?> property ) {
		if( property == owner.diffuseColorTexture ) {
			GlrTexture<? extends Texture> newAdapter = AdapterFactory.getAdapterFor( owner.diffuseColorTexture.getValue() );

			if( this.diffuseColorTextureAdapter != newAdapter ) {
				if( this.diffuseColorTextureAdapter != null ) {
					//It's possible to have multiple property owners reference the same diffuseColorTexture
					//One test case is a world with two grounds--they both reference the same GRASS texture
					this.diffuseColorTextureAdapter.removeReference();
					if( !this.diffuseColorTextureAdapter.isReferenced() ) {
						this.diffuseColorTextureAdapter.handleReleased();
					}
				}
				this.diffuseColorTextureAdapter = newAdapter;
				if( this.diffuseColorTextureAdapter != null ) {
					this.diffuseColorTextureAdapter.addReference();
				}
			}
		} else if( property == owner.isDiffuseColorTextureAlphaBlended ) {
			this.isDiffuseColorTextureAlphaBlended = owner.isDiffuseColorTextureAlphaBlended.getValue();
		} else if( property == owner.isDiffuseColorTextureClamped ) {
			this.isDiffuseColorTextureClamped = owner.isDiffuseColorTextureClamped.getValue();
		} else if( property == owner.bumpTexture ) {
			GlrTexture<? extends Texture> newAdapter = AdapterFactory.getAdapterFor( owner.bumpTexture.getValue() );
			if( this.bumpTextureAdapter != newAdapter ) {
				if( this.bumpTextureAdapter != null ) {
					this.bumpTextureAdapter.removeReference();
					if( !this.bumpTextureAdapter.isReferenced() ) {
						this.bumpTextureAdapter.handleReleased();
					}
				}
				this.bumpTextureAdapter = newAdapter;
				if( this.bumpTextureAdapter != null ) {
					this.bumpTextureAdapter.addReference();
				}
			}
		} else if( property == owner.textureId ) {
			edu.cmu.cs.dennisc.java.util.logging.Logger.todo( "handle textureId?", property.getValue(), this.owner.hashCode(), this.owner );
		} else {
			super.propertyChanged( property );
		}
	}

	private GlrTexture<?> diffuseColorTextureAdapter;
	private boolean isDiffuseColorTextureAlphaBlended;
	private boolean isDiffuseColorTextureClamped;
	private GlrTexture<?> bumpTextureAdapter;
}
