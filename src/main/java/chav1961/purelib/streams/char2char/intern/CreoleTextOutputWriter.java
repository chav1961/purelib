package chav1961.purelib.streams.char2char.intern;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;
import chav1961.purelib.streams.interfaces.internal.CreoleFontActions;
import chav1961.purelib.streams.interfaces.internal.CreoleFontState;
import chav1961.purelib.streams.interfaces.internal.CreoleSectionActions;
import chav1961.purelib.streams.interfaces.internal.CreoleSectionState;
import chav1961.purelib.streams.interfaces.internal.CreoleTerminals;

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
	
	public CreoleTextOutputWriter(final Writer nested, final PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> prologue, final PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> epilogue) throws IOException {
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
		nested.write(content,from,to-from);
	}

	@Override
	public void writeEscaped(long displacement, char[] content, int from, int to, boolean keepNewLines) throws IOException, SyntaxException {
		write(displacement,content,from,to,keepNewLines);
	}	

	@Override
	public void insertImage(final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		if (startCaption < endCaption) {
			internalWrite(displacement,IMAGE_START);
			write(displacement,data,startLink,endLink,false);
			internalWrite(displacement,IMAGE_PART);
			write(displacement,data,startCaption,endCaption,false);
			internalWrite(displacement,IMAGE_END);
		}
		else {
			internalWrite(displacement,IMAGE_START);
			write(displacement,data,startLink,endLink,false);
			internalWrite(displacement,IMAGE_END);
		}
	}

	@Override
	public void insertLink(final boolean localRef, final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		if (startCaption < endCaption) {
			internalWrite(displacement,LINK_START);
			write(displacement,data,startLink,endLink,false);
			internalWrite(displacement,LINK_PART);
			write(displacement,data,startCaption,endCaption,false);
			internalWrite(displacement,LINK_END);
		}
		else {
			internalWrite(displacement,LINK_START);
			write(displacement,data,startLink,endLink,false);
			internalWrite(displacement,LINK_END);
		}
	}

	@Override
	public void processSection(final FSM<CreoleTerminals, CreoleSectionState, CreoleSectionActions, Long> fsm, final CreoleTerminals terminal, final CreoleSectionState fromState, final CreoleSectionState toState, final CreoleSectionActions[] action, final Long parameter) throws FlowException {
		try{for (CreoleSectionActions item : action) {
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
	public void processFont(final FSM<CreoleTerminals, CreoleFontState, CreoleFontActions, Long> fsm, final CreoleTerminals terminal, final CreoleFontState fromState, final CreoleFontState toState, final CreoleFontActions[] action, final Long parameter) throws FlowException {
		try{for (CreoleFontActions item : action) {
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
	
	public static PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> getPrologue() {
		return new PrologueEpilogueMaster<Writer,CreoleTextOutputWriter>(){
			@Override
			public boolean writeContent(Writer writer, CreoleTextOutputWriter instance) throws IOException {
				return true;
			}
		};
	}

	public static PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> getEpilogue() {
		return new PrologueEpilogueMaster<Writer,CreoleTextOutputWriter>(){
			@Override
			public boolean writeContent(Writer writer, CreoleTextOutputWriter instance) throws IOException {
				return true;
			}
		};
	}

	public static PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> getPrologue(final URI source) throws NullPointerException, ContentException {
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
	
	public static PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> getEpilogue(final URI source) throws NullPointerException, ContentException {
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