package dev.zontreck.harbinger.data.types;

public enum DiscordEmbedColor {
	WHITE(0xFFFFFF),
	BLACK(0x000000),
	RED(0xFF0000),
	GREEN(0x00FF00),
	BLUE(0x0000FF),
	DARK_RED(0x550000),
	DARK_GREEN(0x005500),
	DARK_BLUE(0x000055),
	YELLOW(0xFFFF00),
	DARK_YELLOW(0x555500),
	PURPLE(0xFF00FF),
	DARK_PURPLE(0x550055),
	ORANGE(0xFF5F00),
	TEAL(0x008080);

	private final int code;

	public int getCode() {
		return code;
	}

	DiscordEmbedColor(int code) {
		this.code = code;
	}

}
