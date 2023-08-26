using Harbinger.EventsBus;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.EventsBus.Events
{
    /// <summary>
    /// Dispatched every 150ms ideally.
    /// 
    /// Using that, you can take the number of ticks to determine the total percentage of lag over time.
    /// As the server ticks, it will maintain a list of the timespan between the last 50 ticks, and will utilize that to average the current server lag.
    /// </summary>
    public class ServerTickEvent : Event
    {
        public const int TickMilliseconds = 150;
        public static readonly TimeSpan TICK_AT = TimeSpan.FromMilliseconds(150);
    }
}
