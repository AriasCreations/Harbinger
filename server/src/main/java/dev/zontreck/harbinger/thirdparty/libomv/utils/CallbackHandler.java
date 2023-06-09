/**
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
package dev.zontreck.harbinger.thirdparty.libomv.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class CallbackHandler <T> {
	/* This hash list needs to be of a type that guarantees the same iteration
	 * order than in which the items were inserted. Otherwise we get race situations
	 * with user callbacks being added after the library callbacks but depending on
	 * information in the library that has been inserted by the library callback. */
	private LinkedHashMap<Callback<T>, Boolean> callbackHandlers;

	public synchronized int count ( ) {
		if ( null != callbackHandlers ) {
			return this.callbackHandlers.size ( );
		}
		return 0;
	}

	public boolean add ( final Callback<T> handler ) {
		return this.add ( handler , false );
	}

	/**
	 * Add a callback handler to the list of handlers
	 *
	 * @param handler    The callback handler to add to the list
	 * @param autoremove When true the callback handler is automatically removed when
	 *                   invoked
	 * @return True when the callback handler replaced an earlier instance of
	 * itself, false otherwise
	 */
	public synchronized boolean add ( final Callback<T> handler , final boolean autoremove ) {
		if ( null != handler ) {
			if ( null == callbackHandlers )
				this.callbackHandlers = new LinkedHashMap<Callback<T>, Boolean> ( );

			return ( null != callbackHandlers.put ( handler , autoremove ) );
		}
		return false;
	}

	/**
	 * Remove a callback handler from the list of handlers
	 *
	 * @param handler The callback handler to add to the list
	 * @return True when the callback handler was removed, false when it didn't
	 * exist
	 */
	public synchronized boolean remove ( final Callback<T> handler ) {
		if ( null != handler && null != callbackHandlers ) {
			return ( null != callbackHandlers.remove ( handler ) );
		}
		return false;
	}

	/**
	 * Dispatches a callback to all registered handlers
	 *
	 * @param args The argument class to pass to the callback handlers
	 * @return The number of callback handlers that got invoked
	 */
	public synchronized int dispatch ( final T args ) {
		int count = 0;

		if ( null != callbackHandlers ) {
			Iterator<Map.Entry<Callback<T>, Boolean>> iter = callbackHandlers.entrySet ( ).iterator ( );
			while ( iter.hasNext ( ) ) {
				final Map.Entry<Callback<T>, Boolean> entry = iter.next ( );
				final Callback<T> handler = entry.getKey ( );
				boolean remove = false;

				synchronized ( handler ) {
					remove = handler.callback ( args );
					handler.notifyAll ( );
				}
				if ( remove || entry.getValue ( ) )
					iter.remove ( );
				count++;
			}
		}
		return count;
	}
}
