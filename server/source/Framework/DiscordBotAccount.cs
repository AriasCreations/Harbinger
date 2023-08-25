using Harbinger.EventsBus;
using Harbinger.EventsBus.Events;
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

        public string Token { get; set; } = "";
        public Key MY_KEY;

        [Subscribe(Priority.Very_High)]
        public static void onRegistryLoaded(RegistryLoadedEvent ev)
        {
            instance.MY_KEY = Entry.getByPath(KEY)?.Key();
            if(instance.MY_KEY == null)
            {
                instance.MY_KEY = new Key("account", null);
                Entry.ROOT.placeAtPath(KEY.Substring(0, KEY.LastIndexOf('/')), instance.MY_KEY);

                instance.MY_KEY = Entry.getByPath(KEY)?.Key();
            }
        }

        [Subscribe(Priority.Medium)]
        public static void onShutdown(ShutdownEvent ev)
        {
            if (instance.MY_KEY.HasNamedKey("token")) instance.MY_KEY.Add(new Word("token", null));
            instance.MY_KEY.getNamed("token").Word().Value = instance.Token;
        }
    }
}
