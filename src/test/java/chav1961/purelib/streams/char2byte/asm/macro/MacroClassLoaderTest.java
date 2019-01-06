package chav1961.purelib.streams.char2byte.asm.macro;

import java.io.IOException;

import org.junit.Test;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;

public class MacroClassLoaderTest {
	@Test
	public void test() throws CalculationException, IOException {
		try(final MacroClassLoader	mcl = new MacroClassLoader(this.getClass().getClassLoader())) {
			final GrowableCharArray	gca = new GrowableCharArray(false);
			
			gca.append(" .package chav1961.purelib.streams.char2byte.asm\nX .class public\nX .end\n".toCharArray());
			mcl.createClass("chav1961.purelib.streams.char2byte.asm.X",gca);
		}
	}
}
