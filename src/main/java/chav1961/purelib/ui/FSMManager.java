package chav1961.purelib.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonSaxParser;
import chav1961.purelib.streams.interfaces.JsonSaxHandler;

public class FSMManager extends JDialog {
	private static final long 	serialVersionUID = 3649621908902703598L;

	public static final String	TEXT = "[{\"state\":1,\"caption\":\"my caption\",\"text\":\"MY TEXT 1\",\"image\":\"myImage.png\",\"items\":[\"x\",\"y\",\"z\"],\"terminal\":false,\"tooltip\":\"My tooltip\",\"jumps\":[{\"terminal\":\"forward\",\"newState\":2,\"actionCommand\":\"1-2\"}]}"	
									   +",{\"state\":2,\"caption\":\"my caption\",\"text\":\"MY TEXT 2\",\"image\":\"myImage.png\",\"items\":[\"t\"],\"terminal\":true,\"tooltip\":\"My tooltip\"}]";	

	private static final String			BACKWARD_CAPTION = "B";
	private static final String			BACKWARD_ACTION = "backward";
	private static final String			FORWARD_CAPTION = "F";
	private static final String			FORWARD_ACTION = "forward";
	private static final String			CANCEL_CAPTION = "C";
	private static final String			CANCEL_ACTION = "cancel";
	private static final String			FINISH_CAPTION = "ZZZ";
	private static final Set<Class<?>>	FORMATTED_ITEMS = new HashSet<>();

	static {
		FORMATTED_ITEMS.addAll(Arrays.asList(int.class,long.class,float.class,double.class,Integer.class,Long.class,Float.class,Double.class
							  				,BigInteger.class,BigDecimal.class,Date.class,Calendar.class));
	}
									
