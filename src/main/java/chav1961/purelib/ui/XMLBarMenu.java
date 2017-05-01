package chav1961.purelib.ui;

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.w3c.dom.Element;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.i18n.MultilangStringRepo;
import chav1961.purelib.ui.interfaces.CustomMenuInterface;

/**
 *	<p>This class allows programmer to get prepared bar menu by it's XML description to use in the Swing applications.
 * 	This class is a child class of the {@link JMenuBar} class and can' be used everywhere the parent one is used</p>
 * 	<p>XML tree to describe menu can contain:</p>
 * 	<ul>
 * 	<li><code>&lt;submenu name="name" caption="caption" tooltip="tooltip" enabled="{true|false} icon="iconName"&gt;SUBMENU CONTENT&lt;/submenu&gt;</code></li>
 * 	<li><code>&lt;item name="name" caption="caption" tooltip="tooltip" enabled="{true|false} icon="iconName" action="actionString"&gt;</code></li>
 * 	<li><code>&lt;checked name="name" caption="caption" tooltip="tooltip" enabled="{true|false} checked="{true|false} icon="iconName" action="actionString"&gt;</code></li>
 * 	<li><code>&lt;radio name="name" caption="caption" tooltip="tooltip" enabled="{true|false} checked="{true|false} icon="iconName" action="actionString"&gt;</code></li>
 * 	<li><code>&lt;separator/&gt;</code></li>
 * 	</ul>
 *
 *	<p>Every tag can be identified with it's name, so the tag name need be unique (except radio tag, that need have the same names in order to group them together).</p>
 * 	<p>submenu is a group tag containing all the elements inside. It has a caption, an optional tooltip anf icon and can be enabled or disabled (default is enabled)</p>
 *  <p>iten is an ordinal menu string. It has a caption, an optional tooltip anf icon and can be enabled or disabled (default is enabled). Item selection fires
 *  action listener assigned with the item to process menu item selection</p>
 *  <p>checked is a menu string with the check kbox. It has a caption, an optional tooltip and icon and can be enabled or disabled (default is enabled).
 *  Item selection changes check box state and also fires action listener assigned with the item to process menu item selection</p> 
 *  <p>radio is a menu string with the radio group. It has a caption, an optional tooltip and icon and can be enabled or disabled (default is enabled).
 *  Item selection marks it as selected and blanks all other items in the group (item groups is all radio items with the same name). It also fires action listener 
 *  assigned with the item to process menu item selection</p>
 *  <p>separator is a division line in the menu and has no any behavior</p> 
 * 
 * @see javax.swing.JMenuBar JMenuBar
 * @see javax.swing Java Swing
 * @see chav1961.purelib.ui JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class XMLBarMenu extends JMenuBar {
	private static final long 	serialVersionUID = -1513870957660929643L;

	public XMLBarMenu(final Element descriptor, final ActionListener listener, final MultilangStringRepo repo) throws ContentException {
		this(descriptor,listener,repo,new CustomMenuInterface(){
				@Override
				public JMenu getCustomMenu(Element menuDescriptor) {
					throw new IllegalArgumentException("There is a custom menu descriptor in the XML, but no any custom interfaces was used. Call another constructor!");
				}
			}
		);
	}

	public XMLBarMenu(final Element descriptor, final ActionListener listener, final MultilangStringRepo repo, final CustomMenuInterface custom) throws ContentException {
		if (descriptor == null) {
			throw new IllegalArgumentException("Menu descriptor can't be null"); 
		}
		else if (listener == null) {
			throw new IllegalArgumentException("Action listener can't be null"); 
		}
		else if (repo == null) {
			throw new IllegalArgumentException("Multilang repo can't be null"); 
		}
		else if (custom == null) {
			throw new IllegalArgumentException("Custom menu interface can't be null"); 
		}
		else {
			Util.buildMenu(this,descriptor,listener,repo,custom);
		}
	}
}
