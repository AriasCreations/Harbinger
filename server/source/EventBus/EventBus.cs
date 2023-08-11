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


        public Dictionary<Type, List<EventContainer>> registry = new();

        private void Scan(Type type)
        {
            foreach (MethodInfo method in type.GetMethods())
            {
                var subscribe = method.GetCustomAttribute<SubscribeAttribute>();
                if (subscribe != null)
                {
                    ParameterInfo[] para = method.GetParameters();
                    if (para[0].ParameterType.IsSubclassOf(typeof(Event)))
                    {
                        var isSingleShot = subscribe.isSingleShot;
                        registerEvent(para[0].ParameterType, new EventContainer(method, subscribe, isSingleShot), isSingleShot);
                    }
                }
            }
        }

        private void registerEvent(Type type, EventContainer container, bool isSingleShot)
        {
            if (registry.ContainsKey(type))
            {
                registry[type].Add(container);
            }
            else
            {
                registry[type] = new List<EventContainer> { container };
            }

            if (isSingleShot)
            {
                // Mark this container as a single-shot subscriber
                container.isSingleShot = true;
            }
        }

        public bool post(Event evt)
        {
            if (registry.ContainsKey(evt.GetType()))
            {
                List<EventContainer> containers = registry[evt.GetType()];

                containers.Sort((container1, container2) =>
                    container2.subscribeData.priority_level.CompareTo(container1.subscribeData.priority_level));

                foreach (EventContainer container in containers.ToArray()) // Use ToArray to avoid modifying the list while iterating
                {
                    container.function.Invoke(null, new object[1] { evt });

                    if (container.isSingleShot)
                    {
                        containers.Remove(container);
                    }
                }

                return evt.isCancelled;
            }

            return false;
        }
    }

    public class EventContainer
    {
        public MethodInfo function;
        public SubscribeAttribute subscribeData;
        public bool isSingleShot; // Add this field

        public EventContainer(MethodInfo func, SubscribeAttribute subscribeData, bool isSingleShot)
        {
            this.function = func;
            this.subscribeData = subscribeData;
            this.isSingleShot = isSingleShot;
        }
    }
}
