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
	
	public static JComponent prepareInputComponent(final FieldRepresentation controlRepresentation, final String controlName, final String controlTooltip, final int controlLen, final FormFieldFormat controlFormat) throws ParseException {
		JComponent		result;
		
		switch (controlRepresentation) {
			case BOOLVALUE			: 
				final JCheckBox			checkBox = new JCheckBox();
				
				checkBox.setBackground(Color.WHITE);
				result = checkBox;
				break;
			case INTVALUE			:
				final MaskFormatter			integerMask = new MaskFormatter(NUMBER_MASK.substring(0,controlLen));				
				final JFormattedTextField	integerField = new JFormattedTextField(integerMask);
				final Font					integerFont = integerField.getFont();
				final Font					newIntegerFont = new Font(integerFont.getFontName(),integerFont.getStyle()|Font.BOLD,integerFont.getSize());
				
				integerMask.setValidCharacters("0123456789-+");
				integerField.setFont(newIntegerFont);
				integerField.setColumns(controlLen);
				integerField.setHorizontalAlignment(JTextField.RIGHT);
				result = integerField;
				break;
			case REALVALUE			:
				final MaskFormatter			realMask = new MaskFormatter(NUMBER_MASK.substring(0,controlLen));				
				final JFormattedTextField	realField = new JFormattedTextField(realMask);
				final Font					realFont = realField.getFont();
				final Font					newRealFont = new Font(realFont.getFontName(),realFont.getStyle()|Font.BOLD,realFont.getSize());
				
				realMask.setValidCharacters("0123456789-+.");
				realField.setFont(newRealFont);
				realField.setColumns(controlLen);
				realField.setHorizontalAlignment(JTextField.RIGHT);
				result = realField;
				break;
			case CURRENCYVALUE		:
				final JFormattedTextField	currencyField = new JFormattedTextField(NumberFormat.getCurrencyInstance());
				final Font					currencyFont = currencyField.getFont();
				final Font					newCurrencyFont = new Font(currencyFont.getFontName(),currencyFont.getStyle()|Font.BOLD,currencyFont.getSize());
				
				currencyField.setFont(newCurrencyFont);
				currencyField.setColumns(controlLen);
				currencyField.setHorizontalAlignment(JTextField.RIGHT);
				result = currencyField;
				break;
			case DATEVALUE			:
			case TEXTVALUE			:
			case FORMATTEDTEXTVALUE	:
				final JTextField		textField = new JTextField();
				
				result = textField;
				break;
			case PASSWDVALUE		:
				final JPasswordField	passwdField = new JPasswordField();
				
				result = passwdField;
				break;
			case DDLISTVALUE		:
			case LISTVALUE			:
			case MAPVALUE			:
			case WIZARDVALUE		:
			case KEYVALUEPAIR		:
				final JTextField		temp = new JTextField();
				
				result = temp;
				break;
			default : throw new UnsupportedOperationException("Control representation ["+controlRepresentation+"] is not supported yet");
		}

		if (controlFormat.isReadOnly() || controlFormat.isReadOnlyOnExistent()) {
			result.setEnabled(false);
		}
		if (controlFormat.isMandatory()) {
			result.setBackground(MANDATORY_BACKGROUND);
		}
		result.setName(controlName);
		result.setToolTipText(controlTooltip);
		return result;
	}

	public static JComponent prepareCellRendererComponent(final Locale locale, final FieldDescriptor desc, final Object value) {
		JComponent		result;
		
		switch (desc.fieldRepresentation) {
			case BOOLVALUE			: 
				final JCheckBox				checkBox = new JCheckBox();
				
				if (value != null) {
					if (value instanceof Boolean) {
						checkBox.setSelected((Boolean)value);
					}
					else {
						throw new IllegalArgumentException("Illegal value class ["+value.getClass()+"] to assign to check box");
					}
				}
				result = checkBox;
				break;
			case INTVALUE			:
				final JLabel				integerLabel = new JLabel();
				final Font					integerFont = integerLabel.getFont();
				final Font					newIntegerFont = new Font(integerFont.getFontName(),Font.BOLD,integerFont.getSize());
				
				if (value != null) {
					if (value instanceof Integer) {
						integerLabel.setText(NumberFormat.getIntegerInstance(locale).format(((Integer)value).longValue()));
					}
					else if (value instanceof Long) {
						integerLabel.setText(NumberFormat.getIntegerInstance(locale).format(((Long)value).longValue()));
					}
					else {
						throw new IllegalArgumentException("Illegal value class ["+value.getClass()+"] to assign to integer field");
					}
				}
				else {
					integerLabel.setText("0");
				}
				integerLabel.setFont(newIntegerFont);
				integerLabel.setHorizontalAlignment(JLabel.RIGHT);
				result = integerLabel;
				break;
			case REALVALUE			:
				final JLabel				realLabel = new JLabel();
				final Font					realFont = realLabel.getFont();
				final Font					newrealFont = new Font(realFont.getFontName(),Font.BOLD,realFont.getSize());
				
				if (value != null) {
					if (value instanceof Float) {
						realLabel.setText(NumberFormat.getNumberInstance(locale).format(((Float)value).doubleValue()));
					}
					else if (value instanceof Double) {
						realLabel.setText(NumberFormat.getNumberInstance(locale).format(((Double)value).doubleValue()));
					}
					else {
						throw new IllegalArgumentException("Illegal value class ["+value.getClass()+"] to assign to real field");
					}
				}
				else {
					realLabel.setText("0.0");
				}
				realLabel.setFont(newrealFont);
				realLabel.setHorizontalAlignment(JLabel.RIGHT);
				result = realLabel;
				break;
			case CURRENCYVALUE		:
				final JLabel				currencyLabel = new JLabel();
				final Font					currencyFont = currencyLabel.getFont();
				final Font					newCurrencyFont = new Font(currencyFont.getFontName(),currencyFont.getStyle()|Font.BOLD,currencyFont.getSize());
				
				if (value != null) {
					if ((value instanceof BigInteger) || (value instanceof BigDecimal)) {
						currencyLabel.setText(NumberFormat.getCurrencyInstance(locale).format(value));
					}
					else {
						throw new IllegalArgumentException("Illegal value class ["+value.getClass()+"] to assign to currency field");
					}
				}
				else {
					currencyLabel.setText("0");
				}
				currencyLabel.setFont(newCurrencyFont);
				currencyLabel.setHorizontalAlignment(JLabel.RIGHT);
				result = currencyLabel;
				break;
			case DATEVALUE			:
				final JLabel				dateLabel = new JLabel();
				final Font					dateFont = dateLabel.getFont();
				final Font					newDateFont = new Font(dateFont.getFontName(),dateFont.getStyle()|Font.BOLD,dateFont.getSize());
				
				if (value != null) {
					if (value instanceof Timestamp) {
						dateLabel.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT,locale).format(value));
					}
					else if (value instanceof Time) {
						dateLabel.setText(DateFormat.getTimeInstance(DateFormat.SHORT,locale).format(value));
					}
					else if (value instanceof Date) {
						dateLabel.setText(DateFormat.getDateInstance(DateFormat.SHORT,locale).format(value));
					}
					else {
						throw new IllegalArgumentException("Illegal value class ["+value.getClass()+"] to assign to date field");
					}
				}
				else {
					final Date	currentDate = new Date(System.currentTimeMillis());
					
					if (Timestamp.class.isAssignableFrom(desc.fieldType)) {
						dateLabel.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT,locale).format(currentDate));
					}
					else if (Time.class.isAssignableFrom(desc.fieldType)) {
						dateLabel.setText(DateFormat.getTimeInstance(DateFormat.SHORT,locale).format(currentDate));
					}
					else if (Date.class.isAssignableFrom(desc.fieldType)) {
						dateLabel.setText(DateFormat.getDateInstance(DateFormat.SHORT,locale).format(currentDate));
					}
					else {
						throw new IllegalArgumentException("Illegal value class to assign to date field");
					}
				}
				dateLabel.setFont(newDateFont);
				dateLabel.setHorizontalAlignment(JLabel.LEFT);
				result = dateLabel;
				break;
			case TEXTVALUE			:
				final JLabel			textLabel = new JLabel(value == null ? "" : value.toString());
				final Font				textFont = textLabel.getFont();
				final Font				newTextFont = new Font(textFont.getFontName(),Font.PLAIN,textFont.getSize());
				
				textLabel.setFont(newTextFont);
				textLabel.setHorizontalAlignment(JLabel.LEFT);
				result = textLabel;
				break;
			case FORMATTEDTEXTVALUE	:
				final JLabel			formattedLabel = new JLabel();
				final Font				formattedFont = formattedLabel.getFont();
				final Font				newFormattedFont = new Font(formattedFont.getFontName(),Font.PLAIN,formattedFont.getSize());
				
				if (value != null) {
					
				}
				formattedLabel.setFont(newFormattedFont);
				formattedLabel.setHorizontalAlignment(JLabel.LEFT);
				result = formattedLabel;
				break;
			case PASSWDVALUE		:
				final JLabel			passwdLabel = new JLabel("<hidden>");
				final Font				passwdFont = passwdLabel.getFont();
				final Font				newPasswdFont = new Font(passwdFont.getFontName(),Font.PLAIN,passwdFont.getSize());
				
				passwdLabel.setFont(newPasswdFont);
				passwdLabel.setHorizontalAlignment(JLabel.LEFT);
				result = passwdLabel;
				break;
			case DDLISTVALUE		:
				final JLabel			ddlistLabel = new JLabel();
				final Icon				ddlIcon = new ImageIcon(SwingUtils.class.getResource("ddlist.png"));
				final Font				ddlistFont = ddlistLabel.getFont();
				final Font				newDdlistFont = new Font(ddlistFont.getFontName(),Font.PLAIN,ddlistFont.getSize());
				
				if (value != null) {
					if (value instanceof Enum) {
						ddlistLabel.setText(value.toString());
					}
					else {
						throw new IllegalArgumentException("Illegal Enum value class ["+value.getClass()+"] to assign to combobox field");
					}
				}
				else {
					ddlistLabel.setText("<unselected>");
				}				
				ddlistLabel.setFont(newDdlistFont);
				ddlistLabel.setHorizontalAlignment(JLabel.LEFT);
				ddlistLabel.setHorizontalTextPosition(JLabel.LEFT);
				ddlistLabel.setIcon(ddlIcon);
				result = ddlistLabel;
				break;
			case LISTVALUE			:
				final JLabel			listLabel = new JLabel();
				final Icon				listIcon = new ImageIcon(SwingUtils.class.getResource("list.png"));
				final Font				listFont = listLabel.getFont();
				final Font				newListFont = new Font(listFont.getFontName(),Font.PLAIN,listFont.getSize());
				
				if (value != null) {
					if (value instanceof Collection) {
						listLabel.setText(((Collection<?>)value).size()+" items");
					}
					else if (desc.fieldType.isArray()) {
						listLabel.setText(Array.getLength(value)+" items");
					}
					else {
						throw new IllegalArgumentException("Illegal Array/Collection value class ["+value.getClass()+"] to assign to listbox field");
					}
				}
				else {
					listLabel.setText("<null>");
				}				
				listLabel.setFont(newListFont);
				listLabel.setHorizontalAlignment(JLabel.LEFT);
				listLabel.setHorizontalTextPosition(JLabel.LEFT);
				listLabel.setIcon(listIcon);
				result = listLabel;
				break;
			case MAPVALUE			:
				final JLabel			mapLabel = new JLabel();
				final Icon				mapIcon = new ImageIcon(SwingUtils.class.getResource("list.png"));
				final Font				mapFont = mapLabel.getFont();
				final Font				newMapFont = new Font(mapFont.getFontName(),Font.PLAIN,mapFont.getSize());
				
				if (value != null) {
					if (value instanceof Map) {
						mapLabel.setText(((Map<?,?>)value).size()+" items");
					}
					else {
						throw new IllegalArgumentException("Illegal Map value class ["+value.getClass()+"] to assign to map field");
					}
				}
				else {
					mapLabel.setText("<null>");
				}				
				mapLabel.setFont(newMapFont);
				mapLabel.setHorizontalAlignment(JLabel.LEFT);
				mapLabel.setHorizontalTextPosition(JLabel.LEFT);
				mapLabel.setIcon(mapIcon);
				result = mapLabel;
				break;
			case WIZARDVALUE		:
				final JLabel			wizardLabel = new JLabel();
				final Icon				wizardIcon = new ImageIcon(SwingUtils.class.getResource("wizard.png"));
				final Font				wizardFont = wizardLabel.getFont();
				final Font				newWizardFont = new Font(wizardFont.getFontName(),Font.PLAIN,wizardFont.getSize());
				
				if (value != null) {
					if (desc.fieldType.isAssignableFrom(value.getClass())) {
						wizardLabel.setText(value.toString());
					}
					else {
						throw new IllegalArgumentException("Illegal value class ["+value.getClass()+"] to assign to wizard field");
					}
				}
				else {
					wizardLabel.setText("<null>");
				}				
				wizardLabel.setFont(newWizardFont);
				wizardLabel.setHorizontalAlignment(JLabel.LEFT);
				wizardLabel.setHorizontalTextPosition(JLabel.LEFT);
				wizardLabel.setIcon(wizardIcon);
				result = wizardLabel;
				break;
			case KEYVALUEPAIR		:
				final JLabel			pairLabel = new JLabel();
				final Icon				pairIcon = new ImageIcon(SwingUtils.class.getResource("list.png"));
				final Font				pairFont = pairLabel.getFont();
				final Font				newPairFont = new Font(pairFont.getFontName(),Font.PLAIN,pairFont.getSize());
				
				if (value != null) {
					if (desc.fieldType.isAssignableFrom(value.getClass())) {
						pairLabel.setText(value.toString());
					}
					else {
						throw new IllegalArgumentException("Illegal Array/Collection value class ["+value.getClass()+"] to assign to key/value field");
					}
				}
				else {
					pairLabel.setText("<null>");
				}				
				pairLabel.setFont(newPairFont);
				pairLabel.setHorizontalAlignment(JLabel.LEFT);
				pairLabel.setHorizontalTextPosition(JLabel.LEFT);
				pairLabel.setIcon(pairIcon);
				result = pairLabel;
				break;
			default : throw new UnsupportedOperationException("Control representation ["+desc.fieldRepresentation+"] is not supported yet");
		}

		if (desc.fieldFormat.isMandatory()) {
			result.setForeground(MANDATORY_FOREGROUND);
		}
		else {
			result.setForeground(OPTIONAL_FOREGROUND);
		}
		return result;
	}
	
	public static JComponent prepareCellEditorComponent(final Localizer localizer, final FieldDescriptor desc, final Object value) {
		JComponent		result;
		
		switch (desc.fieldRepresentation) {
			case BOOLVALUE			: 
				final JCheckBox				checkBox = new JBooleanEditor(localizer,desc);
				
				if (value != null) {
					if (value instanceof Boolean) {
						checkBox.setSelected((Boolean)value);
					}
					else {
						throw new IllegalArgumentException("Illegal value class ["+value.getClass()+"] to assign to check box");
					}
				}
				result = checkBox;
				break;
			case INTVALUE			:
				final JIntValueEditor	integerField = new JIntValueEditor(localizer,desc);
				
				if (value != null) {
					if ((value instanceof Integer) || (value instanceof Long)) {
						integerField.setValue(value);
					}
					else {
						throw new IllegalArgumentException("Illegal value class ["+value.getClass()+"] to assign to integer field");
					}
				}
				else {
					integerField.setValue(new Integer(0));
				}
				result = integerField;
				break;
			case REALVALUE			:
				final JRealValueEditor		realField = new JRealValueEditor(localizer,desc);
				
				if (value != null) {
					if ((value instanceof Float) || (value instanceof Double)) {
						realField.setValue(value);
					}
					else {
						throw new IllegalArgumentException("Illegal value class ["+value.getClass()+"] to assign to real field");
					}
				}
				else {
					realField.setValue(new Float(0.0f));
				}
				result = realField;
				break;
			case CURRENCYVALUE		:
				final JFormattedTextField	currencyField = new JFormattedTextField(getFormatter(localizer,desc.fieldType,desc.fieldFormat)) { private static final long serialVersionUID = 1L;
												@Override
												public JToolTip createToolTip() {
													return new SmartToolTip(localizer,this);
												}
											};
				final Font					currencyFont = currencyField.getFont();
				final Font					newCurrencyFont = new Font(currencyFont.getFontName(),Font.BOLD,currencyFont.getSize());
				
				if (value != null) {
					if ((value instanceof BigDecimal) || (value instanceof BigInteger)) {
						currencyField.setValue(value);
					}
					else {
						throw new IllegalArgumentException("Illegal value class ["+value.getClass()+"] to assign to currency field");
					}
				}
				else if (desc.fieldType.isAssignableFrom(BigInteger.class)) {
					currencyField.setValue(BigInteger.valueOf(0L));
				}
				else {
					currencyField.setValue(new BigDecimal(0.0));
				}
				currencyField.setFont(newCurrencyFont);
				currencyField.setHorizontalAlignment(JFormattedTextField.RIGHT);
				currencyField.setColumns(desc.fieldLen);
				result = currencyField;
				break;
			case DATEVALUE			:
				final JFormattedTextField	dateField = new JFormattedTextField(getFormatter(localizer,desc.fieldType,desc.fieldFormat)) { private static final long serialVersionUID = 1L;
												@Override
												public JToolTip createToolTip() {
													return new SmartToolTip(localizer,this);
												}
											};
				final Font					dateFont = dateField.getFont();
				final Font					newDateFont = new Font(dateFont.getFontName(),Font.PLAIN,dateFont.getSize());
				
				if (value != null) {
					if ((value instanceof Date) || (value instanceof Time) || (value instanceof Timestamp)) {
						dateField.setValue(value);
					}
					else {
						throw new IllegalArgumentException("Illegal value class ["+value.getClass()+"] to assign to date/time/timestamp field");
					}
				}
				else if (desc.fieldType.isAssignableFrom(BigInteger.class)) {
					dateField.setValue(BigInteger.valueOf(0L));
				}
				else {
					dateField.setValue(new BigDecimal(0.0));
				}
				dateField.setFont(newDateFont);
				dateField.setHorizontalAlignment(JFormattedTextField.LEFT);
				dateField.setColumns(desc.fieldLen);
				result = dateField;
				break;
			case TEXTVALUE			:
				final JTextField			textField = new JTextField() { private static final long serialVersionUID = 1L;
												@Override
												public JToolTip createToolTip() {
													return new SmartToolTip(localizer,this);
												}
											};
				final Font					textFont = textField.getFont();
				final Font					newTextFont = new Font(textFont.getFontName(),Font.PLAIN,textFont.getSize());
				
				if (value != null) {
					textField.setText(value.toString());
				}
				else {
					textField.setText(null);
				}
				textField.setFont(newTextFont);
				textField.setHorizontalAlignment(JTextField.LEFT);
				textField.setColumns(desc.fieldLen);
				result = textField;
				break;
			case FORMATTEDTEXTVALUE	:
				final JTextField			formattedField = new JTextField() { private static final long serialVersionUID = 1L;
												@Override
												public JToolTip createToolTip() {
													return new SmartToolTip(localizer,this);
												}
											};
				final Font					formattedFont = formattedField.getFont();
				final Font					newFormattedFont = new Font(formattedFont.getFontName(),Font.PLAIN,formattedFont.getSize());
				
				if (value != null) {
					formattedField.setText(value.toString());
				}
				else {
					formattedField.setText(null);
				}
				formattedField.setFont(newFormattedFont);
				formattedField.setHorizontalAlignment(JTextField.LEFT);
				formattedField.setColumns(desc.fieldLen);
				result = formattedField;
				break;
			case PASSWDVALUE		:
				final JPasswordField	passwdField = new JPasswordField(desc.fieldLen) { private static final long serialVersionUID = 1L;
											@Override
											public JToolTip createToolTip() {
												return new SmartToolTip(localizer,this);
											}
										};
				final Font				passwdFont = passwdField.getFont();
				final Font				newPasswdFont = new Font(passwdFont.getFontName(),Font.PLAIN,passwdFont.getSize());
				
				passwdField.setFont(newPasswdFont);
				passwdField.setHorizontalAlignment(JLabel.LEFT);
				passwdField.setEchoChar('*');
				result = passwdField;
				break;
			case DDLISTVALUE		:
				final JEnumEditor	ddlistField = new JEnumEditor(localizer, desc);
				
				if (value != null) {
					if (value instanceof Enum<?>) {
						ddlistField.setSelectedItem(value);
					}
					else {
						throw new IllegalArgumentException("Illegal enum value class ["+value.getClass()+"] to assign to combobox field");
					}
				}
				else {
					ddlistField.setSelectedItem(null);
				}				
				result = ddlistField;
				break;
			case LISTVALUE			:
				final JList<Object>		listField = new JList<Object>() { private static final long serialVersionUID = 1L;
											@Override
											public JToolTip createToolTip() {
												return new SmartToolTip(localizer,this);
											}
										};
				final Font				listFont = listField .getFont();
				final Font				newListFont = new Font(listFont.getFontName(),Font.PLAIN,listFont.getSize());
				int[]					indices = null;
				
				if (value != null) {
					if (value instanceof Collection) {
						listField.setListData(((Collection<?>)value).toArray());
						indices = new int[((Collection<?>)value).size()];
					}
					else if (desc.fieldType.isArray()) {
						listField.setListData((Object[])value);
						indices = new int[Array.getLength(value)];
					}
					else {
						throw new IllegalArgumentException("Illegal Array/Collection value class ["+value.getClass()+"] to assign to listbox field");
					}
					for (int index = 0, maxIndex = indices.length; index < maxIndex; index++) {
						indices[index] = index;
					}
					listField .setSelectedIndices(indices);
				}
				listField .setFont(newListFont);
				result = listField;
				break;
			case MAPVALUE			:
				final JTable			mapField = new JTable() { private static final long serialVersionUID = 1L;
											@Override
											public JToolTip createToolTip() {
												return new SmartToolTip(localizer,this);
											}
										};
				final Font				mapFont = mapField.getFont();
				final Font				newMapFont = new Font(mapFont.getFontName(),Font.PLAIN,mapFont.getSize());
				
				if (value != null) {
					if (value instanceof Map) {
//						mapField.setText(((Map<?,?>)value).size()+" items");
					}
					else {
						throw new IllegalArgumentException("Illegal Map value class ["+value.getClass()+"] to assign to map field");
					}
				}
				mapField.setFont(newMapFont);
				result = mapField;
				break;
			case WIZARDVALUE		:
				if (desc.fieldType == File.class) {
					final JFileEditor		fileEditor = new JFileEditor(localizer, desc);
					
					if (value != null) {
						if (desc.fieldType.isAssignableFrom(value.getClass())) {
							fileEditor.setText(value.toString());
						}
						else {
							throw new IllegalArgumentException("Illegal value class ["+value.getClass()+"] to assign to file wizard field");
						}
					}
					else {
						fileEditor.setText("<null>");
					}				
					result = fileEditor;
				}
				else {
					final JLabel			wizardLabel = new JLabel();
					final Icon				wizardIcon = new ImageIcon(SwingUtils.class.getResource("wizard.png"));
					final Font				wizardFont = wizardLabel.getFont();
					final Font				newWizardFont = new Font(wizardFont.getFontName(),Font.PLAIN,wizardFont.getSize());
					
					if (value != null) {
						if (desc.fieldType.isAssignableFrom(value.getClass())) {
							wizardLabel.setText(value.toString());
						}
						else {
							throw new IllegalArgumentException("Illegal value class ["+value.getClass()+"] to assign to wizard field");
						}
					}
					else {
						wizardLabel.setText("<null>");
					}				
					wizardLabel.setFont(newWizardFont);
					wizardLabel.setHorizontalAlignment(JLabel.LEFT);
					wizardLabel.setHorizontalTextPosition(JLabel.LEFT);
					wizardLabel.setIcon(wizardIcon);
					result = wizardLabel;
				}
				break;
			case KEYVALUEPAIR		:
				final JLabel			pairLabel = new JLabel();
				final Icon				pairIcon = new ImageIcon(SwingUtils.class.getResource("list.png"));
				final Font				pairFont = pairLabel.getFont();
				final Font				newPairFont = new Font(pairFont.getFontName(),Font.PLAIN,pairFont.getSize());
				
				if (value != null) {
					if (desc.fieldType.isAssignableFrom(value.getClass())) {
						pairLabel.setText(value.toString());
					}
					else {
						throw new IllegalArgumentException("Illegal Array/Collection value class ["+value.getClass()+"] to assign to key/value field");
					}
				}
				else {
					pairLabel.setText("<null>");
				}				
				pairLabel.setFont(newPairFont);
				pairLabel.setHorizontalAlignment(JLabel.LEFT);
				pairLabel.setHorizontalTextPosition(JLabel.LEFT);
				pairLabel.setIcon(pairIcon);
				result = pairLabel;
				break;
			default : throw new UnsupportedOperationException("Control representation ["+desc.fieldRepresentation+"] is not supported yet");
		}

		if (desc.fieldFormat.isMandatory()) {
			result.setBackground(MANDATORY_BACKGROUND);
			result.setForeground(MANDATORY_FOREGROUND);
		}
		else if (desc.fieldFormat.isReadOnly()) {
			result.setBackground(READONLY_BACKGROUND);
			result.setForeground(READONLY_FOREGROUND);
			if (result instanceof JTextComponent) {
				((JTextComponent)result).setEditable(false);
			}
			else {
				result.setEnabled(false);
			}
		}
		else {
			result.setBackground(OPTIONAL_BACKGROUND);
			result.setForeground(OPTIONAL_FOREGROUND);
		}
		return result;
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
	 * <p>Assign standard F1 key listener for the given component to show standard Help screen in the Swing-based applications</p>
	 * @param component component to assign F1 key to
	 * @param localizer localizer to load help content from
	 * @param helpId help content key in the localizer 
	 * @throws NullPointerException if component or localizer is null
	 * @throws IllegalArgumentException if helpId is null or empty or is missing in the localizer
	 */
	public static void assignHelpKey(final JComponent component, final Localizer localizer, final String helpId) throws NullPointerException, IllegalArgumentException {
		if (component == null) {
			throw new NullPointerException("Component can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (helpId == null || helpId.isEmpty()) {
			throw new IllegalArgumentException("Help identifier can't eb null or empty"); 
		}
		else if (!localizer.containsKey(helpId)) {
			throw new IllegalArgumentException("Help identifier ["+helpId+"] is missing in the localizer passed");
		}
		else {
			component.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtils.KS_HELP,SwingUtils.ACTION_HELP);
			component.getActionMap().put(SwingUtils.ACTION_HELP,new AbstractAction() {private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					try{SwingUtils.showHelpScreen(localizer,helpId);
					} catch (LocalizationException | ContentException exc) {
					}
				}
			});
		}
	}

	/**
	 * <p>How standard help screen in the Swing-based applications</p> 
	 * @param localizer localizer to use for getting help content
	 * @param helpId help content key in the localizer
	 * @throws LocalizationException on any errors in the localizer
	 * @throws ContentException on any errors in the help content
	 */
	public static void showHelpScreen(final Localizer localizer, final String helpId) throws LocalizationException, ContentException {
		final JDialog				dialog = new JDialog((JDialog)null);
		final SimpleHelpComponent	help = new SimpleHelpComponent(localizer,helpId);
		final JPanel				contentPane = (JPanel)dialog.getContentPane(); 

		dialog.setTitle(localizer.getValue(PureLibLocalizer.TITLE_HELP_SCREEN));
		contentPane.setLayout(new BorderLayout());
		contentPane.add(help,BorderLayout.CENTER);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		
		contentPane.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtils.KS_EXIT,SwingUtils.ACTION_EXIT);
		contentPane.getActionMap().put(SwingUtils.ACTION_EXIT,new AbstractAction() {private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		
		centerMainWindow(dialog,0.75f);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setVisible(true);
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
		public JToolTip createToolTip() {
			return new SmartToolTip(localizer,this);
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
		public JToolTip createToolTip() {
			return new SmartToolTip(localizer,this);
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
		public JToolTip createToolTip() {
			return new SmartToolTip(localizer,this);
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
		public JToolTip createToolTip() {
			return new SmartToolTip(localizer,this);
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
		public JToolTip createToolTip() {
			return new SmartToolTip(localizer,this);
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
	
