package chav1961.purelib.basic;

import java.io.IOException;

import org.junit.jupiter.api.Test;
//import org.junit.Assert;

import chav1961.purelib.basic.exceptions.SyntaxException;

public class XMLBasedParserText {
	enum Type {
		T1, T2
	}

	enum Subtype {
		ST1, ST2
	}

	enum Action {
		A1, A2
	}
	
//	@Test
//	public void prepareTest() throws IOException, SyntaxException {
//		try(final InputStream		is = this.getClass().getResourceAsStream("parsertest.xml")) {
//			final XMLBasedParser	xbp = new XMLBasedParser(is,Type.class,Subtype.class,Action.class);
//			
//			
//			try{new XMLBasedParser(null,Type.class,Subtype.class,Action.class);
//				Assert.fail("Mandatory exception was not detected (1-st argument is null)");
//			} catch (NullPointerException exc) {				
//			}
//			try{new XMLBasedParser(is,null,Subtype.class,Action.class);
//				Assert.fail("Mandatory exception was not detected (2-nd argument is null)");
//			} catch (NullPointerException exc) {				
//			}
//			try{new XMLBasedParser(is,Type.class,null,Action.class);
//				Assert.fail("Mandatory exception was not detected (3-rd argument is null)");
//			} catch (NullPointerException exc) {				
//			}
//			try{new XMLBasedParser(is,Type.class,Subtype.class,null);
//				Assert.fail("Mandatory exception was not detected (4-th argument is null)");
//			} catch (NullPointerException exc) {				
//			}
//		}
//
//		for (String item : new String[]{"invalidparsertest1.xml","invalidparsertest2.xml","invalidparsertest3.xml"}) {
//			try(final InputStream		is = this.getClass().getResourceAsStream(item)) {
//				final XMLBasedParser	xbp = new XMLBasedParser(is,Type.class,Subtype.class,Action.class);
//				
//				Assert.fail("Mandatory exception was not detected (file="+item+")");
//			} catch (SyntaxException exc) {				
//			}
//		}
//	}
//
	@Test
	public void parseTest() throws IOException, SyntaxException {
//		try(final InputStream		is = this.getClass().getResourceAsStream("parsertest.xml")) {
//			final XMLBasedParser	xbp = new XMLBasedParser(is,Type.class,Subtype.class,Action.class);
//		}
	}
}
