package chav1961.purelib.json;

import java.util.Arrays;

public class JsonOuterArrayClass {
	JsonInnerClass[]	in1;
	JsonInnerClass[]	in2;
	int		a;
	String	b;
	
	public JsonOuterArrayClass(){}
	
	@Override
	public String toString() {
		return "JsonOuterArrayClass [in1=" + Arrays.toString(in1) + ", in2=" + Arrays.toString(in2) + ", a=" + a + ", b=" + b + "]";
	}
}