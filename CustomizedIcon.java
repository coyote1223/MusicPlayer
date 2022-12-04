package main;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

public class CustomizedIcon implements Icon {

	private int width;
	private int height;
	BufferedImage bufferedImage;

	public CustomizedIcon(int width, int height, BufferedImage bim) {
		this.width = width;
		this.height = height;
		bufferedImage = bim;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {

		g.drawImage(bufferedImage, 0, 0, width, height, null);

	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}

}


