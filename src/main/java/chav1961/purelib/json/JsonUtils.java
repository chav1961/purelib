package chav1961.purelib.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.json.interfaces.JsonTreeWalkerCallback;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

/**
 * <p>This class contains a set of static methods to work with {@linkplain JsonNode} tree. All the methods can be grouped as:</p>
 * <ul>
 * <li>{@linkplain #loadJsonTree(JsonStaxParser)} and {@linkplain #unloadJsonTree(JsonNode, JsonStaxPrinter)} methods to load/unload JSON content into or from {@linkplain JsonNode} tree</li>
 * <li>{@linkplain #walkDownJson(JsonNode, JsonTreeWalkerCallback)} method to walk JSON tree from it's root or any subtree</li>
 * </ul>
 * <p>This class also contains a builder to create <i>XPath-styled</i> filter for use in conjunction with {@linkplain #walkDownJson(JsonNode, JsonTreeWalkerCallback)} method to simplify JSON tree walking.
 * Syntax of the filter see {@linkplain #filterOf(String, JsonTreeWalkerCallback)} method description</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @lastUpdate 0.0.5
 */
public class JsonUtils {
	public static final String		JSON_TYPE_BOOLEAN = "bool";
	public static final String		JSON_TYPE_INTEGER = "int";
	public static final String		JSON_TYPE_REAL = "real";
	public static final String		JSON_TYPE_STR = "str";
	public static final String		JSON_TYPE_OBJ = "{}";
	public static final String		JSON_TYPE_ARR = "[]";
	
	private static final JsonNode[]	EMPTY_LIST = new JsonNode[0];
	private static final Object		NULL_MARKER = new Object();
	
	/**
	 * <p>Build JSON node tree from JSON parser</p>
	 * @param parser parser to build node. Parser must be pointed to any JSON lexema into it. Call parser.next() method before pass it to the
	 * method, if you doesn't call it earlier
	 * @return root of the JSON node tree. Can't be null
	 * @throws NullPointerException parser is null
	 * @throws SyntaxException any syntax errors in the input stream
	 */
	public static JsonNode loadJsonTree(final JsonStaxParser parser) throws SyntaxException, NullPointerException {
		if (parser == null) {
			throw new NullPointerException("Json parser can't be null");  
		}
		else {
			try {
				final JsonNode	node;
				
				if (parser.current() == JsonStaxParserLexType.START_ARRAY) {
					internalLoadJsonTree(parser,node = new JsonNode(JsonNodeType.JsonArray));			
				}
				else if (parser.current() == JsonStaxParserLexType.START_OBJECT) {
					internalLoadJsonTree(parser,node = new JsonNode(JsonNodeType.JsonObject));			
				}
				else {
					throw new SyntaxException(parser.row(),parser.col(),"Neither '[' nor '{' in the input JSON"); 
				}
				return node;
			} catch (IOException exc) {
				throw new SyntaxException(parser.row(),parser.col(),"I/O error : "+exc.getLocalizedMessage(),exc); 
			}
		}
	}
	
	/**
	 * <p>Load JSON tree from {@linkplain URI}</p>
	 * @param source URI to load tree from
	 * @return tree loaded
	 * @throws SyntaxException any syntax errors in the input
	 * @throws NullPointerException source is null
	 * @throws MalformedURLException illegal URI format
	 * @throws IOException any I/O errors
	 * @since 0.0.5
	 */
	public static JsonNode loadJsonTree(final URI source) throws SyntaxException, NullPointerException, MalformedURLException, IOException {
		if (source == null) {
			throw new NullPointerException("Source URI can't be null");  
		}
		else {
			final String	query = URIUtils.extractQueryFromURI(source);
			final URI		input = URIUtils.removeQueryFromURI(source);
			final Hashtable<String, String[]>	parms = query != null ? URIUtils.parseQuery(query) : null;
			
			try(final InputStream		is = input.toURL().openStream();
				final Reader			rdr = new InputStreamReader(is, parms != null && parms.containsKey("encoding") ? parms.get("encoding")[0] : "UTF-8");
				final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
					
				parser.next();
				return loadJsonTree(parser);
			}
		}
	}
	

