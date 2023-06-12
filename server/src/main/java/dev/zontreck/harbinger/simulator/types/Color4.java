package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.MathF;
import dev.zontreck.harbinger.utils.SimUtils;
import org.jetbrains.annotations.NotNull;

public class Color4 implements Comparable<Color4> {
	/// <summary>Red</summary>
	public float R;

	/// <summary>Green</summary>
	public float G;

	/// <summary>Blue</summary>
	public float B;

	/// <summary>Alpha</summary>
	public float A;


	/// <summary>
/// </summary>
/// <param name="r"></param>
/// <param name="g"></param>
/// <param name="b"></param>
/// <param name="a"></param>
	public Color4(byte r, byte g, byte b, byte a) {
		final float quanta = 1.0f / 255.0f;

		R = r * quanta;
		G = g * quanta;
		B = b * quanta;
		A = a * quanta;
	}

	public Color4(float r, float g, float b, float a) {
		// Quick check to see if someone is doing something obviously wrong
		// like using float values from 0.0 - 255.0
		if (r > 1f || g > 1f || b > 1f || a > 1f)
			throw new IllegalArgumentException("Attempting to initialize Color4 with out of range values <{" + r + "},{" + g + "},{" + b + "},{" + a + "}>");

		// Valid range is from 0.0 to 1.0
		R = MathF.Clamp(r, 0f, 1f);
		G = MathF.Clamp(g, 0f, 1f);
		B = MathF.Clamp(b, 0f, 1f);
		A = MathF.Clamp(a, 0f, 1f);
	}

	/// <summary>
///     Builds a color from a byte array
/// </summary>
/// <param name="byteArray">Byte array containing a 16 byte color</param>
/// <param name="pos">Beginning position in the byte array</param>
/// <param name="inverted">
///     True if the byte array stores inverted values,
///     otherwise false. For example the color black (fully opaque) inverted
///     would be 0xFF 0xFF 0xFF 0x00
/// </param>
	public Color4(byte[] byteArray, int pos, boolean inverted) {
		R = G = B = A = 0f;
		FromBytes(byteArray, pos, inverted);
	}

	/// <summary>
///     Returns the raw bytes for this vector
/// </summary>
/// <param name="byteArray">Byte array containing a 16 byte color</param>
/// <param name="pos">Beginning position in the byte array</param>
/// <param name="inverted">
///     True if the byte array stores inverted values,
///     otherwise false. For example the color black (fully opaque) inverted
///     would be 0xFF 0xFF 0xFF 0x00
/// </param>
/// <param name="alphaInverted">
///     True if the alpha value is inverted in
///     addition to whatever the inverted parameter is. Setting inverted true
///     and alphaInverted true will flip the alpha value back to non-inverted,
///     but keep the other color bytes inverted
/// </param>
/// <returns>A 16 byte array containing R, G, B, and A</returns>
	public Color4(byte[] byteArray, int pos, boolean inverted, boolean alphaInverted) {
		R = G = B = A = 0f;
		FromBytes(byteArray, pos, inverted, alphaInverted);
	}

	/// <summary>
///     Copy constructor
/// </summary>
/// <param name="color">Color to copy</param>
	public Color4(Color4 color) {
		R = color.R;
		G = color.G;
		B = color.B;
		A = color.A;
	}

	public void FromBytes(byte[] byteArray, int pos, boolean inverted) {
		final float quanta = 1.0f / 255.0f;

		if (inverted) {
			R = (255 - byteArray[pos]) * quanta;
			G = (255 - byteArray[pos + 1]) * quanta;
			B = (255 - byteArray[pos + 2]) * quanta;
			A = (255 - byteArray[pos + 3]) * quanta;
		} else {
			R = byteArray[pos] * quanta;
			G = byteArray[pos + 1] * quanta;
			B = byteArray[pos + 2] * quanta;
			A = byteArray[pos + 3] * quanta;
		}
	}

