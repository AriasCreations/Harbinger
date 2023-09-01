using Harbinger.FontHelper;
using Harbinger.Framework.Registry;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.RegEdit
{
    internal class EntryPoint
    {
        public static string FILE = "";
        public static string NODE = "";
        public static Key HIVE;

        public static string[] debug_args = new [] { "-file", "harbinger.hsrd", "-delete", "root/HKS/laststats/Ticks", "-dump", "-delete", "root/HKS/bots/secondlife/account/First", "-dump", "-set", "root/HKS/Tests", "word", "Test1", "This is a test", "-dump" };

        public static void printUsage()
        {

            Console.WriteLine("-dump\t\tDumps the current in-memory registry to the console");
            Console.WriteLine("-file [Name]\t\tLoads the filename as a registry hive. Do not provide the extension.");
            Console.WriteLine("-new [Name]\t\tInitializes a new hive in memory. Will be saved to registry/name.hsrd");
            Console.WriteLine("-delete [RegPath]\t\tDeletes a registry path, and any empty parent keys recursively");
            Console.WriteLine("-set [RegPath] [Type] [Name] [value]\t\tSets and creates parent keys.\n\t\t\t\tTypes: Word, Int16, Int32, Int64, Bool");
            Console.WriteLine("-flush\t\tFlushes the in-memory registry to disk and commits the changes");
        }
        static void Main(string[] args)
        {
            Fonts.init();
            EventsBus.EventBus.debug = false;
            Console.ForegroundColor = ConsoleColor.DarkRed;
            Console.WriteLine(Fonts.RenderUsing("banner3-D", "Harbinger"));
            Console.ForegroundColor = ConsoleColor.DarkGreen;


            Console.WriteLine("We Are Harbinger");

            Console.WriteLine("Harbinger Registry Editor - Command Line Interface");
            Console.WriteLine($"Version: {GitVersion.FullVersion}");
            Console.WriteLine($"Registry Format Version: {RegistryIO.Version}.{RegistryIO.Version2}");
            Console.WriteLine();
            Console.WriteLine();
            Console.WriteLine();
            Console.WriteLine();


            if (args.Length == 0)
            {
                printUsage();
                return;
            }
            for (int i=0;i<args.Length; i+=2)
            {
                string option = args[i];

                switch (option)
                {
                    case "-file":
                        {
                            FILE = args[i+1];

                            HIVE = RegistryIO.loadHive(FILE);
                            break;
                        }
                    case "-delete":
                        {
                            NODE = args[i+1];
                            Entry toDelete = HIVE.getAtPath(NODE);
                            while(true)
                            {
                                if (toDelete != null)
                                {
                                    Console.WriteLine($"Deleting: {toDelete.EntryPath}...");
                                    toDelete = HIVE.deleteByPath(toDelete.EntryPath);

                                    if (toDelete is Key key)
                                    {
                                        if (key.Count != 0) toDelete = null;
                                        
                                    }
                                }
                                else break;
                            }
                            
                            break;
                        }
                    case "-dump":
                        {
                            Console.ForegroundColor = ConsoleColor.DarkRed;

                            Console.WriteLine($"Byte Count: {RegistryIO.getBytes(HIVE).Length}");

                            i--; // No value taken for this arg
                            Console.WriteLine(HIVE.PrettyPrint());

                            Console.ForegroundColor = ConsoleColor.DarkGreen;

                            break;
                        }
                    case "-set":
                        {
                            var name = args[i + 3];
                            object value = null;

                            var path = args[i + 1];
                            var type = args[i + 2];
                            bool has_value = false;
                            Entry x = null;
                            // Set the value type 
                            switch(type.ToLower())
                            {
                                case "word":
                                    {
                                        x = new Word(name, "");
                                        has_value = true;

                                        // Set value object!
                                        value = args[i + 4];
                                        break;
                                    }
                                case "int16":
                                    {
                                        x = new VInt16(name, 0);
                                        has_value = true;
                                        value = short.Parse(args[i + 4]);

                                        break;
                                    }
                                case "int32":
                                    {
                                        x = new VInt32(name, 0);
                                        has_value = true;
                                        value = int.Parse(args[i + 4]);

                                        break;
                                    }
                                case "int64":
                                    {
                                        x = new VInt64(name, 0);
                                        has_value = true;
                                        value = long.Parse(args[i + 4]);
                                        break;
                                    }
                                case "bool":
                                    {
                                        x = new VBool(name, false);
                                        has_value = true;
                                        var tmp = args[i + 4];
                                        value = (tmp == "1" || tmp.ToLower() == "true") ? true : false;
                                        break;
                                    }
                                case "byte":
                                    {
                                        x = new VByte(name, 0);
                                        has_value = true;
                                        value = byte.Parse(args[i + 4]);
                                        break;
                                    }
                                default:
                                    {
                                        Console.WriteLine($"ERROR: Node type ({type}) is unsupported, or maybe there was a typo. Path ({path}) not created.");
                                        break;
                                    }

                            }
                            i += 2;
                            if (x == null) break;
                            if (has_value)
                            {
                                x.setValue(value);
                                i++;
                            }

                            if (HIVE.getAtPath(path + "/" + name) != null)
                            {
                                Entry e = HIVE.getAtPath(path + "/" + name);
                                e.setValue(value);
                            }else
                                HIVE.placeAtPath(path, x);

                            break;
                        }
                    case "-new":
                        {
                            HIVE = new Key("root", null)
                            {
                                Type = EntryType.Root
                            };
                            FILE = args[i + 1];

                            Console.WriteLine("New Hive generated in memory");
                            break;
                        }
                    case "-flush":
                        {
                            Console.WriteLine("Registry is being saved...");
                            RegistryIO.saveHive(HIVE, FILE);

                            Console.WriteLine("Registry flushed to disk.");
                            break;
                        }
                    default:
                        {
                            printUsage();
                            break;
                        }
                }

            }


            Console.ForegroundColor = ConsoleColor.White;
        }
    }
}
