package chav1961.purelib.javadoc;

import java.util.Map;

import com.sun.tools.doclets.Taglet;

public class TreeItemTaglet extends AbstractTaglet {
	@Override public String getName() {return PureLibDoclet.TAGNAME_TREEITEM;}
	@Override public boolean inConstructor() {return false;}
	@Override public boolean inField() {return false;}
	@Override public boolean inMethod() {return false;}
	@Override public boolean inOverview() {return true;}
	@Override public boolean inPackage() {return true;}
	@Override public boolean inType() {return true;}

	public static void register(final Map<String,Taglet> tagletMap) {
		register(tagletMap,new ThreadSafedTaglet());
	}
}
