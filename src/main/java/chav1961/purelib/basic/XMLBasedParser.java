package chav1961.purelib.basic;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.xsd.XSDConst;

class XMLBasedParser<Type extends Enum<?>,Subtype extends Enum<?>,Action extends Enum<?>,T extends XMLBasedParserLex<Type,Subtype>> {
	private static final String		TAG_BODY = "body";
	private static final String		TAG_CHOISES = "choises";
	private static final String		TAG_CHOISE = "choise";
	private static final String		TAG_ACTION = "action";
	private static final String		TAG_COMPARE = "compare";
	private static final String		TAG_OPTIONAL = "optional";
	private static final String		TAG_BUILTIN = "builtin";
	private static final String		TAG_OTHERWISE = "otherwise";

	private static final String		ATTR_TYPE = "type";
	private static final String		ATTR_SUBTYPE = "subtype";
	
	private final Class<Type>		typeClass;
	private final Class<Subtype>	subtypeClass;
	private final Class<Action>		actionClass;
	private ParserNode<Action>		start = new ParserNode<>();
	
	public XMLBasedParser(final InputStream parserDescriptor, final Class<Type> typeClass, final Class<Subtype> subtypeClass, final Class<Action> actionClass) throws SyntaxException {
		if (parserDescriptor == null) {
			throw new NullPointerException("Parser descriptor stream can't be null");
		}
		else if (typeClass == null) {
			throw new NullPointerException("Type class can't be null");
		}
		else if (subtypeClass == null) {
			throw new NullPointerException("Subtype class can't be null");
		}
		else if (actionClass == null) {
			throw new NullPointerException("Action class can't be null");
		}
		else {
			this.typeClass = typeClass;
			this.subtypeClass = subtypeClass;
			this.actionClass = actionClass;
			
			try{final DocumentBuilderFactory 	dbFactory = DocumentBuilderFactory.newInstance();
				
				dbFactory.setNamespaceAware(true);
				dbFactory.setValidating(true);
				dbFactory.setAttribute(XSDConst.SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
				dbFactory.setAttribute(XSDConst.SCHEMA_SOURCE, this.getClass().getResource("XMLBasedParser.xsd").toString());
				
			    final DocumentBuilder 			dBuilder = dbFactory.newDocumentBuilder();
			    
			    dBuilder.setErrorHandler(new ErrorHandler(){
					@Override
					public void error(final SAXParseException exc) throws SAXException {
						XMLBasedParser.this.error(String.format("Line %1$d, col %2$d: %3$s",exc.getLineNumber(),exc.getColumnNumber(),exc.getLocalizedMessage()));
						throw exc;
					}

					@Override
					public void fatalError(final SAXParseException exc) throws SAXException {
						error(exc);
					}

					@Override
					public void warning(final SAXParseException exc) throws SAXException {
						XMLBasedParser.this.warning(String.format("Line %1$d, col %2$d: %3$s",exc.getLineNumber(),exc.getColumnNumber(),exc.getLocalizedMessage()));
					}
				});
			    
				final Document 					doc = dBuilder.parse(parserDescriptor);
				final ParserNode<Action>		terminal = new ParserNode<>();  
				
				doc.getDocumentElement().normalize();			
				buildNodes(doc.getDocumentElement(),start,terminal);
				terminal.mode = ParserNode.MODE_TERMINATE;
			} catch (SAXParseException e) {
				throw new SyntaxException(e.getLineNumber(),e.getColumnNumber(),e.getLocalizedMessage()); 
			} catch (IOException | ParserConfigurationException | SAXException e) {
				throw new SyntaxException(0,0,e.getLocalizedMessage()); 
			} 
		}
	}

	public Object parse(final T[] lexRepo) throws SyntaxException {
		if (lexRepo == null || lexRepo.length == 0) {
			throw new IllegalArgumentException("Lex repo can't be null or empty array");
		}
		else {
			ParserNode<?>	currentNode = start;
			int				current = 0;
			boolean			success;
			Object			content = null;
			
loop:		for (;;) {
				switch (currentNode.mode) {
					case ParserNode.MODE_DUMMY 		:
						currentNode = currentNode.next;
						break;
					case ParserNode.MODE_TERMINATE :
						success = true;
						break loop;
					case ParserNode.MODE_COMPARE	:
						if (currentNode.type == lexRepo[current].type && (currentNode.subtype == null || currentNode.subtype == lexRepo[current].subtype)) {
							current++;
							currentNode = currentNode.next;
							break;
						}
						else {
							error(currentNode.message);
							success = false;
							break loop;
						}
					case ParserNode.MODE_OPTION	:
						if (currentNode.type == lexRepo[current].type && (currentNode.subtype == null || currentNode.subtype == lexRepo[current].subtype)) {
							current++;
							currentNode = currentNode.next;
						}
						else {
							currentNode = currentNode.skip;
						}
						break;
					case ParserNode.MODE_CHOISE	:
						for (int index = 0; index < currentNode.choises.length; index++) {
							if (currentNode.mode == ParserNode.MODE_COMPARE && currentNode.choises[index].type == lexRepo[current].type && (currentNode.choises[index].subtype == null || currentNode.choises[index].subtype == lexRepo[current].subtype)) {
								current++;
								currentNode = currentNode.choises[index];
								continue loop;
							}
							else if (currentNode.mode == ParserNode.MODE_DUMMY) {
								currentNode = currentNode.next;
								break;
							}
						}
						error("No any alternatives");
						success = false;
						break loop;
					case ParserNode.MODE_ACTION	:
						content = action(content,currentNode.action,currentNode.parameters);
						currentNode = currentNode.next;
						break;
					case ParserNode.MODE_BUILTIN	:
						current = builtin(content,currentNode.message,lexRepo,current);
						currentNode = currentNode.next;
						break;
					default : throw new UnsupportedOperationException("Mode ["+currentNode.mode+"] is not supported yet");
				}
			}
			return success ? content : null;
		}
	}

	public <Act> Object action(final Object content, final Act actionType, final String[] parameters) {
		return content;
	}

	public int builtin(final Object content, final String type, final T[] lexList, final int current) {
		return current;
	}

	public void warning(final String message) {
	}
	
	public void error(final String message) {
	}

	@SuppressWarnings("unchecked")
	private void buildNodes(final Element node, final ParserNode<Action> current, final ParserNode<Action> terminal) throws SyntaxException {
		ParserNode<Action>[]		pairs;
		
		switch (node.getTagName()) {
			case TAG_BODY		:
				foreach(node,(currentNode)->{buildNodes(currentNode,current,terminal);});
				break;
			case TAG_CHOISES	:
				final List<ParserNode<Action>>		chains = new ArrayList<>();
				
				foreach(node,(currentNode)->{
						final ParserNode<Action>	temp = new ParserNode<>();
						
						chains.add(temp);
						buildNodes(currentNode,temp,terminal);
					}
				);
				current.mode = ParserNode.MODE_CHOISE;
				current.choises = chains.toArray(new ParserNode<?>[chains.size()]);
				break;
			case TAG_CHOISE		:
				pairs = new ParserNode[]{new ParserNode<Action>(),new ParserNode<Action>()};
				buildComparison(node,current);
				current.mode = ParserNode.MODE_COMPARE;
				current.next = pairs[0];				
				foreach(node,(currentNode)->{
						buildNodes(currentNode,pairs[0],pairs[1]);
						pairs[0] = pairs[1];
						pairs[1] = new ParserNode<>();
					}
				);
				pairs[0].next = terminal;
				break;
			case TAG_ACTION		:
				buildAction(node,current);
				current.mode = ParserNode.MODE_ACTION;
				current.next = terminal;
				break;
			case TAG_COMPARE	:
				buildComparison(node,current);
				current.mode = ParserNode.MODE_COMPARE;
				current.next = terminal;
				break;
			case TAG_OPTIONAL	:
				pairs = new ParserNode[]{new ParserNode<Action>(),new ParserNode<Action>()};
				buildComparison(node,current);
				current.mode = ParserNode.MODE_OPTION;
				current.next = pairs[0];				
				current.skip = terminal;				
				foreach(node,(currentNode)->{
						buildNodes(currentNode,pairs[0],pairs[1]);
						pairs[0] = pairs[1];
						pairs[1] = new ParserNode<>();
					}
				);
				pairs[0].next = terminal;
				break;
			case TAG_BUILTIN	:
				buildBuiltin(node,current);
				current.mode = ParserNode.MODE_BUILTIN;
				current.next = terminal;
				break;
			case TAG_OTHERWISE	:
				pairs = new ParserNode[]{new ParserNode<Action>(),new ParserNode<Action>()};
				current.next = pairs[0];				
				foreach(node,(currentNode)->{
						buildNodes(currentNode,pairs[0],pairs[1]);
						pairs[0] = pairs[1];
						pairs[1] = new ParserNode<>();
					}
				);
				pairs[0].next = terminal;
				break;
			default : throw new UnsupportedOperationException("Tag ["+node.getTagName()+"] is not supported yet");			
		}
	}

	private void buildComparison(final Element node, final ParserNode<?> current) throws SyntaxException {
		final String	type = node.getAttribute(ATTR_TYPE), subtype = node.getAttribute(ATTR_SUBTYPE);
		Enum<?>			typeEnum, subtypeEnum = null;
		
		if (type == null) {
			throw new SyntaxException(0,0,"Tag ["+node.getTagName()+"] - mandatory attribute ["+ATTR_TYPE+"] is missing");
		}
		else {
			try{typeEnum = (Enum<?>)typeClass.getMethod("valueOf",String.class).invoke(null,type);
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException exc) {
				throw new SyntaxException(0,0,"Tag ["+node.getTagName()+"], attribute ["+ATTR_TYPE+"]: illegal value ["+type+"] for the ["+typeClass.getSimpleName()+"] enumeration",exc);
			}
		}
		if (subtype != null && !subtype.isEmpty()) {
			try{subtypeEnum = (Enum<?>)subtypeClass.getMethod("valueOf",String.class).invoke(null,subtype);
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException exc) {
				throw new SyntaxException(0,0,"Tag ["+node.getTagName()+"], attribute ["+ATTR_SUBTYPE+"]: illegal value ["+subtype+"] for the ["+subtypeClass.getSimpleName()+"] enumeration",exc);
			}
		}
		current.type = typeEnum;
		current.subtype = subtypeEnum;
	}

	@SuppressWarnings("unchecked")
	private void buildAction(final Element node, final ParserNode<Action> current) throws SyntaxException {
		final String	action = node.getAttribute(ATTR_TYPE);
		Enum<?>			actionEnum;
		
		if (action == null) {
			throw new SyntaxException(0,0,"Tag ["+node.getTagName()+"] - mandatory attribute ["+ATTR_TYPE+"] is missing");
		}
		else {
			try{actionEnum = (Enum<?>)actionClass.getMethod("valueOf",String.class).invoke(null,action);
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException exc) {
				throw new SyntaxException(0,0,"Tag ["+node.getTagName()+"], attribute ["+ATTR_TYPE+"]: illegal value ["+action+"] for the ["+actionClass.getSimpleName()+"] enumeration",exc);
			}
		}
		current.action = (Action) actionEnum;
		
		final NamedNodeMap 	attr = node.getAttributes();
		final List<String>	parms = new ArrayList<>();
		
		for (int index = 0; index < attr.getLength(); index++) {
			if (!ATTR_TYPE.equals(attr.item(index).getNodeName())) {
				parms.add(attr.item(index).getNodeName());
				parms.add(attr.item(index).getTextContent());
			}
		}
		current.parameters = parms.toArray(new String[parms.size()]);
	}

	private void buildBuiltin(final Element node, final ParserNode<?> current) {
		current.message = node.getAttribute(ATTR_TYPE);
	}
	
	@FunctionalInterface
	private interface NodeProcessor {
		void process(Element item) throws SyntaxException;
	}
	
	protected void foreach(final Element node, final NodeProcessor processor) throws SyntaxException {
		final NodeList	list = node.getChildNodes();
		
		for (int index = 0, maxIndex = list.getLength(); index < maxIndex; index++) {
			if (list.item(index).getNodeType() == Node.ELEMENT_NODE) {
				processor.process((Element)list.item(index));
			}
		}
	}
	
	private static class ParserNode<Action extends Enum<?>> {
		private static final int	MODE_DUMMY = 0;
		private static final int	MODE_TERMINATE = 1;
		private static final int	MODE_COMPARE = 2;
		private static final int	MODE_OPTION = 3;
		private static final int	MODE_CHOISE = 4;
		private static final int	MODE_ACTION = 5;
		private static final int	MODE_BUILTIN = 6;
		
		int				mode = MODE_DUMMY;
		Object			type;
		Object			subtype;
		String			message;
		@SuppressWarnings("rawtypes")
		ParserNode		next, skip;
		@SuppressWarnings("rawtypes")
		ParserNode[]	choises;
		Action			action;
		String[]		parameters;
		
		public ParserNode(){
		}
	}
}
