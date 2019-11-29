package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.List;

import javax.activation.MimeType;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * <p>This class implements basic functionality for the {@link ScriptEngineFactory} interface. It's functionality is oriented to use with the
 * {@linkplain AbstractScriptEngine} class in this package. The good examples of the child implementation for the given class is 
 * {@link AsmScriptEngineFactory} class in this package. All children, that extends this class, must register them as an SPI service. File
 * to register is <b>META-INF/services/javax.script.ScriptEngineFactory</b></p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see ScriptEngineFactory
 * @see AsmScriptEngineFactory
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.3
 */

public abstract class AbstractScriptEngineFactory implements ScriptEngineFactory {
	private final String 			engineName;
	private final String 			engineVersion;
	private final List<String> 		engineMIMEs;
	private final String 			language;
	private final String 			languageVersion;
	private final List<String> 		languageSynonyms;

	protected AbstractScriptEngineFactory(final String engineName, final String engineVersion, final List<MimeType> engineMIMEs, final String language, final String languageVersion, final List<String> languageSynonyms) {
		if (engineName == null || engineName.isEmpty()) {
			throw new IllegalArgumentException("Engine name can't be null or empty"); 
		}
		else if (engineVersion == null || engineVersion.isEmpty()) {
			throw new IllegalArgumentException("Engine version can't be null or empty"); 
		}
		else if (engineMIMEs == null || engineMIMEs.size() == 0) {
			throw new IllegalArgumentException("Engine MIMEs can't be null or empty list"); 
		}
		if (language == null || language.isEmpty()) {
			throw new IllegalArgumentException("Language can't be null or empty"); 
		}
		else if (languageVersion == null || languageVersion.isEmpty()) {
			throw new IllegalArgumentException("Language version can't be null or empty"); 
		}
		else if (languageSynonyms == null || languageSynonyms.size() == 0) {
			throw new IllegalArgumentException("Language synonyms can't be null or empty list"); 
		}
		else {
			this.engineName = engineName;
			this.engineVersion = engineVersion;
			this.engineMIMEs = new ArrayList<>();
			this.language = language;
			this.languageVersion = languageVersion; 
			this.languageSynonyms = languageSynonyms;
			
			for (MimeType item : engineMIMEs) {
				this.engineMIMEs.add(item.toString());
			}
		}
	}

	@Override public abstract String getMethodCallSyntax(final String obj, final String m, final String... args);
	@Override public abstract String getOutputStatement(final String toDisplay);
	@Override public abstract String getProgram(final String... statements);
	@Override public abstract ScriptEngine getScriptEngine();
	
	@Override
	public String getEngineName() {
		return engineName;
	}

	@Override
	public String getEngineVersion() {
		return engineVersion;
	}

	@Override
	public List<String> getExtensions() {
		return new ArrayList<>();
	}

	@Override
	public List<String> getMimeTypes() {
		return engineMIMEs;
	}

	@Override
	public List<String> getNames() {
		return languageSynonyms;
	}

	@Override
	public String getLanguageName() {
		return language;
	}

	@Override
	public String getLanguageVersion() {
		return languageVersion;
	}

	@Override
	public Object getParameter(final String key) throws IllegalArgumentException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key parameter can't be null or empty");
		}
		else {
			switch (key) {
				case ScriptEngine.ENGINE : return getEngineName();
				case ScriptEngine.ENGINE_VERSION : return getEngineVersion();
				case ScriptEngine.LANGUAGE : return getLanguageName();
				case ScriptEngine.LANGUAGE_VERSION : return getLanguageVersion();
				case ScriptEngine.NAME : return getLanguageName();
				default : throw new UnsupportedOperationException("Key ["+key+"] is not supported yet"); 
			}
		}
	}
}