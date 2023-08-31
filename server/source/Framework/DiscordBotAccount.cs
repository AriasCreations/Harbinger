using Harbinger.EventsBus;
using Harbinger.EventsBus.Events;
using Harbinger.Framework.Events;
using Harbinger.Framework.Registry;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework
{
    public class DiscordBotAccount
    {
        public const string KEY = "root/HKS/bots/discord/account";

        public static DiscordBotAccount instance = new DiscordBotAccount();

        public Key MY_KEY;

        public DiscordBotCodec codec;

        [Subscribe(Priority.Very_High)]
        public static void onRegistryLoaded(RegistryLoadedEvent ev)
        {
            instance.MY_KEY = Entry.getByPath(KEY)?.Key();
            if(instance.MY_KEY == null)
            {
                instance.MY_KEY = new Key("account", null);
                Entry.ROOT.placeAtPath(KEY.Substring(0, KEY.LastIndexOf('/')), instance.MY_KEY);

                instance.MY_KEY = Entry.getByPath(KEY)?.Key();
                instance.codec = new DiscordBotCodec(instance.MY_KEY);
            }else
            {
                instance.codec = new DiscordBotCodec(instance.MY_KEY);
            }


            EventBus.PRIMARY.post(new DiscordSettingsLoadedEvent());
        }
    }


    public class DiscordBotCodec
    {
        public const int VERSION = 1;

        public Word Token { get; set; }

        public VInt32 CurVer { get; set; }


        private Key key;
        public DiscordBotCodec(Key key)
        {
            this.key = key;
            if (!key.HasNamedKey("version"))
            {
                // Initialize at latest version
                Initialize();
            }
            else
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
            Token = new Word("token", "0123");
            CurVer = new VInt32("version", 1);

            key.Add(Token);
            key.Add(CurVer);
        }

        public void Load(int value)
        {
            switch (value)
            {
                case 1:
                    {
                        Token = key.getNamed("token").Word();
                        CurVer = key.getNamed("version").Int32();


                        break;
                    }
            }
        }
    }
}
