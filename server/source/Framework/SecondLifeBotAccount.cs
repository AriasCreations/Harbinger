using Harbinger.EventsBus;
using Harbinger.EventsBus.Events;
using Harbinger.Framework.Registry;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.ExceptionServices;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework
{
    public class SecondLifeBotAccount
    {
        public const string KEY = "root/HKS/bots/secondlife/account";

        public string First { get; set; } = "FName";
        public string Last { get; set; } = "LName";

        public string Password { get; set; } = "0123";


        public bool isLoggedIn = false;

        private Key MyEntry;

        [Subscribe(Priority.Low)]
        public static void onStartup(RegistryLoadedEvent ev)
        {
            Entry x = Entry.getByPath(KEY);
            if(x != null && x is Key key)
            {
                instance.First = key.getNamed("First").Word().Value;
                instance.Last = key.getNamed("Last").Word().Value;
                instance.Password = key.getNamed("Pass").Word().Value;
            }else
            {
                // Ensure Registry Path exists
                Key account = new Key("account", null);
                Entry.ROOT.placeAtPath(KEY.Substring(0, KEY.LastIndexOf("/")), account);
            }

            x = Entry.getByPath(KEY);
            instance.MyEntry = x.Key();

        }


        [Subscribe(Priority.Severe)]
        public static void onShutdown(ShutdownEvent ev)
        {
            if (!instance.MyEntry.HasNamedKey("First")) instance.MyEntry.Add(new Word("First", instance.MyEntry));
            if (!instance.MyEntry.HasNamedKey("Last")) instance.MyEntry.Add(new Word("Last", instance.MyEntry));
            if (!instance.MyEntry.HasNamedKey("Pass")) instance.MyEntry.Add(new Word("Pass", instance.MyEntry));

            instance.MyEntry.getNamed("First").Word().Value = instance.First;
            instance.MyEntry.getNamed("Last").Word().Value = instance.Last;
            instance.MyEntry.getNamed("Pass").Word().Value = instance.Password;
        }

        public static SecondLifeBotAccount instance = new SecondLifeBotAccount();
    }
}
