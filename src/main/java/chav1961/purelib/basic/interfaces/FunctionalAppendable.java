package chav1961.purelib.basic.interfaces;

import java.io.IOException;

@FunctionalInterface
public interface FunctionalAppendable extends Appendable {
	@Override
    default Appendable append(CharSequence csq) throws IOException {
		if (csq == null) {
			append('n');
			append('u');
			append('l');
			return append('l');
		}
		else {
			Appendable	last = this;
			
			for (int index = 0, maxIndex = csq.length(); index < maxIndex; index++) {
				last = append(csq.charAt(maxIndex));
			}
			return last;
		}
	}
	
	@Override
	default Appendable append(CharSequence csq, int start, int end) throws IOException {
		if (csq == null) {
			append('n');
			append('u');
			append('l');
			return append('l');
		}
		else {
			return append(csq.subSequence(start, end));
		}
	}

}
