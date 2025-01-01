package me.eldodebug.jsmp.parser.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import me.eldodebug.jsmp.parser.JsmpParser;
import me.eldodebug.jsmp.parser.JsmpResult;

public class FlacParser extends JsmpParser {

	public FlacParser(File file) {
		super(file);
	}

	@Override
	public JsmpResult onParse() {
		String title = "";
		String artist = "";
		BufferedImage albumImage = null;

		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

			byte[] marker = new byte[4];
			raf.read(marker);

			boolean isLast = false;

			while (!isLast) {

				int blockHeader = raf.read();
				isLast = (blockHeader & 0x80) != 0;
				int blockType = blockHeader & 0x7F;

				int length = (raf.read() & 0xFF) << 16 | (raf.read() & 0xFF) << 8 | (raf.read() & 0xFF);

				long nextBlockPosition = raf.getFilePointer() + length;

				if (blockType == 4) {
					VorbisCommentData data = readVorbisComment(raf, length);
					title = data.title;
					artist = data.artist;
				} else if (blockType == 6) {
					albumImage = readPicture(raf);
				}

				raf.seek(nextBlockPosition);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new JsmpResult(title, artist, albumImage);
	}

	private static class VorbisCommentData {
		String title = "";
		String artist = "";
	}

	private VorbisCommentData readVorbisComment(RandomAccessFile raf, int totalLength) throws IOException {

		VorbisCommentData data = new VorbisCommentData();

		int vendorLength = readInt32LE(raf);

		raf.skipBytes(vendorLength);

		int userCommentListLength = readInt32LE(raf);

		for (int i = 0; i < userCommentListLength; i++) {

			int commentLength = readInt32LE(raf);

			if (commentLength > 0) {
				byte[] commentBytes = new byte[commentLength];
				raf.readFully(commentBytes);

				String comment = new String(commentBytes, StandardCharsets.UTF_8).trim();

				int equalPos = comment.indexOf('=');
				if (equalPos != -1) {
					String key = comment.substring(0, equalPos).toUpperCase();
					String value = comment.substring(equalPos + 1);

					switch (key) {
					case "TITLE":
						data.title = value;
						break;
					case "ARTIST":
						data.artist = value;
						break;
					}
				}
			}
		}

		return data;
	}

	private BufferedImage readPicture(RandomAccessFile raf) throws IOException {

		raf.skipBytes(4);

		int mimeLength = readInt32BE(raf);
		raf.skipBytes(mimeLength);

		int descLength = readInt32BE(raf);
		raf.skipBytes(descLength);

		raf.skipBytes(16);

		int imageDataLength = readInt32BE(raf);

		byte[] imageData = new byte[imageDataLength];
		raf.readFully(imageData);

		try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
			return ImageIO.read(bais);
		}
	}

	private int readInt32LE(RandomAccessFile raf) throws IOException {
		return (raf.read() & 0xFF) | ((raf.read() & 0xFF) << 8) | ((raf.read() & 0xFF) << 16)
				| ((raf.read() & 0xFF) << 24);
	}

	private int readInt32BE(RandomAccessFile raf) throws IOException {
		return ((raf.read() & 0xFF) << 24) | ((raf.read() & 0xFF) << 16) | ((raf.read() & 0xFF) << 8)
				| (raf.read() & 0xFF);
	}
}