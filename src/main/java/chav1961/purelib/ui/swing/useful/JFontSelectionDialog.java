package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.interfaces.FontStyleType;

/**
 * <p>This class supports {@linkplain Font} choosing.</p>
 * <p>Example:</p>
 * <pre>
 *   JFontChooser fontChooser = new JFontChooser(...);
 *   
 *   if (fontChooser.selectFont(parent)) {
 *   	Font font = fontChooser.getSelectedFont(); 
 *   	System.out.println("Selected Font : " + font); 
 *   }
 * </pre>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 *
 **/

public class JFontSelectionDialog extends JPanel {
	private static final long serialVersionUID = -1901350778269800469L;
	
	private static final String 	KEY_SELECT_FONT = "chav1961.purelib.ui.swing.useful.JFontSelectionDialog.selectFont";
	private static final String 	KEY_FONT_NAME = "chav1961.purelib.ui.swing.useful.JFontSelectionDialog.fontName";
	private static final String 	KEY_FONT_STYLE = "chav1961.purelib.ui.swing.useful.JFontSelectionDialog.fontStyle";
	private static final String 	KEY_FONT_SIZE = "chav1961.purelib.ui.swing.useful.JFontSelectionDialog.fontSize";
	private static final String 	KEY_FONT_SAMPLE = "chav1961.purelib.ui.swing.useful.JFontSelectionDialog.sample";
	private static final String 	KEY_FONT_SAMPLE_TEXT = "chav1961.purelib.ui.swing.useful.JFontSelectionDialog.sampleText";
	private static final Font 		DEFAULT_FONT = new Font("Monospace", Font.PLAIN, 12);
	private static final int[]		DEFAULT_FONT_SIZES = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72};

	private final Localizer				localizer;
	private final JTextField 			fontFamilyTextField = new JTextField();
	private final JTextField 			fontStyleTextField = new JTextField();
	private final JFormattedTextField 	fontSizeTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JLabel 				fontNameLabel = new JLabel();
	private final JLabel 				fontStyleLabel = new JLabel();
	private final JLabel 				fontSizeLabel = new JLabel();
	private final TitledBorder 			titledBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), ""); 
	private final JTextField 			sampleText = new JTextField();
	private final int[] 				fontSizes;
	private final String[] 				fontFamilyNames;
	private final JList<String> 		fontNameList;
	private final JList<FontStyleType>	fontStyleList;
	private final JList<Integer> 		fontSizeList;

	/**
	 * <p>Constructor of the class</p>
	 **/
	public JFontSelectionDialog(final Localizer localizer) {
		this(localizer, DEFAULT_FONT_SIZES);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param fontSizes  the array of font sizes to select. Can't be null or empty
	 **/
	public JFontSelectionDialog(final Localizer localizer, final int... fontSizes) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (fontSizes == null || fontSizes.length == 0) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			final GraphicsEnvironment 	env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			
			this.localizer = localizer;
			this.fontSizes = fontSizes;
			this.fontFamilyNames = env.getAvailableFontFamilyNames();
			
			fontFamilyTextField.setText(DEFAULT_FONT.getFamily());
			fontStyleTextField.setText(FontStyleType.PLAIN.name());
			fontSizeTextField.setValue(DEFAULT_FONT.getSize());
			
			final JPanel 		selectPanel = new JPanel();
			
			selectPanel.setLayout(new BoxLayout(selectPanel, BoxLayout.X_AXIS));
			
			final JPanel 		fontNamePanel = new JPanel(new BorderLayout());
			
			fontNamePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			fontNamePanel.setPreferredSize(new Dimension(180, 130));

			this.fontNameList = new JList<String>(fontFamilyNames);
			
			fontNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			fontNameList.setSelectedIndex(0);
			fontNameList.setFocusable(false);
			
			final JScrollPane 	nameScrollPane = new JScrollPane(fontNameList);
			
			nameScrollPane.getVerticalScrollBar().setFocusable(false);
			nameScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			final JPanel 		pName = new JPanel(new BorderLayout());
			
			pName.add(fontFamilyTextField, BorderLayout.NORTH);
			pName.add(nameScrollPane, BorderLayout.CENTER);

			fontNamePanel.add(this.fontNameLabel, BorderLayout.NORTH);
			fontNamePanel.add(pName, BorderLayout.CENTER);
			
			selectPanel.add(fontNamePanel);

			final JPanel		fontStylePanel = new JPanel(new BorderLayout());
			
			fontStylePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			fontStylePanel.setPreferredSize(new Dimension(140, 130));

			this.fontStyleList = new JList<FontStyleType>(FontStyleType.values());
			
			fontStyleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			fontStyleList.setSelectedIndex(0);
			fontStyleList.setFocusable(false);
			
			final JScrollPane 	styleScrollPane = new JScrollPane(fontStyleList);
			
			styleScrollPane.getVerticalScrollBar().setFocusable(false);
			styleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			final JPanel 		pStyle = new JPanel(new BorderLayout());
			
			pStyle.add(fontStyleTextField, BorderLayout.NORTH);
			pStyle.add(styleScrollPane, BorderLayout.CENTER);

			fontStylePanel.add(fontStyleLabel, BorderLayout.NORTH);
			fontStylePanel.add(pStyle, BorderLayout.CENTER);
			
			selectPanel.add(fontStylePanel);

			final JPanel		fontSizePanel = new JPanel(new BorderLayout());
			
			fontSizePanel.setPreferredSize(new Dimension(70, 130));
			fontSizePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			this.fontSizeList = new JList<Integer>(Utils.wrapArray(this.fontSizes));
			
			fontSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			fontSizeList.setSelectedIndex(0);
			fontSizeList.setFocusable(false);
			
			final JScrollPane 	siseSrollPane = new JScrollPane(fontSizeList);
			
			siseSrollPane.getVerticalScrollBar().setFocusable(false);
			siseSrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			final JPanel 		pSize = new JPanel(new BorderLayout());
			
			pSize.add(fontSizeTextField, BorderLayout.NORTH);
			pSize.add(siseSrollPane, BorderLayout.CENTER);

			fontSizePanel.add(fontSizeLabel, BorderLayout.NORTH);
			fontSizePanel.add(pSize, BorderLayout.CENTER);

			selectPanel.add(fontSizePanel);
			
			final Border 	empty = BorderFactory.createEmptyBorder(5, 10, 10, 10);
			final Border 	border = BorderFactory.createCompoundBorder(titledBorder, empty);
			final JPanel	samplePanel = new JPanel();
			
			samplePanel.setLayout(new BorderLayout());
			samplePanel.setBorder(border);

			final Border 	lowered = BorderFactory.createLoweredBevelBorder();

			sampleText.setBorder(lowered);
			sampleText.setPreferredSize(new Dimension(300, 100));
			
			samplePanel.add(sampleText, BorderLayout.CENTER);

			final JPanel 	contentsPanel = new JPanel();
			
			contentsPanel.setLayout(new GridLayout(2, 1));
			contentsPanel.add(selectPanel, BorderLayout.NORTH);
			contentsPanel.add(samplePanel, BorderLayout.CENTER);
	
			this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			this.add(contentsPanel);
			this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
			final Font	font = (Font)UIManager.getLookAndFeelDefaults().get("defaultFont");
			
			this.setSelectedFont(font != null ? font : DEFAULT_FONT);
			
			fontFamilyTextField.addFocusListener(new TextFieldFocusHandlerForTextSelection(fontFamilyTextField));
			fontFamilyTextField.addKeyListener(new TextFieldKeyHandlerForListSelectionUpDown(fontNameList));
			fontFamilyTextField.getDocument().addDocumentListener(new ListSearchTextFieldDocumentHandler(fontNameList));
			
			fontStyleTextField.addFocusListener(new TextFieldFocusHandlerForTextSelection(fontStyleTextField));
			fontStyleTextField.addKeyListener(new TextFieldKeyHandlerForListSelectionUpDown(fontStyleList));
			fontStyleTextField.getDocument().addDocumentListener(new ListSearchTextFieldDocumentHandler(fontStyleList));

			fontSizeTextField.addFocusListener(new TextFieldFocusHandlerForTextSelection(fontSizeTextField));
			fontSizeTextField.addKeyListener(new TextFieldKeyHandlerForListSelectionUpDown(fontSizeList));
			fontSizeTextField.getDocument().addDocumentListener(new ListSearchTextFieldDocumentHandler(fontSizeList));

			fontNameList.addListSelectionListener((e)->{
				if (e.getValueIsAdjusting() == false) {
					final String selectedValue = fontNameList.getSelectedValue();
					final String oldValue = fontFamilyTextField.getText();
					
					fontFamilyTextField.setText(selectedValue);
					if (!oldValue.equalsIgnoreCase(selectedValue)) {
						fontFamilyTextField.selectAll();
						fontFamilyTextField.requestFocusInWindow();
					}
					updateSampleFont();
				}
			});
			fontStyleList.addListSelectionListener((e)->{
				if (e.getValueIsAdjusting() == false) {
					final FontStyleType selectedValue = fontStyleList.getSelectedValue();
					final FontStyleType oldValue = FontStyleType.valueOf(fontStyleTextField.getText());
					
					fontStyleTextField.setText(selectedValue.name());
					if (!oldValue.equals(selectedValue)) {
						fontStyleTextField.selectAll();
						fontStyleTextField.requestFocus();
					}
					updateSampleFont();
				}
			});
			fontSizeList.addListSelectionListener((e)->{
				if (e.getValueIsAdjusting() == false) {
					final Integer selectedValue = fontSizeList.getSelectedValue();
					final Integer oldValue = Integer.valueOf(fontSizeTextField.getText());
					
					fontSizeTextField.setValue(selectedValue);
					if (!oldValue.equals(selectedValue)) {
						fontSizeTextField.selectAll();
						fontSizeTextField.requestFocus();
					}
					updateSampleFont();
				}
			});
			
			fillLocalizedStrings();
		}
	}


	/**
	 * Get the family name of the selected font.
	 * @return  the font family of the selected font.
	 *
	 * @see #setSelectedFontFamily
	 **/
	public String getSelectedFontFamily() {
		return fontNameList.getSelectedValue();
	}

	/**
	 * Get the style of the selected font.
	 * @return  the style of the selected font.
	 *          <code>Font.PLAIN</code>, <code>Font.BOLD</code>,
	 *          <code>Font.ITALIC</code>, <code>Font.BOLD|Font.ITALIC</code>
	 *
	 * @see java.awt.Font#PLAIN
	 * @see java.awt.Font#BOLD
	 * @see java.awt.Font#ITALIC
	 * @see #setSelectedFontStyle
	 **/
	public int getSelectedFontStyle() {
		return fontStyleList.getSelectedValue().getFontStyle();
	}

	/**
	 * Get the size of the selected font.
	 * @return  the size of the selected font
	 *
	 * @see #setSelectedFontSize
	 **/
	public int getSelectedFontSize() {
		return ((Number)fontSizeTextField.getValue()).intValue();
	}

	/**
	 * Get the selected font.
	 * @return  the selected font
	 *
	 * @see #setSelectedFont
	 * @see java.awt.Font
	 **/
	public Font getSelectedFont() {
		return new Font(getSelectedFontFamily(), getSelectedFontStyle(), getSelectedFontSize());
	}

	/**
	 * Set the family name of the selected font.
	 * @param name  the family name of the selected font. 
	 *
	 * @see getSelectedFontFamily
	 **/
	public void setSelectedFontFamily(final String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Font family can't be null or empty"); 
		}
		else {
			for (int i = 0; i < fontFamilyNames.length; i++) {
				if (fontFamilyNames[i].equalsIgnoreCase(name)) {
					fontNameList.setSelectedIndex(i);
					break;
				}
			}
			updateSampleFont();
		}
	}

	/**
	 * Set the style of the selected font.
	 * @param style  the size of the selected font.
	 *               <code>Font.PLAIN</code>, <code>Font.BOLD</code>,
	 *               <code>Font.ITALIC</code>, or
	 *               <code>Font.BOLD|Font.ITALIC</code>.
	 *
	 * @see java.awt.Font#PLAIN
	 * @see java.awt.Font#BOLD
	 * @see java.awt.Font#ITALIC
	 * @see #getSelectedFontStyle
	 **/
	public void setSelectedFontStyle(final int style) {
		final FontStyleType[] content = FontStyleType.values();
		
		for (int i = 0; i < content.length; i++) {
			if (content[i].getFontStyle() == style) {
				fontStyleList.setSelectedIndex(i);
				updateSampleFont();
				return;
			}
		}
		throw new IllegalArgumentException("Illegal font style ["+style+"] to set. Only Font.PLAIN, Font.BOLD, Font.ITALIC and (Font.BOLD | Font.ITALIC) are available"); 
	}

	/**
	 * Set the size of the selected font.
	 * @param size the size of the selected font
	 *
	 * @see #getSelectedFontSize
	 **/
	public void setSelectedFontSize(final int size) {
		for (int i = 0; i < this.fontSizes.length; i++) {
			if (this.fontSizes[i] == size) {
				fontSizeList.setSelectedIndex(i);
				fontSizeTextField.setValue(size);
				updateSampleFont();
				return;
			}
		}
		throw new IllegalArgumentException("Illegal font size ["+size+"] to set. Only "+Arrays.toString(fontSizes)+" are available");
	}

	/**
	 * Set the selected font.
	 * @param font the selected font
	 *
	 * @see #getSelectedFont
	 * @see java.awt.Font
	 **/
	public void setSelectedFont(final Font font) {
		if (font == null) {
			throw new NullPointerException("Font to set can't be null");
		}
		else {
			setSelectedFontFamily(font.getFamily());
			setSelectedFontStyle(font.getStyle());
			setSelectedFontSize(font.getSize());
		}
	}

	/**
	 *  Show font selection dialog.
	 *  @param parent Dialog's Parent component.
	 *  @return OK_OPTION, CANCEL_OPTION or ERROR_OPTION
	 *
	 **/
	public boolean selectFont(final Component parent) {
		final JFrame			frame = parent instanceof Frame ? (JFrame) parent : (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, parent);
		final JDialogContainer	container = new JDialogContainer(localizer, frame, KEY_SELECT_FONT, this);
		
		SwingUtils.refreshLocale(this, localizer.currentLocale().getLocale(), localizer.currentLocale().getLocale());

		setPreferredSize(new Dimension(640,350));
		return container.showDialog();
	}

	private void fillLocalizedStrings() {
		fontNameLabel.setText(localizer.getValue(KEY_FONT_NAME));
		fontStyleLabel.setText(localizer.getValue(KEY_FONT_STYLE));
		fontSizeLabel.setText(localizer.getValue(KEY_FONT_SIZE));
		titledBorder.setTitle(localizer.getValue(KEY_FONT_SAMPLE));
		sampleText.setText(localizer.getValue(KEY_FONT_SAMPLE_TEXT));
	}

	private class TextFieldFocusHandlerForTextSelection implements FocusListener {
		private JTextComponent textComponent;

		public TextFieldFocusHandlerForTextSelection(final JTextComponent textComponent) {
			this.textComponent = textComponent;
		}

		@Override
		public void focusGained(final FocusEvent e) {
			textComponent.selectAll();
		}

		@Override
		public void focusLost(final FocusEvent e) {
			textComponent.select(0, 0);
			updateSampleFont();
		}
	}

	private static class TextFieldKeyHandlerForListSelectionUpDown extends KeyAdapter {
		private final JList<?>	targetList;

		private TextFieldKeyHandlerForListSelectionUpDown(final JList<?> list) {
			this.targetList = list;
		}

		public void keyPressed(KeyEvent e) {
			final int listSize = targetList.getModel().getSize();
			final int i = targetList.getSelectedIndex();
			
			switch (e.getKeyCode()) {
				case KeyEvent.VK_UP		:	targetList.setSelectedIndex(Math.max(0, i - 1));			break;
				case KeyEvent.VK_DOWN	:	targetList.setSelectedIndex(Math.min(i + 1, listSize - 1));	break;
				default : break;
			}
		}
	}

	private class ListSearchTextFieldDocumentHandler implements DocumentListener {
		final JList<?> targetList;

		public ListSearchTextFieldDocumentHandler(final JList<?> targetList) {
			this.targetList = targetList;
		}

		@Override
		public void insertUpdate(final DocumentEvent e) {
			update(e);
		}

		@Override
		public void removeUpdate(final DocumentEvent e) {
			update(e);
		}

		@Override
		public void changedUpdate(final DocumentEvent e) {
			update(e);
		}

		private void update(DocumentEvent event) {
			String newValue = "";
			try {
				Document doc = event.getDocument();
				newValue = doc.getText(0, doc.getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

			if (newValue.length() > 0) {
				final int index = Math.max(Math.min(0, targetList.getNextMatch(newValue, 0, Position.Bias.Forward)), targetList.getModel().getSize() - 1); 
				
				targetList.ensureIndexIsVisible(index);

				final String matchedName = targetList.getModel().getElementAt(index).toString();
				
				if (newValue.equalsIgnoreCase(matchedName)) {
					if (index != targetList.getSelectedIndex()) {
						SwingUtilities.invokeLater(()->targetList.setSelectedIndex(index));
					}
				}
			}
		}
	}

	private void updateSampleFont() {
		sampleText.setFont(getSelectedFont());
	}

	public static void main(final String[] args) {
		final JFontSelectionDialog	chooser = new JFontSelectionDialog(PureLibSettings.PURELIB_LOCALIZER);
		final boolean				result = chooser.selectFont(null);
		
		System.err.println("Font: "+chooser.getSelectedFont()+", result = "+result);
	}
}
