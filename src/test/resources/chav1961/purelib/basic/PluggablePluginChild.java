package chav1961.purelib.basic;

public class PluggablePluginChild {
	public static void main(String[] args) {
		print("text");
	}

	public static void print(String text) {
		System.err.println("Loader: "+PluggablePluginChild.class.getClassLoader());
		System.err.println(text);
	}
}
