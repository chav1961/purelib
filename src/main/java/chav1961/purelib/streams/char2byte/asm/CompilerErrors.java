package chav1961.purelib.streams.char2byte.asm;

import java.util.Locale;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;

enum CompilerErrors {
	ERR_DUPLICATE_CLASS_REFERENCE_NAME("Duplicate class reference name [%1$s]"),
	ERR_QUALIFIED_NAME_DIFFERENT_TYPE("Qualified name [%1$s] is not a [%2$s], but [%3$s]"),
	ERR_ENTITY_NOT_DECLARED("Name [%1$s] is unknown or illegal! Use .import directive to load it's description.\nPossibly, [%2$s] you need?"),
	ERR_DUPLICATE_MACROS("Duplicate macros [%1$s] in the input stream"),
	ERR_MACROS_CODE_CORRUPTED("Macros [%1$s] - code corrupted: [%2$s]\nMacros class content:\n%3$s"),
	ERR_MACROS_INVOCATION_ERROR("Macros [%1$s] - invocation error [%2$s]"),
	ERR_ILLEGAL_LABEL_ENTRY_NAME("Illegal label/entity name"),
	ERR_BRANCH_LABEL_OUTSIDE_METHOD_BODY("Branch label outside the method body!"),
	ERR_DUPLICATE_CLASS_DIRECTIVE("Duplicate class/interface directive in the same stream. Use separate streams for each class/interface!"),
	ERR_NESTED_CLASS_DIRECTIVE("Nested classes/interfaces are not supported now"),
	ERR_FIELD_DIRECTIVE_OUTSIDE_CLASS("Field directive outside the class/interface!"),
	ERR_FIELD_DIRECTIVE_INSIDE_METHOD("Field directive inside the method!"),
	ERR_METHOD_DIRECTIVE_OUTSIDE_CLASS("Method directive outside the class/interface!"),
	ERR_NESTED_METHOD_DIRECTIVE("Nested method directive!"),
	ERR_PARAMETER_DIRECTIVE_OUTSIDE_METHOD("Parameter directive is used outside the method description!"),
	ERR_VAR_DIRECTIVE_OUTSIDE_METHOD("Var directive is used outside the method description!"),
	ERR_BEGIN_DIRECTIVE_OUTSIDE_METHOD("Begin directive is used outside the method description!"),
	ERR_STACK_DIRECTIVE_OUTSIDE_METHOD("Stack directive is used outside the method description!"),
	ERR_DUPLICATE_STACK_DIRECTIVE("Duplicate Stack directive detected!"),
	ERR_DUPLICATE_PACKAGE_DIRECTIVE("Duplicate package directive!"),
	ERR_PACKAGE_DIRECTIVE_INSIDE_CLASS("Package directive inside the class/interface description! Use it before the class/interface directive only!"),
	ERR_IMPORT_DIRECTIVE_INSIDE_CLASS("Import directive inside the class/interface description! Use it before the class/interface directive only!"),
	ERR_DIRECTIVE_OUTSIDE_METHOD_BODY("Directive [%1$s] must be used inside method body only!"),
	ERR_MACROS_DIRECTIVE_AFTER_PACKAGE("Directive .macro can be used before .package directive only!"),
	ERR_DIRECTIVE_OUTSIDE_SWITCH("Directive .default can be used inside lookupswitch/tableswitch command body only!"),
	ERR_UNKNOWN_DIRECTIVE("Unknown directive [%1$s]"),
	ERR_UNKNOWN_AUTOMATION("Unknown automation [%1$s]"),
	ERR_AUTOMATION_OUTSIDE_METHOD_BODY("Automation [%1$s] must be used inside method body only!"),
	ERR_COMMAND_MNEMONICS_MISSING("Command mnemonics awaited, bit is missing"),
	ERR_UNKNOWN_COMMAND_MNEMONICS("Unknoww command mnemonics [%1$s]"),
	ERR_RESTRICTED_COMMAND("Restricted command in the input stream"),
	ERR_COMMAND_OUTSIDE_METHOD_BODY("Valid command outside the method mody. Check that .stack or .begin directive was typed earlier"),
	ERR_UNPARSED_LINE_IN_CONTEXT("Unparsed line in the input: [%1$s] is unknown or illegal in the [%1$s] context"),
	ERR_MANDATORY_NAME_IS_MISSING("Missing mandatory name before directive!"),
	ERR_NAME_IS_NOT_SUPPORTED_HERE("Name before this directive is not supported!"),
	ERR_DUPLICATE_LABEL_IN_METHOD_BODY("Duplicate label in the method body!"),
	ERR_DUPLICATE_EXTENDS_OPTION("Duplicate 'extends' option in the .class/.interface directive!"),
	ERR_DUPLICATE_IMPLEMENTS_OPTION("Duplicate 'implements' option in the .class/.interface directive!"),
	ERR_ILLEGAL_EXTENDS_OPTION("Attempt to extends final or private class [%1$s]!"),
	ERR_CLASS_IS_NOT_DECLARED("Class/interface [%1$s] is not declared! Use .import directive to load it's description"),
	ERR_CLASS_NOT_FOUND("Class [%1$s] is unknown in the current class loader. Check the class name you want to import and/or make it accessible for the class loader"),
	ERR_CLASS_INSTEAD_OF_INTERFACE("Item [%1$s] references to the class, not interface!"),
	ERR_UNSUPPORTED_CLASS_OPTION("Class definition contains unknown or unsupported option [%1$s]!"),
	ERR_MUTUALLY_EXCLUDED_OPTIONS("Mutually excluded options [%1$s] int he directive!"),
	ERR_VOID_NOT_APPLICABLE("Type 'void' is not applicable here!"),
	ERR_INITIAL_VALUES_FOR_NON_STATIC_FIELD("Initial values can be typed for static fields only!"),
	ERR_INITIAL_VALUES_FOR_ILLEGAL_FIELD_TYPE("Initial values can be typed for primitive types or strings only!"),
	ERR_ILLEGAL_INITIAL_VALUE("Illegal initial value for [%1$s] type!"),
	ERR_RETURN_TYPE_IS_MISSING("Required return type for the method/constructor is missing!"),
	ERR_RETURN_TYPE_MUST_BE_VOID("Constructor must return void type only!"),
	ERR_ABSTRACT_METHOD_INSIDE_NON_ABSTRACT_CLASS("Abstract method inside the non-abstract class!"),
	ERR_DUPLICATE_CLINIT_METHOD("Class can't have more than one <clinit> methods!"),
	ERR_PARAMETERS_INSIDE_CLINIT("Class initialization method <clinit> should not have any parameters!"),
	ERR_ILLEGAL_STACK_DIRECTIVE_PARAMETER("Stack size is neither valid integer constant nor 'optimistic'/'pessimistic' (possibly it's size is long, float or double)"),
	ERR_PACKAGE_NAME_MISSING("Package name is missing!"),
	ERR_RESOURCE_URL_IS_MISSING("Resource URL is missing in the directive. Pay attention that resource URL must be wrapped with (\")!"),
	ERR_INVALID_RESOURCE_URL("Resource URL [%1$s] is unavailable or malformed!"),
	ERR_RESOURCE_URL_ERROR("Source [%1$s], line [%2$d]: I/O error reading data (%3$s)"),
	ERR_VERSION_NUMBER_IS_MISSING("Missing version number!"),
	ERR_UNSUPPORTED_VERSION_NUMBER("Version number %1$d.%2$d is not supported. Only 1.7 and 1.8 are available now!"),
	ERR_CONSTANT_POOL_TOO_LONG("Class file restriction: constant pool is greater than 65536 items. Simplify your class code!");
	
	private final String	format;
	
	private CompilerErrors(final String format) {
		this.format = format;
	}
	
	public ContentException error(final Object... parameters) {
		return new ContentException(String.format(format, parameters));
	}

	public SyntaxException syntaxError(final int lineNo, final Object... parameters) {
		return new SyntaxException(lineNo, 0, String.format(format, parameters));
	}

	public ContentException error(final Locale locale, final Object... parameters) {
		return error(parameters);
	}

	public SyntaxException syntaxError(final int lineNo, final Locale locale, final Object... parameters) {
		return syntaxError(lineNo, parameters);		
	}
}
