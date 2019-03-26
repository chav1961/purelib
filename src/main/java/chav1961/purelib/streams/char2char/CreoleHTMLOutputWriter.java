package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URLEncoder;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
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
	private static final char[]		A_START_LOCAL = "<a href=\"".toCharArray();
	private static final char[]		A_END = "\">".toCharArray();
	private static final char[]		A_CLOSE = "</a>".toCharArray();
	private static final char[]		IMG_START = "<img src=\"".toCharArray();
	private static final char[]		IMG_END = "\">".toCharArray();
	private static final char[]		IMG_CLOSE = "</img>".toCharArray();

	private static final char[]		ESC_LT = "&lt;".toCharArray();
	private static final char[]		ESC_GT = "&gt;".toCharArray();
	private static final char[]		ESC_AMP = "&amp;".toCharArray();
	private static final char[]		ESC_QUOT = "&quot;".toCharArray();
	
	private final Writer			nested;
	private final StringBuilder		internalAnchor = new StringBuilder();
	private final PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter>	epilogue;
	private boolean					storeAnchor = false;
	
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
	void internalWrite(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
		nested.write(content,from,to-from);
		if (storeAnchor) {
			internalAnchor.append(content,from,to-from);
		}
	}

	@Override
	void internalWriteEscaped(long displacement, char[] content, int from, int to, boolean keepNewLines) throws IOException, SyntaxException {
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
	protected void processSection(final FSM<CreoleTerminals,SectionState,SectionActions,Long> fsm,final CreoleTerminals terminal,final SectionState fromState,final SectionState toState,final SectionActions[] action,final Long parameter) throws FlowException {
		try{for (SectionActions item : action) {
				switch (item) {
					case DIV_OPEN		: internalWrite(currentDispl, DIV_OPEN); break;
					case DIV_CLOSE		: internalWrite(currentDispl, DIV_CLOSE); break;
					case P_OPEN			: internalWrite(currentDispl, P_OPEN); break;
					case P_CLOSE		: internalWrite(currentDispl, P_CLOSE); break;
					case H_OPEN			: 
						internalWrite(currentDispl, H_OPEN[parameter.intValue()]);
						storeAnchor = true;
						internalAnchor.setLength(0);
						break;
					case H_CLOSE		: 
						storeAnchor = false;
						internalWrite(currentDispl, ("<a id=\"" + URLEncoder.encode(internalAnchor.toString().trim().toLowerCase(),"UTF-8") + "\"></a>").toCharArray());
						internalWrite(currentDispl, H_CLOSE[parameter.intValue()]); 
						break;
					case HR				: internalWrite(currentDispl, HR); break;
					case UL_OPEN		: internalWrite(currentDispl, UL_OPEN); break;
					case UL_CLOSE		: internalWrite(currentDispl, UL_CLOSE); break;
					case OL_OPEN		: internalWrite(currentDispl, OL_OPEN); break;
					case OL_CLOSE		: internalWrite(currentDispl, OL_CLOSE); break;
					case LI_OPEN		: internalWrite(currentDispl, LI_OPEN); break;	
					case LI_CLOSE		: internalWrite(currentDispl, LI_CLOSE); break;
					case TABLE_OPEN		: internalWrite(currentDispl, TABLE_OPEN); break;
					case TABLE_CLOSE	: internalWrite(currentDispl, TABLE_CLOSE); break;
					case THEAD_OPEN		: internalWrite(currentDispl, THEAD_OPEN); break;
					case THEAD_CLOSE	: internalWrite(currentDispl, THEAD_CLOSE); break;
					case TBODY_OPEN		: internalWrite(currentDispl, TBODY_OPEN); break;
					case TBODY_CLOSE	: internalWrite(currentDispl, TBODY_CLOSE); break;
					case TR_OPEN		: internalWrite(currentDispl, TR_OPEN); break;
					case TR_CLOSE		: internalWrite(currentDispl, TR_CLOSE); break;
					case TH_OPEN		: internalWrite(currentDispl, TH_OPEN); break;
					case TH_CLOSE		: internalWrite(currentDispl, TH_CLOSE); break;
					case TD_OPEN		: internalWrite(currentDispl, TD_OPEN); break;
					case TD_CLOSE		: internalWrite(currentDispl, TD_CLOSE); break;
					default :
				}
			}
		} catch (IOException | SyntaxException e) {
			throw new FlowException(e.getLocalizedMessage());
		}
	}
	
	@Override
	protected void processFont(final FSM<CreoleTerminals,FontState,FontActions,Long> fsm,final CreoleTerminals terminal,final FontState fromState,final FontState toState,final FontActions[] action,final Long parameter) throws FlowException {
		try{for (FontActions item : action) {
				switch (item) {
					case BOLD_OPEN		: internalWrite(currentDispl, BOLD_OPEN); break;
					case BOLD_CLOSE		: internalWrite(currentDispl, BOLD_CLOSE); break;
					case ITALIC_OPEN	: internalWrite(currentDispl, ITALIC_OPEN); break;
					case ITALIC_CLOSE	: internalWrite(currentDispl, ITALIC_CLOSE); break;
					case BR				: internalWrite(currentDispl, BR); break;
					default :
				}
			}
		} catch (IOException | SyntaxException e) {
			throw new FlowException(e.getLocalizedMessage());
		}
	}

	@Override
	void insertImage(final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		internalWrite(displacement,IMG_START);
		internalWrite(displacement,data,startLink,endLink,false);
		internalWrite(displacement,IMG_END);
		internalWrite(displacement,data,startCaption,endCaption,false);
		internalWrite(displacement,IMG_CLOSE);
	}

	@Override
	void insertLink(final boolean localRef, final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		if (localRef) {
			final String	link;
			int				found = -1;
			
			for (int index = startLink; index <= endLink; index++) {
				if (data[index] == '#') {
					found = index;
					break;
				}
			}
			if (found != -1) {
				link = new String(data,startLink,found-startLink) + '#' + URLEncoder.encode(new String(data,found+1,endLink-found-1).trim().toLowerCase(),"UTF-8");
			}
			else {
				link = new String(data,startLink,endLink-startLink); 
			}
			
			if (startCaption == endCaption) {
				internalWrite(displacement,A_START_LOCAL);
				internalWrite(displacement,link.toCharArray(),0,link.length(),false);
				internalWrite(displacement,A_END);
				internalWrite(displacement,data,startLink,endLink,false);
				internalWrite(displacement,A_CLOSE);
			}
			else {
				internalWrite(displacement,A_START_LOCAL);
				internalWrite(displacement,link.toCharArray(),0,link.length(),false);
				internalWrite(displacement,A_END);
				internalWrite(displacement,data,startCaption,endCaption,false);
				internalWrite(displacement,A_CLOSE);
			}
		}
		else {
			if (startCaption == endCaption) {
				internalWrite(displacement,A_START);
				internalWrite(displacement,data,startLink,endLink,false);
				internalWrite(displacement,A_END);
				internalWrite(displacement,data,startLink,endLink,false);
				internalWrite(displacement,A_CLOSE);
			}
			else {
				internalWrite(displacement,A_START);
				internalWrite(displacement,data,startLink,endLink,false);
				internalWrite(displacement,A_END);
				internalWrite(displacement,data,startCaption,endCaption,false);
				internalWrite(displacement,A_CLOSE);
			}
		}
	}
	
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

	static PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter> getPrologue(final URI source) throws NullPointerException, ContentException {
		if (source == null) {
			throw new NullPointerException("Source URI can't be null"); 
		}
		else {
			try{final String	content = Utils.fromResource(source.toURL());
			
				return new PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter>(){
					@Override
					public boolean writeContent(Writer writer, CreoleHTMLOutputWriter instance) throws IOException {
						writer.write(content);
						return false;
					}
				};
			} catch (IOException e) {
				throw new ContentException("I/O error loading content from ["+source+"]: "+e.getLocalizedMessage());
			}
		}
	}

	static PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter> getEpilogue(final URI source) throws NullPointerException, ContentException {
		if (source == null) {
			throw new NullPointerException("Source URI can't be null"); 
		}
		else {
			try{final String	content = Utils.fromResource(source.toURL());
			
				return new PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter>(){
					@Override
					public boolean writeContent(Writer writer, CreoleHTMLOutputWriter instance) throws IOException {
						writer.write(content);
						return false;
					}
				};
			} catch (IOException e) {
				throw new ContentException("I/O error loading content from ["+source+"]: "+e.getLocalizedMessage());
			}
		}
	}
}