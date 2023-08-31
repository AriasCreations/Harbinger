using Harbinger.EventsBus;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.EventsBus.Events
{
    /// <summary>
    /// Broadcast to all classes. Registration is not necessary.
    /// Preferable that the method is static
    /// </summary>
    public class StartupEvent : Event
    {

    }
}
