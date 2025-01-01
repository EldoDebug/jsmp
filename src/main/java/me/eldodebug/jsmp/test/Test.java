package me.eldodebug.jsmp.test;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import me.eldodebug.jsmp.Jsmp;
import me.eldodebug.jsmp.parser.JsmpResult;

public class Test {

	public static void main(String[] args) throws IOException {
		
		File main = new File("./main");
		
		main.mkdir();
		
		JsmpResult result = Jsmp.parse(new File(main, "test-v2.3.mp3"));
		
		System.out.println(result.getTitle());
		System.out.println(result.getArtist());
		ImageIO.write(result.getAlbumImage(), "png", new File(main, "test.png"));
	}

}
