package dev.zontreck.harbinger.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.NBTLoadFailedEvent;
import dev.zontreck.ariaslib.events.NBTLoadedEvent;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.nbt.ByteTag;
import dev.zontreck.ariaslib.nbt.CompoundTag;
import dev.zontreck.ariaslib.nbt.DoubleTag;
import dev.zontreck.ariaslib.nbt.FloatTag;
import dev.zontreck.ariaslib.nbt.IntTag;
import dev.zontreck.ariaslib.nbt.ListTag;
import dev.zontreck.ariaslib.nbt.LongTag;
import dev.zontreck.ariaslib.nbt.NBTIO;
import dev.zontreck.ariaslib.nbt.ShortTag;
import dev.zontreck.ariaslib.nbt.StringTag;
import dev.zontreck.ariaslib.nbt.Tag;

public class NBTTestCommands
{
    public static final String NBT = "nbt";

    @Subscribe
    public static void onNBTTest(CommandEvent ev)
    {
        if(ev.command.equals(NBT))
        {
            if(ev.arguments.get(0).equals("test"))
            {
                CompoundTag tag = new CompoundTag();
                tag.putString("name", "Bananrama");
                tag.setName("hello world");

                try {
                    NBTIO.write(new File("helloworld.nbt"), tag);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if(ev.arguments.get(0).equals("read"))
            {
                try {
                    CommandRegistry.LOGGER.info(NBTIO.read(new File(ev.arguments.get(1))).getAsString(0));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if(ev.arguments.get(0).equals("types"))
            {
                saveATestType(StringTag.valueOf("testing!"), "string.nbt");
                saveATestType(ByteTag.valueOf((byte)0), "byte.nbt");
                saveATestType(DoubleTag.valueOf(94.2), "double.nbt");
                saveATestType(FloatTag.valueOf(92.93f), "float.nbt");
                saveATestType(IntTag.valueOf(123), "int.nbt");
                saveATestType(LongTag.valueOf(987L), "long.nbt");
                saveATestType(ShortTag.valueOf((short)12), "short.nbt");
                ListTag testList = new ListTag();
                testList.add(StringTag.valueOf("testing"));
                testList.add(StringTag.valueOf("testing123"));
                saveATestType(testList, "list.nbt");
            }
        }
    }

    private static void saveATestType(Tag testTag, String file)
    {
        CompoundTag test = new CompoundTag();
        test.setName("Test Type");
        test.put("test", testTag);

        try {
            NBTIO.write(new File(file), test);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public static void onNBTTestFail(NBTLoadFailedEvent ev)
    {
        if(ev.fileName.equals("helloworld.nbt"))
        {
            CommandRegistry.LOGGER.info("Failed to load HelloWorld.nbt");
        }else {
            CommandRegistry.LOGGER.info("Failed to load NBT File: "+ev.fileName);
        }
    }


    @Subscribe
    public static void onNBTTestLoad(NBTLoadedEvent ev)
    {
        if(ev.fileName.equals("helloworld.nbt")){
            CommandRegistry.LOGGER.info("Hello world file loaded: \n"+ev.loadedTag.getAsString(0));
        }
    }
    
}
