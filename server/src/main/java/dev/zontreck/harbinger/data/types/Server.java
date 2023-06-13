package dev.zontreck.harbinger.data.types;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;

import java.util.List;

public class Server {
	public String serverNick;
	public String serverURL;

	public static Server deserialize ( final Entry<List<Entry>> tag ) {
		final Server serv = new Server ( );
		serv.serverNick = EntryUtils.getStr ( Folder.getEntry ( tag , "nick" ) );
		serv.serverURL = EntryUtils.getStr ( Folder.getEntry ( tag , "url" ) );

		return serv;
	}

	public Entry<List<Entry>> save ( ) {
		final Entry<List<Entry>> tag = Folder.getNew ( this.serverNick );
		tag.value.add ( EntryUtils.mkStr ( "nick" , this.serverNick ) );
		tag.value.add ( EntryUtils.mkStr ( "url" , this.serverURL ) );

		return tag;
	}
}
