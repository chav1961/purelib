package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleTerminals;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;

class CreoleTextOutputWriter extends CreoleOutputWriter {
	private static final char[][]	UL = {"*".toCharArray(),"**".toCharArray(),"***".toCharArray(),"****".toCharArray(),"*****".toCharArray()};
	private static final char[][]	OL = {"#".toCharArray(),"##".toCharArray(),"##".toCharArray(),"##".toCharArray(),"##".toCharArray()};
	private static final char[][]	H = {"=".toCharArray(),"==".toCharArray(),"===".toCharArray(),"====".toCharArray(),"=====".toCharArray(),"======".toCharArray()};
	private static final char[]	HR = "----".toCharArray();
	private static final char[]	B = "**".toCharArray();
	private static final char[]	I = "//".toCharArray();
	private static final char[]	BR = "\\\\".toCharArray();
	private static final char[]	LINK_START = "".toCharArray();
	private static final char[]	LINK_PART = "|".toCharArray();
	private static final char[]	LINK_END = "".toCharArray();
	private static final char[]	IMAGE_START = "".toCharArray();
	private static final char[]	IMAGE_PART = "|".toCharArray();
	private static final char[]	IMAGE_END = "".toCharArray();
	private static final char[]	TH = "|=".toCharArray();
	private static final char[]	TD = "|".toCharArray();
	
	private final Writer		nested;
	private final PrologueEpilogueMaster<Writer,CreoleTextOutputWriter>	epilogue;
	
	CreoleTextOutputWriter(final Writer nested, final PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> prologue, final PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> epilogue) throws IOException {
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
	}

	@Override
	void internalWriteEscaped(long displacement, char[] content, int from, int to, boolean keepNewLines) throws IOException, SyntaxException {
		internalWrite(displacement,content,from,to,keepNewLines);
	}	

	@Override
	void insertImage(final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		if (startCaption < endCaption) {
			internalWrite(displacement,IMAGE_START);
			internalWrite(displacement,data,startLink,endLink,false);
			internalWrite(displacement,IMAGE_PART);
			internalWrite(displacement,data,startCaption,endCaption,false);
			internalWrite(displacement,IMAGE_END);
		}
		else {
			internalWrite(displacement,IMAGE_START);
			internalWrite(displacement,data,startLink,endLink,false);
			internalWrite(displacement,IMAGE_END);
		}
	}

	@Override
	void insertLink(final boolean localRef, final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		if (startCaption < endCaption) {
			internalWrite(displacement,LINK_START);
			internalWrite(displacement,data,startLink,endLink,false);
			internalWrite(displacement,LINK_PART);
			internalWrite(displacement,data,startCaption,endCaption,false);
			internalWrite(displacement,LINK_END);
		}
		else {
			internalWrite(displacement,LINK_START);
			internalWrite(displacement,data,startLink,endLink,false);
			internalWrite(displacement,LINK_END);
		}
	}

	@Override
	protected void processSection(final FSM<CreoleTerminals, SectionState, SectionActions, Long> fsm, final CreoleTerminals terminal, final SectionState fromState, final SectionState toState, final SectionActions[] action, final Long parameter) throws FlowException {
		try{for (SectionActions item : action) {
				switch (item) {
					case H_OPEN	: case H_CLOSE		: 
						internalWrite(currentDispl,H[parameter.intValue()]); 
						break;
					case HR				:
						internalWrite(currentDispl,HR); 
						break;
					case UL_OPEN : case UL_CLOSE	:
						internalWrite(currentDispl,UL[parameter.intValue()]); 
						break;
					case OL_OPEN : case OL_CLOSE	:
						internalWrite(currentDispl,OL[parameter.intValue()]); 
						break;
					case TH_OPEN : 
						internalWrite(currentDispl,TH);
						break;
					case TD_OPEN :
						internalWrite(currentDispl,TD);
						break;
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
					case BOLD_OPEN : case BOLD_CLOSE : 
						internalWrite(currentDispl,B); 
						break;
					case ITALIC_OPEN : case ITALIC_CLOSE :
						internalWrite(currentDispl,I);
						break;
					case BR	: 
						internalWrite(currentDispl,BR); 
						break;
					default :
				}
			}
		} catch (IOException | SyntaxException e) {
			throw new FlowException(e.getLocalizedMessage());
		}
	}
	
	static PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> getPrologue() {
		return new PrologueEpilogueMaster<Writer,CreoleTextOutputWriter>(){
			@Override
			public boolean writeContent(Writer writer, CreoleTextOutputWriter instance) throws IOException {
				return true;
			}
		};
	}

	static PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> getEpilogue() {
		return new PrologueEpilogueMaster<Writer,CreoleTextOutputWriter>(){
			@Override
			public boolean writeContent(Writer writer, CreoleTextOutputWriter instance) throws IOException {
				return true;
			}
		};
	}

	static PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> getPrologue(final URI source) throws NullPointerException, ContentException {
		if (source == null) {
			throw new NullPointerException("Source URI can't be null"); 
		}
		else {
			try{final String	content = Utils.fromResource(source.toURL());
			
				return new PrologueEpilogueMaster<Writer,CreoleTextOutputWriter>(){
					@Override
					public boolean writeContent(Writer writer, CreoleTextOutputWriter instance) throws IOException {
						writer.write(content);
						return true;
					}
				};
			} catch (IOException e) {
				throw new ContentException("I/O error loading content from ["+source+"]: "+e.getLocalizedMessage());
			}
		}
	}
	
	static PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> getEpilogue(final URI source) throws NullPointerException, ContentException {
		if (source == null) {
			throw new NullPointerException("Source URI can't be null"); 
		}
		else {
			try{final String	content = Utils.fromResource(source.toURL());
			
				return new PrologueEpilogueMaster<Writer,CreoleTextOutputWriter>(){
					@Override
					public boolean writeContent(Writer writer, CreoleTextOutputWriter instance) throws IOException {
						writer.write(content);
						return true;
					}
				};
			} catch (IOException e) {
				throw new ContentException("I/O error loading content from ["+source+"]: "+e.getLocalizedMessage());
			}
		}
	}	
}