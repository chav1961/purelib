package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.FocusManager;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.MimeParseException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.AbstractLocalizer;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.Constants.Builtin;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.ModelUtils;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.streams.StreamsUtil;
import chav1961.purelib.ui.LRUManager;
import chav1961.purelib.ui.interfaces.ActionFormManager;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.ItemAndSelection;
import chav1961.purelib.ui.interfaces.LRUManagerOwner;
import chav1961.purelib.ui.interfaces.LongItemAndReference;
import chav1961.purelib.ui.interfaces.ReferenceAndComment;
import chav1961.purelib.ui.interfaces.UIItemState;
import chav1961.purelib.ui.interfaces.UIItemState.AvailableAndVisible;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.interfaces.SwingItemEditor;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.LocalizedFormatter;
import chav1961.purelib.ui.swing.useful.editors.StringEditor;
import chav1961.purelib.ui.swing.useful.renderers.StringRenderer;

/**
 * <p>This utility class contains a set of useful methods to use in the Swing-based applications.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @last.update 0.0.7
 */

public abstract class SwingUtils {
	public static final Border				TABLE_CELL_BORDER = new LineBorder(Color.BLACK,1);
	public static final Border				TABLE_HEADER_BORDER = new LineBorder(Color.BLACK,1);	
	public static final Border				FOCUSED_TABLE_CELL_BORDER = new LineBorder(Color.BLUE,1);
	
