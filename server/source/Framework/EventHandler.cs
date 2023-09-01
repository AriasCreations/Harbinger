using Harbinger.EventsBus;
using Harbinger.EventsBus.Attributes;
using Harbinger.EventsBus.Events;
using Harbinger.Framework.Database;
using Harbinger.Framework.Registry;
using Harbinger.Framework.Structures;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework
{
    public class EventHandler
    {
        [Subscribe(Priority.Severe)]
        public static void onStart(StartupEvent ev)
        {
            EventBus.PRIMARY.Scan(typeof(SecondLifeBotAccount));
            EventBus.PRIMARY.Scan(typeof(DiscordBotAccount));
        }

        [Subscribe(Priority.Severe)]
        public static void onRegistryReady(RegistryLoadedEvent evt)
        {
            _ = DatabaseConnection.Instance;


            DB.Instance.getConnection().CreateTable<Migration>(); // Create if not exists!

            Migrations.migrations = DB.Instance.getConnection().Table<Migration>().ToList();

            Server.Migrate();
        }
    }

}
