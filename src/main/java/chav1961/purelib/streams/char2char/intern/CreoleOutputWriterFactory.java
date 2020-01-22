package chav1961.purelib.streams.char2char.intern;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;
import chav1961.purelib.streams.interfaces.intern.CreoleMarkUpOutputWriter;

public class CreoleOutputWriterFactory {
	public static CreoleMarkUpOutputWriter<Long> getInstance(final MarkupOutputFormat format, final Writer nested) throws NullPointerException, IOException {
		if (format == null) {
			throw new NullPointerException("Markup output format can't be null");
		}
		else {
			CreoleMarkUpOutputWriter<Long> writer;
			
			switch (format) {
				case XML 		: writer = new CreoleXMLOutputWriter(nested,getPrologue(format),getEpilogue(format)); break;
				case XML2TEXT	: writer = new CreoleTextOutputWriter(nested,getPrologue(format),getEpilogue(format)); break;
				case XML2HTML	: writer = new CreoleHTMLOutputWriter(nested,getPrologue(format),getEpilogue(format)); break;
				case XML2PDF	: writer = new CreoleFOPOutputWriter(nested,getPrologue(format),getEpilogue(format)); break;
				case PARSEDCSV	: writer = new CreoleHighlighterWriter(nested,getPrologue(format),getEpilogue(format)); break;
				default : throw new UnsupportedOperationException("Output format ["+format+"] is not implemented yet"); 
			}
			return writer;
		}
	}
	
	public static <Wr,T> PrologueEpilogueMaster<Wr,T> getPrologue(final MarkupOutputFormat format) throws NullPointerException {
		if (format == null) {
			throw new NullPointerException("Markup output format can't be null");
		}
		else {
			switch (format) {
				case XML 		: return (PrologueEpilogueMaster<Wr, T>) CreoleXMLOutputWriter.getPrologue();  
				case XML2TEXT	: return (PrologueEpilogueMaster<Wr, T>) CreoleTextOutputWriter.getPrologue();
				case XML2HTML	: return (PrologueEpilogueMaster<Wr, T>) CreoleHTMLOutputWriter.getPrologue();
				case XML2PDF	: return (PrologueEpilogueMaster<Wr, T>) CreoleFOPOutputWriter.getPrologue();
				case PARSEDCSV	: return (PrologueEpilogueMaster<Wr, T>) CreoleHighlighterWriter.getPrologue();
				default : throw new UnsupportedOperationException("Output format ["+format+"] is not implemented yet"); 
			}
		}
	}

	public static <Wr,T> PrologueEpilogueMaster<Wr,T> getEpilogue(final MarkupOutputFormat format) throws NullPointerException {
		if (format == null) {
			throw new NullPointerException("Markup output format can't be null");
		}
		else {
			switch (format) {
				case XML 		: return (PrologueEpilogueMaster<Wr, T>) CreoleXMLOutputWriter.getEpilogue();  
				case XML2TEXT	: return (PrologueEpilogueMaster<Wr, T>) CreoleTextOutputWriter.getEpilogue();
				case XML2HTML	: return (PrologueEpilogueMaster<Wr, T>) CreoleHTMLOutputWriter.getEpilogue();
				case XML2PDF	: return (PrologueEpilogueMaster<Wr, T>) CreoleFOPOutputWriter.getEpilogue();
				case PARSEDCSV	: return (PrologueEpilogueMaster<Wr, T>) CreoleHighlighterWriter.getEpilogue();
				default : throw new UnsupportedOperationException("Output format ["+format+"] is not implemented yet"); 
			}
		}
	}

	public static <Wr,T> PrologueEpilogueMaster<Wr,T> getPrologue(final MarkupOutputFormat format, final URI source) throws NullPointerException, ContentException {
		if (format == null) {
			throw new NullPointerException("Markup output format can't be null");
		}
		else {
			switch (format) {
				case XML 		: return (PrologueEpilogueMaster<Wr, T>) CreoleXMLOutputWriter.getPrologue(source);  
				case XML2TEXT	: return (PrologueEpilogueMaster<Wr, T>) CreoleTextOutputWriter.getPrologue(source);
				case XML2HTML	: return (PrologueEpilogueMaster<Wr, T>) CreoleHTMLOutputWriter.getPrologue(source);
				case XML2PDF	: return (PrologueEpilogueMaster<Wr, T>) CreoleFOPOutputWriter.getPrologue(source);
				case PARSEDCSV	: return (PrologueEpilogueMaster<Wr, T>) CreoleHighlighterWriter.getPrologue(source);
				default : throw new UnsupportedOperationException("Output format ["+format+"] is not implemented yet"); 
			}
		}
	}

	public static <Wr,T> PrologueEpilogueMaster<Wr,T> getEpilogue(final MarkupOutputFormat format, final URI source) throws NullPointerException, ContentException {
		if (format == null) {
			throw new NullPointerException("Markup output format can't be null");
		}
		else {
			switch (format) {
				case XML 		: return (PrologueEpilogueMaster<Wr, T>) CreoleXMLOutputWriter.getEpilogue(source);  
				case XML2TEXT	: return (PrologueEpilogueMaster<Wr, T>) CreoleTextOutputWriter.getEpilogue(source);
				case XML2HTML	: return (PrologueEpilogueMaster<Wr, T>) CreoleHTMLOutputWriter.getEpilogue(source);
				case XML2PDF	: return (PrologueEpilogueMaster<Wr, T>) CreoleFOPOutputWriter.getEpilogue(source);
				case PARSEDCSV	: return (PrologueEpilogueMaster<Wr, T>) CreoleHighlighterWriter.getEpilogue(source);
				default : throw new UnsupportedOperationException("Output format ["+format+"] is not implemented yet"); 
			}
		}
	}
}
