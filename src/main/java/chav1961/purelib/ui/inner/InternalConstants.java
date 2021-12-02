package chav1961.purelib.ui.inner;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class InternalConstants {
	public static final ImageIcon	ICON_DIRECTORY;
	public static final ImageIcon	ICON_FILE;
	public static final ImageIcon	ICON_SUCCESS;
	public static final ImageIcon	ICON_FAIL;
	public static final ImageIcon	ICON_CLOSE;
	public static final ImageIcon	ICON_NEW_DIR;
	public static final ImageIcon	ICON_LEVEL_UP;
	public static final ImageIcon	ICON_SEARCH;
	public static final ImageIcon	ICON_CHECK;
	public static final ImageIcon	ICON_INSERT;
	public static final ImageIcon	ICON_EDIT;
	public static final ImageIcon	ICON_DELETE;
	public static final ImageIcon	ICON_CUT;
	public static final ImageIcon	ICON_COPY;
	public static final ImageIcon	ICON_PASTE;
	public static final ImageIcon	ICON_GOTO_LINK;
	
	public static final ContentMetadataInterface	MDI; 

	static {
		try{ICON_DIRECTORY = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("directory.png")));
			ICON_FILE = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("file.png")));
			ICON_SUCCESS = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("testOK.png")));
			ICON_FAIL = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("testFailed.png")));
			ICON_CLOSE = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("redIcon.png")));
			ICON_NEW_DIR = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("mkdir.png")));
			ICON_LEVEL_UP = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("levelUp.png")));
			ICON_SEARCH = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("search.png")));
			ICON_CHECK = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("check.png")));
			ICON_INSERT = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("insert.png")));
			ICON_EDIT = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("edit.png")));
			ICON_DELETE = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("delete.png")));
			ICON_CUT = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("cut.png")));
			ICON_COPY = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("copy.png")));
			ICON_PASTE = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("paste.png")));
			ICON_GOTO_LINK = new ImageIcon(ImageIO.read(InternalConstants.class.getResource("goto.png")));
			
			MDI = ContentModelFactory.forXmlDescription(InternalConstants.class.getResourceAsStream("application.xml"));			
		} catch (IOException | EnvironmentException e) {
			throw new PreparationException(e.getLocalizedMessage(),e);
		}
	}
}
