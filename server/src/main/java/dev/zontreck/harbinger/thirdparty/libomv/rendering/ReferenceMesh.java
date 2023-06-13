/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2012-2017, Frederick Martian
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or dev.zontreck.harbinger.thirdparty.libomv-java project nor the
 * names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package dev.zontreck.harbinger.thirdparty.libomv.rendering;

import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3;
import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;
import org.apache.commons.io.input.SwappedDataInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ShortBuffer;

/**
 * A reference mesh is one way to implement level of detail
 *
 * @remarks Reference meshes are supplemental meshes to full meshes. For all practical
 * purposes almost all lod meshes are implemented as reference meshes, except for
 * 'avatar_eye_1.llm' which for some reason is implemented as a full mesh.
 */
public class ReferenceMesh {
	protected static final String MESH_HEADER = "Linden Binary Mesh 1.0";
	protected static final String MORPH_FOOTER = "End Morphs";
	public float MinPixelWidth;
	public ShortBuffer Indices;
	protected String _header;
	protected boolean _hasWeights;
	protected boolean _hasDetailTexCoords;
	protected Vector3 _position;
	protected Vector3 _rotationAngles;
	protected byte _rotationOrder;
	protected Vector3 _scale;
	protected short _numFaces;

	public String getHeader ( ) {
		return this._header;
	}

	public boolean getHasWeights ( ) {
		return this._hasWeights;
	}

	public boolean getHasDetailTexCoords ( ) {
		return this._hasDetailTexCoords;
	}

	public Vector3 getPosition ( ) {
		return this._position;
	}

	public Vector3 getRotationAngles ( ) {
		return this._rotationAngles;
	}

	public byte getRotationOrder ( ) {
		return this._rotationOrder;
	}

	public Vector3 getScale ( ) {
		return this._scale;
	}

	public short getNumFaces ( ) {
		return this._numFaces;
	}

	public Face getFace ( final int index ) {
		if ( index >= this._numFaces )
			return null;
		return new Face ( this.Indices , index * 3 );
	}

	public void load ( final String filename ) throws IOException {
		final InputStream stream = new FileInputStream ( filename );
		try {
			this.load ( stream );
		} finally {
			stream.close ( );
		}
	}

	public void load ( final InputStream stream ) throws IOException {
		final SwappedDataInputStream fis = new SwappedDataInputStream ( stream );

		this.load ( fis );

		this._numFaces = fis.readShort ( );
		this.Indices = ShortBuffer.allocate ( 3 * this._numFaces );
		for ( int i = 0 ; i < this._numFaces ; i++ ) {
			this.Indices.put ( fis.readShort ( ) );
			this.Indices.put ( fis.readShort ( ) );
			this.Indices.put ( fis.readShort ( ) );
		}
	}

	protected void load ( final SwappedDataInputStream fis ) throws IOException {
		this._header = Helpers.readString ( fis , 24 );
		if ( ! this._header.equals ( ReferenceMesh.MESH_HEADER ) )
			throw new IOException ( "Unrecognized mesh format" );

		// Populate base mesh variables
		this._hasWeights = 1 != fis.readByte ( );
		this._hasDetailTexCoords = 1 != fis.readByte ( );
		this._position = new Vector3 ( fis );
		this._rotationAngles = new Vector3 ( fis );
		this._rotationOrder = fis.readByte ( );
		this._scale = new Vector3 ( fis );
	}

	public class Face {
		public short Indices1;
		public short Indices2;
		public short Indices3;

		public Face ( final ShortBuffer indices , int idx ) {
			this.Indices1 = indices.get ( idx );
			idx++;
			this.Indices2 = indices.get ( idx );
			idx++;
			this.Indices3 = indices.get ( idx );
			idx++;
		}
	}
}
