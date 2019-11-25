package chav1961.purelib.i18n;

import java.net.URI;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;

/**
 * <p>This class is an own localizer for the Pure Library components. Don't use it directly</p>
 *   
 * @see Localizer
 * @see chav1961.purelib.fsys
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.3
 */

public class PureLibLocalizer extends PropertiesLocalizer {
	public static final String		LOCALIZER_SUBSCHEME = "prop";
	public static final String		LOCALIZER_TAIL = "chav1961/purelib/i18n/localization";
	public static final String		LOCALIZER_SCHEME_STRING = Localizer.LOCALIZER_SCHEME+":"+LOCALIZER_SUBSCHEME+":"+LOCALIZER_TAIL;
	public static final URI			LOCALIZER_SCHEME = URI.create(LOCALIZER_SCHEME_STRING);

	public static final String		BUTTON_ACCEPT = "ACEPT";
	public static final String		BUTTON_CANCEL = "CANCEL";
	public static final String		BUTTON_OK = "OK";	
	
	public static final String		TITLE_HELP_SCREEN = "titleHelpScreen";
	public static final String		TITLE_ASK_PARAMETERS_SCREEN = "titleAskParametersScreen";
	public static final String		TITLE_FILECHOOSER_OPEN = "titleFileChooserOpen";
	public static final String		TITLE_FILECHOOSER_SAVEAS = "titleFileChooserSaveAs";
	
	public static final String		TOOLBAR_HISTORY_BACKWARD = "toolbarHistoryForward";
	public static final String		TOOLBAR_HISTORY_FORWARD = "toolbarHistoryBackward";
	public static final String		TOOLBAR_REFRESH = "toolbarRefresh";
	public static final String		TOOLBAR_COMMIT = "toolbarCommit";
	public static final String		TOOLBAR_ROLLBACK = "toolBarRollback";
	public static final String		TOOLBAR_INSERT = "toolbarInsert";
	public static final String		TOOLBAR_DUPLICATE = "toolbarDuplicate";
	public static final String		TOOLBAR_DELETE = "toolbarDelete";
	public static final String 		TOOLBAR_FILTER = "toolbarFilter";
	public static final String 		TOOLBAR_ORDER = "toolbarOrder";
	public static final String 		TOOLBAR_SELECT = "toolbarSelect";
	public static final String 		TOOLBAR_SELECT_ALL = "toolbarSelectAll";
	public static final String 		TOOLBAR_UNSELECT = "toolbarUnselect";
	public static final String 		TOOLBAR_UNSELECT_ALL = "toolbarUnselectAll";
	public static final String 		TOOLBAR_EXIT = "toolbarExit";

	public static final String		TITLE_STANDARD_SELECTED = "titleStandardSelected";
	public static final String		TITLE_STANDARD_AVAILABLE = "titleStandardAvailable";
	public static final String		TITLE_STANDARD_SELECTION_MARK_TOOLTIP = "titleStandardSelectionMarkTooltip";
	
	public PureLibLocalizer() throws LocalizationException, NullPointerException {
		super(LOCALIZER_TAIL);
	}
}
