package chav1961.purelib.cdb.intern;

public enum EntityType {
	Root(true, true, true, true, true), 
	Rule(true, true, true, true, true),
	Char(true, true, true, false, false), 
	Sequence(true, true, true, false, false),
	Name(true, true, true, false, false),
	Predefined(true, true, true, true, false),
	Option(true, true, true, true, true),
	Repeat(true, true, true, true, true),
	Repeat1(true, true, true, true, true),
	Switch(true, true, true, true, true),
	Case(true, true, true, true, true),
	Detected(false, false, true, true, false);
	
	private final boolean 	createTestMethod;
	private final boolean 	createSkipMethod;
	private final boolean 	createParseMethod;
	private final boolean 	createNode;
	private final boolean 	isCollection;
	
	EntityType(final boolean createTestMethod, final boolean createSkipMethod, final boolean createParseMethod, final boolean createNode, final boolean isCollection) {
		this.createTestMethod = createTestMethod;
		this.createSkipMethod = createSkipMethod;
		this.createParseMethod = createParseMethod;
		this.createNode = createNode;
		this.isCollection = isCollection;
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
	
	public boolean isCollection() {
		return isCollection;
	}
}