using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.EventsBus.Events
{
    /// <summary>
    /// This event is broadcast to all classes
    /// </summary>
    public class ShutdownEvent : Event
    {
        public ShutdownEvent()
        {

        }
    }
}
