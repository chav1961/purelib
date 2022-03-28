package chav1961.purelib.streams.char2byte.asm.macro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.streams.char2byte.AsmWriter;

public class MacroClassLoader extends URLClassLoader {
    private static final String			ANCHOR;

    private final Map<String,Class<?>> 	classesHash = new HashMap<>();
	private final Writer				diagnostics;
	private final Set<String>			antiRecursion = new HashSet<>();
    
	static {
		final String	anchor = MacroClassLoader.class.getResource(MacroClassLoader.class.getSimpleName()+".class").toString();
		
		ANCHOR = anchor.replace(MacroClassLoader.class.getSimpleName()+".class",""); 
	}
	
	
    public MacroClassLoader(final ClassLoader parent) {
    	super(new URL[0],parent);
        this.diagnostics = null;
    }

    public MacroClassLoader(final ClassLoader parent, final Writer diagnostics) throws NullPointerException {
    	super(new URL[0],parent);
    	if (diagnostics == null) {
    		throw new NullPointerException("Diagnostics stream can't be null");
    	}
    	else {
            this.diagnostics = diagnostics;
    	}
    }
    
    @Override
    public void close() throws IOException {
    	classesHash.clear();
    	super.close();
    }
    
	@SuppressWarnings("unchecked")
	public Class<? extends MacroExecutor> createClass(final String className, final GrowableCharArray<?> asmContent) throws IOException, IllegalArgumentException, NullPointerException {
		if (className == null || className.isEmpty()) {
			throw new IllegalArgumentException("Class name can't be null or empty"); 
		}
		else if (asmContent == null) {
			throw new NullPointerException("Assembly content array can't be null"); 
		}
		else {
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				try(final Reader	rdr = asmContent.getReader();
					final Writer	wr = diagnostics != null ? new AsmWriter(baos,diagnostics) : new AsmWriter(baos)) {
				
					Utils.copyStream(rdr, wr);
				}
				
				diag("Define macro class ["+className+"]...");
				
				final byte[]		content = baos.toByteArray();
				final Class<? extends MacroExecutor>	executor = (Class<? extends MacroExecutor>) defineClass(className,content,0,content.length);
				
				diag("Macro class ["+className+"] defined successfully");
				return executor;
			}
		}
	}

	@Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		diag("Load class: "+name);
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
        Class<?> 		result = classesHash.get(name);
        
		diag("Find: "+name);
        if (result != null) {
            return result;
        }
        
        final String	resourceName = ANCHOR+name.substring(name.lastIndexOf('.')+1)+".class";
        final URI		resourceNameURI = URI.create(resourceName);
		
        try{final URL	resource = resourceNameURI.isAbsolute() ? resourceNameURI.toURL() : this.getClass().getResource(resourceNameURI.getSchemeSpecificPart());
	    	
        	if (resource != null) {
	    		try(final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
	    			try(final InputStream		is = resource.openStream()) {
	    				Utils.copyStream(is,baos);
	    			}
	    			
	    			diag("Build local: "+name);
	    			result = defineClass(name,baos.toByteArray(),0,baos.size());
	    			diag("Local created: "+name);
	    			classesHash.put(name,result);
	    			
	    			return result;
	    		} catch (IOException e) {
	    			return this.getParent().loadClass(name);
				}
        	}
        	else {
    			return this.getParent().loadClass(name);
        	}
		} catch (MalformedURLException exc) {
			return this.getParent().loadClass(name);
		} catch (LinkageError exc) {
			synchronized(antiRecursion) {
				if (antiRecursion.contains(name)) {
	    			diag("Local conflict: : "+name);
		        	antiRecursion.add(name);
		        	final Class<?>	found =  super.loadClass(name,false);
		        	antiRecursion.remove(name);
		        	return found;
				}
				else {
					return null;
				}
			}
		}
    }

    private boolean mustUseParentLoader(final String className) {
        return !className.toLowerCase().startsWith(MacroClassLoader.class.getPackage().getName()) || className.equals(MacroExecutor.class.getCanonicalName()); 
//        return !className.toLowerCase().startsWith("chav1961.purelib.streams.char2byte.asm.macro"); 
    }
    
    private void diag(final String text) {
		if (diagnostics != null) {
			try{diagnostics.write(text+"\n");
			} catch (IOException e) {
			}
		}
   }
}