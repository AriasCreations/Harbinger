using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;

namespace Harbinger
{
    public class PluginLoader
    {
        public static HashSet<string> asms = new HashSet<string>();

        public static Dictionary<AssemblyName, Assembly> asmsx = new Dictionary<AssemblyName, Assembly>();

        public static void PreloadReferencedAssemblies()
        {
            LoadAssemblies();
        }

        public static void LoadAssemblies()
        {
            HashSet<AssemblyName> assemblies = new HashSet<AssemblyName>();

            foreach (AssemblyName name in Recurse(Assembly.GetEntryAssembly()).ToArray())
            {
                Console.WriteLine("Assembly Loaded: " + name);

                assemblies.Add(name);
            }
        }

        private static List<AssemblyName> Recurse(Assembly asm)
        {
            List<AssemblyName> asmName = new List<AssemblyName>();


            foreach(AssemblyName name in asm.GetReferencedAssemblies())
            {
                try
                {
                    if(asms.Contains(name.Name))
                    {
                        asmName.AddRange(Recurse(asmsx[name]));
                        continue;
                    }
                    asms.Add(name.Name);
                    Assembly asmx = Assembly.Load(name);
                    asmsx.Add(name, asmx);
                    Console.WriteLine($"Assembly Loaded: {asmx.GetName()}");
                    asmName.AddRange(Recurse(asmx));
                }catch(Exception e)
                {

                }
            }

            return asmName;
        }
    }
}
