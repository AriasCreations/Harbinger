using Harbinger.Framework.Registry;
using SharpFileSystem;
using System;
using System.Collections.Generic;
using System.ComponentModel.Design.Serialization;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Database
{
    public class RegistryFileSystem : IFileSystem
    {
        public Key root { get; set; }

        public RegistryFileSystem(Key K) 
        {
            root = K;
        }

        public static RegistryFileSystem Create()
        {
            return new RegistryFileSystem(new Key("root"));
        }

        public static RegistryFileSystem Open(Stream hsrdStream)
        {
            return new RegistryFileSystem(RegistryIO.loadHive(hsrdStream));
        }

        public void CreateDirectory(FileSystemPath path)
        {
            Key n = new Key(path.EntityName, null);
            root.placeAtPath(path.ParentPath.Path, n);
        }

        public Stream CreateFile(FileSystemPath path)
        {
            throw new NotImplementedException();
        }

        public void Delete(FileSystemPath path)
        {
            throw new NotImplementedException();
        }

        public void Dispose()
        {
            throw new NotImplementedException();
        }

        public bool Exists(FileSystemPath path)
        {
            throw new NotImplementedException();
        }

        public ICollection<FileSystemPath> GetEntities(FileSystemPath path)
        {
            throw new NotImplementedException();
        }

        public Stream OpenFile(FileSystemPath path, FileAccess access)
        {
            throw new NotImplementedException();
        }
    }
}
