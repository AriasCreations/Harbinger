using Harbinger.EventsBus.Attributes;
using Harbinger.EventsBus.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;

[assembly:EventBusBroadcastable()]
namespace Harbinger.EventsBus
{
    public class EventBus
    {
        public static EventBus PRIMARY = new EventBus("Primary");

        internal EventBus(string name, bool updatesEventStats=false)
        {
            nick = name;
            stats = updatesEventStats;
        }

        private string nick;
        private bool stats;

        public static bool debug = true;


        public Dictionary<string, List<EventContainer>> registry = new();

        public void Scan(Type type, bool disallowSingleShot=false)
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
                        registerEvent(para[0].ParameterType, new EventContainer(method, subscribe, disallowSingleShot? false : isSingleShot), disallowSingleShot?false: isSingleShot);
                    }
                }
            }
        }

        private static EventBus broadcaster;

        /// <summary>
        /// Broadcasts the event to all classes regardless of registration with the Bus.
        /// </summary>
        /// <param name="evt">The Event</param>
        /// <returns>Cancellation status</returns>
        public static bool Broadcast(Event evt)
        {
            if(broadcaster == null)
            {
                broadcaster = new EventBus("Broadcaster");

                foreach (Assembly asm in AppDomain.CurrentDomain.GetAssemblies())
                {
                    try
                    {
                        if (asm.GetCustomAttribute<EventBusBroadcastableAttribute>() == null) continue;

                    }catch(Exception ex)
                    {
                        continue;
                    }
                    if(debug)
                        Console.WriteLine($"Scanning: {asm.FullName}");
                    try
                    {

                        foreach (Type t in asm.GetTypes())
                        {
                            broadcaster.Scan(t);
                        }
                    }catch(Exception e) { }
                }
            }


            return broadcaster.post(evt);
        }


        [Subscribe(Priority.Uncategorizable, true)]
        public static void onResetEventBus(ResetEventBusEvent evt)
        {
            evt.setCancelled(true);
            PRIMARY.clear();
        }

        /// <summary>
        /// Clears all registered events from the bus
        /// </summary>
        private void clear()
        {
            registry.Clear();
            EventBusStatistics.TotalHandlers = 0;

            Broadcast(new StatisticsUpdateEvent());
        }

        private void registerEvent(Type type, EventContainer container, bool isSingleShot)
        {
            if (registry.ContainsKey(type.FullName))
            {
                registry[type.FullName].Add(container);
            }
            else
            {
                registry[type.FullName] = new List<EventContainer> { container };
            }

            if(stats)
                EventBusStatistics.TotalHandlers++;

            if (isSingleShot)
            {
                // Mark this container as a single-shot subscriber
                container.isSingleShot = true;
            }
        }

        public bool post(Event evt)
        {
            if (stats)
            {

                EventBusStatistics.EventName = evt.GetType().Name;
                EventBusStatistics.CurrentEvent = evt;
                EventBusStatistics.CurrentBus = nick;
                EventBusStatistics.Cancelled = false;
            }
            if (registry.ContainsKey(evt.GetType().FullName))
            {
                List<EventContainer> containers = registry[evt.GetType().FullName];

                containers.Sort((container1, container2) =>
                    container2.subscribeData.priority_level.CompareTo(container1.subscribeData.priority_level));

                foreach (EventContainer container in containers.ToArray()) // Use ToArray to avoid modifying the list while iterating
                {
                    if (stats)
                    {

                        EventBusStatistics.CurrentlyInvokedClass = container.function.DeclaringType.Name;
                        EventBusStatistics.CurrentlyInvokedMethod = container.function.Name;

                    }

                    if(stats)
                        Broadcast(new StatisticsUpdateEvent());

                    container.function.Invoke(null, new object[1] { evt });

                    if(stats)
                        EventBusStatistics.Cancelled = evt.isCancelled;

                    if (container.isSingleShot)
                    {
                        containers.Remove(container);
                        if (stats)
                            EventBusStatistics.TotalHandlers--;
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
