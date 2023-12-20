package chav1961.purelib.streams.char2char.intern;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;
import chav1961.purelib.streams.interfaces.intern.CreoleFontActions;
import chav1961.purelib.streams.interfaces.intern.CreoleFontState;
import chav1961.purelib.streams.interfaces.intern.CreoleSectionActions;
import chav1961.purelib.streams.interfaces.intern.CreoleSectionState;
import chav1961.purelib.streams.interfaces.intern.CreoleTerminals;

class CreoleXMLOutputWriter extends CreoleOutputWriter {
	private static final String		NAMESPACE = "http://www.wikicreole.org/";
	private static final String		PREFIX = "cre";

	private static final String		TAG_ROOT = "root";
	private static final String		TAG_DIV = "div";
	private static final String		TAG_UL = "ul";
	private static final String		TAG_OL = "ol";
	private static final String		TAG_LI = "li";
	private static final String		TAG_CAPTION = "caption";
	private static final String		TAG_TABLE = "table";
	private static final String		TAG_TABLE_HEADER = "tableHeader";
	private static final String		TAG_TABLE_BODY = "tableBody";
	private static final String		TAG_TR = "tr";
	private static final String		TAG_TH = "th";
	private static final String		TAG_TD = "td";
	private static final String		TAG_BR = "br";
	private static final String		TAG_HR = "hr";
	private static final String		TAG_P = "p";
	private static final String		TAG_BOLD = "bold";
	private static final String		TAG_ITALIC = "italic";
	private static final String		TAG_IMG = "img";
	private static final String		TAG_LINK = "link";

	private static final String		ATTR_SRC = "src";
	private static final String		ATTR_HREF = "href";
	private static final String		ATTR_DEPTH = "depth";
	
	private static final String[]	CAPTION_DEPTH = {"1","2","3","4","5","6"};
	
	private static final char[]		ESC_LT = "&lt;".toCharArray();
	private static final char[]		ESC_GT = "&gt;".toCharArray();
	private static final char[]		ESC_AMP = "&amp;".toCharArray();
	private static final char[]		ESC_QUOT = "&quot;".toCharArray();
	
	private final Writer			nested;
	private final PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter> epilogue;
	private final XMLEventFactory 	eventFactory = XMLEventFactory.newInstance();
	private final XMLEventWriter 	writer;
	
	
	public CreoleXMLOutputWriter(final Writer nested, final PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter> prologue, final PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter> epilogue) throws IOException {
		this.nested = nested;
		this.epilogue = epilogue;
		
		try{this.writer = new XMLEventWrapper(XMLOutputFactory.newInstance().createXMLEventWriter(nested));
			prologue.writeContent(writer,this);
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new IOException("I/O error creating XML output: "+e.getLocalizedMessage(),e);
		}
	}

	@Override
	public void close() throws IOException {
		epilogue.writeContent(writer,this);
	    try{writer.flush();
		} catch (XMLStreamException e) {
			throw new IOException("I/O error closing XML output: "+e.getLocalizedMessage(),e);
		}		
		nested.flush();
	}

	@Override
	public void write(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
		try{writer.add(eventFactory.createCharacters(new String(content,from,to-from)));
		} catch (XMLStreamException e) {
			throw new IOException("I/O error writing content: "+e.getLocalizedMessage());
		}
	}

	@Override
	public void writeEscaped(long displacement, char[] content, int from, int to, boolean keepNewLines) throws IOException, SyntaxException {
		boolean	has2escape = false;
		
		for (int index = from; index < to; index++) {
			if (content[index] == '<' || content[index] == '>' || content[index] == '&' || content[index] == '\"') {
				has2escape = true;
				break;
			}
		}
		if (has2escape) {
			int 	start = from;
			
			for (int index = from; index <= to; index++) {
				if (content[index] == '<' || content[index] == '>' || content[index] == '&' || content[index] == '\"') {
					write(displacement+start-from,content,start,index,keepNewLines);
					switch (content[index]) {
						case '<'	: internalWrite(displacement+start-from,ESC_LT); break;
						case '>'	: internalWrite(displacement+start-from,ESC_GT); break;
						case '&'	: internalWrite(displacement+start-from,ESC_AMP); break;
						case '\"'	: internalWrite(displacement+start-from,ESC_QUOT); break;
					}
					start = index + 1;
				}
			}
			if (start < to) {
				write(displacement+start-from,content,start,to,keepNewLines);
			}
		}
		else {
			write(displacement,content,from,to,keepNewLines);
		}
	}

