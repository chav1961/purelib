package chav1961.purelib.json.intern;

public class Constants {
	public static final int		OK = 0;
	public static final int		ERR_PARSE_ERROR = -32700;
	public static final int		ERR_REQUEST_ERROR = -32600;
	public static final int		ERR_UNKNOWN_METHOD = -32601;
	public static final int		ERR_INVALID_PARAMETERS = -32602;
	public static final int		ERR_INTERNAL_ERROR = -32603;

	public static final int		KW_JSONRPC = 1;
	public static final int		KW_METHOD = 2;
	public static final int		KW_PARAMS = 3;
	public static final int		KW_ID = 4;
	public static final int		KW_RESULT = 5;
	public static final int		KW_ERROR = 6;
	public static final int		KW_CODE = 7;
	public static final int		KW_MESSAGE = 8;
	public static final int		KW_DATA = 9;

	public static final String	VERSION = "2.0"; 
	
	static final String			JSONRPC_NAME = "jsonprc";
	static final String			METHOD_NAME = "method";
	static final String			PARAMS_NAME = "params";
	static final String			ID_NAME = "id";
	static final String			RESULT_NAME = "result";
	static final String			ERROR_NAME = "error";
	static final String			CODE_NAME = "code";
	static final String			MESSAGE_NAME = "message";
	static final String			DATA_NAME = "data";
	
}
