package chav1961.purelib.sql.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.ArrayContent;
import chav1961.purelib.sql.RsMetaDataElement;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;
import chav1961.purelib.streams.JsonSaxParser;
import chav1961.purelib.streams.interfaces.JsonSaxHandler;

public class JsonContentParser implements ResultSetContentParser {
	private enum Terminal {
		StartDoc, EndDoc, StartObj, EndObj, StartArr, EndArr,
		Startname, EndName, StartIndex, EndIndex, Value
	}

	private enum NonTerminal {
		BeforeDoc, BeforeArray, InArray, BeforeName, AfterName, LocalArray, AfterArray, AfterDoc 
	}
	
	private enum Exit {
		PrepareRowOfData, StoreName, StoreRowOfData, StoreNameValuePair, CalculatAmountOfNames, StoreNameDescriptor, 
		PrepareArray, Store2Array, StoreNameArrayPair, JoinNameDescritors, ReformatValues 
	}
	
	@SuppressWarnings("unchecked")
	private static final FSM.FSMLine<Terminal, NonTerminal, Exit>[]	STATES = new FSM.FSMLine[]{
									new FSM.FSMLine<Terminal, NonTerminal, Exit>(NonTerminal.BeforeDoc,Terminal.StartDoc,NonTerminal.BeforeArray)
									}; 
	
	
	private final AbstractContent	content;
	private ResultSetMetaData		metadata = null;
	
	public JsonContentParser() {
		this.content = null;
	}
	
	protected JsonContentParser(final InputStream content, final String encoding) throws IOException, SyntaxException {
		final List<Object[]>		values = new ArrayList<>();
		final SaxHandler			handler = new SaxHandler(values);
		
		try(final LineByLineProcessor	lblp = new LineByLineProcessor(new JsonSaxParser(handler));
			final Reader 	rdr = new InputStreamReader(content,encoding)) {
				
			lblp.write(rdr);
		}
		final Object[][] result = values.toArray(new Object[values.size()][]);
		
		values.clear();
		this.content = new ArrayContent((Object[][])result);
	}
	
	@Override
	public boolean canServe(final URI request) {
		if (request == null) {
			throw new NullPointerException("Request to serve can't be null");
		}
		else if (request.getScheme() == null) {
			throw new IllegalArgumentException("Request ["+request+"] is not absolute (scheme is missing)");
		}
		else {
			return "json".equalsIgnoreCase(request.getScheme());
		}
	}

	@Override
	public ResultSetContentParser newInstance(final URL access, final URI request) throws IOException {
		if (access == null) {
			throw new NullPointerException("Access URL can't be null");
		}
		else if (request == null) {
			throw new NullPointerException("Request URI can't be null");
		}
		else {
			try(final InputStream	is = access.openStream()) {
				final Hashtable<String,String[]> 	queries = NanoServiceFactory.parseQuery(request.getQuery()); 
						
				return new JsonContentParser(is,queries.containsKey("encoding") ? queries.get("encoding")[0] : "UTF-8");
			} catch (SyntaxException e) {
				throw new IOException("Syntax error in content: "+e.getLocalizedMessage());
			}
		}
	}

	@Override
	public ResultSetMetaData getMetaData() throws IOException {
		if (metadata == null) {
			throw new IOException("No one lines in the input stream or the same forst line not contants colimns description"); 
		}
		else {
			return metadata;
		}
	}

	@Override
	public AbstractContent getAccessContent() {
		return content;
	}

	private static class SaxHandler implements JsonSaxHandler {
		private final Map<String,RsMetaDataElement>			meta = new HashMap<>();
		private final List<Object[]> 						content;
		private final List<Object> 							row = new ArrayList<>(), innerArray = new ArrayList<>(); 
		private final FSM<Terminal,NonTerminal,Exit,Object>	fsm = new FSM<>(
										(current,terminal,fromState,toState,actions,parameter)->{
											process(fromState,terminal,toState,parameter,actions);
										}, NonTerminal.BeforeDoc, STATES);
		private String	name;
		
