package chav1961.purelib.streams.char2byte.asm.macro;


import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.CharUtils.CharSubstitutionSource;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.intern.UnsafedCharUtils;

class AssemblerTemplateRepo {
	private static final char[]		THE_END = "\n{_theend_}\n".toCharArray();
	
	private final AssemblerTemplateRepo.PartRecord[]	partNames;
	private final NameKeeper							nameKeeper = new NameKeeper(); 
	
	/**
	 * <p>Constructor of the class</p>
	 * @param content any input stream with parts descriptions (see this class description for syntax)
	 * @throws NullPointerException if input stream content is null
	 * @throws IOException if any I/O errors were detected
	 * @throws SyntaxException if any syntax errors in the parts description were detected 
	 */
	public AssemblerTemplateRepo(final InputStream content) throws NullPointerException, IOException, SyntaxException {
		if (content == null) {
			throw new NullPointerException("Input stream content can't be null"); 
		}
		else {
				
			final Map<char[],char[]>		parts = new HashMap<>(); 
	
			try(final Reader				rdr = new InputStreamReader(content);
				final LineByLineProcessor	lblp = new LineByLineProcessor(
												new LineByLineProcessorCallback(){
													final int[]				ranges = new int[2];
													final GrowableCharArray	content = new GrowableCharArray(true);
													char[]					name = null; 
												
													@Override
													public void processLine(final long displacement, final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
														final int	start = from, to = from + length;
														
														if (data[from] == '{') {
															from = UnsafedCharUtils.uncheckedParseName(data,from+1,ranges);
															if (from < to && data[from] == '}') {
																if (name != null) {
																	parts.put(name,content.extract());
																	content.clear();
																}
																name = Arrays.copyOfRange(data,start+1,from);
															}
															else {
																throw new SyntaxException(lineNo,from-start,"missing '}'");
															}
														}
														else {
															content.append(data,from,to);
														}
													}
												}
											)) {
				lblp.write(rdr);
				lblp.write(THE_END,0,THE_END.length);
			}
			int		index = 0;
			
			partNames = new AssemblerTemplateRepo.PartRecord[parts.size()];
			
			for (Entry<char[], char[]> item : parts.entrySet()) {
				partNames[index++] = new PartRecord(item.getKey(),item.getValue());
			}
			Arrays.sort(partNames);
		}
	}

	/**
	 * <p>Get the {@linkplain NameKeeper} instance associated with the given class instance</p>
	 * @return instance associated
	 */
	public NameKeeper getNameKeeper() {
		return nameKeeper;
	}

	/**
	 * <p>Append part name content into growable char array</p>
	 * @param arr array to append content into
	 * @param partName part name to append to
	 * @return self
	 * @throws NullPointerException if any parameters are null
	 * @throws IllegalArgumentException if any parameters are illegal
	 */
	public AssemblerTemplateRepo append(final GrowableCharArray arr, final char[] partName) throws NullPointerException, IllegalArgumentException {
		return append(arr,partName,getNameKeeper());
	}

	/**
	 * <p>Append part name content into growable char array using the given substitution callback</p>
	 * @param arr array to append content into
	 * @param partName part name to append to
	 * @param callback callback to substitute names (see {@linkplain CharSubstitutionSource} description)
	 * @return self
	 * @throws NullPointerException if any parameters are null
	 * @throws IllegalArgumentException if any parameters are illegal
	 */
	public AssemblerTemplateRepo append(final GrowableCharArray arr, final String partName, final CharSubstitutionSource callback) throws NullPointerException, IllegalArgumentException {
		if (partName == null || partName.isEmpty()) {
			throw new IllegalArgumentException("Part name can't be null or empty"); 
		}
		else {
			return append(arr,partName.toCharArray(),callback);
		}
	}

