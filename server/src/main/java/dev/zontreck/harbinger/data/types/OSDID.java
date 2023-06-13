package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDArray;

import java.util.UUID;

public class OSDID
{
	public static OSD saveUUID(UUID ID)
	{
		OSDArray arr = new OSDArray (  );
		arr.add ( OSD.FromLong(ID.getMostSignificantBits ()) );
		arr.add(OSD.FromLong ( ID.getLeastSignificantBits () ));
		return arr;
	}

	public static UUID loadUUID(OSD osd)
	{
		if(osd instanceof OSDArray arr)
		{
			long msb = arr.get ( 0 ).AsLong ();
			long lsb = arr.get(1).AsLong ();

			return new UUID ( msb, lsb );
		}else return new UUID ( 0,0 );
	}
}
