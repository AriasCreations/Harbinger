using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Structures
{

    public class UUID
    {
        private readonly long mostSigBits;
        private readonly long leastSigBits;

        public UUID(long mostSigBits, long leastSigBits)
        {
            this.mostSigBits = mostSigBits;
            this.leastSigBits = leastSigBits;
        }

        public static UUID RandomUUID()
        {
            byte[] bytes = new byte[16];
            
            new Random().NextBytes(bytes);
            bytes[6] = (byte)((bytes[6] & 0x0F) | 0x40); // Set version (bits 12-15) to 0100 (version 4)
            bytes[8] = (byte)((bytes[8] & 0x3F) | 0x80); // Set bits 6-7 to 10 for variant

            long mostSigBits = FromBytes(bytes, 0);
            long leastSigBits = FromBytes(bytes, 8);


            return new UUID(mostSigBits, leastSigBits);
        }

        public static UUID FromName(string Name, UUIDVersion version)
        {
            if (string.IsNullOrEmpty(Name)) throw new ArgumentException("Name cannot be null");

            byte[] nameBytes = Encoding.UTF8.GetBytes(Name);
            using(HashAlgorithm ha = (version == UUIDVersion.MD5) ? MD5.Create() : SHA1.Create())
            {
                byte[] hash = ha.ComputeHash(nameBytes);


                // Set version and variant bits in the hash
                hash[6] = (byte)((hash[6] & 0x0F) | ((byte)version << 4));
                hash[8] = (byte)((hash[8] & 0x3F) | 0x80);

                long mostSigBits = FromBytes(hash, 0);
                long leastSigBits = FromBytes(hash, 8);

                return new UUID(mostSigBits, leastSigBits);
            }
        }

        private static long FromBytes(byte[] bytes, int startIndex)
        {
            long value = 0;
            for (int i = 0; i < 8; i++)
            {
                value = (value << 8) | bytes[startIndex + i];
            }
            return value;
        }


        public static UUID Parse(string input)
        {
            if (input == null)
                throw new ArgumentNullException(nameof(input));

            input = input.Replace("-", ""); // Remove dashes if present

            if (input.Length != 32)
                throw new FormatException("Invalid UUID string format.");

            long mostSigBits = long.Parse(input.Substring(0, 16), System.Globalization.NumberStyles.HexNumber);
            long leastSigBits = long.Parse(input.Substring(16, 16), System.Globalization.NumberStyles.HexNumber);

            return new UUID(mostSigBits, leastSigBits);
        }

        private static readonly char[] HexChars = "0123456789abcdef".ToCharArray();

        public override string ToString()
        {
            char[] chars = new char[36];
            FormatHex(mostSigBits >> 32, chars, 0, 8);
            chars[8] = '-';
            FormatHex((mostSigBits >> 16) & 0xFFFF, chars, 9, 4);
            chars[13] = '-';
            FormatHex(mostSigBits & 0xFFFF, chars, 14, 4);
            chars[18] = '-';
            FormatHex(leastSigBits >> 48, chars, 19, 4);
            chars[23] = '-';
            FormatHex(leastSigBits & 0xFFFFFFFFFFFF, chars, 24, 12);

            return new string(chars);
        }

        private static void FormatHex(long value, char[] buffer, int offset, int length)
        {
            for (int i = offset + length - 1; i >= offset; i--)
            {
                buffer[i] = HexChars[value & 0xF];
                value >>= 4;
            }
        }

        public override bool Equals(object obj)
        {
            if (obj == this)
                return true;

            if (!(obj is UUID))
                return false;

            UUID other = (UUID)obj;
            return mostSigBits == other.mostSigBits && leastSigBits == other.leastSigBits;
        }

        public override int GetHashCode()
        {
            unchecked
            {
                int result = (int)(mostSigBits ^ (mostSigBits >> 32));
                result = 31 * result + (int)(leastSigBits ^ (leastSigBits >> 32));
                return result;
            }
        }

        public static explicit operator string(UUID ID)
        {
            return ID.ToString();
        }

        public static explicit operator UUID(string ID)
        {
            return Parse(ID);
        }

        public static explicit operator long[](UUID ID)
        {
            long[] l = new long[2];
            l[0] = ID.mostSigBits;
            l[1] = ID.leastSigBits;

            return l;
        }

        public static explicit operator UUID(long[] arr)
        {
            return new UUID(arr[0], arr[1]);
        }
    }


    public enum UUIDVersion
    {
        TimeBased = 1,
        MD5 = 3,
        SHA1 = 5,
        Random = 4
    }
}
