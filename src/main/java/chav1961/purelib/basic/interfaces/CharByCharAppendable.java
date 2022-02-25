package chav1961.purelib.basic.interfaces;

@FunctionalInterface
public interface CharByCharAppendable extends Appendable {
    default Appendable append(final CharSequence csq) {
    	if (csq == null) {
    		throw new NullPointerException("Sequence to append can't be null"); 
    	}
    	else {
        	return append(csq, 0, csq.length());
    	}
    }

    default Appendable append(final CharSequence csq, final int start, final int end) {
    	if (csq == null) {
    		throw new NullPointerException("Sequence to append can't be null"); 
    	}
    	else if (start < 0 || start >= csq.length()) {
    		throw new IllegalArgumentException("Start position ["+start+"] out of range 0.."+(csq.length()-1)); 
    	}
    	else if (end < 0 || end > csq.length()) {
    		throw new IllegalArgumentException("End position ["+end+"] out of range 0.."+(csq.length())); 
    	}
    	else if (start > end) {
    		throw new IllegalArgumentException("Start position ["+start+"] is greater than end position ["+end+"]"); 
    	}
    	else {
    		for (int index = start; index < end; index++) {
    			append(csq.charAt(index));
    		}
    		return this;
    	}
    }

    Appendable append(char c);
}