	@Override
	public void processSection(final FSM<CreoleTerminals, CreoleSectionState, CreoleSectionActions, Long> fsm, final CreoleTerminals terminal, final CreoleSectionState fromState, final CreoleSectionState toState, final CreoleSectionActions[] action, final Long parameter) throws FlowException {
		try{for (CreoleSectionActions item : action) {
				switch (item) {
					case DIV_OPEN		: writeStartTag(TAG_DIV); break;
					case DIV_CLOSE		: writeEndTag(TAG_DIV); break;
					case P_OPEN			: writeStartTag(TAG_P); break;
					case P_CLOSE		: writeEndTag(TAG_P); break;
					case H_OPEN			: 
						writeStartTag(TAG_CAPTION);
						writeAttr(ATTR_DEPTH,CAPTION_DEPTH[parameter.intValue()]);
						break;
					case H_CLOSE		: 
						writeEndTag(TAG_CAPTION);
						break;
					case HR				: writeStartTag(TAG_HR); writeEndTag(TAG_HR); break;
					case UL_OPEN		: writeStartTag(TAG_UL); break;
					case UL_CLOSE		: writeEndTag(TAG_UL); break;
					case OL_OPEN		: writeStartTag(TAG_OL); break;
					case OL_CLOSE		: writeEndTag(TAG_OL); break;
					case LI_OPEN		: writeStartTag(TAG_LI); break;	
					case LI_CLOSE		: writeEndTag(TAG_LI); break;
					case TABLE_OPEN		: writeStartTag(TAG_TABLE); break;
					case TABLE_CLOSE	: writeEndTag(TAG_TABLE); break;
					case THEAD_OPEN		: writeStartTag(TAG_TABLE_HEADER); break;
					case THEAD_CLOSE	: writeEndTag(TAG_TABLE_HEADER); break;
					case TBODY_OPEN		: writeStartTag(TAG_TABLE_BODY); break;
					case TBODY_CLOSE	: writeEndTag(TAG_TABLE_BODY); break;
					case TR_OPEN		: writeStartTag(TAG_TR); break;
					case TR_CLOSE		: writeEndTag(TAG_TR); break;
					case TH_OPEN		: writeStartTag(TAG_TH); break;
					case TH_CLOSE		: writeEndTag(TAG_TH); break;
					case TD_OPEN		: writeStartTag(TAG_TD); break;
					case TD_CLOSE		: writeEndTag(TAG_TD); break;
					default :
				}
			}
		} catch (IOException e) {
			throw new FlowException(e.getLocalizedMessage());
		}
	}

	@Override
	public void processFont(final FSM<CreoleTerminals, CreoleFontState, CreoleFontActions, Long> fsm, final CreoleTerminals terminal, final CreoleFontState fromState, final CreoleFontState toState, final CreoleFontActions[] action, final Long parameter) throws FlowException { 
		try{for (CreoleFontActions item : action) {
				switch (item) {
					case BOLD_OPEN		: writeStartTag(TAG_BOLD); break;
					case BOLD_CLOSE		: writeEndTag(TAG_BOLD); break;
					case ITALIC_OPEN	: writeStartTag(TAG_ITALIC); break;
					case ITALIC_CLOSE	: writeEndTag(TAG_ITALIC); break;
					case BR				: writeStartTag(TAG_BR); writeEndTag(TAG_BR); break;
					default :
				}
			}
		} catch (IOException e) {
			throw new FlowException(e.getLocalizedMessage());
		}
	}
	
	
	@Override
	public void insertImage(final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		writeStartTag(TAG_IMG);
		writeAttr(ATTR_SRC,new String(data,startLink,endLink-startLink));
		write(displacement,data,startCaption,endCaption,false);
		writeEndTag(TAG_IMG);
	}

