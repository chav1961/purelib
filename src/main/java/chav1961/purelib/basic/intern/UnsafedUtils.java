package chav1961.purelib.basic.intern;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import chav1961.purelib.basic.PureLibSettings;

public class UnsafedUtils {
//	private static sun.misc.Unsafe		unsafe;
	private static final MethodHandle	ACCESS_STRING_CONTENT;
	private static final long			STRING_VALUE_DISPL;
	
	static {
//		if (PureLibSettings.instance().getProperty(PureLibSettings.ALLOW_UNSAFE,boolean.class,"false")) {
//			long	valueDispl = -1;
//			
//			try{final Field			f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
//			
//				f.setAccessible(true);
//				unsafe = (Unsafe) f.get(null);
//				f.setAccessible(false);
//
//				
//				try{final Field		ff = String.class.getDeclaredField("value");		
//					
//					if (ff.getDeclaringClass() == char[].class) {
//						valueDispl = unsafe.objectFieldOffset(ff);
//					}
//					else {
//						valueDispl = -1;
//					}
//				} catch (NoSuchFieldException  e) {
//				}
//			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
//				PureLibSettings.logger.log(Level.WARNING,"["+PureLibSettings.ALLOW_UNSAFE+"] property was typed for the Pure Library, but attempt to get access to Unsafe functionality failed: "+e.getMessage()+". This ability will be ignored", e);
//				unsafe = null;
//			}
//			STRING_VALUE_DISPL = valueDispl;
			STRING_VALUE_DISPL = -1;
//		} 
//		else {
//			STRING_VALUE_DISPL = -1;
////			unsafe = null;
//		}
		
		final MethodHandles.Lookup 	lookup = MethodHandles.lookup();
		MethodHandle		mh;
		
		try{final Field 	f = String.class.getDeclaredField("value");

			if (f.getDeclaringClass() == char[].class) {
			 	f.setAccessible(true);
			 	mh = lookup.unreflectGetter(f);
			 	f.setAccessible(false);
			}
			else {
				mh = null;
			}
		} catch (NoSuchFieldException | IllegalAccessException e) {
			mh = null;
		}
		ACCESS_STRING_CONTENT = mh;
	}
	
	public static char[] getStringContent(final String source) {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
//		else if (STRING_VALUE_DISPL >= 0) {
//			return (char[])unsafe.getObject(source,STRING_VALUE_DISPL);
//		}
		else if (ACCESS_STRING_CONTENT != null) {
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
