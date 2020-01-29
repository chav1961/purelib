package chav1961.purelib.json.intern;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.growablearrays.InOutGrowableCharArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.json.JsonRPCFactory.Transport;

public class BasicRPCListener<T> implements Transport {
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
		
		if (source[from] == '[') {
			do {from = CharUtils.skipBlank(source,parseRequest(source,from+1),false);
			} while (source[from] == ',');
			
			if (source[from] == ']') {
				from++;
				return 0;
			}
			else {
				return error(out,Constants.ERR_PARSE_ERROR,"Missing ']' at the end of request");
			}
		}
		else if (source[from] == '{') {
			from = parseRequest(source,from);
			return 0;
		}
		else {
			return error(out,Constants.ERR_PARSE_ERROR,"Neither '[' nor '{' at the beginning of request");
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

	private int parseRequest(final char[] source, final int from) {
		// TODO Auto-generated method stub
		return 0;
	}
}
