package chav1961.purelib.javadoc;

import java.io.InputStream;
import java.io.IOException;
import java.util.HashSet;
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

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.standard.Standard;

public class NewPureLibDoclet extends Standard {
	public static final String	TAG_PREFIX = "jd";

	public static final String	TAG_FIELD = "field";
	public static final String	TAG_PARAMETER = "parameter";
	public static final String	TAG_PACKAGE = "package";
	private static final String TAG_OVERVIEW = "overview";
	public static final String	TAG_ABOUT = "about";
	private static final String TAG_ANNOTATIONPARAM = "parm";
	private static final String TAG_ANNOTATION = "annotation";
	private static final String TAG_ANNOTATIONS = "annotations";
	private static final String TAG_SAMPLE = "sample";
	private static final String TAG_SAMPLES = "samples";
	private static final String TAG_THROW = "exception";
	private static final String TAG_THROWS = "throws";

	public static final String	ATTR_LANG = "lang";
	public static final String	ATTR_NAME = "name";
	public static final String	ATTR_VISIBILITY = "value";
	public static final String	ATTR_TYPE = "type";
	public static final String	ATTR_VALUE = "value";
	public static final String	ATTR_AUTHOR = "author";
	public static final String	ATTR_SINCE = "since";
	public static final String	ATTR_LASTUPDATE = "lastUpdate";
	public static final String	ATTR_BETA = "beta";
	public static final String	ATTR_THREADSAFED = "threadSafed";
	public static final String	ATTR_TREEITEM = "treeItem";
	public static final String	ATTR_KEYWORDS = "keywords";
	public static final String	ATTR_SAMPLES = "samples";
	public static final String	ATTR_REF = "ref";
	public static final String 	ATTR_MODIFIERS = "modifiers";

	public static final String	VALUE_INHERITED = "@Inherited";

	
	public static boolean start(final RootDoc rootDoc) {
		try{final DocumentBuilderFactory 	docFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder 			docBuilder = docFactory.newDocumentBuilder();
			final Document 					doc = docBuilder.newDocument();
		    final Element 					rootElement = doc.createElementNS(TAG_PREFIX,"javadoc");
		    
		    doc.appendChild(rootElement);
			
			buildPackageTree(rootElement,doc,rootDoc);
			for (ClassDoc item : rootDoc.classes()) {
				seekPackage(rootElement,item.containingPackage().name()).appendChild(buildClassDesc(doc,item));
			}
		
			final Transformer 				transformer = TransformerFactory.newInstance().newTransformer();
			final Source 					source = new DOMSource(doc);
			final Result 					output = new StreamResult(System.out);
			
			transformer.transform(source, output);		
		} catch (ParserConfigurationException | TransformerFactoryConfigurationError | TransformerException | DOMException e) {
			e.printStackTrace();
		}
		return true;
	}	
	
	static void buildPackageTree(final Element node, final Document doc, final RootDoc rootDoc) {
		final Set<String>		placed = new HashSet<>();
		
		for (ClassDoc clazz : rootDoc.classes()) {
			final PackageDoc	packageDesc = clazz.containingPackage();
			final String 		packageName = packageDesc.name();
			
			if (!placed.contains(packageName)) {
				placed.add(packageName);
				setPackageAttributes(placePackage(node,doc,packageName),doc,packageDesc);
			}
		}
	}

	private static Element placePackage(final Element node, final Document doc, final String packageName) {
		final int	dotPos;
		
		if ((dotPos = packageName.indexOf('.')) > 0) {
			final String	current = packageName.substring(0,dotPos), tail = packageName.substring(dotPos+1);
			final NodeList	list = node.getChildNodes();
			
			for (int index = 0; index < list.getLength(); index++) {
				final Node	item = list.item(index);
				
				if (TAG_PACKAGE.equals(item.getNodeName()) && current.equals(item.getAttributes().getNamedItem(ATTR_NAME))) {
					return placePackage((Element)item,doc,tail);
				}
			}
		    final Element 	newPackage = doc.createElementNS(TAG_PREFIX,TAG_PACKAGE);
		    
		    newPackage.setAttribute(ATTR_NAME,current);
		    node.appendChild(newPackage);
		    return placePackage(newPackage,doc,tail);
		}
		else {
			final NodeList	list = node.getChildNodes();
			
			for (int index = 0; index < list.getLength(); index++) {
				final Node	item = list.item(index);
				
				if (TAG_PACKAGE.equals(item.getNodeName()) && packageName.equals(item.getAttributes().getNamedItem(ATTR_NAME))) {
					return (Element)item;
				}
			}
		    final Element 	newPackage = doc.createElementNS(TAG_PREFIX,TAG_PACKAGE);
		    
		    newPackage.setAttribute(ATTR_NAME,packageName);
		    node.appendChild(newPackage);
		    return newPackage;
		}
	}

