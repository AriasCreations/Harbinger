/**
 * Copyright (c) 2011 aki@akjava.com
 * Copyright (c) 2012-2017, Frederick Martian
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libomv.character;

import java.util.ArrayList;
import java.util.List;

public class BVHMotion
{
	private int frames;
	private float frameTime;
	
	public int getFrames()
	{
		return this.frames;
	}
	
	public float[] getFrameAt(final int index)
	{
		return this.motions.get(index);
	}
	
	public void setFrames(final int frames)
	{
		this.frames = frames;
	}
	
	public void syncFrames()
	{
		frames = this.motions.size();
	}

	public float getFrameTime()
	{
		return this.frameTime;
	}
	
	public void setFrameTime(final float frameTime)
	{
		this.frameTime = frameTime;
	}
	
	public List<float[]> getMotions()
	{
		return this.motions;
	}
	
	private final List<float[]> motions = new ArrayList<float[]>();
	
	public void add(final float[] motion)
	{
		this.motions.add(motion);
	}
	
	public int size()
	{
		return this.motions.size();
	}
	
	public float getDuration()
	{
		//TODO support ignore first
		return this.frameTime * this.motions.size();
	}
}