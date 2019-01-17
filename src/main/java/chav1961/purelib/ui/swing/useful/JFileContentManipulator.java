package chav1961.purelib.ui.swing.useful;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import java.awt.Dialog;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class JFileContentManipulator implements Closeable, LocaleChangeListener {
	private static final ProgressIndicator	DUMMY = new ProgressIndicator() {
												@Override public void start(String caption) {}
												@Override public void start(String caption, long total) {}
												@Override public boolean processed(long processed) {return true;}
												@Override public void end() {}
											};

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
											
	private final FileSystemInterface	fsi;
	private final Localizer				localizer;
	private final InputStreamGetter		getterIn;
	private final OutputStreamGetter	getterOut;
	private boolean		wasChanged = false;
	private String		currentName = "";
	
	public JFileContentManipulator(final FileSystemInterface fsi, final Localizer localizer, final JTextComponent component) throws NullPointerException, IllegalArgumentException {
		this(fsi,localizer,buildInputStreamGetter(component),buildOutputStreamGetter(component));
	}
	
	public JFileContentManipulator(final FileSystemInterface fsi, final Localizer localizer, final InputStreamGetter getterIn, final OutputStreamGetter getterOut) throws NullPointerException, IllegalArgumentException {
		if (fsi == null) {
			throw new NullPointerException("File system interface can't be null");
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null or empty");
		}
		else if (getterIn == null) {
			throw new NullPointerException("Input stream getter can't be null");
		}
		else if (getterOut == null) {
			throw new NullPointerException("Output stream getter can't be null");
		}
		else {
			this.fsi = fsi;
			this.localizer = localizer;
			this.getterIn = getterIn;
			this.getterOut = getterOut;
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void close() throws IOException {
		if (wasChanged) {
			if (saveFile()) {
				clearModificationFlag();
			}
		}
	}
	
	public boolean newFile() throws IOException {
		return newFile(DUMMY);
	}
	
	public boolean newFile(final ProgressIndicator progress) throws IOException {
		if (wasChanged) {
			if (!saveFile(progress)) {
				return false;
			}
		}
		try(final OutputStream	os = getterOut.getContent()) {
			os.flush();
			clearModificationFlag();
			return true;
		}
	}
	
	public boolean openFile() throws IOException {
		return openFile(DUMMY);
	}

	public boolean openFile(final ProgressIndicator progress) throws IOException {
		if (wasChanged) {
			if (!saveFile(progress)) {
				return false;
			}
		}
		try{for (String item : JFileSelectionDialog.select((Dialog)null, localizer, fsi, JFileSelectionDialog.OPTIONS_FOR_OPEN | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS)) {
				try(final FileSystemInterface	current = fsi.clone().open(item);
					final InputStream			is = current.read();
					final OutputStream			os = getterOut.getContent()) {

					Utils.copyStream(is, os);
					clearModificationFlag();
					currentName = item;
					return true;
				}
			}
			return false;
		} catch (LocalizationException e) {
			throw new IOException(e);
		}
	}

	public boolean saveFile() throws IOException {
		return saveFile(DUMMY);
	}
	
	public boolean saveFile(final ProgressIndicator progress) throws IOException {
		if (currentName.isEmpty()) {
			return saveFileAs(progress);
		}
		else {
			try(final FileSystemInterface	current = fsi.clone().open(currentName);
				final InputStream			is = getterIn.getContent();
				final OutputStream			os = current.write()) {

				Utils.copyStream(is, os);
				clearModificationFlag();
				return true;
			}
		}
	}

	public boolean saveFileAs() throws IOException {
		return saveFileAs(DUMMY);
	}

	public boolean saveFileAs(final ProgressIndicator progress) throws IOException {
		try{for (String item : JFileSelectionDialog.select((Dialog)null, localizer, fsi, JFileSelectionDialog.OPTIONS_FOR_SAVE | JFileSelectionDialog.OPTIONS_ALLOW_MKDIR | JFileSelectionDialog.OPTIONS_ALLOW_DELETE | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE)) {
				try(final FileSystemInterface	current = fsi.clone().open(item);
					final InputStream			is = current.read();
					final OutputStream			os = getterOut.getContent()) {
	
					Utils.copyStream(is, os);
					clearModificationFlag();
					currentName = item;
					return true;
				}
			}
			return false;
		} catch (LocalizationException e) {
			throw new IOException(e);
		}
	}
	
	public String getCurrentNameOfTheFile() {
		if (currentName.isEmpty() || "/".equals(currentName)) {
			return "";
		}
		else {
			return currentName.substring(currentName.lastIndexOf('/')+1);
		}
	}

	public String getCurrentPathOfTheFile() {
		return currentName;
	}
	
	public void setModificationFlag() {
		wasChanged = true;
	}
	
	public void clearModificationFlag() {
		wasChanged = false;
	}

	static InputStreamGetter buildInputStreamGetter(final JTextComponent component) {
		if (component == null) {
			throw new NullPointerException("Text component can't be null"); 
		}
		else {
			return new InputStreamGetter() {
				@Override
				public InputStream getContent() {
					return new ByteArrayInputStream(component.getText().getBytes());
				}
			};
		}
	}

	static OutputStreamGetter buildOutputStreamGetter(final JTextComponent component) {
		if (component == null) {
			throw new NullPointerException("Text component can't be null"); 
		}
		else {
			return new OutputStreamGetter() {
				@Override
				public OutputStream getContent() {
					return new ByteArrayOutputStream() {
						@Override
						public void close() throws IOException{
							super.close();
							component.setText(this.toString());
						}
					};
				}
			};
		}
	}
}
