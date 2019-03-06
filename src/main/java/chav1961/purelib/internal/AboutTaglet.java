package chav1961.purelib.internal;

import java.util.Map;

import com.sun.tools.doclets.Taglet;

public class AboutTaglet extends AbstractTaglet {
	@Override public String getName() {return PureLibDoclet.TAGNAME_ABOUT;}
	@Override public boolean inConstructor() {return false;}
	@Override public boolean inField() {return false;}
	@Override public boolean inMethod() {return false;}
	@Override public boolean inOverview() {return false;}
	@Override public boolean inPackage() {return false;}
	@Override public boolean inType() {return true;}

	public static void register(final Map<String,Taglet> tagletMap) {
		register(tagletMap,new AboutTaglet());
	}
}
