package me.eldodebug.jsmp;

import java.io.File;
import java.io.IOException;

import me.eldodebug.jsmp.api.TagType;
import me.eldodebug.jsmp.exceptions.JsmpException;
import me.eldodebug.jsmp.parser.JsmpParser;
import me.eldodebug.jsmp.parser.JsmpResult;
import me.eldodebug.jsmp.parser.impl.FlacParser;
import me.eldodebug.jsmp.parser.impl.Mp3Id3v1Parser;
import me.eldodebug.jsmp.parser.impl.Mp3Id3v2Parser;

public final class Jsmp {
	
    private Jsmp() {
    }

    public static JsmpResult parse(File file) throws JsmpException {
        validateFile(file);

        try {
            TagType tagType = TagType.getTagType(file);
            JsmpParser parser = createParser(tagType, file);
            
            if (parser == null) {
                return null;
            }

            try {
                return parser.onParse();
            } catch (Exception e) {
                return null;
            }
        } catch (IOException e) {
            throw new JsmpException("Failed to determine file type", e);
        }
    }
    
    private static JsmpParser createParser(TagType tagType, File file) {
        return switch (tagType) {
            case FLAC -> new FlacParser(file);
            case MP3_ID3V1 -> new Mp3Id3v1Parser(file);
            case MP3_ID3V2 -> new Mp3Id3v2Parser(file);
            case UNKNOWN -> null;
        };
    }

    private static void validateFile(File file) throws JsmpException {
        if (file == null) {
            throw new JsmpException("File cannot be null");
        }
        if (!file.exists()) {
            throw new JsmpException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new JsmpException("Not a file: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new JsmpException("File is not readable: " + file.getAbsolutePath());
        }
    }
}