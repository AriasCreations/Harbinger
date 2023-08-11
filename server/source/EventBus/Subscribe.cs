using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.EventBus
{
    [AttributeUsage(AttributeTargets.Method)]
    public class SubscribeAttribute : Attribute
    {
        public readonly Priority priority_level;
        public readonly bool isSingleShot;

        public SubscribeAttribute(Priority priority)
        {
            priority_level = priority;
            isSingleShot = false;
        }
    }
}
