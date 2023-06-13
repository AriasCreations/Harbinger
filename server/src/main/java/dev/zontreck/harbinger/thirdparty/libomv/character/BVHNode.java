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

import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3;

import java.util.ArrayList;
import java.util.List;

public class BVHNode {
	private final List<Vector3> endSites = new ArrayList<Vector3> ( );
	private final List<BVHNode> joints = new ArrayList<BVHNode> ( );
	public boolean[] mIgnorePos;
	public boolean[] mIgnoreRot;
	public int mNumPosKeys;
	public int mNumRotKeys;
	private Vector3 offset;
	private String name;
	private Channels channels;
	//usually null for special purpose
	private String parentName;
	private BVHTranslation translation;

	public Vector3 getOffset ( ) {
		return this.offset;
	}

	public void setOffset ( final Vector3 offset ) {
		this.offset = offset;
	}

	public Vector3 getEndSite ( ) {
		if ( 0 < endSites.size ( ) ) {
			return this.endSites.get ( 0 );
		}
		return null;
	}

	public List<Vector3> getEndSites ( ) {
		return this.endSites;
	}

	public void addEndSite ( final Vector3 endSite ) {
		endSites.add ( endSite );
	}

	public String getName ( ) {
		return this.name;
	}

	public void setName ( final String name ) {
		this.name = name;
	}

	public Channels getChannels ( ) {
		return this.channels;
	}

	public void setChannels ( final Channels channels ) {
		this.channels = channels;
	}

	public List<BVHNode> getJoints ( ) {
		return this.joints;
	}

	public void add ( final BVHNode joint ) {
		this.joints.add ( joint );
	}

	public String getParentName ( ) {
		return this.parentName;
	}

	public void setParentName ( final String parentName ) {
		this.parentName = parentName;
	}

	public BVHTranslation getTranslation ( ) {
		return this.translation;
	}

	public void setTranslation ( final BVHTranslation translation ) {
		this.translation = translation;
	}
}