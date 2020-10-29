package chav1961.purelib.ui.swing;





import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.AbstractLocalizer;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.ModelUtils;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.streams.StreamsUtil;
import chav1961.purelib.streams.char2byte.CompilerUtils;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.LocalizedFormatter;

/**
 * <p>This utility class contains a set of useful methods to use in the Swing-based applications.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.4
 */

public abstract class SwingUtils {
	public static final Border				TABLE_CELL_BORDER = new LineBorder(Color.BLACK,1);
	public static final Border				TABLE_HEADER_BORDER = new LineBorder(Color.BLACK,1);	
	public static final Border				FOCUSED_TABLE_CELL_BORDER = new LineBorder(Color.BLUE,1);
	
	public static final KeyStroke			KS_BACKWARD = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK);
	public static final KeyStroke			KS_FORWARD = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK);
	public static final KeyStroke			KS_HELP = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
	public static final KeyStroke			KS_ACCEPT = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	public static final KeyStroke			KS_DELETE = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
	public static final KeyStroke			KS_EXIT = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	public static final KeyStroke			KS_DROPDOWN = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK);
	public static final KeyStroke			KS_CLOSE = KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK);
	
	public static final String				ACTION_FORWARD = "forward";
	public static final String				ACTION_BACKWARD = "backward";
	public static final String				ACTION_HELP = "help";
	public static final String				ACTION_ACCEPT = "accept";
	public static final String				ACTION_EXIT = "exit";
	
	private static final Map<Class<?>,Object>	DEFAULT_VALUES = new HashMap<>();

	private static final String				UNKNOWN_ACTION_TITLE = "SwingUtils.unknownAction.title";
	private static final String				UNKNOWN_ACTION_CONTENT = "SwingUtils.unknownAction.content";
	private static final URI				MODEL_REF_URI = URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_REF+":/");
	
	
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
					result = new JTextFieldWithMeta(metadata,monitor);
					break;
				case URIContent		:
					result = new JTextFieldWithMeta(metadata,monitor);
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
				case Unclassified	:
				case ArrayContent	:
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
	@SuppressWarnings("unchecked")
	public static <T extends JComponent> T toJComponent(final ContentNodeMetadata node, final Class<T> awaited) throws NullPointerException, IllegalArgumentException{
		if (node == null) {
			throw new NullPointerException("Model node can't be null"); 
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null"); 
		}
		else {
			if (awaited.isAssignableFrom(JMenuBar.class)) {
				if (!node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_TOP_PREFIX)) {
					throw new IllegalArgumentException("Model node ["+node.getUIPath()+"] can't be converted to ["+awaited.getCanonicalName()+"] class"); 
				}
				else {
					final JMenuBar	result = new JMenuBarWithMeta(node);
					
					for (ContentNodeMetadata child : node) {
						toMenuEntity(child,result);
					}
					return (T) result;
				}
			}
			else if (awaited.isAssignableFrom(JPopupMenu.class)) {
				if (!node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_TOP_PREFIX)) {
					throw new IllegalArgumentException("Model node ["+node.getUIPath()+"] can't be converted to ["+awaited.getCanonicalName()+"] class"); 
				}
				else {
					final JPopupMenu	result = new JMenuPopupWithMeta(node);
					
					for (ContentNodeMetadata child : node) {
						toMenuEntity(child,result);
					}
					return (T) result;
				}
			}
			else if (awaited.isAssignableFrom(JToolBar.class)) {
				if (!node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_TOP_PREFIX)) {
					throw new IllegalArgumentException("Model node ["+node.getUIPath()+"] can't be converted to ["+awaited.getCanonicalName()+"] class"); 
				}
				else {
					final JToolBar	result = new JToolBarWithMeta(node);
					
					for (ContentNodeMetadata child : node) {
						if (child.getRelativeUIPath().toString().startsWith("./"+Constants.MODEL_NAVIGATION_NODE_PREFIX)) {
							final JMenuPopupWithMeta	menu = new JMenuPopupWithMeta(child);
							final JButton 				btn = new JButtonWithMetaAndActions(child,JButtonWithMeta.LAFType.ICON_THEN_TEXT,menu);					
							
							for (ContentNodeMetadata item : child) {
								toMenuEntity(item,menu);
							}
							
							btn.addActionListener((e)->{
								menu.show(btn,btn.getWidth()/2,btn.getHeight()/2);
							});
							result.add(btn);
						}
						else if (child.getRelativeUIPath().toString().startsWith("./"+Constants.MODEL_NAVIGATION_LEAF_PREFIX)) {
							result.add(new JButtonWithMeta(child,JButtonWithMeta.LAFType.ICON_THEN_TEXT));
						}
						else if (URI.create("./navigation.separator").equals(child.getRelativeUIPath())) {
							result.addSeparator();
						}
					}
					return (T) result;
				}
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
		centerMainWindow(dialog,0.5f);
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
	 * <p>Prepare action listeners to call methods marked with {@linkplain OnAction} annotation</p>
	 * @param root root component to assign listeners to
	 * @param entity object to call it's annotated methods 
	 */
	public static void assignActionListeners(final JComponent root, final Object entity) {
		assignActionListeners(root,entity,(action)->{sayAboutUnknownAction(null,action);});
	}

	/**
	 * <p>Prepare action listeners to call methods marked with {@linkplain OnAction} annotation</p>
	 * @param root root component to assign listeners to
	 * @param entity object to call it's annotated methods 
	 * @param onUnknown callback to process missing actions
	 */
	public static void assignActionListeners(final JComponent root, final Object entity, final FailedActionListenerCallback onUnknown) {
		if (root == null) {
			throw new NullPointerException("Root component can't be null"); 
		}
		else if (entity == null) {
			throw new NullPointerException("Entity class can't be null"); 
		}
		else {
			assignActionListeners(root,buildAnnotatedActionListener(entity, onUnknown));
		}
	}
	
	/**
	 * <p>Prepare action listeners to call {@linkplain FormManager#onAction(Object, Object, String, Object)} method</p>
	 * @param <T> any entity is used for form manager
	 * @param root component with action sources inside
	 * @param entity instance to call onAction for
	 * @param manager form manager to process action on entity
	 * @since 0.0.4 
	 */
	public static <T> void assignActionListeners(final JComponent root, final T entity, final FormManager<?,T> manager) {
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
			assignActionListeners(root,(e)->{
				try{manager.onAction(entity,null,e.getActionCommand(),null);
				} catch (LocalizationException | FlowException exc) {
				}
			});
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
		return buildAnnotatedActionListener(entity, onUnknown, PureLibSettings.CURRENT_LOGGER);
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
														logger.message(Severity.error, exc, exc.getLocalizedMessage());
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
	
	private static void assignActionListeners(final JComponent root, final ActionListener listener) {
		walkDownInternal(root,(mode,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if ((node instanceof NodeMetadataOwner) && ((NodeMetadataOwner)node).getNodeMetadata().getApplicationPath() != null && (URIUtils.canServeURI(((NodeMetadataOwner)node).getNodeMetadata().getApplicationPath(),MODEL_REF_URI))) {
					return ContinueMode.CONTINUE;
				}
				else {
					try{node.getClass().getMethod("addActionListener",ActionListener.class).invoke(node,listener);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
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
	 * @param helpContent help reference
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException when any parameter is null
	 */
	public static void showCreoleHelpWindow(final Component owner, final URI helpContent) throws IOException, NullPointerException {
		if (owner == null) {
			throw new NullPointerException("Window owner can't be null");
		}
		else if (helpContent == null) {
			throw new NullPointerException("Help content refrrence can't be null");
		}
		else {
			final PopupFactory		pf = PopupFactory.getSharedInstance();
			final JEditorPane		pane = new JEditorPane("text/html","");
			final JScrollPane		scroll = new JScrollPane(pane);
			final Dimension			ownerSize = owner.getSize(), helpSize = new Dimension(Math.max(200,ownerSize.width),Math.max(ownerSize.height,300)); 
			final Point				point = new Point((ownerSize.width-helpSize.width)/2,(ownerSize.height-helpSize.height)/2);
			final HyperlinkListener	hll = (e)->{
										if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
											Utils.startBrowser(e.getURL());
										}
									}; 
			
			pane.setEditable(false);
			scroll.setPreferredSize(helpSize);
			SwingUtilities.convertPointToScreen(point,owner);
			
			final Popup				popup = pf.getPopup(owner,scroll,point.x,point.y);
			
			pane.setText(StreamsUtil.loadCreoleContent(helpContent.toURL(),MarkupOutputFormat.XML2HTML));
			assignActionKey(pane,JPanel.WHEN_IN_FOCUSED_WINDOW,SwingUtils.KS_EXIT,(e)->{
				popup.hide();
				pane.removeHyperlinkListener(hll);
				removeActionKey(pane,JPanel.WHEN_IN_FOCUSED_WINDOW,SwingUtils.KS_EXIT,SwingUtils.ACTION_EXIT);
			},SwingUtils.ACTION_EXIT);
			pane.addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				@Override public void mouseClicked(MouseEvent e) {}
				
				@Override
				public void mouseExited(MouseEvent e) {
					popup.hide();
					pane.removeHyperlinkListener(hll);
					removeActionKey(pane,JPanel.WHEN_IN_FOCUSED_WINDOW,SwingUtils.KS_EXIT,SwingUtils.ACTION_EXIT);
					pane.removeMouseListener(this);
				}
			});
			pane.addHyperlinkListener(hll);
			
			popup.show();
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
		if (node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_NODE_PREFIX)) {
			final JMenu	menu = new JMenuWithMeta(node);
			
			for (ContentNodeMetadata child : node) {
				toMenuEntity(child,menu);
			}
			buildRadioButtonGroups(menu);
			bar.add(menu);
		} 
		else if (node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_LEAF_PREFIX)) {
			bar.add(new JMenuItemWithMeta(node));
		}
	}

	static void toMenuEntity(final ContentNodeMetadata node, final JPopupMenu popup) throws NullPointerException, IllegalArgumentException{
		if (node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_NODE_PREFIX)) {
			final JMenu	menu = new JMenuWithMeta(node);
			
			for (ContentNodeMetadata child : node) {
				toMenuEntity(child,menu);
			}
			buildRadioButtonGroups(menu);
			popup.add(menu);
		} 
		else if (node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_LEAF_PREFIX)) {
			popup.add(new JMenuItemWithMeta(node));
		}
		else if (node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_SEPARATOR)) {
			popup.add(new JSeparator());
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

	static void toMenuEntity(final ContentNodeMetadata node, final JMenu menu) throws NullPointerException, IllegalArgumentException{
		if (node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_NODE_PREFIX)) {
			final JMenu	submenu = new JMenuWithMeta(node);
			
			if (node.getApplicationPath() != null && node.getApplicationPath().toString().contains(Constants.MODEL_APPLICATION_SCHEME_BUILTIN_ACTION)) {
				switch (node.getName()) {
					case Constants.MODEL_BUILTIN_LANGUAGE	:
						final String		currentLang = Locale.getDefault().getLanguage();
						final ButtonGroup	langGroup = new ButtonGroup();
						
						AbstractLocalizer.enumerateLocales((lang,langName,icon)->{
							final JRadioButtonMenuItem	radio = new JRadioButtonMenuItem(langName,icon);
							
							radio.setActionCommand("action:/"+Constants.MODEL_BUILTIN_LANGUAGE+"?lang="+lang.name());
							if (currentLang.equals(lang.toString())) {	// Mark current lang
								radio.setSelected(true);
							}
							langGroup.add(radio);
							submenu.add(radio);
						});
						menu.add(submenu);
						break;
					case Constants.MODEL_BUILTIN_LAF	:
						final String		currentLafDesc = UIManager.getLookAndFeel().getName();
						final ButtonGroup	lafGroup = new ButtonGroup();
						
						for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
							try{final String				clazzName = laf.getClassName();
								final Class<?> 				clazz = Class.forName(clazzName);
								final JRadioButtonMenuItem	radio = new JRadioButtonMenuItem(clazz.getSimpleName());

								radio.setActionCommand("action:/"+Constants.MODEL_BUILTIN_LAF+"?laf="+clazzName);
								radio.setToolTipText(laf.getName());
								if (currentLafDesc.equals(laf.getName())) {	// Mark current L&F
									radio.setSelected(true);
								}
								lafGroup.add(radio);
								submenu.add(radio);
							} catch (ClassNotFoundException e) {
							}
						}
						menu.add(submenu);
						break;
					default : throw new UnsupportedOperationException("Built-in name ["+node.getName()+"] is not suported yet");
				}
			}
			else {
				for (ContentNodeMetadata child : node) {
					toMenuEntity(child,submenu);
				}
				buildRadioButtonGroups(submenu);
				menu.add(submenu);
			}
		} 
		else if (node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_LEAF_PREFIX)) {
			if (node.getApplicationPath().getFragment() != null) {
				final JRadioMenuItemWithMeta	item = new JRadioMenuItemWithMeta(node);
				
				menu.add(item);
			}
			else {
				final JMenuItemWithMeta			item = new JMenuItemWithMeta(node);
				
				menu.add(item);
			}
		}
		else if (node.getRelativeUIPath().getPath().startsWith("./"+Constants.MODEL_NAVIGATION_SEPARATOR)) {
			menu.add(new JSeparator());
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
	
	static class JMenuBarWithMeta extends JMenuBar implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = 2873312186080690483L;
		
		private final ContentNodeMetadata	metadata;
		
		protected JMenuBarWithMeta(final ContentNodeMetadata metadata) {
			this.metadata = metadata;
			this.setName(metadata.getName());
			try{fillLocalizedStrings();
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
				for (int index = 0, maxIndex = this.getMenuCount(); index < maxIndex; index++) {
					final JMenu	item = this.getMenu(index);
					
					if (item instanceof LocaleChangeListener) {
						((LocaleChangeListener)item).localeChanged(oldLocale, newLocale);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void fillLocalizedStrings() throws LocalizationException, IOException {
			final String	ttId = getNodeMetadata().getTooltipId();
			
			if (ttId != null && !ttId.isEmpty()) {
				setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(ttId));
			}
		}
	}

	private static class JMenuPopupWithMeta extends JPopupMenu implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = 2873312186080690483L;
		
		private final ContentNodeMetadata	metadata;
		
		private JMenuPopupWithMeta(final ContentNodeMetadata metadata) {
			this.metadata = metadata;
			this.setName(metadata.getName());
			try{fillLocalizedStrings();
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
				for (int index = 0, maxIndex = this.getComponentCount(); index < maxIndex; index++) {
					final Component	item = this.getComponent(index);
					
					if (item instanceof LocaleChangeListener) {
						((LocaleChangeListener)item).localeChanged(oldLocale, newLocale);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void show(final Component invoker, final int x, final int y) {
			final Map<String,Container>	map = new HashMap<>();
			final Window				frame = SwingUtilities.getWindowAncestor(invoker);
			
			getNodeMetadata().getOwner().walkDown((mode, applicationPath, uiPath, node) -> {
				if (mode == NodeEnterMode.ENTER && applicationPath != null && URIUtils.canServeURI(applicationPath, MODEL_REF_URI)) {
					final Container		item = SwingUtils.findComponentByName(this,node.getName());
					final Container		ref = SwingUtils.findComponentByName(frame,node.getLabelId());
					
					if ((item instanceof JMenuItem) && (ref instanceof JMenuItem)) {
						((JMenuItem)item).setText(((JMenuItem)ref).getText());
						((JMenuItem)item).setIcon(((JMenuItem)ref).getIcon());
						((JMenuItem)item).setToolTipText(((JMenuItem)ref).getToolTipText());
						((JMenuItem)item).setEnabled(((JMenuItem)ref).isEnabled());
						((JMenuItem)item).addActionListener((e)->((JMenuItem)ref).doClick());
					}
					else if ((item instanceof JMenu) && (ref instanceof JMenu)) {
						((JMenu)item).setText(((JMenu)ref).getText());
						((JMenu)item).setIcon(((JMenu)ref).getIcon());
						((JMenu)item).setToolTipText(((JMenu)ref).getToolTipText());
						((JMenu)item).setEnabled(((JMenu)ref).isEnabled());
					}
				}
				return ContinueMode.CONTINUE;
			},getNodeMetadata().getUIPath());
			super.show(invoker, x, y);
		}
		
		private void fillLocalizedStrings() throws LocalizationException, IOException {
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
			try{fillLocalizedStrings();
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
			try{fillLocalizedStrings();
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
			try{fillLocalizedStrings();
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

		private void fillLocalizedStrings() throws LocalizationException, IOException {
			setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
			if (getNodeMetadata().getTooltipId() != null) {
				setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
			}
		}
	}

	private static class JToolBarWithMeta extends JToolBar implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = 366031204608808220L;
		
		private final ContentNodeMetadata	metadata;
		
		private JToolBarWithMeta(final ContentNodeMetadata metadata) {
			this.metadata = metadata;
			this.setName(metadata.getName());
			try{fillLocalizedStrings();
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
			if (getNodeMetadata().getTooltipId() != null) {
				setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
			}
		}
	}

	private static class JButtonWithMeta extends JButton implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = 366031204608808220L;
		
		protected enum LAFType {
			TEXT_ONLY, ICON_INLY, BOTH, ICON_THEN_TEXT
		}
		
		private final ContentNodeMetadata	metadata;
		private final LAFType				type;

		private JButtonWithMeta(final ContentNodeMetadata metadata) {
			this(metadata,LAFType.BOTH);
		}		
		
		private JButtonWithMeta(final ContentNodeMetadata metadata, final LAFType type) {
			this.metadata = metadata;
			this.type = type;
			this.setName(metadata.getName());
			this.setActionCommand(metadata.getApplicationPath() != null ? metadata.getApplicationPath().getSchemeSpecificPart() : "action:/"+metadata.getName());
			try{fillLocalizedStrings();
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
			if (getNodeMetadata().getTooltipId() != null) {
				setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
			}
			switch (type) {
				case BOTH			:
					if (getNodeMetadata().getIcon() != null) {
						setIcon(new ImageIcon(getNodeMetadata().getIcon().toURL()));
					}
					setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
					break;
				case ICON_INLY		:
					if (getNodeMetadata().getIcon() != null) {
						setIcon(new ImageIcon(getNodeMetadata().getIcon().toURL()));
					}
					break;
				case ICON_THEN_TEXT	:
					if (getNodeMetadata().getIcon() != null) {
						setIcon(new ImageIcon(getNodeMetadata().getIcon().toURL()));
						break;
					}
					// break doesn't need!
				case TEXT_ONLY		:
					setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
					break;
				default:
					throw new UnsupportedOperationException("LAF type ["+type+"] is not supported yet"); 
			}
		}
	}

	private static class JButtonWithMetaAndActions extends JButtonWithMeta implements InnerActionNode {
		private static final long serialVersionUID = 366031204608808220L;
		
		private final JComponent[]	actionable;

		private JButtonWithMetaAndActions(final ContentNodeMetadata metadata, final JComponent... actionable) {
			super(metadata);
			this.actionable = actionable;
		}
		
		
		private JButtonWithMetaAndActions(final ContentNodeMetadata metadata, final LAFType type, final JComponent... actionable) {
			super(metadata,type);
			this.actionable = actionable;
		}

		@Override
		public JComponent[] getActionNodes() {
			return actionable;
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			super.localeChanged(oldLocale, newLocale);
			for (JComponent item : getActionNodes()) {
				SwingUtils.refreshLocale(item,oldLocale, newLocale);
			}
		}
	}
}
	