	@Override
	public void insertLink(final boolean localRef, final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		if (localRef) {
			if (startCaption == endCaption) {
				writeStartTag(TAG_LINK);
				writeAttr(ATTR_HREF,new String(data,startLink,endLink-startLink));
				write(displacement,data,startLink,endLink,false);
				writeEndTag(TAG_LINK);
			}
			else {
				writeStartTag(TAG_LINK);
				writeAttr(ATTR_HREF,new String(data,startLink,endLink-startLink));
				write(displacement,data,startCaption,endCaption,false);
				writeEndTag(TAG_LINK);
			}
		}
		else {
			if (startCaption == endCaption) {
				writeStartTag(TAG_LINK);
				writeAttr(ATTR_HREF,new String(data,startLink,endLink-startLink));
				write(displacement,data,startLink,endLink,false);
				writeEndTag(TAG_LINK);
			}
			else {
				writeStartTag(TAG_LINK);
				writeAttr(ATTR_HREF,new String(data,startLink,endLink-startLink));
				write(displacement,data,startCaption,endCaption,false);
				writeEndTag(TAG_LINK);
			}
		}
	}
	
	private void writeStartTag(final String tag) throws IOException {
		try{this.writer.add(eventFactory.createStartElement(PREFIX,NAMESPACE,tag));
		} catch (XMLStreamException e) {
			throw new IOException("I/O error writing tag ["+tag+"] : "+e.getLocalizedMessage());
		}
	}

	private void writeAttr(final String key, final String value) throws IOException {
		try{this.writer.add(eventFactory.createAttribute(key,value));
		} catch (XMLStreamException e) {
			throw new IOException("I/O error writing attr ["+key+"] : "+e.getLocalizedMessage());
		}
	}
	
	private void writeEndTag(final String tag) throws IOException {
		try{this.writer.add(eventFactory.createEndElement(PREFIX,NAMESPACE,tag));
		} catch (XMLStreamException e) {
			throw new IOException("I/O error writing tag ["+tag+"] : "+e.getLocalizedMessage());
		}
	}
	
	public static PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter> getPrologue() {
		return new PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter>(){
			@Override
			public boolean writeContent(XMLEventWriter writer, CreoleXMLOutputWriter instance) throws IOException {
				try{writer.setDefaultNamespace(NAMESPACE);
					writer.add(instance.eventFactory.createStartDocument());
					instance.writeStartTag(TAG_ROOT);
					writer.add(instance.eventFactory.createNamespace(PREFIX,NAMESPACE)); 					
					return true;
				} catch (XMLStreamException e) {
					throw new IOException(e.getLocalizedMessage(),e);
				}
			}
		};
	}

	public static PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter> getEpilogue() {
		return new PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter>(){
			@Override
			public boolean writeContent(XMLEventWriter writer, CreoleXMLOutputWriter instance) throws IOException {
				instance.writeEndTag(TAG_ROOT);
				return true;
			}
		};
	}

	public static PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter> getPrologue(final URI source) throws ContentException {
		if (source == null) {
			throw new NullPointerException("Source URI can't be null"); 
		}
		else {
			try{final String	content = Utils.fromResource(source.toURL());
			
				return new PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter>(){
					@Override
					public boolean writeContent(XMLEventWriter writer, CreoleXMLOutputWriter instance) throws IOException {
						try{writer.setDefaultNamespace(NAMESPACE);
							writer.add(instance.eventFactory.createStartDocument());
							instance.writeStartTag(TAG_ROOT);
							writer.add(instance.eventFactory.createNamespace(PREFIX,NAMESPACE));
							writer.add(instance.eventFactory.createCharacters(content));
							return true;
						} catch (XMLStreamException e) {
							throw new IOException(e.getLocalizedMessage(),e);
						}
					}
				};
			} catch (IOException e) {
				throw new ContentException("I/O error loading content from ["+source+"]: "+e.getLocalizedMessage());
			}
		}
	}

	public static PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter> getEpilogue(final URI source) throws ContentException {
		if (source == null) {
			throw new NullPointerException("Source URI can't be null"); 
		}
		else {
			try{final String	content = Utils.fromResource(source.toURL());
			
				return new PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter>(){
					@Override
					public boolean writeContent(XMLEventWriter writer, CreoleXMLOutputWriter instance) throws IOException {
						try{writer.add(instance.eventFactory.createCharacters(content));
							instance.writeEndTag(TAG_ROOT);
							return true;
						} catch (XMLStreamException e) {
							throw new IOException(e.getLocalizedMessage(),e);
						}
					}
				};
			} catch (IOException e) {
				throw new ContentException("I/O error loading content from ["+source+"]: "+e.getLocalizedMessage());
			}
		}
	}

}