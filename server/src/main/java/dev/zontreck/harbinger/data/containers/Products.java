package dev.zontreck.harbinger.data.containers;

import dev.zontreck.harbinger.data.types.Product;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDArray;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Products {
	public static final String TAG = "products";
	public List<Product> products = new ArrayList<> ( );


	public Products ( ) {

	}

	public Products ( OSD tag ) {
		if ( tag instanceof OSDMap map ) {
			Product.SEQUENCE = new AtomicLong ( map.get ( "seq" ).AsLong ( ) );
			OSDArray entries = ( OSDArray ) map.get ( "entries" );
			products = new ArrayList<> ( );
			Iterator<OSD> it = entries.iterator ( );
			while ( it.hasNext ( ) ) {
				OSDMap mapx = ( OSDMap ) it.next ( );
				products.add ( new Product ( mapx ) );
			}
		}
	}

	public OSD write ( ) {
		OSDMap prods = new OSDMap ( );
		prods.put ( "seq" , OSD.FromLong ( Product.SEQUENCE.get ( ) ) );
		OSDArray arrr = new OSDArray ( );
		for (
				Product p :
				products
		) {
			arrr.add ( p.save ( ) );
		}
		prods.put ( "entries" , arrr );
		return prods;

	}

	public boolean hasProduct ( final UUID prodID ) {
		return 0 < products.stream ( ).filter ( v -> v.productID.equals ( prodID ) ).collect ( Collectors.toList ( ) ).size ( );
	}
}
