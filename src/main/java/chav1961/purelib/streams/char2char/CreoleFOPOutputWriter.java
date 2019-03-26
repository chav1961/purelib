package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;

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
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleTerminals;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;

class CreoleFOPOutputWriter extends CreoleOutputWriter {
	private static final String		NAMESPACE = "http://www.w3.org/1999/XSL/Format";
	private static final String		PREFIX = "fo";
	private static final char[]		EMPTY_LIST = " ".toCharArray();
	private static final char[]		ASTERISK_LIST = "\u2022".toCharArray();
	
	private static final String		TAG_ROOT = "root";
	private static final String		TAG_PROLOGUE_LAYOUT_MASTER_SET = "layout-master-set";
	private static final String		TAG_PROLOGUE_SIMPLE_PAGE_MASTER = "simple-page-master";
	private static final String		TAG_PROLOGUE_REGION_BODY = "region-body";
	private static final String		TAG_PROLOGUE_REGION_BEFORE = "region-before";
	private static final String		TAG_PAGE_SEQUENCE = "page-sequence";
	private static final String		TAG_FLOW = "flow";
	
	private static final TagContentDescriptor	TAG_DIV = new TagContentDescriptor("block");
	private static final TagContentDescriptor	TAG_CAPTION1 = new TagContentDescriptor("block","font-size","24pt","font-weight","bold","space-after","0.5cm","space-after.conditionality","retain");
	private static final TagContentDescriptor	TAG_CAPTION2 = new TagContentDescriptor("block","font-size","20pt","font-weight","bold","space-after","0.5cm","space-after.conditionality","retain");
	private static final TagContentDescriptor	TAG_CAPTION3 = new TagContentDescriptor("block","font-size","18pt","font-weight","bold","space-after","0.5cm","space-after.conditionality","retain");
	private static final TagContentDescriptor	TAG_CAPTION4 = new TagContentDescriptor("block","font-size","16pt","font-weight","bold","space-after","0.5cm","space-after.conditionality","retain");
	private static final TagContentDescriptor	TAG_CAPTION5 = new TagContentDescriptor("block","font-size","14pt","font-weight","bold","space-after","0.5cm","space-after.conditionality","retain");
	private static final TagContentDescriptor	TAG_CAPTION6 = new TagContentDescriptor("block","font-size","12pt","font-weight","bold","space-after","0.5cm","space-after.conditionality","retain");
	private static final TagContentDescriptor[]	TAG_CAPTIONS = {TAG_CAPTION1, TAG_CAPTION2, TAG_CAPTION3, TAG_CAPTION4, TAG_CAPTION5, TAG_CAPTION6};
	private static final TagContentDescriptor	TAG_TABLE = new TagContentDescriptor("table");
	private static final TagContentDescriptor	TAG_TABLE_HEADER = new TagContentDescriptor("table-header");
	private static final TagContentDescriptor	TAG_TABLE_BODY = new TagContentDescriptor("table-body");
	private static final TagContentDescriptor	TAG_TR = new TagContentDescriptor("table-row");
	private static final TagContentDescriptor	TAG_TH = new TagContentDescriptor("table-cell","font-weight","bold","border","solid 1pt black","text-align","center","font-size","10pt");
	private static final TagContentDescriptor	TAG_TD = new TagContentDescriptor("table-cell","border","solid 1pt black","font-size","10pt");
	private static final TagContentDescriptor	TAG_HR = new TagContentDescriptor("leader","leader-pattern","rule","leader-length","100%");
	private static final TagContentDescriptor	TAG_P = new TagContentDescriptor("block","font-size","10pt","font-weight","normal","space-after","0.2cm","space-after.conditionality","retain");
	private static final TagContentDescriptor	TAG_BOLD = new TagContentDescriptor("inline","font-weight","bold");
	private static final TagContentDescriptor	TAG_ITALIC = new TagContentDescriptor("inline","font-style","italic");
	private static final TagContentDescriptor	TAG_IMG = new TagContentDescriptor("external-graphic");
	private static final TagContentDescriptor	TAG_LINK = new TagContentDescriptor("basic-link","color","blue");
	private static final TagContentDescriptor	TAG_LI_BLOCK = new TagContentDescriptor("list-block","provisional-label-separation","5mm","provisional-distance-between-starts","15mm");
	private static final TagContentDescriptor	TAG_LI_ITEM = new TagContentDescriptor("list-item");
	private static final TagContentDescriptor	TAG_LI_ITEM_LABEL = new TagContentDescriptor("list-item-label");
	private static final TagContentDescriptor	TAG_LI_ITEM_BODY = new TagContentDescriptor("list-item-body");
	private static final TagContentDescriptor	TAG_LI_ITEM_BLOCK = new TagContentDescriptor("block","linefeed-treatment","preserve","margin-left","5mm");

