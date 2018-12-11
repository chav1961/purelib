package chav1961.purelib.streams.char2byte.asm.macro;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.streams.char2byte.AsmWriter;

public class MacroClassLoader extends URLClassLoader {
    private final Map<String,Class<?>> 	classesHash = new HashMap<>();
	private final Writer				diagnostics;
	private final List<String>			antiRecursion = new ArrayList<>();
    
    public MacroClassLoader(final ClassLoader parent) {
    	super(new URL[0],parent);
        this.diagnostics = null;
    }

    public MacroClassLoader(final ClassLoader parent, final Writer diagnostics) {
    	super(new URL[0],parent);
        this.diagnostics = diagnostics;
    }
    
    @Override
    public void close() throws IOException {
    	classesHash.clear();
    	super.close();
    }
    
	@SuppressWarnings("unchecked")
	public Class<? extends MacroExecutor> createClass(final String className, final GrowableCharArray asmContent) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final Reader	rdr = asmContent.getReader();
				final Writer	wr = new AsmWriter(baos)) {
			
				Utils.copyStream(rdr,wr);
			}
			
			final byte[]		content = baos.toByteArray();
			
			if (diagnostics != null) {
				diagnostics.write("Define macro class ["+className+"]...\n");
			}

			final Class<? extends MacroExecutor>	executor = (Class<? extends MacroExecutor>) defineClass(className,content,0,content.length);
			
			if (diagnostics != null) {
				diagnostics.write("Macro class ["+className+"] defined successfully\n");
			}
			return executor;
		}
	}

	@Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		if (diagnostics != null) {
			try{diagnostics.write("Load class: "+name+"\n");
			} catch (IOException e) {
			}
		}
		if (mustUseParentLoader(name)) {
			return super.loadClass(name, resolve);
		}
		else {
			final Class<?>	clazz = findClass(name);
			
			if (resolve) {
				resolveClass(clazz);
			}
			return clazz;
		}
    }
	
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
		if (diagnostics != null) {
			try{diagnostics.write("Find: "+name+"\n");
			} catch (IOException e) {
			}
		}
    	
        Class<?> 		result = classesHash.get(name);
        
        if (result != null) {
            return result;
        }
        
        final URL		anchor = MacroClassLoader.class.getResource("MacroClassLoader.class");
        final String	resourceName = anchor.toString().replace("MacroClassLoader.class",name.substring(name.lastIndexOf('.')+1)+".class");
		
        try{final URL	resource = new URL(resourceName);
	    	
    		try(final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
    			try(final InputStream		is = resource.openStream()) {
    				Utils.copyStream(is,baos);
    			}
    			if (diagnostics != null) {
    				try{diagnostics.write("Build local: "+name+"\n");
    				} catch (IOException e) {
    				}
    			}
    			result = defineClass(name,baos.toByteArray(),0,baos.size());
    			if (diagnostics != null) {
    				try{diagnostics.write("Local created: "+name+"\n");
    				} catch (IOException e) {
    				}
    			}
    			classesHash.put(name,result);
    			
    			return result;
    		} catch (IOException e) {
    			return this.getParent().loadClass(name);
			}
		} catch (LinkageError exc) {
			synchronized(antiRecursion) {
				if (antiRecursion.contains(name)) {
	    			if (diagnostics != null) {
	    				try{diagnostics.write("Local conflict: : "+name+"\n");
	    				} catch (IOException e) {
	    				}
	    			}
		        	antiRecursion.add(name);
		        	final Class<?>	found =  super.loadClass(name,false);
		        	antiRecursion.remove(name);
		        	return found;
				}
				else {
					return null;
				}
			}
		} catch (MalformedURLException e) {
        	throw new ClassNotFoundException(name+": "+e.getLocalizedMessage(),e);
		}
    }

    private boolean mustUseParentLoader(final String className) {
        return !className.toLowerCase().startsWith("chav1961.purelib.streams.char2byte.asm.macro"); 
    }
}