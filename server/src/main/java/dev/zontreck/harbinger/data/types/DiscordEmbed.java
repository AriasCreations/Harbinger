package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import dev.zontreck.harbinger.utils.TimeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class DiscordEmbed implements IJsonSerializable {
	public DiscordEmbed(String title, String description) {
		this.title = title;
		this.description = description;
		this.timestamp = TimeUtils.makeTimestamp(new Date());
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
		JSONObject obj = new JSONObject();

		if (!title.isEmpty()) {
			if (title.length() > 256)
				throw new DiscordEmbedLimitsException("Embed title must not be greater than 256 characters");
			obj.put("title", title);
		}

		obj.put("type", type);

		if (!description.isEmpty()) {
			if (description.length() > 4096)
				throw new DiscordEmbedLimitsException("Embed description cannot be greater than 4096 characters");
			obj.put("description", description);
		}

		if (!url.isEmpty()) obj.put("url", url);
		if (!timestamp.isEmpty())
			obj.put("timestamp", timestamp);

		obj.put("color", color.getCode());

		if (footer != null)
			obj.put("footer", footer.serialize());
		if (image != null)
			obj.put("image", image.serialize());
		if (thumbnail != null)
			obj.put("thumbnail", thumbnail.serialize());
		if (video != null)
			obj.put("video", video.serialize());
		if (provider != null)
			obj.put("provider", provider.serialize());
		if (author != null)
			obj.put("author", author.serialize());

		if (fields != null) {
			if (fields.size() > 25)
				throw new DiscordEmbedLimitsException("Fields cannot exceed 25 in a single embed");

			JSONArray arr = new JSONArray();
			for (DiscordEmbedField field : fields) {
				arr.put(field.serialize());
			}
			obj.put("fields", arr);
		}

		return obj;
	}
}
