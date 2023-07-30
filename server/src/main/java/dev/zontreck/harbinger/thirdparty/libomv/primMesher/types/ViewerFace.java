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

import dev.zontreck.harbinger.thirdparty.libomv.types.Quaternion;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector2;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3;

public class ViewerFace {
	public int primFaceNumber;

	public Vector3 v1;
	public Vector3 v2;
	public Vector3 v3;

	public int coordIndex1;
	public int coordIndex2;
	public int coordIndex3;

	public Vector3 n1;
	public Vector3 n2;
	public Vector3 n3;

	public Vector2 uv1;
	public Vector2 uv2;
	public Vector2 uv3;

	public ViewerFace ( final int primFaceNumber ) {
		this.primFaceNumber = primFaceNumber;

		v1 = new Vector3 ( 0 );
		v2 = new Vector3 ( 0 );
		v3 = new Vector3 ( 0 );

		coordIndex1 = coordIndex2 = coordIndex3 = - 1; // -1 means not assigned yet

		n1 = new Vector3 ( 0 );
		n2 = new Vector3 ( 0 );
		n3 = new Vector3 ( 0 );

		uv1 = new Vector2 ( );
		uv2 = new Vector2 ( );
		uv3 = new Vector2 ( );
	}

	public void scale ( final float x , final float y , final float z ) {
		v1.X *= x;
		v1.Y *= y;
		v1.Z *= z;

		v2.X *= x;
		v2.Y *= y;
		v2.Z *= z;

		v3.X *= x;
		v3.Y *= y;
		v3.Z *= z;
	}

	public void addPos ( final float x , final float y , final float z ) {
		v1.X += x;
		v2.X += x;
		v3.X += x;

		v1.Y += y;
		v2.Y += y;
		v3.Y += y;

		v1.Z += z;
		v2.Z += z;
		v3.Z += z;
	}

	public void addRot ( final Quaternion rot ) {
		v1.multiply ( rot );
		v2.multiply ( rot );
		v3.multiply ( rot );

		n1.multiply ( rot );
		n2.multiply ( rot );
		n3.multiply ( rot );
	}

	public void calcSurfaceNormal ( ) {

		final Vector3 edge1 = new Vector3 ( v2.X - v1.X , v2.Y - v1.Y , v2.Z - v1.Z );
		final Vector3 edge2 = new Vector3 ( v3.X - v1.X , v3.Y - v1.Y , v3.Z - v1.Z );

		n1 = edge1.cross ( edge2 ).normalize ( );
		n2 = new Vector3 ( n1 );
		n3 = new Vector3 ( n1 );
	}
}
