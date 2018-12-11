package chav1961.purelib.ui.nanoservice.internal;

import java.io.InputStream;

import chav1961.purelib.ui.nanoservice.interfaces.FromBody;
import chav1961.purelib.ui.nanoservice.interfaces.RootPath;

@RootPath("/root/internal")
public class PseudoPlugin2 {
	public int call(@FromBody(mimeType="text/plain") final InputStream is) {
		return 0;
	}
}