	/**
	 * <p>Append part name content into growable char array using the given substitution callback</p>
	 * @param arr array to append content into
	 * @param partName part name to append to
	 * @param callback callback to substitute names (see {@linkplain CharSubstitutionSource} description)
	 * @return self
	 * @throws NullPointerException if any parameters are null
	 * @throws IllegalArgumentException if any parameters are illegal
	 */
	public AssemblerTemplateRepo append(final GrowableCharArray arr, final char[] partName, final CharSubstitutionSource callback) throws NullPointerException, IllegalArgumentException {
		if (arr == null) {
			throw new NullPointerException("Char array can't be null"); 
		}
		else if (partName == null || partName.length == 0) {
			throw new IllegalArgumentException("Part name can't be null or empty array"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else {
			for (int index = 0, maxIndex = partNames.length; index < maxIndex; index++) {
				if (UnsafedCharUtils.uncheckedCompare(partName,0,partNames[index].name,0,partNames[index].name.length)) {
					arr.append(CharUtils.substitute("",partNames[index].content,0,partNames[index].content.length,callback));
					return this;
				}
			}
			throw new IllegalArgumentException("Part name ["+ new String(partName)+"] is missing in the repo"); 
		}
	}

	/**
	 * <p>Append string formatted to the growable array 'as-is'</p>
	 * @param arr array to append content into
	 * @param format string format (see {@linkplain String#format(String, Object...)}
	 * @param parameters format parameters
	 * @return self
	 * @throws NullPointerException if any parameters are null
	 * @throws IllegalArgumentException if any parameters are illegal
	 */
	public AssemblerTemplateRepo append(final GrowableCharArray arr, final String format, final Object... parameters) throws NullPointerException, IllegalArgumentException {
		if (arr == null) {
			throw new NullPointerException("Char array can't be null"); 
		}
		else if (format == null || format.isEmpty()) {
			throw new IllegalArgumentException("Format can't be null or empty"); 
		}
		else if (parameters == null) {
			throw new NullPointerException("Parameter's list can't be null"); 
		}
		else if (parameters.length == 0) {
			arr.append(format);
		}
		else {
			arr.append(String.format(format,parameters));
		}
		return this;
	}

	/**
	 * <p>This class is used in conjunction with the {@linkplain AssemblerTemplateRepo} class to support name substitutions (see {@linkplain AssemblerTemplateRepo} description)</p>
	 * @see chav1961.purelib.streams.char2byte.asm.macro.MacroCompilerTest
	 * @see chav1961.purelib.streams JUnit tests
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	static class NameKeeper implements CharSubstitutionSource, Closeable {
		private NameKeeper					parent;   
		private SyntaxTreeInterface<char[]>	content = null;
		
		private NameKeeper() {
			this.parent = null;
		}

		private NameKeeper(final NameKeeper parent) {
			this.parent = parent;
		}

		/**
		 * <p>Create nested instance to use in the child (resursive) code generator's calls. Use this instance in the <b>try-with-resource</b> statement!</p> 
		 * @return new nested {@linkplain NameKeeper} instance.
		 */
		public NameKeeper push() {
			return new NameKeeper(this);
		}

		/**
		 * <p>Put name/value pair into the instance
		 * @param name name to put. Using existent name replaces it's content
		 * @param value value associated with the given name
		 * @return self
		 * @throws IllegalArgumentException if any parameters are illegal
		 */
		public NameKeeper put(final String name, final boolean value) throws IllegalArgumentException {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Key name can't be null or empty");
			}
			else {
				return put(name.toCharArray(),value);
			}
		}

		/**
		 * <p>Put name/value pair into the instance
		 * @param name name to put. Using existent name replaces it's content
		 * @param value value associated with the given name
		 * @return self
		 * @throws IllegalArgumentException if any parameters are illegal
		 */
		public NameKeeper put(final String name, final long value) throws IllegalArgumentException {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Key name can't be null or empty");
			}
			else {
				return put(name.toCharArray(),value);
			}
		}