	private static final String		ATTR_SRC = "src";
	private static final String		ATTR_HREF_LOCAL = "internal-destination";
	private static final String		ATTR_HREF_EXTERNAL = "external-destination";
	private static final String		ATTR_LINEFEED = "linefeed-treatment";
	private static final String		ATTR_LIST_MARGIN = "margin-left";

	private static final String		ATTR_PROLOGUE_MARGIN_RIGHT = "margin-right";
	private static final String		ATTR_PROLOGUE_MARGIN_LEFT = "margin-left";
	private static final String		ATTR_PROLOGUE_MARGIN_BOTTOM = "margin-bottom";
	private static final String		ATTR_PROLOGUE_MARGIN_TOP = "margin-top";
	private static final String		ATTR_PROLOGUE_FONT_FAMILY = "font-family";
	private static final String		ATTR_PROLOGUE_PAGE_WIDTH = "page-width";
	private static final String		ATTR_PROLOGUE_PAGE_HEIGHT = "page-height";
	private static final String		ATTR_PROLOGUE_MASTER_NAME = "master-name";
	private static final String		ATTR_PROLOGUE_EXTENT = "extent";
	private static final String		ATTR_MASTER_REFERENCE = "master-reference";
	private static final String		ATTR_FLOW_NAME = "flow-name";

	private static final String		VALUE_PROLOGUE_MASTER_NAME = "main";
	private static final String		VALUE_LINEFEED_PRESERVE = "preserve";

	private static final char[]		ESC_LT = "&lt;".toCharArray();
	private static final char[]		ESC_GT = "&gt;".toCharArray();
	private static final char[]		ESC_AMP = "&amp;".toCharArray();
	private static final char[]		ESC_QUOT = "&quot;".toCharArray();
	
	private final Writer			nested;
	private final PrologueEpilogueMaster<XMLEventWriter,CreoleFOPOutputWriter> epilogue;
	private final XMLEventFactory 	eventFactory = XMLEventFactory.newInstance();
	private final XMLEventWriter 	writer;
	
	private int						listDepth = 0;
	
