package chav1961.purelib.enumerations;

import chav1961.purelib.basic.MimeType;

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
 * @last.update 0.0.7
 */
public enum MarkupOutputFormat {
	/**
	 * <p>Simple text without any markup signs for the given markup language content</p>
	 */
	TEXT(MimeType.MIME_PLAIN_TEXT),
	
	/**
	 * <p>RAW XML for the given markup language content</p>
	 */
	XML(MimeType.MIME_XML_TEXT),
	
	/**
	 * <p>textual representation of the given markup language content</p>
	 */
	XML2TEXT(MimeType.MIME_XML_TEXT),
	
	/**
	 * <p>HTML format for the given markup language content</p>
	 */
	XML2HTML(MimeType.MIME_HTML_TEXT),
	
	/**
	 * <p>XML format to use with <a href="https://xmlgraphics.apache.org/fop/">Apache FOP</a></p>
	 * 
	 */
	XML2PDF(MimeType.MIME_PDF_TYPE),
	
	/**
	 * <p>Parsed CSV format to use in the Creole syntax highlighters.</p>
	 */
	PARSEDCSV(MimeType.MIME_CSS_TEXT),
	
	/**
	 * <p>Content, converted to markdown syntax</p>
	 */
	MARKDOWN(MimeType.MIME_MARKDOWN_TEXT);
	
	private final MimeType	mime;
	
	private MarkupOutputFormat(final MimeType mime) {
		this.mime = mime;
	}
	
	public MimeType getMimeType() {
		return mime;
	}
	
	public static MarkupOutputFormat byMimeType(final MimeType type) {
		if (type == null) {
			throw new NullPointerException("MIME type can't be null");
		}
		else {
			for(MarkupOutputFormat item : values()) {
				if (item.getMimeType().equals(type)) {
					return item;
				}
			}
			throw new IllegalArgumentException("MIME type ["+type+"] is not known in the format list");
		}
	}

}