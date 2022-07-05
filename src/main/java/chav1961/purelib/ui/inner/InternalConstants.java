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
	public static final ImageIcon	ICON_LARGE_DIRECTORY;
	public static final ImageIcon	ICON_DIRECTORY_OPENED;
	public static final ImageIcon	ICON_LARGE_DIRECTORY_OPENED;
	public static final ImageIcon	ICON_FILE;
	public static final ImageIcon	ICON_LARGE_FILE;
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
	public static final ImageIcon	ICON_PREVIEW;	
	public static final ImageIcon	ICON_FOLDER;
	public static final ImageIcon	ICON_GOTO_LINK;
	public static final ImageIcon	ICON_TABLE;
	public static final ImageIcon	ICON_UPPERLEFT_QUAD;
	public static final ImageIcon	ICON_LOWERRIGHT_QUAD;
	public static final ImageIcon	ICON_EYE;
	
	public static final ContentMetadataInterface	MDI; 

	static {
		try{
			ICON_DIRECTORY = loadIcon("directory.png");
			ICON_LARGE_DIRECTORY = loadIcon("largeDirectory.png");
			ICON_DIRECTORY_OPENED = loadIcon("directoryOpened.png");
			ICON_LARGE_DIRECTORY_OPENED = loadIcon("largeDirectoryOpened.png");
			ICON_FILE = loadIcon("file.png");
			ICON_LARGE_FILE = loadIcon("largeFile.png");
			ICON_SUCCESS = loadIcon("testOK.png");
			ICON_FAIL = loadIcon("testFailed.png");
			ICON_CLOSE = loadIcon("redIcon.png");
			ICON_NEW_DIR = loadIcon("mkdir.png");
			ICON_LEVEL_UP = loadIcon("levelUp.png");
			ICON_SEARCH = loadIcon("search.png");
			ICON_CHECK = loadIcon("check.png");
			ICON_INSERT = loadIcon("insert.png");
			ICON_EDIT = loadIcon("edit.png");
			ICON_DELETE = loadIcon("delete.png");
			ICON_CUT = loadIcon("cut.png");
			ICON_COPY = loadIcon("copy.png");
			ICON_PASTE = loadIcon("paste.png");
			ICON_PREVIEW = loadIcon("preview.png");
			ICON_GOTO_LINK = loadIcon("goto.png");
			ICON_TABLE = loadIcon("table.png");
			ICON_FOLDER = loadIcon("folder.png");
			ICON_UPPERLEFT_QUAD = loadIcon("upperleft.png");
			ICON_LOWERRIGHT_QUAD = loadIcon("lowerright.png");
			ICON_EYE = loadIcon("eye.png");
			
			MDI = ContentModelFactory.forXmlDescription(InternalConstants.class.getResourceAsStream("application.xml"));			
		} catch (IOException | EnvironmentException e) {
			throw new PreparationException(e.getLocalizedMessage(),e);
		}
	}
	
	private static ImageIcon loadIcon(final String iconFileName) throws IOException {
		return new ImageIcon(ImageIO.read(InternalConstants.class.getResource(iconFileName)));
	}
}
