package chav1961.purelib.json.intern;

import java.io.CharArrayReader;
import java.io.IOException;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.CharUtils.ArgumentType;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.growablearrays.InOutGrowableCharArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.json.JsonRPCFactory.Transport;
import chav1961.purelib.json.JsonSerializer;
import chav1961.purelib.streams.JsonStaxParser;

public class BasicRPCListener<T> implements Transport {
	private static final char[]				NULL = "null".toCharArray();
	private static final char[][]			JSON_PAIRS = {"{}".toCharArray(),"[]".toCharArray()};
	
	protected final SyntaxTreeInterface<?>	tree;
	protected final T						serverProcessor;
	
	BasicRPCListener(final SyntaxTreeInterface<?> tree, final T serverProcessor) {
		this.tree = tree;
		this.serverProcessor = serverProcessor;
		tree.placeName(Constants.JSONRPC_NAME,Constants.KW_JSONRPC,null);
		tree.placeName(Constants.METHOD_NAME,Constants.KW_METHOD,null);
		tree.placeName(Constants.PARAMS_NAME,Constants.KW_PARAMS,null);
		tree.placeName(Constants.ID_NAME,Constants.KW_ID,null);
		tree.placeName(Constants.RESULT_NAME,Constants.KW_RESULT,null);
		tree.placeName(Constants.ERROR_NAME,Constants.KW_ERROR,null);
		tree.placeName(Constants.CODE_NAME,Constants.KW_CODE,null);
		tree.placeName(Constants.MESSAGE_NAME,Constants.KW_MESSAGE,null);
		tree.placeName(Constants.DATA_NAME,Constants.KW_DATA,null);
	}
	
	@Override
	public int process(final GrowableCharArray<?> in, final InOutGrowableCharArray out) {
		in.append('\0');
		final char[]	source = in.toPlain().toArray();
		int				from = CharUtils.skipBlank(source,0,false);
		
		try{if (source[from] == '[') {
				do {from = CharUtils.skipBlank(source,parseRequest(source,from+1,out),false);
				} while (source[from] == ',');
				
				if (source[from] == ']') {
					from++;
					return 0;
				}
				else {
					throw new JsonRPCException(Constants.ERR_PARSE_ERROR,"Missing ']' at the end of request");
				}
			}
			else if (source[from] == '{') {
				from = parseRequest(source,from,out);
				return 0;
			}
			else {
				throw new JsonRPCException(Constants.ERR_PARSE_ERROR,"Neither '[' nor '{' at the beginning of request");
			}
		} catch (SyntaxException exc) {
			return error(out,Constants.ERR_PARSE_ERROR,exc.getLocalizedMessage());
		} catch (JsonRPCException exc) {
			return error(out,exc.errorCode,exc.getLocalizedMessage());
		} catch (IOException e) {
			return error(out,Constants.ERR_PARSE_ERROR,e.getLocalizedMessage());
		}
	}

