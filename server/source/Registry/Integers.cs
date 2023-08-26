using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Registry
{
    public class VInt16 : Entry
    {
        public VInt16(string name, Entry parent) : base(EntryType.Int16, name)
        {
            Parent = parent;
        }
        public short Value { get; set; }

        public override void readValue(BinaryReader stream)
        {
            Value = stream.ReadInt16();
        }

        public override void Write(BinaryWriter stream)
        {
            base.Write(stream);
            stream.Write(Value);
        }

        public override string PrettyPrint(int indent = 0)
        {
            return base.PrettyPrint(indent) + $" [{Value}]";
        }

        public override void setValue(object value)
        {
            base.setValue(value);

            if (value is short str)
            {
                Value = str;
            }
        }
        public VInt16 setInt16(short value)
        {
            Value = value;
            return this;
        }
    }
    public class VInt32 : Entry
    {
        public VInt32(string name, Entry parent) : base(EntryType.Int32, name)
        {
            Parent = parent;
        }
        public int Value { get; set; }

        public override void readValue(BinaryReader stream)
        {
            Value = stream.ReadInt32();
        }

        public override void Write(BinaryWriter stream)
        {
            base.Write(stream);
            stream.Write(Value);
        }
        public override string PrettyPrint(int indent = 0)
        {
            return base.PrettyPrint(indent) + $" [{Value}]";
        }

        public override void setValue(object value)
        {
            base.setValue(value);

            if (value is int str)
            {
                Value = str;
            }
        }
        public VInt32 setInt32(int value)
        {
            Value = value;
            return this;
        }
    }

    public class VInt64 : Entry
    {
        public VInt64(string name, Entry parent) : base(EntryType.Int64, name)
        {
            Parent = parent;
        }
        public long Value { get; set; }

        public override void readValue(BinaryReader stream)
        {
            Value = stream.ReadInt64();
        }

        public override void Write(BinaryWriter stream)
        {
            base.Write(stream);
            stream.Write(Value);
        }
        public override string PrettyPrint(int indent = 0)
        {
            return base.PrettyPrint(indent) + $" [{Value}]";
        }

        public override void setValue(object value)
        {
            base.setValue(value);

            if (value is long str)
            {
                Value = str;
            }
        }
        public VInt64 setInt64(long value)
        {
            Value = value;
            return this;
        }
    }

    public class VBool : Entry
    {
        public VBool(string name, Entry parent) : base(EntryType.Bool, name)
        {
            Parent = parent;
        }
        public bool Value { get; set; }

        public override void readValue(BinaryReader stream)
        {
            Value = stream.ReadBoolean();
        }

        public override void Write(BinaryWriter stream)
        {
            base.Write(stream);
            stream.Write(Value);
        }
        public override string PrettyPrint(int indent = 0)
        {
            return base.PrettyPrint(indent) + $" [{Value}]";
        }

        public override void setValue(object value)
        {
            base.setValue(value);

            if (value is bool str)
            {
                Value = str;
            }
        }

        public VBool setBool(bool value)
        {
            Value = value; 
            return this;
        }
    }
}
