using Harbinger.EventsBus;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.Design.Serialization;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Registry
{
    public abstract class Entry
    {
        /// <summary>
        /// The type of the entry
        /// </summary>
        public EntryType Type { get; set; } = EntryType.Root;

        /// <summary>
        /// Entry Name
        /// </summary>
        public string Name { get; set; } = "(noname)";

        /// <summary>
        /// Entry Common Name
        /// 
        /// This may be used for friendlier display of a Entry Name
        /// </summary>
        public string Description { get; set; } = "";

        /// <summary>
        /// Entry Path
        /// 
        /// Should only show names, not descriptions
        /// </summary>
        public string EntryPath
        {
            get
            {
                Stack<Entry> stack = new Stack<Entry>();
                Entry? current = this;
                while (current != null)
                {
                    stack.Push(current);
                    current = current.Parent;
                }

                string path = "";
                while(stack.Count > 0)
                {
                    string nxt = stack.Pop()?.Name ?? "";

                    path = path + "/" + nxt;
                    if (path.StartsWith("/")) path = path.Substring(1);
                }

                return path;
            }
        }


        internal void setRoot(Key root)
        {
            MyRoot = root;

            if (this is Key k)
            {
                foreach (Entry entry in k)
                {
                    entry.setRoot(root);
                }
            }
        }

        [JsonIgnore()]
        private Entry? _parent;
        /// <summary>
        /// Parent Entry Key
        /// </summary>
        [JsonIgnore()]
        public Entry? Parent
        {
            get
            {
                return _parent;
            }
            set
            {
                var oldParent = _parent;
                var oldPath = EntryPath;
                _parent = value;
                if(value!=null)
                {
                    if(EventBus.PRIMARY.post(new RegistryEntryAddedEvent(this, EntryPath)))
                    {
                        _parent = oldParent;
                        if(_parent is Key key)
                        {
                            key.Remove(this);
                        }
                    }
                } else
                {
                    if(oldParent!=null)
                    {
                        // It may be possible that this gets fired if the old parent is null, so only dispatch in this case

                        if(EventBus.PRIMARY.post(new RegistryEntryRemovedEvent(this, oldPath, oldParent as Key)))
                        {
                            _parent = oldParent;
                            if(_parent is Key key)
                            {
                                key.Add(this);
                            }
                        }
                    }
                }
            }
        }

        /// <summary>
        /// Static root Key
        /// </summary>
        [JsonIgnore()]
        public static readonly Key ROOT = new Key("root")
        {
            Type = EntryType.Root
        };

        internal Entry()
        {
            Type = EntryType.Root;
            Name = "Root";
            Description = "Registry Root";
            Parent = null;
        }

        /// <summary>
        /// Public Contstructor
        /// 
        /// For consistency, a type should never be root.
        /// 
        /// This constructor is internal and you should only use the subtype constructors.
        /// </summary>
        /// <param name="type">Entry Type</param>
        /// <param name="name">Entry Name</param>
        internal Entry(EntryType type, string name)
        {
            Type = type;
            Name = name;
        }

        public static Entry getByPath(string path)
        {
            try
            {

                // Remove the root/ text
                path = path.Substring(path.IndexOf('/') + 1);

                Entry retentry = null;
                Entry entry = ROOT;
                while (retentry == null)
                {
                    int slash = path.IndexOf("/");
                    string nextEntry = "";
                    if (slash != -1)
                        nextEntry = path.Substring(0, slash);
                    else
                    {
                        nextEntry = path;
                        path = "";
                    }



                    if (entry is Key key && nextEntry != "")
                    {
                        path = path.Substring(slash + 1);

                        entry = key.getNamed(nextEntry);
                        
                    }
                    else retentry = entry;
                }
                return entry;
            }
            catch (Exception e)
            {
                return null;
            }

        }
        public Entry getAtPath(string path)
        {
            try
            {

                // Remove the root/ text
                path = path.Substring(path.IndexOf('/') + 1);

                Entry retentry = null;
                Entry entry = MyRoot; // Get root
                while (retentry == null)
                {
                    int slash = path.IndexOf("/");
                    string nextEntry = "";
                    if (slash != -1)
                        nextEntry = path.Substring(0, slash);
                    else
                    {
                        nextEntry = path;
                        path = "";
                    }



                    if (entry is Key key && nextEntry != "")
                    {
                        path = path.Substring(slash + 1);
                        entry = key.getNamed(nextEntry);
                    }
                    else retentry = entry;
                }
                return entry;
            }
            catch (Exception e)
            {
                // The entry wasn't found, generate a new key, place at path, and return
                return null;
            }

        }

        public virtual void Write(BinaryWriter stream)
        {
            stream.Write((byte)Type);
            stream.Write(Name);
            stream.Write(Description);

        }

        public static Entry? Read(BinaryReader stream)
        {
            Entry x = null;
            var Type = (EntryType)stream.ReadByte();
            var Name = stream.ReadString();
            var Description = stream.ReadString();

            switch(Type)
            {
                case EntryType.Key:
                    {
                        x = new Key(Name);
                        break;
                    }
                case EntryType.Word:
                    {
                        x = new Word(Name, null);
                        break;
                    }
                case EntryType.Int16:
                    {
                        x = new VInt16(Name, null);
                        break;
                    }
                case EntryType.Int32:
                    {
                        x = new VInt32(Name, null);
                        break;
                    }
                case EntryType.Int64:
                    {
                        x = new VInt64(Name, null);
                        break;
                    }
                case EntryType.Bool:
                    {
                        x = new VBool(Name, null);
                        break;
                    }
                case EntryType.Root:
                    {
                        x = new Key("root", null);
                        break;
                    }
            }
            x.Type = Type;
            x.Description = Description;
            x.Parent = null;

            x.readValue(stream);
            

            return x;
        }

        public abstract void readValue(BinaryReader stream);

        private static Key getRoot(Entry entry)
        {
            var ent = entry;
            while(ent.Parent != null)
            {
                ent = ent.Parent;
            }

            return ent as Key;
        }

        public Word Word()
        {
            return (Word)this;
        }
        public VInt16 Int16()
        {
            return (VInt16)this;
        }
        public VInt32 Int32()
        {
            return (VInt32)this;
        }
        public VInt64 Int64()
        {
            return (VInt64)this;
        }
        public VBool Bool()
        {
            return (VBool)this;
        }
        public Key Key()
        {
            return (Key)this;
        }

        /// <summary>
        /// The root key for the current tree
        /// </summary>
        [JsonIgnore()]
        public Key MyRoot;

        public virtual string PrettyPrint(int indent=0)
        {
            return $"{"".PadLeft(indent, ' ')}[{Type}] {Name}";
        }

        public override string ToString()
        {
            return PrettyPrint();
        }

        /// <summary>
        /// Deletes the registry entry at a specified path.
        /// </summary>
        /// <param name="path">Absolute path to delete</param>
        /// <returns>The parent key</returns>
        public Entry deleteByPath(string path)
        {
            if (this is Key key)
            {

                Entry pth = getAtPath(path);
                Key parent = pth.Parent.Key();
                parent.Remove(pth);

                return parent;
            }
            else return null;
        }

        public virtual void setValue(object value)
        {

        }
    }
}
