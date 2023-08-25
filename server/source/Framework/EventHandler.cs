using Harbinger.EventsBus;
using Harbinger.EventsBus.Attributes;
using Harbinger.EventsBus.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;


[assembly: EventBusBroadcastable()]
namespace Harbinger.Framework
{
    public class EventHandler
    {
        [Subscribe(Priority.Severe)]
        public static void onStart(StartupEvent ev)
        {
            EventBus.PRIMARY.Scan(typeof(SecondLifeBotAccount));
        }
    }
}
