package main;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;
import com.formdev.flatlaf.FlatLightLaf;

public class Main {

	public static void main(String[] args) {
		
		FlatLightLaf.setup();
		
		try {
			SwingUtilities.invokeLater(new CPlayer());
		} catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}

		
	}
	
	
	
}

