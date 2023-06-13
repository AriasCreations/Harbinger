package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import dev.zontreck.harbinger.utils.TimeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class DiscordEmbed implements IJsonSerializable {
	public DiscordEmbed(final String title, final String description) {
		this.title = title;
		this.description = description;
		timestamp = TimeUtils.makeTimestamp(new Date());
	}

	public String title;
	public String type = "rich";
	public String description;
	public String url;
	public String timestamp; // must be ISO8601 : 2023-05-12T00:56:07.404Z
	public DiscordEmbedColor color;
	public DiscordEmbedFooter footer;
	public DiscordEmbedImage image;
	public DiscordEmbedImage thumbnail;
	public DiscordEmbedImage video;
	public DiscordEmbedProvider provider;
	public DiscordEmbedAuthor author;
	public List<DiscordEmbedField> fields;


	@Override
	public JSONObject serialize() throws DiscordEmbedLimitsException {
		final JSONObject obj = new JSONObject();

		if (!this.title.isEmpty()) {
			if (256 < title.length())
				throw new DiscordEmbedLimitsException("Embed title must not be greater than 256 characters");
			obj.put("title", this.title);
		}

		obj.put("type", this.type);

		if (!this.description.isEmpty()) {
			if (4096 < description.length())
				throw new DiscordEmbedLimitsException("Embed description cannot be greater than 4096 characters");
			obj.put("description", this.description);
		}

		if (!this.url.isEmpty()) obj.put("url", this.url);
		if (!this.timestamp.isEmpty())
			obj.put("timestamp", this.timestamp);

		obj.put("color", this.color.getCode());

		if (null != footer)
			obj.put("footer", this.footer.serialize());
		if (null != image)
			obj.put("image", this.image.serialize());
		if (null != thumbnail)
			obj.put("thumbnail", this.thumbnail.serialize());
		if (null != video)
			obj.put("video", this.video.serialize());
		if (null != provider)
			obj.put("provider", this.provider.serialize());
		if (null != author)
			obj.put("author", this.author.serialize());

		if (null != fields) {
			if (25 < fields.size())
				throw new DiscordEmbedLimitsException("Fields cannot exceed 25 in a single embed");

			final JSONArray arr = new JSONArray();
			for (final DiscordEmbedField field : this.fields) {
				arr.put(field.serialize());
			}
			obj.put("fields", arr);
		}

		return obj;
	}
}
