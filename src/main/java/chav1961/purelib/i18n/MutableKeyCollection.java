package chav1961.purelib.i18n;

import java.net.URI;
import java.util.Map;

class MutableKeyCollection extends KeyCollection {

	MutableKeyCollection(final Map<String, String> keysAndValues, final Map<String, URI> helpRefs) {
		super(keysAndValues, helpRefs);
	}
}
