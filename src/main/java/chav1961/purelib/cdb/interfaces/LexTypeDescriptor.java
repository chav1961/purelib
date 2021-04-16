package chav1961.purelib.cdb.interfaces;

public interface LexTypeDescriptor<LexType extends Enum<?>> {
	String getPattern();
	LexType getMembers();
}