	private static void setPackageAttributes(final Element node, final Document doc, final PackageDoc desc) {
		final String			comment = desc.getRawCommentText();
		final Tag[]				about = desc.tags(TAG_ABOUT);
		final Tag[]				author = desc.tags(ATTR_AUTHOR);
		final Tag[]				since = desc.tags(ATTR_SINCE);
		final Tag[]				lastUpdate = desc.tags(ATTR_LASTUPDATE);
		final Tag[]				treeItem = desc.tags(ATTR_TREEITEM);
		final AnnotationDesc[] 	anno = desc.annotations();
		
		if (comment != null && !comment.isEmpty()) {
			node.appendChild(buildOverview(doc,comment));
		}
		if (about != null && about.length > 0) {
			node.appendChild(buildAbout(doc,about));
		}
		if (anno != null && anno.length > 0) {
			node.appendChild(buildAnnotatedWith(doc,anno));
		}
		if (author != null && author.length > 0) {
			node.setAttribute(ATTR_AUTHOR,author[0].text());
		}
		else {
			node.setAttribute(ATTR_AUTHOR,VALUE_INHERITED);
		}
		if (since != null && since.length > 0) {
			node.setAttribute(ATTR_SINCE,since[0].text());
		}
		else {
			node.setAttribute(ATTR_SINCE,VALUE_INHERITED);
		}
		if (lastUpdate != null && lastUpdate.length > 0) {
			node.setAttribute(ATTR_LASTUPDATE,lastUpdate[0].text());
		}
		if (treeItem != null && treeItem.length > 0) {
			node.setAttribute(ATTR_TREEITEM,treeItem[0].text());
		}
	}

