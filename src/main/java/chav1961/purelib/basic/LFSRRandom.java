package chav1961.purelib.basic;

/**
 * <p>This class is a random generator based on linear feedback shift register.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @see https://microsin.net/programming/avr/lfsr-pseudo-random-number-generator.html
 * @since 0.0.7 
 */
public class LFSRRandom {
	private static final int[][]	CONFIG = new int[][] {
										{2, 3, (1 << 0) | (1 << 1)},
										{3, 7, (1 << 0) | (1 << 2)},
										{4, 15, (1 << 0) | (1 << 3)},
										{5, 31, (1 << 1) | (1 << 4)},
										{6, 63, (1 << 0) | (1 << 5)},
										{7, 127, (1 << 0) | (1 << 6)},
										{8, 255, (1 << 1) | (1 << 2) | (1 << 3) | (1 << 7)},
										{9, 511, (1 << 3) | (1 << 8)},
										{10, 1023, (1 << 2) | (1 << 9)},
										{11, 2047, (1 << 1) | (1 << 10)},
										{12, 4095, (1 << 0) | (1 << 3) | (1 << 5) | (1 << 11)},
										{13, 8191, (1 << 0) | (1 << 2) | (1 << 3) | (1 << 12)},
										{14, 16383, (1 << 0) | (1 << 2) | (1 << 4) | (1 << 13)},
										{15, 32767, (1 << 0) | (1 << 14)},
										{16, 65535, (1 << 1) | (1 << 2) | (1 << 4) | (1 << 15)},
										{17, 131071, (1 << 2) | (1 << 16)},
										{18, 262143, (1 << 6) | (1 << 17)},
										{19, 524287, (1 << 0) | (1 << 1) | (1 << 4) | (1 << 18)},
										{20, 1048575, (1 << 2) | (1 << 19)},
										{21, 2097151, (1 << 1) | (1 << 20)},
										{22, 4194303, (1 << 0) | (1 << 21)},
										{23, 8388607, (1 << 4) | (1 << 22)},
										{24, 16777215, (1 << 0) | (1 << 2) | (1 << 3) | (1 << 23)},
										{25, 33554431, (1 << 7) | (1 << 24)},
										{26, 67108863, (1 << 0) | (1 << 1) | (1 << 5) | (1 << 25)},
										{27, 134217727, (1 << 0) | (1 << 1) | (1 << 4) | (1 << 26)},
										{28, 268435455, (1 << 2) | (1 << 27)},
										{29, 536870911, (1 << 1) | (1 << 28)},
										{30, 1073741823, (1 << 0) | (1 << 3) | (1 << 5) | (1 << 29)},
										{31, 2147483647, (1 << 2) | (1 << 30)}
									};
	
	private final int	seed;
	private final int	len;
	private final int	powerLen;
	private final int	targetMask;
	private final int	bitmap;
	private int			current;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param sequenceLength sequence length awaited. Must be at least 2
	 */
	public LFSRRandom(final int sequenceLength) {
		this((int) System.nanoTime(), sequenceLength);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param seed initial seed for random generator. Can't be 0
	 * @param sequenceLength sequence length awaited. Must be at least 2
	 */
	public LFSRRandom(final int seed, final int sequenceLength) {
		if (seed == 0) {
			throw new IllegalArgumentException("Seed initial value can't be 0");
		}
		else if (sequenceLength <= 1) {
			throw new IllegalArgumentException("Sequence length ["+sequenceLength+"] must be greater than 1");
		}
		else {
			this.seed = seed;
			this.len = sequenceLength;
			this.powerLen = getPowerLen(sequenceLength);
			this.bitmap = CONFIG[this.powerLen][2];
			this.targetMask = getTargetMask(this.powerLen);
			this.current = seed;
		}
	}
	
	/**
	 * <p>Reset random generator to initial seed</p>
	 */
	public void reset() {
		this.current = seed;
	}

	/**
	 * <p>Get next integer from random generator</p>
	 * @return next integer in the range 1..sequence length up to nearest power of 2.
	 */
	public int next() {
		boolean calc = false;
		
		for(int index = 0, mask = 1; index < powerLen; index++, mask <<= 1) {
			if ((bitmap & mask) != 0) {
				if ((current & mask) != 0) {
					calc = !calc; 
				}
			}
		}
		current >>= 1;
		if (calc) {
			current |= (1 << powerLen);
		}
		return current;
	}
	
	private int getPowerLen(final int sequenceLength) {
		for(int index = CONFIG.length - 1; index >= 0; index--) {
			if (CONFIG[index][1] > sequenceLength) {
				continue;
			}
			else {
				return CONFIG[index + 1][0];
			}
		}
		return 2;
	}

	private int getTargetMask(final int powerLen) {
		int	mask = -1;
		
		for(int index = 31; index >= powerLen; index--) {
			mask >>>= 1;
		}
		return mask;
	}
}
