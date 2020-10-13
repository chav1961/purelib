package chav1961.purelib.ui.swing.useful;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import chav1961.purelib.basic.exceptions.PreparationException;

public class CursorsAndIconsLibrary {
	public static final Cursor	DRAG_HAND;

	public static final Icon	ICON_DIRECTORY;
	public static final Icon	ICON_FILE;
	
	static {
		DRAG_HAND = loadCursor("DragHand",15,15);
		
		try{ICON_DIRECTORY = new ImageIcon(ImageIO.read(CursorsAndIconsLibrary.class.getResource("directory.png")));
			ICON_FILE = new ImageIcon(ImageIO.read(CursorsAndIconsLibrary.class.getResource("file.png")));
		} catch (IOException e) {
			throw new PreparationException(e.getLocalizedMessage(),e);
		}
	}
	
	private static Cursor loadCursor(final String cursorName, final int xPoint, final int yPoint) throws PreparationException {
		try{final Image 	image = ImageIO.read(JExtendedScrollPane.class.getResource(cursorName+".png"));
			
			return Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(xPoint,yPoint), cursorName);
		} catch (IOException e) {
			throw new PreparationException(e); 
		}
	}
}