	/**
	 * <p>Unload JSON tree into JSON printer.</p>
	 * @param root root of JSON tree
	 * @param printer printer to print JSON tree content
	 * @throws NullPointerException root or printer is null
	 * @throws PrintingException any exceptions on printing
	 */
	public static void unloadJsonTree(final JsonNode root, final JsonStaxPrinter printer) throws PrintingException, NullPointerException {
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

	/**
	 * <p>Walk down the JSON tree</p>
	 * @param root root of JSON tree
	 * @param callback callback to process nodes
	 * @return last {@linkplain ContinueMode} code from the walker
	 * @throws NullPointerException root or printer is null
	 * @throws ContentException on any processing errors in the callback
	 */
	public static ContinueMode walkDownJson(final JsonNode root, final JsonTreeWalkerCallback callback) throws ContentException, NullPointerException {
		if (root == null) {
			throw new NullPointerException("Root node can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else {
			final List<Object>	path = new ArrayList<>();
			
			return Utils.<JsonNode>walkDownEverywhere(root, (ref,node)->{
				switch (ref) {
					case CHILDREN	:
						if (node.getType() == JsonNodeType.JsonObject || node.getType() == JsonNodeType.JsonArray) {
							return node.children();
						}
						else {
							return EMPTY_LIST;
						}
					case PARENT		:
						return EMPTY_LIST;
					case SIBLINGS	:
						return EMPTY_LIST;
					default:
						throw new UnsupportedOperationException("Reference type ["+ref+"] is not supported yet");
				}
			}
			, (mode,node)->{
				switch (mode) {
					case ENTER	:
						if (node.hasName()) {
							if (node.getType() == JsonNodeType.JsonArray) {
								path.add(new ArrayRoot(node.getName()));
							}
							else if (node.getType() == JsonNodeType.JsonObject) {
								path.add(new ObjectRoot(node.getName()));
							}
							else {
								path.add(node.getName());
							}
						}
						else {
							if (node.getType() == JsonNodeType.JsonArray) {
								path.add(new ArrayRoot());
							}
							else if (!path.isEmpty() && (path.get(path.size()-1) instanceof ArrayRoot)) {
								path.add(((ArrayRoot)path.get(path.size()-1)).incrementAndGet());	// generate next array index!
							}
							else {
								path.add(node);
							}
						}
						return callback.process(mode, node, path.toArray(new Object[path.size()])); 
					case EXIT	:
						final Object[]	currentPath = path.toArray(new Object[path.size()]);
						
						path.remove(path.size()-1);
						return callback.process(mode, node, currentPath); 
					default:
						throw new UnsupportedOperationException("Enter mode ["+mode+"] is not supported yet");
				}
			});
		}
	}
	
	/**
	 * <p>Check existence of mandatory JSON fields inside JSON record and build list of missing fields, if needed</p> 
	 * @param node node to test. Can't be null
	 * @param fields fields to test mandatories. Can't be empty list, can't contain nulls or empties
	 * @return true if all the fields present, false otherwise
	 * @throws NullPointerException on any null arguments
	 * @throws IllegalArgumentException on any errors in field list
	 * @since 0.0.5
	 */
	public static boolean checkJsonMandatories(final JsonNode node, final String... fields) throws NullPointerException, IllegalArgumentException {
		return checkJsonMandatories(node, new StringBuilder(), fields);
	}

	/**
	 * <p>Check existence of mandatory JSON fields inside JSON record and build list of missing fields, if needed</p> 
	 * @param node node to test. Can't be null
	 * @param forMissingFields instance to keep missing field names. Fields, if any, will be splitted by ' '. 
	 * @param fields fields to test mandatories. Can't be empty list, can't contain nulls or empties
	 * @return true if all the fields present, false otherwise
	 * @throws NullPointerException on any null arguments
	 * @throws IllegalArgumentException on any errors in field list
	 * @since 0.0.5
	 */
	public static boolean checkJsonMandatories(final JsonNode node, final StringBuilder forMissingFields, final String... fields) throws NullPointerException, IllegalArgumentException {
		if (node == null) {
			throw new NullPointerException("Json node to check can't be null");
		}
		else if (forMissingFields == null) {
			throw new NullPointerException("String builder for missing fields can't be null");
		}
		else if (Utils.checkArrayContent4Nulls(fields, true) >= 0) {
			throw new IllegalArgumentException("Fields list is null, empty or contains nulls/empties inside");
		}
		else {
			final int	length = forMissingFields.length();
			
			for (String item : fields) {
				if (!node.hasName(item)) {
					forMissingFields.append(' ').append(item);
				}
			}
			
			if (forMissingFields.length() > length) {
				forMissingFields.delete(length, length);
				return false;
			}
			else {
				return true;
			}
		}
	}
	
	/**
	 * <p>Check JSON field types inside JSON record and build list of illegal fields, if needed</p> 
	 * @param node node to test. Can't be null
	 * @param fieldsAndTypes fields to test field types. Must be 'name/type[,type...] [not null]'.  Can't be empty list, can't contain nulls or empties
	 * @return true if all the fields present, false otherwise
	 * @throws NullPointerException on any null arguments
	 * @throws IllegalArgumentException on any errors in field list
	 * @since 0.0.5
	 */
	public static boolean checkJsonFieldTypes(final JsonNode node, final String... fieldsAndTypes) throws NullPointerException, IllegalArgumentException {
		return checkJsonFieldTypes(node, new StringBuilder(), fieldsAndTypes);
	}	

	/**
	 * <p>Check JSON field types inside JSON record and build list of illegal fields, if needed. Missing fields will not be checked and marked as erroneous</p> 
	 * @param node node to test. Can't be null
	 * @param forIllegalTypes instance to keep field names with illegal type. Fields, if any, will be splitted by ' '.
	 * @param fieldsAndTypes fields to test field types. Must be 'name/type[,type...] [not null]'.  Can't be empty list, can't contain nulls or empties
	 * @return true if all the fields present, false otherwise
	 * @throws NullPointerException on any null arguments
	 * @throws IllegalArgumentException on any errors in field list
	 * @since 0.0.5
	 */
	public static boolean checkJsonFieldTypes(final JsonNode node, final StringBuilder forIllegalTypes, final String... fieldsAndTypes) throws NullPointerException, IllegalArgumentException {
		if (node == null) {
			throw new NullPointerException("Json node to check can't be null");
		}
		else if (forIllegalTypes == null) {
			throw new NullPointerException("String builder for missing fields can't be null");
		}
		else if (Utils.checkArrayContent4Nulls(fieldsAndTypes, true) >= 0) {
			throw new IllegalArgumentException("Fields and types list is null, empty or contains nulls/empties inside");
		}
		else {
			final int	length = forIllegalTypes.length();
			
			for (String item : fieldsAndTypes) {
				if (item.indexOf('/') == -1) {
					throw new IllegalArgumentException("Illegal field and type format for ["+item+"] : missing '/'");
				}
				else {
					final String[]	fieldAndType = item.split("/");
					
					if (node.hasName(fieldAndType[0])) {
						final JsonNode	child = node.getChild(fieldAndType[0]); 
						final String[]	types;
						final boolean	checkNulls;
						
						if (fieldAndType[1].contains("not") && fieldAndType[1].contains("null")) {
							types = fieldAndType[1].replace("not","").replace("null","").trim().split(",");
							checkNulls = true;
						}
						else {
							types = fieldAndType[1].trim().split(",");
							checkNulls = false;
						}
						
						if (checkNulls && child.getType() == JsonNodeType.JsonNull) {
							forIllegalTypes.append(' ').append(item);
						}
						else {
							boolean	allOk = false;
							
							for (String checkType : types) {
								switch (checkType) {
									case JSON_TYPE_BOOLEAN 	:
										if (child.getType() == JsonNodeType.JsonBoolean) {
											allOk = true;
										}
										break;
									case JSON_TYPE_INTEGER 	:
										if (child.getType() == JsonNodeType.JsonInteger) {
											allOk = true;
										}
										break;
									case JSON_TYPE_REAL 	:
										if (child.getType() == JsonNodeType.JsonReal) {
											allOk = true;
										}
										break;
									case JSON_TYPE_STR		:
										if (child.getType() == JsonNodeType.JsonString) {
											allOk = true;
										}
										break;
									case JSON_TYPE_OBJ		:
										if (child.getType() == JsonNodeType.JsonObject) {
											allOk = true;
										}
										break;
									case JSON_TYPE_ARR		:
										if (child.getType() == JsonNodeType.JsonArray) {
											allOk = true;
										}
										break;
									default :
										throw new IllegalArgumentException("Illegal field and type format for ["+item+"] : unknown value type ["+checkType+"] to check");
								}
							}
						}
					}
				}
			}
			
			if (forIllegalTypes.length() > length) {
				forIllegalTypes.delete(length, length);
				return false;
			}
			else {
				return true;
			}
		}
	}	
	
	public static class ArrayRoot {
		private final String	name;
		private int				value;
		
		ArrayRoot() {
			this(-1);
		}

		ArrayRoot(final String name) {
			this(-1,name);
		}
		
		ArrayRoot(final int value) {
			this(value,"");
		}

		ArrayRoot(final int value, final String name) {
			this.value = value;
			this.name = name;
		}
		
		
		private int incrementAndGet() {
			return ++value;
		}

		@Override
		public String toString() {
			return "ArrayRoot" + (name.isEmpty() ? "" : "("+name+")");
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ArrayRoot other = (ArrayRoot) obj;
			if (name == null) {
				if (other.name != null) return false;
			} else if (!name.equals(other.name)) return false;
			return true;
		}
	}

	public static class ObjectRoot {
		private final String	name;
		
		ObjectRoot(){
			this("");
		}

		ObjectRoot(final String name){
			this.name = name;
		}
		
		@Override
		public String toString() {
			return "ObjectRoot" + (name.isEmpty() ? "" : "("+name+")");
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ObjectRoot other = (ObjectRoot) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}
	
	/**
	 * <p>Build callback filter for JSON tree. It's functionality is similar to XPath in the XML DOM tree</p>
	 * <p>Syntax of the 'XPath' expression in BNF is typed below. Character sequences inside apostrophes are treated <b>as-is</b> (for example, ':' is treated as colon typed):</p>
	 * <ul>
	 * <li><b>'/'&lt;nodeSelector&gt;</b> - reference to the root node</li>
	 * <li><b>'./'&lt;nodeSelector&gt;</b> - reference to the current node</li>
	 * <li><b>'../'&lt;nodeSelector&gt;</b> - reference to the parent node</li>
	 * </ul>
	 * <p><b>&lt;nodeSelector&gt;</b> consists of <b>&lt;nodeTemplate&gt;[':'&lt;valueTemplate&gt;]['#'&lt;filter&gt;]</b></p>
	 * <p>Current and parent node references in the 'XPath' can be used multiple time, but only in the beginning of the 'XPath'. Node template can be:</p>
	 * <ul>
	 * <li><b>/</b> - any node </li>
	 * <li><b>/*</b> - any object node</li>
	 * <li><b>/[]</b> - any array node</li>
	 * <li><b>/**</b> - any node chain</li>
	 * <li><b>/&lt;name></b> - any object node with the given name. Name can contain wildcards '*' and '?'</li>
	 * <li><b>/[&lt;indexExpr&gt;]</b> - any array node with the given index selected. Index can contains index expression (see below)</li>
	 * </ul>
	 * <p>Value template can be:</p>
	 * <ul>
	 * <li><b>&lt;valueTemplate&gt;</b>::=&lt;rangeItem&gt;','... - list of ranges</li>
	 * <li><b>&lt;rangeItem&gt;</b>::=&lt;value&gt;['..']&lt;value&gt; - range of values (similar to BETWEEN / AND in the SQL language)</li>
	 * <li><b>&lt;value&gt;</b>::={&lt;int&gt;|&lt;real&gt;|&lt;boolean&gt;|&lt;null&gt;|&lt;string&gt;} - value in the JSON-styled notation</li>
	 * </ul>
	 * <p>Syntax of the index expression is:</p>
	 * <ul>
	 * <li><b>&lt;indexExpr&gt;</b>::={&lt;list&gt;|'has' &lt;condition&gt;}</li>
	 * <li><b>&lt;list&gt;</b>::=&lt;range&gt;[','&lt;range&gt;...]</li>
	 * <li><b>&lt;range&gt;</b>::=&lt;expr&gt;['..'&lt;expr&gt;]</li>
	 * <li><b>&lt;condition&gt;</b>::=&lt;andCondition&gt;['||'&lt;andCondition&gt;...]</li>
	 * <li><b>&lt;andCondition&gt;</b>::=&lt;notCondition&gt;['&&'&lt;notCondition&gt;...]</li>
	 * <li><b>&lt;notCondition&gt;</b>::=[~]&lt;comparison&gt;</li>
	 * <li><b>&lt;comparison></b>::={&lt;expr&gt;{'&gt;'|'&gt;='|'&lt;'|'&lt;='|'=='|'&lt;&gt;'}&lt;expr&gt;|&lt;expr&gt;'in'&lt;list&gt;|&lt;expr&gt;'is'{'int'|'real'|'str'|'bool'|'null'|'arr'|'obj'}}</li>
	 * <li><b>&lt;expr&gt;</b>::=&lt;term&gt;{'+'|'-'|'*'|'/'|'%'}&lt;term&gt;...</li>
	 * <li><b>&lt;term&gt;</b>::={'i'|&lt;value&gt;|'('&lt;expr&gt;')'}</li>
	 * </ul>
	 * <p>Variable <b>'i'</b> always is treated as index of the current element and can be used in the condition index expressions only.
	 * Priority of the logical and arithmetical operators in the expression is traditional.</p>
	 * <p>Syntax of the 'filter' expression is:</p>
	 * <ul>
	 * <li>&lt;filter&gt;::=&lt;expr&gt;{'&&'|'||'}&lt;expr&gt;...</li>
	 * <li>&lt;expr&gt;::=[~]&lt;term&gt;</li>
	 * <li>&lt;term&gt;::={&lt;xpath&gt; [&lt;comparison&gt;] |'('&lt;filter&gt;')'}</li>
	 * </ul>
	 * <p>XPath in the filter without any comparison operators treated as 'check existance', otherwise compared with the given right part of comparison operator. Right part of '=' and '<>' comparisons for the
	 * 'XPath' can contain JSON in the form of '&lt;JSON_content&gt', for example "../ = '{"name":"var1","value":[10,20]}'".</p>
	 * <p>Examples of the syntax are:</p>
	 * <ul>
	 * <li><b>/ ** / [0..2] </b> - all node arrays with indices 0, 1, and 2</li>
	 * <li><b>/ ** / [0..2] / **</b> - all children of the node arrays with indices 0, 1, and 2</li>
	 * <li><b>/ ** / [has(i%2 == 0)] </b> - all node arrays with even indices</li>
	 * <li><b>/ ** / name #../key:10 && ../type:"type1" </b> - all node with 'name' name in the structure with 'key' field = 10 and 'type' field = "type1"</li>
	 * <li><b>/ ** / name #../ = '{"name":"test","type":"20"}'</b> - all node with 'name', where parent of node is exactly equals for JSON '{"name":"test","type":"20"}'</li>
	 * </ul>
	 * <p>Filter built is not reentrant and doesn't be used recursively, but it is reusable and doesn't need re-creation for subsequential calls</p>
	 * @param expression expression to filter content
	 * @param nested nested callback will be called on all the nodes successfully filtered by the filter expressions
	 * @return filter callback. Can't be null. Pass it to {@linkplain #walkDownJson(JsonNode, JsonTreeWalkerCallback)} method directly
	 * @throws IllegalArgumentException expression is null or empty
	 * @throws NullPointerException nested callback is null
	 * @throws SyntaxException any syntax errors in the expression
	 * @see JsonUtils#walkDownJson(JsonNode, JsonTreeWalkerCallback)
	 */
	public static JsonTreeWalkerCallback filterOf(final String expression, final JsonTreeWalkerCallback nested) throws IllegalArgumentException, NullPointerException, SyntaxException {
		if (expression == null || expression.isEmpty()) {
			throw new IllegalArgumentException("String expression can't be null or empty");
		}
		else if (nested == null) {
			throw new NullPointerException("Nested callback can't be null");
		}
		else {
			final List<Lexema>	lex = new ArrayList<>();
			final SyntaxTree	root = new SyntaxTree(0,Command.TEMPLATE,0,null);
			
			buildLexemaList(CharUtils.terminateAndConvert2CharArray(expression,'\0'),lex);
			
			final Lexema[]		lexArray = lex.toArray(new Lexema[lex.size()]);
			final int			lastLex = buildJsonPath(lexArray,0,root);

			if (lexArray[lastLex].type != LexemaType.EOF) {
				throw new SyntaxException(0,lexArray[lastLex].pos,"Unparsed tail in the expression"); 
			}
			else {
				return new XPathStyledFilter(root,nested);
			}
		}
	}
	
	private static JsonNode internalLoadJsonTree(final JsonStaxParser parser, final JsonNode root) throws SyntaxException, IOException {
		switch (parser.current()) {
			case START_ARRAY	:
arrLoop:		while (parser.hasNext()) {
					switch (parser.next()) {
						case BOOLEAN_VALUE	:
							root.addChild(new JsonNode(parser.booleanValue()));
							if (parser.hasNext()) {
								parser.next();
							}
							break;
						case END_ARRAY		:
							break arrLoop;
						case INTEGER_VALUE	:
							root.addChild(new JsonNode(parser.intValue()));
							if (parser.hasNext()) {
								parser.next();
							}
							break;
						case NULL_VALUE		:
							root.addChild(new JsonNode());
							if (parser.hasNext()) {
								parser.next();
							}
							break;
						case REAL_VALUE		:
							root.addChild(new JsonNode(parser.realValue()));
							if (parser.hasNext()) {
								parser.next();
							}
							break;
						case START_ARRAY:
							final JsonNode	arr = new JsonNode(JsonNodeType.JsonArray);
							
							root.addChild(arr);
							internalLoadJsonTree(parser,arr);
							break;
						case START_OBJECT:
							final JsonNode	obj = new JsonNode(JsonNodeType.JsonObject);
							
							root.addChild(obj);
							internalLoadJsonTree(parser,obj);
							break;
						case STRING_VALUE	:
							root.addChild(new JsonNode(parser.stringValue()));
							if (parser.hasNext()) {
								parser.next();
							}
							break;
						default:
							break;
					}
					if (parser.current() != JsonStaxParserLexType.LIST_SPLITTER) {
						break;
					}
				}
				if (parser.current() == JsonStaxParserLexType.END_ARRAY) {
					if (parser.hasNext()) {
						parser.next();
					}
					return root;
				}
				else {
					throw new SyntaxException(parser.row(),parser.col(),"Missing ']'"); 
				}
			case START_OBJECT	:
objLoop:		while (parser.hasNext()) {
					String	name;
	
					switch (parser.next()) {
						case END_OBJECT	:
							break objLoop;
						case NAME		:
							name = parser.name();
							if (parser.hasNext()) {
								if (parser.next() != JsonStaxParserLexType.NAME_SPLITTER) {
									throw new SyntaxException(parser.row(),parser.col(),"Missing name splitter ':'"); 
								}
							}
							break;
						default:
							throw new SyntaxException(parser.row(),parser.col(),"Missing name"); 
					}
					if (parser.hasNext()) {
						switch (parser.next()) {
							case BOOLEAN_VALUE:
								root.addChild(new JsonNode(parser.booleanValue()).setName(name));
								if (parser.hasNext()) {
									parser.next();
								}
								break;
							case INTEGER_VALUE:
								root.addChild(new JsonNode(parser.intValue()).setName(name));
								if (parser.hasNext()) {
									parser.next();
								}
								break;
							case NULL_VALUE:
								root.addChild(new JsonNode().setName(name));
								if (parser.hasNext()) {
									parser.next();
								}
								break;
							case REAL_VALUE:
								root.addChild(new JsonNode(parser.realValue()).setName(name));
								if (parser.hasNext()) {
									parser.next();
								}
								break;
							case START_ARRAY:
								final JsonNode	arr = new JsonNode(JsonNodeType.JsonArray).setName(name);
								
								root.addChild(arr);
								internalLoadJsonTree(parser,arr);
								break;
							case START_OBJECT:
								final JsonNode	obj = new JsonNode(JsonNodeType.JsonObject).setName(name);
								
								root.addChild(obj);
								internalLoadJsonTree(parser,obj);
								break;
							case STRING_VALUE:
								root.addChild(new JsonNode(parser.stringValue()).setName(name));
								if (parser.hasNext()) {
									parser.next();
								}
								break;
							default:
								throw new SyntaxException(parser.row(),parser.col(),"Missing value"); 
						}
						if (parser.current() != JsonStaxParserLexType.LIST_SPLITTER) {
							break;
						}
					}
				}
				if (parser.current() == JsonStaxParserLexType.END_OBJECT) {
					if (parser.hasNext()) {
						parser.next();
					}
					return root;
				}
				else {
					throw new SyntaxException(parser.row(),parser.col(),"Missing '}'"); 
				}
			default:
				throw new SyntaxException(parser.row(),parser.col(),"Illegal content: only '{' or '[' are legal values here");
		}
	}

	private static void internalUnloadJsonTree(final JsonNode root, final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (root.hasName()) {
			printer.name(root.getName());
		}
		switch (root.getType()) {
			case JsonArray		:
				boolean	needSplitterArray = false;
				
				printer.startArray();
				for (JsonNode item : root.children()) {
					if (needSplitterArray) {
						printer.splitter();
					}
					internalUnloadJsonTree((JsonNode) item,printer);
					needSplitterArray = true;
				}
				printer.endArray();
				break;
			case JsonBoolean	:
				printer.value(root.getBooleanValue());
				break;
			case JsonInteger	:
				printer.value(root.getLongValue());
				break;
			case JsonNull		:
				printer.nullValue();
				break;
			case JsonObject		:
				boolean	needSplitterObj = false;
				
				printer.startObject();
				for (JsonNode item : root.children()) {
					if (needSplitterObj) {
						printer.splitter();
					}
					internalUnloadJsonTree((JsonNode) item,printer);
					needSplitterObj = true;
				}
				printer.endObject();
				break;
			case JsonReal		:
				printer.value(root.getDoubleValue());
				break;
			case JsonString		:
				printer.value(root.getStringValue());
				break;
			default:
				throw new UnsupportedOperationException("Json node type ["+root.getType()+"] is not supported yet");
		}
	}
	
	private enum Ordering {
		OR, AND, NOT, CMP, LIST, RANGE, ADD, MUL, NEG, TERM
	}

	enum LexemaType {
		CURRENTSLASH, PARENTSLASH, 
		DOUBLEASTERISK, QUESTIONMARK,
		NAME, CURRENT_VALUE,
		INTEGER, DOUBLE, STRING, JSON,
		COLON, NUMBER,
		OPEN, CLOSE,
		OPENB, CLOSEB,
		LIST, RANGE, DOT,
		OR_OP, AND_OP, NOT_OP,
		CMP_LT_OP, CMP_LE_OP, CMP_GT_OP, CMP_GE_OP, CMP_EQ_OP, CMP_NE_OP, 
		ADD_OP, SUB_OP, MUL_OP, DIV_OP, REM_OP,
		EOF
	}

	enum TemplateType {
		ROOT, PARENT, CURRENT, 
		ANY_NODE, ANY_ARRAY, ANY_STRUCTURE, ANY_CHAIN,
		ANY_INDEX, ANY_NAME,
		SELECTED_INDEX, SELECTED_NAME,
		VALUE_FILTER, COND_FILTER;
	}
	
	enum Command {
		INT_CONST,
		REAL_CONST,
		BOOL_CONST,
		NULL_CONST,
		STRING_CONST,
		CURRENT_INDEX,
		CURRENT_VALUE,
		NAMED_NODE,
//		SUBTREE,
		SUBTREE_JSON,
		NEGATE,
		MULTIPLY,
		ADD,
		RANGE,
		LIST,
		CMP_EQ, CMP_NE, CMP_LT, CMP_LE, CMP_GT, CMP_GE, CMP_IN, CMP_IS,
		NOT,
		AND,
		OR,
		TEMPLATE,
		TEMPLATE_ITEM,
		PATTERN;
	}

	enum ComparisonType {
		FALSE, POSSIBLY_TRUE, TRUE
	}
	
	private static class SyntaxTree extends SyntaxNode<Command,SyntaxTree> {
		public SyntaxTree(final int col, final Command type, final long value, final Object cargo, final SyntaxTree... children) {
			super(0,col,type,value,cargo,children);
		}
		
		public SyntaxTree(final SyntaxTree another) {
			super(another);
		}

		@Override
		public String toString() {
			return "SyntaxTree [type=" + type + ", value=" + value + ", cargo=" + cargo + ", children=" + Arrays.toString(children) + "]";
		}
	}
	
	static class Lexema {
		final int			pos;
		final LexemaType	type;
		final long			longVal;
		final String		stringVal;
		final boolean		splitted;
		
		public Lexema(final int pos, final boolean splitted, final LexemaType type) {
			this.pos = pos;
			this.type = type;
			this.longVal = 0;
			this.stringVal = null;
			this.splitted = splitted;
		}

		public Lexema(final int pos, final boolean splitted, final LexemaType type, final long value) {
			this.pos = pos;
			this.type = type;
			this.longVal = value;
			this.stringVal = null;
			this.splitted = splitted;
		}

		public Lexema(final int pos, final boolean splitted, final LexemaType type, final String value) {
			this.pos = pos;
			this.type = type;
			this.longVal = 0;
			this.stringVal = value;
			this.splitted = splitted;
		}

		@Override
		public String toString() {
			return "Lexema [pos=" + pos + ", type=" + type + ", longVal=" + longVal + ", stringVal=" + stringVal + ", splitted=" + splitted + "]";
		}
	}

	private static class XPathStyledFilter implements JsonTreeWalkerCallback {
		private final List<JsonNode>			stack = new ArrayList<>();
		private final List<Boolean>				needExit = new ArrayList<>();
		private final SyntaxTree				root;
		private final JsonTreeWalkerCallback	nested;

		private XPathStyledFilter(final SyntaxTree root, final JsonTreeWalkerCallback nested) {
			this.root = root;
			this.nested = nested;
		}
		
		@Override
		public ContinueMode process(final NodeEnterMode mode, final JsonNode node, final Object... path) throws ContentException {
			switch (mode) {
				case ENTER	:
					stack.add(node);
					final ComparisonType	ct = isPathMatches(root,stack.toArray(new JsonNode[stack.size()]),path); 
					
					if (ct != ComparisonType.FALSE) {
						if (ct == ComparisonType.TRUE) {
							needExit.add(true);
							return nested.process(mode, node, path);
						}
						else {
							needExit.add(false);
							return ContinueMode.CONTINUE;
						}
					}
					else {
						needExit.add(false);
						return ContinueMode.SKIP_CHILDREN;
					}
				case EXIT	:
					stack.remove(stack.size()-1);
					if (needExit.remove(needExit.size()-1)) {
						return nested.process(mode, node, path);
					}
					else {
						return ContinueMode.CONTINUE;
					}
				default		:
					throw new UnsupportedOperationException("Node enter mode ["+mode+"] is not supported yet");
			}
		}
	}
	
	static int buildLexemaList(final char[] source, final List<Lexema> result) throws SyntaxException {
		final double[]		forDouble = new double[2];
		final long[]		forLong = new long[2];
		final int[]			forName = new int[2];
		final StringBuilder	sb = new StringBuilder();
		int					from  = 0, len = source.length;
		
loop:	for (;from < len;) {
			boolean			splitted = false;
			
			while (from < len && source[from] <= ' ' && source[from] != '\0') {
				splitted = true;
				from++;
			}
			switch (source[from]) {
				case '\0'	:
					break loop;
				case '[' 	:
					result.add(new Lexema(from++,splitted,LexemaType.OPENB));
					break;
				case ']' 	:
					result.add(new Lexema(from++,splitted,LexemaType.CLOSEB));
					break;
				case '(' 	:
					result.add(new Lexema(from++,splitted,LexemaType.OPEN));
					break;
				case ')' 	:
					result.add(new Lexema(from++,splitted,LexemaType.CLOSE));
					break;
				case ':' 	:
					result.add(new Lexema(from++,splitted,LexemaType.COLON));
					break;
				case '#' 	:
					result.add(new Lexema(from++,splitted,LexemaType.NUMBER));
					break;
				case '~' 	:
					result.add(new Lexema(from++,splitted,LexemaType.NOT_OP));
					break;
				case '$' 	:
					result.add(new Lexema(from++,splitted,LexemaType.CURRENT_VALUE));
					break;
				case ',' 	:
					result.add(new Lexema(from++,splitted,LexemaType.LIST));
					break;
				case '/' 	:
					result.add(new Lexema(from++,splitted,LexemaType.DIV_OP));
					break;
				case '%' 	:
					result.add(new Lexema(from++,splitted,LexemaType.REM_OP));
					break;
				case '+' 	:
					result.add(new Lexema(from++,splitted,LexemaType.ADD_OP));
					break;
				case '-' 	:
					result.add(new Lexema(from++,splitted,LexemaType.SUB_OP));
					break;
				case '=' 	:
					if (source[from+1] == '=') {	// '=' OR '==' - equals
						result.add(new Lexema(from,splitted,LexemaType.CMP_EQ_OP));
						from += 2;
					}
					else {
						result.add(new Lexema(from++,splitted,LexemaType.CMP_EQ_OP));
					}
					break;
				case '?' 	:
					result.add(new Lexema(from++,splitted,LexemaType.QUESTIONMARK));
					break;
				case '*' 	:
					if (source[from+1] == '*') {
						result.add(new Lexema(from,splitted,LexemaType.DOUBLEASTERISK));
						from += 2;
					}
					else {
						result.add(new Lexema(from++,splitted,LexemaType.MUL_OP));
					}
					break;
				case '|' 	:
					if (source[from+1] == '|') {
						result.add(new Lexema(from,splitted,LexemaType.OR_OP));
						from += 2;
					}
					else {
						throw new SyntaxException(0,from,"Unknown lexema");
					}
					break;
				case '&' 	:
					if (source[from+1] == '&') {
						result.add(new Lexema(from,splitted,LexemaType.AND_OP));
						from += 2;
					}
					else {
						throw new SyntaxException(0,from,"Unknown lexema");
					}
					break;
				case '.' 	:
					if (source[from+1] == '.') {
						if (source[from+2] == '/') {
							result.add(new Lexema(from,splitted,LexemaType.PARENTSLASH));
							from += 3;
						}
						else {
							result.add(new Lexema(from,splitted,LexemaType.RANGE));
							from += 2;
						}
					}
					else {
						if (source[from+1] == '/') {
							result.add(new Lexema(from,splitted,LexemaType.CURRENTSLASH));
							from += 2;
						}
						else {
							result.add(new Lexema(from++,splitted,LexemaType.DOT));
						}
					}
					break;
				case '>' 	:
					if (source[from+1] == '=') {
						result.add(new Lexema(from,splitted,LexemaType.CMP_GE_OP));
						from += 2;
					}
					else {
						result.add(new Lexema(from++,splitted,LexemaType.CMP_GT_OP));
					}
					break;
				case '<' 	:
					if (source[from+1] == '=') {
						result.add(new Lexema(from,splitted,LexemaType.CMP_LE_OP));
						from += 2;
					}
					else if (source[from+1] == '>') {
						result.add(new Lexema(from,splitted,LexemaType.CMP_NE_OP));
						from += 2;
					}
					else {
						result.add(new Lexema(from++,splitted,LexemaType.CMP_LT_OP));
					}
					break;
				case '0' : case '1'	: case '2' : case '3' : case '4' : case '5'	: case '6' : case '7' : case '8' : case '9' :
					final int	startNumber = from;
					
					try{from = CharUtils.parseLong(source,from,forLong,true);
						if (source[from] == '.' && source[from+1] != '.' || source[from] == 'e' || source[from] == 'E') {	// because of '..' range lexema!
							from = CharUtils.parseDouble(source,startNumber,forDouble,true);
							result.add(new Lexema(startNumber,splitted,LexemaType.DOUBLE,Double.doubleToLongBits(forDouble[0])));
						}
						else {
							result.add(new Lexema(startNumber,splitted,LexemaType.INTEGER,forLong[0]));
						}
					} catch (IllegalArgumentException exc) {
						throw new SyntaxException(0,startNumber,exc.getLocalizedMessage());
					}
					break;
				case '\"' :
					final int	startString = from + 1;
					
					try{sb.setLength(0);
						from = CharUtils.parseString(source,from+1,'\"',sb);
						result.add(new Lexema(startString,splitted,LexemaType.STRING,sb.toString()));
					} catch (IllegalArgumentException exc) {
						throw new SyntaxException(0,startString,exc.getLocalizedMessage());
					}
					break;
				case '\'' :
					final int	startJson = from + 1;
					
					try{sb.setLength(0);
						from = CharUtils.parseString(source,from+1,'\'',sb);
						result.add(new Lexema(startJson,splitted,LexemaType.JSON,sb.toString()));
					} catch (IllegalArgumentException exc) {
						throw new SyntaxException(0,startJson,exc.getLocalizedMessage());
					}
					break;
				default :
					if (Character.isJavaIdentifierStart(source[from])) {
						final int	startName = from;
						
						try{from = CharUtils.parseName(source,from,forName);
							result.add(new Lexema(startName,splitted,LexemaType.NAME,new String(source,forName[0],forName[1]-forName[0]+1)));
						} catch (IllegalArgumentException exc) {
							throw new SyntaxException(0,startName,exc.getLocalizedMessage());
						}
						break;
					}
					else {
						throw new SyntaxException(0,from,"Unsupported symbol ["+source[from]+"]");
					}
			}
		}
		result.add(new Lexema(from,false,LexemaType.EOF));
		return from;
	}

	static int buildJsonPath(final Lexema[] source, int from, final SyntaxTree node) throws SyntaxException {
		final List<SyntaxTree> 	list = new ArrayList<>();
		boolean					wasParentOrCurrent = false;
		SyntaxTree				temp, expr;
	
loop:	for(int maxFrom = source.length; from < maxFrom; from++) {
			switch (source[from].type) {
				case DIV_OP 		:
					temp = (SyntaxTree) node.clone();						
					temp.col = source[from].pos;
					temp.type = Command.TEMPLATE_ITEM;
					temp.cargo = TemplateType.ROOT;
					list.add(temp);
					break loop; 
				case PARENTSLASH 	:
					temp = (SyntaxTree) node.clone();						
					temp.col = source[from].pos;
					temp.type = Command.TEMPLATE_ITEM;
					temp.cargo = TemplateType.PARENT;
					list.add(temp);
					wasParentOrCurrent = true;
					break;
				case CURRENTSLASH	:
					temp = (SyntaxTree) node.clone();						
					temp.col = source[from].pos;
					temp.type = Command.TEMPLATE_ITEM;
					temp.cargo = TemplateType.CURRENT;
					list.add(temp);
					wasParentOrCurrent = true;
					break;
				default :
					break loop; 
			}
		}
		
		do {boolean		wereTemplateItem = false; 
			
div_loop:	while (source[from].type == LexemaType.DIV_OP || wasParentOrCurrent) {
				if (wasParentOrCurrent) {
					from--;
				}
				wasParentOrCurrent = false;
				wereTemplateItem = true;
				
				switch (source[++from].type) {
					case DOUBLEASTERISK	:
						temp = (SyntaxTree) node.clone();						
						temp.col = source[from].pos;
						temp.type = Command.TEMPLATE_ITEM;
						temp.cargo = TemplateType.ANY_CHAIN;
						list.add(temp);
						from++;
						break;
					case NAME :
						if (source[from].splitted) {
							break div_loop;
						}
					case MUL_OP	: case QUESTIONMARK :
						final StringBuilder	sbName = new StringBuilder();
						
						temp = (SyntaxTree) node.clone();						
						temp.col = source[from].pos;
						temp.type = Command.TEMPLATE_ITEM;
						temp.cargo = TemplateType.ANY_STRUCTURE;
						list.add(temp);
						
						while (source[from].type == LexemaType.NAME || source[from].type == LexemaType.MUL_OP || source[from].type == LexemaType.QUESTIONMARK) {
							switch (source[from].type) {
								case MUL_OP 		: 
									sbName.append('*');
									if (source[from+1].type == LexemaType.INTEGER) {
										sbName.append(source[++from].longVal);
									}
									break;
								case QUESTIONMARK	: 
									sbName.append('?');
									if (source[from+1].type == LexemaType.INTEGER) {
										sbName.append(source[++from].longVal);
									}
									break;
								case NAME			: 
									if (!source[from].splitted) {
										sbName.append(source[from].stringVal);
									}
									else {	// Two sequential names - second is NOT a template part
										break div_loop;
									}
									break;
								default	:
							}
							from++;
						}
						temp = (SyntaxTree) node.clone();						
						temp.col = source[from].pos;
						temp.type = Command.TEMPLATE_ITEM;
						temp.cargo = TemplateType.ANY_NAME;
						temp.children = new SyntaxTree[] {new SyntaxTree(source[from].pos,Command.PATTERN,0,Pattern.compile(Utils.fileMask2Regex(sbName.toString())))};
						list.add(temp);
						break;
					case OPENB	:
						temp = (SyntaxTree) node.clone();						
						temp.col = source[from].pos;
						temp.type = Command.TEMPLATE_ITEM;
						temp.cargo = TemplateType.ANY_ARRAY;
						list.add(temp);
						
						if (source[from+1].type == LexemaType.CLOSEB) {
							temp = (SyntaxTree) node.clone();						
							temp.col = source[from].pos;
							temp.type = Command.TEMPLATE_ITEM;
							temp.cargo = TemplateType.ANY_INDEX;
							list.add(temp);
							from += 2;
						}
						else {
							temp = (SyntaxTree) node.clone();
							expr = (SyntaxTree) node.clone(); 						
							temp.col = source[from].pos;
							temp.type = Command.TEMPLATE_ITEM;
							temp.cargo = TemplateType.SELECTED_INDEX;
							temp.children = new SyntaxTree[] {expr};
							list.add(temp);
							from = parseInnerIndex(source,from+1,expr);
							if (source[from].type == LexemaType.CLOSEB) {
								from++;
							}
							else {
								throw new SyntaxException(0,source[from].pos,"Missing ']'");
							}
						}
						break;
					default:
						temp = (SyntaxTree) node.clone();						
						temp.col = source[from].pos;
						temp.type = Command.TEMPLATE_ITEM;
						temp.cargo = TemplateType.ANY_NODE;
						list.add(temp);
						break;
				}
			}
			if (!wereTemplateItem) {
				break;
			}
			if (source[from].type == LexemaType.COLON) {
				temp = (SyntaxTree) node.clone();
				expr = (SyntaxTree) node.clone(); 						
				temp.col = source[from].pos;
				temp.type = Command.TEMPLATE_ITEM;
				temp.cargo = TemplateType.VALUE_FILTER;
				temp.children = new SyntaxTree[] {expr};
				from = parseInnerValueList(source,from+1,expr);
				list.add(temp);
			}
			if (source[from].type == LexemaType.NUMBER) {
				temp = (SyntaxTree) node.clone();
				expr = (SyntaxTree) node.clone(); 						
				temp.col = source[from].pos;
				temp.type = Command.TEMPLATE_ITEM;
				temp.cargo = TemplateType.COND_FILTER;
				temp.children = new SyntaxTree[] {expr};
				from = parseInnerCondition(source,from+1,expr);
				list.add(temp);
			}
		} while (source[from].type != LexemaType.EOF);
		
		node.col = source[from].pos;
		node.type = Command.TEMPLATE;
		node.children = list.toArray(new SyntaxTree[list.size()]);
		return from;
	}

	static int parseInnerIndex(final Lexema[] source, int from, final SyntaxTree node) throws SyntaxException {
		if (source[from].type == LexemaType.NAME && "has".equals(source[from].stringVal)) {
			return parseInnerIndexExpr(source,from+1,node);
		}
		else {
			final SyntaxTree	temp = (SyntaxTree) node.clone();
			
			from = parseInnerIndexList(source,from,temp);
			node.col = source[from].pos;
			node.type = Command.CMP_IN;
			node.cargo = new SyntaxTree(0,Command.CURRENT_INDEX,0,0); 
			node.children = new SyntaxTree[]{temp}; 
			return from;
		}
	}

	private static int parseInnerIndexExpr(final Lexema[] source, int from, final SyntaxTree result) throws SyntaxException {
		return parseExpression(source, from,Ordering.OR,
				(_source,_from,_level,_negParser,_termParser,_result)->{
					if (_source[_from].type == LexemaType.SUB_OP) {
						final SyntaxTree temp = new SyntaxTree(_result);
						
						_result.col = _source[_from].pos;
						_result.type = Command.NEGATE;
						_result.cargo = temp;
						return parseExpression(_source,_from+1,Ordering.TERM,_negParser,_termParser,temp);
					}
					else if (_source[_from].type == LexemaType.ADD_OP) {
						return parseExpression(_source,_from+1,Ordering.TERM,_negParser,_termParser,_result);
					}
					else {
						return parseExpression(_source,_from,Ordering.TERM,_negParser,_termParser,_result);
					}
				},
				(_source,_from,_level,_negParser,_termParser,_result)->{
					switch (_source[_from].type) {
						case NAME 		:
							if ("i".equals(_source[_from].stringVal)) {
								_result.col = _source[_from].pos;
								_result.type = Command.CURRENT_INDEX;
								_from++;
							}
							else {
								throw new SyntaxException(0,_source[_from].pos,"Only 'i' name allowed inside index conditional");
							}
							break;
						case INTEGER	:
							_result.col = _source[_from].pos;
							_result.type = Command.INT_CONST;
							_result.value = _source[_from].longVal;
							_from++;
							break;
						case OPEN		:
							_from = parseExpression(_source,_from+1,Ordering.OR,_negParser,_termParser,_result);
							if (_source[_from].type == LexemaType.CLOSE) {
								_from++;
							}
							else {
								throw new SyntaxException(0,_source[_from].pos,"Missing ')'");
							}
							break;
						default :
							throw new SyntaxException(0,_source[_from].pos,"Missing operand");
					}
					return _from;
				},result);
	}

	private static int parseInnerIndexList(final Lexema[] source, int from, final SyntaxTree result) throws SyntaxException {
		return parseExpression(source, from,Ordering.LIST,
				(_source,_from,_level,_negParser,_termParser,_result)->{
					if (_source[_from].type == LexemaType.SUB_OP) {
						final SyntaxTree temp = new SyntaxTree(_result);
						
						_result.col = _source[_from].pos;
						_result.type = Command.NEGATE;
						_result.cargo = temp;
						return parseExpression(_source,_from+1,Ordering.TERM,_negParser,_termParser,temp);
					}
					else if (_source[_from].type == LexemaType.ADD_OP) {
						return parseExpression(_source,_from+1,Ordering.TERM,_negParser,_termParser,_result);
					}
					else {
						return parseExpression(_source,_from,Ordering.TERM,_negParser,_termParser,_result);
					}
				},
				(_source,_from,_level,_negParser,_termParser,_result)->{
					switch (_source[_from].type) {
						case INTEGER	:
							_result.col = _source[_from].pos;
							_result.type = Command.INT_CONST;
							_result.value = _source[_from].longVal;
							_from++;
							break;
						case OPEN		:
							_from = parseExpression(_source,_from+1,Ordering.ADD,_negParser,_termParser,_result);
							if (_source[_from].type == LexemaType.CLOSE) {
								_from++;
							}
							else {
								throw new SyntaxException(0,_source[_from].pos,"Missing ')'");
							}
							break;
						default :
					}
					return _from;
				},result);
	}

	private static int parseInnerValueList(final Lexema[] source, final int from, final SyntaxTree result) throws SyntaxException {
		final int rc = parseExpression(source, from, Ordering.LIST,
				(_source,_from,_level,_negParser,_termParser,_result)->{
					if (_source[_from].type == LexemaType.SUB_OP) {
						final SyntaxTree temp = (SyntaxTree) result.clone();
						
						_result.col = source[_from].pos;
						_result.type = Command.NEGATE;
						_result.cargo = temp;
						return parseExpression(_source,_from+1,Ordering.TERM,_negParser,_termParser,temp);
					}
					else if (_source[_from].type == LexemaType.ADD_OP) {
						return parseExpression(_source,_from+1,Ordering.TERM,_negParser,_termParser,_result);
					}
					else {
						return parseExpression(_source,_from,Ordering.TERM,_negParser,_termParser,_result);
					}
				},
				(_source,_from,_level,_negParser,_termParser,_result)->{
					switch (_source[_from].type) {
						case NAME 		:
							if ("true".equals(_source[_from].stringVal)) {
								_result.col = _source[_from].pos;
								_result.type = Command.BOOL_CONST;
								_result.value = 1;
								_from++;
							}
							else if ("false".equals(_source[_from].stringVal)) {
								_result.col = _source[_from].pos;
								_result.type = Command.BOOL_CONST;
								_result.value = 0;
								_from++;
							}
							else if ("null".equals(_source[_from].stringVal)) {
								_result.col = _source[_from].pos;
								_result.type = Command.NULL_CONST;
								_from++;
							}
							else {
								throw new SyntaxException(0,_source[_from].pos,"Only 'true', 'false' or 'null' allowed inside value conditional");
							}
							break;
						case INTEGER	:
							_result.col = _source[_from].pos;
							_result.type = Command.INT_CONST;
							_result.value = _source[_from].longVal;
							_from++;
							break;
						case STRING		:
							_result.col = _source[_from].pos;
							_result.type = Command.STRING_CONST;
							_result.cargo = _source[_from].stringVal;
							_from++;
							break;
						case OPEN		:
							_from = parseExpression(_source,_from+1,Ordering.ADD,_negParser,_termParser,_result);
							if (_source[_from].type == LexemaType.CLOSE) {
								_from++;
							}
							else {
								throw new SyntaxException(0,_source[_from].pos,"Missing ')'");
							}
							break;
						default :
							throw new SyntaxException(0,_source[_from].pos,"Missing operand");
					}
					return _from;
				},result);
		final SyntaxTree	temp = (SyntaxTree) result.clone();
		
		result.col = source[from].pos;
		result.type = Command.CMP_IN;
		result.cargo = new SyntaxTree(0,Command.CURRENT_VALUE,0,0); 
		result.children = new SyntaxTree[]{temp}; 
		return rc;
		
	}

	private static int parseInnerCondition(final Lexema[] source, int from, final SyntaxTree result) throws SyntaxException {
		return parseExpression(source, from, Ordering.OR,
				(_source,_from,_level,_negParser,_termParser,_result)->{
					if (_source[_from].type == LexemaType.SUB_OP) {
						final SyntaxTree temp = new SyntaxTree(_result);
						
						_result.col = _source[from].pos;
						_result.type = Command.NEGATE;
						_result.cargo = temp;
						return parseExpression(_source,_from+1,Ordering.TERM,_negParser,_termParser,temp);
					}
					else if (_source[_from].type == LexemaType.ADD_OP) {
						return parseExpression(_source,_from+1,Ordering.TERM,_negParser,_termParser,_result);
					}
					else {
						return parseExpression(_source,_from,Ordering.TERM,_negParser,_termParser,_result);
					}
				},
				(_source,_from,_level,_negParser,_termParser,_result)->{
					switch (_source[_from].type) {
						case NAME 		:
							if ("true".equals(_source[_from].stringVal)) {
								_result.col = _source[_from].pos;
								_result.type = Command.BOOL_CONST;
								_result.value = 1;
								_from++;
							}
							else if ("false".equals(_source[_from].stringVal)) {
								_result.col = _source[_from].pos;
								_result.type = Command.BOOL_CONST;
								_result.value = 0;
								_from++;
							}
							else if ("null".equals(_source[_from].stringVal)) {
								_result.col = _source[_from].pos;
								_result.type = Command.NULL_CONST;
								_from++;
							}
							else {
								throw new SyntaxException(0,_source[_from].pos,"Illegal name: only [true], [false] and [null] are available here");
							}
							break;
						case INTEGER	:
							_result.col = _source[_from].pos;
							_result.type = Command.INT_CONST;
							_result.value = _source[_from].longVal;
							_from++;
							break;
						case DOUBLE		:
							_result.col = _source[_from].pos;
							_result.type = Command.REAL_CONST;
							_result.value = _source[_from].longVal;
							_from++;
							break;
						case CURRENT_VALUE	:
							_result.col = _source[_from].pos;
							_result.type = Command.CURRENT_VALUE;
							_from++;
							break;
						case STRING		:
							_result.col = _source[_from].pos;
							_result.type = Command.STRING_CONST;
							_result.cargo = _source[_from].stringVal;
							_from++;
							break;
						case JSON		:
							_result.col = _source[_from].pos;
							_result.type = Command.SUBTREE_JSON;
							try(final Reader			rdr = new StringReader(_source[_from].stringVal);
								final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
								
								parser.next();
								_result.cargo = loadJsonTree(parser); 
							} catch (IOException exc) {
								throw new SyntaxException(0,_source[_from].pos,"Illegal json syntax: "+exc.getLocalizedMessage());
							}
							_from++;
							break;
						case PARENTSLASH	: case CURRENTSLASH	:
							return buildJsonPath(_source,_from,_result);
						case OPEN		:
							_from = parseExpression(_source,_from+1,Ordering.OR,_negParser,_termParser,_result);
							if (_source[_from].type == LexemaType.CLOSE) {
								_from++;
							}
							else {
								throw new SyntaxException(0,_source[_from].pos,"Missing ')'");
							}
							break;
						default :
							throw new SyntaxException(0,_source[_from].pos,"Missing operand");
					}
					return _from;
				},
				result);
	}

	@FunctionalInterface
	private interface SpecialParser {
		int parse(Lexema[] source, int from, Ordering level, SpecialParser negParser, SpecialParser termParser, SyntaxTree result) throws SyntaxException; 
	}

	private static int parseExpression(final Lexema[] source, int from, final Ordering level, final SpecialParser negParser, final SpecialParser termParser, final SyntaxTree result) throws SyntaxException {
		switch (level) {
			case OR		:
				from = parseExpression(source,from,Ordering.AND,negParser,termParser,result);
				if (source[from].type == LexemaType.OR_OP) {
					final List<SyntaxTree>	list = new ArrayList<>();
					
					SyntaxTree				temp = (SyntaxTree) result.clone();
					list.add(temp);
					
					do {list.add(temp = new SyntaxTree(result));
						from = parseExpression(source,from+1,Ordering.AND,negParser,termParser,temp);
					} while (source[from].type == LexemaType.OR_OP);
					
					result.type = Command.OR;
					result.children = list.toArray(new SyntaxTree[list.size()]);
				}
				break;
			case AND	:
				from = parseExpression(source,from,Ordering.NOT,negParser,termParser,result);
				if (source[from].type == LexemaType.AND_OP) {
					final List<SyntaxTree>	list = new ArrayList<>();
					
					SyntaxTree				temp = (SyntaxTree) result.clone();
					list.add(temp);
					
					do {list.add(temp = new SyntaxTree(result));
						from = parseExpression(source,from+1,Ordering.NOT,negParser,termParser,temp);
					} while (source[from].type == LexemaType.AND_OP);
					
					result.type = Command.AND;
					result.children = list.toArray(new SyntaxTree[list.size()]);
				}
				break;
			case NOT	:
				if (source[from].type == LexemaType.NOT_OP) {
					final SyntaxTree temp = (SyntaxTree) result.clone();
					
					result.col = source[from].pos;
					result.type = Command.NOT;
					result.cargo = temp;
					return parseExpression(source,from+1,Ordering.CMP,negParser,termParser,temp);
				}
				else {
					return parseExpression(source,from,Ordering.CMP,negParser,termParser,result);
				}
			case CMP	:
				from = parseExpression(source,from,Ordering.ADD,negParser,termParser,result);
				switch (source[from].type) {
					case CMP_EQ_OP	:
						final SyntaxTree	leftEQ = (SyntaxTree) result.clone(), rightEQ = (SyntaxTree) result.clone();
						
						result.col = source[from].pos; 
						result.type = Command.CMP_EQ;
						result.children = new SyntaxTree[] {leftEQ,rightEQ};
						from = parseExpression(source,from+1,Ordering.ADD,negParser,termParser,rightEQ);
						break;
					case CMP_GE_OP	:
						final SyntaxTree	leftGE = (SyntaxTree) result.clone(), rightGE = (SyntaxTree) result.clone();
						
						result.col = source[from].pos; 
						result.type = Command.CMP_GE;
						result.children = new SyntaxTree[] {leftGE,rightGE};
						from = parseExpression(source,from+1,Ordering.ADD,negParser,termParser,rightGE);
						break;
					case CMP_GT_OP	:
						final SyntaxTree	leftGT = (SyntaxTree) result.clone(), rightGT = (SyntaxTree) result.clone();
						
						result.col = source[from].pos; 
						result.type = Command.CMP_GT;
						result.children = new SyntaxTree[] {leftGT,rightGT};
						from = parseExpression(source,from+1,Ordering.ADD,negParser,termParser,rightGT);
						break;
					case CMP_LE_OP	:
						final SyntaxTree	leftLE = (SyntaxTree) result.clone(), rightLE = (SyntaxTree) result.clone();
						
						result.col = source[from].pos; 
						result.type = Command.CMP_LE;
						result.children = new SyntaxTree[] {leftLE,rightLE};
						from = parseExpression(source,from+1,Ordering.ADD,negParser,termParser,rightLE);
						break;
					case CMP_LT_OP	:
						final SyntaxTree	leftLT = (SyntaxTree) result.clone(), rightLT = (SyntaxTree) result.clone();
						
						result.col = source[from].pos; 
						result.type = Command.CMP_LT;
						result.children = new SyntaxTree[] {leftLT,rightLT};
						from = parseExpression(source,from+1,Ordering.ADD,negParser,termParser,rightLT);
						break;
					case CMP_NE_OP	:
						final SyntaxTree	leftNE = (SyntaxTree) result.clone(), rightNE = (SyntaxTree) result.clone();
						
						result.col = source[from].pos; 
						result.type = Command.CMP_NE;
						result.children = new SyntaxTree[] {leftNE,rightNE};
						from = parseExpression(source,from+1,Ordering.ADD,negParser,termParser,rightNE);
						break;
					case NAME		:
						if ("in".equals(source[from].stringVal) ) {
							final SyntaxTree	left = (SyntaxTree) result.clone(), right = (SyntaxTree) result.clone();
							
							result.col = source[from].pos; 
							result.type = Command.CMP_IN;
							result.cargo = left;
							result.children = new SyntaxTree[] {right};
							from = parseExpression(source,from+1,Ordering.LIST,negParser,
									(_source,_from,_level,_negParser,_termParser,_result)->{
										if (_source[_from].type == LexemaType.OPEN) {	// Nested expression for list can't contain logical operators!
											final int _rc = parseExpression(_source,_from+1,Ordering.ADD,negParser,termParser,_result);
											
											if (_source[_rc].type == LexemaType.CLOSE) {
												return _rc + 1;
											}
											else {
												throw new SyntaxException(0,source[_rc].pos,"Missing ')'"); 
											}
										}
										else {
											return termParser.parse(_source,_from,_level,negParser,termParser,_result);
										}
									}
									,right);
						}
						else if ("is".equals(source[from].stringVal) ) {
							if (source[from+1].type == LexemaType.NAME) {
								final SyntaxTree	left = (SyntaxTree) result.clone();
								final JsonNodeType	type;
								
								switch (source[from+1].stringVal) {
									case "arr"	:
										type = JsonNodeType.JsonArray;
										break;
									case "obj" 	:
										type = JsonNodeType.JsonObject;
										break;
									case "int" 	:
										type = JsonNodeType.JsonInteger;
										break;
									case "real" :
										type = JsonNodeType.JsonReal;
										break;
									case "bool" :
										type = JsonNodeType.JsonBoolean;
										break;
									case "str" 	:
										type = JsonNodeType.JsonString;
										break;
									case "null" :
										type = JsonNodeType.JsonNull;
										break;
									default : 
										throw new SyntaxException(0,source[from].pos,"Illegal right operand in 'is' clause - type name unsupported. Only 'arr', 'obj', 'int', 'real', 'bool', 'str', 'null' are available"); 
								}
								
								result.col = source[from].pos; 
								result.type = Command.CMP_IS;
								result.cargo = type;
								result.children = new SyntaxTree[] {left};
								from += 2;
							}
							else {
								throw new SyntaxException(0,source[from].pos,"Illegal right operand in 'is' clause - type name awaited"); 
							}
						}
						else {
							throw new SyntaxException(0,source[from].pos,"Illegal operator, 'in' or 'is' awaited"); 
						}
						break;
					default :
				}
				break;
			case LIST	:
				int	lastListFrom = from;
				
				from = parseExpression(source,from,Ordering.RANGE,negParser,termParser,result);
				if (source[from].type == LexemaType.LIST) {
					if (lastListFrom == from) {
						throw new SyntaxException(0,source[from].pos,"List item missing"); 
					}
					else {
						final List<SyntaxTree>	list = new ArrayList<>();
						
						SyntaxTree				temp = (SyntaxTree) result.clone();
						list.add(temp);
						
						do {lastListFrom = ++from;
							list.add(temp = (SyntaxTree) result.clone());
							from = parseExpression(source,from,Ordering.RANGE,negParser,termParser,temp);
							if (from == lastListFrom) {
								throw new SyntaxException(0,source[from].pos,"List item missing"); 
							}
						} while (source[from].type == LexemaType.LIST);
						
						result.type = Command.LIST;
						result.children = list.toArray(new SyntaxTree[list.size()]);
					}
				}
				break;
			case RANGE	:
				int	lastRangeFrom = from;
				
				from = parseExpression(source,from,Ordering.ADD,negParser,termParser,result);
				if (source[from].type == LexemaType.RANGE) {
					if (lastRangeFrom == from) {
						throw new SyntaxException(0,source[from].pos,"Start range item missing");
					}
					else {
						final SyntaxTree		temp1 = (SyntaxTree) result.clone(), temp2 = (SyntaxTree) result.clone(); 
						
						from = parseExpression(source,lastRangeFrom = from+1,Ordering.ADD,negParser,termParser,temp2);
						if (lastRangeFrom == from) {
							throw new SyntaxException(0,source[from].pos,"End range item missing");
						}
						else {
							result.type = Command.RANGE;
							result.children = new SyntaxTree[] {temp1,temp2};
						}
					}
				}
				break;
			case ADD	:
				from = parseExpression(source,from,Ordering.MUL,negParser,termParser,result);
				if (source[from].type == LexemaType.ADD_OP || source[from].type == LexemaType.SUB_OP) {
					final List<SyntaxTree>	list = new ArrayList<>();
					final StringBuilder		sb = new StringBuilder("+");
					
					SyntaxTree				temp = new SyntaxTree(result);
					list.add(temp);
					
					do {
						if (source[from].type == LexemaType.ADD_OP) {
							sb.append('+');
						}
						else {
							sb.append('-');
						}
						list.add(temp = new SyntaxTree(result));
						from = parseExpression(source,from+1,Ordering.MUL,negParser,termParser,temp);
					} while (source[from].type == LexemaType.ADD_OP || source[from].type == LexemaType.SUB_OP);
					
					result.type = Command.ADD;
					result.cargo = sb.toString().toCharArray();
					result.children = list.toArray(new SyntaxTree[list.size()]);
				}
				break;
			case MUL	:
				from = parseExpression(source,from,Ordering.NEG,negParser,termParser,result);
				if (source[from].type == LexemaType.MUL_OP || source[from].type == LexemaType.DIV_OP || source[from].type == LexemaType.REM_OP) {
					final List<SyntaxTree>	list = new ArrayList<>();
					final StringBuilder		sb = new StringBuilder("*");
					
					SyntaxTree				temp = new SyntaxTree(result);
					list.add(temp);
					
					do {
						if (source[from].type == LexemaType.MUL_OP) {
							sb.append('*');
						}
						else if (source[from].type == LexemaType.DIV_OP) {
							sb.append('/');
						}
						else {
							sb.append('%');
						}
						list.add(temp = new SyntaxTree(result));
						from = parseExpression(source,from+1,Ordering.NEG,negParser,termParser,temp);
					} while (source[from].type == LexemaType.MUL_OP || source[from].type == LexemaType.DIV_OP || source[from].type == LexemaType.REM_OP);
					
					result.type = Command.MULTIPLY;
					result.cargo = sb.toString().toCharArray();
					result.children = list.toArray(new SyntaxTree[list.size()]);
				}
				break;
			case NEG	:
				return negParser.parse(source,from,level,negParser,termParser,result);
			case TERM	:
				return termParser.parse(source,from,level,negParser,termParser,result);
			default:
				throw new UnsupportedOperationException("Ordering level ["+level+"] is not supported yet");
		}
		return from;
	}

	static ComparisonType isPathMatches(final SyntaxTree root, final JsonNode[] stack, final Object[] path) {
		final List<Object>	innerStack = new ArrayList<>();
		
		return isPathMatches(root,stack,path,innerStack);
	}
	
	private static ComparisonType isPathMatches(final SyntaxTree node, final JsonNode[] stack, final Object[] path, final List<Object> innerStack) {
		switch (node.type) {
			case TEMPLATE		:
				int		index = 0;
				
				for (SyntaxTree item : node.children) {	// Skip parent and current refs
					if (item.type == Command.TEMPLATE_ITEM && (((TemplateType)item.cargo) == TemplateType.PARENT || ((TemplateType)item.cargo) == TemplateType.CURRENT)) {
						index++;
					}
					else {
						break;
					}
				}
				return compareTemplate(stack,path,0,node.children,index);
			default				:
				throw new UnsupportedOperationException("Command ["+node.type+"] is not supported yet"); 
		}
	}

	private static boolean isExpressionTrue(final SyntaxTree node, final JsonNode[] stack, final Object[] path, final int fromPath, final List<Object> innerStack) {
		int		topStack = innerStack.size() - 1;
		
		switch (node.type) {
			case OR				:
				for (SyntaxTree item : node.children) {
					if (isExpressionTrue(item,stack,path,fromPath,innerStack)) {
						final Object	value = innerStack.remove(innerStack.size()-1); 
						
						if ((value instanceof Boolean) && ((Boolean)value) || (value instanceof JsonNode[])) {
							innerStack.add(true);
							return true;
						}
					}
					else {
						resetStack(innerStack, topStack);
						return false;
					}
				}
				resetStack(innerStack, topStack);
				innerStack.add(false);
				return true;
			case AND				:
				for (SyntaxTree item : node.children) {
					if (isExpressionTrue(item,stack,path,fromPath,innerStack)) {
						final Object	value = innerStack.remove(innerStack.size()-1); 
						
						if ((value instanceof Boolean) && !((Boolean)value)) {
							innerStack.add(false);
							return true;
						}
					}
					else {
						resetStack(innerStack, topStack);
						return false;
					}
				}
				resetStack(innerStack, topStack);
				innerStack.add(true);
				return true;
			case NOT			:
				if (isExpressionTrue((SyntaxTree)node.cargo,stack,path,fromPath,innerStack)) {
					final Object	value = innerStack.remove(innerStack.size()-1); 

					if (value instanceof JsonNode[]) {
						innerStack.add(false);
						return true;
					}
					else if (value instanceof Boolean) {
						innerStack.add(!((Boolean)value));
						return true;
					}
					else {
						resetStack(innerStack, topStack);
						return false;
					}
				}
				else {
					resetStack(innerStack, topStack);
					return false;
				}
			case CMP_EQ			:
				if (isExpressionTrue((SyntaxTree)node.children[0],stack,path,fromPath,innerStack) && isExpressionTrue(node.children[1],stack,path,fromPath,innerStack)) {
					Object	right = innerStack.remove(innerStack.size()-1), left = innerStack.remove(innerStack.size()-1);
					
					if ((left instanceof JsonNode[]) && (right instanceof JsonNode)) {
						innerStack.add(compareJson(((JsonNode[])left)[0],(JsonNode)right) == 0);
					}
					else {
						left = extractValue(left);
						right = extractValue(right);
						innerStack.add(left.equals(convertType(right,left)));
					}
					return true;
				}
				else {
					resetStack(innerStack, topStack);
					return false;
				}
			case CMP_NE			:
				if (isExpressionTrue((SyntaxTree)node.children[0],stack,path,fromPath,innerStack) && isExpressionTrue(node.children[1],stack,path,fromPath,innerStack)) {
					Object	right = innerStack.remove(innerStack.size()-1), left = innerStack.remove(innerStack.size()-1);
					
					if ((left instanceof JsonNode[]) && (right instanceof JsonNode)) {
						innerStack.add(compareJson(((JsonNode[])left)[0],(JsonNode)right) != 0);
					}
					else {
						left = extractValue(left);
						right = extractValue(right);
						innerStack.add(!left.equals(convertType(right,left)));
					}
					return true;
				}
				else {
					resetStack(innerStack, topStack);
					return false;
				}
			case CMP_GE			:
				if (isExpressionTrue((SyntaxTree)node.children[0],stack,path,fromPath,innerStack) && isExpressionTrue(node.children[1],stack,path,fromPath,innerStack)) {
					final Object	right = extractValue(innerStack.remove(innerStack.size()-1));
					final Object	left = extractValue(innerStack.remove(innerStack.size()-1));

					if (left instanceof Comparable) {
						innerStack.add(((Comparable)left).compareTo(convertType(right,left)) >= 0);
						return true;
					}
					else {
						resetStack(innerStack, topStack);
						return false;
					}
				}
				else {
					resetStack(innerStack, topStack);
					return false;
				}
			case CMP_GT			:
				if (isExpressionTrue((SyntaxTree)node.children[0],stack,path,fromPath,innerStack) && isExpressionTrue(node.children[1],stack,path,fromPath,innerStack)) {
					final Object	right = extractValue(innerStack.remove(innerStack.size()-1));
					final Object	left = extractValue(innerStack.remove(innerStack.size()-1));

					if (left instanceof Comparable) {
						innerStack.add(((Comparable)left).compareTo(convertType(right,left)) > 0);
						return true;
					}
					else {
						resetStack(innerStack, topStack);
						return false;
					}
				}
				else {
					resetStack(innerStack, topStack);
					return false;
				}
			case CMP_LE			:
				if (isExpressionTrue((SyntaxTree)node.children[0],stack,path,fromPath,innerStack) && isExpressionTrue(node.children[1],stack,path,fromPath,innerStack)) {
					final Object	right = extractValue(innerStack.remove(innerStack.size()-1));
					final Object	left = extractValue(innerStack.remove(innerStack.size()-1));

					if (left instanceof Comparable) {
						innerStack.add(((Comparable)left).compareTo(convertType(right,left)) <= 0);
						return true;
					}
					else {
						resetStack(innerStack, topStack);
						return false;
					}
				}
				else {
					resetStack(innerStack, topStack);
					return false;
				}
			case CMP_LT			:
				if (isExpressionTrue((SyntaxTree)node.children[0],stack,path,fromPath,innerStack) && isExpressionTrue(node.children[1],stack,path,fromPath,innerStack)) {
					final Object	right = extractValue(innerStack.remove(innerStack.size()-1));
					final Object	left = extractValue(innerStack.remove(innerStack.size()-1));

					if (left instanceof Comparable) {
						innerStack.add(((Comparable)left).compareTo(convertType(right,left)) < 0);
						return true;
					}
					else {
						resetStack(innerStack, topStack);
						return false;
					}
				}
				else {
					resetStack(innerStack, topStack);
					return false;
				}
			case CMP_IN			:
				if (isExpressionTrue((SyntaxTree)node.cargo,stack,path,fromPath,innerStack) && isExpressionTrue(node.children[0],stack,path,fromPath,innerStack)) {
					final Object	right = innerStack.remove(innerStack.size()-1);
					final Object	left = extractValue(innerStack.remove(innerStack.size()-1));
					boolean			compared = false;
					
					switch (node.children[0].type) {
						case LIST 	:
							for (Object item : (Object[])right) {
								if ((item instanceof Object[]) && (left instanceof Comparable)) {
									final  Object[]	temp = (Object[])item;
									
									temp[0] = convertType(temp[0],left);
									temp[1] = convertType(temp[1],left);
									
									if ((temp[0] instanceof Comparable) && (temp[1] instanceof Comparable) && ((Comparable<Object>)temp[0]).compareTo(left) <= 0 && ((Comparable<Object>)temp[1]).compareTo(left) >= 0) {
										compared = true;
										break;
									}
								}
								else if (left.equals(convertType(item,left))) {
									compared = true;
									break;
								}
							}
							break;
						case RANGE	:
							final  Object[]	temp = (Object[])right;
							
							temp[0] = convertType(temp[0],left);
							temp[1] = convertType(temp[1],left);
							
							if ((temp[0] instanceof Comparable) && (temp[1] instanceof Comparable) && ((Comparable<Object>)temp[0]).compareTo(left) <= 0 && ((Comparable<Object>)temp[1]).compareTo(left) >= 0) {
								compared = true;
							}
							break;
						default 	:
							compared = left.equals(convertType(right,left));
							break;
					}
					
					resetStack(innerStack, topStack);
					innerStack.add(compared);
					return true;
				}
				else {
					resetStack(innerStack, topStack);
					return false;
				}
			case CMP_IS			:
				if (isExpressionTrue(node.children[0],stack,path,fromPath,innerStack)) {
					Object			left = innerStack.remove(innerStack.size()-1);
					final boolean	compared;
					
					if (left instanceof JsonNode[]) {
						left = ((JsonNode[])left)[0];
					}
					if (left instanceof JsonNode) {
						compared = ((JsonNode)left).getType() == (JsonNodeType)node.cargo;
					}
					else {
						switch ((JsonNodeType)node.cargo) {
							case JsonArray : case JsonObject :
								compared = false;
								break;
							case JsonBoolean	:
								compared = left instanceof Boolean;
								break;
							case JsonInteger	:
								compared = left instanceof Long;
								break;
							case JsonNull		:
								compared = left == NULL_MARKER;
								break;
							case JsonReal		:
								compared = left instanceof Double;
								break;
							case JsonString		:
								compared = left instanceof String;
								break;
							default:
								throw new UnsupportedOperationException("Json node type ["+node.cargo+"] is not supported yet");
						}
					}
					resetStack(innerStack, topStack);
					innerStack.add(compared);
					return true;
				}
				else {
					resetStack(innerStack, topStack);
					return false;
				}
			case LIST			:
				final List<Object>	listResult = new ArrayList<>();
				
				for (SyntaxTree item : node.children) {
					if (isExpressionTrue(item,stack,path,fromPath,innerStack)) {
						listResult.add(innerStack.remove(innerStack.size()-1));
					}
					else {
						resetStack(innerStack, topStack);
						return false;
					}
				}
				resetStack(innerStack, topStack);
				innerStack.add(listResult.toArray());
				return true;				
			case RANGE			:
				final Object[]	rangeResult = new Object[2];
				
				if (isExpressionTrue(node.children[0],stack,path,fromPath,innerStack)) {
					rangeResult[0] = innerStack.remove(innerStack.size()-1);
					if (isExpressionTrue(node.children[1],stack,path,fromPath,innerStack)) {
						rangeResult[1] = innerStack.remove(innerStack.size()-1);
					}
					else {
						resetStack(innerStack, topStack);
						return false;
					}
				}
				else {
					resetStack(innerStack, topStack);
					return false;
				}
				resetStack(innerStack, topStack);
				innerStack.add(rangeResult);
				return true;				
			case CURRENT_INDEX	:
				if (path[fromPath] instanceof Number) {
					innerStack.add(((Number)path[fromPath]).longValue());
					return true;
				}
				else {
					resetStack(innerStack, topStack);
					return false;
				}
			case CURRENT_VALUE	:
				switch (((JsonNode)stack[fromPath]).getType()) {
					case JsonBoolean	:
						innerStack.add(((JsonNode)stack[fromPath]).getBooleanValue());
						break;
					case JsonInteger	:
						innerStack.add(((JsonNode)stack[fromPath]).getLongValue());
						break;
					case JsonNull		:
						innerStack.add(NULL_MARKER);
						break;
					case JsonReal		:
						innerStack.add(((JsonNode)stack[fromPath]).getDoubleValue());
						break;
					case JsonString		:
						innerStack.add(((JsonNode)stack[fromPath]).getStringValue());
						break;
					default				:
						resetStack(innerStack, topStack);
						return false;
				}
				return true;
			case BOOL_CONST		:
				innerStack.add(node.value != 0);
				return true;
			case INT_CONST		:
				innerStack.add(node.value);
				return true;
			case NULL_CONST		:
				innerStack.add(NULL_MARKER);
				return true;
			case REAL_CONST		:
				innerStack.add(Double.longBitsToDouble(node.value));
				return true;
			case STRING_CONST		:
				innerStack.add(node.cargo.toString());
				return true;
			case MULTIPLY		:
				long	mulLong = 1;
				double	mulDouble = 1;
				boolean	wasDoubleMul = false;
				
				for (int index = 0, maxIndex = node.children.length; index < maxIndex; index++) {
					if (isExpressionTrue(node.children[index],stack,path,fromPath,innerStack)) {
						final Object	operand = extractValue(innerStack.remove(innerStack.size()-1));
						
						wasDoubleMul |= (operand instanceof Double);
						switch (((char[])node.cargo)[index]) {
							case '*' :
								mulLong *= ((Number)convertType(operand,mulLong)).longValue();
								mulDouble *= ((Number)convertType(operand,mulLong)).doubleValue();
								break;
							case '/' :
								mulLong /= ((Number)convertType(operand,mulLong)).longValue();
								mulDouble /= ((Number)convertType(operand,mulLong)).doubleValue();
								break;
							case '%' :
								mulLong %= ((Number)convertType(operand,mulLong)).longValue();
								mulDouble %= ((Number)convertType(operand,mulLong)).doubleValue();
								break;
						}
					}
					else {
						resetStack(innerStack, topStack);
						return false;
					}
				}
				if (wasDoubleMul) {
					innerStack.add(mulDouble);
				}
				else {
					innerStack.add(mulLong);
				}
				return true;
			case ADD		:
				long	addLong = 0;
				double	addDouble = 0;
				boolean	wasDoubleAdd = false;
				
				for (int index = 0, maxIndex = node.children.length; index < maxIndex; index++) {
					if (isExpressionTrue(node.children[index],stack,path,fromPath,innerStack)) {
						final Object	operand = extractValue(innerStack.remove(innerStack.size()-1));
						
						wasDoubleAdd |= (operand instanceof Double);
						switch (((char[])node.cargo)[index]) {
							case '+' :
								addLong += ((Number)convertType(operand,addLong)).longValue();
								addDouble += ((Number)convertType(operand,addLong)).doubleValue();
								break;
							case '-' :
								addLong -= ((Number)convertType(operand,addLong)).longValue();
								addDouble -= ((Number)convertType(operand,addLong)).doubleValue();
								break;
						}
					}
					else {
						resetStack(innerStack, topStack);
						return false;
					}
				}
				if (wasDoubleAdd) {
					innerStack.add(addDouble);
				}
				else {
					innerStack.add(addLong);
				}
				return true;
			case NEGATE			:
				if (isExpressionTrue((SyntaxTree)node.cargo,stack,path,fromPath,innerStack)) {
					Object	operand = extractValue(innerStack.remove(innerStack.size()-1));
					
					if (operand instanceof Double) {
						innerStack.add(-((Double)operand).doubleValue());
						return true;
					}
					else if (operand instanceof Number) {
						innerStack.add(-((Number)operand).longValue());
						return true;
					}
					else {
						resetStack(innerStack, topStack);
						return false;
					}
				}
				else {
					resetStack(innerStack, topStack);
					return false;
				}
			case SUBTREE_JSON	:
				innerStack.add(node.cargo);
				return true;
			case TEMPLATE		:
				try{	// Extract all content found or put false
					final List<JsonNode>	found = new ArrayList<>();
					int						index = fromPath, pathIndex, maxPathIndex;
					final JsonNode			root;
					
loop:				for (pathIndex = 0, maxPathIndex = node.children.length; pathIndex < maxPathIndex; pathIndex++) {
						switch ((TemplateType)node.children[pathIndex].cargo) {
							case CURRENT	:
								break;
							case PARENT		:
								if (index > 0) {
									index--;
								}
								else {	// Jump upper root
									resetStack(innerStack, topStack);
									return false;
								}
								break;
							case ROOT		:
								index = 0;
								break loop;
							default :
								break loop;
						}
					}
					root = (JsonNode)stack[index];
					
					final SyntaxTree	tempNode = (SyntaxTree) node.clone();
					
					tempNode.children = Arrays.copyOfRange(tempNode.children,pathIndex,maxPathIndex); 
					
					walkDownJson(root,new XPathStyledFilter(tempNode,(_mode,_node,_path)-> {
						if (_mode == NodeEnterMode.ENTER) {
							found.add(_node);
						}
						return ContinueMode.CONTINUE;
					}));
					
					if (found.isEmpty()) {
						innerStack.add(false);
					}
					else {
						innerStack.add(found.toArray(new JsonNode[found.size()]));
					}
					return true;
				} catch (ContentException e) {
					resetStack(innerStack, topStack);
					return false;
				}
			default				:
				throw new UnsupportedOperationException("Command ["+node.type+"] is not supported yet"); 
		}
	}
	
	private static Object extractValue(final Object value) {
		if (value instanceof JsonNode[]) {
			return extractValue(((JsonNode[])value)[0]);
		}
		else if (value instanceof JsonNode) {
			final JsonNode	temp = (JsonNode)value;
			
			switch (temp.getType()) {
				case JsonBoolean	:
					return temp.getBooleanValue();
				case JsonInteger	:
					return temp.getLongValue();
				case JsonNull : case JsonArray : case JsonObject :
					return NULL_MARKER;
				case JsonReal		:
					return temp.getDoubleValue();
				case JsonString		:
					return temp.getStringValue();
				default				:
					throw new UnsupportedOperationException("Json node ["+temp.getType()+"] is not supported yet"); 
			}
		}
		else {
			return value;
		}
	}

	private static int compareJson(final JsonNode left, final JsonNode right) {
		if (left == null && right == null) {
			return 0;
		}
		else if (left == null && right != null) {
			return 1;
		}
		else if (left != null && right == null) {
			return -1;
		}
		else if (left.getType() == right.getType()) {
			switch (left.getType()) {
				case JsonArray		:
					final int	maxIndex = Math.min(left.childrenCount(),right.childrenCount());
					int			rcArray;
					
					for (int index = 0; index < maxIndex; index++) {
						if ((rcArray = compareJson(left.children()[index],right.children()[index])) != 0) {
							return rcArray;
						}
					}
					return right.childrenCount() - left.childrenCount();
				case JsonBoolean	:
					if (left.getBooleanValue() == right.getBooleanValue()) {
						return 0;
					}
					else if (left.getBooleanValue()) {
						return -1;
					}
					else {
						return 1;
					}
				case JsonInteger	:
					final long	longResult = right.getLongValue() - left.getLongValue();
					
					return longResult < 0 ? -1 : (longResult > 0 ? 1 : 0);
				case JsonNull		:
					return 0;
				case JsonObject		:
					final Set<String>	leftNames = new HashSet<>(), rightNames = new HashSet<>();
					int					rcObject;
					
					for (JsonNode item : left.children()) {
						if (item.hasName()) {
							leftNames.add(item.getName());
						}
					}
					for (JsonNode item : right.children()) {
						if (item.hasName()) {
							rightNames.add(item.getName());
						}
					}
					leftNames.retainAll(rightNames);
					
					for (String item : leftNames) {
						rcObject = compareJson(left.getChild(item),right.getChild(item));
						
						if (rcObject != 0) {
							return rcObject;
						}
					}
					return right.childrenCount() - left.childrenCount();
				case JsonReal		:
					final double	realResult = right.getDoubleValue() - left.getDoubleValue();
					
					return realResult < 0 ? -1 : (realResult > 0 ? 1 : 0);
				case JsonString		:
					return left.getStringValue().compareTo(right.getStringValue());
				default:
					throw new UnsupportedOperationException("Json node type ["+left.getType()+"] is not supported yet"); 
			}
		}
		else {
			return right.getType().ordinal() - left.getType().ordinal();
		}
	}

	private static Object convertType(final Object source, final Object awaited) {
		try{return SQLUtils.convert(awaited.getClass(),source);
		} catch (ContentException e) {
			return new Object();
		}
	}

	private static void resetStack(final List<Object> innerStack, final int topStack) {
		while (!innerStack.isEmpty() && innerStack.size() > topStack+1) {
			innerStack.remove(innerStack.size()-1);
		}
	}

	private static ComparisonType compareTemplate(final Object[] stack, final Object[] path, final int fromPath, final SyntaxTree[] template, final int fromTemplate) {
		ComparisonType	result;
		
		if (fromTemplate >= template.length) {
			return ComparisonType.TRUE;
		}
		else if (fromPath >= stack.length) {
			return fromPath < template.length ? ComparisonType.POSSIBLY_TRUE : ComparisonType.FALSE;
		}
		else {
			switch ((TemplateType)template[fromTemplate].cargo) {
				case ANY_CHAIN				:
					for (int index = fromPath, maxIndex = stack.length; index < maxIndex; index++) {	// Recursive test
						if ((result = compareTemplate(stack,path,index,template,fromTemplate+1)) == ComparisonType.TRUE) {
							return result;
						}
					}
					return ComparisonType.POSSIBLY_TRUE;
				case ANY_STRUCTURE			:
					if (((JsonNode)stack[fromPath]).getType() == JsonNodeType.JsonObject) {
						return compareTemplate(stack,path,fromPath+1,template,fromTemplate+1);
					}
					else {
						return ComparisonType.FALSE;
					}
				case ANY_NAME				:
					final Pattern	p = (Pattern)template[fromTemplate].children[0].cargo;
					
					if (((JsonNode)stack[fromPath]).hasName() && p.matcher(((JsonNode)stack[fromPath]).getName()).matches()) {
						if (fromTemplate < template.length-1 && (TemplateType)template[fromTemplate+1].cargo == TemplateType.VALUE_FILTER || fromTemplate < template.length-1 && (TemplateType)template[fromTemplate+1].cargo == TemplateType.COND_FILTER) {
							return compareTemplate(stack,path,fromPath,template,fromTemplate+1);
						}
						else {
							return compareTemplate(stack,path,fromPath,template,fromTemplate+1);
						}
					}
					else {
						return ComparisonType.FALSE;
					}
				case ANY_ARRAY				:
					if (((JsonNode)stack[fromPath]).getType() == JsonNodeType.JsonArray) {
						return compareTemplate(stack,path,fromPath+1,template,fromTemplate+1);
					}
					else {
						return ComparisonType.FALSE;
					}
				case ANY_INDEX				:
					if (fromPath > 0 && ((JsonNode)stack[fromPath-1]).getType() == JsonNodeType.JsonArray) {
						return compareTemplate(stack,path,fromPath,template,fromTemplate+1);
					}
					else {
						return ComparisonType.FALSE;
					}
				case ANY_NODE				:
					return compareTemplate(stack,path,fromPath+1,template,fromTemplate+1);
				case ROOT					:
					return compareTemplate(stack,path,0,template,fromTemplate+1);
				case SELECTED_INDEX			:
					final List<Object>	indexStack = new ArrayList<>();
					
					if (isExpressionTrue(template[fromTemplate].children[0],(JsonNode[])stack,path,fromPath,indexStack) && ((Boolean)indexStack.get(0))) {
						return compareTemplate(stack,path,fromPath,template,fromTemplate+1);
					}
					else {
						return ComparisonType.FALSE;
					}
				case VALUE_FILTER			:
					final List<Object>	valueStack = new ArrayList<>();
					
					if (isExpressionTrue(template[fromTemplate].children[0],(JsonNode[])stack,path,fromPath,valueStack) && ((Boolean)valueStack.get(0))) {
						return compareTemplate(stack,path,fromPath-1,template,fromTemplate+1);
					}
					else {
						return ComparisonType.FALSE;
					}
				case COND_FILTER			:
					final List<Object>	condStack = new ArrayList<>();
					
					if (isExpressionTrue(template[fromTemplate].children[0],(JsonNode[])stack,path,fromPath,condStack)) {
						final Object	rc = condStack.get(0);
						
						if (((rc instanceof JsonNode[]) || (rc instanceof Boolean) && ((Boolean)rc))) {
							return compareTemplate(stack,path,fromPath,template,fromTemplate+1);
						}
						else {
							return ComparisonType.FALSE;
						}
					}
					else {
						return ComparisonType.FALSE;
					}
				default	:
					throw new UnsupportedOperationException("Template type ["+template[fromTemplate].cargo+"] is not supported yet");
			}
		}
	}
}
