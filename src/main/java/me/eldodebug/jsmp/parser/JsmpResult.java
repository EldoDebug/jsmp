package me.eldodebug.jsmp.parser;

import java.awt.image.BufferedImage;

public class JsmpResult {
	
	private final String title, artist;
	private final BufferedImage albumImage;
	
	public JsmpResult(String title, String artist, BufferedImage albumImage) {
		this.title = title;
		this.artist = artist;
		this.albumImage = albumImage;
	}

	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}

	public BufferedImage getAlbumImage() {
		return albumImage;
	}
}
