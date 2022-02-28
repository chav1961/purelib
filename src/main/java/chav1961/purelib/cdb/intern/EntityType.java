package chav1961.purelib.cdb.intern;

public enum EntityType {
	Root(true, true, true, true), 
	Rule(true, true, true, true),
	Char(true, true, true, false), 
	Sequence(true, true, true, false),
	Name(true, true, true, false),
	Predefined(true, true, true, true),
	Option(true, true, true, true),
	Repeat(true, true, true, true),
	Switch(true, true, true, true),
	Case(true, true, true, true),
	Detected(false, false, true, true);
	
	private final boolean 	createTestMethod;
	private final boolean 	createSkipMethod;
	private final boolean 	createParseMethod;
	private final boolean 	createNode;
	
	EntityType(final boolean createTestMethod, final boolean createSkipMethod, final boolean createParseMethod, final boolean createNode) {
		this.createTestMethod = createTestMethod;
		this.createSkipMethod = createSkipMethod;
		this.createParseMethod = createParseMethod;
		this.createNode = createNode;
	}
	
	public boolean needCreateTestMethod() {
		return createTestMethod;
	}

	public boolean needCreateSkipMethod() {
		return createSkipMethod;
	}

	public boolean needCreateParseMethod() {
		return createParseMethod;
	}

	public boolean needCreateNode() {
		return createNode;
	}
}