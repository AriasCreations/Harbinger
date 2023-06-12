package dev.zontreck.harbinger.data.types;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;

import java.util.List;

public class Server {
	public String serverNick;
	public String serverURL;


	public Entry<List<Entry>> save() {
		Entry<List<Entry>> tag = Folder.getNew(serverNick);
		tag.value.add(EntryUtils.mkStr("nick", serverNick));
		tag.value.add(EntryUtils.mkStr("url", serverURL));

		return tag;
	}

	public static Server deserialize(Entry<List<Entry>> tag) {
		Server serv = new Server();
		serv.serverNick = EntryUtils.getStr(Folder.getEntry(tag, "nick"));
		serv.serverURL = EntryUtils.getStr(Folder.getEntry(tag, "url"));

		return serv;
	}
}
