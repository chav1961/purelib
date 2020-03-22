package chav1961.purelib.testing;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class TestingUtils {
	public static PrintStream err() {
//		if ("true".equals(System.getProperty("suppress.junit.trace"))) {
			return new PrintStream(new OutputStream() {
				@Override public void write(int b) throws IOException {}
			});
//		}
//		else {
//			return System.err;
//		}
	}
}