	private final String			title;
	private final Object			persistent;
	private final FSMItem[]			table;
	private final JPanel			content = new JPanel(new SpringLayout());
	private final JPanel			contentAndText = new JPanel();
	private final JLabel			icon = new JLabel();
	private final JLabel			message = new JLabel(" ");
	private final JTextPane			text = new JTextPane();
	private final JButton			backward = new JButton(BACKWARD_CAPTION), forward = new JButton(FORWARD_CAPTION), cancel = new JButton(CANCEL_CAPTION);
	private final ActionListener	buttonListener = new ActionListener(){
										@Override
										public void actionPerformed(final ActionEvent event) {
											switch (event.getActionCommand()) {
												case BACKWARD_ACTION	:
													actualState = changeState(actualState,BACKWARD_ACTION,history.size() == 1 ? history.get(0) : history.remove(history.size()-2));
													fillScreen();
													break;
												case FORWARD_ACTION		:
													final FSMItem	item = actualItem();
													
													if (item.terminal) {
														success();
														setVisible(false);
													}
													else {
														history.add(actualState);
														actualState = changeState(actualState,FORWARD_ACTION,nextState(actualState,FORWARD_ACTION));
														fillScreen();
													}
													break;
												case CANCEL_ACTION		:
													setVisible(false);
													break;
											}
										}
									};
	private final KeyListener		totalKeyListener = new KeyListener(){
										@Override public void keyTyped(KeyEvent e) {}
										
										@Override 
										public void keyPressed(KeyEvent e) {
											if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
												setVisible(false);
											}
											else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
												final FSMItem	item = actualItem();
												
												if (item.terminal) {
													success();
													setVisible(false);
												}
												else {
													history.add(actualState);
													actualState = changeState(actualState,FORWARD_ACTION,nextState(actualState,FORWARD_ACTION));
													fillScreen();
												}
											}
										}
								
										@Override public void keyReleased(KeyEvent e) {}		
									};
	private final KeyListener		reducedKeyListener = new KeyListener(){
										@Override public void keyTyped(KeyEvent e) {}
										
										@Override 
										public void keyPressed(KeyEvent e) {
											if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
												setVisible(false);
											}
										}
								
										@Override public void keyReleased(KeyEvent e) {}		
									};
	private final InputVerifier 	verifier = new InputVerifier(){
	     								@Override
									     public boolean verify(JComponent input) {
									         if (input instanceof JFormattedTextField) {
									             final JFormattedTextField 	ftf = (JFormattedTextField)input;
									             final AbstractFormatter 	formatter = ftf.getFormatter();
									             
									             if (formatter != null) {
									                 String text = ftf.getText();
									                 try{formatter.stringToValue(text);
									                      return true;
									                  } catch (ParseException pe) {
									                	  message(pe.getMessage());
									                      return false;
									                  }
									              }
									          }
									          return true;
									     }
									     
									     @Override
									     public boolean shouldYieldFocus(JComponent input) {
									          return verify(input);
									     }
									  };
	private final FocusListener		helper = new FocusListener(){
										@Override
										public void focusGained(FocusEvent e) {
											for (FieldEnumerator field : collectItems4Frame(actualItem().items)) {
												if (field.field.getName().equals(e.getComponent().getName())) {
													message(field.desc.tooltip());
												}
											}
										}
								
										@Override public void focusLost(FocusEvent e) {}
									};
	
	private final Map<String,Image>	images = new HashMap<>();
	private final List<Integer>		history = new ArrayList<>();
	private final int				initialState;
	private int						actualState;
	private boolean					alreadyShowed = false;
	
	
	public FSMManager(final Window owner, final String title, final Dialog.ModalityType modalityType, final Object persistent, final String fsmTable, final int initialState) {
		super(owner,title,modalityType);
		if (persistent == null) {
			throw new IllegalArgumentException("Persistent object can't be null");
		}
		else if (fsmTable == null || fsmTable.isEmpty()) {
			throw new IllegalArgumentException("FSM table descriptor can't be null or empty");
		}
		else {
			this.title = title;
			this.persistent = persistent;
			this.table = loadFSMTable(fsmTable);
			this.initialState = initialState;
			this.actualState = initialState;
			checkParameters();
			prepareBackgroundControls();
		}
	}
	
	public FSMManager(final Frame owner, final String title, final boolean modal, final Object persistent, final String fsmTable, final int initialState) {
		super(owner,title,modal);
		if (persistent == null) {
			throw new IllegalArgumentException("Persistent object can't be null");
		}
		else if (fsmTable == null || fsmTable.isEmpty()) {
			throw new IllegalArgumentException("FSM table descriptor can't be null or empty");
		}
		else {
			this.title = title;
			this.persistent = persistent;
			this.table = loadFSMTable(fsmTable);
			this.initialState = initialState;
			this.actualState = initialState;
			checkParameters();
			prepareBackgroundControls();
		}
	}

	public FSMManager(final Dialog owner, final String title, final boolean modal, final Object persistent, final String fsmTable, final int initialState) {
		super(owner,title,modal);
		if (persistent == null) {
			throw new IllegalArgumentException("Persistent object can't be null");
		}
		else if (fsmTable == null || fsmTable.isEmpty()) {
			throw new IllegalArgumentException("FSM table descriptor can't be null or empty");
		}
		else {
			this.title = title;
			this.persistent = persistent;
			this.table = loadFSMTable(fsmTable);
			this.initialState = initialState;
			this.actualState = initialState;
			checkParameters();
			prepareBackgroundControls();
		}
	}

	@Override
	public void setVisible(final boolean visible) {
		if (visible) {
			if (alreadyShowed) {
				throw new IllegalStateException("Attempt to use FSMManager instance twice!"); 
			}
			else {
				alreadyShowed = true;
				fillScreen();
				super.setVisible(visible);
			}
		}
		else {
			super.setVisible(visible);
			images.clear();
			dispose();
		}
	}

	protected void success() {
		dump();
		System.err.println("Success");
	}

	protected int changeState(final int oldState, final String terminal, final int newState) {
		dump();
		System.err.println("From "+oldState+" by "+terminal+" to "+newState);
		return newState;
	}


	protected Object getValue(final Object persistent, final FieldEnumerator var) {
		try{return var.field.get(persistent);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			message("Field ["+var.desc.caption()+"]: error getting value "+e.getMessage());
			return null;
		} 
	}
	
	protected void setValue(final Object persistent, final FieldEnumerator var, final Object value) {
		try{var.field.set(persistent,value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			message("Field ["+var.desc.caption()+"]: error setting value "+e.getMessage());
		} 
	}
	
	protected void message(final String message) {
		this.message.setText(' '+message);
	}
	
	private int nextState(final int actualState, final String terminal) {
		for (FSMItem line : table) {
			if (line.state == actualState) {
				for (FSMJump jump : line.jumps) {
					if (jump.terminal.equals(terminal)) {
						return jump.newState;
					}
				}
			}			
		}
		return actualState;
	}

	
	private FSMItem[] loadFSMTable(final String fsmTable) {
		final List<FSMItem>		result = new ArrayList<>();
		final List<String>		names = new ArrayList<>();
		final List<FSMJump>		jumps = new ArrayList<>();
		
		try{new JsonSaxParser(new JsonSaxHandler() {
				int			arrayLevel = 0, objectLevel = 0;
				boolean		toItems = false, toJumps = false;
				FSMItem		item;
				String		name;
				FSMJump		jump;

				@Override public void startDoc() {}
				@Override public void endDoc() {}
				
				@Override
				public void startObj() {
					switch (++objectLevel) {
						case 1 : item =	new FSMItem(); break;
						case 2 : jump =	new FSMJump(); break;
					};
				}

				@Override
				public void endObj() {
					switch (objectLevel--) {
						case 1 : result.add(item); break;
						case 2 : jumps.add(jump); break;
					};
				}

				@Override
				public void startArr() {
					switch (++arrayLevel) {
						case 1 : 
							break;
						case 2 : 
							toItems = "items".equals(name);
							toJumps = "jumps".equals(name);
							break;
					}
				}

				@Override
				public void endArr() {
					switch (arrayLevel--) {
						case 1 :
							break;
						case 2 : 
							if (toJumps) {
								item.jumps = jumps.toArray(new FSMJump[jumps.size()]);
								jumps.clear();
								toJumps = false;
							}
							if (toItems) {
								item.items = names.toArray(new String[names.size()]);
								names.clear();
								toItems = false;
							}
							break;
					};
				}
				
				@Override public void startName(long id) {}
				@Override public void startName(String name) {
					this.name = name;}
				@Override public void startName(char[] data, int from, int len) {startName(new String(data,from,len));}
				@Override public void endName() {}
				
				@Override public void startIndex(int index) {}
				@Override public void endIndex() {}
				
				@Override public void value() {}
				@Override public void value(double data) {}
				@Override public void value(char[] data, int from, int len) {value(new String(data,from,len));}

				@Override
				public void value(boolean data) {
					switch (name) {
						case "terminal"			: item.terminal = data; break;
					}
				}
				
				@Override 
				public void value(long data) {
					switch (name) {
						case "state"			: item.state = (int)data; break;
						case "newState"			: jump.newState = (int)data; break;
					}
				}
				
				@Override
				public void value(String data) {
					if (toItems) {
						names.add(data);
					}
					else {
						switch (name) {
							case "caption"			: item.caption = data; break;
							case "text"				: item.text = data; break;
							case "image"			: item.image = data; break;
							case "tooltip"			: item.tooltip = data; break;
							case "terminal"			: jump.terminal = data; break;
							case "actionCommand"	: jump.actionCommand = data; break;	
						}
					}
				}				
			}).parse(fsmTable);
		
			return result.toArray(new FSMItem[result.size()]);
		} catch (IOException | SyntaxException e) {
			e.printStackTrace();
			return new FSMItem[0];
		}
	}

	private void checkParameters() {
		final Class<?>	cl = persistent.getClass();
		
		if (!cl.isAnnotationPresent(WClass.class)) {
			throw new IllegalArgumentException("Persistence class instance is not marked by @WClass annotation");
		}
		else {
			final Set<String>	names = new HashSet<>();
			
			for (Field f : cl.getFields()) {
				if (f.isAnnotationPresent(WField.class)) {
					if (names.contains(f.getAnnotation(WField.class).name())) {
						throw new IllegalArgumentException("Persistence class instance field ["+f.getName()+"] has duplicate name ["+f.getAnnotation(WField.class).name()+"] in the @WField annotation");
					}
					else {
						names.add(f.getAnnotation(WField.class).name());
					}
				}
			}
			if (names.size() == 0) {
				throw new IllegalArgumentException("Persistence class instance has no fields marked by @WField annotation");
			}
			else {
				final Set<Integer>	states = new HashSet<>();
				boolean				wasTerminal = false;
				
				for (FSMItem item : table) {
					for (String name : item.items) {
						if (!names.contains(name)) {
							throw new IllegalArgumentException("FSM table for state ["+item.state+"] has an item reference ["+name+"] to non-existent name in the persistence class instance. Check @WField anoontations for it");
						}
					}
					if (states.contains(item.state)) {
						throw new IllegalArgumentException("FSM table has duplicated records for state ["+item.state+"]");
					}
					else {
						states.add(item.state);
					}
					if (item.terminal && item.jumps != null) {
						throw new IllegalArgumentException("FSM table has record for state ["+item.state+"] with 'terminal':true and non-empty jumps table. This is a mutually exclusive options");
					}
					wasTerminal |= item.terminal;
					images.put(item.image,null);
				}
				if (!wasTerminal) {
					throw new IllegalArgumentException("FSM table has no one record with the \"terminal\":true. At least one record need be presented");
				}
				for (FSMItem item : table) {
					if (item.jumps != null) {
						for (FSMJump jump : item.jumps) {
							if (!states.contains(jump.newState)) {
								throw new IllegalArgumentException("FSM table for state ["+item.state+"] has a jump table 'newState' reference ["+jump.newState+"] to non-existent state in the FSM table");
							}
						}
					}
				}
				if (!states.contains(actualState)) {
					throw new IllegalArgumentException("FSM table has no one record appropriates to the initial state ["+actualState+"]. Check FSM table or 'initialState' parameter");
				}
				else if (actualItem().terminal){
					throw new IllegalArgumentException("Initial state record in the FSM table is marked as terminal ('terminal':true). Use at least two records in the FSM table");
				}
			}
		}
	}

	private FSMItem actualItem() {
		for (FSMItem item : table) {
			if (item.state == actualState) {
				return item;
			}
		}
		throw new RuntimeException("FSM table problem..."); 
	}

	private void prepareBackgroundControls() {
		final JPanel	buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
		final JPanel	iconPanel = new JPanel();
		final JPanel	totalPanel = new JPanel();

		totalPanel.setLayout(new BorderLayout());
		
		buttonPanel.add(backward);	backward.setActionCommand(BACKWARD_ACTION);		backward.addActionListener(buttonListener);		backward.addKeyListener(totalKeyListener);
		buttonPanel.add(forward);	forward.setActionCommand(FORWARD_ACTION);		forward.addActionListener(buttonListener);		forward.addKeyListener(totalKeyListener);
		buttonPanel.add(cancel);	cancel.setActionCommand(CANCEL_ACTION);			cancel.addActionListener(buttonListener);		cancel.addKeyListener(totalKeyListener);
		
		totalPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		icon.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		iconPanel.add(icon);
		totalPanel.add(iconPanel,BorderLayout.WEST);
		
		content.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		text.setEditable(false);
		text.setFocusable(false);
		contentAndText.setLayout(new BoxLayout(contentAndText, BoxLayout.PAGE_AXIS));		
		contentAndText.add(text);
		contentAndText.add(content);
		
		totalPanel.add(contentAndText,BorderLayout.CENTER);

		getContentPane().add(totalPanel,BorderLayout.CENTER);
		getContentPane().add(message,BorderLayout.SOUTH);
		
		history.add(actualState);
		addKeyListener(totalKeyListener);
	}
	
	private void fillScreen() {
		final FSMItem			item = actualItem();
		final FieldEnumerator[]	fields = collectItems4Frame(item.items);
		boolean					focused = false;
		JComponent				forFocus = null, actualControl;
		JLabel					actualLabel;
				
		setTitle(title+": "+item.caption);
		icon.setIcon(new ImageIcon(getImage(item.image)));
		icon.setToolTipText(item.tooltip);
		text.setText(item.text);
		backward.setEnabled(actualState != initialState);
		forward.setText(item.terminal ? FINISH_CAPTION : FORWARD_CAPTION);
		content.setVisible(false);
		content.removeAll();
		
		for (FieldEnumerator field : fields) {
            actualLabel = new JLabel(field.desc.caption(),JLabel.LEADING);
			switch (field.preferred) {
				case checkbox		:
		            final JCheckBox 	checkBox = new JCheckBox();
		            
		            actualControl = checkBox;
		            checkBox.setSelected((Boolean)getValue(persistent,field));
		            content.add(checkBox);
		            content.add(actualLabel);
		            break;
				case combobox		:
		            final JComboBox<?> 	comboBox = new JComboBox<>(field.field.getType().getEnumConstants());
		            
		            actualControl = comboBox;
		            content.add(actualLabel);
		            comboBox.setSelectedItem(getValue(persistent,field));
		            content.add(comboBox);
		            break;
				case listbox		:
				case table			:
				case fileChooser	:
				case colorChooser	:
				case password		:
		            final JPasswordField passwdField = new JPasswordField(10);
		            
		            actualControl = passwdField;
		            content.add(actualLabel);
		            passwdField.setText(getValue(persistent,field).toString());
		            content.add(passwdField);
		            break;
				case text			:
		            final JTextField 	textField = new JTextField(10);
		            
		            actualControl = textField;
		            content.add(actualLabel);
		            textField.setText(getValue(persistent,field).toString());
		            content.add(textField);
		            break;
				case templatetext	:
		            final JFormattedTextField 	tempateTextField = new JFormattedTextField();
		            
		            actualControl = tempateTextField;
		            content.add(actualLabel);
		            tempateTextField.setInputVerifier(verifier);
		            tempateTextField.setValue(getValue(persistent,field));
		            content.add(tempateTextField);
		            break;
				case textarea  		:
		            final JTextArea		textAreaField = new JTextArea(100,10);
		            final JScrollPane	scroll = new JScrollPane(textAreaField);
		            
		            actualControl = textAreaField;
		            content.add(actualLabel);
		            textAreaField.setText(getValue(persistent,field).toString());
		            content.add(scroll);
		            break;
				default :
					throw new UnsupportedOperationException("Preferred type ["+field.preferred+"] is not supported yet");
			}
			
            if (!focused) {
            	focused = true;
            	forFocus = actualControl;
            }
            actualLabel.setLabelFor(actualControl);
            actualControl.setName(field.field.getName());
            actualControl.setToolTipText(field.desc.tooltip());
            actualControl.addKeyListener(actualControl instanceof JTextArea ? reducedKeyListener : totalKeyListener);
            actualControl.addFocusListener(helper);
		}
        SpringUtilities.makeCompactGrid(content, fields.length, 2, 6, 6, 6, 6);
		content.setVisible(true);
		if (focused) {
	    	forFocus.requestFocus();
		}
	}

	private void dump() {
		for (FieldEnumerator field : collectItems4Frame(actualItem().items)) {
			final JComponent		item = byName(content,field.field.getName());
			
			if (item != null) {
				switch (field.preferred) {
					case checkbox		:
						setValue(persistent,field,((JCheckBox)item).isSelected());
			            break;
					case combobox		:
						setValue(persistent,field,((JComboBox<?>)item).getSelectedItem());
			            break;
					case listbox		:
					case table			:
					case fileChooser	:
					case colorChooser	:
					case password		:
						setValue(persistent,field,((JPasswordField)item).getPassword());
			            break;
					case text			:
						setValue(persistent,field,((JTextField)item).getText());
			            break;
					case templatetext	:
						setValue(persistent,field,((JFormattedTextField)item).getValue());
			            break;
					case textarea  		:
						setValue(persistent,field,((JTextArea)item).getText());
			            break;
					default :
						throw new UnsupportedOperationException("Preferred type ["+field.preferred+"] is not supported yet");
				}
			}
		}
	}
	
	private JComponent byName(final JComponent root,final String name) {
		if (name.equals(root.getName())) {
			return root;
		}
		else {
			for (Component item : root.getComponents()) {
				final JComponent	found = byName((JComponent) item,name);
				
				if (found != null) {
					return found;
				}
			}
			return null;
		}
	}
	
	private Image getImage(final String imageName) {
		if (images.get(imageName) == null) {
			try{images.put(imageName.toString(),ImageIO.read(this.getClass().getResource(imageName)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return images.get(imageName);
	}

	private FieldEnumerator[] collectItems4Frame(final String[] items) {
		final List<FieldEnumerator> result = new ArrayList<>();
		final Class<?>				cl = persistent.getClass();
		
		for (Field f : cl.getFields()) {
			if (f.isAnnotationPresent(WField.class)) {
				for (String name : items) {
					if (f.getName().equals(name)) {
						final FieldEnumerator	item = new FieldEnumerator(f,f.getAnnotation(WField.class)); 
						
						if (item.preferred == PreferredType.any) {
							if (f.getType().equals(boolean.class) || f.getType().equals(Boolean.class)) {
								item.preferred = PreferredType.checkbox;
							}
							else if (f.getType().isEnum()) {
								item.preferred = PreferredType.combobox;
							}
							else if (TableModel.class.isAssignableFrom(f.getType())) {
								item.preferred = PreferredType.table;
							}
							else if (f.getType().equals(File.class)) {
								item.preferred = PreferredType.fileChooser;
							}
							else if (f.getType().equals(Color.class)) {
								item.preferred = PreferredType.colorChooser;
							}
							else if (f.getType().equals(char[].class)) {
								item.preferred = PreferredType.password;
							}
							else if (FORMATTED_ITEMS.contains(f.getType())) {
								item.preferred = PreferredType.templatetext;
							}
							else {
								item.preferred = PreferredType.text;
							}
						}
						result.add(item);
						break;
					}
				}
			}
		}
		return result.toArray(new FieldEnumerator[result.size()]);
	}
	
	public static void main(String[] args) throws InterruptedException {
		final Object		container = new PC();
		final FSMManager	mgr = new FSMManager((Frame)null,"title",true,container,TEXT,1); 
		
		mgr.setMinimumSize(new Dimension(400,200));
		mgr.setPreferredSize(new Dimension(400,200));
		mgr.setVisible(true);
		Thread.sleep(1000);
		mgr.setVisible(false);
	}

	protected static class FieldEnumerator {
		public final 			Field	field;
		public final 			WField	desc;
		public PreferredType	preferred;
		
		public FieldEnumerator(final Field field, final WField desc) {
			this.field = field;
			this.desc = desc;
			this.preferred = desc.preferredType();
		}
	}
}
