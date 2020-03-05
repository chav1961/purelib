package chav1961.purelib.testing;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.ui.swing.SwingUtils;

public class SwingUnitTest {
	private final Component	root;
	private final Robot		robo;
	private JComponent		lastFound = null;
	
	public SwingUnitTest(final Component root) throws NullPointerException, EnvironmentException {
		if (root == null) {
			throw new NullPointerException("Root component can't be null");
		}
		else {
			this.root = root;
			
			try{final GraphicsConfiguration	gc = root.getGraphicsConfiguration();
				final GraphicsEnvironment 	ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				 
				for (GraphicsDevice dev : ge.getScreenDevices()) {
					for (GraphicsConfiguration conf : dev.getConfigurations()) {
						if (conf.equals(gc)) {
							this.robo = new Robot(dev);
							this.robo.setAutoWaitForIdle(true);
							return;
						}
					}
				}
				this.robo = new Robot();
				this.robo.setAutoWaitForIdle(true);
			} catch (AWTException e) {
				throw new EnvironmentException("Can't create robot for the root component: "+e.getLocalizedMessage(),e);
			}
		}
	}

	public JComponent getLastFound() throws DebuggingException {
		if (lastFound == null) {
			throw new DebuggingException("Last component found is null. Check previous operation");
		}
		else {
			return lastFound;
		}
	}

	public SwingUnitTest use(final JComponent component) throws DebuggingException {
		if (component == null) {
			throw new NullPointerException("Component to use can't be null"); 
		}
		else {
			lastFound = component;
			return this;
		}
	}
	
