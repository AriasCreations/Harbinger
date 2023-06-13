/**
 * Copyright (c) 2011 aki@akjava.com
 * Copyright (c) 2012-2017, Frederick Martian
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
package dev.zontreck.harbinger.thirdparty.libomv.character;

import java.util.ArrayList;
import java.util.List;

public class BVH {
	public static final double FPS_30 = 0.033;
	public List<NameAndChannel> nameAndChannels = new ArrayList<NameAndChannel> ( );
	private BVHNode hiearchy;
	private BVHMotion motion;
	private int skips = 1;

	public int getSkips ( ) {
		return this.skips;
	}

	public void setSkips ( final int skips ) {
		this.skips = skips;
	}

	public BVHMotion getMotion ( ) {
		return this.motion;
	}

	public void setMotion ( final BVHMotion motion ) {
		this.motion = motion;
	}

	public BVHNode getHiearchy ( ) {
		return this.hiearchy;
	}

	public void setHiearchy ( final BVHNode hiearchy ) {
		this.hiearchy = hiearchy;
	}

	public List<NameAndChannel> getNameAndChannels ( ) {
		return this.nameAndChannels;
	}

	public void add ( final NameAndChannel nc ) {
		this.nameAndChannels.add ( nc );
	}

	public int getFrames ( ) {
		final int f = this.motion.getFrames ( );
		if ( 0 == f ) {
			return 0;
		}
		if ( 0 == skips ) {
			return f;
		}
		final int fs = ( f - 1 ) / this.skips;
		return fs + 1; // +first frame
	}

	public double getFrameTime ( ) {
		final double f = this.motion.getFrameTime ( );
		if ( 0 == f ) {
			return 0;
		}
		if ( 0 == skips ) {
			return f;
		}
		return f * this.skips;
	}

	public float[] getFrameAt ( final int index ) {
		if ( 0 == skips ) {
			return this.motion.getMotions ( ).get ( index );
		}
		return this.motion.getMotions ( ).get ( index * this.skips );
	}

	public List<BVHNode> getNodeList ( ) {
		final List<BVHNode> nodes = new ArrayList<BVHNode> ( );
		this.addNode ( nodes , hiearchy );
		return nodes;
	}

	private void addNode ( final List<BVHNode> nodes , final BVHNode node ) {
		nodes.add ( node );
		for ( final BVHNode child : node.getJoints ( ) ) {
			child.setParentName ( node.getName ( ) );
			this.addNode ( nodes , child );
		}
	}
}