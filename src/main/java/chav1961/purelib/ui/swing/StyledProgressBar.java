package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.JToolTip;
import javax.swing.plaf.basic.BasicProgressBarUI;

import chav1961.purelib.basic.PureLibSettings;

public class StyledProgressBar extends JProgressBar {
	private static final long 	serialVersionUID = 2773678252437329000L;
	private static final double	SCALE_SIZE = 4;

	public StyledProgressBar() {
	    setUI(new StyledProgressBarUI());
	    setBackground(PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_OPTIONAL_BACKGROUND,Color.class));
	    setForeground(PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_OPTIONAL_FOREGROUND,Color.class));
	}
	
	private static class StyledProgressBarUI extends BasicProgressBarUI {
		@Override 
		public void paint(final Graphics g, final JComponent progressBar) {
			final Insets 	insets = progressBar.getInsets();
			final int 		barRectWidth  = progressBar.getWidth()  - insets.right - insets.left;
			final int 		barRectHeight = progressBar.getHeight() - insets.top - insets.bottom;
			
			if (barRectWidth <= 0 || barRectHeight <= 0) {
				return;
			}
			else {
			    final Graphics2D 		g2 = (Graphics2D)g;
				final AffineTransform	oldTransform = g2.getTransform();
				final AffineTransform	newTransform = new AffineTransform();
				
				if (barRectWidth > barRectHeight) {
					newTransform.scale(barRectHeight/SCALE_SIZE,barRectHeight/SCALE_SIZE);
					newTransform.translate(SCALE_SIZE*barRectWidth/(2*barRectHeight),SCALE_SIZE/2);
				}
				else {
					newTransform.scale(barRectWidth/SCALE_SIZE,barRectWidth/SCALE_SIZE);
					newTransform.translate(SCALE_SIZE/2,SCALE_SIZE*barRectHeight/(2*barRectWidth));
				}					
				
				g2.setTransform(newTransform);
			    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			    g2.setPaint(progressBar.getForeground());
			    
			    final double 	degree = 360 * ((JProgressBar)progressBar).getPercentComplete();
			    final double 	outerRadius = SCALE_SIZE * 0.5;
			    final double 	innerRaduis = outerRadius * 0.75;
			    final Shape 	inner = new Ellipse2D.Double(- innerRaduis, - innerRaduis, innerRaduis * 2, innerRaduis * 2);
			    final Shape 	outer = new Arc2D.Double(- outerRadius, - outerRadius, SCALE_SIZE, SCALE_SIZE, 90 - degree, degree, Arc2D.PIE);
			    final Area 		area = new Area(outer);
			    
			    area.subtract(new Area(inner));
			    g2.fill(area);
			
			    if (((JProgressBar)progressBar).isStringPainted()) {
			    	final String	str = String.format("%1$3.0f %%",100*((JProgressBar)progressBar).getPercentComplete());
			    	final Font		oldFont = g2.getFont();
			    	final Color		oldColor = g2.getColor(); 
			    	
			    	g2.setFont(new Font(oldFont.getFontName(),oldFont.getStyle(),1));
			    	g2.setColor(progressBar.getForeground());
			    	
			    	final Rectangle2D	rect = g2.getFontMetrics().getStringBounds(str,g2);

			    	g2.drawString(str,(float)-rect.getWidth()/2,(float)rect.getHeight()/4);
			    	g2.setColor(oldColor);
			    	g2.setFont(oldFont);
			    }
			    g2.setTransform(oldTransform);
			    g2.dispose();
			}
	  	}
	}
}
