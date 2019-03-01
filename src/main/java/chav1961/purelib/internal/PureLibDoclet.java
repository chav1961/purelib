package chav1961.purelib.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;

public class PureLibDoclet {
	public static final String	TAG_ABOUT = "about";
	public static final String	TAG_CODESAMPLE = "codeSample";
	public static final String	TAG_KEYWORDS = "keywords";
	public static final String	TAG_THREADSAFED = "threadSafed";
	public static final String	TAG_TREEITEM = "treeItem";

	private static final String	KEY_TREE_TARGET = "treeTarget";
	
	public static void process(final String sourcePath) throws IOException {
		final Process 	process	= new ProcessBuilder().command("javadoc","neo","-sourcepath","src/main/java","-docletpath","target/classes","-doclet","doclet.DocletExample").start();
		
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
		return true;
	}	
	
	public static int optionLength(final String option) {
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
	}	 
}