	private int error(final InOutGrowableCharArray out, final int errCode, final String message) {
		// TODO Auto-generated method stub
		try{out.append("{\"").append(tree,Constants.KW_JSONRPC).append("\":\"").append(Constants.VERSION).append("\",\"")
				.append(tree,Constants.KW_ERROR).append("\":{\"").append(tree,Constants.KW_CODE).append("\":")
				.print(errCode).append(",\"").append(tree,Constants.KW_MESSAGE).append("\":\"")
				.append(CharUtils.escapeStringContent(message)).append("\"},\"").append(tree,Constants.KW_ID)
				.append(":null}");
			return errCode;
		} catch (PrintingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

	private int parseRequest(final char[] source, final int start, final InOutGrowableCharArray out) throws SyntaxException, JsonRPCException, IOException {
		// TODO Auto-generated method stub
		final Object[]		lex = new Object[1];
		final int[]			location = new int[2];
		MethodCallWrapper	mcw = null;
		Object				requestId = null;
		JsonStaxParser		paramParser = null;
		int					from = CharUtils.skipBlank(source,start,false);
		long				nameId;
		boolean				jsonRpcDetected = false, methodDetected = false, idDetected = false, positionalParameters = true;
		
		if (source[from] == '{') {
			try {
				do {from = CharUtils.skipBlank(source,from+1,false);
					if (source[from] == '\"') {
						from = CharUtils.parseUnescapedString(source,from+1,'\"',true,location);
						if (source[from] == '\"') {
							switch ((int)tree.seekName(source,location[0],location[1])) {
								case Constants.KW_JSONRPC 	:
									if (jsonRpcDetected) {
										throw new SyntaxException(0,from,"Duplicate field ["+Constants.JSONRPC_NAME+"]");
									}
									else {
										from = CharUtils.extract(source, from + 1, lex, ':', ArgumentType.simpleTerminatedString);
										if (lex[0] != null && Constants.VERSION.equals(lex[0])) {
											jsonRpcDetected = true;
											break;
										}
										else {
											throw new SyntaxException(0,from,"Unsupported version number ["+lex[0]+"]");
										}
									}
								case Constants.KW_METHOD	:
									if (methodDetected) {
										throw new SyntaxException(0,from,"Duplicate field ["+Constants.METHOD_NAME+"]");
									}
									else {
										from = CharUtils.extract(source, from + 1, lex, ':', ArgumentType.simpleTerminatedString);
										if (lex[0] != null && (nameId = tree.seekName(lex[0].toString())) >= 0) {
											final Object	cargo = tree.getCargo(nameId);
											
											if (cargo instanceof MethodCallWrapper) {
												mcw = (MethodCallWrapper)cargo;
												methodDetected = true;
												break;
											}
											else {
												throw new SyntaxException(0,from,"Unknown method name ["+lex[0]+"]");
											}
										}
										else {
											throw new SyntaxException(0,from,"Illegal method name ["+lex[0]+"]");
										}
									}
								case Constants.KW_ID		:
									if (idDetected) {
										throw new SyntaxException(0,from,"Duplicate field ["+Constants.ID_NAME+"]");
									}
									else {
										from = CharUtils.skipBlank(source,from+1,false);
										if (source[from] == ':') {
											from = CharUtils.skipBlank(source,from+1,false);
											if (source[from] == '\"') {
												from = CharUtils.parseUnescapedString(source,from+1,'\"',true,location);
												requestId = new String(source,location[0],location[1]-location[0]);
												if (source[from] == '\"') {
													from++;
												}
												else {
													throw new SyntaxException(0,from,"Unpaired double quote");
												}
											}
											else if (CharUtils.compare(source,from,NULL)) {
												requestId = null;
											}
											else if (source[from] >= '0' && source[from] <= '9') {
												from = CharUtils.parseInt(source,from,location,true);
												requestId = Integer.valueOf(location[0]);
											}
											else {
												throw new SyntaxException(0,from,"Invalid ID value");
											}
											idDetected = true;
										}
										else {
											throw new SyntaxException(0,from,"Missing colon");
										}
										break;
									}
								case Constants.KW_PARAMS	:
									if (idDetected) {
										throw new SyntaxException(0,from,"Duplicate field ["+Constants.PARAMS_NAME+"]");
									}
									else {
										from = CharUtils.skipBlank(source,from+1,false);
										if (source[from] == ':') {
											from = CharUtils.skipBlank(source,from+1,false);
											if (source[from] == '[') {
												location[0] = from;
												location[1] = from = CharUtils.skipNested(source,from+1,'\"',JSON_PAIRS,false);
												paramParser = new JsonStaxParser(new CharArrayReader(source,location[0],location[1]-location[0]));
												positionalParameters = true;
											}
											else if (source[from] == '{') {
												location[0] = from;
												location[1] = from = CharUtils.skipNested(source,from+1,'\"',JSON_PAIRS,false);
												paramParser = new JsonStaxParser(new CharArrayReader(source,location[0],location[1]-location[0]));
												positionalParameters = false;
											}
											else {
												throw new SyntaxException(0,from,"Neither '[' nor '{' in the parameter list");
											}
										}
										else {
											throw new SyntaxException(0,from,"Missing colon");
										}
										break;
									}
								default :
									throw new SyntaxException(0,from,"Unknown field name ["+new String(source,location[0],location[1]-location[0])+"] in the request");
							}
						}
						else {
							throw new SyntaxException(0,from,"Unpaired double quote");
						}
					}
					from = CharUtils.skipBlank(source,from,false);
				} while (source[from] == ',');
				
				if  (source[from] == '}') {
					from++;
					if (!jsonRpcDetected) {
						throw new SyntaxException(0,from,"Missing rpc version in the request");
					}
					else if (!methodDetected) {
						throw new SyntaxException(0,from,"Missing method name in the request");
					}
					else if (mcw.hasParameters() && paramParser == null){
						throw new SyntaxException(0,from,"Method name requests parameters, but no any parameters in the request");
					}
					else if (!mcw.hasParameters() && paramParser != null){
						throw new SyntaxException(0,from,"Method name doesn't request parameters, but parameters are typed in the request");
					}
					else {
						try(final MethodCaller<Object>	caller = mcw.<Object>getMethodCaller()) {
							if (positionalParameters) {
								caller.parsePositionalParameters(paramParser);
							}
							else {
								caller.parseNamedParameters(paramParser);
							}
							final Object	result = caller.callMethod();
							
							caller.printResult(result,requestId,null);
						} catch (ContentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else {
					throw new SyntaxException(0,from,"Missing '}' at the end of request");
				}
			} finally {
				if (paramParser != null) {
					paramParser.close();
					paramParser = null;
				}
			}
			return from;
		}
		else {
			throw new SyntaxException(0,from,"Missing '{' at the beginning of request");
		}
	}

	private static class JsonRPCException extends ContentException {
		private static final long 	serialVersionUID = 1L;
		
		final int					errorCode;
		
		public JsonRPCException(final int errorCode) {
			super();
			this.errorCode = errorCode;
		}

		public JsonRPCException(final int errorCode, final String message, final Throwable cause) {
			super(message, cause);
			this.errorCode = errorCode;
		}

		public JsonRPCException(final int errorCode, final String message) {
			super(message);
			this.errorCode = errorCode;
		}

		public JsonRPCException(final int errorCode, final Throwable cause) {
			super(cause);
			this.errorCode = errorCode;
		}
	}

	public static class MethodCallWrapper {
		public boolean hasParameters() {
			return false;
		}

		public <T> MethodCaller<T> getMethodCaller() {
			return null;
		}
	}
}
