using Harbinger.EventsBus;
using Harbinger.EventsBus.Events;
using Harbinger.Framework.HTTP.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.HTTPHandlers.Commands
{
    public class VersionHandler
    {
        public static void init() {
        }

        [Subscribe(Priority.Very_High)]
        public static void onStart(StartupEvent ev)
        {

            EventBus.PRIMARY.Scan(typeof(VersionHandler));
        }


        [Subscribe(Priority.High)]
        public static void onHTTPRequest(HTTPRequestEvent evt)
        {
            if(evt.requestPath.ToLower().StartsWith("/version"))
            {
                evt.responseBody = GitVersion.FullVersion;

                evt.response.StatusCode = 200;
                evt.response.ContentType = "text/plain";

                evt.setCancelled(true);
            }
        }
    }
}
