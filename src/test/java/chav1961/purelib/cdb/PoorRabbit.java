package chav1961.purelib.cdb;

import java.io.IOException;
import java.lang.management.ManagementFactory;

public class PoorRabbit {
	public static int	x = 10;

	public PoorRabbit(final int x) {
		init();
	}
	
	private void init() {
		final int	y = x + 10;
		int			z = 20;
		
		System.err.println("X="+x);
		System.err.println("Y="+y);
		System.err.println("Z="+z);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		final byte		varByte = 10;
		final short		varShort = 20;
		final char		varChar = '*';
		final int		varInt = 30;
		final long		varLong = 40L;
		final float		varFloat = 50.0f;
		final double	varDouble = 60.0f;
		final boolean	varBoolean = true;
		final String	name = ManagementFactory.getRuntimeMXBean().getName();
		final char[]	charContent = "test string".toCharArray();
		final String[]	stringContent = {"test string1","test string2"};
		final Long[]	longContent = {10L, 20L}; 
		
		System.out.println("PID="+Long.valueOf(name.substring(0,name.indexOf('@'))));
		System.out.flush();
		System.in.read();
		new PoorRabbit(100);
	}
}
