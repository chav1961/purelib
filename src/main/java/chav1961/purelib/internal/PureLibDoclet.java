package chav1961.purelib.internal;

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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.standard.Standard;

public class PureLibDoclet extends Standard {
	public static final String	TAG_ABOUT = "about";
	public static final String	TAG_CODESAMPLE = "codeSample";
	public static final String	TAG_KEYWORDS = "keywords";
	public static final String	TAG_THREADSAFED = "threadSafed";
	public static final String	TAG_TREEITEM = "treeItem";

	private static final String	KEY_TREE_TARGET = "treeTarget";
	
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
		try{final DocumentBuilderFactory 	docFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder 			docBuilder = docFactory.newDocumentBuilder();
			final Document 					doc = docBuilder.newDocument();

		    final Element 	rootElement = doc.createElementNS("","navigation");
		    final Attr 		rootAttr = doc.createAttributeNS("","overview");
		    
		    rootAttr.setValue("schema:/");		    
		    doc.appendChild(rootElement);
			
			buildPackageTree(rootElement,doc,collectAvailablePackages(rootDoc));
			
			for(ClassDoc classDoc: rootDoc.classes()) {
				System.out.println("Class: "+classDoc.typeName()+", package "+classDoc.containingPackage().name());
				for (MethodDoc methodDoc : classDoc.methods()) {
					System.out.println("\tmethod "+methodDoc.qualifiedName());
					System.out.println("\t       "+Arrays.toString(methodDoc.parameters()));
					System.out.println("\t       "+methodDoc.commentText());
				}
				for (FieldDoc fieldDoc : classDoc.fields()) {
					System.out.println("\tfield "+fieldDoc.qualifiedName());
				}
			}
		
//
//		    Element rootElement = doc.createElement("company");
//		    doc.appendChild(rootElement);
//
//		    //staff elements
//		    Element staff = doc.createElement("Staff");
//		    rootElement.appendChild(staff);
//
//		    //set attribute to staff element
//		    Attr attr = doc.createAttribute("id");
//		    attr.setValue("1");
//		    staff.setAttributeNode(attr);
//
//		    //shorten way
//		    //staff.setAttribute("id", "1");
//
//		    //firstname elements
//		    Element firstname = doc.createElement("firstname");
//		    firstname.appendChild(doc.createTextNode("yong"));
//		    staff.appendChild(firstname);
//
//		    //lastname elements
//		    Element lastname = doc.createElement("lastname");
//		    lastname.appendChild(doc.createTextNode("mook kim"));
//		    staff.appendChild(lastname);
//
//		    //nickname elements
//		    Element nickname = doc.createElement("nickname");
//		    nickname.appendChild(doc.createTextNode("mkyong"));
//		    staff.appendChild(nickname);
//
//		    //salary elements
//		    Element salary = doc.createElement("salary");
//		    salary.appendChild(doc.createTextNode("100000"));
//		    staff.appendChild(salary);		
		
		
			final Transformer 	transformer = TransformerFactory.newInstance().newTransformer();
			final Source 		source = new DOMSource(doc);
			final Result 		output = new StreamResult(System.out);
			
			transformer.transform(source, output);		
		} catch (ParserConfigurationException | TransformerFactoryConfigurationError | TransformerException e) {
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
			final Element	item = doc.createElementNS("","package");
		    final Attr 		rootAttr = doc.createAttributeNS("","name");
		    
		    rootAttr.setValue(entity.getKey());		    
		    if (entity.getValue().size() > 0) {
		    	buildPackageTree(item,doc,entity.getValue());
		    }
		    node.appendChild(item);			
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
	
	public static void main(final String[] args) throws IOException {
		process("chav1961.purelib.basic");
	}
}
