package chav1961.purelib.ui.swing.useful.svg;

import java.util.Iterator;

import chav1961.purelib.basic.exceptions.SyntaxException;

abstract class AbstractAttribute {
	protected static final AbstractAttribute[]	EMPTY_VALUES = new AbstractAttribute[0];
	
	private final String				attrName;
	private final boolean				fixedValuesOnly;
	private final AbstractAttribute[]	availableValues;
	private final AbstractAttribute		parent;
	
	AbstractAttribute(final AbstractAttribute parent, final String attrName) {
		this.parent = parent;
		this.attrName = attrName;
		this.fixedValuesOnly = false;
		this.availableValues = EMPTY_VALUES;
	}
	
	AbstractAttribute(final AbstractAttribute parent, final String attrName, final boolean fixedValuesOnly, final AbstractAttribute... availableValues) {
		this.parent = parent;
		this.attrName = attrName;
		this.fixedValuesOnly = fixedValuesOnly;
		this.availableValues = availableValues;
	}

	protected abstract Object toValue(final String value) throws SyntaxException;
	
	protected AbstractAttribute getParent() {
		return parent;
	}
	
	protected String getName() {
		return attrName;
	}
	
	protected boolean hasFixedValues() {
		return fixedValuesOnly;
	}
	
	protected Iterable<AbstractAttribute> availableAttributes() {
		return new Iterable<AbstractAttribute>() {
			@Override
			public Iterator<AbstractAttribute> iterator() {
				return new Iterator<AbstractAttribute>() {
					int	index = 0;

					@Override
					public boolean hasNext() {
						return index < availableValues.length;
					}

					@Override
					public AbstractAttribute next() {
						return availableValues[index++];
					}
				};
			}
		};
	}
	
	protected AbstractAttribute byName(final String name) {
		return null;
	}
}

