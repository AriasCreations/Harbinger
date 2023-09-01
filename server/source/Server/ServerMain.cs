using Harbinger.EventsBus;
using Harbinger.EventsBus.Attributes;
using Harbinger.EventsBus.Events;
using Harbinger.FontHelper;
using Harbinger.Framework;
using Harbinger.Framework.HTTP;
using Harbinger.Framework.HTTPHandlers.Commands;
using Harbinger.Framework.Registry;
using Harbinger.GUI;
using System;
using System.IO;
using System.Linq;
using System.Threading;


namespace Harbinger
{
    public class Harbinger
    {

        /// <summary>
        /// This exists only to make sure dotnet loads this library... ugh
        /// </summary>
        public static void init()
        {
            Framework.Framework.init();
            HTTPServer.init();
            VersionHandler.init();
        }
    }
    public class HarbingerContext
    {
        public const string LAST_UPTIME = "root/HKS/laststats";

        public static CancellationTokenSource KeepAlive;
        private static long x_ticks = 0;
        private static readonly object lck = new object();
        private static Key LAST_STAT_KEY;
        public static Thread Ticker;

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

        [Subscribe(Priority.Severe)]
        public static void onStop(ShutdownEvent ev)
        {
            // Save the uptime
            if (!LAST_STAT_KEY.HasNamedKey("Ticks")) LAST_STAT_KEY.Add(new VInt64("Ticks", TotalTicks));
            else LAST_STAT_KEY.getNamed("Ticks").Int64().Value = TotalTicks;
        }

        [Subscribe(Priority.High)]
        public static void onRegistryLoaded(RegistryLoadedEvent ev)
        {
            // Retrieve stats key
            LAST_STAT_KEY = Entry.getByPath(LAST_UPTIME)?.Key();

            if(LAST_STAT_KEY == null)
            {
                LAST_STAT_KEY = new Key("laststats", null);
                Entry.ROOT.placeAtPath(LAST_UPTIME.Substring(0, LAST_UPTIME.LastIndexOf("/")), LAST_STAT_KEY);

                LAST_STAT_KEY = Entry.getByPath(LAST_UPTIME)?.Key();
            }
        }
    }
    public class ServerMain
    {

        public static void Main(string[] args)
        {
            Harbinger.init();
            Console.ForegroundColor = ConsoleColor.White;
            Console.BackgroundColor = ConsoleColor.Black;
            Console.Title = "Loading...";
            Thread.Sleep(1000);

            HarbingerContext.KeepAlive = Framework.Framework.keepAlive;
            DelayedExecutorService.setCancellationToken(HarbingerContext.KeepAlive);


            PluginLoader.PreloadReferencedAssemblies();
            Console.WriteLine();
            EventBus.Broadcast(new StartupEvent());

            Console.CancelKeyPress += Console_CancelKeyPress;

            while (!HarbingerContext.KeepAlive.IsCancellationRequested)
            {
                Thread.Sleep(100);
            }


            Console.ForegroundColor = ConsoleColor.White;
            Console.ResetColor();
            return;
        }

        private static void Console_CancelKeyPress(object sender, ConsoleCancelEventArgs e)
        {
            EventBus.Broadcast(new ShutdownEvent());
            HarbingerContext.KeepAlive.Cancel();

            // EventBus will block the thread until all shutdown event handlers have exited.
            // We can assume we can safely exit now.
            Console.ForegroundColor = ConsoleColor.White;
            Environment.Exit(0);
        }


        [Subscribe(Priority.Uncategorizable)]
        public static void onStartup(StartupEvent startupEvent)
        {
            Fonts.init();

            Console.Clear();
            Console.Title = "Harbinger - " + GitVersion.FullVersion;
            Console.BackgroundColor = ConsoleColor.Black;
            Console.ForegroundColor = ConsoleColor.DarkRed;

            Console.WriteLine(Fonts.RenderUsing("banner3-D", "Harbinger"));
            Console.ForegroundColor = ConsoleColor.DarkGreen;
            Console.WriteLine("We Are Harbinger");
            Console.WriteLine($"Version: {GitVersion.FullVersion}");


            // Start GUI Here
            Welcome.StartMain();

            // Register the server tick event on HarbingerContext
            EventBus.PRIMARY.Scan(typeof(HarbingerContext));
            

            HarbingerContext.Ticker = new Thread(() =>
            {
                while (!HarbingerContext.KeepAlive.IsCancellationRequested)
                {
                    EventBus.PRIMARY.post(new ServerTickEvent());

                    Thread.Sleep(ServerTickEvent.TickMilliseconds);
                }
                
            });
            HarbingerContext.Ticker.Start();

            
        }
    }
}
