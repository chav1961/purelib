package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Writer;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleTerminals;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;

class CreoleHTMLOutputWriter extends CreoleOutputWriter {
	
	private static final char[]		DIV_OPEN = "<div class=\"cwr\">".toCharArray();
	private static final char[]		DIV_CLOSE = "</div>".toCharArray();
	private static final char[]		BOLD_OPEN = "<strong>".toCharArray();
	private static final char[]		BOLD_CLOSE = "</strong>".toCharArray();
	private static final char[]		ITALIC_OPEN = "<em>".toCharArray();
	private static final char[]		ITALIC_CLOSE = "</em>".toCharArray();
	private static final char[]		UL_OPEN = "<ul>".toCharArray();
	private static final char[]		UL_CLOSE = "</ul>".toCharArray();
	private static final char[]		OL_OPEN = "<ol>".toCharArray();
	private static final char[]		OL_CLOSE = "</ol>".toCharArray();
	private static final char[]		LI_OPEN = "<li>".toCharArray();
	private static final char[]		LI_CLOSE = "</li>".toCharArray();
	private static final char[]		P_OPEN = "<p>".toCharArray();
	private static final char[]		P_CLOSE = "</p>".toCharArray();
	private static final char[]		TABLE_OPEN = "<table>".toCharArray();
	private static final char[]		TABLE_CLOSE = "</table>".toCharArray();
	private static final char[]		THEAD_OPEN = "<thead>".toCharArray();
	private static final char[]		THEAD_CLOSE = "</thead>".toCharArray();
	private static final char[]		TBODY_OPEN = "<tbody>".toCharArray();
	private static final char[]		TBODY_CLOSE = "</tbody>".toCharArray();
	private static final char[]		TR_OPEN = "<tr>".toCharArray();
	private static final char[]		TR_CLOSE = "</tr>".toCharArray();
	private static final char[]		TH_OPEN = "<th>".toCharArray();
	private static final char[]		TH_CLOSE = "</th>".toCharArray();
	private static final char[]		TD_OPEN = "<td>".toCharArray();
	private static final char[]		TD_CLOSE = "</td>".toCharArray();
	private static final char[][]	H_OPEN = {"<h1>".toCharArray(),"<h2>".toCharArray(),"<h3>".toCharArray(),"<h4>".toCharArray(),"<h5>".toCharArray(),"<h6>".toCharArray(),};
	private static final char[][]	H_CLOSE = {"</h1>".toCharArray(),"</h2>".toCharArray(),"</h3>".toCharArray(),"</h4>".toCharArray(),"</h5>".toCharArray(),"</h6>".toCharArray()};
	private static final char[]		BR = "<br/>".toCharArray();
	private static final char[]		HR = "<hr/>".toCharArray();
	private static final char[]		A_START = "<a href=\"".toCharArray();
	private static final char[]		A_START_LOCAL = "<a href=\"http://www.examplewiki.com/".toCharArray();
	private static final char[]		A_END = "\">".toCharArray();
	private static final char[]		A_CLOSE = "</a>".toCharArray();
	private static final char[]		IMG_START = "<img src=\"".toCharArray();
	private static final char[]		IMG_END = "\">".toCharArray();
	private static final char[]		IMG_CLOSE = "</img>".toCharArray();
	
	private final Writer			nested;
	private final PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter>	epilogue;
	
