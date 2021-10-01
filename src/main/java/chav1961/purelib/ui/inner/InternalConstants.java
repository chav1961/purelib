package chav1961.purelib.ui.inner;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import chav1961.purelib.basic.exceptions.PreparationException;

public class InternalConstants {
	public static final ImageIcon	ICON_DIRECTORY;
	public static final ImageIcon	ICON_FILE;
	public static final ImageIcon	ICON_SUCCESS;
	public static final ImageIcon	ICON_FAIL;
	public static final ImageIcon	ICON_CLOSE;
	public static final ImageIcon	ICON_NEW_DIR;
	public static final ImageIcon	ICON_LEVEL_UP;

	static {
		try{ICON_DIRECTORY = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("directory.png")));
			ICON_FILE = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("file.png")));
			ICON_SUCCESS = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("testOK.png")));
			ICON_FAIL = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("testFailed.png")));
			ICON_CLOSE = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("redIcon.png")));
			ICON_NEW_DIR = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("mkdir.png")));
			ICON_LEVEL_UP = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("levelUp.png")));
		} catch (IOException e) {
			throw new PreparationException(e.getLocalizedMessage(),e);
		}
	}
}
