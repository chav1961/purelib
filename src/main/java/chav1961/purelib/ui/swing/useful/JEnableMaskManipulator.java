package chav1961.purelib.ui.swing.useful;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.AbstractButton;
import javax.swing.JComponent;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.ui.swing.SwingUtils;

/**
 * <p>This class supports manipulations for swing enabled state components by long masks. Every bit in the long mask associates with
 * some swing component (also inside the swing containers). Thos class allows to manipulate component's states with simple bit operations
 * instead of calling appropriative methods.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @last.update 0.0.8
 */
public class JEnableMaskManipulator {
	private final JEnableMaskManipulator	parent;
	private final ItemDescriptor[]			names;
	private final List<JComponent>			entities = new ArrayList<>();
	private final List<Long>				enableStack = new ArrayList<>();
	private final List<Long>				checkStack = new ArrayList<>();
	private final long						localMask;
	private final boolean					autoDisabled;
	private long							currentEnableMask = 0L;
	private long							currentCheckMask = 0L;
	
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
		this(itemNames, false, components);
	}	

	/**
	 * <p>Constructor of the class.</p>
	 * @param itemNames swing item names to manipulate. the same first name in the array will be associated with 0-th bit in the long mask,
	 * the next will be associated with the 2-th bit in the long mask and so on. Can't be null, empty and doesn't contain nulls or empties inside.
	 * All the names mentioned must exists in the second parameter on the constructor. Number of names must be less than 64 
	 * @param components components to manipulate with the class. Can't be null, empty or contains nulls inside. Can contain duplicate names anywhere.
	 * These enable state of duplicate names will be changed together
	 * @throws IllegalArgumentException any list is null, empty, contains nulls/empties inside or item name in not exists in the component's list
	 * @since 0.0.8  
	 */
	public JEnableMaskManipulator(final ItemDescriptor[] itemNames, final JComponent... components) throws IllegalArgumentException {
		this(itemNames, false, components);
	}	
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param itemNames swing item names to manipulate. the same first name in the array will be associated with 0-th bit in the long mask,
	 * the next will be associated with the 2-th bit in the long mask and so on. Can't be null, empty and doesn't contain nulls or empties inside.
	 * All the names mentioned must exists in the second parameter on the constructor. Number of names must be less than 64 
	 * @param ignoreMissing ignore missing items 
	 * @param components components to manipulate with the class. Can't be null, empty or contains nulls inside. Can contain duplicate names anywhere.
	 * These enable state of duplicate names will be changed together
	 * @throws IllegalArgumentException any list is null, empty, contains nulls/empties inside or item name in not exists in the component's list  
	 */
	public JEnableMaskManipulator(final String[] itemNames, final boolean ignoreMissing, final JComponent... components) throws IllegalArgumentException {
		this(itemNames, ignoreMissing, false, components);
	}	

	/**
	 * <p>Constructor of the class.</p>
	 * @param itemNames swing item names to manipulate. the same first name in the array will be associated with 0-th bit in the long mask,
	 * the next will be associated with the 2-th bit in the long mask and so on. Can't be null, empty and doesn't contain nulls or empties inside.
	 * All the names mentioned must exists in the second parameter on the constructor. Number of names must be less than 64 
	 * @param ignoreMissing ignore missing items 
	 * @param components components to manipulate with the class. Can't be null, empty or contains nulls inside. Can contain duplicate names anywhere.
	 * These enable state of duplicate names will be changed together
	 * @throws IllegalArgumentException any list is null, empty, contains nulls/empties inside or item name in not exists in the component's list  
	 * @since 0.0.8  
	 */
	public JEnableMaskManipulator(final ItemDescriptor[] itemNames, final boolean ignoreMissing, final JComponent... components) throws IllegalArgumentException {
		this(itemNames, ignoreMissing, false, components);
	}	
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param itemNames swing item names to manipulate. the same first name in the array will be associated with 0-th bit in the long mask,
	 * the next will be associated with the 2-th bit in the long mask and so on. Can't be null, empty and doesn't contain nulls or empties inside.
	 * All the names mentioned must exists in the second parameter on the constructor. Number of names must be less than 64 
	 * @param ignoreMissing ignore missing items 
	 * @param autoDisabled disable automatically
	 * @param components components to manipulate with the class. Can't be null, empty or contains nulls inside. Can contain duplicate names anywhere.
	 * These enable state of duplicate names will be changed together
	 * @throws IllegalArgumentException any list is null, empty, contains nulls/empties inside or item name in not exists in the component's list  
	 */
	public JEnableMaskManipulator(final String[] itemNames, final boolean ignoreMissing, final boolean autoDisabled, final JComponent... components) throws IllegalArgumentException {
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
				if (!ignoreMissing) {
					throw new IllegalArgumentException("Item name ["+itemName+"] not found anywhere in the component list");
				}
			}
			this.parent = null;
			this.names = toItems(itemNames);
			this.localMask = 0;
			this.autoDisabled = autoDisabled;
			this.entities.addAll(Arrays.asList(components));
			refreshState(~localMask);
		}
	}

	/**
	 * <p>Constructor of the class.</p>
	 * @param itemNames swing item names to manipulate. the same first name in the array will be associated with 0-th bit in the long mask,
	 * the next will be associated with the 2-th bit in the long mask and so on. Can't be null, empty and doesn't contain nulls or empties inside.
	 * All the names mentioned must exists in the second parameter on the constructor. Number of names must be less than 64 
	 * @param ignoreMissing ignore missing items 
	 * @param autoDisabled disable automatically
	 * @param components components to manipulate with the class. Can't be null, empty or contains nulls inside. Can contain duplicate names anywhere.
	 * These enable state of duplicate names will be changed together
	 * @throws IllegalArgumentException any list is null, empty, contains nulls/empties inside or item name in not exists in the component's list  
	 * @since 0.0.8  
	 */
	public JEnableMaskManipulator(final ItemDescriptor[] itemNames, final boolean ignoreMissing, final boolean autoDisabled, final JComponent... components) throws IllegalArgumentException {
		if (itemNames == null || itemNames.length == 0 || Utils.checkArrayContent4Nulls(itemNames, false) >= 0) {
			throw new IllegalArgumentException("Item names list is null, empty or contains nulls/empties inside");
		}
		else if (components == null || components.length == 0 || Utils.checkArrayContent4Nulls(components) >= 0) {
			throw new IllegalArgumentException("Components list is null, empty or contains nulls inside");
		}
		else {
loop:		for (ItemDescriptor item : itemNames) {
				for (JComponent component : components) {
					if (SwingUtils.findComponentByName(component, item.name) != null) {
						continue loop;
					}
				}
				if (!ignoreMissing) {
					throw new IllegalArgumentException("Item name ["+item.name+"] not found anywhere in the component list");
				}
			}
			this.parent = null;
			this.names = itemNames;
			this.localMask = 0;
			this.autoDisabled = autoDisabled;
			this.entities.addAll(Arrays.asList(components));
			refreshState(~localMask);
		}
	}
	
	public JEnableMaskManipulator(final JEnableMaskManipulator parent, final JComponent... components) throws IllegalArgumentException {
		this(parent, false, 0, components);
	}	

	public JEnableMaskManipulator(final JEnableMaskManipulator parent, final long localMask, final JComponent... components) throws IllegalArgumentException {
		this(parent, false, localMask, components);
	}	

	public JEnableMaskManipulator(final JEnableMaskManipulator parent, final boolean ignoreMissing, final JComponent... components) throws IllegalArgumentException {
		this(parent, ignoreMissing, 0, components);
	}

	public JEnableMaskManipulator(final JEnableMaskManipulator parent, final boolean ignoreMissing, final long localMask, final JComponent... components) throws IllegalArgumentException {
		this(parent, ignoreMissing, parent.autoDisabled, localMask, components);
	}	
	
	public JEnableMaskManipulator(final JEnableMaskManipulator parent, final boolean ignoreMissing, final boolean autoDisables, final long localMask, final JComponent... components) throws IllegalArgumentException {
		if (parent == null) {
			throw new NullPointerException("Parent manipulator can't be null");
		}
		else if (components == null || components.length == 0 || Utils.checkArrayContent4Nulls(components) >= 0) {
			throw new IllegalArgumentException("Components list is null, empty or contains nulls inside");
		}
		else {
			this.parent = parent;
			this.entities.addAll(Arrays.asList(components));
			this.names = null;
			this.localMask = localMask;
			this.autoDisabled = autoDisables;
			refreshState(~0L);
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
		if (parent != null) {
			if (localMask != 0) {
				return parent.getEnableMask() & ~localMask | currentEnableMask & localMask;  
			}
			else {
				return parent.getEnableMask();
			}
		}
		else {
			return currentEnableMask;
		}
	}

	/**
	 * <p>Get current check mask</p>
	 * @return current check mask
	 */
	public long getCheckMask() {
		if (parent != null) {
			if (localMask != 0) {
				return parent.getCheckMask() & ~localMask | currentCheckMask & localMask;  
			}
			else {
				return parent.getCheckMask();
			}
		}
		else {
			return currentCheckMask;
		}
	}
	
	/**
	 * <p>Set current enable mask.</p>
	 * @param enableMask enable mask to set
	 */
	public void setEnableMask(final long enableMask) {
		if (parent != null) {
			if (localMask != 0) {
				parent.setEnableMask(enableMask & ~localMask);
				currentEnableMask = enableMask & localMask;
			}
			else {
				parent.setEnableMask(enableMask);
			}
		}
		else {
			currentEnableMask = enableMask;
		}
		refreshState(~0L);
	}

	/**
	 * <p>Set current check mask.</p>
	 * @param checkMask check mask to set
	 */
	public void setCheckMask(final long checkMask) {
		if (parent != null) {
			if (localMask != 0) {
				parent.setCheckMask(checkMask & ~localMask);
				currentCheckMask = checkMask & localMask;
			}
			else {
				parent.setCheckMask(checkMask);
			}
		}
		else {
			currentCheckMask = checkMask;
		}
		refreshState(~0L);
	}
	
	/**
	 * <p>Set enabled state for the given bits to 'enable'</p>
	 * @param enableMask bits to set enable state to 'enabled'
	 */
	public void setEnableMaskOn(final long enableMask) {
		setEnableMask(getEnableMask() | enableMask);
	}

	/**
	 * <p>Set checked state for the given bits to 'check'</p>
	 * @param checkMask bits to set check state to 'checked'
	 */
	public void setCheckMaskOn(final long checkMask) {
		setCheckMask(getCheckMask() | checkMask);
	}
	
	/**
	 * <p>Set enabled state for the given bits to 'disable'</p>
	 * @param enableMask bits to set enable state to 'disabled'
	 */
	public void setEnableMaskOff(final long enableMask) {
		setEnableMask(getEnableMask() & ~enableMask);
	}

	/**
	 * <p>Set check state for the given bits to 'unchecked'</p>
	 * @param checkMask bits to set check state to 'unchecked'
	 */
	public void setCheckMaskOff(final long checkMask) {
		setCheckMask(getCheckMask() & ~checkMask);
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
	 * <p>Set enabled state for the given bits to value typed by second parameter</p>
	 * @param checkMask bits to change check state
	 * @param state new state for check mask
	 */
	public void setCheckMaskTo(final long checkMask, final boolean state) {
		if (state) {
			setCheckMaskOn(checkMask);
		}
		else {
			setCheckMaskOff(checkMask);
		}
	}
	
	/**
	 * <p>Push current enable mask state and set the new enable mask state</p>
	 * @param enableMask enable mask to set
	 * @return previous enable mask
	 */
	public long pushEnableMask(final long enableMask) {
		final long	result = getEnableMask();
		
		enableStack.add(0,result);
		setEnableMask(enableMask);
		return result;
	}

	/**
	 * <p>Push current check mask state and set the new check mask state</p>
	 * @param checkMask check mask to set
	 * @return previous check mask
	 */
	public long pushCheckMask(final long checkMask) {
		final long	result = getCheckMask();
		
		checkStack.add(0,result);
		setCheckMask(checkMask);
		return result;
	}
	
	/**
	 * <p>Pop enable mask and set it</p>
	 * @return mask popped.
	 * @throws IllegalStateException enable mask stack exhausted
	 */
	public long popEnableMask() throws IllegalStateException {
		if (enableStack.isEmpty()) {
			throw new IllegalStateException("Enable mask stack exhausted");
		}
		else {
			final long result = enableStack.remove(0);
			
			setEnableMask(result);
			return result;
		}
	}

	/**
	 * <p>Pop check mask and set it</p>
	 * @return mask popped.
	 * @throws IllegalStateException check mask stack exhausted
	 */
	public long popCheckMask() throws IllegalStateException {
		if (checkStack.isEmpty()) {
			throw new IllegalStateException("Check mask stack exhausted");
		}
		else {
			final long result = checkStack.remove(0);
			
			setCheckMask(result);
			return result;
		}
	}

	/**
	 * <p>Manually refresh state of the items</p>
	 */
	public void refresh() {
		refreshState(~localMask);
	}

	/**
	 * <p>Calculate enabling for all masks and apply result</p>
	 * @since 0.0.8
	 */
	public void applyEnableMasks() {
		long	mask = 0;
		
		for(int index = 0; index < names.length; index++) {
			if (names[index].enableState.get()) {
				mask |= names[index].mask;
			}
		}
		setEnableMask(mask);
	}

	/**
	 * <p>Calculate checking for all masks and apply result</p>
	 * @since 0.0.8
	 */
	public void applyCheckMasks() {
		long	mask = 0;
		
		for(int index = 0; index < names.length; index++) {
			if (names[index].checkState.get()) {
				mask |= names[index].mask;
			}
		}
		setCheckMask(mask);
	}
	
	/**
	 * <p>Calculate all masks state and apply result</p>
	 * @since 0.0.8
	 */
	public void applyMasks() {
		applyEnableMasks();
		applyCheckMasks();
	}
	
	private ItemDescriptor[] toItems(final String[] itemNames) {
		final ItemDescriptor[] result = new ItemDescriptor[itemNames.length];
		
		for(int index = 0; index < result.length; index++) {
			final long	mask = 1L << index;
			
			result[index] = new ItemDescriptor(itemNames[index], mask, ()->(getEnableMask() & mask) != 0, ()->(getCheckMask() & mask) != 0);
		}
		return result;
	}
	
	private ItemDescriptor[] getComponentDescriptors() {
		if (parent != null) {
			return parent.getComponentDescriptors();
		}
		else {
			return names;
		}
	}
	
	private void refreshState(final long mask) {
		for(int index = 0; index < getComponentDescriptors().length; index++) {
			final long	bit = 1L << index;
			
			if ((mask & bit) != 0) {
				final boolean	enableState = (getEnableMask() & bit) != 0;
				final boolean	checkState = (getCheckMask() & bit) != 0;
				
				for (JComponent component : entities) {
					final Container	c = SwingUtils.findComponentByName(component, getComponentDescriptors()[index].name);
			
					if (c instanceof JComponent) {
						((JComponent)c).setEnabled(enableState);
					}
					if (c instanceof AbstractButton) {
						((AbstractButton)c).setSelected(checkState);
					}
				}
			}
		}
		if (parent != null) {
			parent.refreshState(mask & ~localMask);
		}
	}

	@FunctionalInterface
	public static interface ItemState {
		boolean get();
	}
	
	public static class ItemDescriptor {
		public final String	name;
		public final long mask;
		public final ItemState enableState;
		public final ItemState checkState;

		public ItemDescriptor(final String name, final long mask, final ItemState enableState, final ItemState checkState) {
			if (name == null || Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can be neither null nor empty"); 
			}
			else if (mask == 0) {
				throw new IllegalArgumentException("Mask must contain at least one non-zero bit");
			}
			else if (enableState == null) {
				throw new NullPointerException("Enable state callback can't be null");
			}
			else if (checkState == null) {
				throw new NullPointerException("Check state callback can't be null");
			}
			else {
				this.name = name;
				this.mask = mask;
				this.enableState = enableState;
				this.checkState = checkState;
			}
		}
		
		public ItemDescriptor(final String name, final long mask, final ItemState enableState) {
			if (name == null || Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can be neither null nor empty"); 
			}
			else if (mask == 0) {
				throw new IllegalArgumentException("Mask must contain at least one non-zero bit");
			}
			else if (enableState == null) {
				throw new NullPointerException("Enable state callback can't be null");
			}
			else {
				this.name = name;
				this.mask = mask;
				this.enableState = enableState;
				this.checkState = ()->false;
			}
		}
	}
}
