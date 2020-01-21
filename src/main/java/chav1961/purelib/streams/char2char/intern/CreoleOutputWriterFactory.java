package chav1961.purelib.streams.char2char.intern;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.stream.XMLEventWriter;

import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.streams.interfaces.MarkUpOutputWriter;

public class CreoleOutputWriterFactory {
	public static <T extends Enum<?>,P> MarkUpOutputWriter<T,P> getInstance(final MarkupOutputFormat format) throws NullPointerException {
		if (format == null) {
			throw new NullPointerException("Markup output format can't be null");
		}
		else {
				switch (format) {
					case PARSEDCSV	:
						break;
					case TEXT		:
						break;
					case XML		:
						break;
					case XML2HTML	:
						break;
					case XML2PDF	:
						break;
					case XML2TEXT	:
						break;
					default:
						throw new UnsupportedOperationException("Markup output format ["+format+"] is not supported yet");
				}
		}
		return null;
	}
	
	public static String getPrologue(final MarkupOutputFormat format) throws NullPointerException {
		if (format == null) {
			throw new NullPointerException("Markup output format can't be null");
		}
		else {
				switch (format) {
					case PARSEDCSV	:
						break;
					case TEXT		:
						break;
					case XML		:
						break;
					case XML2HTML	:
						break;
					case XML2PDF	:
						break;
					case XML2TEXT	:
						break;
					default:
						throw new UnsupportedOperationException("Markup output format ["+format+"] is not supported yet");
				}
		}
		return null;
	}

	public static String getEpilogue(final MarkupOutputFormat format) throws NullPointerException {
		if (format == null) {
			throw new NullPointerException("Markup output format can't be null");
		}
		else {
				switch (format) {
					case PARSEDCSV	:
						break;
					case TEXT		:
						break;
					case XML		:
						break;
					case XML2HTML	:
						break;
					case XML2PDF	:
						break;
					case XML2TEXT	:
						break;
					default:
						throw new UnsupportedOperationException("Markup output format ["+format+"] is not supported yet");
				}
		}
		return null;
	}
}
