package dev.zontreck.harbinger.data.types;

import dev.zontreck.ariaslib.nbt.CompoundTag;

public class Server {
    public String serverNick;
    public String serverURL;


    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("nick", serverNick);
        tag.putString("url", serverURL);

        return tag;
    }

    public static Server deserialize(CompoundTag tag)
    {
        Server serv = new Server();
        serv.serverNick = tag.getString("nick");
        serv.serverURL = tag.getString("url");

        return serv;
    }
}
