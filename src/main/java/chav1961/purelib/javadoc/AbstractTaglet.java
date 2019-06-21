package chav1961.purelib.javadoc;

import java.util.Map;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

abstract class AbstractTaglet implements Taglet {
	@Override public abstract String getName();
	@Override public abstract boolean inConstructor();
	@Override public abstract boolean inField();
	@Override public abstract boolean inMethod();
	@Override public abstract boolean inOverview();
	@Override public abstract boolean inPackage();
	@Override public abstract boolean inType();
	
	@Override
	public boolean isInlineTag() {
		return false;
	}

	@Override
	public String toString(final Tag tag) {
		return tag.text();
	}

	@Override
	public String toString(final Tag[] tags) {
		final StringBuilder	sb = new StringBuilder();
		
		for (Tag item : tags) {
			sb.append(',').append(item.text());
		}
		return sb.delete(0,0).toString();
	}

	static void register(final Map<String,Taglet> tagletMap, final Taglet tag) {
		final Taglet 	old = tagletMap.get(tag.getName());
		
		if (old != null) {
			tagletMap.remove(tag.getName());
		}
		tagletMap.put(tag.getName(), tag);
    }
}
