package dev.zontreck.harbinger.data.containers.types;

import java.util.UUID;

public class DiscordWebhook
{
    protected final UUID HookID;
    public String WebHookName;
    public String WebHookURL;

    public DiscordWebhook(UUID ID, String name, String url)
    {
        HookID = ID;
        WebHookName = name;
        WebHookURL = url;
    }

    public DiscordWebhook(String name, String url)
    {
        HookID = UUID.randomUUID();
        WebHookName = name;
        WebHookURL = url;
    }

    public UUID getID()
    {
        return new UUID(HookID.getMostSignificantBits(), HookID.getLeastSignificantBits());
    }
}
