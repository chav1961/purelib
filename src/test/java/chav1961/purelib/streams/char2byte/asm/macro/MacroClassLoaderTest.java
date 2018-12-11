package chav1961.purelib.streams.char2byte.asm.macro;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.streams.char2byte.asm.macro.MacroClassLoader;

public class MacroClassLoaderTest {
	@Test
	public void test() throws CalculationException, IOException {
		final MacroClassLoader	mcl = new MacroClassLoader(this.getClass().getClassLoader());
		final GrowableCharArray	gca = new GrowableCharArray(false);
		
		gca.append(" .package chav1961.purelib.streams.char2byte.asm\nX .class public\nX .end\n".toCharArray());
		final Class<?>			cl = mcl.createClass("chav1961.purelib.streams.char2byte.asm.X",gca);
		
	}
}
