# jsmp
Simple, high-level audio file metadata parsing library  
Written 100% in java with no external libraries

## Example

``` java
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import me.eldodebug.jsmp.Jsmp;
import me.eldodebug.jsmp.parser.JsmpResult;

public class Example {

	public static void main(String[] args) throws IOException {
		
		File file = new File("./path/to/file.mp3");
		
		JsmpResult result = Jsmp.parse(file);
		
		if(result != null) {
			
			String title = result.getTitle();
			String artist = result.getArtist();
			BufferedImage image = result.getAlbumImage();
			
			if(title != null) {
				System.out.println("Title: " + title);
			}
			
			if(artist != null) {
				System.out.println("Artist: " + artist);
			}
			
			if(image != null) {
				ImageIO.write(image, "png", new File("./path/to/save.png"));
			}
		}
	}
}
```

## Supported Files
Mp3 IDv3v1  
Mp3 IDv3v2  
Flac
