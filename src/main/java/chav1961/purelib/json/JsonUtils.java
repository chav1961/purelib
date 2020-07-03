package chav1961.purelib.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.json.interfaces.JsonTreeWalkerCallback;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

class JsonUtils {
	static JsonNode loadJsonTree(final JsonStaxParser parser) throws SyntaxException {
		if (parser == null) {
			throw new NullPointerException("Json parser can't be null");  
		}
		else {
			try {return internalLoadJsonTree(parser);			
			} catch (IOException exc) {
				throw new SyntaxException(parser.row(),parser.col(),"I/O error : "+exc.getLocalizedMessage(),exc); 
			}
		}
	}

	static void unloadJsonTree(final JsonNode root, final JsonStaxPrinter printer) throws PrintingException {
		if (root == null) {
			throw new NullPointerException("Root tree can't be null");
		}
		else if (printer == null) {
			throw new NullPointerException("Json printer can't be null");
		}
		else {
			try{internalUnloadJsonTree(root,printer);
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}
	}

	public static ContinueMode walkDownJson(final SyntaxNode<JsonNodeType,SyntaxNode<?,?>> root, final JsonTreeWalkerCallback callback) throws ContentException {
		return null;
	}
	
	private static JsonNode internalLoadJsonTree(final JsonStaxParser parser) throws SyntaxException, IOException {
		switch (parser.current()) {
			case START_ARRAY	:
				final List<JsonNode>	nestedArray = new ArrayList<>();
				final long 				arrRow = parser.row(), arrCol = parser.col(); 
				
				while (parser.hasNext()) {
					if (parser.next() == JsonStaxParserLexType.END_ARRAY) {
						break;
					}
					nestedArray.add(loadUnnamedJsonTree(parser));
					if (parser.current() != JsonStaxParserLexType.LIST_SPLITTER) {
						break;
					}
				}
				if (parser.current() == JsonStaxParserLexType.END_ARRAY) {
					if (parser.hasNext()) {
						parser.next();
					}
					return new JsonNode((int)arrRow,(int)arrCol,JsonNodeType.JsonArray,0,0,nestedArray.toArray(new JsonNode[nestedArray.size()]));
				}
				else {
					throw new SyntaxException(parser.row(),parser.col(),"Missing ']'"); 
				}
			case START_OBJECT	:
				final List<JsonNode>	nestedObj = new ArrayList<>();
				final long 				objRow = parser.row(), objCol = parser.col(); 
				
				while (parser.hasNext()) {
					if (parser.next() == JsonStaxParserLexType.END_OBJECT) {
						break;
					}
					nestedObj.add(loadNamedJsonTree(parser));
					if (parser.current() != JsonStaxParserLexType.LIST_SPLITTER) {
						break;
					}
				}
				if (parser.current() == JsonStaxParserLexType.END_OBJECT) {
					if (parser.hasNext()) {
						parser.next();
					}
					return new JsonNode((int)objRow,(int)objCol,JsonNodeType.JsonObject,0,0,nestedObj.toArray(new JsonNode[nestedObj.size()]));
				}
				else {
					throw new SyntaxException(parser.row(),parser.col(),"Missing '}'"); 
				}
			default:
				throw new SyntaxException(parser.row(),parser.col(),"Illegal content: only '{' or '[' are legal values here");
		}
	}

	private static JsonNode loadNamedJsonTree(final JsonStaxParser parser) throws SyntaxException, IllegalStateException, IOException {
		final JsonNode	result;
		final String 	name;

		if (parser.current() == JsonStaxParserLexType.NAME) {
			name = parser.name();
			if (parser.hasNext()) {
				if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER) {
					if (parser.hasNext()) {
						parser.next();
					}
				}
				else {
					throw new SyntaxException(parser.row(),parser.col(),"Missing ':'");
				}
			}
		}
		else {
			throw new SyntaxException(parser.row(),parser.col(),"Missing name");
		}
		
		switch (parser.current()) {
			case BOOLEAN_VALUE	:
				result = new JsonNode((int)parser.row(),(int)parser.col(),JsonNodeType.JsonBoolean,parser.booleanValue() ? 1 : 0,name);
				if (parser.hasNext()) {
					parser.next();
				}
				break;
			case INTEGER_VALUE	:
				result = new JsonNode((int)parser.row(),(int)parser.col(),JsonNodeType.JsonInteger,parser.intValue(),name);
				if (parser.hasNext()) {
					parser.next();
				}
				break;
			case NULL_VALUE		:
				result = new JsonNode((int)parser.row(),(int)parser.col(),JsonNodeType.JsonNull,0,name);
				if (parser.hasNext()) {
					parser.next();
				}
				break;
			case REAL_VALUE		:
				result = new JsonNode((int)parser.row(),(int)parser.col(),JsonNodeType.JsonReal,Double.doubleToLongBits(parser.realValue()),name);
				if (parser.hasNext()) {
					parser.next();
				}
				break;
			case START_OBJECT : case START_ARRAY	:
				result = internalLoadJsonTree(parser);
				break;
			case STRING_VALUE	:
				result = new JsonNode((int)parser.row(),(int)parser.col(),JsonNodeType.JsonString,0,name,
						new JsonNode((int)parser.row(),(int)parser.col(),JsonNodeType.JsonString,0,parser.stringValue())
				);
				if (parser.hasNext()) {
					parser.next();
				}
				break;
			default:
				throw new SyntaxException(parser.row(),parser.col(),"Illegal content: only constants, '{' or '[' are legal values here");
		}
		return result;
	}	

