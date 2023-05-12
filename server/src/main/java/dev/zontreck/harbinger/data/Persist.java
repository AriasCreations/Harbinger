package dev.zontreck.harbinger.data;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

import dev.zontreck.ariaslib.file.AriaIO;
import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.harbinger.data.containers.*;
import dev.zontreck.harbinger.data.types.Signature;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.zontreck.ariaslib.events.annotations.Subscribe;

public class Persist {
    public static final Logger LOGGER = LoggerFactory.getLogger(Persist.class.getSimpleName());
    public static final Path FILE_NAME = AriaIO.resolveDataFile("main");

    public static Entry<List<Entry>> MEMORY = Folder.getNew("root");
    public static Products products = new Products();
    public static Servers servers = new Servers();
    public static HTTPServerSettings serverSettings = new HTTPServerSettings();

    public static Signature SIGNATURE = new Signature();
    public static DiscordSettings discordSettings=new DiscordSettings();


    public static String HARBINGER_VERSION;

    static
    {
        try{

            MEMORY = (Entry<List<Entry>>)AriaIO.read(FILE_NAME);

            products = new Products(Folder.getEntry(MEMORY, "products"));

            servers = Servers.deserialize(Folder.getEntry(MEMORY, "servers"));
            SupportReps.load(Folder.getEntry(MEMORY, "support"));

            serverSettings = new HTTPServerSettings(Folder.getEntry(MEMORY, HTTPServerSettings.TAG_NAME));

            SIGNATURE = new Signature(Folder.getEntry(MEMORY, "sig"));

            discordSettings = new DiscordSettings(Folder.getEntry(MEMORY, "discord"));

            LOGGER.info("Memory file loaded");
        }catch(Exception e)
        {
            //e.printStackTrace();
        }

    }

    @Subscribe
    public static void onMemoryAltered(MemoryAlteredEvent ev)
    {
        save();
    }

    private static void save()
    {
        MEMORY = Folder.getNew("root");
        MEMORY.value.add(products.write());
        MEMORY.value.add(servers.save());
        MEMORY.value.add(SupportReps.save());
        MEMORY.value.add(serverSettings.save());
        MEMORY.value.add(SIGNATURE.save());
        MEMORY.value.add(discordSettings.save());


        LOGGER.info("Memory file saved");

        AriaIO.write(FILE_NAME, MEMORY);
    }
}
