package dev.zontreck.harbinger.simulator.types;

public class osUTF8Consts {

	public static final byte[] XMLundef = osUTF8.GetASCIIBytes ( "<undef/>" );
	public static final byte[] XMLfullbooleanOne = osUTF8.GetASCIIBytes ( "<boolean>1</boolean>" );
	public static final byte[] XMLfullbooleanZero = osUTF8.GetASCIIBytes ( "<boolean>0</boolean>" );
	public static final byte[] XMLintegerStart = osUTF8.GetASCIIBytes ( "<integer>" );
	public static final byte[] XMLintegerEmpty = osUTF8.GetASCIIBytes ( "<integer />" );
	public static final byte[] XMLintegerEnd = osUTF8.GetASCIIBytes ( "</integer>" );
	public static final byte[] XMLrealStart = osUTF8.GetASCIIBytes ( "<real>" );
	public static final byte[] XMLrealZero = osUTF8.GetASCIIBytes ( "<real>0</real>" );
	public static final byte[] XMLrealZeroarrayEnd = osUTF8.GetASCIIBytes ( "<real>0</real></array>" );
	public static final byte[] XMLrealEnd = osUTF8.GetASCIIBytes ( "</real>" );
	public static final byte[] XMLrealEndarrayEnd = osUTF8.GetASCIIBytes ( "</real></array>" );
	public static final byte[] XMLstringStart = osUTF8.GetASCIIBytes ( "<string>" );
	public static final byte[] XMLstringEmpty = osUTF8.GetASCIIBytes ( "<string />" );
	public static final byte[] XMLstringEnd = osUTF8.GetASCIIBytes ( "</string>" );
	public static final byte[] XMLuuidStart = osUTF8.GetASCIIBytes ( "<uuid>" );
	public static final byte[] XMLuuidEmpty = osUTF8.GetASCIIBytes ( "<uuid />" );
	public static final byte[] XMLuuidEnd = osUTF8.GetASCIIBytes ( "</uuid>" );
	public static final byte[] XMLdateStart = osUTF8.GetASCIIBytes ( "<date>" );
	public static final byte[] XMLdateEmpty = osUTF8.GetASCIIBytes ( "<date />" );
	public static final byte[] XMLdateEnd = osUTF8.GetASCIIBytes ( "</date>" );
	public static final byte[] XMLuriStart = osUTF8.GetASCIIBytes ( "<uri>" );
	public static final byte[] XMLuriEmpty = osUTF8.GetASCIIBytes ( "<uri />" );
	public static final byte[] XMLuriEnd = osUTF8.GetASCIIBytes ( "</uri>" );
	public static final byte[] XMLformalBinaryStart = osUTF8.GetASCIIBytes ( "<binary encoding=\"base64\">" );
	public static final byte[] XMLbinaryStart = osUTF8.GetASCIIBytes ( "<binary>" );
	public static final byte[] XMLbinaryEmpty = osUTF8.GetASCIIBytes ( "<binary />" );
	public static final byte[] XMLbinaryEnd = osUTF8.GetASCIIBytes ( "</binary>" );
	public static final byte[] XMLmapStart = osUTF8.GetASCIIBytes ( "<map>" );
	public static final byte[] XMLmapEmpty = osUTF8.GetASCIIBytes ( "<map />" );
	public static final byte[] XMLmapEnd = osUTF8.GetASCIIBytes ( "</map>" );
	public static final byte[] XMLkeyStart = osUTF8.GetASCIIBytes ( "<key>" );
	public static final byte[] XMLkeyEmpty = osUTF8.GetASCIIBytes ( "<key />" );
	public static final byte[] XMLkeyEnd = osUTF8.GetASCIIBytes ( "</key>" );
	public static final byte[] XMLkeyEndundef = osUTF8.GetASCIIBytes ( "</key><undef />" );
	public static final byte[] XMLkeyEndmapStart = osUTF8.GetASCIIBytes ( "</key><map>" );
	public static final byte[] XMLkeyEndmapEmpty = osUTF8.GetASCIIBytes ( "</key><map />" );
	public static final byte[] XMLkeyEndarrayStart = osUTF8.GetASCIIBytes ( "</key><array>" );
	public static final byte[] XMLkeyEndarrayEmpty = osUTF8.GetASCIIBytes ( "</key><array />" );
	public static final byte[] XMLkeyEndarrayStartmapStart = osUTF8.GetASCIIBytes ( "</key><array><map>" );
	public static final byte[] XMLarrayStart = osUTF8.GetASCIIBytes ( "<array>" );
	public static final byte[] XMLarrayStartrealZero = osUTF8.GetASCIIBytes ( "<array><real>0</real>" );
	public static final byte[] XMLarrayStartrealStart = osUTF8.GetASCIIBytes ( "<array><real>" );
	public static final byte[] XMLkeyEndarrayStartrealZero = osUTF8.GetASCIIBytes ( "</key><array><real>0</real>" );
	public static final byte[] XMLkeyEndarrayStartrealStart = osUTF8.GetASCIIBytes ( "</key><array><real>" );
	public static final byte[] XMLarrayEmpty = osUTF8.GetASCIIBytes ( "<array />" );
	public static final byte[] XMLarrayEnd = osUTF8.GetASCIIBytes ( "</array>" );
	public static final byte[] XMLamp_lt = osUTF8.GetASCIIBytes ( "&lt;" );
	public static final byte[] XMLamp_gt = osUTF8.GetASCIIBytes ( "&gt;" );
	public static final byte[] XMLamp = osUTF8.GetASCIIBytes ( "&amp;" );
	public static final byte[] XMLamp_quot = osUTF8.GetASCIIBytes ( "&quot;" );
	public static final byte[] XMLamp_apos = osUTF8.GetASCIIBytes ( "&apos;" );
	public static final byte[] XMLformalHeader = osUTF8.GetASCIIBytes ( "<?xml version=\"1.0\" encoding=\"utf-8\"?>" );

