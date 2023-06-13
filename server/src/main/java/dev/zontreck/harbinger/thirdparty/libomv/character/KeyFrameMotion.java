/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
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
package dev.zontreck.harbinger.thirdparty.libomv.character;

import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3;
import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class KeyFrameMotion {
	public static int KEYFRAME_MOTION_VERSION = 1;
	public static int KEYFRAME_MOTION_SUBVERSION;

	public static float MAX_PELVIS_OFFSET = 5.0f;
	public int version; // Always 1
	public int sub_version; // Always 0
	// Animation Priority
	public int Priority;
	// The animation length in seconds.
	public float Length;
	// Expression set in the client.  Null if [None] is selected
	public String ExpressionName; // "" (null)
	// The time in seconds to start the animation
	public float InPoint;
	// The time in seconds to end the animation
	public float OutPoint;
	// Loop the animation
	public boolean Loop;
	// Meta data. Ease in Seconds.
	public float EaseInTime;
	// Meta data. Ease out seconds.
	public float EaseOutTime;
	// Meta Data for the Hand Pose
	public int HandPose;
	// Contains an array of joints
	public Joint[] Joints;
	//    public int ConstraintCount;
	public Constraint[] Constraints;
	// Custom application data that can be attached to a joint
	public Object Tag;

	public KeyFrameMotion ( ) {
		this.version = KeyFrameMotion.KEYFRAME_MOTION_VERSION;
		this.sub_version = KeyFrameMotion.KEYFRAME_MOTION_SUBVERSION;
	}

	/**
	 * Serialize an animation asset binary data into it's joints/keyframes/meta data
	 *
	 * @param animationdata The asset binary data containing the animation
	 */
	public KeyFrameMotion ( final byte[] animationdata ) {
		int i = 0;
		int jointCount;
		final int constraintCount;

		this.version = Helpers.BytesToUInt16L ( animationdata , i );
		i += 2; // Always 1
		this.sub_version = Helpers.BytesToUInt16L ( animationdata , i );
		i += 2; // Always 0
		this.Priority = Helpers.BytesToInt32L ( animationdata , i );
		i += 4;
		this.Length = Helpers.BytesToFloatL ( animationdata , i );
		i += 4;

		this.ExpressionName = this.ReadBytesUntilNull ( animationdata , i , - 1 );
		i += this.ExpressionName.length ( ) + 1;

		this.InPoint = Helpers.BytesToFloatL ( animationdata , i );
		i += 4;
		this.OutPoint = Helpers.BytesToFloatL ( animationdata , i );
		i += 4;
		this.Loop = ( 0 != Helpers.BytesToInt32L ( animationdata , i ) );
		i += 4;
		this.EaseInTime = Helpers.BytesToFloatL ( animationdata , i );
		i += 4;
		this.EaseOutTime = Helpers.BytesToFloatL ( animationdata , i );
		i += 4;
		this.HandPose = ( int ) Helpers.BytesToUInt32L ( animationdata , i );
		i += 4; // Handpose

		jointCount = ( int ) Helpers.BytesToUInt32L ( animationdata , i );
		i += 4; // Get Joint count
		this.Joints = new Joint[ jointCount ];

		// deserialize the number of joints in the animation.
		// Joints are variable length blocks of binary data consisting of joint data and keyframes
		for ( int j = 0 ; j < jointCount ; j++ ) {
			final Joint joint = new Joint ( );
			i = this.readJoint ( animationdata , i , joint );
			this.Joints[ j ] = joint;
		}

		// Read possible constraint records if available
		if ( i < animationdata.length + 4 ) {
			constraintCount = ( int ) Helpers.BytesToUInt32L ( animationdata , i );
			i += 4;
			this.Constraints = new Constraint[ constraintCount ];
			for ( final int j = 0 ; j < constraintCount ; i++ ) {
				final Constraint constraint = new Constraint ( );
				i = this.readConstraint ( animationdata , i , constraint );
				this.Constraints[ j ] = constraint;
			}
		}
	}

	/**
	 * Variable length strings seem to be null terminated in the animation asset..
	 * use with caution, home grown.
	 *
	 * @param data The animation asset byte array
	 * @param i    The offset to start reading
	 * @returns a string
	 */
	public String ReadBytesUntilNull ( final byte[] data , int i , int max ) {
		final int startpos = i;

		if ( max < i || max > data.length )
			max = data.length;

		// Find the null character
		for ( ; i < max ; i++ ) {
			if ( 0 == data[ i ] ) {
				break;
			}
		}

		// We found the end of the string
		// convert the bytes from the beginning of the string to the end of the string
		try {
			return Helpers.BytesToString ( data , startpos , i - startpos );
		} catch ( final UnsupportedEncodingException e ) {
			return Helpers.EmptyString;
		}
	}

	// Number of joints defined in the animation
//    public int JointCount;

	/**
	 * Read in a Joint from an animation asset byte array
	 *
	 * @param data   animation asset byte array
	 * @param i      Byte Offset of the start of the joint
	 * @param pJoint The Joint structure to serialized the data into
	 * @return Byte Offset after the end of the joint
	 */
	public int readJoint ( final byte[] data , int i , final Joint pJoint ) {
		// Joint name
		pJoint.Name = this.ReadBytesUntilNull ( data , i , - 1 );
		i += pJoint.Name.length ( ) + 1;

		// Priority Revisited
		pJoint.Priority = Helpers.BytesToInt32L ( data , i );
		i += 4; // Joint Priority override?

		// Read in rotation keyframes
		pJoint.rotationkeys = this.readKeys ( data , i , - 1.0f , 1.0f );
		i += 4 + ( null != pJoint.rotationkeys ? pJoint.rotationkeys.length * 8 : 0 );

		// Read in position keyframes
		pJoint.positionkeys = this.readKeys ( data , i , - 0.5f , 1.5f );
		i += 4 + ( null != pJoint.rotationkeys ? pJoint.positionkeys.length * 8 : 0 );

		return i;
	}

	/**
	 * Read Keyframes of a certain type
	 *
	 * @param data Animation Byte array
	 * @param i    Offset in the Byte Array
	 * @param min  Scaling Min to pass to the Uint16ToFloat method
	 * @param max  Scaling Max to pass to the Uint16ToFloat method
	 * @return an array of JointKey records
	 */
	public JointKey[] readKeys ( final byte[] data , int i , final float min , final float max ) {
		float x, y, z;

		// int32: number of key frames
		final int keycount = Helpers.BytesToInt32L ( data , i );
		i += 4; // How many rotation keyframes

		// Sanity check how many position keys there are
		if ( 0 < keycount && keycount * 8 <= data.length - i ) {
			// ... n Keyframe data blocks
			// Read in keyframes
			final JointKey[] m_keys = new JointKey[ keycount ];
			for ( int j = 0 ; j < keycount ; j++ ) {
				final JointKey pJKey = new JointKey ( );

				pJKey.time = Helpers.UInt16ToFloatL ( data , i , this.InPoint , this.OutPoint );
				i += 2;
				x = Helpers.UInt16ToFloatL ( data , i , min , max );
				i += 2;
				y = Helpers.UInt16ToFloatL ( data , i , min , max );
				i += 2;
				z = Helpers.UInt16ToFloatL ( data , i , min , max );
				i += 2;
				pJKey.keyElement = new Vector3 ( x , y , z );
				m_keys[ j ] = pJKey;
			}
			return m_keys;
		}
		return null;
	}

	public int readConstraint ( final byte[] data , int i , final Constraint constraint ) {
		constraint.ChainLength = data[ i ];
		i++;
		constraint.ConstraintType = EConstraintType.values ( )[ data[ i ] ];
		i++;
		constraint.SourceJointName = this.ReadBytesUntilNull ( data , i , i += 16 );
		constraint.SourceOffset = new Vector3 ( data , i );
		i += 12;
		constraint.TargetJointName = this.ReadBytesUntilNull ( data , i , i += 16 );
		constraint.TargetOffset = new Vector3 ( data , i );
		i += 12;
		constraint.EaseInStart = Helpers.BytesToFloatL ( data , i );
		i += 4;
		constraint.EaseInStop = Helpers.BytesToFloatL ( data , i );
		i += 4;
		constraint.EaseOutStart = Helpers.BytesToFloatL ( data , i );
		i += 4;
		constraint.EaseOutStop = Helpers.BytesToFloatL ( data , i );
		i += 4;
		return i;
	}

	@Override
	public boolean equals ( final Object obj ) {
		return null != obj && obj instanceof KeyFrameMotion && this.equals ( ( KeyFrameMotion ) obj );
	}

	public boolean equals ( final KeyFrameMotion other ) {
		return null != other && this.version == other.version && this.sub_version == other.sub_version && this.Loop == other.Loop &&
				this.InPoint == other.InPoint && this.OutPoint == other.OutPoint && this.Length == other.Length && this.HandPose == other.HandPose &&
				this.EaseInTime == other.EaseInTime && this.EaseOutTime == other.EaseOutTime && this.Priority == other.Priority &&
				Arrays.equals ( this.Joints , other.Joints ) && Arrays.equals ( this.Constraints , other.Constraints );
	}

	@Override
	public int hashCode ( ) {
		return this.version ^ this.sub_version ^ ( this.Loop ? 1 : 0 ) ^ ( ( Float ) this.InPoint ).hashCode ( ) ^ ( ( Float ) this.OutPoint ).hashCode ( ) ^
				( ( Float ) this.EaseInTime ).hashCode ( ) ^ ( ( Float ) this.EaseOutTime ).hashCode ( ) ^ ( ( Float ) this.Length ).hashCode ( ) ^
				this.HandPose ^ this.Priority ^ Arrays.hashCode ( this.Joints ) ^ Arrays.hashCode ( this.Constraints );
	}

	// Poses set in the animation metadata for the hands
	public enum HandPose {
		Spread,
		Relaxed,
		Point_Both,
		Fist,
		Relaxed_Left,
		Point_Left,
		Fist_Left,
		Relaxed_Right,
		Point_Right,
		Fist_Right,
		Salute_Right,
		Typing,
		Peace_Right
	}

	enum EConstraintType {
		CONSTRAINT_TYPE_POINT,
		CONSTRAINT_TYPE_PLANE,
		NUM_CONSTRAINT_TYPES
	}

	enum EConstraintTargetType {
		CONSTRAINT_TARGET_TYPE_BODY,
		CONSTRAINT_TARGET_TYPE_GROUND,
		NUM_CONSTRAINT_TARGET_TYPES
	}

	// A Joint and it's associated meta data and keyframes
	public class Joint {
		// Name of the Joint. Matches the avatar_skeleton.xml in client distros
		public String Name;

		// Joint Animation Override?   Was the same as the Priority in testing..
		public int Priority;

		// Array of Rotation Keyframes in order from earliest to latest
		public JointKey[] rotationkeys;

		// Array of Position Keyframes in order from earliest to latest
		// This seems to only be for the Pelvis?
		public JointKey[] positionkeys;

		// Custom application data that can be attached to a joint
		public Object Tag;

		@Override
		public boolean equals ( final Object obj ) {
			return null != obj && obj instanceof Joint && this.equals ( ( Joint ) obj );
		}

		public boolean equals ( final Joint other ) {
			if ( null != other ) {
				return null == Name ? this.Name == other.Name : this.Name.equals ( other.Name ) && this.Priority == other.Priority &&
						null == Tag ? this.Tag == other.Tag : this.Tag.equals ( other.Tag ) &&
						Arrays.equals ( this.rotationkeys , other.rotationkeys ) && Arrays.equals ( this.positionkeys , other.positionkeys );
			}
			return false;
		}

		@Override
		public int hashCode ( ) {
			return ( null == Name ? 0 : this.Name.hashCode ( ) ) ^ this.Priority ^ ( null == Tag ? 0 : this.Tag.hashCode ( ) ) ^
					Arrays.hashCode ( this.rotationkeys ) ^ Arrays.hashCode ( this.positionkeys );
		}
	}

	// A Joint Keyframe.  This is either a position or a rotation.
	public class JointKey {
		// Time in seconds for this keyframe.
		public float time;

		// Either a Vector3 position or a Vector3 Euler rotation
		public Vector3 keyElement;

		@Override
		public boolean equals ( final Object obj ) {
			return null != obj && obj instanceof JointKey && this.equals ( ( JointKey ) obj );
		}

		public boolean equals ( final JointKey other ) {
			if ( null != other ) {
				return this.time == other.time && null == keyElement ? this.keyElement == other.keyElement : this.keyElement.equals ( other.keyElement );
			}
			return false;
		}

		@Override
		public int hashCode ( ) {
			return ( ( Float ) this.time ).hashCode ( ) ^ ( null == keyElement ? 0 : this.keyElement.hashCode ( ) );
		}
	}

	public class Constraint {
		String SourceJointName;
		String TargetJointName;
		int ChainLength;
		Vector3 SourceOffset;
		Vector3 TargetOffset;
		Vector3 TargetDir;
		float EaseInStart;
		float EaseInStop;
		float EaseOutStart;
		float EaseOutStop;
		EConstraintType ConstraintType;

		@Override
		public boolean equals ( final Object obj ) {
			return null != obj && this.equals ( ( Constraint ) obj );
		}

		public boolean equals ( final Constraint other ) {
			if ( null != other ) {
				return this.ChainLength == other.ChainLength && this.EaseInStart == other.EaseInStart && this.EaseInStop == other.EaseInStop &&
						this.EaseOutStart == other.EaseOutStart && this.EaseOutStop == other.EaseOutStop &&
						null == SourceJointName ? this.SourceJointName == other.SourceJointName : this.SourceJointName.equals ( other.SourceJointName ) &&
						null == TargetJointName ? this.TargetJointName == other.TargetJointName : this.TargetJointName.equals ( other.TargetJointName ) &&
						null == SourceOffset ? this.SourceOffset == other.SourceOffset : this.SourceOffset.equals ( other.SourceOffset ) &&
						null == TargetOffset ? this.TargetOffset == other.TargetOffset : this.TargetOffset.equals ( other.TargetOffset ) &&
						null == TargetDir ? this.TargetDir == other.TargetDir : this.TargetDir.equals ( other.TargetDir );
			}
			return false;
		}

		@Override
		public int hashCode ( ) {
			return this.ChainLength ^ ( ( Float ) this.EaseInStart ).hashCode ( ) ^ ( ( Float ) this.EaseInStop ).hashCode ( ) ^
					( ( Float ) this.EaseOutStart ).hashCode ( ) ^ ( ( Float ) this.EaseOutStop ).hashCode ( ) ^
					( null == SourceJointName ? 0 : this.SourceJointName.hashCode ( ) ) ^
					( null == TargetJointName ? 0 : this.TargetJointName.hashCode ( ) ) ^
					( null == TargetDir ? 0 : this.TargetDir.hashCode ( ) ) ^
					( null == SourceOffset ? 0 : this.SourceOffset.hashCode ( ) ) ^
					( null != TargetOffset ? 0 : this.TargetOffset.hashCode ( ) );
		}
	}
}
