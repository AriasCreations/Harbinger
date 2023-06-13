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

package dev.zontreck.harbinger.thirdparty.libomv.character;

public class Channels
{
	private boolean Xrotation, Yrotation, Zrotation, Xposition, Yposition, Zposition;

 	public static final int XPOSITION = 0;
	public static final int YPOSITION = 1;
	public static final int ZPOSITION = 2;
	public static final int XROTATION = 3;
	public static final int YROTATION = 4;
	public static final int ZROTATION = 5;

	private int rotOffset;
	
	public Channels(final int rotOffset)
	{
		this.rotOffset = -rotOffset;
	}
	
	public int getRotOffset()
	{
		return this.rotOffset;
	}
	
	private String order = "";
	private String text = "";

	public String getOrder()
	{
		return this.order;
	}

	public void addOrder(final String ch)
	{
		this.order += ch;
	}

	public boolean isXrotation()
	{
		return this.Xrotation;
	}

	public void setXrotation(final boolean xrotation)
	{
		if (1 > rotOffset)
			this.rotOffset = -this.rotOffset;
		this.Xrotation = xrotation;
		this.text += "Xrotation ";
	}

	public boolean isYrotation()
	{
		return this.Yrotation;
	}

	public void setYrotation(final boolean yrotation)
	{
		if (1 > rotOffset)
			this.rotOffset = -this.rotOffset;
		this.Yrotation = yrotation;
		this.text += "Yrotation ";
	}

	public boolean isZrotation()
	{
		return this.Zrotation;
	}

	public void setZrotation(final boolean zrotation)
	{
		if (1 > rotOffset)
			this.rotOffset = -this.rotOffset;
		this.Zrotation = zrotation;
		this.text += "Zrotation ";
	}

	public boolean isXposition()
	{
		return this.Xposition;
	}

	public void setXposition(final boolean xposition)
	{
		if (1 > rotOffset)
			this.rotOffset--;
		this.Xposition = xposition;
		this.text += "Xposition ";
	}

	public boolean isYposition()
	{
		return this.Yposition;
	}

	public void setYposition(final boolean yposition)
	{
		if (1 > rotOffset)
			this.rotOffset--;
		this.Yposition = yposition;
		this.text += "Yposition ";
	}

	public boolean isZposition()
	{
		return this.Zposition;
	}

	public void setZposition(final boolean zposition)
	{
		if (1 > rotOffset)
			this.rotOffset--;
		this.Zposition = zposition;
		this.text += "Zposition ";
	}
	
	public int getNumChannels()
	{
		int size = 0;
		if (this.Xposition)
		{
			size++;
		}
		if (this.Yposition)
		{
			size++;
		}
		if (this.Zposition)
		{
			size++;
		}
		if (this.Xrotation)
		{
			size++;
		}
		if (this.Yrotation)
		{
			size++;
		}
		if (this.Zrotation)
		{
			size++;
		}
		return size;
	}
	
	@Override
	public String toString()
	{
		if (this.text.isEmpty())
		{
			return "CHANNELS 0";
		}
		return "CHANNELS " + this.getNumChannels() + " " + this.text.substring(0, this.text.length() - 1);
	}
}