	public static final byte[] XMLformalHeaderllsdstart =
			osUTF8.GetASCIIBytes ( "<?xml version=\"1.0\" encoding=\"utf-8\"?><llsd>" );

	public static final byte[] XMLllsdStart = osUTF8.GetASCIIBytes ( "<llsd>" );
	public static final byte[] XMLllsdEnd = osUTF8.GetASCIIBytes ( "</llsd>" );
	public static final byte[] XMLllsdEmpty = osUTF8.GetASCIIBytes ( "<llsd><map /></llsd>" );
	public static final byte[] XMLmapEndarrayEnd = osUTF8.GetASCIIBytes ( "</map></array>" );

	public static final byte[] XMLarrayEndmapEnd = osUTF8.GetASCIIBytes ( "</array></map>" );

	public static final byte[] XMLelement_name_Empty = osUTF8.GetASCIIBytes ( "<key>name</key><string />" );
	public static final byte[] XMLelement_name_Start = osUTF8.GetASCIIBytes ( "<key>name</key><string>" );

	public static final byte[] XMLelement_agent_id_Empty = osUTF8.GetASCIIBytes ( "<key>agent_id</key><uuid />" );
	public static final byte[] XMLelement_agent_id_Start = osUTF8.GetASCIIBytes ( "<key>agent_id</key><uuid>" );

	public static final byte[] XMLelement_owner_id_Empty = osUTF8.GetASCIIBytes ( "<key>owner_id</key><uuid />" );
	public static final byte[] XMLelement_owner_id_Start = osUTF8.GetASCIIBytes ( "<key>owner_id</key><uuid>" );

	public static final byte[] XMLelement_creator_id_Empty = osUTF8.GetASCIIBytes ( "<key>creator_id</key><uuid />" );
	public static final byte[] XMLelement_creator_id_Start = osUTF8.GetASCIIBytes ( "<key>creator_id</key><uuid>" );

	public static final byte[] XMLelement_group_id_Empty = osUTF8.GetASCIIBytes ( "<key>group_id</key><uuid />" );
	public static final byte[] XMLelement_group_id_Start = osUTF8.GetASCIIBytes ( "<key>cgroup_id</key><uuid>" );

	public static final byte[] XMLelement_parent_id_Empty = osUTF8.GetASCIIBytes ( "<key>parent_id</key><uuid />" );
	public static final byte[] XMLelement_parent_id_Start = osUTF8.GetASCIIBytes ( "<key>parent_id</key><uuid>" );

