package dev.zontreck.harbinger.data.containers;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.harbinger.data.types.Server;

public class Servers {
    public Map<String, Server> servers = Maps.newHashMap();

    public Entry<List<Entry>> save()
    {
        Entry<List<Entry>> tag = Folder.getNew("servers");
        for (Map.Entry<String, Server> entry : servers.entrySet()) {
            tag.value.add(entry.getValue().save());
        }

        return tag;
    }

    public static Servers deserialize(Entry<List<Entry>> lst)
    {
        try{

            Servers servers = new Servers();
            for(int i=0;i<lst.value.size(); i++)
            {
                Entry<?> eX = lst.value.get(i);
                Server serv = Server.deserialize((Entry<List<Entry>>)eX);
                servers.servers.put(serv.serverNick, serv);
            }

            return servers;
        }catch(Exception e)
        {
            return new Servers();
        }
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
