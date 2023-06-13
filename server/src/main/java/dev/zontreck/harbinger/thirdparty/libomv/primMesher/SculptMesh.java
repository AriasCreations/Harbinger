/**
 * Copyright (c) 2010-2012, Dahlia Trimble
 * Copyright (c) 2011-2017, Frederick Martian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or dev.zontreck.harbinger.thirdparty.libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import dev.zontreck.harbinger.thirdparty.libomv.primMesher.types.Face;
import dev.zontreck.harbinger.thirdparty.libomv.primMesher.types.ViewerFace;
import dev.zontreck.harbinger.thirdparty.libomv.types.Quaternion;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector2;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3;

public class SculptMesh implements Cloneable
{
    public ArrayList<Vector3> coords;
    public ArrayList<Face> faces;

    public ArrayList<ViewerFace> viewerFaces;
    public ArrayList<Vector3> normals;
    public ArrayList<Vector2> uvs;

    public enum SculptType
    {
    	sphere, torus, plane, cylinder;
    
    	public static SculptType valueOf(final int value)
    	{
    		return SculptType.values()[value];
    	}
    }

	/**
     * ** Experimental ** May disappear from future versions ** not recommeneded for use in applications
     * Construct a sculpt mesh from a 2D array of floats
     *
     * @param zMap
     * @param xBegin
     * @param xEnd
     * @param yBegin
     * @param yEnd
     * @param viewerMode
     */
    public SculptMesh(final float[][] zMap, final float xBegin, final float xEnd, final float yBegin, final float yEnd, final boolean viewerMode)
    {
        final float xStep;
        final float yStep;
        final float uStep;
        final float vStep;

        final int numYElements = zMap.length;
        final int numXElements = zMap[0].length;

        try
        {
            xStep = (xEnd - xBegin) / (numXElements - 1);
            yStep = (yEnd - yBegin) / (numYElements - 1);

            uStep = 1.0f / (numXElements - 1);
            vStep = 1.0f / (numYElements - 1);
        }
        catch (final Exception ex)
        {
            return;
        }

        this.coords = new ArrayList<Vector3>();
        this.faces = new ArrayList<Face>();
        this.normals = new ArrayList<Vector3>();
        this.uvs = new ArrayList<Vector2>();

        this.viewerFaces = new ArrayList<ViewerFace>();

        int p1, p2, p3, p4;

        int x, y;
        final int xStart = 0;
        final int yStart = 0;

        for (y = yStart; y < numYElements; y++)
        {
            final int rowOffset = y * numXElements;

            for (x = xStart; x < numXElements; x++)
            {
                /*
                *   p1-----p2
                *   | \ f2 |
                *   |   \  |
                *   | f1  \|
                *   p3-----p4
                */

                p4 = rowOffset + x;
                p3 = p4 - 1;

                p2 = p4 - numXElements;
                p1 = p3 - numXElements;

                final Vector3 c = new Vector3(xBegin + x * xStep, yBegin + y * yStep, zMap[y][x]);
                coords.add(c);
                if (viewerMode)
                {
                    normals.add(new Vector3(0.0f));
                    uvs.add(new Vector2(uStep * x, 1.0f - vStep * y));
                }

                if (0 < y && 0 < x)
                {
                    final Face f1;
                    final Face f2;

                    if (viewerMode)
                    {
                        f1 = new Face(p1, p4, p3, p1, p4, p3);
                        f1.uv1 = p1;
                        f1.uv2 = p4;
                        f1.uv3 = p3;

                        f2 = new Face(p1, p2, p4, p1, p2, p4);
                        f2.uv1 = p1;
                        f2.uv2 = p2;
                        f2.uv3 = p4;
                    }
                    else
                    {
                        f1 = new Face(p1, p4, p3);
                        f2 = new Face(p1, p2, p4);
                    }

                    faces.add(f1);
                    faces.add(f2);
                }
            }
        }

        if (viewerMode)
            this.calcVertexNormals(SculptType.plane, numXElements, numYElements);
    }

    public SculptMesh(final ArrayList<ArrayList<Vector3>> rows, final SculptType sculptType, final boolean viewerMode, final boolean mirror, final boolean invert)
    {
        this._SculptMesh(rows, sculptType, viewerMode, mirror, invert);
    }

    void _SculptMesh(final ArrayList<ArrayList<Vector3>> rows, final SculptType sculptType, final boolean viewerMode, final boolean mirror, boolean invert)
    {
        this.coords = new ArrayList<Vector3>();
        this.faces = new ArrayList<Face>();
        this.normals = new ArrayList<Vector3>();
        this.uvs = new ArrayList<Vector2>();

        if (mirror)
            invert = !invert;

        this.viewerFaces = new ArrayList<ViewerFace>();

        final int height = rows.size();
        final int width = rows.get(0).size();

        int p1, p2, p3, p4;

        int imageX, imageY;

        if (SculptType.plane != sculptType)
        {
            if (0 == height % 2)
            {
                for (int i = 0; i < rows.size(); i++)
                    rows.get(i).add(rows.get(i).get(0));
            }
            else
            {
                final int lastIndex = width - 1;

                for (int i = 0; i < rows.size(); i++)
                	rows.get(i).set(0, rows.get(i).get(lastIndex));
            }
        }

        final Vector3 topPole = rows.get(0).get(width / 2);
        final Vector3 bottomPole = rows.get(height - 1).get(width / 2);
        if (SculptType.sphere == sculptType)
        {
            if (0 == height % 2)
            {
                final ArrayList<Vector3> topPoleRow = new ArrayList<Vector3>(width);
                final ArrayList<Vector3> bottomPoleRow = new ArrayList<Vector3>(width);

                for (int i = 0; i < height; i++)
                {
                    topPoleRow.add(topPole);
                    bottomPoleRow.add(bottomPole);
                }
                rows.add(0, topPoleRow);
                rows.add(bottomPoleRow);
            }
            else
            {
            	final ArrayList<Vector3> topPoleRow = rows.get(0);
            	final ArrayList<Vector3> bottomPoleRow = rows.get(height - 1);

                for (int i = 0; i < height; i++)
                {
                    topPoleRow.set(i, topPole);
                    bottomPoleRow.set(i, bottomPole);
                }
            }
        }

        if (SculptType.torus == sculptType)
            rows.add(rows.get(0));

        final int coordsDown = rows.size();
        final int coordsAcross = rows.get(0).size();

        final float widthUnit = 1.0f / (coordsAcross - 1);
        final float heightUnit = 1.0f / (coordsDown - 1);

        for (imageY = 0; imageY < coordsDown; imageY++)
        {
            final int rowOffset = imageY * coordsAcross;

            for (imageX = 0; imageX < coordsAcross; imageX++)
            {
                /*
                *   p1-----p2
                *   | \ f2 |
                *   |   \  |
                *   | f1  \|
                *   p3-----p4
                */

                p4 = rowOffset + imageX;
                p3 = p4 - 1;

                p2 = p4 - coordsAcross;
                p1 = p3 - coordsAcross;

                coords.add(rows.get(imageY).get(imageX));
                if (viewerMode)
                {
                    normals.add(new Vector3(0.0f));
                    uvs.add(new Vector2(widthUnit * imageX, heightUnit * imageY));
                }

                if (0 < imageY && 0 < imageX)
                {
                    final Face f1;
                    final Face f2;

                    if (viewerMode)
                    {
                        if (invert)
                        {
                            f1 = new Face(p1, p4, p3, p1, p4, p3);
                            f1.uv1 = p1;
                            f1.uv2 = p4;
                            f1.uv3 = p3;

                            f2 = new Face(p1, p2, p4, p1, p2, p4);
                            f2.uv1 = p1;
                            f2.uv2 = p2;
                            f2.uv3 = p4;
                        }
                        else
                        {
                            f1 = new Face(p1, p3, p4, p1, p3, p4);
                            f1.uv1 = p1;
                            f1.uv2 = p3;
                            f1.uv3 = p4;

                            f2 = new Face(p1, p4, p2, p1, p4, p2);
                            f2.uv1 = p1;
                            f2.uv2 = p4;
                            f2.uv3 = p2;
                        }
                    }
                    else
                    {
                        if (invert)
                        {
                            f1 = new Face(p1, p4, p3);
                            f2 = new Face(p1, p2, p4);
                        }
                        else
                        {
                            f1 = new Face(p1, p3, p4);
                            f2 = new Face(p1, p4, p2);
                        }
                    }

                    faces.add(f1);
                    faces.add(f2);
                }
            }
        }

        if (viewerMode)
            this.calcVertexNormals(sculptType, coordsAcross, coordsDown);
    }

    /**
     * Duplicates a SculptMesh object. All object properties are copied by value, including lists.
     */
    public SculptMesh copy()
    {
        return new SculptMesh(this);
    }

    public SculptMesh(final SculptMesh sm)
    {
        this.coords = new ArrayList<Vector3>(sm.coords);
        this.faces = new ArrayList<Face>(sm.faces);
        this.viewerFaces = new ArrayList<ViewerFace>(sm.viewerFaces);
        this.normals = new ArrayList<Vector3>(sm.normals);
        this.uvs = new ArrayList<Vector2>(sm.uvs);
    }

    private void calcVertexNormals(final SculptType sculptType, final int xSize, final int ySize)
    {  
    	// compute vertex normals by summing all the surface normals of all the triangles sharing
        // each vertex and then normalizing
        final int numFaces = faces.size();
        for (int i = 0; i < numFaces; i++)
        {
            final Face face = faces.get(i);
            final Vector3 surfaceNormal = face.surfaceNormal(coords);
            normals.set(face.n1, Vector3.add(normals.get(face.n1), surfaceNormal));
            normals.set(face.n2, Vector3.add(normals.get(face.n2), surfaceNormal));
            normals.set(face.n3, Vector3.add(normals.get(face.n3), surfaceNormal));
        }

        final int numNormals = normals.size();
        for (int i = 0; i < numNormals; i++)
            normals.set(i, Vector3.normalize(normals.get(i)));

        if (SculptType.plane != sculptType)
        { // blend the vertex normals at the cylinder seam
            for (int y = 0; y < ySize; y++)
            {
                final int rowOffset = y * xSize;
                final Vector3 vect = Vector3.add(normals.get(rowOffset), normals.get(rowOffset + xSize - 1)).normalize();
                
                normals.set(rowOffset, vect);
                normals.set(rowOffset + xSize - 1, vect);
            }
        }

        for (final Face face : faces)
        {
            final ViewerFace vf = new ViewerFace(0);
            vf.v1 = coords.get(face.v1);
            vf.v2 = coords.get(face.v2);
            vf.v3 = coords.get(face.v3);

            vf.coordIndex1 = face.v1;
            vf.coordIndex2 = face.v2;
            vf.coordIndex3 = face.v3;

            vf.n1 = normals.get(face.n1);
            vf.n2 = normals.get(face.n2);
            vf.n3 = normals.get(face.n3);

            vf.uv1 = uvs.get(face.uv1);
            vf.uv2 = uvs.get(face.uv2);
            vf.uv3 = uvs.get(face.uv3);

            viewerFaces.add(vf);
        }
    }

    /**
     * Adds a value to each XYZ vertex coordinate in the mesh
     * 
     * @param x
     * @param y
     * @param z
     */
    public void addPos(final float x, final float y, final float z)
    {
        int i;
        final int numVerts = coords.size();
        Vector3 vert;

        for (i = 0; i < numVerts; i++)
        {
            vert = coords.get(i);
            vert.X += x;
            vert.Y += y;
            vert.Z += z;
            coords.set(i, vert);
        }

        if (null != this.viewerFaces)
        {
            final int numViewerFaces = viewerFaces.size();

            for (i = 0; i < numViewerFaces; i++)
            {
                final ViewerFace v = viewerFaces.get(i);
                v.addPos(x, y, z);
                viewerFaces.set(i, v);
            }
        }
    }

    /**
     * Rotates the mesh
     * 
     * @param q
     */
    public void addRot(final Quaternion q)
    {
        int i;
        final int numVerts = coords.size();

        for (i = 0; i < numVerts; i++)
        	coords.set(i, Vector3.multiply(coords.get(i), q));

        final int numNormals = normals.size();
        for (i = 0; i < numNormals; i++)
        	normals.set(i, Vector3.multiply(normals.get(i), q));

        if (null != this.viewerFaces)
        {
            final int numViewerFaces = viewerFaces.size();

            for (i = 0; i < numViewerFaces; i++)
            {
                final ViewerFace v = viewerFaces.get(i);
                v.v1 = Vector3.multiply(v.v1, q);
                v.v2 = Vector3.multiply(v.v2, q);
                v.v3 = Vector3.multiply(v.v3, q);

                v.n1 = Vector3.multiply(v.n1, q);
                v.n2 = Vector3.multiply(v.n2, q);
                v.n3 = Vector3.multiply(v.n3, q);
                viewerFaces.set(i, v);
            }
        }
    }

    public void scale(final float x, final float y, final float z)
    {
        int i;
        final int numVerts = coords.size();

        final Vector3 m = new Vector3(x, y, z);
        for (i = 0; i < numVerts; i++)
            coords.set(i, Vector3.multiply(coords.get(i), m));

        if (null != this.viewerFaces)
        {
            final int numViewerFaces = viewerFaces.size();
            for (i = 0; i < numViewerFaces; i++)
            {
                final ViewerFace v = viewerFaces.get(i);
                v.v1 = Vector3.multiply(v.v1, m);
                v.v2 = Vector3.multiply(v.v2, m);
                v.v3 = Vector3.multiply(v.v3, m);
                viewerFaces.set(i, v);
            }
        }
    }

    public void dumpRaw(final String path, final String name, final String title) throws IOException
    {
        if (null == path)
            return;
        final String fileName = name + "_" + title + ".raw";
        final File completePath = new File(path, fileName);
        final FileWriter sw = new FileWriter(completePath, StandardCharsets.UTF_8);

        for (int i = 0; i < faces.size(); i++)
        {
            sw.append(coords.get(faces.get(i).v1).toString() + " ");
            sw.append(coords.get(faces.get(i).v2).toString() + " ");
            sw.append(coords.get(faces.get(i).v3).toString() + "\n");
        }
        sw.close();
    }
}
