package dev.zontreck.harbinger.daemons;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.ServerStoppingEvent;
import dev.zontreck.harbinger.events.discord.DiscordBotTokenUpdatedEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

/**
 * Discord Bot class
 * <p>
 * Loads settings from DiscordSettings
 *
 * @see dev.zontreck.harbinger.data.Persist#discordSettings
 */
public enum DiscordBot {
	;
	private static JDA jda;

	@Subscribe
	public static void onBotSettingsUpdated(final DiscordBotTokenUpdatedEvent event) {
		// We cannot login to discord if the settings are null
		if (Persist.discordSettings.BOT_TOKEN.isEmpty())
			return;
		if (null != jda) {
			DiscordBot.jda.shutdownNow();
		}
		DiscordBot.jda = JDABuilder.createDefault(Persist.discordSettings.BOT_TOKEN).build();
		DiscordBot.jda.setAutoReconnect(true);
	}

	public static void onServerStopping(final ServerStoppingEvent event) {
		DiscordBot.jda.shutdown();
	}
}
