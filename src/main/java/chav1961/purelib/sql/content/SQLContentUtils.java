package chav1961.purelib.sql.content;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.sql.RsMetaDataElement;
import chav1961.purelib.sql.SQLUtils;

public class SQLContentUtils {
	public static final String			OPTION_ENCODING = "encoding";
	public static final String			OPTION_SEPARATOR = "separator";
	public static final String			OPTION_FIRST_LINE_ARE_NAMES = "firstlinearenames";
	public static final String			OPTION_ROW_TAG = "rowtag";

	public static final String			DEFAULT_OPTION_ENCODING = "UTF-8";
	
	static RsMetaDataElement[] buildMetadataFromQueryString(final String query, final Hashtable<String,String[]> includes) throws SyntaxException {
		if (query == null || query.isEmpty()) {
			throw new IllegalArgumentException("Query string can't be null or empty");
		}
		else if (includes == null) {
			throw new NullPointerException("Includes list can't be null");
		}
		else {
			final List<String>					names = new ArrayList<>();
			final Hashtable<String,String[]>	parsed = Utils.parseQuery(query);
			
			for (String item : CharUtils.split(query,'&')) {	// To avoid loosing of fields ordering
				final int	eqPlace;
				
				if ((eqPlace = item.indexOf('=')) > 0) {
					final String	key = item.substring(0,eqPlace).trim(); 
							
					if (includes.containsKey(key)) {
						names.add(key);
					}
				}
			}
			
			if (names.size() == 0) {
				throw new SyntaxException(0,0,"Query string ["+query+"] doesn't contain any descriptions pointed in includes list ["+includes.keySet()+"]"); 
			}
			else {
				final RsMetaDataElement[]	result = new RsMetaDataElement[names.size()];
				
				for (int index = 0; index < result.length; index++) {
					result[index] = SQLUtils.prepareMetadataElement(names.get(index), parsed.get(names.get(index))[0].trim());
				}
				return result;
			}
		}
	}
	
	static SubstitutableProperties extractOptions(final Hashtable<String,String[]> source, final Hashtable<String,String[]> excludes) {
		final Properties	props = new Properties();
		
		for (Entry<String, String[]> item : source.entrySet()) {
			if (!excludes.containsKey(item.getKey())) {
				props.setProperty(item.getKey(),item.getValue()[0]);
			}
		}
		return new SubstitutableProperties(props);
	}	
}
