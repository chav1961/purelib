package chav1961.purelib.streams.char2byte;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;


public class AsmWriter extends OutputStreamWriter {
	private static final int			CONSTRUCTOR_TYPE_0 = 0; 
	private static final int			CONSTRUCTOR_TYPE_1 = 1; 
	private static final int			CONSTRUCTOR_TYPE_2 = 2; 

	private final LineByLineProcessor	lblp = new LineByLineProcessor((lineNo,data,from,len)->process(lineNo,data,from,len));  
	private final Map<String,Object>	settings;
	private final int					constructorType;
	private final StringBuilder			sb = new StringBuilder();
	
	private Charset						charset;
	private String						encoding;
	private								int lineNo = 1;

	public AsmWriter(final OutputStream arg0, final Charset arg1) {
		super(arg0, arg1);
		this.settings = new HashMap<String,Object>();
		this.constructorType = CONSTRUCTOR_TYPE_0;
		this.charset = arg1;
	}

	public AsmWriter(final OutputStream arg0, final CharsetEncoder arg1) {
		super(arg0, arg1);
		this.settings = new HashMap<String,Object>();
		this.constructorType = CONSTRUCTOR_TYPE_0;
		this.charset = arg1.charset();
	}

	public AsmWriter(final OutputStream arg0, final String arg1) throws UnsupportedEncodingException {
		super(arg0, arg1);
		this.settings = new HashMap<String,Object>();
		this.constructorType = CONSTRUCTOR_TYPE_1;
		this.encoding = arg1;
	}

	public AsmWriter(final OutputStream arg0) {
		super(arg0);
		this.settings = new HashMap<String,Object>();
		this.constructorType = CONSTRUCTOR_TYPE_2;
	}
	
	public AsmWriter(final OutputStream arg0, final Charset arg1, final Map<String,Object> settings) {
		super(arg0, arg1);
		if (settings == null) {
			throw new IllegalArgumentException("Settings can't be null!"); 
		}
		else {
			this.settings = settings;
			this.constructorType = CONSTRUCTOR_TYPE_0;
			this.charset = arg1;
		}
	}

	public AsmWriter(final OutputStream arg0, final CharsetEncoder arg1, final Map<String,Object> settings) {
		super(arg0, arg1);
		if (settings == null) {
			throw new IllegalArgumentException("Settings can't be null!"); 
		}
		else {
			this.settings = settings;
			this.constructorType = CONSTRUCTOR_TYPE_0;
			this.charset = arg1.charset();
		}
	}

	public AsmWriter(final OutputStream arg0, final String arg1, final Map<String,Object> settings) throws UnsupportedEncodingException {
		super(arg0, arg1);
		if (settings == null) {
			throw new IllegalArgumentException("Settings can't be null!"); 
		}
		else {
			this.settings = new HashMap<String,Object>();
			this.constructorType = CONSTRUCTOR_TYPE_1;
			this.encoding = arg1;
		}
	}

	public AsmWriter(final OutputStream arg0, final Map<String,Object> settings) {
		super(arg0);
		if (settings == null) {
			throw new IllegalArgumentException("Settings can't be null!"); 
		}
		else {
			this.settings = settings;
			this.constructorType = CONSTRUCTOR_TYPE_2;
		}
	}

	public AsmWriter(final OutputStream arg0, final Charset arg1, final Object... settings) {
		this(arg0, arg1, Utils.mkMap(settings));
	}

	public AsmWriter(final OutputStream arg0, final CharsetEncoder arg1, final Object... settings) {
		this(arg0, arg1, Utils.mkMap(settings));
	}

	public AsmWriter(final OutputStream arg0, final String arg1, final Object... settings) throws UnsupportedEncodingException {
		this(arg0, arg1, Utils.mkMap(settings));
	}

	public AsmWriter(final OutputStream arg0, final Object... settings) {
		this(arg0, Utils.mkMap(settings));
	}
	
	
	@Override
	public void write(final char[] cbuf, final int off, final int len) throws IOException {
		try{lblp.write(cbuf,off,len);
		} catch (SyntaxException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		write(str.toCharArray(),off,len);
	}	
	
	@Override
    public void write(int c) throws IOException {
        write(new char[]{(char) c},0,1);
    }	

	private void process(final int lineNo, final char[] data, final int from, final int length) throws IOException {
	}

	private void pushReader(final String source, final InputStream is) throws IOException, SyntaxException {
		switch (constructorType) {
			case CONSTRUCTOR_TYPE_0	:
				try(final Reader			rdr = new InputStreamReader(is,this.charset);) {
					processReader(rdr);
				}
				break;
			case CONSTRUCTOR_TYPE_1	: 
				try(final Reader			rdr = new InputStreamReader(is,this.encoding);) {
					processReader(rdr);
				}
				break;
			case CONSTRUCTOR_TYPE_2	: 
				try(final Reader			rdr = new InputStreamReader(is);) {
					processReader(rdr);
				}
				break;
		}
	}
	
	private void processReader(final Reader rdr) throws IOException, SyntaxException {
		try(final LineByLineProcessor	lblp = new LineByLineProcessor((lineNo,data,from,len)->process(lineNo,data,from,len))) {
			lblp.write(rdr);
		}
	}
}
