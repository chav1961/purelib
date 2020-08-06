package chav1961.purelib.json;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.json.JsonUtils.ArrayRoot;
import chav1961.purelib.json.JsonUtils.Lexema;
import chav1961.purelib.json.JsonUtils.LexemaType;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class JsonNodeAndUtilsTest {

	@Test
	public void basicNodeTest() {
		// null node
		JsonNode	node = new JsonNode(), node2 = new JsonNode();
		
		Assert.assertEquals(JsonNodeType.JsonNull,node.getType());
		Assert.assertEquals(node2,node);
		Assert.assertEquals(node2.hashCode(),node.hashCode());
		Assert.assertEquals(node2.toString(),node.toString());
		Assert.assertFalse(node.hasName());
		
		node.setName("test");
		Assert.assertTrue(node.hasName());
		Assert.assertEquals("test",node.getName());

		try {node.setName(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {node.setName("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		node.removeName();
		Assert.assertFalse(node.hasName());
		Assert.assertNull(node.getName());
		
		try {node.childrenCount();
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}
		try {node.children();
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}
		try {node.addChild(new JsonNode());
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}
		try {node.setChild(0,new JsonNode());
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}
		try {node.removeChild(0);
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}
		try {node.getChild(0);
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}
		
		// boolean node
		node = new JsonNode(true);
		Assert.assertEquals(JsonNodeType.JsonBoolean,node.getType());
		Assert.assertTrue(node.getBooleanValue());
		
		try {node.getStringValue();
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}
		
		Assert.assertFalse(node.setValue(false).getBooleanValue());

		try {node.setValue("test");
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}
		
		// integer node
		node = new JsonNode(100);
		Assert.assertEquals(JsonNodeType.JsonInteger,node.getType());
		Assert.assertEquals(100,node.getLongValue());
		
		try {node.getBooleanValue();
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}
		
		Assert.assertEquals(200,node.setValue(200).getLongValue());

		try {node.setValue(true);
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}

		// real node
		node = new JsonNode(100.0);
		Assert.assertEquals(JsonNodeType.JsonReal,node.getType());
		Assert.assertEquals(100.0,node.getDoubleValue(),0.0001);
		
		try {node.getLongValue();
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}
		
		Assert.assertEquals(200.0,node.setValue(200.0).getDoubleValue(),0.0001);

		try {node.setValue(100);
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}
		
		// string node
		node = new JsonNode("test");
		Assert.assertEquals(JsonNodeType.JsonString,node.getType());
		Assert.assertEquals("test",node.getStringValue());
		
		try {node.getDoubleValue();
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}
		
		Assert.assertEquals("newtest",node.setValue("newtest").getStringValue());

		try {node.setValue(100.0);
			Assert.fail("Mandatory exception was not detected (incompatible value type and method call)");
		} catch (IllegalStateException exc) {
		}

		// array node
		node = new JsonNode(JsonNodeType.JsonArray,new JsonNode(100),new JsonNode(200));
		Assert.assertEquals(JsonNodeType.JsonArray,node.getType());
		Assert.assertEquals(2,node.childrenCount());
		Assert.assertArrayEquals(new JsonNode[] {new JsonNode(100),new JsonNode(200)},node.children());
		Assert.assertEquals(new JsonNode(200),node.getChild(1));

		try {new JsonNode(null,new JsonNode(100),new JsonNode(200));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JsonNode(JsonNodeType.JsonNull,new JsonNode(100),new JsonNode(200));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try {new JsonNode(JsonNodeType.JsonArray,(JsonNode[])null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {new JsonNode(JsonNodeType.JsonArray,new JsonNode(100),null);
			Assert.fail("Mandatory exception was not detected (nulls inside 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {new JsonNode(JsonNodeType.JsonArray,new JsonNode(100).setName("s"),new JsonNode(200));
			Assert.fail("Mandatory exception was not detected (attempt to add named items into array)");
		} catch (IllegalArgumentException exc) {
		}
		
		node.addChild(new JsonNode(300));
		Assert.assertEquals(3,node.childrenCount());

		try {node.addChild(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {node.addChild(new JsonNode(400).setName("s"));
			Assert.fail("Mandatory exception was not detected (attempt to add named child)");
		} catch (IllegalArgumentException exc) {
		}

		node.removeChild(0);
		Assert.assertEquals(2,node.childrenCount());
		Assert.assertEquals(new JsonNode(200),node.getChild(0));
		
		try {node.removeChild(-1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try {node.removeChild(666);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		node.setChild(0,new JsonNode(1000));
		Assert.assertEquals(new JsonNode(1000),node.getChild(0));

		try {node.setChild(-1,new JsonNode(1000));
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try {node.setChild(666,new JsonNode(1000));
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try {node.setChild(0,new JsonNode(1000).setName("s"));
			Assert.fail("Mandatory exception was not detected (2-nd argument is named item)");
		} catch (IllegalArgumentException exc) {
		}

		// object node
		node = new JsonNode(JsonNodeType.JsonObject,new JsonNode(100).setName("n1"),new JsonNode(200).setName("n2"));
		Assert.assertEquals(JsonNodeType.JsonObject,node.getType());
		Assert.assertEquals(2,node.childrenCount());
		Assert.assertArrayEquals(new JsonNode[] {new JsonNode(100).setName("n1"),new JsonNode(200).setName("n2")},node.children());
		Assert.assertEquals(new JsonNode(200).setName("n2"),node.getChild(1));

		try {new JsonNode(null,new JsonNode(100).setName("n1"),new JsonNode(200).setName("n2"));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JsonNode(JsonNodeType.JsonNull,new JsonNode(100),new JsonNode(200));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try {new JsonNode(JsonNodeType.JsonObject,(JsonNode[])null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {new JsonNode(JsonNodeType.JsonObject,new JsonNode(100).setName("n1"),null);
			Assert.fail("Mandatory exception was not detected (nulls inside 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {new JsonNode(JsonNodeType.JsonObject,new JsonNode(100).setName("s"),new JsonNode(200));
			Assert.fail("Mandatory exception was not detected (attempt to add unnamed items into objecy)");
		} catch (IllegalArgumentException exc) {
		}
		
		node.addChild(new JsonNode(300).setName("n3"));
		Assert.assertEquals(3,node.childrenCount());

		try {node.addChild(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {node.addChild(new JsonNode(400));
			Assert.fail("Mandatory exception was not detected (attempt to add unnamed child)");
		} catch (IllegalArgumentException exc) {
		}
		try {node.addChild(new JsonNode(400).setName("n3"));
			Assert.fail("Mandatory exception was not detected (attempt to add child with duplicate name)");
		} catch (IllegalArgumentException exc) {
		}

		node.removeChild(0);
		Assert.assertEquals(2,node.childrenCount());
		Assert.assertEquals(new JsonNode(200).setName("n2"),node.getChild(0));
		
		try {node.removeChild(-1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try {node.removeChild(666);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		node.setChild(0,new JsonNode(1000).setName("n10"));
		Assert.assertEquals(new JsonNode(1000).setName("n10"),node.getChild(0));

		try {node.setChild(-1,new JsonNode(1000).setName("n10"));
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try {node.setChild(666,new JsonNode(1000).setName("n10"));
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try {node.setChild(0,new JsonNode(1000));
			Assert.fail("Mandatory exception was not detected (2-nd argument is unnamed item)");
		} catch (IllegalArgumentException exc) {
		}
		try {node.setChild(0,new JsonNode(1000).setName("n3"));
			Assert.fail("Mandatory exception was not detected (2-nd argument is duplicate named item)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void loadUnloadTest() throws SyntaxException, IOException, PrintingException {
		JsonNode	node1, node2;
		
		// array test
		node1 = loadJson("[]");
		node2 = loadJson(unloadJson(node1));
		Assert.assertEquals(node1,node2);
		
		node1 = loadJson("[100,false,5.3,null,\"test\"]");
		node2 = loadJson(unloadJson(node1));
		Assert.assertEquals(node1,node2);
		
		node1 = loadJson("[[200],[],{\"name\":10},{}]");
		node2 = loadJson(unloadJson(node1));
		Assert.assertEquals(node1,node2);
		
		// object test
		node1 = loadJson("{}");
		node2 = loadJson(unloadJson(node1));
		Assert.assertEquals(node1,node2);

		node1 = loadJson("{\"a\":100,\"b\":false,\"c\":5.3,\"d\":null,\"e\":\"test\"}");
		node2 = loadJson(unloadJson(node1));
		Assert.assertEquals(node1,node2);
		
		node1 = loadJson("{\"n1\":{\"a\":200},\"n2\":{},\"n3\":[10],\"n4\":[]}");
		node2 = loadJson(unloadJson(node1));
		Assert.assertEquals(node1,node2);
		
		// exceptions
		try{loadJson("100");
			Assert.fail("Mandatory exception was not detected (neither '[' nor '{' in the JSON)");
		} catch (SyntaxException exc) {
		}
		try{loadJson("[");
			Assert.fail("Mandatory exception was not detected (unclosed ']')");
		} catch (SyntaxException exc) {
		}
		try{loadJson("[100 200]");
			Assert.fail("Mandatory exception was not detected (missing ',')");
		} catch (SyntaxException exc) {
		}
		try{loadJson("{");
			Assert.fail("Mandatory exception was not detected (unclosed '}')");
		} catch (SyntaxException exc) {
		}
		try{loadJson("[\"a\":100]");
			Assert.fail("Mandatory exception was not detected (name in the array)");
		} catch (SyntaxException exc) {
		}
		try{loadJson("{\"a\"100}");
			Assert.fail("Mandatory exception was not detected (name without ':')");
		} catch (SyntaxException exc) {
		}
		try{loadJson("{\"a\":100\"b\":200}");
			Assert.fail("Mandatory exception was not detected (missing ',')");
		} catch (SyntaxException exc) {
		}
		try{loadJson("{100}");
			Assert.fail("Mandatory exception was not detected (name missing)");
		} catch (SyntaxException exc) {
		}
		try{loadJson("{\"name\":}");
			Assert.fail("Mandatory exception was not detected (value missing)");
		} catch (SyntaxException exc) {
		}

		try{JsonUtils.loadJsonTree(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{JsonUtils.unloadJsonTree(null,new JsonStaxPrinter(new StringWriter()));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{JsonUtils.unloadJsonTree(node1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void walkingTest() throws IOException, ContentException {
		final JsonNode			root = loadJson("[100,{\"name1\":200,\"name2\":[true,null]}]");
		final List<Object[]>	results = new ArrayList<>();
		final int[]				count = new int[] {0};
		
		results.add(new Object[] {new ArrayRoot()});
		results.add(new Object[] {new ArrayRoot(), Integer.valueOf(0)});
		results.add(new Object[] {new ArrayRoot(), Integer.valueOf(1)});
		results.add(new Object[] {new ArrayRoot(), Integer.valueOf(1), "name1"});
		results.add(new Object[] {new ArrayRoot(), Integer.valueOf(1), new ArrayRoot("name2")});
		results.add(new Object[] {new ArrayRoot(), Integer.valueOf(1), new ArrayRoot("name2"), Integer.valueOf(0)});
		results.add(new Object[] {new ArrayRoot(), Integer.valueOf(1), new ArrayRoot("name2"), Integer.valueOf(1)});
		
		JsonUtils.walkDownJson(root,(mode,node,path)->{
			if (mode == NodeEnterMode.ENTER) {
//				System.err.println("Path="+Arrays.toString(path));
				Assert.assertArrayEquals(results.get(count[0]++),path);
			}
			return ContinueMode.CONTINUE;
		});
		Assert.assertEquals(7,count[0]);
		
		try{JsonUtils.walkDownJson(null,(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{JsonUtils.walkDownJson(root,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void filterLexemasTest() throws IOException, ContentException {
		final List<Lexema>	list = new ArrayList<>();
		
		JsonUtils.buildLexemaList("[]():#~$,/%+-=*?**||&&...././.>>=<<=<>12345\"test\"'{[]}'name123\0".toCharArray(),list);
		final LexemaType[]	lt = new LexemaType[] {LexemaType.OPENB,		LexemaType.CLOSEB,			LexemaType.OPEN, 			LexemaType.CLOSE, 		LexemaType.COLON, 
												   LexemaType.NUMBER,		LexemaType.NOT_OP,			LexemaType.CURRENT_VALUE,	LexemaType.LIST,		LexemaType.DIV_OP,
												   LexemaType.REM_OP,		LexemaType.ADD_OP,			LexemaType.SUB_OP,			LexemaType.CMP_EQ_OP, 	LexemaType.MUL_OP,
												   LexemaType.QUESTIONMARK,	LexemaType.DOUBLEASTERISK,	LexemaType.OR_OP,			LexemaType.AND_OP, 		LexemaType.RANGE,
												   LexemaType.PARENTSLASH,	LexemaType.CURRENTSLASH,	LexemaType.DOT,				LexemaType.CMP_GT_OP,	LexemaType.CMP_GE_OP,
												   LexemaType.CMP_LT_OP,	LexemaType.CMP_LE_OP,		LexemaType.CMP_NE_OP,		LexemaType.INTEGER,		LexemaType.STRING,
												   LexemaType.JSON,			LexemaType.NAME,			LexemaType.EOF
												   };
		for (int index = 0, maxIndex = list.size(); index < maxIndex; index++) {
			Assert.assertEquals(lt[index],list.get(index).type);
		}
		Assert.assertEquals(12345,list.get(28).longVal);
		Assert.assertEquals("test",list.get(29).stringVal);
		Assert.assertEquals("{[]}",list.get(30).stringVal);
		Assert.assertEquals("name123",list.get(31).stringVal);
		
		try{JsonUtils.buildLexemaList("^\0".toCharArray(),list);
			Assert.fail("Mandatory exception was not detected (unknown char)");
		} catch (SyntaxException exc) {
		}
		try{JsonUtils.buildLexemaList("|\0".toCharArray(),list);
			Assert.fail("Mandatory exception was not detected (unknown char)");
		} catch (SyntaxException exc) {
		}
		try{JsonUtils.buildLexemaList("&\0".toCharArray(),list);
			Assert.fail("Mandatory exception was not detected (unknown char)");
		} catch (SyntaxException exc) {
		}
		try{JsonUtils.buildLexemaList("^'\0".toCharArray(),list);
			Assert.fail("Mandatory exception was not detected (unclosed quote)");
		} catch (SyntaxException exc) {
		}
		try{JsonUtils.buildLexemaList("^\"\0".toCharArray(),list);
			Assert.fail("Mandatory exception was not detected (unclosed double quote)");
		} catch (SyntaxException exc) {
		}
	}	
	
	@Test
	public void unconditionalFilterTest() throws IOException, ContentException {
		JsonNode	root;
		final int	count[] = {0};
		
		// Array nodes
		root = loadJson("[100]");
		
		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/",(mode,node,path)->{count[0]++;
//			System.err.println("Call: mode="+mode+", path="+Arrays.toString(path));
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(4,count[0]);
		
		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/*",(mode,node,path)->{count[0]++;
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(4,count[0]);

		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/**",(mode,node,path)->{count[0]++;
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(4,count[0]);
		
		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]",(mode,node,path)->{count[0]++;
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,count[0]);
		
		root = loadJson("{\"x1\":100}");
		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]",(mode,node,path)->{count[0]++; 
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(0,count[0]);

		// Object nodes
		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/*",(mode,node,path)->{count[0]++;
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(4,count[0]);

		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/**",(mode,node,path)->{count[0]++;
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(4,count[0]);

		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x1",(mode,node,path)->{count[0]++;
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,count[0]);

		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x?",(mode,node,path)->{count[0]++;
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,count[0]);

		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x*",(mode,node,path)->{count[0]++;
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,count[0]);

		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/?1",(mode,node,path)->{count[0]++;
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,count[0]);

		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/*1",(mode,node,path)->{count[0]++;
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,count[0]);

		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x2",(mode,node,path)->{count[0]++;
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(0,count[0]);
		
		// Complex test
		root = loadJson("[{\"x1\":100}]");

		count[0] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]/x?",(mode,node,path)->{count[0]++;
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,count[0]);
	}

	@Test
	public void conditionalArrayIndexFilterTest() throws IOException, ContentException {
		JsonNode	root;
		final int	countAndSum[] = {0,0};
		
		root = loadJson("[100,200,300,400,500]");

		// List values: terms
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[0..1,3]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
//			System.err.println("Call: mode="+mode+", path="+Arrays.toString(path));
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*3,countAndSum[0]);
		Assert.assertEquals(2*(100+200+400),countAndSum[1]);

		// List values: unary
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[0..1,+3]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*3,countAndSum[0]);
		Assert.assertEquals(2*(100+200+400),countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[-1..1,3]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*3,countAndSum[0]);
		Assert.assertEquals(2*(100+200+400),countAndSum[1]);

		// List values: nested expressions
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[0..1,(2+1)]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*3,countAndSum[0]);
		Assert.assertEquals(2*(100+200+400),countAndSum[1]);
		
		// List values: syntax errors
		try{JsonUtils.filterOf("/[0..1,]",(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (missing list operand)");
		} catch (SyntaxException exc) {
		}
		try{JsonUtils.filterOf("/[..1,3]",(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (missing range operand)");
		} catch (SyntaxException exc) {
		}
		try{JsonUtils.filterOf("/[0..,3]",(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (missing range operand)");
		} catch (SyntaxException exc) {
		}
		try{JsonUtils.filterOf("/[0..1,(2+1]",(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (missing ')')");
		} catch (SyntaxException exc) {
		}
		
		// Arithmetic: terms
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i = 0]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(2*100,countAndSum[1]);

		// Arithmetic: unary
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has -i = 0]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(2*100,countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has +i = 0]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(2*100,countAndSum[1]);
		
		// Arithmetic: multiplication
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i * 2 > 4]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*2,countAndSum[0]);
		Assert.assertEquals(2*(400+500),countAndSum[1]);
		
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i / 2 = 0]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*2,countAndSum[0]);
		Assert.assertEquals(2*(100+200),countAndSum[1]);
		
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i % 2 = 0]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(6,countAndSum[0]);
		Assert.assertEquals(2*900,countAndSum[1]);

		// Arithmetic: addition
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i + 1 = 1]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(2*(100),countAndSum[1]);
		
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i - 1 = 3]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(2*(500),countAndSum[1]);
		
		// Arithmetic: comparisons
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i < 2]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*2,countAndSum[0]);
		Assert.assertEquals(2*(100+200),countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i <= 2]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*3,countAndSum[0]);
		Assert.assertEquals(2*(100+200+300),countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i > 2]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*2,countAndSum[0]);
		Assert.assertEquals(2*(400+500),countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i >= 2]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*3,countAndSum[0]);
		Assert.assertEquals(2*(300+400+500),countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i = 2]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(2*300,countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i <> 2]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*4,countAndSum[0]);
		Assert.assertEquals(2*(100+200+400+500),countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i in 0..2,4]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*4,countAndSum[0]);
		Assert.assertEquals(2*(100+200+300+500),countAndSum[1]);

		// Arithmetic: NOT, AND, OR
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has ~i = 2]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*4,countAndSum[0]);
		Assert.assertEquals(2*(100+200+400+500),countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i <> 2 && i <> 4]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*3,countAndSum[0]);
		Assert.assertEquals(2*(100+200+400),countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i = 2 || i = 4]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*2,countAndSum[0]);
		Assert.assertEquals(2*(300+500),countAndSum[1]);

		// Arithmetic: nested expression
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has (i + 1) / 2 = 0]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(2*(100),countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[has i in (1+2)..4]",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*2,countAndSum[0]);
		Assert.assertEquals(2*(400+500),countAndSum[1]);

		// Arithmetic:  syntax errors
		try{JsonUtils.filterOf("/[has j = 0]",(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (only 'i' variable is supported)");
		} catch (SyntaxException exc) {
		}
		try{JsonUtils.filterOf("/[has (i + 1 = 0]",(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (missing ')')");
		} catch (SyntaxException exc) {
		}
		try{JsonUtils.filterOf("/[has * i = 0]",(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (missing operand)");
		} catch (SyntaxException exc) {
		}
	}	

	@Test
	public void conditionalListFilterTest() throws IOException, ContentException {
		JsonNode			root;
		final int			countAndSum[] = {0,0};
		final List<Object>	items = new ArrayList<>();
		
		// Array items
		root = loadJson("[100,200,300,400,500]");

		// List values: terms
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]:100",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(2*100,countAndSum[1]);
		
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]:100..200",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(4,countAndSum[0]);
		Assert.assertEquals(2*300,countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]:100,300..500",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(8,countAndSum[0]);
		Assert.assertEquals(2*1300,countAndSum[1]);

		// List values: terms
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]:-100,100",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(2*100,countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]:+100,-200",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(2*100,countAndSum[1]);

		// List values: nested expression
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]:(100+100),300",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*2,countAndSum[0]);
		Assert.assertEquals(2*(200+300),countAndSum[1]);

		// List values: syntax exceptions
		try{JsonUtils.filterOf("/[]:,300",(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (list item missing)");
		} catch (SyntaxException exc) {
		}
		try{JsonUtils.filterOf("/[]:100,",(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (list item missing)");
		} catch (SyntaxException exc) {
		}
		try{JsonUtils.filterOf("/[]:..200,300",(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (range item missing)");
		} catch (SyntaxException exc) {
		}
		try{JsonUtils.filterOf("/[]:100..,300",(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (range item missing)");
		} catch (SyntaxException exc) {
		}
		try{JsonUtils.filterOf("/[]:100..(100+100,300",(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (')' missing)");
		} catch (SyntaxException exc) {
		}
		
		// Object items
		root = loadJson("{\"x\":100,\"y\":200,\"z\":300,\"t\":\"test\",\"flag\":true,\"zero\":null}");
		
		// List values: term
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x:100",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(2*100,countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x:100..200",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(2*100,countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x:100,300..500",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(2*100,countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/t:\"test\",\"another\"",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getStringValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(Arrays.asList("test","test"),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/t:\"test\"..\"test2\",\"another\"",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getStringValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(Arrays.asList("test","test"),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/flag:true",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getBooleanValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(true,true),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/flag:false",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getBooleanValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(0,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(),items);
		
		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/zero:null",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getType() == JsonNodeType.JsonNull);
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(true,true),items);
		
		// List values: syntax exception
		try{JsonUtils.filterOf("/zero:unknown",(mode,node,path)->ContinueMode.CONTINUE);
			Assert.fail("Mandatory exception was not detected (unknown name)");
		} catch (SyntaxException exc) {
		}
	}	

	@Test
	public void conditionalHasFilterTest() throws IOException, ContentException {
		JsonNode			root;
		final int			countAndSum[] = {0,0};
		final List<Object>	items = new ArrayList<>();
		
		// Array items
		root = loadJson("[100,200,300,400,500]");

		// Arithmetic: terms
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]#$ = 300",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(2*300,countAndSum[1]);

		// Arithmetic: unary
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]#-$ + 500= 100",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(2*400,countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]#+$ = 100",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(2*100,countAndSum[1]);
		
		// Arithmetic : templates expression
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]#../[]:300",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*5,countAndSum[0]);
		Assert.assertEquals(2*(100+200+300+400+500),countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]#../[]:600",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*0,countAndSum[0]);
		Assert.assertEquals(2*0,countAndSum[1]);

		// Arithmetic : templates expression and NOT AND OR
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]#(../[]:600 || ../[]:300)",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*5,countAndSum[0]);
		Assert.assertEquals(2*(100+200+300+400+500),countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]#(../[]:600 && ../[]:300)",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*0,countAndSum[0]);
		Assert.assertEquals(2*0,countAndSum[1]);

		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]#(~../[]:600 && ../[]:300)",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*5,countAndSum[0]);
		Assert.assertEquals(2*(100+200+300+400+500),countAndSum[1]);

		// Arithmetic : templates expression and JSON
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]#../ = '[100,200,300,400,500]'",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*5,countAndSum[0]);
		Assert.assertEquals(2*(100+200+300+400+500),countAndSum[1]);
		
		countAndSum[0] = countAndSum[1] = 0;
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/[]#../ <> '[100,200,300]'",(mode,node,path)->{
			countAndSum[0]++;
			countAndSum[1] += node.getLongValue();
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*5,countAndSum[0]);
		Assert.assertEquals(2*(100+200+300+400+500),countAndSum[1]);
		
		// Object items
		root = loadJson("{\"x\":100,\"y\":200,\"z\":300.0,\"t\":\"test\",\"flag\":true,\"zero\":null}");
		
		// Arithmetic: terms
		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/z#$ > 100",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getDoubleValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(300.0,300.0),items);
		
		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/t#$ = \"test\"",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getStringValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(Arrays.asList("test","test"),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/flag#$",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getBooleanValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(true,true),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/flag#~$",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getBooleanValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(0,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/flag#$ = true",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getBooleanValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(true,true),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/flag#$ <> false",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getBooleanValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(true,true),items);
		
		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/zero#$ = null",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getType() == JsonNodeType.JsonNull);
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(true,true),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/zero#($ = null)",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getType() == JsonNodeType.JsonNull);
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2*1,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(true,true),items);

		// Arithmetic : templates expression and JSON
		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x#../ = '{\"x\":100,\"y\":200,\"z\":300.0,\"t\":\"test\",\"flag\":true,\"zero\":null}'",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getLongValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(100L,100L),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x#../ <> '{\"x\":100,\"y\":200,\"z\":300.0,\"t\":\"test\",\"flag\":true}'",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getLongValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(100L,100L),items);

		// Arithmetic : templates expression and ordinal operands
		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x#../x = 100",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getLongValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(100L,100L),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x#../z = 300.0",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getLongValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(100L,100L),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x#../t = \"test\"",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getLongValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(100L,100L),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x#../flag",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getLongValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(100L,100L),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x#../flag = true",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getLongValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(100L,100L),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x#../flag <> false",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getLongValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(100L,100L),items);

		countAndSum[0] = countAndSum[1] = 0;
		items.clear();
		JsonUtils.walkDownJson(root,JsonUtils.filterOf("/x#../zero = null",(mode,node,path)->{
			countAndSum[0]++;
			items.add(node.getLongValue());
			return ContinueMode.CONTINUE;}
		));
		Assert.assertEquals(2,countAndSum[0]);
		Assert.assertEquals(Arrays.asList(100L,100L),items);
	}	
	
	private JsonNode loadJson(final String content) throws IOException, SyntaxException {
		try(final Reader			rdr = new StringReader(content);
			final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
			
			if (parser.hasNext()) {
				parser.next();
			}
			return JsonUtils.loadJsonTree(parser);
		}
	}

	private String unloadJson(final JsonNode root) throws IOException, PrintingException {
		try(final Writer			wr = new StringWriter();
			final JsonStaxPrinter	pr = new JsonStaxPrinter(wr)) {
			
			JsonUtils.unloadJsonTree(root,pr);
			pr.flush();
			return wr.toString();
		}
	}
}
