package chav1961.purelib.ui;

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;

import org.w3c.dom.Element;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.i18n.MultilangStringRepo;
import chav1961.purelib.ui.interfaces.CustomMenuInterface;

/**
 * <p>This class allows programmer to get prepared popup menu by it's XML description to use in the Swing applications.
 * This class is a child class of the {@link JPopupMenu} class and can' be used everywhere the parent one is used</p>
 * 
 * @see javax.swing.JPopupMenu JPopupMenu
 * @see javax.swing Java Swing
 * @see chav1961.purelib.ui JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class XMLPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = -5618590022288486477L;

	public XMLPopupMenu(final Element descriptor, final ActionListener listener, final MultilangStringRepo repo) throws ContentException {
		this(descriptor,listener,repo,new CustomMenuInterface(){
				@Override
				public JMenu getCustomMenu(Element menuDescriptor) {
					throw new IllegalArgumentException("There is a custom menu descriptor in the XML, but no any custom interfaces was used. Call another constructor!");
				}
			}
		);
	}

	public XMLPopupMenu(final Element descriptor, final ActionListener listener, final MultilangStringRepo repo, final CustomMenuInterface custom) throws ContentException {
		if (descriptor == null) {
			throw new IllegalArgumentException("Menu descriptor can't be null"); 
		}
		else if (listener == null) {
			throw new IllegalArgumentException("Action listener can't be null"); 
		}
		else if (custom == null) {
			throw new IllegalArgumentException("Custom menu interface can't be null"); 
		}
		else {
			Util.buildMenu(this,descriptor,listener,repo,custom);
		}
	}
}
