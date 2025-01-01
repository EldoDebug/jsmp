package me.eldodebug.jsmp.parser.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import me.eldodebug.jsmp.parser.JsmpParser;
import me.eldodebug.jsmp.parser.JsmpResult;

public class Mp3Id3v2Parser extends JsmpParser {

	public Mp3Id3v2Parser(File file) {
		super(file);
	}

	@Override
	public JsmpResult onParse() {

		if (!file.exists() || file.length() < 10) {
			return null;
		}

		try (FileInputStream fis = new FileInputStream(file)) {

			byte[] header = new byte[10];
			int readCount = fis.read(header);
			if (readCount < 10 || !isId3v2Header(header)) {
				return null;
			}

			int version = header[3];
			int tagSize = readSynchsafeInt(header, 6);

			byte[] tagData = new byte[tagSize];
			int totalRead = fis.read(tagData);

			if (totalRead < tagSize) {
				return null;
			}

			String title = null;
			String artist = null;
			BufferedImage albumImage = null;

			int offset = 0;

			while (offset + 10 <= tagData.length) {

				String frameId = new String(tagData, offset, 4, "ISO-8859-1");
				int frameSize = getFrameSize(tagData, offset + 4, version);

				if (frameSize <= 0 || offset + 10 + frameSize > tagData.length) {
					break;
				}

				switch (frameId) {
				case "TIT2":
					title = parseTextFrame(tagData, offset + 10, frameSize);
					break;
				case "TPE1":
					artist = parseTextFrame(tagData, offset + 10, frameSize);
					break;
				case "APIC":
					albumImage = parseApicFrame(tagData, offset + 10, frameSize);
					break;
				default:
					break;
				}

				offset += (10 + frameSize);
			}

			return new JsmpResult(title, artist, albumImage);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private boolean isId3v2Header(byte[] header) {
		return header[0] == 'I' && header[1] == 'D' && header[2] == '3';
	}

	private int readSynchsafeInt(byte[] buffer, int offset) {
		return (buffer[offset] & 0x7F) << 21 | (buffer[offset + 1] & 0x7F) << 14 | (buffer[offset + 2] & 0x7F) << 7
				| (buffer[offset + 3] & 0x7F);
	}

	private int getFrameSize(byte[] data, int offset, int version) {
		if (version == 4) {
			return (data[offset] & 0x7F) << 21 | (data[offset + 1] & 0x7F) << 14 | (data[offset + 2] & 0x7F) << 7
					| (data[offset + 3] & 0x7F);
		} else {
			return (data[offset] & 0xFF) << 24 | (data[offset + 1] & 0xFF) << 16 | (data[offset + 2] & 0xFF) << 8
					| (data[offset + 3] & 0xFF);
		}
	}

	private String parseTextFrame(byte[] frameData, int offset, int size) {

		if (size < 2) {
			return null;
		}

		byte encoding = frameData[offset];
		byte[] textBytes = new byte[size - 1];
		System.arraycopy(frameData, offset + 1, textBytes, 0, size - 1);

		switch (encoding) {
		case 0:
			return new String(textBytes, java.nio.charset.StandardCharsets.ISO_8859_1).trim();
		case 1:
			return new String(textBytes, java.nio.charset.StandardCharsets.UTF_16).trim();
		case 2:
			return new String(textBytes, java.nio.charset.StandardCharsets.UTF_16BE).trim();
		case 3:
			return new String(textBytes, java.nio.charset.StandardCharsets.UTF_8).trim();
		default:
			return new String(textBytes, java.nio.charset.StandardCharsets.ISO_8859_1).trim();
		}
	}

	private BufferedImage parseApicFrame(byte[] frameData, int offset, int size) {

		if (size < 4) {
			return null;
		}

		int pos = offset;
		pos++;

		while (pos < offset + size && frameData[pos] != 0) {
			pos++;
		}
		if (pos >= offset + size)
			return null;
		pos++;

		if (pos >= offset + size)
			return null;
		pos++;

		while (pos < offset + size && frameData[pos] != 0) {
			pos++;
		}
		if (pos >= offset + size)
			return null;
		pos++;

		int imageDataSize = (offset + size) - pos;
		if (imageDataSize <= 0) {
			return null;
		}
		byte[] imageBytes = new byte[imageDataSize];
		System.arraycopy(frameData, pos, imageBytes, 0, imageDataSize);

		try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
			BufferedImage img = ImageIO.read(bais);
			return img;
		} catch (IOException e) {
			return null;
		}
	}
}