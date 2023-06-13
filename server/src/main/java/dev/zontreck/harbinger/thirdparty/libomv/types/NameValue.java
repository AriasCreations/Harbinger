/**
 * Copyright (c) 2006-2014, openmetaverse.org
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
 * - Neither the name of the openmetaverse.org or dev.zontreck.harbinger.thirdparty.libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
package dev.zontreck.harbinger.thirdparty.libomv.types;

import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;
import dev.zontreck.harbinger.thirdparty.libomv.utils.RefObject;

public final class NameValue
{
	/* Type of the value */
	public enum ValueType
	{
		// Unknown
		Unknown(-1),
		// String value
		String(0),

		F32(1),

		S32(2),

		VEC3(3),

		U32(4),
		// Deprecated
		CAMERA(5),
		// String value, but designated as an asset
		Asset(6),

		U64(7);

		public int val;

		ValueType(final int val)
		{
			this.val = val;
		}
	}

	public enum ClassType
	{
		Unknown(-1), ReadOnly(0), ReadWrite(1), Callback(2);

		public int val;

		ClassType(final int val)
		{
			this.val = val;
		}
	}

	public enum SendtoType
	{
		Unknown(-1), Sim(0), DataSim(1), SimViewer(2), DataSimViewer(3);

		public int val;

		SendtoType(final int val)
		{
			this.val = val;
		}
	}

	public String Name;
	public ValueType Type;
	public ClassType Class;
	public SendtoType Sendto;
	public Object Value;

	private static final String[] TypeStrings = { "STRING", "F32", "S32", "VEC3", "U32", "ASSET", "U64" };
	private static final String[] ClassStrings = { "R", "RW", "CB" };
	private static final String[] SendtoStrings = { "S", "DS", "SV", "DSV" };
	private static final char[] Separators = { ' ', '\n', '\t', '\r' };

	/**
	 * Constructor that takes all the fields as parameters
	 * 
	 * @param name
	 * @param valueType
	 * @param classType
	 * @param sendtoType
	 * @param value
	 */
	public NameValue(final String name, final ValueType valueType, final ClassType classType, final SendtoType sendtoType, final Object value)
	{
		this.Name = name;
		this.Type = valueType;
		this.Class = classType;
		this.Sendto = sendtoType;
		this.Value = value;
	}

	/**
	 * Constructor that takes a single line from a NameValue field
	 * 
	 * @param data
	 */
	public NameValue(String data)
	{
		int i;

		// Name
		i = Helpers.indexOfAny(data, NameValue.Separators);
		if (1 > i)
		{
			this.Name = Helpers.EmptyString;
			this.Type = ValueType.Unknown;
			this.Class = ClassType.Unknown;
			this.Sendto = SendtoType.Unknown;
			this.Value = null;
			return;
		}
		this.Name = data.substring(0, i);
		data = data.substring(i + 1);

		// Type
		i = Helpers.indexOfAny(data, NameValue.Separators);
		if (0 < i)
		{
			this.Type = NameValue.getValueType(data.substring(0, i));
			data = data.substring(i + 1);

			// Class
			i = Helpers.indexOfAny(data, NameValue.Separators);
			if (0 < i)
			{
				this.Class = NameValue.getClassType(data.substring(0, i));
				data = data.substring(i + 1);

				// Sendto
				i = Helpers.indexOfAny(data, NameValue.Separators);
				if (0 < i)
				{
					this.Sendto = NameValue.getSendtoType(data.substring(0, 1));
					data = data.substring(i + 1);
				}
			}
		}

		// Value
		this.Type = ValueType.String;
		this.Class = ClassType.ReadOnly;
		this.Sendto = SendtoType.Sim;
		this.Value = null;
		this.setValue(data);
	}

	public static String NameValuesToString(final NameValue[] values)
	{
		if (null == values || 0 == values.length)
		{
			return "";
		}

		final StringBuilder output = new StringBuilder();

		for (int i = 0; i < values.length; i++)
		{
			final NameValue value = values[i];

			if (null != value.Value)
			{
				final String newLine = (i < values.length - 1) ? "\n" : "";
				output.append(String.format("%s %s %s %s %s%s", value.Name, NameValue.TypeStrings[value.Type.val],
						NameValue.ClassStrings[value.Class.val], NameValue.SendtoStrings[value.Sendto.val], value.Value, newLine));
			}
		}

		return output.toString();
	}

	private void setValue(final String value)
	{
		switch (this.Type)
		{
			case Asset:
			case String:
				this.Value = value;
				break;
			case F32:
			{
				final float temp = Helpers.TryParseFloat(value);
				this.Value = temp;
				break;
			}
			case S32:
			{
				final int temp;
				temp = Helpers.TryParseInt(value);
				this.Value = temp;
				break;
			}
			case U32:
			{
				final int temp = Helpers.TryParseInt(value);
				this.Value = temp;
				break;
			}
			case U64:
			{
				final long temp = Helpers.TryParseLong(value);
				this.Value = temp;
				break;
			}
			case VEC3:
			{
				final RefObject<Vector3> temp = new RefObject<Vector3>((Vector3) this.Value);
				Vector3.TryParse(value, temp);
				break;
			}
			default:
				this.Value = null;
				break;
		}
	}

	private static ValueType getValueType(final String value)
	{
		ValueType type = ValueType.Unknown;
		final int i = 1;
		for (final String s : NameValue.TypeStrings)
		{
			if (s.equals(value)) {
				type = ValueType.values()[i];
				break;
			}
		}

		if (ValueType.Unknown == type)
		{
			type = ValueType.String;
		}

		return type;
	}

	private static ClassType getClassType(final String value)
	{
		ClassType type = ClassType.Unknown;
		final int i = 1;
		for (final String s : NameValue.ClassStrings)
		{
			if (s.equals(value)) {
				type = ClassType.values()[i];
				break;
			}
		}

		if (ClassType.Unknown == type)
		{
			type = ClassType.ReadOnly;
		}

		return type;
	}

	private static SendtoType getSendtoType(final String value)
	{
		SendtoType type = SendtoType.Unknown;
		final int i = 1;
		for (final String s : NameValue.SendtoStrings)
		{
			if (s.equals(value)) {
				type = SendtoType.values()[i];
				break;
			}
		}

		if (SendtoType.Unknown == type)
		{
			type = SendtoType.Sim;
		}

		return type;
	}
}
