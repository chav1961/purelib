package chav1961.purelib.enumerations;

/**
 * <p>This enumerations describes output format for the markup language writer. It includes :</p>
 * <ul>
 * <li>{@link #TEXT} - simple text without any markup signs for the given markup language content</li>
 * <li>{@link #XML} - RAW XML for the given markup language content</li>
 * <li>{@link #XML2TEXT} - textual representation of the given markup language content</li> 
 * <li>{@link #XML2HTML} - HTML format for the given markup language content</li> 
 * <li>{@link #XML2PDF} - XML format to use with <a href="https://xmlgraphics.apache.org/fop/">Apache FOP</a></li>
 * </ul> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public enum MarkupOutputFormat {
	/**
	 * <p>Simple text without any markup signs for the given markup language content</p>
	 */
	TEXT,
	
	/**
	 * <p>RAW XML for the given markup language content</p>
	 */
	XML,
	
	/**
	 * <p>textual representation of the given markup language content</p>
	 */
	XML2TEXT,
	
	/**
	 * <p>HTML format for the given markup language content</p>
	 */
	XML2HTML, 
	
	/**
	 * <p>XML format to use with <a href="https://xmlgraphics.apache.org/fop/">Apache FOP</a></p>
	 * 
	 */
	XML2PDF,
	
	/**
	 * <p>Parsed CSV format to use in the Creole syntax highlighters.</p>
	 */
	PARSEDCSV
}