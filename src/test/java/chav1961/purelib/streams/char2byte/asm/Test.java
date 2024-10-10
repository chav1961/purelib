package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import chav1961.purelib.basic.exceptions.ContentException;

public class Test {

	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ContentException, NullPointerException, IOException {
		// TODO Auto-generated method stub
		for(int index = 0; index < 100000000; index++) {
			new AsmWriterTest().basicTest();
		}
	}

}
