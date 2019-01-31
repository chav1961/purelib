package chav1961.purelib.ui.swing;




import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;
import javax.swing.text.html.parser.ContentModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.AbstractLowLevelFormFactory.FieldDescriptor;
import chav1961.purelib.ui.FieldFormat;
import chav1961.purelib.ui.FormFieldFormat;
import chav1961.purelib.ui.interfacers.FieldRepresentation;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.interfacers.RefreshMode;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.OnAction;

/**
 * <p>This utility class contains a set of useful methods to use in the Swing-based applications.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
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
	
	private static final String				NUMBER_MASK = "######################################";

	private static final FocusListener		RIGHT_FOCUS = new FocusListener() {
												@Override
												public void focusLost(FocusEvent e) {
												}
												
												@Override
												public void focusGained(FocusEvent e) {
													final JTextComponent	item = (JTextComponent)e.getComponent();
													
													SwingUtilities.invokeLater(()->{item.setCaretPosition(item.getDocument().getLength());});
												}
											};	
	private static final FocusListener		SELECT_ALL = new FocusListener() {
												@Override
												public void focusLost(FocusEvent e) {
												}
												
												@Override
												public void focusGained(FocusEvent e) {
													final JTextComponent	item = (JTextComponent)e.getComponent();
													
													SwingUtilities.invokeLater(()->{item.selectAll();});
												}
											};	
	private static final FocusListener		RIGHT_FOCUS_AND_SELECT_ALL = new FocusListener() {
												@Override public void focusLost(final FocusEvent e) {}
												
												@Override
												public void focusGained(final FocusEvent e) {
													final JTextComponent	item = (JTextComponent)e.getComponent();
													
													SwingUtilities.invokeLater(()->{
														item.selectAll();
														});
												}
											};	
	
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
	
	public static JComponent prepareRenderer(final ContentNodeMetadata metadata, final FieldFormat format) {
		return null;
	}
	
	
	
	
	
	private static void setContentColor(final JTextComponent item, final Object value, final FormFieldFormat format) {
		final double 	content;
		
		if (value instanceof Double) {
			content = ((Double)value).doubleValue();
		}
		else if (value instanceof Float) {
			content = ((Float)value).doubleValue();
		}
		else if (value instanceof Long) {
			content = ((Long)value).doubleValue();
		}
		else if (value instanceof Integer) {
			content = ((Integer)value).doubleValue();
		}
		else if (value instanceof Short) {
			content = ((Short)value).doubleValue();
		}
		else if (value instanceof Byte) {
			content = ((Byte)value).doubleValue();
		}
		else {
			return;
		}
		
		if (format.isNegativeMarked() && content < 0) {
			item.setForeground(format.isMandatory() ? NEGATIVEMARK_FOREGROUND : NEGATIVEMARK_FOREGROUND);
		}
		else if (format.isPositiveMarked() && content > 0) {
			item.setForeground(format.isMandatory() ? POSITIVEMARK_FOREGROUND : POSITIVEMARK_FOREGROUND);
		}
		else if (format.isZeroMarked() && content == 0) {
			item.setForeground(format.isMandatory() ? ZEROMARK_FOREGROUND : ZEROMARK_FOREGROUND);
		}
		else {
			item.setForeground(format.isMandatory() ? MANDATORY_FOREGROUND : (format.isReadOnly() ? READONLY_FOREGROUND : OPTIONAL_FOREGROUND));
		}
	}

	public static Object getRawDataFromComponent(final JComponent component) throws NullPointerException {
		if (component == null) {
			throw new NullPointerException("Component to get value from can't be null"); 
		}
		else if (component instanceof JCheckBox) {
			return ((JCheckBox)component).isSelected();
		}
		else if (component instanceof JFormattedTextField) {
			return ((JFormattedTextField)component).getText();
		}
		else if (component instanceof JPasswordField) {
			return ((JPasswordField)component).getPassword();
		}
		else if (component instanceof JTextField) {
			return ((JTextField)component).getText();
		}
		else if (component instanceof JComboBox) {
			return ((JComboBox<?>)component).getSelectedItem();
		} else {
			throw new UnsupportedOperationException("Component class ["+component.getClass()+"] is not supported yet");
		}
	}
	
	public static Object getValueFromComponent(final JComponent component) throws NullPointerException {
		if (component == null) {
			throw new NullPointerException("Component to get value from can't be null"); 
		}
		else if (component instanceof JCheckBox) {
			return ((JCheckBox)component).isSelected();
		}
		else if (component instanceof JFormattedTextField) {
			return ((JFormattedTextField)component).getValue();
		}
		else if (component instanceof JPasswordField) {
			return ((JPasswordField)component).getPassword();
		}
		else if (component instanceof JTextField) {
			return ((JTextField)component).getText();
		}
		else if (component instanceof JComboBox) {
			return ((JComboBox<?>)component).getSelectedItem();
		} else {
			throw new UnsupportedOperationException("Component class ["+component.getClass()+"] is not supported yet");
		}
	}

	public static void assignValueToComponent(final JComponent component, final Object value) {
		if (component == null) {
			throw new NullPointerException("Component to get value from can't be null"); 
		}
		else if (component instanceof JCheckBox) {
			((JCheckBox)component).setSelected((Boolean)value);
		}
		else if (component instanceof JFormattedTextField) {
			((JFormattedTextField)component).setValue(value);
		}
		else if (component instanceof JPasswordField) {
			((JPasswordField)component).setText(new String((char[])value));
		}
		else if (component instanceof JTextField) {
			((JTextField)component).setText(value.toString());
		}
		else if (component instanceof JComboBox) {
			((JComboBox<?>)component).setSelectedItem(value);
		} else {
			throw new UnsupportedOperationException("Component class ["+component.getClass()+"] is not supported yet");
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

	private static Format getFormatter(final Localizer localizer, final Class<?> fieldType, final FormFieldFormat format) {
		if (format != null) {
			if (fieldType.isAssignableFrom(Integer.class) || fieldType.isAssignableFrom(int.class) || fieldType.isAssignableFrom(Long.class) || fieldType.isAssignableFrom(long.class) || fieldType.isAssignableFrom(BigInteger.class)) {
				return NumberFormat.getIntegerInstance(localizer.currentLocale().getLocale());
			}
			else if (fieldType.isAssignableFrom(Float.class) || fieldType.isAssignableFrom(float.class) || fieldType.isAssignableFrom(Double.class) || fieldType.isAssignableFrom(double.class) || fieldType.isAssignableFrom(BigDecimal.class)) {
				return new DecimalFormat("#####0.0##");
			}
			else if (fieldType.isAssignableFrom(BigDecimal.class) || fieldType.isAssignableFrom(BigInteger.class)) {
				return NumberFormat.getCurrencyInstance(localizer.currentLocale().getLocale());
			}
			else if (fieldType.isAssignableFrom(Date.class)) {
				return DateFormat.getDateInstance(DateFormat.SHORT,localizer.currentLocale().getLocale());
			}
			else if (fieldType.isAssignableFrom(Time.class)) {
				return DateFormat.getTimeInstance(DateFormat.SHORT,localizer.currentLocale().getLocale());
			}
			else if (fieldType.isAssignableFrom(Timestamp.class)) {
				return DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT,localizer.currentLocale().getLocale());
			}
			else {
				return null;
			}
		}
		else {
			if (fieldType.isAssignableFrom(Integer.class) || fieldType.isAssignableFrom(int.class) || fieldType.isAssignableFrom(Long.class) || fieldType.isAssignableFrom(long.class) || fieldType.isAssignableFrom(BigInteger.class)) {
				return NumberFormat.getIntegerInstance(localizer.currentLocale().getLocale());
			}
			else if (fieldType.isAssignableFrom(Float.class) || fieldType.isAssignableFrom(float.class) || fieldType.isAssignableFrom(Double.class) || fieldType.isAssignableFrom(double.class) || fieldType.isAssignableFrom(BigDecimal.class)) {
				return new DecimalFormat("#####0.0##");
			}
			else if (fieldType.isAssignableFrom(BigDecimal.class) || fieldType.isAssignableFrom(BigInteger.class)) {
				return NumberFormat.getCurrencyInstance(localizer.currentLocale().getLocale());
			}
			else if (fieldType.isAssignableFrom(Date.class)) {
				return DateFormat.getDateInstance(DateFormat.SHORT,localizer.currentLocale().getLocale());
			}
			else if (fieldType.isAssignableFrom(Time.class)) {
				return DateFormat.getTimeInstance(DateFormat.SHORT,localizer.currentLocale().getLocale());
			}
			else if (fieldType.isAssignableFrom(Timestamp.class)) {
				return DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT,localizer.currentLocale().getLocale());
			}
			else {
				return null;
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

	static class JIntValueEditor extends JFormattedTextField implements JComponentInterface, LocaleChangeListener {
		private static final long 		serialVersionUID = -4695163217452195559L;
		private static final int		NEGATIVE_SIGN = 1;
		private static final int		ZERO_SIGN = 2;
		private static final int		POSITIVE_SIGN = 4;

		private final Localizer			localizer;
		private final FieldDescriptor	desc;
		
		JIntValueEditor(final Localizer localizer, final FieldDescriptor desc) {
			if (localizer == null) {
				throw new NullPointerException("Localizer can't be null");
			}
			else if (desc == null) {
				throw new NullPointerException("Field descriptor can't be null");
			}
			else {
				final Font	currentFont = getFont();
				
				this.localizer = localizer;
				this.desc = desc;
				setLocale(localizer.currentLocale().getLocale());
				setFont(new Font(currentFont.getFontName(),Font.BOLD,currentFont.getSize()));
				setHorizontalAlignment(JFormattedTextField.RIGHT);
				setColumns(desc.fieldLen);
				setFormatterFactory(new AbstractFormatterFactory() {
					@Override
					public AbstractFormatter getFormatter(JFormattedTextField tf) {
						return new NumberFormatter(new DecimalFormat(
								 NUMBER_MASK.substring(0,Math.max(0,desc.fieldFormat.getLen()-1))+"0"
								,new DecimalFormatSymbols(localizer.currentLocale().getLocale())
							));
					}
				});
				if (desc.fieldFormat.isMandatory()) {
					setBackground(MANDATORY_BACKGROUND);
					setForeground(MANDATORY_FOREGROUND);
					setSelectedTextColor(MANDATORY_SELECTION_FOREGROUND);
					setSelectionColor(MANDATORY_SELECTION_BACKGROUND);
				}
				else {
					setBackground(OPTIONAL_BACKGROUND);
					setForeground(OPTIONAL_FOREGROUND);
					setSelectedTextColor(OPTIONAL_SELECTION_FOREGROUND);
					setSelectionColor(OPTIONAL_SELECTION_BACKGROUND);
				}
				if (desc.fieldFormat.isSelectAllContent()) {
					addFocusListener(RIGHT_FOCUS_AND_SELECT_ALL);
				}
				else {
					addFocusListener(RIGHT_FOCUS);
				}
				if (desc.fieldFormat.isNegativeMarked() || desc.fieldFormat.isPositiveMarked() || desc.fieldFormat.isZeroMarked()) {
					setInputVerifier(new InputVerifier() {
						@Override
						public boolean verify(JComponent input) {
							try{final Long	value = Long.valueOf(((JTextComponent)input).getText());
								setContentColor((JTextComponent)input,value,desc.fieldFormat);
								
								return true;
							} catch (NumberFormatException exc) {
								return false;
							}
						}
					});
				}
				if (desc.fieldFormat.isReadOnly()) {
					setEditable(false);
				}
			};
		}

		@Override
		public String getRawDataFromComponent() {
			return getText();
		}

		@Override
		public Object getValueFromComponent() {
			return getValue();
		}

		@Override
		public Object getChangedValueFromComponent() throws SyntaxException {
			try{return getFormatter().stringToValue(getRawDataFromComponent());
			} catch (ParseException e) {
				throw new SyntaxException(0,0,e.getLocalizedMessage(),e);
			}
		}

		@Override
		public void assignValueToComponent(final Object value) {
			setValue(value);
		}
		
		@Override
		public Class<?> getValueType() {
			return desc.fieldType;
		}

		@Override
		public FieldDescriptor getFieldDescriptor() {
			return desc;
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			final Object	value = getValue();
			
			setLocale(newLocale);
			setValue(value);
		}

		@Override
		public void setValue(final Object value) {
			super.setValue(value);
			if (desc.fieldFormat.isNegativeMarked() || desc.fieldFormat.isPositiveMarked() || desc.fieldFormat.isZeroMarked()) {
				setContentColor(this,value,desc.fieldFormat);
			}
		}
		
		@Override
	    protected void processFocusEvent(FocusEvent e) {
	    	super.processFocusEvent(e);
	    	if (e.getID() == FocusEvent.FOCUS_GAINED) {
	    		getCaret().setDot(0);
	    		getCaret().moveDot(getDocument().getLength());
	    	}
	    }
		
		private static int sign(final Object value) {
			if (value == null) {
				return 0;
			}
			else {
				return 0;
			}
		}
	}

	static class JRealValueEditor extends JFormattedTextField implements JComponentInterface, LocaleChangeListener {
		private static final long 		serialVersionUID = -4695163217452195559L;
		private static final int		NEGATIVE_SIGN = 1;
		private static final int		ZERO_SIGN = 2;
		private static final int		POSITIVE_SIGN = 4;

		private final Localizer			localizer;
		private final FieldDescriptor	desc;
		
		JRealValueEditor(final Localizer localizer, final FieldDescriptor desc) {
			if (localizer == null) {
				throw new NullPointerException("Localizer can't be null");
			}
			else if (desc == null) {
				throw new NullPointerException("Field descriptor can't be null");
			}
			else {
				final Font	currentFont = getFont();
				
				this.localizer = localizer;
				this.desc = desc;
				setLocale(localizer.currentLocale().getLocale());
				setFont(new Font(currentFont.getFontName(),Font.BOLD,currentFont.getSize()));
				setHorizontalAlignment(JFormattedTextField.RIGHT);
				setColumns(desc.fieldLen);
				setFormatterFactory(new AbstractFormatterFactory() {
					@Override
					public AbstractFormatter getFormatter(JFormattedTextField tf) {
						return new NumberFormatter(new DecimalFormat(
									 NUMBER_MASK.substring(0,Math.max(0,desc.fieldFormat.getLen()-desc.fieldFormat.getFrac()-1))+"0.0"+NUMBER_MASK.substring(0,Math.max(0,desc.fieldFormat.getFrac()-1))
									,new DecimalFormatSymbols(localizer.currentLocale().getLocale())
								));
					}
				});
				if (desc.fieldFormat.isMandatory()) {
					setBackground(MANDATORY_BACKGROUND);
					setForeground(MANDATORY_FOREGROUND);
					setSelectedTextColor(MANDATORY_SELECTION_FOREGROUND);
					setSelectionColor(MANDATORY_SELECTION_BACKGROUND);
				}
				else {
					setBackground(OPTIONAL_BACKGROUND);
					setForeground(OPTIONAL_FOREGROUND);
					setSelectedTextColor(OPTIONAL_SELECTION_FOREGROUND);
					setSelectionColor(OPTIONAL_SELECTION_BACKGROUND);
				}
				if (desc.fieldFormat.isSelectAllContent()) {
					addFocusListener(RIGHT_FOCUS_AND_SELECT_ALL);
				}
				else {
					addFocusListener(RIGHT_FOCUS);
				}
				if (desc.fieldFormat.isNegativeMarked() || desc.fieldFormat.isPositiveMarked() || desc.fieldFormat.isZeroMarked()) {
					setInputVerifier(new InputVerifier() {
						@Override
						public boolean verify(JComponent input) {
							try{final Long	value = Long.valueOf(((JTextComponent)input).getText());
								setContentColor((JTextComponent)input,value,desc.fieldFormat);
								
								return true;
							} catch (NumberFormatException exc) {
								return false;
							}
						}
					});
				}
				if (desc.fieldFormat.isReadOnly()) {
					setEditable(false);
				}
			};
		}

		@Override
		public String getRawDataFromComponent() {
			return getText();
		}

		@Override
		public Object getValueFromComponent() {
			return getValue();
		}

		@Override
		public Object getChangedValueFromComponent() throws SyntaxException {
			try{return getFormatter().stringToValue(getRawDataFromComponent());
			} catch (ParseException e) {
				throw new SyntaxException(0,0,e.getLocalizedMessage(),e);
			}
		}

		@Override
		public void assignValueToComponent(final Object value) {
			setValue(value);
		}
		
		@Override
		public Class<?> getValueType() {
			return desc.fieldType;
		}

		@Override
		public FieldDescriptor getFieldDescriptor() {
			return desc;
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			final Object	value = getValue();
			
			setLocale(newLocale);
			setValue(value);
		}

		@Override
		public void setValue(final Object value) {
			boolean	marked = false;

			super.setValue(value);
			if (desc.fieldFormat.isNegativeMarked() || desc.fieldFormat.isPositiveMarked() || desc.fieldFormat.isZeroMarked()) {
				final int	valueSign = sign(value);
				
				if (desc.fieldFormat.isNegativeMarked() && (valueSign & NEGATIVE_SIGN) != 0) {
					setForeground(desc.fieldFormat.isMandatory() ? MANDATORY_FOREGROUND_NEGATIVE : OPTIONAL_FOREGROUND_NEGATIVE);
					marked = true;
				}
				if (desc.fieldFormat.isPositiveMarked() && (valueSign & POSITIVE_SIGN) != 0) {
					setForeground(desc.fieldFormat.isMandatory() ? MANDATORY_FOREGROUND_POSITIVE : OPTIONAL_FOREGROUND_POSITIVE);
					marked = true;
				}
				if (desc.fieldFormat.isZeroMarked() && (valueSign & ZERO_SIGN) != 0) {
					setForeground(desc.fieldFormat.isMandatory() ? MANDATORY_FOREGROUND_ZERO : OPTIONAL_FOREGROUND_ZERO);
					marked = true;
				}
				if (!marked) {
					setForeground(desc.fieldFormat.isMandatory() ? MANDATORY_FOREGROUND : OPTIONAL_FOREGROUND);
				}
			}
		}
		
		@Override
	    protected void processFocusEvent(FocusEvent e) {
	    	super.processFocusEvent(e);
	    	if (e.getID() == FocusEvent.FOCUS_GAINED) {
	    		getCaret().setDot(0);
	    		getCaret().moveDot(getDocument().getLength());
	    	}
	    }
		
//		private static String buildFormatTemplate(final FieldDescriptor desc) {
//			return NUMBER_MASK.substring(0,Math.max(0,desc.fieldFormat.len-desc.fieldFormat.frac-1))+"0.0"+NUMBER_MASK.substring(0,Math.max(0,desc.fieldFormat.frac-1));
//		}
		
		private static int sign(final Object value) {
			if (value == null) {
				return 0;
			}
			else {
				return 0;
			}
		}
	}

	static class JEnumEditor extends JComboBox<Enum<?>> implements JComponentInterface, LocaleChangeListener {
		private static final long 		serialVersionUID = 3409672516846867218L;
		
		private final Localizer			localizer;
		private final FieldDescriptor	desc;
		
		JEnumEditor(final Localizer localizer, final FieldDescriptor desc) {
			super((Enum<?>[])desc.fieldType.getEnumConstants());
			if (localizer == null) {
				throw new NullPointerException("Localizer can't be null");
			}
			else {
				this.localizer = localizer;
				this.desc = desc;
				setLocale(localizer.currentLocale().getLocale());
				
				if (desc.fieldFormat.isMandatory()) {
					setBackground(MANDATORY_BACKGROUND);
					setForeground(MANDATORY_FOREGROUND);
				}
				else {
					setBackground(OPTIONAL_BACKGROUND);
					setForeground(OPTIONAL_FOREGROUND);
				}
				if (desc.fieldFormat.isReadOnly()) {
					setEditable(false);
				}
				
				setRenderer(new ListCellRenderer<Enum<?>>() {
					@Override
					public Component getListCellRendererComponent(JList<? extends Enum<?>> list, Enum<?> value, int index, boolean isSelected, boolean cellHasFocus) {
						try{if (value.getClass().getField(value.name()).isAnnotationPresent(LocaleResource.class)) {
								final String	label = localizer.getValue(value.getClass().getField(value.name()).getAnnotation(LocaleResource.class).value());
								
								if (label.startsWith("uri(")) {
									final Icon	image = new ImageIcon(new URL(label.substring(0,label.length()-1).substring(4)));
									
									return new JLabel(image);
								}
								else {
									return new JLabel(label);
								}
							}
							else {
								return new JLabel(value.name());
							}
						} catch (NoSuchFieldException | SecurityException | LocalizationException | IllegalArgumentException | MalformedURLException e) {
							return new JLabel(value.name());
						}
					}
				});
			};
		}
		
		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			final Object	value = getSelectedItem();
			
			setLocale(newLocale);
			setSelectedItem(value);
		}

		@Override
		public String getRawDataFromComponent() {
			return getSelectedItem().toString();
		}

		@Override
		public Object getValueFromComponent() {
			return getSelectedItem();
		}

		@Override
		public Object getChangedValueFromComponent() throws SyntaxException {
			return getSelectedItem();
		}

		@Override
		public void assignValueToComponent(final Object value) {
			setSelectedItem(value);
		}

		@Override
		public Class<?> getValueType() {
			return desc.fieldType;
		}

		@Override
		public FieldDescriptor getFieldDescriptor() {
			return desc;
		}
	}

	static class JBooleanEditor extends JCheckBox implements JComponentInterface, LocaleChangeListener {
		private static final long serialVersionUID = 6424585158670513019L;

		private final Localizer			localizer;
		private final FieldDescriptor	desc;
		
		JBooleanEditor(final Localizer localizer, final FieldDescriptor desc) {
			if (localizer == null) {
				throw new NullPointerException("Localizer can't be null");
			}
			else if (desc == null) {
				throw new NullPointerException("Field descriptor can't be null");
			}
			else {
				this.localizer = localizer;
				this.desc = desc;
				setLocale(localizer.currentLocale().getLocale());
				
				if (desc.fieldFormat.isReadOnly()) {
					setEnabled(false);
				}
			}
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		}

		@Override
		public String getRawDataFromComponent() {
			return isSelected() ? "true" : "false";
		}

		@Override
		public Object getValueFromComponent() {
			return Boolean.valueOf(isSelected());
		}

		@Override
		public Object getChangedValueFromComponent() throws SyntaxException {
			return Boolean.valueOf(isSelected());
		}

		@Override
		public void assignValueToComponent(final Object value) {
			if (value instanceof Boolean) {
				setSelected(((Boolean)value).booleanValue());
			}
		}

		@Override
		public Class<?> getValueType() {
			return desc.fieldType;
		}

		@Override
		public FieldDescriptor getFieldDescriptor() {
			return desc;
		}
	}

	static class JFileEditor extends JTextField implements JComponentInterface, LocaleChangeListener {
		private static final long serialVersionUID = -3940286547765916867L;
		private static final int		WIZARD_BUTTON_SIZE = 20;

		private final Localizer			localizer;
		private final FieldDescriptor	desc;
		private final JButton			wizardButton = new JButton("...");
		
		JFileEditor(final Localizer localizer, final FieldDescriptor desc) {
			if (localizer == null) {
				throw new NullPointerException("Localizer can't be null");
			}
			else if (desc == null) {
				throw new NullPointerException("Field descriptor can't be null");
			}
			else {
				this.localizer = localizer;
				this.desc = desc;
				setLocale(localizer.currentLocale().getLocale());
				setHorizontalAlignment(JFormattedTextField.LEFT);
				setColumns(desc.fieldLen);
				if (desc.fieldFormat.isMandatory()) {
					setBackground(MANDATORY_BACKGROUND);
					setForeground(MANDATORY_FOREGROUND);
					setSelectedTextColor(MANDATORY_SELECTION_FOREGROUND);
					setSelectionColor(MANDATORY_SELECTION_BACKGROUND);
				}
				else {
					setBackground(OPTIONAL_BACKGROUND);
					setForeground(OPTIONAL_FOREGROUND);
					setSelectedTextColor(OPTIONAL_SELECTION_FOREGROUND);
					setSelectionColor(OPTIONAL_SELECTION_BACKGROUND);
				}
				if (desc.fieldFormat.isReadOnly()) {
					setEditable(false);
					wizardButton.setEnabled(false);
				}
				add(wizardButton);
				addComponentListener(new ComponentListener() {
					@Override
					public void componentResized(final ComponentEvent e) {
						final Insets	insets = JFileEditor.this.getInsets();
						
						insets.right -= getHeight();
						wizardButton.setLocation(JFileEditor.this.getWidth()-JFileEditor.this.getHeight(),insets.top);
						wizardButton.setSize(JFileEditor.this.getHeight()-insets.right,JFileEditor.this.getHeight()-insets.top-insets.bottom);
					}
					
					@Override public void componentShown(ComponentEvent e) {}
					@Override public void componentMoved(ComponentEvent e) {}
					@Override public void componentHidden(ComponentEvent e) {}
				});
				wizardButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final JFileChooser	chooser = new JFileChooser();
						final File			f = new File(JFileEditor.this.getText().isEmpty() ? "./" : JFileEditor.this.getText());
						
						chooser.setSelectedFile(f);
						if (chooser.showSaveDialog(JFileEditor.this) == JFileChooser.APPROVE_OPTION) {
							JFileEditor.this.setText(chooser.getSelectedFile().getAbsolutePath());
							JFileEditor.this.requestFocusInWindow();
						}
					}
				});
			};
		}
		
		@Override
		public Insets getInsets() {
			if (getBorder() != null) {
				final Insets	result = getBorder().getBorderInsets(this);
				
				result.right += getHeight();
				return result;
			}
			else {
				return super.getInsets();
			}
		}
		
		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		}

		@Override
		public String getRawDataFromComponent() {
			return getText();
		}

		@Override
		public Object getValueFromComponent() {
			return new File(getText());
		}

		@Override
		public Object getChangedValueFromComponent() throws SyntaxException {
			return new File(getText());
		}

		@Override
		public void assignValueToComponent(final Object value) {
			if (value instanceof File) {
				setText(((File)value).getAbsolutePath());
			}
		}

		@Override
		public Class<?> getValueType() {
			return desc.fieldType;
		}

		@Override
		public FieldDescriptor getFieldDescriptor() {
			return desc;
		}
	}

}
	
