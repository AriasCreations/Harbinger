using TP.CS.EventsBus;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Events
{
    [Cancellable()]
    /// <summary>
    /// This event is fired to signal when all discord settings have finished loading from the registry.
    /// </summary>
    public class DiscordSettingsLoadedEvent : Event
    {

        /// <summary>
        /// Gives the reason for cancellation. Usually has to do with incorrect settings, or having not yet been configured.
        /// </summary>
        public string reason;
    }
}
