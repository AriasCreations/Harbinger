using Harbinger.EventsBus;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.EventsBus
{
    public class EventBusStatistics
    {
        public static string EventName;
        public static bool Cancelled;
        public static Event CurrentEvent;

        public static string CurrentBus;

        public static string CurrentlyInvokedClass;
        public static string CurrentlyInvokedMethod;

        public static int TotalHandlers = 0;
    }
}
