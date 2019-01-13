package chav1961.purelib.ui.swing.useful;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class JFileContentManipulator implements Closeable, LocaleChangeListener {
	@FunctionalInterface
	public interface InputStreamGetter {
		InputStream getContent();
	}

	@FunctionalInterface
	public interface OutputStreamGetter {
		OutputStream getContent();
	}

	public interface ProgressIndicator {
		void start(String caption);
		void start(String caption, long total);
		boolean processed(long processed);
		void end();
	}

	public JFileContentManipulator(final FileSystemInterface fsi, final String initialPath, final JTextComponent component) {
		this(fsi,initialPath,buildInputStreamGetter(component),buildOutputStreamGetter(component));
	}
	
	public JFileContentManipulator(final FileSystemInterface fsi, final String initialPath, final InputStreamGetter getterIn, final OutputStreamGetter getterOut) {
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
	}
	
	public boolean newFile() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean newFile(final ProgressIndicator progress) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean openFile() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean openFile(final ProgressIndicator progress) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean saveFile() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean saveFile(final ProgressIndicator progress) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean saveFileAs() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean saveFileAs(final ProgressIndicator progress) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String getCurrentNameOfTheFile() {
		// TODO Auto-generated method stub
		return "";
	}

	public String getCurrentPathOfTheFile() {
		// TODO Auto-generated method stub
		return "";
	}
	
	public void setModificationFlag() {
		// TODO Auto-generated method stub
	}
	
	public void clearModificationFlag() {
		// TODO Auto-generated method stub
	}

	static InputStreamGetter buildInputStreamGetter(final JTextComponent component) {
		// TODO Auto-generated method stub
		return null;
	}

	static OutputStreamGetter buildOutputStreamGetter(final JTextComponent component) {
		// TODO Auto-generated method stub
		return null;
	}
}
