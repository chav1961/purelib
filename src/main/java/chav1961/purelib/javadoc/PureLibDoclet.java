package chav1961.purelib.javadoc;


import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.standard.Standard;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public class PureLibDoclet extends Standard {
	public static final String	TAGNAME_ABOUT = "about";
	public static final String	TAGNAME_CODESAMPLE = "codeSample";
	public static final String	TAGNAME_KEYWORDS = "keywords";
	public static final String	TAGNAME_THREADSAFED = "threadSafed";
	public static final String	TAGNAME_TREEITEM = "treeItem";
	
	public static final String	TAG_PREFIX = "nav";
	public static final String	TAG_PACKAGE = "package";
	public static final String	TAG_CLASS = "class";
	public static final String	TAG_METHOD = "method";
	public static final String	TAG_FIELD = "field";
	public static final String	TAG_PARAMETER = "parameter";
	public static final String	TAG_OVERVIEW = "overview";

	public static final String	ATTR_NAME = "name";
	public static final String	ATTR_SINCE = "since";
	public static final String	ATTR_KEYWORDS = "keywords";
	public static final String	ATTR_MODIFIERS = "modifiers";
	public static final String	ATTR_KINDOF = "kindof";
	public static final String	ATTR_SAMPLES = "samples";
	public static final String	ATTR_EXTENDS = "extends";
	public static final String	ATTR_IMPLEMENTS = "implements";
	public static final String	ATTR_INSIDE = "inside";
	public static final String	ATTR_TYPE = "type";
	public static final String	ATTRVALUE_INHERITED = "@inherited";

	public enum ClassType {
		CT_CLASS, CT_INTERFACE, CT_ENUM, CT_ANNOTATION, CT_PACKAGE
	}
	
	public static void process(final String sourceLocation) throws IOException {
		final Process 	process	= new ProcessBuilder().command("javadoc",sourceLocation,"-sourcepath","src/main/java","@./src/main/java/chav1961/purelib/internal/doclet.configuration").start();
		
		new Thread(()->{
			final InputStream	is = process.getInputStream();
			final byte[]		content = new byte[100];
			int		len;

			try{while ((len = is.read(content)) > 0) {
					System.out.write(content,0,len);
				}
			} catch (IOException e) {
			}
		}).start();
		new Thread(()->{
			final InputStream	is = process.getErrorStream();
			final byte[]		content = new byte[100];
			int		len;

			try{while ((len = is.read(content)) > 0) {
					System.err.write(content,0,len);
				}
			} catch (IOException e) {
			}
		}).start();
	}
	
	public static boolean start(final RootDoc rootDoc) {
		try{final DocumentBuilderFactory 		docFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder 				docBuilder = docFactory.newDocumentBuilder();
			final Document 						doc = docBuilder.newDocument();
			final SyntaxTreeInterface<char[]>	availableClasses = new AndOrTree<>();

		    final Element 	rootElement = doc.createElementNS(TAG_PREFIX,"navigation");
		    final Attr 		rootAttr = doc.createAttributeNS("","overview");
		    
		    rootAttr.setValue("schema:/");		    
		    doc.appendChild(rootElement);
			
			buildPackageTree(rootElement,doc,collectAvailablePackages(rootDoc));
			buildClassesList(rootDoc,availableClasses);
			
			for(ClassDoc classDoc: rootDoc.classes()) {
				seekPackage(rootElement,classDoc.containingPackage().name()).appendChild(buildClassSubtree(doc,classDoc,availableClasses));
			}
		
			final Transformer 	transformer = TransformerFactory.newInstance().newTransformer();
			final Source 		source = new DOMSource(doc);
			final Result 		output = new StreamResult(System.out);
			
			transformer.transform(source, output);		
		} catch (ParserConfigurationException | TransformerFactoryConfigurationError | TransformerException | SyntaxException | DOMException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    //root elements
		
		return true;
	}	
	
	static Set<String> collectAvailablePackages(final RootDoc rootDoc) {
		final Set<String>	packages = new HashSet<>();
		
		for (ClassDoc clazz : rootDoc.classes()) {
			packages.add(clazz.containingPackage().name());
		}
		return packages;
	}

	static void buildPackageTree(final Element node, final Document doc, final Set<String> packages) {
		final Map<String,Set<String>>	subpackages = new HashMap<>();
		
		for (String item : packages) {
			final String	head, tail;
			final int		index;
			
			if ((index = item.indexOf('.')) == -1) {
				head = item;
				tail = null;
			}
			else {
				head = item.substring(0,index);
				tail = item.substring(index+1);
			}
			if (!subpackages.containsKey(head)) {
				subpackages.put(head,new HashSet<>());
			}
			if (tail != null) {
				subpackages.get(head).add(tail);
			}
		}
		for (Entry<String, Set<String>> entity : subpackages.entrySet()) {
			final Element	item = doc.createElementNS(TAG_PREFIX,TAG_PACKAGE);
		    
		    item.setAttribute(ATTR_NAME, entity.getKey());
		    if (entity.getValue().size() > 0) {
		    	buildPackageTree(item,doc,entity.getValue());
		    }
		    node.appendChild(item);			
		}
	}

	static Element seekPackage(final Element root, final String packageName) {
		if (root == null) {
			throw new NullPointerException("Node can't be null");
		}
		else if (packageName == null || packageName.isEmpty()) {
			throw new IllegalArgumentException("Package name can't be null or empty");
		}
		else {
			final NodeList 	list = root.getChildNodes();
			
			for (int index = 0, maxIndex = list.getLength(); index < maxIndex; index++) {
				final Element	item = (Element)list.item(index);
				final Element	result = seekPackageInternal(item, packageName); 

				if (result != null) {
					return result;
				}
			}
			throw new IllegalArgumentException("Package ["+packageName+"] not found in the package tree"); 
		}
	}

	private static Element seekPackageInternal(final Element node, final String name) {
		final int		dot = name.indexOf('.');
		
		if (dot == -1) {
			if (node.getAttribute(ATTR_NAME).equals(name)) {
				return node;
			}
			else {
				return null;
			}
		}
		else if (node.getAttribute(ATTR_NAME).equals(name.substring(0,dot))) {
			final NodeList 	list = node.getChildNodes();
			final String	tail = name.substring(dot+1);
			
			for (int index = 0, maxIndex = list.getLength(); index < maxIndex; index++) {
				final Element	found = seekPackageInternal((Element)list.item(index),tail);
				
				if (found != null) {
					return found;
				}
			}
			return null;
		}
		else {
			return null;
		}
	}
	
	static Node buildFieldSubtree(final Document doc, final FieldDoc fieldDoc, final  SyntaxTreeInterface<char[]> availableClasses) throws SyntaxException, DOMException, IOException {	
		final Element	result = doc.createElementNS(TAG_PREFIX,TAG_FIELD);
		
		result.setAttribute(ATTR_NAME,fieldDoc.name());
		if (fieldDoc.tags(TAGNAME_KEYWORDS).length > 0) {
			result.setAttribute(ATTR_KEYWORDS,extractTagsBody(fieldDoc.tags(TAGNAME_KEYWORDS)));
		}
		if (fieldDoc.tags(ATTR_SINCE).length > 0) {
			result.setAttribute(ATTR_SINCE,extractTagsBody(fieldDoc.tags(ATTR_SINCE)));
		}
		else {
			result.setAttribute(ATTR_SINCE,ATTRVALUE_INHERITED);
		}
		result.setAttribute(ATTR_TYPE,fieldDoc.type().toString());
		result.setAttribute(ATTR_MODIFIERS,fieldDoc.modifiers().toString());
		result.appendChild(buildOverview(doc,DocletUtils.javadoc2Creole(fieldDoc.getRawCommentText(),availableClasses)[0].content));
		return result;
	}
	
	static Element buildMethodSubtree(final Document doc, final MethodDoc methodDoc, final  SyntaxTreeInterface<char[]> availableClasses) throws SyntaxException, DOMException, IOException {
		final Element	result = doc.createElementNS(TAG_PREFIX,TAG_METHOD);
		
		result.setAttribute(ATTR_NAME,methodDoc.name());
		if (methodDoc.tags(TAGNAME_KEYWORDS).length > 0) {
			result.setAttribute(ATTR_KEYWORDS,extractTagsBody(methodDoc.tags(TAGNAME_KEYWORDS)));
		}
		if (methodDoc.tags(ATTR_SINCE).length > 0) {
			result.setAttribute(ATTR_SINCE,extractTagsBody(methodDoc.tags(ATTR_SINCE)));
		}
		else {
			result.setAttribute(ATTR_SINCE,ATTRVALUE_INHERITED);
		}
		result.setAttribute(ATTR_TYPE,methodDoc.returnType().toString());
		result.setAttribute(ATTR_MODIFIERS,methodDoc.modifiers().toString());
		result.appendChild(buildOverview(doc,DocletUtils.javadoc2Creole(methodDoc.getRawCommentText(),availableClasses)[0].content));
		
		for (Parameter item : methodDoc.parameters()) {
			final Element	parm = doc.createElementNS("nav",TAG_PARAMETER);
			
			parm.setAttribute(ATTR_NAME,item.name());
			parm.setAttribute(ATTR_TYPE,item.type().toString());
			result.appendChild(parm);
		}
		return result;
	}
	
	static Element buildClassSubtree(final Document doc, final ClassDoc classDoc, final  SyntaxTreeInterface<char[]> availableClasses) throws SyntaxException, DOMException, IOException {
		final Element	result = doc.createElementNS(TAG_PREFIX,TAG_CLASS);

		result.setAttribute(ATTR_NAME,classDoc.name());
		result.setAttribute(ATTR_TYPE,classDoc.qualifiedName());
		if (classDoc.tags(TAGNAME_KEYWORDS).length > 0) {
			result.setAttribute(ATTR_KEYWORDS,extractTagsBody(classDoc.tags(TAGNAME_KEYWORDS)));
		}
		if (classDoc.tags(ATTR_SINCE).length > 0) {
			result.setAttribute(ATTR_SINCE,extractTagsBody(classDoc.tags(ATTR_SINCE)));
		}
		else {
			result.setAttribute(ATTR_SINCE,ATTRVALUE_INHERITED);
		}
		if (classDoc.tags(TAGNAME_CODESAMPLE).length > 0) {
			result.setAttribute(ATTR_SAMPLES,Arrays.toString(classDoc.tags(TAGNAME_CODESAMPLE)));
		}
		if (classDoc.superclass() != null) {
			result.setAttribute(ATTR_EXTENDS,classDoc.superclass().toString());
		}
		if (classDoc.interfaceTypes().length > 0) {
			result.setAttribute(ATTR_IMPLEMENTS,Arrays.toString(classDoc.interfaceTypes()));
		}
		result.setAttribute(ATTR_MODIFIERS,classDoc.modifiers().toString());
		result.setAttribute(ATTR_KINDOF,detectKindOf(classDoc).name());
		if (classDoc.containingClass() != null) {
			result.setAttribute(ATTR_INSIDE,classDoc.containingClass().name());
		}
		result.appendChild(buildOverview(doc,DocletUtils.javadoc2Creole(classDoc.getRawCommentText(),availableClasses)[0].content));
		
		for (FieldDoc fieldDoc : classDoc.fields()) {
			result.appendChild(buildFieldSubtree(doc,fieldDoc,availableClasses));
		}
		for (MethodDoc methodDoc : classDoc.methods()) {
			result.appendChild(buildMethodSubtree(doc,methodDoc,availableClasses));
		}
		return result;
	}

	@FunctionalInterface
	interface WalkCallback {
		ContinueMode process(NodeEnterMode mode, Element node);
	}
	
	static ContinueMode walk(final Element node, final WalkCallback callback) {
		if (node == null) {
			throw new NullPointerException("Node can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else {
			return walkInternal(node,callback);
		}
	}

	static String extractTagsBody(final Tag[] tags) {
		final StringBuilder	sb = new StringBuilder();
		
		for (Tag item : tags) {
			final String	stringItem = item.toString();
			final int		index = stringItem.indexOf(':');
			
			if (index == -1) {
				sb.append(';').append(stringItem);
			}
			else {
				sb.append(';').append(stringItem,index+1,stringItem.length());
			}
		}
		return sb.delete(0,1).toString();
	}

	private static ContinueMode walkInternal(final Element node, final WalkCallback callback) {
		switch (callback.process(NodeEnterMode.ENTER,node)) {
			case CONTINUE	:
				final NodeList 	list = node.getChildNodes();
					
loop:			for (int index = 0, maxIndex = list.getLength(); index < maxIndex; index++) {
					switch (walk((Element)list.item(index),callback)) {
						case CONTINUE :
							break;
						case SKIP_CHILDREN :
							break loop;
						case SKIP_SIBLINGS :
							callback.process(NodeEnterMode.EXIT,node);
							return ContinueMode.SKIP_CHILDREN; 
						case SKIP_PARENT : case PARENT_ONLY : case SIBLINGS_ONLY :
							break loop;
						case STOP :
							callback.process(NodeEnterMode.EXIT,node);
							return ContinueMode.STOP; 
						default:
							break;
					}
				}
			case SKIP_CHILDREN : 
				callback.process(NodeEnterMode.EXIT,node);
				return ContinueMode.CONTINUE;
			case SKIP_SIBLINGS	:
				callback.process(NodeEnterMode.EXIT,node);
				return ContinueMode.SKIP_CHILDREN;
			case STOP			:
				callback.process(NodeEnterMode.EXIT,node);
				return ContinueMode.STOP; 
			case PARENT_ONLY : case SIBLINGS_ONLY : case SKIP_PARENT :
				return callback.process(NodeEnterMode.EXIT,node);
			default :
				throw new UnsupportedOperationException("");
		}
	}

	private static ClassType detectKindOf(final ClassDoc doc) {
		if (doc.isAnnotationType()) {
			return ClassType.CT_ANNOTATION;
		}
		else if (doc.isClass()) {
			return ClassType.CT_CLASS;
		}
		else if (doc.isEnum()) {
			return ClassType.CT_ENUM;
		}
		else if (doc.isInterface()) {
			return ClassType.CT_INTERFACE;
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	private static Element buildOverview(final Document doc, final char[] content) {
		final Element	overview = doc.createElementNS("",TAG_OVERVIEW);
		
		overview.setTextContent(new String(content));
		return overview;
	}

	private static void buildClassesList(final RootDoc rootDoc, final SyntaxTreeInterface<char[]> availableClasses) {
		for (ClassDoc clazz : rootDoc.classes()) {
			final String	name = clazz.name();
			final int		lastDot = name.lastIndexOf('.');
			
			availableClasses.placeOrChangeName(lastDot >= 0 ? name.substring(lastDot+1) : name, ("./"+name).replace('.','/').toCharArray());
		}
	}
	
/*	public static int optionLength(final String option) {
		switch (option) {
			case KEY_TREE_TARGET	:
				return 2;
			default :
				return 0;
		}
    }	
	 
	public static boolean validOptions(final String options[][], final DocErrorReporter reporter) {
		boolean success = true;
		
		for (String[] item : options) {
			switch (item[0]) {
				case KEY_TREE_TARGET	:
					break;
				default :
					break;
			}
		}
		if (!success) {
			reporter.printError("Usage: javadoc -tag mytag -doclet ListTags ...");
		}
		return success;
	}*/
	
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		process("chav1961.purelib.basic");
	}
}