	private static Element seekPackage(final Element node, final String packageName) {
		final int	dotPos;
		
		if ((dotPos = packageName.indexOf('.')) > 0) {
			final String	current = packageName.substring(0,dotPos), tail = packageName.substring(dotPos+1);
			final NodeList	list = node.getChildNodes();
			
			for (int index = 0; index < list.getLength(); index++) {
				final Node	item = list.item(index);
				
				if (TAG_PACKAGE.equals(item.getNodeName()) && current.equals(item.getAttributes().getNamedItem(ATTR_NAME))) {
					return seekPackage((Element)item,tail);
				}
			}
		}
		else {
			final NodeList	list = node.getChildNodes();
			
			for (int index = 0; index < list.getLength(); index++) {
				final Node	item = list.item(index);
				
				if (TAG_PACKAGE.equals(item.getNodeName()) && packageName.equals(item.getAttributes().getNamedItem(ATTR_NAME))) {
					return (Element)item;
				}
			}
		}
		throw new RuntimeException("Package not found!");
	}
	
	
	private static Element buildClassDesc(final Document doc, final ClassDoc desc) {
	    final Element 			newClass = doc.createElementNS(TAG_PREFIX,TAG_PACKAGE);
		final String			comment = desc.getRawCommentText();
		final Tag[]				about = desc.tags(TAG_ABOUT);
		final Tag[]				author = desc.tags(ATTR_AUTHOR);
		final Tag[]				since = desc.tags(ATTR_SINCE);
		final Tag[]				lastUpdate = desc.tags(ATTR_LASTUPDATE);
		final Tag[]				beta = desc.tags(ATTR_LASTUPDATE);
		final Tag[]				threadSafed = desc.tags(ATTR_LASTUPDATE);
		final Tag[]				treeItem = desc.tags(ATTR_TREEITEM);
		final Tag[]				keywords = desc.tags(ATTR_KEYWORDS);
		final Tag[]				samples = desc.tags(ATTR_SAMPLES);
		final AnnotationDesc[] 	anno = desc.annotations();

	    newClass.setAttribute(ATTR_NAME,desc.name());
		if (comment != null && !comment.isEmpty()) {
			newClass.appendChild(buildOverview(doc,comment));
		}
		if (about != null && about.length > 0) {
			newClass.appendChild(buildAbout(doc,about));
		}
		if (anno != null && anno.length > 0) {
			newClass.appendChild(buildAnnotatedWith(doc,anno));
		}

		for (FieldDoc item : desc.fields()) {
			newClass.appendChild(buildFieldDesc(doc,item));
		}
		for (MethodDoc item : desc.methods()) {
			newClass.appendChild(buildMethodDesc(doc,item));
		}
		for (ConstructorDoc item : desc.constructors()) {
			newClass.appendChild(buildConstructorDesc(doc,item));
		}
		
		if (samples != null && samples.length > 0) {
			newClass.appendChild(buildSamples(doc,samples));
		}
		
		if (author != null && author.length > 0) {
			newClass.setAttribute(ATTR_AUTHOR,author[0].text());
		}
		else {
			newClass.setAttribute(ATTR_AUTHOR,VALUE_INHERITED);
		}
		if (since != null && since.length > 0) {
			newClass.setAttribute(ATTR_SINCE,since[0].text());
		}
		else {
			newClass.setAttribute(ATTR_SINCE,VALUE_INHERITED);
		}
		if (lastUpdate != null && lastUpdate.length > 0) {
			newClass.setAttribute(ATTR_LASTUPDATE,lastUpdate[0].text());
		}
		if (beta != null && beta.length > 0) {
			newClass.setAttribute(ATTR_BETA,"true");
		}
		if (threadSafed != null && threadSafed.length > 0) {
			newClass.setAttribute(ATTR_THREADSAFED,"true");
		}
		if (treeItem != null && treeItem.length > 0) {
			newClass.setAttribute(ATTR_TREEITEM,treeItem[0].text());
		}
		if (keywords != null && keywords.length > 0) {
			newClass.setAttribute(ATTR_KEYWORDS,keywords[0].text());
		}
		
		return newClass;
	}

	private static Node buildFieldDesc(final Document doc, final FieldDoc desc) {
	    final Element 			newField = doc.createElementNS(TAG_PREFIX,TAG_FIELD);
		final String			comment = desc.getRawCommentText();
		final String			type = desc.type().toString();
		final String			modifiers = desc.modifiers();
		final String			visibility = buildVisibility(desc);
		final Tag[]				about = desc.tags(TAG_ABOUT);
		final Tag[]				since = desc.tags(ATTR_SINCE);
		final Tag[]				lastUpdate = desc.tags(ATTR_LASTUPDATE);
		final Tag[]				keywords = desc.tags(ATTR_KEYWORDS);
		final AnnotationDesc[] 	anno = desc.annotations();

	    newField.setAttribute(ATTR_NAME,desc.name());
		if (comment != null && !comment.isEmpty()) {
			newField.appendChild(buildOverview(doc,comment));
		}
		if (about != null && about.length > 0) {
			newField.appendChild(buildAbout(doc,about));
		}
		if (anno != null && anno.length > 0) {
			newField.appendChild(buildAnnotatedWith(doc,anno));
		}
		
	    newField.setAttribute(ATTR_VISIBILITY,visibility);
	    newField.setAttribute(ATTR_TYPE,type);
	    newField.setAttribute(ATTR_MODIFIERS,modifiers);
		if (since != null && since.length > 0) {
			newField.setAttribute(ATTR_SINCE,since[0].text());
		}
		else {
			newField.setAttribute(ATTR_SINCE,VALUE_INHERITED);
		}
		if (lastUpdate != null && lastUpdate.length > 0) {
			newField.setAttribute(ATTR_LASTUPDATE,lastUpdate[0].text());
		}
		if (keywords != null && keywords.length > 0) {
			newField.setAttribute(ATTR_KEYWORDS,keywords[0].text());
		}
		
		return newField;
	}

