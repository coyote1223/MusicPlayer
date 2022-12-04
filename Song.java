package main;

import java.io.File;
import java.util.Objects;

public class Song {

	private File file;
	private String songName;
	private String songNameWithoutExtension;
	private String extension;
	
	public Song(String path) {
		file = new File(path);
		songName = file.getName();
		songNameWithoutExtension = songName.substring(0, songName.lastIndexOf('.'));
		extension = songName.substring(songName.lastIndexOf('.'), songName.length());
	}
	
	public Song(File file) {
		this.file = file;
		songName = file.getName();
		songNameWithoutExtension = songName.substring(0, songName.lastIndexOf('.'));
		extension = songName.substring(songName.lastIndexOf('.'), songName.length());
	}
	
	
	public String getSongNameWithoutExtension() {
		return songNameWithoutExtension;
	}
	
	public String getExtension() {
		return extension;
	}
	
	public File getFile() {
		return file;
	}
	
	public String getFullPathAsString() {
		return file.toString();
	}
	
	public String getSongName() {
		return songName;
	}
	
	public String toString() {
		return songName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(file, songName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Song)) {
			return false;
		}
		
		Song other = (Song) obj;
		
		return Objects.equals(file, other.file) && Objects.equals(songName, other.songName);
	}
}
