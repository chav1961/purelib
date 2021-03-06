package chav1961.purelib.fsys;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class is a class loader, based on {@linkplain FileSystemInterface} file system.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class FileSystemClassLoader extends ClassLoader {
	private final FileSystemInterface	fsi;
	
	/**
	 * <p>Constructor if the class</p>
	 * @param parent parent class loader
	 * @param fsi file system to find class and resources
	 * @throws NullPointerException when the file system is null
	 */
	public FileSystemClassLoader(final ClassLoader parent, final FileSystemInterface fsi) throws NullPointerException {
        super(parent);
        if (fsi == null) {
        	throw new NullPointerException("File system interface ref can't be null"); 
        }
        else {
        	this.fsi = fsi;
        }
    }
	
	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		final String	path = '/'+name.replace('.', '/')+".class";
		
		try(final FileSystemInterface	file = fsi.clone().open(path)) {
			if (file.exists() && file.isFile() && file.canRead()) {
				try(final InputStream			is = file.read();
					final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
					
					Utils.copyStream(is,baos);
					final byte[]	content = baos.toByteArray(); 
					
					return defineClass(name,content,0,content.length);
				}
			}
			else {
				return super.findClass(name);
			}
		} catch (IOException e) {
			throw new ClassNotFoundException("Class ["+name+"]: I/O error loading from file system ("+e.getLocalizedMessage()+")");
		} 
    }

	@Override
    protected URL findResource(final String name) {
		try(final FileSystemInterface	file = fsi.clone().open(name)) {
			if (file.exists() && file.isFile() && file.canRead()) {
				return file.toURI().toURL();
			}
			else {
				return null;
			}
		} catch (IOException e) {
			return null;
		} 
    }

	@Override
    protected Enumeration<URL> findResources(final String name) throws IOException {
		final URL	result = findResource(name);
        
		return new Enumeration<URL>() {
			int		count = 0;
			
			@Override
			public boolean hasMoreElements() {
				return count == 0;
			}

			@Override
			public URL nextElement() {
				count++;
				return result;
			}
		};
    }
}
