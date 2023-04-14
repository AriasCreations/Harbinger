package dev.zontreck.harbinger.data.containers;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;

import java.util.List;

public class HTTPServerSettings {
    public static final String TAG_NAME = "http_server";

    public boolean enabled=false;
    public int port = 7768;

    public String PSK;

    public HTTPServerSettings(){}

    public HTTPServerSettings(Entry<List<Entry>> tag)
    {
        enabled = EntryUtils.getBool(Folder.getEntry(tag, "enable"));
        port = EntryUtils.getInt(Folder.getEntry(tag, "port"));
        PSK = EntryUtils.getStr(Folder.getEntry(tag, "psk"));
    }


    public Entry<?> save()
    {
        Entry<List<Entry>> tag = Folder.getNew(TAG_NAME);
        tag.value.add(EntryUtils.mkBool("enable", enabled));
        tag.value.add(EntryUtils.mkInt("port", port));
        tag.value.add(EntryUtils.mkStr("psk", PSK));
        return tag;
    }
}
