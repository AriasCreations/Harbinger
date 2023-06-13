/**
 * Copyright (c) 2010-2012, Dahlia Trimble
 * Copyright (c) 2011-2017, Frederick Martian
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
package dev.zontreck.harbinger.thirdparty.libomv.primMesher.types;

import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3;

import java.util.ArrayList;

public class Face {
	public int primFace;

	// vertices
	public int v1;
	public int v2;
	public int v3;

	//normals
	public int n1;
	public int n2;
	public int n3;

	// uvs
	public int uv1;
	public int uv2;
	public int uv3;

	public Face ( ) {
		this ( 0 , 0 , 0 );
	}

	public Face ( final int v1 , final int v2 , final int v3 ) {
		this ( v1 , v2 , v3 , 0 , 0 , 0 );
	}

	public Face ( final int v1 , final int v2 , final int v3 , final int n1 , final int n2 , final int n3 ) {
		this.primFace = 0;

		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;

		this.n1 = n1;
		this.n2 = n2;
		this.n3 = n3;

		uv1 = 0;
		uv2 = 0;
		uv3 = 0;
	}

	public Face ( final Face face ) {
		this.primFace = face.primFace;

		v1 = face.v1;
		v2 = face.v2;
		v3 = face.v3;

		n1 = face.n1;
		n2 = face.n2;
		n3 = face.n3;

		uv1 = face.uv1;
		uv2 = face.uv2;
		uv3 = face.uv3;
	}

	public Vector3 surfaceNormal ( final ArrayList<Vector3> coordList ) {
		final Vector3 c1 = coordList.get ( v1 );
		final Vector3 c2 = coordList.get ( v2 );
		final Vector3 c3 = coordList.get ( v3 );

		final Vector3 edge1 = new Vector3 ( c2.X - c1.X , c2.Y - c1.Y , c2.Z - c1.Z );
		final Vector3 edge2 = new Vector3 ( c3.X - c1.X , c3.Y - c1.Y , c3.Z - c1.Z );

		return edge1.cross ( edge2 ).normalize ( );
	}
}
