package chav1961.purelib.ui;

import java.io.IOException;
import java.net.URI;
import java.util.ServiceLoader;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.ui.interfaces.UIServer;

/**
 * <p>This is utility class to support useful methods for UI.</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @see CreoleWriter 
 * @since 0.0.2
 */
public class UIServerFactory {
	
	private UIServerFactory(){
	}

	public static UIServer createUIServer(final URI serverType) throws IOException {
		if (serverType == null) {
			throw new NullPointerException("Server type can't be null");
		}
		else if (!serverType.isAbsolute()) {
			throw new IllegalArgumentException("Server type URI ["+serverType+"] need be absolute and need contain a scheme");
		}
		else {
			for (UIServer item : ServiceLoader.load(UIServer.class)){
				try{if (item.canServe(serverType)) {
						return item.newInstance(serverType);
					}
				} catch (EnvironmentException e) {
					throw new IOException("Error creating UI server instance for ["+serverType+"]: "+e.getCause().getMessage());
				}
			}
			throw new IOException("No registered UI servers supported the resource URI ["+serverType+"]");
		}
	}
}