	CreoleHTMLOutputWriter(final Writer nested, final PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter> prologue, final PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter> epilogue) throws IOException {
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
	void internalWrite(final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
		try {nested.write(content,from,to-from);
		} catch (IndexOutOfBoundsException exc) {
			exc.printStackTrace();
		}
	}


	@Override
	protected void processSection(final FSM<CreoleTerminals,SectionState,SectionActions,Integer> fsm,final CreoleTerminals terminal,final SectionState fromState,final SectionState toState,final SectionActions[] action,final Integer parameter) throws FlowException {
		try{for (SectionActions item : action) {
				switch (item) {
					case DIV_OPEN		: internalWrite(DIV_OPEN); break;
					case DIV_CLOSE		: internalWrite(DIV_CLOSE); break;
					case P_OPEN			: internalWrite(P_OPEN); break;
					case P_CLOSE		: internalWrite(P_CLOSE); break;
					case H_OPEN			: internalWrite(H_OPEN[parameter]); break;
					case H_CLOSE		: internalWrite(H_CLOSE[parameter]); break;
					case HR				: internalWrite(HR); break;
					case UL_OPEN		: internalWrite(UL_OPEN); break;
					case UL_CLOSE		: internalWrite(UL_CLOSE); break;
					case OL_OPEN		: internalWrite(OL_OPEN); break;
					case OL_CLOSE		: internalWrite(OL_CLOSE); break;
					case LI_OPEN		: internalWrite(LI_OPEN); break;	
					case LI_CLOSE		: internalWrite(LI_CLOSE); break;
					case TABLE_OPEN		: internalWrite(TABLE_OPEN); break;
					case TABLE_CLOSE	: internalWrite(TABLE_CLOSE); break;
					case THEAD_OPEN		: internalWrite(THEAD_OPEN); break;
					case THEAD_CLOSE	: internalWrite(THEAD_CLOSE); break;
					case TBODY_OPEN		: internalWrite(TBODY_OPEN); break;
					case TBODY_CLOSE	: internalWrite(TBODY_CLOSE); break;
					case TR_OPEN		: internalWrite(TR_OPEN); break;
					case TR_CLOSE		: internalWrite(TR_CLOSE); break;
					case TH_OPEN		: internalWrite(TH_OPEN); break;
					case TH_CLOSE		: internalWrite(TH_CLOSE); break;
					case TD_OPEN		: internalWrite(TD_OPEN); break;
					case TD_CLOSE		: internalWrite(TD_CLOSE); break;
					default :
				}
			}
		} catch (IOException | SyntaxException e) {
			throw new FlowException(e.getLocalizedMessage());
		}
	}
	
	@Override
	protected void processFont(final FSM<CreoleTerminals,FontState,FontActions,Integer> fsm,final CreoleTerminals terminal,final FontState fromState,final FontState toState,final FontActions[] action,final Integer parameter) throws FlowException {
		try{for (FontActions item : action) {
				switch (item) {
					case BOLD_OPEN		: internalWrite(BOLD_OPEN); break;
					case BOLD_CLOSE		: internalWrite(BOLD_CLOSE); break;
					case ITALIC_OPEN	: internalWrite(ITALIC_OPEN); break;
					case ITALIC_CLOSE	: internalWrite(ITALIC_CLOSE); break;
					case BR				: internalWrite(BR); break;
					default :
				}
			}
		} catch (IOException | SyntaxException e) {
			throw new FlowException(e.getLocalizedMessage());
		}
	}

	@Override
	void insertImage(final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		internalWrite(IMG_START);
		internalWrite(data,startLink,endLink,false);
		internalWrite(IMG_END);
		internalWrite(data,startCaption,endCaption,false);
		internalWrite(IMG_CLOSE);
	}

	@Override
	void insertLink(final boolean localRef, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		if (localRef) {
			if (startCaption == endCaption) {
				internalWrite(A_START_LOCAL);
				internalWrite(data,startLink,endLink,false);
				internalWrite(A_END);
				internalWrite(data,startLink,endLink,false);
				internalWrite(A_CLOSE);
			}
			else {
				internalWrite(A_START_LOCAL);
				internalWrite(data,startLink,endLink,false);
				internalWrite(A_END);
				internalWrite(data,startCaption,endCaption,false);
				internalWrite(A_CLOSE);
			}
		}
		else {
			if (startCaption == endCaption) {
				internalWrite(A_START);
				internalWrite(data,startLink,endLink,false);
				internalWrite(A_END);
				internalWrite(data,startLink,endLink,false);
				internalWrite(A_CLOSE);
			}
			else {
				internalWrite(A_START);
				internalWrite(data,startLink,endLink,false);
				internalWrite(A_END);
				internalWrite(data,startCaption,endCaption,false);
				internalWrite(A_CLOSE);
			}
		}
	}
	
//	private String asPState(final int state) {
//		switch (state) {
//			case PS_INITIAL					: return "PS_INITIAL";
//			case PS_CANBE_PARAGRAPH			: return "PS_CANBE_PARAGRAPH"; 
//			case PS_PARAGRAPH				: return "PS_PARAGRAPH"; 
//			case PS_AFTER_PARAGRAPH			: return "PS_AFTER_PARAGRAPH"; 
//			case PS_CANBE_NEXT_PARAGRAPH	: return "PS_CANBE_NEXT_PARAGRAPH"; 
//			case PS_SKIP_LINES				: return "PS_SKIP_LINES"; 
//			case PS_CAPTION					: return "PS_CAPTION"; 
//			case PS_AFTER_CAPTION			: return "PS_AFTER_CAPTION"; 
//			case PS_LIST					: return "PS_LIST"; 
//			case PS_LIST_ITEM				: return "PS_LIST_ITEM"; 
//			case PS_AFTER_LIST_ITEM			: return "PS_AFTER_LIST_ITEM"; 
//			case PS_TABLE_HEADER			: return "PS_TABLE_HEADER"; 
//			case PS_AFTER_TABLE_HEADER		: return "PS_AFTER_TABLE_HEADER"; 
//			case PS_TABLE_DATA				: return "PS_TABLE_DATA"; 
//			case PS_AFTER_TABLE_DATA		: return "PS_AFTER_TABLE_DATA"; 
//			default :  return "PS_UNKNOWN";
//		}
//	}

	static PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter> getPrologue() {
		return new PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter>(){
			@Override
			public boolean writeContent(Writer writer, CreoleHTMLOutputWriter instance) throws IOException {
				writer.write("<html><head></head><body>");
				return false;
			}
		};
	}

	static PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter> getEpilogue() {
		return new PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter>(){
			@Override
			public boolean writeContent(Writer writer, CreoleHTMLOutputWriter instance) throws IOException {
				writer.write("</body></html>");
				return false;
			}
		};
	}
}