package chav1961.purelib.ui.swing;




import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.OnAction;

/**
 * <p>This utility class contains a set of useful methods to use in the Swing-based applications.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2 last update 0.0.3
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
	
	public static final String				ACTION_FORWARD = "forward";
	public static final String				ACTION_BACKWARD = "backward";
	public static final String				ACTION_HELP = "help";
	public static final String				ACTION_EXIT = "exit";
	
	private static final Map<Class<?>,Object>	DEFAULT_VALUES = new HashMap<>();
	
	static {
		DEFAULT_VALUES.put(byte.class,(byte)0);
		DEFAULT_VALUES.put(short.class,(short)0);
		DEFAULT_VALUES.put(int.class,0);
		DEFAULT_VALUES.put(long.class,0L);
		DEFAULT_VALUES.put(float.class,0.0f);
		DEFAULT_VALUES.put(double.class,0.0);
		DEFAULT_VALUES.put(Float.class,0.0f);
		DEFAULT_VALUES.put(Double.class,0.0);
		DEFAULT_VALUES.put(BigInteger.class,BigInteger.ZERO);
		DEFAULT_VALUES.put(BigDecimal.class,BigDecimal.ZERO);
	}
	
	
	private SwingUtils() {}

	public static URL url(final String resource) {
		return SwingUtils.class.getResource(resource);	
	}

	public static Container findComponentByName(final Container node, final String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name to find can't be null or empty");
		}
		else if (node != null) {
			if (name.equals(node.getName())) {
				return node;
			}
			else {
				if (node.getComponentCount() > 0) {
					for (int index = 0, maxIndex = node.getComponentCount(); index < maxIndex; index++) {
						if (node.getComponent(index) instanceof Container) {
							final Container	child = findComponentByName((Container)node.getComponent(index),name);
							
							if (child != null) {
								return child;
							}
						}
					}
				}
				return null;
			}
		}
		else {
			return null;
		}
	}
	
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
					result = new JLabelWithMeta(metadata);
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

	public static String prepareMessage(final Severity severity, final String format, final Object... parameters) {
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
		// TODO:
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
				throw new IllegalArgumentException("Value type ["+value.getClass().getCanonicalName()+"] is not a numerical");
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
		else if (root != null) {
			if (root instanceof LocaleChangeListener) {
				((LocaleChangeListener)root).localeChanged(oldLocale, newLocale);
			}
			else if (root instanceof Container) {
				for (Component item : ((Container)root).getComponents()) {
					refreshLocale(item,oldLocale,newLocale);
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
			throw new IllegalArgumentException("Help identifier can't eb null or empty"); 
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
		if (dialog == null) {
			throw new NullPointerException("Dialog window can't be null"); 
		}
		else {
			final Dimension	screen = Toolkit.getDefaultToolkit().getScreenSize();
			final Dimension	size = dialog.getContentPane().getPreferredSize();
			final Point		location = new Point((screen.width-size.width)/2,(screen.height-size.height)/2);
			
			dialog.setLocation(location);
		}
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
	
	@FunctionalInterface
	public interface ExitMethodCallback {
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
				public void windowClosing(WindowEvent e) {
					try{callback.processExit();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}				
			});
		}
	}
	
	@FunctionalInterface
	public interface FailedActionListenerCallback {
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
		if (entity == null) {
			throw new NullPointerException("Entity class can't be null"); 
		}
		else {
			final Map<String,Method>	annotatedMethods = new HashMap<>(); 
			Class<?>					entityClass = entity.getClass();
			
			while (entityClass != null) {
				for (Method m : entityClass.getDeclaredMethods()) {
					if (m.isAnnotationPresent(OnAction.class)) {
						if (!annotatedMethods.containsKey(m.getAnnotation(OnAction.class).value())) {
							annotatedMethods.put(m.getAnnotation(OnAction.class).value(),m);
						}
					}
				}
				entityClass = entityClass.getSuperclass();
			}
			if (annotatedMethods.size() == 0) {
				throw new IllegalArgumentException("No any methods in the entity object are annotated with ["+OnAction.class+"] annotation"); 
			}
			else {
				final Map<String,MethodHandle>	calls = new HashMap<>();
				
				for (Entry<String, Method> item : annotatedMethods.entrySet()) {
					final Method	m = item.getValue();
					
					m.setAccessible(true);
					try{calls.put(item.getKey(),MethodHandles.lookup().unreflect(m));
					} catch (IllegalAccessException exc) {
						throw new IllegalArgumentException("Can't get access to annotated method ["+m+"]: "+exc.getLocalizedMessage()); 
					}
				}
				
				return new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						if (calls.containsKey(e.getActionCommand())) {
							try{calls.get(e.getActionCommand()).invoke(entity);
							} catch (Throwable t) {
								t.printStackTrace();
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

}
	