	private static JsonNode loadUnnamedJsonTree(final JsonStaxParser parser) throws SyntaxException, IOException {
		final JsonNode	result;
		
		switch (parser.current()) {
			case BOOLEAN_VALUE	:
				result = new JsonNode((int)parser.row(),(int)parser.col(),JsonNodeType.JsonBoolean,parser.booleanValue() ? 1 : 0,null);
				if (parser.hasNext()) {
					parser.next();
				}
				break;
			case INTEGER_VALUE	:
				result = new JsonNode((int)parser.row(),(int)parser.col(),JsonNodeType.JsonInteger,parser.intValue(),null);
				if (parser.hasNext()) {
					parser.next();
				}
				break;
			case NULL_VALUE		:
				result = new JsonNode((int)parser.row(),(int)parser.col(),JsonNodeType.JsonNull,0,null);
				if (parser.hasNext()) {
					parser.next();
				}
				break;
			case REAL_VALUE		:
				result = new JsonNode((int)parser.row(),(int)parser.col(),JsonNodeType.JsonReal,Double.doubleToLongBits(parser.realValue()),null);
				if (parser.hasNext()) {
					parser.next();
				}
				break;
			case START_OBJECT : case START_ARRAY	:
				result = internalLoadJsonTree(parser);
				break;
			case STRING_VALUE	:
				result = new JsonNode((int)parser.row(),(int)parser.col(),JsonNodeType.JsonString,0,null,
						new JsonNode((int)parser.row(),(int)parser.col(),JsonNodeType.JsonString,0,parser.stringValue())
				);
				if (parser.hasNext()) {
					parser.next();
				}
				break;
			default:
				throw new SyntaxException(parser.row(),parser.col(),"Illegal content: only constants, '{' or '[' are legal values here");
		}
		return result;
	}	
	
	private static void internalUnloadJsonTree(final JsonNode root, final JsonStaxPrinter printer) throws PrintingException, IOException {
		switch (root.getType()) {
			case JsonArray		:
				boolean	needSplitterArray = false;
				
				printer.startArray();
				for (SyntaxNode<?, ?> item : root.children) {
					if (needSplitterArray) {
						printer.splitter();
					}
					internalUnloadJsonTree((JsonNode) item,printer);
					needSplitterArray = true;
				}
				printer.endArray();
				break;
			case JsonBoolean	:
				if (root.cargo != null) {
					printer.name((String)root.cargo);
				}
				printer.value(root.value != 0);
				break;
			case JsonInteger	:
				if (root.cargo != null) {
					printer.name((String)root.cargo);
				}
				printer.value(root.value);
				break;
			case JsonNull		:
				if (root.cargo != null) {
					printer.name((String)root.cargo);
				}
				printer.nullValue();
				break;
			case JsonObject		:
				boolean	needSplitterObj = false;
				
				printer.startObject();
				for (SyntaxNode<?, ?> item : root.children) {
					if (needSplitterObj) {
						printer.splitter();
					}
					internalUnloadJsonTree((JsonNode) item,printer);
					needSplitterObj = true;
				}
				printer.endObject();
				break;
			case JsonReal		:
				if (root.cargo != null) {
					printer.name((String)root.cargo);
				}
				printer.value(Double.longBitsToDouble(root.value));
				break;
			case JsonString		:
				if (root.cargo != null) {
					printer.name((String)root.cargo);
				}
				printer.value(root.children[0].cargo.toString());
				break;
			default:
				throw new UnsupportedOperationException("Json node type ["+root.getType()+"] is not supported yet");
		}
	}
}
