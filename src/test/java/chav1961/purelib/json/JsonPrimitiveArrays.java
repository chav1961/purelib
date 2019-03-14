package chav1961.purelib.json;

import java.util.Arrays;

public class JsonPrimitiveArrays {
	byte[]		a;	
	short[]		b;	
	int[]		c;	
	long[]		d;	
	float[]		e;	
	double[]	f;	
	boolean[]	g;
	char[]		h;	

	public JsonPrimitiveArrays(){}
	
	@Override
	public String toString() {
		return "JsonPrimitiveArrays [a=" + Arrays.toString(a) + ", b="
				+ Arrays.toString(b) + ", c=" + Arrays.toString(c) + ", d="
				+ Arrays.toString(d) + ", e=" + Arrays.toString(e) + ", f="
				+ Arrays.toString(f) + ", g=" + Arrays.toString(g) 
				+ ", h=" + Arrays.toString(h) + "]";
	}
}