package chav1961.purelib.basic;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class is an implementation of class loader, that can install and uninstall jars with the additional classes on-the-fly.
 * This class loader is a collection of <i>internal</i> class loaders, every of this loads it's own plugin. The overridden 
 * {@link #loadClass(String, boolean)} method of this class loader tries to find appropriative classes in the each class loader
 * in the collection. When one of the plugins will be uninstalled, find fails.</p>
 * <p>The template to use this class is:</p>
 * <p><code>PluggableClassLoader pcl = new PluggableClassLoader(parent);<br>
 * pcl.install(...) // install new plugin<br>
 * Class cl = pcl.loadClass("Class.Name") // get any class from the plugin loaded<br>
 * // use plugin class
 * pcl.uninstall(...) // uninstall existent plugin<br>
 * </code></p>
 * <p>WARNING! Don't use {@link Class#forName(String, boolean, ClassLoader)} method to load classes from this loader, because
 * this method has an internal cache for all classes loaded, and this cache doesn't clear when the plugin will be uninstalled</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see java.lang.ClassLoader
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class PluggableClassLoader extends ClassLoader implements Closeable {
	private InternalClassLoader[]	loaders = new InternalClassLoader[0];
	
	public PluggableClassLoader(final ClassLoader parent) {
		super(parent);
	}

	@Override
	public void close() throws IOException {
		synchronized(this) {
			for (InternalClassLoader item : loaders) {
				item.loadedClasses.clear();
				item.content.close();
			}
		}
		loaders = null;
	}
	
	@Override
	protected Class<?> findClass(final String className) throws ClassNotFoundException {
		synchronized(this) {
			for (int index = 0; index < loaders.length; index++) {
				if (loaders[index].hasClass(className)) {
					return loaders[index].findClass(className);
				}
			}
			return super.findClass(className);
		}
	}
	
	@Override
    protected Class<?> loadClass(final String className, final boolean resolve) throws ClassNotFoundException {
		synchronized(this) {
			for (int index = 0; index < loaders.length; index++) {
				if (loaders[index].hasClass(className)) {
					return loaders[index].loadClass(className,resolve);
				}
			}
			return super.loadClass(className,resolve);
		}
    }

	public void install(final String name, final String description, final FileSystemInterface content) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Plugin name can't be null"); 
		}
		else if (description == null || description.isEmpty()) {
			throw new IllegalArgumentException("Plugin name can't be null"); 
		}
		else if (content == null) {
			throw new IllegalArgumentException("Content can't be null"); 
		}
		else {
			synchronized(this) {
				for (InternalClassLoader item : loaders) {
					if (item.getName().equals(name)) {
						throw new IllegalArgumentException("Plugin name ["+name+"] already installed in the system"); 
					}
				}
				final InternalClassLoader[]	newLoaders = new InternalClassLoader[loaders.length+1];
				
				System.arraycopy(loaders, 0, newLoaders, 0, loaders.length);
				newLoaders[loaders.length] = new InternalClassLoader(this,name,description,content);
				loaders = newLoaders;
			}
		}
	}

	public void uninstall(final String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Plugin name can't be null"); 
		}
		else {
			synchronized(this) {
				for (int index = 0; index < loaders.length; index++) {
					if (loaders[index].getName().equals(name)) {
						loaders[index].close();
						for (int pack = index; pack < loaders.length-1; pack++) {
							loaders[pack] = loaders[pack+1];
						}
						final InternalClassLoader[]	newLoaders = new InternalClassLoader[loaders.length-1];
						
						System.arraycopy(loaders, 0, newLoaders, 0, loaders.length-1);
						loaders = newLoaders;
						return;
					}
				}
				throw new IllegalArgumentException("Plugin name ["+name+"] is not installed in the system");
			}
		}		
	}

	public boolean wasInstalled(final String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Plugin name can't be null"); 
		}
		else {
			synchronized(this) {
				for (InternalClassLoader item : loaders) {
					if (item.getName().equals(name)) {
						return true; 
					}
				}
				return false;
			}
		}
	}
	
	public String getDescription(final String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Plugin name can't be null"); 
		}
		else {
			synchronized(this) {
				for (InternalClassLoader item : loaders) {
					if (item.getName().equals(name)) {
						return item.getDescription(); 
					}
				}
				throw new IllegalArgumentException("Plugin name ["+name+"] is not installed in the system");
			}
		}
	}
	
	public String[] installed() {
		synchronized(this) {
			final String[]	result = new String[loaders.length];
					
			for (int index = 0; index < loaders.length; index++) {
				result[index] =  loaders[index].getName();
			}
			return result;
		}
	}

	
	private class InternalClassLoader extends ClassLoader implements Closeable {
		private final String		name;
		private final String 		description;
		private final FileSystemInterface	content;
		private final Map<String,Class<?>>	loadedClasses = new HashMap<>();	
		
		InternalClassLoader(final ClassLoader parent, final String name, final String description, final FileSystemInterface content) {
			super(parent);
			this.name = name;
			this.description = description;
			this.content = content;
		}

		@Override
		public void close() {
			loadedClasses.clear();
		}
		
		String getName() {
			return name;
		}

		String getDescription() {
			return description;
		}
		
		boolean hasClass(final String className) {
			if (loadedClasses.containsKey(className)) {
				return true;
			}
			else{
				final String	substitutedClassName = '/'+className.replace('.','/')+".class";
				
				try(final FileSystemInterface	fs = content.clone().open(substitutedClassName)) {
					
					return fs.exists() && fs.isFile() && fs.canRead();
				} catch (IOException e) {
					return false;
				}
			}
		}
		
		@Override
	    protected Class<?> loadClass(final String className, final boolean resolve) throws ClassNotFoundException {
			if (loadedClasses.containsKey(className)) {
				return loadedClasses.get(className);
			}
			else{
				final String	substitutedClassName = '/'+className.replace('.','/')+".class";
				
				try(final FileSystemInterface	fs = content.clone().open(substitutedClassName)) {
					
					if (fs.exists() && fs.isFile() && fs.canRead()) {
						try(final InputStream			is = fs.read();
							final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
							final byte[]	buffer = new byte[8192];
							int				len;
							
							while ((len = is.read(buffer)) > 0) {
								baos.write(buffer,0,len);
							}
							final Class<?>		cl = defineClass(className,baos.toByteArray(),0,baos.size());
							
							loadedClasses.put(className,cl);
							if (resolve) {
								resolveClass(cl);
							}
							return cl;
						}
					}
					else {
						return getParent().loadClass(className);
					}
				} catch (IOException e) {
					throw new ClassNotFoundException("Class ["+className+"] : I/O error on loading data: "+e.getMessage());
				}
			}
	    }

		@Override
	    protected Class<?> findClass(final String className) throws ClassNotFoundException {
			if (loadedClasses.containsKey(className)) {
				return loadedClasses.get(className);
			}
			else{
				final String	substitutedClassName = '/'+className.replace('.','/')+".class";
				
				try(final FileSystemInterface	fs = content.clone().open(substitutedClassName)) {
					
					if (fs.exists() && fs.isFile() && fs.canRead()) {
						try(final InputStream			is = fs.read();
							final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
							final byte[]	buffer = new byte[8192];
							int				len;
							
							while ((len = is.read(buffer)) > 0) {
								baos.write(buffer,0,len);
							}
							final Class<?>		cl = defineClass(className,baos.toByteArray(),0,baos.size());
							
							loadedClasses.put(className,cl);
							return cl;
						}
					}
					else {
						return getParent().loadClass(className);
					}
				} catch (IOException e) {
					throw new ClassNotFoundException("Class ["+className+"] : I/O error on loading data: "+e.getMessage());
				}
			}
	    }
	}
}