	public SwingUnitTest seek(final String name) throws DebuggingException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name to seek can't be null or empty"); 
		}
		else {
			lastFound = (JComponent) SwingUtils.findComponentByName(root,name);
			getLastFound();	// check and throw exception
			return this;
		}
	}
	
	public SwingUnitTest move(final int x, final int y) throws DebuggingException {
		final Point	p = new Point(x,y);
		
		SwingUtilities.convertPointToScreen(p,getLastFound());
		robo.mouseMove(p.x,p.y);
		return this;
	}
	
	public SwingUnitTest keys(final String keys) throws DebuggingException {
		if (keys == null || keys.isEmpty()) {
			throw new IllegalArgumentException("Keys to print can't be null or empty"); 
		}
		else {
			final char[]		content = keys.toCharArray();
			final KeyStroke[]	tmp = new KeyStroke[content.length];
			
			for (int index = 0; index < content.length; index++) {
				tmp[index] = KeyStroke.getKeyStroke(Character.toUpperCase(content[index]),Character.isUpperCase(content[index]) ? InputEvent.SHIFT_DOWN_MASK : 0);
			}
			return keys(tmp);
		}
	}
	
	public SwingUnitTest keys(final KeyStroke... keys) throws DebuggingException {
		final int	nullItem;
		
		if (keys == null || keys.length == 0) {
			throw new IllegalArgumentException("Keys to print can't be null or empty"); 
		}
		else if ((nullItem = Utils.checkArrayContent4Nulls(keys)) >= 0) {
			throw new IllegalArgumentException("Key's list contains null at index ["+nullItem+"]"); 
		}
		else {
			int	modifiers = 0;			
			
			for (KeyStroke item : keys) {
				if (item.getModifiers() != modifiers) {
					final int	newModifiers = item.getModifiers(); 
					
					processModifiers(modifiers,newModifiers);
					modifiers = newModifiers;
				}
				robo.keyPress(item.getKeyCode());
				robo.keyRelease(item.getKeyCode());
			}
			processModifiers(modifiers,0);
			return this;
		}
	} 

	public SwingUnitTest click(final int buttonId, final int clickCount) throws DebuggingException {
		move(1,1);
		switch (buttonId) {
			case MouseEvent.BUTTON1		: clickInternal(InputEvent.BUTTON1_DOWN_MASK,clickCount); break;
			case MouseEvent.BUTTON2		: clickInternal(InputEvent.BUTTON1_DOWN_MASK,clickCount); break;
			case MouseEvent.BUTTON3		: clickInternal(InputEvent.BUTTON1_DOWN_MASK,clickCount); break;
			case MouseEvent.MOUSE_WHEEL	: robo.mouseWheel(clickCount); break;
			default : throw new IllegalArgumentException("Illegal button id ["+buttonId+"]. Use MouseEvent.BUTTON1, MouseEvent.BUTTON2, MouseEvent.BUTTON3 or MouseEvent.MOUSE_WHEEL only!"); 
		}
		return this;
	}

	public SwingUnitTest drag(final int buttonId, final int xFrom, final int yFrom, final int xTo, final int yTo) throws DebuggingException {
		move(xFrom,yFrom);
		switch (buttonId) {
			case MouseEvent.BUTTON1		: robo.mousePress(InputEvent.BUTTON1_DOWN_MASK); break;
			case MouseEvent.BUTTON2		: robo.mousePress(InputEvent.BUTTON2_DOWN_MASK); break;
			case MouseEvent.BUTTON3		: robo.mousePress(InputEvent.BUTTON3_DOWN_MASK); break;
			default : throw new IllegalArgumentException("Illegal button id ["+buttonId+"]. Use MouseEvent.BUTTON1, MouseEvent.BUTTON2, MouseEvent.BUTTON3 or MouseEvent.MOUSE_WHEEL only!"); 
		}
		move(xTo,yTo);
		switch (buttonId) {
			case MouseEvent.BUTTON1		: robo.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); break;
			case MouseEvent.BUTTON2		: robo.mouseRelease(InputEvent.BUTTON2_DOWN_MASK); break;
			case MouseEvent.BUTTON3		: robo.mouseRelease(InputEvent.BUTTON3_DOWN_MASK); break;
			default : throw new IllegalArgumentException("Illegal button id ["+buttonId+"]. Use MouseEvent.BUTTON1, MouseEvent.BUTTON2, MouseEvent.BUTTON3 or MouseEvent.MOUSE_WHEEL only!"); 
		}
		return this;
	}

	public SwingUnitTest drag(final int buttonId, final int xFrom, final int yFrom, final String name, final int xTo, final int yTo) throws DebuggingException {
		final JComponent	tmp = lastFound;
		
		move(xFrom,yFrom);
		switch (buttonId) {
			case MouseEvent.BUTTON1		: robo.mousePress(InputEvent.BUTTON1_DOWN_MASK); break;
			case MouseEvent.BUTTON2		: robo.mousePress(InputEvent.BUTTON2_DOWN_MASK); break;
			case MouseEvent.BUTTON3		: robo.mousePress(InputEvent.BUTTON3_DOWN_MASK); break;
			default : throw new IllegalArgumentException("Illegal button id ["+buttonId+"]. Use MouseEvent.BUTTON1, MouseEvent.BUTTON2, MouseEvent.BUTTON3 or MouseEvent.MOUSE_WHEEL only!"); 
		}
		seek(name);
		move(xTo,yTo);
		switch (buttonId) {
			case MouseEvent.BUTTON1		: robo.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); break;
			case MouseEvent.BUTTON2		: robo.mouseRelease(InputEvent.BUTTON2_DOWN_MASK); break;
			case MouseEvent.BUTTON3		: robo.mouseRelease(InputEvent.BUTTON3_DOWN_MASK); break;
			default : throw new IllegalArgumentException("Illegal button id ["+buttonId+"]. Use MouseEvent.BUTTON1, MouseEvent.BUTTON2, MouseEvent.BUTTON3 or MouseEvent.MOUSE_WHEEL only!"); 
		}
		lastFound = tmp;
		return this;
	} 
	
	public SwingUnitTest select(final String name) throws DebuggingException {
		seek(name);
		final JComponent	found = getLastFound(); 
		
		if (found.isFocusable()) {
			try{SwingTestingUtils.syncRequestFocus(found);
			} catch (InterruptedException exc) {
				throw new DebuggingException("Timeout awaiting focus!");
			}
		}
		else {
			throw new DebuggingException("Last component found is not focusable, and can't be selected. Check previous operation");
		}
		return this;
	}
	
	private void processModifiers(final int oldModifiers, final int newModifiers) {
		for (int mask = 0x01, changes = newModifiers ^ oldModifiers; changes != 0; changes >>= 1, mask <<= 1) {
			if ((changes & 0x01) != 0) {
				if ((mask & newModifiers) != 0) {
					switch (mask) {
						case InputEvent.SHIFT_DOWN_MASK		: robo.keyPress(KeyEvent.VK_SHIFT); break;
						case InputEvent.CTRL_DOWN_MASK		: robo.keyPress(KeyEvent.VK_CONTROL); break;
						case InputEvent.META_DOWN_MASK		: robo.keyPress(KeyEvent.VK_META); break;
						case InputEvent.ALT_DOWN_MASK		: robo.keyPress(KeyEvent.VK_ALT); break;
						case InputEvent.ALT_GRAPH_DOWN_MASK	: robo.keyPress(KeyEvent.VK_ALT_GRAPH); break;
					}
				}
				else {
					switch (mask) {
						case InputEvent.SHIFT_DOWN_MASK		: robo.keyRelease(KeyEvent.VK_SHIFT); break;
						case InputEvent.CTRL_DOWN_MASK		: robo.keyRelease(KeyEvent.VK_CONTROL); break;
						case InputEvent.META_DOWN_MASK		: robo.keyRelease(KeyEvent.VK_META); break;
						case InputEvent.ALT_DOWN_MASK		: robo.keyRelease(KeyEvent.VK_ALT); break;
						case InputEvent.ALT_GRAPH_DOWN_MASK	: robo.keyRelease(KeyEvent.VK_ALT_GRAPH); break;
					}
				}
			}
		}
	}

	private void clickInternal(final int buttonMask, int clickCount) {
		for (int index = 0; index < clickCount; index++) {
			robo.mousePress(buttonMask);
			robo.mouseRelease(buttonMask);
		}
	}
}
