package dev.zontreck.harbinger.data.containers.types;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.UUID;

public class DiscordWebhook
{
    @BsonIgnore
    protected final UUID HookID;

    @BsonProperty("id0")
    public final long MSB;

    @BsonProperty("id1")
    public final long LSB;

    @BsonProperty("name")
    public String WebHookName;

    @BsonProperty("url")
    public String WebHookURL;

    public DiscordWebhook(UUID ID, String name, String url)
    {
        HookID = ID;
        WebHookName = name;
        WebHookURL = url;

        MSB = HookID.getMostSignificantBits();
        LSB = HookID.getLeastSignificantBits();
    }

    public DiscordWebhook(String name, String url)
    {
        HookID = UUID.randomUUID();
        WebHookName = name;
        WebHookURL = url;


        MSB = HookID.getMostSignificantBits();
        LSB = HookID.getLeastSignificantBits();
    }

    public DiscordWebhook(){
        HookID = UUID.randomUUID();

        MSB = HookID.getMostSignificantBits();
        LSB = HookID.getLeastSignificantBits();
    }

    public UUID getID()
    {
        return new UUID(MSB, LSB);
    }
}
