using Harbinger;
using Harbinger.EventsBus;
using Harbinger.EventsBus.Events;
using Harbinger.FontHelper;
using System;
using System.Reflection;
using System.Threading;

namespace Harbinger
{
    public class ServerMain
    {

        public static void Main(string[] args)
        {

            Console.WriteLine();
            EventBus.PRIMARY.Scan(typeof(ServerMain));
            EventBus.PRIMARY.post(new StartupEvent());

        }


        [Subscribe(Priority.Very_High)]
        public static void onStartup(StartupEvent startupEvent)
        {
            Fonts.init();
            Console.Clear();
            Console.Title = "Harbinger - " + GitVersion.FullVersion;
            Console.BackgroundColor = ConsoleColor.Black;
            Console.ForegroundColor = ConsoleColor.DarkGreen;

            Console.WriteLine(Fonts.RenderUsing("banner3-D", "Harbinger"));
            Console.WriteLine("We Are Harbinger");
            Console.WriteLine($"Version: {GitVersion.FullVersion}");


            // Start GUI Here


        }
    }
}