	public static final KeyStroke			KS_BACKWARD = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK);
	public static final KeyStroke			KS_FORWARD = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK);
	public static final KeyStroke			KS_HELP = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
	public static final KeyStroke			KS_ACCEPT = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	public static final KeyStroke			KS_SOFT_ACCEPT = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);
	public static final KeyStroke			KS_CLICK = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
	public static final KeyStroke			KS_INSERT = KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0);
	public static final KeyStroke			KS_DUPLICATE = KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
	public static final KeyStroke			KS_DELETE = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
	public static final KeyStroke			KS_EXIT = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	public static final KeyStroke			KS_SOFT_EXIT = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, InputEvent.SHIFT_DOWN_MASK);
	public static final KeyStroke			KS_DROPDOWN = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK);
	public static final KeyStroke			KS_CONTEXTMENU = KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0);
	public static final KeyStroke			KS_CLOSE = KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK);
	public static final KeyStroke			KS_SOFT_CLOSE = KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_DOWN_MASK);
	public static final KeyStroke			KS_CUT = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK); 
	public static final KeyStroke			KS_COPY = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK); 
	public static final KeyStroke			KS_PASTE = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK); 
	public static final KeyStroke			KS_PRINT = KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK); 
	public static final KeyStroke			KS_SAVE = KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK); 
	public static final KeyStroke			KS_UNDO = KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK); 
	public static final KeyStroke			KS_REDO = KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK); 
	public static final KeyStroke			KS_UP = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0); 
	public static final KeyStroke			KS_DOWN = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0); 
	public static final KeyStroke			KS_FIND = KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK); 
	public static final KeyStroke			KS_FIND_REPLACE = KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK); 
	
	public static final String				ACTION_FORWARD = "forward";
	public static final String				ACTION_BACKWARD = "backward";
	public static final String				ACTION_INSERT = "insert";
	public static final String				ACTION_DUPLICATE = "duplicate";
	public static final String				ACTION_DELETE = "delete";
	public static final String				ACTION_DROPDOWN = "dropdown";
	public static final String				ACTION_ACCEPT = "accept";
	public static final String				ACTION_SOFT_ACCEPT = "softaccept";
	public static final String				ACTION_EXIT = "exit";
	public static final String				ACTION_SOFT_EXIT = "softexit";
	public static final String				ACTION_CLICK = "click";
	public static final String				ACTION_ROLLBACK = "rollback-value";
	public static final String				ACTION_HELP = "help";
	public static final String				ACTION_CUT = "cut";
	public static final String				ACTION_COPY = "copy";
	public static final String				ACTION_PASTE = "paste";
	public static final String				ACTION_PRINT = "print";
	public static final String				ACTION_SAVE = "save";
	public static final String				ACTION_CONTEXTMENU = "contextmenu";
	public static final String				ACTION_UNDO = "undo";
	public static final String				ACTION_REDO = "redo";
	public static final String				ACTION_UP = "up";
	public static final String				ACTION_DOWN = "down";
	public static final String				ACTION_SOFT_CLOSE = "softclose";
	public static final String				ACTION_FIND = "find";
	public static final String				ACTION_FIND_REPLACE = "findReplace";

	/**
	 * <p>This enumeration describes widely used editor keys.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 * @last.update 0.0.8
	 */
	public static enum EditorKeys {
		EK_INSERT(KS_INSERT, ACTION_INSERT),
		EK_DUPLICATE(KS_DUPLICATE, ACTION_DUPLICATE),
		EK_DELETE(KS_DELETE, ACTION_DELETE),
		EK_ACCEPT(KS_ACCEPT, ACTION_ACCEPT),
		EK_EXIT(KS_EXIT, ACTION_EXIT),
		EX_HELP(KS_HELP, ACTION_HELP);
		
		private final KeyStroke	ks;
		private final String	action;
		
		private EditorKeys(final KeyStroke ks, final String action) {
			this.ks = ks;
			this.action = action;
		}
		
		/**
		 * <p>Get key stroke associated with the current item</p>
		 * @return key stroke associated. Can't be null.
		 */
		public KeyStroke getKeyStroke() {
			return ks;
		}
		
		/**
		 * <p>Get action associated with the current item</p> 
		 * @return action associated. Can't be null or empty</p>
		 */
		public String getAction() {
			return action;
		}

		/**
		 * <p>Calculate editor key item by it's action string</p>
		 * @param action action string to calculate item for. Can't be null or empty
		 * @return item calculated. Can't be null
		 * @throws IllegalArgumentException action string is null, empty or not found anywhere
		 */
		public static EditorKeys byAction(final String action) throws IllegalArgumentException {
			if (Utils.checkEmptyOrNullString(action)) {
				throw new IllegalArgumentException("Action string can't be null or empty");
			}
			else {
				for(EditorKeys item : values()) {
					if (item.getAction().equals(action)) {
						return item;
					}
				}
				throw new IllegalArgumentException("Action string ["+action+"] not found anywhere");
			}
		}
	};

	/**
	 * 
	 */
	private static enum NavigationNodeType {
		MENU,
		SUBMENU,
		BUILTIN_SUBMENU,
		ITEM,
		SEPARATOR,
		UNKNOWN;
	};
	
	private static final Map<Class<?>,Object>	DEFAULT_VALUES = new HashMap<>();
	private static final Object[]				EMPTY_OPTIONS = new Object[0];

	private static final String		UNKNOWN_ACTION_TITLE = "SwingUtils.unknownAction.title";
	private static final String		UNKNOWN_ACTION_CONTENT = "SwingUtils.unknownAction.content";
	private static final String		PROP_THEONLY_HELP_DIALOG = "purelib.settings.ui.swing.theOnly.help.window";
	private static final JDialog	HELP_DIALOG;
	static final URI				MODEL_REF_URI = URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_REF+":/");
	static final URI				MODEL_FIELD_URI = URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/");
	
	static {
		DEFAULT_VALUES.put(byte.class,(byte)0);
		DEFAULT_VALUES.put(Byte.class,(byte)0);
		DEFAULT_VALUES.put(short.class,(short)0);
		DEFAULT_VALUES.put(Short.class,(short)0);
		DEFAULT_VALUES.put(int.class,0);
		DEFAULT_VALUES.put(Integer.class,0);
		DEFAULT_VALUES.put(long.class,0L);
		DEFAULT_VALUES.put(Long.class,0L);
		DEFAULT_VALUES.put(float.class,0.0f);
		DEFAULT_VALUES.put(Float.class,0.0f);
		DEFAULT_VALUES.put(double.class,0.0);
		DEFAULT_VALUES.put(Double.class,0.0);
		DEFAULT_VALUES.put(BigInteger.class,BigInteger.ZERO);
		DEFAULT_VALUES.put(BigDecimal.class,BigDecimal.ZERO);
		
		if (PureLibSettings.instance().getProperty(PROP_THEONLY_HELP_DIALOG, boolean.class)) {
			HELP_DIALOG = new JDialog((JFrame)null, ModalityType.MODELESS);
		}
		else {
			HELP_DIALOG = null;
		}
	}
	
	private SwingUtils() {}

	/**
	 * <p>This interface describes callback for walking on the swing component's tree</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface WalkCallback {
		/**
		 * <p>Process current node</p> 
		 * @param mode enter mode
		 * @param node current node
		 * @return continue mode
		 */
		ContinueMode process(final NodeEnterMode mode, final Component node);
	}

	@FunctionalInterface
	interface InnerActionNode {
		JComponent[] getActionNodes();
	}
	
	/**
	 * <p>Walk on the swing components tree</p>
	 * @param node root node to walk from
	 * @param callback callback to process nodes
	 * @return the same last continue mode (can't be null)
	 */
	public static ContinueMode walkDown(final Component node, final WalkCallback callback) {
		if (callback == null) {
			throw new NullPointerException("Root component can't be null");
		}
		else if (node == null) {
			throw new NullPointerException("Node callback can't be null");
		}
		else {
			return walkDownInternal(node,callback);
		}
	}	
	
	private static ContinueMode walkDownInternal(final Component node, final WalkCallback callback) {
		switch (callback.process(NodeEnterMode.ENTER,node)) {
			case CONTINUE		:
loop:			for (Component comp : children(node)) {
					switch (walkDownInternal(comp,callback)) {
						case CONTINUE:
							break;
						case SKIP_CHILDREN:
							break loop;
						case SKIP_SIBLINGS	:
							callback.process(NodeEnterMode.EXIT,node);
							return ContinueMode.SKIP_CHILDREN;
						case STOP:
							callback.process(NodeEnterMode.EXIT,node);
							return ContinueMode.STOP;
						default:
							break;
					}
				}
			case SKIP_CHILDREN	:
				callback.process(NodeEnterMode.EXIT,node);
				return ContinueMode.CONTINUE;
			case SKIP_SIBLINGS	:
				callback.process(NodeEnterMode.EXIT,node);
				return ContinueMode.SKIP_CHILDREN;
			case STOP			:
				callback.process(NodeEnterMode.EXIT,node);
				return ContinueMode.STOP;
			default:
				return ContinueMode.CONTINUE;
		}
	}

	/**
	 * <p>Make iterable for all children of the given component</p>
	 * @param component component to make children iterable for
	 * @return iterable for children. Can be empty but not null
	 * @throws NullPointerException when argument is null
	 */
	public static Iterable<Component> children(final Component component) throws NullPointerException {
		if (component == null) {
			throw new NullPointerException("Component to get children for can't be null");
		}
		else {
			final List<Component>	result = new ArrayList<>();
			Component[]				content;
			
			if (component instanceof Container) {
				if ((content = ((Container)component).getComponents()) != null) {
					result.addAll(Arrays.asList(content));
				}				
			}
			if (component instanceof JMenu) {
				if ((content = ((JMenu)component).getMenuComponents()) != null) {
					result.addAll(Arrays.asList(content));
				}				
			}
			if (component instanceof JFrame) {
				if ((content = ((JFrame)component).getRootPane().getComponents()) != null) {
					result.addAll(Arrays.asList(content));
				}				
			}
			if (component instanceof JDialog) {
				if ((content = ((JDialog)component).getRootPane().getComponents()) != null) {
					result.addAll(Arrays.asList(content));
				}				
			}
			if (component instanceof JDesktopPane) {
				if ((content = ((JDesktopPane)component).getAllFrames()) != null) {
					result.addAll(Arrays.asList(content));
				}				
			}
			if (component instanceof JLayeredPane) {
				final JLayeredPane	pane = ((JLayeredPane)component);
				
				for (int index = pane.lowestLayer(), maxIndex = pane.highestLayer(); index <= maxIndex; index++) {
					if (pane.getComponentCountInLayer(index) > 0 && (content = pane.getComponentsInLayer(index)) != null) {
						result.addAll(Arrays.asList(content));
					}				
				}
			}
			if (component instanceof JButtonWithMetaAndActions) {
				result.addAll(Arrays.asList(((JButtonWithMetaAndActions)component).getActionNodes()));
			}
			return result;
		}
	}
	
	
	/**
	 * <p>Find  component in the component tree by it's name</p>
	 * @param node root node to seek component
	 * @param name component name
	 * @return component found or null when  missing
	 * @throws NullPointerException when root node is null
	 * @throws IllegalArgumentException when name to seek is null or empty 
	 */
	public static Container findComponentByName(final Component node, final String name) throws NullPointerException, IllegalArgumentException {
		if (node == null) {
			throw new NullPointerException("Node component can't be null");
		}
		else if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name to find can't be null or empty");
		}
		else {
			final Component[]	result = new Component[]{null};
			
			walkDown(node,(mode,component)->{
				if (mode == NodeEnterMode.ENTER && name.equals(component.getName())) {
					result[0] = component;
					return ContinueMode.STOP;
				}
				else {
					return ContinueMode.CONTINUE;
				}
			});
			return (Container)result[0];
		}
	}
	
	/**
	 * <p>Add prefixes to all the names components in container<p>
	 * @param node root component to add prefix to it's content names
	 * @param prefix prefix to all
	 * @throws NullPointerException root component is null
	 * @throws IllegalArgumentException prefix to add is null or empty
	 * @since 0.0.4
	 */
	public static void addPrefix2ComponentNames(final Component node, final String prefix) throws NullPointerException, IllegalArgumentException {
		if (node == null) {
			throw new NullPointerException("Node can't be null");
		}
		else if (prefix == null || prefix.isEmpty()) {
			throw new IllegalArgumentException("Prefix name can't be null or empty");
		}
		else {
			walkDown(node,(mode,component)->{
				if (mode == NodeEnterMode.ENTER) {
					final String	name = component.getName();
					
					if (name != null && !name.startsWith(prefix)) {
						component.setName(prefix+name);
					}
				}
				return ContinueMode.CONTINUE;
			});
		}
	}
	
	
	/**
	 * <p>Prepare renderer for the given meta data and field format</p>
	 * @param metadata meta data to prepare renderer for
	 * @param localizer localizer for the given control
	 * @param content field content format
	 * @param monitor field monitor
	 * @return component prepared
	 * @throws NullPointerException when any parameters are null
	 * @throws LocalizationException when there are problems with localizers
	 * @throws SyntaxException on format errors for the given control
	 */
	public static JComponent prepareRenderer(final ContentNodeMetadata metadata, final Localizer localizer, final FieldFormat.ContentType content, final JComponentMonitor monitor) throws NullPointerException, LocalizationException, SyntaxException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content type can't be null");
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null");
		}
		else {
			JComponent	result = null;
			
			switch (content) {
				case BooleanContent	:
					result = new JCheckBoxWithMeta(metadata,localizer,monitor);
					break;
				case DateContent	:
					result = new JDateFieldWithMeta(metadata,localizer,monitor);
					break;
				case EnumContent	:
					result = new JEnumFieldWithMeta(metadata,monitor);
					break;
				case FileContent	:
					result = new JFileFieldWithMeta(metadata,monitor);
					break;
				case FormattedStringContent	:
					result = new JFormattedTextFieldWithMeta(metadata,monitor);
					break;
				case IntegerContent	:
					result = new JIntegerFieldWithMeta(metadata,monitor);
					break;
				case NumericContent	:
					result = new JNumericFieldWithMeta(metadata,monitor);
					break;
				case StringContent	:
					if (metadata.getFormatAssociated() != null && metadata.getFormatAssociated().getHeight() > 1) {
						result = new JTextAreaWithMeta(metadata,monitor);
					}
					else {
						result = new JTextFieldWithMeta(metadata,monitor);
					}
					break;
				case URIContent		:
					result = new JUriFieldWithMeta(metadata,monitor);
					break;
				case ColorContent	:
					result = new JColorPickerWithMeta(metadata,localizer,monitor);
					break;
				case ColorPairContent	:
					result = new JColorPairPickerWithMeta(metadata,localizer,monitor);
					break;
				case PasswordContent	:
					result = new JPasswordFieldWithMeta(metadata,monitor);
					break;
				case ArrayContent	:
					if (metadata.getType().isArray()) {
						final Class<?>	componentClass = metadata.getType().getComponentType();
						
						if (ItemAndSelection.class.isAssignableFrom(componentClass)) {
							result = new JSelectableListWithMeta<>(metadata, monitor);
						}
						else if (ReferenceAndComment.class.isAssignableFrom(componentClass)) {
							result = new JReferenceListWithMeta(metadata, monitor);
						}
						else if (LongItemAndReference.class.isAssignableFrom(componentClass)) {
							result = new JLongItemAndReferenceListWithMeta(metadata, localizer, monitor);
						}
						else {
							throw new UnsupportedOperationException("Content type ["+content+"] for metadata ["+metadata.getName()+"] is not supported yet");
						}
					}
					else {
						throw new UnsupportedOperationException("Content type ["+content+"] for metadata ["+metadata.getName()+"] is not supported yet");
					}
					break;
				case ImageContent	:
					result = new JImageContainerWithMeta(metadata, localizer, monitor);
					break;
				case ForeignKeyRefContent	:
					result = new JLongItemAndReferenceFieldWithMeta(metadata, localizer, monitor);
					break;
				case ForeignKeyRefListContent	:
					result = new JLongItemAndReferenceListWithMeta(metadata, localizer, monitor);
					break;
				case MimeBasedContent	:
					result = new JMimeContentFieldWithMeta(metadata, localizer, monitor);
					break;
				case LocaliizedStringContent	:
					result = new JLocalizedStringContentWithMeta(metadata, localizer, monitor);
					break;
				case DottedVersionContent	:
					result = new JDottedVersionFieldWithMeta(metadata, monitor);
					break;
				case RangeContent	:
					result = new JRangeSliderWithMeta(metadata, monitor);
					break;
				case Unclassified	:
				case NestedContent	:
				case TimestampContent	:
				default:
					throw new UnsupportedOperationException("Content type ["+content+"] for metadata ["+metadata.getName()+"] is not supported yet");
			}
			result.setName(metadata.getUIPath().toString());
			return result;
		}
	}

	/**
	 * <p>Build JComponent child by it's model description. Awaited classes can be JMenuBar, JPopupMenu or JToolBar only</p>
	 * @param <T> awaited JComponent type
	 * @param node metadata to build component for
	 * @param awaited awaited class for returned component
	 * @return returned component
	 * @throws NullPointerException on any nulls in the parameter's list
	 * @throws IllegalArgumentException unsupported class to build component.
	 */
	public static <T extends JComponent> T toJComponent(final ContentNodeMetadata node, final Class<T> awaited) throws NullPointerException, IllegalArgumentException{
		return toJComponent(node, awaited, (meta)->AvailableAndVisible.DEFAULT);
	}	
	
	/**
	 * <p>Build JComponent child by it's model description. Awaited classes can be JMenuBar, JPopupMenu or JToolBar only</p>
	 * @param <T> awaited JComponent type
	 * @param node metadata to build component for. Can't be null
	 * @param awaited awaited class for returned component. Can't be null
	 * @param state item state monitor for returned component. Can't be null
	 * @return returned component
	 * @throws NullPointerException on any nulls in the parameter's list
	 * @throws IllegalArgumentException unsupported class to build component.
	 * @since 0.0.5
	 */
	@SuppressWarnings("unchecked")
	public static <T extends JComponent> T toJComponent(final ContentNodeMetadata node, final Class<T> awaited, final UIItemState state) throws NullPointerException, IllegalArgumentException{
		if (node == null) {
			throw new NullPointerException("Model node can't be null"); 
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null"); 
		}
		else if (state == null) {
			throw new NullPointerException("Item state monitor can't be null"); 
		}
		else {
			if (awaited.isAssignableFrom(JMenuBar.class)) {
				if (!node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_TOP_PREFIX)) {
					throw new IllegalArgumentException("Model node ["+node.getUIPath()+"] can't be converted to ["+awaited.getCanonicalName()+"] class"); 
				}
				else {
					final JMenuBar	result = new JMenuBarWithMeta(node, state);
					
					for (ContentNodeMetadata child : node) {
						toMenuEntity(child,result);
					}
					result.addMouseListener(new MouseListener() {
						@Override public void mouseReleased(MouseEvent e) {}
						@Override public void mousePressed(MouseEvent e) {}
						@Override public void mouseExited(MouseEvent e) {}
						@Override public void mouseClicked(MouseEvent e) {}
						
						@Override
						public void mouseEntered(MouseEvent e) {
							processMenuVisibility(result, state);
						}
					});
					return (T) result;
				}
			}
			else if (awaited.isAssignableFrom(JPopupMenu.class)) {
				if (!node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_TOP_PREFIX)) {
					throw new IllegalArgumentException("Model node ["+node.getUIPath()+"] can't be converted to ["+awaited.getCanonicalName()+"] class"); 
				}
				else {
					final JPopupMenu	result = new JMenuPopupWithMeta(node, state);
					
					for (ContentNodeMetadata child : node) {
						toMenuEntity(child,result);
					}
					result.addComponentListener(new ComponentListener() {
						@Override public void componentResized(ComponentEvent e) {}
						@Override public void componentMoved(ComponentEvent e) {}
						@Override public void componentHidden(ComponentEvent e) {}
						
						@Override 
						public void componentShown(ComponentEvent e) {
							processMenuVisibility(result, state);
						}						
					});
					return (T) result;
				}
			}
			else if (awaited.isAssignableFrom(JToolBar.class)) {
				if (!node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_TOP_PREFIX)) {
					throw new IllegalArgumentException("Model node ["+node.getUIPath()+"] can't be converted to ["+awaited.getCanonicalName()+"] class"); 
				}
				else {
					try {
						return (T)new JToolBarWithMeta(node, state);
					} catch (LocalizationException | ContentException e) {
						throw new IllegalArgumentException("Error creation toolbar: "+e.getLocalizedMessage(),e); 
					}
				}
			}
			else if (awaited.isAssignableFrom(Action.class)) {
				throw new IllegalArgumentException("Unsupported awaited class ["+awaited.getCanonicalName()+"]. Only JMenuBar, JPopupMenu or JToolBar are supported"); 
			}
			else {
				throw new IllegalArgumentException("Unsupported awaited class ["+awaited.getCanonicalName()+"]. Only JMenuBar, JPopupMenu or JToolBar are supported"); 
			}
		}
	}
	
	/**
	 * <p>Assign value of pointed argument to it's screen presentation</p>
	 * @param metadata metadata to point argument to
	 * @param content object instance with value will be got from
	 * @param uiRoot any root of screen presentation
	 * @return true is assignment was successful
	 * @throws NullPointerException on any null arguments
	 * @throws IllegalArgumentException content instance is not annotated with @LocaleResource
	 * @throws ContentException exception on extracting data or storing it to screen
	 * @since 0.0.4
	 */
	public static boolean putToScreen(final ContentNodeMetadata metadata, final Object content, final Container uiRoot) throws NullPointerException, ContentException, IllegalArgumentException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (content == null) {
			throw new NullPointerException("Content instance object can't be null"); 
		}
		else if (uiRoot == null) {
			throw new NullPointerException("UI root component can't be null"); 
		}
		else if (!content.getClass().isAnnotationPresent(LocaleResource.class)) {
			throw new IllegalArgumentException("Content instance class ["+content.getClass().getCanonicalName()+"] doesn't annotated with @LocaleResource, and can't be used here"); 
		}
		else {
			final Component	component = SwingUtils.findComponentByName(uiRoot,metadata.getUIPath().toString());
			
			if (component instanceof JComponentInterface) {
				((JComponentInterface)component).assignValueToComponent(
							ModelUtils.getValueByGetter(content,
									GettersAndSettersFactory.buildGetterAndSetter(metadata.getApplicationPath())
							,metadata));
				return true;
			}
			else if (metadata.getChildrenCount() > 0) {
				boolean	put = false;
				
				for (ContentNodeMetadata item : metadata) {
					if (URIUtils.canServeURI(item.getApplicationPath(), MODEL_FIELD_URI) && putToScreen(item,content,uiRoot)) {
						put = true;
					}
				}
				return put;
			}
			else {
				return false;
			}
		}
	}
	
	/**
	 * <p>Read value from screen presentation and assign it to instance value for pointed argument</p> 
	 * @param metadata metadata to point argument to
	 * @param uiRoot any root of screen presentation
	 * @param content object instance with value will be stored from
	 * @return true is assignment was successful
	 * @throws NullPointerException on any null arguments
	 * @throws IllegalArgumentException content instance is not annotated with @LocaleResource
	 * @throws ContentException exception on extracting data or storing it to the instance
	 * @since 0.0.4
	 */
	public static boolean getFromScreen(final ContentNodeMetadata metadata, final Component uiRoot, final Object content) throws ContentException, NullPointerException, IllegalArgumentException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (content == null) {
			throw new NullPointerException("Content instance object can't be null"); 
		}
		else if (uiRoot == null) {
			throw new NullPointerException("UI root component can't be null"); 
		}
		else if (!content.getClass().isAnnotationPresent(LocaleResource.class)) {
			throw new IllegalArgumentException("Content instance class ["+content.getClass().getCanonicalName()+"] doesn't annotated with @LocaleResource, and can't be used here"); 
		}
		else {
			final Component	component = SwingUtils.findComponentByName(uiRoot,metadata.getUIPath().toString());
			
			if (component instanceof JComponentInterface) {
				ModelUtils.setValueBySetter(content,
							((JComponentInterface)component).getChangedValueFromComponent(),
								GettersAndSettersFactory.buildGetterAndSetter(metadata.getApplicationPath())
							,metadata);
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	
	/**
	 * <p>Prepare message in HTML format to show it in the JTextComponent controls</p>
	 * @param severity message severity
	 * @param format message format (see {@linkplain String#format(String, Object...)}
	 * @param parameters additional parameters
	 * @return HTML representation of the message
	 * @throws NullPointerException when any parameters are null
	 */
	public static String prepareHtmlMessage(final Severity severity, final String format, final Object... parameters) throws NullPointerException {
		if (severity == null) {
			throw new NullPointerException("Severity can't be null");
		}
		else if (format == null) {
			throw new NullPointerException("Format string can't be null");
		}
		else {
			final String	value = (parameters.length == 0 ? format : String.format(format,parameters)).replace("\n","<br>");
			final String	wrappedValue;
			
			switch (severity) {
				case debug	: wrappedValue = "<html><body><font color=gray>" + value + "</font></body></html>"; break;
				case error	: wrappedValue = "<html><body><font color=red>" + value + "</font></body></html>"; break;
				case info	: wrappedValue = "<html><body><font color=black>" + value + "</font></body></html>"; break;
				case severe	: wrappedValue = "<html><body><font color=red><b>" + value + "</b></font></body></html>"; break;
				case trace	: wrappedValue = "<html><body><font color=gray><i>" + value + "</i></font></body></html>"; break;
				case warning: wrappedValue = "<html><body><font color=blue>" + value + "</font></body></html>"; break;
				default		: wrappedValue = "<html><body><font color=black>" + value + "</font></body></html>"; break;
			}
			return wrappedValue;
		}
	}

	public static Object getDefaultValue4Class(final Class<?> clazz) {
		return DEFAULT_VALUES.get(clazz);
	}
	
	public static int getSignum4Value(final Object value) {
		if (value == null) {
			throw new NullPointerException("Value to get signum can't be null");
		}
		else {
			if (value instanceof BigInteger) {
				return ((BigInteger)value).signum();
			}
			else if (value instanceof BigDecimal) {
				return ((BigDecimal)value).signum();
			}
			else if (value instanceof Number) {
				return (int) Math.signum(((Number)value).doubleValue());
			}
			else {
				throw new IllegalArgumentException("Value type ["+value.getClass().getCanonicalName()+"] for value ["+value+"] is not a numerical type");
			}
		}
	}
	
	
	/**
	 * <p>Refresh localization content for all the GUI components tree. The method recursively walks on all the UI component tree, but doesn't walk down if the current
	 * UI component implements {@linkplain LocaleChangeListener} interface. All the implementers of this interface must refresh it's own content by self</p>
	 * @param root root of the GUI components to refresh localization
	 * @param oldLocale old locale
	 * @param newLocale new locale
	 * @throws NullPointerException if any parameter is null
	 * @throws LocalizationException in any localization errors
	 */
	public static void refreshLocale(final Component root, final Locale oldLocale, final Locale newLocale) throws NullPointerException, LocalizationException {
		if (root == null) {
			throw new NullPointerException("Root component can't be null"); 
		}
		else if (oldLocale == null) {
			throw new NullPointerException("Old locale can't be null"); 
		}
		else if (newLocale == null) {
			throw new NullPointerException("New locale can't be null"); 
		}
		else {
			refreshLocaleInternal(root, oldLocale, newLocale);
		}
	}
	
	private static void refreshLocaleInternal(final Component root, final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		if (root != null) {
			if (root instanceof LocaleChangeListener) {
				((LocaleChangeListener)root).localeChanged(oldLocale, newLocale);
			}
			else if (root instanceof Container) {
				for (Component item : children(root)) {
					refreshLocaleInternal(item,oldLocale,newLocale);
				}
			}
		}
	}

	/**
	 * <p>Assign key to process action on component</p>
	 * @param component component to assign key to
	 * @param keyStroke keystroke to assign
	 * @param listener action listener to call
	 * @param actionId action string to associate with the keystroke
	 * @throws NullPointerException any parameters are null
	 * @throws IllegalArgumentException some parameters are invalid
	 */
	public static void assignActionKey(final JComponent component, final KeyStroke keyStroke, final ActionListener listener, final String actionId) throws NullPointerException, IllegalArgumentException {
		assignActionKey(component,JPanel.WHEN_FOCUSED,keyStroke, listener, actionId);
	}	

	/**
	 * <p>Assigns key to process action on component</p>
	 * @param component component to assign key to
	 * @param listener action listener to call
	 * @param keys key  list to assign. Can't be null
	 * @throws NullPointerException any parameters are null
	 * @throws IllegalArgumentException some parameters are invalid
	 * @since 0.0.5
	 */
	public static void assignActionKeys(final JComponent component, final ActionListener listener, final EditorKeys... keys) throws NullPointerException, IllegalArgumentException {
		assignActionKeys(component,JPanel.WHEN_FOCUSED,listener,keys);
	}	

	/**
	 * <p>Assigns key to process action on component</p>
	 * @param component component to assign key to
	 * @param mode input map mode
	 * @param listener action listener to call
	 * @param keys key  list to assign. Can't be null
	 * @throws NullPointerException any parameters are null
	 * @throws IllegalArgumentException some parameters are invalid
	 * @since 0.0.5
	 */
	public static void assignActionKeys(final JComponent component, final int mode, final ActionListener listener, final EditorKeys... keys) throws NullPointerException, IllegalArgumentException {
		if (keys == null) {
			throw new NullPointerException("Editor keys can't be null");
		}
		else {
			for (EditorKeys item : keys) {
				assignActionKey(component, mode, item.getKeyStroke(), listener, item.getAction());
			}
		}
	}	
	
	/**
	 * <p>Remove action key was assigned earlier</p>
	 * @param component component to remove key from
	 * @param keyStroke keystroke assigned
	 * @param actionId action string assigned
	 * @throws NullPointerException any parameters are null
	 * @throws IllegalArgumentException some parameters are invalid
	 * @see #assignActionKey(JComponent, KeyStroke, ActionListener, String)
	 * @since 0.0.4
	 */
	public static void removeActionKey(final JComponent component, final KeyStroke keyStroke, final String actionId) throws NullPointerException, IllegalArgumentException {
		removeActionKey(component,JPanel.WHEN_FOCUSED,keyStroke, actionId);
	}	
	
	/**
	 * <p>Assign key to process action on component</p>
	 * @param component component to assign key to
	 * @param mode input map mode
	 * @param keyStroke keystroke to assign
	 * @param listener action listener to call
	 * @param actionId action string to associate with the keystroke
	 * @throws NullPointerException any parameters are null
	 * @throws IllegalArgumentException some parameters are invalid
	 */
	public static void assignActionKey(final JComponent component, final int mode, final KeyStroke keyStroke, final ActionListener listener, final String actionId) throws NullPointerException, IllegalArgumentException {
		if (component == null) {
			throw new NullPointerException("Component can't be null"); 
		}
		else if (keyStroke == null) {
			throw new NullPointerException("KeyStroke can't be null"); 
		}
		else if (listener == null) {
			throw new NullPointerException("Action listener can't be null"); 
		}
		else if (actionId == null || actionId.isEmpty()) {
			throw new IllegalArgumentException("Action identifier can't be null or empty"); 
		}
		else {
			component.getInputMap(mode).put(keyStroke,actionId);
			component.getActionMap().put(actionId,new AbstractAction() {
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					listener.actionPerformed(new ActionEvent(component,0,actionId));
				}
			});
		}
	}

	/**
	 * <p>Remove action key was assigned earlier</p>
	 * @param component component to remove key from
	 * @param mode input map mode
	 * @param keyStroke keystroke assigned
	 * @param actionId action string assigned
	 * @throws NullPointerException any parameters are null
	 * @throws IllegalArgumentException some parameters are invalid
	 * @see #removeActionKey(JComponent, KeyStroke, String)
	 * @since 0.0.4
	 */
	public static void removeActionKey(final JComponent component, final int mode, final KeyStroke keyStroke, final String actionId) throws NullPointerException, IllegalArgumentException {
		if (component == null) {
			throw new NullPointerException("Component can't be null"); 
		}
		else if (keyStroke == null) {
			throw new NullPointerException("KeyStroke can't be null"); 
		}
		else if (actionId == null || actionId.isEmpty()) {
			throw new IllegalArgumentException("Action identifier can't be null or empty"); 
		}
		else {
			component.getInputMap(mode).remove(keyStroke);
			component.getActionMap().remove(actionId);
		}
	}	
	
	/**
	 * <p>Center main window on the screen</p>
	 * @param frame main window
	 * @throws NullPointerException if frame is null
	 */
	public static void centerMainWindow(final JFrame frame) throws NullPointerException {
		centerMainWindow(frame,0.75f);
	}	
	
	/**
	 * <p>Center main window on the screen</p>
	 * @param frame main window
	 * @param fillPercent filling percent on the screen. Must be in the range 0.0f..1.0f
	 * @throws NullPointerException if frame is null
	 */
	public static void centerMainWindow(final JFrame frame, final float fillPercent) throws NullPointerException {
		if (frame == null) {
			throw new NullPointerException("Frame window can't be null"); 
		}
		else if (fillPercent < 0 || fillPercent > 1) {
			throw new IllegalArgumentException("Fill percent ["+fillPercent+"] out of range 0.0f..1.0f"); 
		}
		else {
			final Dimension	screen = Toolkit.getDefaultToolkit().getScreenSize();
			final Dimension	size = new Dimension((int)(screen.getWidth()*fillPercent),(int)(screen.getHeight()*fillPercent));
			final Point		location = new Point((int)(screen.getWidth()*(1-fillPercent)/2),(int)(screen.getHeight()*(1-fillPercent)/2));
			
			frame.setLocation(location);
			frame.setSize(size);
			frame.setPreferredSize(size);
		}
	}

	/**
	 * <p>Center main window on the screen</p>
	 * @param dialog main window
	 * @throws NullPointerException if dialog is null
	 */
	public static void centerMainWindow(final JDialog dialog) throws NullPointerException {
		centerMainWindow(dialog, 0.5f);
	}
	
	/**
	 * <p>Center main window on the screen and resize it to the given screen percent</p>
	 * @param dialog main window
	 * @param fillPercent filling percent on the screen. Must be in the range 0.0f..1.0f
	 * @throws NullPointerException if dialog is null
	 */
	public static void centerMainWindow(final JDialog dialog, final float fillPercent) throws NullPointerException {
		if (dialog == null) {
			throw new NullPointerException("Dialog window can't be null"); 
		}
		else if (fillPercent < 0 || fillPercent > 1) {
			throw new IllegalArgumentException("Fill percent ["+fillPercent+"] out of range 0.0f..1.0f"); 
		}
		else {
			final Dimension	screen = Toolkit.getDefaultToolkit().getScreenSize();
			final Dimension	size = new Dimension((int)(screen.getWidth()*fillPercent),(int)(screen.getHeight()*fillPercent));
			final Point		location = new Point((int)(screen.getWidth()*(1-fillPercent)/2),(int)(screen.getHeight()*(1-fillPercent)/2));
			
			dialog.setLocation(location);
			dialog.setSize(size);
			dialog.setPreferredSize(size);
		}
	}
	
	/**
	 * <p>This interface describes callback for closing frame</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface ExitMethodCallback {
		/**
		 * <p>Process exit from main frame</p>
		 * @throws Exception on any errors on the exit
		 */
		void processExit() throws Exception;
	}

	/**
	 * <p>Assign exit method for the window when pressed close button on the title</p>
	 * @param frame window to assign method to
	 * @param callback callback to execute
	 */
	public static void assignExitMethod4MainWindow(final JFrame frame, final ExitMethodCallback callback) {
		if (frame == null) {
			throw new NullPointerException("Window to assign exit method can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback to assign to window can't be null");
		}
		else {
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.addWindowListener(new WindowListener() {
				@Override public void windowOpened(WindowEvent e) {}
				@Override public void windowIconified(WindowEvent e) {}
				@Override public void windowDeiconified(WindowEvent e) {}
				@Override public void windowDeactivated(WindowEvent e) {}
				@Override public void windowClosed(WindowEvent e) {}
				@Override public void windowActivated(WindowEvent e) {}
				
				@Override 
				public void windowClosing(final WindowEvent e) {
					try{callback.processExit();
					} catch (Exception exc) {
					}
				}				
			});
		}
	}
	
	/**
	 * <p>This interface describes callback for processing unknown action string</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface FailedActionListenerCallback {
		/**
		 * <p>Process unknown action command</p>
		 * @param actionCommand command to process
		 */
		void processUnknown(final String actionCommand);
	}

	/**
	 * <p>This interface describes callback for preprocessing action command string</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.5
	 * @param <Obj> object instance type associated with the given action
	 * @param <C> cargo associated with the item
	 */
	@FunctionalInterface
	public interface PreprocessActionStringCallback<Obj,C> {
		/**
		 * <p>Preprocess action command</p>
		 * @param actionCommand action command to process. Can be null
		 * @param item item associated with action command
		 * @param meta metadata associated with item. Can be null
		 * @param cargo cargo associated with item. Can be null
		 * @return string preprocessed. Can be null
		 */
		String process(final String actionCommand, final Obj item, final ContentNodeMetadata meta, final C cargo);
	}
	
	/**
	 * <p>Prepare action listeners to call methods marked with {@linkplain OnAction} annotation</p>
	 * @param root root component to assign listeners to
	 * @param entity object to call it's annotated methods 
	 * @throws NullPointerException on any parameter is null
	 */
	public static void assignActionListeners(final JComponent root, final Object entity) throws NullPointerException {
		assignActionListeners(root,entity,(action)->{sayAboutUnknownAction(null,action);},(actionCommand,item,meta,cargo)->actionCommand);
	}

	/**
	 * <p>Prepare action listeners to call methods marked with {@linkplain OnAction} annotation</p>
	 * @param <C> cargo type
	 * @param root root component to assign listeners to. Can't be null
	 * @param entity object to call it's annotated methods. Can't be null
	 * @param preprocess callback to preprocess action command. Can't be null 
	 * @throws NullPointerException on any parameter is null
	 * @since 0.0.5
	 */
	public static <C> void assignActionListeners(final JComponent root, final Object entity, final PreprocessActionStringCallback<?,?> preprocess) throws NullPointerException {
		assignActionListeners(root,entity,(action)->{sayAboutUnknownAction(null,action);},preprocess);
	}

	/**
	 * <p>Prepare action listeners to call methods marked with {@linkplain OnAction} annotation</p>
	 * @param root root component to assign listeners to
	 * @param entity object to call it's annotated methods 
	 * @param onUnknown callback to process missing actions
	 * @throws NullPointerException on any parameter is null
	 */
	public static void assignActionListeners(final JComponent root, final Object entity, final FailedActionListenerCallback onUnknown) throws NullPointerException {
		assignActionListeners(root,entity,onUnknown,(actionCommand,item,meta,cargo)->actionCommand);
	}	

	/**
	 * <p>Prepare action listeners to call methods marked with {@linkplain OnAction} annotation</p>
	 * @param <Obj> entity type
	 * @param <C> cargo type
	 * @param root root component to assign listeners to. Can't be null
	 * @param entity object to call it's annotated methods. Can't be null
	 * @param onUnknown callback to process missing actions. Can't be null
	 * @param preprocess callback to preprocess action command. Can't be null 
	 * @throws NullPointerException on any parameter is null
	 * @since 0.0.5
	 */
	public static void assignActionListeners(final JComponent root, final Object entity, final FailedActionListenerCallback onUnknown, final PreprocessActionStringCallback<?,?> preprocess) throws NullPointerException {
		if (root == null) {
			throw new NullPointerException("Root component can't be null"); 
		}
		else if (entity == null) {
			throw new NullPointerException("Entity class can't be null"); 
		}
		else {
			internalAssignActionListeners(root,buildAnnotatedActionListener(entity, onUnknown), preprocess);
		}
	}

	/**
	 * <p>Prepare action listeners to call {@linkplain FormManager#onAction(Object, Object, String, Object)} method</p>
	 * @param <T> any entity is used for form manager
	 * @param root component with action sources inside
	 * @param entity instance to call onAction for
	 * @param manager form manager to process action on entity
	 * @since 0.0.4 
	 * @lastUpdate 0.0.5 
	 */
	public static <T> void assignActionListeners(final JComponent root, final T entity, final ActionFormManager<?,T> manager) {
		assignActionListeners(root, entity, manager, (actionCommand,item,meta,cargo)->actionCommand);
	}

	/**
	 * <p>Prepare action listeners to call {@linkplain FormManager#onAction(Object, Object, String, Object)} method</p>
	 * @param <T> any entity is used for form manager
	 * @param <Obj> entity type
	 * @param <C> cargo type
	 * @param root component with action sources inside
	 * @param entity instance to call onAction for
	 * @param manager form manager to process action on entity
	 * @param preprocess callback to preprocess action command. Can't be null 
	 * @since 0.0.5 
	 */
	public static <T> void assignActionListeners(final JComponent root, final T entity, final ActionFormManager<?,T> manager, final PreprocessActionStringCallback<?,?> preprocess) {
		if (root == null) {
			throw new NullPointerException("Root component can't be null"); 
		}
		else if (entity == null) {
			throw new NullPointerException("Entity class can't be null"); 
		}
		else if (manager == null) {
			throw new NullPointerException("Form manager can't be null"); 
		}
		else {
			internalAssignActionListeners(root,(e)->{
				try{manager.onAction(entity,null,e.getActionCommand(),null);
				} catch (LocalizationException | FlowException exc) {
				}
			}, preprocess);
		}
	}

	/**
	 * <p>Assign action listener for object and all it's subtree</p> 
	 * @param root object to assign action listener to. Can't be null
	 * @param listener listener to assign. Can't be null
	 * @since 0.0.5 
	 */
	public static void assignActionListeners(final JComponent root, final ActionListener listener) {
		assignActionListeners(root, listener, (a, b, c, d)->a);
	}
	
	/**
	 * <p>Assign action listener for object and all it's subtree</p>
	 * @param root object to assign action listener to. Can't be null
	 * @param listener listener to assign. Can't be null
	 * @param preprocess preprocessor for action commands. Can't be null
	 * @since 0.0.5 
	 */
	public static void assignActionListeners(final JComponent root, final ActionListener listener, final PreprocessActionStringCallback<?,?> preprocess) {
		if (root == null) {
			throw new NullPointerException("Root component can't be null"); 
		}
		else if (listener == null) {
			throw new NullPointerException("Entity class can't be null"); 
		}
		else if (preprocess == null) {
			throw new NullPointerException("Preprocess callback can't be null"); 
		}
		else {
			internalAssignActionListeners(root, listener, preprocess);
		}
	}
	
	
	/**
	 * <p>Build {@linkplain ActionListener} to call methods marked with {@linkplain OnAction} annotation</p>  
	 * @param entity marked instance to call methods in
	 * @return action listener built
	 * @throws IllegalArgumentException when no entity methods are marked with {@linkplain OnAction}
	 * @throws NullPointerException when entity reference is null
	 */
	public static ActionListener buildAnnotatedActionListener(final Object entity) throws IllegalArgumentException, NullPointerException {
		return buildAnnotatedActionListener(entity,(action)->{sayAboutUnknownAction(null,action);});
	}
	
	/**
	 * <p>Build {@linkplain ActionListener} to call methods marked with {@linkplain OnAction} annotation</p>  
	 * @param entity marked instance to call methods in
	 * @param onUnknown callback to process missing actions
	 * @return action listener built
	 * @throws IllegalArgumentException when no entity methods are marked with {@linkplain OnAction}
	 * @throws NullPointerException when entity reference is null
	 */
	public static ActionListener buildAnnotatedActionListener(final Object entity, final FailedActionListenerCallback onUnknown) throws IllegalArgumentException, NullPointerException {
		return buildAnnotatedActionListener(entity, onUnknown, entity instanceof Component ? SwingUtils.getNearestLogger((Component)entity) : PureLibSettings.CURRENT_LOGGER);
	}

	/**
	 * <p>Build {@linkplain ActionListener} to call methods marked with {@linkplain OnAction} annotation</p>  
	 * @param entity marked instance to call methods in
	 * @param onUnknown callback to process missing actions
	 * @param logger logger facade to print errors to
	 * @return action listener built
	 * @throws IllegalArgumentException when no entity methods are marked with {@linkplain OnAction}
	 * @throws NullPointerException when entity reference is null
	 */
	public static ActionListener buildAnnotatedActionListener(final Object entity, final FailedActionListenerCallback onUnknown, final LoggerFacade logger) throws IllegalArgumentException, NullPointerException {
		if (entity == null) {
			throw new NullPointerException("Entity class can't be null"); 
		}
		else if (onUnknown == null) {
			throw new NullPointerException("OnUnknown callback can't be null"); 
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else {
			final Map<String,Method>	annotatedMethods = new HashMap<>(); 
			Class<?>					entityClass = entity.getClass();
			
			CompilerUtils.walkMethods(entityClass,(clazz,m)->{
				if (m.isAnnotationPresent(OnAction.class)) {
					annotatedMethods.putIfAbsent(m.getAnnotation(OnAction.class).value(),m);
				}
			});
			
			if (annotatedMethods.size() == 0) {
				throw new IllegalArgumentException("No any methods in the entity object are annotated with ["+OnAction.class+"] annotation"); 
			}
			else {
				final Map<String,MethodHandleAndAsync>	calls = new HashMap<>();
				
				for (Entry<String, Method> item : annotatedMethods.entrySet()) {
					final Method	m = item.getValue();
					
					try{m.setAccessible(true);
						calls.put(item.getKey(),new MethodHandleAndAsync(CompilerUtils.buildMethodPath(m)+CompilerUtils.buildMethodSignature(m),MethodHandles.lookup().unreflect(m),m.getAnnotation(OnAction.class).async()));
					} catch (IllegalAccessException exc) {
						throw new IllegalArgumentException("Can't get access to annotated method ["+m+"]: "+exc.getLocalizedMessage()); 
					}
				}
				
				return (e)-> {
					final URI			action = URI.create(e.getActionCommand());
					final Map<String,?>	query = URIUtils.parseQuery(action);
					final String		actionKey = URIUtils.removeQueryFromURI(action).toString();
					
					if (calls.containsKey(actionKey)) {
						try{final MethodHandleAndAsync	mha = calls.get(actionKey);
						
							if (mha.async) {
								final Thread	t = new Thread(()->{
													try{if (!query.isEmpty()) {
															mha.handle.invoke(entity,query);
														}
														else {
															mha.handle.invoke(entity);
														}
													} catch (ThreadDeath d) {
														throw d;
													} catch (Throwable exc) {
														logger.message(Severity.error, exc, exc.getLocalizedMessage() == null ? exc.getClass().getSimpleName() : exc.getLocalizedMessage());
													}
												});
								
								t.setDaemon(true);
								t.start();
							}
							else {
								try{if (!query.isEmpty()) {
										mha.handle.invoke(entity,query);
									}
									else {
										mha.handle.invoke(entity);
									}
								} catch (WrongMethodTypeException exc) {
									throw new EnvironmentException("Illegal method signature ["+mha.signature+"] in class ["+entity.getClass()+"]: "+exc.getLocalizedMessage()); 
								}
							}
						} catch (ThreadDeath d) {
							throw d;
						} catch (Throwable t) {
							logger.message(Severity.error, t, t.getLocalizedMessage() == null ? t.getClass().getSimpleName() : t.getLocalizedMessage());
						}
					}
					else {
						onUnknown.processUnknown(e.getActionCommand());
					}
				};
			}
		}
	}
	
	private static void internalAssignActionListeners(final JComponent root, final ActionListener listener, final PreprocessActionStringCallback preprocess) {
		walkDownInternal(root,(mode,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if ((node instanceof NodeMetadataOwner) && ((NodeMetadataOwner)node).getNodeMetadata().getApplicationPath() != null && (URIUtils.canServeURI(((NodeMetadataOwner)node).getNodeMetadata().getApplicationPath(), MODEL_REF_URI))) {
					return ContinueMode.CONTINUE;
				}
				else if ((node instanceof JButtonWithMetaAndActions) && ((NodeMetadataOwner)node).getNodeMetadata().getChildrenCount() > 0) {
					return ContinueMode.CONTINUE;
				}
				else {
					final ActionListener	al = (e)-> listener.actionPerformed(new ActionEvent(e.getSource(), e.getID() 
														, preprocess.process(e.getActionCommand()
															, node
															, node instanceof NodeMetadataOwner ? ((NodeMetadataOwner)node).getNodeMetadata() : null
															, listener
														), e.getWhen(), e.getModifiers()));
					
					try{
						node.getClass().getMethod("addActionListener",ActionListener.class).invoke(node, al);
					} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					}
				}
			}
			return ContinueMode.CONTINUE;
		});
	}

	private static void sayAboutUnknownAction(final Component parent, final String actionName) {
		try{new JLocalizedOptionPane(PureLibSettings.PURELIB_LOCALIZER).message(parent,new LocalizedFormatter(UNKNOWN_ACTION_CONTENT,actionName),UNKNOWN_ACTION_TITLE,JOptionPane.ERROR_MESSAGE);
		} catch (LocalizationException exc) {
			PureLibSettings.CURRENT_LOGGER.message(Severity.error, exc, exc.getLocalizedMessage());
		}
	}
	
	/**
	 * <p>Build enable group on the screen. When group leader is turned on, all it's members will be enabled. When group leader is turned off, all it's members will be disabled
	 * @param leader group leader (usually {@linkplain JCheckBox})
	 * @param members members of the group
	 */
	public static void buildEnabledGroup(final AbstractButton leader, final JComponent... members) {
		// TODO:
	}

	@FunctionalInterface
	public interface FileGetter {
		File get();
	}

	@FunctionalInterface
	public interface FileSetter {
		void set(File file) throws ContentException;
	}
	
	public static void buildFileSelectionGroup(final JTextComponent fileName, final AbstractButton selector, final FileGetter getter, final FileSetter setter) {
		// TODO:
	}

	@FunctionalInterface
	public interface FileListGetter {
		File[] get();
	}

	@FunctionalInterface
	public interface FileListSetter {
		void set(File[] fileList) throws ContentException;
	}
	
	public static void buildFileListSelectionGroup(final JTextComponent fileName, final AbstractButton selector, final FileListGetter getter, final FileListSetter setter) {
		// TODO:
	}

	public static void buildDirectorySelectionGroup(final JTextComponent fileName, final AbstractButton selector, final FileGetter getter, final FileSetter setter) {
		// TODO:
	}

	/**
	 * <p>Show Creole-based help</p>
	 * @param owner help window owner
	 * @param localizer localizer to get help from. Can't be null
	 * @param helpKey key inside localizer. Can't be null or empty
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException when any parameter is null
	 * @throws IllegalArgumentException when help key is null or empty
	 * @since 0.0.5
	 */
	public static void showCreoleHelpWindow(final Component owner, final Localizer localizer, final String helpKey) throws IOException, NullPointerException, IllegalArgumentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (helpKey == null || helpKey.isEmpty()) {
			throw new IllegalArgumentException("Help key can't be null or empty"); 
		}
		else {
			try {
				SwingUtils.showCreoleHelpWindow(owner, 
						URIUtils.convert2selfURI(
								Utils.fromResource(localizer.getContent(helpKey, MimeType.MIME_CREOLE_TEXT, MimeType.MIME_HTML_TEXT)).toCharArray(),
						Localizer.LOCALIZER_DEFAULT_ENCODING)
				);
			} catch (LocalizationException exc) {
				throw new IOException("Localization error on ["+helpKey+"]: "+exc.getLocalizedMessage(), exc);
			}
		}
	}	
	
	/**
	 * <p>Show Creole-based help</p>
	 * @param owner help window owner
	 * @param helpContent help reference. Can't be null
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException when any parameter is null
	 */
	public static void showCreoleHelpWindow(final Component owner, final URI helpContent) throws IOException, NullPointerException {
		if (owner == null) {
			throw new NullPointerException("Window owner can't be null");
		}
		else if (helpContent == null) {
			throw new NullPointerException("Help content reference can't be null");
		}
		else {
			try {
				final JEditorPane 		pane = buildAboutContent(helpContent, new Dimension(640,480));
				final JScrollPane		scroll = new JScrollPane(pane);
				final JDialog			dlg = PureLibSettings.instance().getProperty(PROP_THEONLY_HELP_DIALOG, boolean.class) 
												? HELP_DIALOG
												: new JDialog((JFrame)null, ModalityType.MODELESS);

				dlg.getContentPane().removeAll();
				dlg.getContentPane().add(scroll);
				SwingUtils.assignActionKey(scroll, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, KS_EXIT, (e)->{
					dlg.setVisible(false);
					if (!PureLibSettings.instance().getProperty(PROP_THEONLY_HELP_DIALOG, boolean.class)) {
						dlg.dispose();
					}
				}, ACTION_EXIT);
				
				centerMainWindow(dlg);
				dlg.setVisible(true);
			} catch (MimeParseException e) {
				throw new IOException(e); 
			}
		}
	}

	/**
	 * <p>Show "About" window</p>
	 * @param owner window owner
	 * @param localizer localizer to use. Can;t be null
	 * @param title title key in the localizer. Can't be null or empty.
	 * @param content content key in the localizer. Can't be null or empty.
	 * @param imageIcon left icon to show window. Can't be null.
	 * @param preferredSize preferred window size. Can't be null
	 * @throws IllegalArgumentException any strings are null or empty
	 * @throws NullPointerException any arguments are null
	 * @since 0.0.5
	 * @lastUpdate 0.0.6
	 */
	public static void showAboutScreen(final JFrame owner, final Localizer localizer, final String title, final String content, final URI imageIcon, final Dimension preferredSize) throws IllegalArgumentException, NullPointerException {
		if (title == null || title.isEmpty()) {
			throw new IllegalArgumentException("Title string can't be null or empty");
		}
		else if (content == null || content.isEmpty()) {
			throw new IllegalArgumentException("Content string can't be null or empty");
		}
		else if (imageIcon == null) {
			throw new NullPointerException("Image icon can't be null");
		}
		else if (preferredSize == null) {
			throw new NullPointerException("Preferred size can't be null");
		}
		else {
			try{JOptionPane.showMessageDialog(owner, new JScrollPane(buildAboutContent(localizer, content, preferredSize)), localizer.getValue(title), JOptionPane.PLAIN_MESSAGE, new ImageIcon(imageIcon.toURL()));
			} catch (LocalizationException | IOException | MimeParseException e) {
				SwingUtils.getNearestLogger(owner.getContentPane()).message(Severity.error, e.getLocalizedMessage());
			}
		}
	}

	/**
	 * <p>Show "About" window</p>
	 * @param owner window owner
	 * @param localizer localizer to use. Can;t be null
	 * @param title title key in the localizer. Can't be null or empty.
	 * @param content content key in the localizer. Can't be null or empty.
	 * @param imageIcon left icon to show window. Can't be null.
	 * @param preferredSize preferred window size. Can't be null
	 * @throws IllegalArgumentException any strings are null or empty
	 * @throws NullPointerException any arguments are null
	 * @since 0.0.5
	 * @lastUpdate 0.0.6
	 */
	public static void showAboutScreen(final JDialog owner, final Localizer localizer, final String title, final String content, final URI imageIcon, final Dimension preferredSize) {
		if (title == null || title.isEmpty()) {
			throw new IllegalArgumentException("Title string can't be null or empty");
		}
		else if (content == null || content.isEmpty()) {
			throw new IllegalArgumentException("Content string can't be null or empty");
		}
		else if (imageIcon == null) {
			throw new NullPointerException("Image icon can't be null");
		}
		else if (preferredSize == null) {
			throw new NullPointerException("Preferred size can't be null");
		}
		else {
			try{JOptionPane.showMessageDialog(owner, new JScrollPane(buildAboutContent(localizer, content, preferredSize)), localizer.getValue(title), JOptionPane.PLAIN_MESSAGE, new ImageIcon(imageIcon.toURL()));
			} catch (LocalizationException | IOException | MimeParseException e) {
				SwingUtils.getNearestLogger(owner.getContentPane()).message(Severity.error, e.getLocalizedMessage());
			}
		}
	}

	/**
	 * <p>Calculate location of left-top corner for the window to fit in into screen.</p>
	 * @param xPosition x-coordinate of window anchor in the screen coordinates (for example, mouse hot spot)
	 * @param yPosition y-coordinate of window anchor in the screen coordinates (for example, mouse hot spot)
	 * @param popupWidth width of the window to fit
	 * @param popupHeight height of the window to fit
	 * @return left-top location of the window in the screen coordinates. If any moving is not required, returns window anchor coordinates
	 * @since 0.0.3
	 */
	public static Point locateRelativeToAnchor(final int xPosition, final int yPosition, final int popupWidth, final int popupHeight) {
		final Point 	popupLocation = new Point(xPosition, yPosition);

        if(GraphicsEnvironment.isHeadless()) {
            return popupLocation;
        }

        final GraphicsConfiguration gc = getCurrentGraphicsConfiguration(popupLocation);
        final Toolkit 	toolkit = Toolkit.getDefaultToolkit();
        final Rectangle scrBounds = gc != null ? gc.getBounds() : new Rectangle(toolkit.getScreenSize()); 

        final long 		popupRightX = (long)popupLocation.x + (long)popupWidth;
        final long 		popupBottomY = (long)popupLocation.y + (long)popupHeight;
        final Insets 	scrInsets = toolkit.getScreenInsets(gc);
        int				scrWidth = scrBounds.width;
        int 			scrHeight = scrBounds.height;

        scrBounds.x += scrInsets.left;
        scrBounds.y += scrInsets.top;
        scrWidth -= scrInsets.left + scrInsets.right;
        scrHeight -= scrInsets.top + scrInsets.bottom;
        
        int 			scrRightX = scrBounds.x + scrWidth;
        int 			scrBottomY = scrBounds.y + scrHeight;

        if (popupRightX > (long) scrRightX) {
            popupLocation.x = scrRightX - popupWidth;
        }

        if (popupBottomY > (long) scrBottomY) {
            popupLocation.y = scrBottomY - popupHeight;
        }

        if (popupLocation.x < scrBounds.x) {
            popupLocation.x = scrBounds.x;
        }

        if (popupLocation.y < scrBounds.y) {
            popupLocation.y = scrBounds.y;
        }

        return popupLocation;
	}

	/**
	 * <p>Build contour path around transparent image content.</p>
	 * @param image image to build contour for. Can't be null
	 * @return contour built. Can't be null
	 * @throws NullPointerException on any parameter s are null
	 * @since 0.0.5
	 */
	public static GeneralPath buildContour(final BufferedImage image) throws NullPointerException {
		if (image == null) {
			throw new NullPointerException("Raster to build contour for can't be null");
		}
		else {
			return buildContour(image,new Color(image.getRGB(0, 0)));
		}
	}
	
	/**
	 * <p>Build contour path around transparent image content.</p>
	 * @param image image to build contour for. Can't be null
	 * @param transparentColor transparent color. Can't be null
	 * @return contour built. Can't be null
	 * @throws NullPointerException on any parameter s are null
	 * @since 0.0.5
	 */
	// SImplest algorithm:
	// 1. previous (at the beginning - the same first) line has all falses for all image points
	// 2. Current line of image has falses at transparent color and trues otherwise.
	// 3. Any changes between lines produce new point into point set
	// 4. After testing current line becomes previous
	// 5. When all points have been built, make chain from it as a shortest distance between them
	public static GeneralPath buildContour(final BufferedImage image, final Color transparentColor) throws NullPointerException {
		if (image == null) {
			throw new NullPointerException("Raster to build contour for can't be null");
		}
		else if (transparentColor == null) {
			throw new NullPointerException("Transparent color for can't be null");
		}
		else {
			final GeneralPath	gp = new GeneralPath();
			final int			w = image.getWidth(), h = image.getHeight();
			
			if (w + h >= Short.MAX_VALUE) {
				throw new IllegalArgumentException("Image size is too long");
			}
			else if (((long)w) * h >= Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Image size is too long");
			}
			else {
				final int			transparent = transparentColor.getRGB() & 0xFFFFFF, maxDim = Math.max(w, h);
				final Set<Point>	points = new HashSet<>();
				final int[]			line = new int[maxDim];
				boolean[]			prevLine = new boolean[maxDim], currentLine = prevLine.clone(), temp;
				
				for (int y = 0; y < h; y++) {	// Collect bounds by Y axis
					image.getRGB(0, y, w, 1, line, 0, 1);
					for (int x = 0; x < w; x++) {
						currentLine[x] = (line[x] & 0xFFFFFF) != transparent;
					}
					for (int x = 0; x < w; x++) {
						if (currentLine[x] != prevLine[x]) {
							points.add(new Point(x,currentLine[x] ? y : y - 1));
						}
					}
					temp = prevLine;
					prevLine = currentLine;
					currentLine = temp;
				}
				for (int x = 0; x < w; x++) {
					if (prevLine[x]) {
						points.add(new Point(x,h-1));
					}
				}

				Arrays.fill(prevLine, false);
				for (int x = 0; x < w; x++) {	// Collect bounds by X axis
					image.getRGB(x, 0, 1, h, line, 0, 1);
					for (int y = 0; y < h; y++) {
						currentLine[y] = line[y] != transparent;
					}
					for (int y = 0; y < h; y++) {
						if (currentLine[y] != prevLine[y]) {
							points.add(new Point(currentLine[y] ? x : x - 1,y));
						}
					}
					temp = prevLine;
					prevLine = currentLine;
					currentLine = temp;
				}
				for (int y = 0; y < h; y++) {
					if (prevLine[y]) {
						points.add(new Point(w-1,y));
					}
				}
				
				final Point[]		p = points.toArray(new Point[points.size()]);
				final boolean[]		used = new boolean[p.length];
				Point				last, preLast, candidate;
				double				dist, newDist;
				int					candidateIndex;
				boolean				found;

				for (int index = 0, maxIndex = p.length; index < maxIndex; index++) {	// Search point pairs with nearest distance between 
					if (!used[index]) {
						last = p[index];
						preLast = null;
						
						used[index] = true;
						gp.moveTo(last.getX(), last.getY());
						
						do {found = false;
							dist = Double.MAX_VALUE;
							candidateIndex = -1;
							
							for (int next = 0; next < maxIndex; next++) {
								if (!used[next]) {
									newDist = last.distanceSq(p[next]);
									if (newDist < dist) {
										dist = newDist;
										candidateIndex = next;
										found = true;
									}
								}
							}
							if (dist > 2.5) {
								gp.lineTo(last.getX(), last.getY());
								break;
							}
							else if (found) {
								candidate = p[candidateIndex]; 
								used[candidateIndex] = true;
								
								// Reduce number of points - if new point "continues" direction of previous two, it will not be included in the path
								if (preLast != null && !(last.x - preLast.x == candidate.x - last.x && last.y - preLast.y == candidate.y - last.y)) {	// line is not continued...
									gp.lineTo(last.getX(), last.getY());
								}
								preLast = last;
								last = candidate;
							}
						} while (found);
						gp.closePath();
					}
				}
				
				return gp;
			}
		}
	}

	private static GraphicsConfiguration getCurrentGraphicsConfiguration(final Point popupLocation) {
        final GraphicsEnvironment 	ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] 		gd = ge.getScreenDevices();
        
        for(int i = 0; i < gd.length; i++) {
            if(gd[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                final GraphicsConfiguration 	dgc = gd[i].getDefaultConfiguration();
                
                if(dgc.getBounds().contains(popupLocation)) {
                    return dgc;
                }
            }
        }
        return gd[0].getDefaultConfiguration();
    }

	/**
	 * <p>Redirect all mouse and/or key events from one component to another</p>
	 * @param from component to redirect events from
	 * @param to component to redirect events to
	 * @throws NullPointerException any component is null
	 * @since 0.0.5
	 */
	public static void redirectMouseAndKeyEvents(final JComponent from, final JComponent to) throws NullPointerException {
		redirectMouseAndKeyEvents(from, to, true, true, true, true);
	}	

	/**
	 * <p>Redirect all mouse and/or key events from one component to another</p>
	 * @param from component to redirect events from
	 * @param to component to redirect events to
	 * @param forMouseEvent redirect mouse events
	 * @param forMouseMotionEvent redirect mouse motion events
	 * @param forMouseWheelEvent redirect mouse wheel events
	 * @param forKeyEvent redirect  key events
	 * @throws NullPointerException any component is null
	 * @since 0.0.5
	 */
	public static void redirectMouseAndKeyEvents(final JComponent from, final JComponent to, final boolean forMouseEvent, final boolean forMouseMotionEvent, final boolean forMouseWheelEvent, final boolean forKeyEvent) throws NullPointerException {
		if (from == null) {
			throw new NullPointerException("From component can't be null");
		}
		else if (to == null) {
			throw new NullPointerException("To component can't be null");
		}
		else {
			if (forMouseEvent) {
				from.addMouseListener(new MouseListener() {
					@Override public void mouseReleased(MouseEvent e) {to.dispatchEvent(e);}
					@Override public void mousePressed(MouseEvent e) {to.dispatchEvent(e);}
					@Override public void mouseExited(MouseEvent e) {to.dispatchEvent(e);}
					@Override public void mouseEntered(MouseEvent e) {to.dispatchEvent(e);}
					@Override public void mouseClicked(MouseEvent e) {to.dispatchEvent(e);}
				});
			}
			if (forMouseMotionEvent) {
				from.addMouseMotionListener(new MouseMotionListener() {
					@Override public void mouseMoved(MouseEvent e) {to.dispatchEvent(e);}
					@Override public void mouseDragged(MouseEvent e) {to.dispatchEvent(e);}
				});
			}
			if (forMouseWheelEvent) {
				from.addMouseWheelListener(new MouseWheelListener() {
					@Override public void mouseWheelMoved(MouseWheelEvent e) {to.dispatchEvent(e);}
				});
			}
			if (forKeyEvent) {
				from.addKeyListener(new KeyListener() {
					@Override public void keyTyped(KeyEvent e) {to.dispatchEvent(e);}
					@Override public void keyReleased(KeyEvent e) {to.dispatchEvent(e);}
					@Override public void keyPressed(KeyEvent e) {to.dispatchEvent(e);}
				});
			}
		}
	}

	/**
	 * <p>Fill last-recently-used menu (LRU) with items</p>
	 * @param menu menu to fill. Must be JMenuWithMeta instance  
	 * @param lruList list of instances to add. Can be empty but not null
	 * @since 0.0.5
	 */
	public static void fillLruSubmenu(final JMenu menu, final Iterable<String> lruList) {
		if (menu == null || !(menu instanceof JMenuWithMeta)) {
			throw new IllegalArgumentException("Menu to fill can't be null and must be JMenuWithMeta instance");
		}
		else if (lruList ==  null) {
			throw new NullPointerException("LRU list can't be null");
		}
		else {
			final JMenuWithMeta	metaMenu = (JMenuWithMeta)menu;
			boolean				filled = false;
			
			menu.removeAll();			
			for (String item : lruList) {
				final JMenuItem	menuItem = new JMenuItem(item);
				
				menuItem.addActionListener((e)->metaMenu.processActionEvent(metaMenu.getNodeMetadata().getApplicationPath().getSchemeSpecificPart()+"?name="+item));
				menu.add(menuItem);
				filled = true;
			}
			menu.setEnabled(filled);
		}
	}

	/**
	 * <p>Convert JPopupMenu content to PopupMenu</p>
	 * @param menu menu to convert to popup. Can't be null
	 * @param listener listener to associate with popup menu built. Can;t be null 
	 * @return popup menu built. Can't be null
	 * @throws NullPointerException on any nulls in arguments
	 * @since 0.0.5
	 */
	public static PopupMenu toPopupMenu(final JPopupMenu menu, final ActionListener listener) throws NullPointerException {
		if (menu == null) {
			throw new NullPointerException("Popup menu to convert can't be null");
		}
		else if (listener == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			final PopupMenu		pm = new PopupMenu();
			final List<Menu>	stack = new ArrayList<>();
			
			walkDown(menu, (mode,c) -> {
				if (mode == NodeEnterMode.ENTER) {
					if (c instanceof JSeparator) {
						if (stack.isEmpty()) {
							pm.addSeparator();
						}
						else {
							stack.get(0).addSeparator();
						}
					}
					else if (c instanceof JMenuItem) {
						final JMenuItem	mi = (JMenuItem)c;
						final MenuItem	item = new MenuItem(mi.getText());

						item.setActionCommand(mi.getActionCommand());
						item.addActionListener(listener);
						if (stack.isEmpty()) {
							pm.add(item);
						}
						else {
							stack.get(0).add(item);
						}
					}
					else if (c instanceof JMenu) {
						stack.add(0,new Menu(((JMenu)c).getText()));
					}
				}
				else if (c instanceof JMenu) {
					final Menu	item = stack.remove(0);
					
					if (stack.isEmpty()) {
						pm.add(item);
					}
					else {
						stack.get(0).add(item);
					}
				}
				return ContinueMode.CONTINUE;
			});
			return pm;
		}
	}

	/**
	 * <p>Get cell renderer for different controls. Each renderer is an SPI service with {@linkplain SwingItemRenderer} implementation </p>
	 * @param <R> renderer type ({@linkplain ListCellRenderer}, {@linkplain TableCellRenderer}, {@linkplain TreeCellRenderer})
	 * @param meta model descriptor. Can't be null
	 * @param rendererType renderer type. Can't be null
	 * @return renderer found
	 * @throws EnvironmentException renderer is missing
	 * @since 0.0.5
	 */
	public static <R> R getCellRenderer(final ContentNodeMetadata meta, final Class<R> rendererType) throws EnvironmentException {
		return getCellRenderer(meta, rendererType, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * <p>Get cell renderer for different controls. Each renderer is an SPI service with {@linkplain SwingItemRenderer} implementation </p>
	 * @param <R> renderer type ({@linkplain ListCellRenderer}, {@linkplain TableCellRenderer}, {@linkplain TreeCellRenderer})
	 * @param meta model descriptor. Can't be null
	 * @param rendererType renderer type. Can't be null
	 * @param loader class loader to use SPI in. Can't be null
	 * @return renderer found
	 * @throws EnvironmentException renderer is missing
	 * @since 0.0.5
	 */
	public static <R> R getCellRenderer(final ContentNodeMetadata meta, final Class<R> rendererType, final ClassLoader loader) throws EnvironmentException {
		return getCellRenderer(meta.getType(), meta.getFormatAssociated(), rendererType, loader);
	}	

	/**
	 * <p>Get cell renderer for different controls. Each renderer is an SPI service with {@linkplain SwingItemRenderer} implementation </p>
	 * @param <T> class to render (for example, {@linkplain String})
	 * @param <R> renderer type ({@linkplain ListCellRenderer}, {@linkplain TableCellRenderer}, {@linkplain TreeCellRenderer})
	 * @param clazz class to render. Can't be null
	 * @param ff field format. Can't be null
	 * @param rendererType renderer type. Can't be null
	 * @return renderer found
	 * @throws EnvironmentException renderer is missing
	 * @since 0.0.5
	 */
	public static <T, R> R getCellRenderer(final Class<T> clazz, final FieldFormat ff, final Class<R> rendererType) throws EnvironmentException {
		return getCellRenderer(clazz, ff, rendererType, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * <p>Get cell renderer for different controls. Each renderer is an SPI service with {@linkplain SwingItemRenderer} implementation </p>
	 * @param <T> class to render (for example, {@linkplain String})
	 * @param <R> renderer type ({@linkplain ListCellRenderer}, {@linkplain TableCellRenderer}, {@linkplain TreeCellRenderer})
	 * @param clazz class to render. Can't be null
	 * @param ff field format. Can't be null
	 * @param rendererType renderer type. Can't be null
	 * @param options options to pass to renderer. Can be empty but not null
	 * @return renderer found
	 * @throws EnvironmentException renderer is missing
	 * @since 0.0.7
	 */
	public static <T, R> R getCellRenderer(final Class<T> clazz, final FieldFormat ff, final Class<R> rendererType, final Object... options) throws EnvironmentException {
		return getCellRenderer(clazz, ff, rendererType, Thread.currentThread().getContextClassLoader(), options);
	}	
	
	/**
	 * <p>Get cell renderer for different controls. Each renderer is an SPI service with {@linkplain SwingItemRenderer} implementation </p>
	 * @param <T> class to render (for example, {@linkplain String})
	 * @param <R> renderer type ({@linkplain ListCellRenderer}, {@linkplain TableCellRenderer}, {@linkplain TreeCellRenderer})
	 * @param clazz class to render. Can't be null
	 * @param ff field format. Can't be null
	 * @param rendererType renderer type. Can't be null
	 * @param loader class loader to use SPI in. Can't be null
	 * @return renderer found
	 * @throws EnvironmentException renderer is missing
	 * @since 0.0.5
	 * @lastUpdate 0.0.7
	 */
	public static <T, R> R getCellRenderer(final Class<T> clazz, final FieldFormat ff, final Class<R> rendererType, final ClassLoader loader) throws EnvironmentException {
		return getCellRenderer(clazz, ff, rendererType, loader, EMPTY_OPTIONS);
	}
	
	/**
	 * <p>Get cell renderer for different controls. Each renderer is an SPI service with {@linkplain SwingItemRenderer} implementation </p>
	 * @param <T> class to render (for example, {@linkplain String})
	 * @param <R> renderer type ({@linkplain ListCellRenderer}, {@linkplain TableCellRenderer}, {@linkplain TreeCellRenderer})
	 * @param clazz class to render. Can't be null
	 * @param ff field format. Can't be null
	 * @param rendererType renderer type. Can't be null
	 * @param loader class loader to use SPI in. Can't be null
	 * @param options options to pass to renderer. Can be empty but not null
	 * @return renderer found
	 * @throws EnvironmentException renderer is missing
	 * @since 0.0.7
	 */
	public static <T, R> R getCellRenderer(final Class<T> clazz, final FieldFormat ff, final Class<R> rendererType, final ClassLoader loader, final Object... options) throws EnvironmentException {
		for (SwingItemRenderer<T,R> item : ServiceLoader.load(SwingItemRenderer.class, loader)) {
			if (item.canServe(clazz, rendererType, ff)) {
				return item.getRenderer(rendererType, ff, options);
			}
		}
		if (clazz.isArray()) {
			return getCellRenderer(clazz.getComponentType(), ff, rendererType, loader);
		}
		else {
			for (SwingItemRenderer<T,R> item : ServiceLoader.load(SwingItemRenderer.class, loader)) {
				if (item instanceof StringRenderer) {
					return item.getRenderer(rendererType, ff, options);
				}
			}
			throw new EnvironmentException("Renderer type ["+rendererType+"] for class ["+clazz+"] not found"); 
		}
	}

	/**
	 * <p>Get cell editor for different controls. Each editor is an SPI service with {@linkplain SwingItemEditor} implementation </p>
	 * @param <R> editor type ({@linkplain TableCellEditor}, {@linkplain TreeCellEditor})
	 * @param meta model descriptor. Can't be null
	 * @param editorType editor type. Can't be null
	 * @param loader class loader to use SPI in. Can't be null
	 * @return editor found
	 * @throws EnvironmentException editor is missing
	 * @since 0.0.6
	 */
	public static <R> R getCellEditor(final ContentNodeMetadata meta, final Class<R> editorType, final ClassLoader loader) throws EnvironmentException {
		return getCellEditor(meta.getType(), meta.getFormatAssociated(), editorType, loader);
	}	

	/**
	 * <p>Get cell editor for different controls. Each editor is an SPI service with {@linkplain SwingItemEditor} implementation </p>
	 * @param <T> class to edit (for example, {@linkplain String})
	 * @param <R> editor type ({@linkplain TableCellEditor}, {@linkplain TreeCellEditor})
	 * @param clazz class to edit. Can't be null
	 * @param ff field format. Can't be null
	 * @param editorType editor type. Can't be null
	 * @return editor found
	 * @throws EnvironmentException editor is missing
	 * @since 0.0.6
	 */
	public static <T, R> R getCellEditor(final Class<T> clazz, final FieldFormat ff, final Class<R> editorType) throws EnvironmentException {
		return getCellEditor(clazz, ff, editorType, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * <p>Get cell editor for different controls. Each editor is an SPI service with {@linkplain SwingItemEditor} implementation </p>
	 * @param <T> class to edit (for example, {@linkplain String})
	 * @param <R> editor type ({@linkplain TableCellEditor}, {@linkplain TreeCellEditor})
	 * @param clazz class to edit. Can't be null
	 * @param ff field format. Can't be null
	 * @param editorType editor type. Can't be null
	 * @param loader class loader to use SPI in. Can't be null
	 * @return editor found
	 * @throws EnvironmentException editor is missing
	 * @since 0.0.6
	 */
	public static <T, R> R getCellEditor(final Class<T> clazz, final FieldFormat ff, final Class<R> editorType, final ClassLoader loader) throws EnvironmentException {
		for (SwingItemEditor<T,R> item : ServiceLoader.load(SwingItemEditor.class, loader)) {
			if (item.canServe(clazz, editorType, ff)) {
				return item.getEditor(editorType, clazz, ff);
			}
		}
		if (clazz.isArray()) {
			return getCellEditor(clazz.getComponentType(), ff, editorType, loader);
		}
		else {
			for (SwingItemEditor<T,R> item : ServiceLoader.load(SwingItemEditor.class, loader)) {
				if (item instanceof StringEditor) {
					return item.getEditor(editorType, clazz, ff);
				}
			}
			throw new EnvironmentException("Editor type ["+editorType+"] for class ["+clazz+"] not found"); 
		}
	}
	
	/**
	 * <p>Get nearest logger available from component parents.</p>
	 * @param component component to get logger for
	 * @return logger found or standard Pure Library logger if not found
	 * @since 0.0.5
	 * @lastUpdate 0.0.6
	 */
	public static LoggerFacade getNearestLogger(final Component component) {
		return getNearestOwner(component, LoggerFacadeOwner.class, ()->PureLibSettings.CURRENT_LOGGER).getLogger();
	}

	/**
	 * <p>Get nearest owner in the swing tree</p>
	 * @param <T> owner type
	 * @param component current component. Can't be null
	 * @param ownerClass owner class awaited. Can't be null
	 * @return owner found or null if missing
	 * @since 0.0.6
	 */
	public static <T> T getNearestOwner(final Component component, final Class<T> ownerClass) {
		return getNearestOwner(component, ownerClass, null);
	}	
	
	/**
	 * <p>Get nearest owner in the swing tree</p>
	 * @param <T> owner type
	 * @param component current component. Can't be null
	 * @param ownerClass owner class awaited. Can't be null
	 * @param defaultOwner default owner value when not found (can be null)
	 * @return owner found or default owner if missing
	 * @since 0.0.6
	 */
	public static <T> T getNearestOwner(final Component component, final Class<T> ownerClass, final T defaultOwner) {
		if (component == null) {
			throw new NullPointerException("Component can't be null");
		}
		else if (ownerClass == null) {
			throw new NullPointerException("Owner class can't be null");
		}
		else {
			Component	current = component;
			
			while (current != null) {
				if (ownerClass.isAssignableFrom(current.getClass())) {
					return (T)current;
				}
				else {
					current = current.getParent(); 
				}
			}
			return defaultOwner;
		}
	}
	
	/**
	 * <p>Create SprintLayout instance and place children into container with constraints formatted.<p>
	 * @param container container to set {@linkplain SpringLayout} and place children into. Can't be null
	 * @param format format string. Can't be null or empty. Format syntax see {@linkplain #placeContainerBySpringLayout(JComponent, int, int, String, JComponent...)}
	 * @param children children to place into the container. Can't be null or empty array  
	 * @throws NullPointerException on container parameter is null 
	 * @throws IllegalArgumentException format string is null or empty or children is null, empty or contains nulls inside
	 * @throws EnvironmentException on syntax errors inside the format string
	 * @see {@link #placeContainerBySpringLayout(JComponent, int, int, String, JComponent...)}
	 * @since 0.0.7
	 */
	public static void placeContainerBySpringLayout(final JComponent container, final String format, final JComponent... children) throws NullPointerException, IllegalArgumentException, EnvironmentException {
		placeContainerBySpringLayout(container, 5, 5, format, children);
	}

	/**
	 * <p>Create SprintLayout instance and place children into container with constraints formatted.<p>
	 * <p>Constraint format is:</p>
	 * <ul>
	 * <li>&lt;format&gt;::=&lt;part&gt;[';'...]</li>
	 * <li>&lt;part&gt;::=&lt;constraint&gt;[' '...]</li>
	 * <li>&lt;constraint&gt;::=&lt;itemRef&gt;&lt;padding&gt;&lt;itemRef&gt;</li>
	 * <li>&lt;itemRef&gt;::='#'[&lt;number&gt;]{'n'|'s'|'w'|'e'}</li>
	 * <li>&lt;padding&gt;::={'0'|{'+'|'-'}{&lt;number&gt;|'X'|'Y'}}</li>
	 * </ul>
	 * <p>Item ref number is a sequential zero-based number of child in the children list. When the number missing, item ref points to the container self. 'X' and 'Y' are values of xGap and yGap parameters passed.
	 * Semicolon inside the doesn't have any semantics associated and can be used as splitter between parts<p> 	
	 * @param container container to set {@linkplain SpringLayout} and place children into. Can't be null
	 * @param xGap horizontal gaps between items. Refers by 'X' variable inside format string
	 * @param yGap vertical gaps between items. Refers by 'Y' variable inside format string
	 * @param format format string. Can't be null or empty. Format syntax see above
	 * @param children children to place into the container. Can't be null or empty array  
	 * @throws NullPointerException on container parameter is null 
	 * @throws IllegalArgumentException format string is null or empty or children is null, empty or contains nulls inside
	 * @throws EnvironmentException on syntax errors inside the format string
	 * @see {@linkplain #placeContainerBySpringLayout(JComponent, String, JComponent...)}
	 * @since 0.0.7
	 */
	public static void placeContainerBySpringLayout(final JComponent container, final int xGap, final int yGap, final String format, final JComponent... children) throws NullPointerException, IllegalArgumentException, EnvironmentException {
		if (container == null) {
			throw new NullPointerException("Container can't be null"); 
		}
		else if (Utils.checkEmptyOrNullString(format)) {
			throw new IllegalArgumentException("Format string can't be null or empty"); 
		}
		else if (children == null || children.length == 0 || Utils.checkArrayContent4Nulls(children) >= 0) {
			throw new IllegalArgumentException("Children list is null or empty or contains nulls inside"); 
		}
		else {
			final SpringLayout	layout = new SpringLayout();
			final char[]		content = CharUtils.terminateAndConvert2CharArray(format, '\n');
			String				leftConstraint = null;
			JComponent			leftComponent = null;
			int					gap = 0;
			int					forNumber[] = new int[1];
			int					pos = 0, state = 1;
			
			container.setLayout(layout);
			for (JComponent item : children) {
				container.add(item);
			}
			
			try	{
loop:			for (;;) {
					switch (content[pos = CharUtils.skipBlank(content, pos, true)]) {
						case '\n'	:
							break loop;
						case ';'	:
							pos++;
							break;
						case '#'	:
							final JComponent	ref;
							final String		constraint;
							
							pos = CharUtils.skipBlank(content, pos + 1, true);
							if (content[pos] >= '0' && content[pos] <= '9') {
								pos = CharUtils.skipBlank(content, CharUtils.parseInt(content, pos, forNumber, true), true);
								if (forNumber[0] < children.length) {
									ref = children[forNumber[0]];
								}
								else {
									throw new SyntaxException(0, pos, "Reference number ["+forNumber[0]+"] out of range 0.."+(children.length-1));
								}
							}
							else {
								ref = container;
							}
							switch (content[pos]) {
								case 'n' : case 'N'	:
									constraint = SpringLayout.NORTH;
									pos++;
									break;
								case 's' : case 'S'	:
									constraint = SpringLayout.SOUTH;
									pos++;
									break;
								case 'w' : case 'W'	:
									constraint = SpringLayout.WEST;
									pos++;
									break;
								case 'e' : case 'E'	:
									constraint = SpringLayout.EAST;
									pos++;
									break;
								default :
									throw new SyntaxException(0, pos, "Illegal reference modifier ["+content[pos]+"]. Only 'n', 'e', 's', 'w' are valid");
							}
							if (state == 1) {
								leftConstraint = constraint;
								leftComponent = ref;
								state = 2;
							}
							else {
								layout.putConstraint(leftConstraint, leftComponent, gap, constraint, ref);
								state = 1;
							}
							break;
						case '0' :
							gap = 0;
							pos++;
							break;
						case '+' : case '-' :
							final int	multiplier = content[pos] == '-' ? -1 : 1;
							
							pos = CharUtils.skipBlank(content, pos + 1, true);
							if (content[pos] == 'X' || content[pos] == 'x') {
								gap = multiplier * xGap;
								pos++;
							}
							else if (content[pos] == 'Y' || content[pos] == 'y') {
								gap = multiplier * yGap;
								pos++;
							}
							else {
								pos = CharUtils.parseInt(content, pos, forNumber, true);
								gap = multiplier * forNumber[0];
							}
							break;
						default :
							throw new SyntaxException(0, pos, "Unknown symbol"); 
					}
				}
				if (state == 2) {
					throw new SyntaxException(0, pos, "Last constraint: right component is missing"); 
				}
			} catch (SyntaxException e) {
				throw new EnvironmentException(e.getLocalizedMessage(), e); 
			}
		}
	}

	private static JEditorPane buildAboutContent(final Localizer localizer, final String content, final Dimension preferredSize) throws MimeParseException, LocalizationException, IOException {
		try(final Reader	rdr = localizer.getContent(content, MimeType.MIME_CREOLE_TEXT, MimeType.MIME_HTML_TEXT)) {

			return buildAboutContent(URIUtils.convert2selfURI(Utils.fromResource(rdr).toCharArray(), Localizer.LOCALIZER_DEFAULT_ENCODING), preferredSize);
		}
	}	
	
	private static JEditorPane buildAboutContent(final URI content, final Dimension preferredSize) throws MimeParseException, LocalizationException, IOException {
		final JEditorPane 	pane = new JEditorPane(MimeType.MIME_HTML_TEXT.toString(),null);
	
		try(final Reader	rdr = new InputStreamReader(content.toURL().openStream(), PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
			pane.read(rdr,null);
		}
		pane.setEditable(false);
		pane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		pane.setPreferredSize(preferredSize);
		pane.addHyperlinkListener(new HyperlinkListener() {
						@Override
						public void hyperlinkUpdate(final HyperlinkEvent e) {
							if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
								try{Desktop.getDesktop().browse(e.getURL().toURI());
								} catch (URISyntaxException | IOException exc) {
									exc.printStackTrace();
								}
							}
						}
		});
		return pane;
	}
	
	private static void processMenuVisibility(final JComponent component, final UIItemState state) {
		walkDown(component, (mode, node)->{
			if ((mode == NodeEnterMode.ENTER) && (node instanceof NodeMetadataOwner)) {
				switch (state.getItemState(((NodeMetadataOwner)node).getNodeMetadata())) {
					case DEFAULT		: 
						return ContinueMode.CONTINUE;
					case READONLY: case AVAILABLE :
						node.setVisible(true);
						node.setEnabled(true);
						return ContinueMode.CONTINUE;
					case NOTAVAILABLE : case HIDDEN :
						node.setVisible(true);
						node.setEnabled(false);
						return ContinueMode.CONTINUE;
					case NOTVISIBLE		:
						node.setVisible(false);
						node.setEnabled(false);
						return ContinueMode.CONTINUE;
					default : throw new UnsupportedOperationException("Item state ["+state.getItemState(((NodeMetadataOwner)component).getNodeMetadata())+"] is not supported yet");
				}
			}
			return ContinueMode.CONTINUE;
		});
	}
	
	private static class MethodHandleAndAsync {
		final String		signature;
		final MethodHandle	handle;
		final boolean		async;
		
		public MethodHandleAndAsync(final String signature, MethodHandle handle, boolean async) {
			this.signature = signature;
			this.handle = handle;
			this.async = async;
		}
	}

	static void toMenuEntity(final ContentNodeMetadata node, final JMenuBar bar) throws NullPointerException, IllegalArgumentException{
		switch (getNavigationNodeType(node)) {
			case ITEM	:
				bar.add(createMenuItem(node));
				break;
			case SEPARATOR	:
				bar.add(new JSeparator());
				break;
			case SUBMENU	:
				final JMenu	menu = new JMenuWithMeta(node);
				
				for (ContentNodeMetadata child : node) {
					toMenuEntity(child,menu);
				}
				buildRadioButtonGroups(menu);
				bar.add(menu);
				break;
			case BUILTIN_SUBMENU :
				switch (Builtin.forConstant(node.getName())) {
					case BUILTIN_LAF		:
						bar.add(createBuiltinSubmenu(node));
						break;
					case BUILTIN_LANGUAGE	:
						bar.add(createBuiltinSubmenu(node));
						break;
					default:
						throw new UnsupportedOperationException("Builtin type ["+Builtin.forConstant(node.getName())+"] is not supported yet");
				}
				break;
			default : throw new UnsupportedOperationException("Navigation node type ["+getNavigationNodeType(node)+"] is not supported yet");
		}
	}

	static void toMenuEntity(final ContentNodeMetadata node, final JPopupMenu popup) throws NullPointerException, IllegalArgumentException{
		switch (getNavigationNodeType(node)) {
			case ITEM	:
				popup.add(createMenuItem(node));
				break;
			case SEPARATOR	:
				popup.add(new JSeparator());
				break;
			case SUBMENU	:
				final JMenu	submenu = new JMenuWithMeta(node);
				
				for (ContentNodeMetadata child : node) {
					toMenuEntity(child,submenu);
				}
				buildRadioButtonGroups(submenu);
				popup.add(submenu);
				break;
			case BUILTIN_SUBMENU :
				switch (Builtin.forConstant(node.getName())) {
					case BUILTIN_LAF		:
						popup.add(createBuiltinSubmenu(node));
						break;
					case BUILTIN_LANGUAGE	:
						popup.add(createBuiltinSubmenu(node));
						break;
					default:
						throw new UnsupportedOperationException("Builtin type ["+Builtin.forConstant(node.getName())+"] is not supported yet");
				}
				break;
			default : throw new UnsupportedOperationException("Navigation node type ["+getNavigationNodeType(node)+"] is not supported yet");
		}
	}
	
	static void toMenuEntity(final ContentNodeMetadata node, final JMenu menu) throws NullPointerException, IllegalArgumentException{
		switch (getNavigationNodeType(node)) {
			case ITEM	:
				menu.add(createMenuItem(node));
				break;
			case SEPARATOR	:
				menu.add(new JSeparator());
				break;
			case SUBMENU	:
				final JMenu	submenu = new JMenuWithMeta(node);
				
				for (ContentNodeMetadata child : node) {
					toMenuEntity(child,submenu);
				}
				buildRadioButtonGroups(submenu);
				menu.add(submenu);
				break;
			case BUILTIN_SUBMENU :
				switch (Builtin.forConstant(node.getName())) {
					case BUILTIN_LAF		:
						menu.add(createBuiltinSubmenu(node));
						break;
					case BUILTIN_LANGUAGE	:
						menu.add(createBuiltinSubmenu(node));
						break;
					default:
						throw new UnsupportedOperationException("Builtin type ["+Builtin.forConstant(node.getName())+"] is not supported yet");
				}
				break;
			default : throw new UnsupportedOperationException("Navigation node type ["+getNavigationNodeType(node)+"] is not supported yet");
		}
	}

	private static JMenu createBuiltinSubmenu(final ContentNodeMetadata node) {
		final JMenuWithMeta	submenu = new JMenuWithMeta(node);

		switch (Builtin.forConstant(node.getName())) {
			case BUILTIN_LAF	:
				final String		currentLafDesc = UIManager.getLookAndFeel().getName();
				final ButtonGroup	lafGroup = new ButtonGroup();
				
				for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
					try{final String				clazzName = laf.getClassName();
						final Class<?> 				clazz = Class.forName(clazzName);
						final JRadioButtonMenuItem	radio = new JRadioButtonMenuItem(clazz.getSimpleName());
	
						radio.setActionCommand("action:builtin:/"+Constants.MODEL_BUILTIN_LAF+"?laf="+clazzName);
						radio.setToolTipText(laf.getName());
						if (currentLafDesc.equals(laf.getName())) {	// Mark current L&F
							radio.setSelected(true);
						}
						lafGroup.add(radio);
						submenu.add(radio);
					} catch (ClassNotFoundException e) {
					}
				}
				break;
			case BUILTIN_LANGUAGE	:
				final String		currentLang = Locale.getDefault().getLanguage();
				final ButtonGroup	langGroup = new ButtonGroup();
				
				AbstractLocalizer.enumerateLocales((lang, langName, icon)->{
					final String						appPath = submenu.getNodeMetadata().getApplicationPath()+"?lang="+lang.name(); 
					final MutableContentNodeMetadata	md = new MutableContentNodeMetadata(langName, String.class, "./"+langName, PureLibLocalizer.LOCALIZER_SCHEME_URI, lang.name(), lang.name()+".tt", null, null, URI.create(appPath), lang.getIconURI()) 
																{{setOwner(node.getOwner());}};
					final JRadioMenuItemWithMeta		radio = new JRadioMenuItemWithMeta(md);
						
					if (currentLang.equals(lang.toString())) {	// Mark current lang
						radio.setSelected(true);
					}
					langGroup.add(radio);
					submenu.add(radio);
				});
				break;
			default : throw new UnsupportedOperationException("Built-in name ["+node.getName()+"] is not suported yet");
		}
		return submenu; 
	}

	private static JMenuItem createMenuItem(final ContentNodeMetadata node) {
		if (node.getApplicationPath().getFragment() != null) {
			return new JRadioMenuItemWithMeta(node);
		}
		else {
			if (node.getApplicationPath() != null) {
				final String	query = URIUtils.extractQueryFromURI(node.getApplicationPath());
				
				if (query != null && !query.isEmpty()) {
					final Hashtable<String, String[]>	queryItems = URIUtils.parseQuery(query);
					
					if (queryItems.containsKey("checkable")) {
						final JCheckBoxMenuItemWithMeta	miwm = new JCheckBoxMenuItemWithMeta(node); 
						
						if (queryItems.containsKey("checked")) {
							miwm.setSelected(Boolean.valueOf(queryItems.get("checked")[0]));
						}
						return miwm;
					}
					else {
						return new JMenuItemWithMeta(node);
					}
				}
				else {
					return new JMenuItemWithMeta(node);
				}
			}
			else {
				return new JMenuItemWithMeta(node);
			}
		}
	}	
	
	private static void buildRadioButtonGroups(final JMenu menu) {
		final Set<String>	availableGroups = new HashSet<>(); 
		
		for (int index = 0, maxIndex = menu.getMenuComponentCount(); index < maxIndex; index++) {
			if (menu.getMenuComponent(index) instanceof JRadioMenuItemWithMeta) {
				availableGroups.add(((JRadioMenuItemWithMeta)menu.getMenuComponent(index)).getRadioGroup());
			}
		}
		if (availableGroups.size() > 0) {
			for (String group : availableGroups) {
				final ButtonGroup 	buttonGroup = new ButtonGroup();
				ButtonModel			buttonModel = null;

				for (int index = 0, maxIndex = menu.getMenuComponentCount(); index < maxIndex; index++) {
					Component	c = menu.getMenuComponent(index); 
					
					if ((c instanceof JRadioMenuItemWithMeta) && group.equals(((JRadioMenuItemWithMeta)c).getRadioGroup())) {
						buttonGroup.add((JRadioMenuItemWithMeta)c);
						if (buttonModel == null) {
							buttonModel = ((JRadioMenuItemWithMeta)c).getModel();
						}
					}
				}
				if (buttonModel != null) {
					buttonGroup.setSelected(buttonModel,true);
				}
			}
		}				
	}

	static void buildRadioButtonGroups(final JPopupMenu menu) {
		final Set<String>	availableGroups = new HashSet<>(); 
		
		for (int index = 0, maxIndex = menu.getComponentCount(); index < maxIndex; index++) {
			if (menu.getComponent(index) instanceof JRadioMenuItemWithMeta) {
				availableGroups.add(((JRadioMenuItemWithMeta)menu.getComponent(index)).getRadioGroup());
			}
		}
		if (availableGroups.size() > 0) {
			for (String group : availableGroups) {
				final ButtonGroup 	buttonGroup = new ButtonGroup();
				ButtonModel			buttonModel = null;

				for (int index = 0, maxIndex = menu.getComponentCount(); index < maxIndex; index++) {
					Component	c = menu.getComponent(index); 
					
					if ((c instanceof JRadioMenuItemWithMeta) && group.equals(((JRadioMenuItemWithMeta)c).getRadioGroup())) {
						buttonGroup.add((JRadioMenuItemWithMeta)c);
						if (buttonModel == null) {
							buttonModel = ((JRadioMenuItemWithMeta)c).getModel();
						}
					}
				}
				if (buttonModel != null) {
					buttonGroup.setSelected(buttonModel,true);
				}
			}
		}				
	}
	
	private static NavigationNodeType getNavigationNodeType(final ContentNodeMetadata node) {
		if (node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_NODE_PREFIX)) {
			if (node.getApplicationPath() != null && node.getApplicationPath().toString().contains(Constants.MODEL_APPLICATION_SCHEME_BUILTIN_ACTION)) {
				return NavigationNodeType.BUILTIN_SUBMENU;
			}
			else {
				return NavigationNodeType.SUBMENU;
			}
		}
		else if (node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_LEAF_PREFIX)) {
			return NavigationNodeType.ITEM;
		}
		else if (node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_SEPARATOR)) {
			return NavigationNodeType.SEPARATOR;
		}
		else {
			return NavigationNodeType.UNKNOWN;
		}
	}
	
	static boolean inAllowedClasses(final Object val, final Class<?>... classes) {
		if (val == null) {
			return false;
		}
		else {
			final Class<?>	clazz = val.getClass();
			
			for (Class<?> cl : classes) {
				if (cl.isAssignableFrom(clazz)) {
					return true;
				}
			}
			return false;
		}
	}

	static ImageIcon extractIcon(final URI iconURI) throws IOException {
		final URL		url = URIUtils.truncateQueryFromURI(iconURI).toURL();
		final Image		image = ImageIO.read(url);
		final String	query = URIUtils.extractQueryFromURI(iconURI);
		
		if (query != null) {
			final Hashtable<String, String[]> 	q = URIUtils.parseQuery(query);
			
			if (q.containsKey("size")) {
				final String[]	parts = q.get("size")[0].split("\\*");
				
				if (parts.length == 2) {
					try{return new ImageIcon(image.getScaledInstance(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]), Image.SCALE_DEFAULT));
					} catch (NumberFormatException exc) {
						return new ImageIcon(image);
					}
				}
				else {
					return new ImageIcon(image);
				}
			}
			else {
				return new ImageIcon(image);
			}
		}
		else {
			return new ImageIcon(image);
		}
	}
	
	static class JMenuBarWithMeta extends JMenuBar implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = 2873312186080690483L;
		
		private final ContentNodeMetadata	metadata;
		private final UIItemState			state;
		
		protected JMenuBarWithMeta(final ContentNodeMetadata metadata, final UIItemState state) {
			this.metadata = metadata;
			this.state = state;
			this.setName(metadata.getName());
			fillLocalizedStrings();
		}

		@Override
		public ContentNodeMetadata getNodeMetadata() {
			return metadata;
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			fillLocalizedStrings();
			for (int index = 0, maxIndex = this.getMenuCount(); index < maxIndex; index++) {
				final JMenu	item = this.getMenu(index);
				
				if (item instanceof LocaleChangeListener) {
					((LocaleChangeListener)item).localeChanged(oldLocale, newLocale);
				}
			}
		}
		
		@Override
		public void setVisible(final boolean visible) {
			if (visible) {
				SwingUtils.walkDown(this, (mode, node) ->{
					if (mode == NodeEnterMode.ENTER) {
						if (node instanceof NodeMetadataOwner) {
							switch (state.getItemState(((NodeMetadataOwner)node).getNodeMetadata())) {
								case DEFAULT : case AVAILABLE : case READONLY :  
									node.setVisible(true);
									node.setEnabled(true);
									return ContinueMode.CONTINUE;
								case NOTAVAILABLE : case HIDDEN :
									node.setVisible(true);
									node.setEnabled(false);
									return ContinueMode.SKIP_CHILDREN;
								case NOTVISIBLE		:
									node.setVisible(false);
									node.setEnabled(false);
									return ContinueMode.SKIP_CHILDREN;
								default : throw new UnsupportedOperationException("Available type ["+state.getItemState(((NodeMetadataOwner)node).getNodeMetadata())+"] is not supported yet");
							}
						}
					}
					return ContinueMode.CONTINUE;
				});
			}
			super.setVisible(visible);
		}

		private void fillLocalizedStrings() throws LocalizationException {
			final String	ttId = getNodeMetadata().getTooltipId();
			
			if (ttId != null && !ttId.isEmpty()) {
				setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(ttId));
			}
		}
	}

	private static class JMenuItemWithMeta extends JMenuItem implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = -1731094524456032387L;

		private final ContentNodeMetadata	metadata;
		
		private JMenuItemWithMeta(final ContentNodeMetadata metadata) {
			this.metadata = metadata;
			this.setName(metadata.getName());
			this.setActionCommand(metadata.getApplicationPath().getSchemeSpecificPart());
			for (ContentNodeMetadata item : metadata.getOwner().byApplicationPath(metadata.getApplicationPath())) {
				if (item.getRelativeUIPath().toString().startsWith("./keyset.key")) {
					this.setAccelerator(KeyStroke.getKeyStroke(item.getLabelId()));
					break;
				}
			}
			
			fillLocalizedStrings();
			try{
				if (metadata.getIcon() != null) {
					this.setIcon(extractIcon(metadata.getIcon()));
				}
			} catch (IOException | LocalizationException e) {
				e.printStackTrace();
			}
		}

		@Override
		public ContentNodeMetadata getNodeMetadata() {
			return metadata;
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			fillLocalizedStrings();
		}

		private void fillLocalizedStrings() throws LocalizationException {
			setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
			if (getNodeMetadata().getTooltipId() != null) {
				setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
			}
		}
	}

	private static class JCheckBoxMenuItemWithMeta extends JCheckBoxMenuItem implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = -1731094524456032387L;

		private final ContentNodeMetadata	metadata;
		
		private JCheckBoxMenuItemWithMeta(final ContentNodeMetadata metadata) {
			this.metadata = metadata;
			this.setName(metadata.getName());
			this.setActionCommand(metadata.getApplicationPath().getSchemeSpecificPart());
			for (ContentNodeMetadata item : metadata.getOwner().byApplicationPath(metadata.getApplicationPath())) {
				if (item.getRelativeUIPath().toString().startsWith("./keyset.key")) {
					this.setAccelerator(KeyStroke.getKeyStroke(item.getLabelId()));
					break;
				}
			}
			
			try{
				fillLocalizedStrings();
				if (metadata.getIcon() != null) {
					this.setIcon(extractIcon(metadata.getIcon()));
				}
			} catch (IOException | LocalizationException e) {
				e.printStackTrace();
			}
		}

		@Override
		public ContentNodeMetadata getNodeMetadata() {
			return metadata;
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			try{fillLocalizedStrings();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void fillLocalizedStrings() throws LocalizationException, IOException {
			setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
			if (getNodeMetadata().getTooltipId() != null) {
				setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
			}
		}
	}
	
	private static class JRadioMenuItemWithMeta extends JRadioButtonMenuItem implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = -1731094524456032387L;

		private final ContentNodeMetadata	metadata;
		private final String				radioGroup;
		
		private JRadioMenuItemWithMeta(final ContentNodeMetadata metadata) {
			this.metadata = metadata;
			this.radioGroup = metadata.getApplicationPath().getFragment();
			this.setName(metadata.getName());
			this.setActionCommand(metadata.getApplicationPath().getSchemeSpecificPart());
			for (ContentNodeMetadata item : metadata.getOwner().byApplicationPath(metadata.getApplicationPath())) {
				if (item.getRelativeUIPath().toString().startsWith("./keyset.key")) {
					this.setAccelerator(KeyStroke.getKeyStroke(item.getLabelId()));
					break;
				}
			}
			try{
				fillLocalizedStrings();
				if (metadata.getIcon() != null) {
					this.setIcon(extractIcon(metadata.getIcon()));
				}
			} catch (IOException | LocalizationException e) {
				e.printStackTrace();
			}
		}

		@Override
		public ContentNodeMetadata getNodeMetadata() {
			return metadata;
		}

		public String getRadioGroup() {
			return radioGroup;
		}
		
		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			try{fillLocalizedStrings();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void fillLocalizedStrings() throws LocalizationException, IOException {
			setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
			if (getNodeMetadata().getTooltipId() != null) {
				setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
			}
		}
	}
	
	
	private static class JMenuWithMeta extends JMenu implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = 366031204608808220L;
		
		private final ContentNodeMetadata	metadata;
		
		private JMenuWithMeta(final ContentNodeMetadata metadata) {
			this.metadata = metadata;
			this.setName(metadata.getName());
			
			try{
				fillLocalizedStrings();
				if (metadata.getIcon() != null) {
					this.setIcon(extractIcon(metadata.getIcon()));
				}
			} catch (IOException | LocalizationException e) {
				e.printStackTrace();
			}
		}

		@Override
		public ContentNodeMetadata getNodeMetadata() {
			return metadata;
		}
		
		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			try{fillLocalizedStrings();
				for (int index = 0, maxIndex = this.getMenuComponentCount(); index < maxIndex; index++) {
					final Component	item = this.getMenuComponent(index);
					
					if (item instanceof LocaleChangeListener) {
						((LocaleChangeListener)item).localeChanged(oldLocale, newLocale);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		void processActionEvent(final String actionString) {
			for(ActionListener item : listenerList.getListeners(ActionListener.class)) {
				item.actionPerformed(new ActionEvent(JMenuWithMeta.this, 0, actionString));
			}
		}
		
		private void fillLocalizedStrings() throws LocalizationException, IOException {
			setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
			if (getNodeMetadata().getTooltipId() != null) {
				setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
			}
		}
	}
}
	