	private static Node buildMethodDesc(final Document doc, final MethodDoc desc) {
	    final Element 			newMethod = doc.createElementNS(TAG_PREFIX,TAG_FIELD);
		final String			comment = desc.getRawCommentText();
		final String			type = desc.returnType().toString();
		final String			modifiers = desc.modifiers();
		final String			visibility = buildVisibility(desc);
		final ClassDoc[] 		throwsList = desc.thrownExceptions();
		final Tag[]				about = desc.tags(TAG_ABOUT);
		final Tag[]				since = desc.tags(ATTR_SINCE);
		final Tag[]				lastUpdate = desc.tags(ATTR_LASTUPDATE);
		final Tag[]				keywords = desc.tags(ATTR_KEYWORDS);
		final Tag[]				samples = desc.tags(ATTR_SAMPLES);
		final AnnotationDesc[] 	anno = desc.annotations();

		newMethod.setAttribute(ATTR_NAME,desc.name());
		if (comment != null && !comment.isEmpty()) {
			newMethod.appendChild(buildOverview(doc,comment));
		}
		if (about != null && about.length > 0) {
			newMethod.appendChild(buildAbout(doc,about));
		}
		if (anno != null && anno.length > 0) {
			newMethod.appendChild(buildAnnotatedWith(doc,anno));
		}

		for (Parameter item : desc.parameters()) {
			newMethod.appendChild(buildParameter(doc,item));
		}
		if (throwsList != null && throwsList.length > 0) {
			newMethod.appendChild(buildThrows(doc,throwsList));
		}
		if (samples != null && samples.length > 0) {
			newMethod.appendChild(buildSamples(doc,samples));
		}
		
		newMethod.setAttribute(ATTR_VISIBILITY,visibility);
		newMethod.setAttribute(ATTR_TYPE,type);
		newMethod.setAttribute(ATTR_MODIFIERS,modifiers);
		if (since != null && since.length > 0) {
			newMethod.setAttribute(ATTR_SINCE,since[0].text());
		}
		else {
			newMethod.setAttribute(ATTR_SINCE,VALUE_INHERITED);
		}
		if (lastUpdate != null && lastUpdate.length > 0) {
			newMethod.setAttribute(ATTR_LASTUPDATE,lastUpdate[0].text());
		}
		if (keywords != null && keywords.length > 0) {
			newMethod.setAttribute(ATTR_KEYWORDS,keywords[0].text());
		}
		
		return newMethod;
	}

	private static Node buildConstructorDesc(final Document doc, final ConstructorDoc desc) {
	    final Element 			newConstructor = doc.createElementNS(TAG_PREFIX,TAG_FIELD);
		final String			comment = desc.getRawCommentText();
		final String			visibility = buildVisibility(desc);
		final ClassDoc[] 		throwsList = desc.thrownExceptions();
		final Tag[]				about = desc.tags(TAG_ABOUT);
		final Tag[]				since = desc.tags(ATTR_SINCE);
		final Tag[]				lastUpdate = desc.tags(ATTR_LASTUPDATE);
		final Tag[]				keywords = desc.tags(ATTR_KEYWORDS);
		final Tag[]				samples = desc.tags(ATTR_SAMPLES);
		final AnnotationDesc[] 	anno = desc.annotations();

		newConstructor.setAttribute(ATTR_NAME,desc.name());
		if (comment != null && !comment.isEmpty()) {
			newConstructor.appendChild(buildOverview(doc,comment));
		}
		if (about != null && about.length > 0) {
			newConstructor.appendChild(buildAbout(doc,about));
		}
		if (anno != null && anno.length > 0) {
			newConstructor.appendChild(buildAnnotatedWith(doc,anno));
		}

		for (Parameter item : desc.parameters()) {
			newConstructor.appendChild(buildParameter(doc,item));
		}
		if (throwsList != null && throwsList.length > 0) {
			newConstructor.appendChild(buildThrows(doc,throwsList));
		}
		if (samples != null && samples.length > 0) {
			newConstructor.appendChild(buildSamples(doc,samples));
		}
		
		newConstructor.setAttribute(ATTR_VISIBILITY,visibility);
		if (since != null && since.length > 0) {
			newConstructor.setAttribute(ATTR_SINCE,since[0].text());
		}
		else {
			newConstructor.setAttribute(ATTR_SINCE,VALUE_INHERITED);
		}
		if (lastUpdate != null && lastUpdate.length > 0) {
			newConstructor.setAttribute(ATTR_LASTUPDATE,lastUpdate[0].text());
		}
		if (keywords != null && keywords.length > 0) {
			newConstructor.setAttribute(ATTR_KEYWORDS,keywords[0].text());
		}
		
		return newConstructor;
	}

