using Harbinger.EventsBus;
using Harbinger.EventsBus.Events;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Registry
{
    public class RegistryIO
    {
        public const byte Version = 1;
        public const byte Version2 = 0;
        /// <summary>
        /// Saves the entire Registry to disk
        /// 
        /// This uses the Entry.ROOT object. To use another registry, see the saveHive function.
        /// </summary>
        public static void save()
        {
            string filename = Path.Combine(DataFolder, RootHSRD);
            filename = Path.ChangeExtension(filename, HSRDExtension);

            Console.WriteLine($"Saving Registry : \n\n{Entry.ROOT.PrettyPrint()}");

            // Reset the file to zero bytes if it exists
            ensureFolder();
            resetFile(filename);
            using(FileStream fs = new FileStream(filename, FileMode.OpenOrCreate))
            {
                using(BinaryWriter bw = new BinaryWriter(fs))
                {
                    writeHeader(bw);

                    Entry.ROOT.Write(bw);
                }
            }

            Console.WriteLine("Registry Saved");
        }

        /// <summary>
        /// Saves the specified Root Hive to disk
        /// 
        /// This requires a file name!
        /// </summary>
        /// <param name="root"></param>
        public static void saveHive(Key root, string name)
        {
            string filename = Path.ChangeExtension(name, HSRDExtension);
            filename = Path.Combine(DataFolder, filename);

            // Reset the file to zero bytes if it exists
            ensureFolder();
            resetFile(filename);

            using (FileStream fs = new FileStream(filename, FileMode.OpenOrCreate))
            {
                using(BinaryWriter bw = new BinaryWriter(fs))
                {
                    writeHeader(bw);


                    root.Write(bw);
                }
            }
        }

        /// <summary>
        /// Write the header to the file
        /// </summary>
        /// <param name="bw"></param>
        private static void writeHeader(BinaryWriter bw)
        {
            // Write out header!
            bw.Write(Version);
            bw.Write(Version2); // 2
            bw.Write("Tara Piccari"); // 1 + 12
            bw.Write(new byte[16]); // 16

            // 16 bytes of padding for minor version changes. Potential bitmasks may be added.
            // Minor version upgrades should make padding changes when necessary during an upgrade to maintain compatibility when a major update arrives.
        }

        /// <summary>
        /// Loads the Root Hive into memory
        /// </summary>
        public static void load()
        {

            string filename = Path.Combine(DataFolder, RootHSRD);
            filename = Path.ChangeExtension(filename, HSRDExtension);

            ensureFolder();
            if(File.Exists(filename))
            {

                using (FileStream fs = new FileStream(filename, FileMode.Open))
                {
                    using (BinaryReader br = new BinaryReader(fs))
                    {
                        readHeader(br);

                        Entry.ROOT.replaceEntries(Entry.Read(br));
                    }
                }
            }

            Entry.ROOT.Type = EntryType.Root;

            Console.WriteLine("Registry Loaded.");

            EventBus.Broadcast(new RegistryLoadedEvent(Entry.ROOT));
        }

        private static void readHeader(BinaryReader br)
        {

            if (br.ReadByte() != Version)
            {
                throw new OutdatedRegistryException("Primary version mismatch, format is incompatible");
            }
            if (br.ReadByte() != Version2)
            {
                // We should be okay, but print a warning to the console. The format will be migrated during saving if there are any differences

                Console.WriteLine("WARNING: Registry minor version is different, there may be some missing header data. If your registry file fails to load report it to the developer");
                Console.WriteLine("The registry will be migrated to the new minor version upon being flushed to disk");
            }
            _ = br.ReadString(); // 13 bytes. Creator's signature.


            br.ReadBytes(16);
        }

        /// <summary>
        /// Loads the specified custom Hive into memory
        /// </summary>
        public static Key loadHive(string name)
        {

            string filename = Path.Combine(DataFolder, RootHSRD);
            filename = Path.ChangeExtension(filename, HSRDExtension);

            ensureFolder();
            Key x = new Key("root");
            if(File.Exists(filename))
            {

                using (FileStream fs = new FileStream(filename, FileMode.Open))
                {
                    using (BinaryReader br = new BinaryReader(fs))
                    {
                        readHeader(br);

                        x.replaceEntries(Entry.Read(br));
                    }
                }
            }

            x.Type = EntryType.Root;

            // We may not yet be done scanning all events and registering, so use the broadcast system for this.
            EventBus.Broadcast(new RegistryLoadedEvent(x));

            return x;
        }

        private static void ensureFolder()
        {
            if(!Directory.Exists(DataFolder))
            {
                Directory.CreateDirectory(DataFolder);
            }
        }

        private static void resetFile(string filename)
        {
            File.WriteAllBytes(filename, new byte[0] { });
        }

        // HSRD
        // Harbinger Serialized Registry Data

        public const string RootHSRD = "harbinger";
        public const string DataFolder = "registry";

        public const string HSRDExtension = "hsrd";


        [Subscribe(Priority.Severe)]
        public static void onStartup(StartupEvent ev)
        {
            Console.WriteLine("Loading Registry...");
            load();
        }



        [Subscribe(Priority.Low)]
        public static void onShutdown(ShutdownEvent ev)
        {
            Console.WriteLine("Flushing Registry...");
            save();
        }

    }
}
