package dev.zontreck.harbinger.data.types;


import dev.zontreck.harbinger.data.Persist;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;

public class URLCallback {
	public JSONObject forwarded;
	public Server serverForRequest;

	public URLCallback(final String request) {
		this.forwarded = new JSONObject(request);

		final String product = this.forwarded.getString("product");
		final Product p = Persist.products.products.stream().filter(v -> v.productName.equals(product)).collect(Collectors.toList()).get(0);
		this.serverForRequest = p.containingServer;
		this.forwarded.put("product", p.productItem);
	}

	/**
	 * Actioning will submit the request
	 */
	public void actionIt() throws URISyntaxException, IOException, InterruptedException {
		final HttpRequest request = HttpRequest.newBuilder(new URI(this.serverForRequest.serverURL)).POST(HttpRequest.BodyPublishers.ofString(this.forwarded.toString())).build();
		final HttpClient client = HttpClient.newBuilder().build();
		client.send(request, HttpResponse.BodyHandlers.discarding());
	}
}
