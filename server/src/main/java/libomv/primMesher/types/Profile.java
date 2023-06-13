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
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
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
package libomv.primMesher.types;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

import libomv.types.Quaternion;
import libomv.types.Vector2;
import libomv.types.Vector3;

// generates a profile for extrusion
public class Profile
{
    private final float twoPi = 2.0f * (float)Math.PI;

    public String errorMessage;

    public ArrayList<Vector3> coords;
    public ArrayList<Face> faces;
    public ArrayList<Vector3> vertexNormals;
    public ArrayList<Float> us;
    public ArrayList<Vector2> faceUVs;
    public ArrayList<Integer> faceNumbers;

    // use these for making individual meshes for each prim face
    public ArrayList<Integer> outerCoordIndices;
    public ArrayList<Integer> hollowCoordIndices;
    public ArrayList<Integer> cut1CoordIndices;
    public ArrayList<Integer> cut2CoordIndices;

    public Vector3 faceNormal = new Vector3(0.0f, 0.0f, 1.0f);
    public Vector3 cutNormal1 = new Vector3(0.0f);
    public Vector3 cutNormal2 = new Vector3(0.0f);

    public int numOuterVerts;
    public int numHollowVerts;

    public int outerFaceNumber = -1;
    public int hollowFaceNumber = -1;

    public boolean calcVertexNormals;
    public int bottomFaceNumber;
    public int numPrimFaces;

    public Profile()
    {
        coords = new ArrayList<Vector3>();
        faces = new ArrayList<Face>();
        vertexNormals = new ArrayList<Vector3>();
        us = new ArrayList<Float>();
        faceUVs = new ArrayList<Vector2>();
        faceNumbers = new ArrayList<Integer>();
    }

