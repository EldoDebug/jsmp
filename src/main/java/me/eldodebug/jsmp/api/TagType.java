package me.eldodebug.jsmp.api;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public enum TagType {
	FLAC(new byte[] { 'f', 'L', 'a', 'C' }), MP3_ID3V2(new byte[] { 'I', 'D', '3' }),
	MP3_ID3V1(new byte[] { 'T', 'A', 'G' }), UNKNOWN(null);

	private static final int MINIMUM_FILE_SIZE = 4;
	private static final int ID3V1_TAG_SIZE = 128;
	private static final int HEADER_READ_SIZE = 4;

	private final byte[] marker;

	private TagType(byte[] marker) {
		this.marker = marker;
	}

	public static TagType getTagType(File file) throws IOException {
		validateFile(file);

		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

			byte[] header = new byte[HEADER_READ_SIZE];

			if (raf.read(header) == HEADER_READ_SIZE) {

				if (compareBytes(header, FLAC.marker)) {
					return FLAC;
				}

				if (compareBytes(header, 0, MP3_ID3V2.marker, 0, MP3_ID3V2.marker.length)) {
					return MP3_ID3V2;
				}
			}

			if (file.length() >= ID3V1_TAG_SIZE) {
				raf.seek(file.length() - ID3V1_TAG_SIZE);
				byte[] id3v1Header = new byte[MP3_ID3V1.marker.length];
				if (raf.read(id3v1Header) == MP3_ID3V1.marker.length && compareBytes(id3v1Header, MP3_ID3V1.marker)) {
					return MP3_ID3V1;
				}
			}
		}

		return UNKNOWN;
	}

	private static void validateFile(File file) {
		if (file == null) {
			throw new IllegalArgumentException("File cannot be null");
		}
		if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
		}
		if (!file.isFile()) {
			throw new IllegalArgumentException("Not a file: " + file.getAbsolutePath());
		}
		if (file.length() < MINIMUM_FILE_SIZE) {
			throw new IllegalArgumentException("File too small: " + file.getAbsolutePath());
		}
	}

	private static boolean compareBytes(byte[] arr1, byte[] arr2) {
		return Arrays.equals(arr1, arr2);
	}

	private static boolean compareBytes(byte[] arr1, int start1, byte[] arr2, int start2, int length) {
		return Arrays.equals(Arrays.copyOfRange(arr1, start1, start1 + length),
				Arrays.copyOfRange(arr2, start2, start2 + length));
	}
}
