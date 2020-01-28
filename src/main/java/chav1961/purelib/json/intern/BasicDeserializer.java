package chav1961.purelib.json.intern;

public class BasicDeserializer {
	protected BasicDeserializer() {
	}
	
	protected static void throwException(final Throwable exc) throws Throwable {
		throw exc;
	}

	protected static void throwUnknownException() throws Throwable {
		throwException(new IllegalArgumentException("Unknown classId/fieldId combination to set value"));
	}

	protected static void throwCastException() throws Throwable {
		throwException(new IllegalArgumentException("Source primitive type can't be cast to target field type"));
	}
	
	protected static void throwCreateException() throws Throwable {
		throwException(new IllegalArgumentException("Unknown classId value to create instance"));
	}
}