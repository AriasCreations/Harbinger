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

import dev.zontreck.harbinger.thirdparty.libomv.primMesher.types.Face;
import dev.zontreck.harbinger.thirdparty.libomv.primMesher.types.Profile;
import dev.zontreck.harbinger.thirdparty.libomv.primMesher.types.ViewerFace;
import dev.zontreck.harbinger.thirdparty.libomv.types.Quaternion;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3;
import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PrimMesh {
	public class PathNode {
		public Vector3 position;
		public Quaternion rotation;
		public float xScale;
		public float yScale;
		public float percentOfPath;
	}

	public enum PathType {Linear, Circular, Flexible}

	public class Path {
		public ArrayList<PathNode> pathNodes = new ArrayList<PathNode>();

		public float twistBegin;
		public float twistEnd;
		public float topShearX;
		public float topShearY;
		public float pathCutBegin;
		public float pathCutEnd = 1.0f;
		public float dimpleBegin;
		public float dimpleEnd = 1.0f;
		public float skew;
		public float holeSizeX = 1.0f; // called pathScaleX in pbs
		public float holeSizeY = 0.25f;
		public float taperX;
		public float taperY;
		public float radius;
		public float revolutions = 1.0f;
		public int stepsPerRevolution = 24;

		public void create(final PathType pathType, int steps) {
			if (0.999f < this.taperX) taperX = 0.999f;
			if (-0.999f > this.taperX) taperX = -0.999f;
			if (0.999f < this.taperY) taperY = 0.999f;
			if (-0.999f > this.taperY) taperY = -0.999f;

			if (PathType.Linear == pathType || PathType.Flexible == pathType) {
				int step = 0;

				final float length = pathCutEnd - pathCutBegin;
				final float twistTotal = this.twistEnd - this.twistBegin;
				final float twistTotalAbs = Math.abs(twistTotal);
				if (0.01f < twistTotalAbs)
					steps += (int) (twistTotalAbs * 3.66); //  dahlia's magic number

				final float start = -0.5f;
				final float stepSize = length / steps;
				final float percentOfPathMultiplier = stepSize * 0.999999f;
				float xOffset = topShearX * pathCutBegin;
				float yOffset = topShearY * pathCutBegin;
				float zOffset = start;
				final float xOffsetStepIncrement = topShearX * length / steps;
				final float yOffsetStepIncrement = topShearY * length / steps;

				float percentOfPath = pathCutBegin;
				zOffset += percentOfPath;

				// sanity checks

				boolean done = false;

				while (!done) {
					final PathNode newNode = new PathNode();

					newNode.xScale = 1.0f;
					if (0.0f == this.taperX)
						newNode.xScale = 1.0f;
					else if (0.0f < this.taperX)
						newNode.xScale = 1.0f - percentOfPath * taperX;
					else
						newNode.xScale = 1.0f + (1.0f - percentOfPath) * taperX;

					newNode.yScale = 1.0f;
					if (0.0f == this.taperY)
						newNode.yScale = 1.0f;
					else if (0.0f < this.taperY)
						newNode.yScale = 1.0f - percentOfPath * taperY;
					else
						newNode.yScale = 1.0f + (1.0f - percentOfPath) * taperY;

					final float twist = this.twistBegin + twistTotal * percentOfPath;

					newNode.rotation = new Quaternion(Vector3.UnitZ, twist);
					newNode.position = new Vector3(xOffset, yOffset, zOffset);
					newNode.percentOfPath = percentOfPath;

					this.pathNodes.add(newNode);

					if (step < steps) {
						step += 1;
						percentOfPath += percentOfPathMultiplier;
						xOffset += xOffsetStepIncrement;
						yOffset += yOffsetStepIncrement;
						zOffset += stepSize;
						if (percentOfPath > pathCutEnd)
							done = true;
					} else done = true;
				}
			} // end of linear path code

			else // pathType == Circular
			{
				final float twistTotal = this.twistEnd - this.twistBegin;

				// if the profile has a lot of twist, add more layers otherwise the layers may overlap
				// and the resulting mesh may be quite inaccurate. This method is arbitrary and doesn't
				// accurately match the viewer
				final float twistTotalAbs = Math.abs(twistTotal);
				if (0.01f < twistTotalAbs) {
					if (Math.PI * 1.5f < twistTotalAbs)
						steps *= 2;
					if (Math.PI * 3.0f < twistTotalAbs)
						steps *= 2;
				}

				final float yPathScale = holeSizeY * 0.5f;
				final float pathLength = pathCutEnd - pathCutBegin;
				final float totalSkew = skew * 2.0f * pathLength;
				final float skewStart = pathCutBegin * 2.0f * skew - skew;
				final float xOffsetTopShearXFactor = topShearX * (0.25f + 0.5f * (0.5f - holeSizeY));
				final float yShearCompensation = 1.0f + Math.abs(topShearY) * 0.25f;

				// It's not quite clear what pushY (Y top shear) does, but subtracting it from the start and end
				// angles appears to approximate it's effects on path cut. Likewise, adding it to the angle used
				// to calculate the sine for generating the path radius appears to approximate it's effects there
				// too, but there are some subtle differences in the radius which are noticeable as the prim size
				// increases and it may affect megaprims quite a bit. The effect of the Y top shear parameter on
				// the meshes generated with this technique appear nearly identical in shape to the same prims when
				// displayed by the viewer.

				final float startAngle = (Helpers.TWO_PI * pathCutBegin * revolutions) - topShearY * 0.9f;
				final float endAngle = (Helpers.TWO_PI * pathCutEnd * revolutions) - topShearY * 0.9f;
				final float stepSize = Helpers.TWO_PI / stepsPerRevolution;

				int step = (int) (startAngle / stepSize);
				float angle = startAngle;

				boolean done = false;
				while (!done) // loop through the length of the path and add the layers
				{
					final PathNode newNode = new PathNode();

					float xProfileScale = (1.0f - Math.abs(skew)) * holeSizeX;
					float yProfileScale = holeSizeY;

					final float percentOfPath = angle / (Helpers.TWO_PI * revolutions);
					final float percentOfAngles = (angle - startAngle) / (endAngle - startAngle);

					if (0.01f < this.taperX)
						xProfileScale *= 1.0f - percentOfPath * taperX;
					else if (-0.01f > this.taperX)
						xProfileScale *= 1.0f + (1.0f - percentOfPath) * taperX;

					if (0.01f < this.taperY)
						yProfileScale *= 1.0f - percentOfPath * taperY;
					else if (-0.01f > this.taperY)
						yProfileScale *= 1.0f + (1.0f - percentOfPath) * taperY;

					newNode.xScale = xProfileScale;
					newNode.yScale = yProfileScale;

					float radiusScale = 1.0f;
					if (0.001f < this.radius)
						radiusScale = 1.0f - radius * percentOfPath;
					else if (0.001f > this.radius)
						radiusScale = 1.0f + radius * (1.0f - percentOfPath);

					final float twist = this.twistBegin + twistTotal * percentOfPath;

					float xOffset = 0.5f * (skewStart + totalSkew * percentOfAngles);
					xOffset += (float) Math.sin(angle) * xOffsetTopShearXFactor;

					final float yOffset = yShearCompensation * (float) Math.cos(angle) * (0.5f - yPathScale) * radiusScale;

					final float zOffset = (float) Math.sin(angle + topShearY) * (0.5f - yPathScale) * radiusScale;

					newNode.position = new Vector3(xOffset, yOffset, zOffset);

					// now orient the rotation of the profile layer relative to it's position on the path
					// adding taperY to the angle used to generate the quat appears to approximate the viewer

					newNode.rotation = new Quaternion(Vector3.UnitX, angle + topShearY);

					// next apply twist rotation to the profile layer
					if (0.0f != twistTotal || 0.0f != twistBegin)
						newNode.rotation = Quaternion.multiply(newNode.rotation, new Quaternion(Vector3.UnitZ, twist));

					newNode.percentOfPath = percentOfPath;

					this.pathNodes.add(newNode);

					// calculate terms for next iteration
					// calculate the angle for the next iteration of the loop

					if (angle >= endAngle - 0.01)
						done = true;
					else {
						step += 1;
						angle = stepSize * step;
						if (angle > endAngle)
							angle = endAngle;
					}
				}
			}
		}
	}

	public String errorMessage = "";

	public ArrayList<Vector3> coords;
	public ArrayList<Vector3> normals;
	public ArrayList<Face> faces;

	public ArrayList<ViewerFace> viewerFaces;

	private int sides = 4;
	private int hollowSides = 4;
	private float profileStart;
	private float profileEnd = 1.0f;
	private float hollow;
	public int twistBegin;
	public int twistEnd;
	public float topShearX;
	public float topShearY;
	public float pathCutBegin;
	public float pathCutEnd = 1.0f;
	public float dimpleBegin;
	public float dimpleEnd = 1.0f;
	public float skew;
	public float holeSizeX = 1.0f; // called pathScaleX in pbs
	public float holeSizeY = 0.25f;
	public float taperX;
	public float taperY;
	public float radius;
	public float revolutions = 1.0f;
	public int stepsPerRevolution = 24;

	private int profileOuterFaceNumber = -1;
	private int profileHollowFaceNumber = -1;

	private boolean hasProfileCut;
	private boolean hasHollow;
	public boolean calcVertexNormals;
	private boolean normalsProcessed;
	public boolean viewerMode;
	public boolean sphereMode;

	public int numPrimFaces;

	/// <summary>
	/// Human readable string representation of the parameters used to create a mesh.
	/// </summary>
	/// <returns></returns>
	public String ParamsToDisplayString() {
		final String s = "sides..................: " + sides + "\nhollowSides..........: " + hollowSides + "\nprofileStart.........: " + profileStart + "\nprofileEnd...........: " + profileEnd + "\nhollow...............: " + hollow + "\ntwistBegin...........: " + twistBegin + "\ntwistEnd.............: " + twistEnd + "\ntopShearX............: " + topShearX + "\ntopShearY............: " + topShearY + "\npathCutBegin.........: " + pathCutBegin + "\npathCutEnd...........: " + pathCutEnd + "\ndimpleBegin..........: " + dimpleBegin + "\ndimpleEnd............: " + dimpleEnd + "\nskew.................: " + skew + "\nholeSizeX............: " + holeSizeX + "\nholeSizeY............: " + holeSizeY + "\ntaperX...............: " + taperX + "\ntaperY...............: " + taperY + "\nradius...............: " + radius + "\nrevolutions..........: " + revolutions + "\nstepsPerRevolution...: " + stepsPerRevolution + "\nsphereMode...........: " + sphereMode + "\nhasProfileCut........: " + hasProfileCut + "\nhasHollow............: " + hasHollow + "\nviewerMode...........: " + viewerMode;
		return s;
	}

	public int getProfileOuterFaceNumber() {
		return this.profileOuterFaceNumber;
	}

	public int getProfileHollowFaceNumber() {
		return this.profileHollowFaceNumber;
	}

	public boolean getHasProfileCut() {
		return this.hasProfileCut;
	}

	public boolean getHasHollow() {
		return this.hasHollow;
	}


	/**
	 * Constructs a PrimMesh object and creates the profile for extrusion.
	 *
	 * @param sides
	 * @param profileStart
	 * @param profileEnd
	 * @param hollow
	 * @param hollowSides
	 */
	public PrimMesh(final int sides, final float profileStart, final float profileEnd, final float hollow, final int hollowSides) {
		coords = new ArrayList<Vector3>();
		faces = new ArrayList<Face>();

		this.sides = sides;
		this.profileStart = profileStart;
		this.profileEnd = profileEnd;
		this.hollow = hollow;
		this.hollowSides = hollowSides;

		if (3 > sides) this.sides = 3;
		if (3 > hollowSides) this.hollowSides = 3;
		if (0.0f > profileStart) this.profileStart = 0.0f;
		if (1.0f < profileEnd) this.profileEnd = 1.0f;
		if (0.02f > profileEnd) this.profileEnd = 0.02f;
		if (profileStart >= profileEnd)
			this.profileStart = profileEnd - 0.02f;
		if (0.99f < hollow) this.hollow = 0.99f;
		if (0.0f > hollow) this.hollow = 0.0f;
	}

	/**
	 * Extrudes a profile along a path.
	 *
	 * @param pathType
	 */
	public void extrude(final PathType pathType) {
		boolean needEndFaces = false;

		coords = new ArrayList<Vector3>();
		faces = new ArrayList<Face>();

		if (viewerMode) {
			viewerFaces = new ArrayList<ViewerFace>();
			calcVertexNormals = true;
		}

		if (calcVertexNormals)
			normals = new ArrayList<Vector3>();

		int steps = 1;

		final float length = pathCutEnd - pathCutBegin;
		this.normalsProcessed = false;

		if (viewerMode && 3 == this.sides) {
			// prisms don't taper well so add some vertical resolution
			// other prims may benefit from this but just do prisms for now
			if (0.01 < Math.abs(this.taperX) || 0.01 < Math.abs(this.taperY))
				steps = (int) (steps * 4.5 * length);
		}

		if (sphereMode)
			hasProfileCut = 0.4999f > this.profileEnd - this.profileStart;
		else
			hasProfileCut = 0.9999f > this.profileEnd - this.profileStart;
		hasHollow = (0.001f < this.hollow);

		final float twistBegin = this.twistBegin / 360.0f * Helpers.TWO_PI;
		final float twistEnd = this.twistEnd / 360.0f * Helpers.TWO_PI;
		final float twistTotal = twistEnd - twistBegin;
		final float twistTotalAbs = Math.abs(twistTotal);
		if (0.01f < twistTotalAbs)
			steps += (int) (twistTotalAbs * 3.66); //  dahlia's magic number

		float hollow = this.hollow;

		if (PathType.Circular == pathType) {
			needEndFaces = false;
			if (0.0f != this.pathCutBegin || 1.0f != this.pathCutEnd)
				needEndFaces = true;
			else if (0.0f != this.taperX || 0.0f != this.taperY)
				needEndFaces = true;
			else if (0.0f != this.skew) needEndFaces = true;
			else if (0.0f != twistTotal)
				needEndFaces = true;
			else if (0.0f != this.radius)
				needEndFaces = true;
		} else needEndFaces = true;

		// sanity checks
		float initialProfileRot = 0.0f;
		if (PathType.Circular == pathType) {
			if (3 == this.sides) {
				initialProfileRot = (float) Math.PI;
				if (4 == this.hollowSides) {
					if (0.7f < hollow) hollow = 0.7f;
					hollow *= 0.707f;
				} else hollow *= 0.5f;
			} else if (4 == this.sides) {
				initialProfileRot = 0.25f * (float) Math.PI;
				if (4 != this.hollowSides) hollow *= 0.707f;
			} else if (4 < this.sides) {
				initialProfileRot = (float) Math.PI;
				if (4 == this.hollowSides) {
					if (0.7f < hollow) hollow = 0.7f;
					hollow /= 0.7f;
				}
			}
		} else {
			if (3 == this.sides) {
				if (4 == this.hollowSides) {
					if (0.7f < hollow) hollow = 0.7f;
					hollow *= 0.707f;
				} else hollow *= 0.5f;
			} else if (4 == this.sides) {
				initialProfileRot = 1.25f * (float) Math.PI;
				if (4 != this.hollowSides) hollow *= 0.707f;
			} else if (24 == this.sides && 4 == this.hollowSides)
				hollow *= 1.414f;
		}

		final Profile profile = new Profile(sides, profileStart, profileEnd, hollow, hollowSides, true, this.calcVertexNormals);
		errorMessage = profile.errorMessage;

		numPrimFaces = profile.numPrimFaces;

		int cut1FaceNumber = profile.bottomFaceNumber + 1;
		int cut2FaceNumber = cut1FaceNumber + 1;
		if (!needEndFaces) {
			cut1FaceNumber -= 2;
			cut2FaceNumber -= 2;
		}

		this.profileOuterFaceNumber = profile.outerFaceNumber;
		if (!needEndFaces) this.profileOuterFaceNumber--;

		if (this.hasHollow) {
			this.profileHollowFaceNumber = profile.hollowFaceNumber;
			if (!needEndFaces)
				this.profileHollowFaceNumber--;
		}

		int cut1Vert = -1;
		int cut2Vert = -1;
		if (this.hasProfileCut) {
			cut1Vert = this.hasHollow ? profile.coords.size() - 1 : 0;
			cut2Vert = this.hasHollow ? profile.numOuterVerts - 1 : profile.numOuterVerts;
		}

		if (0.0f != initialProfileRot) {
			profile.addRot(new Quaternion(Vector3.UnitZ, initialProfileRot));
			if (this.viewerMode) profile.makeFaceUVs();
		}

		Vector3 lastCutNormal1 = new Vector3(0.0f);
		Vector3 lastCutNormal2 = new Vector3(0.0f);
		float thisV = 0.0f;
		float lastV = 0.0f;

		final Path path = new Path();
		path.twistBegin = twistBegin;
		path.twistEnd = twistEnd;
		path.topShearX = this.topShearX;
		path.topShearY = this.topShearY;
		path.pathCutBegin = this.pathCutBegin;
		path.pathCutEnd = this.pathCutEnd;
		path.dimpleBegin = this.dimpleBegin;
		path.dimpleEnd = this.dimpleEnd;
		path.skew = this.skew;
		path.holeSizeX = this.holeSizeX;
		path.holeSizeY = this.holeSizeY;
		path.taperX = this.taperX;
		path.taperY = this.taperY;
		path.radius = this.radius;
		path.revolutions = this.revolutions;
		path.stepsPerRevolution = this.stepsPerRevolution;

		path.create(pathType, steps);

		for (int nodeIndex = 0; nodeIndex < path.pathNodes.size(); nodeIndex++) {
			final PathNode node = path.pathNodes.get(nodeIndex);
			final Profile newLayer = profile.copy();
			newLayer.Scale(node.xScale, node.yScale);

			newLayer.addRot(node.rotation);
			newLayer.addPos(node.position);

			if (needEndFaces && 0 == nodeIndex) {
				newLayer.flipNormals();

				// add the bottom faces to the viewerFaces list
				if (viewerMode) {
					final Vector3 faceNormal = newLayer.faceNormal;
					final ViewerFace newViewerFace = new ViewerFace(profile.bottomFaceNumber);
					final int numFaces = newLayer.faces.size();
					final ArrayList<Face> faces = newLayer.faces;

					for (int i = 0; i < numFaces; i++) {
						final Face face = faces.get(i);
						newViewerFace.v1 = newLayer.coords.get(face.v1);
						newViewerFace.v2 = newLayer.coords.get(face.v2);
						newViewerFace.v3 = newLayer.coords.get(face.v3);

						newViewerFace.coordIndex1 = face.v1;
						newViewerFace.coordIndex2 = face.v2;
						newViewerFace.coordIndex3 = face.v3;

						newViewerFace.n1 = faceNormal;
						newViewerFace.n2 = faceNormal;
						newViewerFace.n3 = faceNormal;

						newViewerFace.uv1 = newLayer.faceUVs.get(face.v1);
						newViewerFace.uv2 = newLayer.faceUVs.get(face.v2);
						newViewerFace.uv3 = newLayer.faceUVs.get(face.v3);

						if (PathType.Linear == pathType) {
							/* FIXME: Need to create a copy here! */
							newViewerFace.uv1.flip();
							newViewerFace.uv2.flip();
							newViewerFace.uv3.flip();
						}

						viewerFaces.add(newViewerFace);
					}
				}
			} // if (nodeIndex == 0)

			// append this layer

			final int coordsLen = coords.size();
			newLayer.addValue2FaceVertexIndices(coordsLen);

			coords.addAll(newLayer.coords);

			if (calcVertexNormals) {
				newLayer.addValue2FaceNormalIndices(normals.size());
				normals.addAll(newLayer.vertexNormals);
			}

			if (node.percentOfPath < pathCutBegin + 0.01f || node.percentOfPath > pathCutEnd - 0.01f)
				faces.addAll(newLayer.faces);

			// fill faces between layers

			final int numVerts = newLayer.coords.size();
			final Face newFace1 = new Face();
			final Face newFace2 = new Face();

			thisV = 1.0f - node.percentOfPath;

			if (0 < nodeIndex) {
				int startVert = coordsLen + 1;
				final int endVert = coords.size();

				if (5 > sides || hasProfileCut || hasHollow)
					startVert--;

				for (int i = startVert; i < endVert; i++) {
					int iNext = i + 1;
					if (i == endVert - 1) iNext = startVert;

					final int whichVert = i - startVert;

					newFace1.v1 = i;
					newFace1.v2 = i - numVerts;
					newFace1.v3 = iNext;

					newFace1.n1 = newFace1.v1;
					newFace1.n2 = newFace1.v2;
					newFace1.n3 = newFace1.v3;
					faces.add(newFace1);

					newFace2.v1 = iNext;
					newFace2.v2 = i - numVerts;
					newFace2.v3 = iNext - numVerts;

					newFace2.n1 = newFace2.v1;
					newFace2.n2 = newFace2.v2;
					newFace2.n3 = newFace2.v3;
					faces.add(newFace2);

					if (viewerMode) {
						// add the side faces to the list of viewerFaces here

						int primFaceNum = profile.faceNumbers.get(whichVert);
						if (!needEndFaces) primFaceNum -= 1;

						final ViewerFace newViewerFace1 = new ViewerFace(primFaceNum);
						final ViewerFace newViewerFace2 = new ViewerFace(primFaceNum);

						int uIndex = whichVert;
						if (!this.hasHollow && 4 < sides && uIndex < newLayer.us.size() - 1) {
							uIndex++;
						}

						float u1 = newLayer.us.get(uIndex);
						float u2 = 1.0f;
						if (uIndex < newLayer.us.size() - 1)
							u2 = newLayer.us.get(uIndex + 1);

						if (whichVert == cut1Vert || whichVert == cut2Vert) {
							u1 = 0.0f;
							u2 = 1.0f;
						} else if (5 > sides) {
							if (whichVert < profile.numOuterVerts) { // boxes and prisms have one texture face per side of the prim, so the U values have to be scaled
								// to reflect the entire texture width
								u1 *= this.sides;
								u2 *= this.sides;
								u2 -= (int) u1;
								u1 -= (int) u1;
								if (0.1f > u2) u2 = 1.0f;
							}
						}

						if (sphereMode) {
							if (whichVert != cut1Vert && whichVert != cut2Vert) {
								u1 = u1 * 2.0f - 1.0f;
								u2 = u2 * 2.0f - 1.0f;

								if (whichVert >= newLayer.numOuterVerts) {
									u1 -= hollow;
									u2 -= hollow;
								}

							}
						}

						newViewerFace1.uv1.X = u1;
						newViewerFace1.uv2.X = u1;
						newViewerFace1.uv3.X = u2;

						newViewerFace1.uv1.Y = thisV;
						newViewerFace1.uv2.Y = lastV;
						newViewerFace1.uv3.Y = thisV;

						newViewerFace2.uv1.X = u2;
						newViewerFace2.uv2.X = u1;
						newViewerFace2.uv3.X = u2;


						newViewerFace2.uv1.Y = thisV;
						newViewerFace2.uv2.Y = lastV;
						newViewerFace2.uv3.Y = lastV;

						newViewerFace1.v1 = coords.get(newFace1.v1);
						newViewerFace1.v2 = coords.get(newFace1.v2);
						newViewerFace1.v3 = coords.get(newFace1.v3);

						newViewerFace2.v1 = coords.get(newFace2.v1);
						newViewerFace2.v2 = coords.get(newFace2.v2);
						newViewerFace2.v3 = coords.get(newFace2.v3);

						newViewerFace1.coordIndex1 = newFace1.v1;
						newViewerFace1.coordIndex2 = newFace1.v2;
						newViewerFace1.coordIndex3 = newFace1.v3;

						newViewerFace2.coordIndex1 = newFace2.v1;
						newViewerFace2.coordIndex2 = newFace2.v2;
						newViewerFace2.coordIndex3 = newFace2.v3;

						// profile cut faces
						if (whichVert == cut1Vert) {
							newViewerFace1.primFaceNumber = cut1FaceNumber;
							newViewerFace2.primFaceNumber = cut1FaceNumber;
							newViewerFace1.n1 = newLayer.cutNormal1;
							newViewerFace1.n2 = newViewerFace1.n3 = lastCutNormal1;

							newViewerFace2.n1 = newViewerFace2.n3 = newLayer.cutNormal1;
							newViewerFace2.n2 = lastCutNormal1;
						} else if (whichVert == cut2Vert) {
							newViewerFace1.primFaceNumber = cut2FaceNumber;
							newViewerFace2.primFaceNumber = cut2FaceNumber;
							newViewerFace1.n1 = newLayer.cutNormal2;
							newViewerFace1.n2 = lastCutNormal2;
							newViewerFace1.n3 = lastCutNormal2;

							newViewerFace2.n1 = newLayer.cutNormal2;
							newViewerFace2.n3 = newLayer.cutNormal2;
							newViewerFace2.n2 = lastCutNormal2;
						} else // outer and hollow faces
						{
							if ((5 > sides && whichVert < newLayer.numOuterVerts) || (5 > hollowSides && whichVert >= newLayer.numOuterVerts)) { // looks terrible when path is twisted... need vertex normals here
								newViewerFace1.calcSurfaceNormal();
								newViewerFace2.calcSurfaceNormal();
							} else {
								newViewerFace1.n1 = normals.get(newFace1.n1);
								newViewerFace1.n2 = normals.get(newFace1.n2);
								newViewerFace1.n3 = normals.get(newFace1.n3);

								newViewerFace2.n1 = normals.get(newFace2.n1);
								newViewerFace2.n2 = normals.get(newFace2.n2);
								newViewerFace2.n3 = normals.get(newFace2.n3);
							}
						}

						viewerFaces.add(newViewerFace1);
						viewerFaces.add(newViewerFace2);

					}
				}
			}

			lastCutNormal1 = newLayer.cutNormal1;
			lastCutNormal2 = newLayer.cutNormal2;
			lastV = thisV;

			if (needEndFaces && nodeIndex == path.pathNodes.size() - 1 && this.viewerMode) {
				// add the top faces to the viewerFaces list here
				final Vector3 faceNormal = newLayer.faceNormal;
				final ViewerFace newViewerFace = new ViewerFace(0);
				final int numFaces = newLayer.faces.size();
				final ArrayList<Face> faces = newLayer.faces;

				for (int i = 0; i < numFaces; i++) {
					final Face face = faces.get(i);
					newViewerFace.v1 = newLayer.coords.get(face.v1 - coordsLen);
					newViewerFace.v2 = newLayer.coords.get(face.v2 - coordsLen);
					newViewerFace.v3 = newLayer.coords.get(face.v3 - coordsLen);

					newViewerFace.coordIndex1 = face.v1 - coordsLen;
					newViewerFace.coordIndex2 = face.v2 - coordsLen;
					newViewerFace.coordIndex3 = face.v3 - coordsLen;

					newViewerFace.n1 = faceNormal;
					newViewerFace.n2 = faceNormal;
					newViewerFace.n3 = faceNormal;

					newViewerFace.uv1 = newLayer.faceUVs.get(face.v1 - coordsLen);
					newViewerFace.uv2 = newLayer.faceUVs.get(face.v2 - coordsLen);
					newViewerFace.uv3 = newLayer.faceUVs.get(face.v3 - coordsLen);

					if (PathType.Linear == pathType) {
						newViewerFace.uv1.flip();
						newViewerFace.uv2.flip();
						newViewerFace.uv3.flip();
					}

					viewerFaces.add(newViewerFace);
				}
			}
		} // for (int nodeIndex = 0; nodeIndex < path.pathNodes.Count; nodeIndex++)
	}

	/**
	 * DEPRICATED - use Extrude(PathType.Linear) instead
	 * <p>
	 * Extrudes a profile along a straight line path. Used for prim types box, cylinder, and prism.
	 */
	@Deprecated
	public void extrudeLinear() {
		extrude(PathType.Linear);
	}


	/**
	 * DEPRICATED - use Extrude(PathType.Circular) instead
	 * <p>
	 * Extrude a profile into a circular path prim mesh. Used for prim types torus, tube, and ring.
	 */
	@Deprecated
	public void extrudeCircular() {
		extrude(PathType.Circular);
	}


	private Vector3 surfaceNormal(final Vector3 c1, final Vector3 c2, final Vector3 c3) {
		final Vector3 edge1 = Vector3.subtract(c1, c2);
		final Vector3 edge2 = Vector3.subtract(c3, c1);

		return edge1.cross(edge2).normalize();
	}

	private Vector3 surfaceNormal(final Face face) {
		return this.surfaceNormal(coords.get(face.v1), coords.get(face.v2), coords.get(face.v3));
	}

	/**
	 * Calculate the surface normal for a face in the list of faces
	 *
	 * @param faceIndex
	 * @throws Exception
	 * @returns
	 */
	public Vector3 surfaceNormal(final int faceIndex) throws Exception {
		final int numFaces = faces.size();
		if (0 > faceIndex || faceIndex >= numFaces)
			throw new Exception("faceIndex out of range");

		return this.surfaceNormal(faces.get(faceIndex));
	}

	/**
	 * Duplicates a PrimMesh object. All object properties are copied by value, including lists.
	 *
	 * @returns
	 */
	public PrimMesh copy() {
		final PrimMesh copy = new PrimMesh(sides, profileStart, profileEnd, hollow, hollowSides);
		copy.twistBegin = twistBegin;
		copy.twistEnd = twistEnd;
		copy.topShearX = topShearX;
		copy.topShearY = topShearY;
		copy.pathCutBegin = pathCutBegin;
		copy.pathCutEnd = pathCutEnd;
		copy.dimpleBegin = dimpleBegin;
		copy.dimpleEnd = dimpleEnd;
		copy.skew = skew;
		copy.holeSizeX = holeSizeX;
		copy.holeSizeY = holeSizeY;
		copy.taperX = taperX;
		copy.taperY = taperY;
		copy.radius = radius;
		copy.revolutions = revolutions;
		copy.stepsPerRevolution = stepsPerRevolution;
		copy.calcVertexNormals = calcVertexNormals;
		copy.normalsProcessed = normalsProcessed;
		copy.viewerMode = viewerMode;
		copy.numPrimFaces = numPrimFaces;
		copy.errorMessage = errorMessage;

		copy.coords = new ArrayList<Vector3>(coords);
		copy.faces = new ArrayList<Face>(faces);
		copy.viewerFaces = new ArrayList<ViewerFace>(viewerFaces);
		copy.normals = new ArrayList<Vector3>(normals);

		return copy;
	}

	/**
	 * Calculate surface normals for all of the faces in the list of faces in this mesh
	 *
	 * @throws Exception
	 */
	public void calcNormals() throws Exception {
		if (this.normalsProcessed) return;

		this.normalsProcessed = true;

		final int numFaces = this.faces.size();

		if (!calcVertexNormals)
			normals = new ArrayList<Vector3>();

		for (int i = 0; i < numFaces; i++) {
			final Face face = this.faces.get(i);

			normals.add(this.surfaceNormal(i));

			final int normIndex = this.normals.size() - 1;
			face.n1 = normIndex;
			face.n2 = normIndex;
			face.n3 = normIndex;

			faces.set(i, face);
		}
	}

	/**
	 * Adds a value to each XYZ vertex coordinate in the mesh
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
	public void addPos(final float x, final float y, final float z) {
		int i;
		final int numVerts = coords.size();
		Vector3 vert;

		for (i = 0; i < numVerts; i++) {
			vert = coords.get(i);
			vert.X += x;
			vert.Y += y;
			vert.Z += z;
			coords.set(i, vert);
		}

		if (null != this.viewerFaces) {
			final int numViewerFaces = viewerFaces.size();

			for (i = 0; i < numViewerFaces; i++) {
				final ViewerFace v = viewerFaces.get(i);
				v.addPos(x, y, z);
				viewerFaces.set(i, v);
			}
		}
	}

	/**
	 * Rotates the mesh
	 *
	 * @param quaternion
	 */
	public void addRot(final Quaternion quaternion) {
		int i;
		final int numVerts = coords.size();

		for (i = 0; i < numVerts; i++)
			coords.set(i, Vector3.multiply(coords.get(i), quaternion));

		if (null != this.normals) {
			final int numNormals = normals.size();
			for (i = 0; i < numNormals; i++)
				normals.set(i, Vector3.multiply(normals.get(i), quaternion));
		}

		if (null != this.viewerFaces) {
			final int numViewerFaces = viewerFaces.size();

			for (i = 0; i < numViewerFaces; i++) {
				final ViewerFace v = viewerFaces.get(i);
				v.v1 = Vector3.multiply(v.v1, quaternion);
				v.v2 = Vector3.multiply(v.v2, quaternion);
				v.v3 = Vector3.multiply(v.v3, quaternion);

				v.n1 = Vector3.multiply(v.n1, quaternion);
				v.n2 = Vector3.multiply(v.n2, quaternion);
				v.n3 = Vector3.multiply(v.n3, quaternion);
				viewerFaces.set(i, v);
			}
		}
	}

	public VertexIndexer getVertexIndexer() {
		if (viewerMode && 0 < this.viewerFaces.size())
			return new VertexIndexer(this);
		return null;
	}

	/**
	 * Scales the mesh
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
	public void scale(final float x, final float y, final float z) {
		int i;
		final int numVerts = coords.size();

		final Vector3 m = new Vector3(x, y, z);
		for (i = 0; i < numVerts; i++)
			coords.set(i, Vector3.multiply(coords.get(i), m));

		if (null != this.viewerFaces) {
			final int numViewerFaces = viewerFaces.size();
			for (i = 0; i < numViewerFaces; i++) {
				final ViewerFace v = viewerFaces.get(i);
				v.v1 = Vector3.multiply(v.v1, m);
				v.v2 = Vector3.multiply(v.v2, m);
				v.v3 = Vector3.multiply(v.v3, m);
				viewerFaces.set(i, v);
			}
		}
	}

	/**
	 * Dumps the mesh to a Blender compatible "Raw" format file
	 *
	 * @param path
	 * @param name
	 * @param title
	 * @throws IOException
	 */
	public void dumpRaw(final String path, final String name, final String title) throws IOException {
		if (null == path) return;
		final String fileName = name + "_" + title + ".raw";
		final File completePath = new File(path, fileName);
		final FileWriter sw = new FileWriter(completePath, StandardCharsets.UTF_8);

		for (int i = 0; i < faces.size(); i++) {
			sw.append(coords.get(faces.get(i).v1).toString() + " ");
			sw.append(coords.get(faces.get(i).v2).toString() + " ");
			sw.append(coords.get(faces.get(i).v3).toString() + "\n");
		}
		sw.close();
	}
}