using Harbinger.EventsBus;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Registry
{
    /// <summary>
    /// Signals that the main Registry Hive has been loaded successfully.
    /// </summary>
    public class RegistryLoadedEvent : Event
    {
        public Key root;
        internal RegistryLoadedEvent(Key root)
        {
            this.root = root;
        }
    }

    /// <summary>
    /// Signals that the main Registry was saved successfully.
    /// </summary>
    public class RegistrySavedEvent : Event
    {
        public Key root;
        public string filename;
        internal RegistrySavedEvent(Key root, string filename)
        {
            this.root = root;
            this.filename = filename;
        }
    }

    /// <summary>
    /// This event is cancellable, and indicates a entry was added to a key
    /// 
    /// If cancelled, the entry gets removed from the key immediately.
    /// 
    /// The remove event may be dispatched.
    /// </summary>
    [Cancellable()]
    public class RegistryEntryAddedEvent : Event
    {
        public Entry newEntry;
        public string entryPath;

        public RegistryEntryAddedEvent(Entry newEntry, string entryPath)
        {
            this.newEntry = newEntry;
            this.entryPath = entryPath;
        }
    }

    /// <summary>
    /// This event is cancellable and indicates a entry was removed from a key
    /// 
    /// If cancelled, the entry won't get removed.
    /// 
    /// The add event may be dispatched.
    /// </summary>
    [Cancellable()]
    public class RegistryEntryRemovedEvent : Event
    {
        public Entry newEntry;
        public string oldEntryPath;
        public Key oldParent;

        public RegistryEntryRemovedEvent(Entry newEntry, string entryPath, Key oldParent)
        {
            this.newEntry = newEntry;
            this.oldEntryPath = entryPath;
            this.oldParent = oldParent;
        }
    }
}
