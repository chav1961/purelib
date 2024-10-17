package chav1961.purelib.streams.char2char.intern;

import java.io.IOException;
import java.io.Writer;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;
import chav1961.purelib.streams.interfaces.internal.CreoleFontActions;
import chav1961.purelib.streams.interfaces.internal.CreoleFontState;
import chav1961.purelib.streams.interfaces.internal.CreoleSectionActions;
import chav1961.purelib.streams.interfaces.internal.CreoleSectionState;
import chav1961.purelib.streams.interfaces.internal.CreoleTerminals;

class CreoleMarkdownWriter extends CreoleOutputWriter {
	
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
	private final PrologueEpilogueMaster<Writer,CreoleMarkdownWriter> epilogue;
	
	
	public CreoleMarkdownWriter(final Writer nested, final PrologueEpilogueMaster<Writer,CreoleMarkdownWriter> prologue, final PrologueEpilogueMaster<Writer,CreoleMarkdownWriter> epilogue) throws IOException {
		this.nested = nested;
		this.epilogue = epilogue;
			
		prologue.writeContent(nested,this);
	}

	@Override
	public void close() throws IOException {
		epilogue.writeContent(nested,this);
		nested.flush();
	}

	@Override
	public void write(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
		nested.write(content, from, to-from);		
	}

	@Override
	public void writeEscaped(long displacement, char[] content, int from, int to, boolean keepNewLines) throws IOException, SyntaxException {
		boolean	has2escape = false;
		
		for (int index = from; index < to; index++) {
			final char	current = content[index];
			
			if (current == '<' || current == '>' || current == '&' || current == '\"') {
				has2escape = true;
				break;
			}
		}
		if (has2escape) {
			int 	start = from;
			
			for (int index = from; index <= to; index++) {
				final char	current = content[index];
				
				if (current == '<' || current == '>' || current == '&' || current == '\"') {
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
		nested.write(tag);
	}

	private void writeAttr(final String key, final String value) throws IOException {
	}
	
	private void writeEndTag(final String tag) throws IOException {
		nested.write(tag);
	}
	
	public static PrologueEpilogueMaster<Writer,CreoleMarkdownWriter> getPrologue() {
		return new PrologueEpilogueMaster<Writer,CreoleMarkdownWriter>(){
			@Override
			public boolean writeContent(Writer writer, CreoleMarkdownWriter instance) throws IOException {
				return true;
			}
		};
	}

	public static PrologueEpilogueMaster<Writer,CreoleMarkdownWriter> getEpilogue() {
		return new PrologueEpilogueMaster<Writer,CreoleMarkdownWriter>(){
			@Override
			public boolean writeContent(Writer writer, CreoleMarkdownWriter instance) throws IOException {
				return true;
			}
		};
	}

}