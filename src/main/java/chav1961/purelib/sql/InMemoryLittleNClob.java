package chav1961.purelib.sql;

import java.sql.NClob;

/**
 * <p>This class implements in-memory {@linkplain NClob} to use in the SQL.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.sql
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public class InMemoryLittleNClob extends InMemoryLittleClob implements NClob {
	/**
	 * <p>Create empty NClob</p>
	 */
	InMemoryLittleNClob(){
		super();
	}

	/**
	 * <p>Create NCLob with the initial content</p>
	 * @param content initial conent if th NClob
	 */
	InMemoryLittleNClob(final char[] content){
		super(content);
	}
	
	/**
	 * <p>Create Clob with the initial content</p>
	 * @param content initial content
	 */
	InMemoryLittleNClob(final String content){
		super(content);
	}
	
	@Override
	public String toString() {
		return "SimpleLittleNClob [length=" + gca.length() + "]";
	}
}
