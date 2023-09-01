using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Registry
{
    public enum EntryType : byte
    {
        Word,
        Int16,
        Int32,
        Int64,
        Bool,
        Byte,
        Empty, // Undefined value type
        
        Key,    // Contains children
        Root    // Contains Children - Is key but with nullable parent
    }
}
