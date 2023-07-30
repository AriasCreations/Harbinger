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

public class BVHWriter {
	public String writeToString ( final BVH bvh ) {
		final StringBuilder buffer = new StringBuilder ( );
		//hierachy
		buffer.append ( "HIERARCHY\n" );
		this.writeTo ( bvh.getHiearchy ( ) , buffer , 0 );

		//MOTION
		final int frames = bvh.getFrames ( );
		buffer.append ( "MOTION\n" );
		buffer.append ( "Frames: " + frames + "\n" );
		buffer.append ( "Frame Time: " + bvh.getFrameTime ( ) + "\n" );

		for ( int i = 0 ; i < frames ; i++ ) {
			final float[] values = bvh.getFrameAt ( i );
			String v = "";
			for ( int j = 0 ; j < values.length ; j++ ) {
				v += values[ j ];
				if ( j != values.length - 1 ) {
					v += " ";
				}
			}
			buffer.append ( v + "\n" );
		}
		//return text;
		return buffer.toString ( );
	}

	private void writeTo ( final BVHNode node , final StringBuilder buffer , final int indent ) {
		String indentText = "";
		for ( int i = 0 ; i < indent ; i++ ) {
			indentText += "\t";
		}
		if ( 0 == indent ) {
			buffer.append ( "ROOT " + node.getName ( ) + "\n" );
		}
		else {
			buffer.append ( indentText + "JOINT " + node.getName ( ) + "\n" );
		}
		buffer.append ( indentText + "{" + "\n" );
		//offset
		buffer.append ( "\t" + indentText + node.getOffset ( ).toString ( ) + "\n" );
		//channel
		buffer.append ( "\t" + indentText + node.getChannels ( ).toString ( ) + "\n" );
		//joint
		for ( int i = 0 ; i < node.getJoints ( ).size ( ) ; i++ ) {
			this.writeTo ( node.getJoints ( ).get ( i ) , buffer , indent + 1 );
		}
		//endsite
		if ( null != node.getEndSite ( ) ) {
			buffer.append ( "\t" + indentText + "End Site" + "\n" );
			buffer.append ( "\t" + indentText + "{" + "\n" );
			buffer.append ( "\t" + indentText + "\t" + node.getEndSite ( ).toString ( ) + "\n" );
			buffer.append ( "\t" + indentText + "}" + "\n" );
		}
		buffer.append ( indentText + "}" + "\n" );
	}
}