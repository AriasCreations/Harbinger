using System;
using Harbinger.EventsBus;
using Harbinger.EventsBus.Events;

namespace Harbinger.FontHelper
{
    public class FontHelperExe
    {
        public static void Main(string[] args)
        {

            EventBus current_bus = EventBus.PRIMARY;

            current_bus.Scan(typeof(Fonts));
            current_bus.post(new StartupEvent());


            Console.WriteLine("Font Helper execution complete, press any key to exit");
            Console.ReadKey();
        }
    }
}
