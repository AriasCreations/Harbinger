package dev.zontreck.harbinger.data.containers;

import java.util.Map;

import com.google.common.collect.Maps;

import dev.zontreck.ariaslib.nbt.CompoundTag;
import dev.zontreck.ariaslib.nbt.ListTag;
import dev.zontreck.ariaslib.nbt.Tag;
import dev.zontreck.harbinger.data.types.Server;

public class Servers {
    public Map<String, Server> servers = Maps.newHashMap();

    public Tag save()
    {
        ListTag tags = new ListTag();
        for (Map.Entry<String, Server> entry : servers.entrySet()) {
            tags.add(entry.getValue().save());
        }

        return tags;
    }

    public static Servers deserialize(ListTag lst)
    {
        Servers servers = new Servers();
        for(int i=0;i<lst.size(); i++)
        {
            CompoundTag entry = (CompoundTag)lst.get(i);
            Server serv = Server.deserialize(entry);
            servers.servers.put(serv.serverNick, serv);
        }

        return servers;
    }

    public void add(Server server)
    {
        servers.put(server.serverNick, server);
    }

    public void remove(String nick)
    {
        servers.remove(nick);
    }
}
