package chav1961.purelib.sql.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import chav1961.purelib.basic.BitCharSet;
import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.AbstractResultSetMetaData;
import chav1961.purelib.sql.ArrayContent;
import chav1961.purelib.sql.RsMetaDataElement;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;
import chav1961.purelib.streams.JsonSaxParser;
import chav1961.purelib.streams.interfaces.JsonSaxHandler;

public class JsonContentParser implements ResultSetContentParser {
	private enum Terminal {
		StartDoc, EndDoc, StartObj, EndObj, StartArr, EndArr,
		StartName, EndName, StartIndex, EndIndex, Value
	}

	private enum NonTerminal {
		BeforeDoc, BeforeArray, InArray, BeforeName, AfterName, LocalArray, AfterArray, AfterDoc 
	}
	
	private enum Exit {
		PrepareRowOfData, StoreName, StoreRowOfData, StoreNameValuePair, CalculatAmountOfNames, 
		PrepareArray, Store2Array, StoreNameArrayPair, JoinNameDescritors, ReformatValues 
	}

	@SuppressWarnings("unchecked")
	private static final FSM.FSMLine<Terminal, NonTerminal, Exit>[]	STATES = new FSM.FSMLine[]{
									 new FSM.FSMLine<Terminal, NonTerminal, Exit>(NonTerminal.BeforeDoc,Terminal.StartDoc,NonTerminal.BeforeArray)
									,new FSM.FSMLine<Terminal, NonTerminal, Exit>(NonTerminal.BeforeArray,Terminal.StartArr,NonTerminal.InArray)
									,new FSM.FSMLine<Terminal, NonTerminal, Exit>(NonTerminal.InArray,Terminal.EndArr,NonTerminal.AfterArray)
									,new FSM.FSMLine<Terminal, NonTerminal, Exit>(NonTerminal.InArray,Terminal.StartObj,NonTerminal.BeforeName,Exit.PrepareRowOfData)
									,new FSM.FSMLine<Terminal, NonTerminal, Exit>(NonTerminal.BeforeName,Terminal.StartName,NonTerminal.AfterName,Exit.StoreName)
									,new FSM.FSMLine<Terminal, NonTerminal, Exit>(NonTerminal.BeforeName,Terminal.EndObj,NonTerminal.InArray,Exit.StoreRowOfData)
									,new FSM.FSMLine<Terminal, NonTerminal, Exit>(NonTerminal.AfterName,Terminal.Value,NonTerminal.BeforeName,Exit.StoreNameValuePair,Exit.CalculatAmountOfNames)
									,new FSM.FSMLine<Terminal, NonTerminal, Exit>(NonTerminal.AfterName,Terminal.StartArr,NonTerminal.LocalArray,Exit.PrepareArray)
									,new FSM.FSMLine<Terminal, NonTerminal, Exit>(NonTerminal.LocalArray,Terminal.Value,NonTerminal.LocalArray,Exit.Store2Array)
									,new FSM.FSMLine<Terminal, NonTerminal, Exit>(NonTerminal.LocalArray,Terminal.EndArr,NonTerminal.LocalArray,Exit.StoreNameArrayPair,Exit.CalculatAmountOfNames)
									,new FSM.FSMLine<Terminal, NonTerminal, Exit>(NonTerminal.AfterArray,Terminal.EndDoc,NonTerminal.AfterDoc,Exit.JoinNameDescritors,Exit.ReformatValues)
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
		private final List<Object[]> 						content;
		private final List<Object> 							row = new ArrayList<>(), innerArray = new ArrayList<>();
		private final Map<String,NameDescriptor>			names = new HashMap<>();
		private final FSM<Terminal,NonTerminal,Exit,Object>	fsm = new FSM<>(
										(current,terminal,fromState,toState,actions,parameter)->{
											process(fromState,terminal,toState,parameter,actions);
										}, NonTerminal.BeforeDoc, STATES);
		
		private ResultSetMetaData	metadata;
		private int		nameLocation = 0, totalNames = 0;
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
			try{fsm.processTerminal(Terminal.StartName,new String(data,from,len));
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void startName(final String name) throws ContentException {
			try{fsm.processTerminal(Terminal.StartName,name);
			} catch (FlowException e) {
				throw new ContentException(e);
			}
		}

		@Override
		public void startName(final long id) throws ContentException {
			try{fsm.processTerminal(Terminal.StartName,id);
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
		
		
		private void process(final NonTerminal fromState, final Terminal terminal, final NonTerminal toState, final Object parameter, final Exit... actions) {
			for (Exit item : actions) {
				switch (item) {
					case CalculatAmountOfNames	:
						totalNames = names.size();
						break;
					case JoinNameDescritors		:
						final List<RsMetaDataElement>	meta = new ArrayList<>();
						
						for (Entry<String, NameDescriptor> entity : names.entrySet()) {
							if (entity.getValue().dataTypes.contains(NameDescriptor.TYPE_STRING)) {
								meta.add(new RsMetaDataElement(entity.getKey(),"", "VARCHAR", Types.VARCHAR, entity.getValue().contentLength, 0));
							}
							else if (entity.getValue().dataTypes.contains(NameDescriptor.TYPE_BOOLEAN)) {
								meta.add(new RsMetaDataElement(entity.getKey(),"", "BOOLEAN", Types.BOOLEAN, 1, 0));
							}
							else if (entity.getValue().dataTypes.contains(NameDescriptor.TYPE_REAL)) {
								meta.add(new RsMetaDataElement(entity.getKey(),"", "NUMERIC", Types.NUMERIC, 18, 2));
							}
							else {
								meta.add(new RsMetaDataElement(entity.getKey(),"", "BIGINT", Types.BIGINT, 18, 0));
							}
						}
						metadata = new AbstractResultSetMetaData(meta.toArray(new RsMetaDataElement[meta.size()]),true) {
										@Override public String getTableName(int column) throws SQLException {return "table";}
										@Override public String getSchemaName(int column) throws SQLException {return "schema";}
										@Override public String getCatalogName(int column) throws SQLException {return "catalog";}
									};
						meta.clear();
						break;
					case PrepareArray			:
						innerArray.clear();
						break;
					case PrepareRowOfData		:
						for (int index = 0; index < totalNames; index++) {
							row.add(null);
						}
						break;
					case ReformatValues			:
						for (int index = 0, maxIndex = content.size(); index < maxIndex; index++) {
							if (content.get(index).length < totalNames) {
								content.set(index,Arrays.copyOf(content.get(index),totalNames));
							}
						}
						break;
					case Store2Array			:
						innerArray.add(parameter);
						break;
					case StoreName				:
						name = parameter.toString();
						break;
					case StoreNameArrayPair		:
						if (names.containsKey(name)) {
							row.set(names.get(name).location, innerArray.toArray());
						}
						else {
							names.put(name,new NameDescriptor(nameLocation++));
							row.add(innerArray.toArray());
						}
						names.get(name).markDataType(parameter);
						break;
					case StoreNameValuePair		:
						if (names.containsKey(name)) {
							row.set(names.get(name).location, parameter);
						}
						else {
							names.put(name,new NameDescriptor(nameLocation++));
							row.add(parameter);
						}
						names.get(name).markDataType(parameter);
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

	private static class NameDescriptor {
		private static final char	TYPE_INT = 1;
		private static final char	TYPE_REAL = 2;
		private static final char	TYPE_STRING = 3;
		private static final char	TYPE_BOOLEAN = 4;
		private static final char	TYPE_ARRAY = 5;
		
		final int			location;
		final BitCharSet	dataTypes = new BitCharSet();
		int					contentLength = 0;
		
		NameDescriptor(int location) {
			this.location = location;
		}
		
		void markDataType(final Object value) {
			if (value instanceof Long) {
				dataTypes.add(TYPE_INT);
			}
			else if (value instanceof Double) {
				dataTypes.add(TYPE_REAL);
			}
			else if (value instanceof String) {
				dataTypes.add(TYPE_STRING);
				contentLength = Math.max(contentLength, value.toString().length());
			}
			else if (value instanceof Boolean) {
				dataTypes.add(TYPE_BOOLEAN);
			}
			else if (value != null && value.getClass().isArray()) {
				dataTypes.add(TYPE_ARRAY);
			}
		}
	}
}