    public Profile(final int sides, final float profileStart, final float profileEnd, final float hollow, final int hollowSides, final boolean createFaces, final boolean calcVertexNormals)
    {
        this.calcVertexNormals = calcVertexNormals;
        coords = new ArrayList<Vector3>();
        faces = new ArrayList<Face>();
        vertexNormals = new ArrayList<Vector3>();
        us = new ArrayList<Float>();
        faceUVs = new ArrayList<Vector2>();
        faceNumbers = new ArrayList<Integer>();

        final Vector3 center = new Vector3(0.0f, 0.0f, 0.0f);

        ArrayList<Vector3> hollowCoords = new ArrayList<Vector3>();
        ArrayList<Vector3> hollowNormals = new ArrayList<Vector3>();
        ArrayList<Float> hollowUs = new ArrayList<Float>();

        if (calcVertexNormals)
        {
            outerCoordIndices = new ArrayList<Integer>();
            hollowCoordIndices = new ArrayList<Integer>();
            cut1CoordIndices = new ArrayList<Integer>();
            cut2CoordIndices = new ArrayList<Integer>();
        }

        final boolean hasHollow = (0.0f < hollow);

        final boolean hasProfileCut = (0.0f < profileStart || 1.0f > profileEnd);

        final AngleList angles = new AngleList();
        AngleList hollowAngles = new AngleList();

        float xScale = 0.5f;
        float yScale = 0.5f;
        if (4 == sides)  // corners of a square are sqrt(2) from center
        {
            xScale = 0.707107f;
            yScale = 0.707107f;
        }

        final float startAngle = profileStart * this.twoPi;
        final float stopAngle = profileEnd * this.twoPi;

        try
        {
        	angles.makeAngles(sides, startAngle, stopAngle);
        }
        catch (final Exception ex)
        {
            this.errorMessage = String.format("makeAngles failed: Exception: %s\nsides: %f startAngle: %f stopAngle: %f",
                    ex, sides, startAngle, stopAngle);
            return;
        }

        numOuterVerts = angles.angles.size();

        // flag to create as few triangles as possible for 3 or 4 side profile
        final boolean simpleFace = (5 > sides && !hasHollow && !hasProfileCut);

        if (hasHollow)
        {
            if (sides == hollowSides)
                hollowAngles = angles;
            else
            {
                try
                { 
                	hollowAngles.makeAngles(hollowSides, startAngle, stopAngle); 
                }
                catch (final Exception ex)
                {
                    this.errorMessage = String.format("makeAngles failed: Exception: %s\nsides: %f startAngle: %f stopAngle: %f",
                            ex, sides, startAngle, stopAngle);
                    return;
                }
            }
            numHollowVerts = hollowAngles.angles.size();
        }
        else if (!simpleFace)
        {
            coords.add(center);
            if (this.calcVertexNormals)
                vertexNormals.add(new Vector3(0.0f, 0.0f, 1.0f));
            us.add(0.0f);
        }

        final float z = 0.0f;

        Angle angle;
        if (hasHollow && hollowSides != sides)
        {
            final int numHollowAngles = hollowAngles.angles.size();
            for (int i = 0; i < numHollowAngles; i++)
            {
                angle = hollowAngles.angles.get(i);
                hollowCoords.add(new Vector3(hollow * xScale * angle.X, hollow * yScale * angle.Y, z));
                if (this.calcVertexNormals)
                {
                    if (5 > hollowSides)
                        hollowNormals.add(Vector3.negate(hollowAngles.normals.get(i)));
                    else
                        hollowNormals.add(new Vector3(-angle.X, -angle.Y, 0.0f));

                    if (4 == hollowSides)
                        hollowUs.add(angle.angle * hollow * 0.707107f);
                    else
                        hollowUs.add(angle.angle * hollow);
                }
            }
        }

        int index = 0;
        final int numAngles = angles.angles.size();
        final Vector3 newVert = new Vector3(0.0f);
        for (int i = 0; i < numAngles; i++)
        {
            angle = angles.angles.get(i);
            newVert.X = angle.X * xScale;
            newVert.Y = angle.Y * yScale;
            newVert.Z = z;
            coords.add(new Vector3(newVert));
            if (this.calcVertexNormals)
            {
                outerCoordIndices.add(coords.size() - 1);

                if (5 > sides)
                {
                    vertexNormals.add(angles.normals.get(i));
                    final float u = angle.angle;
                    us.add(u);
                }
                else
                {
                    vertexNormals.add(new Vector3(angle.X, angle.Y, 0.0f));
                    us.add(angle.angle);
                }
            }

            if (hasHollow)
            {
                if (hollowSides == sides)
                {
                    newVert.X *= hollow;
                    newVert.Y *= hollow;
                    newVert.Z = z;
                    hollowCoords.add(new Vector3(newVert));
                    if (this.calcVertexNormals)
                    {
                        if (5 > sides)
                        {
                            hollowNormals.add(angles.normals.get(i).negate());
                        }

                        else
                            hollowNormals.add(new Vector3(-angle.X, -angle.Y, 0.0f));

                        hollowUs.add(angle.angle * hollow);
                    }
                }
            }
            else if (!simpleFace && createFaces && 0.0001f < angle.angle)
            {
                final Face newFace = new Face();
                newFace.v1 = 0;
                newFace.v2 = index;
                newFace.v3 = index + 1;

                faces.add(newFace);
            }
            index += 1;
        }

        if (hasHollow)
        {
        	Collections.reverse(hollowCoords);
            if (this.calcVertexNormals)
            {
                Collections.reverse(hollowNormals);
                Collections.reverse(hollowUs);
            }

            if (createFaces)
            {
                final int numTotalVerts = numOuterVerts + numHollowVerts;
                final Face newFace = new Face();

                if (numOuterVerts == numHollowVerts)
                {

                    for (int coordIndex = 0; coordIndex < numOuterVerts - 1; coordIndex++)
                    {
                        newFace.v1 = coordIndex;
                        newFace.v2 = coordIndex + 1;
                        newFace.v3 = numTotalVerts - coordIndex - 1;
                        faces.add(new Face(newFace));

                        newFace.v1 = coordIndex + 1;
                        newFace.v2 = numTotalVerts - coordIndex - 2;
                        newFace.v3 = numTotalVerts - coordIndex - 1;
                        faces.add(new Face(newFace));
                    }
                }
                else
                {
                    if (numOuterVerts < numHollowVerts)
                    {
                        int j = 0; // j is the index for outer vertices
                        final int maxJ = numOuterVerts - 1;
                        for (int i = 0; i < numHollowVerts; i++) // i is the index for inner vertices
                        {
                            if (j < maxJ)
                                if (angles.angles.get(j + 1).angle - hollowAngles.angles.get(i).angle < hollowAngles.angles.get(i).angle - angles.angles.get(j).angle + 0.000001f)
                                {
                                    newFace.v1 = numTotalVerts - i - 1;
                                    newFace.v2 = j;
                                    newFace.v3 = j + 1;

                                    faces.add(new Face(newFace));
                                    j += 1;
                                }

                            newFace.v1 = j;
                            newFace.v2 = numTotalVerts - i - 2;
                            newFace.v3 = numTotalVerts - i - 1;

                            faces.add(new Face(newFace));
                        }
                    }
                    else // numHollowVerts < numOuterVerts
                    {
                        int j = 0; // j is the index for inner vertices
                        final int maxJ = numHollowVerts - 1;
                        for (int i = 0; i < numOuterVerts; i++)
                        {
                            if (j < maxJ)
                                if (hollowAngles.angles.get(j + 1).angle - angles.angles.get(i).angle < angles.angles.get(i).angle - hollowAngles.angles.get(j).angle + 0.000001f)
                                {
                                    newFace.v1 = i;
                                    newFace.v2 = numTotalVerts - j - 2;
                                    newFace.v3 = numTotalVerts - j - 1;

                                    faces.add(new Face(newFace));
                                    j += 1;
                                }

                            newFace.v1 = numTotalVerts - j - 1;
                            newFace.v2 = i;
                            newFace.v3 = i + 1;

                            faces.add(new Face(newFace));
                        }
                    }
                }
            }

            if (calcVertexNormals)
            {
                for (final Vector3 hc : hollowCoords)
                {
                    coords.add(hc);
                    this.hollowCoordIndices.add(coords.size() - 1);
                }
            }
            else
                coords.addAll(hollowCoords);

            if (this.calcVertexNormals)
            {
                vertexNormals.addAll(hollowNormals);
                us.addAll(hollowUs);

            }
        }

        if (simpleFace && createFaces)
        {
            if (3 == sides)
                faces.add(new Face(0, 1, 2));
            else if (4 == sides)
            {
                faces.add(new Face(0, 1, 2));
                faces.add(new Face(0, 2, 3));
            }
        }

        if (calcVertexNormals && hasProfileCut)
        {
            final int lastOuterVertIndex = numOuterVerts - 1;

            if (hasHollow)
            {
                cut1CoordIndices.add(0);
                cut1CoordIndices.add(coords.size() - 1);

                cut2CoordIndices.add(lastOuterVertIndex + 1);
                cut2CoordIndices.add(lastOuterVertIndex);

                cutNormal1.X = coords.get(0).Y - coords.get(coords.size() - 1).Y;
                cutNormal1.Y = -(coords.get(0).X - coords.get(coords.size() - 1).X);

                cutNormal2.X = coords.get(lastOuterVertIndex + 1).Y - coords.get(lastOuterVertIndex).Y;
                cutNormal2.Y = -(coords.get(lastOuterVertIndex + 1).X - coords.get(lastOuterVertIndex).X);
            }

            else
            {
                cut1CoordIndices.add(0);
                cut1CoordIndices.add(1);

                cut2CoordIndices.add(lastOuterVertIndex);
                cut2CoordIndices.add(0);

                cutNormal1.X = vertexNormals.get(1).Y;
                cutNormal1.Y = -vertexNormals.get(1).X;

                cutNormal2.X = -vertexNormals.get(vertexNormals.size() - 2).Y;
                cutNormal2.Y = vertexNormals.get(vertexNormals.size() - 2).X;

            }
            cutNormal1.normalize();
            cutNormal2.normalize();
        }

        this.makeFaceUVs();

        hollowCoords = null;
        hollowNormals = null;
        hollowUs = null;

        if (calcVertexNormals)
        { // calculate prim face numbers

            // face number order is top, outer, hollow, bottom, start cut, end cut
            // I know it's ugly but so is the whole concept of prim face numbers

            int faceNum = 1; // start with outer faces
            outerFaceNumber = faceNum;

            final int startVert = hasProfileCut && !hasHollow ? 1 : 0;
            if (0 < startVert)
                faceNumbers.add(-1);
            for (int i = 0; i < numOuterVerts - 1; i++)
                faceNumbers.add(5 > sides && i <= sides ? faceNum++ : faceNum);

            faceNumbers.add(hasProfileCut ? -1 : faceNum++);

            if (4 < sides && (hasHollow || hasProfileCut))
                faceNum++;

            if (5 > sides && (hasHollow || hasProfileCut) && numOuterVerts < sides)
                faceNum++;

            if (hasHollow)
            {
                for (int i = 0; i < numHollowVerts; i++)
                    faceNumbers.add(faceNum);

                hollowFaceNumber = faceNum;
                faceNum++;
            }

            bottomFaceNumber = faceNum;
            faceNum++;

            if (hasHollow && hasProfileCut) {
                this.faceNumbers.add(faceNum);
                faceNum++;
            }

            for (int i = 0; i < faceNumbers.size(); i++)
                if (-1 == this.faceNumbers.get(i)) {
                    this.faceNumbers.set(i, faceNum);
                    faceNum++;
                }

            numPrimFaces = faceNum;
        }

    }