		private SaxHandler(final List<Object[]> forContent) {
			this.content = forContent;
		}

		@Override
		public void startDoc() throws ContentException {
			try{fsm.processTerminal(Terminal.StartDoc,null);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void endDoc() throws ContentException {
			try{fsm.processTerminal(Terminal.EndDoc,null);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void startObj() throws ContentException {
			try{fsm.processTerminal(Terminal.StartObj,null);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void endObj() throws ContentException {
			try{fsm.processTerminal(Terminal.EndObj,null);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void startArr() throws ContentException {
			try{fsm.processTerminal(Terminal.StartArr,null);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void endArr() throws ContentException {
			try{fsm.processTerminal(Terminal.EndArr,null);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void startName(final char[] data, final int from, final int len) throws ContentException {
			try{fsm.processTerminal(Terminal.Startname,new String(data,from,len));
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void startName(final String name) throws ContentException {
			try{fsm.processTerminal(Terminal.Startname,name);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void startName(final long id) throws ContentException {
			try{fsm.processTerminal(Terminal.Startname,id);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void endName() throws ContentException {
			try{fsm.processTerminal(Terminal.EndName,null);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void startIndex(final int index) throws ContentException {
			try{fsm.processTerminal(Terminal.StartIndex,index);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void endIndex() throws ContentException {
			try{fsm.processTerminal(Terminal.EndIndex,null);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void value(final char[] data, final int from, final int len) throws ContentException {
			try{fsm.processTerminal(Terminal.Value,new String(data,from,len));
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void value(final String data) throws ContentException {
			try{fsm.processTerminal(Terminal.Value,data);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void value(final long data) throws ContentException {
			try{fsm.processTerminal(Terminal.Value,data);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void value(final double data) throws ContentException {
			try{fsm.processTerminal(Terminal.Value,data);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void value(final boolean data) throws ContentException {
			try{fsm.processTerminal(Terminal.Value,data);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void value() throws ContentException {
			try{fsm.processTerminal(Terminal.Value,null);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}
		
//		-before doc
//		-- startDoc --> before array
//		-before array
//		-- startArray --> in array
//		-in array
//		--endArray --> after array
//		-- startObj --> before name, {prepare row of data}
//		-before name
//		-- name --> after name, {store name}
//		-- endObj --> in array, {store row of data into common list}
//		-after name
//		-- value --> before name, {store name/value pair, calculate maximun amount of names, build and store name descriptor}
//		-- startArray --> local array, {prepare array for storing}
//		-local array
//		-- value --> local array, {store value}
//		-- endArray --> before name, {store name/value pair (from array), calculate maximun amount of names, build and store name descriptor}
//		-after array
//		-- endDoc --> after doc, {join name descriptors, reformat values with filling nulls}
//		All names are placed into syntax tree. Each of them is associated with bit mask for predefined types, available in json, and location in the target array.
		
		private void process(final NonTerminal fromState, final Terminal terminal, final NonTerminal toState, final Object parameter, final Exit... actions) {
			// TODO Auto-generated method stub
			for (Exit item : actions) {
				switch (item) {
					case CalculatAmountOfNames	:
						break;
					case JoinNameDescritors		:
						break;
					case PrepareArray			:
						innerArray.clear();
						break;
					case PrepareRowOfData		:
						row.clear();
						break;
					case ReformatValues			:
						break;
					case Store2Array			:
						innerArray.add(parameter);
						break;
					case StoreName				:
						name = parameter.toString();
						break;
					case StoreNameArrayPair		:
						break;
					case StoreNameDescriptor	:
						break;
					case StoreNameValuePair		:
						break;
					case StoreRowOfData			:
						content.add(row.toArray(new Object[row.size()]));
						break;
					default:
						break;				
				}
			}
		}
	}

}
