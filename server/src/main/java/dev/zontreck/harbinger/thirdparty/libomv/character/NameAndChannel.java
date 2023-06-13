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

import java.util.HashMap;
import java.util.Map;

/**
 * each data target,usually bones x 3
 *
 * @author aki
 */
public class NameAndChannel {
	public static Map<String, String> orderMap;

	static {
		NameAndChannel.orderMap = new HashMap<String, String> ( );
		NameAndChannel.orderMap.put ( "X" , "XYZ" );
		NameAndChannel.orderMap.put ( "XY" , "XYZ" );
		NameAndChannel.orderMap.put ( "XZ" , "XZY" );

		NameAndChannel.orderMap.put ( "Y" , "YXZ" );
		NameAndChannel.orderMap.put ( "YX" , "YXZ" );
		NameAndChannel.orderMap.put ( "YZ" , "YZX" );

		NameAndChannel.orderMap.put ( "Z" , "ZYX" );
		NameAndChannel.orderMap.put ( "ZX" , "ZXY" );
		NameAndChannel.orderMap.put ( "ZY" , "ZYX" );

		NameAndChannel.orderMap.put ( "" , "XYZ" );
	}

	private final Channels channels;
	private String name;
	private int channelType;

	public NameAndChannel ( final String name , final int channel , final Channels channels ) {
		this.name = name;
		channelType = channel;
		this.channels = channels;
	}

	public String getName ( ) {
		return this.name;
	}

	public void setName ( final String name ) {
		this.name = name;
	}

	public int getChannel ( ) {
		return this.channelType;
	}

	public void setChannel ( final int channel ) {
		channelType = channel;
	}

	public String getOrder ( ) {
		String order = this.channels.getOrder ( );
		if ( 3 > order.length ( ) ) {
			order = NameAndChannel.orderMap.get ( order );
		}
		return order;
	}
}