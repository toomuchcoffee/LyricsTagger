package tagging;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Tagger {
	
	private List<AudioFileRecord> records = new ArrayList<AudioFileRecord>();
	
	public void readFile(File file) {
        try {
            AudioFile f = AudioFileIO.read(file);
            Tag tag = f.getTag();
            
            String lyrics = tag.getFirst(FieldKey.LYRICS);
            if (lyrics==null || lyrics.trim().length()==0 || lyrics.trim().equalsIgnoreCase("not found")) {
            	AudioFileRecord newRecord = new AudioFileRecord();
            	newRecord.setFile(file);
            	newRecord.setArtist(tag.getFirst(FieldKey.ARTIST));
            	newRecord.setAlbum(tag.getFirst(FieldKey.ALBUM));
            	newRecord.setTitle(tag.getFirst(FieldKey.TITLE));
            	records.add(newRecord);
            }
        } 
        catch (CannotReadException e) {
        	System.out.println("file is not an audio file: "+file.getAbsolutePath());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public void writeToFile(File file, FieldKey key, String value) {
        try {
            AudioFile f = AudioFileIO.read(file);
            Tag tag = f.getTag();
            tag.setField(key, value);
            f.commit();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

	public List<AudioFileRecord> getRecords() {
		return records;
	}

}
