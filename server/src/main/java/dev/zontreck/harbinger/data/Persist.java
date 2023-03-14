package dev.zontreck.harbinger.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.zontreck.ariaslib.events.NBTLoadFailedEvent;
import dev.zontreck.ariaslib.events.NBTLoadedEvent;
import dev.zontreck.ariaslib.events.NBTSavingEvent;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.nbt.CompoundTag;
import dev.zontreck.ariaslib.nbt.NBTIO;
import dev.zontreck.harbinger.data.containers.Products;
import dev.zontreck.harbinger.data.containers.Servers;

public class Persist {
    public static final Logger LOGGER = LoggerFactory.getLogger(Persist.class.getName());
    public static final String FILE_NAME = "memory.dat";

    public static CompoundTag MEMORY;
    public static Products products;
    public static Servers servers;

    static
    {
        try {
            NBTIO.read(new File(FILE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public static void onNBTLoaded(NBTLoadedEvent ev)
    {
        if(ev.fileName.equals(FILE_NAME)){

            MEMORY=ev.loadedTag;

            products = Products.deserialize(MEMORY.getCompound("products"));
            servers = Servers.deserialize(MEMORY.getList("servers"));

            LOGGER.info("NBT File loaded successfully");
        }
    }

    @Subscribe
    public static void onNBTLoadingFailure(NBTLoadFailedEvent ev)
    {
        if(ev.fileName.equals(FILE_NAME))
        {
            MEMORY = new CompoundTag();
            products = new Products();
            servers = new Servers();
        }
    }

    @Subscribe
    public static void onNBTSaving(NBTSavingEvent ev)
    {
        if(ev.file.equals(FILE_NAME))
        {
            ev.tag.clear();
            ev.tag.put("products", products.save());
            ev.tag.put("servers", servers.save());

            
            ev.setCancelled(true);
        }
    }

    public static void save()
    {
        try {
            NBTIO.write(new File(FILE_NAME), MEMORY);
        } catch (IOException e) {
            LOGGER.error("Failed to save the NBT File. Here are the values that are set: "+MEMORY.getAsString(0));
        }
    }
}