	private static Node buildParameter(final Document doc, final Parameter desc) {
	    final Element 			newField = doc.createElementNS(TAG_PREFIX,TAG_PARAMETER);
		final String			type = desc.type().toString();
		final AnnotationDesc[] 	anno = desc.annotations();

	    newField.setAttribute(ATTR_NAME,desc.name());
		if (anno != null && anno.length > 0) {
			newField.appendChild(buildAnnotatedWith(doc,anno));
		}
		
	    newField.setAttribute(ATTR_TYPE,type);
		return newField;
	}

	private static Node buildThrows(final Document doc, final ClassDoc[] throwsList) {
	    final Element 		container = doc.createElementNS(TAG_PREFIX,TAG_THROWS);
		
	    for (ClassDoc item : throwsList) {
		    final Element 	exception = doc.createElementNS(TAG_PREFIX,TAG_THROW);
	    	
		    exception.setAttribute(ATTR_NAME,item.name());
		    container.appendChild(exception);
	    }
	    return container;
	}

	private static Node buildOverview(final Document doc, final String comment) {
	    final Element 	overview = doc.createElementNS(TAG_PREFIX,TAG_OVERVIEW);
		
	    overview.setTextContent(comment);
	    overview.setAttribute(ATTR_LANG, "ru");
	    return overview;
	}

	private static Node buildAbout(final Document doc, final Tag[] aboutTags) {
	    final Element 		about = doc.createElementNS(TAG_PREFIX,TAG_ABOUT);
		
	    for (Tag item : aboutTags) {
	    	about.setAttribute(ATTR_REF,item.text());
		    break;
	    }
	    return about;
	}

	private static Node buildAnnotatedWith(final Document doc, final AnnotationDesc[] annotations) {
	    final Element 		container = doc.createElementNS(TAG_PREFIX,TAG_ANNOTATIONS);
		
	    for (AnnotationDesc item : annotations) {
		    final Element 	annotation = doc.createElementNS(TAG_PREFIX,TAG_ANNOTATION);
	    	
		    for (ElementValuePair itemParm : item.elementValues()) {
			    final Element 	parm = doc.createElementNS(TAG_PREFIX,TAG_ANNOTATIONPARAM);
		    	
			    parm.setAttribute(ATTR_NAME,itemParm.element().name());
			    parm.setAttribute(ATTR_VALUE,itemParm.value().toString());
		    }
		    annotation.setAttribute(ATTR_NAME,item.annotationType().name());
		    container.appendChild(annotation);
	    }
	    return container;
	}
	
	private static Node buildSamples(final Document doc, final Tag[] samples) {
	    final Element 		container = doc.createElementNS(TAG_PREFIX,TAG_SAMPLES);
		
	    for (Tag item : samples) {
		    final Element 	ref = doc.createElementNS(TAG_PREFIX,TAG_SAMPLE);
	    	
		    ref.setAttribute(ATTR_REF,item.text());
		    container.appendChild(ref);
	    }
	    return container;
	}

	private static String buildVisibility(final MemberDoc member) {
		if (member.isPublic()) {
			return "public";
		}
		else if (member.isProtected()) {
			return "protected";
		}
		else if (member.isPrivate()) {
			return "private";
		}
		else {
			return "";
		}
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

	public static void main(final String[] args) throws IOException, ParserConfigurationException {
		process("chav1961.purelib.basic");
	}
}
