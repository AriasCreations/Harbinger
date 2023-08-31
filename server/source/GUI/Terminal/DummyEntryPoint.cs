using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.GUI
{
    public class DummyEntryPoint
    {
        public static void Main(string[] args)
        {
            Console.WriteLine("This program is intended to be run as a part of the Harbinger server. Please close this, and run Harbinger instead.");

            Console.WriteLine("Press any key to exit");
            Console.ReadKey();
        }
    }
}
