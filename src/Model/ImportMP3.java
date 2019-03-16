package Model;

import java.io.File;

public class ImportMP3 implements ImportSong {
    public static SongInterface createSong(String filelocation) {
        AudioParserInterface parser = new AudioParser();
        SongInterface s = new Song();
        File songFile = new File(filelocation);
        s.setSongfile(songFile);
        s.setSize(songFile.length());
        s = parser.getSongDetails(filelocation);

        return s;
    }
}
