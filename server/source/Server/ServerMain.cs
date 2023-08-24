using Harbinger.EventsBus;
using Harbinger.EventsBus.Events;
using Harbinger.FontHelper;
using Harbinger.GUI;
using System;
using System.Threading;

namespace Harbinger
{

    public class HarbingerContext
    {
        public static CancellationTokenSource KeepAlive;
        private static long x_ticks = 0;
        private static readonly object lck = new object();
        public static long TotalTicks
        {
            get
            {
                lock(lck)
                {
                    return x_ticks;
                }
            }
            set
            {
                lock (lck)
                {
                    x_ticks = value;
                }
            }
        }


        [Subscribe(Priority.High)]
        public static void onTick(ServerTickEvent ev)
        {
            TotalTicks++;
        }
    }
    public class ServerMain
    {

        public static void Main(string[] args)
        {

            Console.WriteLine();
            EventBus.Broadcast(new StartupEvent());
            HarbingerContext.KeepAlive = new CancellationTokenSource();

            return;
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
            Welcome.StartMain();

            // Register the server tick event on HarbingerContext
            EventBus.PRIMARY.Scan(typeof(HarbingerContext));

            Thread Main_Ticks = new Thread(() =>
            {
                EventBus.PRIMARY.post(new ServerTickEvent());
            });

            Timer X = new Timer((X) =>
            {
                EventBus.PRIMARY.post(new ServerTickEvent());
            }, null, TimeSpan.FromMilliseconds(250), TimeSpan.FromMilliseconds(250));
        }
    }
}
