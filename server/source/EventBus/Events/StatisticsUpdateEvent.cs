using Harbinger.EventsBus;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.EventsBus.Events
{
    /// <summary>
    /// Broadcasts to all classes when the EventStatistics is updated
    /// </summary>
    public class StatisticsUpdateEvent : Event
    {
        public StatisticsUpdateEvent() { 
        }
    }
}
