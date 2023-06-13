/**
 * Copyright (c) 2011 aki@akjava.com
 * Copyright (c) 2011-2017, Frederick Martian
 * All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libomv.character;

import libomv.types.Matrix4;
import libomv.types.Quaternion;
import libomv.types.Vector3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BVHReader extends KeyFrameMotion {
	private final float INCHES_TO_METERS = 0.02540005f;

	private final float POSITION_KEYFRAME_THRESHOLD = 0.03f;
	private final float ROTATION_KEYFRAME_THRESHOLD = 0.01f;

	private final float POSITION_MOTION_THRESHOLD = 0.001f;
	private final float ROTATION_MOTION_THRESHOLD = 0.001f;

	private final int HIERARCHY = 0;
	private final int ROOT = 1;
	private final int JOINT_OPEN = 2;
	private final int JOINT_CLOSE = 3;
	private final int JOINT_INSIDE = 4;
	private final int ENDSITES_OPEN = 5;
	private final int ENDSITES_INSIDE = 6;
	private final int ENDSITES_CLOSE = 7;

	private final int MOTION = 8;
	private final int MOTION_FRAMES = 9;
	private final int MOTION_FRAME_TIME = 10;
	private final int MOTION_DATA = 11;

	private int mode;
	private BVH bvh;
	private final List<BVHNode> nodes = new ArrayList<BVHNode>();
	private final List<Constraint> constraints = new ArrayList<Constraint>();

	// Translation values
	HashMap<String, BVHTranslation> translations = new HashMap<String, BVHTranslation>();

	public BVHReader(final BufferedReader reader) throws InvalidLineException, IOException {
		this.mode = this.HIERARCHY;
		this.loadTranslationTable("anim.ini");
		this.loadBVHFile(reader);
		this.optimize();
		this.translate();
		this.constraints.clear();
	}

	private void loadTranslationTable(final String filename) throws InvalidLineException, IOException {
		this.translations.clear();
		this.constraints.clear();
		final InputStream st = this.getClass().getResourceAsStream("/res/" + filename);
		final BufferedReader br = new BufferedReader(new InputStreamReader(st, StandardCharsets.UTF_8));
		try {
			int i = 1;
			String line = br.readLine();

			if (!line.startsWith("Translations 1.0")) {
				throw new InvalidLineException(i, line, "Invalid Translation file header");
			}
			boolean loadingGlobals = false;
			BVHTranslation trans = null;
			while (null != (line = br.readLine())) {
				line = line.trim();
				i++;

				/* if the line is empty or a comment ignore it */
				if (line.isEmpty() || line.startsWith("#"))
					continue;

				if (line.startsWith("[")) {
					if (line.startsWith("[GLOBALS]")) {
						loadingGlobals = true;
					} else {
						loadingGlobals = false;
						trans = new BVHTranslation();
						this.translations.put(line.substring(1, line.indexOf(']') - 1), trans);
					}
					continue;
				}

				final int offset = line.indexOf('=');
				final String[] vals;
				final String token = line.substring(0, offset - 1).trim();
				line = line.substring(offset + 1).trim();

				if (loadingGlobals) {
					if (0 == token.compareToIgnoreCase("emote")) {
						this.ExpressionName = line;
					} else if (0 == token.compareToIgnoreCase("priority")) {
						this.Priority = Integer.parseInt(line);
					} else if (0 == token.compareToIgnoreCase("loop")) {
						vals = line.split(" ");
						float loop_in = 0.0f;
						float loop_out = 1.0f;
						if (2 <= vals.length) {
							this.Loop = true;
							loop_in = Float.parseFloat(vals[0]);
							loop_out = Float.parseFloat(vals[1]);
						} else if (1 == vals.length) {
							this.Loop = 0 == vals[0].compareToIgnoreCase("true");
						}
						this.InPoint = loop_in * this.Length;
						this.OutPoint = loop_out * this.Length;
					} else if (0 == token.compareToIgnoreCase("easein")) {
						this.EaseInTime = Float.parseFloat(line);
					} else if (0 == token.compareToIgnoreCase("easeout")) {
						this.EaseOutTime = Float.parseFloat(line);
					} else if (0 == token.compareToIgnoreCase("hand")) {
						this.HandPose = Integer.parseInt(line);
					} else if (0 == token.compareToIgnoreCase("constraint") || 0 == token.compareToIgnoreCase("planar_constraint")) {
						final Constraint constraint = new Constraint();
						vals = line.split(" ");
						if (13 <= vals.length) {
							constraint.ChainLength = Integer.parseInt(vals[0]);
							constraint.EaseInStart = Float.parseFloat(vals[1]);
							constraint.EaseInStop = Float.parseFloat(vals[2]);
							constraint.EaseOutStart = Float.parseFloat(vals[3]);
							constraint.EaseOutStop = Float.parseFloat(vals[4]);
							constraint.SourceJointName = vals[5];
							constraint.SourceOffset = new Vector3(Float.parseFloat(vals[6]), Float.parseFloat(vals[7]), Float.parseFloat(vals[8]));
							constraint.TargetJointName = vals[9];
							constraint.TargetOffset = new Vector3(Float.parseFloat(vals[10]), Float.parseFloat(vals[11]), Float.parseFloat(vals[12]));
							if (16 <= vals.length) {
								constraint.TargetDir = new Vector3(Float.parseFloat(vals[13]), Float.parseFloat(vals[14]), Float.parseFloat(vals[15])).normalize();
							}
						} else {
							throw new InvalidLineException(i, line, "Invalid constraint entry");
						}
						if (0 == token.compareToIgnoreCase("constraint"))
							constraint.ConstraintType = EConstraintType.CONSTRAINT_TYPE_POINT;
						else
							constraint.ConstraintType = EConstraintType.CONSTRAINT_TYPE_PLANE;
						this.constraints.add(constraint);
					}
				} else if (null == trans) {
					throw new InvalidLineException(i, line, "Invalid Translation file format");
				} else if (0 == token.compareToIgnoreCase("ignore")) {
					trans.mIgnore = 0 == line.compareToIgnoreCase("true");
				} else if (0 == token.compareToIgnoreCase("relativepos")) {
					vals = line.split(" ");
					if (3 <= vals.length) {
						trans.mRelativePosition = new Vector3(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]), Float.parseFloat(vals[2]));
					} else if (1 <= vals.length && 0 == vals[0].compareToIgnoreCase("firstkey")) {
						trans.mRelativePositionKey = true;
					} else {
						throw new InvalidLineException(i, line, "No relative key");
					}
				} else if (0 == token.compareToIgnoreCase("relativerot")) {
					if (0 == line.compareToIgnoreCase("firstkey")) {
						trans.mRelativePositionKey = true;
					} else {
						throw new InvalidLineException(i, line, "No relative key");
					}
				} else if (0 == token.compareToIgnoreCase("outname")) {
					if (!line.isEmpty()) {
						trans.mOutName = line;
					} else {
						throw new InvalidLineException(i, line, "No valid outname");
					}
				} else if (0 == token.compareToIgnoreCase("frame")) {
					vals = line.split("[, ]+");
					if (9 <= vals.length) {
						trans.mFrameMatrix = new Matrix4(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]), Float.parseFloat(vals[2]), 0, Float.parseFloat(vals[3]), Float.parseFloat(vals[4]), Float.parseFloat(vals[5]), 0, Float.parseFloat(vals[6]), Float.parseFloat(vals[7]), Float.parseFloat(vals[8]), 0, 0, 0, 0, 0);
					} else {
						throw new InvalidLineException(i, line, "No valid matrix");
					}
				} else if (0 == token.compareToIgnoreCase("offset")) {
					vals = line.split("[, ]+");
					if (9 <= vals.length) {
						trans.mOffsetMatrix = new Matrix4(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]), Float.parseFloat(vals[2]), 0, Float.parseFloat(vals[3]), Float.parseFloat(vals[4]), Float.parseFloat(vals[5]), 0, Float.parseFloat(vals[6]), Float.parseFloat(vals[7]), Float.parseFloat(vals[8]), 0, 0, 0, 0, 0);
					} else {
						throw new InvalidLineException(i, line, "No valid matrix");
					}
				} else if (0 == token.compareToIgnoreCase("mergeparent")) {
					if (!line.isEmpty()) {
						trans.mMergeParentName = line;
					} else {
						throw new InvalidLineException(i, line, "No valid merge parent");
					}
				} else if (0 == token.compareToIgnoreCase("mergechild")) {
					if (!line.isEmpty()) {
						trans.mMergeChildName = line;
					} else {
						throw new InvalidLineException(i, line, "No valid merge parent");
					}
				} else if (0 == token.compareToIgnoreCase("priority")) {
					if (!line.isEmpty()) {
						trans.mPriorityModifier = Integer.parseInt(line);
					} else {
						throw new InvalidLineException(i, line, "No valid priority");
					}
				}
			}
		} finally {
			br.close();
			st.close();
		}
	}

	private void loadBVHFile(final BufferedReader reader) throws InvalidLineException, IOException {
		int i = 0, rotOffset = 0;
		String line;
		String[] values;
		while (null != (line = reader.readLine())) {
			line = line.trim();
			i++;
			if (line.isEmpty()) {
				continue; // skip any empty line
			}
			switch (mode) {
				case HIERARCHY:
					if (!"HIERARCHY".equals(line)) {
						throw new InvalidLineException(i, line, "Expected HIERARCHY");
					}
					this.mode = this.ROOT;
					break;
				case ROOT:
					if (line.startsWith("ROOT")) {
						final BVHNode node = new BVHNode();
						final String name = line.substring("ROOT".length()).trim();
						node.setName(name);
						node.setTranslation(this.translations.get(name));
						this.bvh.setHiearchy(node);
						this.nodes.add(node);

						this.mode = this.JOINT_OPEN;
					} else {
						throw new InvalidLineException(i, line, "Expected ROOT");
					}
					break;
				case JOINT_OPEN:
					if ("{".equals(line)) {
						this.mode = this.JOINT_INSIDE;
					} else {
						throw new InvalidLineException(i, line, "Expected {");
					}
					break;
				case JOINT_INSIDE:
					values = line.split(" ");
					if ("OFFSET".equals(values[0])) {
						if (4 != values.length) {
							throw new InvalidLineException(i, line, "OFFSET value need 3 points");
						}
						final float x = this.toFloat(i, line, values[1]);
						final float y = this.toFloat(i, line, values[2]);
						final float z = this.toFloat(i, line, values[3]);
						this.getLast().setOffset(new Vector3(x, y, z));
					} else if ("CHANNELS".equals(values[0])) {
						if (2 > values.length) {
							throw new InvalidLineException(i, line, "CHANNEL must have 3 values");
						}
						final int channelSize = this.toInt(i, line, values[1]);

						if (1 > channelSize) {
							throw new InvalidLineException(i, line, "CHANNEL size must be larger than 0");
						}

						if (channelSize != values.length - 2) {
							throw new InvalidLineException(i, line, " Invalid CHANNEL size:" + channelSize);
						}
						final Channels channel = new Channels(rotOffset);
						for (int j = 2; j < values.length; j++) {
							if ("Xposition".equals(values[j])) {
								channel.setXposition(true);
								this.bvh.add(new NameAndChannel(this.getLast().getName(), Channels.XPOSITION, channel));
							} else if ("Yposition".equals(values[j])) {
								channel.setYposition(true);
								this.bvh.add(new NameAndChannel(this.getLast().getName(), Channels.YPOSITION, channel));
							} else if ("Zposition".equals(values[j])) {
								channel.setZposition(true);
								this.bvh.add(new NameAndChannel(this.getLast().getName(), Channels.ZPOSITION, channel));
							} else if ("Xrotation".equals(values[j])) {
								channel.setXrotation(true);
								channel.addOrder("X");
								this.bvh.add(new NameAndChannel(this.getLast().getName(), Channels.XROTATION, channel));
							} else if ("Yrotation".equals(values[j])) {
								channel.setYrotation(true);
								channel.addOrder("Y");
								this.bvh.add(new NameAndChannel(this.getLast().getName(), Channels.YROTATION, channel));
							} else if ("Zrotation".equals(values[j])) {
								channel.setZrotation(true);
								channel.addOrder("Z");
								this.bvh.add(new NameAndChannel(this.getLast().getName(), Channels.ZROTATION, channel));
							} else {
								throw new InvalidLineException(i, line, " Invalid CHANNEL value:" + values[j]);
							}
						}
						rotOffset += values.length - 2;
						this.getLast().setChannels(channel);
					} else if ("End Site".equals(line)) {
						this.mode = this.ENDSITES_OPEN;
					} else if ("}".equals(line)) {
						this.mode = this.JOINT_INSIDE;
						this.nodes.remove(this.getLast()); //pop up

						if (0 == nodes.size()) {
							this.mode = this.MOTION;
						}
					} else if ("JOINT".equals(values[0])) {
						if (2 != values.length) {
							throw new InvalidLineException(i, line, " Invalid Joint name:" + line);
						}
						final String name = values[1];
						final BVHNode node = new BVHNode();
						node.setName(name);
						node.setTranslation(this.translations.get(name));
						this.getLast().add(node);
						this.nodes.add(node);
						this.mode = this.JOINT_OPEN;
					} else {
						throw new InvalidLineException(i, line, " Invalid Joint inside:" + values[0]);
					}
					break;
				case ENDSITES_OPEN:
					if ("{".equals(line)) {
						this.mode = this.ENDSITES_INSIDE;
					} else {
						throw new InvalidLineException(i, line, "Expected {");
					}
					break;
				case ENDSITES_INSIDE:
					values = line.split(" ");
					if ("OFFSET".equals(values[0])) {
						if (4 != values.length) {
							throw new InvalidLineException(i, line, "OFFSET value need 3 points");
						}
						final float x = this.toFloat(i, line, values[1]);
						final float y = this.toFloat(i, line, values[2]);
						final float z = this.toFloat(i, line, values[3]);
						this.getLast().addEndSite(new Vector3(x, y, z));
						this.mode = this.ENDSITES_CLOSE;
					} else {
						throw new InvalidLineException(i, line, "Endsite only support offset");
					}
					break;
				case ENDSITES_CLOSE:
					if ("}".equals(line)) {
						this.mode = this.JOINT_CLOSE;
					} else {
						throw new InvalidLineException(i, line, "Expected {");
					}
					break;
				case JOINT_CLOSE:
					if ("}".equals(line)) {
						this.mode = this.JOINT_INSIDE;//maybe joint again or close
						this.nodes.remove(this.getLast());//pop up

						if (0 == nodes.size()) {
							this.mode = this.MOTION;
						}
					} else if ("End Site".equals(line)) {
						this.mode = this.ENDSITES_OPEN;
					} else {
						throw new InvalidLineException(i, line, "Expected {");
					}
					break;
				case MOTION:
					if ("MOTION".equals(line)) {
						final BVHMotion motion = new BVHMotion();
						this.bvh.setMotion(motion);
						this.mode = this.MOTION_FRAMES;
					} else {
						throw new InvalidLineException(i, line, "Expected MOTION");
					}
					break;
				case MOTION_FRAMES:
					values = line.split(" ");
					if ("Frames:".equals(values[0]) && 2 == values.length) {
						final int frames = this.toInt(i, line, values[1]);
						this.bvh.getMotion().setFrames(frames);
						this.mode = this.MOTION_FRAME_TIME;
					} else {
						throw new InvalidLineException(i, line, "Expected Frames:");
					}
					break;
				case MOTION_FRAME_TIME:
					values = line.split(":"); //not space
					if ("Frame Time".equals(values[0]) && 2 == values.length) {
						final float frameTime = this.toFloat(i, line, values[1].trim());
						this.bvh.getMotion().setFrameTime(frameTime);
						this.mode = this.MOTION_DATA;
					} else {
						throw new InvalidLineException(i, line, "Expected Frame Time");
					}
					break;
				case MOTION_DATA:
					final float[] vs = this.toFloat(i, line);
					this.bvh.getMotion().getMotions().add(vs);
					break;
				default:
					break;
			}
		}
	}

	private void optimize() throws IOException {
		if (!this.Loop && this.EaseInTime + this.EaseOutTime > this.Length && 0.0f != Length) {
			final float factor = this.Length / (this.EaseInTime + this.EaseOutTime);
			this.EaseInTime *= factor;
			this.EaseOutTime *= factor;
		}

		if (0 == bvh.getMotion().size()) {
			throw new IOException("No motion frames");
		}

		final float[] first_frame = this.bvh.getMotion().getFrameAt(0);
		final Vector3 first_frame_pos = new Vector3(first_frame);

		for (final BVHNode node : this.bvh.getNodeList()) {
			boolean pos_changed = false;
			boolean rot_changed = false;

			if (!(node.getTranslation().mIgnore)) {
				int ki = 0;
				int size = bvh.getMotion().size();
				final int rotOffset = node.getChannels().getRotOffset();

				// We need to reverse the channel order, so use the ..Rev function
				final Quaternion.Order order = Quaternion.StringToOrderRev(node.getChannels().getOrder());
				final Quaternion first_frame_rot = Quaternion.mayaQ(first_frame, rotOffset, order);

				node.mIgnorePos = new boolean[this.bvh.getMotion().size()];
				node.mIgnoreRot = new boolean[this.bvh.getMotion().size()];

				if (1 == size) {
					// FIXME: use single frame to move pelvis
					// if we have only one keyframe force output for this joint
					rot_changed = true;
				} else {
					// if more than one keyframe, use first frame as reference and skip to second keyframe
					ki++;
				}

				int ki_prev = ki, ki_last_good_pos = ki, ki_last_good_rot = ki;
				int numPosFramesConsidered = 2;
				int numRotFramesConsidered = 2;

				double diff_max = 0;
				final float rot_threshold = this.ROTATION_KEYFRAME_THRESHOLD / Math.max(node.getJoints().size() * 0.33f, 1.0f);

				for (; ki < size; ki++) {
					if (ki_prev == ki_last_good_pos) {
						node.mNumPosKeys++;
						if (POSITION_MOTION_THRESHOLD < Vector3.distance(new Vector3(bvh.getMotion().getFrameAt(ki_prev), rotOffset), first_frame_pos)) {
							pos_changed = true;
						}
					} else {
						//check position for noticeable effect
						final Vector3 test_pos = new Vector3(this.bvh.getMotion().getFrameAt(ki_prev), rotOffset);
						final Vector3 last_good_pos = new Vector3(this.bvh.getMotion().getFrameAt(ki_last_good_pos), rotOffset);
						final Vector3 current_pos = new Vector3(this.bvh.getMotion().getFrameAt(ki), rotOffset);
						final Vector3 interp_pos = Vector3.lerp(current_pos, last_good_pos, 1.0f / numPosFramesConsidered);

						if (POSITION_MOTION_THRESHOLD < Vector3.distance(current_pos, first_frame_pos)) {
							pos_changed = true;
						}

						if (POSITION_KEYFRAME_THRESHOLD > Vector3.distance(interp_pos, test_pos)) {
							node.mIgnorePos[ki] = true;
							numPosFramesConsidered++;
						} else {
							numPosFramesConsidered = 2;
							ki_last_good_pos = ki_prev;
							node.mNumPosKeys++;
						}
					}

					final Quaternion test_rot = Quaternion.mayaQ(this.bvh.getMotion().getFrameAt(ki_prev), rotOffset, order);
					float x_delta = Vector3.distance(first_frame_rot.multiply(Vector3.UnitX), test_rot.multiply(Vector3.UnitX));
					float y_delta = Vector3.distance(first_frame_rot.multiply(Vector3.UnitY), test_rot.multiply(Vector3.UnitY));
					float rot_test = x_delta + y_delta;

					if (ki_prev == ki_last_good_rot) {
						node.mNumRotKeys++;

						if (ROTATION_MOTION_THRESHOLD < rot_test) {
							rot_changed = true;
						}
					} else {
						//check rotation for noticeable effect
						final Quaternion last_good_rot = Quaternion.mayaQ(this.bvh.getMotion().getFrameAt(ki_last_good_rot), rotOffset, order);
						final Quaternion current_rot = Quaternion.mayaQ(this.bvh.getMotion().getFrameAt(ki), rotOffset, order);
						final Quaternion interp_rot = Quaternion.lerp(current_rot, last_good_rot, 1.0f / numRotFramesConsidered);

						// Test if the rotation has changed significantly since the very first frame. If false
						// for all frames, then we'll just throw out this joint's rotation entirely.
						if (ROTATION_MOTION_THRESHOLD < rot_test) {
							rot_changed = true;
						}
						x_delta = Vector3.distance(interp_rot.multiply(Vector3.UnitX), test_rot.multiply(Vector3.UnitX));
						y_delta = Vector3.distance(interp_rot.multiply(Vector3.UnitY), test_rot.multiply(Vector3.UnitY));
						rot_test = x_delta + y_delta;

						// Draw a line between the last good keyframe and current. Test the distance between the last 
						// frame (current - 1, i.e. ki_prev) and the line. If it's greater than some threshold, then it
						// represents a significant frame and we want to include it.
						if (rot_test >= rot_threshold || (ki + 1 == size && 2 < numRotFramesConsidered)) {
							// Add the current test keyframe (which is technically the previous key, i.e. ki_prev).
							numRotFramesConsidered = 2;
							ki_last_good_rot = ki_prev;
							node.mNumRotKeys++;

							// Add another keyframe between the last good keyframe and current, at whatever point was
							// the most "significant" (i.e. had the largest deviation from the earlier tests). 
							// Note that a more robust approach would be test all intermediate keyframes against the
							// line between the last good keyframe and current, but we're settling for this other method
							// because it's significantly faster.
							if (0 < diff_max) {
								if (node.mIgnoreRot[ki]) {
									node.mIgnoreRot[ki] = false;
									node.mNumRotKeys++;
								}
								diff_max = 0;
							}
						} else {
							// This keyframe isn't significant enough, throw it away.
							node.mIgnoreRot[ki] = true;
							numRotFramesConsidered++;
							// Store away the keyframe that has the largest deviation from the interpolated line, for insertion later.
							if (rot_test > diff_max) {
								diff_max = rot_test;
							}
						}
					}
					ki_prev = ki;
				}
			}

			// don't output joints with no motion
			if (!(pos_changed || rot_changed)) {
				node.getTranslation().mIgnore = true;
			}
		}
	}

	// converts BVH contents to our KeyFrameMotion format
	protected void translate() {
		// count number of non-ignored joints
		int j = 0, numJoints = 0;
		for (final BVHNode node : this.bvh.getNodeList()) {
			if (!node.getTranslation().mIgnore) numJoints++;
		}

		// fill in header
		this.Joints = new Joint[numJoints];

		Quaternion first_frame_rot = new Quaternion();

		for (final BVHNode node : this.bvh.getNodeList()) {
			if (node.getTranslation().mIgnore) continue;

			final Joint joint = this.Joints[j] = new Joint();

			joint.Name = node.getTranslation().mOutName;
			joint.Priority = node.getTranslation().mPriorityModifier;

			// compute coordinate frame rotation
			final Quaternion frameRot = new Quaternion(node.getTranslation().mFrameMatrix);
			final Quaternion frameRotInv = Quaternion.inverse(frameRot);

			final Quaternion offsetRot = new Quaternion(node.getTranslation().mOffsetMatrix);

			// find mergechild and mergeparent nodes, if specified
			Quaternion mergeParentRot, mergeChildRot;
			BVHNode mergeParent = null, mergeChild = null;

			for (final BVHNode mnode : this.bvh.getNodeList()) {
				String name = mnode.getTranslation().mMergeParentName;
				if (!name.isEmpty() && (name.equals(mnode.getName()))) {
					mergeParent = mnode;
				}
				name = mnode.getTranslation().mMergeChildName;
				if (!name.isEmpty() && (name.equals(mnode.getName()))) {
					mergeChild = mnode;
				}
			}

			joint.rotationkeys = new KeyFrameMotion.JointKey[node.mNumRotKeys];

			final Quaternion.Order order = Quaternion.StringToOrderRev(node.getChannels().getOrder());
			int frame = 0;
			final int rotOffset = node.getChannels().getRotOffset();
			for (final float[] keyFrame : this.bvh.getMotion().getMotions()) {
				if (0 == frame && node.getTranslation().mRelativeRotationKey) {
					first_frame_rot = Quaternion.mayaQ(keyFrame, rotOffset, order);
				}

				if (node.mIgnoreRot[frame]) {
					frame++;
					continue;
				}

				if (null != mergeParent) {
					mergeParentRot = Quaternion.mayaQ(keyFrame, mergeParent.getChannels().getRotOffset(), Quaternion.StringToOrderRev(mergeParent.getChannels().getOrder()));
					final Quaternion parentFrameRot = new Quaternion(mergeParent.getTranslation().mFrameMatrix);
					final Quaternion parentOffsetRot = new Quaternion(mergeParent.getTranslation().mOffsetMatrix);
					mergeParentRot = parentFrameRot.inverse().multiply(mergeParentRot).multiply(parentFrameRot).multiply(parentOffsetRot);
				} else {
					mergeParentRot = Quaternion.Identity;
				}

				if (null != mergeChild) {
					mergeChildRot = Quaternion.mayaQ(keyFrame, mergeChild.getChannels().getRotOffset(), Quaternion.StringToOrderRev(mergeChild.getChannels().getOrder()));
					final Quaternion childFrameRot = new Quaternion(mergeChild.getTranslation().mFrameMatrix);
					final Quaternion childOffsetRot = new Quaternion(mergeChild.getTranslation().mOffsetMatrix);
					mergeChildRot = childFrameRot.inverse().multiply(mergeChildRot).multiply(childFrameRot).multiply(childOffsetRot);
				} else {
					mergeChildRot = Quaternion.Identity;
				}

				final Quaternion inRot = Quaternion.mayaQ(keyFrame, rotOffset, order);
				final Quaternion outRot = frameRotInv.multiply(mergeChildRot).multiply(inRot).multiply(mergeParentRot).multiply(first_frame_rot.inverse()).multiply(frameRot).multiply(offsetRot);

				joint.rotationkeys[frame] = new JointKey();
				joint.rotationkeys[frame].time = (frame + 1) * this.bvh.getMotion().getFrameTime();
				joint.rotationkeys[frame].keyElement = outRot.toVector3();

				frame++;
			}

			// output position keys (only for 1st joint)
			if (0 == j && !node.getTranslation().mIgnorePositions) {
				joint.positionkeys = new KeyFrameMotion.JointKey[node.mNumPosKeys];

				final Vector3 relPos = node.getTranslation().mRelativePosition;
				Vector3 relKey = Vector3.Zero;

				frame = 0;
				for (final float[] keyFrame : this.bvh.getMotion().getMotions()) {
					if ((0 == frame) && node.getTranslation().mRelativePositionKey) {
						relKey = new Vector3(keyFrame, 0);
					}

					if (node.mIgnorePos[frame]) {
						frame++;
						continue;
					}

					final Vector3 inPos = new Vector3(keyFrame, 0).subtract(relKey).multiply(first_frame_rot.inverse());
					Vector3 outPos = inPos.multiply(frameRot).multiply(offsetRot);

					outPos = outPos.multiply(this.INCHES_TO_METERS).subtract(relPos);

					joint.positionkeys[frame] = new JointKey();
					joint.positionkeys[frame].time = (frame + 1) * this.bvh.getMotion().getFrameTime();
					joint.positionkeys[frame].keyElement = outPos.clamp(-KeyFrameMotion.MAX_PELVIS_OFFSET, KeyFrameMotion.MAX_PELVIS_OFFSET);
					frame++;
				}
				j++;
			} else {
				joint.positionkeys = new KeyFrameMotion.JointKey[0];
			}
		}
		this.Constraints = this.constraints.toArray(this.Constraints);
	}

	public boolean validate() {
		return this.bvh.getMotion().getFrames() == this.bvh.getMotion().getMotions().size();
	}

	protected BVHNode getLast() {
		return this.nodes.get(this.nodes.size() - 1);
	}

	protected float[] toFloat(final int index, final String line) throws InvalidLineException {
		final String[] values = line.split(" ");
		final float[] vs = new float[values.length];
		try {
			for (int i = 0; i < values.length; i++) {
				vs[i] = Float.parseFloat(values[i]);
			}
		} catch (final Exception e) {
			throw new InvalidLineException(index, line, "has invalid double");
		}
		return vs;
	}


	protected int toInt(final int index, final String line, final String v) throws InvalidLineException {
		int d = 0;
		try {
			d = Integer.parseInt(v);
		} catch (final Exception e) {
			throw new InvalidLineException(index, line, "invalid integer value:" + v);
		}
		return d;
	}

	protected float toFloat(final int index, final String line, final String v) throws InvalidLineException {
		float d = 0;
		try {
			d = Float.parseFloat(v);
		} catch (final Exception e) {
			throw new InvalidLineException(index, line, "invalid double value:" + v);
		}
		return d;
	}

	public class InvalidLineException extends Exception {
		private static final long serialVersionUID = 1L;

		public InvalidLineException(final int index, final String line, final String message) {
			super("line " + index + ":" + line + ":message:" + message);
		}
	}
}
