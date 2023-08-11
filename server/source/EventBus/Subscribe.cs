using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.EventBus
{
    [AttributeUsage(AttributeTargets.Method)]
    public class Subscribe : Attribute
    {
        public readonly Priority priority_level;

        public Subscribe(Priority priority)
        {
            priority_level = priority;
        }
    }
}
