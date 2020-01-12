package chav1961.purelib.streams.char2byte.asm.macro;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.char2byte.asm.macro.BreakCommand;
import chav1961.purelib.streams.char2byte.asm.macro.ChoiseConditionCommand;
import chav1961.purelib.streams.char2byte.asm.macro.ChoiseContainer;
import chav1961.purelib.streams.char2byte.asm.macro.Command;
import chav1961.purelib.streams.char2byte.asm.macro.CommandType;
import chav1961.purelib.streams.char2byte.asm.macro.ContinueCommand;
import chav1961.purelib.streams.char2byte.asm.macro.ElseCommand;
import chav1961.purelib.streams.char2byte.asm.macro.ExitCommand;
import chav1961.purelib.streams.char2byte.asm.macro.ForCommand;
import chav1961.purelib.streams.char2byte.asm.macro.IfConditionCommand;
import chav1961.purelib.streams.char2byte.asm.macro.IfContainer;
import chav1961.purelib.streams.char2byte.asm.macro.MErrorCommand;
import chav1961.purelib.streams.char2byte.asm.macro.MacroCommand;
import chav1961.purelib.streams.char2byte.asm.macro.OtherwiseCommand;
import chav1961.purelib.streams.char2byte.asm.macro.SetCommand;
import chav1961.purelib.streams.char2byte.asm.macro.SubstitutionCommand;
import chav1961.purelib.streams.char2byte.asm.macro.WhileCommand;

public class CommandTest {

