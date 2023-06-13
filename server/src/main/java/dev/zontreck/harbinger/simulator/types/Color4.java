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
	public Color4(final byte r, final byte g, final byte b, final byte a) {
		final float quanta = 1.0f / 255.0f;

		this.R = r * quanta;
		this.G = g * quanta;
		this.B = b * quanta;
		this.A = a * quanta;
	}

	public Color4(final float r, final float g, final float b, final float a) {
		// Quick check to see if someone is doing something obviously wrong
		// like using float values from 0.0 - 255.0
		if (1.0f < r || 1.0f < g || 1.0f < b || 1.0f < a)
			throw new IllegalArgumentException("Attempting to initialize Color4 with out of range values <{" + r + "},{" + g + "},{" + b + "},{" + a + "}>");

		// Valid range is from 0.0 to 1.0
		this.R = MathF.Clamp(r, 0.0f, 1.0f);
		this.G = MathF.Clamp(g, 0.0f, 1.0f);
		this.B = MathF.Clamp(b, 0.0f, 1.0f);
		this.A = MathF.Clamp(a, 0.0f, 1.0f);
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
	public Color4(final byte[] byteArray, final int pos, final boolean inverted) {
		this.R = this.G = this.B = this.A = 0.0f;
		this.FromBytes(byteArray, pos, inverted);
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
	public Color4(final byte[] byteArray, final int pos, final boolean inverted, final boolean alphaInverted) {
		this.R = this.G = this.B = this.A = 0.0f;
		this.FromBytes(byteArray, pos, inverted, alphaInverted);
	}

	/// <summary>
///     Copy constructor
/// </summary>
/// <param name="color">Color to copy</param>
	public Color4(final Color4 color) {
		this.R = color.R;
		this.G = color.G;
		this.B = color.B;
		this.A = color.A;
	}

	public void FromBytes(final byte[] byteArray, final int pos, final boolean inverted) {
		final float quanta = 1.0f / 255.0f;

		if (inverted) {
			this.R = (255 - byteArray[pos]) * quanta;
			this.G = (255 - byteArray[pos + 1]) * quanta;
			this.B = (255 - byteArray[pos + 2]) * quanta;
			this.A = (255 - byteArray[pos + 3]) * quanta;
		} else {
			this.R = byteArray[pos] * quanta;
			this.G = byteArray[pos + 1] * quanta;
			this.B = byteArray[pos + 2] * quanta;
			this.A = byteArray[pos + 3] * quanta;
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
	public void FromBytes(final byte[] byteArray, final int pos, final boolean inverted, final boolean alphaInverted) {
		this.FromBytes(byteArray, pos, inverted);

		if (alphaInverted)
			this.A = 1.0f - this.A;
	}

	public byte[] GetBytes() {
		return this.GetBytes(false);
	}

	public byte[] GetBytes(final boolean inverted) {
		final var byteArray = new byte[4];
		this.ToBytes(byteArray, 0, inverted);
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
	public void ToBytes(final byte[] dest, final int pos) {
		this.ToBytes(dest, pos, false);
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
	public void ToBytes(final byte[] dest, final int pos, final boolean inverted) {
		if (!inverted) {
			dest[pos] = SimUtils.FloatZeroOneToByte(this.R);
			dest[pos + 1] = SimUtils.FloatZeroOneToByte(this.G);
			dest[pos + 2] = SimUtils.FloatZeroOneToByte(this.B);
			dest[pos + 3] = SimUtils.FloatZeroOneToByte(this.A);
		} else {
			dest[pos] = (byte) (255 - SimUtils.FloatZeroOneToByte(this.R));
			dest[pos + 1] = (byte) (255 - SimUtils.FloatZeroOneToByte(this.G));
			dest[pos + 2] = (byte) (255 - SimUtils.FloatZeroOneToByte(this.B));
			dest[pos + 3] = (byte) (255 - SimUtils.FloatZeroOneToByte(this.A));
		}
	}


	public float GetHue() {
		final float HUE_MAX = 360.0f;

		final var max = MathF.Max(MathF.Max(this.R, this.G), this.B);
		final var min = MathF.Min(MathF.Min(this.R, this.B), this.B);

		if (max == min)
			// Achromatic, hue is undefined
			return -1.0f;

		if (this.R == max) {
			final var bDelta = ((max - this.B) * (HUE_MAX / 6.0f) + (max - min) / 2.0f) / (max - min);
			final var gDelta = ((max - this.G) * (HUE_MAX / 6.0f) + (max - min) / 2.0f) / (max - min);
			return bDelta - gDelta;
		}

		if (this.G == max) {
			final var rDelta = ((max - this.R) * (HUE_MAX / 6.0f) + (max - min) / 2.0f) / (max - min);
			final var bDelta = ((max - this.B) * (HUE_MAX / 6.0f) + (max - min) / 2.0f) / (max - min);
			return HUE_MAX / 3.0f + rDelta - bDelta;
		} else // B == max
		{
			final var gDelta = ((max - this.G) * (HUE_MAX / 6.0f) + (max - min) / 2.0f) / (max - min);
			final var rDelta = ((max - this.R) * (HUE_MAX / 6.0f) + (max - min) / 2.0f) / (max - min);
			return 2.0f * HUE_MAX / 3.0f + gDelta - rDelta;
		}
	}

	/// <summary>
///     Ensures that values are in range 0-1
/// </summary>
	public void ClampValues() {
		if (0.0f > R) this.R = 0.0f;
		else if (1.0f < R) this.R = 1.0f;

		if (0.0f > G) this.G = 0.0f;
		else if (1.0f < G) this.G = 1.0f;

		if (0.0f > B) this.B = 0.0f;
		else if (1.0f < B) this.B = 1.0f;

		if (0.0f > A) this.A = 0.0f;
		else if (1.0f < A) this.A = 1.0f;
	}


	/// <summary>
///     Create an RGB color from a hue, saturation, value combination
/// </summary>
/// <param name="hue">Hue</param>
/// <param name="saturation">Saturation</param>
/// <param name="value">Value</param>
/// <returns>An fully opaque RGB color (alpha is 1.0)</returns>
	public static Color4 FromHSV(final double hue, final double saturation, final double value) {
		var r = 0.0d;
		var g = 0.0d;
		var b = 0.0d;

		if (0.0d == saturation) {
			// If s is 0, all colors are the same.
			// This is some flavor of gray.
			r = value;
			g = value;
			b = value;
		} else {
			final double p;
			final double q;
			final double t;

			final double fractionalSector;
			final int sectorNumber;
			final double sectorPos;

			// The color wheel consists of 6 sectors.
			// Figure out which sector you//re in.
			sectorPos = hue / 60.0d;
			sectorNumber = (int) MathF.Floor(sectorPos);

			// get the fractional part of the sector.
			// That is, how many degrees into the sector
			// are you?
			fractionalSector = sectorPos - sectorNumber;

			// Calculate values for the three axes
			// of the color.
			p = value * (1.0d - saturation);
			q = value * (1.0d - saturation * fractionalSector);
			t = value * (1.0d - saturation * (1.0d - fractionalSector));

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

		return new Color4((float) r, (float) g, (float) b, 1.0f);
	}

	/// <summary>
///     Performs linear interpolation between two colors
/// </summary>
/// <param name="value1">Color to start at</param>
/// <param name="value2">Color to end at</param>
/// <param name="amount">Amount to interpolate</param>
/// <returns>The interpolated color</returns>
	public static Color4 Lerp(final Color4 value1, final Color4 value2, final float amount) {
		return new Color4(
				SimUtils.Lerp(value1.R, value2.R, amount),
				SimUtils.Lerp(value1.G, value2.G, amount),
				SimUtils.Lerp(value1.B, value2.B, amount),
				SimUtils.Lerp(value1.A, value2.A, amount));
	}


	@Override
	public String toString() {
		return String.format("<%f, %f, %f, %f>", this.R, this.G, this.B, this.A);
	}

	public String ToRGBString() {
		return String.format("<%f, %f, %f>", this.R, this.G, this.B);
	}

	public boolean Equals(final Object obj) {
		if (obj instanceof Color4) {
			return this == obj;
		} else return false;
	}


	public boolean Equals(final Color4 other) {
		return this == other;
	}


	/// <summary>A Color4 with zero RGB values and fully opaque (alpha 1.0)</summary>
	public static final Color4 Black = new Color4(0.0f, 0.0f, 0.0f, 1.0f);

	/// <summary>A Color4 with full RGB values (1.0) and fully opaque (alpha 1.0)</summary>
	public static final Color4 White = new Color4(1.0f, 1.0f, 1.0f, 1.0f);

	@Override
	public int compareTo(@NotNull final Color4 color4) {
		if(color4.R > this.R) return 1;
		else if(color4.R < this.R) return -1;
		if(color4.G > this.G) return 1;
		else if(color4.G < this.G) return -1;
		if(color4.B > this.B) return 1;
		else if(color4.B < this.B) return -1;
		if(color4.A > this.A) return 1;
		else if(color4.A < this.A) return -1;



		return 0;
	}
}
