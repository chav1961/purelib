package chav1961.purelib.ui.swing.useful;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.ui.swing.SwingUtils;

/**
 * <p>This class supports manipulations for swing enabled state components by long masks. Every bit in the long mask associates with
 * some swing component (also inside the swing containers). Thos class allows to manipulate component's states with simple bit operations
 * instead of calling appropriative methods.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public class JEnableMaskManipulator {
	private final String[]			names;
	private final List<JComponent>	entities = new ArrayList<>();
	private final List<Long>		stack = new ArrayList<>();
	private long					currentMask = 0;
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param itemNames swing item names to manipulate. the same first name in the array will be associated with 0-th bit in the long mask,
	 * the next will be associated with the 2-th bit in the long mask and so on. Can't be null, empty and doesn't contain nulls or empties inside.
	 * All the names mentioned must exists in the second parameter on the constructor. Number of names must be less than 64 
	 * @param components components to manipulate with the class. Can't be null, empty or contains nulls inside. Can contain duplicate names anywhere.
	 * These enable state of duplicate names will be changed together
	 * @throws IllegalArgumentException any list is null, empty, contains nulls/empties inside or item name in not exists in the component's list  
	 */
	public JEnableMaskManipulator(final String[] itemNames, final JComponent... components) throws IllegalArgumentException {
		if (itemNames == null || itemNames.length == 0 || Utils.checkArrayContent4Nulls(itemNames, true) >= 0) {
			throw new IllegalArgumentException("Item names list is null, empty or contains nulls/empties inside");
		}
		else if (components == null || components.length == 0 || Utils.checkArrayContent4Nulls(components) >= 0) {
			throw new IllegalArgumentException("Components list is null, empty or contains nulls inside");
		}
		else {
loop:		for (String itemName : itemNames) {
				for (JComponent component : components) {
					if (SwingUtils.findComponentByName(component, itemName) != null) {
						continue loop;
					}
				}
				throw new IllegalArgumentException("Item name ["+itemName+"] not found anywhere in the component list");
			}
			this.names = itemNames;
			this.entities.addAll(Arrays.asList(components));
			refreshState();
		}
	}

	/**
	 * <p>Add component to control enable state</p>
	 * @param component component to add. Can't be null
	 * @throws NullPointerException component to add is null
	 */
	public void addComponent(final JComponent component) throws NullPointerException {
		if (component == null) {
			throw new NullPointerException("Component to add can't eb null"); 
		}
		else {
			entities.add(component);
		}
	}
	
	/**
	 * <p>Remove component from control enable state</p>
	 * @param component component to remove. Can't be null
	 * @throws NullPointerException component to remove is null
	 */
	public void removeComponent(final JComponent component) throws NullPointerException {
		if (component == null) {
			throw new NullPointerException("Component to remove can't eb null"); 
		}
		else {
			entities.remove(component);
		}
	}
	
	/**
	 * <p>Get current enable mask</p>
	 * @return current enable mask
	 */
	public long getEnableMask() {
		return currentMask;
	}

	/**
	 * <p>Set current enable mask.</p>
	 * @param enableMask enable mask to set
	 */
	public void setEnableMask(final long enableMask) {
		currentMask = enableMask;
		refreshState();
	}
	
	/**
	 * <p>Set enabled state for the given bits to 'enable'</p>
	 * @param enableMask bits to set enable state to 'enabled'
	 */
	public void setEnableMaskOn(final long enableMask) {
		setEnableMask(getEnableMask() | enableMask);
	}

	/**
	 * <p>Set enabled state for the given bits to 'disable'</p>
	 * @param enableMask bits to set enable state to 'disabled'
	 */
	public void setEnableMaskOff(final long enableMask) {
		setEnableMask(getEnableMask() & ~enableMask);
	}

	/**
	 * <p>Set enabled state for the given bits to value typed by second parameter</p>
	 * @param enableMask bits to change enable state
	 * @param state new state for enable mask
	 */
	public void setEnableMaskTo(final long enableMask, final boolean state) {
		if (state) {
			setEnableMaskOn(enableMask);
		}
		else {
			setEnableMaskOff(enableMask);
		}
	}
	
	/**
	 * <p>Push current enable mask state and set the new enable mask state</p>
	 * @param enableMask enable mask to set
	 * @return previous enable mask
	 */
	public long pushEnableMask(final long enableMask) {
		final long	result = getEnableMask();
		
		stack.add(0,result);
		setEnableMask(enableMask);
		return result;
	}

	/**
	 * <p>Pop enable mask and set it</p>
	 * @return mask popped.
	 * @throws IllegalStateException enable mask stack exhausted
	 */
	public long popEnableMask() throws IllegalStateException {
		if (stack.isEmpty()) {
			throw new IllegalStateException("Enable mask stack exhausted");
		}
		else {
			final long result = stack.remove(0);
			
			setEnableMask(result);
			return result;
		}
	}

	private void refreshState() {
		for(int index = 0; index < names.length; index++) {
			final boolean	state = (getEnableMask() & (1L << index)) != 0;
			
			for (JComponent component : entities) {
				final Container	c = SwingUtils.findComponentByName(component, names[index]);
		
				if (c instanceof JComponent) {
					((JComponent)c).setEnabled(state);
				}
			}
		}
	}

}
