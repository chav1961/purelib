package chav1961.purelib.sql.junit;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.IntGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.LongGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.exceptions.ContentException;

class JUnitSequenceImpl extends JUnitEntityImpl {
	private static final int		SEQ_TYPE_INT = 0;
	private static final int		SEQ_TYPE_LONG = 1;
	private static final int		SEQ_TYPE_ATOMIC_INT = 2;
	private static final int		SEQ_TYPE_ATOMIC_LONG = 3;
	
	private final String			seqName;
	private final int				type;
	private final GetterAndSetter	gas;
	
	JUnitSequenceImpl(final String fieldName, final IntGetterAndSetter gas) {
		if (fieldName == null || fieldName.isEmpty()) {
			throw new IllegalArgumentException("Sequence field name can't be null or empty"); 
		}
		else if (gas == null) {
			throw new NullPointerException("Getter/setter for sequence ["+fieldName+"] can't be null"); 
		}
		else {
			this.type = SEQ_TYPE_INT;
			this.seqName = fieldName;
			this.gas = gas;
		}
	}

	JUnitSequenceImpl(final String fieldName, final LongGetterAndSetter gas) {
		if (fieldName == null || fieldName.isEmpty()) {
			throw new IllegalArgumentException("Sequence field name can't be null or empty"); 
		}
		else if (gas == null) {
			throw new NullPointerException("Getter/setter for sequence ["+fieldName+"] can't be null"); 
		}
		else {
			this.type = SEQ_TYPE_LONG;			
			this.seqName = fieldName;
			this.gas = gas;
		}
	}

	<T> JUnitSequenceImpl(final String fieldName, final ObjectGetterAndSetter<T> gas, final Class<T> cast) {
		if (fieldName == null || fieldName.isEmpty()) {
			throw new IllegalArgumentException("Sequence field name can't be null or empty"); 
		}
		else if (gas == null) {
			throw new NullPointerException("Getter/setter for sequence ["+fieldName+"] can't be null"); 
		}
		else if (cast == null) {
			throw new NullPointerException("Class to cast for sequence ["+fieldName+"] can't be null"); 
		}
		else if (!AtomicInteger.class.isAssignableFrom(cast) && !AtomicLong.class.isAssignableFrom(cast)) {
			throw new NullPointerException("Class to cast for sequence ["+fieldName+"] can be AtomicInteger or AtomicLong only"); 
		}
		else {
			if (cast == AtomicInteger.class) {
				this.type = SEQ_TYPE_ATOMIC_INT;
			}
			else {
				this.type = SEQ_TYPE_ATOMIC_LONG;				
			}
			this.seqName = fieldName;
			this.gas = gas;
		}
	}

	@Override
	public JUnitEntityType getType() {
		return JUnitEntityType.SEQUENCE;
	}
	
	public String getSeqName() {
		return seqName;
	}
	
	public long nextValue(final Object instance) throws ContentException {
		switch (type) {
			case SEQ_TYPE_INT	:
				final int	intVal = ((IntGetterAndSetter)gas).get(instance)+1;
				
				((IntGetterAndSetter)gas).set(instance,intVal);
				return intVal;
			case SEQ_TYPE_LONG	: 
				final long	longVal = ((LongGetterAndSetter)gas).get(instance)+1;
				
				((LongGetterAndSetter)gas).set(instance,longVal);
				return longVal;
			case SEQ_TYPE_ATOMIC_INT	:
				return ((ObjectGetterAndSetter<AtomicInteger>)gas).get(instance).incrementAndGet();
			case SEQ_TYPE_ATOMIC_LONG	:
				return ((ObjectGetterAndSetter<AtomicLong>)gas).get(instance).incrementAndGet();
			default : throw new UnsupportedOperationException();
		}
	}

	public long currentValue(final Object instance) throws ContentException {
		switch (type) {
			case SEQ_TYPE_INT	:
				return ((IntGetterAndSetter)gas).get(instance);
			case SEQ_TYPE_LONG	: 
				return ((LongGetterAndSetter)gas).get(instance);
			case SEQ_TYPE_ATOMIC_INT	: 
				return ((ObjectGetterAndSetter<AtomicInteger>)gas).get(instance).get();
			case SEQ_TYPE_ATOMIC_LONG	: 
				return ((ObjectGetterAndSetter<AtomicLong>)gas).get(instance).get();
			default : throw new UnsupportedOperationException();
		}
	}

	@Override
	public String toString() {
		return "JUnitSequenceImpl [seqName=" + seqName + ", type=" + type + "]";
	}
}