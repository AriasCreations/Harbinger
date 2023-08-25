using Harbinger.Framework.Registry;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.RegRead
{
    public class Program
    {
        public static void Main(string[] args)
        {
            string file = args[0];
            string key = args[1];
            EventsBus.EventBus.debug = false;

            Key hive = RegistryIO.loadHive(file);

            Entry en = hive.getAtPath(key);
            Console.WriteLine(JsonConvert.SerializeObject(en, Formatting.Indented));
        }
    }
}
