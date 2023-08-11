using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.EventBus
{
    public class EventBus
    {
        public static EventBus PRIMARY = new EventBus("Main");

        public EventBus(string name)
        {
            nick = name;
        }

        private string nick;


        public Dictionary<Event, MethodInfo> registry = new();

        public void Scan(Type type)
        {
            foreach(MethodInfo method in type.GetMethods())
            {

            }
        }
    }
}
