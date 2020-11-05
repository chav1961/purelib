package chav1961.purelib.ui.swing.useful;


import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.FlowException;

public class JTextTooltipWindow extends JList<String> {
	private static final long serialVersionUID = 7405405940483457223L;

	@FunctionalInterface
	public interface ContentLoader {
		String[] loadContent(String key, int maxSize) throws FlowException;
	}
	
	private final JTextComponent	buddy;
	private final int				maxSize;
	private final ContentLoader		loader;
	private final JScrollPane		scroll;
	private Popup					popup = null; 
	
	public JTextTooltipWindow(final JTextComponent buddy, final int maxSize, final ContentLoader loader) {
		super(new DefaultListModel<String>());
		
		if (buddy == null) {
			throw new NullPointerException("Buddy can't be null"); 
		}
		else if (maxSize < 0) {
			throw new IllegalArgumentException("Max size ["+maxSize+"] must be greater or equals 0"); 
		}
		else if (loader == null) {
			throw new NullPointerException("Content loader can't be null"); 
		}
		else {
			this.buddy = buddy;
			this.maxSize = maxSize;
			this.loader = loader;
			this.scroll = maxSize == 0 ? new JScrollPane(this) : null;
			
			buddy.addFocusListener(new FocusListener() {
				@Override public void focusGained(final FocusEvent e) {}
				
				@Override
				public void focusLost(final FocusEvent e) {
					if (isShown()) {
						hideWindow();
					}
				}
			});
			buddy.addKeyListener(new KeyListener() {
				@Override public void keyReleased(final KeyEvent e) {}
				
				@Override
				public void keyTyped(final KeyEvent e) {
					if (buddy.getText().length() >= 2 && !isShown()) {
						showWindow();
					}
				}
				
				@Override
				public void keyPressed(final KeyEvent e) {
					switch (e.getKeyCode()) {
						case KeyEvent.VK_SPACE :
							if (e.isControlDown()) {
								showWindow();
							}
							fillWindow(buddy.getText());
							break;
						case KeyEvent.VK_ESCAPE	:
							hideWindow();
							e.consume();
							break;
						case KeyEvent.VK_ENTER	:
							buddy.setText(getSelectedValue());
							hideWindow();
							e.consume();
							break;
						case KeyEvent.VK_DOWN	:
							changeSelection(1);
							break;
						case KeyEvent.VK_UP		:
							changeSelection(-1);
							break;
					}
				}
			});
			buddy.getDocument().addDocumentListener(new DocumentListener() {
				private boolean	recursiveProtector = false;
				private String  prevValue = "\uFFFF";
				
				@Override public void removeUpdate(DocumentEvent e) {process();}
				@Override public void insertUpdate(DocumentEvent e) {process();}
				@Override public void changedUpdate(DocumentEvent e) {process();}
				
				private void process() {
					if (isShown()) {
						try{recursiveProtector = true;
							final String	current = buddy.getText();
							
							if (!current.isEmpty() && !current.equals(prevValue)) {
								fillWindow(prevValue = current);
							}
						} finally {
							recursiveProtector = false;
						}
					}
				}
			});
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
	}
	
	private void showWindow() {
		final Dimension		size = buddy.getSize();
		final Point			p = new Point(0,0);

		SwingUtilities.convertPointToScreen(p,buddy);
		
		if (popup != null) {
			popup.hide();
		}
		
		if (maxSize == 0) {
			scroll.setPreferredSize(new Dimension(size.width,10*size.height));
			popup = PopupFactory.getSharedInstance().getPopup(buddy,scroll,p.x,p.y+size.height-1);
		}
		else {
			setPreferredSize(new Dimension(size.width,maxSize*size.height));
			popup = PopupFactory.getSharedInstance().getPopup(buddy,this,p.x,p.y+size.height-1);
		}
		popup.show();
	}

	private boolean isShown() {
		return popup != null;
	}
	
	private void hideWindow() {
		if (popup != null) {
			popup.hide();
			popup = null;
		}
	}
	
	private void fillWindow(final String key) {
		try{final char[]	keyContent = key.toUpperCase().toCharArray();
			final String[]	content = loader.loadContent(key, maxSize);
			
			if (content != null && content.length > 0) {
				final int[]	nearest = new int[content.length];
				
				for (int index = 0; index < nearest.length; index++) {
					final char[]	array = content[index].toUpperCase().toCharArray();
						
					if (array.length >= keyContent.length) {
						nearest[index] = CharUtils.calcLevenstain(array,keyContent).distance - (array.length - keyContent.length);
					}
					else {
						nearest[index] = Integer.MAX_VALUE;
					}
				}
				int	selIndex = 0, selValue = nearest[0];
				
				for (int index = 1; index < nearest.length; index++) {
					if (nearest[index] < selValue) {
						selIndex = index;
						selValue = nearest[index]; 
					}
				}
				
				((DefaultListModel<String>)getModel()).removeAllElements();
				for (String item : content) {
					((DefaultListModel<String>)getModel()).addElement(item);
				}
				setSelectedIndex(selIndex);
			}
		} catch (FlowException exc) {
		}
	}
	
	private void changeSelection(final int delta) {
		if (isShown()) {
			int		index = getSelectionModel().getMinSelectionIndex();
			
			if (index < 0) {
				index = 0;
			}
			if (index+delta >= 0 && index+delta < getModel().getSize()) {
				index += delta;
			}
			getSelectionModel().setSelectionInterval(index, index);
			
			final Rectangle	bounds = getCellBounds(index, index); 
			
			if (bounds != null) {
				scrollRectToVisible(bounds);
			}
		}
	}
}
