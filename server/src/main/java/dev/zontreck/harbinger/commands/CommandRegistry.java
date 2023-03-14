package dev.zontreck.harbinger.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.zontreck.ariaslib.events.EventBus;

public class CommandRegistry {
    public static final Logger LOGGER = LoggerFactory.getLogger(CommandRegistry.class.getName());
    public static void register(EventBus bus)
    {
        bus.register(HelpCommand.class);
        bus.register(StopCommand.class);
    }
}
