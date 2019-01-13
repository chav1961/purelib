package chav1961.purelib.nanoservice.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.net.httpserver.*;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;

public interface MethodExecutor {
	@SuppressWarnings("restriction")
	int execute(QueryType type, char[] path, char[] query, Headers requestHeaders, Headers responseHeaders, InputStream is, OutputStream os) throws IOException, ContentException, FlowException, EnvironmentException;
}
