package me.eldodebug.jsmp.parser.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import me.eldodebug.jsmp.parser.JsmpParser;
import me.eldodebug.jsmp.parser.JsmpResult;

public class Mp3Id3v1Parser extends JsmpParser {

	public Mp3Id3v1Parser(File file) {
		super(file);
	}

	@Override
	public JsmpResult onParse() {

		if (!file.exists() || file.length() < 128) {
			return null;
		}

		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

			raf.seek(file.length() - 128);
			byte[] buffer = new byte[128];
			int bytesRead = raf.read(buffer);

			if (bytesRead < 128) {
				return null;
			}

			if (!(buffer[0] == 'T' && buffer[1] == 'A' && buffer[2] == 'G')) {
				return null;
			}

			String title = extractString(buffer, 3, 30);
			String artist = extractString(buffer, 33, 30);

			return new JsmpResult(title, artist, null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private String extractString(byte[] buffer, int offset, int length) {
		byte[] slice = Arrays.copyOfRange(buffer, offset, offset + length);
		String raw = new String(slice, StandardCharsets.UTF_8).trim();
		return raw.isEmpty() ? null : raw;
	}
}
