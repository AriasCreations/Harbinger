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


        public static void PreloadReferencedAssemblies()
        {
            Assembly entryAssembly = Assembly.GetEntryAssembly();
            asms.Clear();

            if (entryAssembly == null)
            {
                Console.WriteLine("Cannot determine entry assembly.");
                return;
            }

            foreach (AssemblyName assemblyName in entryAssembly.GetReferencedAssemblies())
            {
                try
                {
                    Assembly.Load(assemblyName);
                    Console.WriteLine("Loaded assembly: " + assemblyName.Name);

                    asms.Add(assemblyName.FullName);
                }
                catch (Exception e)
                {
                    Console.WriteLine("Error loading assembly: " + e.Message);
                }
            }
        }
    }
}
