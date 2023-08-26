using Harbinger.EventsBus;
using Harbinger.EventsBus.Events;
using Harbinger.Framework.Registry;
using OpenJpegDotNet;
using OpenMetaverse.ImportExport.Collada14;
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



        public bool isLoggedIn = false;

        private Key MyEntry;
        public SecondLifeAccountCodec codec;

        [Subscribe(Priority.Low)]
        public static void onStartup(RegistryLoadedEvent ev)
        {
            Entry x = Entry.getByPath(KEY);
            if(x == null)
            {
                // Ensure Registry Path exists
                var account = new Key("account", null);

                Entry.ROOT.placeAtPath(KEY.Substring(0, KEY.LastIndexOf("/")), account);
            }

            x = Entry.getByPath(KEY);
            instance.MyEntry = x.Key();
            instance.codec = new SecondLifeAccountCodec(instance.MyEntry);

        }

        public static SecondLifeBotAccount instance = new SecondLifeBotAccount();
    }

    public class SecondLifeAccountCodec
    {
        public const int VERSION = 1;

        public Word First { get; set; }
        public Word Last { get; set; }
        public Word Password { get; set; }

        public VInt32 CurVer { get; set; }


        private Key key;
        public SecondLifeAccountCodec(Key key)
        {

            this.key = key;
            if (!key.HasNamedKey("version"))
            {
                // Initialize at latest version
                Initialize();
            } else
            {
                VInt32 ver = key.getNamed("version").Int32();

                Load(ver.Value);
            }
        }

        public void Initialize()
        {
            ActivateV1();
        }

        public void ActivateV1()
        {
            First = new Word("first", null).setWord("FName");
            Last = new Word("last", null).setWord("LName");
            Password = new Word("pass", null).setWord("Password01");
            CurVer = new VInt32("version", null).setInt32(1);

            key.Add(First);
            key.Add(Last);
            key.Add(Password);
            key.Add(CurVer);
        }

        public void Load(int value)
        {
            switch (value)
            {
                case 1:
                    {
                        First = key.getNamed("first").Word();
                        Last = key.getNamed("last").Word();
                        Password = key.getNamed("pass").Word();
                        CurVer = key.getNamed("version").Int32();


                        break;
                    }
            }
        }
    }
}
