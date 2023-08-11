using Harbinger.EventBus;
using Harbinger.Events;
using System;

namespace Server
{
    public class ServerMain
    {
        public static void Main(string[] args)
        {
            Console.WriteLine();
            EventBus.PRIMARY.post(new StartupEvent());
        }


        [Subscribe(Priority.Very_High)]
        public static void onStartup(StartupEvent startupEvent)
        {
            Console.WriteLine("We Are Harbinger");

        }
    }
}
