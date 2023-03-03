package chav1961.purelib.nanoservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;

public class StaticHelp implements AutoCloseable {
	private static final int	MAX_TRIES = 100;
	private static final Random	RANDOM = new Random(System.nanoTime()); 
	
	private final File	helpContent;
	
	public StaticHelp(final LoggerFacade logger, final URI contentReference, final String subdir) throws IOException {
		final File	tempDir = new File(System.getProperty("java.io.tmpdir"));
		
		this.helpContent = new File(tempDir, subdir);
		if (helpContent.exists()) {
			if (helpContent.isDirectory()) {
				Utils.deleteDir(helpContent);
			}
			else {
				helpContent.delete();
			}
		}
		helpContent.mkdirs();
		
		try(final InputStream	is = contentReference.toURL().openStream()) {

			try(final ZipInputStream	zis = new ZipInputStream(is)) {
				ZipEntry		ze;
				
				while ((ze = zis.getNextEntry()) != null) {
					final String	name = ze.getName();
					
					if (name.endsWith("/")) {
						new File(helpContent, name).mkdirs();
					}
					else {
						final File	f = new File(helpContent, name);
						
						f.getParentFile().mkdirs();
						try(final OutputStream	os = new FileOutputStream(f)) {
							Utils.copyStream(zis, os);
						}
					}
				}
			}
		} catch (IOException e) {
			Utils.deleteDir(helpContent);
			throw e;
		}
	}

	public URI getStaticHelpRoot() {
		return helpContent.getAbsoluteFile().toURI();
	}
	
	@Override
	public void close() throws EnvironmentException {
		if (helpContent.exists()) {
			Utils.deleteDir(helpContent);
		}
	}

	public static File createUnuqieName(final File parent, final String prefix) throws IOException {
		for (int index = 0; index < MAX_TRIES; index++) {
			final File	f = new File(parent, prefix+RANDOM.nextInt(1000000000));
			
			if (!f.exists()) {
				return f;
			}
		}
		throw new IOException("Attempt to create unique name ["+(new File(parent, prefix).getAbsolutePath()+"ZZZ")+"] failed");
	}
}
