package chav1961.purelib.cdb.interfaces;

/**
 * <p>This interface is a minimalistic descriptor of the lexemas were parsed</p> 
 * @param <LexType> lexema type of any kind
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public interface LexemaInterface<LexType extends Enum<?>> {
	/**
	 * <p>Get row where the lexema is located</p> 
	 * @return row where the lexema is located
	 */
	int getRow();
	
	/**
	 * <p>Get column where the lexema is located</p>
	 * @return column where the lexema is located
	 */
	int getColumn();
	/**
	 * <p>Get lexema type</p>
	 * @return lexema type. Can't be null
	 */
	LexType getType();
}
