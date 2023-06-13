/**
 * Copyright (c) 2009-2017, Frederick Martian
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
 * - Neither the name of the dev.zontreck.harbinger.thirdparty.libomv-java project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
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
package dev.zontreck.harbinger.thirdparty.libomv.utils;

import java.util.ArrayList;

public class TimeoutEventQueue<T>
{
	private final ArrayList<TimeoutEvent<T>> list = new ArrayList<TimeoutEvent<T>>();

	/**
	 * Create a new timeout event and add it to the internal list of events
	 * 
	 * @return a timeout event object that is in a reset state
	 */
	public TimeoutEvent<T> create()
	{
		final TimeoutEvent<T> event = new TimeoutEvent<T>();
		synchronized (this.list)
		{
			this.list.add(event);
		}
		return event;
	}

	/**
	 * Signals every event in the list and then cleans out the list
	 */
	public void set(final T object)
	{
		synchronized (this.list)
		{
			for (final TimeoutEvent<T> e : this.list)
			{
				e.set(object);
			}
		}
	}

	/**
	 * Resets every event in the list and then cleans out the list
	 * 
	 * @returns the number of events that have been reset
	 */
	public int cancel()
	{
		synchronized (this.list)
		{
			final int count = this.list.size();
			for (final TimeoutEvent<T> e : this.list)
			{
				e.reset();
			}
			this.list.clear();
			return count;
		}
	}

	/**
	 * Signals the specified event in the list and then cleans it out from the
	 * list
	 * 
	 * @param event
	 *            the timeout event to look for in the list
	 * @return a boolean indicating if the event was found in the list
	 */
	public boolean cancel(final TimeoutEvent<T> event)
	{
		final boolean success = false;
		synchronized (this.list)
		{
			final int idx = this.list.indexOf(event);
			if (0 <= idx)
			{
				this.list.remove(idx);
			}
		}
		event.reset();
		return success;
	}

	/**
	 * Returns the number of events that are currently in the list
	 * 
	 * @returns the number of events that are currently in the list
	 */
	public int size()
	{
		synchronized (this.list)
		{
			return this.list.size();
		}
	}
}
