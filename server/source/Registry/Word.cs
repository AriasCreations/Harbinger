using DSharpPlus.Entities;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Registry
{
    public class Word : Entry
    {
        public Word(string name, string value) : base(EntryType.Word, name) {
            Parent = null;
            Value = value;
        }
        public string Value { get; set; } = "";

        public override void Write(BinaryWriter stream)
        {
            base.Write(stream);
            stream.Write(Value);
        }

        public override void readValue(BinaryReader stream)
        {
            Value = stream.ReadString();
        }

        public override string PrettyPrint(int indent = 0)
        {
            return base.PrettyPrint(indent) + " [" + Value + "]";
        }

        public override void setValue(object value)
        {
            base.setValue(value);

            if(value is string str)
            {
                Value = str;
            }
        }

        public Word setWord(string value)
        {
            Value = value;
            return this;
        }
    }
}
