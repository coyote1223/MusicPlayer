package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class SongWithIconRenderer extends JLabel implements ListCellRenderer {

	public SongWithIconRenderer() {
		setOpaque(true);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {

		BufferedImage bim = CPlayer.getBufferedImageIcon("src\\resources\\musicNote64.png");


		CustomizedIcon songIcon = new CustomizedIcon(20, 20, bim); // taking a 64 pixel image and drawing it as 24
																	// pixel makes the quality better

		setIcon(songIcon);

		setText(((Song) value).toString());

		if (isSelected) {
			setBackground(new Color(204, 204, 204));
			setForeground(Color.BLACK);
		} else {
			setBackground(Color.WHITE);
			setForeground(Color.BLACK);
		}

		return this;
	}

}