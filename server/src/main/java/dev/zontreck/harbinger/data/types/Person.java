package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

import java.util.UUID;

public class Person {
	public UUID ID;
	public String Name;
	public PermissionLevel Permissions;

	public Person ( final UUID id , final String user , final PermissionLevel lvl ) {
		this.ID = id;
		this.Name = user;
		this.Permissions = lvl;
	}

	public Person ( OSD dat ) {
		if ( dat instanceof OSDMap map ) {
			ID = OSDID.loadUUID ( map.get ( "id" ) );
			Name = map.get ( "name" ).AsString ( );
			Permissions = PermissionLevel.of ( map.get ( "perms" ).AsInteger ( ) );
		}
	}

	public String print ( final int indent ) {
		String ind = "";
		for ( int i = 0 ; i < indent ; i++ ) {
			ind += "\t";
		}
		String s = "";
		s += ind + "{\n" + ind + "\tID: ";
		s += this.ID.toString ( ) + "\n" + ind + "\tName: ";
		s += this.Name + "\n" + ind + "\tPermissions: ";
		s += this.Permissions.toString ( ) + "\n";
		s += ind + "}";

		return s;
	}

	public OSD save ( ) {
		OSDMap map = new OSDMap ( );
		map.put ( "id" , OSDID.saveUUID ( ID ) );
		map.put ( "name" , OSD.FromString ( Name ) );
		map.put ( "perms" , OSD.FromInteger ( Permissions.getFlag ( ) ) );
		return map;
	}
}