	@Test
	public void commandTest() throws SyntaxException {
		final Command	cmd = new Command() {
								@Override CommandType getType() {return CommandType.EXIT;}
								@Override Command processCommand(int lineNo, int begin, char[] data, int from, int to, MacroCommand macro) throws SyntaxException {return this;}
							};
		
		Assert.assertEquals(cmd.append(0,new BreakCommand()),cmd);
		try{cmd.append(0,new BreakCommand());
			Assert.fail("Mandatory exception was not detected (dead code)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void nonParseableCommandTest() throws SyntaxException {
		Command			cmd;
		
		cmd = new IfContainer();
		Assert.assertEquals(cmd.getType(),CommandType.IF);
		Assert.assertEquals(cmd.processCommand(0,0,null,0,0,null),cmd);

		cmd = new ElseCommand();
		Assert.assertEquals(cmd.getType(),CommandType.ELSE);
		Assert.assertEquals(cmd.processCommand(0,0,null,0,0,null),cmd);

		cmd = new BreakCommand();
		Assert.assertEquals(cmd.getType(),CommandType.BREAK);
		Assert.assertEquals(cmd.processCommand(0,0,new char[]{'\n'},0,0,null),cmd);
		Assert.assertNull(((BreakCommand)cmd).getLabel());
		
		cmd = new BreakCommand();
		Assert.assertEquals(cmd.getType(),CommandType.BREAK);
		Assert.assertEquals(cmd.processCommand(0,0,"MYLABEL\n".toCharArray(),0,8,null),cmd);
		Assert.assertArrayEquals(((BreakCommand)cmd).getLabel(),"MYLABEL".toCharArray());
		
		cmd = new ContinueCommand();
		Assert.assertEquals(cmd.getType(),CommandType.CONTINUE);
		Assert.assertEquals(cmd.processCommand(0,0,new char[]{'\n'},0,0,null),cmd);
		Assert.assertNull(((ContinueCommand)cmd).getLabel());
		
		cmd = new ContinueCommand();
		Assert.assertEquals(cmd.getType(),CommandType.CONTINUE);
		Assert.assertEquals(cmd.processCommand(0,0,"MYLABEL\n".toCharArray(),0,8,null),cmd);
		Assert.assertArrayEquals(((ContinueCommand)cmd).getLabel(),"MYLABEL".toCharArray());
		
		cmd = new OtherwiseCommand();
		Assert.assertEquals(cmd.getType(),CommandType.OTHERWISE);
		Assert.assertEquals(cmd.processCommand(0,0,null,0,0,null),cmd);

		cmd = new ExitCommand();
		Assert.assertEquals(cmd.getType(),CommandType.EXIT);
		Assert.assertEquals(cmd.processCommand(0,0,null,0,0,null),cmd);
	}

	@Test
	public void parseableCommandTest() throws SyntaxException {
		final MacroCommand		macro = new MacroCommand("MACRO".toCharArray());
		final LocalVariable		var = new LocalVariable("var1".toCharArray(),ExpressionNodeValue.STRING); 
		Command					cmd;

		macro.addDeclaration(var);
		macro.commitDeclarations();
		
		cmd = new IfConditionCommand();
		Assert.assertEquals(cmd.getType(),CommandType.IF_CONDITION);
		Assert.assertEquals(cmd.processCommand(0,0,"true\n".toCharArray(),0,0,macro),cmd);

		cmd = new WhileCommand();
		Assert.assertEquals(cmd.getType(),CommandType.WHILE);
		Assert.assertEquals(cmd.processCommand(0,0,"true\n".toCharArray(),0,0,macro),cmd);

		cmd = new ChoiseContainer();
		Assert.assertEquals(cmd.getType(),CommandType.CHOISE);
		Assert.assertEquals(cmd.processCommand(0,0,"true\n".toCharArray(),0,0,macro),cmd);

		cmd = new ChoiseConditionCommand();
		Assert.assertEquals(cmd.getType(),CommandType.CHOISE_CONDITION);
		Assert.assertEquals(cmd.processCommand(0,0,"true\n".toCharArray(),0,0,macro),cmd);
	
		cmd = new MErrorCommand();
		Assert.assertEquals(cmd.getType(),CommandType.MERROR);
		Assert.assertEquals(cmd.processCommand(0,0,"\"text\"\n".toCharArray(),0,0,macro),cmd);

		cmd = new ForCommand();
		Assert.assertEquals(cmd.getType(),CommandType.FOR);
		Assert.assertEquals(cmd.processCommand(0,0,"var1 = 10 to 20 step 3\n".toCharArray(),0,0,macro),cmd);

		cmd = new ForEachCommand();
		Assert.assertEquals(cmd.getType(),CommandType.FOR_EACH);
		Assert.assertEquals(cmd.processCommand(0,0,"var1 in \"1,2\" splitted by \",\"\n".toCharArray(),0,0,macro),cmd);
		
		cmd = new SetCommand(var);
		Assert.assertEquals(cmd.getType(),CommandType.SET);
		Assert.assertEquals(cmd.processCommand(0,0,"123\n".toCharArray(),0,0,macro),cmd);
	}

	@Test
	public void macroCommandTest() throws SyntaxException {
		final MacroCommand			macro = new MacroCommand("MACRO".toCharArray());
		final PositionalParameter	pp1 = new PositionalParameter("parm1".toCharArray(),ExpressionNodeValue.STRING);
		final PositionalParameter	pp2 = new PositionalParameter("parm2".toCharArray(),ExpressionNodeValue.STRING);
		final KeyParameter			k1 = new KeyParameter("key1".toCharArray(),ExpressionNodeValue.STRING);
		final KeyParameter			k2 = new KeyParameter("key2".toCharArray(),ExpressionNodeValue.STRING,new ConstantNode(true));
		final LocalVariable			var1 = new LocalVariable("var1".toCharArray(),ExpressionNodeValue.STRING);
		final LocalVariable			var2 = new LocalVariable("var2".toCharArray(),ExpressionNodeValue.STRING,new ConstantNode(123));
		
		Assert.assertArrayEquals(macro.getName(),"MACRO".toCharArray());
		Assert.assertNull(macro.seekDeclaration("parm1".toCharArray()));
		
		macro.addDeclaration(pp1);
		macro.addDeclaration(pp2);
		try{macro.addDeclaration(pp2);
			Assert.fail("Mandatory exception was not detected (duplicate name)");
		} catch (SyntaxException exc) {
		}
		macro.addDeclaration(k1);
		macro.addDeclaration(k2);
		macro.addDeclaration(var1);
		macro.addDeclaration(var2);
		
		try{macro.getDeclarations();
			Assert.fail("Mandatory exception was not detected (non-committed macro)");
		} catch (IllegalStateException exc) {
		}
		
		macro.commitDeclarations();
		try{macro.commitDeclarations();
			Assert.fail("Mandatory exception was not detected (second commit)");
		} catch (IllegalStateException exc) {
		}

		final AssignableExpressionNode[] 	decl = macro.getDeclarations();
		
		Assert.assertEquals(decl.length,6);
		for (int index = 0; index < decl.length; index++) {
			Assert.assertEquals(decl[index].getSequentialNumber(),index);
		}
	}

	@Test
	public void substitutionTest() throws SyntaxException {
		final MacroCommand		macro = new MacroCommand("MACRO".toCharArray());
		Command					cmd;

		macro.addDeclaration(new LocalVariable("var1".toCharArray(),ExpressionNodeValue.STRING));
		macro.commitDeclarations();
		
		cmd = new SubstitutionCommand();
		Assert.assertEquals(cmd.getType(),CommandType.SUBSTITUTION);
		Assert.assertEquals(cmd.processCommand(0,0,"before&var1&var1.between&var1..after\n".toCharArray(),0,0,macro),cmd);
	}
}
