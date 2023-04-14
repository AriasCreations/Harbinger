package dev.zontreck.harbinger.data.types;


import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.data.types.Product;
import dev.zontreck.harbinger.data.types.Server;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;

public class URLCallback
{
	public JSONObject forwarded;
	public Server serverForRequest;

	public URLCallback(String request)
	{
		forwarded = new JSONObject(request);

		String product = forwarded.getString("product");
		Product p = Persist.products.products.stream().filter(v->v.productName.equals(product)).collect(Collectors.toList()).get(0);
		serverForRequest = p.containingServer;
		forwarded.put("product", p.productItem);
	}

	/**
	 * Actioning will submit the request
	 */
	public void actionIt() throws URISyntaxException, IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(new URI(serverForRequest.serverURL)).POST(HttpRequest.BodyPublishers.ofString(forwarded.toString())).build();
		HttpClient client = HttpClient.newBuilder().build();
		client.send(request, HttpResponse.BodyHandlers.discarding());
	}
}
