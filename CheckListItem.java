package main;

import java.io.File;

public class CheckListItem {
	
	private String label;
	private boolean isSelected = false;
	private File file;
	private String fullPath;
	private Song song;

	public CheckListItem(String fullPath) {
		file = new File(fullPath);
		this.label = file.getName();
		this.fullPath = fullPath;
		song = new Song(fullPath);
	}
	
	public CheckListItem(Song song) {
		this(song.getFullPathAsString());
	}
	
	
	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public File getFile() {
		return file;
	}

	public String getFullPathAsString() {
		return fullPath;
	}

	public Song getSong() {
		return song;
	}

	public String toString() {
		return label;
	}
	
}
