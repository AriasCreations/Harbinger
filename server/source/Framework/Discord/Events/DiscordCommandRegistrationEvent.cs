using DSharpPlus.CommandsNext;
using DSharpPlus.SlashCommands;
using Harbinger.EventsBus;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Discord.Events
{
    /// <summary>
    /// 
    /// Dispatched on the broadcast bus.
    /// 
    /// Indicates it is time to register commands.
    /// </summary>
    public class DiscordCommandRegistrationEvent : Event
    {
        public CommandsNextExtension commands;

        public DiscordCommandRegistrationEvent(CommandsNextExtension commands)
        {
            this.commands = commands;
        }


        public bool send()
        {
            return EventBus.Broadcast(this);
        }
    }

    /// <summary>
    /// 
    /// Dispatched on the broadcast bus.
    /// 
    /// Indicates it is time to register commands.
    /// </summary>
    public class DiscordSlashCommandRegistrationEvent : Event
    {
        public SlashCommandsExtension commands;

        public DiscordSlashCommandRegistrationEvent(SlashCommandsExtension commands)
        {
            this.commands = commands;
        }


        public bool send()
        {
            return EventBus.Broadcast(this);
        }
    }
}