    public void makeFaceUVs()
    {
        faceUVs = new ArrayList<Vector2>();
        for (final Vector3 c : coords)
            faceUVs.add(new Vector2(1.0f - (0.5f + c.X), 1.0f - (0.5f - c.Y)));
    }

    public Profile copy()
    {
        return copy(true);
    }

    public Profile copy(final boolean needFaces)
    {
        final Profile copy = new Profile();

        copy.coords.addAll(coords);
        copy.faceUVs.addAll(faceUVs);

        if (needFaces)
            copy.faces.addAll(faces);
        if ((copy.calcVertexNormals = calcVertexNormals))
        {
            copy.vertexNormals.addAll(vertexNormals);
            copy.faceNormal = faceNormal;
            copy.cutNormal1 = cutNormal1;
            copy.cutNormal2 = cutNormal2;
            copy.us.addAll(us);
            copy.faceNumbers.addAll(faceNumbers);

            copy.cut1CoordIndices = new ArrayList<Integer>(cut1CoordIndices);
            copy.cut2CoordIndices = new ArrayList<Integer>(cut2CoordIndices);
            copy.hollowCoordIndices = new ArrayList<Integer>(hollowCoordIndices);
            copy.outerCoordIndices = new ArrayList<Integer>(outerCoordIndices);
        }
        copy.numOuterVerts = numOuterVerts;
        copy.numHollowVerts = numHollowVerts;

        return copy;
    }

