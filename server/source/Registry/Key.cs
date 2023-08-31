using Harbinger.EventsBus;
using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;

namespace Harbinger.Framework.Registry
{
    public class Key : Entry, IList<Entry>
    {
        public Key(string name, Entry parent) : base(EntryType.Key, name)
        {
            Parent = parent;
        }

        internal Key(string name) : base(EntryType.Key, name)
        {
            Parent = null;
        }

        private Key() : base(EntryType.Root, "root")
        {
        }

        #region List Implementation
        private List<Entry> _entries = new List<Entry>();

        public Entry this[int index]
        {
            get
            {
                return _entries[index];
            }
            set
            {
                value.Parent = this;
                _entries[index] = value;
            }
        }

        public int Count => _entries.Count;

        public bool IsReadOnly => false;

        public void Add(Entry item)
        {
            if (EventBus.PRIMARY.post(new RegistryEntryAddedEvent(item, EntryPath + "/" + item.Name)))
            {
                return;
            } else
            {
                _entries.Add(item);
                item.Parent = this;

                updateRoots();
            }
        }

        public void updateRoots()
        {
            if (Type == EntryType.Root) setRoot(this);
        }


        public void Clear()
        {
            foreach (Entry item in _entries)
            {
                item.Parent = null;
                item.MyRoot = null;
            }
            _entries.Clear();
        }

        public bool Contains(Entry item)
        {
            return _entries.Contains(item);
        }

        public void CopyTo(Entry[] array, int arrayIndex)
        {
            _entries.CopyTo(array, arrayIndex);

            updateRoots();
        }

        public IEnumerator<Entry> GetEnumerator()
        {
            return _entries.GetEnumerator();
        }

        public int IndexOf(Entry item)
        {
            return _entries.IndexOf(item);
        }

        public void Insert(int index, Entry item)
        {

            if (EventBus.PRIMARY.post(new RegistryEntryAddedEvent(item, EntryPath + "/" + item.Name)))
            {
                return;
            }
            else
            {
                _entries.Insert(index, item);
                item.Parent = this;

                updateRoots();
            }
        }

        public bool Remove(Entry item)
        {
            if(EventBus.PRIMARY.post(new RegistryEntryRemovedEvent(item, item.EntryPath, this)))
            {
                return false;
            }else
            {

                item.Parent = null;
                item.MyRoot = null;
                var ret = _entries.Remove(item);
                updateRoots();
                return ret;
            }
        }

        public void RemoveAt(int index)
        {
            Entry item = _entries[index];
            if (EventBus.PRIMARY.post(new RegistryEntryRemovedEvent(item, item.EntryPath, this)))
            {
                return;
            }
            else
            {

                item.Parent = null;
                item.MyRoot = null;

                updateRoots();
                return;
            }
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return _entries.GetEnumerator();
        }

        #endregion

        /// <summary>
        /// Finds and returns a named subentry
        /// </summary>
        /// <param name="name">The name of the entry to find</param>
        /// <returns>The entry</returns>
        /// <exception cref="Exception">Thrown if the entry is not found</exception>
        public Entry getNamed(string name)
        {
            var e= _entries.Where(x => x.Name == name).ToList();
            if (e.Count == 0) throw new Exception("Entry not found");

            return e[0];
        }

        public override void Write(BinaryWriter stream)
        {
            base.Write(stream);
            stream.Write(_entries.Count);
            foreach (var entry in _entries)
            {
                entry.Write(stream);
            }
        }

        public override void readValue(BinaryReader stream)
        {
            int count = stream.ReadInt32();
            for(var i = 0; i < count; i++)
            {
                var x = Read(stream);
                x.Parent = this;
                Add(x);
            }
        }


        internal void replaceEntries(Entry replaceWith)
        {
            if(replaceWith is Key key)
            {
                _entries.AddRange(key);
                updateRoots();
            }
        }

        public void placeAtPath(string path, Entry toPlace)
        {
            if(Type != EntryType.Root)
            {
                toPlace.MyRoot = MyRoot;
                MyRoot.placeAtPath(path, toPlace);
                return;
            } 
            string pth = path.Substring(path.IndexOf('/') + 1); // Cut root/ since we can assume we are executing within root

            // Iterate over the entire path, and place a empty key until we reach the target path
            Key e = this;
            while(e.EntryPath != path)
            {
                int inx = pth.IndexOf("/");
                string nextEntryName = pth;

                if (inx != -1)
                {

                    nextEntryName = pth.Substring(0, pth.IndexOf("/"));
                    pth = pth.Substring(pth.IndexOf("/") + 1);
                }
                else pth = "";

                if(!e.HasNamedKey(nextEntryName))
                {
                    Key nxt = new Key(nextEntryName);
                    e.Add(nxt);
                    e = nxt;
                }else
                {
                    e = e.getNamed(nextEntryName).Key();
                }
            }

            e.Add(toPlace);
        }

        public bool HasNamedKey(string name)
        {
            return _entries.Any(x => x.Name == name);
        }

        public override string PrettyPrint(int indent = 0)
        {
            var line = base.PrettyPrint(indent);

            line += $" [{_entries.Count}]";
            line += "\n";

            foreach(Entry x in _entries)
            {
                line += x.PrettyPrint(indent+4)+"\n";
            }
            return line;
        }
    }
}
