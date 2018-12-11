package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleTerminals;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;

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
	
	private final Writer			nested;
	private final PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter> epilogue;
	private final XMLEventFactory 	eventFactory = XMLEventFactory.newInstance();
	private final XMLEventWriter 	writer;
	
	
	CreoleXMLOutputWriter(final Writer nested, final PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter> prologue, final PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter> epilogue) throws IOException {
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
	void internalWrite(final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
		try{writer.add(eventFactory.createCharacters(new String(content,from,to-from)));
		} catch (XMLStreamException e) {
			throw new IOException("I/O error writing content: "+e.getLocalizedMessage());
		}
	}


	@Override
	protected void processSection(final FSM<CreoleTerminals, SectionState, SectionActions, Integer> fsm, final CreoleTerminals terminal, final SectionState fromState, final SectionState toState, final SectionActions[] action, final Integer parameter) throws FlowException {
		try{for (SectionActions item : action) {
				switch (item) {
					case DIV_OPEN		: writeStartTag(TAG_DIV); break;
					case DIV_CLOSE		: writeEndTag(TAG_DIV); break;
					case P_OPEN			: writeStartTag(TAG_P); break;
					case P_CLOSE		: writeEndTag(TAG_P); break;
					case H_OPEN			: 
						writeStartTag(TAG_CAPTION);
						writeAttr(ATTR_DEPTH,CAPTION_DEPTH[parameter]);
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
	protected void processFont(final FSM<CreoleTerminals, FontState, FontActions, Integer> fsm, final CreoleTerminals terminal, final FontState fromState, final FontState toState, final FontActions[] action, final Integer parameter) throws FlowException { 
		try{for (FontActions item : action) {
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
	void insertImage(final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		writeStartTag(TAG_IMG);
		writeAttr(ATTR_SRC,new String(data,startLink,endLink-startLink));
		internalWrite(data,startCaption,endCaption,false);
		writeEndTag(TAG_IMG);
	}

	@Override
	void insertLink(final boolean localRef, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		if (localRef) {
			if (startCaption == endCaption) {
				writeStartTag(TAG_LINK);
				writeAttr(ATTR_HREF,new String(data,startLink,endLink-startLink));
				internalWrite(data,startLink,endLink,false);
				writeEndTag(TAG_LINK);
			}
			else {
				writeStartTag(TAG_LINK);
				writeAttr(ATTR_HREF,new String(data,startLink,endLink-startLink));
				internalWrite(data,startCaption,endCaption,false);
				writeEndTag(TAG_LINK);
			}
		}
		else {
			if (startCaption == endCaption) {
				writeStartTag(TAG_LINK);
				writeAttr(ATTR_HREF,new String(data,startLink,endLink-startLink));
				internalWrite(data,startLink,endLink,false);
				writeEndTag(TAG_LINK);
			}
			else {
				writeStartTag(TAG_LINK);
				writeAttr(ATTR_HREF,new String(data,startLink,endLink-startLink));
				internalWrite(data,startCaption,endCaption,false);
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
	
	static PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter> getPrologue() {
		return new PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter>(){
			@Override
			public boolean writeContent(XMLEventWriter writer, CreoleXMLOutputWriter instance) throws IOException {
				try{writer.setDefaultNamespace(NAMESPACE);
					writer.add(instance.eventFactory.createStartDocument());
					instance.writeStartTag(TAG_ROOT);
					writer.add(instance.eventFactory.createNamespace(PREFIX,NAMESPACE)); 					
					return false;
				} catch (XMLStreamException e) {
					throw new IOException(e.getLocalizedMessage(),e);
				}
			}
		};
	}

	static PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter> getEpilogue() {
		return new PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter>(){
			@Override
			public boolean writeContent(XMLEventWriter writer, CreoleXMLOutputWriter instance) throws IOException {
				instance.writeEndTag(TAG_ROOT);
				return false;
			}
		};
	}
}