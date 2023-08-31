using Harbinger.Framework.Registry;
using SharpFileSystem.SharpZipLib;
using System.IO;

namespace Harbinger.Framework.Database
{
    public class DatabaseFile
    {
        public SharpZipLibFileSystem FileSystem;
        public DatabaseFile(string name)
        {
            string path = Path.Combine(RegistryIO.DataFolder, Consts.DatabaseFolder);
            if(!Directory.Exists(path))
            {
                Directory.CreateDirectory(path);
            }
            path = Path.Combine(path, name);
            path = Path.ChangeExtension(path, "zip");

            if(File.Exists(path))
            {
                FileSystem = SharpZipLibFileSystem.Open(new FileStream(Path.Combine(path, name + ".zip"), FileMode.OpenOrCreate));
            }else
            {
                FileSystem = SharpZipLibFileSystem.Create(new FileStream(Path.Combine(path, name + ".zip"), FileMode.OpenOrCreate));
            }
        }
    }

    public class RegistryDatabase
    {
        public RegistryFileSystem FileSystem;

        public RegistryDatabase(string name)
        {

        }
    }
}
