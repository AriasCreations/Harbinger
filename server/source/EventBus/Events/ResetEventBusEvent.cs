using Harbinger.EventsBus;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.EventsBus.Events
{
    /// <summary>
    /// This event is mass broadcast. Registration is not necessary, however the handler should ideally be static.
    /// </summary>
    public class ResetEventBusEvent : Event
    {
    }
}
