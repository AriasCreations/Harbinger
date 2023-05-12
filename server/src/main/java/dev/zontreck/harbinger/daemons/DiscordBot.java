package dev.zontreck.harbinger.daemons;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.ServerStoppingEvent;
import dev.zontreck.harbinger.events.discord.DiscordBotSettingsUpdatedEvent;
import dev.zontreck.harbinger.events.discord.DiscordBotTokenUpdatedEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

/**
 * Discord Bot class
 *
 * Loads settings from DiscordSettings
 * @see dev.zontreck.harbinger.data.Persist#discordSettings
 */
public class DiscordBot
{
	private static JDA jda;
	@Subscribe
	public static void onBotSettingsUpdated(DiscordBotTokenUpdatedEvent event)
	{
		// We cannot login to discord if the settings are null
		if(Persist.discordSettings==null)return;
		if(jda!=null)
		{
			jda.shutdownNow();
		}
		jda = JDABuilder.createDefault(Persist.discordSettings.BOT_TOKEN).build();
		jda.setAutoReconnect(true);
	}

	public static void onServerStopping(ServerStoppingEvent event)
	{
		jda.shutdown();
	}
}