		/**
		 * <p>Put name/value pair into the instance
		 * @param name name to put. Using existent name replaces it's content
		 * @param value value associated with the given name
		 * @return self
		 * @throws IllegalArgumentException if any parameters are illegal
		 */
		public NameKeeper put(final String name, final double value) throws IllegalArgumentException {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Key name can't be null or empty");
			}
			else {
				return put(name.toCharArray(),value);
			}
		}

		/**
		 * <p>Put name/value pair into the instance
		 * @param name name to put. Using existent name replaces it's content
		 * @param value value associated with the given name
		 * @return self
		 * @throws IllegalArgumentException if any parameters are illegal
		 * @throws NullPointerException if value parameter is null
		 */
		public NameKeeper put(final String name, final char[] value) throws IllegalArgumentException, NullPointerException {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Key name can't be null or empty");
			}
			else if (value == null) {
				throw new NullPointerException("Value can't be null");
			}
			else {
				return put(name.toCharArray(),value);
			}
		}

		/**
		 * <p>Put name/value pair into the instance
		 * @param name name to put. Using existent name replaces it's content
		 * @param value value associated with the given name
		 * @return self
		 * @throws IllegalArgumentException if any parameters are illegal
		 * @throws NullPointerException if value parameter is null
		 */
		public NameKeeper put(final String name, final String value) throws IllegalArgumentException, NullPointerException {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Key name can't be null or empty");
			}
			else if (value == null) {
				throw new NullPointerException("Value can't be null");
			}
			else {
				return put(name.toCharArray(),value);
			}
		}

		/**
		 * <p>Put name/value pair into the instance
		 * @param name name to put. Using existent name replaces it's content
		 * @param value value associated with the given name
		 * @return self
		 * @throws IllegalArgumentException if any parameters are illegal
		 * @throws NullPointerException if value parameter is null
		 */
		public NameKeeper put(final String name, final Object value) throws IllegalArgumentException, NullPointerException {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Key name can't be null or empty");
			}
			else if (value == null) {
				throw new NullPointerException("Value can't be null");
			}
			else {
				return put(name.toCharArray(),value);
			}
		}

		/**
		 * <p>Put name/value pair into the instance
		 * @param name name to put. Using existent name replaces it's content
		 * @param value value associated with the given name
		 * @return self
		 * @throws IllegalArgumentException if any parameters are illegal
		 */
		public NameKeeper put(final char[] name, final boolean value) throws IllegalArgumentException {
			if (name == null || name.length == 0) {
				throw new IllegalArgumentException("Key name can't be null or zero-length array");
			}
			else {
				return put(name,String.valueOf(value));
			}
		}

		/**
		 * <p>Put name/value pair into the instance
		 * @param name name to put. Using existent name replaces it's content
		 * @param value value associated with the given name
		 * @return self
		 * @throws IllegalArgumentException if any parameters are illegal
		 */
		public NameKeeper put(final char[] name, final long value) throws IllegalArgumentException {
			if (name == null || name.length == 0) {
				throw new IllegalArgumentException("Key name can't be null or zero-length array");
			}
			else {
				return put(name,String.valueOf(value));
			}
		}

		/**
		 * <p>Put name/value pair into the instance
		 * @param name name to put. Using existent name replaces it's content
		 * @param value value associated with the given name
		 * @return self
		 * @throws IllegalArgumentException if any parameters are illegal
		 */
		public NameKeeper put(final char[] name, final double value) throws IllegalArgumentException {
			if (name == null || name.length == 0) {
				throw new IllegalArgumentException("Key name can't be null or zero-length array");
			}
			else {
				return put(name,String.valueOf(value));
			}
		}

		/**
		 * <p>Put name/value pair into the instance
		 * @param name name to put. Using existent name replaces it's content
		 * @param value value associated with the given name
		 * @return self
		 * @throws IllegalArgumentException if any parameters are illegal
		 * @throws NullPointerException if value parameter is null
		 */
		public NameKeeper put(final char[] name, final char[] value) throws IllegalArgumentException, NullPointerException {
			if (name == null || name.length == 0) {
				throw new IllegalArgumentException("Key name can't be null or zero-length array");
			}
			else if (value == null) {
				throw new NullPointerException("Value can't be null");
			}
			else {
				if (content == null) {	// Little optimization
					content = new AndOrTree<>(); 
				}
				content.placeName(name,0,name.length,value);
				return this;
			}
		}			

		/**
		 * <p>Put name/value pair into the instance
		 * @param name name to put. Using existent name replaces it's content
		 * @param value value associated with the given name
		 * @return self
		 * @throws IllegalArgumentException if any parameters are illegal
		 */
		public NameKeeper put(final char[] name, final String value) throws IllegalArgumentException {
			return put(name,value.toCharArray());
		}

		/**
		 * <p>Put name/value pair into the instance
		 * @param name name to put. Using existent name replaces it's content
		 * @param value value associated with the given name
		 * @return self
		 * @throws IllegalArgumentException if any parameters are illegal
		 * @throws NullPointerException if value parameter is null
		 */
		public NameKeeper put(final char[] name, final Object value) throws IllegalArgumentException, NullPointerException {
			if (name == null || name.length == 0) {
				throw new IllegalArgumentException("Key name can't be null or zero-length array");
			}
			else if (value == null) {
				throw new NullPointerException("Value can't be null");
			}
			else {
				return put(name,value.toString());
			}
		}
		
		@Override
		public void close() {
			if (content != null) {
				content.clear();
				content = null;
			}
			parent = null;
		}

		@Override
		public char[] getValue(final char[] data, final int from, final int to) {
			if (content == null) {
				if (parent != null) {
					return parent.getValue(data,from,to);
				}
				else {
					return null;
				}
			}
			else {
				final long	id = content.seekName(data,from,to);
				
				if (id >= 0) {
					return content.getCargo(id);
				}
				else if (parent != null) {
					return parent.getValue(data,from,to);
				}
				else {
					return null;
				}
			}
		}
	}
	
	private static class PartRecord implements Comparable<AssemblerTemplateRepo.PartRecord>{
		final char[]	name;
		final char[]	content;
		
		public PartRecord(char[] name, char[] content) {
			this.name = name;
			this.content = content;
		}

		@Override
		public String toString() {
			return "PartRecord [name=" + Arrays.toString(name) + ", content=" + Arrays.toString(content) + "]";
		}

		@Override
		public int compareTo(final AssemblerTemplateRepo.PartRecord o) {
			for (int index = 0, maxIndex = Math.min(this.name.length,o.name.length); index < maxIndex; index++) {
				int	result = this.name[index] - o.name[index];
				
				if (result != 0) {
					return result;
				}
			}
			return this.name.length - o.name.length;
		}
	}
}