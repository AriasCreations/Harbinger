package dev.zontreck.harbinger.data.types;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import dev.zontreck.harbinger.utils.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class PresharedKey {
	private final String hash;
	private final String salt;

	public PresharedKey ( final String key ) throws NoSuchAlgorithmException {
		this.salt = DigestUtils.md5hex ( String.valueOf ( System.currentTimeMillis ( ) / 1000L ).getBytes ( StandardCharsets.UTF_8 ) );
		this.hash = DigestUtils.md5hex ( ( key + ":" + this.salt ).getBytes ( StandardCharsets.UTF_8 ) );
	}

	public PresharedKey ( final OSD tag ) {
		if(tag instanceof OSDMap map )
		{
			salt = map.get("salt").AsString ();
			hash = map.get("hash").AsString ();
		}else {
			throw new IllegalArgumentException ( "Must be OSDMap" );
		}
	}

	public boolean validate ( final String key ) {
		final String hsh = DigestUtils.md5hex ( ( key + ":" + this.salt ).getBytes ( StandardCharsets.UTF_8 ) );
		return hsh.equals ( this.hash );
	}

	public OSD save ( ) {
		OSDMap map = new OSDMap (  );
		map.put("hash", OSD.FromString ( hash ));
		map.put("salt", OSD.FromString ( salt ));
		return map;
	}
}
