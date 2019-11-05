package chav1961.purelib.ui.swing.useful;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class JBackgroundComponent extends JComponent implements LocaleChangeListener, ImageObserver {
	private static final long 	serialVersionUID = 5947661254360981622L;

	private final Localizer 	localizer;
	private Image				currentImage = null;
	private int					imgWidth = -1, imgHeight = -1;
	
	public JBackgroundComponent(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
		}
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
	}

	public void setBackground(final URI imageUri) throws ContentException {
		if (imageUri == null) {
			throw new NullPointerException("Image URI can't be null"); 
		}
		else {
			try{setBackground(ImageIO.read(imageUri.toURL()));
			} catch (IOException e) {
				throw new ContentException(e.getLocalizedMessage(),e);
			}
		}
	}
	
	public void setBackground(final Image image) {
		if (image == null) {
			throw new NullPointerException("Image can't be null"); 
		}
		else {
			this.currentImage = image;
			this.imgWidth = image.getWidth(this);
			this.imgHeight = image.getHeight(this);
			repaint();
		}		
	}
	
	@Override
	public boolean imageUpdate(final Image img, final int infoflags, final int x, final int y, final int w, final int h) {
		repaint();
		return super.imageUpdate(img, infoflags, x, y, w, h);
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		if (imgWidth != -1 && imgHeight != -1) {
			final Graphics2D		g2d = (Graphics2D)g;
			final Dimension			windowSize = getSize();
			final AffineTransform	at = new AffineTransform();
			
			at.scale(1.0*windowSize.width/imgWidth,-1.0*windowSize.height/imgHeight);
			g2d.drawImage(currentImage,at,this);
		}
		else {
			super.paintComponent(g);
		}
	}
}
