package chav1961.purelib.basic;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

class UnsafedUtils {
	private static final MethodHandle	ACCESS_STRING_CONTENT;
	
	static {
		final MethodHandles.Lookup 	lookup = MethodHandles.lookup();
		MethodHandle				mh;
		
		try{mh = lookup.findGetter(String.class, "value", char[].class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			mh = null;
		}
		ACCESS_STRING_CONTENT = mh;
	}
	
	public static char[] getStringContent(final String source) {
		if (ACCESS_STRING_CONTENT != null) {
			try{return (char[])ACCESS_STRING_CONTENT.invokeExact(source);
			} catch (ThreadDeath e) {
				throw e; 
			} catch (Throwable e) {
				return source.toCharArray();
			}
		}
		else {
			return source.toCharArray();
		}
	}
}