	CreoleFOPOutputWriter(final Writer nested, final PrologueEpilogueMaster<XMLEventWriter,CreoleFOPOutputWriter> prologue, final PrologueEpilogueMaster<XMLEventWriter,CreoleFOPOutputWriter> epilogue) throws IOException {
		this.nested = nested;
		this.epilogue = epilogue;
		try{this.writer = XMLOutputFactory.newInstance().createXMLEventWriter(nested);
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
	void internalWrite(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
		try{String	value = new String(content,from,to-from);
			
			if (!keepNewLines && value.endsWith("\r\n")) {
				writer.add(eventFactory.createCharacters(value.substring(0,value.length()-2)+" "));
			}
			else if (!keepNewLines && value.endsWith("\n")) {
				writer.add(eventFactory.createCharacters(value.substring(0,value.length()-1)+" "));
			}
			else {
				writer.add(eventFactory.createCharacters(value));
			}
		} catch (XMLStreamException e) {
			throw new IOException("I/O error writing content: "+e.getLocalizedMessage());
		}
	}

	@Override
	void internalWriteEscaped(long displacement, char[] content, int from, int to, boolean keepNewLines) throws IOException, SyntaxException {
		boolean	has2escape = false;
		
		for (int index = from; index <= to; index++) {
			if (content[index] == '<' || content[index] == '>' || content[index] == '&' || content[index] == '\"') {
				has2escape = true;
				break;
			}
		}
		if (has2escape) {
			int 	start = from;
			
			for (int index = from; index <= to; index++) {
				if (content[index] == '<' || content[index] == '>' || content[index] == '&' || content[index] == '\"') {
					internalWrite(displacement+start-from,content,start,index,keepNewLines);
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
				internalWrite(displacement+start-from,content,start,to,keepNewLines);
			}
		}
		else {
			internalWrite(displacement,content,from,to,keepNewLines);
		}
	}
	
	@Override
	void insertImage(final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		writeStartTag(TAG_IMG);
		writeAttr(ATTR_SRC,new String(data,startLink,endLink-startLink));
		writeEndTag(TAG_IMG);
	}

	@Override
	void insertLink(final boolean localRef, final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		if (localRef) {
			if (startCaption == endCaption) {
				writeStartTag(TAG_LINK);
				writeAttr(ATTR_HREF_LOCAL,new String(data,startLink,endLink-startLink));
				internalWrite(displacement,data,startLink,endLink,false);
				writeEndTag(TAG_LINK);
			}
			else {
				writeStartTag(TAG_LINK);
				writeAttr(ATTR_HREF_LOCAL,new String(data,startLink,endLink-startLink));
				internalWrite(displacement,data,startCaption,endCaption,false);
				writeEndTag(TAG_LINK);
			}
		}
		else {
			if (startCaption == endCaption) {
				writeStartTag(TAG_LINK);
				writeAttr(ATTR_HREF_EXTERNAL,new String(data,startLink,endLink-startLink));
				internalWrite(displacement,data,startLink,endLink,false);
				writeEndTag(TAG_LINK);
			}
			else {
				writeStartTag(TAG_LINK);
				writeAttr(ATTR_HREF_EXTERNAL,new String(data,startLink,endLink-startLink));
				internalWrite(displacement,data,startCaption,endCaption,false);
				writeEndTag(TAG_LINK);
			}
		}
	}

	@Override
	protected void processSection(final FSM<CreoleTerminals, SectionState, SectionActions, Long> fsm, final CreoleTerminals terminal, final SectionState fromState, final SectionState toState, final SectionActions[] action, final Long parameter) throws FlowException {
		try{for (SectionActions item : action) {
				switch (item) {
					case DIV_OPEN		: 
						writeStartTag(TAG_DIV); 
						writeAttr(ATTR_PROLOGUE_FONT_FAMILY,"monospace");
						break;
					case DIV_CLOSE		: writeEndTag(TAG_DIV); break;
					case P_OPEN			: 
						writeStartTag(TAG_P);
						writeAttr(ATTR_LINEFEED,VALUE_LINEFEED_PRESERVE);
						break;
					case P_CLOSE		: writeEndTag(TAG_P); break;
					case H_OPEN			: writeStartTag(TAG_CAPTIONS[parameter.intValue()]); break;
					case H_CLOSE		: writeEndTag(TAG_CAPTIONS[parameter.intValue()]); break;
					case HR				: writeStartTag(TAG_HR); writeEndTag(TAG_HR); break;
					case UL_OPEN : case OL_OPEN : 
						if (listDepth++ > 0) {
							writeStartLi(EMPTY_LIST);
						}
						writeStartTag(TAG_LI_BLOCK);
						writeAttr(ATTR_LIST_MARGIN,(3*listDepth)+"mm");
						break;
					case UL_CLOSE : case OL_CLOSE :
						writeEndTag(TAG_LI_BLOCK);
						if (--listDepth != 0) {
							writeEndLi();
						}
						break;
					case LI_OPEN		: 
						writeStartLi(ASTERISK_LIST); 
						writeStartTag(TAG_LI_ITEM_BLOCK); 
						break;	
					case LI_CLOSE		: writeEndTag(TAG_LI_ITEM_BLOCK); writeEndLi(); break;
					case TABLE_OPEN		: writeStartTag(TAG_TABLE); break;
					case TABLE_CLOSE	: writeEndTag(TAG_TABLE); break;
					case THEAD_OPEN		: writeStartTag(TAG_TABLE_HEADER); break;
					case THEAD_CLOSE	: writeEndTag(TAG_TABLE_HEADER); break;
					case TBODY_OPEN		: writeStartTag(TAG_TABLE_BODY); break;
					case TBODY_CLOSE	: writeEndTag(TAG_TABLE_BODY); break;
					case TR_OPEN		: writeStartTag(TAG_TR); break;
					case TR_CLOSE		: writeEndTag(TAG_TR); break;
					case TH_OPEN		: writeStartTag(TAG_TH); writeStartTag(TAG_DIV); break;
					case TH_CLOSE		: writeEndTag(TAG_DIV); writeEndTag(TAG_TH); break;
					case TD_OPEN		: 
						writeStartTag(TAG_TD); 
						writeStartTag(TAG_DIV); 
						writeAttr(ATTR_LINEFEED,VALUE_LINEFEED_PRESERVE);
						break;
					case TD_CLOSE		: writeEndTag(TAG_DIV); writeEndTag(TAG_TD); break;
					default :
				}
			}
		} catch (IOException | SyntaxException e) {
			throw new FlowException(e.getLocalizedMessage());
		}
	}

	@Override
	protected void processFont(final FSM<CreoleTerminals, FontState, FontActions, Long> fsm, final CreoleTerminals terminal, final FontState fromState, final FontState toState, final FontActions[] action, final Long parameter) throws FlowException {
		try{for (FontActions item : action) {
				switch (item) {
					case BOLD_OPEN		: writeStartTag(TAG_BOLD); break;
					case BOLD_CLOSE		: writeEndTag(TAG_BOLD); break;
					case ITALIC_OPEN	: writeStartTag(TAG_ITALIC); break;
					case ITALIC_CLOSE	: writeEndTag(TAG_ITALIC); break;
					case BR				:
						writer.add(eventFactory.createCharacters("\n"));
						break;
					default :
				}
			}
		} catch (IOException | XMLStreamException e) {
			throw new FlowException(e.getLocalizedMessage());
		}
	}
	
	private void writeStartTag(final String tag) throws IOException {
		try{this.writer.add(eventFactory.createStartElement(PREFIX,NAMESPACE,tag));
		} catch (XMLStreamException e) {
			throw new IOException("I/O error writing tag ["+tag+"] : "+e.getLocalizedMessage());
		}
	}

	private void writeStartTag(final TagContentDescriptor desc) throws IOException {
		try{this.writer.add(eventFactory.createStartElement(PREFIX,NAMESPACE,desc.tagName));
			for (String[] item : desc.properties) {
				this.writer.add(eventFactory.createAttribute(item[0],item[1]));
			}
		} catch (XMLStreamException e) {
			throw new IOException("I/O error writing tag ["+desc.tagName+"] : "+e.getLocalizedMessage());
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

	private void writeEndTag(final TagContentDescriptor desc) throws IOException {
		try{this.writer.add(eventFactory.createEndElement(PREFIX,NAMESPACE,desc.tagName));
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new IOException("I/O error writing tag ["+desc.tagName+"] : "+e.getLocalizedMessage());
		}
	}

	private void writeStartLi(char[] mark) throws IOException, SyntaxException {
		writeStartTag(TAG_LI_ITEM);
		writeStartTag(TAG_LI_ITEM_LABEL);
		writeStartTag(TAG_DIV);
		internalWrite(currentDispl,mark,0,mark.length,false);
		writeEndTag(TAG_DIV);
		writeEndTag(TAG_LI_ITEM_LABEL);
		writeStartTag(TAG_LI_ITEM_BODY);
	}

	private void writeEndLi() throws IOException, SyntaxException {
		writeEndTag(TAG_LI_ITEM_BODY);
		writeEndTag(TAG_LI_ITEM);
	}
	
	private String writePrologue() throws IOException {
		writeStartTag(TAG_PROLOGUE_LAYOUT_MASTER_SET);
		
		writeStartTag(TAG_PROLOGUE_SIMPLE_PAGE_MASTER);
		writeAttr(ATTR_PROLOGUE_MARGIN_RIGHT,"2cm");
		writeAttr(ATTR_PROLOGUE_MARGIN_LEFT,"2cm");
		writeAttr(ATTR_PROLOGUE_MARGIN_BOTTOM,"1cm");
		writeAttr(ATTR_PROLOGUE_MARGIN_TOP,"0.5cm");
		writeAttr(ATTR_PROLOGUE_PAGE_WIDTH,"21cm");
		writeAttr(ATTR_PROLOGUE_PAGE_HEIGHT,"29.7cm");
		writeAttr(ATTR_PROLOGUE_MASTER_NAME,VALUE_PROLOGUE_MASTER_NAME);

		writeStartTag(TAG_PROLOGUE_REGION_BODY);
		writeAttr(ATTR_PROLOGUE_MARGIN_BOTTOM,"1cm");
		writeAttr(ATTR_PROLOGUE_MARGIN_TOP,"1cm");
		writeEndTag(TAG_PROLOGUE_REGION_BODY);
		
		writeStartTag(TAG_PROLOGUE_REGION_BEFORE);
		writeAttr(ATTR_PROLOGUE_EXTENT,"1.5cm");
		writeEndTag(TAG_PROLOGUE_REGION_BEFORE);
		
		writeEndTag(TAG_PROLOGUE_SIMPLE_PAGE_MASTER);
		writeEndTag(TAG_PROLOGUE_LAYOUT_MASTER_SET);
		
		return VALUE_PROLOGUE_MASTER_NAME;
	}
	
	static PrologueEpilogueMaster<XMLEventWriter,CreoleFOPOutputWriter> getPrologue() {
		return new PrologueEpilogueMaster<XMLEventWriter,CreoleFOPOutputWriter>(){
			@Override
			public boolean writeContent(XMLEventWriter writer, CreoleFOPOutputWriter instance) throws IOException {
				final String	masterRef;
				
				try{writer.setDefaultNamespace(NAMESPACE);
					writer.add(instance.eventFactory.createStartDocument());
					instance.writeStartTag(TAG_ROOT);
					writer.add(instance.eventFactory.createNamespace(PREFIX,NAMESPACE));
					masterRef = instance.writePrologue();
					instance.writeStartTag(TAG_PAGE_SEQUENCE);
					instance.writeAttr(ATTR_MASTER_REFERENCE,masterRef);
					instance.writeStartTag(TAG_FLOW);
					instance.writeAttr(ATTR_FLOW_NAME,"xsl-region-body");
					return false;
				} catch (XMLStreamException e) {
					throw new IOException(e.getLocalizedMessage(),e);
				}
			}
		};
	}

	static PrologueEpilogueMaster<XMLEventWriter,CreoleFOPOutputWriter> getEpilogue() {
		return new PrologueEpilogueMaster<XMLEventWriter,CreoleFOPOutputWriter>(){
			@Override
			public boolean writeContent(XMLEventWriter writer, CreoleFOPOutputWriter instance) throws IOException {
			    instance.writeEndTag(TAG_FLOW); 
			    instance.writeEndTag(TAG_PAGE_SEQUENCE); 
			    instance.writeEndTag(TAG_ROOT); 
				return false;
			}
		};
	}
	
	static PrologueEpilogueMaster<XMLEventWriter,CreoleFOPOutputWriter> getPrologue(final URI source) throws NullPointerException, ContentException {
		if (source == null) {
			throw new NullPointerException("Source URI can't be null"); 
		}
		else {
			try{final String	content = Utils.fromResource(source.toURL());
			
				return new PrologueEpilogueMaster<XMLEventWriter,CreoleFOPOutputWriter>(){
					@Override
					public boolean writeContent(XMLEventWriter writer, CreoleFOPOutputWriter instance) throws IOException {
						final String	masterRef;
						
						try{writer.setDefaultNamespace(NAMESPACE);
							writer.add(instance.eventFactory.createStartDocument());
							instance.writeStartTag(TAG_ROOT);
							writer.add(instance.eventFactory.createNamespace(PREFIX,NAMESPACE));
							masterRef = instance.writePrologue();
							instance.writeStartTag(TAG_PAGE_SEQUENCE);
							instance.writeAttr(ATTR_MASTER_REFERENCE,masterRef);
							instance.writeStartTag(TAG_FLOW);
							instance.writeAttr(ATTR_FLOW_NAME,"xsl-region-body");
							writer.add(instance.eventFactory.createCharacters(content));
							return false;
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

	static PrologueEpilogueMaster<XMLEventWriter,CreoleFOPOutputWriter> getEpilogue(final URI source) throws NullPointerException, ContentException {
		if (source == null) {
			throw new NullPointerException("Source URI can't be null"); 
		}
		else {
			try{final String	content = Utils.fromResource(source.toURL());
			
				return new PrologueEpilogueMaster<XMLEventWriter,CreoleFOPOutputWriter>(){
					@Override
					public boolean writeContent(XMLEventWriter writer, CreoleFOPOutputWriter instance) throws IOException {
						try{writer.add(instance.eventFactory.createCharacters(content));
							instance.writeEndTag(TAG_FLOW); 
						    instance.writeEndTag(TAG_PAGE_SEQUENCE); 
						    instance.writeEndTag(TAG_ROOT); 
							return false;
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
	
	
	private static class TagContentDescriptor {
		private final String		tagName;
		private final String[][]	properties;
		
		TagContentDescriptor(String tagName, String... propsAnsValues) {
			this.tagName = tagName;
			this.properties = new String[propsAnsValues.length/2][];
			for (int index = 0; index < this.properties.length; index++) {
				this.properties[index] = new String[]{propsAnsValues[2*index],propsAnsValues[2*index+1]};
			}
		}

		@Override
		public String toString() {
			return "TagContentDescriptor [tagName=" + tagName + ", properties=" + Arrays.toString(properties) + "]";
		}
	}

}