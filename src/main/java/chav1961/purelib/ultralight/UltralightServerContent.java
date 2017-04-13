package chav1961.purelib.ultralight;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class UltralightServerContent implements HttpHandler, Closeable {
	public static final String	ROOT_FILESYSTEM = "rootFileSystem";
	public static final String	MOUNT_FILESYSTEM = "mountFileSystem";
	
	private final Properties			props;
	private final DataSource			ds;
	private final FileSystemInterface	root;
	
	public UltralightServerContent(final Properties props, final DataSource ds) throws IOException {
		if (props == null) {
			throw new IllegalArgumentException("Properties can't be null");
		}
		else if (!props.containsKey(ROOT_FILESYSTEM)) {
			throw new IllegalArgumentException("Properties not contains mandatory paremeter ["+ROOT_FILESYSTEM+"]");
		}
		else {
			this.props = props;		this.ds = ds;
			this.root = FileSystemFactory.createFileSystem(URI.create(props.getProperty(ROOT_FILESYSTEM)));
			
			mount(root);
		}
	}
	
	@Override
	public void close() throws IOException {
		unmount(root);
		root.close();
	}

	@Override
	public void handle(final HttpExchange exchange) throws IOException {
		if (exchange == null)  {
			throw new IllegalArgumentException("Exchange can't be null");
		}
		else {
			final Map<String,Object>	parameters = new HashMap<String,Object>();
			
			parameters.put("method",exchange.getRequestMethod());
			switch (exchange.getRequestMethod()) {
				case "GET" 		:
					
				case "POST"		:
				case "PUT"		:
				case "DELETE"	:
				case "OPTIONS"	:
				default :
			}
		}
	}

	private void mount(final FileSystemInterface root) throws IOException {
		for (String item : root.list()) {
			final Map<String,Object>	props = root.push(item).getAttributes(); 
			
			if (root.isDirectory()) {
				if (props.containsKey(MOUNT_FILESYSTEM)) {
					root.mount(FileSystemFactory.createFileSystem(URI.create(props.get(MOUNT_FILESYSTEM).toString())));
				}
				else {
					mount(root);
				}
			}
			root.pop();
		}
	}

	private void unmount(final FileSystemInterface root) throws IOException {
		for (String item : root.list()) {
			final Map<String,Object>	props = root.push(item).getAttributes(); 
			
			if (root.isDirectory()) {
				if (props.containsKey(MOUNT_FILESYSTEM)) {
					root.unmount().close();
				}
				else {
					unmount(root);
				}
			}
			root.pop();
		}
	}
}
