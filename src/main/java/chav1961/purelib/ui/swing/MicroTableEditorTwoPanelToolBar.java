package chav1961.purelib.ui.swing;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JToolBar;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfacers.ControllerAction;

class MicroTableEditorTwoPanelToolBar extends LocalizedToolBar {
	public static final int			SELECT = 1;
	public static final int			SELECT_ALL = 2;
	public static final int			UNSELECT = 3;
	public static final int			UNSELECT_ALL = 4;
	
	private static final long 		serialVersionUID = -1857133639572455483L;
	private static final String		SELECT_ICON = "twoPanelSelect.png";
	private static final String		SELECT_ICON_GRAY = "twoPanelSelectGray.png";
	private static final String		SELECT_ALL_ICON = "twoPanelSelectAll.png";
	private static final String		SELECT_ALL_ICON_GRAY = "twoPanelSelectAllGray.png";
	private static final String		UNSELECT_ICON = "twoPanelUnselect.png";
	private static final String		UNSELECT_ICON_GRAY = "twoPanelUnselectGray.png";
	private static final String		UNSELECT_ALL_ICON = "twoPanelUnselectAll.png";
	private static final String		UNSELECT_ALL_ICON_GRAY = "twoPanelUnselectAllGray.png";
	
	private final JButton			select;
	private final JButton			selectAll;
	private final JButton			unselect;
	private final JButton			unselectAll;
	
	MicroTableEditorTwoPanelToolBar(final Localizer localizer, final ActionListener parent) throws LocalizationException, IllegalArgumentException {
		super(localizer);
		setOrientation(JToolBar.VERTICAL);
		
		add(select = createButton(new SimpleCallAction(ControllerAction.ACTION_SELECT,parent),SwingUtils.url(SELECT_ICON_GRAY),SwingUtils.url(SELECT_ICON),PureLibLocalizer.TOOLBAR_SELECT),PureLibLocalizer.TOOLBAR_SELECT);
		add(selectAll = createButton(new SimpleCallAction(ControllerAction.ACTION_SELECT_ALL,parent),SwingUtils.url(SELECT_ALL_ICON_GRAY),SwingUtils.url(SELECT_ALL_ICON),PureLibLocalizer.TOOLBAR_SELECT_ALL),PureLibLocalizer.TOOLBAR_SELECT_ALL);
		addSeparator();
		add(unselect = createButton(new SimpleCallAction(ControllerAction.ACTION_UNSELECT,parent),SwingUtils.url(UNSELECT_ICON_GRAY),SwingUtils.url(UNSELECT_ICON),PureLibLocalizer.TOOLBAR_UNSELECT),PureLibLocalizer.TOOLBAR_UNSELECT);
		add(unselectAll = createButton(new SimpleCallAction(ControllerAction.ACTION_UNSELECT_ALL,parent),SwingUtils.url(UNSELECT_ALL_ICON_GRAY),SwingUtils.url(UNSELECT_ALL_ICON),PureLibLocalizer.TOOLBAR_UNSELECT_ALL),PureLibLocalizer.TOOLBAR_UNSELECT_ALL);
		
		setMinimumSize(new Dimension(32,32));
		setPreferredSize(new Dimension(32,100));
	}

	boolean isEnabled(final int buttonId) {
		switch (buttonId) {
			case SELECT			: return select.isEnabled();
			case SELECT_ALL		: return selectAll.isEnabled();
			case UNSELECT		: return unselect.isEnabled();
			case UNSELECT_ALL	: return unselectAll.isEnabled(); 
			default : throw new IllegalArgumentException("Illegal button id ["+buttonId+"]. Available are "+SELECT+".."+UNSELECT_ALL);
		}
	}
	
	void setEnabled(final int buttonId, final boolean state) {
		switch (buttonId) {
			case SELECT			: 
				select.setEnabled(state);
				break;
			case SELECT_ALL		:
				selectAll.setEnabled(state);
				break;
			case UNSELECT		:
				unselect.setEnabled(state);
				break;
			case UNSELECT_ALL	: 
				unselectAll.setEnabled(state);
				break;
			default : throw new IllegalArgumentException("Illegal button id ["+buttonId+"]. Available are "+SELECT+".."+UNSELECT_ALL);
		}
	}
}