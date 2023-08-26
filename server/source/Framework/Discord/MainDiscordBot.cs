using DSharpPlus;
using DSharpPlus.CommandsNext;
using DSharpPlus.CommandsNext.Attributes;
using DSharpPlus.Entities;
using DSharpPlus.SlashCommands;
using Harbinger.EventsBus;
using Harbinger.EventsBus.Events;
using Harbinger.Framework.Events;
using Harbinger.Framework.Registry;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Discord
{
    public class MainDiscordBot
    {
        [Subscribe(Priority.Uncategorizable,true)]
        public static void onStartup(StartupEvent evt)
        {
            EventBus.PRIMARY.Scan(typeof(MainDiscordBot));
        }

        [Subscribe(Priority.Very_High)]
        public static async void onShutdown(ShutdownEvent evt)
        {
            await client.DisconnectAsync();
            client = null;
        }

        public static DiscordClient client;
        public static DiscordConfiguration config;
        public static SlashCommandsExtension slash;
        public static CommandsNextExtension nxtcmds;
        public static CommandsNextConfiguration nxtConfig;


        
        [Subscribe(Priority.High)]
        public static async void onDiscordSettingsLoaded(DiscordSettingsLoadedEvent evt)
        {
            var token = DiscordBotAccount.instance.codec.Token.Value;

            if (token == "0123")
            {
                evt.setCancelled(true);
                evt.reason = "Token is not set";
                return;
            }

            config = new DiscordConfiguration();
            config.TokenType = TokenType.Bot;
            config.Token = DiscordBotAccount.instance.codec.Token.Value;
            config.AutoReconnect = true;
            config.Intents = DiscordIntents.AllUnprivileged;

            config.MinimumLogLevel = Microsoft.Extensions.Logging.LogLevel.None;

            client = new DiscordClient(config);

            slash = client.UseSlashCommands();
            nxtConfig = new CommandsNextConfiguration();
            nxtConfig.EnableDms = true;
            

            nxtcmds = client.UseCommandsNext(nxtConfig);
            
            
            slash.RegisterCommands<UnprivilegedCommands>();


            await client.ConnectAsync(status: DSharpPlus.Entities.UserStatus.Online, activity: new DSharpPlus.Entities.DiscordActivity(GitVersion.FullVersion));

            client.Ready += Client_Ready;
        }

        private static Task Client_Ready(DiscordClient sender, DSharpPlus.EventArgs.ReadyEventArgs args)
        {
            Console.WriteLine("Connected to Discord");

            client.Ready -= Client_Ready;
            return Task.CompletedTask;
        }
    }

    public class UnprivilegedCommands : ApplicationCommandModule
    {
        [SlashCommand("version", "Makes the bot respond with its version number!")]
        public async Task versionCommand(InteractionContext ctx)
        {
            await ctx.CreateResponseAsync(new DiscordEmbedBuilder().WithAuthor("Harbinger").WithTitle("Version").AddField("Full Version", GitVersion.FullVersion).AddField("Registry Version", $"{RegistryIO.Version}.{RegistryIO.Version2}").WithColor(DiscordColor.Teal).Build());
            
        }

        [Command("version")]
        public async Task versionCommand(CommandContext ctx)
        {
            await ctx.Channel.SendMessageAsync(new DiscordEmbedBuilder().WithAuthor("Harbinger").WithTitle("Version").AddField("Full Version", GitVersion.FullVersion).AddField("Registry Version", $"{RegistryIO.Version}.{RegistryIO.Version2}").WithColor(DiscordColor.Teal).Build());
        }

    }
}
