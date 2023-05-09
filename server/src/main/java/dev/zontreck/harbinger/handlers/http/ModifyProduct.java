package dev.zontreck.harbinger.handlers.http;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.data.types.Product;
import dev.zontreck.harbinger.events.APIRequestEvent;
import org.json.JSONObject;

import java.util.UUID;

public class ModifyProduct
{
	@Subscribe
	public static void onAPIRequest(APIRequestEvent event)
	{
		if(event.request_object.getString("type").equals("modify_product"))
		{
			switch(event.request_object.getString("action"))
			{
				case "update":
				{
					if(Persist.products.hasProduct(UUID.fromString(event.request_object.getString("product"))))
					{
					}
					break;
				}
				case "make":
				{
					long prod_num = Product.SEQUENCE.getAndIncrement();
					UUID ID = Product.makeProductID(event.request_object.getLong("group"), prod_num);
					JSONObject resp = new JSONObject();
					resp.put("product", prod_num);

					break;
				}
			}
		}
	}
}