	/// <summary>
///     Builds a color from a byte array
/// </summary>
/// <param name="byteArray">Byte array containing a 16 byte color</param>
/// <param name="pos">Beginning position in the byte array</param>
/// <param name="inverted">
///     True if the byte array stores inverted values,
///     otherwise false. For example the color black (fully opaque) inverted
///     would be 0xFF 0xFF 0xFF 0x00
/// </param>
/// <param name="alphaInverted">
///     True if the alpha value is inverted in
///     addition to whatever the inverted parameter is. Setting inverted true
///     and alphaInverted true will flip the alpha value back to non-inverted,
///     but keep the other color bytes inverted
/// </param>
	public void FromBytes(byte[] byteArray, int pos, boolean inverted, boolean alphaInverted) {
		FromBytes(byteArray, pos, inverted);

		if (alphaInverted)
			A = 1.0f - A;
	}

	public byte[] GetBytes() {
		return GetBytes(false);
	}

	public byte[] GetBytes(boolean inverted) {
		var byteArray = new byte[4];
		ToBytes(byteArray, 0, inverted);
		return byteArray;
	}


	/// <summary>
///     Writes the raw bytes for this color to a byte array
/// </summary>
/// <param name="dest">Destination byte array</param>
/// <param name="pos">
///     Position in the destination array to start
///     writing. Must be at least 16 bytes before the end of the array
/// </param>
	public void ToBytes(byte[] dest, int pos) {
		ToBytes(dest, pos, false);
	}

	/// <summary>
///     Serializes this color into four bytes in a byte array
/// </summary>
/// <param name="dest">Destination byte array</param>
/// <param name="pos">
///     Position in the destination array to start
///     writing. Must be at least 4 bytes before the end of the array
/// </param>
/// <param name="inverted">
///     True to invert the output (1.0 becomes 0
///     instead of 255)
/// </param>
	public void ToBytes(byte[] dest, int pos, boolean inverted) {
		if (!inverted) {
			dest[pos + 0] = SimUtils.FloatZeroOneToByte(R);
			dest[pos + 1] = SimUtils.FloatZeroOneToByte(G);
			dest[pos + 2] = SimUtils.FloatZeroOneToByte(B);
			dest[pos + 3] = SimUtils.FloatZeroOneToByte(A);
		} else {
			dest[pos + 0] = (byte) (255 - SimUtils.FloatZeroOneToByte(R));
			dest[pos + 1] = (byte) (255 - SimUtils.FloatZeroOneToByte(G));
			dest[pos + 2] = (byte) (255 - SimUtils.FloatZeroOneToByte(B));
			dest[pos + 3] = (byte) (255 - SimUtils.FloatZeroOneToByte(A));
		}
	}


	public float GetHue() {
		final float HUE_MAX = 360f;

		var max = MathF.Max(MathF.Max(R, G), B);
		var min = MathF.Min(MathF.Min(R, B), B);

		if (max == min)
			// Achromatic, hue is undefined
			return -1f;

		if (R == max) {
			var bDelta = ((max - B) * (HUE_MAX / 6f) + (max - min) / 2f) / (max - min);
			var gDelta = ((max - G) * (HUE_MAX / 6f) + (max - min) / 2f) / (max - min);
			return bDelta - gDelta;
		}

		if (G == max) {
			var rDelta = ((max - R) * (HUE_MAX / 6f) + (max - min) / 2f) / (max - min);
			var bDelta = ((max - B) * (HUE_MAX / 6f) + (max - min) / 2f) / (max - min);
			return HUE_MAX / 3f + rDelta - bDelta;
		} else // B == max
		{
			var gDelta = ((max - G) * (HUE_MAX / 6f) + (max - min) / 2f) / (max - min);
			var rDelta = ((max - R) * (HUE_MAX / 6f) + (max - min) / 2f) / (max - min);
			return 2f * HUE_MAX / 3f + gDelta - rDelta;
		}
	}

