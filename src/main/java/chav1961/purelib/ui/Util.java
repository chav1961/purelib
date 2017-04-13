package chav1961.purelib.ui;

import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Util {
	private static final String		TAG_SUBMENU = "submenu";
	private static final String		TAG_ITEM = "item";
	private static final String		TAG_CHECKED = "checked";
	private static final String		TAG_RADIO = "radio";
	private static final String		TAG_SEPARATOR = "separator";
	
	private static final String		ATTR_NAME = "name";	
	private static final String		ATTR_CAPTION = "caption";	
	private static final String		ATTR_TOOLTIP = "tooltip";	
	private static final String		ATTR_ENABLED = "enabled";	
	private static final String		ATTR_CHECKED = "checked";	
	private static final String		ATTR_ICON = "icon";	
	private static final String		ATTR_ACTION = "action";	
	
	
	static JComponent buildMenu(final JComponent location, final Element descriptor, final ActionListener listener) {
		final NodeList	list = descriptor.getChildNodes();
		
		for (int index = 0; index < list.getLength(); index++) {
			final Object		temp = list.item(index);
			
			if (temp instanceof Element) {
				final Element	entity = (Element)temp;
				
				switch (entity.getTagName()) {
					case TAG_SUBMENU	:
						final JMenu		submenu = new JMenu(testNullString(TAG_SUBMENU+"->"+ATTR_CAPTION,entity.getAttribute(ATTR_CAPTION)));
						
						submenu.setName(testNullString(TAG_SUBMENU+"->"+ATTR_NAME,entity.getAttribute(ATTR_NAME)));
						submenu.setToolTipText(fillNullString(entity.getAttribute(ATTR_TOOLTIP),""));
						submenu.setEnabled(new Boolean(fillNullString(entity.getAttribute(ATTR_ENABLED),"true")));
						buildMenu(submenu,entity,listener);
						location.add(submenu);
						break;
					case TAG_ITEM		:
						final JMenuItem	item = new JMenuItem(testNullString(TAG_ITEM+"->"+ATTR_CAPTION,entity.getAttribute(ATTR_CAPTION)));
						
						item.setName(testNullString(TAG_ITEM+"->"+ATTR_NAME,entity.getAttribute(ATTR_NAME)));
						item.setToolTipText(fillNullString(entity.getAttribute(ATTR_TOOLTIP),""));
						item.setEnabled(new Boolean(fillNullString(entity.getAttribute(ATTR_ENABLED),"true")));
						item.setActionCommand(testNullString(TAG_ITEM+"->"+ATTR_ACTION,entity.getAttribute(ATTR_ACTION)));
						item.addActionListener(listener);
						location.add(item);
						break;
					case TAG_CHECKED	:
						final JCheckBoxMenuItem	check = new JCheckBoxMenuItem(testNullString(TAG_ITEM+"->"+ATTR_CAPTION,entity.getAttribute(ATTR_CAPTION)));
						
						check.setName(testNullString(TAG_ITEM+"->"+ATTR_NAME,entity.getAttribute(ATTR_NAME)));
						check.setToolTipText(fillNullString(entity.getAttribute(ATTR_TOOLTIP),""));
						check.setEnabled(new Boolean(fillNullString(entity.getAttribute(ATTR_ENABLED),"true")));
						check.setSelected(new Boolean(fillNullString(entity.getAttribute(ATTR_CHECKED),"false")));
						check.setActionCommand(testNullString(TAG_ITEM+"->"+ATTR_ACTION,entity.getAttribute(ATTR_ACTION)));
						check.addActionListener(listener);
						location.add(check);
						break;
					case TAG_RADIO		:
						final JRadioButtonMenuItem	radio = new JRadioButtonMenuItem(testNullString(TAG_ITEM+"->"+ATTR_CAPTION,entity.getAttribute(ATTR_CAPTION)));
						
						radio.setName(testNullString(TAG_ITEM+"->"+ATTR_NAME,entity.getAttribute(ATTR_NAME)));
						radio.setToolTipText(fillNullString(entity.getAttribute(ATTR_TOOLTIP),""));
						radio.setEnabled(new Boolean(fillNullString(entity.getAttribute(ATTR_ENABLED),"true")));
						radio.setSelected(new Boolean(fillNullString(entity.getAttribute(ATTR_CHECKED),"false")));
						radio.setActionCommand(testNullString(TAG_ITEM+"->"+ATTR_ACTION,entity.getAttribute(ATTR_ACTION)));
						radio.addActionListener(listener);
						location.add(radio);
						break;
					case TAG_SEPARATOR	:
						location.add(new JSeparator());
						break;
				}
			}
		}
		return location;
	}
	
	private static String testNullString(final String name, final String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Attribute ["+name+"] can't be null or empty");
		}
		else {
			return value;
		}
	}
	
	private static String fillNullString(final String value, final String defaultValue) {
		return value == null || value.trim().isEmpty() ? defaultValue : value; 
	}
}
