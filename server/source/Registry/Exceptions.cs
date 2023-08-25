using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Registry
{
    public class OutdatedRegistryException : Exception
    {
        public OutdatedRegistryException(string message) : base(message) { }
    }
}