	/// <summary>
///     Ensures that values are in range 0-1
/// </summary>
	public void ClampValues() {
		if (R < 0f) R = 0f;
		else if (R > 1f) R = 1f;

		if (G < 0f) G = 0f;
		else if (G > 1f) G = 1f;

		if (B < 0f) B = 0f;
		else if (B > 1f) B = 1f;

		if (A < 0f) A = 0f;
		else if (A > 1f) A = 1f;
	}


	/// <summary>
///     Create an RGB color from a hue, saturation, value combination
/// </summary>
/// <param name="hue">Hue</param>
/// <param name="saturation">Saturation</param>
/// <param name="value">Value</param>
/// <returns>An fully opaque RGB color (alpha is 1.0)</returns>
	public static Color4 FromHSV(double hue, double saturation, double value) {
		var r = 0d;
		var g = 0d;
		var b = 0d;

		if (saturation == 0d) {
			// If s is 0, all colors are the same.
			// This is some flavor of gray.
			r = value;
			g = value;
			b = value;
		} else {
			double p;
			double q;
			double t;

			double fractionalSector;
			int sectorNumber;
			double sectorPos;

			// The color wheel consists of 6 sectors.
			// Figure out which sector you//re in.
			sectorPos = hue / 60d;
			sectorNumber = (int) MathF.Floor(sectorPos);

			// get the fractional part of the sector.
			// That is, how many degrees into the sector
			// are you?
			fractionalSector = sectorPos - sectorNumber;

			// Calculate values for the three axes
			// of the color.
			p = value * (1d - saturation);
			q = value * (1d - saturation * fractionalSector);
			t = value * (1d - saturation * (1d - fractionalSector));

			// Assign the fractional colors to r, g, and b
			// based on the sector the angle is in.
			switch (sectorNumber) {
				case 0:
					r = value;
					g = t;
					b = p;
					break;
				case 1:
					r = q;
					g = value;
					b = p;
					break;
				case 2:
					r = p;
					g = value;
					b = t;
					break;
				case 3:
					r = p;
					g = q;
					b = value;
					break;
				case 4:
					r = t;
					g = p;
					b = value;
					break;
				case 5:
					r = value;
					g = p;
					b = q;
					break;
			}
		}

		return new Color4((float) r, (float) g, (float) b, 1f);
	}

	/// <summary>
///     Performs linear interpolation between two colors
/// </summary>
/// <param name="value1">Color to start at</param>
/// <param name="value2">Color to end at</param>
/// <param name="amount">Amount to interpolate</param>
/// <returns>The interpolated color</returns>
	public static Color4 Lerp(Color4 value1, Color4 value2, float amount) {
		return new Color4(
				SimUtils.Lerp(value1.R, value2.R, amount),
				SimUtils.Lerp(value1.G, value2.G, amount),
				SimUtils.Lerp(value1.B, value2.B, amount),
				SimUtils.Lerp(value1.A, value2.A, amount));
	}


	@Override
	public String toString() {
		return String.format("<%f, %f, %f, %f>", R, G, B, A);
	}

	public String ToRGBString() {
		return String.format("<%f, %f, %f>", R, G, B);
	}

	public boolean Equals(Object obj) {
		if (obj instanceof Color4) {
			return this == (Color4) obj;
		} else return false;
	}


	public boolean Equals(Color4 other) {
		return this == other;
	}


	/// <summary>A Color4 with zero RGB values and fully opaque (alpha 1.0)</summary>
	public static final Color4 Black = new Color4(0f, 0f, 0f, 1f);

	/// <summary>A Color4 with full RGB values (1.0) and fully opaque (alpha 1.0)</summary>
	public static final Color4 White = new Color4(1f, 1f, 1f, 1f);

	@Override
	public int compareTo(@NotNull Color4 color4) {
		if(color4.R > R) return 1;
		else if(color4.R < R) return -1;
		if(color4.G > G) return 1;
		else if(color4.G < G) return -1;
		if(color4.B > B) return 1;
		else if(color4.B < B) return -1;
		if(color4.A > A) return 1;
		else if(color4.A < A) return -1;



		return 0;
	}
}
