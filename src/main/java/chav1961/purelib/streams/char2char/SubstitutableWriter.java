package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.CharUtils.CharSubstitutionSource;
import chav1961.purelib.basic.CharUtils.SubstitutionSource;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.ModelUtils;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

/**
 * <p>This class processes output stream to seek &lt;START_KEY&gt;&lt;key_name&gt;&lt;END_KEY&gt; char sequences inside content to write,  and substitute &lt;key_name&gt; with
 * key value associated. It uses {@linkplain CharUtils.SubstitutionSource} or {@linkplain CharUtils.CharSubstitutionSource} to substitute key with it's value. You also can build
 * this substitutions with static methods {@linkplain #buildSubstitutionSource(Object)} and {@linkplain #buildSubstitutionSource(ContentNodeMetadata, Object)}.</p>     
 * @see CharUtils.SubstitutionSource
 * @see CharUtils.CharSubstitutionSource
 * @see chav1961.purelib.streams.char2char JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public class SubstitutableWriter extends Writer {
	public static final String		DEFAULT_START_KEY = "${";
	public static final String		DEFAULT_END_KEY = "}";
	public static final char[]		DEFAULT_START_CHAR_KEY = DEFAULT_START_KEY.toCharArray();
	public static final char[]		DEFAULT_END_CHAR_KEY = DEFAULT_END_KEY.toCharArray();

	private static final SimpleURLClassLoader						INTERNAL_LOADER = new SimpleURLClassLoader(new URL[0]);
	private static final Map<Class<?>,Class<Map<Object, Object>>>	INTERNAL_MAP = new HashMap<>();
	
	private static final String		EMPTY_STRING = "";

	private  static final int		STATE_ORDINAL = 0;
	private  static final int		STATE_CAN_BE_START = 1;
	private  static final int		STATE_INSIDE_KEY = 2;
	private  static final int		STATE_CAN_BE_END = 3;
	
	private final Writer			nested;	
	private final char[]			startKey, endKey;
	private final StringBuilder		sb = new StringBuilder();
	private final SubstitutionSource	ss;
	private int						currentState = STATE_ORDINAL;
	private int						cursor;

	/**
	 * <p>Constructor of the class.</p>
	 * @param nested writer to write processed content to. Can't be null
	 * @param instance instance to use it-s fields for substitution. Can't be null and must implements {@linkplain ModuleAccessor} interface
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameter is null 
	 */
	public SubstitutableWriter(final Writer nested, final Object instance) throws IOException, NullPointerException {
		this(nested, buildSubstitutionSource(instance), DEFAULT_START_KEY, DEFAULT_END_KEY);
	}
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param nested writer to write processed content to. Can't be null
	 * @param meta model descriptor for the instance class. If null, no substitutions will be processed
	 * @param instance instance to use it-s fields for substitution.  If null, no substitutions will be processed, otherwise it must implements {@linkplain ModuleAccessor} interface. 
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameter is null 
	 */
	public SubstitutableWriter(final Writer nested, final ContentNodeMetadata meta, final Object instance) throws IOException, NullPointerException {
		this(nested, buildSubstitutionSource(meta, instance), DEFAULT_START_KEY, DEFAULT_END_KEY);
	}
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param nested writer to write processed content to. Can't be null
	 * @param ss substitution source. Can't be null
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameter is null 
	 */
	public SubstitutableWriter(final Writer nested, final SubstitutionSource ss) throws IOException, NullPointerException {
		this(nested, ss, DEFAULT_START_KEY, DEFAULT_END_KEY);
	}

	/**
	 * <p>Constructor of the class.</p>
	 * @param nested writer to write processed content to. Can't be null
	 * @param ss substitution source. Can't be null
	 * @param startKey start sequence of the substitution marks in the stream to write. Can't be null or empty. Use {@linkplain} #DEFAULT_START_KEY} for default sequence. 
	 * @param endKey end sequence of the substitution marks in the stream to write. Can't be null or empty. Use {@linkplain} #DEFAULT_END_KEY} for default sequence.
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameter is null 
	 * @throws IllegalArgumentException when start or end key are null or empty
	 */
	public SubstitutableWriter(final Writer nested, final SubstitutionSource ss, final String startKey, final String endKey) throws IOException, NullPointerException, IllegalArgumentException {
		if (nested == null) {
			throw new NullPointerException("Nested writer can't be null");
		}
		else if (ss == null) {
			throw new NullPointerException("Substitution source can't be null");
		}
		else if (startKey == null || startKey.isEmpty()) {
			throw new IllegalArgumentException("Start key can't be null or empty");
		}
		else if (endKey == null || endKey.isEmpty()) {
			throw new IllegalArgumentException("End key can't be null or empty");
		}
		else {
			this.nested = nested;
			this.startKey = startKey.toCharArray();
			this.endKey = endKey.toCharArray();
			this.ss = ss;
		}
	}

	/**
	 * <p>Constructor of the class.</p>
	 * @param nested writer to write processed content to. Can't be null
	 * @param ss substitution source. Can't be null
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameter is null 
	 */
	public SubstitutableWriter(final Writer nested, final CharSubstitutionSource ss) throws IOException, NullPointerException {
		this(nested, ss, DEFAULT_START_CHAR_KEY, DEFAULT_END_CHAR_KEY);
	}
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param nested writer to write processed content to. Can't be null
	 * @param ss substitution source. Can't be null
	 * @param startKey start sequence of the substitution marks in the stream to write. Can't be null or empty. Use {@linkplain #DEFAULT_START_CHAR_KEY} for default sequence. 
	 * @param endKey end sequence of the substitution marks in the stream to write. Can't be null or empty. Use {@linkplain} #DEFAULT_END_CHAR_KEY} for default sequence.
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameter is null 
	 * @throws IllegalArgumentException when start or end key are null or empty
	 */
	public SubstitutableWriter(final Writer nested, final CharSubstitutionSource ss, final char[] startKey, final char[] endKey) throws IOException, NullPointerException, IllegalArgumentException {
		if (nested == null) {
			throw new NullPointerException("Nested writer can't be null");
		}
		else if (ss == null) {
			throw new NullPointerException("Substitution source can't be null");
		}
		else if (startKey == null || startKey.length == 0) {
			throw new IllegalArgumentException("Start key can't be null or empty array");
		}
		else if (endKey == null || endKey.length == 0) {
			throw new IllegalArgumentException("End key can't be null or empty array");
		}
		else {
			this.nested = nested;
			this.startKey = startKey;
			this.endKey = endKey;
			this.ss = toSubstitutionSource(ss);
		}
	}
	
	@Override
	public void write(final char[] cbuf, int off, int len) throws IOException {
        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
        	throw new IndexOutOfBoundsException();
        } else if (len != 0) {
        	int		from = off, to = off+len;
        	
        	for(int index = off; index < off+len; index++) {
	    		switch (currentState) {
    				case STATE_ORDINAL 		:
    					if (cbuf[index] == startKey[0]) {
    						if (index > from) {
        						sync(cbuf, from, index);
        						from = index;
    						}
    						sb.setLength(0);
    						sb.append(cbuf[index]);
    						cursor = 1;
    						currentState = STATE_CAN_BE_START;
    					}
    					break;
    				case STATE_CAN_BE_START	:
    					if (cursor >= startKey.length) {
    						sb.append(cbuf[index]);
    						currentState = STATE_INSIDE_KEY;
    					}
    					else if (cbuf[index] != startKey[cursor]) {
    						if (index > from) {
        						sync(sb.toString());
        						from = index;
    						}
    						currentState = STATE_ORDINAL;
    					}
    					else {
    						sb.append(cbuf[index]);
    						cursor++;
    					}
    					break;
    				case STATE_INSIDE_KEY	:
    					if (cbuf[index] == endKey[0]) {
    						cursor = 1;
    						currentState = STATE_CAN_BE_END;
    					}
   						sb.append(cbuf[index]);
    					break;
    				case STATE_CAN_BE_END	:
    					if (cursor >= endKey.length) {
    						currentState = STATE_ORDINAL;
    						write(ss.getValue(sb.substring(startKey.length, sb.length()-endKey.length)));
    						from = index;
    					}
    					else if (cbuf[index] != endKey[cursor]) {
    						if (index > from) {
        						sync(sb.toString());
        						from = index;
    						}
    						currentState = STATE_ORDINAL;
    					}
    					else {
    						sb.append(cbuf[index]);
    						cursor++;
    					}
    					break;
	    		}
        	}
    		if (from < to) {
				sync(cbuf, from, to);
    		}
        }
	}

	@Override
	public void flush() throws IOException {
		nested.flush();
	}

	@Override
	public void close() throws IOException {
		if (currentState != STATE_ORDINAL) {
			write(sb.toString());
		}
		flush();
		nested.close();
	}

	private void sync(final String content) throws IOException {
		nested.write(sb.toString());
	}
	
	private void sync(final char[] content, final int from, final int to) throws IOException {
		nested.write(content, from, to-from);
	}
	
	/**
	 * <p>Build substitution source by annotated class.</p>
	 * @param instance instance to build substitution source for. Can't be null and must be annotated as described in {@linkplain ContentModelFactory#forAnnotatedClass(Class)}
	 * @return substitution source built. Can't be null.
	 * @throws IOException on any errors during build
	 * @throws NullPointerException if instance is null
	 * @see ContentModelFactory#forAnnotatedClass(Class)
	 */
	public static SubstitutionSource buildSubstitutionSource(final Object instance) throws IOException, NullPointerException {
		if (instance == null) {
			throw new NullPointerException("Instance can't be null"); 
		}
		else {
			try{return buildSubstitutionSource(ContentModelFactory.forAnnotatedClass(instance.getClass()).getRoot(), instance);
			} catch (ContentException e) {
				throw new IOException(e.getLocalizedMessage(), e); 
			}
		}
	}

	/**
	 * <p>Build substitution source by class and model</p>
	 * @param meta model to build substitution source for. Can't be null.
	 * @param instance instance to build substitution source for. Can't be null 
	 * @return substitution source built. Can't be null.
	 * @throws IOException on any errors during build
	 * @throws NullPointerException if instance is null
	 * @throws NullPointerException if instance doesn't implement {@linkplain ModuleAccessor} interface
	 * @see ModelUtils#buildMappedWrapperClassByModel(ContentNodeMetadata, String)
	 */
	public static SubstitutionSource buildSubstitutionSource(final ContentNodeMetadata meta, final Object instance) throws IOException, NullPointerException, IllegalArgumentException {
		if (meta == null || instance == null) {
			return (s)->s;
		}
		else if (!(instance instanceof ModuleAccessor)) {
			throw new IllegalArgumentException("Instance to build substitution for must implements "+ModuleAccessor.class.getName()+" interface"); 
		}
		else {
			try{
				synchronized (INTERNAL_MAP) {
					final Class<Map<Object, Object>>	mapClass;
					final boolean						needAllowAccess;
					
					if (INTERNAL_MAP.containsKey(meta.getType())) {
						mapClass = INTERNAL_MAP.get(meta.getType());
						needAllowAccess = false;
					}
					else {
						mapClass = ModelUtils.buildMappedWrapperClassByModel(meta, meta.getType().getName()+"$SubstitutableWriter$Map", INTERNAL_LOADER);
						INTERNAL_MAP.put(meta.getType(), mapClass);
						needAllowAccess = true;
					}
					final Map<Object, Object>	map = mapClass.getConstructor(meta.getType()).newInstance(instance);
	
					if (needAllowAccess) {
						((ModuleAccessor)instance).allowUnnamedModuleAccess(INTERNAL_LOADER.getUnnamedModule());
					}
					return (s)->{
						if (map.containsKey(s)) {
							final Object	result = map.get(s);
							
							return result == null ? "null" : result.toString();
						}
						else {
							return s;
						}
					};
				}
			} catch (ContentException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
				throw new IOException(e.getLocalizedMessage(), e);
			}
		}
	}
	
	private static SubstitutionSource toSubstitutionSource(final CharSubstitutionSource ss) {
		return new SubstitutionSource() {
			@Override
			public String getValue(String key) {
				final char[]	result = ss.getValue(key.toCharArray(), 0, key.length());
				
				return result == null ? EMPTY_STRING : new String(result);
			}
		};
	}
}
