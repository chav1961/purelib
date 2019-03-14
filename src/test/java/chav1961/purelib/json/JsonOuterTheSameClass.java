package chav1961.purelib.json;

public class JsonOuterTheSameClass {
	JsonInnerClass	in;
	int		x;
	String	y;

	public JsonOuterTheSameClass(){}
	
	@Override
	public String toString() {
		return "JsonOuterTheSameClass [in=" + in + ", x=" + x + ", y=" + y + "]";
	}
}