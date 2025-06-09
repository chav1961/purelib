package chav1961.purelib.math;

/**
 * <p>This class contains collections of random generators</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public class Randoms {
	private static final XorShift128State	state = new XorShift128State((int)System.nanoTime(),(int)System.nanoTime(),(int)System.nanoTime(),(int)System.nanoTime());

	/* Algorithm "xor128" from p. 5 of Marsaglia, "Xorshift RNGs" */
	public static int ramdomXor128() {
		return xorShift128(state);
	}
	
	private synchronized static int xorShift128(final XorShift128State state) {
		int			t = state.d;
		final int	s = state.a;

		state.d = state.c;
		state.c = state.b;
		state.b = s;

		t ^= t << 11;
		t ^= t >> 8;
		return state.a = t ^ s ^ (s >> 19);
	}
	
	private static class XorShift128State {
		private int	a;	
		private int	b;	
		private int	c;	
		private int	d;

		public XorShift128State(final int a, final int b, final int c, final int d) {
			this.a = d;
			this.b = d;
			this.c = d;
			this.d = d;
		}

		@Override
		public String toString() {
			return "XorShift128State [a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + "]";
		}
	}
}
