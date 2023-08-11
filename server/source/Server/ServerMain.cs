using Figgle;
using Harbinger;
using Harbinger.EventBus;
using Harbinger.Events;
using System;
using System.Reflection;
using System.Threading;

namespace Server
{
    public class ServerMain
    {

        public static void Main(string[] args)
        {
            //string text = args[0];

            /*
            foreach (var info in typeof(Fonts).GetProperties())
            {
                FiggleFont font = FiggleFonts.TryGetByName(((string)info.GetValue(null)));
                if (font!=null)
                {
                    try
                    {

                        Console.WriteLine(font.Render((text.Length>0 ? text : info.Name)) + $"\n{info.Name}");
                        Thread.Sleep(150);
                    }catch(Exception ex) { }

                }
            }*/

            Console.WriteLine();
            EventBus.PRIMARY.Scan(typeof(ServerMain));
            EventBus.PRIMARY.post(new StartupEvent());

        }


        [Subscribe(Priority.Very_High)]
        public static void onStartup(StartupEvent startupEvent)
        {
            Console.Clear();
            Console.Title = "Harbinger - " + GitVersion.FullVersion;
            Console.BackgroundColor = ConsoleColor.Black;
            Console.ForegroundColor = ConsoleColor.DarkGreen;


            Console.WriteLine(FiggleFonts.Banner3D.Render("Harbinger"));
            Console.WriteLine("We Are Harbinger");
            Console.WriteLine($"Version: {GitVersion.FullVersion}");



        }
    }
}
