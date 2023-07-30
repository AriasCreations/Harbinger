/**
 * Copyright (c) 2010-2012, Dahlia Trimble
 * Copyright (c) 2011-2017, Frederick Martian
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
package dev.zontreck.harbinger.thirdparty.libomv.primMesher.types;

import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3;

import java.util.ArrayList;

public class AngleList {
	private static final Angle[] angles3 = {
			new Angle ( 0.0f , 1.0f , 0.0f ) ,
			new Angle ( 0.33333333333333333f , - 0.5f , 0.86602540378443871f ) ,
			new Angle ( 0.66666666666666667f , - 0.5f , - 0.86602540378443837f ) ,
			new Angle ( 1.0f , 1.0f , 0.0f )
	};
	private static final Vector3[] normals3 =
			{
					new Vector3 ( 0.25f , 0.4330127019f , 0.0f ).normalize ( ) ,
					new Vector3 ( - 0.5f , 0.0f , 0.0f ).normalize ( ) ,
					new Vector3 ( 0.25f , - 0.4330127019f , 0.0f ).normalize ( ) ,
					new Vector3 ( 0.25f , 0.4330127019f , 0.0f ).normalize ( )
			};
	private static final Angle[] angles4 =
			{
					new Angle ( 0.0f , 1.0f , 0.0f ) ,
					new Angle ( 0.25f , 0.0f , 1.0f ) ,
					new Angle ( 0.5f , - 1.0f , 0.0f ) ,
					new Angle ( 0.75f , 0.0f , - 1.0f ) ,
					new Angle ( 1.0f , 1.0f , 0.0f )
			};
	private static final Vector3[] normals4 =
			{
					new Vector3 ( 0.5f , 0.5f , 0.0f ).normalize ( ) ,
					new Vector3 ( - 0.5f , 0.5f , 0.0f ).normalize ( ) ,
					new Vector3 ( - 0.5f , - 0.5f , 0.0f ).normalize ( ) ,
					new Vector3 ( 0.5f , - 0.5f , 0.0f ).normalize ( ) ,
					new Vector3 ( 0.5f , 0.5f , 0.0f ).normalize ( )
			};
	private static final Angle[] angles24 =
			{
					new Angle ( 0.0f , 1.0f , 0.0f ) ,
					new Angle ( 0.041666666666666664f , 0.96592582628906831f , 0.25881904510252074f ) ,
					new Angle ( 0.083333333333333329f , 0.86602540378443871f , 0.5f ) ,
					new Angle ( 0.125f , 0.70710678118654757f , 0.70710678118654746f ) ,
					new Angle ( 0.16666666666666667f , 0.5f , 0.8660254037844386f ) ,
					new Angle ( 0.20833333333333331f , 0.25881904510252096f , 0.9659258262890682f ) ,
					new Angle ( 0.25f , 0.0f , 1.0f ) ,
					new Angle ( 0.29166666666666663f , - 0.25881904510252063f , 0.96592582628906831f ) ,
					new Angle ( 0.33333333333333333f , - 0.5f , 0.86602540378443871f ) ,
					new Angle ( 0.375f , - 0.70710678118654746f , 0.70710678118654757f ) ,
					new Angle ( 0.41666666666666663f , - 0.86602540378443849f , 0.5f ) ,
					new Angle ( 0.45833333333333331f , - 0.9659258262890682f , 0.25881904510252102f ) ,
					new Angle ( 0.5f , - 1.0f , 0.0f ) ,
					new Angle ( 0.54166666666666663f , - 0.96592582628906842f , - 0.25881904510252035f ) ,
					new Angle ( 0.58333333333333326f , - 0.86602540378443882f , - 0.5f ) ,
					new Angle ( 0.62499999999999989f , - 0.70710678118654791f , - 0.70710678118654713f ) ,
					new Angle ( 0.66666666666666667f , - 0.5f , - 0.86602540378443837f ) ,
					new Angle ( 0.70833333333333326f , - 0.25881904510252152f , - 0.96592582628906809f ) ,
					new Angle ( 0.75f , 0.0f , - 1.0f ) ,
					new Angle ( 0.79166666666666663f , 0.2588190451025203f , - 0.96592582628906842f ) ,
					new Angle ( 0.83333333333333326f , 0.5f , - 0.86602540378443904f ) ,
					new Angle ( 0.875f , 0.70710678118654735f , - 0.70710678118654768f ) ,
					new Angle ( 0.91666666666666663f , 0.86602540378443837f , - 0.5f ) ,
					new Angle ( 0.95833333333333326f , 0.96592582628906809f , - 0.25881904510252157f ) ,
					new Angle ( 1.0f , 1.0f , 0.0f )
			};
	ArrayList<Angle> angles;
	ArrayList<Vector3> normals;
	private float iX, iY; // intersection point

	private Angle interpolatePoints ( final float newPoint , final Angle p1 , final Angle p2 ) {
		final float m = ( newPoint - p1.angle ) / ( p2.angle - p1.angle );
		return new Angle ( newPoint , p1.X + m * ( p2.X - p1.X ) , p1.Y + m * ( p2.Y - p1.Y ) );
	}

	private void intersection ( final double x1 , final double y1 , final double x2 , final double y2 , final double x3 , final double y3 , final double x4 , final double y4 ) { // ref: http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/
		final double denom = ( y4 - y3 ) * ( x2 - x1 ) - ( x4 - x3 ) * ( y2 - y1 );
		final double uaNumerator = ( x4 - x3 ) * ( y1 - y3 ) - ( y4 - y3 ) * ( x1 - x3 );

		if ( 0.0 != denom ) {
			final double ua = uaNumerator / denom;
			this.iX = ( float ) ( x1 + ua * ( x2 - x1 ) );
			this.iY = ( float ) ( y1 + ua * ( y2 - y1 ) );
		}
	}

	protected void makeAngles ( final int sides , float startAngle , float stopAngle ) throws Exception {
		this.angles = new ArrayList<Angle> ( );
		this.normals = new ArrayList<Vector3> ( );

		final double twoPi = Math.PI * 2.0;
		final float twoPiInv = 1.0f / ( float ) twoPi;

		if ( 1 > sides )
			throw new Exception ( "number of sides not greater than zero" );
		if ( stopAngle <= startAngle )
			throw new Exception ( "stopAngle not greater than startAngle" );

		if ( ( 3 == sides || 4 == sides || 24 == sides ) ) {
			startAngle *= twoPiInv;
			stopAngle *= twoPiInv;

			final Angle[] sourceAngles;
			if ( 3 == sides )
				sourceAngles = AngleList.angles3;
			else if ( 4 == sides )
				sourceAngles = AngleList.angles4;
			else sourceAngles = AngleList.angles24;

			final int startAngleIndex = ( int ) ( startAngle * sides );
			int endAngleIndex = sourceAngles.length - 1;
			if ( 1.0f > stopAngle )
				endAngleIndex = ( int ) ( stopAngle * sides ) + 1;
			if ( endAngleIndex == startAngleIndex )
				endAngleIndex++;

			for ( int angleIndex = startAngleIndex ; angleIndex < endAngleIndex + 1 ; angleIndex++ ) {
				this.angles.add ( sourceAngles[ angleIndex ] );
				if ( 3 == sides )
					this.normals.add ( AngleList.normals3[ angleIndex ] );
				else if ( 4 == sides )
					this.normals.add ( AngleList.normals4[ angleIndex ] );
			}

			if ( 0.0f < startAngle )
				this.angles.set ( 0 , this.interpolatePoints ( startAngle , this.angles.get ( 0 ) , this.angles.get ( 1 ) ) );

			if ( 1.0f > stopAngle ) {
				final int lastAngleIndex = this.angles.size ( ) - 1;
				this.angles.set ( lastAngleIndex , this.interpolatePoints ( stopAngle , this.angles.get ( lastAngleIndex - 1 ) , this.angles.get ( lastAngleIndex ) ) );
			}
		}
		else {
			final double stepSize = twoPi / sides;

			final int startStep = ( int ) ( startAngle / stepSize );
			double angle = stepSize * startStep;
			int step = startStep;
			double stopAngleTest = stopAngle;
			if ( twoPi > stopAngle ) {
				stopAngleTest = stepSize * ( ( int ) ( stopAngle / stepSize ) + 1 );
				if ( stopAngleTest < stopAngle )
					stopAngleTest += stepSize;
				if ( twoPi < stopAngleTest )
					stopAngleTest = twoPi;
			}

			while ( angle <= stopAngleTest ) {
				this.angles.add ( new Angle ( ( float ) angle , ( float ) Math.cos ( angle ) , ( float ) Math.sin ( angle ) ) );
				step += 1;
				angle = stepSize * step;
			}

			if ( startAngle > this.angles.get ( 0 ).angle ) {
				final Angle angle1 = this.angles.get ( 0 );
				final Angle angle2 = this.angles.get ( 1 );
				this.intersection ( angle1.X , angle1.Y , angle2.X , angle2.Y , 0.0f , 0.0f , ( float ) Math.cos ( startAngle ) , ( float ) Math.sin ( startAngle ) );
				this.angles.set ( 0 , new Angle ( startAngle , this.iX , this.iY ) );
			}

			final int index = this.angles.size ( ) - 1;
			if ( stopAngle < this.angles.get ( index ).angle ) {
				final Angle angle1 = this.angles.get ( index - 1 );
				final Angle angle2 = this.angles.get ( index );
				this.intersection ( angle1.X , angle1.Y , angle2.X , angle2.Y , 0.0f , 0.0f , ( float ) Math.cos ( stopAngle ) , ( float ) Math.sin ( stopAngle ) );
				this.angles.set ( index , new Angle ( stopAngle , this.iX , this.iY ) );
			}
		}
	}
}
