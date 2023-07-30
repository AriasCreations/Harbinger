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
package dev.zontreck.harbinger.thirdparty.libomv.primMesher;

import dev.zontreck.harbinger.thirdparty.libomv.primMesher.types.ViewerPolygon;
import dev.zontreck.harbinger.thirdparty.libomv.primMesher.types.ViewerVertex;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector2;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3;
import dev.zontreck.harbinger.thirdparty.libomv.utils.HashMapInt;
import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ObjMesh {
	public String meshName = Helpers.EmptyString;
	public ArrayList<ArrayList<ViewerVertex>> viewerVertices = new ArrayList<ArrayList<ViewerVertex>> ( );
	public ArrayList<ArrayList<ViewerPolygon>> viewerPolygons = new ArrayList<ArrayList<ViewerPolygon>> ( );
	public int numPrimFaces;
	ArrayList<Vector3> coords = new ArrayList<Vector3> ( );
	ArrayList<Vector3> normals = new ArrayList<Vector3> ( );
	ArrayList<Vector2> uvs = new ArrayList<Vector2> ( );
	ArrayList<ViewerVertex> faceVertices = new ArrayList<ViewerVertex> ( );
	ArrayList<ViewerPolygon> facePolygons = new ArrayList<ViewerPolygon> ( );
	HashMapInt<Integer> viewerVertexLookup = new HashMapInt<Integer> ( );

	public ObjMesh ( final String path ) throws IOException {
		final BufferedReader br = new BufferedReader ( new FileReader ( new File ( path ) , StandardCharsets.UTF_8 ) );
		try {
			this.processStream ( br );
		} finally {
			br.close ( );
		}
	}

	public ObjMesh ( final Reader sr ) throws IOException {
		final BufferedReader br = sr instanceof BufferedReader ? ( BufferedReader ) sr : new BufferedReader ( sr );
		this.processStream ( br );
	}

	private void processStream ( final BufferedReader s ) throws IOException {
		this.numPrimFaces = 0;
		String line;
		do {
			line = s.readLine ( ).trim ( );
			if ( null != line ) {
				final String[] tokens = line.split ( " +" );

				// Skip blank lines and comments
				if ( 0 < tokens.length && tokens[ 0 ].isEmpty ( ) && ! tokens[ 0 ].startsWith ( "#" ) )
					this.processTokens ( tokens );
			}
		}
		while ( null != line );
		this.makePrimFace ( );
	}

	public VertexIndexer getVertexIndexer ( ) {
		final VertexIndexer vi = new VertexIndexer ( );
		vi.numPrimFaces = numPrimFaces;
		vi.viewerPolygons = viewerPolygons;
		vi.viewerVertices = viewerVertices;

		return vi;
	}


	private void processTokens ( final String[] tokens ) {
		final String token = tokens[ 0 ].toLowerCase ( );
		if ( "o".equals ( token ) ) {
			this.meshName = tokens[ 1 ];
		}
		else if ( "v".equals ( token ) ) {
			this.coords.add ( this.parseCoord ( tokens ) );
		}
		else if ( "vt".equals ( token ) ) {
			this.uvs.add ( this.parseUVCoord ( tokens ) );
		}
		else if ( "vn".equals ( token ) ) {
			this.normals.add ( this.parseCoord ( tokens ) );
		}
		else if ( "g".equals ( token ) ) {
			this.makePrimFace ( );
		}
		else if ( "s".equals ( token ) ) {

		}
		else if ( "f".equals ( token ) ) {
			final int[] vertIndices = new int[ 3 ];

			for ( int vertexIndex = 1 ; 3 >= vertexIndex ; vertexIndex++ ) {
				final String[] indices = tokens[ vertexIndex ].split ( "/" );

				final int positionIndex = Integer.parseInt ( indices[ 0 ] ) - 1;
				int texCoordIndex = - 1;
				int normalIndex = - 1;

				if ( 1 < indices.length ) {
					try {
						texCoordIndex = Integer.parseInt ( indices[ 1 ] ) - 1;
					} catch (
							final NumberFormatException ex ) {
						texCoordIndex = - 1;
					}
				}

				if ( 2 < indices.length ) {
					try {
						normalIndex = Integer.parseInt ( indices[ 2 ] ) - 1;
					} catch (
							final NumberFormatException ex ) {
						normalIndex = - 1;
					}
				}

				final int hash = this.hashInts ( positionIndex , texCoordIndex , normalIndex );

				if ( this.viewerVertexLookup.containsKey ( hash ) )
					vertIndices[ vertexIndex - 1 ] = this.viewerVertexLookup.get ( hash );
				else {
					final ViewerVertex vv = new ViewerVertex ( );
					vv.v = this.coords.get ( positionIndex );
					if ( - 1 < normalIndex )
						vv.n = this.normals.get ( normalIndex );
					if ( - 1 < texCoordIndex )
						vv.uv = this.uvs.get ( texCoordIndex );
					this.faceVertices.add ( vv );
					vertIndices[ vertexIndex - 1 ] = this.faceVertices.size ( ) - 1;
					this.viewerVertexLookup.put ( hash , this.faceVertices.size ( ) - 1 );
				}
			}
			this.facePolygons.add ( new ViewerPolygon ( vertIndices[ 0 ] , vertIndices[ 1 ] , vertIndices[ 2 ] ) );
		}
		else if ( "mtllib".equals ( token ) ) {
		}
		else if ( "usemtl".equals ( token ) ) {
		}
		else {
		}
	}


	private void makePrimFace ( ) {
		if ( 0 < faceVertices.size ( ) && 0 < facePolygons.size ( ) ) {
			this.viewerVertices.add ( this.faceVertices );
			this.faceVertices = new ArrayList<ViewerVertex> ( );
			this.viewerPolygons.add ( this.facePolygons );

			this.facePolygons = new ArrayList<ViewerPolygon> ( );

			this.viewerVertexLookup = new HashMapInt<Integer> ( );

			this.numPrimFaces++;
		}
	}

	private Vector2 parseUVCoord ( final String[] tokens ) {
		return new Vector2 (
				Float.valueOf ( tokens[ 1 ] ) ,
				Float.valueOf ( tokens[ 2 ] )
		);
	}

	private Vector3 parseCoord ( final String[] tokens ) {
		return new Vector3 (
				Float.valueOf ( tokens[ 1 ] ) ,
				Float.valueOf ( tokens[ 2 ] ) ,
				Float.valueOf ( tokens[ 3 ] )
		);
	}

	private int hashInts ( final int i1 , final int i2 , final int i3 ) {
		return ( i1 + " " + i2 + " " + i3 ).hashCode ( );
	}
}