package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import javax.swing.JButton;

public class RoundButton extends JButton{
	
	private BufferedImage bim;

	public RoundButton() {
	 
	    setFocusable(false);
	 
	    /*
	     These statements enlarges the button so that it 
	     becomes a circle rather than an oval.
	    */
	    Dimension size = getPreferredSize();
	    size.width = size.height = Math.max(size.width, size.height);
	    setPreferredSize(size);
	 
	    /*
	     This call causes the JButton not to paint the background.
	    */
	    setContentAreaFilled(false);
	 }
	  
	  protected void paintComponent(Graphics g) {
		
		g.setClip(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));  // set the area that shall be painted
		g.drawImage(bim, 0, 0, getWidth(), getHeight(), null); 
		
	    if (getModel().isArmed()) {
	      g.setColor(Color.WHITE);
	    } else {
	      g.setColor(getBackground());
	    }
	 
	    super.paintComponent(g);
	  }
	  
	  public void setImage(BufferedImage pbim) {
		  bim = pbim;
		  repaint();
	  }
	  
	 
	  // Hit detection.
	  Shape shape;
	 
	  public boolean contains(int x, int y) {
	    // If the button has changed size,  make a new shape object.
	    if (shape == null || !shape.getBounds().equals(getBounds())) {
	      shape = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
	    }
	    return shape.contains(x, y);
	  }
	
}