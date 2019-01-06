package chav1961.purelib.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ORMSerializer;
import chav1961.purelib.streams.char2byte.AsmWriter;

public class ModelUtils {
	private static final AsmWriter		writer;
	private static final IOException	initExc;
	private static final AtomicInteger	uniqueSuffix = new AtomicInteger(0);
	
	static {
		AsmWriter	temp;
		IOException	tempExc;
		
		try{temp = new AsmWriter(new OutputStream(){@Override public void write(int b) throws IOException {}},new OutputStreamWriter(System.err));
			try(final InputStream	is = ModelUtils.class.getResourceAsStream("macros.txt");
				final Reader		rdr = new InputStreamReader(is)) {
				
				Utils.copyStream(rdr, temp);
			}
			tempExc = null;
		} catch (IOException exc) {
			temp = null;
			tempExc = exc;
		}
		writer = temp;
		initExc = tempExc;
	}
	
	public static <Key,Content> ORMSerializer<Key,Content> buildORMSerializer(final ContentMetadataInterface metadata, final ClassLoader deploy) {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			// TODO:
			return null;
		}
	}

	public static <Key,Content> ORMSerializer<Key,Content> buildResultsetSerializer(final ContentMetadataInterface metadata, final Class<Key> keyClass, final Class<Content> contentClass, final ClassLoader deploy) {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (keyClass == null) {
			throw new NullPointerException("Key class can't be null");
		}
		else if (contentClass == null) {
			throw new NullPointerException("Content class can't be null");
		}
		else if (deploy == null) {
			throw new NullPointerException("Loader to deploy to can't be null");
		}
		else if (initExc != null) {
			throw new PreparationException("Class ["+ModelUtils.class.getCanonicalName()+"]: error during static initialization: ("+initExc.getLocalizedMessage()+"). This class can't be used before solving problems",initExc);
		}
		else {
			// TODO:
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
				final Writer				wr = writer.clone(baos)) {
				final int					suffix = uniqueSuffix.incrementAndGet();
				
				wr.write(" makeIncludes \""+keyClass.getCanonicalName()+"\",\""+keyClass.getCanonicalName()+"\",\"");
			} catch (IOException e) {
			}
			return null;
		}
	}
}