    public void addPos(final Vector3 v)
    {
        addPos(v.X, v.Y, v.Z);
    }

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
    }

    public void addRot(final Quaternion rot)
    {
        int i;
        final int numVerts = coords.size();

        for (i = 0; i < numVerts; i++)
            coords.set(i, Vector3.multiply(coords.get(i), rot));

        if (calcVertexNormals)
        {
            final int numNormals = vertexNormals.size();
            for (i = 0; i < numNormals; i++)
                vertexNormals.set(i, Vector3.multiply(vertexNormals.get(i) ,rot));

            faceNormal = Vector3.multiply(faceNormal ,rot);
            cutNormal1 = Vector3.multiply(cutNormal1 ,rot);
            cutNormal2 = Vector3.multiply(cutNormal2 ,rot);
        }
    }

    public void Scale(final float x, final float y)
    {
        int i;
        final int numVerts = coords.size();
        Vector3 vert;

        for (i = 0; i < numVerts; i++)
        {
            vert = coords.get(i);
            vert.X *= x;
            vert.Y *= y;
            coords.set(i, vert);
        }
    }

    /// <summary>
    /// Changes order of the vertex indices and negates the center vertex normal. Does not alter vertex normals of radial vertices
    /// </summary>
    public void flipNormals()
    {
        int i;
        final int numFaces = faces.size();
        Face tmpFace;
        int tmp;

        for (i = 0; i < numFaces; i++)
        {
            tmpFace = faces.get(i);
            tmp = tmpFace.v3;
            tmpFace.v3 = tmpFace.v1;
            tmpFace.v1 = tmp;
            faces.set(i, tmpFace);
        }

        if (calcVertexNormals)
        {
            final int normalCount = vertexNormals.size();
            if (0 < normalCount)
            {
                final Vector3 n = vertexNormals.get(normalCount - 1);
                n.Z = -n.Z;
                vertexNormals.set(normalCount - 1, n);
            }
        }

        faceNormal.X = -faceNormal.X;
        faceNormal.Y = -faceNormal.Y;
        faceNormal.Z = -faceNormal.Z;

        final int numfaceUVs = faceUVs.size();
        for (i = 0; i < numfaceUVs; i++)
        {
            final Vector2 uv = faceUVs.get(i);
            uv.Y = 1.0f - uv.Y;
            faceUVs.set(i, uv);
        }
    }

    public void addValue2FaceVertexIndices(final int num)
    {
        final int numFaces = faces.size();
        Face tmpFace;
        for (int i = 0; i < numFaces; i++)
        {
            tmpFace = faces.get(i);
            tmpFace.v1 += num;
            tmpFace.v2 += num;
            tmpFace.v3 += num;

            faces.set(i, tmpFace);
        }
    }

    public void addValue2FaceNormalIndices(final int num)
    {
        if (calcVertexNormals)
        {
            final int numFaces = faces.size();
            Face tmpFace;
            for (int i = 0; i < numFaces; i++)
            {
                tmpFace = faces.get(i);
                tmpFace.n1 += num;
                tmpFace.n2 += num;
                tmpFace.n3 += num;

                faces.set(i, tmpFace);
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
