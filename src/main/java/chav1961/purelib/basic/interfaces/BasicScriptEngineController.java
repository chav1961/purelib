package chav1961.purelib.basic.interfaces;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * <p>This interface extends functionality of the standard Java {@linkplain ScriptEngine} interface.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public interface BasicScriptEngineController {
	/**
	 * <p>Execute precompiled content of the Script engine.</p>
	 * @param parameters parameters to pass to the content
	 * @return return value
	 * @throws ScriptException on any script exceptions
	 */
	Object execute(String... parameters) throws ScriptException;
	
	/**
	 * <p>Upload pre-compiled content from the Script engine</p>
	 * @param target URI to store content to
	 * @throws ScriptException on any exceptions
	 */
	void upload(final URI target) throws ScriptException;
	
	/**
	 * <p>Upload pre-compiled content from the Script engine</p>
	 * @param target stream to store content to
	 * @throws ScriptException on any exceptions
	 */
	void upload(final OutputStream target) throws ScriptException;
	
	/**
	 * <p>Download pre-compiled content to the Script engine</p>
	 * @param source URI to get content from
	 * @throws ScriptException on any exceptions
	 */
	void download(final URI source) throws ScriptException;
	
	/**
	 * <p>Download pre-compiled content to the Script engine</p>
	 * @param source stream to get content from
	 * @throws ScriptException on any exceptions
	 */
	void download(final InputStream source) throws ScriptException;
}
