package chav1961.purelib.ui.swing.useful;

import java.awt.Color;
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
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class JBackgroundComponent extends JComponent implements LocaleChangeListener, ImageObserver, LocalizerOwner {
	private static final long 	serialVersionUID = 5947661254360981622L;

	public static enum FillMode {
		ORIGINAL,
		FILL,
		SQUARES
	}
	
	private final Localizer 	localizer;
	private FillMode			currentFillMode = FillMode.FILL;
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
	public Localizer getLocalizer() {
		return localizer;
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
	}

	public void setBackgroundImage(final URI imageUri) throws ContentException, NullPointerException {
		if (imageUri == null) {
			throw new NullPointerException("Image URI can't be null"); 
		}
		else {
			try{setBackgroundImage(ImageIO.read(imageUri.toURL()));
			} catch (IOException e) {
				throw new ContentException(e.getLocalizedMessage(),e);
			}
		}
	}
	
	public void setBackgroundImage(final Image image) throws NullPointerException {
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
	
	public Image getBackgroundImage() {
		return currentImage;
	}
	
	public FillMode getFillMode() {
		return currentFillMode;
	}
	
	public void setFillMode(final FillMode newMode) throws NullPointerException {
		if (newMode == null) {
			throw new NullPointerException("Fill mode can't be null"); 
		}
		else {
			currentFillMode = newMode;
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
			final AffineTransform	oldAt = g2d.getTransform();
			final AffineTransform	at = new AffineTransform();
			
			switch (currentFillMode) {
				case FILL		:
					at.scale(1.0*windowSize.width/imgWidth,1.0*windowSize.height/imgHeight);
					g2d.drawImage(currentImage, at, this);
					break;
				case ORIGINAL	:
					g2d.setColor(Color.GRAY);
					g2d.fillRect(0, 0, windowSize.width, windowSize.height);
//					at.translate((windowSize.width - imgWidth)/2,1.0*(windowSize.height - imgHeight)/2);
					g2d.drawImage(currentImage, at, this);
					break;
				case SQUARES	:
					for (int x = 0, maxX = (windowSize.width + 1 ) / imgWidth; x < maxX; x++) {
						for (int y = 0, maxY = (windowSize.height + 1) / imgHeight; y < maxY; y++) {
							final AffineTransform	sq = new AffineTransform();
							
							sq.translate(x * imgWidth,y * imgHeight);
							g2d.drawImage(currentImage, sq, this);
						}
					}
					break;
				default :
					throw new UnsupportedOperationException("Fill mode ["+currentFillMode+"] is not supported yet"); 
			}
			g2d.setTransform(oldAt);			
		}
		else {
			super.paintComponent(g);
		}
	}
}