	public static final byte[] XMLelement_folder_id_Empty = osUTF8.GetASCIIBytes ( "<key>folder_id</key><uuid />" );
	public static final byte[] XMLelement_folder_id_Start = osUTF8.GetASCIIBytes ( "<key>folder_id</key><uuid>" );

	public static final byte[] XMLelement_asset_id_Empty = osUTF8.GetASCIIBytes ( "<key>asset_id</key><uuid />" );
	public static final byte[] XMLelement_asset_id_Start = osUTF8.GetASCIIBytes ( "<key>asset_id</key><uuid>" );

	public static final byte[] XMLelement_item_id_Empty = osUTF8.GetASCIIBytes ( "<key>item_id</key><uuid />" );
	public static final byte[] XMLelement_item_id_Start = osUTF8.GetASCIIBytes ( "<key>item_id</key><uuid>" );

	public static final byte[] XMLelement_category_id_Empty = osUTF8.GetASCIIBytes ( "<key>category_id</key><uuid />" );
	public static final byte[] XMLelement_category_id_Start = osUTF8.GetASCIIBytes ( "<key>category_id</key><uuid>" );

	public static final byte[] XMLelement_version_Empty = osUTF8.GetASCIIBytes ( "<key>version</key><integer />" );
	public static final byte[] XMLelement_version_Start = osUTF8.GetASCIIBytes ( "<key>version</key><integer>" );

	public static final byte[] XMLelement_sale_info_Empty =
			osUTF8.GetASCIIBytes (
					"<key>sale_info</key><map><key>sale_price</key><integer /><key>sale_type</key><integer /></map>" );

	public static final byte[] XMLelement_sale_info_Start =
			osUTF8.GetASCIIBytes ( "<key>sale_info</key><map><key>sale_price</key><integer>" );

	public static final byte[] XMLelement_sale_info_Mid =
			osUTF8.GetASCIIBytes ( "</integer><key>sale_type</key><integer>" );

	public static final byte[] XMLelement_sale_info_End = osUTF8.GetASCIIBytes ( "</integer></map>" );

	public static final byte[] OSUTF8null = { ( byte ) 'n' , ( byte ) 'u' , ( byte ) 'l' , ( byte ) 'l' };
	public static final byte[] OSUTF8true = { ( byte ) 't' , ( byte ) 'r' , ( byte ) 'u' , ( byte ) 'e' };
	public static final byte[] OSUTF8false = { ( byte ) 'f' , ( byte ) 'a' , ( byte ) 'l' , ( byte ) 's' , ( byte ) 'e' };

	public static final byte[] base64Bytes =
			{
					( byte ) 'A' , ( byte ) 'B' , ( byte ) 'C' , ( byte ) 'D' , ( byte ) 'E' , ( byte ) 'F' , ( byte ) 'G' , ( byte ) 'H' , ( byte ) 'I' , ( byte ) 'J' ,
					( byte ) 'K' , ( byte ) 'L' , ( byte ) 'M' , ( byte ) 'N' , ( byte ) 'O' ,
					( byte ) 'P' , ( byte ) 'Q' , ( byte ) 'R' , ( byte ) 'S' , ( byte ) 'T' , ( byte ) 'U' , ( byte ) 'V' , ( byte ) 'W' , ( byte ) 'X' , ( byte ) 'Y' ,
					( byte ) 'Z' , ( byte ) 'a' , ( byte ) 'b' , ( byte ) 'c' , ( byte ) 'd' ,
					( byte ) 'e' , ( byte ) 'f' , ( byte ) 'g' , ( byte ) 'h' , ( byte ) 'i' , ( byte ) 'j' , ( byte ) 'k' , ( byte ) 'l' , ( byte ) 'm' , ( byte ) 'n' ,
					( byte ) 'o' , ( byte ) 'p' , ( byte ) 'q' , ( byte ) 'r' , ( byte ) 's' ,
					( byte ) 't' , ( byte ) 'u' , ( byte ) 'v' , ( byte ) 'w' , ( byte ) 'x' , ( byte ) 'y' , ( byte ) 'z' , ( byte ) '0' , ( byte ) '1' , ( byte ) '2' ,
					( byte ) '3' , ( byte ) '4' , ( byte ) '5' , ( byte ) '6' , ( byte ) '7' ,
					( byte ) '8' , ( byte ) '9' , ( byte ) '+' , ( byte ) '/'
			};
}
