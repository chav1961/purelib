package chav1961.purelib.ui.swing;




import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.char2byte.asm.CompilerUtils;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.OnAction;

/**
 * <p>This utility class contains a set of useful methods to use in the Swing-based applications.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */

public abstract class SwingUtils {
	public static final Color				MANDATORY_BACKGROUND = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_MANDATORY_BACKGROUND,Color.class,"LightCyan3");
	public static final Color				MANDATORY_FOREGROUND = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_MANDATORY_FOREGROUND,Color.class,"blue");
	public static final Color				MANDATORY_FOREGROUND_NEGATIVE = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_MANDATORY_FOREGROUND_NEGATIVE,Color.class,"red");
	public static final Color				MANDATORY_FOREGROUND_ZERO = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_MANDATORY_FOREGROUND_ZERO,Color.class,"blue");
	public static final Color				MANDATORY_FOREGROUND_POSITIVE = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_MANDATORY_FOREGROUND_POSITIVE,Color.class,"green");
	public static final Color				MANDATORY_SELECTION_BACKGROUND = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_MANDATORY_SELECTED,Color.class,"blue");
	public static final Color				MANDATORY_SELECTION_FOREGROUND = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_MANDATORY_SELECTED_TEXT,Color.class,"white");
	public static final Color				OPTIONAL_BACKGROUND = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_OPTIONAL_BACKGROUND,Color.class,"white");
	public static final Color				OPTIONAL_FOREGROUND = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_OPTIONAL_FOREGROUND,Color.class,"black");
	public static final Color				OPTIONAL_FOREGROUND_NEGATIVE = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_MANDATORY_FOREGROUND_NEGATIVE,Color.class,"red");
	public static final Color				OPTIONAL_FOREGROUND_ZERO = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_MANDATORY_FOREGROUND_ZERO,Color.class,"blue");
	public static final Color				OPTIONAL_FOREGROUND_POSITIVE = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_MANDATORY_FOREGROUND_POSITIVE,Color.class,"green");
	public static final Color				OPTIONAL_SELECTION_BACKGROUND = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_MANDATORY_SELECTED,Color.class,"blue");
	public static final Color				OPTIONAL_SELECTION_FOREGROUND = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_MANDATORY_SELECTED_TEXT,Color.class,"white");
	public static final Color				READONLY_BACKGROUND = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_READONLY_BACKGROUND,Color.class,"LightGray");
	public static final Color				READONLY_FOREGROUND = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_READONLY_FOREGROUND,Color.class,"black");

	public static final Color				NEGATIVEMARK_FOREGROUND = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_NEGATIVEMARK_FOREGROUND,Color.class,"red");
	public static final Color				POSITIVEMARK_FOREGROUND = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_POSITIVEMARK_FOREGROUND,Color.class,"green");
	public static final Color				ZEROMARK_FOREGROUND = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_ZEROMARK_FOREGROUND,Color.class,"brown");
	
	public static final Color				DATEPICKER_DAY_NAME_COLOR = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_DATEPICKER_DAY_NAME_COLOR,Color.class,"blue"); 
	public static final Color				DATEPICKER_WEEKEND_NAME_COLOR = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_DATEPICKER_WEEKEND_NAME_COLOR,Color.class,"red");
	public static final Color				DATEPICKER_DAY_VALUE_COLOR = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_DATEPICKER_DAY_VALUE_COLOR,Color.class,"black");
	public static final Color				DATEPICKER_WEEKEND_VALUE_COLOR = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_DATEPICKER_WEEKEND_VALUE_COLOR,Color.class,"red");
			
	public static final Border				TABLE_CELL_BORDER = new LineBorder(Color.BLACK,1);
	public static final Border				TABLE_HEADER_BORDER = new LineBorder(Color.BLACK,1);	
	public static final Border				FOCUSED_TABLE_CELL_BORDER = new LineBorder(Color.BLUE,1);
	
	public static final Color				TOOLTIP_BORDER_COLOR = PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_TOOLTIP_BORDER_COLOR,Color.class,"IndianRed");
	
	public static final Color				SELECTED_TABLE_LINE = Color.WHITE;
	public static final Color				UNSELECTED_TABLE_LINE = new Color(224,224,224);
	
	public static final KeyStroke			KS_BACKWARD = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK);
	public static final KeyStroke			KS_FORWARD = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK);
	public static final KeyStroke			KS_HELP = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
	public static final KeyStroke			KS_EXIT = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	public static final KeyStroke			KS_CLOSE = KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_DOWN_MASK);
	
	public static final String				ACTION_FORWARD = "forward";
	public static final String				ACTION_BACKWARD = "backward";
	public static final String				ACTION_HELP = "help";
	public static final String				ACTION_EXIT = "exit";
	
	private static final Map<Class<?>,Object>	DEFAULT_VALUES = new HashMap<>();
	private static final Point2D.Float		ZERO_POINT = new Point2D.Float(0.0f,0.0f);
	
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
				if ((node instanceof Container) && ((Container)node).getComponentCount() > 0) {
loop:				for (int index = 0, maxIndex = ((Container)node).getComponentCount(); index < maxIndex; index++) {
						switch (walkDownInternal(((Container)node).getComponent(index),callback)) {
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
				}
				else if ((node instanceof JMenu) && ((JMenu)node).getMenuComponentCount() > 0) {
loop:				for (int index = 0, maxIndex = ((JMenu)node).getMenuComponentCount(); index < maxIndex; index++) {
						switch (walkDownInternal(((JMenu)node).getMenuComponent(index),callback)) {
							case CONTINUE:
								break;
							case SKIP_CHILDREN	:
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
	 * <p>Find  component in the component tree by it's name</p>
	 * @param node root node to seek component
	 * @param name component name
	 * @return component found or null when  missing
	 * @throws NullPointerException when root node is null
	 * @throws IllegalArgumentException when name to seek is null or empty 
	 */
	public static Container findComponentByName(final Component node, final String name) throws NullPointerException, IllegalArgumentException {
		if (node == null) {
			throw new NullPointerException("Node callbacl can't be null");
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
	 * <p>Prepare renderer for the given meta data and field format</p>
	 * @param metadata meta data to prepare renderer for
	 * @param format field format
	 * @param monitor field monitor
	 * @return component prepared
	 * @throws NullPointerException when any parameters are null
	 * @throws LocalizationException when there are problems with localizers
	 */
	public static JComponent prepareRenderer(final ContentNodeMetadata metadata, final FieldFormat format, final JComponentMonitor monitor) throws NullPointerException, LocalizationException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (format == null) {
			throw new NullPointerException("Field format can't be null");
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null");
		}
		else {
			JComponent	result = null;
			
			switch (format.getContentType()) {
				case BooleanContent	:
					result = new JCheckBoxWithMeta(metadata,monitor);
					break;
				case DateContent	:
					result = new JDateFieldWithMeta(metadata,format,monitor);
					break;
				case EnumContent	:
					result = new JEnumFieldWithMeta(metadata,format,monitor);
					break;
				case FileContent	:
					result = new JFileFieldWithMeta(metadata,format,monitor);
					break;
				case FormattedStringContent	:
					result = new JFormattedTextFieldWithMeta(metadata,format,monitor);
					break;
				case IntegerContent	:
					result = new JIntegerFieldWithMeta(metadata,format,monitor);
					break;
				case NumericContent	:
					result = new JNumericFieldWithMeta(metadata,format,monitor);
					break;
				case StringContent	:
					result = new JTextFieldWithMeta(metadata,format,monitor);
					break;
				case Unclassified	:
					result = null;
					break;
				case URIContent		:
					result = new JTextFieldWithMeta(metadata,format,monitor);
					break;
				case ArrayContent	:
				case NestedContent	:
				case TimestampContent	:
				default:
					throw new UnsupportedOperationException("Content type ["+format.getContentType()+"] is not supported yet");
			}
			result.setName(metadata.getUIPath().toString());
			return result;
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
			if (value instanceof Byte) {
				return (int) Math.signum(((Byte)value).floatValue());
			}
			else if (value instanceof Short) {
				return (int) Math.signum(((Short)value).floatValue());
			}
			else if (value instanceof Integer) {
				return (int) Math.signum(((Integer)value).floatValue());
			}
			else if (value instanceof Long) {
				return (int) Math.signum(((Long)value).floatValue());
			}
			else if (value instanceof Float) {
				return (int) Math.signum(((Float)value).floatValue());
			}
			else if (value instanceof Double) {
				return (int) Math.signum(((Double)value).doubleValue());
			}
			else if (value instanceof BigInteger) {
				return ((BigInteger)value).signum();
			}
			else if (value instanceof BigDecimal) {
				return ((BigDecimal)value).signum();
			}
			else {
				throw new IllegalArgumentException("Value type ["+value.getClass().getCanonicalName()+"] for value ["+value+"] is not a numerical type");
			}
		}
	}
	
	
	/**
	 * <p>Refresh localization content for all the GUI components tree</p>
	 * @param root root of the GUI components to refresh localization
	 * @param oldLocale old locale
	 * @param newLocale new locale
	 * @throws NullPointerException if any parameter is null
	 * @throws LocalizationException in any localization errors
	 */
	public static void refreshLocale(final Component root, final Locale oldLocale, final Locale newLocale) throws NullPointerException, LocalizationException {
		if (oldLocale == null) {
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
				for (Component item : ((Container)root).getComponents()) {
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
		if (component == null) {
			throw new NullPointerException("Component can't be null"); 
		}
		else if (keyStroke == null) {
			throw new NullPointerException("KeyStroke can't be null"); 
		}
		else if (listener == null) {
			throw new IllegalArgumentException("Action listener can't be null"); 
		}
		else if (actionId == null || actionId.isEmpty()) {
			throw new IllegalArgumentException("Action identifier can't be null or empty"); 
		}
		else {
			component.getInputMap(JPanel.WHEN_FOCUSED).put(keyStroke,actionId);
			component.getActionMap().put(actionId,new AbstractAction() {private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					listener.actionPerformed(new ActionEvent(component,0,actionId));
				}
			});
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
		 * @throws Exception
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
						exc.printStackTrace();
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
		 * @param actionCommand
		 */
		void processUnknown(final String actionCommand);
	}

	/**
	 * <p>Prepare action listeners to call methods marked with {@linkplain OnAction} annotation</p>
	 * @param root root component to assign listeners to
	 * @param entity object to call it's annotated methods 
	 */
	public static void assignActionListeners(final JComponent root, final Object entity) {
		assignActionListeners(root,entity,(action)->{JOptionPane.showMessageDialog(null,"unknown action command ["+action+"]");});
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
		return buildAnnotatedActionListener(entity,(action)->{JOptionPane.showMessageDialog(null,"unknown action command ["+action+"]");});
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
		return buildAnnotatedActionListener(entity, onUnknown, PureLibSettings.NULL_LOGGER);
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
					if (!annotatedMethods.containsKey(m.getAnnotation(OnAction.class).value())) {
						annotatedMethods.put(m.getAnnotation(OnAction.class).value(),m);
					}
				}
			});
			if (annotatedMethods.size() == 0) {
				throw new IllegalArgumentException("No any methods in the entity object are annotated with ["+OnAction.class+"] annotation"); 
			}
			else {
				final Map<String,MethodHandleAndAsync>	calls = new HashMap<>();
				
				for (Entry<String, Method> item : annotatedMethods.entrySet()) {
					final Method	m = item.getValue();
					
					m.setAccessible(true);
					try{calls.put(item.getKey(),new MethodHandleAndAsync(MethodHandles.lookup().unreflect(m),m.getAnnotation(OnAction.class).async()));
					} catch (IllegalAccessException exc) {
						throw new IllegalArgumentException("Can't get access to annotated method ["+m+"]: "+exc.getLocalizedMessage()); 
					}
				}
				
				return new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						if (calls.containsKey(e.getActionCommand())) {
							try{final MethodHandleAndAsync	mha = calls.get(e.getActionCommand());
							
								if (mha.async) {
									new Thread(()->{
										try{mha.handle.invoke(entity);
										} catch (ThreadDeath d) {
											throw d;
										} catch (Throwable t) {
											logger.message(Severity.error, t, t.getLocalizedMessage());
										}
									}).start();
								}
								else {
									mha.handle.invoke(entity);
								}
							} catch (Throwable t) {
								logger.message(Severity.error, t, t.getLocalizedMessage());
							}
						}
						else {
							onUnknown.processUnknown(e.getActionCommand());
						}
					}
				};
			}
		}
	}
	
	private static void assignActionListeners(final JComponent root, final ActionListener listener) {
		if (root != null) {
			try{root.getClass().getMethod("addActionListener",ActionListener.class).invoke(root,listener);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			}
			if (root instanceof JMenu) {
				for (int index = 0; index < ((JMenu)root).getMenuComponentCount(); index++) {
					assignActionListeners((JComponent)((JMenu)root).getMenuComponent(index),listener);
				}
			}
			else {
				if (root instanceof InnerActionNode) {
					for (JComponent item : ((InnerActionNode)root).getActionNodes()) {
						assignActionListeners(item,listener);
					}
				}
				for (int index = 0; index < root.getComponentCount(); index++) {
					assignActionListeners((JComponent)root.getComponent(index),listener);
				}
			}
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
			final Dimension			ownerSize = owner.getSize(), helpSize = new Dimension(200,300); 
			final Point				point = new Point((ownerSize.width-helpSize.width)/2,(ownerSize.height-helpSize.height)/2);
			final HyperlinkListener	hll = new HyperlinkListener() {
										@Override
										public void hyperlinkUpdate(final HyperlinkEvent e) {
											if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
												try{Desktop.getDesktop().browse(e.getURL().toURI());
												} catch (URISyntaxException | IOException exc) {
													PureLibSettings.SYSTEM_ERR_LOGGER.message(Severity.error,exc,exc.getLocalizedMessage());
												}
											}
										}
									}; 
			
			pane.setEditable(false);
			scroll.setPreferredSize(helpSize);
			SwingUtilities.convertPointToScreen(point,owner);
			
			final Popup				popup = pf.getPopup(owner,scroll,point.x,point.y);
			
			try(final InputStream	is = helpContent.toURL().openStream();
				final Reader		rdr = new InputStreamReader(is);
				final Writer		wr = new StringWriter()) {
				
				try(final Writer	cwr = new CreoleWriter(wr,MarkupOutputFormat.XML2HTML)) {
				
					Utils.copyStream(rdr,cwr);
				}
				pane.setText(wr.toString());
			}
			
			pane.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(SwingUtils.KS_EXIT,SwingUtils.ACTION_EXIT);
			pane.getActionMap().put(SwingUtils.ACTION_EXIT,new AbstractAction() {private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					popup.hide();
					pane.removeHyperlinkListener(hll);
					pane.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).remove(SwingUtils.KS_EXIT);
					pane.getActionMap().remove(SwingUtils.ACTION_EXIT);
				}
			});
			pane.addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				@Override public void mouseClicked(MouseEvent e) {}
				
				@Override
				public void mouseExited(MouseEvent e) {
					popup.hide();
					pane.removeHyperlinkListener(hll);
					pane.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).remove(SwingUtils.KS_EXIT);
					pane.getActionMap().remove(SwingUtils.ACTION_EXIT);
					pane.removeMouseListener(this);
				}
			});
			pane.addHyperlinkListener(hll);
			
			popup.show();
		}
	}

	private static class MethodHandleAndAsync {
		final MethodHandle	handle;
		final boolean		async;
		
		public MethodHandleAndAsync(MethodHandle handle, boolean async) {
			this.handle = handle;
			this.async = async;
		}
	}
}
	
